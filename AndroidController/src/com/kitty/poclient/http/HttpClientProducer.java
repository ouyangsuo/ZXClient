package com.kitty.poclient.http;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class HttpClientProducer {
	
	public DefaultHttpClient getHttpClient(int rTimeOut, int sTimeOut) {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, rTimeOut);
		HttpConnectionParams.setSoTimeout(httpParams, sTimeOut);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
		return client;
	}
	
}
