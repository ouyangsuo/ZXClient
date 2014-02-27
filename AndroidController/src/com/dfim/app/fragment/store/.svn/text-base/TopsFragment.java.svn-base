package com.dfim.app.fragment.store;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.adapter.ColumnsListAdapter;
import com.dfim.app.common.Constant;
import com.dfim.app.common.ViewFactory;
import com.dfim.app.common.WatchDog;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.Column;
import com.dfim.app.domain.ColumnDetail;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.interfaces.SelfReloader;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.union.cellremote.R;

public class TopsFragment extends Fragment implements NobleMan,TitlebarUpdateFragment,SelfReloader {

	// Looper.prepare,Pools.execut

	private final String TAG = "TopsFragment";
	private Context context;

	private View view;
	private AnimationDrawable ad;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llNoData;
	private ExpandableListView xlvTops;
	private ColumnsListAdapter adapter;
	private boolean fragmentIsActive = false;
	private boolean loadingRunning = false;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 5;//
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量

	private String uri = "";
	private List<Column> tops = new ArrayList<Column>();

	private final int MSG_TOPS_DATA_GOT = 0;
	private final int MSG_NO_DATA = 1;
	private final int MSG_LETS_GET_DATA = 2;
	private final int MSG_ADAPTER_DATA_SET_CHANGED = 3;
	private final int MSG_DATA_LOAD_FAILD = 4;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TOPS_DATA_GOT:
				updateUI();
				break;

			case MSG_NO_DATA:
				llNoData.setVisibility(View.VISIBLE);
				llLoading.setVisibility(View.GONE);
				llContent.setVisibility(View.GONE);
				break;

			case MSG_LETS_GET_DATA:
//				if (WatchDog.flagInBotiques == true) {
//					getSavedDataAndPosition();
//				} else {
//					getTopsList();
//				}
				getTopsList();
				break;

			case MSG_ADAPTER_DATA_SET_CHANGED:
				adapter.notifyDataSetChanged();
				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();
			}

			super.handleMessage(msg);
		}
	};

	private BroadcastReceiver fitTopsTitleReceier = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			parentActivityChangeTitle();
			parentActivityChangeButton();
		}
	};
	
	private void stopLoadingAnimation() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}
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
	
	protected void updateUI() {
		if (adapter != null) {

			if (loadingRunning == true) {
				endLoading();
			}

			adapter.notifyDataSetChanged();
			for (int i = 0; i < adapter.getGroupCount(); i++) {
				xlvTops.expandGroup(i);
			}

			xlvTops.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
			handler.sendMessageDelayed(handler.obtainMessage(MSG_ADAPTER_DATA_SET_CHANGED), 500);
		}
	}

	public TopsFragment() {

	}

	public TopsFragment(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		parentActivityChangeButton();
//		parentActivityChangeTitle();
		updateTitlebar();
		view = LayoutInflater.from(context).inflate(R.layout.tops_fragment, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		startLoading();
		getTopListWhenActive();
//		registerReceivers();

		return view;
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("TOP100");
		TabWebActivity.currentMenuItem = "TOP100";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
	}

	private void getTopListWhenActive() {
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

	@Override
	public void onResume() {
		// listViewGetFormerPosition();
		WatchDog.currentSelfReloader = this;
		fragmentIsActive = true;
		super.onResume();
	}

	private void startLoading() {
		Log.e(TAG, "startLoading()");
		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);

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

	private void listViewGetFormerPosition() {
		if (firstVisibleItemPosition != -1) {
			xlvTops.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
		}
	}

	// private void parentActivityChangeTitle() {
	// TabWebActivity.tvTitle.setText("TOP100");
	// TabWebActivity.currentFragment = "TOP100";
	// // TabWebActivity.slidingMenuInitOk = false;
	// }
	//
	// private void parentActivityChangeButton() {
	// TabWebActivity.changeButton("btnMenu");
	// }

	private void getTopsList() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				tops = new HttpGetter(context).getTopsList();

				if (tops != null && tops.size() != 0) {
					adapter.setColumns(tops);
					// handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);

					// 下载每个子栏目的专辑列表
					for (Column botique : tops) {
						getTopAlbumList(botique);
					}

					// WatchDog.topsDataGot = true;
				}
				// else {
				// handler.sendEmptyMessage(MSG_NO_DATA);
				// }
			}
		});

	}

	protected void getTopAlbumList(final Column botique) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(botique, Constant.COLUMN_ALBUMS_4_TOPS);

				ColumnDetail botiqueDetail = new JsonUtil().getColumnDetail(json);
				botique.setDetail(botiqueDetail);

				// 拿到栏目专辑列表后刷新界面
				handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);

				/*
				 * // 下载每张专辑的封面图片 for (int i = 0; i <
				 * botiqueDetail.getAlbums().size(); i++) { //
				 * botiqueDetail.getAlbums
				 * ().get(i).setCoverBitmap(Constant.albumCover);
				 * downloadImage(botiqueDetail.getAlbums().get(i)); }
				 */
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

						if (bitmap != null && !bitmap.isRecycled()) {
							album.setBitmap(new SoftReference<Bitmap>(bitmap));
							handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);
						}

					}
				});

				// 得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);
				}

				bitmap = null;
				// Looper.loop();
			}
		});

	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);

		// 初始化expandableListView
		xlvTops = (ExpandableListView) view.findViewById(R.id.xlv_tops);
		initExpandableListView();
	}

	@Override
	public void onDetach() {
//		unregisterReceivers();
		if (tops != null && tops.size() != 0) {
			recycleBitmaps();
		}
		super.onDetach();
	}

	private void registerReceivers() {
		context.registerReceiver(fitTopsTitleReceier, new IntentFilter("fitTopsTitleReceier"));
	}

	private void initArguments() {

	}

	private void initExpandableListView() {
		xlvTops.setGroupIndicator(null);
		xlvTops.setItemsCanFocus(true);
		xlvTops.setOnGroupClickListener(null);
		adapter = new ColumnsListAdapter(getActivity(), tops, xlvTops, this);
		xlvTops.setAdapter(adapter);

		xlvTops.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true){
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				firstVisibleItemPosition = xlvTops.getFirstVisiblePosition();
				lastVisibleItemPosition = xlvTops.getLastVisiblePosition();
				super.onScrollStateChanged(view, scrollState);
			}
			
		});
