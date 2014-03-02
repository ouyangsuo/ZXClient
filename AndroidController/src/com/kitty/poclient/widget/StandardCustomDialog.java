package com.kitty.poclient.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.Constant;

public class StandardCustomDialog extends Dialog {

	public StandardCustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public StandardCustomDialog(Context context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private boolean isClearContentHorizotalMargin = false;
		private boolean isClearContentVerticalMargin = false;

		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public StandardCustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final StandardCustomDialog dialog = new StandardCustomDialog(context, R.style.Dialog);
			View layout = inflater.inflate(R.layout.dialog_standard, null);
//			layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			// dialog.addContentView(layout, new LayoutParams(
			// LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.title)).setText(title);
			// set the confirm button
			if (positiveButtonText != null) {
				((TextView) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.positiveButton)).setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_UP) {
								if (v.getTag() != null && "isdown".equals(v.getTag())) {
									// 在控件内离开才触发业务
									positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
								}
								v.setBackgroundColor(Color.TRANSPARENT);
							} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
								// 控件内
								v.setTag("isdown");
								v.setBackgroundColor(Color.parseColor(context.getString(R.color.tips_orange)));
							} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
								if (event.getX() < v.getX() || event.getX() > (v.getX() + v.getWidth()) || event.getY() < v.getY() || event.getY() > (v.getY() + v.getHeight())) {
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
				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
				layout.findViewById(R.id.txv_dialog_common_diliver).setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((TextView) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((TextView) layout.findViewById(R.id.negativeButton)).setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_UP) {
								if (v.getTag() != null && "isIn".equals(v.getTag())) {
									// 在控件内离开才触发业务
									negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
								}
								v.setBackgroundColor(Color.TRANSPARENT);
							} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
								// 控件内
								v.setTag("isIn");
								v.setBackgroundColor(Color.parseColor(context.getString(R.color.tips_orange)));
							} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
								if (event.getX() < 0 || event.getX() > (0 + v.getWidth()) || event.getY() < v.getY() || event.getY() > (v.getY() + v.getHeight())) {
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
				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
			}
			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.message)).setText(message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				LinearLayout contentLayout = ((LinearLayout) layout.findViewById(R.id.content));

				// 是否使用默认的margin值
				if (isClearContentHorizotalMargin && isClearContentVerticalMargin) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 0, 0, 0);
					contentLayout.setLayoutParams(lp);
				} else if (isClearContentHorizotalMargin) {
					// 内容域水平方向不使用默认的margin值
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 24, 0, 25);
					contentLayout.setLayoutParams(lp);
				} else if (isClearContentVerticalMargin) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp.setMargins(38, 0, 38, 0);
					contentLayout.setLayoutParams(lp);
				}

				contentLayout.removeAllViews();
				contentLayout.addView(contentView, new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
			}

			dialog.setContentView(layout);
			return dialog;
		}

		/**
		 * 清除内容域水平方向margin值
		 */
		public void clearContentHorizotalMargin() {
			this.isClearContentHorizotalMargin = true;
		}

		public void clearContentVerticalMargin() {
			this.isClearContentVerticalMargin = true;
		}

	}

}