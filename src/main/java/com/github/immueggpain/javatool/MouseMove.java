package com.github.immueggpain.javatool;

import java.awt.Robot;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Move mouse ", name = "mmove", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class MouseMove implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Spec
	CommandSpec spec;

	@Override
	public Void call() throws Exception {
		Robot robot = new Robot();
		robot.mouseMove(0, 0);
		return null;
	}

}
