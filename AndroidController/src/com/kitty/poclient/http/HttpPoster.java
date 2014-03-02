package com.kitty.poclient.http;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.kitty.poclient.R;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.util.Md5Util;
import com.kitty.poclient.util.SignaturGenUtil;
import com.kitty.poclient.util.StringUtil;

public class HttpPoster {

	public static final int MAX_SEARCH_ITEM = 100;

	public String buyAlbumOrMusic(String ordertype, long id) {
		String json = "";

		// 构造URL
		Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "& timestamp=" + timestamp;
		// String pStringToSign = StringUtil.StringFilter(paramsInOrder);//
		// 去掉参数中的特殊字符
		// String pKey = Md5Util.process(WatchDog.currentUserId + "_" +
		// WatchDog.currentPassword);
		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Md5Util.process(WatchDog.currentUserId + "_" + WatchDog.currentPassword));
		String url = Constant.getBaseUrl() + "order/albummusicforpad?apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;
		// System.out.println("buy url="+url);

		// 构造client和httppost
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		// 设置HttpPost对象参数
		String psw = Md5Util.process(WatchDog.currentPassword + WatchDog.currentUserId);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ordertype", ordertype));
		params.add(new BasicNameValuePair("contentid", "" + id));
		// System.out.println("contentid="+id);
		params.add(new BasicNameValuePair("password", psw));

		// 获取返回值
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = client.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				// System.out.println("buy json="+json);
			} else {
				System.out.println("购买失败:" + httpResponse.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	/* 购买主题 */
	public String buyPack(long id) {
		// apikey=DEVICENO& timestamp=TIMESTATMP&protocolver=PROTOCOLVER&
		// signature=SIGNATURE
		String json = "";

		// 构造URL
		Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "& timestamp=" + timestamp;

		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Md5Util.process(WatchDog.currentUserId + "_" + WatchDog.currentPassword));
		String url = Constant.getBaseUrl() + "order/packforpad?apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;

		// 构造client和httppost
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		// 设置HttpPost对象参数
		String psw = Md5Util.process(WatchDog.currentPassword + WatchDog.currentUserId);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("packid", "" + id));
		params.add(new BasicNameValuePair("password", psw));

		// 获取返回值
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = client.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				// System.out.println("buy json="+json);
			} else {
				System.out.println("主题购买失败:" + httpResponse.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	/* 搜索全部 */
	public String searchAll(String inputStr) {
		// content/search?
		// searchtype=SEARCHTYPE&maxitem=MAXITEMS&startitem=STARTITEM&apikey=DEVICENO&timestamp=TIMESTATMP&protocolver=PROTOCOLVER&signature=SIGNATURE
		String json = "";

		// 构造URL
		Long timestamp = System.currentTimeMillis();
		int maxItems = MAX_SEARCH_ITEM;
		int startItem = 0;
		int searchType = 0;
		String paramsInOrder = "apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp;

		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Md5Util.process(WatchDog.currentUserId + "_" + WatchDog.currentPassword));
		String url = Constant.getBaseUrl() + "content/searchproduct?apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;

		// 构造client和httppost
		DefaultHttpClient client = new HttpClientProducer().getHttpClient(Constant.CONNECTION_TIMEOUT_MILLIS, Constant.SOCKET_TIMEOUT_MILLIS);//设置client的连接超时和读取数据超时时间
		HttpPost httpPost = new HttpPost(url);

		// 设置HttpPost对象参数
		String psw = Md5Util.process(WatchDog.currentPassword + WatchDog.currentUserId);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", inputStr));
		// params.add(new BasicNameValuePair("password", psw));

		// 获取返回值
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = client.execute(httpPost);

			System.out.println("httpResponse.getStatusLine().getStatusCode()=" + httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				System.out.println("jsonSearch=" + json);
			} else {

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	/*
	 * 搜索单项内容 0-全部，1-专辑，5-单曲，10-演出者
	 */
	public String search(String inputStr, int searchType) {
		String json = "";

		// 构造URL
		Long timestamp = System.currentTimeMillis();
		int maxItems = MAX_SEARCH_ITEM;
		int startItem = 0;
		
		String paramsInOrder = "apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp;
//		String paramsInOrder = "apikey=" + "133024" + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp;

		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Constant.P_KEY);
//		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Md5Util.process("133024" + "_" + WatchDog.currentPassword));
		
		String url = Constant.getBaseUrl() + "content/searchproduct?apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;
//		String url = "http://192.168.1.17:8090/zhenxianwang/ws/" + "content/searchproduct?apikey=" + "133024" + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;
		System.out.println("url="+url);
		
		// 构造client和httppost
		DefaultHttpClient client = new HttpClientProducer().getHttpClient(Constant.CONNECTION_TIMEOUT_MILLIS, Constant.SOCKET_TIMEOUT_MILLIS);//设置client的连接超时和读取数据超时时间
		HttpPost httpPost = new HttpPost(url);

		// 设置HttpPost对象参数
		String psw = Md5Util.process(WatchDog.currentPassword + WatchDog.currentUserId);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", URLEncoder.encode(inputStr)));

		// 获取返回值
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			System.out.println("params="+params);
			HttpResponse httpResponse = client.execute(httpPost);

			System.out.println("httpResponse.getStatusLine().getStatusCode()=" + httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				System.out.println("jsonSearch" + searchType + "=" + json);
			} else {

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			json=UpnpApp.context.getResources().getString(R.string.data_load_failed);
			currentFragmentShowNoData();
		}

		return json;
	}
	
	private void currentFragmentShowNoData() {
		WatchDog.currentSelfReloader.onDataLoadFailed();
	}

	/*
	 * 搜索单项内容 0-全部，1-专辑，5-单曲，10-演出者
	 */
//	public String search(String inputStr, int searchType, int startItem) {
//		String json = "";
//
//		// 构造URL
//		Long timestamp = System.currentTimeMillis();
//		int maxItems = MAX_SEARCH_ITEM;
//		// int startItem = 0;
//		String paramsInOrder = "apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp;
//
//		String signature = SignaturGenUtil.generator(StringUtil.StringFilter(paramsInOrder), Md5Util.process(WatchDog.currentUserId + "_" + WatchDog.currentPassword));
//		String url = Constant.getBaseUrl() + "content/searchproduct?apikey=" + Constant.apikey + "&maxitem=" + maxItems + "&protocolver=" + Constant.protocolver + "&searchtype=" + searchType + "&startitem=" + startItem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + timestamp + "&signature=" + signature;
//		System.out.println("url="+url);
//		
//		// 构造client和httppost
//		DefaultHttpClient client = new HttpClientProducer().getHttpClient(Constant.CONNECTION_TIMEOUT_MILLIS, Constant.SOCKET_TIMEOUT_MILLIS);//设置client的连接超时和读取数据超时时间
//		HttpPost httpPost = new HttpPost(url);
//
//		// 设置HttpPost对象参数
//		String psw = Md5Util.process(WatchDog.currentPassword + WatchDog.currentUserId);
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("key", inputStr));
//
//		// 获取返回值
//		try {
//			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//			HttpResponse httpResponse = client.execute(httpPost);
//
//			System.out.println("httpResponse.getStatusLine().getStatusCode()=" + httpResponse.getStatusLine().getStatusCode());
//			if (httpResponse.getStatusLine().getStatusCode() == 200) {
//				json = EntityUtils.toString(httpResponse.getEntity());
//				System.out.println("jsonSearch" + searchType + "=" + json);
//			} else {
//
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return json;
//	}

}
