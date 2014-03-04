package com.kitty.poclient.fragment.store;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.kitty.poclient.R;
import com.kitty.poclient.adapter.CommonGridViewAdapter;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.ViewFactory;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.ColumnDetail;
import com.kitty.poclient.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.kitty.poclient.http.HttpGetter;
import com.kitty.poclient.interfaces.NobleMan;
import com.kitty.poclient.interfaces.SelfReloader;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.AnimUtil;
import com.kitty.poclient.util.JsonUtil;

//notifyData,Looper.prepare,null",Log.e
public class ColumnDetailFragment extends Fragment implements NobleMan, TitlebarUpdateFragment,SelfReloader {

	private final String TAG = "ColumnDetailFragment: ";
	private Context context;

	/*
	 * 控件
	 */
	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private LinearLayout llDataLoadFailed;
	private RelativeLayout rlContent;
	private GridView gvAlbums;
	private LinearLayout llLoadingMore;
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

	/* 新的图片加载类 */
	private ImageLoader loader;
	private DisplayImageOptions options;

	/*
	 * 刷新界面
	 */
	private final int MSG_IMG_GOT = 0;
	private final int MSG_ALBUMS_GOT = 1;
	private final int MSG_DATA_LOAD_FAILD = 2;
//	private final int MSG_SHOW_LOADING_MORE = 3;
//	private final int MSG_HIDE_LOADING_MORE = 4;

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
					printAlbums(adapter.getAlbums());
					adapter.notifyDataSetChanged();

					gvAlbums.setSelection(firstVisibleItemPosition);
					// loadAlbumCovers();
					// paintAlbumsWithinSightNFreeOthers();
					hideLoadingAnimation();
				} else if (albums != null && albums.size() == 0) {
					showNoData();
				}
				isGettingData = false;

				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowDataLoadFailed();
				break;
				
//			case MSG_SHOW_LOADING_MORE:
//				llLoadingMore.setVisibility(View.VISIBLE);
//				break;
//				
//			case MSG_HIDE_LOADING_MORE:
//				llLoadingMore.setVisibility(View.GONE);
//				break;
			}
			super.handleMessage(msg);
		}
	};

//	private BroadcastReceiver fitBotiqueDetailTitleReceier = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			parentActivityChangeTitle();
//			parentActivityChangeButton("btnBack");
//		}
//	};

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
		Log.e("BUG953",TAG+"showNoData");
		
		llLoading.setVisibility(View.GONE);
		rlContent.setVisibility(View.GONE);
//		gvAlbums.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);
	}
	
	protected void uiShowDataLoadFailed() {
		Log.e("BUG953",TAG+"uiShowDataLoadFailed");

		rlContent.setVisibility(View.GONE);
//		gvAlbums.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.VISIBLE);
		
		View loadFailureView=new ViewFactory().createLoadFailureView(this);
		llDataLoadFailed.removeAllViews();
		llDataLoadFailed.addView(loadFailureView);
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
		updateTitlebar();
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.column_detail, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		initGridView();
		initLoadingmoreAnim();
		startLoading();
		if (WatchDog.flagInColumnDetail == true) {
			getSavedDataAndPosition();
		} else {
			getData(0, Constant.COLUMN_DETAIL_ITEMS_COUNT_PER_PAGE);
		}
		// registerReceivers();
		return view;
	}

	private void initLoadingmoreAnim() {	
		ImageView ivAnim=(ImageView) llLoadingMore.findViewById(R.id.iv_loading_more);		
		new AnimUtil(context).initLoadingmoreAnim(ivAnim);
		
//		AnimationDrawable ad=(AnimationDrawable)getResources().getDrawable(R.anim.login_seraching_anim); 
//		ivAnim.setBackgroundDrawable(ad); 		
//		ad.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		// ((TabWebActivity)context).checkSlidingHidden();
		WatchDog.currentSelfReloader = this;
	}

	private void startLoading() {
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);
		rlContent.setVisibility(View.GONE);
//		gvAlbums.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.GONE);		
		
		AnimationDrawable ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();
	}

	protected void endLoading() {
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.GONE);
		rlContent.setVisibility(View.VISIBLE);
//		gvAlbums.setVisibility(View.VISIBLE);
	}

