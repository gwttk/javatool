package com.github.immueggpain.javatool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send files between PCs and phones", name = "sync", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class SyncFiles implements Callable<Void> {

	@Option(names = { "-p", "--port" }, required = false, description = "listening port")
	public int listenPort = 23333;

	@Option(names = { "-w", "--password" }, required = true, description = "password")
	public String password;

	@ArgGroup(exclusive = true, multiplicity = "1")
	public FromTo fromto;

	public static class FromTo {
		@Option(names = { "-f", "--from" }, required = true, description = "sync from a file or dir")
		public Path fromFile;

		@Option(names = { "-t", "--to" }, required = true, description = "sync to a file or dir")
		public Path toFile;
	}

	@Override
	public Void call() throws Exception {
		if (fromto.fromFile != null) {
			System.out.println("sync from: " + fromto.fromFile);
			serveFiles();
		}
		if (fromto.toFile != null) {
			System.out.println("sync to: " + fromto.toFile);
			getFiles();
		}
		return null;
	}

	private Gson gson = new Gson();

	private static class BeaconPkt {
		public String password;
	}

	public static class WirePkt {
		public long binLength = 0;
		public WirePktType type;

		public List<String> fileList;

		public String queryPath;
		public String fileMd5;
		public long fileSize;
	}

	public enum WirePktType {
		LIST, FILEINFO, FILECONTENT, DONE, CLOSESERVER
	}

	private void getFiles() throws IOException {
		InetSocketAddress peerAddr = queryBeacon();
		System.out.println("found file serve beacon: " + peerAddr);
		Socket s = new Socket(peerAddr.getAddress(), peerAddr.getPort());
		DataInputStream is = new DataInputStream(s.getInputStream());
		DataOutputStream os = new DataOutputStream(s.getOutputStream());

		// query file list
		WirePkt query = new WirePkt();
		query.type = WirePktType.LIST;
		os.writeUTF(gson.toJson(query));
		WirePkt reply = gson.fromJson(is.readUTF(), WirePkt.class);

		for (ListIterator<String> iterator = reply.fileList.listIterator(); iterator.hasNext();) {
			String filePath = iterator.next();

			Path localFile = fromto.toFile.resolve(filePath);
			// query file info
			WirePkt queryFI = new WirePkt();
			queryFI.type = WirePktType.FILEINFO;
			queryFI.queryPath = filePath;
			os.writeUTF(gson.toJson(queryFI));
			WirePkt replyFI = gson.fromJson(is.readUTF(), WirePkt.class);

			// if file exists, either it's ok, otherwise delete it
			if (Files.exists(localFile)) {
				// check md5
				String localMd5 = fileMd5(localFile);
				if (localMd5.equals(replyFI.fileMd5))
					continue;
				else
					FileUtils.deleteQuietly(localFile.toFile());
			}

			// now the file should not exist

			if (replyFI.fileMd5.equals("DIR")) {
				Files.createDirectory(fromto.toFile.resolve(filePath));
			} else {
				// query file content
				WirePkt queryFC = new WirePkt();
				queryFC.type = WirePktType.FILECONTENT;
				queryFC.queryPath = filePath;
				os.writeUTF(gson.toJson(queryFC));
				WirePkt replyFC = gson.fromJson(is.readUTF(), WirePkt.class);
				try (OutputStream fos = Files.newOutputStream(localFile)) {
					IOUtils.copyLarge(is, fos, 0, replyFC.binLength);
				}

				// check md5
				String localMd5 = fileMd5(localFile);
				if (!localMd5.equals(replyFI.fileMd5)) {
					// md5 not same!
					// repeat this iteration of loop
					iterator.previous();
				} else {
					// file is ok
					System.out.println("downloaded " + filePath);
				}
			}
		}

		// done
		System.out.println("sync done");
		WirePkt done = new WirePkt();
		done.type = WirePktType.DONE;
		os.writeUTF(gson.toJson(done));
		s.close();
	}

	private void serveFiles() throws IOException {
		new Thread(this::replyBeacon, "beacon").start();
		ServerSocket ss = new ServerSocket(listenPort);
		acceptLoop: while (true) {
			Socket s = ss.accept();
			DataInputStream is = new DataInputStream(s.getInputStream());
			DataOutputStream os = new DataOutputStream(s.getOutputStream());
			recvLoop: while (true) {
				String queryJson;
				try {
					queryJson = is.readUTF();
				} catch (EOFException e) {
					break;
				}
				WirePkt query = gson.fromJson(queryJson, WirePkt.class);
				IOUtils.toByteArray(is, query.binLength); // currently no use of the bin data
				WirePkt reply = new WirePkt();
				Path queryFile;
				switch (query.type) {
				case LIST:
					reply.type = WirePktType.LIST;
					reply.fileList = listFiles(fromto.fromFile);
					os.writeUTF(gson.toJson(reply));
					break;

				case FILEINFO:
					reply.type = WirePktType.FILEINFO;
					queryFile = fromto.fromFile.resolve(query.queryPath);
					reply.fileMd5 = fileMd5(queryFile);
					reply.fileSize = fileSize(queryFile);
					os.writeUTF(gson.toJson(reply));
					break;

				case FILECONTENT:
					reply.type = WirePktType.FILECONTENT;
					queryFile = fromto.fromFile.resolve(query.queryPath);
					reply.binLength = fileSize(queryFile);
					os.writeUTF(gson.toJson(reply));
					if (reply.binLength > 0) {
						try (InputStream fis = Files.newInputStream(queryFile)) {
							IOUtils.copyLarge(fis, os, 0, reply.binLength);
						}
						System.out.println("uploaded " + query.queryPath);
					}
					break;

				case DONE:
					// break recv loop, close conn
					System.out.println("a client is done");
					break recvLoop;

				case CLOSESERVER:
					// break accept loop, close conn, close listen socket
					s.close();
					break acceptLoop;

				default:
					System.err.println("unknown type: " + query.type);
					break;
				}
			}
			s.close();
		}
		ss.close();
	}

	/** all items are relative to start */
	private List<String> listFiles(Path start) throws IOException {
		Stream<Path> stream = Files.walk(start);
		List<String> list = stream.map(start::relativize).map(Object::toString).collect(Collectors.toList());
		stream.close();
		return list;
	}

	private String fileMd5(Path file) throws IOException {
		if (Files.isDirectory(file))
			return "DIR";
		try (InputStream fis = Files.newInputStream(file)) {
			return DigestUtils.md5Hex(fis);
		}
	}

	private long fileSize(Path file) throws IOException {
		if (Files.isDirectory(file))
			return 0;
		return Files.size(file);
	}

	/** beacon respond to udp broadcast of same passwd */
	private void replyBeacon() {
		try (DatagramSocket s = new DatagramSocket(listenPort)) {
			s.setBroadcast(true);
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			while (true) {
				p.setData(buf);
				s.receive(p);
				System.out.println("recv beacon sig from: " + p.getSocketAddress());
				String jsonStr = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
				System.out.println("beacon sig: " + jsonStr);
				BeaconPkt beaconSig = gson.fromJson(jsonStr, BeaconPkt.class);
				if (password.equals(beaconSig.password)) {
					// passwd is same, reply beacon signal
					p.setData(gson.toJson(beaconSig).getBytes(StandardCharsets.UTF_8));
					s.send(p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * beacon broadcast until find peer's addr
	 */
	private InetSocketAddress queryBeacon() throws IOException {
		try (DatagramSocket s = new DatagramSocket(listenPort)) {
			s.setBroadcast(true);
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			while (true) {
				BeaconPkt beaconSig = new BeaconPkt();
				beaconSig.password = password;
				String jsonStr = gson.toJson(beaconSig);
				p.setData(jsonStr.getBytes(StandardCharsets.UTF_8));
				p.setPort(listenPort);
				p.setAddress(InetAddress.getByName("255.255.255.255"));
				s.send(p);

				// wait for reply, if timeout, loop to send beacon sig again
				p.setData(buf);
				s.setSoTimeout(2000);
				try {
					s.receive(p);
					// maybe it's from self, try again
					if (p.getAddress().equals(InetAddress.getLocalHost()))
						s.receive(p);
				} catch (SocketTimeoutException e) {
					continue;
				}
				gson.fromJson(new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8),
						BeaconPkt.class); // currently no use of reply pkt
				return (InetSocketAddress) p.getSocketAddress();
			}
		}
	}

}
