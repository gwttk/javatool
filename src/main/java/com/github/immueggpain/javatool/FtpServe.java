package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Start an FTP server.", name = "ftpsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class FtpServe implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		FtpServerFactory serverFactory = new FtpServerFactory();
		FtpServer server = serverFactory.createServer();
		// start the server
		server.start();
		return null;
	}

}
