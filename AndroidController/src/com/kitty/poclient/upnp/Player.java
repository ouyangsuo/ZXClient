package com.kitty.poclient.upnp;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.kitty.poclient.R;
import com.kitty.poclient.common.UIHelper;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.fragment.PlayerFragment;
import com.kitty.poclient.util.MediaInfoUtil;
import com.kitty.poclient.util.URIParams;

public class Player {
	
	public static final String TAG = "WebListenActivity: ";
	private String currentState="";
	
	private static final String PLAYING = "PLAYING";
	private static final String PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
	private static final String STOPPED = "STOPPED";
	private static final String PREPARED = "PREPARED";
	
	private PositionInfo positionInfo;
	private String currentTime;
	private String duration;
	private int currentPercent;
	private int totalSeconds;
	private int currentSeconds;
	
	//记录操作的失败次数
	private int numSetNewUri=0;
	private int numPlayNext=0;
	private int numPlayPrev=0;
	
	public void play(String uri) {

		setNewUri(uri);	
		
	}
	
	//重连盒子
	private void showErrorMessage(String msg) {
		Looper.prepare();
		if (msg==null || "".equals(msg)) {
			UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
		}else{
			UpnpApp.mainHandler.showAlert(msg);
		}
		
		UpnpApp.reconnect();
		
		Looper.loop();
	}

	private void setNewUri(final String uri) {
			
		UpnpApp.upnpService.getControlPoint().execute(new SetAVTransportURI(new UnsignedIntegerFourBytes(0),UpnpApp.avTransportService, uri) {
				
				@Override
				public void failure(ActionInvocation arg0,UpnpResponse arg1, String arg2) {
					System.out.println("URI设置失败="+numSetNewUri);
					if (numSetNewUri<3) {
						setNewUri(uri);
						numSetNewUri++;
					}else{
						showErrorMessage("播放失败：通信异常");
						return;
					}
				}
				
				@Override
				public void success(ActionInvocation arg0) {														
					System.out.println("URI设置成功");
					
					if (WatchDog.babyNotMine==true) {
						WatchDog.babyNotMine=false;
					}
					
					currentState=PREPARED;
					WatchDog.currentUri=uri;
					numSetNewUri=0;
				}
			});
			
	}
	
	public void play() {		
		UpnpApp.upnpService.getControlPoint().execute(
				new Play(new UnsignedIntegerFourBytes(0),UpnpApp.avTransportService) {

					@Override
					public void failure(ActionInvocation arg0,UpnpResponse arg1, String arg2) {
						System.out.println("播放失败");
					}

					@Override
					public void success(ActionInvocation arg0) {
						currentState = PLAYING;
						System.out.println("播放成功");
					}
				});
	}

	public void stop() {
		Log.e(TAG, "Player stop() called");
		
		UpnpApp.upnpService.getControlPoint().execute(
				new Stop(new UnsignedIntegerFourBytes(0),UpnpApp.avTransportService) {
					
					@Override
					public void failure(ActionInvocation arg0,UpnpResponse arg1, String arg2) {
						System.out.println("停止失败");
					}
					
					@Override
					public void success(ActionInvocation arg0) {
						currentState = STOPPED;
						System.out.println("停止成功");
					}
				});
	}
	
	public void sendBoxPlayMode(final int mode) {
		String modeStr="";
		switch (mode) {
		case PlayerFragment.MODE_ORDER:
			modeStr="NORMAL";
			break;
		case PlayerFragment.MODE_SHUFFLE:
			modeStr="RANDOM";
			break;
		case PlayerFragment.MODE_ALL:
			modeStr="REPEAT_ALL";
			break;
		case PlayerFragment.MODE_SINGLE:
			modeStr="REPEAT_ONE";
			break;
		}
		
		ActionInvocation ai=new ActionInvocation(UpnpApp.avTransportService.getAction("SetPlayMode"));
		ai.setInput("InstanceID", new UnsignedIntegerFourBytes(0));
		ai.setInput("NewPlayMode", modeStr);
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			
			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("sendBoxPlayMode success:"+mode);
			}
			
