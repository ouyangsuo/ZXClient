package com.dfim.app.common;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.union.cellremote.R;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 实现自定义的弹出通知视图（Notification）。对于不同类型的通知，会有不同的颜色，
 * 比如info是蓝色，alert是红色，confirm是绿色等等。 可以在通知中加入图片。支持在屏幕顶部或者屏幕中间弹出通知。
 * @author fangyishuai@dfim.com.cn
 * @version 1.0
 * @since 1.3.38
 * @created 2013-12-31
 */
public class CroutonHelper {
	
	public static final int TYPE_INFO_DEFAULT = 0; //无图标
	
	public static final int TYPE_INFO_LOADING = 1;
	public static final int TYPE_INFO_ADD_SUCCESS = 2;
	
	public static final int DEFAULT_DURATION = 1500;
	
	/**
	 * Show Crouton on Activity with specified text, style & configuration. 
	 * @param activity 		The {@link Activity} that the {@link Crouton} should be attached to.
	 * @param croutonText 	The text you want to display.
	 * @param croutonStyle  The style that this {@link Crouton} should be created with.
	 * @param configuration The {@link Configuration} for this {@link Crouton}.
	 * @version 1.0
	 */
	public static void showCrouton(final Activity activity,
			final String croutonText, final Style croutonStyle,
			final Configuration configuration) {

		final Crouton crouton;
		crouton = Crouton.makeText(activity, croutonText, croutonStyle);
		crouton.setConfiguration(configuration).show();
	}
	
	public static void showCrouton(Activity activity, String croutonText, int croutonDuration, int croutonViewColor, int croutonImageId){
		
		Configuration croutonConfiguration = new Configuration.Builder().setDuration(croutonDuration).build();
		
		View view = activity.getLayoutInflater().inflate(R.layout.crouton_view, null);
		view.setBackgroundColor(croutonViewColor);
		
		TextView textView = (TextView) view.findViewById(R.id.crouton_text);
		textView.setText(croutonText);
		
		ImageView image = (ImageView) view.findViewById(R.id.crouton_image);
		if(croutonImageId == 0){
			image.setVisibility(View.INVISIBLE);
		}else{
			image.setImageResource(croutonImageId);
		}
		
		final Crouton crouton;
		crouton = Crouton.make(activity, view);
		crouton.setConfiguration(croutonConfiguration).show();
	}
	
	public static void showAlertCrouton(Activity activity, String croutonText, int croutonDuration) {
		
		int croutonViewColor = activity.getResources().getColor(R.color.crouton_alert_color);
		int croutonImageId = R.drawable.crouton_alert;
		
		showCrouton(activity, croutonText, croutonDuration, croutonViewColor, croutonImageId);
	}
	
	public static void showAlertCrouton(Activity activity, String croutonText){
		int croutonDuration = DEFAULT_DURATION;
		showAlertCrouton(activity, croutonText, croutonDuration);
	}
	
	/* Same as alert*/
//	public static void showErrorCrouton(Activity activity, String croutonText){
//		int croutonDuration = DEFAULT_DURATION;
//		showAlertCrouton(activity, croutonText, croutonDuration);
//	}
	
	private static void showInfoCrouton(Activity activity, String croutonText, int croutonDuration, int infoType){

		int croutonViewColor = activity.getResources().getColor(R.color.crouton_info_color);
		
		int croutonImageId;
		switch (infoType) {
			case TYPE_INFO_LOADING:
				croutonImageId = R.drawable.crouton_loading;
				break;
				
			case TYPE_INFO_ADD_SUCCESS:
				croutonImageId = R.drawable.crouton_add_success;
				break;
	
			default:
				//TODO 待设计默认info图标
				croutonImageId = 0;
				break;
		}
		
		showCrouton(activity, croutonText, croutonDuration, croutonViewColor, croutonImageId);
	}
	
	/* 无图标info */
	public static void showInfoCrouton(Activity activity, String croutonText){
		int croutonDuration = DEFAULT_DURATION;
		int infoType = TYPE_INFO_DEFAULT;
		showInfoCrouton(activity, croutonText, croutonDuration, infoType);
	}
	
	/* show info Crouton on MainActivity */
	public static void showSystemInfoCrouton(String croutonText){
		int croutonDuration = DEFAULT_DURATION;
		int infoType = TYPE_INFO_DEFAULT;
		showInfoCrouton(UpnpApp.mainActivity, croutonText, croutonDuration, infoType);
	}
	/* show alert Crouton on MainActivity */
	public static void showSystemAlertCrouton(String croutonText){
		showAlertCrouton(UpnpApp.mainActivity, croutonText);
	}
	
}
