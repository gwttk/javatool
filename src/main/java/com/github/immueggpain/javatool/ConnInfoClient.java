package com.github.immueggpain.javatool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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

	@Option(names = { "-s", "--server-addr" }, required = true, description = "server address")
	public String[] serverAddrs;

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Option(names = { "-w", "--password" }, required = true, description = "password for AES encryption")
	public String pswdStr;

	public static final Gson gson = new Gson();
	private Cipher encrypter;
	private Cipher decrypter;
	private SecretKeySpec secretKey;

	private int outerPort;

	@Override
	public Void call() throws Exception {
		// convert password to aes key
		byte[] bytes = pswdStr.getBytes(StandardCharsets.UTF_8);
		byte[] byteKey = new byte[16];
		System.arraycopy(bytes, 0, byteKey, 0, Math.min(byteKey.length, bytes.length));
		secretKey = new SecretKeySpec(byteKey, "AES");
		// we use 2 ciphers because we want to support encrypt/decrypt full-duplex
		String transformation = "AES/GCM/PKCS5Padding";
		encrypter = Cipher.getInstance(transformation);
		decrypter = Cipher.getInstance(transformation);

		// setup sockets
		try (DatagramSocket cserver_s = new DatagramSocket(); DatagramSocket cserver_s_ctrl = new DatagramSocket();) {
			cserver_s.setSoTimeout(1000 * 10);

			// send ctrl please send to
			{
				ClientAsk clientAsk = new ClientAsk();
				clientAsk.id = "please send to";
				clientAsk.address = "";
				clientAsk.port = cserver_s.getLocalPort();
				sendUdp(clientAsk, cserver_s_ctrl, serverAddrs[0], serverPort);
			}

			// recv naked
			recvUdp(cserver_s);

			// send punch
			{
				ClientAsk clientAsk = new ClientAsk();
				clientAsk.id = "punch";
				clientAsk.address = cserver_s.getLocalAddress().getHostAddress();
				clientAsk.port = cserver_s.getLocalPort();
				sendUdp(clientAsk, cserver_s, serverAddrs[0], serverPort);
			}

			// recv same ip different port
			// recv same ip same port
			recvUdp(cserver_s);
			recvUdp(cserver_s);

			// send ctrl to another server to send to punched
			{
				ClientAsk clientAsk = new ClientAsk();
				clientAsk.id = "please send to";
				clientAsk.address = "";
				clientAsk.port = outerPort;
				sendUdp(clientAsk, cserver_s_ctrl, serverAddrs[1], serverPort);
			}

			// recv different ip
			recvUdp(cserver_s);
		}

		return null;
	}

	private void sendUdp(Object obj, DatagramSocket sender, String addr, int port)
			throws GeneralSecurityException, IOException {
		String clientAskStr = gson.toJson(obj);
		byte[] clientAskBytes = clientAskStr.getBytes(StandardCharsets.UTF_8);
		byte[] clientAskEncrypted = Util.encrypt(encrypter, secretKey, clientAskBytes, 0, clientAskBytes.length);
		DatagramPacket p = new DatagramPacket(clientAskEncrypted, clientAskEncrypted.length);
		p.setAddress(InetAddress.getByName(addr));
		p.setPort(port);
		sender.send(p);
	}

	private void recvUdp(DatagramSocket receiver) throws IOException, GeneralSecurityException {
		try {
			byte[] recvBuf = new byte[4096];
			DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
			receiver.receive(p);
			byte[] decrypted = Util.decrypt(decrypter, secretKey, p.getData(), p.getOffset(), p.getLength());
			String serverReplyStr = new String(decrypted, StandardCharsets.UTF_8);
			ServerReply serverReply = gson.fromJson(serverReplyStr, ServerReply.class);
			outerPort = serverReply.port;
			System.out.println("from " + p.getSocketAddress() + " " + gson.toJson(serverReply));
		} catch (SocketTimeoutException e) {
			System.out.println("recv timed out");
		}
	}

}
