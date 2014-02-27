package com.union.cellremote.store;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.Artist;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.thread.Pools;
import com.dfim.app.widget.MyLetterListView;
import com.dfim.app.widget.MyLetterListView.OnTouchingLetterChangedListener;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ArtistsListAdapter;

public class ArtistsFragment extends Fragment implements NobleMan {

	// Looper.prepare
	private final String TAG = "ArtistsFragment";
	private Context context;

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llNoData;
	private ListView lvArtists;
	private MyLetterListView mllvLetters;
	private TextView overlay;
	private ArtistsListAdapter adapter;
	private View footerView;
	private TextView tvLoadMore;
	private LinearLayout llLoadingMore;

	private boolean isAddingData = false;
	private List<Artist> artists = new ArrayList<Artist>();
	private List<Artist> tempList = new ArrayList<Artist>();// 分页临时数据
	private int maxDataItems = 50;
	private String[] sections;
	private OverlayThread overlayThread;
	private String chosenLetter = "all";

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

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_LETS_GET_DATA:
				if (WatchDog.flagInArtists == true) {
					getSavedDataAndPosition();
				} else {
					getArtistsData(0, maxDataItems, "all", MSG_DATA_NEW_GOT);
				}
				break;

			case MSG_DATA_NEW_GOT:
				Log.e(TAG, "chosenLetter="+chosenLetter+" & msg.obj="+msg.obj);
				
				if (!chosenLetter.equals((String) msg.obj)) {
					break;
				}
				artists.clear();
				artists.addAll(tempList);
				updateUI();
//				paintArtistsWithinSightAndFreeOthers();
				tempList.clear();
				break;

			case MSG_DATA_ADD_GOT:
				artists.addAll(tempList);
				updateUI();
//				paintArtistsWithinSightAndFreeOthers();
				tempList.clear();
				break;

			case MSG_NOTIFY_DATA_SET_CHANGED:
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
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

			if (isAddingData == true) {
				tvLoadMore.setVisibility(View.VISIBLE);
				llLoadingMore.setVisibility(View.GONE);
				isAddingData = false;

				checkIfDataLoadCompleted();
			}
		}
	}

	private void checkIfDataLoadCompleted() {
		if (tempList.size() < maxDataItems) {
			tvLoadMore.setText("加载完毕");
			tvLoadMore.setEnabled(false);
		}
	}

	private void lvAddFooterView() {
		lvArtists.addFooterView(footerView);

		tvLoadMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tvLoadMore.setVisibility(View.GONE);
				llLoadingMore.setVisibility(View.VISIBLE);
				isAddingData = true;

				// 获取更多数据
				getArtistsData(artists.size(), maxDataItems, chosenLetter, MSG_DATA_ADD_GOT);
			}
		});
	}

	private void getArtistsData(final int startItem, final int maxItems, final String firstLetter, final int dataMode) {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				tempList = new HttpGetter(context).getArtistsList(startItem, maxItems, firstLetter);// 获取到的数据
				System.out.println("tempList=" + tempList);

				Message msg = new Message();
				msg.what = dataMode;
				msg.obj = firstLetter;
				handler.sendMessage(msg);
				
				WatchDog.artistsDataGot = true;
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
		parentActivityChangeButton();
		parentActivityChangeTitle();
		view = LayoutInflater.from(context).inflate(R.layout.artists_fragment, null);

		initComponents();
		startLoading();
		getArtistListWhenActive();
		initListeners();
		registerReceivers();

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

	@Override
	public void onResume() {
		super.onResume();
		// listViewGetFormerPosition();
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

/*	protected void downloadImage(final Artist artist) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				String imageKey = artist.getImgUrl() + "150";

				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, artist.getImgUrl(), 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						// // 得到专辑封面后刷新界面
						// artist.setBitmap(new SoftReference<Bitmap>(bitmap));
						// handler.post(new Runnable() {
						// @Override
						// public void run() {
						// if (adapter != null) {
						// adapter.notifyDataSetChanged();
						// }
						// }
						// });

						// 得到封面后刷新界面
						if (bitmap != null && !bitmap.isRecycled()) {
							artist.setBitmap(new SoftReference<Bitmap>(bitmap));
							handler.sendEmptyMessage(MSG_NOTIFY_DATA_SET_CHANGED);
						}

					}
				});

				// 得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					artist.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_NOTIFY_DATA_SET_CHANGED);
				}

				bitmap = null;
			}
		});

	}*/

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		lvArtists = (ListView) view.findViewById(R.id.lv_artists);
		mllvLetters = (MyLetterListView) view.findViewById(R.id.mllv);

		footerView = LayoutInflater.from(context).inflate(R.layout.artists_item_footerview, null);
		tvLoadMore = (TextView) footerView.findViewById(R.id.tv_load_more);
		llLoadingMore = (LinearLayout) footerView.findViewById(R.id.ll_loading_more);

		initListView();
		initMyLetterListView();
		initOverlay();
	}

	private void initOverlay() {
		overlayThread = new OverlayThread();// 用于让提示字母消失

		LayoutInflater inflater = LayoutInflater.from(context);
		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
		overlay.setVisibility(View.INVISIBLE);
//		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(context.getResources().getInteger(R.integer.letter_overlay_size), context.getResources().getInteger(R.integer.letter_overlay_size), WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
	}

	private void initMyLetterListView() {
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
					((TabWebActivity) context).goArtistDetail(artist);
				}
				// else {
				// CustomToast.makeText(context, "触发加载更多",
				// Toast.LENGTH_SHORT).show();
				// }
			}
		});

