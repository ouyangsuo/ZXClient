package com.dfim.app.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BasePagerAdapter extends FragmentPagerAdapter {
	
	private ArrayList<Fragment> mFragments;

	public BasePagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
		super(fm);
		mFragments = fragmentList;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

}
