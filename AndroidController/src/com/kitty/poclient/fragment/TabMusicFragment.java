package com.kitty.poclient.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.MymusicManager;
import com.kitty.poclient.common.UIHelper;
import com.kitty.poclient.fragment.mymusic.AlbumDetailFragment;
import com.kitty.poclient.fragment.mymusic.AlbumListFragment;
import com.kitty.poclient.fragment.mymusic.BaseFragment;
import com.kitty.poclient.fragment.mymusic.CacheAlbumDetailFragment;
import com.kitty.poclient.fragment.mymusic.MymusicThemeDetailFragment;
import com.kitty.poclient.fragment.mymusic.MymusicThemesFragment;
import com.kitty.poclient.fragment.mymusic.SinglesFragment;

public class TabMusicFragment extends TabFragment {

	private static final String TAG = TabMusicFragment.class.getSimpleName();
	public static boolean IS_ALIVE = false;

	public static final int ALBUM = 0;
	public static final int SINGLE = 1;
	public static final int THEME = 2;

	private TextView tabTitle;
	private ImageButton btnMenuOrBack;
	private ImageButton btnPlayer;
	private String menuItems[];
	public static int currentPosition = ALBUM;

	private OnClickListener menuOrBackButtonListener;
	private OnClickListener btnPlayerButtonListener;

	private AlbumDetailFragment lastAlbumDetailFragment;

	private String currentFragmentName;
	
	private FragmentManager musicFragmentManager;

