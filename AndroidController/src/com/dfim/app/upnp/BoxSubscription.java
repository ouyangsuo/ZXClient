package com.dfim.app.upnp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.lastchange.LastChange;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.dfim.app.activity.WebListenActivity;
import com.dfim.app.common.CrashHandler;
import com.dfim.app.common.MainHandler;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.util.PullXmlUtil;
import com.union.cellremote.R;

public class BoxSubscription extends SubscriptionCallback {

	private final String TAG = "BoxSubscription";
	private LastChange lastChange;

	public BoxSubscription(Service service) {
		super(service);
	}

	@Override
	protected void ended(GENASubscription arg0, CancelReason arg1, UpnpResponse arg2) {
		Log.e(TAG, "boxSub ended");
		CrashHandler.getInstance().saveCrashInfo2FileII("boxSub ended: CancelReason=" + arg1 + ";UpnpResponse=" + arg2);
//		TabMusicActivity.receiveBoxSub();
//		MyMusicActivity.receiveBoxSub();

		WatchDog.reboxcontrolservice = 1;
	}

	@Override
	protected void established(GENASubscription arg0) {
		Log.e(TAG, "boxSub established");
		WatchDog.boxSubFailCount = 0;
		CrashHandler.getInstance().saveCrashInfo2FileII("boxSub established: time=" + new Date(System.currentTimeMillis()));
	}

	/**
	 * 一开始运行程序的时候利用创建sevice，执行此方法，访问是否有购买
	 */
	@Override
	protected void eventReceived(GENASubscription arg0) {
		Log.e(TAG, "eventReceived");
		Log.e("BUG975", "9>>"+TAG+"eventReceived() xml="+arg0.getCurrentValues().get("LastChange").toString());
		
		synchronized (BoxSubscription.this) {
			WatchDog.reboxcontrolservice = 5;//标识订阅已经开启
			
			//处理第一次订阅
			if (WatchDog.buySubState == 0) {
				WatchDog.clearCacheProductType = 5;
				UpnpApp.context.sendBroadcast(new Intent("updateboxControlReceiver"));
			}
			WatchDog.buySubState = 1;
			
			try {
				//拿到订阅信息字符串
				String xml = arg0.getCurrentValues().get("LastChange").toString();
				Log.e(TAG, "xml=" + xml);
				byte[] by = xml.getBytes();
				InputStream is = new ByteArrayInputStream(by);
				String str = PullXmlUtil.getData(is);
				
				if (str != null && str != "") {

					if (str.startsWith("cloudchange")) {// 操作云数据：转到本地/云端（删除）
						/*
						 * <Event
						 * xmlns="urn:schemas-upnp-org:metadata-1-0/AVT/">
						 * <InstanceID val="0"><Hassyn val=
						 * "cloudchange:1380191845606,type:1,5,10,ids:11233-754554,oper:1,5"
						 * /></InstanceID></Event> 说明：type :1专辑 5单曲 15主题
						 * oper:1删除 5同步本地
						 */
						// [cloudchange:1382427837849, type:1,
						// ids:1365149241679, oper:5]
						String[] str1 = str.split(",");
						String type = str1[1];
						String ids = str1[2];
						String oper = str1[3];

						// if(type.indexOf("15")!=-1){ids:1365149239413
						// WatchDog.clearCacheProductType=15;
						// }

						new BoxControl().getCloudStates(Integer.parseInt(type.substring(type.indexOf(":") + 1)), Integer.parseInt(oper.substring(oper.indexOf(":") + 1)), ids.substring(ids.indexOf(":") + 1));
					}

					/*
					 * 处理网络试听失败
					 */
					else if (str.startsWith("listenfailure")) {
						UpnpApp.mainHandler.showAlert(R.string.store_listen_failure_alert);
						finishWebListenActivity();
						
					} else if(str.startsWith("playlistUpdate")){
						// TODO 播放列表更新
						
					}  else if(str.startsWith("usbInsert")){
						// 插入外联设备
						String msgText = UpnpApp.mainHandler.getString(R.string.usb_insert_info);
//						UpnpApp.mainHandler.showCommonMsg(MainHandler.USB_INSERT, msgText);
						/*if(ExternalDeviceFragment.IS_ALIVE){
							ExternalDeviceFragment.hasExternalDevice = true;
							ExternalDeviceFragment.getCurrentInstance().openFirstDir();
						}*/
						UpnpApp.mainHandler.showInfo(msgText);
						UpnpApp.mainHandler.sendEmptyMessage(MainHandler.USB_INSERT);
						
					}  else if(str.startsWith("usbDelete")){
						// 拔出外联设备
						String msgText = UpnpApp.mainHandler.getString(R.string.usb_remove_info);
//						UpnpApp.mainHandler.showCommonMsg(MainHandler.USB_REMOVE, msgText);
						/*if(ExternalDeviceFragment.IS_ALIVE){
							ExternalDeviceFragment.hasExternalDevice = false;
							ExternalDeviceFragment.getCurrentInstance().showNoUsbdeviceDialog();
						}*/
						UpnpApp.mainHandler.showInfo(msgText);
						UpnpApp.mainHandler.sendEmptyMessage(MainHandler.USB_REMOVE);
					} 

					else {// 购买音乐操作
							// UpnpApp.context.sendBroadcast(new
							// Intent("updateboxControlReceiver"));
						WatchDog.clearCacheProductType = 5;
						new BoxControl().buyHandler();
					}

				}

			} catch (Exception e) {
				System.out.println("报错。。。");
				e.printStackTrace();
			}
			// lastChange.getEventedValue(new
			// UnsignedIntegerFourBytes(0),AVTransportVariable.TransportState.class);
		}
	}

	private void finishWebListenActivity() {
		if (!WatchDog.isWebListenActivityRunning) {
			return;
		}

		List<Activity> activities = ExitApplication.getInstance().getActivityList();
		for (int i = activities.size() - 1; i >= 0; i--) {
			Activity a = activities.get(i);
			if (a instanceof WebListenActivity) {
				a.finish();
				break;
			}
		}
		activities = null;
	}

	@Override
	protected void eventsMissed(GENASubscription arg0, int arg1) {
		Log.e(TAG, "boxSub eventsMissed");
		CrashHandler.getInstance().saveCrashInfo2FileII("boxSub eventsMissed: time=" + new Date(System.currentTimeMillis()));
	}

	@Override
	protected void failed(GENASubscription arg0, UpnpResponse arg1, Exception arg2, String arg3) {
		Log.e(TAG, "boxSub failed");
		CrashHandler.getInstance().saveCrashInfo2FileII("boxSub failed: ex=" + arg2);

		WatchDog.boxSubFailCount++;
		if (WatchDog.boxSubFailCount <= 3) {
//			TabMusicActivity.receiveBoxSub();
//			MyMusicActivity.receiveBoxSub();
		} else {
			UpnpApp.reconnect();
		}

	}
}
