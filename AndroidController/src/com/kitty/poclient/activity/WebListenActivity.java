package com.kitty.poclient.activity;

import java.util.List;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.model.PositionInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.kitty.poclient.R;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.fragment.PlayerFragment;
import com.kitty.poclient.upnp.Player;
import com.kitty.poclient.util.Buyer;
import com.kitty.poclient.util.DateTimeFormatUtil;
import com.kitty.poclient.util.ExitApplication;

//演出者，未知演出者
public class WebListenActivity extends Activity {

	public static final String TAG = "WebListenActivity: ";

	/*
	 * 控件
	 */
	private ImageView ivAlbum;
	private TextView tvName;
	private TextView tvArtist;
	private TextView tvBuy;
	private SeekBar sbProgress;
	private LinearLayout llLeft, llRight, llUp, llDown;// 空白区域
	// private Bitmap bitmap;

	private String imgUrl;

	/*
	 * 进度相关
	 */
	private int gpfail = 0;// GetPositionInfo失败次数
	private boolean shouldSayHello2Death = false;// 为真时叫停所有循环
	private PositionInfo positionInfo;
	private String duration;
	private int currentPercent;
	private int totalSeconds;
	private int currentSeconds;
	private String currentTime;
	private Thread progressThread;
	private Runnable progressRunnable;
	private String currentState = "";

	private boolean isFromSearch = false;
	private boolean isFromAlbum = false;
	private boolean isFromPack = false;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if (totalSeconds != 0) {
					sbProgress.setProgress(currentPercent);
				} else {
					getPositionInfo();
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate()");
		
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		WatchDog.currentActivities.add(this);
		WatchDog.isWebListenActivityRunning = true;
		WatchDog.runningWebListenActivity = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		getWindow().getDecorView().setSystemUiVisibility(4);
		setContentView(R.layout.web_listen);

		initView();
		initData();
		initListeners();
		// registerReceivers();
		
		Log.e(TAG, "onCreate() finished");
	}
	
	@Override
	protected void onResume() {
		Log.e(TAG, "onResume()");
		super.onResume();
	}

	private void initView() {
		ivAlbum = (ImageView) findViewById(R.id.iv_album_cover);
		tvName = (TextView) findViewById(R.id.tv_music_name);
		tvArtist = (TextView) findViewById(R.id.tv_artist);
		tvBuy = (TextView) findViewById(R.id.tv_buy);
		sbProgress = (SeekBar) findViewById(R.id.sb_progress);

		llLeft = (LinearLayout) findViewById(R.id.ll_left);
		llRight = (LinearLayout) findViewById(R.id.ll_right);
		llUp = (LinearLayout) findViewById(R.id.ll_up);
		llDown = (LinearLayout) findViewById(R.id.ll_down);
	}

	private void initData() {
		String musicName = getIntent().getStringExtra("musicName");
		String artist = getIntent().getStringExtra("artist");
		if (artist.equals("null")) {
			artist = "未知演出者";
		}
		// bitmap = getIntent().getParcelableExtra("bitmap");
		imgUrl = getIntent().getStringExtra("imgUrl");
		// String musicIsBought = getIntent().getStringExtra("musicIsBought");
		String btnBuyText = getIntent().getStringExtra("btnBuyText");
		boolean btnBuyEnabled = getIntent().getBooleanExtra("btnBuyEnabled", false);
		isFromSearch = getIntent().getBooleanExtra("isFromSearch", false);
		isFromAlbum = getIntent().getBooleanExtra("isFromAlbum", false);
		isFromPack = getIntent().getBooleanExtra("isFromPack", false);
		// String price = getIntent().getStringExtra("price");

		/*
		 * if (bitmap != null) { ivAlbum.setImageBitmap(bitmap); }
		 */

		ImageLoader.getInstance().displayImage(imgUrl, ivAlbum, new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.pic).cacheInMemory(true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build());

		if (musicName != null && !musicName.equals("")) {
			tvName.setText(musicName);
		}

		if (artist != null && !artist.equals("")) {
			tvArtist.setText(artist);
		}

		tvBuy.setText(btnBuyText);
		tvBuy.setEnabled(btnBuyEnabled);
		/*
		 * if ("已购买".equals(musicIsBought)) { tvBuy.setEnabled(false); } else {
		 * tvBuy.setEnabled(true); }
		 */
	}

