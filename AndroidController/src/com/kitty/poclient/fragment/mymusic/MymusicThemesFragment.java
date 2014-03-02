package com.kitty.poclient.fragment.mymusic;

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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.adapter.ThemeListAdapter;
import com.kitty.poclient.bean.LocalThemes;
import com.kitty.poclient.common.BroadcastManager;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Pack;
import com.kitty.poclient.fragment.TabMusicFragment;
import com.kitty.poclient.service.impl.PackServiceImpl;

public class MymusicThemesFragment extends BaseFragment {

	private final String TAG = MymusicThemesFragment.class.getSimpleName();

	private LinearLayout llLvThemes;
	private TextView tvNoData;

	private ListView lvThemes;
	private ListAdapter adapter;
	private View view;
	
//	private Themes themes;
	private Activity attachedActivity;
	private TabMusicFragment tabMusicFragment;
	
	public MymusicThemesFragment(Activity parentActivity, TabMusicFragment fragment) {
		this.attachedActivity = parentActivity;
		this.tabMusicFragment = fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.mymusic_themes_fragment, null);// 空指针
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
			boolean refreshThemeList = intent.getBooleanExtra(BroadcastManager.EXTRA_INCLUDE_LOCALTHEME, BroadcastManager.EXTRA_BOOLEAN_DEFAULT);
			if (refreshThemeList) {
				if(adapter != null){
					((BaseAdapter) adapter).notifyDataSetChanged();
				}
			}
		}
	};
	private BroadcastReceiver onCacheStateMapEstablishedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("BUG580", TAG+ "onCacheStateMapEstablishedReceiver onReceive():WatchDog.cacheStateMap.size()="+WatchDog.cacheStateMap.size());
			// 初始化 缓存状态
			if(VirtualData.packs!=null && VirtualData.packs.size()>0){
				VirtualData.localThemes = LocalThemes.translatePacks(VirtualData.packs);
				if (adapter != null) {
					((BaseAdapter) adapter).notifyDataSetChanged();
					Log.i(TAG, "Refresh theme cache state according all music cache state");
				}
			}
		}
	};
	
	private void initArguments() {
		if (VirtualData.packs == null || VirtualData.packs.size() == 0 
				|| VirtualData.albums == null || VirtualData.albums.size() == 0
				|| VirtualData.localThemes == null || VirtualData.localThemes.size() == 0) {
			// 显示暂无数据
			llLvThemes.setVisibility(View.GONE);
			tvNoData.setVisibility(View.VISIBLE);
		} else {
			llLvThemes.setVisibility(View.VISIBLE);
			tvNoData.setVisibility(View.GONE);
			
			adapter = new ThemeListAdapter(attachedActivity, lvThemes, VirtualData.localThemes);
			lvThemes.setAdapter(adapter);
		}
	}

	private void initComponents() {
		llLvThemes = (LinearLayout) view.findViewById(R.id.ll_lv_themes);
		tvNoData = (TextView) view.findViewById(R.id.tv_no_data);
		lvThemes = (ListView) view.findViewById(R.id.lv_themes);
//		adapter = new ThemeListAdapter(attachedActivity, lvThemes, VirtualData.localThemes);
//		lvThemes.setAdapter(adapter);
	}

	private void initListeners() {
		lvThemes.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
//				Pack pack = (Pack) lvThemes.getItemAtPosition(position); 
				Pack pack = VirtualData.packs.get(position);
				pack = new PackServiceImpl().getPackDetailById(pack);
				
				MymusicThemeDetailFragment themeDetail = new MymusicThemeDetailFragment(attachedActivity, pack);
				tabMusicFragment.showThemeDetailFragment(themeDetail);
			}
		});
	}

	@Override
	public void onResume() {
		registerReceivers();
		super.onResume();
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		super.onDetach();
	}
	

	private void unregisterReceivers() {
		attachedActivity.unregisterReceiver(updateListReceiver);
		attachedActivity.unregisterReceiver(onCacheStateMapEstablishedReceiver);
	}

	private void registerReceivers() {
		attachedActivity.registerReceiver(updateListReceiver, new IntentFilter(BroadcastManager.FILTER_UPDATE_LOCALLIST));
		attachedActivity.registerReceiver(onCacheStateMapEstablishedReceiver, new IntentFilter("onCacheStateMapEstablishedReceiver"));
	}
}
