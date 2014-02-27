package com.dfim.app.util;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.dfim.app.common.UpnpApp;
import com.union.cellremote.R;

public class LrucacheUtil {
	//开辟80M硬缓存空间
//	 private static final int hardCachedSize = 15*1024*1024;
	 private static final int hardCachedSize = UpnpApp.context.getResources().getInteger(R.integer.hard_cached_size)*1024*1024;
	 //hard cache
	 private static final LruCache<String, Bitmap> sHardBitmapCache = new LruCache<String, Bitmap>(hardCachedSize){
		  @Override
		  public int sizeOf(String key, Bitmap value){
			  return value.getRowBytes() * value.getHeight();
		  }
		  
		  @Override
		  protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue){
			   Log.v("tag", "hard cache is full , push to soft cache");
			   //硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
			   sSoftBitmapCache.put(key, new SoftReference<Bitmap>(oldValue));
		  }
	 };
	 
	 //软引用
	 private static final int SOFT_CACHE_CAPACITY = 200;
	 private final static LinkedHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = 
	  new LinkedHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_CAPACITY, 0.75f, true){		 
		  @Override
		  public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value){
			  return super.put(key, value);
		  }
		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<String, SoftReference<Bitmap>> eldest) {
			// TODO Auto-generated method stub
			   if(size() > SOFT_CACHE_CAPACITY){
				    Log.v("tag", "Soft Reference limit , purge one");
//				    Bitmap bitmap = eldest.getValue().get();
//					if(bitmap!=null&&!bitmap.isRecycled()){
//						bitmap.recycle();
//					}
				    return true;
				   }
				   return false;
		}

	 };
	 //缓存bitmap
	 public boolean putBitmap(String key, Bitmap bitmap){
	  if(bitmap != null){
	   synchronized(sHardBitmapCache){
	    sHardBitmapCache.put(key, bitmap);
	   }
	   return true;
	  }  
	  return false;
	 }
	 //从缓存中获取bitmap
	public Bitmap getBitmap(String key){
	  synchronized(sHardBitmapCache){
	   final Bitmap bitmap = sHardBitmapCache.get(key);
	   if(bitmap != null)
	    return bitmap;
	  }
	  //硬引用缓存区间中读取失败，从软引用缓存区间读取
	  synchronized(sSoftBitmapCache){
	   SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(key);
	   if(bitmapReference != null){
	    final Bitmap bitmap2 = bitmapReference.get();
	    if(bitmap2 != null)
	     return bitmap2;
	    else{
	     Log.v("tag", "soft reference 已经被回收");
	     sSoftBitmapCache.remove(key);
	    }
	   }
	  }
	  return null;
	 }
}
