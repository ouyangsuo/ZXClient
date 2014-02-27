package com.dfim.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dfim.app.adapter.MenuListAdapter;
import com.dfim.app.common.UpnpApp;
import com.union.cellremote.R;

public class MenuFragment extends ListFragment {
	private String currentFragment = null;
	private MenuListAdapter adapter = null;
	private String[] menuItems = null;
	private OnMenuChangedListener onMenuChangedListenr;
	private OnSearchViewClickListener onSearchViewClickListener;
	private RelativeLayout rlSearch;
	private Button btnSearch;

	public void setOnMenuChangedListener(OnMenuChangedListener listener) {
		this.onMenuChangedListenr = listener;
	}

	public void setOnSearchViewClickListener(OnSearchViewClickListener listener) {
		this.onSearchViewClickListener = listener;
	}

	public interface OnMenuChangedListener {
		void onMenuChanged(String currentFragment, int position);
	}

	public interface OnSearchViewClickListener {
		void onSearchViewClick();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mymusic_menu_fragment, null);
		rlSearch = (RelativeLayout) view.findViewById(R.id.rl_search);
		btnSearch = (Button) rlSearch.findViewById(R.id.btn_search);
		rlSearch.setVisibility(View.GONE);

		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("searchview onClick");
				onSearchViewClickListener.onSearchViewClick();
			}
		});

		return view;
	}

	public void setSearchViewVisibility(int arg) {
		rlSearch.setVisibility(arg);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 菜单 item 适配器
		menuItems = getResources().getStringArray(R.array.sliding_menu_music);
		if (menuItems != null && menuItems.length > 0) {
			currentFragment = menuItems[0];
			adapter = new MenuListAdapter(UpnpApp.context, menuItems, "tab_music", currentFragment);
			setListAdapter(adapter);
		}
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {

		adapter.setSelectedItem(menuItems[position]);
		if (onMenuChangedListenr != null) {
			onMenuChangedListenr.onMenuChanged(menuItems[position], position);
		}
	}

	public void setAdapter(String[] menuItems, int currentFragmetPosition, String tab) {
		this.currentFragment = menuItems[currentFragmetPosition];
		this.menuItems = menuItems;
		this.adapter = new MenuListAdapter(UpnpApp.context, menuItems, tab, currentFragment);
		setListAdapter(adapter);
	}
}
