package com.dfim.app.upnp;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportState;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.dfim.app.activity.WebListenActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.CrashHandler;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.fragment.usb.ExternalDeviceFragment;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.util.URIParams;
import com.dfim.app.util.UsbFileUtil;
import com.union.cellremote.R;


public class AVTransportSubscriptionCallback extends SubscriptionCallback {
	
	private final static String TAG="AVTransportSubscriptionCallback:";
	private LastChange lastChange;

	public AVTransportSubscriptionCallback(Service service) {
		super(service);
	}

	@Override
	protected void ended(GENASubscription arg0, CancelReason reason, UpnpResponse arg2) {
		try {
			Intent intent = new Intent("ended");
			intent.putExtra("reason", reason.ordinal());
			UpnpApp.context.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void established(GENASubscription arg0) {
		Log.e(TAG, "AVSub established");
		WatchDog.avSubFailCount = 0;
		CrashHandler.getInstance().saveCrashInfo2FileII("AVSub established: time=" + new Date(System.currentTimeMillis()));
	}

	@Override
	protected void eventReceived(GENASubscription sub) {
		Log.e(TAG,"AVTransportSubscriptionCallback eventReceived");

		UnsignedIntegerFourBytes currentSequence = sub.getCurrentSequence();
		Log.e(TAG,"currentSequence=" + currentSequence);

		Map currentValues = sub.getCurrentValues();
		Log.e(TAG,"currentValues=" + currentValues);
		
		//【外联设备】盒子在播放，客户端后打开时
		boolean readUsbPlayingMediaInfo = false;
		if(ExternalDeviceFragment.currentListType==-1){
			readUsbPlayingMediaInfo = true;
			getMediaInfoForUpdateUsbFragment() ;
			updateMediaInfoForUsb(sub);
		}
		if(WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE){
		//【外联设备】-----------------------------------------
			if(!readUsbPlayingMediaInfo){
				
				getMediaInfoForUpdateUsbFragment() ;
				
				updateMediaInfoForUsb(sub);
			}
			
			
		} else { //【我的音乐】【音乐商店】-----------------------------------------
			
			try {
				// 拿到LastChange
				lastChange = new LastChange(new AVTransportLastChangeParser(), sub.getCurrentValues().get("LastChange").toString());
				Log.i(TAG, "lastChange=" + lastChange);
				
				// 拿到并处理CurrentPlayMode
				AVTransportVariable.CurrentPlayMode currentPlayMode = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentPlayMode.class);
				if (currentPlayMode != null) {
					onCurrentPlayModeChange(0, currentPlayMode.getValue().toString());
				}
				Log.i(TAG, "currentPlayMode=" + currentPlayMode);

				// 拿到并处理CurrentTrackURI
				AVTransportVariable.CurrentTrackURI currentTrackURI = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentTrackURI.class);
				if (currentTrackURI != null) {
					Log.i(TAG, "小喇叭CurrentTrackURI=" + currentTrackURI.getValue().toString());
					onCurrentTrackURIChange(0, currentTrackURI.getValue() != null ? currentTrackURI.getValue().toString() : "");
					Log.i(TAG, "CurrentTrackURI=" + currentTrackURI.getValue().toString());
				}

				// 拿到并处理TransportState
				AVTransportVariable.TransportState transportState = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.TransportState.class);
				Log.i(TAG, "transportState.getValue=");
				if (transportState != null) {
					Log.i(TAG, "transportState.getValue=" + transportState.getValue());
					if (currentTrackURI != null) {
						// 让currentTrackURI先得到处理
						Thread.sleep(500);
						onStateChange(0, transportState.getValue());
					} else {
						onStateChange(0, transportState.getValue());
					}

					// 拿到并处理CurrentTrackDuration
					AVTransportVariable.CurrentTrackDuration currentTrackDuration = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentTrackDuration.class);
					Log.i(TAG, "currentTrackDuration.getValue=");
					if (currentTrackDuration != null) {
						Log.i(TAG, "currentTrackDuration.getValue=" + currentTrackDuration.getValue());
						onCurrentTrackDurationChange(0, currentTrackDuration.getValue() != null ? currentTrackDuration.getValue().toString() : "");
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void onCurrentPlayModeChange(int i, String mode) {
		System.out.println("mode="+mode);
		
		if (mode.equals("RANDOM")) {
			WatchDog.currentPlaymode=PlayerFragment.MODE_SHUFFLE;
			System.out.println("RANDOM setok");
		}else if (mode.equals("REPEAT_ONE")) {
			WatchDog.currentPlaymode=PlayerFragment.MODE_SINGLE;
			System.out.println("REPEAT_ONE setok");
		}else if (mode.equals("REPEAT_ALL")) {
			WatchDog.currentPlaymode=PlayerFragment.MODE_ALL;
			System.out.println("REPEAT_ALL setok");
		}else if (mode.equals("NORMAL")) {
			WatchDog.currentPlaymode=PlayerFragment.MODE_ORDER;
			System.out.println("NORMAL setok");
		}else{
			System.out.println("未知播放模式");
		}
		
		Intent intent = new Intent("setCurrentPlaymodeReceiver");
		UpnpApp.context.sendBroadcast(intent);
		System.out.println("sendBroadcast: setCurrentPlaymodeReceiver");
	}

	@Override
	protected void eventsMissed(GENASubscription arg0, int arg1) {
		Log.e(TAG, "AVSub eventsMissed");
		CrashHandler.getInstance().saveCrashInfo2FileII("AVSub eventsMissed: time=" + new Date(System.currentTimeMillis()));
	}

	@Override
	protected void failed(GENASubscription arg0, UpnpResponse arg1, Exception ex, String arg3) {
		Log.e(TAG, "AVSub failed");
		CrashHandler.getInstance().saveCrashInfo2FileII("AVSub failed: ex=" + ex);

		WatchDog.avSubFailCount++;
		if (WatchDog.avSubFailCount <= 3) {
//			TabMusicActivity.receiveAVSub();
//			MyMusicActivity.receiveAVSub();
		} else {
			UpnpApp.reconnect();
		}
	}

	private void onStateChange(int instanceId, TransportState transportState) {
		System.out.println(TAG+":onStateChange:transportState="+transportState);
		
		String state = transportState.getValue();
		WatchDog.currentState = state;
		
		if (state.equals(PlayerFragment.STOPPED)) {
			if (WatchDog.mediaOutOfService==true) {
				WatchDog.mediaOutOfService=false;
//				System.out.println(TAG+":onStateChange:WatchDog.mediaOutOfService set false");
			}
			finishWebListenIfExist();
		}

		Intent intent = new Intent("setTransportState");
		intent.putExtra("transportState", state);
		UpnpApp.context.sendBroadcast(intent);

		// 半秒钟后通知各列表更新当前正在播放曲目的标记位置
		if (state.equals(PlayerFragment.PLAYING)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			UIHelper.refreshLocalSinglesView();	
			
			// 试听进度开始更新
			webListenActivityStartPlaying();
		}
		
		
		UIHelper.refreshPlayerButton();
//		Log.e("send ibp broadcast", "avsub onStateChange");
	}
	
	private void webListenActivityStartPlaying() {
		if (!WatchDog.isWebListenActivityRunning) {
			return;
		}

		List<Activity> activities = ExitApplication.getInstance().getActivityList();
		for (int i = activities.size() - 1; i >= 0; i--) {
			Activity a = activities.get(i);
			if (a instanceof WebListenActivity) {
				((WebListenActivity)a).setStatePlaying();
				break;
			}
		}
		activities = null;
	}

	private void finishWebListenIfExist() {
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

	private void onCurrentTrackURIChange(int id, String uri) {
		Log.e("BUG896", TAG+"onCurrentTrackURIChange:uri="+uri);
		
		ensureTheBabyBeMine(uri);
		URIParams.setCurrentTrackUri(uri);// 搞定WatchDog中当前播放对象、名称、演出者
		stopWebListenWhenNecessary(uri);
		
		//通知播放器更新
		Intent intent = new Intent("setCurrentTrackURI");
		intent.putExtra("currentTrackURI", uri);
		UpnpApp.context.sendBroadcast(intent);
		
		//通知列表更新
		UIHelper.refreshLocalSinglesView();
	}
	
	private void stopWebListenWhenNecessary(String uri) {
		if (!uri.startsWith("xxbox://listen") && WatchDog.isWebListenActivityRunning) {
			WatchDog.runningWebListenActivity.finish();
		}
	}

	private void ensureTheBabyBeMine(String uri) {
		Log.e("BUG896", TAG+"ensureTheBabyBeMine:uri="+uri);
		
		if (!uri.startsWith("xxbox")) {
			WatchDog.babyNotMine=true;
			//提示设备连接已被其它控制端占用，返回登录界面
			UpnpApp.context.sendBroadcast(new Intent("babyNotMineReceiver"));
		}else{
			if (WatchDog.babyNotMine==true) {
				WatchDog.babyNotMine=false;
			}
		}
		
		if (uri.startsWith("xxbox://listen?")) {
			WatchDog.mediaOutOfService=true;
			WatchDog.mediaOutOfServiceReson=UpnpApp.context.getResources().getString(R.string.mosReasonListen);
			WatchDog.currentState=PlayerFragment.STOPPED;//暂时先置为停止
			WatchDog.currentPlayingId=0L;
			UpnpApp.context.sendBroadcast(new Intent("mediaOutOfServiceReceiver"));
		}
	}

	private void onCurrentTrackDurationChange(int id, String duration) {
		Intent intent = new Intent("setCurrentTrackDuration");
		intent.putExtra("currentTrackDuration", duration);
		UpnpApp.context.sendBroadcast(intent);
	}
	
	
	private void updateMediaInfoForUsb(GENASubscription sub){
		Log.i(TAG,"usbCueType-updateMediaInfoForUsb begin, sub.getCurrentValues()=" + sub.getCurrentValues());
		try {
			// 拿到LastChange
			lastChange = new LastChange(new AVTransportLastChangeParser(), sub.getCurrentValues().get("LastChange").toString());
			Log.i(TAG, "usbCueType-lastChange=" + lastChange);
			
			// 拿到并处理CurrentPlayMode
			AVTransportVariable.CurrentPlayMode currentPlayMode = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentPlayMode.class);
			if (currentPlayMode != null) {
				onCurrentPlayModeChange(0, currentPlayMode.getValue().toString());
			}
			Log.i(TAG, "usbCueType-currentPlayMode=" + currentPlayMode);

			// 拿到并处理CurrentTrackURI
			AVTransportVariable.CurrentTrackURI currentTrackURI = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentTrackURI.class);
			if (currentTrackURI != null) {
				Log.i(TAG, "usbCueType-小喇叭CurrentTrackURI=" + currentTrackURI.getValue().toString());
				onCurrentTrackURIChange(0, currentTrackURI.getValue() != null ? currentTrackURI.getValue().toString() : "");
				Log.i(TAG, "usbCueType-CurrentTrackURI=" + currentTrackURI.getValue().toString());
			}

			// 拿到并处理TransportState
			AVTransportVariable.TransportState transportState = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.TransportState.class);
			if (transportState != null) {
				TransportState transportStateValue = transportState.getValue();
				String state = transportStateValue.getValue();
				Log.i(TAG, "usbCueType-transportStateValue.getValue()=" + state);
				Log.i(TAG, "usbCueType-1state=" + state);
				Log.i(TAG, "usbCueType-currentTrackURI=" + currentTrackURI);
				if(!state.equals(PlayerFragment.TRANSITIONING)){
					Log.i(TAG, "usbCueType-2state=" + state);
					// 让currentTrackURI先得到处理
					Thread.sleep(100);
					onStateChange(0, transportStateValue);
				}

				// 拿到并处理CurrentTrackDuration
				AVTransportVariable.CurrentTrackDuration currentTrackDuration = lastChange.getEventedValue(new UnsignedIntegerFourBytes(0), AVTransportVariable.CurrentTrackDuration.class);
				if (currentTrackDuration != null) {
					Log.i(TAG, "usbCueType-currentTrackDuration.getValue=" + currentTrackDuration.getValue());
					onCurrentTrackDurationChange(0, currentTrackDuration.getValue() != null ? currentTrackDuration.getValue().toString() : "");
				}
			}
		} catch (Exception e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}
	private void getMediaInfoForUpdateUsbFragment() {

		UpnpApp.upnpService.getControlPoint().execute(new GetMediaInfo(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.i(TAG,"GetMediaInfo failure");
			}
			
			@Override
			public void received(ActionInvocation arg0, MediaInfo arg1) {
				String currentURI=arg1.getCurrentURI();
				String mediaDuration=arg1.getMediaDuration();	
				Log.i(TAG,"GetMediaInfo success, currentURI=" + currentURI);
				
				if(UsbFileUtil.isUsbMediaURI(currentURI)){
					currentURI = currentURI.replaceAll("%20", " ");
					
					if(UsbFileUtil.isUsbMediaURI_UsbType(currentURI)){
						WatchDog.currentListType = Constant.URI_USB;
						ExternalDeviceFragment.currentListType = Constant.URI_USB;
					} else if(UsbFileUtil.isUsbMediaURI_CueType(currentURI)){
						WatchDog.currentListType = Constant.URI_USB;
						ExternalDeviceFragment.currentListType = Constant.URI_USB;
					}
					
					//update usb playing info
					WatchDog.currentUri = currentURI;
					ExternalDeviceFragment.currentUri = currentURI;
					
					UsbFileUtil.GetNowPlaylist();         //playlist,
					
					//通知更新播放器按钮图标 + 更新【外联设备】文件列表的播放状态
					UIHelper.refreshPlayerButton();
				}

			}
		});
	}
}
