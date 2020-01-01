package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Start an FTP server.", name = "ftpsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class FtpServe implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(2221);
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		// start the server
		FtpServer server = serverFactory.createServer();
		server.start();
		return null;
	}

}
