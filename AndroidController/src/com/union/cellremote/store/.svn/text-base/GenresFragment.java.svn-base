package com.union.cellremote.store;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.Column;
import com.dfim.app.domain.ColumnDetail;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ColumnsListAdapter;

public class GenresFragment extends Fragment implements NobleMan {

	// Looper.prepare

	private final String TAG = "BotiqueFragment";
	private Context context;

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private LinearLayout llContent;
	private ExpandableListView xlvGenres;
	private ColumnsListAdapter adapter;
	private AnimationDrawable ad;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 5;// 记录停止卷动时第一个ITEM的序号
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	private String uri = "";
	private List<Column> genres = new ArrayList<Column>();

	private BroadcastReceiver fitGenresTitleReceier = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			parentActivityChangeTitle();
			parentActivityChangeButton();
		}
	};

	private final int MSG_GENRES_DATA_GOT = 0;
	private final int MSG_LETS_GET_DATA = 1;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GENRES_DATA_GOT:
				updateUI();
				break;

			case MSG_LETS_GET_DATA:
				if (WatchDog.flagInBotiques == true) {
					getSavedDataAndPosition();
				} else {
					getColumnsList();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void getColumnsList() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				genres = new HttpGetter(context).getGenresList();

				if (genres != null && genres.size() != 0) {
					adapter.setColumns(genres);
					Log.e(TAG, "botiques.size()=" + genres.size());
					handler.sendEmptyMessage(MSG_GENRES_DATA_GOT);

					// 下载每个子栏目的专辑列表
					for (Column genre : genres) {
						getColumnAlbumsList(genre);
					}

					// WatchDog.genresDataGot = true;
				}
			}
		});
	}

	protected void updateUI() {
		if (adapter != null) {
			endLoading();

			adapter.notifyDataSetChanged();
			for (int i = 0; i < adapter.getGroupCount(); i++) {
				xlvGenres.expandGroup(i);
			}

			xlvGenres.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
		}
	}

	public GenresFragment() {

	}

	public GenresFragment(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		System.out.println("onCreateView");
		parentActivityChangeTitle();
		parentActivityChangeButton();
		view = LayoutInflater.from(context).inflate(R.layout.genres_fragment, null);

		initComponents();
		startLoading();
		getColumnsWhenActive();
		initListeners();
		registerReceivers();

		return view;
	}

	private void getColumnsWhenActive() {
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

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("类型");
		TabWebActivity.currentMenuItem = "类型";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
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

		llContent.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);

		loadingRunning = false;
	}

	@Override
	public void onResume() {
		System.out.println("onResume");
		super.onResume();
		fragmentIsActive = true;
		// ((TabWebActivity)context).checkSlidingHidden();
	}

	// private void parentActivityChangeTitle() {
	// TabWebActivity.tvTitle.setText("类型");
	// TabWebActivity.currentFragment = "类型";
	// // TabWebActivity.slidingMenuInitOk = false;
	// }
	//
	// private void parentActivityChangeButton() {
	// TabWebActivity.changeButton("btnMenu");
	// }

	protected void getColumnAlbumsList(final Column column) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(column, Constant.COLUMN_ALBUMS_4_GENRES);

				ColumnDetail columnDetail = new JsonUtil().getColumnDetail(json);
				column.setDetail(columnDetail);

				// 拿到栏目专辑列表后刷新界面
				handler.sendEmptyMessage(MSG_GENRES_DATA_GOT);

				// // 下载每张专辑的封面图片
				// for (int i = 0; i < columnDetail.getAlbums().size(); i++) {
				// //
				// columnDetail.getAlbums().get(i).setCoverBitmap(Constant.albumCover);
				// downloadImage(columnDetail.getAlbums().get(i));
				// }
			}
		});

	}

	protected void downloadImage(final Album album) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = album.getImgUrl() + "150";

				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, album.getImgUrl(), 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						// 得到专辑封面后刷新界面
