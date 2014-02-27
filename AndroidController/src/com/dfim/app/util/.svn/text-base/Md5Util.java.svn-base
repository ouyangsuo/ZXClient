package com.dfim.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName:Md5Util
 * @Description:md5加密
 * @author Mark
 * @date 2012-1-5 上午11:22:05
 */
public class Md5Util {
	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
	
	public static String process(String pSource) {
		String lDigest = "None";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(pSource.getBytes());
			byte[] byteArray = md.digest();

			StringBuffer md5StrBuff = new StringBuffer();

			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
					md5StrBuff.append("0").append(
							Integer.toHexString(0xFF & byteArray[i]));
				else
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}
			lDigest = md5StrBuff.toString();
		} catch (NoSuchAlgorithmException lEx) {
			throw new RuntimeException("Problems calculating MD5", lEx);
		}
		return lDigest;
	}

	public static void main(String[] args){
		System.out.println(process("68f278bb-b0d6-c554-f386-8b43f0d513b0"));
	}

	/**
	 * @Title: process
	 * @Description: 获得文件的md5校验码
	 * @param @param file
	 * @param @return
	 * @param @throws IOException
	 * @return String    
	 * @throws
	 */
	public static String process(File file) throws IOException {
		InputStream inputStream = null;
		MessageDigest md5 = null;
		try {
			inputStream = new FileInputStream(file);
			byte[] buffer = new byte[512];
			md5 = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while ((numRead = inputStream.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
		}
		byte[] b = md5.digest();
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

}
