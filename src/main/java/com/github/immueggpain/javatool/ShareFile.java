package com.github.immueggpain.javatool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

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
		public String fromPath;

		@Option(names = { "-t", "--to" }, required = true, description = "sync to a file or dir")
		public String toPath;
	}

	@Override
	public Void call() throws Exception {
		new Thread(this::beacon, "beacon").start();
		if (fromto.fromPath != null) {
			System.out.println("sync from: " + fromto.fromPath);
			serveFiles();
		}
		if (fromto.toPath != null) {
			System.out.println("sync to: " + fromto.toPath);
			getFiles();
		}
		return null;
	}

	private Gson gson = new Gson();

	private static class BeaconPkt {
		public String password;
	}

	private void getFiles() {

	}

	private void serveFiles() throws IOException {
		ServerSocket ss = new ServerSocket(listenPort);
		Socket s = ss.accept();
		DataInputStream is = new DataInputStream(s.getInputStream());
		DataOutputStream os = new DataOutputStream(s.getOutputStream());

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
