package com.dfim.app.fragment.store;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.adapter.ArtistsListAdapter;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.ViewFactory;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.Artist;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.interfaces.SelfReloader;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.AnimUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.widget.MyLetterListView;
import com.dfim.app.widget.MyLetterListView.OnTouchingLetterChangedListener;
import com.union.cellremote.R;

public class ArtistsFragment extends Fragment implements NobleMan, TitlebarUpdateFragment,SelfReloader {

	// Looper.prepare
	private final String TAG = "ArtistsFragment";
	private Context context;

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llNoData;
	private LinearLayout llLetterListview;
	private ListView lvArtists;
	private MyLetterListView mllvLetters;
	private TextView overlay;
	private ArtistsListAdapter adapter;
	private View footerView;
	private TextView tvLoadMore;
	private LinearLayout llLoadingMore;

//	private boolean isAddingData = false;
	private boolean dataLoadComplete = false;
	
	private List<Artist> artists = new ArrayList<Artist>();
	private List<Artist> tempList = new ArrayList<Artist>();// 分页临时数据
	private int maxDataItems = 10;
	private int maxDataItemsLarger = 30;
	private String[] sections;
	private OverlayThread overlayThread;
//	private String chosenLetter = "all";

	// 动画
	private AnimationDrawable ad;
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 9;// 记录停止卷动时第一个ITEM的序号
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量

	private final int MSG_LETS_GET_DATA = 0;
	private final int MSG_DATA_NEW_GOT = 1;
	private final int MSG_DATA_ADD_GOT = 2;
	private final int MSG_NOTIFY_DATA_SET_CHANGED = 3;
	private final int MSG_DATA_LOAD_FAILD = 4;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_LETS_GET_DATA:
				if (WatchDog.flagInArtists == true) {
					getSavedDataAndPosition();
				} else {
					getArtistsData(0, maxDataItems, WatchDog.currentChosenLetter, MSG_DATA_NEW_GOT);
				}
				break;

			case MSG_DATA_NEW_GOT:
				Log.e(TAG, "WatchDog.currentChosenLetter="+WatchDog.currentChosenLetter+" & msg.obj="+msg.obj);
				
				if (!WatchDog.currentChosenLetter.equals((String) msg.obj)) {
					break;
				}
				
				artists.clear();
				artists.addAll(tempList);
				updateUI();
				lvArtists.setSelection(0);//fuck
				// paintArtistsWithinSightAndFreeOthers();
				tempList.clear();
				break;

			case MSG_DATA_ADD_GOT:
				artists.addAll(tempList);
				updateUI();
				// paintArtistsWithinSightAndFreeOthers();
				tempList.clear();
				break;

			case MSG_NOTIFY_DATA_SET_CHANGED:
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private BroadcastReceiver fitArtistsTitleReceier = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			parentActivityChangeButton();
			parentActivityChangeTitle();
		}
	};

	protected void updateUI() {
		if (adapter != null) {
			if (loadingRunning == true) {
				endLoading();
			}

			// artists.addAll(tempList);
			adapter.notifyDataSetChanged();

//			if (isAddingData == true) {
//				tvLoadMore.setVisibility(View.VISIBLE);
//				llLoadingMore.setVisibility(View.GONE);				
				tvLoadMore.setVisibility(View.GONE);
				llLoadingMore.setVisibility(View.VISIBLE);
//				isAddingData = false;

				checkIfDataLoadCompleted();
//			}

		}
	}

	private void checkIfDataLoadCompleted() {
		if (tempList.size() < maxDataItems) {
//			tvLoadMore.setVisibility(View.VISIBLE);
//			llLoadingMore.setVisibility(View.GONE);			
//			tvLoadMore.setText("加载完毕");
			
			dataLoadComplete = true;
			tvLoadMore.setVisibility(View.GONE);
			llLoadingMore.setVisibility(View.GONE);
		}else{
			dataLoadComplete = false;
		}
	}

	private void hideLoadingAndShowDataLoadComplete() {		
		tvLoadMore.setVisibility(View.GONE);
		llLoadingMore.setVisibility(View.GONE);
		UpnpApp.mainHandler.showInfo(R.string.loading_complete_info);
	}
	

	protected void showLoadingAndLoadMoreData() {
		llLoadingMore.setVisibility(View.VISIBLE);
		getArtistsData(artists.size(), maxDataItemsLarger, WatchDog.currentChosenLetter, MSG_DATA_ADD_GOT);
	}

	private void lvAddFooterView() {
		lvArtists.addFooterView(footerView);

//		tvLoadMore.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onTvLoadMoreClick();
//			}
//		});
	}

