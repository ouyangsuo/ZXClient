package com.dfim.app.fragment.setting;

//import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dfim.app.activity.MainActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.MymusicManager;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.Music;
import com.dfim.app.fragment.PlayerFragment;
import com.union.cellremote.R;

public class SettingsFragment extends Fragment {
	
	public static final String TAG = SettingsFragment.class.getSimpleName()+":";
	public static boolean IS_ALIVE=false;
	
	private View view;
	private ImageButton btnPlayer;
	private TextView tvCurrentDevice;
	private TextView tvCurrentUser;
	private TextView tvBoxVersion;
	private TextView tvControllerVersion;
	private Button btnResearchBox;
	private Button shoudongResearchBox;

	public SettingsFragment() {
		
	}

	@Override
	public void onResume() {
		super.onResume();
		IS_ALIVE = true;
	}
	@Override
	public void onPause() {
		super.onPause();
		IS_ALIVE = false;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.settings_fragment, null);
		initComponent();
		initBtnPlayer();
		registerReceivers();
		return view;
	}

	private void initComponent() {
		btnPlayer = (ImageButton) view.findViewById(R.id.btn_player);
		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDog.checkMediaReady()) {
					return;
				}

				MymusicManager.mainActivity.showViewpage(MainActivity.PLAYER_PAGE_ITEM_NUM);
			}
		});

		tvCurrentDevice = (TextView) view.findViewById(R.id.tv_current_device);
		tvCurrentDevice.setText(WatchDog.currentDevice);

		tvCurrentUser = (TextView) view.findViewById(R.id.tv_current_user);
		if (WatchDog.currentUserId.equals("0")) {
			tvCurrentUser.setText("当前用户：无用户");
		} else {
			tvCurrentUser.setText("当前用户：" + WatchDog.currentUserId);
		}
		
		tvBoxVersion = (TextView) view.findViewById(R.id.tv_device_version);
		tvBoxVersion.setText("设备版本号：" +WatchDog.boxVersionName);
		
		tvControllerVersion = (TextView) view.findViewById(R.id.tv_controller_version);
		tvControllerVersion.setText("控制端版本号：" +WatchDog.currentControllerVersion);

		btnResearchBox = (Button) view.findViewById(R.id.btn_research_box);
		btnResearchBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("BUG965", TAG+"btnResearchBox onClick");
				getActivity().sendBroadcast(new Intent("finishMainActivity"));
				resetTabMusicFragment();
				markResearch();
			}
		});

		shoudongResearchBox = (Button) view.findViewById(R.id.shoudong_research_box);
		shoudongResearchBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WatchDog.updateLocalData(getActivity());
			}
		});

	}

	protected void markResearch() {
		WatchDog.researchFlag = true;
	}

	protected void resetTabMusicFragment() {
		MymusicManager.tabMusicFragment.currentPosition=MymusicManager.tabMusicFragment.ALBUM;
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
			// btnPlayer.setEnabled(false);
		} else {
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
			// btnPlayer.setImageResource(R.drawable.btn_player);
			// btnPlayer.setEnabled(true);
		}
		
		//外联设备文件播放情况
        if(WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE){
			if(WatchDog.currentState.equals(PlayerFragment.PLAYING)){
				AnimationDrawable animationDrawable = (AnimationDrawable) getResources()
						.getDrawable(R.anim.playing);
				btnPlayer.setImageDrawable(animationDrawable);
				animationDrawable.start();
			}else{
				btnPlayer.setImageResource(R.drawable.btn_player);
			}
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}

	private BroadcastReceiver initBtnPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			initBtnPlayer();
		}
	};

	private void registerReceivers() {
		getActivity().registerReceiver(initBtnPlayerReceiver, new IntentFilter("initBtnPlayerReceiver"));
	}

	private void unregisterReceivers() {
		getActivity().unregisterReceiver(initBtnPlayerReceiver);
	}

}
