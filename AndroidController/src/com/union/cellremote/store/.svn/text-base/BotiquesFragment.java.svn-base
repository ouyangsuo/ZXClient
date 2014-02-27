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
import android.widget.TextView;

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

public class BotiquesFragment extends Fragment implements NobleMan {

	// Looper.prepare,精品聚焦

	private final String TAG = "BotiqueFragment";
	private Context context;

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

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LETS_GET_DATA:
				System.out.println("再也加载不到数据？=>lets get data");
				if (WatchDog.flagInBotiques == true) {
					getSavedDataAndPosition();
				} else {
					getBotiquesList();
				}
				break;

			case MSG_BOTIQUES_DATA_GOT:
				System.out.println("再也加载不到数据？=>data got");
				updateUI();
				break;

			case MSG_ADAPTER_DATA_SET_CHANGED:
				adapter.notifyDataSetChanged();
				break;
			}

			super.handleMessage(msg);
		}
	};

	private BroadcastReceiver fitBotiquesTitleReceier = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			parentActivityChangeTitle();
			parentActivityChangeButton();
		}
	};

	public BotiquesFragment(Context context) {
		this.context = context;
	}

	protected void updateUI() {
		if (adapter != null) {

			if (loadingRunning == true) {
				endLoading();
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

		parentActivityChangeTitle();
		parentActivityChangeButton();
		view = LayoutInflater.from(context).inflate(R.layout.botiques_fragment, null);

		initComponents();
		startLoading();
		getBotiqueListWhenActive();
		initListeners();// 暂时啥也没有
		registerReceivers();// 暂时啥也没有

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
		llBotiques.setVisibility(View.INVISIBLE);
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
		Log.e(TAG, "--endLoading()--");
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}

		llBotiques.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);

		loadingRunning = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		// getSavedDataAndPosition();
		fragmentIsActive = true;
	}

	@Override
	public void onPause() {
		// recordCurrentDataAndPosition();
		super.onPause();
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("精品聚焦");
		TabWebActivity.currentMenuItem = "精品聚焦";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void getBotiquesList() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				botiques = new HttpGetter(context).getBotiquesList();

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
		});
	}

	protected void getBotiqueAlbumList(final Column botique) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(botique, Constant.COLUMN_ALBUMS_4_BOTIQUES);

				ColumnDetail botiqueDetail = new JsonUtil().getColumnDetail(json);
				botique.setDetail(botiqueDetail);

				// 拿到栏目专辑列表后刷新界面
				handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT);

				// // 下载每张专辑的封面图片
				// for (int i = 0; i < botiqueDetail.getAlbums().size(); i++) {
				// //
				// botiqueDetail.getAlbums().get(i).setCoverBitmap(Constant.albumCover);
				// downloadImage(botiqueDetail.getAlbums().get(i));
				// }

			}
		});
	}

	// protected void downloadImage(final Album album) {
	// Pools.executorService2.submit(new Runnable() {
	// @Override
	// public void run() {
	// // Looper.prepare();
	// String imageKey = album.getImgUrl() + "250";
	//
	// Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey,
	// album.getImgUrl(), 250, false, new ImageCallBack() {
	// @Override
	// public void imageLoaded(Bitmap bitmap) {
	// // 得到专辑封面后刷新界面
	// album.setCoverBitmap(new SoftReference<Bitmap>(bitmap));
	// handler.post(new Runnable() {
	// @Override
	// public void run() {
	// if (adapter != null) {
	// // if (loadingRunning == true) {
	// // endLoading();
	// // }
	// adapter.notifyDataSetChanged();
	// for (int i = 0; i < adapter.getGroupCount(); i++) {
	// xlvBotiques.expandGroup(i);
	// }
	// }
	// }
	// });
	// }
	// });
	//
	// // 得到封面后刷新界面
	// if (bitmap != null) {
	// album.setCoverBitmap(new SoftReference<Bitmap>(bitmap));
	// handler.sendEmptyMessage(MSG_BOTIQUES_DATA_GOT);
	// }
	//
	// bitmap = null;
	// // Looper.loop();
	// }
	// });
	//
	// }

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

		if (botiques.size() != 0) {
			recycleBitmaps();
		}
		unregisterReceivers();
		// letsSeeHeaven();

		super.onDetach();
	}

	public void recycleBitmaps() {
		for (Column column : botiques) {
			if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
				continue;
			}
			for (Album album : column.getDetail().getAlbums()) {
				album.recyleBitmap();
			}
		}
	}

	private void registerReceivers() {
		context.registerReceiver(fitBotiquesTitleReceier, new IntentFilter("fitBotiquesTitleReceier"));
	}

	private void initExpandableListView() {
		// 初始化expandableListView
		xlvBotiques = (ExpandableListView) view.findViewById(R.id.xlv_botiques);
		xlvBotiques.setGroupIndicator(null);
		xlvBotiques.setItemsCanFocus(true);
		xlvBotiques.setOnGroupClickListener(null);
		adapter = new ColumnsListAdapter(context, botiques, xlvBotiques, this);
		xlvBotiques.setAdapter(adapter);

		xlvBotiques.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					firstVisibleItemPosition = xlvBotiques.getFirstVisiblePosition();
					lastVisibleItemPosition = xlvBotiques.getLastVisiblePosition();
					if (xlvBotiques.getChildAt(0) != null) {
						scrollTop = xlvBotiques.getChildAt(0).getTop();
					}

					// adapter.notifyDataSetChanged();
					paintAlbumsWithinSightNFreeOthers();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

	protected void paintAlbumsWithinSightNFreeOthers() {
		for (int i = 0; i < botiques.size(); i++) {
			if (2 * i + 1 >= firstVisibleItemPosition && 2 * i - 1 <= lastVisibleItemPosition) {
				downloadColumnImage(botiques.get(i));
			} else {
				FreeColumnImage(botiques.get(i));
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
		if (column.getDetail() == null || column.getDetail().getAlbums() == null) {
			return;
		}
		for (Album album : column.getDetail().getAlbums()) {
			downloadImage(album);
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
						if (bitmap != null && !bitmap.isRecycled()) {
							album.setBitmap(new SoftReference<Bitmap>(bitmap));
							handler.sendEmptyMessage(MSG_ADAPTER_DATA_SET_CHANGED);
						}
					}
				});

				// 从缓存中得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_ADAPTER_DATA_SET_CHANGED);
				}

				bitmap = null;
				// Looper.loop();
			}
		});

	}

	private void unregisterReceivers() {
		context.unregisterReceiver(fitBotiquesTitleReceier);
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

}