	private void initListeners() {

		tvBuy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();

				if (isFromSearch) {
					Log.e(TAG, "isFromSearch");
					
					Buyer buyer = new Buyer(getMainActivityContext());
					synchronized (WatchDog.currentListeningMusic) {
						Log.e(TAG, "miniPlayer>>WatchDog.currentListeningMusic:" + WatchDog.currentListeningMusic.getName());
						buyer.setMusicToBuy(WatchDog.currentListeningMusic);
						buyer.getBalanceNLanunchBuy();
					}
				} else if (isFromAlbum) {
					Log.e(TAG, "isFromAlbum");
					
					Intent intent = new Intent("buyAlbumMusicReceiver");
					sendBroadcast(intent);
				} else if (isFromPack) {
					Log.e(TAG, "isFromPack");
					
					Intent intent = new Intent("buyPackMusicReceiver");
					sendBroadcast(intent);
				}

			}
		});

		sbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int pro = seekBar.getProgress();
				seekTo(pro);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		});

		llLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitListening();
			}
		});
		llRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitListening();
			}
		});
		llUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitListening();
			}
		});
		llDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitListening();
			}
		});
	}

	protected Context getMainActivityContext() {
		List<Activity> list = ExitApplication.getInstance().getActivityList();
		for (Activity activity : list) {
			if (activity instanceof com.kitty.poclient.activity.MainActivity) {
				return activity;
			}
		}
		return null;
	}

	private void seekTo(final int progress) {
		double seekTargetSeconds = totalSeconds * progress / 100d;
		final String targetTime = ModelUtil.toTimeString(new Long(Math.round(seekTargetSeconds)).intValue());
		sbProgress.setProgress(progress);

		UpnpApp.upnpService.getControlPoint().execute(new Seek(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService, targetTime) {
			@Override
			public void success(final ActionInvocation invocation) {
				currentPercent = progress;
				currentSeconds = (int) Math.round(totalSeconds * (currentPercent / 100d));
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

			}
		});

	}

	protected void exitListening() {
		finish();
	}
	
	@Override
	protected void onStop() {
		Log.e(TAG, "onStop()");
		
		// 停止当前曲目试听
		stopPlaying();
		stopAllHiddenThread();
		// unregisterReceivers();

		WatchDog.isWebListenActivityRunning = false;
		WatchDog.runningWebListenActivity = null;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void stopPlaying() {
		if (WatchDog.currentUri.startsWith("xxbox://listen?")) {
			new Player().stop();
		}
	}

	private void stopAllHiddenThread() {
		shouldSayHello2Death = true;
	}

	private void getPositionInfo() {
		UpnpApp.upnpService.getControlPoint().execute(new GetPositionInfo(new UnsignedIntegerFourBytes(0), UpnpApp.avTransportService) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				gpfail++;
				if (gpfail <= 2) {
					getPositionInfo();
				}
			}

			@Override
			public void received(ActionInvocation arg0, PositionInfo arg1) {
				gpfail = 0;
				positionInfo = arg1;

				duration = positionInfo.getTrackDuration().substring(3);
				totalSeconds = DateTimeFormatUtil.time2IntMillis(duration) / 1000;

				currentTime = positionInfo.getRelTime().substring(3);
				currentSeconds = DateTimeFormatUtil.time2IntMillis(currentTime) / 1000;
				currentPercent = positionInfo.getElapsedPercent();

				handler.sendEmptyMessage(0);
			}
		});
	}

	public void setStatePlaying() {
		currentState = PlayerFragment.PLAYING;
		WatchDog.currentPlayingName = getIntent().getStringExtra("musicName") == null ? getIntent().getStringExtra("musicName") : "试听曲目";

		// 获取并显示播放进度
		getPositionInfo();
		if (progressRunnable == null) {
			updateProgress(WatchDog.currentPlayingName);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (progressRunnable == null) {
			updateProgress(WatchDog.currentPlayingName);
		}

	}

	private void updateProgress(String currentPlayingName) {
		final String myOwner = currentPlayingName;
		progressRunnable = new Runnable() {
			@Override
			public void run() {
				while (currentState.equals(PlayerFragment.PLAYING) && WatchDog.currentPlayingName.equals(myOwner) && shouldSayHello2Death == false) {
					currentTime = DateTimeFormatUtil.parseInt2Time(currentSeconds * 1000);
					currentPercent = (int) Math.round(currentSeconds * 1.0 * 100 / totalSeconds);

					handler.sendEmptyMessage(0);

					currentSeconds++;

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				progressRunnable = null;
			}
		};

		progressThread = new Thread(progressRunnable);
		progressThread.start();
	}
}
