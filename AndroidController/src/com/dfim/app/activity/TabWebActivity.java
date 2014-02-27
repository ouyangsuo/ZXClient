package com.dfim.app.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dfim.app.common.Constant;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.http.HttpPoster;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.util.JsonUtil;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.domain.ColumnDetail;
import com.union.cellremote.domain.Music;
import com.union.cellremote.domain.SearchDataObject;
import com.union.cellremote.store.ArtistDetailFragment;
import com.union.cellremote.store.BotiquesFragment;
import com.union.cellremote.store.ColumnDetailFragment;
import com.union.cellremote.store.SearchFragment;
import com.union.cellremote.store.WebAlbumDetailFragment;
import com.union.cellremote.store.WebPackDetailFragment;

public class TabWebActivity extends Activity {

	// new Thread,bitmap,etSearch
	private final String TAG = "TabWebActivity: ";
	public final static int TITLE_STYLE_NORMAL = 1;
	public final static int TITLE_STYLE_SEARCH = 2;

	private ImageButton btnPlayer;
	public static LinearLayout llTitleNormal;
	public static LinearLayout llTitleSearch;
	public static ImageButton btnMenu;
	public static ImageButton btnBack;
	public static TextView tvTitle;
	public static ImageButton ibClear;
	public static EditText etSearch;
	public static InputMethodManager imm;

	// 侧滑菜单属性,WatchDog.isSlidingMenuShown
//	private SlidingMenuFragment slidingMenuFragment;
	private SearchFragment searchResultFragment;
	
	private FrameLayout flSlidingMenu;
	public static String currentMenuItem = "精品聚焦";
	public static boolean slidingMenuInitOk = false;
	private FrameLayout flMainUI;
	private RelativeLayout rlParent;
	// private VelocityTracker vt;
	private long currentAnimationTime;
	private int currentPosition = 0;
	private int menuWidth;
	private int scale;
	private final int ANIMATION_DURATION = 10;

	private FragmentTransaction fragmentTransaction;
	private ColumnDetail botiqueDetail;// 用于跳转子栏目详情页
	private Bitmap albumBitmap;// 用于跳转专辑详情页
	private String imgUrl;// 用于跳转并购买后刷新本地专辑界面
	private boolean columnDetailExist = false;// 判断进入专辑详情时是否经由栏目详情页
	// private List<NobleMan> nobleMen = new ArrayList<NobleMan>();

	private final int MSG_ANIM = 0;
	private final int MSG_ENTER_ALBUM_DETAIL = 1;
	private final int MSG_ENTER_BOTIQUE_DETAIL = 2;
	private final int FT_COMMIT = 3;
	private final int MSG_SHOW_SEARCH_RESULT = 4;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_ANIM:
				doAnimation();
				break;

			case MSG_ENTER_ALBUM_DETAIL:
				break;

