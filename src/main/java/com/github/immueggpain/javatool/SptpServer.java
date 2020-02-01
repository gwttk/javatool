package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Smartproxy Tunnel Protocol Server", name = "sptpsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class SptpServer implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	public static class Stream{
		
	}
	
	

	@Override
	public Void call() throws Exception {
		return null;
	}

	public void send(byte[] data, int streamID) {

	}

	public void recv(byte[] data, int streamID) {

	}

	public int accept() {
		return 1;
	}

}
