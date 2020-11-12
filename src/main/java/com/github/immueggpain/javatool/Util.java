package com.github.immueggpain.javatool;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.commons.lang3.ArrayUtils;

public class Util {

	public static class ServerReply {
		public String address;
		public int port;
		public String id;
	}

	public static class ClientAsk {
		public String address;
		public int port;
		public String id;
	}

	public static byte[] encrypt(Cipher encrypter, Key secretKey, byte[] input, int offset, int length)
			throws GeneralSecurityException {
		// we need init every time because we want random iv
		encrypter.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] iv = encrypter.getIV();
		byte[] encrypedBytes = encrypter.doFinal(input, offset, length);
		return ArrayUtils.addAll(iv, encrypedBytes);
	}

	public static byte[] decrypt(Cipher decrypter, Key secretKey, byte[] input, int offset, int length)
			throws GeneralSecurityException {
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, input, offset, 12);
		decrypter.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
		byte[] decryptedBytes = decrypter.doFinal(input, offset + 12, length - 12);
		return decryptedBytes;
	}

}
