package com.dfim.app.fragment.store;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.ViewFactory;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.interfaces.SelfReloader;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.PowerfulBigMan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.Column;
import com.union.cellremote.domain.ColumnDetail;
import com.union.cellremote.http.HttpGetter;

public class TopsFragment139 extends Fragment implements NobleMan, TitlebarUpdateFragment, SelfReloader {

	private final String TAG = "TopsFragment";
	private Context context;
	private boolean stopped = false;

	private View view;
	private AnimationDrawable ad;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llNoData;
	private ListView lvTops;
	private TopsAdapter adapter;
	private boolean fragmentIsActive = false;
	private boolean loadingRunning = false;

	private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号
	private int lastVisibleItemPosition = 5;//
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量

	private String uri = "";
	private List<Column> tops = new ArrayList<Column>();

	private final int BRUSH_START_DELAYED = 2000;
	private final int LISTVIEW_BRUSH_INTERVAL_MILLIS = 1500;
	private static final int ALBUMCOVER_FADEIN_INTERVAL_MILLIS = 1500;
	
	private final int MSG_TOPS_DATA_GOT = 0;
	private final int MSG_NO_DATA = 1;
	private final int MSG_LETS_GET_DATA = 2;
	private final int MSG_ADAPTER_DATA_SET_CHANGED = 3;
	private final int MSG_DATA_LOAD_FAILD = 4;
	private final int MSG_BRUSH_LIST = 5;

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
				getTopsList();
				break;

			case MSG_ADAPTER_DATA_SET_CHANGED:
				adapter.notifyDataSetChanged();
				break;

			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();

			case MSG_BRUSH_LIST:
				brushList();
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

	protected void brushList() {
		Log.e(TAG, "brushList()");

		adapter.brushCount++;
		adapter.notifyDataSetChanged();
	}

	public void uiShowNoData() {
		System.out.println(TAG + "showNoData");
		stopLoadingAnimation();

		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);

		View loadFailureView = new ViewFactory().createLoadFailureView(this);
		llNoData.removeAllViews();
		llNoData.addView(loadFailureView);
	}

	protected void updateUI() {
		if (adapter != null) {

			if (loadingRunning == true) {
				endLoading();
			}

			adapter.notifyDataSetChanged();

			// lvTops.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
			// handler.sendMessageDelayed(handler.obtainMessage(MSG_ADAPTER_DATA_SET_CHANGED),
			// 500);
		}
	}

	public TopsFragment139() {

	}

	public TopsFragment139(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView()");
		stopped = false;

		updateTitlebar();
		view = LayoutInflater.from(context).inflate(R.layout.tops_fragment_for_139, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		startLoading();
		getTopListWhenActive();

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
		
		// 3秒钟后开始列表自刷新
		handler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				startListViewBrush();
			}
		}, BRUSH_START_DELAYED);

		loadingRunning = false;
	}

	private void getTopsList() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				tops = new HttpGetter(context).getTopsList();

				if (tops != null && tops.size() != 0) {
					handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);

					// 下载每个子栏目的专辑列表
					for (Column column : tops) {
						getTopAlbumList(column);
					}
				}
			}
		});
	}

	protected void getTopAlbumList(final Column column) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(column, Constant.COLUMN_ALBUMS_4_TOPS);

				ColumnDetail columnDetail = new JsonUtil().getColumnDetail(json);
				column.setDetail(columnDetail);

				// 拿到栏目专辑列表后刷新界面
				handler.sendEmptyMessage(MSG_TOPS_DATA_GOT);
				
