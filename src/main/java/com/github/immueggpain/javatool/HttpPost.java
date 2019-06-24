package com.github.immueggpain.javatool;

import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Send an http post request", name = "hpost", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class HttpPost implements Callable<Void> {

	@Option(names = { "-u", "--url" }, required = true, description = "post URL")
	public String urlStr;

	@Option(names = { "-b", "--body" }, required = true, description = "post URL")
	public String body;

	@Override
	public Void call() throws Exception {
		URL url = new URL(urlStr);
		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(64 * 1024);
		Socket s = SSLSocketFactory.getDefault().createSocket(url.getHost(),
				url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
		conn.bind(s);
		HttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", url.getPath());
		StringEntity entity = new StringEntity(body);
		request.setEntity(entity);
		request.setHeader("Host", url.getHost());
		request.setHeader("Content-Length", "" + entity.getContentLength());
		request.setHeader("Accept-Encoding", "gzip");
		conn.sendRequestHeader(request);
		conn.sendRequestEntity(request);
		HttpResponse response = conn.receiveResponseHeader();
		conn.receiveResponseEntity(response);

		for (Header header : response.getAllHeaders()) {
			System.out.println(header);
		}
		HttpEntity responseEntity = response.getEntity();
		byte[] buf = IOUtils.toByteArray(responseEntity.getContent());
		System.out.println("response body length: " + buf.length);

		// IOUtils.copy(response.getEntity().getContent(), System.out);
		conn.close();
		return null;
	}

}
