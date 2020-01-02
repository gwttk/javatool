package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Start an FTP server.", name = "ftpsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class FtpServe implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;
	@Option(names = { "-d", "--home-dir" }, required = true, description = "user home dir")
	public String homeDir;

	@Override
	public Void call() throws Exception {
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(serverPort);
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		// add anonymous user
		BaseUser user = new BaseUser();
		user.setName("anonymous");
		user.setHomeDirectory(homeDir);
		serverFactory.getUserManager().save(user);
		// start the server
		FtpServer server = serverFactory.createServer();
		server.start();
		Thread.sleep(Long.MAX_VALUE);
		return null;
	}

}
