package com.union.cellremote.store;

import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.adapter.SpinnerListAdapter;
import com.dfim.app.dao.AlbumDao;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.Music;
import com.dfim.app.domain.Pack;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.LoadImageAysnc;
import com.dfim.app.util.SingletonUtil;
import com.dfim.app.widget.ScrollOverListView;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ExtendsAlbumListAdapter;
import com.union.cellremote.adapter.ExtendsMusicListAdapter;
import com.union.cellremote.adapter.ExtendsPackListAdapter;
import com.union.cellremote.service.IAlbumService;
import com.union.cellremote.service.IMusicService;
import com.union.cellremote.service.IPackService;
import com.union.cellremote.service.impl.AlbumServiceImpl;
import com.union.cellremote.service.impl.MusicServiceImpl;
import com.union.cellremote.service.impl.PackServiceImpl;

public class PurchasedFragment extends Fragment {
	public static String FragmentName="已购音乐";
	private final String TAG = "PurchasedFragment";
	public static BaseAdapter listadapter;
	public int product_type_state;// 区分是点击类型还是状态
	public int product_type = 0;// 商品类型 0 专辑 1单曲，2主题
	public int product_state;// 商品状态
	private long pageIndex = 0;// 起始页码
	private int pageSize = 20;// 每页的大小
	private Context context;
	private View view;
	private View headview;
	private TextView spinnerTextItemSelected;
	private TextView spinnerTextState;
	private TextView spinnerText;
	private LinearLayout pursed_lv;
	private View vitem;// 视图listview item
	private IAlbumService albumService;
	private IMusicService musicService;
	private IPackService packService;
	private ListView lvAlbum;
	private List<Album> lialbum;
	private List<Music> liMusic;
	private List<Pack> liPack;
	private List<Album> prealbum;
	private List<Music> preMusic;
	private List<Pack> prePack;
	private static final String[] m = { "专辑", "单曲", "主题" };
	private static final String[] n = { "全部", "云端", "本地" };
	private ListAdapter adapter;
	private ListAdapter adapterstate;
	private int flagItemOrState = 0;// 标记滚动到出现更多
	private long moreDataAnchor;// 标记需要取更多数据的起始位置
	private PopupWindow m_popupWindow;
	private ListView product_type_state_lv;
	private View popview;
	private int st=-5;// 屏幕滑动第一个可见item的位置
	private int end;// 屏幕滑动最后一个可见item的位置
	private int objectItemCount;// 列表对象的总数
	private Object object;// 由于listview是动态加载数据，所以每一项的类型是object
	private View imageView;// 专辑图片
	private int scrollTop=0;
	private View oncreateview; 
	private LinearLayout no_music_ll;
	
	public static boolean Trigger_From_Cellremote =false; //由控制制触发从云端下载到本地
	
	// private SpinnerSelectedListener spl=new SpinnerSelectedListener();
	public PurchasedFragment() {

	}

