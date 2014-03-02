package com.kitty.poclient.util;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpClientManager {
   private static final String charset =HTTP.UTF_8;
   private static HttpClient customerHttpClient;
   
   public static synchronized HttpClient getHttpClient(){
	   if(customerHttpClient == null){
		   HttpParams params= new BasicHttpParams();
		   HttpProtocolParams.setVersion(params,HttpVersion.HTTP_1_1 );
	       HttpProtocolParams.setContentCharset(params, charset);
	       HttpProtocolParams.setUseExpectContinue(params, true);
	       HttpProtocolParams.setUserAgent(params,"android client");
	         /* 超时设置 */
           /* 从连接池中取连接的超时时间 */
           ConnManagerParams.setTimeout(params, 60000);
           HttpConnectionParams.setConnectionTimeout(params, 60000);
           /* 请求超时 */
           HttpConnectionParams.setSoTimeout(params, 60000);
           /* 设置我们的HttpClient支持HTTP和HTTPS两种模式 */
           SchemeRegistry schReg = new SchemeRegistry();
           schReg.register(new Scheme("http", PlainSocketFactory
                   .getSocketFactory(), 80));
           schReg.register(new Scheme("https", SSLSocketFactory
                   .getSocketFactory(), 443));
           /* 使用线程安全的连接管理来创建HttpClient */
           ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                   params, schReg);
           customerHttpClient = new DefaultHttpClient(conMgr, params);
	   }
	   return customerHttpClient;
	   
   }
   /**
    * 
    */
   
	
	
	
	
}