//	protected void onTvLoadMoreClick() {
//		tvLoadMore.setVisibility(View.GONE);
//		llLoadingMore.setVisibility(View.VISIBLE);
//		isAddingData = true;
//
//		// 获取更多数据
//		getArtistsData(artists.size(), maxDataItemsLarger, WatchDog.currentChosenLetter, MSG_DATA_ADD_GOT);
//	}

	private void getArtistsData(final int startItem, final int maxItems, final String firstLetter, final int dataMode) {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				String json = new HttpGetter(context).getArtistsListII(startItem, maxItems, firstLetter);// 获取到的数据
				
				if (!(new JsonUtil().validate(json))) {
					return;
				}else{
					tempList = new JsonUtil().getArtists(json);// 获取到的数据
					System.out.println("tempList=" + tempList);

					Message msg = new Message();
					msg.what = dataMode;
					msg.obj = firstLetter;
					handler.sendMessage(msg);
					
					WatchDog.artistsDataGot = true;
				}
			}
		});
	}

	public ArtistsFragment() {

	}

	public ArtistsFragment(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// parentActivityChangeButton();
		// parentActivityChangeTitle();
		updateTitlebar();
		view = LayoutInflater.from(context).inflate(R.layout.artists_fragment, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		startLoading();
		getArtistListWhenActive();
		// registerReceivers();

		return view;
	}

	private void getArtistListWhenActive() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (fragmentIsActive == false) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(MSG_LETS_GET_DATA);
			}
		}).start();
	}

	private void startLoading() {
		Log.e(TAG, "startLoading()");
		llContent.setVisibility(View.INVISIBLE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.INVISIBLE);

		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();

		loadingRunning = true;
	}

	protected void endLoading() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}

		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
		llContent.setVisibility(View.VISIBLE);

		loadingRunning = false;
	}
	
	public void uiShowNoData() {
		System.out.println(TAG+"showNoData");
		stopLoadingAnimation();

		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);

		View loadFailureView=new ViewFactory().createLoadFailureView(this);
		llNoData.removeAllViews();
		llNoData.addView(loadFailureView);
	}
	
	private void stopLoadingAnimation() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}
		loadingRunning = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		// listViewGetFormerPosition();
		WatchDog.currentSelfReloader = this;
		fragmentIsActive = true;
	}

	private void listViewGetFormerPosition() {
		if (firstVisibleItemPosition != -1) {
			lvArtists.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
		}
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("演出者");
		TabWebActivity.currentMenuItem = "演出者";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
	}

	/*
	 * protected void downloadImage(final Artist artist) {
	 * Pools.executorService2.submit(new Runnable() {
	 * 
	 * @Override public void run() { String imageKey = artist.getImgUrl() +
	 * "150";
	 * 
	 * Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey,
	 * artist.getImgUrl(), 150, false, new ImageCallBack() {
	 * 
	 * @Override public void imageLoaded(Bitmap bitmap) { // // 得到专辑封面后刷新界面 //
	 * artist.setBitmap(new SoftReference<Bitmap>(bitmap)); // handler.post(new
	 * Runnable() { // @Override // public void run() { // if (adapter != null)
	 * { // adapter.notifyDataSetChanged(); // } // } // });
	 * 
	 * // 得到封面后刷新界面 if (bitmap != null && !bitmap.isRecycled()) {
	 * artist.setBitmap(new SoftReference<Bitmap>(bitmap));
	 * handler.sendEmptyMessage(MSG_NOTIFY_DATA_SET_CHANGED); }
	 * 
	 * } });
	 * 
	 * // 得到封面后刷新界面 if (bitmap != null && !bitmap.isRecycled()) {
	 * artist.setBitmap(new SoftReference<Bitmap>(bitmap));
	 * handler.sendEmptyMessage(MSG_NOTIFY_DATA_SET_CHANGED); }
	 * 
	 * bitmap = null; } });
	 * 
	 * }
	 */

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		llLetterListview = (LinearLayout) view.findViewById(R.id.ll_letter_listview);
		lvArtists = (ListView) view.findViewById(R.id.lv_artists);
		mllvLetters = (MyLetterListView) view.findViewById(R.id.mllv);

		footerView = LayoutInflater.from(context).inflate(R.layout.artists_item_footerview, null);
		tvLoadMore = (TextView) footerView.findViewById(R.id.tv_load_more);
		llLoadingMore = (LinearLayout) footerView.findViewById(R.id.ll_loading_more);
		initLoadingmoreAnim();

		initListView();
		initMyLetterListView();
		initOverlay();
	}
	
	private void initLoadingmoreAnim() {	
		ImageView ivAnim=(ImageView) llLoadingMore.findViewById(R.id.iv_loading_more);		
		new AnimUtil(context).initLoadingmoreAnim(ivAnim);
		
//		AnimationDrawable ad=(AnimationDrawable)getResources().getDrawable(R.anim.login_seraching_anim); 
//		ivAnim.setBackgroundDrawable(ad); 		
//		ad.start();
	}

	private void initOverlay() {
		overlayThread = new OverlayThread();// 用于让提示字母消失

		LayoutInflater inflater = LayoutInflater.from(context);
		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
		overlay.setVisibility(View.INVISIBLE);
		// WindowManager.LayoutParams lp = new
		// WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT,
		// WindowManager.LayoutParams.TYPE_APPLICATION,
		// WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
		// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
		// PixelFormat.TRANSLUCENT);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(context.getResources().getInteger(R.integer.letter_overlay_size), context.getResources().getInteger(R.integer.letter_overlay_size), WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
	}

	private void initMyLetterListView() {
		mllvLetters.setChoose(getChosenLetterPosition(WatchDog.currentChosenLetter));
		llLetterListview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				Log.e(TAG, "llLetterListview onTouch: event.getY()=" + event.getY());
				mllvLetters.onTouch(event);
				return true;
			}
		});

		mllvLetters.setOnTouchingLetterChangedListener(new LetterListViewListener());
	}

	private void initListView() {
		lvAddFooterView();

		adapter = new ArtistsListAdapter(context);
		adapter.setArtists(artists);
		lvArtists.setAdapter(adapter);

		lvArtists.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < adapter.getCount()) {
					recordCurrentDataAndPosition();
					// 进入相关演出者详情页
					Artist artist = artists.get(position);
					WatchDog.tabWebFragment.goArtistDetail(artist);
				}
				// else {
				// CustomToast.makeText(context, "触发加载更多",
				// Toast.LENGTH_SHORT).show();
				// }
			}
		});

		// 启动滑动监听
		lvArtists.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					firstVisibleItemPosition = lvArtists.getFirstVisiblePosition();
					lastVisibleItemPosition = lvArtists.getLastVisiblePosition();					
					if (lvArtists.getChildAt(0) != null) {
						scrollTop = lvArtists.getChildAt(0).getTop();
					}
					
					// 判断是否见底
					if(lastVisibleItemPosition>=artists.size()-1){
						if(dataLoadComplete){
							//显示加载完毕
							hideLoadingAndShowDataLoadComplete();
						}else{
							// 加载更多并显示动画
							showLoadingAndLoadMoreData();
						}
					}

