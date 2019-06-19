package com.github.immueggpain.javatool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "base64 encoder/decoder", name = "base64", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class Base64 implements Callable<Void> {

	@Option(names = { "-i", "--input" }, required = true, description = "input file")
	public Path inputFile;

	@Option(names = { "-o", "--output" }, required = true, description = "output file")
	public Path outputFile;

	@Option(names = { "-m", "--mode" }, required = true, description = "encode/decode mode")
	public Mode mode;

	public static enum Mode {
		ENCODE, DECODE
	}

	@Override
	public Void call() throws Exception {
		if (mode == Mode.ENCODE) {
			byte[] in = Files.readAllBytes(inputFile);
			byte[] out = org.apache.commons.codec.binary.Base64.encodeBase64(in);
			Files.write(outputFile, out);
		} else if (mode == Mode.DECODE) {
			byte[] in = Files.readAllBytes(inputFile);
			byte[] out = org.apache.commons.codec.binary.Base64.decodeBase64(in);
			Files.write(outputFile, out);
		} else {
			System.err.println("unknown mode");
		}
		return null;
	}

}