			int n=0;
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("sendBoxPlayMode failure:"+mode);
				WatchDog.currentPlaymode=mode;
				UpnpApp.context.sendBroadcast(new Intent("setBackCurrentPlaymode"));
//				n++;
//				while(n<3){
//					sendBoxPlayMode(mode);
//				}				
			}
		});
	}
	
	public void setBoxPlayNext() {
		ActionInvocation ai=new ActionInvocation(UpnpApp.avTransportService.getAction("Next"));
		ai.setInput("InstanceID", new UnsignedIntegerFourBytes(0));
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			
			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("setBoxPlayNext success");
//				UpnpApp.context.sendBroadcast(new Intent("initPlayerReceiver"));
				numPlayNext=0;
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("播放上一首失败numPlayNext="+numPlayNext);
				if(numPlayNext<3){				
					setBoxPlayNext();
					numPlayNext++;			
				}else{
					showErrorMessage("下一曲播放失败：通信异常");
				}	
			}
		});
	}
	
	public void setBoxPlayPrev() {
		ActionInvocation ai=new ActionInvocation(UpnpApp.avTransportService.getAction("Previous"));
		ai.setInput("InstanceID", new UnsignedIntegerFourBytes(0));
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			
			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("setBoxPlayPrev success");
				numPlayPrev=0;
//				UpnpApp.context.sendBroadcast(new Intent("initPlayerReceiver"));
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("播放上一首失败numPlayPrev="+numPlayPrev);
				if(numPlayPrev<3){
					setBoxPlayPrev();
					numPlayPrev++;			
				}else{
					showErrorMessage("上一曲播放失败：通信异常");
				}			
			}
		});
	}

	public void getPositionInfo() {
		
	}
	
	public void getMediaInfo() {
		
		if (!UpnpApp.isUpnpAlive()) {	
			return;
		}

		UpnpApp.upnpService.getControlPoint().execute(new GetMediaInfo(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("GetMediaInfo failure");
			}
			
			@Override
			public void received(ActionInvocation arg0, MediaInfo arg1) {
				System.out.println("GetMediaInfo received");
				String currentURI=arg1.getCurrentURI();
				String mediaDuration=arg1.getMediaDuration();				
				System.out.println("currentURI="+currentURI+";mediaDuration="+mediaDuration);	
				
				if (!currentURI.startsWith("xxbox")) {
					//其它控制端在控制设备
					WatchDog.babyNotMine=true;
					return;
				}else{
					URIParams.ensureTheBabyMine(currentURI);//嫌疑！
					MediaInfoUtil.getCurrentPlayingInfo(currentURI,mediaDuration);
					
					//通知播放器更新
					Intent intent=new Intent("updateMediaInfo");
					intent.putExtra("reason", "歌单功能将在稍后为您开放");
					UpnpApp.context.sendBroadcast(intent);
					
					//通知列表更新正在播放标记
					UIHelper.refreshLocalSinglesView();
					
					//通知更新播放器图标
					UIHelper.refreshPlayerButton();
//					Log.e("send ibp broadcast", "player getMediaInfo received");
				}

			}
		});
	}

	public void getTransportInfo() {
		if (!UpnpApp.isUpnpAlive()) {
			return;
		}
		
		UpnpApp.upnpService.getControlPoint().execute(new GetTransportInfo(new UnsignedIntegerFourBytes(0),UpnpApp.avTransportService) {
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("GetTransportInfo failure");
//				getTransportInfo() ;
			}
			
			@Override
			public void received(ActionInvocation arg0, TransportInfo arg1) {
				System.out.println("GetTransportInfo received");
				String currentTransportState=arg1.getCurrentTransportState().getValue();
				System.out.println("currentTransportState="+currentTransportState);
				WatchDog.currentState=currentTransportState;
				
				//通知播放器更新
				Intent intent=new Intent("setTransportState");
				intent.putExtra("transportState", currentTransportState);
				UpnpApp.context.sendBroadcast(intent);
				
				//通知更新播放器图标
				UIHelper.refreshPlayerButton();
//				Log.e("send ibp broadcast", "player getTransportInfo received");
			}
		});		
	}

	public void getPlayMode() {
		if (!UpnpApp.isUpnpAlive()) {	
			return;
		}
		
		ActionInvocation ai=new ActionInvocation(UpnpApp.avTransportService.getAction("GetTransportSettings"));
		ai.setInput("InstanceID", new UnsignedIntegerFourBytes(0));
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			
			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("GetTransportSettings success");
				String playmode=arg0.getOutput("PlayMode").toString();
						
				if (playmode.equals("NORMAL")) {
					WatchDog.currentPlaymode=PlayerFragment.MODE_ORDER;					
				}else if(playmode.equals("RANDOM")){
					WatchDog.currentPlaymode=PlayerFragment.MODE_SHUFFLE;				
				}else if(playmode.equals("REPEAT_ONE")){
					WatchDog.currentPlaymode=PlayerFragment.MODE_SINGLE;					
				}else if(playmode.equals("REPEAT_ALL")){
					WatchDog.currentPlaymode=PlayerFragment.MODE_ALL;				
				}
				
				UpnpApp.context.sendBroadcast(new Intent("updatePlayModeReceiver"));
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("GetTransportSettings failure");
			}
		});

	}


}
