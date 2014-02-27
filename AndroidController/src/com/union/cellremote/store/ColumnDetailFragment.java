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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.union.cellremote.R;
import com.union.cellremote.adapter.CommonGridViewAdapter;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.ColumnDetail;
import com.union.cellremote.http.HttpGetter;

//notifyData,Looper.prepare,null"
public class ColumnDetailFragment extends Fragment implements NobleMan {

	private final String TAG = "BotiqueDetailFragment: ";
	private Context context;

	/*
	 * 控件
	 */
	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private GridView gvAlbums;
	private CommonGridViewAdapter adapter;

	/*
	 * 数据
	 */
	public ColumnDetail columnDetail;
	private List<Album> albums = new ArrayList<Album>();
	private String columnName = "";
	private long columnId = -1L;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 14;// 记录停止卷动时最后一个ITEM的序号
	// private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量
	private boolean isGettingData = false;
	private boolean dataLoadFinished = false;

	/*
	 * 刷新界面
	 */
	private final int MSG_IMG_GOT = 0;
	private final int MSG_ALBUMS_GOT = 1;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_IMG_GOT:
				if (adapter != null) {
					// endLoading();
					adapter.notifyDataSetChanged();
				}
				break;

			case MSG_ALBUMS_GOT:
				if (albums != null && albums.size() != 0) {
					endLoading();
					adapter.notifyDataSetChanged();

					gvAlbums.setSelection(firstVisibleItemPosition);
					// loadAlbumCovers();
					paintAlbumsWithinSightNFreeOthers();
				} else {
					showNoData();
				}
				isGettingData = false;

