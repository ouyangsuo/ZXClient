package com.kitty.poclient.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.dfim.app.fragment.store.PurchasedFragment;





import com.kitty.poclient.R;
import com.kitty.poclient.activity.MainActivity;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.dao.SearchHistoryDao;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.AlbumDetail;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.ColumnDetail;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.domain.SearchDataObject;
import com.kitty.poclient.http.HttpPoster;
import com.kitty.poclient.interfaces.OnCurrentPlayingStateChangedListener;
import com.kitty.poclient.store.ArtistDetailFragment;
import com.kitty.poclient.store.ArtistsFragment;
import com.kitty.poclient.store.BotiquesFragmentII;
import com.kitty.poclient.store.ColumnDetailFragment;
import com.kitty.poclient.store.GenresFragment;
import com.kitty.poclient.store.PurseFragment;
import com.kitty.poclient.store.SearchFragment;
import com.kitty.poclient.store.TestFragment;
import com.kitty.poclient.store.ThemesFragment;
import com.kitty.poclient.store.TopDetailFragment;
import com.kitty.poclient.store.TopsFragment139;
import com.kitty.poclient.store.WebAlbumDetailFragmentII;
import com.kitty.poclient.store.WebPackDetailFragment;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.JsonUtil;

public class TabWebFragment extends TabFragment implements OnCurrentPlayingStateChangedListener{

	private static final String TAG = TabWebFragment.class.getSimpleName() + " ";
	public static boolean IS_ALIVE = false;
	public final static int TITLE_STYLE_NORMAL = 1;
	public final static int TITLE_STYLE_SEARCH = 2;

	public static final int BOUTIQUES = 0;
	public static final int TOP100 = 1;
	public static final int ARTISTS = 2;
	public static final int GENRES = 3;
	public static final int THEMES = 4;
	public static final int MYPURSE = 5;
	public static final int PURCHASED = 6;
	public static final int TEST = 7;

	private Fragment botiquesFragment;
	private TopsFragment139 topsFragment;
	private ArtistsFragment artistsFragment;
	private GenresFragment genresFragment;
	private ThemesFragment themesFragment;
	private PurseFragment purseFragment;
//	private PurchasedFragment purchasedFragment;
	private TestFragment testFragment;
	private SearchFragment searchFragment;
	
	private String[] menuItems;
	private static int currentPosition = BOUTIQUES;

	private View view;
	public static LinearLayout llTitleNormal;
	public static LinearLayout llTitleSearch;
	public static View loadFailureView;
	public static EditText etSearch;
	public static ImageButton ibClear;
	private TextView tabTitle;
	private ImageButton btnMenuOrBack;
	private ImageButton btnPlayer;

	private FragmentManager tabWebFragmentManager;

	// private Bitmap albumBitmap;// 用于跳转专辑详情页
	private String imgUrl;// 用于跳转并购买后刷新本地专辑界面
	public boolean popbackable = false;// 当前是否支持回退
	private ColumnDetail botiqueDetail;// 用于跳转子栏目详情页
	public static String currentMenuItem = "精品聚焦";
	public static InputMethodManager imm;

