package com.github.immueggpain.javatool;

import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send an http request", name = "http", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class HttpHead implements Callable<Void> {

	@Option(names = { "-u", "--url" }, required = true, description = "HTTP URL")
	public String urlStr;

	@Option(names = { "-m", "--method" }, required = true, description = "HTTP Method")
	public String method;

	@Override
	public Void call() throws Exception {
		System.out.println(urlStr);
		URL url = new URL(urlStr);
		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(64 * 1024);
		Socket s = new Socket("127.0.0.1", 1082);
		s.setTcpNoDelay(true);
		conn.bind(s);
		BasicHttpRequest request = new BasicHttpRequest(method.toUpperCase(), urlStr, HttpVersion.HTTP_1_1);
		request.setHeader("Host", url.getAuthority());
		conn.sendRequestHeader(request);
		conn.flush();
		HttpResponse response = conn.receiveResponseHeader();
		conn.receiveResponseEntity(response);

		System.out.println(response.getStatusLine());

		for (Header header : response.getAllHeaders()) {
			System.out.println(header);
		}
		HttpEntity responseEntity = response.getEntity();
		if (responseEntity != null) {
			byte[] buf = IOUtils.toByteArray(responseEntity.getContent());
			System.out.println("response body length: " + buf.length);
		}

		// IOUtils.copy(response.getEntity().getContent(), System.out);
		conn.close();
		return null;
	}

}
