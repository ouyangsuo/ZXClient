package com.dfim.app.fragment.store;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.adapter.SpinnerListAdapter;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.AlbumDao;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.util.AnimUtil;
import com.dfim.app.util.ListviewDataPositionRecorder;
import com.dfim.app.util.LoadImageAysnc;
import com.dfim.app.util.SingletonUtil;
import com.dfim.app.widget.ScrollOverListView;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ExtendsAlbumListAdapter;
import com.union.cellremote.adapter.ExtendsMusicListAdapter;
import com.union.cellremote.adapter.ExtendsPackListAdapter;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Music;
import com.union.cellremote.domain.Pack;
import com.union.cellremote.service.IAlbumService;
import com.union.cellremote.service.IMusicService;
import com.union.cellremote.service.IPackService;
import com.union.cellremote.service.impl.AlbumServiceImpl;
import com.union.cellremote.service.impl.MusicServiceImpl;
import com.union.cellremote.service.impl.PackServiceImpl;

//import android.app.Fragment;

//R.layout.musics_item_for_cloud,已加载完成
public class PurchasedFragment extends Fragment implements TitlebarUpdateFragment {

	public static String FragmentName = "已购音乐";
	private final static String TAG = "PurchasedFragment:";

	public static BaseAdapter listadapter;
	public int product_or_location;// 区分是点击类型还是状态
	public int product_type = 0;// 商品类型 0 专辑 1单曲，2主题
	public int product_state;// 商品状态
	private long pageIndex = 0;// 起始页码
	private int pageSize = 20;// 每页的大小
	private Context context;

	private View view;
	private View headview;
	private TextView spinnerTextItemSelected;
	private TextView spinnerLocation;
	private TextView spinnerProduct;
	private LinearLayout pursed_lv;
	private View vitem;// 视图listview item
	private IAlbumService albumService;
	private IMusicService musicService;
	private IPackService packService;

	private ListviewDataPositionRecorder ldpRecorder;
	private ListView lvContent;
	private List<Album> albums;
	private List<Music> musics;
	private List<Pack> packs;
	
	private List<Album> prealbum;
	private List<Music> preMusic;
	private List<Pack> prePack;
	
	private static final String[] m = { "专辑", "单曲", "主题" };
	private static final String[] M = { "Album", "Music", "Pack" };
	private static final String[] n = { "全部", "在云端", "本地" };
	private static final String[] N = { "All", "Cloud", "Local" };
	
	private ListAdapter prouductAdapter;
	private ListAdapter locationAdapter;
	private int flagItemOrState = 0;// 标记滚动到出现更多
	private long moreDataAnchor=0L;// 标记需要取更多数据的起始位置moreDataAnchor
	private PopupWindow myPopupWindow;
	private ListView lvSpinner;
	private View popview;
	private int firstVisibleItemPosition = -5;// 屏幕滑动第一个可见item的位置
	private int lastVisibleItemPosition;// 屏幕滑动最后一个可见item的位置
	private int objectItemCount;// 列表对象的总数
	private Object object;// 由于listview是动态加载数据，所以每一项的类型是object
	private View imageView;// 专辑图片
	private int scrollTop = 0;
	// private View oncreateview;
	private LinearLayout no_music_ll;

	public static boolean Trigger_From_Cellremote = false; // 由控制制触发从云端下载到本地
	// private SpinnerSelectedListener spl=new SpinnerSelectedListener();
	
	private boolean currentSelectedListLoadComplete = false;// 判断当前列表是否加载完成
	
	public PurchasedFragment() {

	}

	public PurchasedFragment(Context context) {
		this.context = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		view = LayoutInflater.from(getActivity()).inflate(R.layout.purchased_product1, null);

		initComponent();
		initLvContent();
		initMenuListView();
		initMyPopupWindow();
		initListeners();
		lvClick();

		return view;
	}

	private void initLvContent() {
		lvContent = new ScrollOverListView(context);
		pursed_lv.addView(lvContent, 1);
	}

	private void initMenuListView() {
		lvSpinner = (ListView) popview.findViewById(R.id.product_type_state_lv);
		lvSpinner.setLayoutParams(new LinearLayout.LayoutParams(Constant.SCREEN_WIDTH / 2 - 2 * getActivity().getResources().getInteger(R.integer.popup_listview_padding_in_purchase), getActivity().getResources().getInteger(R.integer.popup_listview_height_in_purchase)));
		prouductAdapter = new SpinnerListAdapter(context, m);
		locationAdapter = new SpinnerListAdapter(context, n);
	}

