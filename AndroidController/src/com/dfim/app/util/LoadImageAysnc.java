package com.dfim.app.util;

import java.io.File;
import java.io.IOException;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dfim.app.common.UpnpApp;

public class LoadImageAysnc {
    private Object lock=new Object();
	private static boolean mAllowLoad = true;
	
	private static  boolean firstLoad = true;
	
	private  int mStartLoadLimit = 0;
	
	private int mStopLoadLimit = 0;
	final static String SAVEPATH=mikfile();
	private LrucacheUtil lrucache;
	public LoadImageAysnc() {		
		lrucache = new LrucacheUtil();		
	}
    /**
     * 初始化缓存图片文件夹
     */
     public static  String mikfile(){
      String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/imagescache/";//获得sdcard路径
    	 File filename=new File(path);
    	 //ken 2013.10.21 尽量减少无用代码，保持代码整洁
//   	  if(filename.isDirectory()){
//   	
//   		  
//   	  }else{
//   	  try {
//          filename.mkdir();
//   	} catch (Exception e) {
//   			e.printStackTrace();
//   	}
//     }
    	 
   	 if(!filename.isDirectory()){
   		try {
            filename.mkdir();
     	} catch (Exception e) {
     			e.printStackTrace();
     	}
 	  }
   	  return path;
     }
     /**
      * 从盒子取图片
      * @param imgurl
      * @return
      */
     public Bitmap loadImage( final String imgkey, final String imgurl,final View view){
    		//如果有缓存则使用缓存中的图片
 		Bitmap bitmap = lrucache.getBitmap(imgkey);
 		if(bitmap!=null){
 			return bitmap;
 		}
    	 
    	 final Handler handler=new Handler(){
    		 @Override
 			public void handleMessage(Message msg) {
 				Bitmap bitmap = (Bitmap) msg.obj;
 				//图片加载完成的回调函数，用于更新imageview
 			 	if(bitmap!=null){
 					//imageCallBack.imageLoaded(bitmap);
 					ImageView imageViewTag = (ImageView) view.findViewWithTag(imgkey);
					if (imageViewTag != null) {
						if (bitmap != null) {
							if(bitmap.isRecycled()){
								bitmap=null;
							}else{
						imageViewTag.setBackgroundDrawable(new BitmapDrawable(bitmap));}
					   for(int i=0;i<100;i++){
						   
					   }
					   bitmap=null;
						}
					}
 					
 				}				
 			}
    	 };
    	 
    	//异步图片下载方法
 		QueryTask.executorService.submit(
 				new Runnable() {

					@Override
					public void run() {
						Log.i("download", mAllowLoad+"");
						if(!mAllowLoad){
							Log.i("download", "等待下载。。。。");
					/*	synchronized(lock){
							try {
								lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}*/
						Log.i("download","等待下载。。。。");
						}else{
							Log.i("download","正在下载。。。。");
						String name = imgurl.substring(imgurl.lastIndexOf("/")+1);
						File cachefile = new File(SAVEPATH+name);
//						Bitmap bitmap = null;
						Bitmap bm = null;
						String md5code = "";
						String namemd5code="";
						if(cachefile.exists()){
							try {
								md5code = Md5Util.process(cachefile);		
								namemd5code = name.substring(0, name.lastIndexOf("."));
							} catch (IOException e) {
								e.printStackTrace();
							}
							if(namemd5code.equals(md5code)){
								try{
								bm = BitmapUtil.decodeScaleFile(cachefile.getAbsolutePath(), 150);
								}catch(OutOfMemoryError e){
									Log.i("kk","memoryerror:"+e);
								}
								if(bm!=null){
						 		   	lrucache.putBitmap(imgkey, bm);
						 		    }
										Message msg = handler.obtainMessage(0, bm);
										msg.sendToTarget();	
							}else{
							 getboximages(imgkey, imgurl, handler);
							}
						
					 	
						
					}else{
						getboximages(imgkey, imgurl, handler);
						
					}
						bm=null;
						
					}
					}
 				});
   
    	 
    	 return null;
    	 
     }
     
     /**
      * 从盒子取图片
      * @param imgurl
      * @return
      */
	public Bitmap loadImageForUsbMusic(final String imgkey, final String imgurl, final ImageView usbMusicImageView) {
		// 如果有缓存则使用缓存中的图片
		Bitmap bitmap = lrucache.getBitmap(imgkey);
		if (bitmap != null) {
			return bitmap;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap = (Bitmap) msg.obj;
				// 图片加载完成的回调函数，用于更新imageview
				if (bitmap != null) {
					if (usbMusicImageView != null) {
						if (bitmap != null) {
							if (bitmap.isRecycled()) {
								bitmap = null;
							} else {
								usbMusicImageView.setImageBitmap(bitmap);
							}
//							for (int i = 0; i < 100; i++) {
//
//							}
							bitmap = null;
						}
					}

				}
			}
		};

