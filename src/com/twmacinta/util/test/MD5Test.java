package com.twmacinta.util.test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.twmacinta.util.MD5;
import com.twmacinta.util.MD5State;
import com.twmacinta.util.MD5StateIO;

public class MD5Test {
	public static void main(String[] args) throws Exception {
		MD5Test t = new MD5Test();
		
		System.out.println("Testing MD5 algorithm");
		t.runBasicTests(new SingleRunDigest());
		
		System.out.println("Testing MD5 with resume");
		t.runBasicTests(new ResumableDigest());
		
		System.out.println("Running MD5 on random data with default JDK MD5, Fast MD5 and Fast MD5 with resume");
		t.compareThreeMd5Implementations();
		
		System.out.println("Testing state serialization/deserialization");
		t.testInitState();
		
		System.out.println("Testing state serialization/deserialization after some hashing");
		t.testStateAfterUpdates();
	}
	
	protected void runBasicTests(Digest d) throws UnsupportedEncodingException {
		assertEquals("d41d8cd98f00b204e9800998ecf8427e", runMD5(d, ""));
		assertEquals("0cc175b9c0f1b6a831c399e269772661", runMD5(d, "a"));
		assertEquals("900150983cd24fb0d6963f7d28e17f72", runMD5(d, "abc"));
		assertEquals("f96b697d7cb7938d525a2f31aaf161d0", runMD5(d, "message digest"));
		assertEquals("c3fcd3d76192e4007dfb496cca67e13b", runMD5(d, "abcdefghijklmnopqrstuvwxyz"));
		assertEquals("d174ab98d277d9f5a5611c2c9f419d9f", runMD5(d, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));
		assertEquals("57edf4a22be3c955ac49da2e2107b67a", runMD5(d, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"));
		assertEquals("9e107d9d372bb6826bd81d3542a419d6", runMD5(d, "The quick brown fox jumps over the lazy dog"));
		assertEquals("e4d909c290d0fb1ca068ffaddf22cbd0", runMD5(d, "The quick brown fox jumps over the lazy dog."));
	}

	public void compareThreeMd5Implementations() {
		Digest d1 = new SunDigest();
		Digest d2 = new SingleRunDigest();
		Digest d3 = new ResumableDigest();

		Random r = new Random();
		for (int i = 0; i < 32; i++) {
			int len = r.nextInt(16*1024*1024);

			byte[] input = new byte[len];
			r.nextBytes(input);

			String r1 = runMD5(d1, input);
			String r2 = runMD5(d2, input);
			String r3 = runMD5(d3, input);
			
			if (!r1.equals(r2)) {
				throw new IllegalStateException("MD5 checksums don't match: " + r1 + ", " + r2);
			}
			
			if (!r2.equals(r3)) {
				throw new IllegalStateException("MD5 checksums don't match: " + r2 + ", " + r3);
			}

			System.out.println("* " + i + ": size: " + len + ", hashes: " + r1 + " " + r2 + " " + r3 + " OK");
		}
	}

	String runMD5(Digest digest, String str) throws UnsupportedEncodingException {
		return runMD5(digest, str.getBytes("ISO-8859-1"));
	}

	String runMD5(Digest digest, byte[] data) {
		byte[] result = digest.digest(data);
		return MD5.asHex(result);
	}

	static abstract class Digest {
		abstract byte[] digest(byte[] data);
	}

	static class SingleRunDigest extends Digest {
		byte[] digest(byte[] data) {
			MD5 md5 = new MD5();
			md5.Update(data);
			return md5.Final();
		}
	}

	static class ResumableDigest extends Digest {
		byte[] digest(byte[] data) {
			int rounds = 0;
			Random r = new Random();

			MD5State state = new MD5State();
			int remaining = data.length;
			int ix = 0;

			while (remaining > 0) {
				rounds ++;
				int len = r.nextInt(remaining) + 1;

				MD5 md5 = new MD5();
				md5.setState(state);
				md5.Update(data, ix, len);

				ix += len;
				remaining -= len;
				state = md5.getState();
			}

			MD5 md5 = new MD5();
			md5.setState(state);
			return md5.Final();
		}
	}

	static class SunDigest extends Digest {
		byte[] digest(byte[] data) {
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				return digest.digest(data);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void testInitState() throws Exception {
		byte[] result = MD5StateIO.serialize(new MD5State());
		assertEquals(88, result.length);
		assertEquals(new MD5State(), MD5StateIO.deserialize(result));
	}

	public void testStateAfterUpdates() throws Exception {
		MD5 md5 = new MD5();
		md5.Update("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...".getBytes("ISO-8859-1"));

		MD5State state = md5.getState();

		assertEquals(state, MD5StateIO.deserialize(MD5StateIO.serialize(state)));
	}

	public static void assertEquals(int expected, int actual) {
		if (expected != actual) {
			throw new IllegalStateException("Expected: " + expected + ", got: " + actual);
		}
	}
	
	public static void assertEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new IllegalStateException("Expected: " + expected + ", got: " + actual);
		}
	}
}
