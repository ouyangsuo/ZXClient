package com.dfim.app.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.common.WatchDog;
import com.dfim.app.util.ExitApplication;
import com.union.cellremote.R;

//演出者，未知演出者
public class AlbumInfoActivity extends Activity {

	public static final String TAG = "AlbumInfoActivity: ";

	private TextView tvArtist;
	private TextView tvPublishtime;
	private TextView tvLanguage;
	private TextView tvCompany;
	private TextView tvText;

	private LinearLayout llLeft, llRight, llUp, llDown;// 空白区域

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		WatchDog.currentActivities.add(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		getWindow().getDecorView().setSystemUiVisibility(4);
		setContentView(R.layout.album_info_activity);

		initComponent();
		getData();
		initListeners();
	}

	private void initComponent() {
		tvArtist = (TextView) findViewById(R.id.tv_artist);
		tvPublishtime = (TextView) findViewById(R.id.tv_publishtime);
		tvLanguage = (TextView) findViewById(R.id.tv_language);
		tvCompany = (TextView) findViewById(R.id.tv_company);
		tvText = (TextView) findViewById(R.id.tv_text);

		llLeft = (LinearLayout) findViewById(R.id.ll_left);
		llRight = (LinearLayout) findViewById(R.id.ll_right);
		llUp = (LinearLayout) findViewById(R.id.ll_up);
		llDown = (LinearLayout) findViewById(R.id.ll_down);
	}

	private void getData() {
		ArrayList<String> list = getIntent().getStringArrayListExtra("paramList");

		tvArtist.setText(list.get(0));
		tvPublishtime.setText(list.get(1));
		tvLanguage.setText(list.get(2));
		tvCompany.setText(list.get(3));
		tvText.setText(list.get(4));
	}

	private void initListeners() {
		llLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		llRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		llUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		llDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
