package com.dfim.app.fragment;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dfim.app.activity.MainActivity;
import com.dfim.app.common.BroadcastManager;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.fragment.usb.ExternalDeviceFragment;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.DateTimeFormatUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.dfim.app.util.PowerfulBigMan;
import com.dfim.app.util.URIParams;
import com.dfim.app.widget.CustomToast;
import com.union.cellremote.R;
import com.union.cellremote.adapter.PlaylistMusicListAdapter;

//试听曲目
public class PlaylistFragment extends Fragment {

	private final String TAG = PlaylistFragment.class.getSimpleName()+":";//onItemC
	
	private View view;
	
	private LinearLayout llPlayer;
	private LinearLayout llPlaypic;
	private LinearLayout llVolumn;
	private LinearLayout llCache;
	private LinearLayout llLvPlaylist;
	private FrameLayout flBtnAnim;
	private ImageView ivBtnAnimArrow;
	private Thread threadBtnAnim;
	private Animation btnAnimation;

	// 控件
	private FrameLayout flControlArea;
	private FrameLayout flSbProgress;// flSbProgress.setVisibility(View.GONE)
	private Button btnPlayPause, btnNext, btnPrev;// btnPrev.seton
	private ImageButton btnBack;
	private ImageButton btnPlaylist;
	private Button btnPlaymode;
	private Button btnVolumn;
	private TextView tvCurrentTime, tvTotalTime;
	private TextView tvMusicName;
	private TextView tvArtistName;
	private SeekBar sbProgress, sbVolumn;
	private SeekBar sbCache;
	private ImageView ivPicPlaying;
	private ListView lvPlaylist;

	// 播放状态
	public static final String PLAYING = "PLAYING";
	public static final String PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
	public static final String STOPPED = "STOPPED";
	public static final String PREPARED = "PREPARED";
	public static final String TRANSITIONING = "TRANSITIONING";

	// 播放模式
	public static final int MODE_ORDER = 1;
	public static final int MODE_SHUFFLE = 2;
	public static final int MODE_SINGLE = 3;
	public static final int MODE_ALL = 4;

	// 数据
	private String currentState = "";
	private String currentTime;
	private String duration;
	private int currentPercent;
	private int totalSeconds;
	private int currentSeconds;
	private int currentVolumn;
	private int gpfail = 0;
	private int currentCachingChanger = 0;
	private Runnable progressRunnable;
	private PositionInfo positionInfo;
	private boolean mute = false;
	private boolean llVolumnShown = false;
	private boolean llLvPlaylistShown = false;
	private boolean isCaching = false;
	private boolean shouldSayHello2Death = false;// 为真时叫停所有循环
	private boolean hasEverPaused = false;// 屏幕是否曾休眠
	// private boolean isLastClickFromBtnPrev = false;
	private int countPrevClick = 0;
	// private boolean transitioningCancel = false;
	private Thread progressThread;
	private SubscriptionCallback myAvTransportSub;

	// 正在播放列表
	private ArrayList<Music> playlist;
	private PlaylistMusicListAdapter adapter;

	private final int MSG_MUTE = 1;
	private final int MSG_VOLUMN = 2;
	private final int MSG_INIT_PIC = 3;
	private final int MSG_CLEAR_PLAYING_PROGRESS = 4;
	private final int MSG_BTN_PREV = 5;
	
