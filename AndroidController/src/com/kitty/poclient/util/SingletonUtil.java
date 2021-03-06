package com.kitty.poclient.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import com.kitty.poclient.R;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Album;

/**
 * 
 * 单例模式Util，用于处理同一个对象相关操作
 *
 */
public class SingletonUtil {
	public static boolean imagflag=false;
    private static SingletonUtil singletonutil=null;

	private SingletonUtil(){
		
	}
	
	public static SingletonUtil getSingletonUtil(){
		if(singletonutil==null){
			singletonutil=new SingletonUtil();
		}
		return singletonutil;
	}
	public  void loadAlbumImage(Album album,final View listView, View imageView) {
		String imageurl="";
		if (album.getImgUrl() != null) {
			if(WatchDog.synBoxOrservice==1){
				final String imageKey = album.getImgUrl();
				imageurl = album.getImgUrl();
				Bitmap bitmap=BitmapUtil.loadImageAysnc.loadImage(imageKey, imageurl,listView);
				
				if (bitmap != null) {
					if(bitmap.isRecycled()){
						bitmap=null;
						Log.i("mm","recycled");
					}else{
						imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
					
						
					}
		    }else{
		    	//imageView.setBackgroundDrawable(new BitmapDrawable(BitmapUtil.decodeScaleScream(context.getResources().openRawResource(R.drawable.pic),2)));
		          imageView.setBackgroundResource(R.drawable.pic);
		    }
				
//				try {
					for(int i=0;i<100;i++){
						
					}
					if(bitmap!=null){
					//bitmap.recycle();
					bitmap=null;
					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			} else {
				System.out.println("album.getImgUrl() == null");
			}

		
	}
		
	}
}
