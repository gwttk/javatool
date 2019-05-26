package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Server which produces random data", name = "randsvr", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class Template implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int server_port;

	@Spec
	CommandSpec spec;

	@Override
	public Void call() throws Exception {
		return null;
	}

}
