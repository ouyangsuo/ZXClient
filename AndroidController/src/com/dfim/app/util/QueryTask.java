package com.dfim.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.os.Message;

public class QueryTask {
    //获取当前系统的CPU 数目 ,ExecutorService通常根据系统资源情况灵活定义线程池大小 	
	public final static RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();   
	private final static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(50) ;
    public final static ThreadPoolExecutor executorService = new ThreadPoolExecutor(5, 10,200, TimeUnit.SECONDS, queue,handler);
    
	
	public static void doGet(final String url,final TaskCallback ajaxCallback)
	{   
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				ajaxCallback.callback(msg.what,(String)msg.obj);	
			}
		};
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream inputStream = null;//输入流对象
				HttpClient httpClient = null;
				HttpGet httpGet = null;
				try{
					
				    httpGet = new HttpGet(url);
				    httpGet.setHeader("Connection", "close");
			        //生成一个http客户端对象
			        httpClient = CustomerHttpClient.getHttpClient();
			      
			       //发送请求           			     
			  	      HttpResponse httpResponse = httpClient.execute(httpGet);//接收响应
			         int code = httpResponse.getStatusLine().getStatusCode();
			         HttpEntity httpEntity = httpResponse.getEntity();//取出响应
			         String result = "";
			         if(code==200){
			             //客户端收到响应的信息流
			             inputStream = httpEntity.getContent();
			             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			             
			             String line = "";
			             while((line = reader.readLine()) != null){
			                 result = result + line;
			             }									
			         }
					 Message msg = handler.obtainMessage(code, result);
					 msg.sendToTarget();	
					}catch(SocketTimeoutException e){
						if(e.getMessage()!=null){
							 Message msg = handler.obtainMessage(-101, e.getMessage());
							 msg.sendToTarget();	
						}else{
							 Message msg = handler.obtainMessage(-101, "请求超时");
							 msg.sendToTarget();	
						}
					}catch(Exception e){
						 e.printStackTrace();
						 Message msg = handler.obtainMessage(-1, e.getMessage());
						 msg.sendToTarget();	
					}finally{
						try {
							
							if(inputStream!=null){
			            		   inputStream.close();
			            	}
							
							if(httpGet!=null){
								httpGet.abort();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
		});
	}
	
	
	public static void doPost(final String url,final List<NameValuePair> nameValuePairs,final TaskCallback ajaxCallback)
	{
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				ajaxCallback.callback(msg.what,(String)msg.obj);	
			}
		};
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream inputStream = null;//输入流对象
				 HttpPost httpPost = null;
				try{

					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);//对参数进行编码操作
		            //生成一个post请求对象
		            httpPost = new HttpPost(url);
		            httpPost.setHeader("Connection", "close");
		            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		            httpPost.setEntity(requestHttpEntity);
		            //生成一个http客户端对象
		            HttpClient httpClient = CustomerHttpClient.getHttpClient();//发送请求		            
		            String result = "";
		            HttpResponse httpResponse = httpClient.execute(httpPost);//接收响应		            
		            HttpEntity httpEntity = httpResponse.getEntity();//取出响应
		            int code = httpResponse.getStatusLine().getStatusCode();
		            if(code==200){
		               //客户端收到响应的信息流
		               inputStream = httpEntity.getContent();
		               BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));		                    
		               String line = "";
		               while((line = reader.readLine()) != null){
		                   result = result + line;
		               }        										
			         }
					 Message msg = handler.obtainMessage(code, result);
					 msg.sendToTarget();	
					}catch(SocketTimeoutException e){
						if(e.getMessage()!=null){
							 Message msg = handler.obtainMessage(-101, e.getMessage());
							 msg.sendToTarget();	
						}else{
							 Message msg = handler.obtainMessage(-101, "请求超时");
							 msg.sendToTarget();	
						}
					}catch(Exception e){
						e.printStackTrace();
						 Message msg = handler.obtainMessage(-1, e.getMessage());
						 msg.sendToTarget();	
					}finally{
						try {
							if(inputStream!=null){
			            		   inputStream.close();
			            	   }
							if(httpPost!=null){
								httpPost.abort();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
		});
	}
	
	
	public static void doGetInThread(final String url,final TaskCallback ajaxCallback)
	{   
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream inputStream = null;//输入流对象
				HttpGet httpGet = null;
				try{					
				   httpGet = new HttpGet(url);
			       //生成一个http客户端对象
			       HttpClient httpClient = CustomerHttpClient.getHttpClient();
			       //发送请求           			     
			  	   HttpResponse httpResponse = httpClient.execute(httpGet);//接收响应
			         int code = httpResponse.getStatusLine().getStatusCode();
			         HttpEntity httpEntity = httpResponse.getEntity();//取出响应
			         String result = "";
			         if(code==200){
			             //客户端收到响应的信息流
			             inputStream = httpEntity.getContent();
			             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			             
			             String line = "";
			             while((line = reader.readLine()) != null){
			                 result = result + line;
			             }	
			         }
			         ajaxCallback.callback(code,result);
					}catch(SocketTimeoutException e){
						if(e.getMessage()!=null){
							 ajaxCallback.callback(-101,e.getMessage());
						}else{
							 ajaxCallback.callback(-101,"请求超时");
						}
					}catch(Exception e){
						e.printStackTrace();
						ajaxCallback.callback(-1,e.getMessage());
					}finally{
						try {
							if(inputStream!=null){
			            		   inputStream.close();
			            	   } 
							if(httpGet!=null){
								httpGet.abort();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
		});	
	}
	
	
	public static void doPostInThread(final String url,final List<NameValuePair> nameValuePairs,final TaskCallback ajaxCallback)
	{
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream inputStream = null;//输入流对象
				HttpPost httpPost = null;
				try{				
					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);//对参数进行编码操作
		            //生成一个post请求对象
		            httpPost = new HttpPost(url);
		            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		            httpPost.setEntity(requestHttpEntity);
		            //生成一个http客户端对象
		            HttpClient httpClient = CustomerHttpClient.getHttpClient();//发送请求
		            	String result = "";
		            	HttpResponse httpResponse = httpClient.execute(httpPost);//接收响应
		            	HttpEntity httpEntity = httpResponse.getEntity();//取出响应
		            	int code = httpResponse.getStatusLine().getStatusCode();
		            	if(code==200){
		                    //客户端收到响应的信息流
		                    inputStream = httpEntity.getContent();
		                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		                    
		                    String line = "";
		                    while((line = reader.readLine()) != null){
		                        result = result + line;
		                    }        									
			         }
		             ajaxCallback.callback(code,result);	
					}catch(SocketTimeoutException e){
						if(e.getMessage()!=null){
							 ajaxCallback.callback(-101,e.getMessage());
						}else{
							 ajaxCallback.callback(-101,"请求超时");
						}
					}catch(Exception e){
						e.printStackTrace();
						ajaxCallback.callback(-1,e.getMessage());
					}finally{
						try {
							if(inputStream!=null){
			            		   inputStream.close();
			            	   }  
							if(httpPost!=null){
								httpPost.abort();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
		});	
	}
	
	
	//回调接口
	public interface TaskCallback
	{
		public void callback(int code,String json);
	}
}