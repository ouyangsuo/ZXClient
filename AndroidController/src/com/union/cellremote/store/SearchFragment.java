package com.union.cellremote.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.activity.WebListenActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.MusicDao;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.BitmapUtil;
import com.union.cellremote.R;
import com.union.cellremote.adapter.SearchResultListAdapter;
import com.union.cellremote.adapter.SearchResultXListAdapter;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.domain.Music;
import com.union.cellremote.domain.SearchDataObject;

public class SearchFragment extends Fragment {
	// Looper.prepare

	private final String TAG = "SearchResultFragment: ";
	private Context context;

	private View view;
	private LinearLayout llFace;
	private LinearLayout llContent;
	private LinearLayout llTabContent;
	private LinearLayout llNoResult;
	private ExpandableListView xlvResult;
	private ListView lvResultAlbums;
	private ListView lvResultMusics;
	private ListView lvResultArtists;

	private SearchResultXListAdapter adapter;
	private SearchResultListAdapter albumAdapter;
	private SearchResultListAdapter musicAdapter;
	private SearchResultListAdapter artistAdapter;

	private int firstVisibleItemPosition = -1;// 记录停止卷动时第一个ITEM的序号
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量
	// public List<Bitmap> bitmaps = new ArrayList<Bitmap>();
	public Set<Bitmap> bitmaps = new HashSet<Bitmap>();
	// private Bitmap dontRecycleMeBitmap;
	// private boolean recycleNeeded = true;

	private SearchDataObject sdo;
	// private List<Album> albums = new ArrayList<Album>();
	// private List<Music> musics = new ArrayList<Music>();
	// private List<Artist> artists = new ArrayList<Artist>();

	private Button tabAll;
	private Button tabAlbum;
	private Button tabMusic;
	private Button tabArtist;
	private Button[] tabs;

