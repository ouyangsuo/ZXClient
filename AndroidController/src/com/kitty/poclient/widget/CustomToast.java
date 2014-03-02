package com.kitty.poclient.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import com.kitty.poclient.R;

public class CustomToast extends Toast {
	private TextView mTextView;

	public CustomToast(Context context) {
		super(context);
		mTextView = new TextView(context);
		mTextView.setBackgroundResource(R.drawable.toast_bg);
		mTextView.setPadding(42, 18, 42, 18);
		mTextView.setTextSize(22f);
		mTextView.setTextColor(Color.WHITE);
		mTextView.getPaint().setFakeBoldText(true);
		setView(mTextView);
	}

	@Override
	public void setText(int resId) {
		mTextView.setText(resId);
	}

	@Override
	public void setText(CharSequence s) {
		mTextView.setText(s);
	}
	public static Toast makeText(Context context, CharSequence text, int duration) {
		Toast t = new CustomToast(context);
		t.setText(text);
		t.setDuration(duration);
		return t;
	}
	
	public static Toast makeText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
	return makeText(context, context.getResources().getText(resId), duration);
	}
}
