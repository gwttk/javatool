package com.github.immueggpain.javatool;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef.INT_PTR;
import com.sun.jna.platform.win32.WinUser;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "ShellExecute open a file on windows", name = "shellexec", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ShellExecute implements Callable<Void> {

	@Option(names = { "-f", "--file" }, required = true, description = "file to open")
	public Path file;

	@Override
	public Void call() throws Exception {
		System.out.println("opening " + file);
		INT_PTR r = Shell32.INSTANCE.ShellExecute(null, null, file.toString(), null, file.getParent().toString(),
				WinUser.SW_SHOWNORMAL);
		System.out.println("first attempt result: " + r);
		if (r.longValue() == 31) {
			r = Shell32.INSTANCE.ShellExecute(null, "openas", file.toString(), null, file.getParent().toString(),
					WinUser.SW_SHOWNORMAL);
			System.out.println("second attempt(OpenAs) result: " + r);
		}
		return null;
	}

}
