package com.kitty.poclient.activity;

import java.util.ArrayList;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.kitty.poclient.R;
import com.kitty.poclient.adapter.LoginAdapter;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.CroutonHelper;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.models.StateModel;
import com.kitty.poclient.thread.SyncAsyncTask;
import com.kitty.poclient.upnp.BoxControl;
import com.kitty.poclient.upnp.BoxDevice;
import com.kitty.poclient.upnp.MyUpnpServiceImpl;
import com.kitty.poclient.util.DeviceUtil;
import com.kitty.poclient.util.SystemUtil;
import com.kitty.poclient.widget.CustomToast;
import com.kitty.poclient.widget.LoginView;
import com.kitty.poclient.widget.StandardCustomDialog;
import com.kitty.poclient.widget.SyncView;
import com.kitty.poclient.widget.LoginView.LoginViewListener;

//startActivity,serviceConnection,.search(,828
public class LoginActivity extends Activity {

	private static final String TAG = LoginActivity.class.getSimpleName()+":";

	private LoginView view;
	private SyncView syncView;
	private StateModel model;
	private LoginAdapter adapter;
	private ArrayList<BoxDevice> devices = new ArrayList<BoxDevice>();

	public static final String BUNDLE_STATE = "state";
	private StandardCustomDialog dialog;
	
	private static final String TARGET_DEVICE_PREFIX = "union";
	private static final String DFIM = "dFiM";
	private static final String XXBOX = "xxbox"; 
	private static final String CURRENT_DEVICE_TYPE = DFIM; //<!-- 两版本发布修改处 7-2-->

	private DeviceChangeListener deviceChangeListener = new DeviceChangeListener();

	public static final int UPGRADE_PROGRESS = 0;
	public static final int SYNC_FINISHED = 1;
	public static final int DEVICE_REMOVED_WHILE_ACCESSING = 5;
	public static final int MY_NETWORK_DEAD = 6;
	public static final int BOX_VERSION_LOW = 7;
	public static final int BOX_VERSION_OK = 8;
	
	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPGRADE_PROGRESS:
				if (syncView != null) {
					syncView.setPercentage(msg.arg1);
					if (msg.arg1 == 100) {
						view.setHint(getString(R.string.login_hint_sync_complete), View.VISIBLE);
					}
				}
				break;
				
			case SYNC_FINISHED:
				devices.clear();
				LoginActivity.this.finish();
				Log.i(TAG, "LoginActivity Finished");
				break;								
				
			case DEVICE_REMOVED_WHILE_ACCESSING:
//				reconnect();
				model.setState(StateModel.STATE_ERROR);
				break;
				
			case MY_NETWORK_DEAD:
				CustomToast.makeText(LoginActivity.this, getResources().getString(R.string.im_out_of_network), Toast.LENGTH_LONG).show();
				break;
				
			case BOX_VERSION_LOW:
				Log.e("0221", TAG+"deal BOX_VERSION_LOW");	
				model.setState(StateModel.STATE_BOX_VERSION_LOW);//fuck
				break;
				
			case BOX_VERSION_OK:
				Log.e("0221", TAG+"deal BOX_VERSION_OK");	
				model.setState(StateModel.STATE_SYNC);
				break;
				
			default:
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.i("Reconnect", "LoginActivity onCreate");
		Log.e("BUG853", TAG+" onCreate()");
		getScreenParams();
//		ExitApplication.getInstance().addActivity(this);
//		WatchDog.currentActivities.add(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);		
		
		startDeviceNetworkCheckingThread();//开启线程监听移动设备网络状态
//		updateControllerVersionIfNeccessary();
		
		adapter = new LoginAdapter(this, devices);
		model = StateModel.getInstance();
		view = (LoginView) getLayoutInflater().inflate(R.layout.layout_login, null);
		view.setListener(viewListener);
		view.setAdapter(adapter);
		setContentView(view);
		
		initState();
		SystemUtil.getLocalMacAddress();
		bindBroadcastReceiver();
		
