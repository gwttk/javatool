package com.github.immueggpain.javatool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "recv udp", name = "udprecv", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class UdpRecv implements Callable<Void> {

	@Option(names = { "-p", "--port" }, required = true, description = "listening udp port")
	public int port;

	@SuppressWarnings("resource")
	@Override
	public Void call() throws Exception {
		InetAddress allbind_addr = InetAddress.getByName("0.0.0.0");
		DatagramSocket s = new DatagramSocket(port, allbind_addr);
		byte[] recvBuf = new byte[4096];
		DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
		while (true) {
			p.setData(recvBuf);
			s.receive(p);
			String clientAskStr = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
			System.out.println(p.getSocketAddress() + ": " + clientAskStr);
		}
	}
}
