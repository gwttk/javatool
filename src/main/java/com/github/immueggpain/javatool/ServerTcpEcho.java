package com.github.immueggpain.javatool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Server which produces random data", name = "svrtcpecho", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ServerTcpEcho implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int server_port;

	@Spec
	CommandSpec spec;

	@Override
	public Void call() throws Exception {
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
			s.setTcpNoDelay(true);
			OutputStream os = s.getOutputStream();
			InputStream is = s.getInputStream();
			byte[] buf = new byte[1024 * 32];
			while (true) {
				int n = is.read(buf);
				if (n == -1)
					break;
				os.write(buf, 0, n);
				os.flush();
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
