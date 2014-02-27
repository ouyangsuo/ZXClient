package com.dfim.app.fragment;

import com.dfim.app.fragment.setting.SettingsFragment;
import com.dfim.app.fragment.usb.ExternalDeviceFragment;
import com.union.cellremote.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

public class MainFragment extends Fragment implements TabFragment.OnTitleClickListener {

	private final static String TAG = "MainFragment ";

	public static final int TAB_MUSIC = 0;
	public static final int TAB_WEB = 1;
	public static final int TAB_DEVICE = 2;
	public static final int TAB_SETTING = 3;

	public OnMainChangedListener listener;

	public void setOnMainChangedListener(OnMainChangedListener listener) {
		this.listener = listener;
	}

	public interface OnMainChangedListener {
		void onTabChanged(String tabId);

		void onToggle();

		void onPlayerClick();
	}

	private FragmentTabHost mTabHost;
	private TabWidget mTabWidget;
	private String tab_arrays[];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println(TAG + "onCreateView");

		View view = (FragmentTabHost) inflater.inflate(R.layout.layout_tabhost_main, null);
		mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realcontent);
		tab_arrays = getResources().getStringArray(R.array.tab_arrays);
		
		mTabHost.addTab(mTabHost.newTabSpec(tab_arrays[TAB_MUSIC]).setIndicator("", getResources().getDrawable(R.drawable.tab_music_selector)), TabMusicFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(tab_arrays[TAB_WEB]).setIndicator("", getResources().getDrawable(R.drawable.tab_store_selector)), TabWebFragment.class, null);
//		mTabHost.addTab(mTabHost.newTabSpec(tab_arrays[TAB_DEVICE]).setIndicator("", getResources().getDrawable(R.drawable.tab_device_selector)), ExternalDeviceFragment.class, null);
//		mTabHost.addTab(mTabHost.newTabSpec(tab_arrays[TAB_SETTING]).setIndicator("", getResources().getDrawable(R.drawable.tab_setting_selector)), SettingsFragment.class, null);
		
		mTabWidget = mTabHost.getTabWidget();
		int h = getResources().getInteger(R.integer.tab_widget_height);

		for (int i = 0, count = mTabWidget.getTabCount(); i < count; i++) {
			mTabWidget.getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_background_selector));
			mTabWidget.getChildAt(i).getLayoutParams().height = h;
		}

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				if (listener != null)
					listener.onTabChanged(tabId);
			}
		});

		return view;
	}

	public void menuChanged(String currentFragment, int position) {
		Log.i(getTag(), "currentFragment " + currentFragment + " ,position " + position);
		System.out.println(TAG + "menuChanged(" + currentFragment + "," + position + ")");
//		TabWebFragment webFragment = (TabWebFragment) getChildFragmentManager().findFragmentByTag(tab_arrays[TAB_WEB]);

		switch (mTabHost.getCurrentTab()) {
		case TAB_MUSIC:
			TabMusicFragment musicFragment = (TabMusicFragment) getChildFragmentManager().findFragmentByTag(tab_arrays[TAB_MUSIC]);
			musicFragment.setContentFragment(currentFragment, position);
			break;
			
		case TAB_WEB:
			TabWebFragment webFragment = (TabWebFragment) getChildFragmentManager().findFragmentByTag(tab_arrays[TAB_WEB]);
			webFragment.setContentFragment(currentFragment, position);
			break;
			
		case TAB_DEVICE:
			break;
			
		case TAB_SETTING:
			break;
		}
	}

	public void onSearchClick() {
		System.out.println(TAG + "onSearchClick");

		switch (mTabHost.getCurrentTab()) {
		case TAB_WEB:
			TabWebFragment webFragment = (TabWebFragment) getChildFragmentManager().findFragmentByTag(tab_arrays[TAB_WEB]);
			webFragment.setSearchFragment();
			break;
		default:
			break;
		}
	}

	public static int getTabPosition(String name, Context context) {
		System.out.println(TAG + "getTabPosition(" + name + "," + context + ")");

		String tab_arrays[] = context.getResources().getStringArray(R.array.tab_arrays);
		int position = -1;
		for (int i = 0, count = tab_arrays.length; i < count; i++) {
			if (name.equals(tab_arrays[i])) {
				position = i;
				break;
			}
		}
		return position;
	}

	@Override
	public void onMenuClick() {
		if (listener != null)
			listener.onToggle();
	}

	@Override
	public void onPlayerClick() {
		if (listener != null)
			listener.onPlayerClick();
	}

}
