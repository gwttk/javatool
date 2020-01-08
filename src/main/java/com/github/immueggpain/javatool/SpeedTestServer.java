package com.github.immueggpain.javatool;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Speed test server", name = "spdsvr", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class SpeedTestServer implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int server_port;

	@Option(names = { "-z", "--buf-size" }, description = "buf size. default is ${DEFAULT-VALUE}")
	public int buf_size = 1024 * 32;

	@Option(names = { "--sndbuf" }, description = "socket send buf size.")
	public int sndbuf_size = 0;

	@Option(names = { "--rcvbuf" }, description = "socket recv buf size.")
	public int rcvbuf_size = 0;

	@Override
	public Void call() throws Exception {
		ExecutorService executor = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(server_port, 50, InetAddress.getByName("0.0.0.0"))) {
			System.out.println("listening on port " + server_port);
			if (rcvbuf_size > 0)
				ss.setReceiveBufferSize(rcvbuf_size);
			System.out.println("protocol is 'speed test v1'");
			while (true) {
				Socket s = ss.accept();
				if (sndbuf_size > 0)
					s.setSendBufferSize(sndbuf_size);
				executor.execute(() -> handleConn(s));
			}
		}
	}

	private void handleConn(Socket s) {
		try {
			System.out.println(String.format("%s connected", s.getRemoteSocketAddress()));
			Random rand = new Random();
			// this is random enough, using time
			OutputStream os = s.getOutputStream();
			DataInputStream is = new DataInputStream(s.getInputStream());
			// client says how many random bytes will be sent
			long bytesLeft = is.readLong();
			System.out.println(String.format("request data size: %s", SpeedTestClient.format1024(bytesLeft, "B")));
			byte[] randomBytes = new byte[buf_size];
			while (bytesLeft > 0) {
				rand.nextBytes(randomBytes);
				int len = (int) Math.min(bytesLeft, randomBytes.length);
				os.write(randomBytes, 0, len);
				bytesLeft -= len;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
