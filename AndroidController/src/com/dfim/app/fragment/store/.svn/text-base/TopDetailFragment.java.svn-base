package com.dfim.app.fragment.store;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.ViewFactory;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.http.HttpGetter;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.interfaces.SelfReloader;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.AnimUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.PowerfulBigMan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.ColumnDetail;

//notifyData,Looper.prepare,null",Log.e
public class TopDetailFragment extends Fragment implements NobleMan, TitlebarUpdateFragment, SelfReloader {

	private final String TAG = "ColumnDetailFragment139: ";
	private Context context;

	/*
	 * 控件
	 */
	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private LinearLayout llDataLoadFailed;
	private RelativeLayout rlContent;
	private ListView lvAlbums;
	private LinearLayout llLoadingMore;
	private TopAlbumListAdapter adapter;

	/*
	 * 数据
	 */
	public ColumnDetail columnDetail;
	private List<Album> albums = new ArrayList<Album>();
	private String columnName = "";
	private long columnId = -1L;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 14;// 记录停止卷动时最后一个ITEM的序号
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量
	
	
	private boolean isGettingMore = false;
	private boolean isGettingData = false;
	private boolean dataLoadFinished = false;

	private static final int ALBUMCOVER_FADEIN_INTERVAL_MILLIS = 1000;
	/*
	 * 刷新界面
	 */
	private final int MSG_IMG_GOT = 0;
	private final int MSG_ALBUMS_GOT = 1;
	private final int MSG_DATA_LOAD_FAILD = 2;

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
					// printAlbums(adapter.getDataAlbums());
					adapter.notifyDataSetChanged();

					if (!isGettingMore) {
						lvAlbums.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
					} else {
						isGettingMore = false;
					}
					
