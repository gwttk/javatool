package com.github.immueggpain.javatool;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;
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

	@Option(names = { "-s", "--data-size" }, required = true, description = "size of data to download in bytes")
	public long data_size;

	@Option(names = { "-x", "--proxy-port" }, required = false, description = "socks proxy's port")
	public Integer proxy_port;

	@Override
	public Void call() throws Exception {
		Proxy proxy;
		if (proxy_port != null) {
			SocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", proxy_port);
			proxy = new Proxy(Type.SOCKS, proxyAddr);
		} else
			proxy = Proxy.NO_PROXY;
		try (Socket s = new Socket(proxy)) {
			s.connect(new InetSocketAddress(server_host, server_port));
			System.out.println(String.format("connected to %s", s.getRemoteSocketAddress()));
			DataOutputStream os = new DataOutputStream(s.getOutputStream());
			InputStream is = s.getInputStream();
			os.writeLong(data_size);
			long bytesReceived = 0;
			byte[] buf = new byte[1024 * 32];
			long startTime = System.currentTimeMillis();
			while (true) {
				int n = is.read(buf);
				if (n == -1)
					break;
				bytesReceived += n;
			}
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			double speedRate = bytesReceived / ((double) duration / 1000) / 1024;
			System.out.println(String.format("received: %s, duration: %d s, speed: %.2f KB/s",
					formatBytes(bytesReceived), duration / 1000, speedRate));
			return null;
		}
	}

	public static String formatBytes(long num) {
		int decimals = 2;
		long KB = 1024;
		long MB = 1024 * KB;
		long GB = 1024 * MB;
		long TB = 1024 * GB;
		long PB = 1024 * TB;
		long EB = 1024 * PB;
		double numd = num;
		if (num < KB)
			return num + " B";
		if (num < MB)
			return Precision.round(numd / KB, decimals) + " KB";
		if (num < GB)
			return Precision.round(numd / MB, decimals) + " MB";
		if (num < TB)
			return Precision.round(numd / GB, decimals) + " GB";
		if (num < PB)
			return Precision.round(numd / TB, decimals) + " TB";
		if (num < EB)
			return Precision.round(numd / PB, decimals) + " PB";
		else
			return Precision.round(numd / EB, decimals) + " EB";
	}

}
