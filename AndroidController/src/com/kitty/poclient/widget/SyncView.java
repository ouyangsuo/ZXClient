package com.kitty.poclient.widget;

import com.kitty.poclient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncView extends LinearLayout{

	private TextView deviceName;
	private ProgressBar progress;
	private TextView percentage;
	
	public SyncView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		deviceName = (TextView)findViewById(R.id.tv_login_device_name);
		progress = (ProgressBar)findViewById(R.id.pb_login_sync);
		percentage = (TextView)findViewById(R.id.tv_login_sync_percentage);
	}

	public void setPercentage(int values) {
		progress.setProgress(values);
		percentage.setText(values+"%");
		if(values == 100){
			((ProgressBar) findViewById(R.id.pb_login_sync_loading)).setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.pb_login_sync_complete)).setVisibility(View.VISIBLE);
		}
	}

	public void setName(String string) {
		deviceName.setText(string);
	}

	
}
