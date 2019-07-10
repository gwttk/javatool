package com.github.immueggpain.javatool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send files between PCs and phones", name = "sync", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ShareFile implements Callable<Void> {

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
		new Thread(this::beacon, "beacon").start();
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

	private static class WirePkt {
		public long binLength;
		public WirePktType type;

		public List<String> fileList;
	}

	public enum WirePktType {
		LIST, FILEINFO, FILECONTENT
	}

	private void getFiles() {

	}

	private void serveFiles() throws IOException {
		ServerSocket ss = new ServerSocket(listenPort);
		Socket s = ss.accept();
		DataInputStream is = new DataInputStream(s.getInputStream());
		DataOutputStream os = new DataOutputStream(s.getOutputStream());
		while (true) {
			String queryJson = is.readUTF();
			WirePkt query = gson.fromJson(queryJson, WirePkt.class);
			IOUtils.toByteArray(is, query.binLength); // currently no use of the bin data
			switch (query.type) {
			case LIST:
				WirePkt reply = new WirePkt();
				reply.type = WirePktType.LIST;
				reply.fileList = listFiles(fromto.fromFile);
				os.writeUTF(gson.toJson(reply));
				break;

			case FILEINFO:

				break;

			case FILECONTENT:

				break;

			default:
				System.err.println("unknown type: " + query.type);
				break;
			}
		}
	}

	/** all items are relative to start */
	private List<String> listFiles(Path start) throws IOException {
		Stream<Path> stream = Files.walk(start);
		return stream.map(start::relativize).map(Object::toString).collect(Collectors.toList());
	}

	/** beacon respond to udp broadcast of same passwd */
	private void beacon() {
		try (DatagramSocket s = new DatagramSocket(listenPort)) {
			s.setBroadcast(true);
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			while (true) {
				p.setData(buf);
				s.receive(p);
				String jsonStr = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
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

}
