package com.github.immueggpain.javatool;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Server produce random data", name = "randsvr", mixinStandardHelpOptions = true)
public class RandomServer implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, description = "SVR PORT, ...")
	public int server_port;

	@Spec
	CommandSpec spec;

	@Override
	public Void call() throws Exception {
		spec.commandLine().usage(System.out);
		ExecutorService executor = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(server_port, 50, InetAddress.getByName("0.0.0.0"))) {
			System.out.println("listened on port " + server_port);
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
