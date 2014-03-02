package com.kitty.poclient.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.activity.MainActivity;
import com.kitty.poclient.adapter.MusicListAdapter;
import com.kitty.poclient.common.BroadcastManager;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UIHelper;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.fragment.usb.ExternalDeviceFragment;
import com.kitty.poclient.interfaces.OnCurrentPlayingInfoChangedListener;
import com.kitty.poclient.test.MusicPlayService;
import com.kitty.poclient.upnp.Cacher;
import com.kitty.poclient.upnp.Player;
import com.kitty.poclient.util.BitmapUtil;
import com.kitty.poclient.util.DateTimeFormatUtil;
import com.kitty.poclient.util.MediaUtil;
import com.kitty.poclient.util.PowerfulBigMan;
import com.kitty.poclient.util.URIParams;
import com.kitty.poclient.util.LoadImageAysnc.ImageCallBack;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PlayerFragment extends Fragment implements OnCurrentPlayingInfoChangedListener {

	private final String TAG = PlayerFragment.class.getSimpleName() + ":";// UpnpApp.mainHandler

	private View view;

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
	private ImageView ivPicPlaying;
	private ListView lvPlaylist;

	// 播放状态
	private String currentState = "STOPPED";
	private String formerState = "STOPPED";
	public static final String PLAYING = "PLAYING";
	public static final String PAUSED = "PAUSED";
	public static final String STOPPED = "STOPPED";

	// 播放模式
	public static final int MODE_ORDER = 1;
	public static final int MODE_SHUFFLE = 2;
	public static final int MODE_SINGLE = 3;
	public static final int MODE_ALL = 4;
	public static final int SWITCH_TO_PLAY_PROGRESS = 6;

	private MusicListAdapter adapter;

	private MainActivity mainActivity;

	// 加载图片
	private ImageLoader loader;
	private DisplayImageOptions options;

	// 进度相关
	private String duration = "00:00";
	private int totalSeconds = 0;
	private long startMillis = 0L;
	private int stopTrackingProgress = 0;
	private Runnable currentPlayingProgressRunnable = null;
	private Runnable progressRunnable = new Runnable() {

		@Override
		public void run() {
			while (System.currentTimeMillis() - startMillis < totalSeconds * 1000 && PLAYING.equals(currentState)) {
				int progress = DateTimeFormatUtil.getPlayingProgress(System.currentTimeMillis() - startMillis, totalSeconds * 1000L);
				String elapsedTimeStr = DateTimeFormatUtil.parseMills2Time(System.currentTimeMillis() - startMillis);

				Message msg = handler.obtainMessage(MSG_UPDATE_PLAYING_PROGRESS);
				msg.arg1 = progress;
				msg.obj = elapsedTimeStr;
				handler.sendMessage(msg);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			System.out.println("elapsedMills=" + (System.currentTimeMillis() - startMillis));
			System.out.println("currentState=" + currentState);
			if (!PAUSED.equals(currentState)) {
				handler.sendEmptyMessage(MSG_SET_STATE_STOPPED);
			}
		}
	};
	
	// 声音
	private boolean llVolumnShown = false;
	private LinearLayout llVolumn;

	// handler
	private final int MSG_INIT_PLAYING_INFO = 10;
	private final int MSG_UPDATE_PLAYING_PROGRESS = 11;
	private final int MSG_SET_STATE_STOPPED = 12;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_INIT_PLAYING_INFO:
				initPlayingInfo();
				setStatePlaying(true);
				break;

			case MSG_UPDATE_PLAYING_PROGRESS:
				sbProgress.setProgress(msg.arg1);
				tvCurrentTime.setText((String) msg.obj);
				break;

			case MSG_SET_STATE_STOPPED:
				setStateStopped();
				break;
			}
		}
	};

	protected void checkIfCurrentListEmpty() {
		if (WatchDog.currentListType == 0 || WatchDog.currentListType == -1 || WatchDog.currentList == null || WatchDog.currentList.size() == 0) {
			setBtnsEnabled(false);
			clearProgress();
		} else {
			Log.e("BUG901", "WatchDog.currentList.size()=" + WatchDog.currentList.size());
			setBtnsEnabled(true);
		}
	}

	private void clearProgress() {
		sbProgress.setProgress(0);
		sbProgress.setSecondaryProgress(0);
		tvTotalTime.setText("00:00");
		tvCurrentTime.setText("00:00");
	}

	public PlayerFragment() {
		initImageLoader();
	}

	public PlayerFragment(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		initImageLoader();
	}

	private void initImageLoader() {
		loader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.pic).cacheInMemory(true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreateView()");
		WatchDog.cpiListeners.add(this);

		view = inflater.inflate(R.layout.player, null);
		initComponents();
		initParams();
		initListeners();

		return view;
	}

	private void initParams() {
		llVolumnShown=false;
	}

	private void initListeners() {
		btnPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (currentState.equals(PLAYING)) {
					pause();
				} else {
					play();
				}
			}
		});

		sbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				stopTrackingProgress = seekBar.getProgress();
				seekTo(stopTrackingProgress * totalSeconds * 1000 / 100);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		});
		
		btnVolumn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showOrHideVolumn();
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
		
	}
	
	protected void setVolumn(int pro) {
		if(MusicPlayService.mPlayer!=null){
			MusicPlayService.mPlayer.setVolume(pro/100f, pro/100f);
		}
	}

	private void showOrHideVolumn() {
		if (llVolumnShown == false) {
			btnVolumn.setBackgroundResource(R.drawable.btn_volumn_hover);

			llVolumn.setVisibility(View.VISIBLE);
			flSbProgress.setVisibility(View.GONE);
			llVolumnShown = true;			
		} else {
			btnVolumn.setBackgroundResource(R.drawable.btn_volumn);

			llVolumn.setVisibility(View.GONE);
			flSbProgress.setVisibility(View.VISIBLE);
			llVolumnShown = false;		
		}
	}

	protected void seekTo(int progressOfMillis) {
		if (MusicPlayService.mPlayer != null && MusicPlayService.mPlayer.isPlaying()) {
			startMillis = processStartMillis();
			MusicPlayService.mPlayer.seekTo(progressOfMillis);
		}
	}

	private long processStartMillis() {
		startMillis = System.currentTimeMillis() - stopTrackingProgress * (totalSeconds * 1000) / 100;
		return startMillis;
	}

	protected void play() {
		MusicPlayService.mPlayer.start();
		// 假设已成功
		if (PAUSED.equals(currentState)) {
			setStatePlaying(false);
		} else if (STOPPED.equals(currentState)) {
			setStatePlaying(true);
		}

	}

	protected void pause() {
		MusicPlayService.mPlayer.pause();
		// 假设已成功
		setStatePaused();
	}

	protected void setStatePlaying(boolean playFromBeginning) {
		currentState = PLAYING;
		WatchDog.setCurrentPlayingState(PLAYING);
		
		btnPlayPause.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.btn_pause_selector));

		totalSeconds = DateTimeFormatUtil.time2IntMillis(duration) / 1000;
		if (playFromBeginning) {
			startMillis = System.currentTimeMillis();
		} else {
			System.out.println("sbProgress.getProgress()="+sbProgress.getProgress());
			startMillis = System.currentTimeMillis() - sbProgress.getProgress()*(totalSeconds*1000)/100;
		}
		System.out.println("totalSeconds=" + totalSeconds);

		startProgressUpadate();
	}

	protected void setStatePaused() {
		currentState = PAUSED;
		WatchDog.setCurrentPlayingState(PAUSED);
		
		handler.removeCallbacks(currentPlayingProgressRunnable);
		btnPlayPause.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.btn_play_selector));
	}

	protected void setStateStopped() {
		currentState = STOPPED;
		WatchDog.setCurrentPlayingState(STOPPED);
		
		startMillis = 0L;
		stopTrackingProgress = 0;

		sbProgress.setProgress(0);
		tvCurrentTime.setText("00:00");
		btnPlayPause.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.btn_play_selector));
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void initComponents() {

		flControlArea = (FrameLayout) view.findViewById(R.id.fl_control_area);
		flSbProgress = (FrameLayout) view.findViewById(R.id.fl_sb_progress);
		ivPicPlaying = (ImageView) view.findViewById(R.id.iv_pic_playing);
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
		llVolumn = (LinearLayout) view.findViewById(R.id.ll_volumn);
	}

	private void setBtnsEnabled(boolean b) {
		btnPlaylist.setEnabled(b);
		btnPlayPause.setEnabled(b);
		btnNext.setEnabled(b);
		btnPrev.setEnabled(b);
		sbProgress.setEnabled(b);
	}

	protected void clearPlayingProgress() {
		sbProgress.setProgress(0);
		tvCurrentTime.setText("00:00");
		tvTotalTime.setText("00:00");
	}

	@Override
	public void onResume() {
		Log.e(TAG, "onResume()");
		super.onResume();
	}

	private void initPlayingInfo() {
		Log.e(TAG, "WatchDog.currentPlayingInfo=" + WatchDog.getCurrentPlayingInfo());

		if (WatchDog.getCurrentPlayingInfo() != null) {
			duration = WatchDog.getCurrentPlayingInfo().getDuration().substring(3);

			tvMusicName.setText(WatchDog.getCurrentPlayingInfo().getName());
			tvArtistName.setText(WatchDog.getCurrentPlayingInfo().getArtist());
			tvTotalTime.setText(WatchDog.getCurrentPlayingInfo().getDuration().substring(3));

			// 显示专辑图片
			String imgUrl = WatchDog.getCurrentPlayingInfo().getImgUrl();
			System.out.println("imgUrl=" + imgUrl);
			loader.displayImage(imgUrl, ivPicPlaying, options);
		}
	}

	private void startProgressUpadate() {
		currentPlayingProgressRunnable = progressRunnable;
		new Thread(currentPlayingProgressRunnable).start();
	}

	@Override
	public void onCurrentPlayingInfoChanged() {
		handler.sendEmptyMessage(MSG_INIT_PLAYING_INFO);
	}

}
