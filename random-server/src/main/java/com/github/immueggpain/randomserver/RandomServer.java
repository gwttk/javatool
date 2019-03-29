package com.github.immueggpain.randomserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.immueggpain.randomserver.Launcher.ServerSettings;

public class RandomServer {

	public void run(ServerSettings settings) throws Exception {
		ExecutorService executor = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(settings.server_port, 50, InetAddress.getByName("0.0.0.0"))) {
			System.out.println("listened on port " + settings.server_port);
			while (true) {
				Socket s = ss.accept();
				executor.execute(() -> handleConn(s));
			}
		}
	}

	private void handleConn(Socket s) {
		try {
			OutputStream os = s.getOutputStream();
			SecureRandom rand = new SecureRandom();
			byte[] randomBytes = new byte[1024 * 32];
			while (true) {
				rand.nextBytes(randomBytes);
				os.write(randomBytes);
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