	private final int MSG_SHOW_SEARCH_RESULT = 4;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW_SEARCH_RESULT:
				showSearchResult((SearchDataObject) msg.obj);
				break;
			}
		}
	};

	private void registerReceivers() {
		// TODO Auto-generated method stub

	}

	private void unregisterReceivers() {
		// TODO Auto-generated method stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG,"onCreateView");
		view = inflater.inflate(R.layout.layout_tab_main, null);

		initComponents();
		initListeners();
		initEtSearch();
		initData();

		// setContentFragment(new BotiquesFragment());
		setContentFragment(menuItems[currentPosition], currentPosition);
		registerReceivers();

		WatchDog.tabWebFragment = this;
		WatchDog.cpsListeners.add(this);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Fragment parentFragment = getParentFragment();
		if (parentFragment instanceof TabFragment.OnTitleClickListener) {
			this.listener = (TabFragment.OnTitleClickListener) parentFragment;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		tabWebFragmentManager = getChildFragmentManager();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		super.onDetach();
	}

	@Override
	public void onResume() {
		IS_ALIVE = true;
		super.onResume();
		onCurrentPlayingStateChanged();
	}

	@Override
	public void onPause() {
		IS_ALIVE = false;
		super.onPause();
	}

	private void initComponents() {
		llTitleNormal = (LinearLayout) view.findViewById(R.id.ll_title_normal);
		llTitleSearch = (LinearLayout) view.findViewById(R.id.ll_title_search);
		etSearch = (EditText) view.findViewById(R.id.et_search);
		ibClear = (ImageButton) view.findViewById(R.id.ib_clear);

		tabTitle = (TextView) view.findViewById(R.id.tv_title);
		btnMenuOrBack = (ImageButton) view.findViewById(R.id.btn_menu);
		btnPlayer = (ImageButton) view.findViewById(R.id.btn_player);
		((MainActivity) getActivity()).btnPlayer = btnPlayer;
		((MainActivity) getActivity()).refreshPlayStatus();
	}

	protected void reloadCurrentFragment() {
		WatchDog.currentSelfReloader.reload();
	}

	private void initListeners() {
		btnMenuOrBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener == null) {
					return;
				}
				if (popbackable) {
					System.out.println("btnMenuOrBack onClick popbackable");
					tabWebFragmentManager.popBackStack();
				} else {
					System.out.println("btnMenuOrBack onClick unpopbackable");
					listener.onMenuClick();
				}
			}
		});

		btnPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onPlayerClick();
				}
			}
		});
	}

	private void initData() {
		menuItems = getActivity().getResources().getStringArray(R.array.sliding_menu_web);
		tabTitle.setText(menuItems[currentPosition]);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public void setContentFragment(String title, int position) {
		System.out.println(TAG + "setContentFragment(" + title + "," + position + ")");

		setTitle(title);
		displayMenuButtonImage();
		currentPosition = position;

		switch (position) {
		case BOUTIQUES:
			setContentFragment(getBotiquesFragment());
			break;
		case TOP100:
			setContentFragment(getTopsFragment());
			break;
		case ARTISTS:
			setContentFragment(getArtistsFragment());
			break;
		case GENRES:
			setContentFragment(getGenresFragment());
			break;
		case THEMES:
			setContentFragment(getThemesFragment());
			break;
		case MYPURSE:
			setContentFragment(getPurseFragment());
			break;
//		case PURCHASED:
//			setContentFragment(getPurchasedFragment());
//			break;
		case TEST:
			setContentFragment(getTestFragment());
			break;
		}
	}

	public void setTitle(String title) {
		tabTitle.setText(title);
	}

	private void displayBackButtonImage() {
		btnMenuOrBack.setImageResource(R.drawable.btn_back);
	}

	private void displayMenuButtonImage() {
		btnMenuOrBack.setImageResource(R.drawable.btn_menu);
	}

	private void setContentFragment(Fragment fragment) {
		setContentFragment(fragment, false, null);
	}

	public void setSearchFragment() {
		setContentFragment(getSearchFragment(), true, "SearchFragment");
	}

	private void setContentFragment(Fragment fragment, boolean addToBackStack, String name) {
		FragmentTransaction ft = tabWebFragmentManager.beginTransaction();
		ft.replace(R.id.fragment_stub, fragment);
		Log.i(TAG, "total fragments " + tabWebFragmentManager.getBackStackEntryCount());
		if (addToBackStack) {
			ft.addToBackStack(name);
		} else {
			WatchDog.flagInColumnDetail = false;// 去到首页时取消栏目详情数据的数据缓存
		}
		ft.commit();
	}

	private Fragment getBotiquesFragment() {
		if (null == botiquesFragment) {
			botiquesFragment = new BotiquesFragmentII(); 
		} else {

		}
		return botiquesFragment;
	}

	private TopsFragment139 getTopsFragment() {
		if (null == topsFragment) {
			topsFragment = new TopsFragment139(getActivity());
		}
		return topsFragment;
	}

	private ArtistsFragment getArtistsFragment() {
		if (null == artistsFragment) {
			artistsFragment = new ArtistsFragment(getActivity());
		}
		return artistsFragment;
	}

	private GenresFragment getGenresFragment() {
		if (null == genresFragment) {
			genresFragment = new GenresFragment(getActivity());
		}
		return genresFragment;
	}

	private ThemesFragment getThemesFragment() {
		if (null == themesFragment) {
			themesFragment = new ThemesFragment(getActivity());
		}
		return themesFragment;
	}

	private PurseFragment getPurseFragment() {
		if (null == purseFragment) {
			purseFragment = new PurseFragment(getActivity());
		}
		return purseFragment;
	}

//	private PurchasedFragment getPurchasedFragment() {
//		if (null == purchasedFragment) {
//		}
//		purchasedFragment = new PurchasedFragment(getActivity());
//		return purchasedFragment;
//	}
	
	private TestFragment getTestFragment() {
		if (null == testFragment) {
		}
		testFragment = new TestFragment(getActivity());
		return testFragment;
	}

	private SearchFragment getSearchFragment() {
		if (null == searchFragment) {
		}
		searchFragment = new SearchFragment(getActivity());
		return searchFragment;
	}

	public void showAlbumContentReceiverOnReceive(Intent intent) {

		long albumId = intent.getLongExtra("albumId", -1L);//
		String albumName = intent.getStringExtra("albumName");
		String _imgUrl = intent.getStringExtra("imgUrl");
		imgUrl = _imgUrl;

		showAlbumContent(albumId, albumName, imgUrl);
	}

	public void showAlbumContent(final long albumId, String albumName, String imgUrl) {
		goAlbumDetail(albumId, albumName, imgUrl, -1, null);
	}

	public void goAlbumDetail(long albumId, String albumName, String imgUrl, int location, AlbumDetail albumDetail) {// 须区分是在二级界面呈现，还是三级界面呈现
		WebAlbumDetailFragmentII albumDetailFragment = new WebAlbumDetailFragmentII(getActivity(), albumId, albumName, imgUrl, location, albumDetail);
		setContentFragment(albumDetailFragment, true, "WebAlbumDetailFragment");

		setTitle(albumName);
	}

	public void setPopbackable(boolean popbackable) {
		this.popbackable = popbackable;
		if (popbackable) {
			displayBackButtonImage();
		} else {
			displayMenuButtonImage();
		}
	}

	public interface TitlebarUpdateFragment {
		public void updateTitlebar();
	}

	public void showBotiqueContentReceiverOnReceive(Intent intent) {
		long botiqueId = intent.getLongExtra("botiqueId", -1L);
		String botiqueName = intent.getStringExtra("botiqueName");
		showBotiqueContent(botiqueId, botiqueName);
	}
	
	protected void showBotiqueContent(final long botiqueId, final String botiqueName) {
		botiqueDetail = null;
		if (botiqueId != -1) {
			goColumnDetail(botiqueDetail, botiqueId, botiqueName);
		}
	}
	public void goColumnDetail(ColumnDetail columnDetail, long columnId, String columnName) {
		ColumnDetailFragment columnDetailFragment = new ColumnDetailFragment(getActivity(), columnDetail, columnId, columnName);
		setContentFragment(columnDetailFragment, true, "ColumnDetailFragment");

		setTitle(columnName);
	}
	
	public void showTopContent(final long topId, final String topName) {
		ColumnDetail columnDetail = null;
		if (topId != -1) {
			goColumnDetail139(columnDetail, topId, topName);
		}
	}

	public void goColumnDetail139(ColumnDetail columnDetail, long columnId, String columnName) {
		TopDetailFragment columnDetailFragment = new TopDetailFragment(getActivity(), columnDetail, columnId, columnName);
		setContentFragment(columnDetailFragment, true, "ColumnDetailFragment");

		setTitle(columnName);
	}

	public void goArtistDetail(Artist artist) {

		ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment(getActivity(), artist);
		setContentFragment(artistDetailFragment, true, "ArtistDetailFragment");

		setTitle(artist.getName());
	}

	public void goPackDetail(long packId, String packName, int musicCount, String imgUrl) {

		WebPackDetailFragment packDetailFragment = new WebPackDetailFragment(getActivity(), packId, packName, musicCount, imgUrl);
		setContentFragment(packDetailFragment, true, "WebPackDetailFragment");
		setTitle(packName);
	}

	public void useTitleStyle(int titleStyle) {
		switch (titleStyle) {
		case TITLE_STYLE_SEARCH:
			llTitleNormal.setVisibility(View.GONE);
			llTitleSearch.setVisibility(View.VISIBLE);
			break;
		case TITLE_STYLE_NORMAL:
			llTitleNormal.setVisibility(View.VISIBLE);
			llTitleSearch.setVisibility(View.GONE);
			break;
		default:
			llTitleNormal.setVisibility(View.VISIBLE);
			llTitleSearch.setVisibility(View.GONE);
			break;
		}
	}

	public void initEtSearch() {
		imm = (InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);

		etSearch.setOnFocusChangeListener(new OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && "".equals(etSearch.getText().toString()) && !WatchDog.flagInSearchResult) {//由详情返回搜索列表时不要显示历史记录
					Log.e("BUG959",TAG+"etSearch onFocusChange");
					searchFragmentShowHistory();
				}
			}
		});
		
		etSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				//更新搜索记录
				if (etSearch.hasFocus()) {
					Log.e("BUG959",TAG+"etSearch afterTextChanged");					
					searchFragmentShowHistory();
				}			
			}
		});
		
		ibClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("ibclear onclick");
				etSearch.setText("");
				etSearch.requestFocus();
				showSoftInput();
			}
		});
	}

	protected void searchFragmentShowHistory() {
		Log.e("BUG959",TAG+"searchFragmentShowHistory");
		searchFragment.showHistory(etSearch.getText().toString());
	}

	public void showSoftInput() {
		// imm = (InputMethodManager)
		// etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
	}

	public void hideSoftInput() {
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
	}

	public void search() {
		if (etSearch.getText().toString() != null && !etSearch.getText().toString().trim().equals("")) {
			hideSoftInput();// source not found
			showSearchLoading();
			searchFragment.setKeyword(etSearch.getText().toString());
			search(etSearch.getText().toString());
			saveSearchText(etSearch.getText().toString());
		} else {
//			UpnpApp.showToastMessage(getResources().getString(R.string.inputShouldntBeEmpty));
			UpnpApp.mainHandler.showAlert(R.string.store_search_input_empty_alert);
		}

	}

	protected void search(final String inputStr) {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				System.out.println("search(...):inputStr="+inputStr);
				
				String json1 = new HttpPoster().search(inputStr, Constant.SEARCH_TYPE_ALBUMS);
				String json5 = new HttpPoster().search(inputStr, Constant.SEARCH_TYPE_MUSICS);
				String json10 = new HttpPoster().search(inputStr, Constant.SEARCH_TYPE_ARTISTS);
				
				if (!(new JsonUtil().validate(json1)) && !(new JsonUtil().validate(json5)) && !(new JsonUtil().validate(json10))) {
//					showNoData();
					return;
				}else{
					// SearchDataObject sdo = new JsonUtil().getSearchDataAll(json);
					List<Album> albums = new JsonUtil().getSearchDataAlbums(json1);
					List<Music> musics = new JsonUtil().getSearchDataMusics(json5);
					List<Artist> artists = new JsonUtil().getSearchDataArtists(json10);

					SearchDataObject sdo = new SearchDataObject();
					sdo.setAlbums(albums);
					sdo.setMusics(musics);
					sdo.setArtists(artists);

					Message msg = handler.obtainMessage(MSG_SHOW_SEARCH_RESULT);
					msg.obj = sdo;
					handler.sendMessage(msg);
				}
			}
		});
	}
	

	private void saveSearchText(final String string) {
		new Thread(new Runnable() {		
			@Override
			public void run() {
				new SearchHistoryDao().insertSearchRecord(string);
			}
		}).start();		
	}

	protected void showSearchLoading() {
		searchFragment.showSearchLoading();
	}

	protected void showSearchResult(SearchDataObject sdo) {
		System.out.println("searchResultFragment.getSdo().getAlbums().size()=" + searchFragment.getSdo().getAlbums().size());
		
		if (searchFragment.getSdo().getAlbums().size() != 0 || searchFragment.getSdo().getMusics().size() != 0 || searchFragment.getSdo().getArtists().size() != 0) {
			// 多次搜索时释放上一次搜索的图片
			searchFragment.setSdo(sdo);// null pointer
			searchFragment.showSearchResult();
			searchFragment.update();
		} else {
			// 第一次搜索
			searchFragment.setSdo(sdo);// null pointer
			searchFragment.showSearchResult();
		}
		
		focusOnContent();
	}

	public void focusOnContent() {
		System.out.println(TAG+" focusOnContent");
		((LinearLayout)view.findViewById(R.id.fragment_stub)).requestFocus();
	}

	public void clearEtSearch() {
		etSearch.setText("");
	}

	public void shortenEtText() {
		Log.e(TAG, "shortenEtText");
		String str = etSearch.getText().toString();
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
			setEtText(str);
		}
		
//		if(etSearch.getText().toString().equals("")){
//			searchFragment.showHistory();
//		}
	}

	public static int getCurrentPosition() {
		return currentPosition;
	}

	public void setEtText(String str) {
		etSearch.setText(str);
		etSearch.setSelection(str.length());
	}

	@Override
	public void onCurrentPlayingStateChanged() {
		if(PlayerFragment.PLAYING.equals(WatchDog.currentState)){
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
		}else{
			btnPlayer.setImageDrawable(getResources().getDrawable(R.drawable.btn_player));
		}	
	}

}
