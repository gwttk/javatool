package com.github.immueggpain.javatool;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.", name = "javatool",
		mixinStandardHelpOptions = true, version = Launcher.VERSTR,
		subcommands = { HelpCommand.class, RandomServer.class })
public class Launcher implements Callable<Void> {

	public static final String VERSTR = "1.0.0";

	@Option(names = { "-k", "--asda" }, description = "SVR PORT, ...")
	public int kkp;

	public static void main(String[] args) {
		// in laucher, we dont use log file, just print to console
		// cuz it's all about process input args
		try {
			new Launcher().run(args);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("use -h to see help");
		}
	}

	private void run(String[] args) throws Throwable {
		CommandLine.call(this, args);
	}

	@Override
	public Void call() throws Exception {
		return null;
	}

}