					hideLoadingAnimation();
				} else if (albums != null && albums.size() == 0) {
					showNoData();
				}
				isGettingData = false;

				break;

			case MSG_DATA_LOAD_FAILD:
				uiShowDataLoadFailed();
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

	public TopDetailFragment() {

	}

	public void letsSeeHeaven() {
		for (int i = 6; i < albums.size(); i++) {
			albums.get(i).recyleBitmap();
		}
		albums = null;
		columnDetail = null;
	}

	protected void showNoData() {
		Log.e("BUG953", TAG + "showNoData");

		llLoading.setVisibility(View.GONE);
		rlContent.setVisibility(View.GONE);
		// gvAlbums.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);
	}

	protected void uiShowDataLoadFailed() {
		Log.e("BUG953", TAG + "uiShowDataLoadFailed");

		rlContent.setVisibility(View.GONE);
		// gvAlbums.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
		llDataLoadFailed.setVisibility(View.VISIBLE);

		View loadFailureView = new ViewFactory().createLoadFailureView(this);
		llDataLoadFailed.removeAllViews();
		llDataLoadFailed.addView(loadFailureView);
	}

	public TopDetailFragment(Context context, ColumnDetail botiqueDetail, long botiqueId, String botiqueName) {
		this.context = context;
		this.columnId = botiqueId;
		this.columnName = botiqueName;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView()");

		updateTitlebar();
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.top_detail, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		initLvAlbums();
		initLoadingmoreAnim();
		startLoading();

		if (WatchDog.flagInTopDetail == true) {
			getSavedDataAndPosition();
		} else {
			getData(0, Constant.COLUMN_DETAIL_ITEMS_COUNT_PER_PAGE);
		}

		return view;
	}

	private void initLoadingmoreAnim() {
		ImageView ivAnim = (ImageView) llLoadingMore.findViewById(R.id.iv_loading_more);
		new AnimUtil(context).initLoadingmoreAnim(ivAnim);
	}

	@Override
	public void onResume() {
		super.onResume();
		WatchDog.currentSelfReloader = this;
	}

	private void startLoading() {
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);
		rlContent.setVisibility(View.GONE);
		// gvAlbums.setVisibility(View.GONE);
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
		// gvAlbums.setVisibility(View.VISIBLE);
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText(columnName);
		// TabWebActivity.currentFragment = "精品聚焦-精品详情";
	}

	private void parentActivityChangeButton(String which) {
		TabWebActivity.changeButton(which);
	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		llDataLoadFailed = (LinearLayout) view.findViewById(R.id.ll_dataload_failed);
		rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
		llLoadingMore = (LinearLayout) rlContent.findViewById(R.id.ll_loading_anim);
	}

	private void initLvAlbums() {
		lvAlbums = (ListView) view.findViewById(R.id.lv_albums);
		adapter = new TopAlbumListAdapter();
		adapter.setDataAlbums(albums);
		lvAlbums.setAdapter(adapter);

		lvAlbums.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true) {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {// 如果当前页已见底

					// 或获取更多数据
					if (columnDetail != null && albums.size() < columnDetail.getTotal() && !isGettingData) {
						// 显示加载动画
						showLoadingAnimation();
						getData(albums.size(), 30);
						
						isGettingData = true;
						isGettingMore = true;
					}

					// 或标记加载已完毕
					else if (columnDetail != null && albums.size() >= columnDetail.getTotal()) {
						if (!dataLoadFinished) {
							dataLoadFinished = true;
						}
					}
				}
				super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}

			@Override
			// 卷动状态发生变化（滑动中-停止中-甩动中）
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {// 如果当前状态为停止中

					// 获取可见头，可见尾
					firstVisibleItemPosition = lvAlbums.getFirstVisiblePosition();
					lastVisibleItemPosition = lvAlbums.getLastVisiblePosition();
					if (lvAlbums.getChildAt(0) != null) {
						scrollTop = lvAlbums.getChildAt(0).getTop();
					}

					// 如果可见尾等于数据总长度，提示“加载完成”
					if (lastVisibleItemPosition >= columnDetail.getTotal() - 1) {
						showDataLoadFinishedMsg();
					}
				}
				super.onScrollStateChanged(view, scrollState);
			}

		});

		lvAlbums.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}
				
				recordCurrentDataAndPosition();//在跳转之前记录数据和位置
				
				Album album=albums.get(position);
				WatchDog.tabWebFragment.showAlbumContent(album.getId(), album.getName(), album.getImgUrl());				
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
		private ImageView ivAlbumCover;

		Holder(View convertView) {
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_album_artist);
			ivAlbumCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
		}
	}

	public void getData(final int startItem, final int maxItems) {
		// 开线程获取数据
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(columnId, columnName, startItem, maxItems);

				if (!(new JsonUtil().validate(json))) {
					return;
				} else {
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
		context.registerReceiver(fitBotiqueDetailTitleReceier, new IntentFilter("fitBotiqueDetailTitleReceier"));
	}

	private void unregisterReceivers() {

	}

	public void recordCurrentDataAndPosition() {
		WatchDog.albumsInTopDetail = albums;
		WatchDog.fvipInTopDetail = firstVisibleItemPosition;
		WatchDog.lvipInTopDetail = lastVisibleItemPosition;
		WatchDog.stInTopDetail = scrollTop;
		WatchDog.flagInTopDetail = true;
	}

	public void getSavedDataAndPosition() {
		albums = WatchDog.albumsInTopDetail;
		adapter.setDataAlbums(albums);
//		printAlbums(albums);
		firstVisibleItemPosition = WatchDog.fvipInTopDetail;
		lastVisibleItemPosition = WatchDog.lvipInTopDetail;
		scrollTop = WatchDog.stInTopDetail;

		handler.sendEmptyMessage(MSG_ALBUMS_GOT);
		WatchDog.flagInTopDetail = false;
		WatchDog.albumsInTopDetail = null;// ?
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
		Log.e("BUG953", TAG + "onDataLoadFailed");
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

	@Override
	public void reload() {
		Log.e("BUG953", TAG + "reload");
		startLoading();
		getData(0, Constant.COLUMN_DETAIL_ITEMS_COUNT_PER_PAGE);
	}

	class TopAlbumListAdapter extends BaseAdapter {

		private List<Album> dataAlbums;
		private ImageLoader loader;
		private DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener;

		private int color1, color2, colorOther;

		public TopAlbumListAdapter() {
			dataAlbums = new ArrayList<Album>();

			loader = ImageLoader.getInstance();
			options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.pic).cacheInMemory(true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
			animateFirstListener = new MyImageLoadingListener();

			color1 = getResources().getColor(R.color.top_1_textcolor);
			color2 = getResources().getColor(R.color.top_2_textcolor);
			colorOther = getResources().getColor(R.color.top_other_textcolor);
		}

		public List<Album> getDataAlbums() {
			return dataAlbums;
		}

		public void setDataAlbums(List<Album> dataAlbums) {
			this.dataAlbums = dataAlbums;
		}

		@Override
		public int getCount() {
			return dataAlbums.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;

			if (convertView == null || convertView.getTag() == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.top_detail_item, null);
				holder = new Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			// 设置排名字体颜色
			switch (position) {
			case 0:
				holder.tvNum.setTextColor(color1);
				break;
			case 1:
				holder.tvNum.setTextColor(color2);
				break;
			case 2:
				holder.tvNum.setTextColor(color2);
				break;
			default:
				holder.tvNum.setTextColor(colorOther);
				break;
			}

			// 设置文本
			holder.tvNum.setText((position + 1) + "");
			holder.tvName.setText(dataAlbums.get(position).getName());
			holder.tvArtist.setText(dataAlbums.get(position).getArtistli().get(0).getName());

			// 设置图片
			String imgUrl = dataAlbums.get(position).getImgUrl();
			loader.displayImage(imgUrl, holder.ivAlbumCover, options, animateFirstListener);

			return convertView;
		}

	}

	/**
	 * 使用动画
	 */
	private static class MyImageLoadingListener extends SimpleImageLoadingListener {

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 图片淡入效果
				FadeInBitmapDisplayer.animate(imageView, ALBUMCOVER_FADEIN_INTERVAL_MILLIS);
			}
		}

	}

}
