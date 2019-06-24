package com.github.immueggpain.javatool;

import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send an http post request", name = "hpost", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class HttpPost implements Callable<Void> {

	@Option(names = { "-u", "--url" }, required = true, description = "post URL")
	public String urlStr;

	@Override
	public Void call() throws Exception {
		URL url = new URL(urlStr);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStream os = connection.getOutputStream();
		byte[] postBody = "".getBytes(StandardCharsets.UTF_8);
		os.write(postBody);
		os.close();
		// send request
		int contentLength = connection.getContentLength();
		System.out.println(String.format("contentLength: %d", contentLength));
		return null;
	}

}
