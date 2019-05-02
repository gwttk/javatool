package com.github.immueggpain.randomserver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Launcher {

	private static final String VERSTR = "0.1.0";

	public static class ServerSettings {
		public int server_port;
	}

	public static void main(String[] args) {
		// in laucher, we dont use log file, just print to console
		// cuz it's all about process input args
		try {
			new Launcher().run(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("use -h to see help");
		}
	}

	private void run(String[] args) throws ParseException {
		// option long names
		String help = "help";
		String server_port = "server_port";

		// define options
		Options options = new Options();
		options.addOption("h", help, false, "print help then exit");
		options.addOption(Option.builder("p").longOpt(server_port).hasArg().desc("server listening port")
				.argName("PORT").build());

		// parse from cmd args
		DefaultParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		// first let's check if it's help
		if (cmd.hasOption(help)) {
			String header = "";
			String footer = "\nPlease report issues at https://github.com/Immueggpain/javatool/issues";

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar random-server-" + VERSTR + ".jar", header, options, footer, true);
			return;
		}

		// run server
		ServerSettings settings = new ServerSettings();
		settings.server_port = Integer.parseInt(cmd.getOptionValue(server_port));
		try {
			new RandomServer().run(settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
