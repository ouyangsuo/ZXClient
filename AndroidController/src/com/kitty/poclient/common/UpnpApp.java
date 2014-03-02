package com.kitty.poclient.common;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDN;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.kitty.poclient.R;
import com.kitty.poclient.activity.LoginActivity;
import com.kitty.poclient.activity.MainActivity;
import com.kitty.poclient.db.DBHelper;
import com.kitty.poclient.models.StateModel;
import com.kitty.poclient.upnp.AVTransportSubscriptionCallback;
import com.kitty.poclient.upnp.BoxSubscription;
import com.kitty.poclient.upnp.CacheControlSubscriptionCallback;

public class UpnpApp extends Application {

	public static final String TAG = "UpnpApp";
	
	public static Context context;
	
	public static MainActivity mainActivity;
	
	public static AndroidUpnpService upnpService;
	public static Service directoryService;
	public static Service avTransportService;
	public static Service renderingControlService;
	public static Service boxControlService;
	public static Service cacheControlService;
	public static Service connectionManagerService;
	public static UDN BOXUDN;
	public static CacheControlSubscriptionCallback myCacheSub;
	public static AVTransportSubscriptionCallback myAVSub;
	public static BoxSubscription myBoxSub;

	public static MainHandler mainHandler = new MainHandler();

	@Override
	public void onCreate() {
		super.onCreate();
		
		// ken,为捕获系统崩溃故障添加2013.07.17
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		
		if (Constant.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		}

		// important! this sax driver is necessary for cling stack
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
		context = getApplicationContext();
		System.out.println("getApplicationContext()=" + context);
		
		initImageLoader(context);
		initDB(1);
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
	}

	public static boolean isUpnpAlive() {
		if (UpnpApp.upnpService == null) {
//			UpnpApp.showToastMessage("通信异常：pnpService==null");
			UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
			return false;
		} else if (UpnpApp.upnpService.getControlPoint() == null) {
//			UpnpApp.showToastMessage("通信异常：controlPoint==null");
			UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
			return false;
		}
		return true;
	}

	// 重连盒子
	public static void reconnect() {
/*		System.gc();

		Looper.prepare();
		Intent intent = new Intent(context, LoginActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.e(TAG, "reconnect from upnpapp.java");
		UpnpApp.context.startActivity(intent);
//		CustomToast.makeText(UpnpApp.context, "连接中断，请重连设备", Toast.LENGTH_LONG).show();
    	UpnpApp.mainHandler.showAlert(R.string.device_disconnect_alert);
		Looper.loop();*/
		
		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putInt(LoginActivity.BUNDLE_STATE, StateModel.STATE_ERROR);
		loginIntent.putExtras(bundle);
		UpnpApp.context.startActivity(loginIntent);
	}
	
	public static Context getContext(){
		return context;
	}

	public static void initAllServices(Device rootDevice) {
		if (rootDevice == null || rootDevice.getServices() == null) {
			Log.e("BUG828", TAG + " initAllServices():rootDevice==null?" + (rootDevice == null));
			return;
		}
		
		for (Service service : rootDevice.getServices()) {
			if (service.getServiceType().getType().equals("ContentDirectory")) {
				UpnpApp.directoryService = service;
			} else if (service.getServiceType().getType().equals("AVTransport")) {
				UpnpApp.avTransportService = service;
			} else if (service.getServiceType().getType().equals("RenderingControl")) {
				UpnpApp.renderingControlService = service;
			} else if (service.getServiceType().getType().equals("BoxControl")) {
				UpnpApp.boxControlService = service;
			} else if (service.getServiceType().getType().equals("CacheControl")) {
				UpnpApp.cacheControlService = service;
			} else if (service.getServiceType().getType().equals("ConnectionManager")) {
				UpnpApp.connectionManagerService = service;
			}
		}
	}

	public static void restartApplication(final Context context) {
		Log.e("BUG965", TAG+"restartApplication()");		
	}
	
	public static void sendBroadcast(String action){
		context.sendBroadcast(new Intent(action));
	}
	
	private void initDB( int version) {
		SQLiteDatabase sqlite = DBHelper.getSqLitedatabase();

		if (sqlite != null) {
			if (sqlite.isOpen()) {
				sqlite.close();
			}
		}

		String host = "";
		if (WatchDog.currentHost != null) {
			host = WatchDog.currentHost.replaceAll("[.,/,:]", "");
		}
		String dbname = WatchDog.currentUserId + host;
		
		DBHelper db = new DBHelper(UpnpApp.context, null, null, version, dbname + ".db", null);
		db.getReadableDatabase();
	}
	
}