//				// 下载每张专辑的封面图片
//				for (int i = 0; i < columnDetail.getAlbums().size(); i++) { 
//					columnDetail.getAlbums().get(i).setCoverBitmap(Constant.albumCover);
//					downloadImage(columnDetail.getAlbums().get(i));
//				}

			}
		});
	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);

		// 初始化expandableListView
		lvTops = (ListView) view.findViewById(R.id.lv_tops);
		initTopsListView();
	}

	@Override
	public void onStop() {
		stopped = true;
		super.onStop();
	}
	
	@Override
	public void onDetach() {
		// unregisterReceivers();
		if (tops != null && tops.size() != 0) {
			recycleBitmaps();
		}
		super.onDetach();
	}

	private void initTopsListView() {
		Log.e(TAG, "initTopsListView()");

		adapter = new TopsAdapter();
		lvTops.setAdapter(adapter);

		lvTops.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				goColumnDetailFragment(position);
			}
		});
	}

	private void startListViewBrush() {
		Log.e(TAG, "startListViewBrush()");
		Log.e(TAG, "detached=" + stopped);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true && !stopped) {
					handler.sendEmptyMessage(MSG_BRUSH_LIST);

					try {
						Thread.sleep(LISTVIEW_BRUSH_INTERVAL_MILLIS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	private void goColumnDetailFragment(int position) {
		if (PowerfulBigMan.testClickInterval() == false) {
			return;
		}

		String name = tops.get(position).getName();
		long id = tops.get(position).getId();
		
		WatchDog.tabWebFragment.showTopContent(id,name);
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
		System.out.println(TAG + "reloading...");
		startLoading();
		getTopListWhenActive();
	}

	@Override
	public void onDataLoadFailed() {
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

	class TopsAdapter extends BaseAdapter {

		private ImageLoader loader;
		private DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener;  
		private boolean currentPositionBrushNeeded =false;

		public int brushCount = 0;
		
		public int drawable1 = 0;
		public int drawable2 = 1;

		public TopsAdapter() {
			loader = ImageLoader.getInstance();
			options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.pic).cacheInMemory(true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
			animateFirstListener = new MyImageLoadingListener();  
		}

		@Override
		public int getCount() {
			// return tops.size();
			return tops.size();
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;

			if (convertView == null || convertView.getTag() == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.tops_item_139, null);
				holder = new Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			try {
				Log.e(TAG, "getView():brushCount=" + brushCount + ",position=" + position + ",albumPosition=" + getAlbumPosition(position));
				String imgUrl = tops.get(position).getDetail().getAlbums().get(getAlbumPosition(position)).getImgUrl();
				if(currentPositionBrushNeeded){
					loader.displayImage(imgUrl, holder.ivAlbumCover, options,animateFirstListener);
				}else{
					loader.displayImage(imgUrl, holder.ivAlbumCover);
				}				
				
//				Drawable d1=getResources().getDrawable(R.drawable.ic_launcher);
//				Drawable d2=getResources().getDrawable(R.drawable.ic_launcher_xxbox);
//				Drawable[] layers;
//				if(brushCount%2==0){
//					layers=new Drawable[]{d1,d2};			
//				}else{
//					layers=new Drawable[]{d2,d1};	
//				}					
//				TransitionDrawable td=new TransitionDrawable(layers);
//				holder.ivAlbumCover.setImageDrawable(td);				
//				td.startTransition(3000);
				
			} catch (Exception e) {
				Log.e(TAG, "excetption using imageloader e=" + e);
				e.printStackTrace();
			}
			holder.tvRankName.setText(tops.get(position).getName());

			return convertView;
		}

		private int getAlbumPosition(int position) {
			int albumPosition = 0;

			// 刷新位置
			if (position % 7 == (brushCount - 1) % 7) {
				albumPosition = (brushCount - 1) / 7 + 1;
				currentPositionBrushNeeded = true;
			}

			// 向前的位置
			else if (position % 7 < (brushCount - 1) % 7) {
				albumPosition = (brushCount - 1) / 7 + 1;
				currentPositionBrushNeeded = false;
			}

			// 向后的位置
			else {
				albumPosition = (brushCount - 1) / 7;
				currentPositionBrushNeeded = false;
			}

			return correctAlbumPosition(albumPosition);
		}

		private int correctAlbumPosition(int albumPosition) {
			if (albumPosition >= 6) {
				albumPosition = albumPosition % 6;
			}
			return albumPosition;
		}

	}

	class Holder {
		private ImageView ivAlbumCover;
		private TextView tvRankName;

		public Holder(View convertView) {
			ivAlbumCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvRankName = (TextView) convertView.findViewById(R.id.tv_rank_name);
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
