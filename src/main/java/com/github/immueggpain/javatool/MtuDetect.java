package com.github.immueggpain.javatool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

public class MtuDetect {

	public static void main(String[] args) {
		try {
			System.out.println("java -jar xxx.jar host");
			String host = args[0];
			// host = "118.184.84.204";

			Function<Integer, Boolean> cantFragment = null;
			if (SystemUtils.IS_OS_WINDOWS) {
				cantFragment = (dataSize) -> execCommandR("ping -n 2 -l " + dataSize + " -f " + host)[0]
						.contains("Packet needs to be fragmented but DF set");
			} else if (SystemUtils.IS_OS_LINUX) {
				cantFragment = (dataSize) -> execCommandR("ping -c 2 -M do -s " + dataSize + " " + host)[1]
						.contains("ping: local error: Message too long");
			} else {
				System.out.println("unknown OS");
				return;
			}

			int dataSize = 1500 - 28; // -28 cuz it's fucking faster for me!
			for (; dataSize >= 0; dataSize--) {
				if (!cantFragment.apply(dataSize))
					break;
			}

			// +28 because ip(20)+icmp(8)
			int mtu = dataSize + 28;
			// -40 because ip(20)+tcp(20)
			int mss = mtu - 40;
			// for udp payload. ip(20~60)+udp(8). because there may be optional ip header.
			int mud_min = mtu - 60 - 8;
			int mud_max = mtu - 20 - 8;
			System.out.println("IP MTU is " + mtu + " bytes");
			System.out.println("MSS is " + mss + " bytes");
			System.out.println("max udp data length is " + mud_min + "~" + mud_max + " bytes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * wait for process terminated.
	 */
	public static String[] execCommandR(String command) {
		try {
			System.out.println("exec: " + command);
			Process pro = Runtime.getRuntime().exec(command);
			pro.waitFor();
			String[] ret = new String[2];
			ret[0] = IOUtils.toString(pro.getInputStream(), Charset.defaultCharset());
			ret[1] = IOUtils.toString(pro.getErrorStream(), Charset.defaultCharset());
			pro.exitValue();
			return ret;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("see cause", e);
		}
	}

}
