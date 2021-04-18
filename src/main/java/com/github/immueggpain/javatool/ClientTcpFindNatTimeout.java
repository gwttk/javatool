package com.github.immueggpain.javatool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "find timeout of NAT router", name = "clttcpnatto", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class ClientTcpFindNatTimeout implements Callable<Void> {

	@Option(names = { "-s", "--server-addr" }, required = true, description = "server address")
	public String serverAddr;

	@Option(names = { "-p", "--server-port" }, required = true, description = "server listening port")
	public int serverPort;

	@Override
	public Void call() throws Exception {
		try (Socket s = new Socket(Proxy.NO_PROXY)) {
			s.setTcpNoDelay(true);
			s.connect(InetSocketAddress.createUnresolved(serverAddr, serverPort));
			DataInputStream is = new DataInputStream(s.getInputStream());
			DataOutputStream os = new DataOutputStream(s.getOutputStream());

			Random r = new Random();
			long seconds = 15;

			while (true) {
				long v = r.nextLong();
				os.writeLong(v);
				long vIn = is.readLong();
				if (vIn == v) {
					System.out.println(String.format("%d seconds is ok!", seconds));
					seconds = seconds + 1000 * 5;
					Thread.sleep(seconds * 1000);
				} else {
					System.out.println("data error");
					break;
				}
			}
		}
		return null;
	}

}
