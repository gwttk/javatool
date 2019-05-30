package com.github.immueggpain.javatool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "send udp", name = "udpsend", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class UdpSend implements Callable<Void> {

	@Option(names = { "-p", "--port" }, required = true, description = "target udp port")
	public int port;

	@Option(names = { "-a", "--addr" }, required = true, description = "target host address")
	public String addr;

	@Parameters(index = "0")
	public String content;

	@SuppressWarnings("resource")
	@Override
	public Void call() throws Exception {
		DatagramSocket s = new DatagramSocket();
		byte[] recvBuf = content.getBytes(StandardCharsets.UTF_8);
		DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
		p.setAddress(InetAddress.getByName(addr));
		p.setPort(port);
		s.send(p);
		return null;
	}
}
