package com.dfim.app.fragment.usb;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dfim.app.activity.MainActivity;
import com.dfim.app.adapter.UsbFileListAdapter;
import com.dfim.app.common.Constant;
import com.dfim.app.common.MymusicManager;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.fragment.TabFragment;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.UsbFileUtil;
import com.dfim.app.widget.CustomToast;
import com.union.cellremote.R;
import com.union.cellremote.domain.Music;

@SuppressLint("ValidFragment")
public class ExternalDeviceFragment extends TabFragment {

	private static String TAG = "UsbFragment";

	public static boolean IS_ALIVE = false;
	
	public static boolean hasExternalDevice = false;

	private View view;
	private Context context;
	private static ExternalDeviceFragment currentInstance;
	
	private TextView tvTabname;
	private LinearLayout usblayout;
	private ListView usbfilelist;
	private ImageButton backButton;
	private ImageButton btnPlayer;
	private TextView currentDirPath;
	// private TextView fileinfo;

	private LinearLayout loadingDialog;
	private View loadingAnim;
	private AnimationDrawable animatiorList;
	private boolean isLoading = false;

	private LinearLayout noUsbDeviceDialog;
	private boolean hasUsb = false;

	private String tabnameString = "外联设备";

	private static final int BROWSE_FLAG_FAILURE = 0;
	private static final int BROWSE_FLAG_RECEIVE = 1;
	private static final int BROWSE_FLAG_UPDATESTATUS = 2;
	private static final int BROWSE_FLAG_LOADING = 3;
	private static final int BROWSE_FLAG_NODEVICE = 4;
	private static final int BROWSE_FLAG_NO_MEDIA_FILE = 5;

	private static final int PLAY_FLAG_UNSUPPORT = 10;

	public static final String ROOT_DIR = "/mnt/usb_storage";
	public static final String FIRST_DIR = "0";
	public static String CURRENT_DIR = FIRST_DIR;
	public static DIDLContent CURRENT_DIDL;
	private String nextDir = FIRST_DIR;
	private String parentDir = FIRST_DIR;

	public static String PLAY_URI = "";
	// <20131106 add
	public static int currentListType = -1;
	public static ArrayList<Music> currentList;
	public static String currentUri;
	public static Music currentPlayingMusic;
	public static String currentPlayingName = "";
	public static String currentArtistName = "";
	public static int currentPlayingIndex = -1;
	public static Long currentPlayingId = 0L;
	// >

	private List<DIDLObject> didlObjectList;

	private Handler browseHandler;
	private UsbFileListAdapter usbadp = null;

	public ExternalDeviceFragment() {
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = getActivity();
		currentInstance = this;
	}
	public static ExternalDeviceFragment getCurrentInstance(){
		return currentInstance;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(context).inflate(R.layout.usb, null);
		initComponent();
		initHandler();
		initBtnPlayer();
		
		/*if(CURRENT_DIR.equals(FIRST_DIR)){
			browse(FIRST_DIR);
		} else {
			browse(CURRENT_DIR);
		}*/
		browse(CURRENT_DIR);

		return view;
	}

	public void showNoUsbdeviceDialog() {
		usblayout.setVisibility(View.GONE);
		loadingDialog.setVisibility(View.GONE);
		backButton.setVisibility(ImageButton.INVISIBLE);

		noUsbDeviceDialog.setVisibility(View.VISIBLE);
	}

	private void hideNoUsbdeviceDialog() {
		noUsbDeviceDialog.setVisibility(View.GONE);
		hasUsb = true;
	}

	private void showLoadingDialog() {
		if (isLoading == false) {
			usblayout.setVisibility(View.GONE);
			loadingDialog.setVisibility(View.VISIBLE);
			loadingAnim.setBackgroundDrawable(animatiorList); // 设备加载动画
			animatiorList.start();
			isLoading = true;
		}
	}

	private void hideLoadingDialog() {
		if (isLoading == true) {
			loadingDialog.setVisibility(View.GONE);
			usblayout.setVisibility(View.VISIBLE);
			isLoading = false;
		}
	}

	private void initComponent() {
		tvTabname = (TextView) view.findViewById(R.id.tv_tabname);
		tvTabname.setText(tabnameString);

		backButton = (ImageButton) view.findViewById(R.id.btn_back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				browseParentDir();
			}
		});

		btnPlayer = (ImageButton) view.findViewById(R.id.btn_player);
		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDog.checkMediaReady()) {
					return;
				}
				MymusicManager.mainActivity.showViewpage(MainActivity.PLAYER_PAGE_ITEM_NUM);
