package com.kitty.poclient.fragment.store;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.kitty.poclient.R;
import com.kitty.poclient.adapter.ColumnsListAdapter;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.ViewFactory;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Column;
import com.kitty.poclient.domain.ColumnDetail;
import com.kitty.poclient.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.kitty.poclient.http.HttpGetter;
import com.kitty.poclient.interfaces.NobleMan;
import com.kitty.poclient.interfaces.SelfReloader;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.JsonUtil;

public class BotiquesFragmentII extends Fragment implements NobleMan, TitlebarUpdateFragment,SelfReloader {

	// Looper.prepare,精品聚焦

	private final String TAG = "BotiqueFragment";

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llBotiques;
	private LinearLayout llNoData;
	private ExpandableListView xlvBotiques;
	private TextView tvNoData;
	private ColumnsListAdapter adapter;

	private AnimationDrawable ad;
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 5;//
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量

	private String uri = "";
	private List<Column> botiques = new ArrayList<Column>();

	private final int MSG_BOTIQUES_DATA_GOT = 0;
	private final int MSG_LETS_GET_DATA = 1;
	private final int MSG_ADAPTER_DATA_SET_CHANGED = 2;
	private final int MSG_DATA_LOAD_FAILD = 3;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LETS_GET_DATA:
				// if (WatchDog.flagInBotiques == true) {
				// getSavedDataAndPosition();
				// } else {
				// getBotiquesList();
				// }
				getBotiquesList();
				break;

			case MSG_BOTIQUES_DATA_GOT:
				updateUI();
				break;

