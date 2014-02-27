package com.dfim.app.fragment.mymusic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.dfim.app.common.BroadcastManager;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.PackDao;
import com.dfim.app.domain.Music;
import com.dfim.app.domain.Pack;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.PowerfulBigMan;
import com.union.cellremote.R;
import com.union.cellremote.adapter.MusicListAdapter;

public class MymusicThemeDetailFragment extends BaseFragment {
	private final String TAG = MymusicThemeDetailFragment.class.getSimpleName();
	
	private View view;
	
	private ListView listview;
	private MusicListAdapter adapter;
	
	private Activity attachedActivity;
	private Pack pack;
	
	
	public MymusicThemeDetailFragment(Activity parentActivity, Pack pack) {
		this.attachedActivity = parentActivity;
		this.pack = pack;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.mymusi_theme_detail_fragment, null);
		initComponents();
		initArguments();
		initListeners();
		registerReceivers();
		return view;
	}
	
	public String getFragmentName(){
		return TAG;
	}

	private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Pack newPack =new PackDao().getPackDetailById(pack);
			
			List<Music> musicList = new ArrayList<Music>();
			if(newPack != null){
				musicList = newPack.getLi();
			}
			adapter.setMlist(musicList) ;
			Log.i(TAG, "musicList.size=" + newPack.getLi().size()+"");
		    adapter.notifyDataSetChanged();
		}
	};
	
	private void registerReceivers() {
		attachedActivity.registerReceiver(updateListReceiver, new IntentFilter(BroadcastManager.FILTER_UPDATE_LOCALLIST));
	}
	private void unregisterReceivers() {
		attachedActivity.unregisterReceiver(updateListReceiver);
	}
	
	public void initComponents() {
		listview = (ListView) view.findViewById(R.id.lv_packdetaillist);
	}
	public String getThemeName(){
		String name = "未知";
		if(null != pack){
			name = pack.getName();
		}
		return name;
	}
	private void initArguments() {
		adapter = new MusicListAdapter(attachedActivity, pack.getLi());
		listview.setAdapter(adapter);
	}

	private void initListeners() {

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}

				// 初始化播放列表
				WatchDog.currentList = (ArrayList<Music>) pack.getLi();
				WatchDog.currentListType = Constant.URI_THEME;
				WatchDog.currentListId = pack.getId();

				WatchDog.currentPlayingMusic = pack.getLi().get(position);
				WatchDog.currentPlayingId = WatchDog.currentPlayingMusic.getId();
				WatchDog.currentPlayingName = pack.getLi().get(position).getName();
				WatchDog.currentArtistName = pack.getLi().get(position).getArtistName();

				WatchDog.updateCachingState();// 如果上一曲未缓存完则修改其状态为等待
				WatchDog.currentState = PlayerFragment.STOPPED;// 先改为停止以停止播放器图标的跳动

				UIHelper.refreshPlayerButton();
				adapter.notifyDataSetChanged();

				String uri = pack.getLi().get(position).getUri(Constant.URI_THEME, pack.getId());
				new Player().play(uri);
			}
		});
	}

	@Override
	public void onResume() {
		System.out.println(TAG + " onResume");
		if (adapter != null) {
			adapter.notifyDataSetChanged();
			System.out.println(TAG + "adapter.notifyDataSetChanged()");
		} else {
			System.out.println(TAG + "adapter==null");
		}
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDetach");
		WatchDog.keybackRefersExitPro=true;//返回键指向退出程序
		unregisterReceivers();
		super.onDetach();
	}
}
