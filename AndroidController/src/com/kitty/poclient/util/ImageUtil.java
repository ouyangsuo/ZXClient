package com.kitty.poclient.util; 

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
/**
 * @ClassName:ImageUtil
 * @Description:图片工具类
 * @author 张纯鹏
 * @date 2012-7-30 下午5:27:40
 */
public class ImageUtil { 
	/**
	 * @Title: zoomBitmap
	 * @Description: 放大缩小图片 
	 * @param @param bitmap
	 * @param @param w
	 * @param @param h
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap,int w,int h){ 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 
		Matrix matrix = new Matrix(); 
		float scaleWidht = ((float)w / width); 
		float scaleHeight = ((float)h / height); 
		matrix.postScale(scaleWidht, scaleHeight); 
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
		return newbmp; 
	} 
	/**
	 * @Title: drawableToBitmap
	 * @Description: 将Drawable转化为Bitmap 
	 * @param @param drawable
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap drawableToBitmap(Drawable drawable){ 
		int width = drawable.getIntrinsicWidth(); 
		int height = drawable.getIntrinsicHeight(); 
		Bitmap bitmap = Bitmap.createBitmap(width, height, 
		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565); 
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0,0,width,height); 
		drawable.draw(canvas); 
		return bitmap; 
	} 

    /**
     * @Title: getRoundedCornerBitmap
     * @Description: 获得圆角图片的方法 
     * @param @param bitmap
     * @param @param roundPx
     * @param @return
     * @return Bitmap    
     * @throws
     */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){ 	
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888); 
		Canvas canvas = new Canvas(output); 
		
		final int color = 0xff424242; 
		final Paint paint = new Paint(); 
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
		final RectF rectF = new RectF(rect); 
		
		paint.setAntiAlias(true); 
		canvas.drawARGB(0, 0, 0, 0); 
		paint.setColor(color); 
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
		
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
		canvas.drawBitmap(bitmap, rect, rect, paint); 
		 
		return output; 
	} 
	/**
	 * @Title: createReflectionImageWithOrigin
	 * @Description: 获得带倒影的图片方法 
	 * @param @param bitmap
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap,float reflectscale,int imagepadding){ 
		final int reflectionGap = imagepadding; 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 		
		Matrix matrix = new Matrix(); 
		matrix.preScale(1, -reflectscale); 		
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false); 		
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height +(int)(height*reflectscale)+reflectionGap), Config.ARGB_8888); 		
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint deafalutPaint = new Paint(); 
		canvas.drawRect(0, height,width,height + reflectionGap, deafalutPaint); 		
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null); 		
		Paint rpaint = new Paint(); 
		LinearGradient rshader = new LinearGradient(0, bitmap.getHeight(), 0, bitmap.getHeight()+reflectionGap, 0x00ffffff, 0x00ffffff, TileMode.MIRROR); 
		rpaint.setShader(rshader); 
		// Set the Transfer mode to be porter duff and destination in 
		rpaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		// Draw a rectangle using the paint with our linear gradient 
		canvas.drawRect(0, height, width, height+reflectionGap, rpaint); 								
		Paint paint = new Paint(); 
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight()+reflectionGap, 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.MIRROR); 
		paint.setShader(shader); 
		// Set the Transfer mode to be porter duff and destination in 
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		// Draw a rectangle using the paint with our linear gradient 
		canvas.drawRect(0, height+reflectionGap, width, bitmapWithReflection.getHeight() + reflectionGap, paint); 	
		if(reflectionImage!=null&&!reflectionImage.isRecycled()){
			reflectionImage.recycle();
		}
		return bitmapWithReflection; 
	} 

	
	
	
	public static Bitmap createOtherReflectionImage(Bitmap bitmap,float reflectscale,int imagepadding){ 
		final int reflectionGap = imagepadding; 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 		
		Matrix matrix = new Matrix(); 
        float[] src = new float[]{0,0,  width,0,  0,height,  width,height};
        float[] dst = new float[]{width*0.02f,height*0.4f,width*0.98f,height*0.4f, 0,0,  width,0 };
        matrix.setPolyToPoly(src, 0, dst,0, 4);
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height +(int)(height*0.2f)+reflectionGap), Config.ARGB_8888); 		
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint deafalutPaint = new Paint(); 
		canvas.drawRect(0, height,width,height + reflectionGap, deafalutPaint); 		
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null); 		
		Paint rpaint = new Paint(); 
		LinearGradient rshader = new LinearGradient(0, bitmap.getHeight(), 0, bitmap.getHeight()+reflectionGap, 0x00ffffff, 0x00ffffff, TileMode.MIRROR); 
		rpaint.setShader(rshader); 
		// Set the Transfer mode to be porter duff and destination in 
		rpaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		// Draw a rectangle using the paint with our linear gradient 
		canvas.drawRect(0, height, width, height+reflectionGap, rpaint); 								
		Paint paint = new Paint(); 
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight()+reflectionGap, 0, bitmapWithReflection.getHeight() + reflectionGap, 0x20ffffff, 0x00ffffff, TileMode.MIRROR); 
		paint.setShader(shader); 
		// Set the Transfer mode to be porter duff and destination in 
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		// Draw a rectangle using the paint with our linear gradient 
		canvas.drawRect(0, height+reflectionGap, width, bitmapWithReflection.getHeight() + reflectionGap, paint); 	
		if(reflectionImage!=null&&!reflectionImage.isRecycled()){
			reflectionImage.recycle();
		}
		return bitmapWithReflection; 
	}
	
	
	public static Bitmap createnewReflectedImage(Bitmap originalBitmap) {
		// 图片与倒影间隔距离
		final int reflectionGap = 4;
		
		// 图片的宽度
		int width = originalBitmap.getWidth();
		// 图片的高度
		int height = originalBitmap.getHeight();
		
		Matrix matrix = new Matrix();
		// 图片缩放，x轴变为原来的1倍，y轴为-1倍,实现图片的反转
		matrix.preScale(1, -1);
		// 创建反转后的图片Bitmap对象，图片高是原图的一半。
		Bitmap reflectionBitmap = Bitmap.createBitmap(originalBitmap, 0,
				(height*3) /4, width, height / 4, matrix, false);
		// 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍。
		Bitmap withReflectionBitmap = Bitmap.createBitmap(width, (height
				+ height /4 + reflectionGap), Config.ARGB_8888);

		// 构造函数传入Bitmap对象，为了在图片上画图
		Canvas canvas = new Canvas(withReflectionBitmap);
		// 画原始图片
		canvas.drawBitmap(originalBitmap, 0, 0, null);

		// 画间隔矩形
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);

		// 画倒影图片
		canvas.drawBitmap(reflectionBitmap, 0, height + reflectionGap, null);

		// 实现倒影效果
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, originalBitmap.getHeight(), 
				0, withReflectionBitmap.getHeight(), 0x70ffffff, 0x00ffffff,
				TileMode.MIRROR);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// 覆盖效果
		canvas.drawRect(0, height, width, withReflectionBitmap.getHeight(), paint);

		return withReflectionBitmap;
	}
	
	public static Bitmap createshadowImage(Bitmap bitmap,float reflectscale,int imagepadding){ 
		final int reflectionGap = imagepadding; 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 				
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height +(int)(height*reflectscale)+reflectionGap), Config.ARGB_8888); 		
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint deafalutPaint = new Paint(); 
		canvas.drawRect(0, height,width,height + reflectionGap, deafalutPaint); 				
		Paint rpaint = new Paint(); 
		rpaint.setColor(Color.BLACK);
		Path path=new Path();
		   path.moveTo(0,height);
		   path.lineTo((int)(width*0.05f),(height +(int)(height*reflectscale)+reflectionGap));
		   path.lineTo(width-(int)(width*0.05f),(height +(int)(height*reflectscale)+reflectionGap));
		   path.lineTo(width,height);
		   path.close();
		canvas.drawPath(path,rpaint); 								
		Paint paint = new Paint(); 
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight()+reflectionGap, 0, bitmapWithReflection.getHeight() + reflectionGap, 0x50ffffff, 0x00ffffff, TileMode.MIRROR); 
		paint.setShader(shader); 
		// Set the Transfer mode to be porter duff and destination in 
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		// Draw a rectangle using the paint with our linear gradient 
		canvas.drawRect(0, height+reflectionGap, width, bitmapWithReflection.getHeight() + reflectionGap, paint); 	
		return bitmapWithReflection; 
	}
	
	
	
	/**
	 * @Title: createReflectionImage
	 * @Description: 创建倒影图片
	 * @param @param bitmap
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap createReflectionImage(Bitmap bitmap,float reflectscale){ 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 		
		Matrix matrix = new Matrix(); 
		matrix.preScale(1, -reflectscale); 		
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false); 		
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, ((int)(height*reflectscale)), Config.ARGB_8888); 		
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(reflectionImage, 0, 0, null); 
		Paint paint = new Paint(); 
		LinearGradient shader = new LinearGradient(0, 0, 0, bitmapWithReflection.getHeight(), 0xc0ffffff, 0x00ffffff, TileMode.MIRROR); 
		paint.setShader(shader); 
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint); 		
		if(reflectionImage!=null&&!reflectionImage.isRecycled()){
			reflectionImage.recycle();
		}
		return bitmapWithReflection; 
	} 
	
	/**
	 * @Title: createReflectiveimage
	 * @Description: 绘制半弧形的反光效果
	 * @param @param bitmap
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap createReflectiveimage(Bitmap bitmap){	
		int height= bitmap.getHeight();
		int width = bitmap.getWidth();
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAlpha(15);
		int w = width/2;
		int h = height/3;
		final Rect rect = new Rect(-w, -2*h, 3*w, h); 
		final RectF rectF = new RectF(rect); 
		canvas.drawOval(rectF, paint);
		return bitmapWithReflection;
	}
	
	/**
	 * @Title: createReflectiveimage
	 * @Description: 绘制半弧形的反光效果
	 * @param @param bitmap
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap createSharpeimage(Bitmap bitmap){	
		int height= bitmap.getHeight();
		int width = bitmap.getWidth();
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height+(int)(height*0.2f), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAlpha(15);
		int w = width/2;
		int h = height/3;
		final Rect rect = new Rect(-w, -2*h, 3*w, h); 
		final RectF rectF = new RectF(rect); 
		canvas.drawOval(rectF, paint);
		return bitmapWithReflection;
	}
	
	
	/**
	 * @Title: createTixingimage
	 * @Description: 绘制梯形的反光效果
	 * @param @param bitmap
	 * @param @return
	 * @return Bitmap    
	 * @throws
	 */
	public static Bitmap createTixingimage(Bitmap bitmap,float leftlen,float rightlen){	
		int height= bitmap.getHeight();
		int width = bitmap.getWidth();
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection); 
		canvas.drawBitmap(bitmap, 0, 0, null); 
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAlpha(15);		
		Path path=new Path();
		   path.moveTo(0,0);
		   path.lineTo(0,height*leftlen);
		   path.lineTo(width,height*rightlen);
		   path.lineTo(width,0);
		   path.close();
		canvas.drawPath(path,paint);
		return bitmapWithReflection;
	}
	
	
	public static Bitmap decodeScaleFile(String path,int height){
	      BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        // 获取这个图片的宽和高
	        Bitmap bitmap = BitmapFactory.decodeFile(path, options); //此时返回bm为空
	        options.inJustDecodeBounds = false;
	        options.inPurgeable = true;    
	        options.inInputShareable = true;
	        //计算缩放比
	        int be = (int)(options.outHeight / (float)height);
	        if (be <= 0)
	            be = 1;
	        options.inSampleSize = be;
	        //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
	        bitmap=BitmapFactory.decodeFile(path,options);
	        return bitmap;
	}

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
    public static Bitmap loadImageFromUrl(String url, int sc)
    {

        URL m;
        InputStream i = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream out = null;

        if (url == null)
            return null;
        try
        {
            m = new URL(url);
            i = (InputStream) m.getContent();
            bis = new BufferedInputStream(i, 1024 * 4);
            out = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024]; 
            while ((len = bis.read(buffer)) != -1)
            {
                out.write(buffer, 0, len);
            }
            out.close();
            bis.close();
        } catch (MalformedURLException e1)
        {
            e1.printStackTrace();
            return null;
        } catch (IOException e)
        {
            e.printStackTrace();
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
        if (be <= 0)
        {
            be = 1;
        } else if (be > 3)
        {
            be = 3;
        }
        options.inSampleSize = be;
        Bitmap bmp =null;
        try
        {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options); //返回缩略图
        } catch (OutOfMemoryError e)
        {
            // TODO: handle exception        
            System.gc();
            bmp =null;
        }
        return bmp;
    }
} 
