package com.dfim.app.util;

import java.util.HashMap;

import org.fourthline.cling.binding.annotations.UpnpAction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.MusicDetail;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.test.MusicPlayService;
import com.dfim.app.test.MusicServiceActivity;

public class MediaUtil {

	private Context context;

	public MediaUtil(Context context) {
		this.context = context;
	}

	public void playLocally(final Long musicId) {
		// 显示progressDialog
		final ProgressDialog pd=new ProgressDialog(context);
		pd.setMessage("正在读取...");
		pd.show();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String json=new HttpGetter(context).getMusicDetail(musicId);
				System.out.println("jsonMusicDetail="+json);
				
				try {
					MusicDetail mDetail=new JsonUtil().getMusicDetail(json);
					playAudio(mDetail.getListenUrl());
					pd.dismiss();
				} catch (Exception e) {
					UpnpApp.mainHandler.showAlert("网络异常！");
					pd.dismiss();
					e.printStackTrace();
				}
			}
		}).start();		
	}
	
	public void playAudio(String audioPath) {
		audioPath = processUrl(audioPath);
		final String url = audioPath;

		// Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setDataAndType(Uri.parse(url), "audio/*");
		// intent.setComponent(new ComponentName("com.android.music",
		// "com.android.music.MediaPlaybackActivity"));
		// context.startActivity(intent);

		// Intent it = new Intent(Intent.ACTION_VIEW);
		// it.setDataAndType(Uri.parse(url), "audio/*");
		// context.startActivity(it);

		if (WatchDog.runningMusicPlayServiceIntent != null) {
			context.stopService(WatchDog.runningMusicPlayServiceIntent);
		}

		Intent intent = new Intent(context, MusicPlayService.class);
		intent.putExtra("url", url);
		context.startService(intent);
		WatchDog.runningMusicPlayServiceIntent = intent;
	}

	private String processUrl(String mediaurl) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("terminaltype", 0);
		paramMap.put("apikey", Constant.DEVICE_NUMBER);
		paramMap.put("timestamp", System.currentTimeMillis());
		paramMap.put("protocolver", "zx/1.1");
		String playurl = SignaturGenUtil.createurl(mediaurl, paramMap, Constant.DEVICE_KEY);
		System.out.println("playurl=" + playurl);

		return playurl;
	}

}