	private BroadcastReceiver playlistSeekPositionReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BUG839", TAG+"playlistSeekPositionReceiver onReceive()");
			seekToCurrentPlayingPosition();
		}
		
	};

	private MainActivity mainActivity;
	
	public PlaylistFragment() {
	}

	
	public PlaylistFragment(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if (totalSeconds != 0) {
					tvCurrentTime.setText(currentTime);
					tvTotalTime.setText(duration);
					sbProgress.setProgress(currentPercent);
				} else {
					getPositionInfo();
				}
				break;

			case MSG_VOLUMN:
				sbVolumn.setProgress(currentVolumn);
				break;

			case MSG_INIT_PIC:
				Bitmap bitmap = (Bitmap) msg.getData().get("bitmap");
				ivPicPlaying.setImageBitmap(bitmap);
				break;

			case MSG_CLEAR_PLAYING_PROGRESS:
				clearPlayingProgress();
				break;

			case MSG_BTN_PREV:
				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}
				if (countPrevClick == 1) {
					playThisAgain();
					countPrevClick = 0;
				} else if (countPrevClick > 1) {
					WatchDog.latestOperation = "prev";
					playPrev();
					countPrevClick = 0;
				}
				break;
			}
		}
	};

	private BroadcastReceiver playNewMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// stop();
			String uri = intent.getStringExtra("uri");
			setNewUri(uri);
			while (!currentState.equals(PREPARED) && shouldSayHello2Death == false) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			play();
		}
	};

	private BroadcastReceiver setTransportStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String transportState = intent.getStringExtra("transportState");
			System.out.println(TAG + ":setTransportStateReceiver:transportState=" + transportState);
			setTransportState(transportState);
		}
	};

	private BroadcastReceiver setCurrentTrackURIReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String currentTrackURI = intent.getStringExtra("currentTrackURI");
			setCurrentTrackURI(currentTrackURI);
		}
	};

	private BroadcastReceiver setCurrentTrackDurationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String currentTrackDuration = intent.getStringExtra("currentTrackDuration");
			setCurrentTrackDuration(currentTrackDuration);
		}
	};

	// 设置模式失败时退回上一个模式
	private BroadcastReceiver setCurrentPlaymodeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("setBackCurrentPlaymodeReceiver onReceive");

			switch (WatchDog.currentPlaymode) {
			case MODE_ORDER:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_order_selector);
				break;
			case MODE_SHUFFLE:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_shuffle_selector);
				break;
			case MODE_SINGLE:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_singlel_selector);
				break;
			case MODE_ALL:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_all_selector);
				break;
			}
		}
	};

	// 设置模式失败时退回上一个模式
	private BroadcastReceiver setBackCurrentPlaymodeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("setBackCurrentPlaymodeReceiver onReceive");
			switch (WatchDog.currentPlaymode) {
			case MODE_ORDER:
				WatchDog.currentPlaymode = MODE_ALL;
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_all_selector);
				break;

			case MODE_SHUFFLE:
				WatchDog.currentPlaymode = MODE_ORDER;
				btnPlaymode.setBackgroundResource(R.drawable.playmode_order_selector);
				break;

			case MODE_SINGLE:
				WatchDog.currentPlaymode = MODE_SHUFFLE;
				btnPlaymode.setBackgroundResource(R.drawable.playmode_shuffle_selector);
				break;

			case MODE_ALL:
				WatchDog.currentPlaymode = MODE_SINGLE;
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_singlel_selector);
				break;
			}
		}
	};

	private BroadcastReceiver updateCacheProgressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int progress = intent.getIntExtra("progress", -1);
			if (progress != -1) {
				sbCache.setProgress(progress);
			}

			if (progress >= 100) {
				isCaching = false;
				WatchDog.cacheStateMap.put(WatchDog.currentPlayingMusic.getId(), Music.CACHE_DOWNLOADED);
				UIHelper.refreshLocalSinglesView();

				btnPlayPause.setEnabled(true);
				flSbProgress.setVisibility(View.VISIBLE);
				tvCurrentTime.setVisibility(View.VISIBLE);
				tvTotalTime.setVisibility(View.VISIBLE);

				llCache.setVisibility(View.GONE);

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				getMediaInfo();
				getTransportInfo();
			}
		}
	};
	private void refreshUsbMusicDisplayInfo(){
		tvArtistName.setText(ExternalDeviceFragment.currentArtistName);
		tvMusicName.setText(ExternalDeviceFragment.currentPlayingName);
	}
	private BroadcastReceiver updateMediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			boolean isUsbMusic = intent.getBooleanExtra("is_usb_music", false);
			if (isUsbMusic
					|| ExternalDeviceFragment.currentListType == Constant.URI_USB
					|| ExternalDeviceFragment.currentListType == Constant.URI_CUE) {//[外联设备]
				initPlaylist();
				
			} else {//【我的音乐】【音乐商店】
				tvArtistName.setText(WatchDog.currentArtistName);
				tvMusicName.setText(WatchDog.currentPlayingName);
				Log.i(TAG, "tvMusicName.setText(" + WatchDog.currentPlayingName + ");");
				initPic();

				btnNext.setEnabled(true);
				btnPrev.setEnabled(true);
				// getPositionInfo();

				if (progressRunnable == null) {
					updateProgress(WatchDog.currentPlayingName);
					System.out.println("fucking updateProgress(" + WatchDog.currentPlayingName + ")");
				}
			}
		}

	};

	// 播放下一曲返回成功，且状态为正在播放时，刷新媒体信息
	private BroadcastReceiver initPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (isCaching == false && currentState == PLAYING) {
				getTransportInfo();
				getMediaInfo();
			}
		}
	};

	private BroadcastReceiver updatePlayModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (WatchDog.currentPlaymode) {
			case MODE_ORDER:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_order_selector);
				break;
			case MODE_SHUFFLE:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_shuffle_selector);
				break;
			case MODE_SINGLE:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_singlel_selector);
				break;
			case MODE_ALL:
				btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_all_selector);
				break;
			}
		}
	};

	private BroadcastReceiver errorMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra("msg");
			if ("".equals(msg) || msg == null) {
				System.out.println(">>>>>>>>>>>>>>>>>Empty Error Msg<<<<<<<<<<<<<<<<<");
			} else {
				CustomToast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	};