		// 如果由babyNotMineReceiver触发则显示提示信息
		checkIntentFrom();
	}
	
	private void startDeviceNetworkCheckingThread() {
		new DeviceUtil(this).startDeviceNetworkCheckingThread(this, Constant.MY_CONNECTION_TIMEOUT_MILLIS, mHandler, MY_NETWORK_DEAD);
	}
	

	@Override
	protected void onResume() {
		doResearchIfCommanded();
		super.onResume();
	}
	
	private void doResearchIfCommanded() {
		if (WatchDog.researchFlag) {
			doOnServiceConnected(WatchDog.researchComponentName, WatchDog.researchIBinder);
			WatchDog.researchFlag = false;
		}
	}
	
	private void checkIntentFrom() {
		Log.e("BUG896", TAG+"checkIntentFrom");
		
		String intnetFrom=getIntent().getStringExtra("from");
		if("babyNotMineReceiver".equals(intnetFrom)){
//			UpnpApp.mainHandler.showAlert(R.string.device_occupated);
			CustomToast.makeText(this, getResources().getString(R.string.device_occupated), Toast.LENGTH_LONG).show();
		}
	}

	private void getScreenParams() {
		new DeviceUtil(this).getScreenResolution();
	}

	private void initState() {
		
		Log.i("Reconnect", "LoginActivity initState");
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
//			doOnDestroy();
			
			int state = bundle.getInt(BUNDLE_STATE, StateModel.STATE_LOADING);
			if (state == StateModel.STATE_LOADING) {
				if (devices != null)
					devices.clear();
				WatchDog.choseboxObj = null;
			}
			
			bindService(new Intent(this, MyUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
			model.setState(state);
		} else {
			bindService(new Intent(this, MyUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
			model.setState(StateModel.STATE_LOADING);
		}
	}

	private void bindBroadcastReceiver() {
		registerReceiver(noWifiAvailableReceiver, new IntentFilter(MyUpnpServiceImpl.NOWIFIAVAILABLE));
		registerReceiver(dealWithUpnpTimeoutOrFailureReceiver, new IntentFilter(Constant.ACTION_DEAL_STREAMCLIENT_TIMEOUT_OR_FAILURE));
	}

	/*
	 * received broadcast while init upnp router error
	 */
	private BroadcastReceiver noWifiAvailableReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (MyUpnpServiceImpl.NOWIFIAVAILABLE.equals(action)) {
				model.setState(StateModel.STATE_ERROR);
			}
		}

	};

//	发送广播：UpnpApp.sendBroadcast(Constant.ACTION_);
	private BroadcastReceiver dealWithUpnpTimeoutOrFailureReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BUG828", "dealWithSocketTimeoutExceptionReceiver onReceive()");
			dealWithUpnpTimeoutOrFailure();
		}
		
	};

	@Override
	protected void onDestroy() {
		Log.e("BUG853", TAG+" onDestroy()");
		doOnDestroy();
		super.onDestroy();
	}

	private void doOnDestroy() {
		if (UpnpApp.upnpService != null) {
			UpnpApp.upnpService.getRegistry().removeListener(deviceChangeListener);
		}
		unbindService(serviceConnection);
		view.destroy();
		model.dispose();
		unregisterReceivers();
	}

	private void unregisterReceivers() {
		unregisterReceiver(noWifiAvailableReceiver);
		unregisterReceiver(dealWithUpnpTimeoutOrFailureReceiver);
	}
	

	/*
	 * received event from view
	 */
	private LoginView.LoginViewListener viewListener = new LoginViewListener() {

		@Override
		public void onViewChange(int message, Object data) {
			switch (message) {
				case LoginView.MESSAGE_CONNECT:
					Log.i("Reconnect", "onViewChange MESSAGE_CONNECT");
					connect();
					break;
				case LoginView.MESSAGE_CHOSSE_DEVICE:
					Log.i("Reconnect", "onViewChange MESSAGE_CHOSSE_DEVICE");
					chooseDevice(Integer.parseInt((String) data));
					break;
				case LoginView.MESSAGE_RECONNECT:
					Log.i("Reconnect", "onViewChange MESSAGE_RECONNECT");
					reconnect();
					break;
				case LoginView.MESSAGE_CANCEL:
					Log.i("Reconnect", "onViewChange MESSAGE_CANCEL");
					model.setState(StateModel.STATE_ERROR);
					break;
				case LoginView.MESSAGE_START_SYNC:
					Log.i("Reconnect", "onViewChange MESSAGE_START_SYNC");
					
					//开始同步数据
					startSync((SyncView) data);
					
					break;
			}
		}
	};
	
	/**
	 * 选择盒子后，开始同步数据
	 * @param data 登录界面上显示的UI 
	 */
	private void startSync(SyncView data) {
		this.syncView = data;
		syncView.setName(((BoxDevice) WatchDog.choseboxObj).toString());
		SyncAsyncTask task = new SyncAsyncTask();
		task.execute(mHandler);
	}

	private void reconnect() {
		Log.i("Reconnect", "reconnecting......");
		if (UpnpApp.upnpService != null) {
			UpnpApp.upnpService.getRegistry().removeListener(deviceChangeListener);
		}
		deviceChangeListener=new DeviceChangeListener();
		
		unbindService(serviceConnection);
		bindService(new Intent(this, MyUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
		
		model.setState(StateModel.STATE_LOADING);
	}

	private void chooseDevice(int position) {
		Log.e("0221", "chooseDevice()");
		BoxDevice device=devices.get(position);
		String deviceDescritorURL = ((RemoteDevice)device.getDevice()).getIdentity().getDescriptorURL().toString();
		Constant.DEVICE_IP_ADDRESS=getIpAddress(deviceDescritorURL);
		Log.e("0221", "Constant.DEVICE_IP_ADDRESS=" + Constant.DEVICE_IP_ADDRESS);
		
		WatchDog.choseboxObj = devices.get(position);
		adapter.notifyDataSetChanged();
	}
	
	private String getIpAddress(String deviceDescritorURL) {
		String ip = deviceDescritorURL.substring(deviceDescritorURL.indexOf("http://") + 7, deviceDescritorURL.lastIndexOf(":"));
		return ip;
	}

	private void connect() {
		if (WatchDog.choseboxObj == null) {
			String croutonText = getResources().getString(R.string.login_toast_not_chose_device);
			CroutonHelper.showAlertCrouton(this, croutonText);
			return;
		}
//		initServices((BoxDevice) WatchDog.choseboxObj);
//		getBoxVersion();
		
//		if(true){
//			model.setState(StateModel.STATE_ERROR);
//		}else{
//			model.setState(StateModel.STATE_SYNC);
//		}
	}

//	private void getBoxVersion() {
//		new BoxControl().getBoxVersion(this);
//	}

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			UpnpApp.upnpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			Log.i("Reconnect", "onServiceConnected begin");
			saveResearchArguments(name,service);
			doOnServiceConnected(name,service);
		}
	};

	private class DeviceChangeListener extends DefaultRegistryListener {

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			Log.e("DeviceChangeListener", TAG+"localDeviceAdded()");
			deviceAdd(device);
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			Log.e("DeviceChangeListener", TAG+"localDeviceRemoved()");
			deviceRemoved(device);
		}

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			Log.e("DeviceChangeListener", TAG+"remoteDeviceAdded()="+device.getDetails().getFriendlyName());
			deviceAdd(device);
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
			Log.e("DeviceChangeListener", TAG+"remoteDeviceDiscoveryFailed()");
			deviceRemoved(device);
		}

		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
			Log.e("DeviceChangeListener", TAG+"remoteDeviceDiscoveryStarted()");
			deviceAdd(device);
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			Log.e("BUG828", TAG+"remoteDeviceRemoved()="+device.getDetails().getFriendlyName());
			Log.e("DeviceChangeListener", TAG+"remoteDeviceRemoved()");
			deviceRemoved(device);
