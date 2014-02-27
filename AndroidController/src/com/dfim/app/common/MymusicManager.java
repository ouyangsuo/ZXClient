package com.dfim.app.common;

import android.util.Log;

import com.dfim.app.activity.MainActivity;
import com.dfim.app.fragment.TabMusicFragment;
import com.dfim.app.upnp.CacheControlSubscriptionCallback;
import com.dfim.app.upnp.Cacher;
import com.dfim.app.upnp.Player;

public class MymusicManager {
	private static final String TAG = MymusicManager.class.getSimpleName();
//	private static Stack<BaseFragment> fragmentStack;
//	private static MymusicManager instance;
	
	public static MainActivity mainActivity;
	
	public static TabMusicFragment tabMusicFragment;
	
	/*public static MymusicManager getMymusicManager(){
		if(instance==null){
			instance=new MymusicManager();
		}
		return instance;
	}*/
	
	public static void receiveCacheSub() {
		UpnpApp.myCacheSub = new CacheControlSubscriptionCallback(UpnpApp.cacheControlService);
		try {
			UpnpApp.upnpService.getControlPoint().execute(UpnpApp.myCacheSub);
		} catch (Exception e) {
			e.printStackTrace();
			CrashHandler.getInstance().saveCrashInfo2FileII("receiveCacheSub exception caught:" + e);
//			CustomToast.makeText(UpnpApp.context, "启动缓存消息订阅出现异常", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "启动缓存消息订阅出现异常" + e.getMessage());
		}
	}
	
	public static void getCacheInfo() {
		Log.i(TAG, "getCacheInfo");
		new Thread(new Runnable() {
			@Override
			public void run() {
				new Cacher().getCacheInfo(Constant.cacheUriAllMusic);
			}
		}).start();
	}
	
	public static void getMediaInfo() {
		Log.i(TAG, "getMediaInfo");
		new Thread(new Runnable() {
			@Override
			public void run() {
				new Player().getMediaInfo();
			}
		}).start();
	}
	public static void getTransportInfo() {
		Log.i(TAG, "getTransportInfo");
		new Thread(new Runnable() {
			@Override
			public void run() {
				new Player().getTransportInfo();
			}
		}).start();
	}
}
