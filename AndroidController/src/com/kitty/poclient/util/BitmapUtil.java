package com.kitty.poclient.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;

import com.kitty.poclient.common.Constant;

public class BitmapUtil {
	public static LoadImageAysnc loadImageAysnc = new LoadImageAysnc();

	public static Bitmap decodeScaleRes(int id, int height, Resources res) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		BitmapFactory.decodeResource(res, id, options); // 此时返回bm为空
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// 计算缩放比
		int be = (int) (options.outHeight / (float) height);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
		// 此时返回bm为空

		return BitmapFactory.decodeResource(res, id, options);
	}

	/*
	 * public static Bitmap decodeScaleFile(String path,int height){
	 * BitmapFactory.Options options = new BitmapFactory.Options();
	 * options.inJustDecodeBounds = true; // 获取这个图片的宽和高 Bitmap bitmap =
	 * BitmapFactory.decodeFile(path, options); //此时返回bm为空
	 * options.inJustDecodeBounds = false; options.inPurgeable = true;
	 * options.inInputShareable = true; //计算缩放比 int be = (int)(options.outHeight
	 * / (float)height); if (be <= 0) be = 1; options.inSampleSize = be;
	 * //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦 try{
	 * bitmap=BitmapFactory.decodeFile(path,options); }catch (OutOfMemoryError
	 * e) { e.printStackTrace(); } return bitmap; }
	 */

	public static Bitmap decodeScaleFile(String path, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		BitmapFactory.decodeFile(path, options); // 此时返回bm为空
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// 计算缩放比
		System.out.println("options.outHeight=" + options.outHeight);
		int be = (int) (options.outHeight / (float) height);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
		// try{
		// bitmap=BitmapFactory.decodeFile(path,options);
		// }catch (OutOfMemoryError e) {
		// e.printStackTrace();
		// }
		return BitmapFactory.decodeFile(path, options);
	}

	public static Bitmap decodeScaleScream(InputStream is, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		BitmapFactory.decodeStream(is, null, options); // 此时返回bm为空
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// 计算缩放比
		int be = (int) (options.outHeight / (float) height);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
		// try{
		// bitmap=BitmapFactory.decodeStream(is,null,options);
		// }catch (OutOfMemoryError e) {
		// e.printStackTrace();
		// }finally{
		// try {
		// is.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		return BitmapFactory.decodeStream(is, null, options);
	}

	/**
	 * 获取网落图片资源
	 * 
	 * @param url
	 * @return
	 */
	/**
	 * @param url
	 *            图片的url
	 * @param sc
	 *            ，显示的像素大小
	 * @return 返回指定RUL的缩略图
	 * 
	 * @author jevan 2012-7-3
	 * 
	 */
	public static Bitmap loadImageFromUrl(String url, int sc, String path, String filename) {
		URL m;
		InputStream i = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream out = null;
		HttpURLConnection conn = null;

		if (url == null)
			return null;
		try {
			File imagesdir = new File(path);
			if (!imagesdir.exists()) {
				imagesdir.mkdirs();
				imagesdir = null;
			}

			m = new URL(url);
			conn = (HttpURLConnection) m.openConnection();
			conn.connect();
			i = conn.getInputStream();

			bis = new BufferedInputStream(i, 1024 * 500);
			out = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024 * 500];
			while ((len = bis.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (i != null) {
					i.close();
				}
				if (conn != null) {
					conn.disconnect();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (out == null)
			return null;
		byte[] data = out.toByteArray();

		File downloadfile = new File(path + filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(downloadfile);
			fos.write(data, 0, data.length);
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		int be = (int) (options.outHeight / (float) sc);
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// Bitmap bmp =null;
		// try
		// {
		// bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		// //返回缩略图
		// } catch (OutOfMemoryError e)
		// {
		// // TODO: handle exception
		// System.gc();
		// bmp =null;
		// }
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}
	
	/*由网络地址拿取bitmap对象*/
	public static Bitmap loadImageFromUrl(String url, int sc) {
		URL m;
		InputStream i = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream out = null;
		HttpURLConnection conn = null;
		
		if (url == null)
			return null;
		
		try {		
			m = new URL(url);
			conn = (HttpURLConnection) m.openConnection();
			conn.connect();
			i = conn.getInputStream();
			
			bis = new BufferedInputStream(i, 1024 * 500);
			out = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024 * 500];
			while ((len = bis.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (i != null) {
					i.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (out == null)
			return null;
		byte[] data = out.toByteArray();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		int be = (int) (options.outHeight / (float) sc);
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;

		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public static Bitmap Bytes2Bimap(byte[] b, int size) {
		if (b.length != 0) {
			Options options = new Options();
			options.inSampleSize = size;
			return BitmapFactory.decodeByteArray(b, 0, b.length, options);
		} else {
			return null;
		}
	}

	public static Bitmap getBitmap(String path, int height) {
		Bitmap bitmap = new LrucacheUtil().getBitmap(path);
		if (bitmap != null) {
			return bitmap;
		}
		return decodeScaleFile(path, height);
	}

	public Bitmap processBigBitmap(Bitmap bitmap, int byteCount, Bitmap defaultBitmap) {
		if (bitmap.getByteCount() >= byteCount) {
			byte[] bmBytes = Bitmap2Bytes(bitmap);
			bitmap = Bytes2Bimap(bmBytes, 2);

			// 如此仁至义尽了
			if (bitmap.getByteCount() > byteCount) {
				bitmap = defaultBitmap;
			}
		}
		return bitmap;
	}

	public static Bitmap cutBitmap(Bitmap bmp) {	
		return Bitmap.createBitmap(bmp, 0, bmp.getHeight()/4, bmp.getWidth(), bmp.getHeight()/2);
	}

	public static BitmapDrawable createBlurryBitmapDrawable(String imgUrl) {
		Bitmap bmp = null;
		try {
			bmp = loadImageFromUrl(imgUrl, Constant.READY_TO_BLUR_BITMAP_HEIGHT);
			bmp = cutBitmap(bmp);
		} catch (Exception e) {
			bmp = Constant.albumCover;
			e.printStackTrace();
		}
		
		if(!Constant.albumCover.equals(bmp)){
			BitmapDrawable bd = new BitmapDrawable(bmp);
			return bd;
		}else{
			return null;
		}

	}

}