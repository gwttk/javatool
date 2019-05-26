package com.github.immueggpain.javatool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.immueggpain.javatool.Util.ClientAsk;
import com.github.immueggpain.javatool.Util.ServerReply;
import com.google.gson.Gson;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "udp server which returns infomation about connector's ip and port", name = "infosvr",
		mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class ConnInfoServer implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Option(names = { "-w", "--password" }, required = true, description = "password for AES encryption")
	public String pswdStr;

	public static final Gson gson = new Gson();

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

		// setup sockets
		InetAddress allbind_addr = InetAddress.getByName("0.0.0.0");
		try (DatagramSocket sclient_s = new DatagramSocket(serverPort, allbind_addr);
				DatagramSocket sclient_s2 = new DatagramSocket()) {

			// recv packets and reply
			byte[] recvBuf = new byte[4096];
			DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
			while (true) {
				p.setData(recvBuf);
				sclient_s.receive(p);
				byte[] decrypted = Util.decrypt(decrypter, secretKey, p.getData(), p.getOffset(), p.getLength());
				String clientAskStr = new String(decrypted, StandardCharsets.UTF_8);
				ClientAsk clientAsk = gson.fromJson(clientAskStr, ClientAsk.class);
				System.out.println("from " + p.getSocketAddress() + " " + gson.toJson(clientAsk));

				// making reply
				ServerReply serverReply = new Util.ServerReply();
				serverReply.id = "re: " + clientAsk.id;
				serverReply.address = p.getAddress().getHostAddress();
				serverReply.port = p.getPort();
				String serverReplyStr = gson.toJson(serverReply);
				byte[] serverReplyBytes = serverReplyStr.getBytes(StandardCharsets.UTF_8);
				byte[] serverReplyEncrypted = Util.encrypt(encrypter, secretKey, serverReplyBytes, 0,
						serverReplyBytes.length);

				// send reply
				p.setData(serverReplyEncrypted);
				if (clientAsk.id.equals("please send to")) {
					p.setPort(clientAsk.port);
				} else {
					sclient_s2.send(p);
					System.out.println(
							sclient_s2.getLocalSocketAddress() + " to " + p.getSocketAddress() + " " + serverReplyStr);
				}
				sclient_s.send(p);
				System.out.println(
						sclient_s.getLocalSocketAddress() + " to " + p.getSocketAddress() + " " + serverReplyStr);
			}
		}
	}

}