			case FT_COMMIT:
				try {
					fragmentTransaction.commit();//Can not perform this action after onSaveInstanceState
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case MSG_ENTER_BOTIQUE_DETAIL:
				break;
				
			case MSG_SHOW_SEARCH_RESULT:
				showSearchResult((SearchDataObject) msg.obj);
				break;
			}
		}
	};
	// private float x = 0;

	private BroadcastReceiver initBtnPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initBtnPlayer();
		}
	};

	public static void changeButton(String which) {

		if (which.equals("btnBack")) {
			btnBack.setVisibility(View.VISIBLE);
			btnMenu.setVisibility(View.GONE);
			WatchDog.keybackRefersExitPro = false;

		} else if (which.equals("btnMenu")) {
			btnBack.setVisibility(View.GONE);
			btnMenu.setVisibility(View.VISIBLE);
			WatchDog.keybackRefersExitPro = true;
		} else {

		}

	}

	public void showBotiqueContentReceiverOnReceive(Intent intent) {
		if (WatchDog.isSlidingMenuShownInTabWeb == true) {
			btnMenuOnClick();
		}

		long botiqueId = intent.getLongExtra("botiqueId", -1L);
		String botiqueName = intent.getStringExtra("botiqueName");
		showBotiqueContent(botiqueId, botiqueName);
	}

	public void showAlbumContentReceiverOnReceive(Intent intent) {
		// System.out.println(TAG + "showAlbumContentReceiver onReceive");
		if (WatchDog.isSlidingMenuShownInTabWeb == true) {
			btnMenuOnClick();
		}

		long albumId = intent.getLongExtra("albumId", -1L);// OutOfMemoryError=manytimes!
		String albumName = intent.getStringExtra("albumName");
		String _imgUrl = intent.getStringExtra("imgUrl");
		Bitmap bitmap = intent.getParcelableExtra("bitmap");

		// long albumId = WatchDog.albumId2send;
		// String albumName = WatchDog.albumName2send;
		// String _imgUrl =WatchDog.albumImgurl2send;
		// Bitmap bitmap =WatchDog.albumBitmap2send;

		albumBitmap = bitmap;
		imgUrl = _imgUrl;

		showAlbumContent(albumId, albumName);
	}

	/*
	 * 生命周期方法×2
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();
		ExitApplication.getInstance().addActivity(this);
		WatchDog.currentActivities.add(this);
		setContentView(R.layout.web_root);

		initComponents();
		initBtnPlayer();
		initView();
		initSilidingMenu();
		initListeners();
		registerReceivers();
	}

	@Override
	protected void onResume() {
		resetSlidingMenu();

		super.onResume();
	}

	@Override
	protected void onPause() {
		System.gc();
		if (WatchDog.isSlidingMenuShownInTabWeb) {
			btnMenuOnClick();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}

	private void resetSlidingMenu() {
		if (WatchDog.isSlidingMenuShownInTabWeb == true) {
			// WatchDog.isSlidingMenuShown = false;
			// doAnimation();
			btnMenuOnClick();
		}

		// currentAnimationTime = 0;
		// currentPosition = 0;
	}

	private void registerReceivers() {
		// registerReceiver(showAlbumContentReceiver, new
		// IntentFilter("showAlbumContentReceiver"));
		// registerReceiver(showBotiqueContentReceiver, new
		// IntentFilter("showBotiqueContentReceiver"));
		registerReceiver(initBtnPlayerReceiver, new IntentFilter("initBtnPlayerReceiver"));
	}

	private void initComponents() {		rlParent = (RelativeLayout) findViewById(R.id.rl_parent);
	flSlidingMenu = (FrameLayout) findViewById(R.id.fl_sliding_menu);
	flMainUI = (FrameLayout) findViewById(R.id.fl_main_ui);

	llTitleNormal = (LinearLayout) findViewById(R.id.ll_title_normal);
	llTitleSearch = (LinearLayout) findViewById(R.id.ll_title_search);

	btnMenu = (ImageButton) findViewById(R.id.btn_menu);
	btnBack = (ImageButton) findViewById(R.id.btn_back);
	btnPlayer = (ImageButton) findViewById(R.id.btn_player);
	ibClear = (ImageButton) findViewById(R.id.ib_clear);
	etSearch = (EditText) findViewById(R.id.et_search);
	tvTitle = (TextView) findViewById(R.id.tv_title);

	tvTitle.setEnabled(false);}

	public void initView() {
		goStoreFragment(new BotiquesFragment(this), R.id.ll_web_root, true);
		tvTitle.setText("精品聚焦");
		currentMenuItem = "精品聚焦";

		// goStoreFragment(new PurseFragment(this), R.id.ll_web_root, true);
		// tvTitle.setText("我的钱包");
		// currentFragment = "我的钱包";

		changeButton("btnMenu");
	}

	private void initListeners() {
		
		tvTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
			}
		});

		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDog.checkMediaReady()) {
					return;
				}
				
//				TabWebActivity.this.startActivity(new Intent(TabWebActivity.this, PlayerActivity.class));
			}
		});

		btnMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnMenuOnClick();
			}
		});

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// FragmentManager fragmentManager = getFragmentManager();
				// fragmentManager.popBackStack();
				iPopBackStack();
			}
		});
	}

	protected void iPopBackStack() {
		// useSpecifiedLayout(WatchDog.previousLayout);
		getFragmentManager().popBackStack();
	}

	@SuppressLint("Recycle")
	private void initSilidingMenu() {
		// vt = VelocityTracker.obtain();

		loadSlidingMenuFragment();
		slidingMenuInitOk = true;
	}

	public void btnMenuOnClick() {
		flSlidingMenu.setVisibility(View.VISIBLE);
		if (slidingMenuInitOk == false) {
			initSilidingMenu();
		}
		moveLayout();
	}

	private void moveLayout() {
		menuWidth = flSlidingMenu.getWidth();
		scale = menuWidth / 5;

		long now = SystemClock.uptimeMillis();
		currentAnimationTime = now + ANIMATION_DURATION;

		handler.sendEmptyMessageAtTime(MSG_ANIM, currentAnimationTime);
	}

	/*
	 * 平移上层界面一段距离
	 */
	private void moveLayout(int scale) {
		int left = flMainUI.getLeft();

		if ((left + scale) < menuWidth && (left + scale) > 0) {
			// 还没有越界，左右滑动：
			flMainUI.offsetLeftAndRight(scale);
			currentPosition += scale;
		} else if ((left + scale) >= menuWidth) {
			// 再偏移就要越界的情况下，定格在menuWidth：
			flMainUI.offsetLeftAndRight(menuWidth - left);
			currentPosition += menuWidth - left;
			currentPosition = menuWidth;
			// // isShown = true;
			// WatchDog.isSlidingMenuShown = true;

		} else if ((left + scale) <= 0) {
			// 归零矫正，定格在0
			flMainUI.offsetLeftAndRight(0 - left);
			// currentPosition += 0 - left;
			currentPosition = 0;

			// // isShown = false;
			// WatchDog.isSlidingMenuShown = false;
		}

		rlParent.invalidate();
		flMainUI.forceLayout();
	}

	private void doAnimation() {
		if (WatchDog.isSlidingMenuShownInTabWeb == true) {
			moveLayout(-scale);

			if (currentPosition <= 0) {
				handler.removeMessages(MSG_ANIM);// 不要再移了

				currentAnimationTime = 0;
				currentPosition = 0;

				// isShown = false;
				WatchDog.isSlidingMenuShownInTabWeb = false;
				flSlidingMenu.setVisibility(View.GONE);
			} else {
				currentAnimationTime += ANIMATION_DURATION;
				handler.sendMessageAtTime(handler.obtainMessage(MSG_ANIM), currentAnimationTime);
			}

		} else if (WatchDog.isSlidingMenuShownInTabWeb == false) {
			moveLayout(scale);

			if (currentPosition >= menuWidth) {
				handler.removeMessages(MSG_ANIM);// 不要再移了

				currentAnimationTime = 0;
				currentPosition = menuWidth;

				// isShown = true;
				WatchDog.isSlidingMenuShownInTabWeb = true;
				flSlidingMenu.setVisibility(View.VISIBLE);

			} else {
				currentAnimationTime += ANIMATION_DURATION;
				handler.sendMessageAtTime(handler.obtainMessage(MSG_ANIM), currentAnimationTime);
			}

		}

	}

	private void loadSlidingMenuFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