//						album.setCoverBitmap(new SoftReference<Bitmap>(bitmap));
//						handler.post(new Runnable() {
//							@Override
//							public void run() {
//								if (adapter != null) {
//									// endLoading();
//									adapter.notifyDataSetChanged();
//									for (int i = 0; i < adapter.getGroupCount(); i++) {
//										xlvGenres.expandGroup(i);
//									}
//								}
//							}
//						});
						
						if (bitmap != null && !bitmap.isRecycled()) {
							album.setBitmap(new SoftReference<Bitmap>(bitmap));
							handler.sendEmptyMessage(MSG_GENRES_DATA_GOT);
						}
						
					}
				});

				// 得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_GENRES_DATA_GOT);
				}

				bitmap = null;
				// Looper.loop();
			}
		});

	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);//

		// 初始化expandableListView
		xlvGenres = (ExpandableListView) view.findViewById(R.id.xlv_genres);
		initExpandableListView();
	}

	private void initListeners() {

	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		// letsSeeHeaven();
		if (genres.size() != 0) {
			recycleBitmaps();
		}

		super.onDetach();
	}

	private void registerReceivers() {
		context.registerReceiver(fitGenresTitleReceier, new IntentFilter("fitGenresTitleReceier"));
	}

	private void initArguments() {

	}

	private void initExpandableListView() {
		xlvGenres.setGroupIndicator(null);
		xlvGenres.setItemsCanFocus(true);
		xlvGenres.setOnGroupClickListener(null);
		adapter = new ColumnsListAdapter(context, genres, xlvGenres, this);
		xlvGenres.setAdapter(adapter);

		xlvGenres.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					firstVisibleItemPosition = xlvGenres.getFirstVisiblePosition();
					lastVisibleItemPosition = xlvGenres.getLastVisiblePosition();
					if (xlvGenres.getChildAt(0) != null) {
						scrollTop = xlvGenres.getChildAt(0).getTop();
					}

					paintAlbumsWithinSightNFreeOthers();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

	protected void paintAlbumsWithinSightNFreeOthers() {
		for (int i = 0; i < genres.size(); i++) {
			if (2 * i + 1 >= firstVisibleItemPosition && 2 * i - 1 <= lastVisibleItemPosition) {
				downloadColumnImage(genres.get(i));
			} else {
				FreeColumnImage(genres.get(i));
			}
		}
	}

	private void FreeColumnImage(Column column) {
		if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
			return;
		}
		for (Album album : column.getDetail().getAlbums()) {
			freeImage(album);
		}
	}

	private void freeImage(Album album) {
		Bitmap bitmap = album.getBitmap();
		if (bitmap != null && !bitmap.equals(Constant.albumCover)) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	private void downloadColumnImage(Column column) {
		for (Album album : column.getDetail().getAlbums()) {
			downloadImage(album);
		}
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(fitGenresTitleReceier);
	}

	@Override
	public void letsSeeHeaven() {
		for (Column column : genres) {
			for (Album album : column.getDetail().getAlbums()) {
				album.recyleBitmap();
			}
		}
	}

	@Override
	public void recordCurrentDataAndPosition() {
		WatchDog.columnsInBotiques = genres;
		WatchDog.fvipInGenres = firstVisibleItemPosition;
		// WatchDog.lvipInBotiques = -1;
		WatchDog.stInGenres = scrollTop;
		WatchDog.flagInGenres = true;
	}

	@Override
	public void getSavedDataAndPosition() {
		genres = WatchDog.columnsInGenres;
		firstVisibleItemPosition = WatchDog.fvipInGenres;
		scrollTop = WatchDog.stInGenres;

		handler.sendEmptyMessage(MSG_GENRES_DATA_GOT);
		WatchDog.flagInGenres = false;
		WatchDog.columnsInGenres = null;
	}

	@Override
	public int getFistVisiblePosition() {
		return firstVisibleItemPosition;
	}

	@Override
	public int getLastVisiblePosition() {
		return lastVisibleItemPosition;
	}

	@Override
	public void recycleBitmaps() {
		for (Column column : genres) {
			if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
				continue;
			}
			for (Album album : column.getDetail().getAlbums()) {// null pointer
				album.recyleBitmap();
			}
		}
	}
}