	private void initComponent() {
		no_music_ll = (LinearLayout) view.findViewById(R.id.no_music_tip);
		pursed_lv = (LinearLayout) view.findViewById(R.id.pursed_lv);
		spinnerProduct = (TextView) view.findViewById(R.id.spinnerText);
		spinnerLocation = (TextView) view.findViewById(R.id.spinnerTextState);

		popview = LayoutInflater.from(getActivity()).inflate(R.layout.popupmenu, null);
	}

	private void initMyPopupWindow() {
		// myPopupWindow = new PopupWindow(popview, width, height, true);
		myPopupWindow = new PopupWindow(popview, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		myPopupWindow.setOutsideTouchable(true);
		myPopupWindow.setAnimationStyle(R.style.PopupAnimation);
	}

	private BroadcastReceiver updateListPurchased = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!Trigger_From_Cellremote) {
				updateCurrentDataList();
			}
			Trigger_From_Cellremote = false;
		}
	};

	private void registerReceivers() {
		getActivity().registerReceiver(updateListPurchased, new IntentFilter("updateListPurchased"));
	}

	private void unregisterReceivers() {
		getActivity().unregisterReceiver(updateListPurchased);
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText(FragmentName);
		TabWebActivity.currentMenuItem = FragmentName;
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
	}

	@Override
	public void onResume() {
		Log.e("BUG734", TAG+"onResume()");
		updateTitlebar();

		// if (firstVisibleItemPosition != -5) {
		// lvContent.setSelectionFromTop(firstVisibleItemPosition, scrollTop);
		// }
		getSavedDataAndPosition();

		LinearLayout li = (LinearLayout) lvContent.findViewById(R.id.pulldown_footer_li);
		if (prouductAdapter.getCount() > 20) {
			if (li != null) {
				li.findViewById(R.id.pulldown_footer_loading).setVisibility(View.GONE);
				TextView tv = (TextView) li.findViewById(R.id.pulldown_footer_text);
				tv.setText("已加载完成");
				tv.setVisibility(View.GONE);
			}
		} else {
			li.setVisibility(View.GONE);
			li.findViewById(R.id.pulldown_footer_loading).setVisibility(View.GONE);
			TextView tv = (TextView) li.findViewById(R.id.pulldown_footer_text);
			tv.setVisibility(View.GONE);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		// TabWebActivity.tvTitle.setText("已购音乐");
		// TabWebActivity.currentFragment = "已购音乐";
		// parentActivityChangeTitle();
		super.onPause();
	}

	public void initListeners() {

		spinnerProduct.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					product_or_location = 0;
					if (myPopupWindow.isShowing()) {
						myPopupWindow.dismiss();
					} else {
						lvSpinner.setAdapter(prouductAdapter);
						// myPopupWindow.showAsDropDown(v, 0,
						// Constant.POPUP_Y_OFFSET_IN_PURCHASED);
						myPopupWindow.showAsDropDown(v, 0, getActivity().getResources().getInteger(R.integer.popup_y_offset_in_purchase));
					}
				} catch (Exception e) {
					// Toast.makeText(context, e.getMessage(),
					// Toast.LENGTH_SHORT);
					UpnpApp.mainHandler.showAlert(e.getMessage());
				}
			}
		});

		spinnerLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					product_or_location = 1;
					if (myPopupWindow.isShowing()) {
						myPopupWindow.dismiss();
					} else {
						lvSpinner.setAdapter(locationAdapter);
						// myPopupWindow.showAsDropDown(v, Constant.SCREEN_WIDTH
						// / 2, Constant.POPUP_Y_OFFSET_IN_PURCHASED);
						myPopupWindow.showAsDropDown(v, Constant.SCREEN_WIDTH / 2, getActivity().getResources().getInteger(R.integer.popup_y_offset_in_purchase));
					}
				} catch (Exception e) {
					// Toast.makeText(context, e.getMessage(),
					// Toast.LENGTH_SHORT);
					UpnpApp.mainHandler.showAlert(e.getMessage());
				}
			}
		});

		popview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myPopupWindow.dismiss();
			}
		});

		lvSpinner.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				spinnerTextItemSelected = (TextView) view.findViewById(R.id.spinnertest);
				spinnerTextItemSelected.setBackgroundColor(getResources().getColor(R.color.spinnerItemSeleted));
				spinnerTextItemSelected.setTextColor(getResources().getColor(R.color.spinnerTextColorSeleted));
				doLvSpinnerOnItemClick();

				if (product_or_location == 0) {

					if (position == 0) {
						product_type = 0;
						spinnerProduct.setText("专辑");

						switch (product_state) {
						case 0: {
							onAlbumAllSelected();
							break;
						}
						case 1: {
							onAlbumCloudSelected();
							break;
						}
						case 2: {
							onAlbumLocalSelected();
							break;
						}
						}
					}

					else if (position == 1) {
						product_type = 1;
						spinnerProduct.setText("单曲");

						switch (product_state) {
						case 0: {
							onMusicAllSelected();
							break;
						}
						case 1: {
							onMusicCloudSelected();
							break;
						}
						case 2: {
							onMusicLocalSelected();
							break;
						}
						}
					}

					else if (position == 2) {
						product_type = 2;
						spinnerProduct.setText("主题");

						switch (product_state) {
						case 0: {
							onPackAllSelected();
							break;
						}
						case 1: {
							onPackCloudSelected();
							break;
						}
						case 2: {
							onPackLocalSelected();
							break;
						}
						}
					}
				}

				else if (product_or_location == 1) {
					if (position == 0) {
						product_state = 0;
						spinnerLocation.setText("全部");

						switch (product_type) {
						case 0: {
							onAllAlbumSelected();
							break;
						}
						case 1: {
							onAllMusicSelected();
							break;
						}
						case 2: {
							onAllPackSelected();
							break;
						}
						}
					}

					else if (position == 1) {
						product_state = 1;
						spinnerLocation.setText("云端");

						switch (product_type) {
						case 0: {
							onCloudAlbumSelected();
							break;
						}
						case 1: {
							onCloudMusicSelected();
							break;
						}
						case 2: {
							onCloudPackSelected();
							break;
						}
						}
					} else if (position == 2) {
						product_state = 2;
						spinnerLocation.setText("本地");

						switch (product_type) {
						case 0: {
							onLocalAlbumSelected();
							break;
						}
						case 1: {
							onLocalMusicSelected();
							break;
						}
						case 2: {
							onLocalPackSelected();
							break;
						}
						}
					}
				}

			}
		});

		// 启动滑动监听
		lvContent.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					// 记录列表位置
					firstVisibleItemPosition = lvContent.getFirstVisiblePosition();
					lastVisibleItemPosition = lvContent.getLastVisiblePosition();
					if (lvContent.getChildAt(0) != null) {
						scrollTop = lvContent.getChildAt(0).getTop();
					}

					SingletonUtil.imagflag = true;
					LoadImageAysnc.unlock();
					if (lastVisibleItemPosition < objectItemCount - 4) {
						lastVisibleItemPosition += 2;
					}
					
					for (int i = 0; i < lastVisibleItemPosition - firstVisibleItemPosition; i++) {
						vitem = lvContent.getChildAt(i);
						if (vitem != null) {
							object = lvContent.getItemAtPosition(firstVisibleItemPosition + i);
							if (object instanceof Album) {
								imageView = vitem.findViewById(R.id.iv_album_cover);
								Log.i("kk", ((Album) object).getName() + "----image:" + ((Album) object).getImgUrl());
								imageView.setTag(((Album) object).getImgUrl());
								SingletonUtil.getSingletonUtil().loadAlbumImage((Album) object, lvContent, imageView);
							}
						}
					}

					// 是否需要加载更多数据
					if (flagItemOrState == 1) {
						loadMore(moreDataAnchor);
						flagItemOrState = 0;
					}
					
					// 如果见底且加载完成，提示加载完成
					if(lastVisibleItemPosition>=objectItemCount-1 && currentSelectedListLoadComplete){
						UpnpApp.mainHandler.showInfo(R.string.loading_complete_info);
					}
				}

				// 其它状态
				else {
					SingletonUtil.imagflag = false;
					LoadImageAysnc.lock();
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount > visibleItemCount && firstVisibleItem != 0) {
					lvContent.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);
				} else {
					lvContent.findViewById(R.id.pulldown_footer_li).setVisibility(View.GONE);
				}
				
				if (firstVisibleItem + visibleItemCount == totalItemCount && firstVisibleItem != 0) {
					lvContent.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);
					flagItemOrState = 1;
					moreDataAnchor = firstVisibleItem + visibleItemCount;
					Log.e("BUG969", TAG+"onScroll:firstVisibleItem="+firstVisibleItem+",visibleItemCount="+visibleItemCount+",moreDataAnchor="+moreDataAnchor);
				} else {
					// lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.GONE);
				}
				
				objectItemCount = totalItemCount;
			}
		});

		lvContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				System.out.println(TAG + " lvContent onItemClick");// llparent
																	// onClick

				Object object = lvContent.getItemAtPosition(position);
				if (object instanceof Album) {
					Album album = (Album) object;
					// ((TabWebActivity)getActivity()).goAlbumDetail(album.getId(),album.getName(),
					// BitmapUtil.getBitmap(album.getImgUrl(),150),
					// album.getIsCloud(), new
					// AlbumDao().getAlbumDetailForPurchased(album.getId()));

					Long albumId = album.getId();
					String albumName = album.getName();
					String imgUrl = album.getImgUrl();
					int location = -1;
					AlbumDetail albumDetail = new AlbumDao().getAlbumDetailForPurchased(album.getId());

//					// 记录筛选条件和当前列表位置
//					recordCurrentDataAndPosition();

					WatchDog.tabWebFragment.goAlbumDetail(albumId, albumName, imgUrl, location, albumDetail);
				} else if (object instanceof Music) {
					// TODO
				}
			}
		});

	}

	protected void onLocalPackSelected() {
		Log.e("BUG969", TAG + "onLocalPackSelected");

		spinnerProduct.setText("主题");
		packService = new PackServiceImpl();
		packs = packService.getAllPackList(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 2);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onLocalMusicSelected() {
		Log.e("BUG969", TAG + "onLocalMusicSelected");

		spinnerProduct.setText("单曲");
		musicService = new MusicServiceImpl();
		musics = musicService.getAllMusic(pageIndex, pageSize);
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 2, true);// 本地单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onLocalAlbumSelected() {
		Log.e("BUG969", TAG + "onLocalAlbumSelected");

		spinnerProduct.setText("专辑");
		albumService = new AlbumServiceImpl();
		albums = albumService.getalbumlist(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 2);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onCloudPackSelected() {
		Log.e("BUG969", TAG + "onCloudPackSelected");

		spinnerProduct.setText("主题");
		packService = new PackServiceImpl();
		packs = packService.getPackListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 1);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onCloudMusicSelected() {
		Log.e("BUG969", TAG + "onCloudMusicSelected");

		spinnerProduct.setText("单曲");
		musicService = new MusicServiceImpl();
		musics = musicService.getMusicListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 1, false);// 云端单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onCloudAlbumSelected() {
		Log.e("BUG969", TAG + "onCloudAlbumSelected");

		spinnerProduct.setText("专辑");
		albumService = new AlbumServiceImpl();
		albums = albumService.getAlbumListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 1);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAllPackSelected() {
		Log.e("BUG969", TAG + "onAllPackSelected");

		spinnerProduct.setText("主题");
		packService = new PackServiceImpl();
		packs = packService.getAllPackList(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 0);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAllMusicSelected() {
		Log.e("BUG969", TAG + "onAllMusicSelected");

		spinnerProduct.setText("单曲");
		musicService = new MusicServiceImpl();
		musics = musicService.getAllMusicList(pageIndex, pageSize);
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 0, true);// 全部单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAllAlbumSelected() {
		Log.e("BUG969", TAG + "onAllAlbumSelected");

		spinnerProduct.setText("专辑");
		albumService = new AlbumServiceImpl();
		albums = albumService.getAllAlbumList(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 0);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onPackLocalSelected() {
		Log.e("BUG969", TAG + "onPackLocalSelected");

		spinnerLocation.setText("本地");
		packService = new PackServiceImpl();
		packs = packService.getAllPack(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 2);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onPackCloudSelected() {
		Log.e("BUG969", TAG + "onPackCloudSelected");

		spinnerLocation.setText("云端");
		packService = new PackServiceImpl();
		packs = packService.getPackListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 1);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onPackAllSelected() {
		Log.e("BUG969", TAG + "onPackAllSelected");

		spinnerLocation.setText("全部");
		packService = new PackServiceImpl();
		packs = packService.getAllPackList(pageIndex, pageSize);
		listadapter = new ExtendsPackListAdapter(getActivity(), packs, lvContent, 0);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onMusicLocalSelected() {
		Log.e("BUG969", TAG + "onMusicLocalSelected");

		spinnerLocation.setText("本地");
		musicService = new MusicServiceImpl();
		musics = musicService.getAllMusic();
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 2, true);// 本地单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onMusicCloudSelected() {
		Log.e("BUG969", TAG + "onMusicCloudSelected");

		spinnerLocation.setText("云端");
		musicService = new MusicServiceImpl();
		musics = musicService.getMusicListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 1, false);// 云端单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onMusicAllSelected() {
		Log.e("BUG969", TAG + "onMusicAllSelected");

		spinnerLocation.setText("全部");
		musicService = new MusicServiceImpl();
		musics = musicService.getAllMusicList(pageIndex, pageSize);
		listadapter = new ExtendsMusicListAdapter(getActivity(), musics, 0, true);// 全部单曲
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAlbumLocalSelected() {
		Log.e("BUG969", TAG + "onAlbumLocalSelected");

		spinnerLocation.setText("本地");
		albumService = new AlbumServiceImpl();
		albums = albumService.getalbumlist(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 2);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAlbumCloudSelected() {
		Log.e("BUG969", TAG + "onAlbumCloudSelected");

		spinnerLocation.setText("在云端");
		albumService = new AlbumServiceImpl();
		albums = albumService.getAlbumListForCloud(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 1);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void onAlbumAllSelected() {
		Log.e("BUG969", TAG + "onAlbumAllSelected");

		spinnerLocation.setText("全部");
		albumService = new AlbumServiceImpl();
		albums = albumService.getAllAlbumList(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 0);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	protected void doLvSpinnerOnItemClick() {
		View lvfoot = lvContent.findViewById(R.id.pulldown_footer_li);
		lvfoot.findViewById(R.id.pulldown_footer_loading).setVisibility(View.VISIBLE);
		TextView tv = (TextView) lvfoot.findViewById(R.id.pulldown_footer_text);
		tv.setText("加载更多中...");

		// spinnerTextItemSelected = (TextView)
		// view.findViewById(R.id.spinnertest);
		// spinnerTextItemSelected.setBackgroundColor(getResources().getColor(R.color.spinnerItemSeleted));
		// spinnerTextItemSelected.setTextColor(getResources().getColor(R.color.spinnerTextColorSeleted));

		if (myPopupWindow != null) {
			myPopupWindow.dismiss();
		}
	}

	protected void recordCurrentDataAndPosition() {
		Log.e("BUG734", TAG + "recordCurrentDataAndPosition");

		ldpRecorder = new ListviewDataPositionRecorder();
		// 保存当前列表位置
		ldpRecorder.setFirstVisibleItemPosition(firstVisibleItemPosition);
		ldpRecorder.setScrollTop(scrollTop);
		// 保存当前spinner选项
		ldpRecorder.setDataArray(new Integer[] { product_type, product_state });
		// 保存当前分页锚记
		HashMap<String, Object> dataMap=new HashMap<String, Object>();
		dataMap.put("moreDataAnchor", moreDataAnchor);
		ldpRecorder.setDataMap(dataMap);
		// 保存对象
		WatchDog.listviewPositionMap.put(TAG, ldpRecorder);
		Log.e("BUG734", TAG + "lpRecorder=" + ldpRecorder);
	}

	private void getSavedDataAndPosition() {
		Log.e("BUG734", TAG + "getSavedDataAndPosition");
		ListviewDataPositionRecorder recorder = WatchDog.listviewPositionMap.get(TAG);
		Log.e("BUG734", TAG + "recorder=" + recorder);

		if (recorder != null) {
			// 拿取设置spinner选项
			product_type = (Integer) recorder.getDataArray()[0];
			product_state = (Integer) recorder.getDataArray()[1];
			spinnerProduct.setText(m[product_type]);
			spinnerLocation.setText(n[product_state]);

			//模拟用户切换spinner
			doLvSpinnerOnItemClick();
			onTypeStateSelected(product_type, product_state);
			
			//模拟多次拿取分页数据
			try {
				mimeLoadMore((Long)(recorder.getDataMap().get("moreDataAnchor")));//nullPointer
			} catch (Exception e) {
				Log.e("BUG734", TAG + "exception cautht!!!!!!!e=" + e);
				Log.e("BUG734", TAG + "recorder=" + recorder);
				e.printStackTrace();
			}

			// 拿取设置当前列表位置
			lvContent.setSelectionFromTop(recorder.getFirstVisibleItemPosition(), recorder.getScrollTop());
		}
	}

	private void mimeLoadMore(Long itemsCount) {
		Log.e("BUG734", TAG + "mimeLoadMore:itemsCount="+itemsCount);
		
		int num=(Integer.parseInt(itemsCount+""))/pageSize;
		for(int i=1;i<=num;i++){
			loadMore(i*pageSize+2);
		}		
	}

	private void onTypeStateSelected(int _product_type, int _product_state) {
		Log.e("BUG969", TAG + "onTypeStateSelected");
		// 反射方法和常规方法二选一：
		onTypeStateSelectedReflecedly(_product_type, _product_state);
		// onTypeStateSelectedNormally(_product_type,_product_state);
	}

	private void onTypeStateSelectedNormally(int _product_type, int _product_state) {

		switch (_product_type) {
		case 0: // 专辑
			if (_product_state == 0) {// 全部
				onAlbumAllSelected();
			} else if (_product_state == 1) {// 云端
				onAlbumCloudSelected();
			} else if (_product_state == 2) {// 本地
				onAlbumLocalSelected();
			}
			break;

		case 1: // 单曲
			if (_product_state == 0) {// 全部
				onMusicAllSelected();
			} else if (_product_state == 1) {// 云端
				onMusicCloudSelected();
			} else if (_product_state == 2) {// 本地
				onMusicLocalSelected();
			}
			break;

		case 2: // 主题
			if (_product_state == 0) {// 全部
				onPackAllSelected();
			} else if (_product_state == 1) {// 云端
				onPackCloudSelected();
			} else if (_product_state == 2) {// 本地
				onPackLocalSelected();
			}
			break;
		}
	}

	private void onTypeStateSelectedReflecedly(int _product_type, int _product_state) {
		Log.e("BUG969", TAG + "onTypeStateSelectedReflecedly");
		String methodName = "on" + M[_product_type] + N[_product_state] + "Selected";
		Log.e("BUG969", TAG + "onTypeStateSelected:methodName=" + methodName);

		try {
			Method m = PurchasedFragment.class.getDeclaredMethod(methodName);
			m.invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("BUG969", TAG + "onTypeStateSelected:Exception caught=" + e);
		}
	}

	/**
	 * 加载更多数据
	 */
	public void loadMore(final long index) {
		
		View lview = lvContent.findViewById(R.id.pulldown_footer_li);
		// lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);

		switch (product_type) {
		//专辑
		case 0: {
			switch (product_state) {
			case 0:
				prealbum = new AlbumServiceImpl().getAllAlbumList(index, pageSize);
				reconsitutionMore(1, lview);
				break;

			case 1:
				prealbum = new AlbumServiceImpl().getAlbumListForCloud(index, pageSize);
				reconsitutionMore(1, lview);
				break;

			case 2:
				prealbum = new AlbumServiceImpl().getalbumlist(index, pageSize);
				reconsitutionMore(1, lview);
				break;
			}
			break;
		}

		//单曲
		case 1: {
			switch (product_state) {
			case 0: {
				preMusic = new MusicServiceImpl().getAllMusicList(index, pageSize);
				reconsitutionMore(5, lview);
				break;
			}
			case 1: {
				preMusic = new MusicServiceImpl().getMusicListForCloud(index, pageSize);
				reconsitutionMore(5, lview);
				break;
			}
			case 2: {
				preMusic = new MusicServiceImpl().getAllMusic(index, pageSize);
				reconsitutionMore(5, lview);
				break;
			}
			}
			break;
		}

		//主题
		case 2: {
			switch (product_state) {
			case 0: {
				prePack = new PackServiceImpl().getAllPackList(index, pageSize);
				reconsitutionMore(15, lview);
				break;
			}
			case 1: {
				prePack = new PackServiceImpl().getPackListForCloud(index, pageSize);
				reconsitutionMore(15, lview);
				break;
			}
			case 2: {
				prePack = new PackServiceImpl().getAllPack(index, pageSize);
				reconsitutionMore(15, lview);
				break;
			}
			}

			break;
		}
		}

	}

	public void lvClick() {
		albumService = new AlbumServiceImpl();
		albums = albumService.getAllAlbumList(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), albums, lvContent, 0);
		setIfShowMusic(listadapter);
		lvContent.setAdapter(listadapter);
	}

	/**
	 * 重构加载更多 type 1 专辑 5单曲 15主题
	 */
	public void reconsitutionMore(int type, View view) {

		switch (type) {
		case 1: {
			if (albums != null && prealbum != null) {
				albums.addAll(albums != null ? albums.size() : 0, prealbum);
				listadapter.notifyDataSetChanged();
				updateFootView(view, 2);
			} else {
				updateFootView(view, 1);

			}
			break;
		}

		case 5: {
			if (musics != null && preMusic != null) {
				musics.addAll(musics != null ? musics.size() : 0, preMusic);
				listadapter.notifyDataSetChanged();
				updateFootView(view, 2);
			} else {
				updateFootView(view, 1);
			}
			break;
		}

		case 15: {
			if (packs != null && prePack != null) {
				packs.addAll(packs != null ? packs.size() : 0, prePack);
				listadapter.notifyDataSetChanged();
				updateFootView(view, 2);
			} else {
				updateFootView(view, 1);
			}
			break;

		}
		}

	}

	/**
	 * 重构切换菜单加载数据
	 */

	/**
	 * 加载完成更改底部布局
	 */
	public void updateFootView(View lview, int state) {

		TextView tv = null;
		View view = null;
		if (lview != null) {
			tv = (TextView) lview.findViewById(R.id.pulldown_footer_text);
			view = lview.findViewById(R.id.pulldown_footer_loading);
			initLoadingmoreAnim((ImageView)view);
			
			if (state == 1) {
				currentSelectedListLoadComplete = true;
				view.setVisibility(View.GONE);
				tv.setText("已加载完成");
				tv.setVisibility(View.GONE);
				
			} else if (state == 2) {
				currentSelectedListLoadComplete = false;				
				view.setVisibility(View.VISIBLE);
				tv.setText("正在加载...");
				tv.setVisibility(View.VISIBLE);
			}
		}

	}
	
	private void initLoadingmoreAnim(ImageView ivAnim) {		
		new AnimUtil(context).initLoadingmoreAnim(ivAnim);
		
//		AnimationDrawable ad=(AnimationDrawable)getResources().getDrawable(R.anim.login_seraching_anim); 
//		ivAnim.setBackgroundDrawable(ad); 		
//		ad.start();
	}

	/**
	 * 更新当前列表数据
	 */
	public void updateCurrentDataList() {

		if (product_type == 0) {
			switch (product_state) {
			case 0: {
				albums = albumService.getAllAlbumList(0, pageSize);

				break;
			}
			case 1: {
				albums = albumService.getAlbumListForCloud(0, pageSize);

				break;
			}
			case 2: {
				albums = albumService.getalbumlist(0, pageSize);
				break;
			}
			}
			((ExtendsAlbumListAdapter) listadapter).setLialbum(albums);
		} else if (product_type == 1) {
			switch (product_state) {
			case 0: {
				musics = musicService.getAllMusicList(0, pageSize);
				break;
			}
			case 1: {
				musics = musicService.getMusicListForCloud(0, pageSize);
				break;
			}
			case 2: {
				musics = musicService.getAllMusic(0, pageSize);
				break;
			}
			}
			((ExtendsMusicListAdapter) listadapter).setLiMusic(musics);
		} else if (product_type == 2) {
			switch (product_state) {
			case 0: {
				packs = packService.getAllPackList(0, pageSize);
				break;
			}
			case 1: {
				packs = packService.getPackListForCloud(0, pageSize);
				break;
			}
			case 2: {
				packs = packService.getAllPack(0, pageSize);
				break;
			}
			}
			((ExtendsPackListAdapter) listadapter).setLiPack(packs);
		}
		if (listadapter != null) {
			// listadapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onDestroyView() {
		Log.e("BUG734", TAG+"onDestroyView()");
		recordCurrentDataAndPosition();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		// unregisterReceivers();
		super.onDestroy();
	}

	public void setIfShowMusic(BaseAdapter listadapter) {
		if (listadapter != null) {
			if (no_music_ll != null) {
				if (!(listadapter.getCount() > 0)) {

					no_music_ll.setVisibility(View.VISIBLE);
				} else {
					no_music_ll.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(false);
		WatchDog.tabWebFragment.setTitle("已购音乐");
	}

}