/*		xlvTops.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				firstVisibleItemPosition = xlvTops.getFirstVisiblePosition();
				lastVisibleItemPosition = xlvTops.getLastVisiblePosition();
				if (xlvTops.getChildAt(0) != null) {
					scrollTop = xlvTops.getChildAt(0).getTop();
				}

				paintAlbumsWithinSightNFreeOthers();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});*/
	}

	protected void paintAlbumsWithinSightNFreeOthers() {
		for (int i = 0; i < tops.size(); i++) {
			if (2 * i + 1 >= firstVisibleItemPosition && 2 * i - 1 <= lastVisibleItemPosition) {
				downloadColumnImage(tops.get(i));
			} else {
				FreeColumnImage(tops.get(i));
			}
		}
	}

	private void FreeColumnImage(Column column) {
		if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
			return;
		}
		for (Album album : column.getDetail().getAlbums()) {// null pointer
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
		if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
			return;
		}
		for (Album album : column.getDetail().getAlbums()) {
			downloadImage(album);
		}
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(fitTopsTitleReceier);
	}

	@Override
	public void letsSeeHeaven() {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordCurrentDataAndPosition() {
		WatchDog.columnsInTops = tops;
		WatchDog.fvipInTops = firstVisibleItemPosition;
		// WatchDog.lvipInBotiques = lastVisibleItemPosition;
		WatchDog.stInTops = scrollTop;
		WatchDog.flagInTops = true;
	}

	@Override
	public void getSavedDataAndPosition() {
		tops = WatchDog.columnsInTops;
		firstVisibleItemPosition = WatchDog.fvipInTops;
		scrollTop = WatchDog.stInTops;

		handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);
		WatchDog.flagInTops = false;
		WatchDog.columnsInTops = null;
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
		for (Column column : tops) {
			if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
				continue;
			}
			for (Album album : column.getDetail().getAlbums()) {
				album.recyleBitmap();
			}
		}
	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(false);
		WatchDog.tabWebFragment.setTitle("TOP100");
		WatchDog.tabWebFragment.currentMenuItem = "TOP100";
	}

	@Override
	public void reload() {
		System.out.println(TAG+"reloading...");
		startLoading();
		getTopListWhenActive();
	}

	@Override
	public void onDataLoadFailed() {
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

}