//			notifyIfAccessingRemovedDevice();
		}
		
		/**
		 * 判断是否为本公司的目标设备。
		 * @param device 获取的设备
		 * @return true / false
		 */
		private boolean isTargeDevice(final Device device){
			boolean isTarge = false;
			String manufacturerName = device.getDetails().getManufacturerDetails().getManufacturer();
			if(manufacturerName.startsWith(TARGET_DEVICE_PREFIX)){
				Log.i(TAG, "发现union device: " +  manufacturerName);
				/*if(CURRENT_DEVICE_TYPE.equals(DFIM)){
					if(!manufacturerName.contains(XXBOX)){
						isTarge = true;
					}
				} else if(CURRENT_DEVICE_TYPE.equals(XXBOX)){
					if(!manufacturerName.contains(DFIM)){
						isTarge = true;
					}
				}*/
				isTarge = true;
			} else {
				Log.i(TAG, "发现其他device: " +  manufacturerName);
			}
			
			return isTarge;
		}
		
		public void deviceAdd(final Device device){			
			//过滤设备
			boolean allowToAdd = isTargeDevice(device);
			if(allowToAdd){
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (device.isFullyHydrated()) {
							if (model.getState() == StateModel.STATE_LOADING) {
								model.setState(StateModel.STATE_CHOOSE);
							}
							BoxDevice d = new BoxDevice(device);
							if (devices.contains(d)) {
								devices.remove(d);
								devices.add(d);
							} else {
								devices.add(d);
							}
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		}

		public void deviceRemoved(final Device device) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					devices.remove(new BoxDevice(device));
					adapter.notifyDataSetChanged();
					if (devices.size() == 0) {
						model.setState(StateModel.STATE_LOADING);
					}
				}
			});
		}
	}