//	private void parentActivityChangeTitle() {
//		TabWebActivity.tvTitle.setText(columnName);
//		// TabWebActivity.currentFragment = "精品聚焦-精品详情";
//	}
//
//	private void parentActivityChangeButton(String which) {
//		TabWebActivity.changeButton(which);
//	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		llDataLoadFailed = (LinearLayout) view.findViewById(R.id.ll_dataload_failed);
		rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
		llLoadingMore=(LinearLayout) rlContent.findViewById(R.id.ll_loading_anim);
	}

	private void initGridView() {
		llLoadingMore.setVisibility(View.GONE);
		
		gvAlbums = (GridView) view.findViewById(R.id.gv_albums);
		adapter = new CommonGridViewAdapter(context, albums, this);
		adapter.setAlbums(albums);
		gvAlbums.setAdapter(adapter);

		gvAlbums.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true) {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {//如果当前页已见底
					
					//或获取更多数据
					if (columnDetail != null && albums.size() < columnDetail.getTotal() && !isGettingData) {
						//显示加载动画
						showLoadingAnimation();
						getData(albums.size(), 30);
						isGettingData = true;
					} 
					
					//或标记加载已完毕
					else if (columnDetail != null && albums.size() >= columnDetail.getTotal()) {
						if (!dataLoadFinished) {
							dataLoadFinished = true;
						}
					}
				}
				super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}

			
			@Override//卷动状态发生变化（滑动中-停止中-甩动中）
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {//如果当前状态为停止中
					
					//获取可见头，可见尾
					firstVisibleItemPosition = gvAlbums.getFirstVisiblePosition();
					lastVisibleItemPosition = gvAlbums.getLastVisiblePosition();
					Log.e(TAG, "view.getLastVisiblePosition()=" + view.getLastVisiblePosition());
					Log.e(TAG, "columnDetail.getTotal()=" + columnDetail.getTotal());
					
					//如果可见尾等于数据总长度，提示“加载完成”
					if (lastVisibleItemPosition >= columnDetail.getTotal() - 1) {
						showDataLoadFinishedMsg();
					}
				}
				super.onScrollStateChanged(view, scrollState);
			}

		});

	}

	protected void showLoadingAnimation() {
		llLoadingMore.setVisibility(View.VISIBLE);
	}
	
	protected void hideLoadingAnimation() {
		// 强制用户多观赏一会动画
		handler.postDelayed(new Runnable() {		
			@Override
			public void run() {
				llLoadingMore.setVisibility(View.GONE);
			}
		}, 500);
	}

	protected void showDataLoadFinishedMsg() {
		UpnpApp.mainHandler.showInfo(R.string.data_load_finished);
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
				Log.e("BUG953", TAG+"json="+json);
				
				if (!(new JsonUtil().validate(json))) {
					return;
				}else{
					columnDetail = new JsonUtil().getColumnDetail(json);
					if (startItem == 0) {
						albums.clear();
					}
					albums.addAll(columnDetail.getAlbums());

					handler.sendEmptyMessage(MSG_ALBUMS_GOT);
				}
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
//		context.registerReceiver(fitBotiqueDetailTitleReceier, new IntentFilter("fitBotiqueDetailTitleReceier"));
		// context.registerReceiver(collectTheFuckingGarbageReceiver, new
		// IntentFilter("collectTheFuckingGarbageReceiver"));
	}

	private void unregisterReceivers() {
		// context.unregisterReceiver(fitBotiqueDetailTitleReceier);
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
		adapter.setAlbums(albums);
		printAlbums(albums);
		firstVisibleItemPosition = WatchDog.fvipInColumnDetail;
		lastVisibleItemPosition = WatchDog.lvipInColumnDetail;

		handler.sendEmptyMessage(MSG_ALBUMS_GOT);
		WatchDog.flagInColumnDetail = false;
		WatchDog.albumsInColumnDetail = null;// ?
	}

	private void printAlbums(List<Album> _albums) {
		for (Album album : _albums) {
			Log.e(TAG, album.getName());
		}
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

	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(true);
		WatchDog.tabWebFragment.setTitle(columnName);
	}

	@Override
	public void onDataLoadFailed() {
		Log.e("BUG953", TAG+"onDataLoadFailed");
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

	@Override
	public void reload() {
		Log.e("BUG953", TAG+"reload");
		startLoading();
		getData(0, Constant.COLUMN_DETAIL_ITEMS_COUNT_PER_PAGE);
	}

}
