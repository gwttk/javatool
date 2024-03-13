package com.github.immueggpain.javatool.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import com.github.immueggpain.javatool.Launcher;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "send 1 udp pkt then recv 1 udp pkt", name = "1s1r", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class Send1Recv1 implements Callable<Void> {

	@Option(names = { "-s", "--server-addr" }, required = true, description = "server address")
	public String serverAddr;

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		try (DatagramSocket s = new DatagramSocket()) {
			byte[] buf = "hello my Sparkle!".getBytes(StandardCharsets.UTF_8);
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			p.setAddress(InetAddress.getByName(serverAddr));
			p.setPort(serverPort);
			s.send(p);

			buf = new byte[2000];
			p.setData(buf);
			s.receive(p);
			System.out.println(new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8));
			System.out.println("done!");
		}
		return null;
	}

}
