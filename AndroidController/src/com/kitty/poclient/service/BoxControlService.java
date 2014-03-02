package com.kitty.poclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.upnp.BoxSubscription;

public class BoxControlService  extends Service {
     BoxSubscription boxControlSub ;
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	  
		if(boxControlSub!=null){
			boxControlSub.end();
			boxControlSub=null;
		}
	   Log.i("boxSub","第一次订阅");
		boxControlSub=new BoxSubscription(UpnpApp.boxControlService);		
		if(boxControlSub!=null&&UpnpApp.upnpService!=null){
			try {
				UpnpApp.upnpService.getControlPoint().execute(boxControlSub);//曾报空指针?无法getControlPoint??
			} catch (Exception e) {
				System.out.println("exception caught: e="+e);
//				CustomToast.makeText(UpnpApp.context, "连接中断，请重新连接设备", Toast.LENGTH_SHORT).show();
//				UpnpApp.mainHandler.showAlert(R.string.device_disconnect_alert);
//				startActivity(new Intent(this,BrowserActivity.class));
				UpnpApp.reconnect();
			}			
		}
	    System.out.println("BrowserActivity.upnpService="+UpnpApp.upnpService);
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		if(boxControlSub!=null){
			boxControlSub.end();
			boxControlSub=null;
		}
		Log.i("boxcontrolsub", "空");
		super.onDestroy();
	}

}