//	private void initServices(BoxDevice deviceDisplay) {
//		Device rootDevice = deviceDisplay.getDevice();
//		WatchDog.currentDevice = "当前设备：" + rootDevice.getDetails().getFriendlyName();
//
//		UpnpApp.BOXUDN = deviceDisplay.getDevice().getIdentity().getUdn();
//		UpnpApp.initAllServices(rootDevice);
//	}

//	public void notifyIfAccessingRemovedDevice() {
//		Log.e("BUG828", TAG+"notifyIfAccessingRemovedDevice()");
//		if(model.getState()==StateModel.STATE_SYNC){
//			mHandler.sendEmptyMessage(DEVICE_REMOVED_WHILE_ACCESSING);
//			UpnpApp.mainHandler.showAlert(R.string.device_accessing_removed);
//		}
//	}

	protected void saveResearchArguments(ComponentName name, IBinder service) {
		if (WatchDog.researchFlag) {
			WatchDog.researchFlag = false;
		}
		WatchDog.researchComponentName = name;
		WatchDog.researchIBinder = service;
	}

	protected void doOnServiceConnected(ComponentName name, IBinder service) {		
		UpnpApp.upnpService = (AndroidUpnpService) service;
		devices.clear();
		UpnpApp.upnpService.getRegistry().addListener(deviceChangeListener);
		for (Device device : UpnpApp.upnpService.getRegistry().getDevices()) {
			deviceChangeListener.deviceAdd(device);
		}
		UpnpApp.upnpService.getControlPoint().search();

		Log.i("Reconnect", "UpnpApp.upnpService.getControlPoint().search()");
	}
	
	private void dealWithUpnpTimeoutOrFailure(){
		Log.e("BUG828", TAG+"dealWithUpnpTimeoutOrFailure()");
		if(model.getState()==StateModel.STATE_SYNC){
			mHandler.sendEmptyMessage(DEVICE_REMOVED_WHILE_ACCESSING);
			UpnpApp.mainHandler.showAlert(R.string.streamclient_timeout_or_failure);
		}
	}
	
}
