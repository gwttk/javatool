package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send files between PCs and phones", name = "randsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ShareFile implements Callable<Void> {

	@Option(names = { "-p", "--port" }, required = false, description = "listening port")
	public int listenPort = 23333;

	@Option(names = { "-w", "--password" }, required = true, description = "password")
	public String password;

	@Option(names = { "-f", "--from" }, required = true, description = "sync from a file or dir")
	public String fromPath;

	@Option(names = { "-t", "--to" }, required = true, description = "sync to a file or dir")
	public String toPath;

	@Override
	public Void call() throws Exception {
		return null;
	}

}
