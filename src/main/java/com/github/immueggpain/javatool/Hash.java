package com.github.immueggpain.javatool;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Calculate hash of file", name = "hash", mixinStandardHelpOptions = true, version = Launcher.VERSTR)
public class Hash implements Callable<Void> {

	@Option(names = { "-a", "--algo" }, required = true, description = "hash algorithm: sha512, md5, sha1")
	public String algo;

	@Parameters
	public Path[] files;

	@Override
	public Void call() throws Exception {
		for (Path file : files) {
			try (InputStream is = Files.newInputStream(file)) {
				String digestStr = "no algo";
				if (algo.equals("sha512"))
					digestStr = DigestUtils.sha512Hex(is);
				else if (algo.equals("md5"))
					digestStr = DigestUtils.md5Hex(is);
				else if (algo.equals("sha1"))
					digestStr = DigestUtils.sha1Hex(is);
				System.out.println(String.format("%s  %s", digestStr, algo));
			}
		}
		return null;
	}

}
