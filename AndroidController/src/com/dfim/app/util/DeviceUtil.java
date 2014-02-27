package com.dfim.app.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dfim.app.common.Constant;

public class DeviceUtil {

	private static final String TAG=DeviceUtil.class.getSimpleName()+":";
	private Activity activity;

	public DeviceUtil(Activity activity) {
		this.activity = activity;
	}

	public void getScreenResolution() {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		Constant.SCREEN_WIDTH = dm.widthPixels;
		Constant.SCREEN_HEIGHT = dm.heightPixels;

		System.out.println("Constant.SCREEN_WIDTH =" + Constant.SCREEN_WIDTH);
		System.out.println("Constant.SCREEN_HEIGHT  =" + Constant.SCREEN_HEIGHT);
	}
	
	/**
	 * @description 检查当前移动设备是否连网
	 * */
	public boolean checkNetWorkStatus(Context context) {
		boolean netSataus = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		connectivityManager.getActiveNetworkInfo();
		if (connectivityManager.getActiveNetworkInfo() != null) {
			netSataus = connectivityManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}

	/**
	 * @description 在线程中不断监听设备连网状态
	 * */
	public void startDeviceNetworkCheckingThread(final Context context, final long timeMillisInterval,final Handler handler,final int what) {
		new Thread(new Runnable() {			
			@Override
			public void run() {	
				while(true){
					boolean networkAlive=checkNetWorkStatus(context);
					if(!networkAlive){
						Log.e(TAG, "network dead!!!!!!!!!!!!!!!!!!!!");
						handler.sendEmptyMessage(what);
					}else{
//						Log.e(TAG, "network alive...");
					}
					
					try {
						Thread.sleep(timeMillisInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	

}