//	private BroadcastReceiver babyNotMineReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Log.e("BUG896", TAG + "babyNotMineReceiver onReceive");
////			UpnpApp.mainHandler.showAlert(R.string.device_occupated);
//
//			Intent intent2 = new Intent(getActivity(), LoginActivity.class);
//			intent2.putExtra("from", "babyNotMineReceiver");
//			startActivity(intent2);
//		}
//	};

	private BroadcastReceiver mediaOutOfServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mosExit();
		}
	};

	private void mosExit() {
//		finish();
		//屏蔽提示“网络试听同步功能将在稍后开放”
//		CustomToast.makeText(getActivity(), WatchDog.mediaOutOfServiceReson, Toast.LENGTH_SHORT).show();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e("BUG839", TAG+"onCreateView()");
		Log.e("BUG901", "PlayerFragment onCreateView()");
		super.onCreate(savedInstanceState);
		
		view = inflater.inflate(R.layout.player, null);
		initComponents();
		initArguments();
		initListeners();
		registerReceivers();

		getPlayMode();
		getVolumn();

		if (isCaching == false) {
			getMediaInfo();
			getTransportInfo();
		}
		showPlayList();
		
		return view;
	}

	private void getPlayMode() {
		new Player().getPlayMode();
	}

	protected void initPic() {

		if (WatchDog.currentPlayingMusic == null) {
			ivPicPlaying.setImageResource(R.drawable.pic);
			return;
		} else {
			String imgurl = WatchDog.currentPlayingMusic.getImgUrl();
			if (imgurl != null) {
				final String imageKey = imgurl + "150";// 250
				// 开始异步加载数据
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImage(imageKey, imgurl, 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {

						if (ivPicPlaying != null) {
							
							ivPicPlaying.setImageBitmap(bitmap);
						}
					}
				});

				if (bitmap != null) {
					ivPicPlaying.setImageBitmap(bitmap);
				}
			}
		}

	}

	private void getMediaInfo() {
		new Player().getMediaInfo();
	}

	private void getTransportInfo() {
		new Player().getTransportInfo();
	}

	@Override
	public void onDetach() {
		stopAllHiddenThread();

		UIHelper.refreshLocalSinglesView();
		
		WatchDog.latestOperation = "";
		super.onDetach();
	}
	
	@Override
	public void onDestroyView() {
		Log.e("BUG839", TAG+"onDestroyView()");
		unregisterReceivers();
		super.onDestroyView();
	}

	private void initComponents() {
		llPlayer = (LinearLayout) view.findViewById(R.id.ll_player);
		llPlaypic = (LinearLayout) view.findViewById(R.id.ll_playpic);
		llVolumn = (LinearLayout) view.findViewById(R.id.ll_volumn);
		llCache = (LinearLayout) view.findViewById(R.id.ll_cache);
		llLvPlaylist = (LinearLayout) view.findViewById(R.id.ll_lv_playlist);
		flBtnAnim = (FrameLayout) view.findViewById(R.id.fl_btn_anim);

		flControlArea = (FrameLayout) view.findViewById(R.id.fl_control_area);
		flSbProgress = (FrameLayout) view.findViewById(R.id.fl_sb_progress);
		ivPicPlaying = (ImageView) view.findViewById(R.id.iv_pic_playing);
		ivBtnAnimArrow = (ImageView) view.findViewById(R.id.iv_btn_anim_arrow);
		lvPlaylist = (ListView) view.findViewById(R.id.lv_playlist);

		btnPlayPause = (Button) view.findViewById(R.id.btn_play_pause);
		btnPrev = (Button) view.findViewById(R.id.btn_prev);
		btnNext = (Button) view.findViewById(R.id.btn_next);
		btnBack = (ImageButton) view.findViewById(R.id.btn_back);
		btnPlaylist = (ImageButton) view.findViewById(R.id.btn_playlist);
		btnPlaymode = (Button) view.findViewById(R.id.btn_playmode);
		btnVolumn = (Button) view.findViewById(R.id.btn_volumn);

		tvCurrentTime = (TextView) view.findViewById(R.id.tv_current_time);
		tvTotalTime = (TextView) view.findViewById(R.id.tv_total_time);
		tvMusicName = (TextView) view.findViewById(R.id.tv_music_name);
		tvArtistName = (TextView) view.findViewById(R.id.tv_artist_name);

		sbProgress = (SeekBar) view.findViewById(R.id.sb_progress);
		sbVolumn = (SeekBar) view.findViewById(R.id.sb_volumn);
		sbCache = (SeekBar) view.findViewById(R.id.sb_cache_progress);
	}

	private void initArguments() {
		llVolumn.setVisibility(View.GONE);

		if (WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE) {
			btnPlayPause.setEnabled(true);
			flSbProgress.setVisibility(View.VISIBLE);
			tvCurrentTime.setVisibility(View.VISIBLE);
			tvTotalTime.setVisibility(View.VISIBLE);

			llCache.setVisibility(View.GONE);
		} else {
			long id = 0;
			if (WatchDog.currentPlayingMusic != null) {
				id = WatchDog.currentPlayingMusic.getId();
			}

			if (WatchDog.cacheStateMap.containsKey(id) == false) {
				btnPlayPause.setEnabled(true);
				flSbProgress.setVisibility(View.VISIBLE);
				tvCurrentTime.setVisibility(View.VISIBLE);
				tvTotalTime.setVisibility(View.VISIBLE);

				llCache.setVisibility(View.GONE);

			} else if (!WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
				isCaching = true;
				btnPlayPause.setEnabled(false);
				setBtnCachingAnim();
				flSbProgress.setVisibility(View.GONE);
				tvCurrentTime.setVisibility(View.GONE);
				tvTotalTime.setVisibility(View.GONE);

				llCache.setVisibility(View.VISIBLE);
				initPic();
				getCacheProgress(id);
			} else {
				btnPlayPause.setEnabled(true);
				flSbProgress.setVisibility(View.VISIBLE);
				tvCurrentTime.setVisibility(View.VISIBLE);
				tvTotalTime.setVisibility(View.VISIBLE);

				llCache.setVisibility(View.GONE);
			}
		}

		tvMusicName.setText(WatchDog.currentPlayingName == "" || WatchDog.currentPlayingName == null ? "歌曲名" : WatchDog.currentPlayingName);
		tvArtistName.setText(WatchDog.currentArtistName == "" || WatchDog.currentArtistName == null ? "演出者" : WatchDog.currentArtistName);

		switch (WatchDog.currentPlaymode) {
		case MODE_ORDER:
			btnPlaymode.setBackgroundResource(R.drawable.playmode_order_selector);
			break;
		case MODE_SHUFFLE:
			btnPlaymode.setBackgroundResource(R.drawable.playmode_shuffle_selector);
			break;
		case MODE_SINGLE:
			btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_singlel_selector);
			break;
		case MODE_ALL:
			btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_all_selector);
			break;
		}

		if (WatchDog.currentList == null || WatchDog.currentList.size() == 0) {
			btnPlaylist.setEnabled(false);
			btnNext.setEnabled(false);
			btnPrev.setEnabled(false);
		} else {
			btnPlaylist.setEnabled(true);
			btnNext.setEnabled(true);
			btnPrev.setEnabled(true);
		}

		if (WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE) {
			// TODO 外联设备 - 文件 - 播放列表 -未稳定
			btnPlaylist.setVisibility(View.VISIBLE);
		}
		
		btnPlaylist.setVisibility(View.INVISIBLE);
	}

	private void getCacheProgress(final long id) {
//		new Thread(new Runnable() {
//			int n1 = currentCachingChanger;
//			Music m = WatchDog.currentPlayingMusic;
//
//			@Override
//			public void run() {
//				while (!WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED) && m == WatchDog.currentPlayingMusic && n1 == currentCachingChanger && shouldSayHello2Death == false) {
//					new Cacher().getCacheProgress(Constant.regCacheUriMusic + WatchDog.currentPlayingMusic.getId());
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
	}

	private void initListeners() {
		btnPlayPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}

				if (!currentState.equals(PLAYING)) {
					play();
					btnPlayPause.setBackgroundResource(R.drawable.btn_pause_selector);
				} else {
					pause();
					btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
				}
			}
		});

		sbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int pro = seekBar.getProgress();
				System.out.println("pro=" + pro);
				seekTo(pro);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// System.out.println("newProgress="+progress);
			}
		});

		sbVolumn.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int pro = seekBar.getProgress();
				System.out.println("pro=" + pro);
				setVolumn(pro);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// System.out.println("newProgress="+progress);
			}
		});

		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}

				WatchDog.latestOperation = "next";
				playNext();
			}
		});

		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				countPrevClick++;
				handler.sendEmptyMessageDelayed(MSG_BTN_PREV, 1000);// 一秒钟后根据点击次数判断单击还是双击
			}
		});

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.showViewpage(MainActivity.PLAYER_PAGE_ITEM_NUM);
			}
		});

		btnPlaylist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				parent.showPlayer();
			}
		});

		btnPlaymode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (WatchDog.currentPlaymode) {

				case MODE_ORDER:
					WatchDog.currentPlaymode = MODE_SHUFFLE;
					sendBoxPlayMode(MODE_SHUFFLE);
					btnPlaymode.setBackgroundResource(R.drawable.playmode_shuffle_selector);
					break;

				case MODE_SHUFFLE:
					WatchDog.currentPlaymode = MODE_SINGLE;
					sendBoxPlayMode(MODE_SINGLE);
					btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_singlel_selector);
					break;

				case MODE_SINGLE:
					WatchDog.currentPlaymode = MODE_ALL;
					sendBoxPlayMode(MODE_ALL);
					btnPlaymode.setBackgroundResource(R.drawable.playmode_repeat_all_selector);
					break;

				case MODE_ALL:
					WatchDog.currentPlaymode = MODE_ORDER;
					sendBoxPlayMode(MODE_ORDER);
					btnPlaymode.setBackgroundResource(R.drawable.playmode_order_selector);
					break;
				}
			}
		});

		btnVolumn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showOrHideVolumn();
			}
		});

	}

	protected void playThisAgain() {
		Player p = new Player();
		p.stop();
		// p.play(WatchDog.currentPlayingMusic.getUri(Constant.URI_MUSIC, 0));
		p.play(WatchDog.currentUri);
		p = null;
	}

	protected void sendBoxPlayMode(int mode) {
		Player p = new Player();
		p.sendBoxPlayMode(mode);
	}

	private void showPlayList(){
		llLvPlaylist.setVisibility(View.VISIBLE);
		llPlaypic.setVisibility(View.GONE);
		flControlArea.setVisibility(View.GONE);
		// flControlArea.setBackgroundResource(R.color.icecream_bg);
		llLvPlaylistShown = true;
		
		//首次打开列表
		initPlaylist();

		btnPlaylist.setImageResource(R.drawable.btn_playpic);
	}
	
	private void initPlaylist() {
		Log.i(TAG, "ExternalDeviceFragment.currentListType:" + ExternalDeviceFragment.currentListType);
		Log.e("BUG902", TAG+"initPlaylist():WatchDog.currentListType="+WatchDog.currentListType);
		
		if (WatchDog.currentListType == Constant.URI_USB 
				|| WatchDog.currentListType == Constant.URI_CUE) {
			// 【外联设备】初始化播放列表
			
			playlist = ExternalDeviceFragment.currentList;
			adapter = new PlaylistMusicListAdapter(getActivity(), playlist);
			lvPlaylist.setAdapter(adapter);
			
			int currentIndex = ExternalDeviceFragment.currentPlayingIndex;
			lvPlaylist.setSelection(currentIndex > 3 ? currentIndex - 3 : currentIndex);
			tvMusicName.setText(ExternalDeviceFragment.currentPlayingName);
			
		} 

		else if (WatchDog.currentListType == 0 || WatchDog.currentListType == -1) {
			//尚未初始化播放列表
		}
		
		else {
			// 【我的音乐】初始化播放列表
			playlist = WatchDog.currentList;

			adapter = new PlaylistMusicListAdapter(getActivity(), playlist);
			lvPlaylist.setAdapter(adapter);

			// 定位到当前播放曲目
			if (!WatchDog.currentPlayingId.equals(0L)) {
				for (int i = 0; i < playlist.size(); i++) {
					if (playlist.get(i).getId().equals(WatchDog.currentPlayingId)) {
						lvPlaylist.setSelection(i > 3 ? i - 3 : i);
					}
				}
			}
		}

		lvPlaylist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				Log.i(TAG, "click playing music list, position=" + position);

				String uri = null;
				if (WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE) {
					// [外联设备]
					uri = playlist.get(position).getUri();
					// stopCurrentCachingUpdate();
					// WatchDog.latestOperation = "playlist_click";
					ExternalDeviceFragment.currentList = playlist;
					ExternalDeviceFragment.currentPlayingIndex = position;
					ExternalDeviceFragment.currentPlayingMusic = playlist.get(position);
					ExternalDeviceFragment.currentPlayingId = ExternalDeviceFragment.currentPlayingMusic.getId();
					// initPic();
					ExternalDeviceFragment.currentPlayingName = playlist.get(position).getName();
					// WatchDog.updateCachingState();// 如果上一曲未缓存完则修改其状态为等待
					WatchDog.currentState = PlayerFragment.STOPPED;// 先改为停止以停止播放器图标的跳动
					tvMusicName.setText(ExternalDeviceFragment.currentPlayingName);
					adapter.notifyDataSetChanged();

				} else {// [我的音乐]

					if (PowerfulBigMan.testClickInterval() == false) {
						return;
					}

					stopCurrentCachingUpdate();

					WatchDog.latestOperation = "playlist_click";
					WatchDog.currentList = playlist;
					WatchDog.currentPlayingIndex = position;
					WatchDog.currentPlayingMusic = playlist.get(position);
					WatchDog.currentPlayingId = WatchDog.currentPlayingMusic.getId();
					initPic();
					WatchDog.currentPlayingName = playlist.get(position).getName();

					WatchDog.updateCachingState();// 如果上一曲未缓存完则修改其状态为等待
					WatchDog.currentState = PlayerFragment.STOPPED;// 先改为停止以停止播放器图标的跳动
					tvMusicName.setText(WatchDog.currentPlayingName);
					adapter.notifyDataSetChanged();

					uri = playlist.get(position).getUri(WatchDog.currentListType, WatchDog.currentListId);
				}

				if (uri == null || uri.equals("")) {
//					CustomToast.makeText(getActivity(), "播放列表尚未初始化", Toast.LENGTH_SHORT).show();
					Log.e(TAG, UpnpApp.mainHandler.getString(R.string.playlist_not_init_error));
					return;
				}

				Log.i(TAG, "playMusic, uri=" + uri);
				playMusic(uri);
			}
		});
	}

	private void playMusic(String uri) {
		Player p = new Player();
		p.play(uri);
	}

	private void registerReceivers() {
		getActivity().registerReceiver(playNewMusicReceiver, new IntentFilter("playNewMusic"));
		getActivity().registerReceiver(setTransportStateReceiver, new IntentFilter("setTransportState"));
		getActivity().registerReceiver(setCurrentTrackURIReceiver, new IntentFilter("setCurrentTrackURI"));
		getActivity().registerReceiver(setCurrentTrackDurationReceiver, new IntentFilter("setCurrentTrackDuration"));
		getActivity().registerReceiver(setBackCurrentPlaymodeReceiver, new IntentFilter("setBackCurrentPlaymode"));
		getActivity().registerReceiver(setCurrentPlaymodeReceiver, new IntentFilter("setCurrentPlaymodeReceiver"));
		getActivity().registerReceiver(updateCacheProgressReceiver, new IntentFilter("updateCacheProgress"));
		getActivity().registerReceiver(updateMediaInfoReceiver, new IntentFilter("updateMediaInfo"));
		getActivity().registerReceiver(initPlayerReceiver, new IntentFilter("initPlayerReceiver"));
		getActivity().registerReceiver(updatePlayModeReceiver, new IntentFilter("updatePlayModeReceiver"));
		getActivity().registerReceiver(errorMessageReceiver, new IntentFilter("errorMessageReceiver"));
		getActivity().registerReceiver(updateListReceiver, new IntentFilter(BroadcastManager.FILTER_UPDATE_LOCALLIST));
//		getActivity().registerReceiver(babyNotMineReceiver, new IntentFilter("babyNotMineReceiver"));// 收归MainActivity管理
		getActivity().registerReceiver(mediaOutOfServiceReceiver, new IntentFilter("mediaOutOfServiceReceiver"));
		getActivity().registerReceiver(playlistSeekPositionReceiver, new IntentFilter(Constant.ACTION_PLAYLIST_SEEK_POSITION));
	}

	private void unregisterReceivers() {
		getActivity().unregisterReceiver(playNewMusicReceiver);
		getActivity().unregisterReceiver(setTransportStateReceiver);
		getActivity().unregisterReceiver(setCurrentTrackURIReceiver);
		getActivity().unregisterReceiver(setCurrentTrackDurationReceiver);
		getActivity().unregisterReceiver(setBackCurrentPlaymodeReceiver);
		getActivity().unregisterReceiver(setCurrentPlaymodeReceiver);
		getActivity().unregisterReceiver(updateCacheProgressReceiver);
		getActivity().unregisterReceiver(updateMediaInfoReceiver);
		getActivity().unregisterReceiver(initPlayerReceiver);
		getActivity().unregisterReceiver(updatePlayModeReceiver);
		getActivity().unregisterReceiver(errorMessageReceiver);
		getActivity().unregisterReceiver(updateListReceiver);
//		getActivity().unregisterReceiver(babyNotMineReceiver);
		getActivity().unregisterReceiver(mediaOutOfServiceReceiver);
		getActivity().unregisterReceiver(playlistSeekPositionReceiver);
	}

	public void play() {
		UpnpApp.upnpService.getControlPoint().execute(new Play(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("播放失败");
				btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
			}

			@Override
			public void success(ActionInvocation arg0) {
				currentState = PLAYING;
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause_selector);
				System.out.println("播放成功");
				getPositionInfo();
				updateProgress(WatchDog.currentPlayingName);
			}

		});
	}

	protected void pause() {
		UpnpApp.upnpService.getControlPoint().execute(new Pause(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("暂停失败");
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause_selector);
			}

			@Override
			public void success(ActionInvocation arg0) {
				System.out.println("暂停成功");
				currentState = PAUSED_PLAYBACK;
				btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
			}
		});
	}

	private void getVolumn() {
		UpnpApp.upnpService.getControlPoint().execute(new GetVolume(UpnpApp.renderingControlService) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("获取音量失败");
			}

			@Override
			public void received(ActionInvocation arg0, int arg1) {
				System.out.println("获取音量成功");
				currentVolumn = arg1;
				handler.sendEmptyMessage(MSG_VOLUMN);
			}
		});
	}

	private void getPositionInfo() {
		// Player p=new Player();
		// p.getPositionInfo();
		UpnpApp.upnpService.getControlPoint().execute(new GetPositionInfo(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("获取位置信息失败");
				gpfail++;
				if (gpfail <= 2) {
					getPositionInfo();
				}
			}

			@Override
			public void received(ActionInvocation arg0, PositionInfo arg1) {
				System.out.println("获取位置信息成功");
				gpfail = 0;
				positionInfo = arg1;
				System.out.println("positionInfo=" + positionInfo);

				duration = positionInfo.getTrackDuration().substring(3);
				totalSeconds = DateTimeFormatUtil.time2IntMillis(duration) / 1000;

				currentTime = positionInfo.getRelTime().substring(3);
				currentSeconds = DateTimeFormatUtil.time2IntMillis(currentTime) / 1000;
				currentPercent = positionInfo.getElapsedPercent();

				handler.sendEmptyMessage(0);
			}
		});
	}


	private void updateProgress(String currentPlayingName) {
		// final String myOwner = WatchDog.currentPlayingName;
		progressRunnable = new Runnable() {
			@Override
			public void run() {
				String myOwner = WatchDog.currentPlayingName;
				System.out.println("pr currentState=" + currentState);
				while (currentState.equals(PLAYING)
				// && WatchDog.currentPlayingName.equals(myOwner)
						&& shouldSayHello2Death == false) {// 空指针
					currentTime = DateTimeFormatUtil.parseInt2Time(currentSeconds * 1000);
					currentPercent = (int) Math.round(currentSeconds * 1.0 * 100 / totalSeconds);

					handler.sendEmptyMessage(0);

					currentSeconds++;
					if (currentSeconds > totalSeconds && totalSeconds != 0) {

						handler.sendEmptyMessage(MSG_CLEAR_PLAYING_PROGRESS);

					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("currentState=" + currentState + "&&WatchDog.currentPlayingName=" + WatchDog.currentPlayingName + "&&myOwner=" + myOwner);
				System.out.println("progress end and pr=null");
				progressRunnable = null;
			}
		};

		progressThread = new Thread(progressRunnable);
		progressThread.start();
	}

	protected void clearPlayingProgress() {
		sbProgress.setProgress(0);
		tvCurrentTime.setText("00:00");
		tvTotalTime.setText("00:00");
	}

	private void seekTo(final int progress) {
		double seekTargetSeconds = totalSeconds * progress / 100d;
		String targetTime = ModelUtil.toTimeString(new Long(Math.round(seekTargetSeconds)).intValue());
		System.out.println("targetTime=" + targetTime);
		if ("00:00:00".equals(targetTime)) {
			targetTime="00:00:01";
		}

		sbProgress.setProgress(progress);

		UpnpApp.upnpService.getControlPoint().execute(new Seek(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService, targetTime) {
			@Override
			public void success(final ActionInvocation invocation) {
				System.out.println("定位成功");
				currentPercent = progress;
				currentSeconds = (int) Math.round(totalSeconds * (currentPercent / 100d));
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				System.out.println("定位失败");
			}
		});

	}

	private void setVolumn(int volumn) {
		System.out.println("setVolumn=" + volumn);

		UpnpApp.upnpService.getControlPoint().execute(new SetVolume(UpnpApp.renderingControlService, volumn) {

			@Override
			public void success(ActionInvocation invocation) {
				System.out.println("音量设置成功");
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("音量设置失败");
			}
		});
	}

	private void setNewUri(final String uri) {
		while (!currentState.equals(STOPPED) && shouldSayHello2Death == false) {
			try {
				synchronized (this) {
					wait(100);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (currentState.equals(STOPPED)) {
			UpnpApp.upnpService.getControlPoint().execute(new SetAVTransportURI(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService, uri) {

				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
					System.out.println("URI设置失败");
				}

				@Override
				public void success(ActionInvocation arg0) {
					System.out.println("URI设置成功");
					currentState = PREPARED;
					WatchDog.currentUri = uri;
				}
			});
		}
	}

	private void playNext() {
		// 当前缓存进度还在显示中，而用户切换下一曲时，应当停止拿缓存进度线程；
		stopCurrentCachingUpdate();

		Player p = new Player();
		p.setBoxPlayNext();
	}

	private void stopCurrentCachingUpdate() {
		currentCachingChanger++;
	}


	private void playPrev() {

		stopCurrentCachingUpdate();

		Player p = new Player();
		p.setBoxPlayPrev();

	}


	private void setTransportState(String transportStateStr) {
		System.out.println("transportStateStr=" + transportStateStr);

		if (transportStateStr.equals(PAUSED_PLAYBACK)) {
			setStatePaused();

		} else if (transportStateStr.equals(PLAYING)) {
			setStatePlaying();

		} else if (transportStateStr.equals(STOPPED)) {
			System.out.println(TAG + ":setTransportState:transportStateStr=" + transportStateStr);
			setStateStopped();

		} else if (transportStateStr.equals(TRANSITIONING)) {
			setStateTransitioning();// 正在缓存
		}
	}

	private void setStateTransitioning() {
		// if (transitioningCancel == true) {
		// return;
		// }

		System.out.println("setStateTransitioning");
		initPic();

		isCaching = true;
		currentState = TRANSITIONING;

		// 当缓存来自用户在列表中点击未缓存歌曲时，显示缓存进度和媒体和媒体信息
		long id = -1;
		String currengCachingState = "";
		if (WatchDog.currentPlayingMusic != null) {
			id = WatchDog.currentPlayingMusic.getId();
			currengCachingState = WatchDog.cacheStateMap.get(id);
		}

		if (!(Music.CACHE_DOWNLOADED).equals(currengCachingState)) {
			btnPlayPause.setEnabled(false);
			setBtnCachingAnim();

			flSbProgress.setVisibility(View.GONE);
			tvCurrentTime.setVisibility(View.GONE);
			tvTotalTime.setVisibility(View.GONE);

			llCache.setVisibility(View.VISIBLE);
			if (id != -1) {
				getCacheProgress(id);
			}
		}

		// }
	}

	private void setPicCachingAnim() {
		ivPicPlaying.setImageResource(R.drawable.pic1);
	}

	private void setBtnCachingAnim() {
		System.out.println("setBtnCachingAnim");
		initBtnAnim();

		btnPlayPause.setVisibility(View.GONE);
		flBtnAnim.setVisibility(View.VISIBLE);

		// threadBtnAnim.start();
		handler.post(threadBtnAnim);
	}

	private void initBtnAnim() {
		btnAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_tweener);
		btnAnimation.setRepeatCount(Animation.INFINITE);

		threadBtnAnim = null;
		threadBtnAnim = new Thread(new Runnable() {
			@Override
			public void run() {
				ivBtnAnimArrow.startAnimation(btnAnimation);// 对控件使用补间效果
				btnAnimation.start();
			}
		});
	}

	private void setStateStopped() {
		System.out.println(TAG + ":setStateStopped");
		currentState = STOPPED;// damn
		btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
	}

	private void setStatePaused() {
		System.out.println("setStatePaused by others");
		currentState = PAUSED_PLAYBACK;
		btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
	}

	private void setStatePlaying() {
		System.out.println("setStatePlaying");
		isCaching = false;
		currentState = PLAYING;

		// 开始暂停按钮恢复状态
		flBtnAnim.setVisibility(View.GONE);
		if (btnAnimation != null) {
			btnAnimation.cancel();
		}
		handler.removeCallbacks(threadBtnAnim);
		threadBtnAnim = null;
		btnPlayPause.setVisibility(View.VISIBLE);
		btnPlayPause.setBackgroundResource(R.drawable.btn_pause_selector);
		btnPlayPause.setEnabled(true);

		// 还原播放进度
		if (llVolumnShown == false) {
			flSbProgress.setVisibility(View.VISIBLE);
		} else {
			flSbProgress.setVisibility(View.GONE);
		}
		tvCurrentTime.setVisibility(View.VISIBLE);
		tvTotalTime.setVisibility(View.VISIBLE);
		llCache.setVisibility(View.GONE);

		// 获取并显示播放进度
		getPositionInfo();
		getMediaInfo();
		stopAllHiddenThread();
		shouldSayHello2Death = false;
	}

	protected void setCurrentTrackURI(String currentTrackURI) {
		Log.i(TAG, "setCurrentTrackURI: currentTrackURI=" + currentTrackURI);

		currentTrackURI = currentTrackURI.replaceAll("%20", " ");

		Log.i(TAG, "setCurrentTrackURI: currentTrackURI=" + currentTrackURI);

		WatchDog.currentUri = currentTrackURI;

		int uriType = new URIParams(currentTrackURI).getType();
		String str = "";
		String state = "-1";
		switch (uriType) {
		case 1:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			state = new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 2:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("id");
			state = new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 3:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			state = new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 4:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			state = new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 5:// 歌单
			WatchDog.mediaOutOfService = true;
			WatchDog.mediaOutOfServiceReson = UpnpApp.context.getResources().getString(R.string.mosReasonPlaylist);
			mosExit();

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			state = new URIParams(currentTrackURI).getParams().get("state");
			break;
		case Constant.URI_USB:
			//TODO 标题， 选择项
			refreshUsbMusicDisplayInfo();
			break;
		case Constant.URI_CUE:
			//TODO 标题， 选择项
			refreshUsbMusicDisplayInfo();
			break;
		}

		if (state != null && state.equals("4")) {
			isCaching = false;
		} else {
			isCaching = true;
		}

		if (uriType == Constant.URI_USB || uriType == Constant.URI_CUE) {
			// 外联设备：do nothing
			// getMusicNameAndArtistFromUsbFileUri(currentTrackURI);
		} else {
			if(!str.equals("")){
				long musicId = Long.parseLong(str);
				getMusicNameAndArtistFromId(musicId);
			}
		}

		// }

	}

	private void getMusicNameAndArtistFromId(long musicId) {
		List<Music> list;
		if (WatchDog.currentList != null) {
			list = WatchDog.currentList;
		} else {
			list = VirtualData.musics;
		}

		for (Music music : list) {
			if (music.getId() == musicId) {
				initPic();
				WatchDog.currentPlayingMusic = music;
				WatchDog.currentPlayingId = WatchDog.currentPlayingMusic.getId();
				WatchDog.currentPlayingName = music.getName();
				WatchDog.currentArtistName = music.getArtistName();

				tvArtistName.setText(WatchDog.currentArtistName);
				tvMusicName.setText(WatchDog.currentPlayingName);

				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
				return;
			}
		}
		tvArtistName.setText("未知曲目");
		tvMusicName.setText("未知演出者");
	}

	protected void setCurrentTrackDuration(String durationIn) {
		System.out.println("setCurrentTrackDuration");
		duration = durationIn.substring(3);
		totalSeconds = DateTimeFormatUtil.time2IntMillis(duration) / 1000;
	}

	private void stopAllHiddenThread() {
		shouldSayHello2Death = true;
	}

	@Override
	public void onResume() {
		Log.e("BUG839", TAG+"onResume()");
		
		if (hasEverPaused) {
			getMediaInfo();
			getTransportInfo();
			System.out.println("onRestart: mediaInfo and transportInfo got");
		}
		seekToCurrentPlayingPosition();
		super.onResume();
	}

	private void seekToCurrentPlayingPosition() {
		if(lvPlaylist!=null && lvPlaylist.getVisibility()==View.VISIBLE){
			int p=getCurrentPlayingPosition();
			Log.e("BUG839", TAG+"seekToCurrentPlayingPosition():p="+p);
			lvPlaylist.setSelection(p);
		}
	}

	private int getCurrentPlayingPosition() {
		if (playlist == null || playlist.size() == 0 || WatchDog.currentPlayingMusic == null) {
			return 0;
		}
		
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).getId() == WatchDog.currentPlayingMusic.getId()) {
				return i - 2 > 0 ? i - 2 : 0;
			}
		}
		
		return 0;
	}


	private void showOrHideVolumn() {
		if (llVolumnShown == false) {
			btnVolumn.setBackgroundResource(R.drawable.btn_volumn_hover);

			llVolumn.setVisibility(View.VISIBLE);
			llVolumnShown = true;
			flSbProgress.setVisibility(View.GONE);
			llCache.setVisibility(View.GONE);
		} else {
			btnVolumn.setBackgroundResource(R.drawable.btn_volumn);

			llVolumn.setVisibility(View.GONE);
			llVolumnShown = false;
			if (isCaching) {
				llCache.setVisibility(View.VISIBLE);
				flSbProgress.setVisibility(View.GONE);
			} else {
				flSbProgress.setVisibility(View.VISIBLE);
				llCache.setVisibility(View.GONE);
			}
		}
	}

}
