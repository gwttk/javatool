package com.github.immueggpain.javatool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "echo server", name = "svrecho", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class ServerEcho implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		try (ServerSocket ss = new ServerSocket(serverPort)) {
			while (true) {
				Socket s = ss.accept();
				handle(s);
			}
		}
	}

	private void handle(Socket s) {
		try {
			s.setTcpNoDelay(true);
			IOUtils.copy(s.getInputStream(), s.getOutputStream(), 1024 * 1024);
		} catch (Exception e) {
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
