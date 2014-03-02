package com.kitty.poclient.util;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.kitty.poclient.R;

public class AnimUtil {

	private Context context;

	public AnimUtil(Context context) {
		this.context = context;
	}

	public void initLoadingmoreAnim(ImageView ivAnim) {
		AnimationDrawable ad = (AnimationDrawable) context.getResources().getDrawable(R.anim.loading_more_anim);
		ivAnim.setBackgroundDrawable(ad);
		ad.start();
	}
	
	public void initAnim(ImageView ivAnim, int animId) {
		AnimationDrawable ad = (AnimationDrawable) context.getResources().getDrawable(animId);
		ivAnim.setBackgroundDrawable(ad);
		ad.start();
	}

}