				break;
			}
			super.handleMessage(msg);
		}
	};

	private BroadcastReceiver fitBotiqueDetailTitleReceier = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			parentActivityChangeTitle();
			parentActivityChangeButton("btnBack");
		}
	};

	// private BroadcastReceiver collectTheFuckingGarbageReceiver = new
	// BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// letsSeeTheHeaven();
	// }
	// };

	public ColumnDetailFragment() {

	}

	public void letsSeeHeaven() {
		for (int i = 6; i < albums.size(); i++) {
			albums.get(i).recyleBitmap();
		}
		albums = null;
		columnDetail = null;
	}

	protected void showNoData() {
		llLoading.setVisibility(View.GONE);
		gvAlbums.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);
	}

	public ColumnDetailFragment(Context context, ColumnDetail botiqueDetail, long botiqueId, String botiqueName) {
		this.context = context;
		this.columnId = botiqueId;
		this.columnName = botiqueName;

		// if (botiqueDetail != null) {
		// this.botiqueDetail = botiqueDetail;
		// this.albums = botiqueDetail.getAlbums();
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentActivityChangeButton("btnBack");
		parentActivityChangeTitle();
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.column_detail, null);

		initComponents();
		startLoading();
		if (WatchDog.flagInColumnDetail == true) {
			getSavedDataAndPosition();
		} else {
			getData(0, 30);
		}
		registerReceivers();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// ((TabWebActivity)context).checkSlidingHidden();
	}

	private void startLoading() {
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);
		gvAlbums.setVisibility(View.GONE);
		AnimationDrawable ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();
	}

	protected void endLoading() {
		llLoading.setVisibility(View.GONE);
		gvAlbums.setVisibility(View.VISIBLE);
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText(columnName);
		// TabWebActivity.currentFragment = "精品聚焦-精品详情";
	}

	private void loadAlbumCovers() {
		for (Album album : albums) {
			// album.setCoverBitmap(Constant.albumCover);
			if (album.getBitmap() == null || album.getBitmap().equals(Constant.albumCover)) {
				downloadImage(album);
			}
		}
	}

	protected void downloadImage(final Album album) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = album.getImgUrl() + "150";
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, album.getImgUrl(), 150, false, new ImageCallBack() {

					// 下载得到专辑封面后刷新界面
					@Override
					public void imageLoaded(Bitmap bitmap) {
						album.setBitmap(new SoftReference<Bitmap>(bitmap));
						handler.sendEmptyMessage(MSG_IMG_GOT);
					}
				});

				// 从缓存中得到封面后刷新界面
				if (bitmap != null) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_IMG_GOT);
				}

				bitmap = null;
				// Looper.loop();
			}
		});

	}

	private void parentActivityChangeButton(String which) {
		TabWebActivity.changeButton(which);
	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);

		initGridView();
	}

	private void initGridView() {
		gvAlbums = (GridView) view.findViewById(R.id.gv_albums);
		adapter = new CommonGridViewAdapter(context, albums, this);
		adapter.setAlbums(albums);
		gvAlbums.setAdapter(adapter);

		gvAlbums.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					/* 将当前界面内的专辑封面设置为实际图片，其余使用默认图 */
					firstVisibleItemPosition = gvAlbums.getFirstVisiblePosition();
					lastVisibleItemPosition = gvAlbums.getLastVisiblePosition();
					// if (gvAlbums.getChildAt(0) != null) {
					// scrollTop = gvAlbums.getChildAt(0).getTop();
					// }

					paintAlbumsWithinSightNFreeOthers();
					break;

				case SCROLL_STATE_TOUCH_SCROLL:
					if (dataLoadFinished && lastVisibleItemPosition >= albums.size() - 1) {
//						CustomToast.makeText(context, "加载完成", Toast.LENGTH_SHORT).show();
						UpnpApp.mainHandler.showInfo(R.string.loading_complete_info);
					}
					break;

				case SCROLL_STATE_FLING:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {
					if (columnDetail != null && albums.size() < columnDetail.getTotal() && !isGettingData) {
						getData(albums.size(), 30);
						isGettingData = true;

					} else if (columnDetail != null && albums.size() >= columnDetail.getTotal()) {
						if (!dataLoadFinished) {
							// CustomToast.makeText(context, "加载完成",
							// Toast.LENGTH_SHORT).show();
							dataLoadFinished = true;
						}
					}
				}
			}

		});
	}

	protected void paintAlbumsWithinSightNFreeOthers() {
		for (int i = 0; i < albums.size(); i++) {
			if (i >= firstVisibleItemPosition && i <= lastVisibleItemPosition) {
				// getAlbumBitmap(albums.get(i));
				downloadImage(albums.get(i));
			} else {
				Bitmap bitmap = albums.get(i).getBitmap();
				if (bitmap != null && !bitmap.equals(Constant.albumCover)) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}

	class Holder {
		private TextView tvNum;
		private TextView tvName;
		private TextView tvArtist;

		Holder(View convertView) {
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
		}
	}

	public void getData(final int startItem, final int maxItems) {
		// 开线程获取数据
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(columnId, columnName, startItem, maxItems);
				columnDetail = new JsonUtil().getColumnDetail(json);
				if (startItem == 0) {
					albums.clear();
				}
				albums.addAll(columnDetail.getAlbums());

				handler.sendEmptyMessage(MSG_ALBUMS_GOT);
			}
		});
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		recycleBitmaps();
		super.onDetach();
	}

	private void registerReceivers() {
		context.registerReceiver(fitBotiqueDetailTitleReceier, new IntentFilter("fitBotiqueDetailTitleReceier"));
		// context.registerReceiver(collectTheFuckingGarbageReceiver, new
		// IntentFilter("collectTheFuckingGarbageReceiver"));
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(fitBotiqueDetailTitleReceier);
		// context.unregisterReceiver(collectTheFuckingGarbageReceiver);
	}

	public void recordCurrentDataAndPosition() {
		WatchDog.albumsInColumnDetail = albums;
		WatchDog.fvipInColumnDetail = firstVisibleItemPosition;
		WatchDog.lvipInColumnDetail = lastVisibleItemPosition;
		WatchDog.flagInColumnDetail = true;
	}

	public void getSavedDataAndPosition() {
		albums = WatchDog.albumsInColumnDetail;
		firstVisibleItemPosition = WatchDog.fvipInColumnDetail;
		lastVisibleItemPosition = WatchDog.lvipInColumnDetail;

		handler.sendEmptyMessage(MSG_ALBUMS_GOT);
		WatchDog.flagInColumnDetail = false;
		WatchDog.albumsInColumnDetail = null;
	}

	@Override
	public int getFistVisiblePosition() {
		return -1;
	}

	@Override
	public int getLastVisiblePosition() {
		return -1;
	}

	@Override
	public void recycleBitmaps() {
		if (columnDetail == null || columnDetail.getAlbums() == null) {
			return;
		}
		for (Album album : columnDetail.getAlbums()) {
			album.recyleBitmap();
		}
	}

}
