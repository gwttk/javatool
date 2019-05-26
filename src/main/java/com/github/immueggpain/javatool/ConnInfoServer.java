package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "udp server which returns infomation about connector's ip and port", name = "infosvr",
		mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class ConnInfoServer implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int server_port;

	@Override
	public Void call() throws Exception {
		return null;
	}

}
