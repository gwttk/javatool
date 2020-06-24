package com.github.immueggpain.javatool;

import java.awt.MouseInfo;
import java.util.concurrent.Callable;

import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.INPUT;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(description = "Move mouse ", name = "mmove", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class MouseMove implements Callable<Void> {

	@Option(names = { "-x", "--dx" }, required = true, description = "mouse move dx")
	public int serverPort;

	@Spec
	CommandSpec spec;

	@Override
	public Void call() throws Exception {
		System.out.println(MouseInfo.getPointerInfo().getLocation());

		WinUser.INPUT input = new WinUser.INPUT();
		input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
		input.input.setType("mi");
		input.input.mi.dx = new LONG(200);
		input.input.mi.dy = new LONG(0);
		input.input.mi.mouseData = new DWORD(0);
		input.input.mi.dwFlags = new DWORD(1);
		input.input.mi.time = new DWORD(0);
		input.input.mi.dwExtraInfo = new ULONG_PTR(0);

		User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] { input }, input.size());

		System.out.println(MouseInfo.getPointerInfo().getLocation());
		return null;
	}

}
