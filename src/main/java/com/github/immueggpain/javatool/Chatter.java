package com.github.immueggpain.javatool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "udp chatter which send/recv udp packets", name = "chat", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class Chatter implements Callable<Void> {

	@Option(names = { "-p", "--port" }, required = true, description = "local listening port")
	public int localPort;

	@Option(names = { "-w", "--password" }, required = true, description = "password for AES encryption")
	public String pswdStr;

	public static final Gson gson = new Gson();

	@SuppressWarnings("resource")
	@Override
	public Void call() throws Exception {
		// convert password to aes key
		byte[] bytes = pswdStr.getBytes(StandardCharsets.UTF_8);
		byte[] byteKey = new byte[16];
		System.arraycopy(bytes, 0, byteKey, 0, Math.min(byteKey.length, bytes.length));
		SecretKeySpec secretKey = new SecretKeySpec(byteKey, "AES");
		// we use 2 ciphers because we want to support encrypt/decrypt full-duplex
		String transformation = "AES/GCM/PKCS5Padding";
		Cipher encrypter = Cipher.getInstance(transformation);
		Cipher decrypter = Cipher.getInstance(transformation);

		InetAddress allbind_addr = InetAddress.getByName("0.0.0.0");
		DatagramSocket sclient_s = new DatagramSocket(localPort, allbind_addr);

		// recving
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				byte[] recvBuf = new byte[4096];
				DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
				while (true) {
					p.setData(recvBuf);
					sclient_s.receive(p);
					byte[] decrypted = Util.decrypt(decrypter, secretKey, p.getData(), p.getOffset(), p.getLength());
					String clientAskStr = new String(decrypted, StandardCharsets.UTF_8);
					System.out.println("from " + p.getSocketAddress() + ": " + clientAskStr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// sending
		byte[] recvBuf = new byte[4096];
		DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
		Scanner stdin = new Scanner(System.in);
		while (true) {
			String ip = stdin.next();
			int port = stdin.nextInt();
			String content = stdin.nextLine();

			byte[] serverReplyBytes = content.getBytes(StandardCharsets.UTF_8);
			byte[] serverReplyEncrypted = Util.encrypt(encrypter, secretKey, serverReplyBytes, 0,
					serverReplyBytes.length);

			p.setAddress(InetAddress.getByName(ip));
			p.setPort(port);
			p.setData(serverReplyEncrypted);
			sclient_s.send(p);
		}
	}
}
