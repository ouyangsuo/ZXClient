package com.dfim.app.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pools {

	public static ExecutorService executorService1=Executors.newCachedThreadPool();//
	public static ExecutorService executorService2=Executors.newCachedThreadPool();//下载图片专用
//	public static ExecutorService executorService3=Executors.newCachedThreadPool();
	
	public static void initCachedPool(ExecutorService es){
		es=Executors.newCachedThreadPool();
	}
	
}
