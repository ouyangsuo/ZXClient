package com.kitty.poclient.util;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;



/**
 * @ClassName:SignaturGenUtil
 * @Description:ǩ���㷨
 * @author Mark
 * @date 2012-1-5 ����11:27:25
 */
public class SignaturGenUtil {
	/**
	 * @Title: generator
	 * @Description: ���ǩ���㷨
	 * @param @param pStringToSign
	 * @param @param pKey
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String generator(String pStringToSign, String pKey) {
		String lSignature = "None";
		try {
			Mac lMac = Mac.getInstance("HmacSHA1");
			SecretKeySpec lSecret = new SecretKeySpec(pKey.getBytes(), "HmacSHA1");
			lMac.init(lSecret);

			byte[] lDigest = lMac.doFinal(pStringToSign.getBytes());
			lSignature = new String(Base64Util.encode(lDigest));
		} catch (NoSuchAlgorithmException lEx) {
			throw new RuntimeException("Problems calculating HMAC", lEx);
		} catch (InvalidKeyException lEx) {
			throw new RuntimeException("Problems calculating HMAC", lEx);
		}
		return lSignature;
	}
	
	public static String generator(String pStringToSign) throws UnknownHostException {
		return generator(pStringToSign,GUIDGenUtil.generate(false, true));
	}
	/**
	 * @Title: getRequestUrl
	 * @Description: ��ÿͻ��������url
	 * @param @param url
	 * @param @param params
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String createurl(String url, Map<String, Object> params,String devicekey) {
		StringBuffer sb = new StringBuffer(url);
		String paramurl="";
		try {
			paramurl = MapUtil.getUrlFromMap(MapUtil.filter(params,""));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//���ȥ��ǩ��Ĳ���
		String pStringToSign = StringUtil.StringFilter(paramurl);//ȥ�������е������ַ�
		String sign_url = "&signature=" + SignaturGenUtil.generator(pStringToSign, devicekey);// ���ǩ��
		sb.append("?").append(paramurl).append(sign_url);
		return sb.toString();
	}
}
