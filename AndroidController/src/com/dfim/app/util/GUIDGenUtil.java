package com.dfim.app.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @ClassName:GUIDGenUtil
 * @Description:���ʱ������GUID
 * @author Mark
 * @date 2012-1-5 ����11:18:02
 */
public class GUIDGenUtil {
	private static SecureRandom _secureRand = new SecureRandom();
	private static Random _rand;

	static {
		long l = _secureRand.nextLong();
		_rand = new Random(l);
	}

	public static String generate(boolean flag, boolean sep)
			throws UnknownHostException {
		MessageDigest messagedigest;
		StringBuilder stringbuffer = new StringBuilder();
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nosuchalgorithmexception) {

			throw new RuntimeException(nosuchalgorithmexception);
		}
		InetAddress inetaddress = InetAddress.getLocalHost();
		System.out.println(inetaddress);
		long l = System.currentTimeMillis();
		long l1 = 0L;
		if (flag)
			l1 = _secureRand.nextLong();
		else
			l1 = _rand.nextLong();
		stringbuffer.append(inetaddress.toString());
		stringbuffer.append(":");
		stringbuffer.append(Long.toString(l));
		stringbuffer.append(":");
		stringbuffer.append(Long.toString(l1));
		messagedigest.update(stringbuffer.toString().getBytes());
		byte[] abyte0 = messagedigest.digest();
		StringBuffer stringbuffer1 = new StringBuffer();
		for (int i = 0; i < abyte0.length; i++) {
			int j = abyte0[i] & 0xFF;
			if (j < 16)
				stringbuffer1.append('0');
			stringbuffer1.append(Integer.toHexString(j));
		}

		String s = stringbuffer1.toString();
		StringBuffer stringbuffer2 = new StringBuffer();

		if (sep) {
			stringbuffer2.append(s.substring(0, 8));
			stringbuffer2.append("-");
			stringbuffer2.append(s.substring(8, 12));
			stringbuffer2.append("-");
			stringbuffer2.append(s.substring(12, 16));
			stringbuffer2.append("-");
			stringbuffer2.append(s.substring(16, 20));
			stringbuffer2.append("-");
			stringbuffer2.append(s.substring(20));
			return stringbuffer2.toString();
		}
		return s;
	}
	
	public static void main(String[] args) throws UnknownHostException{
		System.out.println(generate(false,true));
	}
}
