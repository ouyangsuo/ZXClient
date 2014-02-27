package com.dfim.app.activity;

import java.io.File;
import java.util.ArrayList;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dfim.app.adapter.BasePagerAdapter;
import com.dfim.app.common.Constant;
import com.dfim.app.common.MainHandler;
import com.dfim.app.common.MymusicManager;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.MainFragment;
import com.dfim.app.fragment.MenuFragment.OnMenuChangedListener;
import com.dfim.app.fragment.MenuFragment.OnSearchViewClickListener;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.fragment.PlaylistFragment;
import com.dfim.app.fragment.TabMusicFragment;
import com.dfim.app.fragment.TabWebFragment;
import com.dfim.app.fragment.setting.SettingsFragment;
import com.dfim.app.fragment.usb.ExternalDeviceFragment;
import com.dfim.app.models.StateModel;
import com.dfim.app.upnp.AVTransportSubscriptionCallback;
import com.dfim.app.upnp.BoxSubscription;
import com.dfim.app.upnp.CacheControlSubscriptionCallback;
import com.dfim.app.upnp.MyUpnpServiceImpl;
import com.dfim.app.util.DialogUtil;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.util.FileUtil;
import com.dfim.app.util.UpdateUtil;
import com.dfim.app.widget.StandardCustomDialog;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.union.cellremote.R;

public class MainActivity extends SlidingBaseActivity implements
		MainFragment.OnMainChangedListener, OnMenuChangedListener,
		OnSearchViewClickListener {

	private static final String TAG = MainActivity.class.getSimpleName() + ":";

	public static final int MAIN_PAGE_ITEM_NUM = 0;
	public static final int PLAYER_PAGE_ITEM_NUM = 1;
	public static final int PLAYLIST_PAGE_ITEM_NUM = 2;
	public static final int ENDED = 2;
//	public static final int SHOW_BOX_UPDATE_NOTIFICATION = 3;
	public static final int SHOW_CONTROLLER_UPDATE_NOTIFICATION = 4;
	public static final int VERSION_UPDATE_DIALOG_ON_POSITIVEBTN_CLICK = 5;
	public static final int VERSION_UPDATE_DIALOG_ON_NEGATIVEBTN_CLICK = 6;
	public static Handler mHandler;
	
//	private static boolean APP_FIRST_OPEN = true;

	public MainActivity() {
		super(R.string.viewpager);
	}

	private StandardCustomDialog dialog;
	private NotificationManager notificationManager;
	private Notification notification;

	private CustomViewPager mainViewPager;
	private MainFragment mainFragment;
	private boolean canSliding = true;
	private AVTransportSubscriptionCallback avTransportSubscriptionCallback;
	private BoxSubscription boxSub;
	private CacheControlSubscriptionCallback cacheSub;
	public ImageButton btnPlayer;
	
//	private static final int OPEN_MENU = 1;
//	private static final int CLOSE_MENU = 0;
	
	private BroadcastReceiver dealWithUpnpTimeoutOrFailureReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BUG828", "dealWithSocketTimeoutExceptionReceiver onReceive()");
			dealWithUpnpTimeoutOrFailure();
		}
		
	};
	
	private BroadcastReceiver finishMainActivityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			WatchDog.clearData();
			finish();
			
			Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt(LoginActivity.BUNDLE_STATE, StateModel.STATE_ERROR);
			loginIntent.putExtras(bundle);
			startActivity(loginIntent);
		}
	};

	private BroadcastReceiver errorMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra("msg");

			if ("".equals(msg) || msg == null) {
//				CustomToast.makeText(MainActivity.this, "音乐陶冶情操", Toast.LENGTH_SHORT).show();
			} else {
//				CustomToast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
				UpnpApp.mainHandler.showCommonMsg(MainHandler.SHOW_ALERT, msg);
			}
		}
	};

	private BroadcastReceiver babyNotMineReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BUG896", TAG + "babyNotMineReceiver onReceive");
			UpnpApp.mainHandler.showAlert(R.string.device_occupated);

