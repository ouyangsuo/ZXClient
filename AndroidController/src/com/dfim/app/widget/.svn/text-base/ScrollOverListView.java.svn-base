package com.dfim.app.widget;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.union.cellremote.R;

public class ScrollOverListView extends ListView implements OnScrollListener {
    private int mLastY;
	private static final String TAG = "ScrollOverListView";
	private int mBottomPosition;
	private boolean isBack;
	private LayoutInflater inflater;
	//底部刷新布局
	private LinearLayout footView;
	//头部刷新的布局
	private LinearLayout headView;
	/**箭头图标**/
	private ImageView arrowImageView;
	/**头部滚动条**/
	private ProgressBar progressBar;
	/**头部显示下拉刷新等的控件**/
	private TextView tipsTextview;
	/**刷新控件**/
	private TextView lastUpdatedTextView;
	/**头部高度**/
	private int headContentHeight;
	/**底部高度*/
	private int footContentHeight;
	/**显示动画**/
	private RotateAnimation animation;
	/**头部回退显示动画**/
	private RotateAnimation reverseAnimation;
	
	/**状态**/
	private int state;
	/**无**/
	private final static int DONE = 3;
	/**开始的Y坐标**/
	private int startY;
	/**第一个item**/
	private int firstItemIndex;
	/** 用于保证startY的值在一个完整的touch事件中只被记录一次**/
	private boolean isRecored;
	/**是否要使用下拉刷新功能**/
	public boolean showRefresh = true;
	/**实际的padding的距离与界面上偏移距离的比例**/
	private final static int RATIO = 3;
	/**松开更新**/
	private final static int RELEASE_To_REFRESH = 0;
	/**下拉更新**/
	private final static int PULL_To_REFRESH = 1;
	/**更新中**/
	private final static int REFRESHING = 2;
	/**加载中**/
	private final static int LOADING = 4;
	public static boolean canRefleash = true;

