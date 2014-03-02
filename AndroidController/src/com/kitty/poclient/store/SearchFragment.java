package com.kitty.poclient.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.adapter.SearchHistoryListAdapter;
import com.kitty.poclient.adapter.SearchResultListAdapter;
import com.kitty.poclient.adapter.SearchResultXListAdapter;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.ViewFactory;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.dao.MusicDao;
import com.kitty.poclient.dao.SearchHistoryDao;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.domain.SearchDataObject;
import com.kitty.poclient.fragment.TabWebFragment;
import com.kitty.poclient.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.kitty.poclient.http.HttpPoster;
import com.kitty.poclient.interfaces.SelfReloader;
import com.kitty.poclient.util.ListviewDataPositionRecorder;
import com.kitty.poclient.util.MediaUtil;

public class SearchFragment extends Fragment implements TitlebarUpdateFragment,SelfReloader {
	// Looper.prepare

	private final String TAG = "SearchResultFragment: ";
	private Context context;

	private View view;
	private LinearLayout llFace;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llHistory;
	private LinearLayout llTabContent;
	private LinearLayout llNoResult;

	private ExpandableListView xlvResult;
	private ListView lvResultAlbums;
	private ListView lvResultMusics;
	private ListView lvResultArtists;
	
	// 历史记录界面
	private View historyView;
	private ListView lvHistory;
	private Button btnClearHistory;
	private SearchHistoryListAdapter historyAdapter;

	private View footerView;
	private TextView tvLoadMore;
	private LinearLayout llLoadingMore;
	private boolean isAddingData = false;

	private AnimationDrawable ad;

	private SearchResultXListAdapter adapter;
	private SearchResultListAdapter albumAdapter;
	private SearchResultListAdapter musicAdapter;
	private SearchResultListAdapter artistAdapter;

	private ListviewDataPositionRecorder lpRecorder;
	public Set<Bitmap> bitmaps = new HashSet<Bitmap>();

	private String keyword = "";
	private SearchDataObject sdo;

	private Button tabAll;
	private Button tabAlbum;
	private Button tabMusic;
	private Button tabArtist;
	private Button btnReload;
	private Button[] tabs;

	private TextView tabAllSelected;
	private TextView tabAlbumSelected;
	private TextView tabMusicSelected;
	private TextView tabArtistSelected;
	private List<TextView> tabUnderlines = new ArrayList<TextView>();

	private final int MSG_SHOW_ADDED_DATA = 0;
	private final int MSG_DATA_LOAD_FAILD = 1;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW_ADDED_DATA:
				musicAdapter.notifyDataSetChanged();

				if (isAddingData == true) {
					tvLoadMore.setVisibility(View.VISIBLE);
					llLoadingMore.setVisibility(View.GONE);
					isAddingData = false;

					checkIfDataLoadCompleted(msg.arg1);
				}
				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public void uiShowNoData() {
		System.out.println(TAG+"showNoData");
		
		llFace.setVisibility(View.VISIBLE);
		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llHistory.setVisibility(View.GONE);		

		View loadFailureView=new ViewFactory().createLoadFailureView(this);
		llFace.removeAllViews();
		llFace.addView(loadFailureView);
	}

	public SearchFragment() {

	}

	public SearchFragment(Context context) {
		this.context = context;
	}

	public SearchFragment(Context context, SearchDataObject sdo) {
		this.context = context;
		this.sdo = sdo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println(TAG + "onCreateView");

		// parentActivityChangeButton();
		// parentActivityChangeTitle();
		updateTitlebar();
		view = LayoutInflater.from(context).inflate(R.layout.search_result, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		initComponents();
		initHistoryView();
		initData();
		initBtnReload();
		initExpandableListView();
		initLvAlbums();
		initLvMusics();
		initLvArtists();		
		initTabListeners();
		initListviewOnScrollListeners();

		WatchDog.searchResultFragmentRunning = true;
		return view;
	}

	private void initListviewOnScrollListeners() {
		ListView[] listViews=new ListView[]{xlvResult,lvResultAlbums,lvResultArtists};
		lpRecorder=new ListviewDataPositionRecorder().registerListviews(listViews);
	}

	private void initHistoryView() {
		historyView=LayoutInflater.from(getActivity()).inflate(R.layout.search_history_listview, null);
		lvHistory=(ListView) historyView.findViewById(R.id.lv_search_history);
		btnClearHistory=(Button) historyView.findViewById(R.id.btn_clear_search_history);
		historyAdapter=new SearchHistoryListAdapter(getActivity());		
		lvHistory.setAdapter(historyAdapter);		
		btnClearHistory.setFocusableInTouchMode(false);
		
		btnClearHistory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("BUG956", "btnClearHistory onClick");
				clearSearchHistory();
			}
		});
		
