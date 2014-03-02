package com.kitty.poclient.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kitty.poclient.R;

public class CustomDialog extends Dialog {
	public static View layout;


	public static TextView tt;
	public static AnimationDrawable animationDrawable;// 存储动画句柄
	public static View searchdeviceanim;// 存储动画承载句柄
	public static View devicelist;// 存储盒子列表
    public static View searchtext;// 储存搜索视图信息
    public static View nofinddevice;//储存没有发现设备视图信息
	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}

	public static class Builder {

		private Context context;
		public   String title;
		private String message;
		private String positiveButtonText;
		private String devicelinkText;
		private String stopsearchButtonText;
		private String reloadlinkText;

		private View contentView;
		private boolean isClearContentHorizotalMargin = false;
		private boolean isClearContentVerticalMargin = false;

		private DialogInterface.OnClickListener positiveButtonClickListener,
				devicelinkButtonClickListener, stopsearchButtonClickListener,
				reloadlinkButtonclickListener;

		// stopsearchButtonClickListener
		public Builder(Context context) {
			this.context = context;
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setDeviceLinkButton(int deviceLinkButtonText,
				DialogInterface.OnClickListener listener) {
			this.devicelinkText = (String) context
					.getText(deviceLinkButtonText);
			this.devicelinkButtonClickListener = listener;
			return this;
		}

		public Builder setDeviceLinkButton(String deviceLinkButtonText,
				DialogInterface.OnClickListener listener) {
			this.devicelinkText = deviceLinkButtonText;
			this.devicelinkButtonClickListener = listener;
			return this;
		}

		public Builder setStopSearchButton(String stopSearchButtonText,
				DialogInterface.OnClickListener listener) {
			this.stopsearchButtonText = stopSearchButtonText;
			this.stopsearchButtonClickListener = listener;
			return this;
		}

		public Builder setStopSearchButton(int stopSearchButtonText,
				DialogInterface.OnClickListener listener) {
			this.stopsearchButtonText = (String) context
					.getText(stopSearchButtonText);
			this.stopsearchButtonClickListener = listener;
			return this;

		}

		public Builder setReloadLinkButton(int reloadLinkButtonText,
				DialogInterface.OnClickListener listener) {
			this.reloadlinkText = (String) context
					.getText(reloadLinkButtonText);
			this.stopsearchButtonClickListener = listener;
			return this;
		}
		public Builder setReloadLinkButton(String reloadLinkButtonText,
				DialogInterface.OnClickListener listener) {
			this.reloadlinkText =reloadLinkButtonText;
			this.reloadlinkButtonclickListener = listener;
			return this;
		}

		public CustomDialog create(int size) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context,
					R.style.Dialog);
			if (layout == null) {
				layout = inflater.inflate(R.layout.dialog_common, null);
			}
			// dialog.addContentView(layout, new LayoutParams(
			// LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.title)).setText(title);
			// set the confirm button
			if (positiveButtonText != null) {
				((TextView) layout.findViewById(R.id.positiveButton))
						.setText(positiveButtonText);

				if (positiveButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.positiveButton))
							.setOnTouchListener(new OnTouchListener() {
								@Override
								public boolean onTouch(View v, MotionEvent event) {
									if (event.getAction() == MotionEvent.ACTION_UP) {
										if (v.getTag() != null
												&& "isdown".equals(v.getTag())) {
											// 在控件内离开才触发业务
											positiveButtonClickListener
													.onClick(
															dialog,
															DialogInterface.BUTTON_POSITIVE);
										}
										v.setBackgroundColor(Color.TRANSPARENT);
									} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
										// 控件内
										v.setTag("isdown");
										v.setBackgroundColor(Color.parseColor(context
												.getString(R.color.tips_orange)));
									} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
										if (event.getX() < v.getX()
												|| event.getX() > (v.getX() + v
														.getWidth())
												|| event.getY() < v.getY()
												|| event.getY() > (v.getY() + v
														.getHeight())) {
											// 已离开控件
											v.setTag("isleft");
										} else {
											// 又回到控件
											v.setTag("isdown");
										}
									}

									return false;
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(
						View.GONE);
				// layout.findViewById(R.id.txv_dialog_common_diliver).setVisibility(View.GONE);
			}
			// set the cancel button
			if (devicelinkText != null) {
				tt = ((TextView) layout.findViewById(R.id.device_link));
				tt.setText(devicelinkText);
				if (devicelinkButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.device_link))
							.setOnTouchListener(new OnTouchListener() {
								@Override
								public boolean onTouch(View v, MotionEvent event) {
									if (event.getAction() == MotionEvent.ACTION_UP) {
										if (v.getTag() != null
												&& "isIn".equals(v.getTag())) {
											// 在控件内离开才触发业务
											devicelinkButtonClickListener
													.onClick(
															dialog,
															DialogInterface.BUTTON_POSITIVE);
										}
										v.setBackgroundColor(Color.TRANSPARENT);
									} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
										// 控件内
										v.setTag("isIn");
										v.setBackgroundColor(Color.parseColor(context
												.getString(R.color.tips_orange)));
									} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
										if (event.getX() < 0
												|| event.getX() > (0 + v
														.getWidth())
												|| event.getY() < v.getY()
												|| event.getY() > (v.getY() + v
														.getHeight())) {
											// 已离开控件
											v.setTag("isleft");
										} else {
											// 又回到控件
											v.setTag("isIn");
										}
									}

									return false;
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.device_link).setVisibility(View.GONE);
			}

			if (stopsearchButtonText != null) {
				TextView stopsearchview = ((TextView) layout
						.findViewById(R.id.stop_search));
				stopsearchview.setText(stopsearchButtonText);
				if (stopsearchButtonClickListener != null) {
					stopsearchview.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_UP) {
								// 在控件内离开才触发业务
								if (v.getTag() != null
										&& "isIn".equals(v.getTag())) {
									stopsearchButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
								v.setBackgroundColor(Color.TRANSPARENT);

							} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
								// 控件内
								v.setTag("isIn");
								v.setBackgroundColor(Color.parseColor(context
										.getString(R.color.tips_orange)));
							} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
								if (event.getX() < 0
										|| event.getX() > (0 + v.getWidth())
										|| event.getY() < v.getY()
										|| event.getY() > (v.getY() + v
												.getHeight())) {
									// 已离开控件
									v.setTag("isleft");
								} else {
									// 又回到控件
									v.setTag("isIn");
								}
							}
							return false;
						}

					});

				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.device_link).setVisibility(View.GONE);
			}

			if (reloadlinkText != null) {
				TextView stopsearchview = ((TextView) layout
						.findViewById(R.id.reload_link));
				stopsearchview.setText(reloadlinkText);
				if (reloadlinkButtonclickListener != null) {
					stopsearchview.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_UP) {
								// 在控件内离开才触发业务
								if (v.getTag() != null
										&& "isIn".equals(v.getTag())) {
									reloadlinkButtonclickListener.onClick(
											dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
								v.setBackgroundColor(Color.TRANSPARENT);

							} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
								// 控件内
								v.setTag("isIn");
								v.setBackgroundColor(Color.parseColor(context
										.getString(R.color.tips_orange)));
							} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
								if (event.getX() < 0
										|| event.getX() > (0 + v.getWidth())
										|| event.getY() < v.getY()
										|| event.getY() > (v.getY() + v
												.getHeight())) {
									// 已离开控件
									v.setTag("isleft");
								} else {
									// 又回到控件
									v.setTag("isIn");
								}
							}
							return false;
						}

					});

				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.reload_link).setVisibility(View.GONE);
			}
			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.message)).setText(message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				LinearLayout contentLayout = ((LinearLayout) layout
						.findViewById(R.id.content));
				// 是否使用默认的margin值
				if (isClearContentHorizotalMargin
						&& isClearContentVerticalMargin) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 0, 0, 0);
					contentLayout.setLayoutParams(lp);
				} else if (isClearContentHorizotalMargin) {
					// 内容域水平方向不使用默认的margin值
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 24, 0, 25);
					contentLayout.setLayoutParams(lp);
				} else if (isClearContentVerticalMargin) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(38, 0, 38, 0);
					contentLayout.setLayoutParams(lp);
				}

				contentLayout.removeAllViews();
				contentLayout
						.addView(
								contentView,
								new LinearLayout.LayoutParams(
										android.widget.LinearLayout.LayoutParams.FILL_PARENT,
										android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			dialog.setContentView(layout);
			return dialog;
		}
	

		public void clearContentHorizotalMargin() {
			this.isClearContentHorizotalMargin = true;
		}

		public void clearContentVerticalMargin() {
			this.isClearContentVerticalMargin = true;
		}

		
	}
    
}