		// 异步图片下载方法
		QueryTask.executorService.submit(new Runnable() {

			@Override
			public void run() {
				Log.i("download", mAllowLoad + "");
				if (!mAllowLoad) {
					Log.i("download", "等待下载。。。。");
					/*
					 * synchronized(lock){ try { lock.wait(); } catch
					 * (InterruptedException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); } }
					 */
					Log.i("download", "等待下载。。。。");
				} else {
					Log.i("download", "正在下载。。。。");
					String name = imgurl.substring(imgurl.lastIndexOf("/") + 1);
					File cachefile = new File(SAVEPATH + name);
					// Bitmap bitmap = null;
					Bitmap bm = null;
					String md5code = "";
					String namemd5code = "";
					if (cachefile.exists()) {
						try {
							md5code = Md5Util.process(cachefile);
							namemd5code = name.substring(0,
									name.lastIndexOf("."));
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (namemd5code.equals(md5code)) {
							try {
								bm = BitmapUtil.decodeScaleFile(
										cachefile.getAbsolutePath(), 150);
							} catch (OutOfMemoryError e) {
								Log.i("kk", "memoryerror:" + e);
							}
							if (bm != null) {
								lrucache.putBitmap(imgkey, bm);
							}
							Message msg = handler.obtainMessage(0, bm);
							msg.sendToTarget();
						} else {
							getboximages(imgkey, imgurl, handler);
						}

					} else {
						getboximages(imgkey, imgurl, handler);

					}
					bm = null;

				}
			}
		});

		return null;

	}
     