//		btnClearHistory.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				Log.e("BUG956", "btnClearHistory onTouch:event="+event);
//				if(event.getAction()==MotionEvent.ACTION_UP){
//					clearSearchHistory();
//				}
//				return false;
//			}
//		});
		
	}

	private void initBtnReload() {
		btnReload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WatchDog.tabWebFragment.showSoftInput();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						WatchDog.tabWebFragment.hideSoftInput();
					}
				}, 100);

			}
		});
	}

	private void initData() {
		sdo = new SearchDataObject();
		sdo.setAlbums(new ArrayList<Album>());
		sdo.setMusics(new ArrayList<Music>());
		sdo.setArtists(new ArrayList<Artist>());
	}

//	private void parentActivityChangeTitle() {
//		// TabWebActivity.tvTitle.setText("演出者");
//		TabWebFragment.currentMenuItem = "搜索结果";
//		((TabWebActivity) context).useTitleStyle(TabWebFragment.TITLE_STYLE_SEARCH);
//		// ((TabWebActivity) context).initEtSearch();
//	}

	// private void parentActivityChangeButton() {
	// TabWebActivity.changeButton("btnBack");
	// // TabWebActivity.changeButton("btnMenu");
	// }

	private void initTabListeners() {
		tabAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tabOnClickListener(v);
			}
		});

		tabAlbum.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tabOnClickListener(v);
			}
		});

		tabMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tabOnClickListener(v);
			}
		});

		tabArtist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tabOnClickListener(v);
			}
		});
	}

	protected void tabOnClickListener(View v) {
		onTabSelected((Button)v);
		WatchDog.tabWebFragment.focusOnContent();
	}

	protected void onTabSelected(Button btn) {
		WatchDog.selectedSearchResultTabText = btn.getText().toString();
		System.out.println("record selectedSearchResultTabText=" + btn.getText().toString());
		for (TextView underline : tabUnderlines) {
			if (underline.getParent() == btn.getParent()) {
				underline.setVisibility(View.VISIBLE);
				showTabContent(btn);
			} else {
				underline.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void showTabContent(TextView tv) {
		if (tv == tabAll) {
			showTabAll();
		} else if (tv == tabAlbum) {
			showTabAlbum();
		} else if (tv == tabMusic) {
			showTabMusic();
		} else if (tv == tabArtist) {
			showTabArtist();
		}
	}

	private void showNoResult() {
		llTabContent.removeAllViews();
		llTabContent.addView(llNoResult);

		llNoResult.requestFocus();
		llNoResult.invalidate();
	}

	private void showTabArtist() {
		if (hasResult(sdo.getArtists())) {
			// artistAdapter.setList(sdo.getArtists());
			// artistAdapter.notifyDataSetChanged();
			// lvResultArtists.requestFocus();
			// lvResultArtists.scrollTo(0, 0);

			llTabContent.removeAllViews();
			llTabContent.addView(lvResultArtists);
			artistAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabMusic() {
		if (hasResult(sdo.getMusics())) {
			// musicAdapter.setList(sdo.getMusics());
			// musicAdapter.notifyDataSetChanged();
			// lvResultMusics.requestFocus();
			// lvResultMusics.scrollTo(0, 0);

			llTabContent.removeAllViews();
			llTabContent.addView(lvResultMusics);
			musicAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabAlbum() {
		if (hasResult(sdo.getAlbums())) {
			// albumAdapter.setList(sdo.getAlbums());
			// albumAdapter.notifyDataSetChanged();
			// lvResultAlbums.requestFocus();
			// lvResultAlbums.scrollTo(0, 0);

			llTabContent.removeAllViews();
			llTabContent.addView(lvResultAlbums);
			albumAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabAll() {
		if (hasResult(sdo.getAlbums()) || hasResult(sdo.getMusics()) || hasResult(sdo.getArtists())) {
			// adapter.setSdo(sdo);
			// adapter.notifyDataSetChanged();
			// xlvResult.requestFocus();
			// xlvResult.scrollTo(0, 0);

			llTabContent.removeAllViews();
			llTabContent.addView(xlvResult);
			adapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private boolean hasResult(List list) {
		if (list == null || list.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onResume() {
		Log.e("BUG913", TAG+" onResume");
		WatchDog.tabWebFragment.useTitleStyle(TabWebFragment.TITLE_STYLE_SEARCH);
		WatchDog.tabWebFragment.etSearch.requestFocus();

		/* 由详情页返回时拿取原来的数据和位置 */
		if (WatchDog.flagInSearchResult) {
			getSavedDataAndPosition();
			WatchDog.tabWebFragment.setEtText(WatchDog.keywordInSearchResult);
			showSearchResult();
			int i=useFormerTab();
			useFormerPosition(i);
			WatchDog.flagInSearchResult = false;
		}

		WatchDog.currentSelfReloader = this;
		super.onResume();
	}

	private void useFormerPosition(int tabIndex) {
		Log.e("BUG913", TAG+" useFormerPosition:tabIndex="+tabIndex);
		int arg0=WatchDog.listviewPositionMap.get(TAG).getFirstVisibleItemPosition();
		int arg1=WatchDog.listviewPositionMap.get(TAG).getScrollTop();
		Log.e("BUG913", TAG+" arg0="+arg0);
		Log.e("BUG913", TAG+" arg1="+arg1);

		switch (tabIndex) {
		case 0:	
//			xlvResult.setSelectionFromTop(WatchDog.fvipInSearchResult, WatchDog.stInSearchResult);
			xlvResult.setSelectionFromTop(arg0,arg1);
			break;
		case 1:		
//			lvResultAlbums.setSelectionFromTop(WatchDog.fvipInSearchResult, WatchDog.stInSearchResult);
			lvResultAlbums.setSelectionFromTop(arg0,arg1);
			break;
		case 2:			
			break;
		case 3:		
//			lvResultArtists.setSelectionFromTop(WatchDog.fvipInSearchResult, WatchDog.stInSearchResult);
			lvResultArtists.setSelectionFromTop(arg0,arg1);
			break;
		}
	}

	private int useFormerTab() {
		System.out.println("useFormerTab");

		for (int i = 0; i < tabs.length; i++) {
			System.out.println("tabs[i].getText().toString()=" + tabs[i].getText().toString());
			System.out.println("WatchDog.selectedSearchResultTabText=" + WatchDog.selectedSearchResultTabText);

			if (tabs[i].getText().toString().equals(WatchDog.selectedSearchResultTabText)) {
				System.out.println("ok show it");
				onTabSelected(tabs[i]);
				return i;
			}
		}
		return 0;
	}

	private void initComponents() {
		llFace = (LinearLayout) view.findViewById(R.id.ll_face);
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llHistory = (LinearLayout) view.findViewById(R.id.ll_history);
		llTabContent = (LinearLayout) view.findViewById(R.id.ll_search_tab_content);
		// llNoResult = (LinearLayout) view.findViewById(R.id.ll_no_result);

		btnReload = (Button) view.findViewById(R.id.btn_reload);
		tabAll = (Button) view.findViewById(R.id.tab_all);
		tabAlbum = (Button) view.findViewById(R.id.tab_album);
		tabMusic = (Button) view.findViewById(R.id.tab_music);
		tabArtist = (Button) view.findViewById(R.id.tab_artist);
		tabs = new Button[] { tabAll, tabAlbum, tabMusic, tabArtist };

		tabAllSelected = (TextView) view.findViewById(R.id.tab_all_selected);
		tabAlbumSelected = (TextView) view.findViewById(R.id.tab_album_selected);
		tabMusicSelected = (TextView) view.findViewById(R.id.tab_music_selected);
		tabArtistSelected = (TextView) view.findViewById(R.id.tab_artist_selected);

		tabUnderlines.add(tabAllSelected);
		tabUnderlines.add(tabAlbumSelected);
		tabUnderlines.add(tabMusicSelected);
		tabUnderlines.add(tabArtistSelected);

		xlvResult = (ExpandableListView) LayoutInflater.from(context).inflate(R.layout.search_all_xlistview, null);
		lvResultAlbums = (ListView) LayoutInflater.from(context).inflate(R.layout.search_album_listview, null);
		lvResultMusics = (ListView) LayoutInflater.from(context).inflate(R.layout.search_music_listview, null);
		lvResultArtists = (ListView) LayoutInflater.from(context).inflate(R.layout.search_artist_listview, null);
		llNoResult = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.search_no_result, null);

		footerView = LayoutInflater.from(context).inflate(R.layout.artists_item_footerview, null);
		tvLoadMore = (TextView) footerView.findViewById(R.id.tv_load_more);
		llLoadingMore = (LinearLayout) footerView.findViewById(R.id.ll_loading_more);

		adapter = new SearchResultXListAdapter(getActivity(), this);
		albumAdapter = new SearchResultListAdapter(getActivity(), this, 0);
		musicAdapter = new SearchResultListAdapter(getActivity(), this, 1);
		artistAdapter = new SearchResultListAdapter(getActivity(), this, 2);
	}

	public void onAlbumItemClick(int position) {
		long albumId = sdo.getAlbums().get(position).getId();
		String albumName = sdo.getAlbums().get(position).getName();
//		Bitmap albumBitmap = sdo.getAlbums().get(position).getBitmap();
		String imgUrl = sdo.getAlbums().get(position).getImgUrl();

		Intent intent = new Intent("showAlbumContentReceiver");
		intent.putExtra("albumId", albumId);
		intent.putExtra("albumName", albumName);
//		intent.putExtra("bitmap", albumBitmap);// cant draw recycled bitmaps
		intent.putExtra("imgUrl", imgUrl);
//		intent.putExtra("layout", R.id.ll_web_root);
//		recordCurrentDataAndPosition();
		// recycleNeeded = false;
		WatchDog.tabWebFragment.showAlbumContentReceiverOnReceive(intent);
	}

	public void onMusicItemClick(int position) {

		Music music = sdo.getMusics().get(position);
//		int state = getMusicStateById(music.getId());
//		String uri = "xxbox://listen?id=" + music.getId();
//		new Player().play(uri);
//		/*
//		 * 1208； 试听失败提示：“试听失败，请检查网络”； setUri成功； 信息由boxSub订阅信息返回； 让纯鹏看下；
//		 */
//
//		Intent intent = new Intent(getActivity(), WebListenActivity.class);
//		intent.putExtra("musicName", music.getName());
//		intent.putExtra("artist", music.getArtistName());
//
//		intent.putExtra("imgUrl", music.getImgUrl());
//		intent.putExtra("btnBuyText", getListenBtnText(music, state));
//		intent.putExtra("btnBuyEnabled", getListenBtnEnabled(state));
//		intent.putExtra("isFromSearch", true);
//
//		WatchDog.currentListeningMusic = music;
//		Log.e(TAG, "onMusicItemClick>>WatchDog.currentListeningMusic: "+WatchDog.currentListeningMusic.getName());
//		
//		UpnpApp.mainHandler.showInfo(R.string.store_listen_music_loading_info);
//		
//		getActivity().startActivity(intent);
		
		new MediaUtil(context).playLocally(music.getId());
	}

	private boolean getListenBtnEnabled(int state) {
		return state == -1 ? true : false;
	}

	private int getMusicStateById(Long id) {
		return new MusicDao().getMusicStateById(id);
	}

	private String getListenBtnText(Music music, int state) {
		String btnText = "";
		switch (state) {
		case 5:// 在本地
			btnText = "在本地";
			break;

		case 0:// 在云端
			btnText = "在云端";
			break;

		default:// 未购买
			String price = music.getPrice();
			if ("0".equals(price)) {
				btnText = getResources().getString(R.string.freeBtnText);
			} else {
				btnText = price + ".00 元";
			}
			break;
		}

		return btnText;
	}

	public void onArtistItemClick(int position) {
		Artist artist = sdo.getArtists().get(position);
//		recordCurrentDataAndPosition();
		// recycleNeeded = false;
		WatchDog.tabWebFragment.goArtistDetail(artist);
	}

	private void initExpandableListView() {
		adapter.setSdo(sdo);
		xlvResult.setAdapter(adapter);
		xlvResult.setGroupIndicator(null);
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			xlvResult.expandGroup(i);
		}
		
//		firstVisibleItemPosition = xlvResult.getFirstVisiblePosition();
//		xlvResult.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true){
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
//					if(xlvResult.getChildAt(0) != null){
//						scrollTop = xlvResult.getChildAt(0).getTop();
//					}
//				}
//				super.onScrollStateChanged(view, scrollState);
//			}
//			
//		});
	}

	private void initLvAlbums() {
		albumAdapter.setList(sdo.getAlbums());
		lvResultAlbums.setAdapter(albumAdapter);
		lvResultAlbums.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// UpnpApp.showToastMessage("show album detail");
				onAlbumItemClick(position);
			}
		});
	}

	private void initLvArtists() {
		artistAdapter.setList(sdo.getArtists());
		lvResultArtists.setAdapter(artistAdapter);
		lvResultArtists.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onArtistItemClick(position);
			}
		});
	}

	private void initLvMusics() {
		// lvAddFooterView(lvResultMusics, Constant.SEARCH_TYPE_MUSICS);

		musicAdapter.setList(sdo.getMusics());
		lvResultMusics.setAdapter(musicAdapter);
		lvResultMusics.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onMusicItemClick(position);
			}
		});
	}

//	private void lvAddFooterView(ListView listview, final int searchType) {
//		listview.addFooterView(footerView);
//
//		tvLoadMore.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				tvLoadMore.setVisibility(View.GONE);
//				llLoadingMore.setVisibility(View.VISIBLE);
//				isAddingData = true;
//
//				// 获取更多数据
//				getMoreData(searchType);
//			}
//		});
//	}

//	protected void getMoreData(int searchType) {
//		Pools.executorService1.submit(new Runnable() {
//
//			@Override
//			public void run() {
//				String json5 = new HttpPoster().search(keyword, Constant.SEARCH_TYPE_MUSICS, sdo.getMusics().size());
//				System.out.println("json5=" + json5);
//				List<Music> musics = new JsonUtil().getSearchDataMusics(json5);
//
//				sdo.getMusics().addAll(musics);
//
//				Message msg = handler.obtainMessage(MSG_SHOW_ADDED_DATA);
//				msg.arg1 = musics.size();
//				handler.sendMessage(msg);
//			}
//		});
//	}

	private void checkIfDataLoadCompleted(int numItems) {
		if (numItems < HttpPoster.MAX_SEARCH_ITEM) {
			tvLoadMore.setText("加载完毕");
			tvLoadMore.setEnabled(false);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		WatchDog.tabWebFragment.useTitleStyle(TabWebFragment.TITLE_STYLE_NORMAL);
	}
	
	@Override
	public void onDestroyView() {
		Log.e("BUG913", TAG+" onDestroyView");
		recordCurrentDataAndPosition();
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		Log.e("BUG913", TAG+" onDetach");
		
		WatchDog.searchResultFragmentRunning = false;
		WatchDog.tabWebFragment.hideSoftInput();
		unregisterReceivers();
		recycleAllBitmaps();
		super.onDetach();
	}

	/* 离开页面或重新搜索时，释放所有图片资源 */
	private void recycleAllBitmaps() {
		System.out.println(TAG + "recycleAllBitmaps");
		System.out.println(TAG + "bitmaps.size()=" + bitmaps.size());

		for (Bitmap bitmap : bitmaps) {
			bitmap.recycle();
		}

		bitmaps.clear();
		WatchDog.selectedSearchResultTabText = "";
	}

	private void unregisterReceivers() {

	}

	public void recordCurrentDataAndPosition() {
		Log.e("BUG913", TAG+" recordCurrentDataAndPosition");
		
		WatchDog.sdoInSearchResult = sdo;
//		WatchDog.fvipInSearchResult = firstVisibleItemPosition;
//		WatchDog.stInSearchResult = scrollTop;
		WatchDog.listviewPositionMap.put(TAG, lpRecorder);
		WatchDog.keywordInSearchResult = keyword;	
		WatchDog.flagInSearchResult = true;	
		
//		Log.e("BUG913", TAG+" WatchDog.fvipInSearchResult="+WatchDog.fvipInSearchResult);
//		Log.e("BUG913", TAG+" WatchDog.stInSearchResult="+WatchDog.stInSearchResult);
		Log.e("BUG913", TAG+" lpRecorder="+lpRecorder);
	}

	public void getSavedDataAndPosition() {
		Log.e("BUG913", TAG+" getSavedDataAndPosition");
		
		sdo = WatchDog.sdoInSearchResult;
//		firstVisibleItemPosition = WatchDog.fvipInSearchResult;
//		scrollTop = WatchDog.stInSearchResult;		
//		Log.e("BUG913", TAG+" WatchDog.fvipInSearchResult="+WatchDog.fvipInSearchResult);
//		Log.e("BUG913", TAG+" WatchDog.stInSearchResult="+WatchDog.stInSearchResult);
	}

	public void setSdo(SearchDataObject sdo) {
		this.sdo = sdo;
	}

	public SearchDataObject getSdo() {
		return sdo;
	}

	public void update() {
		// 先清空当前图片
		Set<Bitmap> bmps = bitmaps;
		bitmaps.clear();

		// 初始化新的数据
		adapter.setSdo(sdo);
		albumAdapter.setList(sdo.getAlbums());
		musicAdapter.setList(sdo.getMusics());
		artistAdapter.setList(sdo.getArtists());

		// adapter.notifyDataSetChanged();
		// albumAdapter.notifyDataSetChanged();
		// musicAdapter.notifyDataSetChanged();
		// artistAdapter.notifyDataSetChanged();

		// 选中所有
		for (TextView underline : tabUnderlines) {
			if (underline == tabAllSelected) {
				underline.setVisibility(View.VISIBLE);
			} else {
				underline.setVisibility(View.INVISIBLE);
			}
		}
		showTabAll();

		// 回收旧图片
		for (Bitmap bitmap : bmps) {
			bitmap.recycle();
		}
		System.gc();// 旧的adapter可以回收了
	}

	public void showSearchLoading() {
		// 显示内容布局
		llFace.setVisibility(View.GONE);
		llLoading.setVisibility(View.VISIBLE);
		llContent.setVisibility(View.GONE);
		llHistory.setVisibility(View.GONE);

		// 载入动画资源
		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();
	}

	public void showSearchResult() {
		// 显示内容布局
		llFace.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llContent.setVisibility(View.VISIBLE);
		llHistory.setVisibility(View.GONE);

		// 结束动画
		if (ad != null && ad.isRunning()) {
			ad.stop();
			ad = null;
		}

		// 初始化数据
		adapter.setSdo(sdo);
		albumAdapter.setList(sdo.getAlbums());
		musicAdapter.setList(sdo.getMusics());
		artistAdapter.setList(sdo.getArtists());

		// 选中当前
		for (TextView underline : tabUnderlines) {
			if (underline == tabAllSelected) {
				underline.setVisibility(View.VISIBLE);
			} else {
				underline.setVisibility(View.INVISIBLE);
			}
		}
		showTabAll();
	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(true);
		WatchDog.tabWebFragment.setTitle("搜索结果");
		WatchDog.tabWebFragment.currentMenuItem = "搜索结果";
		WatchDog.tabWebFragment.initEtSearch();
		WatchDog.tabWebFragment.clearEtSearch();
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	@Override
	public void reload() {
		System.out.println(TAG+"reloading...");
		WatchDog.tabWebFragment.search();
	}

	@Override
	public void onDataLoadFailed() {
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}	

	private void showLlFace() {	
		System.out.println("showLlFace");
		
		llFace.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llContent.setVisibility(View.GONE);
		llHistory.setVisibility(View.GONE);
	}

	public void showHistory(String input) {
		Log.e("BUG959", TAG+"showHistory");
		
		llFace.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llContent.setVisibility(View.GONE);
		llHistory.setVisibility(View.VISIBLE);
				
		llHistory.removeAllViews();
		getSearchHistory(input);
		llHistory.addView(historyView);
	}

	private void getSearchHistory(String input) {
		//拿到历史记录
		final ArrayList<String> historyList=createHistoryList(input);
		historyAdapter.setHistoryList(historyList);
		historyAdapter.notifyDataSetChanged();
		
		//实现ITEM点击监听：将点击条目输送到文本框
		lvHistory.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				String str = historyList.get(position);
				WatchDog.tabWebFragment.setEtText(str);
			}
		});

	}

	private ArrayList<String> createHistoryList(String input) {
		ArrayList<String> arrayList=new SearchHistoryDao().getSearchHistory(input,Constant.SEARCH_HISTORY_ITEMS_SHOWN);
//		arrayList.add("刘德华");
//		arrayList.add("张学友");
//		arrayList.add("周杰伦");
//		arrayList.add("eagles");
		
		return arrayList;
	}
	
	protected void clearSearchHistory() {
		Log.e("BUG956", TAG+"clearSearchHistory");
		historyAdapter.getHistoryList().clear();
		historyAdapter.notifyDataSetChanged();
		
		new SearchHistoryDao().clearclearSearchHistory();
	}

}
