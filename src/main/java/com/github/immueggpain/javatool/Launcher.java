package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(description = "Toolkit written in java.", name = "javatool", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR,
		subcommands = { HelpCommand.class, Hash.class, RandomServer.class, ConnInfoServer.class, ConnInfoClient.class,
				Chatter.class, UdpSend.class, UdpRecv.class, ShellExecute.class, SpeedTestClient.class,
				SpeedTestServer.class, Base64.class, HttpPost.class, HttpSend.class, SyncFiles.class })
public class Launcher implements Callable<Void> {

	public static final String VERSTR = "1.2.0";

	public static void main(String[] args) {
		new CommandLine(new Launcher()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
	}

	@Override
	public Void call() throws Exception {
		CommandLine.usage(this, System.out);
		return null;
	}

}
