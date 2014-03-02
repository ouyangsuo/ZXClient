package com.kitty.poclient.util;

import java.util.HashMap;

import org.fourthline.cling.binding.annotations.UpnpAction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.MusicDetail;
import com.kitty.poclient.http.HttpGetter;
import com.kitty.poclient.models.PlayingInfo;
import com.kitty.poclient.test.MusicPlayService;
import com.kitty.poclient.test.MusicServiceActivity;

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
					initPlayingInfo(mDetail);
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
	
	protected void initPlayingInfo(MusicDetail mDetail) {
		String name=mDetail.getName();
		String artist=mDetail.getArtist();
		String imgUrl=mDetail.getImgUrl();
		String duration=mDetail.getDuration();
		
		WatchDog.setCurrentPlayingInfo(new PlayingInfo(name, artist, imgUrl, duration));
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
