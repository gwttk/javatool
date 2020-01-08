package com.github.immueggpain.javatool;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Callable;

import org.apache.commons.math3.util.Precision;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Speed test client", name = "spdclt", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class SpeedTestClient implements Callable<Void> {

	@Option(names = { "-p", "--server-port" }, required = true, description = "server port")
	public int server_port;

	@Option(names = { "-r", "--server-host" }, required = true, description = "server host (ip or domain)")
	public String server_host;

	@Option(names = { "-s", "--data-size" }, required = true, description = "size of data to download in MB")
	public long data_size;

	@Option(names = { "-z", "--buf-size" }, description = "buf size. default is ${DEFAULT-VALUE}")
	public int buf_size = 1024 * 32;

	@Option(names = { "--sndbuf" }, description = "socket send buf size.")
	public int sndbuf_size = 0;

	@Option(names = { "--rcvbuf" }, description = "socket recv buf size.")
	public int rcvbuf_size = 0;

	@Option(names = { "-x", "--proxy-port" }, required = false, description = "socks proxy's port")
	public Integer proxy_port;

	@Option(names = { "--proto" }, description = "protocol, udp or tcp. default is ${DEFAULT-VALUE}")
	public String proto = "tcp";

	@Override
	public Void call() throws Exception {
		Proxy proxy;
		if (proxy_port != null) {
			SocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", proxy_port);
			proxy = new Proxy(Type.SOCKS, proxyAddr);
		} else
			proxy = Proxy.NO_PROXY;
		try (Socket s = new Socket(proxy)) {
			if (sndbuf_size > 0)
				s.setSendBufferSize(sndbuf_size);
			if (rcvbuf_size > 0)
				s.setReceiveBufferSize(rcvbuf_size);

			s.connect(new InetSocketAddress(server_host, server_port));
			System.out.println(String.format("connected to %s", s.getRemoteSocketAddress()));
			DataOutputStream os = new DataOutputStream(s.getOutputStream());
			InputStream is = s.getInputStream();
			os.writeLong(data_size * 1024 * 1024);
			long bytesReceived = 0;
			byte[] buf = new byte[buf_size];
			long startTime = System.currentTimeMillis();
			while (true) {
				int n = is.read(buf);
				if (n == -1)
					break;
				bytesReceived += n;
				if (n > 65536)
					System.out.println("n > 25536 !");
				if (n == buf.length)
					System.out.println("maximum buf!");
			}
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			double speedRate = bytesReceived / ((double) duration / 1000) * 8;
			System.out.println(String.format("received: %s, duration: %d s, speed: %s", format1024(bytesReceived, "B"),
					duration / 1000, format1024((long) speedRate, "bps")));
			try {
				String local = s.getLocalSocketAddress().toString();
				int rbufsz = s.getReceiveBufferSize();
				int sbufsz = s.getSendBufferSize();
				System.out.println(String.format("%s, rbufsz: %d, sbufsz: %d", local, rbufsz, sbufsz));
			} catch (SocketException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static String format1024(long num, String unit) {
		int decimals = 2;
		long KB = 1024;
		long MB = 1024 * KB;
		long GB = 1024 * MB;
		long TB = 1024 * GB;
		long PB = 1024 * TB;
		long EB = 1024 * PB;
		double numd = num;
		if (num < KB)
			return num + " " + unit;
		if (num < MB)
			return Precision.round(numd / KB, decimals) + " K" + unit;
		if (num < GB)
			return Precision.round(numd / MB, decimals) + " M" + unit;
		if (num < TB)
			return Precision.round(numd / GB, decimals) + " G" + unit;
		if (num < PB)
			return Precision.round(numd / TB, decimals) + " T" + unit;
		if (num < EB)
			return Precision.round(numd / PB, decimals) + " P" + unit;
		else
			return Precision.round(numd / EB, decimals) + " E" + unit;
	}

}
