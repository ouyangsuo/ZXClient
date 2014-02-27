package com.dfim.app.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.dfim.app.fragment.MenuFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.union.cellremote.R;

//sliding_menu_single_2
public class SlidingBaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected MenuFragment mFrag;

	public SlidingBaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new MenuFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (MenuFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

	}
	
	public void setOnMenuChangedListener(MenuFragment.OnMenuChangedListener listener){
		((MenuFragment)mFrag).setOnMenuChangedListener(listener);
	}
	
	public void setOnSearchViewClickListener(MenuFragment.OnSearchViewClickListener listener){
		((MenuFragment)mFrag).setOnSearchViewClickListener(listener);
	}
}
