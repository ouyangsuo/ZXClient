package com.kitty.poclient.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class FileUtil {
	
	private Context context;
	
	public FileUtil(Context context){
		this.context=context;
	}
	
	public static boolean musicExist(String uri){
		System.out.println("test="+new File("/mnt/sdcard/xxbox/cfg/cfg.txt").exists());
		String path=uri.substring(6);
		File file=new File(path);
		return file.exists();	
	}
	
	public static void getBitmapFromUrl(String url){
		
	}
	
	public void installAPKFile(String fileName){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
}
