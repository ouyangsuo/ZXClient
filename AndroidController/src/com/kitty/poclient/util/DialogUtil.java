package com.kitty.poclient.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

import com.kitty.poclient.widget.StandardCustomDialog;

public class DialogUtil {

	private Context context;
	private StandardCustomDialog dialog;

	public DialogUtil(Context context) {
		this.context = context;
	}

	/**
	 * 封存类：使用BUILDER生成器模式生成Dialog
	 * */
	public void showDialog(String title, String msg, String positiveBtnText, String negativeBtnText, final Handler handler, final int whatPositive, final int whatNegative) {
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);

		builder.setPositiveButton(positiveBtnText, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				handler.sendEmptyMessage(whatPositive);
			}
		});

		builder.setNegativeButton(negativeBtnText, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				handler.sendEmptyMessage(whatNegative);
			}
		});

		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	
	/**
	 * 封存类：使用BUILDER生成器模式生成Dialog
	 * */
	public void showDialog(int title, int msg, int positiveBtnText, int negativeBtnText, final Handler handler, final int whatPositive, final int whatNegative) {
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		
		builder.setPositiveButton(positiveBtnText, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if(handler!=null && whatPositive!=-1){
					handler.sendEmptyMessage(whatPositive);
				}				
			}
		});
		
		if(negativeBtnText!=-1){
			builder.setNegativeButton(negativeBtnText, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if(handler!=null && whatNegative!=-1){
						handler.sendEmptyMessage(whatNegative);
					}				
				}
			});
		}
		
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

}