//				context.startActivity(new Intent(context, PlayerActivity.class));
			}
		});

		// 初始化：加载对话框
		loadingDialog = (LinearLayout) view.findViewById(R.id.loading_dialog);
		loadingAnim = loadingDialog.findViewById(R.id.loading_anim);
		animatiorList = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);

		// no usb dialog
		noUsbDeviceDialog = (LinearLayout) view.findViewById(R.id.no_usb_device);

		usblayout = (LinearLayout) view.findViewById(R.id.usbDisplay);

		currentDirPath = (TextView) usblayout.findViewById(R.id.current_dir_path);
		// fileinfo = (TextView) usblayout.findViewById(R.id.fileinfo);
		usbfilelist = (ListView) usblayout.findViewById(R.id.usbfilelist);
		
		didlObjectList = new ArrayList<DIDLObject>();
		usbadp = new UsbFileListAdapter(context, didlObjectList);
		usbfilelist.setAdapter(usbadp);
		usbfilelist.setOnItemClickListener(new ItemClickListener());
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		browseHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BROWSE_FLAG_FAILURE:
					break;

				case BROWSE_FLAG_LOADING:
					showLoadingDialog();
					break;

				case BROWSE_FLAG_NODEVICE:
					showNoUsbdeviceDialog();
					break;

				case BROWSE_FLAG_NO_MEDIA_FILE:
//					CustomToast.makeText(context, "该文件夹内无适播文件", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showInfo(R.string.usb_no_file_info);
					
					// 隐藏加载对话框
					hideLoadingDialog();

					// 刷新目录和列表
					refreshPathAndFileList();
					break;

				case BROWSE_FLAG_RECEIVE:
					// 隐藏加载对话框
					hideLoadingDialog();
					hideNoUsbdeviceDialog();

					// 刷新目录和列表
					refreshPathAndFileList();
					break;

				case BROWSE_FLAG_UPDATESTATUS:
					// 显示更新状态
					break;

				case PLAY_FLAG_UNSUPPORT:
					CustomToast.makeText(context, "暂不支持此文件播放", Toast.LENGTH_SHORT).show();
					break;
				}

			}
		};
	}

	private void refreshPathAndFileList() {
		// 更新目录
		CURRENT_DIR = nextDir;
		if (!UsbFileUtil.isUsbRootDir(nextDir)) {
			parentDir = UsbFileUtil.getParentPathFromAbsolutePath(nextDir);
			// 更新为usb名称
			if (UsbFileUtil.isUsbRootDir(parentDir)) {
				tvTabname.setText(UsbFileUtil.getFileNameFromAbsolutePath(CURRENT_DIR));
			}
			// 还源为“外联设备”
			if (UsbFileUtil.isUsbRootDir(CURRENT_DIR)) {
				tvTabname.setText(tabnameString);
			}
		}

		// 刷新列表
		usbadp.notifyDataSetChanged();
		if (UsbFileUtil.isUsbRootDir(CURRENT_DIR)) {
			currentDirPath.setVisibility(TextView.GONE);
			backButton.setVisibility(ImageButton.INVISIBLE);
		} else {
			backButton.setVisibility(ImageButton.VISIBLE);
			currentDirPath.setVisibility(TextView.VISIBLE);
			currentDirPath.setText(getDisplayPathFromCurrentDir(CURRENT_DIR));
		}
	}

	private String getDisplayPathFromCurrentDir(String currentDir) {
		String displayPath = currentDir.replaceFirst(ROOT_DIR, "USB");
		String[] displayPathArray = displayPath.split("/");
		int length = displayPathArray.length;
		if (length >= 3) {
			displayPath = ".../" + displayPathArray[length - 2] + "/" + displayPathArray[length - 1];
		}
		return displayPath;
	}

	private void initBtnPlayer() {
		String currentCacheState = Music.CACHE_WAIT;
		if (WatchDog.currentPlayingMusic != null) {
			currentCacheState = WatchDog.cacheStateMap.get(WatchDog.currentPlayingMusic.getId());// 查到当前曲目缓存状态
		}

		if (WatchDog.mediaOutOfService == true && WatchDog.currentState.equals(PlayerFragment.PLAYING)) {
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
		} else if (!WatchDog.currentState.equals(PlayerFragment.PLAYING) || !Music.CACHE_DOWNLOADED.equals(currentCacheState)) {// 空指针
			btnPlayer.setImageResource(R.drawable.btn_player);
		} else {
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
		}

		// 外联设备文件播放情况
		if (WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE) {
			if (WatchDog.currentState.equals(PlayerFragment.PLAYING)) {
				AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
				btnPlayer.setImageDrawable(animationDrawable);
				animationDrawable.start();
			} else {
				btnPlayer.setImageResource(R.drawable.btn_player);
			}
		}
	}

	@Override
	public void onResume() {
		IS_ALIVE = true;
		if (hasUsb) {
			if (didlObjectList.size() == 0) {
				browse(CURRENT_DIR);
			}
		} 

		registerReceivers();
		super.onResume();
	}

	@Override
	public void onPause() {
		IS_ALIVE = false;
		
		unregisterReceivers();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void browse(String containId, String filter, long startingindex, long requestedcount) {
		// default: filter="*", startingindex=0, requestedcount=999
	}
	
	public void openFirstDir(){
		browse(FIRST_DIR);
	}
	public void back(){
		if (UsbFileUtil.isUsbRootDir(CURRENT_DIR)) {
			UIHelper.showExitDialog(getActivity());
		}else{
			 browseParentDir();
		}
	}
	public void browseParentDir() {
		if (UsbFileUtil.isUsbRootDir(CURRENT_DIR)) {
//			CustomToast.makeText(context, "当前已是根目录", Toast.LENGTH_SHORT).show();
			// backButton.setVisibility(ImageButton.INVISIBLE);
		} else {
			browse(parentDir);
		}
	}

	private void browse(String containId) {
		browseHandler.sendEmptyMessage(BROWSE_FLAG_LOADING);
		nextDir = containId;
		Browse browse = new Browse(UpnpApp.directoryService, containId, BrowseFlag.DIRECT_CHILDREN) {
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				browseHandler.sendEmptyMessage(BROWSE_FLAG_FAILURE);
			}

			@Override
			public void updateStatus(Status arg0) {
			}

			@Override
			public void received(ActionInvocation arg0, DIDLContent didl) {
				CURRENT_DIDL = didl;
				// 清空usb file list view
				didlObjectList.clear();

				// 更新usb file list view
				didlObjectList.addAll(didl.getContainers());
				didlObjectList.addAll(didl.getItems());

				// 更新UI
				if (didlObjectList.size() == 0) {
					if (UsbFileUtil.isUsbRootDir(nextDir)) {
						// 无外联设备
						browseHandler.sendEmptyMessage(BROWSE_FLAG_NODEVICE);
					} else {
						// 无适播文件
						browseHandler.sendEmptyMessage(BROWSE_FLAG_NO_MEDIA_FILE);
					}
				} else {
					browseHandler.sendEmptyMessage(BROWSE_FLAG_RECEIVE);
				}
			}
		};
		UpnpApp.upnpService.getControlPoint().execute(browse);
	}

	private BroadcastReceiver initBtnPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initBtnPlayer();

			//TODO 刷新列表 - 小喇叭
			usbadp.notifyDataSetChanged();
		}
	};

	private void registerReceivers() {
		context.registerReceiver(initBtnPlayerReceiver, new IntentFilter("initBtnPlayerReceiver"));
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(initBtnPlayerReceiver);
	}

	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				DIDLObject didlObject = didlObjectList.get(position);
				// 如果选择的是文件夹则进入子目录
				if (didlObject instanceof Container) {
					// 打開目錄
					browse(didlObject.getId());
				} else {

					ExternalDeviceFragment.currentPlayingName = didlObject.getTitle();
					WatchDog.currentPlayingName = didlObject.getTitle();

					String uri = null;
					if (UsbFileUtil.isCue(didlObject.getParentID())) {
						uri = didlObject.getId(); // Constant.URI_CUE
													// xxbox://cue?source=xxx

						WatchDog.currentListType = Constant.URI_CUE;
						ExternalDeviceFragment.currentListType = Constant.URI_CUE;
					} else {
						if (UsbFileUtil.isMusic(didlObject.getId())) {// 普通音乐
							uri = "xxbox://usb?source=" + didlObject.getId(); // xxbox://usb?source=xxx

							WatchDog.currentListType = Constant.URI_USB;
							ExternalDeviceFragment.currentListType = Constant.URI_USB;

						} else if (UsbFileUtil.isVideo(didlObject.getId())) {// 普通视频
							browseHandler.sendEmptyMessage(PLAY_FLAG_UNSUPPORT);
						} else {
							browseHandler.sendEmptyMessage(PLAY_FLAG_UNSUPPORT);
						}
					}
					if (uri != null) {
						WatchDog.currentUri = uri;
						ExternalDeviceFragment.currentUri = uri;
						
						//TODO 刷新小喇叭
						usbadp.notifyDataSetChanged();

						playMedia(uri);

					} else {

					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}

		public void playMedia(String uri) {
			Player p = new Player();
			p.play(uri);

			// update playing music list
			// UsbFileUtil.updateMusicListFromDIDLContent(currentDIDL);
			UsbFileUtil.GetNowPlaylist();

		}

	}
}