	//异步加载图片的方法（其实可以以任何形式得到图片）
	public Bitmap loadImage(final String key, final String imageurl,final int sc,final boolean isReflection, final ImageCallBack imageCallBack)
	{
		//如果有缓存则使用缓存中的图片
		Bitmap bitmap = lrucache.getBitmap(key);
		if(bitmap!=null){
			return bitmap;
		}
		
		//图片加载完成
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap = (Bitmap) msg.obj;
				//图片加载完成的回调函数，用于更新imageview
				if(bitmap!=null){
					if(!bitmap.isRecycled()){
						imageCallBack.imageLoaded(bitmap);
					}
					
				}				
			}
		};
		
		//异步图片下载方法
		QueryTask.executorService.submit(
				new Runnable() {					
					@Override
					public void run() { 		
						String name = imageurl.substring(imageurl.lastIndexOf("/")+1);
						File cachefile = new File(SAVEPATH+name);
						Bitmap bitmap = null;
						Bitmap bm = null;
						String md5code = "";
						String namemd5code="";
						if(cachefile.exists()){
							try {
								md5code = Md5Util.process(cachefile);		
								namemd5code = name.substring(0, name.lastIndexOf("."));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(namemd5code.equals(md5code)){
								bm = BitmapUtil.decodeScaleFile(cachefile.getAbsolutePath(), sc);
							}else{
								bm = BitmapUtil.loadImageFromUrl(imageurl,sc,SAVEPATH,name);		
							}
						}else{
							//cachefile.delete();
							bm = BitmapUtil.loadImageFromUrl(imageurl,sc,SAVEPATH,name);		
						}
						if(bm!=null){
							if(isReflection){			
								bitmap =ImageUtil.createnewReflectedImage(bm);		
							}else{
								bitmap=bm;
							}
							lrucache.putBitmap(key, bitmap);
							bm=null;	
						}
											
						Message msg = handler.obtainMessage(0, bitmap);
						msg.sendToTarget();						
					}
				});
		return null;
	}
	
	
	//异步加载图片的方法（其实可以以任何形式得到图片）
	public Bitmap loadImage(final String key,final int id,final int sc,final Resources res, final ImageCallBack imageCallBack)
	{
	
		//如果有缓存则使用缓存中的图片
		Bitmap bitmap = lrucache.getBitmap(key);
		if(bitmap!=null){
			return bitmap;
		}
		
		//图片加载完成
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap = (Bitmap) msg.obj;
				//图片加载完成的回调函数，用于更新imageview
				if(bitmap!=null){
					imageCallBack.imageLoaded(bitmap);
				}				
			}
		};
		
		//异步图片下载方法
		QueryTask.executorService.submit(
				new Runnable() {					
					@Override
					public void run() {					
						Bitmap bitmap = BitmapUtil.decodeScaleRes(id, sc, res);
						if(bitmap!=null){
							lrucache.putBitmap(key, bitmap);	
						}
						Message msg = handler.obtainMessage(0, bitmap);
						msg.sendToTarget();	
					}
				});
		return null;
	}
	
	// 异步加载图片的方法无Handler
	public Bitmap loadImageNohandler(final String key, final String imageurl, final int sc, final boolean isReflection, final ImageCallBack imageCallBack) {

		// 如果有缓存则使用缓存中的图片
		Bitmap bitmap = lrucache.getBitmap(key);
		if (bitmap != null && !bitmap.isRecycled()) {
			return bitmap;
		}

//		// 图片加载完成
//		final Handler handler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				Bitmap bitmap = (Bitmap) msg.obj;
//				// 图片加载完成的回调函数，用于更新imageview
//				if (bitmap != null) {
//					imageCallBack.imageLoaded(bitmap);
//				}
//			}
//		};

		// 异步图片下载方法
		QueryTask.executorService.submit(new Runnable() {
			@Override
			public void run() {
				String name = imageurl.substring(imageurl.lastIndexOf("/") + 1);
				File cachefile = new File(SAVEPATH + name);
				Bitmap bitmap = null;
				Bitmap bm = null;
				String md5code = "";
				String namemd5code = "";
				if (cachefile.exists()) {
					try {
						md5code = Md5Util.process(cachefile);
						namemd5code = name.substring(0, name.lastIndexOf("."));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (namemd5code.equals(md5code)) {
						bm = BitmapUtil.decodeScaleFile(cachefile.getAbsolutePath(), sc);
					} else {
						bm = BitmapUtil.loadImageFromUrl(imageurl, sc, SAVEPATH, name);
					}
				} else {
					// cachefile.delete();
					bm = BitmapUtil.loadImageFromUrl(imageurl, sc, SAVEPATH, name);
				}
				if (bm != null) {
					if (isReflection) {
						bitmap = ImageUtil.createnewReflectedImage(bm);
					} else {
						bitmap = bm;
					}
					lrucache.putBitmap(key, bitmap);
					bm = null;
				}

				imageCallBack.imageLoaded(bitmap);
			}
		});
		
		return null;
	}
	
	//回调接口
	public interface ImageCallBack
	{
		public void imageLoaded(Bitmap bitmap);
	}
	
	/**
	 * 去盒子拿图片
	 */
	public void getboximages(final String imgkey, final String imgurl,final Handler handler){
		 ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetImage"));
 		 ai.setInput("Json","{imageurl:\""+imgurl+"\"}");
 		 UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai){
 			@Override
 			public void failure(ActionInvocation arg0, UpnpResponse arg1,
 					String arg2) {
 				Log.i("images", "失败");
 			}

 			@Override
 			public void success(ActionInvocation arg0) {
 			byte[] sycontent =(byte[])arg0.getOutput("Image").getValue();
 			Log.i("images",sycontent+"");
 			Bitmap bitmap=null;
 		    if(sycontent!=null){
 		    	String name = imgurl.substring(imgurl.lastIndexOf("/")+1);
 				File cachefile = new File(SAVEPATH+name);
 				if(cachefile.exists()){
 					bitmap = BitmapUtil.decodeScaleFile(cachefile.getAbsolutePath(), 250);
 				}else{
 					bitmap = BitmapUtil.loadImageFromUrl(imgurl,250,SAVEPATH,name);
 				}
			}
 		    if(bitmap!=null){
 		   	lrucache.putBitmap(imgkey, bitmap);
 		    }
				Message msg = handler.obtainMessage(0, bitmap);
				msg.sendToTarget();						
 		    }
 				
 			
 			//}
 			
 		});
    	 
		
	}
	/**
	 * 加载图片
	 */
	public static void unlock(){
		 LoadImageAysnc.mAllowLoad=true;
		 LoadImageAysnc.firstLoad=true;
	   /* synchronized (lock) {
		     lock.notifyAll();
	   }*/
	}
	/**
	 * 停止加载图片，等待通知
	 */
	 public  static void lock(){
		 LoadImageAysnc.mAllowLoad=false;
		 LoadImageAysnc.firstLoad=false;
	 }
	 /**
	  * 重置
	  */
	  public void restore(){
		  LoadImageAysnc.mAllowLoad = true;
		  LoadImageAysnc.firstLoad = true;
		}
	
}