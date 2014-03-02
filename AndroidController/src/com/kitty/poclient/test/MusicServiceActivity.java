package com.kitty.poclient.test;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kitty.poclient.R;

public class MusicServiceActivity extends Activity {

	private static String TAG = "MusicService";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_service_activity);
		Toast.makeText(this, "MusicServiceActivity",
		Toast.LENGTH_SHORT).show();
		Log.e(TAG, "MusicServiceActivity");

		initlizeViews();
	}

	private void initlizeViews() {
		Button btnStart = (Button) findViewById(R.id.startMusic);
		Button btnStop = (Button) findViewById(R.id.stopMusic);
		Button btnBind = (Button) findViewById(R.id.bindMusic);
		Button btnUnbind = (Button) findViewById(R.id.unbindMusic);

		OnClickListener ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 显示指定 intent所指的对象是个 service
				Intent intent = new Intent(MusicServiceActivity.this, MusicPlayService.class);

				switch (v.getId()) {
				case R.id.startMusic:
					startService(intent);
					break;

				case R.id.stopMusic:
					stopService(intent);
					break;

				case R.id.bindMusic:
					bindService(intent, conn, Context.BIND_AUTO_CREATE);
					break;

				case R.id.unbindMusic:
					unbindService(conn);
					break;
				}
			}
		};

		// 绑定点击监听

		btnStart.setOnClickListener(ocl);
		btnStop.setOnClickListener(ocl);
		btnBind.setOnClickListener(ocl);
		btnUnbind.setOnClickListener(ocl);
	}

	// 定义服务链接对象

	final ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(MusicServiceActivity.this, "MusicServiceActivity onSeviceDisconnected", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "MusicServiceActivity onSeviceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(MusicServiceActivity.this, "MusicServiceActivity onServiceConnected",Toast.LENGTH_SHORT).show();
			Log.e(TAG, "MusicServiceActivity onServiceConnected");
		}

	};

}
