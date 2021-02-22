package com.github.immueggpain.javatool;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "base64 encoder/decoder", name = "base64", mixinStandardHelpOptions = true,
		version = Launcher.VERSTR)
public class Base64 implements Callable<Void> {

	@Option(names = { "-i", "--input" }, description = "input file")
	public Path inputFile;

	@Option(names = { "-o", "--output" }, description = "output file")
	public Path outputFile;

	@Option(names = { "-s", "--string" }, split = ",", arity = "1..*", description = "input string(s)")
	public String[] inputStrings;

	@Option(names = { "-m", "--mode" }, required = true, description = "Valid values: ${COMPLETION-CANDIDATES}")
	public Mode mode;

	public static enum Mode {
		ENCODE, DECODE
	}

	@Override
	public Void call() throws Exception {
		if (mode == Mode.ENCODE) {
			if (inputFile != null) {
				byte[] in = Files.readAllBytes(inputFile);
				byte[] out = org.apache.commons.codec.binary.Base64.encodeBase64(in);
				Files.write(outputFile, out);
			}
			if (inputStrings != null) {
				for (String str : inputStrings) {
					String outstr = org.apache.commons.codec.binary.Base64.encodeBase64String(str.getBytes("UTF-8"));
					System.out.println(String.format("%s --> %s", str, outstr));
				}
			}
		} else if (mode == Mode.DECODE) {
			if (inputFile != null) {
				byte[] in = Files.readAllBytes(inputFile);
				byte[] out = org.apache.commons.codec.binary.Base64.decodeBase64(in);
				Files.write(outputFile, out);
			}
			if (inputStrings != null) {
				for (String str : inputStrings) {
					byte[] outstr = org.apache.commons.codec.binary.Base64.decodeBase64(str);
					System.out.println(String.format("%s --> %s", str, new String(outstr, StandardCharsets.UTF_8)));
				}
			}
		} else {
			throw new Exception("impossible");
		}
		return null;
	}

}
