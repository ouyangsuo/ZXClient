package com.kitty.poclient.widget;

//import android.R;
import com.kitty.poclient.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyLetterListView extends View {

	private final String TAG = "MyLetterListView";

	Context context;
	OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	String[] b = { "*", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };
	int choose = 1;
	Paint paint = new Paint();
	boolean showBkg = false;

	public MyLetterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public MyLetterListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public MyLetterListView(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 绘制默认的背景色
		if (showBkg) {
			canvas.drawColor(context.getResources().getColor(R.color.white));
		}

		// 拿取控件尺寸并计算出单个字母高度
		int height = getHeight();//
		int width = getWidth();
		int singleHeight = height / b.length;

		// 逐一绘制选中的和未选中的字母
		for (int i = 0; i < b.length; i++) {

			paint.setColor(Color.BLACK);
			paint.setTypeface(Typeface.DEFAULT_BOLD);// 设置字体
			paint.setTextSize(context.getResources().getInteger(R.integer.letter_lv_textsize));
			paint.setAntiAlias(true);

			if (i == choose) {
				paint.setColor(context.getResources().getColor(R.color.letter_chosen));
				paint.setFakeBoldText(true);
			}

			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;// 留出顶部
			canvas.drawText(b[i], xPos, yPos, paint);

			paint.reset();
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.e(TAG, "dispatchTouchEvent");
		onTouch(event);
		return true;
	}

	public void onTouch(MotionEvent event) {

		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			showBkg = true;
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c]);
					choose = c;
					invalidate();
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c && listener != null) {
				if (c > 0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c]);
					choose = c;
					invalidate();
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			showBkg = false;
			// choose = -1;
			int _c = c;
			if (_c > 27) {
				_c = 27;
			} else if (_c < 1) {
				_c = 1;
			}
			choose = _c;
			invalidate();
			break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}

	public void setChoose(int choose) {
		this.choose = choose;
	}

}