//			Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
//			intent2.putExtra("from", "babyNotMineReceiver");
//			startActivity(intent2);
		}
	};

	private BroadcastReceiver initBtnPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println(TAG + "initBtnPlayerReceiver onReceive");
			if(WatchDog.currentState.equals(PlayerFragment.PLAYING)){
				mainViewPager.setScrollable(true);
//				btnPlayer.setVisibility(View.VISIBLE);
				//TODO BUG #805 【AndroidController】无正在播放音乐时，取消向左滑动滑出正在播放页面和无正在播放音乐的提示。
			}
			refreshPlayStatus();
		}
	};
	
    private BroadcastReceiver endedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "ended"){
                int reason = intent.getIntExtra("reason",0);
                if(reason == CancelReason.DEVICE_WAS_REMOVED.ordinal()){
                    if(mHandler != null){
                    	mHandler.sendEmptyMessage(ENDED);
                    }
                }
            }
        }
    };

    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.e("软件升级", "hello, i'm testReceiver");
    		notificationManager.cancelAll();
    		unregisterDownloadCompleteReceiver();
    		installAPKFile();
    	}
    };
	
    private BroadcastReceiver apkDownloadCompleteReceiver;
	
	public void refreshPlayStatus() {
		if (btnPlayer != null) {
			UIHelper.initMusicFragmentBtnPlayer(btnPlayer, getResources(), null);
		}
	}

	protected void dealWithUpnpTimeoutOrFailure() {
		finish();
		UpnpApp.reconnect();
		UpnpApp.mainHandler.showAlert(R.string.streamclient_timeout_or_failure);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		MymusicManager.mainActivity = this;
		UpnpApp.mainActivity = this;		
		ExitApplication.getInstance().addActivity(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏

		setOnMenuChangedListener(this);
		setOnSearchViewClickListener(this);
		initMainViewPager();

		registerReceivers();
//		bindService(new Intent(this, MyUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
		
//		// 更新控制端版本
//		if(!WatchDog.versionsUpdateNotificationShown){
//			updateControllerVersionIfNeccessary();
//			WatchDog.versionsUpdateNotificationShown=true;
//		}
		
	}

//	private void updateControllerVersionIfNeccessary() {
//		new UpdateUtil(this, mHandler, SHOW_CONTROLLER_UPDATE_NOTIFICATION).updateControllerVersionIfNeccessary();
//	}

	private void initMainViewPager() {
		mainViewPager = new CustomViewPager(this);
		mainViewPager.setId(1);

		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		
		mainFragment = new MainFragment();
		mainFragment.setOnMainChangedListener(this);

		fragments.add(mainFragment);
		fragments.add(new PlayerFragment(this));
		fragments.add(new PlaylistFragment(this));

		BasePagerAdapter mainAdapter = new BasePagerAdapter(getSupportFragmentManager(), fragments);
		mainViewPager.setAdapter(mainAdapter);
		mainViewPager.setCurrentItem(MAIN_PAGE_ITEM_NUM);
		
		if(WatchDog.currentState.equals(PlayerFragment.STOPPED)){
			//TODO BUG #805 【AndroidController】无正在播放音乐时，取消向左滑动滑出正在播放页面和无正在播放音乐的提示。
/*			mainViewPager.setScrollable(false);
			btnPlayer.setVisibility(View.INVISIBLE);*/
		}
		
		mainViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				
				if (position == PLAYER_PAGE_ITEM_NUM) {
					if (!WatchDog.checkMediaReady()) {
						Log.i(TAG, "当前没有正在播放的音乐，onPageSelected, position=" + position);
//						mainViewPager.setScrollable(false);
						//TODO BUG #805 【AndroidController】无正在播放音乐时，取消向左滑动滑出正在播放页面和无正在播放音乐的提示。
					}
				}

				setSlidingMode(position);
			}

			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
				// Log.i(TAG, "onPageScrolled, position=" + position);
			}

			@Override
			public void onPageScrollStateChanged(int position) {
				// Log.i(TAG, "onPageScrollStateChanged, position=" + position);
			}
		});
		
		setContentView(mainViewPager);
		
		mHandler = new Handler(){
			//打开程序展开侧滑菜单
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
					case ENDED:
						// eshine test: demo reconnect
	//					reconnect();
						finish();
						UpnpApp.reconnect();
						break;
						
//					case SHOW_BOX_UPDATE_NOTIFICATION:
//						showBoxUpdateDialog();
//						showBoxUpdateNotification();
//						break;
						
					case SHOW_CONTROLLER_UPDATE_NOTIFICATION:
						showVersionUpdateDialog(msg.getData().getString("currentVersion"),msg.getData().getString("latestVersion"));
						break;
						
					case VERSION_UPDATE_DIALOG_ON_POSITIVEBTN_CLICK:
						break;
						
					case VERSION_UPDATE_DIALOG_ON_NEGATIVEBTN_CLICK:
						startVersionUpdate();
						break;
				}
			}
		};
		
		//eshine test: demo reconnect
