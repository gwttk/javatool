package com.github.immueggpain.javatool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.immueggpain.javatool.Util.ClientAsk;
import com.github.immueggpain.javatool.Util.ServerReply;
import com.google.gson.Gson;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "client to infosvr", name = "infoclt", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ConnInfoClient implements Callable<Void> {

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
		try (DatagramSocket cserver_s = new DatagramSocket()) {

			// making ask
			ClientAsk clientAsk = new ClientAsk();
			clientAsk.address = cserver_s.getLocalAddress().getHostAddress();
			clientAsk.port = cserver_s.getLocalPort();
			String clientAskStr = gson.toJson(clientAsk);
			byte[] clientAskBytes = clientAskStr.getBytes(StandardCharsets.UTF_8);
			byte[] clientAskEncrypted = Util.encrypt(encrypter, secretKey, clientAskBytes, 0, clientAskBytes.length);

			// send ask
			DatagramPacket p = new DatagramPacket(clientAskEncrypted, clientAskEncrypted.length);
			cserver_s.send(p);

			// recv reply
			byte[] recvBuf = new byte[4096];
			p.setData(recvBuf);
			cserver_s.receive(p);
			byte[] decrypted = Util.decrypt(decrypter, secretKey, p.getData(), p.getOffset(), p.getLength());
			String serverReplyStr = new String(decrypted, StandardCharsets.UTF_8);
			ServerReply serverReply = gson.fromJson(serverReplyStr, ServerReply.class);
			System.out.println(gson.toJson(serverReply));
		}

		return null;
	}

}
