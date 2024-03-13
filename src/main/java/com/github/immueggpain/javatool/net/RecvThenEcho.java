package com.github.immueggpain.javatool.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import com.github.immueggpain.javatool.Launcher;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "recv udp pkt then echo back", name = "echo", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class RecvThenEcho implements Callable<Void> {

	@Option(names = { "-l", "--listen-addr" }, required = true, description = "listening address")
	public String listenAddr;

	@Option(names = { "-p", "--listen-port" }, required = true, description = "listening port")
	public int listenPort;

	@Override
	public Void call() throws Exception {
		try (DatagramSocket s = new DatagramSocket(listenPort, InetAddress.getByName(listenAddr))) {
			byte[] buf = new byte[2000];
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			while (true) {
				p.setData(buf);
				s.receive(p);
				System.out.println(new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8));

				s.send(p);
			}
		}
	}

}