//		mHandler.sendEmptyMessageDelayed(ENDED, 4000);
	}
	
	private void showVersionUpdateDialog(String currentVersion,String latestVersion) {		
		String msg="v"+latestVersion+"更新内容\n"+WatchDog.latestVersionDescription;
		String title=getResources().getString(R.string.controller_version_update_dialog_title);
		String positiveBtnText=getResources().getString(R.string.controller_version_update_dialog_positive);
		String negativeBtnText=getResources().getString(R.string.controller_version_update_dialog_negative);
		
		new DialogUtil(this).showDialog(title, msg, positiveBtnText, negativeBtnText, mHandler, VERSION_UPDATE_DIALOG_ON_POSITIVEBTN_CLICK, VERSION_UPDATE_DIALOG_ON_NEGATIVEBTN_CLICK);
	}
		
	protected void startVersionUpdate() {
		Log.e("软件升级", TAG+"startVersionUpdate()");
		downloadAPK();
	}

	// download APK using DownloadManager
	private void downloadAPK() {
		Log.e("软件升级", TAG+"downloadAPK() start...");
		
		String serviceString = Context.DOWNLOAD_SERVICE;
		DownloadManager downloadManager;
		downloadManager = (DownloadManager) getSystemService(serviceString);

		Uri uri = Uri.parse(WatchDog.latestVersionapkDownloadUrl);
		DownloadManager.Request request = new Request(uri);
		File destinationFile=new File(Constant.APK_DOWNLOAD_PATH);
		if(destinationFile.exists()){
			destinationFile.delete();
		}
		request.setDestinationUri(Uri.fromFile(destinationFile)); 
		long originReference = downloadManager.enqueue(request);

		initAndRegisterDownloadCompleteReceiver(originReference,downloadManager);
	}

	private void initAndRegisterDownloadCompleteReceiver(final long originReference,final DownloadManager downloadManager) {
		apkDownloadCompleteReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.e("软件升级", "FILE DOWNLOAD COMPLETE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				
				long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//				Log.e("软件升级", "originReference="+originReference);
//				Log.e("软件升级", "reference="+reference);
				
				if (originReference == reference) {
					createDownloadComleteNotification();
				}
			}
		};
		
		registerReceiver(apkDownloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	protected void createDownloadComleteNotification() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.ic_launcher_small, "文件下载完成", System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("testReceiver"), PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, "应用下载完成", "点击进行安装", pendingIntent);
		notificationManager.notify(0, notification);
	}

	private void unregisterDownloadCompleteReceiver() {
		if(apkDownloadCompleteReceiver!=null){
			unregisterReceiver(apkDownloadCompleteReceiver);
			Log.e("软件升级", "apkDownloadCompleteReceiver unregistered !");
		}		
	}

	protected void installAPKFile() {
		Log.e("软件升级", "install APK...");		
		new FileUtil(this).installAPKFile(Constant.APK_DOWNLOAD_PATH);
	}

	private void setTouchModeAbove(int mode) {
		getSlidingMenu().setTouchModeAbove(mode);
	}

	private void registerReceivers() {
		registerReceiver(dealWithUpnpTimeoutOrFailureReceiver, new IntentFilter(Constant.ACTION_DEAL_STREAMCLIENT_TIMEOUT_OR_FAILURE));
		registerReceiver(finishMainActivityReceiver, new IntentFilter("finishMainActivity"));
		registerReceiver(errorMessageReceiver, new IntentFilter("errorMessageReceiver"));
		registerReceiver(babyNotMineReceiver, new IntentFilter("babyNotMineReceiver"));
		registerReceiver(initBtnPlayerReceiver, new IntentFilter("initBtnPlayerReceiver"));
		registerReceiver(endedReceiver, new IntentFilter("ended"));
		registerReceiver(testReceiver, new IntentFilter("testReceiver"));
	}

	@Override
	protected void onDestroy() {
		unregisterReceivers();
		unbindService(serviceConnection);
		super.onDestroy();
	}

	private void unregisterReceivers() {
		unregisterReceiver(dealWithUpnpTimeoutOrFailureReceiver);
		unregisterReceiver(finishMainActivityReceiver);
		unregisterReceiver(errorMessageReceiver);
		unregisterReceiver(babyNotMineReceiver);
		unregisterReceiver(initBtnPlayerReceiver);
		unregisterReceiver(endedReceiver);
		unregisterReceiver(testReceiver);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	private void showExitDialog() {
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(this);
		builder.setTitle(R.string.exit_dialog_title);
		builder.setMessage(R.string.exit_dialog_message);

		builder.setPositiveButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setNegativeButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				ExitApplication.getInstance().exit();
			}
		});

		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean isConsumed = false;
		
		Log.e(TAG, "dispatchKeyEvent:event=" + event);

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			back();
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
			Log.e(TAG, "enter onclick");
			WatchDog.tabWebFragment.search();
		}else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
			WatchDog.tabWebFragment.shortenEtText();
		}
		
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			Intent setVolumeIntent = new Intent("setVolumeReceiver");
			
			switch(event.getKeyCode()){
			
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					 Log.i("VolumeKeyTest", "音量减小 ");
					 setVolumeIntent.putExtra("volumeKeyCode", KeyEvent.KEYCODE_VOLUME_DOWN);
					 UpnpApp.mainActivity.sendBroadcast(setVolumeIntent);
					 Log.i("VolumeKeyTest", "sendBroadcast(KEYCODE_VOLUME_DOWN)");
					 isConsumed = true;
					 break;
					 
				case KeyEvent.KEYCODE_VOLUME_UP:
					 Log.i("VolumeKeyTest", " ");
					 setVolumeIntent.putExtra("volumeKeyCode", KeyEvent.KEYCODE_VOLUME_UP);
					 UpnpApp.mainActivity.sendBroadcast(setVolumeIntent);
					 Log.i("VolumeKeyTest", "sendBroadcast(KEYCODE_VOLUME_UP)");
					 isConsumed = true;
					 break;
					 
			}
			
		}
		
		return isConsumed;
	}

	private void back() {
		switch (mainViewPager.getCurrentItem()) {
		case MAIN_PAGE_ITEM_NUM:
			if (TabMusicFragment.IS_ALIVE == true) {
				Log.i(TAG, "TabMusicFragment:BACK");
				MymusicManager.tabMusicFragment.back();

			} else if (TabWebFragment.IS_ALIVE == true) {
				Log.i(TAG, "TabWebFragment:BACK");
				if (WatchDog.tabWebFragment.popbackable) {
					WatchDog.tabWebFragment.getChildFragmentManager().popBackStack();
				} else {
					showExitDialog();
				}

			}else if(ExternalDeviceFragment.IS_ALIVE){
				// 外联设备
				ExternalDeviceFragment.getCurrentInstance().back();
			} else if(SettingsFragment.IS_ALIVE){
				// 设置
				showExitDialog();
			}
			break;
		case PLAYER_PAGE_ITEM_NUM:
			mainViewPager.setCurrentItem(MAIN_PAGE_ITEM_NUM);
			break;
		case PLAYLIST_PAGE_ITEM_NUM:
			mainViewPager.setCurrentItem(PLAYER_PAGE_ITEM_NUM);
			break;
		default:
			UIHelper.showExitDialog(this);
			break;
		}
	}

	// 重连盒子
