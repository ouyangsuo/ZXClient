package com.dfim.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MediaUtil {
	
	private Context context;
	
	public MediaUtil(Context context){
		this.context=context;
	}

	public void playAudio(String audioPath) {
		final String url=audioPath;
		
//		Intent intent = new Intent();
//		intent.setAction(android.content.Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.parse(url), "audio/mp3");
//		intent.setComponent(new ComponentName("com.android.music", "com.android.music.MediaPlaybackActivity"));
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
//		UpnpApp.context.startActivity(intent);
		
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.setDataAndType(Uri.parse(url), "audio/mp3");
		context.startActivity(it);

	}
}