/*		lvArtists.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					firstVisibleItemPosition = lvArtists.getFirstVisiblePosition();
					lastVisibleItemPosition = lvArtists.getLastVisiblePosition();
					if (lvArtists.getChildAt(0) != null) {
						scrollTop = lvArtists.getChildAt(0).getTop();
					}

					paintArtistsWithinSightAndFreeOthers();
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});*/

	}

/*	protected void paintArtistsWithinSightAndFreeOthers() {
		for (int i = 0; i < artists.size(); i++) {
			if (i >= firstVisibleItemPosition && i <= lastVisibleItemPosition) {
				downloadImage(artists.get(i));
			} else {
				Bitmap bitmap = artists.get(i).getBitmap();
				if (bitmap != null && !bitmap.equals(Constant.albumCover)) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}

	private void freeArtistsWithinSight() {
		for (int i = 0; i < artists.size(); i++) {
			if (i >= firstVisibleItemPosition && i <= lastVisibleItemPosition) {
				Bitmap bitmap = artists.get(i).getBitmap();
				if (bitmap != null && !bitmap.equals(Constant.albumCover)) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}
*/
	private void initListeners() {

	}

	@Override
	public void onDetach() {
		unregisterReceivers();
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
//		freeArtistsWithinSight();
	}

	@Override
	public void letsSeeHeaven() {

	}

	@Override
	public void recordCurrentDataAndPosition() {
		WatchDog.formerArtists = artists;
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
		updateUI();
//		paintArtistsWithinSightAndFreeOthers();

		WatchDog.flagInArtists = false;
		WatchDog.formerArtists = null;
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
//				if (chosenLetter.equals(s)) {
//					getArtistsData(0, maxDataItems, chosenLetter, MSG_DATA_NEW_GOT);
//				}
				getArtistsData(0, maxDataItems, s, MSG_DATA_NEW_GOT);
			}
		}, 100);
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

			chosenLetter = _s;
			loadContentIfChosenConfirmed(_s);

			overlay.setText(s);
			overlay.setVisibility(View.VISIBLE);
			handler.removeCallbacks(overlayThread);
			handler.postDelayed(overlayThread, 1500);
		}
	}

}