//					paintArtistsWithinSightAndFreeOthers();
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});

	}

	/*
	 * protected void paintArtistsWithinSightAndFreeOthers() { for (int i = 0; i
	 * < artists.size(); i++) { if (i >= firstVisibleItemPosition && i <=
	 * lastVisibleItemPosition) { downloadImage(artists.get(i)); } else { Bitmap
	 * bitmap = artists.get(i).getBitmap(); if (bitmap != null &&
	 * !bitmap.equals(Constant.albumCover)) { bitmap.recycle(); bitmap = null; }
	 * } } }
	 * 
	 * private void freeArtistsWithinSight() { for (int i = 0; i <
	 * artists.size(); i++) { if (i >= firstVisibleItemPosition && i <=
	 * lastVisibleItemPosition) { Bitmap bitmap = artists.get(i).getBitmap(); if
	 * (bitmap != null && !bitmap.equals(Constant.albumCover)) {
	 * bitmap.recycle(); bitmap = null; } } } }
	 */

	@Override
	public void onDetach() {
		// unregisterReceivers();
		if (artists.size() != 0) {
			recycleBitmaps();
		}
		super.onDetach();
	}

	private void registerReceivers() {
		context.registerReceiver(fitArtistsTitleReceier, new IntentFilter("fitArtistsTitleReceier"));
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(fitArtistsTitleReceier);
	}

	@Override
	public void recycleBitmaps() {
		// freeArtistsWithinSight();
	}

	@Override
	public void letsSeeHeaven() {

	}

	@Override
	public void recordCurrentDataAndPosition() {
		WatchDog.formerArtists = artists;
//		WatchDog.currentChosenLetter = chosenLetter;
		WatchDog.fvipInArtists = firstVisibleItemPosition;
		WatchDog.lvipInArtists = lastVisibleItemPosition;
		WatchDog.stInArtists = scrollTop;
		
		WatchDog.flagInArtists = true;
	}

	@Override
	public void getSavedDataAndPosition() {
		firstVisibleItemPosition = WatchDog.fvipInArtists;
		scrollTop = WatchDog.stInArtists;

		artists = WatchDog.formerArtists;
//		chosenLetter = WatchDog.currentChosenLetter;
		mllvLetters.setChoose(getChosenLetterPosition(WatchDog.currentChosenLetter));
		updateUI();
		// paintArtistsWithinSightAndFreeOthers();

		WatchDog.flagInArtists = false;
		WatchDog.formerArtists = null;
	}

	private int getChosenLetterPosition(String chosenLetter) {
		String[] b = { "*", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };
		for (int i = 0; i < b.length; i++) {
			if (b[i].equals(chosenLetter)) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public int getFistVisiblePosition() {
		return firstVisibleItemPosition;
	}

	@Override
	public int getLastVisiblePosition() {
		return lastVisibleItemPosition;
	}

	private class OverlayThread implements Runnable {
		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
		}
	}

	public void loadContentIfChosenConfirmed(final String s) {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
//				if (WatchDog.currentChosenLetter.equals(s)) {
//					getArtistsData(0, maxDataItems, WatchDog.currentChosenLetter, MSG_DATA_NEW_GOT);
//				}
				getArtistsData(0, maxDataItems, s, MSG_DATA_NEW_GOT);
			}
		},
		// 1500
				0);
	}

	private class LetterListViewListener implements OnTouchingLetterChangedListener {

		@Override
		public void onTouchingLetterChanged(final String s) {
			// UpnpApp.showToastMessage(s + " touched!");
			String _s = s;
			if (_s.equals("#")) {
				_s = "all";
			} else if (_s.equals("*")) {
				return;
			}

			// 加载字母下的内容
			WatchDog.currentChosenLetter = _s;
			loadContentIfChosenConfirmed(_s);

			// 显示字母并在1.5秒后消失
			overlay.setText(s);
			overlay.setVisibility(View.VISIBLE);
			handler.removeCallbacks(overlayThread);
			handler.postDelayed(overlayThread, 1500);
		}
	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(false);
		WatchDog.tabWebFragment.setTitle("演出者");
		WatchDog.tabWebFragment.currentMenuItem = "演出者";
	}
	
	@Override
	public void reload() {
		System.out.println(TAG+"reloading...");
		startLoading();
		getArtistListWhenActive();
	}

	@Override
	public void onDataLoadFailed() {
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

}
