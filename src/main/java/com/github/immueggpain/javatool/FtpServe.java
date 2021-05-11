package com.github.immueggpain.javatool;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

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
		// the FtpServer uses slf4j, which is an interface
		// i use slf4j-simple as backend
		// slf4j-simple needs to be set loglevel like this:
		Properties props = System.getProperties();
		props.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

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
		ArrayList<Authority> authorities = new ArrayList<>();
		authorities.add(new WritePermission());
		user.setAuthorities(authorities);
		serverFactory.getUserManager().save(user);
		// start the server
		FtpServer server = serverFactory.createServer();
		server.start();
		System.out.println(String.format("ftp server started on port %d", serverPort));
		// don't end main thread
		Thread.sleep(Long.MAX_VALUE);
		return null;
	}

}