	private BroadcastReceiver initBtnPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshPlayStatus();
		}
	};

	public void refreshPlayStatus() {
		UIHelper.initMusicFragmentBtnPlayer(btnPlayer, getResources(), null);
	}

	private void registerReceivers() {
		getActivity().registerReceiver(initBtnPlayerReceiver, new IntentFilter("initBtnPlayerReceiver"));
	}

	private void unregisterReceivers() {
		getActivity().unregisterReceiver(initBtnPlayerReceiver);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Fragment parentFragment = getParentFragment();
		if(parentFragment instanceof TabFragment.OnTitleClickListener){
			this.listener = (TabFragment.OnTitleClickListener)parentFragment;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		musicFragmentManager = getChildFragmentManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e(TAG,"onCreateView");
		View view = inflater.inflate(R.layout.layout_tab_main, null);
		tabTitle = (TextView) view.findViewById(R.id.tv_title);
		btnMenuOrBack = (ImageButton) view.findViewById(R.id.btn_menu);
		btnPlayer = (ImageButton) view.findViewById(R.id.btn_player);

		initListeners();
		
		menuItems = getActivity().getResources().getStringArray(R.array.sliding_menu_music);

		setContentFragment(menuItems[currentPosition], currentPosition);
		
		registerReceivers();

		MymusicManager.tabMusicFragment = this;
//		MymusicManager.receiveCacheSub();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		IS_ALIVE = true;
		super.onResume();
	}

	@Override
	public void onPause() {
		IS_ALIVE = false;
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		super.onDetach();
	}

	public void back() {
		int backStackEntryCount = musicFragmentManager.getBackStackEntryCount();
		int topFragmentIndex = backStackEntryCount - 1;
		String topFragemntName = musicFragmentManager.getBackStackEntryAt(topFragmentIndex).getName();

		Log.i(TAG, "BackStackEntryCount:" + backStackEntryCount);
		Log.i(TAG, "topFragemntName:" + topFragemntName);
		
		if (isRoot(topFragemntName)) {
			UIHelper.showExitDialog(MymusicManager.mainActivity);
		} else {
			musicFragmentManager.popBackStack();
			topFragmentIndex--;
		}
		
		boolean isMenu = false;
		if(topFragmentIndex>=0){
			topFragemntName = musicFragmentManager.getBackStackEntryAt(topFragmentIndex).getName();
			currentFragmentName = topFragemntName;
			if(isRoot(topFragemntName)){
				isMenu = true;
			}
		}else{
			isMenu = true;
		}
		
		//按钮
		resetMenuOrBack(isMenu);		
		//标题
		resetTile(currentFragmentName);
		
	}
	
	private boolean isRoot(String name){
		boolean flag = false;
		if(name.equals(AlbumListFragment.class.getSimpleName())
				|| name.equals(SinglesFragment.class.getSimpleName())
				|| name.equals(MymusicThemesFragment.class.getSimpleName())
				){
			flag = true;	
		}
		return flag;
	}
	
	private void initListeners() {
		menuOrBackButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRoot(currentFragmentName)) {
					Log.i(TAG, "perform MENU, currentFragment:" + currentFragmentName);
					if(listener != null)
						listener.onMenuClick(); // 此时为菜单按钮
				} else {
					Log.i(TAG, "perform BACK, currentFragment:" + currentFragmentName);
					back();
				}
			}
		};
		btnPlayerButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onPlayerClick();
				}
			}
		};

		btnMenuOrBack.setOnClickListener(menuOrBackButtonListener);
		btnPlayer.setOnClickListener(btnPlayerButtonListener);
	}

	private AlbumListFragment getAlbumsFragment() {
		AlbumListFragment albumListFragment= new AlbumListFragment(getActivity(), this);		
		return albumListFragment;
	}

	private BaseFragment getSinglesFragment() {
		SinglesFragment singlesFragment= new SinglesFragment();		
		return singlesFragment;
	}

	private BaseFragment getThemesFragment() {
		MymusicThemesFragment mymusicThemesFragment =  new MymusicThemesFragment(getActivity(), this);
		return mymusicThemesFragment;
	}
	
	//
	private void setContentFragment(BaseFragment fragment) {
		FragmentTransaction transaction = musicFragmentManager.beginTransaction();
		transaction.replace(R.id.fragment_stub, fragment);
		transaction.addToBackStack(fragment.getFragmentName()); 
		transaction.commit();
		
		currentFragmentName = fragment.getFragmentName();
		Log.i(TAG, "currentFragment.getFragmentName:" + currentFragmentName);
		
		//按钮 图标
		resetMenuOrBack(isRoot(currentFragmentName));
		
		//标题
		resetTile(currentFragmentName);
		
	}
	
	private void resetTile(String fragmentName){
		int index = -1;
		if(fragmentName.equals(AlbumListFragment.class.getSimpleName())){
			index = 0;	
		} else if(fragmentName.equals(SinglesFragment.class.getSimpleName())){
			index = 1;
		} else if(fragmentName.equals(MymusicThemesFragment.class.getSimpleName())){
			index = 2;
		}
		
		if(index != -1){
			tabTitle.setText(menuItems[index]);
		}
	}
	
	public void setContentFragment(String title, int position) {
		// 标题
		if(title!=null){
			tabTitle.setText(title);
		}
		currentPosition = position;
		switch (position) {
			case ALBUM:
				setContentFragment(getAlbumsFragment());
				break;
			case SINGLE:
				setContentFragment(getSinglesFragment());
				break;
			case THEME:
				setContentFragment(getThemesFragment());
				break;
		}
	}
	
	/**
	 * @param isMenu
	 */
	private void resetMenuOrBack(boolean isMenu){
		if (isMenu) {
			// 菜单 图标
			btnMenuOrBack.setImageResource(R.drawable.btn_menu);
		} else {
			// 退回 图标
			btnMenuOrBack.setImageResource(R.drawable.btn_back);
		}
	}

	private void setContentFragment(BaseFragment fragment, String title) {
		// 标题
		tabTitle.setText(title);
		// content fragment
		setContentFragment(fragment);
	}

	public void showAlbumDetailFragment(AlbumDetailFragment fragment) {
		String title = fragment.getAlbumName();
		setContentFragment(fragment, title);
		lastAlbumDetailFragment = fragment; // 记录打开的专辑
	}

	public void showThemeDetailFragment(MymusicThemeDetailFragment fragment) {
		String title = fragment.getThemeName();
		setContentFragment(fragment, title);
	}

	public void showCacheAlbumDetailFragment(CacheAlbumDetailFragment fragment) {
		String title = lastAlbumDetailFragment.getAlbumName();
		setContentFragment(fragment, title);
	}
	
	public static int getCurrentPosition(){
		return currentPosition;
	}
}
