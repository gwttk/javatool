package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "find timeout of NAT router", name = "clttcpnatto", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ClientTcpFindNatTimeout implements Callable<Void> {

	@Option(names = { "-s", "--server-addr" }, required = true, description = "server address")
	public String[] serverAddrs;

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		

		return null;
	}

}