	public ScrollOverListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ScrollOverListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ScrollOverListView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisibleItem;
		
	}
    
	
	
	/**
	 * 初始化控件
	 */
	private void init(Context context){
		mBottomPosition=0;
		setCacheColorHint(0);
		inflater = LayoutInflater.from(context);
		headView=(LinearLayout) inflater.inflate(R.layout.pull_down_head,null);
		footView=(LinearLayout) inflater.inflate(R.layout.pulldown_footer,null);
		if(footView.getVisibility()==View.GONE){
			System.out.println(View.GONE);
		}else if(footView.getVisibility()==View.VISIBLE){
			System.out.println(View.VISIBLE);
		}else if(footView.getVisibility()==View.INVISIBLE){
			System.out.println(View.INVISIBLE);
		}
		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);
		measureView(headView);
		headContentHeight=headView.getMeasuredHeight();
		headView.setPadding(0, -1*headContentHeight, 0, 0);
		headView.invalidate();//XX
		footContentHeight=footView.getMeasuredHeight();
		footView.setPadding(0, 0, 0,-1*footContentHeight);
		footView.invalidate();
		/**列表添加头部**/
		addHeaderView(headView, null, false);
		/**添加底部*/
	    addFooterView(footView,null,false);
		setOnScrollListener(this);
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		state =DONE;
	}
	//估计headView的width以及height
		private void measureView(View child) {
			ViewGroup.LayoutParams p = child.getLayoutParams();
			if (p == null) {
				p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			}
			int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
			int lpHeight = p.height;
			int childHeightSpec;
			if (lpHeight > 0) {
				childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
						MeasureSpec.EXACTLY);
			} else {
				childHeightSpec = MeasureSpec.makeMeasureSpec(0,
						MeasureSpec.UNSPECIFIED);
			}
			child.measure(childWidthSpec, childHeightSpec);
		}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int y = (int) ev.getRawY();// XX
		cancelLongPress();
		switch (action) {
		/*
		 * case MotionEvent.ACTION_DOWN: {
		 * 
		 * if(firstItemIndex==0 && !isRecored){ isRecored=true;
		 * startY=(int)ev.getY(); Log.i(TAG, "在down时候记录当前位置"); } mLastY=y; final
		 * boolean isHandled = mOnScrollOverListener.onMotionDown(ev); if
		 * (isHandled) { mLastY = y; return isHandled; }
		 * 
		 * break; } case MotionEvent.ACTION_MOVE: { // 手指正在移动的时候
		 * 
		 * int tempY=(int) ev.getY(); headView.setPadding(0,
		 * tempY/4-headContentHeight, 0, 0);
		 * footView.setPadding(0,tempY/4-footContentHeight, 0, 0); int[]
		 * prosut=new int[2]; headView.getLocationInWindow(prosut); Log.i("kk",
		 * headView.getPaddingTop()+"------"+headContentHeight+"==="
		 * +prosut[0]+"=="+prosut[1]);
		 * 
		 * break; } case MotionEvent.ACTION_UP: {// 弹起 int[] prosut = new
		 * int[2]; headView.getLocationInWindow(prosut); if (prosut[1] == 90) {
		 * // tipsTextview.setText("刷新成功");
		 * 
		 * try { VirtualData.clearcacheinitData(); Thread.sleep(500);
		 * 
		 * } catch (Exception e) { // tipsTextview.setText("刷新失败");
		 * e.printStackTrace(); } } headView.setPadding(0, -1 *
		 * headContentHeight, 0, 0); footView.setPadding(0, 0, 0, -1 *
		 * footContentHeight); // tipsTextview.setText(""); }
		 */
		}
		mLastY = y;
		return super.onTouchEvent(ev);
	}
		
	
	
		/**空的*/
		private OnScrollOverListener mOnScrollOverListener = new OnScrollOverListener(){

			@Override
			public boolean onListViewTopAndPullDown(int delta) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onListViewBottomAndPullUp(int delta) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onMotionDown(MotionEvent ev) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onMotionMove(MotionEvent ev, int delta) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onMotionUp(MotionEvent ev) {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
	
		/**
	     *滚动监听接口 
	     *
	     */
		public interface OnScrollOverListener{
			/**
			 * 到达最顶部触发  手指点击移动产生的偏移量
			 */
			boolean onListViewTopAndPullDown(int delta);
			/**
			 *            手指点击移动产生的偏移量
			 * 到达最底部触发
			 */
			boolean onListViewBottomAndPullUp(int delta);
			/**
			 * 手指触摸按下触发，相当于{@link MotionEvent#ACTION_DOWN}
			 */
			boolean onMotionDown(MotionEvent ev);
			/**
			 * 手指触摸移动触发，
			 *
			 * */
			boolean onMotionMove(MotionEvent ev, int delta);
			/**
			 * 手指触摸后提起触发，相当于{@link MotionEvent#ACTION_UP}
			 */
			boolean onMotionUp(MotionEvent ev);
			
		}
		
		// 当状态改变时候，调用该方法，以更新界面
			private void changeHeaderViewByState() {
				switch (state) {
				case RELEASE_To_REFRESH:
					arrowImageView.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					tipsTextview.setVisibility(View.VISIBLE);
					lastUpdatedTextView.setVisibility(View.VISIBLE);

					arrowImageView.clearAnimation();
					arrowImageView.startAnimation(animation);

					tipsTextview.setText("松开刷新");

					Log.v(TAG, "当前状态，松开刷新");
					break;
				case PULL_To_REFRESH:
					progressBar.setVisibility(View.GONE);
					tipsTextview.setVisibility(View.VISIBLE);
					lastUpdatedTextView.setVisibility(View.VISIBLE);
					arrowImageView.clearAnimation();
					arrowImageView.setVisibility(View.VISIBLE);
					// 是由RELEASE_To_REFRESH状态转变来的
					if (isBack) {
						isBack = false;
						arrowImageView.clearAnimation();
						arrowImageView.startAnimation(reverseAnimation);

						tipsTextview.setText("下拉刷新");
					} else {
						tipsTextview.setText("下拉刷新");
					}
					Log.v(TAG, "当前状态，下拉刷新");
					break;

				case REFRESHING:

					headView.setPadding(0, 0, 0, 0);
					progressBar.setVisibility(View.VISIBLE);
					arrowImageView.clearAnimation();
					arrowImageView.setVisibility(View.GONE);
					tipsTextview.setText("正在刷新...");
					lastUpdatedTextView.setVisibility(View.VISIBLE);

					Log.v(TAG, "当前状态,正在刷新...");
					break;
				case DONE:
					headView.setPadding(0, -1 * headContentHeight, 0, 0);
					progressBar.setVisibility(View.GONE);
					arrowImageView.clearAnimation();
					arrowImageView.setImageResource(R.drawable.pull_down_arrow);
					tipsTextview.setText("下拉刷新");
					lastUpdatedTextView.setVisibility(View.VISIBLE);

					Log.v(TAG, "当前状态，done");
					break;
				}
			}
			public void onRefreshComplete() {
				state = DONE;
				lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
				changeHeaderViewByState();
			}
			
			/**
			 * 设置这个Listener可以监听是否到达顶端，或者是否到达低端等事件</br>
			 * 
			 * @see OnScrollOverListener
			 */
			public void setOnScrollOverListener(
					OnScrollOverListener onScrollOverListener) {
				mOnScrollOverListener = onScrollOverListener;
			}
			// =============================== public method
			/**
			 * 可以自定义其中一个条目为头部，头部触发的事件将以这个为准，默认为第一个
			 * 
			 * @param index  正数第几个，必须在条目数范围之内
			 */
			public void setTopPosition(int index) {
				if (getAdapter() == null)
					throw new NullPointerException(
							"You must set adapter before setTopPosition!");
				if (index < 0)
					throw new IllegalArgumentException("Top position must > 0");
			}

			/**
			 * 可以自定义其中一个条目为尾部，尾部触发的事件将以这个为准，默认为最后一个
			 * 
			 * @param index  倒数第几个，必须在条目数范围之内
			 */
			public void setBottomPosition(int index) {
				if (getAdapter() == null)
					throw new NullPointerException(
							"You must set adapter before setBottonPosition!");
				if (index < 0)
					throw new IllegalArgumentException("Bottom position must > 0");

				mBottomPosition = index;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
				
}