	public PurchasedFragment(Context context) {
		this.context = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		registerReceivers();
		parentActivityChangeButton();
		parentActivityChangeTitle();
		view = LayoutInflater.from(getActivity()).inflate(R.layout.purchased_product1, null);
		no_music_ll= (LinearLayout) view.findViewById(R.id.no_music_tip);
		pursed_lv = (LinearLayout) view.findViewById(R.id.pursed_lv);
		lvAlbum = new ScrollOverListView(context);
		pursed_lv.addView(lvAlbum, 1);
		spinnerText = (TextView) view.findViewById(R.id.spinnerText);
		spinnerTextState = (TextView) view.findViewById(R.id.spinnerTextState);
		popview = LayoutInflater.from(getActivity()).inflate(R.layout.popupmenu, null);
		// m_popupWindowState=LayoutInflater.from(getActivity()).inflate(R.layout.popupmenu,
		// null);
		product_type_state_lv = (ListView) popview.findViewById(R.id.product_type_state_lv);
		adapter = new SpinnerListAdapter(context, m);
		adapterstate = new SpinnerListAdapter(context, n);
		m_popupWindow = new PopupWindow(popview, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		m_popupWindow.setOutsideTouchable(true);
		m_popupWindow.setAnimationStyle(R.style.PopupAnimation);
		spinnerText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					product_type_state = 0;
					if (m_popupWindow.isShowing()) {
						m_popupWindow.dismiss();
					} else {

						product_type_state_lv.setAdapter(adapter);
						m_popupWindow.showAsDropDown(v, 110, 0);
					}
				} catch (Exception e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
				}
			}
		});
		spinnerTextState.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					product_type_state = 1;
					if (m_popupWindow.isShowing()) {
						m_popupWindow.dismiss();
					} else {
						product_type_state_lv.setAdapter(adapterstate);
						m_popupWindow.showAsDropDown(v, 110, 0);
					}
				} catch (Exception e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
				}
			}
		});
		popview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_popupWindow.dismiss();

			}
		});
		listenner();
		lvClick();
		oncreateview=view;
		//return view;
	}

	private BroadcastReceiver updateListPurchased = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		    if(!Trigger_From_Cellremote){
		    	updateCurrentDataList();
		    }
		    Trigger_From_Cellremote=false;
		}
	};
	private void registerReceivers(){
		getActivity().registerReceiver(updateListPurchased,new IntentFilter("updateListPurchased"));
	}
	private void unregisterReceivers(){
		getActivity().unregisterReceiver(updateListPurchased);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return oncreateview;
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
		if(st!=-5){
			lvAlbum.setSelectionFromTop(st,scrollTop);
		}
		LinearLayout li=(LinearLayout) lvAlbum.findViewById(R.id.pulldown_footer_li);
		if(adapter.getCount()>20){
			if(li!=null){
				li.findViewById(R.id.pulldown_footer_loading).setVisibility(View.GONE);
				TextView tv=(TextView) li.findViewById(R.id.pulldown_footer_text);
				tv.setText("已加载完成");
				
			}
		}else {
			li.setVisibility(View.GONE);
			li.findViewById(R.id.pulldown_footer_loading).setVisibility(View.GONE);
			TextView tv=(TextView) li.findViewById(R.id.pulldown_footer_text);
			tv.setVisibility(View.GONE);
		}
	    super.onResume();
	}

	@Override
	public void onPause() {
//		TabWebActivity.tvTitle.setText("已购音乐");
//		TabWebActivity.currentFragment = "已购音乐";
		parentActivityChangeTitle();
		super.onPause();
	}

	public void listenner() {
		product_type_state_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				spinnerTextItemSelected = (TextView) view.findViewById(R.id.spinnertest);
				spinnerTextItemSelected.setBackgroundColor(getResources().getColor(R.color.spinnerItemSeleted));
				spinnerTextItemSelected.setTextColor(getResources().getColor(R.color.spinnerTextColorSeleted));

				View lvfoot =lvAlbum.findViewById(R.id.pulldown_footer_li);
				lvfoot.findViewById(R.id.pulldown_footer_loading).setVisibility(View.VISIBLE);
				 TextView tv=(TextView) lvfoot.findViewById(R.id.pulldown_footer_text);
				 tv.setText("加载更多中...");
				spinnerTextItemSelected = (TextView) view
						.findViewById(R.id.spinnertest);
				spinnerTextItemSelected.setBackgroundColor(getResources()
						.getColor(R.color.spinnerItemSeleted));
				spinnerTextItemSelected.setTextColor(getResources().getColor(
						R.color.spinnerTextColorSeleted));
				if (product_type_state == 0) {
					if (position == 0) {
						product_type = 0;
						spinnerText.setText("专辑");
						switch (product_state) {
						case 0: {
							spinnerTextState.setText("全部");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getAllAlbumList(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 1: {
							spinnerTextState.setText("云端");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getAlbumListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 2: {
							spinnerTextState.setText("本地");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getalbumlist(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						}

					} else if (position == 1) {
						product_type = 1;
						spinnerText.setText("单曲");
						switch (product_state) {
						case 0: {
							spinnerTextState.setText("全部");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getAllMusicList(pageIndex, pageSize);
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 1: {
							spinnerTextState.setText("云端");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getMusicListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 2: {
							spinnerTextState.setText("本地");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getAllMusic();
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						}
					} else if (position == 2) {
						product_type = 2;
						spinnerText.setText("主题");
						switch (product_state) {
						case 0: {
							spinnerTextState.setText("全部");
							packService = new PackServiceImpl();
							liPack = packService.getAllPackList(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						case 1: {
							spinnerTextState.setText("云端");
							packService = new PackServiceImpl();
							liPack = packService.getPackListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						case 2: {
							spinnerTextState.setText("本地");
							packService = new PackServiceImpl();
							liPack = packService.getAllPack(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						}
					}

				} else if (product_type_state == 1) {
					if (position == 0) {
						product_state = 0;
						spinnerTextState.setText("全部");
						switch (product_type) {
						case 0: {
							spinnerText.setText("专辑");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getAllAlbumList(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 1: {
							spinnerText.setText("单曲");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getAllMusicList(pageIndex, pageSize);
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						case 2: {
							spinnerText.setText("主题");
							packService = new PackServiceImpl();
							liPack = packService.getAllPackList(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,0);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						}
					} else if (position == 1) {
						product_state = 1;

						spinnerTextState.setText("云端");
						switch (product_type) {
						case 0: {
							spinnerText.setText("专辑");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getAlbumListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 1: {
							spinnerText.setText("单曲");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getMusicListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						case 2: {
							spinnerText.setText("主题");
							packService = new PackServiceImpl();
							liPack = packService.getPackListForCloud(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,1);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						}
					} else if (position == 2) {
						product_state = 2;
						spinnerTextState.setText("本地");
						switch (product_type) {
						case 0: {
							spinnerText.setText("专辑");
							albumService = new AlbumServiceImpl();
							lialbum = albumService.getalbumlist(pageIndex, pageSize);
							listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						case 1: {
							spinnerText.setText("单曲");
							musicService = new MusicServiceImpl();
							liMusic = musicService.getAllMusic(pageIndex, pageSize);
							listadapter = new ExtendsMusicListAdapter(getActivity(), liMusic,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);

							break;
						}
						case 2: {
							spinnerText.setText("主题");
							packService = new PackServiceImpl();
							liPack = packService.getAllPackList(pageIndex, pageSize);
							listadapter = new ExtendsPackListAdapter(getActivity(), liPack, lvAlbum,2);
							setIfShowMusic(listadapter);
							lvAlbum.setAdapter(listadapter);
							break;
						}
						}
					}
				}
				if (m_popupWindow != null) {

					m_popupWindow.dismiss();
				}
			}
		});

		lvAlbum.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					st = lvAlbum.getFirstVisiblePosition();
					
					 if(lvAlbum.getChildAt(0)!=null){
						 scrollTop=lvAlbum.getChildAt(0).getTop();
					 }
					
					end = lvAlbum.getLastVisiblePosition();
					SingletonUtil.imagflag = true;
					LoadImageAysnc.unlock();
					if (end < objectItemCount - 4) {
						end += 2;
					}
					for (int i = 0; i < end - st ; i++) {
						vitem = lvAlbum.getChildAt(i);
						if (vitem != null) {
							object = lvAlbum.getItemAtPosition(st + i);
							if (object instanceof Album) {
								imageView = vitem.findViewById(R.id.iv_album_cover);
							Log.i("kk", ((Album) object).getName()+"----image:"+((Album) object).getImgUrl());
								imageView.setTag(((Album) object).getImgUrl());
								SingletonUtil.getSingletonUtil().loadAlbumImage((Album) object, lvAlbum, imageView);
							}
						}
					}
					if (flagItemOrState == 1) {
						loadingMore(moreDataAnchor);
						flagItemOrState = 0;
					}
				} else {
					SingletonUtil.imagflag = false;
					LoadImageAysnc.lock();

				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount > visibleItemCount && firstVisibleItem != 0) {
					lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);
				} else {
					lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.GONE);
				}
				if (firstVisibleItem + visibleItemCount == totalItemCount && firstVisibleItem != 0) {
					lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);
					flagItemOrState = 1;
					moreDataAnchor = firstVisibleItem + visibleItemCount;

				} else {
					// lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.GONE);
				}
				objectItemCount = totalItemCount;
			}
		});
		lvAlbum.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object object = lvAlbum.getItemAtPosition(position);
				if (object instanceof Album) {
					Album album = (Album) object;
					((TabWebActivity) getActivity()).goAlbumDetail(album.getId(), album.getName(), BitmapUtil.getBitmap(album.getImgUrl(), 150), album.getIsCloud(), new AlbumDao().getAlbumDetailForPurchased(album.getId()));
				} else if (object instanceof Music) {

				}
			}
		});

	}

	/**
	 * 加载更多数据
	 */
	public void loadingMore(final long index) {

		View lview = lvAlbum.findViewById(R.id.pulldown_footer_li);
		// lvAlbum.findViewById(R.id.pulldown_footer_li).setVisibility(View.VISIBLE);
		switch (product_type) {
		case 0: {
			switch (product_state) {
			case 0: {
				prealbum = new AlbumServiceImpl().getAllAlbumList(index, pageSize);
				reconsitutionMore(1, lview);
				break;
			}
			case 1: {
				prealbum = new AlbumServiceImpl().getAlbumListForCloud(index, pageSize);
				reconsitutionMore(1, lview);
				break;
			}
			case 2: {
				prealbum = new AlbumServiceImpl().getalbumlist(index, pageSize);
				reconsitutionMore(1, lview);
				break;
			}
			}

			break;

		}
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
		lialbum = albumService.getAllAlbumList(pageIndex, pageSize);
		listadapter = new ExtendsAlbumListAdapter(getActivity(), lialbum, lvAlbum,0);
		setIfShowMusic(listadapter);
		lvAlbum.setAdapter(listadapter);
	}

	/**
	 * 重构加载更多 type 1 专辑 5单曲 15主题
	 */
	public void reconsitutionMore(int type, View view) {

		switch (type) {
		case 1: {
			if (lialbum != null && prealbum != null) {
				lialbum.addAll(lialbum != null ? lialbum.size() : 0, prealbum);
				listadapter.notifyDataSetChanged();
				updateFootView(view, 2);
			} else {
				updateFootView(view, 1);

			}
			break;
		}

		case 5: {
			if (liMusic != null && preMusic != null) {
				liMusic.addAll(liMusic != null ? liMusic.size() : 0, preMusic);
				listadapter.notifyDataSetChanged();
				updateFootView(view, 2);
			} else {
				updateFootView(view, 1);
			}
			break;
		}

		case 15: {
			if (liPack != null && prePack != null) {
				liPack.addAll(liPack != null ? liPack.size() : 0, prePack);
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
			if (state == 1) {
				view.setVisibility(View.GONE);
				tv.setText("已加载完成!");

				// tv.setVisibility(View.GONE);
			} else if (state == 2) {
				view.setVisibility(View.VISIBLE);
				tv.setText("加载更多中...");
				tv.setVisibility(View.VISIBLE);
			}
		}

	}
	/**
	 * 更新当前列表数据
	 */
	public void updateCurrentDataList(){
		
		if(product_type==0){
			switch(product_state){
			case 0:{
				lialbum=albumService.getAllAlbumList(0, pageSize);
				
				break;
			}
			case 1:{
				lialbum=albumService.getAlbumListForCloud(0, pageSize);
				
				break;
			}
			case 2:{
				lialbum=albumService.getalbumlist(0, pageSize);
				break;
			}
			}
			((ExtendsAlbumListAdapter)listadapter).setLialbum(lialbum);
		}else if(product_type==1){
			switch(product_state){
			case 0:{
				liMusic=musicService.getAllMusicList(0, pageSize);
				break;
			}
			case 1:{
			    liMusic=musicService.getMusicListForCloud(0, pageSize);
				break;
			}
			case 2:{
				liMusic=musicService.getAllMusic(0, pageSize);
				break;
			}
			}
		  ((ExtendsMusicListAdapter)listadapter).setLiMusic(liMusic);
		}else if (product_type==2){
			switch(product_state){
			case 0:{
				liPack=packService.getAllPackList(0, pageSize);
				break;
			}
			case 1:{
				liPack=packService.getPackListForCloud(0, pageSize);
				break;
			}
			case 2:{
				liPack=packService.getAllPack(0, pageSize);
				break;
			}
			}
			((ExtendsPackListAdapter)listadapter).setLiPack(liPack);
		}
		if(listadapter!=null){
		//	listadapter.notifyDataSetChanged();
		}
		
	}
	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}
	public void setIfShowMusic(BaseAdapter listadapter){
		if(listadapter!=null){
			if(no_music_ll!=null){
			if(!(listadapter.getCount()>0)){
			 
				 no_music_ll.setVisibility(View.VISIBLE);
			 }else{
				 no_music_ll.setVisibility(View.GONE);
			 }
			}
		}
	}



}