/*		Bundle bundle = new Bundle();
		slidingMenuFragment = new SlidingMenuFragment(this, "tab_web", currentMenuItem);
		slidingMenuFragment.setArguments(bundle);

		fragmentTransaction.replace(R.id.ll_menu_content, slidingMenuFragment);*/
		fragmentTransaction.commit();
	}

	private void initBtnPlayer() {
		String currentCacheState = Music.CACHE_WAIT;
		if (WatchDog.currentPlayingMusic != null) {
			currentCacheState = WatchDog.cacheStateMap.get(WatchDog.currentPlayingMusic.getId());// 查到当前曲目缓存状态
		}

		if (WatchDog.mediaOutOfService == true && WatchDog.currentState.equals(PlayerFragment.PLAYING)) {
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
		} else if (!WatchDog.currentState.equals(PlayerFragment.PLAYING) || !Music.CACHE_DOWNLOADED.equals(currentCacheState)) {// 空指针
			btnPlayer.setImageResource(R.drawable.btn_player);
			// btnPlayer.setEnabled(false);
		} else {
			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
			// btnPlayer.setImageResource(R.drawable.btn_player);
			// btnPlayer.setEnabled(true);
		}

		// 外联设备文件播放情况
		if (WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE) {
			if (WatchDog.currentState.equals(PlayerFragment.PLAYING)) {
				AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
				btnPlayer.setImageDrawable(animationDrawable);
				animationDrawable.start();
			} else {
				btnPlayer.setImageResource(R.drawable.btn_player);
			}
		}
	}

	protected void showAlbumContent(final long albumId, String albumName) {
		// System.out.println(TAG + "showContent>>" + "id=" + albumId +
		// ";name="+ albumName);
		goAlbumDetail(albumId, albumName, albumBitmap, -1, null);
	}

	protected void showBotiqueContent(final long botiqueId, final String botiqueName) {
		botiqueDetail = null;

		if (botiqueId != -1) {
			// 立即进入栏目详情页
			goColumnDetail(botiqueDetail, botiqueId, botiqueName);
		}
	}

	/*
	 * 载入专辑详情
	 */
	public void goAlbumDetail(long albumId, String albumName, Bitmap _albumBitmap, int location, AlbumDetail albumDetail) {// 须区分是在二级界面呈现，还是三级界面呈现
		// checkSlidingHidden();
		if (_albumBitmap == null) {
			_albumBitmap = Constant.albumCover;
		}

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		WebAlbumDetailFragment albumDetailFragment = new WebAlbumDetailFragment(this, albumId, albumName, _albumBitmap, location, albumDetail);
		albumDetailFragment.setArguments(bundle);

		// int layout = getAlbumLayout();
		fragmentTransaction.replace(R.id.ll_web_root, albumDetailFragment);
		fragmentTransaction.addToBackStack("WebAlbumDetailFragment");

		try {
			fragmentTransaction.commit();
		} catch (IllegalStateException e) {
			fragmentTransaction.commitAllowingStateLoss();
		}
		// nobleMen.add(albumDetailFragment);

		// useSpecifiedLayout(layout);
		tvTitle.setText(albumName);// nullpointer
	}

	/*
	 * 载入主题详情
	 */
	public void goPackDetail(long packId, String packName, int musicCount, String imgUrl) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		WebPackDetailFragment packDetailFragment = new WebPackDetailFragment(this, packId, packName, musicCount, imgUrl);
		packDetailFragment.setArguments(bundle);

		int layout = R.id.ll_web_root;
		fragmentTransaction.replace(layout, packDetailFragment);
		fragmentTransaction.addToBackStack("WebPackDetailFragment");

		try {
			fragmentTransaction.commit();
		} catch (IllegalStateException e) {
			fragmentTransaction.commitAllowingStateLoss();
		}
		// nobleMen.add(packDetailFragment);

		// useSpecifiedLayout(layout);
		tvTitle.setText(packName);// nullpointer

	}

	/*
	 * 载入演出者详情
	 */
	public void goArtistDetail(Artist artist) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment(this, artist);
		artistDetailFragment.setArguments(bundle);

		int layout = R.id.ll_web_root;
		fragmentTransaction.replace(layout, artistDetailFragment);
		fragmentTransaction.addToBackStack("ArtistDetailFragment");

		try {
			fragmentTransaction.commit();
		} catch (IllegalStateException e) {
			// fragmentTransaction.commitAllowingStateLoss();//IllegalStateException:
			// commit already called
			e.printStackTrace();
			System.out.println("e=" + e);
		}
		// nobleMen.add(artistDetailFragment);

		// useSpecifiedLayout(layout);
		tvTitle.setText(artist.getName());
	}

	/*
	 * 载入栏目专辑列表
	 */
	public void goColumnDetail(ColumnDetail columnDetail, long columnId, String columnName) {
		// checkSlidingHidden();

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		ColumnDetailFragment columnDetailFragment = new ColumnDetailFragment(this, columnDetail, columnId, columnName);
		columnDetailFragment.setArguments(bundle);

		// int layout = getColumnLayout();
		fragmentTransaction.replace(R.id.ll_web_root, columnDetailFragment);
		fragmentTransaction.addToBackStack("BotiqueDetailFragment");
		try {
			fragmentTransaction.commit();// IllegalStateException: Can not
											// perform this action after
											// onSaveInstanceState
		} catch (IllegalStateException e) {
			fragmentTransaction.commitAllowingStateLoss();
		}
		// nobleMen.add(columnDetailFragment);

		// useSpecifiedLayout(layout);
		tvTitle.setText(columnName);
	}

	// private int getColumnLayout() {
	// // if ("精品聚焦".equals(currentFragment)) {
	// // return R.id.ll_web_root_11;
	// // } else if ("TOP100".equals(currentFragment)) {
	// // return R.id.ll_web_root_21;
	// // } else if ("类型".equals(currentFragment)) {
	// // return R.id.ll_web_root_31;
	// // } else {
	// // return R.id.ll_web_root;
	// // }
	//
	// if ("精品聚焦".equals(currentFragment)) {
	// return R.id.ll_web_root;
	// } else if ("TOP100".equals(currentFragment)) {
	// return R.id.ll_web_root;
	// } else if ("类型".equals(currentFragment)) {
	// // return R.id.ll_web_root_30;
	// return R.id.ll_web_root;
	// } else {
	// return R.id.ll_web_root;
	// }
	// }

	// private int getAlbumLayout() {
	// // if ("精品聚焦".equals(currentFragment)) {
	// // return R.id.ll_web_root_12;
	// // } else if ("TOP100".equals(currentFragment)) {
	// // return R.id.ll_web_root_22;
	// // } else if ("类型".equals(currentFragment)) {
	// // return R.id.ll_web_root_32;
	// // } else {
	// // return R.id.ll_web_root;
	// // }
	//
	// if ("精品聚焦".equals(currentFragment)) {
	// return R.id.ll_web_root_10;
	// } else if ("TOP100".equals(currentFragment)) {
	// return R.id.ll_web_root_20;
	// } else if ("类型".equals(currentFragment)) {
	// // return R.id.ll_web_root_30;
	// return R.id.ll_web_root_10;
	// } else if ("已购音乐".equals(currentFragment)) {
	// // return R.id.ll_web_root_30;
	// return R.id.ll_web_root_70;
	// } else {
	// return R.id.ll_web_root;
	// }
	// }

	// private int getMenuItemLayout() {
	// if ("精品聚焦".equals(currentFragment)) {
	// return R.id.ll_web_root_10;
	// } else if ("TOP100".equals(currentFragment)) {
	// return R.id.ll_web_root_20;
	// } else if ("类型".equals(currentFragment)) {
	// // return R.id.ll_web_root_30;
	// return R.id.ll_web_root_10;
	// } else {
	// return R.id.ll_web_root;
	// }
	// }

	/*
	 * 载入各首页
	 */
	public void goStoreFragment(Fragment _fragment, int _layout, boolean _flag) {
		// checkSlidingHidden();

		FragmentManager _fragmentManager = getFragmentManager();
		fragmentTransaction = _fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		_fragment.setArguments(bundle);
		// int layout = getMenuItemLayout();

		fragmentTransaction.replace(_layout, _fragment);
		if (_fragment instanceof SearchFragment) {
			searchResultFragment = (SearchFragment) _fragment;
			fragmentTransaction.addToBackStack("SearchResultFragment");
		}
		handler.sendMessageDelayed(handler.obtainMessage(FT_COMMIT), 200);
		// _fragmentTransaction.commit();

		// useSpecifiedLayout(_layout);
	}

	/** 已购音乐 */
	public void goStoreFragmentForPurchased(Fragment fragment, int layout) {
		FragmentManager _fragmentManager = getFragmentManager();
		fragmentTransaction = _fragmentManager.beginTransaction();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);
		// useSpecifiedLayout(layout);
		fragmentTransaction.replace(layout, fragment);
		fragmentTransaction.addToBackStack("PurchasedFragment");
		handler.sendMessageDelayed(handler.obtainMessage(FT_COMMIT), 200);
	}

	// private void useSpecifiedLayout(int _layout) {
	// switch (_layout) {
	// case R.id.ll_web_root:
	// findViewById(R.id.ll_web_root).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	//
	// columnDetailExist = false;// 进入主页即注销栏目详情
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// // detailsCollectGarbage();
	//
	// break;
	//
	// /*
	// * 使用精品聚焦布局
	// */
	// case R.id.ll_web_root_10:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_10).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_11).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_12).setVisibility(View.GONE);
	//
	// // sendBroadcast(new Intent("fitBotiquesTitleReceier"));
	// // columnDetailExist = false;// 进入主页即注销栏目详情
	// // detailsCollectGarbage();
	//
	// break;
	//
	// case R.id.ll_web_root_11:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_10).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_11).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_12).setVisibility(View.GONE);
	//
	// if (columnDetailExist == true) {
	// sendBroadcast(new Intent("fitBotiqueDetailTitleReceier"));// 由专辑详情回退时切换标题
	// }
	// WatchDog.previousLayout = R.id.ll_web_root_10;
	// columnDetailExist = true;// 进入栏目详情即注册
	// break;
	//
	// case R.id.ll_web_root_12:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_10).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_11).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_12).setVisibility(View.VISIBLE);
	//
	// if (columnDetailExist == true) {
	// WatchDog.previousLayout = R.id.ll_web_root_11;
	// } else {
	// WatchDog.previousLayout = R.id.ll_web_root_10;
	// }
	//
	// break;
	//
	// /*
	// * 使用TOP100布局
	// */
	// case R.id.ll_web_root_20:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_20).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_21).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_22).setVisibility(View.GONE);
	//
	// sendBroadcast(new Intent("fitTopsTitleReceier"));
	// columnDetailExist = false;// 进入主页即注销栏目详情
	// // detailsCollectGarbage();
	//
	// break;
	//
	// case R.id.ll_web_root_21:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_20).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_21).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_22).setVisibility(View.GONE);
	//
	// if (columnDetailExist == true) {
	// sendBroadcast(new Intent("fitBotiqueDetailTitleReceier"));// 由专辑详情回退时切换标题
	// }
	// WatchDog.previousLayout = R.id.ll_web_root_20;
	// columnDetailExist = true;// 进入栏目详情即注册
	// break;
	//
	// case R.id.ll_web_root_22:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_20).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_21).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_22).setVisibility(View.VISIBLE);
	//
	// if (columnDetailExist == true) {
	// WatchDog.previousLayout = R.id.ll_web_root_21;
	// } else {
	// WatchDog.previousLayout = R.id.ll_web_root_20;
	// }
	// break;
	//
	// /*
	// * 使用类型布局
	// */
	// case R.id.ll_web_root_30:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_30).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_31).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_32).setVisibility(View.GONE);
	//
	// // sendBroadcast(new Intent("fitGenresTitleReceier"));
	// // columnDetailExist = false;// 进入主页即注销栏目详情
	// // detailsCollectGarbage();
	//
	// break;
	//
	// case R.id.ll_web_root_31:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_30).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_31).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_32).setVisibility(View.GONE);
	//
	// if (columnDetailExist == true) {
	// sendBroadcast(new Intent("fitBotiqueDetailTitleReceier"));// 由专辑详情回退时切换标题
	// }
	// WatchDog.previousLayout = R.id.ll_web_root_30;
	// columnDetailExist = true;// 进入栏目详情即注册
	// break;
	//
	// case R.id.ll_web_root_32:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_30).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_31).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_32).setVisibility(View.VISIBLE);
	//
	// if (columnDetailExist == true) {
	// WatchDog.previousLayout = R.id.ll_web_root_31;
	// } else {
	// WatchDog.previousLayout = R.id.ll_web_root_30;
	// }
	// break;
	//
	// /*
	// * 使用演出者布局
	// */
	// case R.id.ll_web_root_40:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_40).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_41).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_42).setVisibility(View.GONE);
	//
	// sendBroadcast(new Intent("fitArtistsTitleReceier"));
	// columnDetailExist = false;// 进入主页即注销栏目详情
	// // detailsCollectGarbage();
	//
	// break;
	//
	// case R.id.ll_web_root_41:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_40).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_41).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_42).setVisibility(View.GONE);
	//
	// sendBroadcast(new Intent("fitArtistsDetailTitleReceier"));// 由专辑详情回退时切换标题
	// WatchDog.previousLayout = R.id.ll_web_root_40;
	// columnDetailExist = true;// 进入栏目详情即注册
	// break;
	//
	// case R.id.ll_web_root_42:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_40).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_41).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_42).setVisibility(View.VISIBLE);
	//
	// WatchDog.previousLayout = R.id.ll_web_root_41;
	// break;
	//
	// /*
	// * 使用音乐主题布局
	// */
	// case R.id.ll_web_root_50:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_50).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_51).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_52).setVisibility(View.GONE);
	//
	// sendBroadcast(new Intent("fitThemesTitleReceier"));
	// columnDetailExist = false;// 进入主页即注销栏目详情
	// // detailsCollectGarbage();
	//
	// break;
	//
	// case R.id.ll_web_root_51:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_50).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_51).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_52).setVisibility(View.GONE);
	//
	// sendBroadcast(new Intent("fitThemesDetailTitleReceier"));// 由专辑详情回退时切换标题
	// WatchDog.previousLayout = R.id.ll_web_root_50;
	// break;
	//
	// case R.id.ll_web_root_52:
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.VISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.INVISIBLE);
	//
	// findViewById(R.id.ll_web_root_50).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_51).setVisibility(View.GONE);
	// findViewById(R.id.ll_web_root_52).setVisibility(View.VISIBLE);
	//
	// WatchDog.previousLayout = R.id.ll_web_root_51;
	// break;
	//
	// case R.id.ll_web_root_70: {
	// findViewById(R.id.ll_web_root).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_1).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_2).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_3).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_4).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_5).setVisibility(View.INVISIBLE);
	// findViewById(R.id.ll_web_root_7).setVisibility(View.VISIBLE);
	//
	// findViewById(R.id.ll_web_root_70).setVisibility(View.VISIBLE);
	// break;
	// }
	//
	// default:
	// break;
	// }
	//
	// }

	// private void detailsCollectGarbage() {
	// // sendBroadcast(new Intent("collectTheFuckingGarbageReceiver"));
	// for (NobleMan nobleMan : nobleMen) {
	// nobleMan.letsSeeHeaven();
	// nobleMan = null;
	// }
	// nobleMen.clear();
	// }

	private void unregisterReceivers() {
		// unregisterReceiver(showAlbumContentReceiver);
		// unregisterReceiver(showBotiqueContentReceiver);
		unregisterReceiver(initBtnPlayerReceiver);
	}

	public void checkSlidingHidden() {
/*		if (slidingMenuFragment != null) {
			slidingMenuFragment.slidingHideHide();
		}*/

		// sendBroadcast(new Intent("slidingHideHideReceiver"));
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
		if (WatchDog.searchResultFragmentRunning) {
			return;
		}

		etSearch.requestFocus();
		imm = (InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);

		etSearch.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
					hideSoftInput();// source not found
					search(etSearch.getText().toString());
					// showSearchResult();
				}
				return true;
			}
		});

		ibClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etSearch.setText("");
				etSearch.requestFocus();
				imm = (InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
			}
		});
	}

	protected void search(final String inputStr) {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				String json1 = new HttpPoster().search(inputStr, 1);
				String json5 = new HttpPoster().search(inputStr, 5);
				String json10 = new HttpPoster().search(inputStr, 10);

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
		});

	}

	protected void showSearchResult(SearchDataObject sdo) {
		System.out.println("searchResultFragment.getSdo().getAlbums().size()=" + searchResultFragment.getSdo().getAlbums().size());
		if (searchResultFragment.getSdo().getAlbums().size() != 0 || searchResultFragment.getSdo().getMusics().size() != 0 || searchResultFragment.getSdo().getArtists().size() != 0) {
			// 多次搜索时释放上一次搜索的图片
			searchResultFragment.setSdo(sdo);// null pointer
			searchResultFragment.update();
		} else {
			// 第一次搜索
			searchResultFragment.setSdo(sdo);// null pointer
			searchResultFragment.showSearchResult();
		}
	}

	public void hideSoftInput() {
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
	}

	public void activate() {
		/* 方案1：跳出输入软键盘再收起，将焦点带到前台 */
		// if (imm!=null) {
		// imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		// }
		// }, 1000);
		//
		// }else{
		// imm = (InputMethodManager)
		// etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		// }
		// }, 1000);
		// }

		/* 方案2：解决侧滑在下面抢焦点的问题 */
		flSlidingMenu.setVisibility(View.GONE);
	}

}