/*	private void reconnect() {
//		CustomToast.makeText(UpnpApp.context, "连接中断，请重连设备", Toast.LENGTH_LONG).show();
//		UpnpApp.mainHandler.showAlert(R.string.device_disconnect_alert);
		
		finish();
		Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(LoginActivity.BUNDLE_STATE, StateModel.STATE_ERROR);
		loginIntent.putExtras(bundle);
		startActivity(loginIntent);
	}*/

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			UpnpApp.upnpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			UpnpApp.upnpService = (AndroidUpnpService) service;
			initAllService();
		}
	};

	protected void initAllService() {
		Device device = UpnpApp.upnpService.getRegistry().getDevice(UpnpApp.BOXUDN, true);
		UpnpApp.initAllServices(device);

		callIsFirstControlAction();

		avTransportSubscriptionCallback = new AVTransportSubscriptionCallback(UpnpApp.avTransportService);
		UpnpApp.upnpService.getControlPoint().execute(avTransportSubscriptionCallback);

		cacheSub = new CacheControlSubscriptionCallback(UpnpApp.cacheControlService);
		UpnpApp.upnpService.getControlPoint().execute(cacheSub);

		boxSub = new BoxSubscription(UpnpApp.boxControlService);
		UpnpApp.upnpService.getControlPoint().execute(boxSub);
	}

	private void callIsFirstControlAction() {
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("IsFirstControl"));// IsFirstControl

		ai.setInput("Controlkey", WatchDog.macAddress);

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e(TAG, "IsFirstControl failure");
			}

			@Override
			public void success(ActionInvocation arg0) {
				Log.e(TAG, "IsFirstControl success");
			}

		});
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.i(TAG, "onTabChange get string " + tabId);
		Log.e("BUG852",TAG + "onTabChanged(" + tabId + ")");
		System.out.println(TAG + "onTabChanged(" + tabId + ")");

		int position = getTabPosition(tabId);
		WatchDog.currentTabPosition = position;

		switch (position) {
		case MainFragment.TAB_MUSIC:
//			canSliding = true;
//			mFrag.setSearchViewVisibility(View.GONE);
//			mFrag.setAdapter(getResources().getStringArray(R.array.sliding_menu_music), TabMusicFragment.getCurrentPosition(), "tab_music");
			
			canSliding = true;
			mFrag.setSearchViewVisibility(View.GONE);
			mFrag.setAdapter(getResources().getStringArray(R.array.sliding_menu_music), TabMusicFragment.getCurrentPosition(), "tab_music");
			break;
			
		case MainFragment.TAB_WEB:
			canSliding = true;
			mFrag.setSearchViewVisibility(View.VISIBLE);
			mFrag.setAdapter(getResources().getStringArray(R.array.sliding_menu_web), TabWebFragment.getCurrentPosition(), "tab_web");
			break;
			
		case MainFragment.TAB_DEVICE:
//			canSliding = false;
			break;
			
		case MainFragment.TAB_SETTING:
//			canSliding = false;
			break;
		}
		setSlidingMode(mainViewPager.getCurrentItem());
	}

	private int getTabPosition(String tabId) {
		System.out.println(TAG + "getTabPosition(" + tabId + ")");
		return MainFragment.getTabPosition(tabId, this);
	}

	@Override
	public void onMenuChanged(String currentFragment, int position) {
		System.out.println(TAG + "onMenuChanged(" + currentFragment + "," + position + ")");

		mainFragment.menuChanged(currentFragment, position);
		getSlidingMenu().toggle();
	}

	@Override
	public void onSearchViewClick() {
		System.out.println(TAG + "onSearchViewClick");
		mainFragment.onSearchClick();
		getSlidingMenu().toggle();
	}

	@Override
	public void onToggle() {
		System.out.println(TAG + "onToggle()");
		getSlidingMenu().toggle();
	}

	@Override
	public void onPlayerClick() {
		mainViewPager.setCurrentItem(1);
	}

	public void showViewpage(int pageItemNum) {
		mainViewPager.setCurrentItem(pageItemNum);
	}

	/**
	 * 
	 * @param position
	 *            which pageview
	 */
	private void setSlidingMode(int position) {
		switch (position) {
		case 0:
			if (canSliding)
				setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			else
				setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			break;
		default:
			setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			break;
		}
	}

}