	private TextView tabAllSelected;
	private TextView tabAlbumSelected;
	private TextView tabMusicSelected;
	private TextView tabArtistSelected;
	private List<TextView> tabUnderlines = new ArrayList<TextView>();

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			}
			super.handleMessage(msg);
		}
	};

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

		parentActivityChangeButton();
		parentActivityChangeTitle();
		view = LayoutInflater.from(context).inflate(R.layout.search_result, null);

		initComponents();
		initData();
		initExpandableListView();
		initLvAlbums();
		initLvMusics();
		initLvArtists();
		initTabListeners();

		WatchDog.searchResultFragmentRunning = true;
		return view;
	}

	private void initData() {
		sdo = new SearchDataObject();
		sdo.setAlbums(new ArrayList<Album>());
		sdo.setMusics(new ArrayList<Music>());
		sdo.setArtists(new ArrayList<Artist>());
	}

	private void parentActivityChangeTitle() {
		// TabWebActivity.tvTitle.setText("演出者");
		TabWebActivity.currentMenuItem = "搜索结果";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_SEARCH);
		((TabWebActivity) context).initEtSearch();
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnBack");
		// TabWebActivity.changeButton("btnMenu");
	}

	private void initTabListeners() {
		tabAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onTabSelected(tabAll);
			}
		});

		tabAlbum.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onTabSelected(tabAlbum);
			}
		});

		tabMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onTabSelected(tabMusic);
			}
		});

		tabArtist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onTabSelected(tabArtist);
			}
		});
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
			llTabContent.removeAllViews();
			llTabContent.addView(lvResultArtists);

			artistAdapter.notifyDataSetChanged();
			lvResultArtists.requestFocus();
			lvResultArtists.scrollTo(0, 0);
			artistAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabMusic() {
		if (hasResult(sdo.getMusics())) {
			llTabContent.removeAllViews();
			llTabContent.addView(lvResultMusics);

			musicAdapter.notifyDataSetChanged();
			lvResultMusics.requestFocus();
			lvResultMusics.scrollTo(0, 0);
			musicAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabAlbum() {
		if (hasResult(sdo.getAlbums())) {
			llTabContent.removeAllViews();
			llTabContent.addView(lvResultAlbums);

			albumAdapter.notifyDataSetChanged();
			lvResultAlbums.requestFocus();
			lvResultAlbums.scrollTo(0, 0);
			albumAdapter.notifyDataSetChanged();
		} else {
			showNoResult();
		}
	}

	private void showTabAll() {
		if (hasResult(sdo.getAlbums()) || hasResult(sdo.getMusics()) || hasResult(sdo.getArtists())) {
			llTabContent.removeAllViews();
			llTabContent.addView(xlvResult);

			adapter.notifyDataSetChanged();
			xlvResult.requestFocus();
			xlvResult.scrollTo(0, 0);
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
		System.out.println(TAG + "onResume");

		/* 由详情页返回时拿取原来的数据和位置 */
		if (WatchDog.flagInSearchResult) {
			getSavedDataAndPosition();
			showSearchResult();
			useFormerTab();
			useFormerPosition();
		}

		super.onResume();
	}

	private void useFormerPosition() {
		xlvResult.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
	}

	private void useFormerTab() {
		System.out.println("useFormerTab");

		for (int i = 0; i < tabs.length; i++) {
			System.out.println("tabs[i].getText().toString()=" + tabs[i].getText().toString());
			System.out.println("WatchDog.selectedSearchResultTabText=" + WatchDog.selectedSearchResultTabText);

			if (tabs[i].getText().toString().equals(WatchDog.selectedSearchResultTabText)) {
				System.out.println("ok show it");
				onTabSelected(tabs[i]);
				return;
			}
		}
	}

	private void initComponents() {
		llFace = (LinearLayout) view.findViewById(R.id.ll_face);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llTabContent = (LinearLayout) view.findViewById(R.id.ll_search_tab_content);
		// llNoResult = (LinearLayout) view.findViewById(R.id.ll_no_result);

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

		adapter = new SearchResultXListAdapter(context, this);
		albumAdapter = new SearchResultListAdapter(context, this, 0);
		musicAdapter = new SearchResultListAdapter(context, this, 1);
		artistAdapter = new SearchResultListAdapter(context, this, 2);
	}

	public void onAlbumItemClick(int position) {
		long albumId = sdo.getAlbums().get(position).getId();
		String albumName = sdo.getAlbums().get(position).getName();
		Bitmap albumBitmap = sdo.getAlbums().get(position).getBitmap();
		String imgUrl = sdo.getAlbums().get(position).getImgUrl();

		Intent intent = new Intent("showAlbumContentReceiver");
		intent.putExtra("albumId", albumId);
		intent.putExtra("albumName", albumName);
		intent.putExtra("bitmap", albumBitmap);// cant draw recycled bitmaps
		intent.putExtra("imgUrl", imgUrl);
		intent.putExtra("layout", R.id.ll_web_root);

		recordCurrentDataAndPosition();
		// recycleNeeded = false;
		((TabWebActivity) context).showAlbumContentReceiverOnReceive(intent);
	}

	public void onMusicItemClick(int position) {

		Music music = sdo.getMusics().get(position);
		int state = getMusicStateById(music.getId());
		String uri = "xxbox://listen?id=" + music.getId();
		new Player().play(uri);
		/*
		 * 1208； 试听失败提示：“试听失败，请检查网络”； setUri成功； 信息由boxSub订阅信息返回； 让纯鹏看下；
		 */

		Intent intent = new Intent(getActivity(), WebListenActivity.class);
		intent.putExtra("musicName", music.getName());
		intent.putExtra("artist", music.getArtistName());
		intent.putExtra("bitmap", new BitmapUtil().processBigBitmap(music.getBitmap(), 250000, Constant.albumCover));
		intent.putExtra("btnBuyText", getListenBtnText(music, state));
		intent.putExtra("btnBuyEnabled", getListenBtnEnabled(state));
		intent.putExtra("isFromSearch", true);

		// recycleNeeded = false;
		getActivity().startActivity(intent);

		WatchDog.currentPlayingMusic = music;
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
		recordCurrentDataAndPosition();
		// recycleNeeded = false;
		((TabWebActivity) context).goArtistDetail(artist);
	}

	private void initExpandableListView() {
		adapter.setSdo(sdo);
		xlvResult.setAdapter(adapter);
		xlvResult.setGroupIndicator(null);
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			xlvResult.expandGroup(i);
		}

		xlvResult.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					firstVisibleItemPosition = xlvResult.getFirstVisiblePosition();
					if (xlvResult.getChildAt(0) != null) {
						scrollTop = xlvResult.getChildAt(0).getTop();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
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
		musicAdapter.setList(sdo.getMusics());
		lvResultMusics.setAdapter(musicAdapter);
		lvResultMusics.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onMusicItemClick(position);
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDetach() {
		System.out.println(TAG + "onDetach");
		WatchDog.searchResultFragmentRunning = false;
		((TabWebActivity) context).hideSoftInput();
		// ((TabWebActivity)context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
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
//		WatchDog.sdoInSearchResult = sdo;
//		WatchDog.fvipInSearchResult = firstVisibleItemPosition;
//		WatchDog.stInSearchResult = scrollTop;
//		WatchDog.flagInSearchResult = true;
	}

	public void getSavedDataAndPosition() {
//		sdo = WatchDog.sdoInSearchResult;
//		firstVisibleItemPosition = WatchDog.fvipInSearchResult;
//		scrollTop = WatchDog.stInSearchResult;
//		WatchDog.flagInSearchResult = false;
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

	public void showSearchResult() {
		// 显示内容布局
		llFace.setVisibility(View.GONE);
		llContent.setVisibility(View.VISIBLE);

		// 初始化数据
		adapter.setSdo(sdo);
		albumAdapter.setList(sdo.getAlbums());
		musicAdapter.setList(sdo.getMusics());
		artistAdapter.setList(sdo.getArtists());

		// adapter.notifyDataSetChanged();
		// albumAdapter.notifyDataSetChanged();
		// musicAdapter.notifyDataSetChanged();
		// artistAdapter.notifyDataSetChanged();

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

}