			case MSG_ADAPTER_DATA_SET_CHANGED:
				adapter.notifyDataSetChanged();
				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();
				break;
			}

			super.handleMessage(msg);
		}
	};

	public BotiquesFragmentII() {
		// TODO Auto-generated constructor stub
	}

	protected void updateUI() {
		if (adapter != null) {

			if (loadingRunning == true) {
				showWebData();
			}

			adapter.notifyDataSetChanged();// 告诉适配器显示基本信息并开始下载图片
			for (int i = 0; i < adapter.getGroupCount(); i++) {
				xlvBotiques.expandGroup(i);
			}

			xlvBotiques.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
			handler.sendMessageDelayed(handler.obtainMessage(MSG_ADAPTER_DATA_SET_CHANGED), 500);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		updateTitlebar();
		view = inflater.inflate(R.layout.botiques_fragment, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		view.setBackgroundColor(getActivity().getResources().getColor(R.color.icecream_bg));

		initComponents();
		startLoading();
		getBotiqueListWhenActive();

		return view;
	}

	private void getBotiqueListWhenActive() {
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

	private void initThreadPool() {
		Pools.initCachedPool(Pools.executorService1);
	}

	private void startLoading() {
		Log.e(TAG, "startLoading()");
		llBotiques.setVisibility(View.GONE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);

		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();

		loadingRunning = true;
	}

	protected void showWebData() {
		Log.e(TAG, "--endLoading()--");
		stopLoadingAnimation();

		llBotiques.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
	}

	public void uiShowNoData() {
		System.out.println(TAG+"showNoData");
		stopLoadingAnimation();

		llBotiques.setVisibility(View.GONE);
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
		// getSavedDataAndPosition();
		WatchDog.currentSelfReloader = this;
		fragmentIsActive = true;
	}

	@Override
	public void onPause() {
		// recordCurrentDataAndPosition();
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void getBotiquesList() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				String json = new HttpGetter(getActivity()).getBotiquesListII();
				
				if (!(new JsonUtil().validate(json))) {
//					showNoData();
					return;
				}else{
					botiques = new JsonUtil().getBotiques(json);
					
					if (botiques != null && botiques.size() != 0) {
						adapter.setColumns(botiques);
						Log.e(TAG, "botiques.size()=" + botiques.size());// step1:确认收到有效数据
						// handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT);

						// 下载每个子栏目的专辑列表
						for (Column botique : botiques) {
							getBotiqueAlbumList(botique);
						}

						// WatchDog.botiquesDataGot=true;
					}
				}
			}
		});
	}

	protected void getBotiqueAlbumList(final Column botique) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(getActivity()).getColumnAlbumsList(botique, Constant.COLUMN_ALBUMS_4_BOTIQUES);
				
				ColumnDetail botiqueDetail = new JsonUtil().getColumnDetail(json);
				botique.setDetail(botiqueDetail);

				// 拿到栏目专辑列表后刷新界面
				handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT);
			}
		});
	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llBotiques = (LinearLayout) view.findViewById(R.id.ll_botiques);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);//
		tvNoData = (TextView) view.findViewById(R.id.tv_no_data);

		initExpandableListView();
	}

	private void initListeners() {

	}

	@Override
	public void onDetach() {
		Log.e(TAG, "onDetach()");

		super.onDetach();
	}

	private void initExpandableListView() {
		// 初始化expandableListView
		xlvBotiques = (ExpandableListView) view.findViewById(R.id.xlv_botiques);
		xlvBotiques.setGroupIndicator(null);
		xlvBotiques.setItemsCanFocus(true);
		xlvBotiques.setOnGroupClickListener(null);
		adapter = new ColumnsListAdapter(getActivity(), botiques, xlvBotiques, this);
		xlvBotiques.setAdapter(adapter);
		xlvBotiques.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true) {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				firstVisibleItemPosition = xlvBotiques.getFirstVisiblePosition();
				lastVisibleItemPosition = xlvBotiques.getLastVisiblePosition();
				super.onScrollStateChanged(view, scrollState);
			}

		});

		/*
		 * xlvBotiques.setOnScrollListener(new OnScrollListener() {
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { firstVisibleItemPosition =
		 * xlvBotiques.getFirstVisiblePosition(); lastVisibleItemPosition =
		 * xlvBotiques.getLastVisiblePosition(); if (xlvBotiques.getChildAt(0)
		 * != null) { scrollTop = xlvBotiques.getChildAt(0).getTop(); }
		 * 
		 * paintAlbumsWithinSightNFreeOthers(); }
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) {
		 * 
		 * } });
		 */
	}

	/*
	 * protected void paintAlbumsWithinSightNFreeOthers() { for (int i = 0; i <
	 * botiques.size(); i++) { if (2 * i + 1 >= firstVisibleItemPosition && 2 *
	 * i - 1 <= lastVisibleItemPosition) { downloadColumnImage(botiques.get(i));
	 * } else { freeColumnImage(botiques.get(i)); } } }
	 */

	private void freeColumnImage(Column column) {
		if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
			return;
		}
		for (Album album : column.getDetail().getAlbums()) {// null pointer
			freeImage(album);
		}
	}

	/*
	 * private void downloadColumnImage(Column column) { if (column.getDetail()
	 * == null || column.getDetail().getAlbums() == null) { return; } for (Album
	 * album : column.getDetail().getAlbums()) { downloadImage(album); } }
	 * 
	 * protected void downloadImage(final Album album) {
	 * Pools.executorService2.submit(new Runnable() {
	 * 
	 * @Override public void run() { // Looper.prepare(); String imageKey =
	 * album.getImgUrl() + "150";
	 * 
	 * Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey,
	 * album.getImgUrl(), 150, false, new ImageCallBack() {
	 * 
	 * @Override public void imageLoaded(Bitmap bitmap) {
	 * 
	 * if (bitmap != null && !bitmap.isRecycled()) { album.setBitmap(new
	 * SoftReference<Bitmap>(bitmap));
	 * handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT); }
	 * 
	 * } });
	 * 
	 * // 得到封面后刷新界面 if (bitmap != null && !bitmap.isRecycled()) {
	 * album.setBitmap(new SoftReference<Bitmap>(bitmap));
	 * handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT); }
	 * 
	 * bitmap = null; // Looper.loop(); } });
	 * 
	 * }
	 */

	private void freeImage(Album album) {
		Bitmap bitmap = album.getBitmap();
		if (bitmap != null && !bitmap.equals(Constant.albumCover)) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	@Override
	public void letsSeeHeaven() {
		for (Column column : botiques) {
			for (Album album : column.getDetail().getAlbums()) {
				album.recyleBitmap();
			}
		}
		botiques = null;
	}

	@Override
	public void recordCurrentDataAndPosition() {
		WatchDog.columnsInBotiques = botiques;
		WatchDog.fvipInBotiques = firstVisibleItemPosition;
		// WatchDog.lvipInBotiques = lastVisibleItemPosition;
		WatchDog.stInBotiques = scrollTop;
		WatchDog.flagInBotiques = true;
	}

	@Override
	public void getSavedDataAndPosition() {
		botiques = WatchDog.columnsInBotiques;
		firstVisibleItemPosition = WatchDog.fvipInBotiques;
		scrollTop = WatchDog.stInBotiques;

		handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT);
		WatchDog.flagInBotiques = false;
		WatchDog.columnsInBotiques = null;
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
	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(false);
		WatchDog.tabWebFragment.setTitle("精品聚焦");
		WatchDog.tabWebFragment.currentMenuItem = "精品聚焦";
	}

	@Override
	public void reload() {
		System.out.println(TAG+"reloading...");
		startLoading();
		getBotiqueListWhenActive();
	}

	@Override
	public void onDataLoadFailed() {//.showNoData()
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

}
