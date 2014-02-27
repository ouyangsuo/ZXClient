package com.dfim.app.upnp;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

import android.os.Looper;
import android.util.Log;

import com.dfim.app.common.UpnpApp;
import com.dfim.app.util.JsonUtil;

public class Cacher {
	
	public static final String TAG="Cacher ";
	public Cacher(){
		
	}
	
	public void getCacheInfo(final String chacheUri) {
		ActionInvocation ai = null;
		try {
			ai = new ActionInvocation(
					UpnpApp.cacheControlService.getAction("GetCacheInfo"));// 此处曾报空指针
			ai.setInput("CacheUri", chacheUri);
		} catch (Exception e) {
			Looper.prepare();
			System.out.println("exception:e=" + e);
			// CustomToast.makeText(UpnpApp.context, "连接中断，请重新连接设备",
			// Toast.LENGTH_SHORT).show();
//			UpnpApp.mainHandler.showAlert(R.string.device_disconnect_alert);
			UpnpApp.reconnect();
			Looper.loop();
			return;
		}

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("GetCacheInfo success");
				String jsonCacheInfo = arg0.getOutput("CacheInfo").getValue()
						.toString();
				System.out.println("jsonCacheInfo=" + jsonCacheInfo);
				if(jsonCacheInfo.length()>2){
					new JsonUtil().dealCacheInfo(jsonCacheInfo, chacheUri);
				}else{
					System.out.println("jsonCacheInfo.length()<=2");
				}
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1,
					String arg2) {
				System.out.println("GetCacheInfo failure");
			}
		});

	}
	
	public void getCacheProgress(final String cacheUri){// new Cacher().getCacheProgress(
		ActionInvocation ai=new ActionInvocation(UpnpApp.cacheControlService.getAction("GetCacheProgress"));
		ai.setInput("CacheUri", cacheUri);
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			
			@Override
			public void success(ActionInvocation arg0) {
				Log.e("BUG974", TAG+"getCacheProgress success ");
				
				String jsonCacheProgress=arg0.getOutput("CacheProgress").getValue().toString();
				Log.e("BUG974", TAG+"jsonCacheProgress="+jsonCacheProgress);
				new JsonUtil().dealjsonCacheProgress(jsonCacheProgress,cacheUri);
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e("BUG974", TAG+"getCacheProgress failure ");
			}
		});		
	}

}
