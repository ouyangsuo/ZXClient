package com.dfim.app.fragment.mymusic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.common.BroadcastManager;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.AlbumDao;
import com.dfim.app.domain.AlbumDetail;
import com.dfim.app.domain.Disk;
import com.dfim.app.domain.Music;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.fragment.TabMusicFragment;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.PowerfulBigMan;
import com.union.cellremote.R;

//notifyData，正在播放
public class AlbumDetailFragment extends BaseFragment {

	private final String TAG = AlbumDetailFragment.class.getSimpleName();

	private Activity attachedActivity;
	private TabMusicFragment tabMusicFragment;
	
	private AlbumDetail albumdetail;
	private List<Disk> diskli;
	private ArrayList<Music> theFirstDiskList;
	private ExpandableListView expandablelistview;
	private BaseExpandableListAdapter adapter;
	private View view;
	private AlbumDetail albumd;
//	private ImageView ivPlayAlbum; //播放专辑 - 图标
	private LinearLayout llPlayalbum; //播放专辑 - 区域（layout）
	private LinearLayout ll_clearcache;

	public AlbumDetailFragment() {
		Log.i(TAG, "AlbumDetailFragment");
	}

	public AlbumDetailFragment(Activity context, TabMusicFragment tabMusicFragment, AlbumDetail albumdetail) {
		Log.i(TAG, "AlbumDetailFragment(context,albumdetail) getArtistName=" + albumdetail.getArtistName());
		this.attachedActivity = context;
		this.tabMusicFragment = tabMusicFragment;
		
		WatchDog.clearCacheProductType=5;
		this.albumdetail = albumdetail;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		WatchDog.keybackRefersExitPro = false;// 返回键指向界面回退
		view = inflater.inflate(R.layout.album_detail_fragment, null);// 异常：曾报空指针
		initComponents();
		initArguments();
		initListeners();
		registerReceivers();
		return view;
	}
	
	public String getFragmentName(){
		return TAG;
	}
	
	private void initComponents() {
		llPlayalbum = (LinearLayout) view.findViewById(R.id.ll_playalbum);
		ll_clearcache = (LinearLayout) view.findViewById(R.id.ll_clearcache);
//		ivPlayAlbum = (ImageView) view.findViewById(R.id.iv_play_album);
		expandablelistview = (ExpandableListView) view.findViewById(R.id.expandlistview);
		expandablelistview.setGroupIndicator(null);
	}

	private void initListeners() {

		llPlayalbum.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 * 新的播放专辑方法
				 */
				ArrayList<Music> list = new ArrayList<Music>();
				for (Disk _disk : diskli) {
					list.addAll(_disk.getMusicList());
				}

				// 初始化播放列表
				WatchDog.currentList = list;
				WatchDog.currentListType = Constant.URI_ALBUM;
				WatchDog.currentListId = albumdetail.getAlbumId();

				WatchDog.currentPlayingIndex = 0;
				WatchDog.currentPlayingMusic = list.get(0);
				WatchDog.currentPlayingId = WatchDog.currentPlayingMusic.getId();
				WatchDog.currentPlayingName = list.get(0).getName();
				WatchDog.currentArtistName = list.get(0).getArtistName();
				System.out.println("album detail: WatchDog.currentArtistName=" + WatchDog.currentArtistName);

				WatchDog.updateCachingState();// 如果上一曲未缓存完则修改其状态为等待
				WatchDog.currentState = PlayerFragment.STOPPED;// 先改为停止以停止播放器图标的跳动

				UIHelper.refreshPlayerButton();// 点击曲目后立即停止图标跳动
				
				adapter.notifyDataSetChanged();

				String uri = list.get(0).getUri(Constant.URI_ALBUM, albumdetail.getAlbumId());
				playMusic(uri);

			}
		});
		
		ll_clearcache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("BUG975", "1>>"+TAG+"ll_clearcache onClick()");
				
				AlbumDetail albumDetail = albumdetail;
				if (albumDetail != null && albumDetail.getDisklist() != null) {
					CacheAlbumDetailFragment detail = new CacheAlbumDetailFragment(attachedActivity, tabMusicFragment, albumDetail);
					tabMusicFragment.showCacheAlbumDetailFragment(detail);
				} else {
//					CustomToast.makeText(attachedActivity, "该专辑数据有误", Toast.LENGTH_SHORT).show();
					Log.e(TAG, "该专辑数据有误");
				}
			
			}

		});

		expandablelistview.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (PowerfulBigMan.testClickInterval() == false) {
					return false;
				}

				System.out.println("child click:" + groupPosition + ":" + childPosition);
				ArrayList<Music> list = (ArrayList<Music>) diskli.get(groupPosition).getMusicList();

				// 初始化播放列表
				WatchDog.currentList = list;
				WatchDog.currentListType = Constant.URI_ALBUM;
				WatchDog.currentListId = albumdetail.getAlbumId();

				WatchDog.currentPlayingIndex = childPosition;
				WatchDog.currentPlayingMusic = list.get(childPosition);
				WatchDog.currentPlayingId = WatchDog.currentPlayingMusic.getId();
				WatchDog.currentPlayingName = list.get(childPosition).getName();
				WatchDog.currentArtistName = list.get(childPosition).getArtistName();
				System.out.println("album detail: WatchDog.currentArtistName=" + WatchDog.currentArtistName);

				WatchDog.updateCachingState();// 如果上一曲未缓存完则修改其状态为等待
				WatchDog.currentState = PlayerFragment.STOPPED;// 先改为停止以停止播放器图标的跳动

				// initBtnPlayer();
				UIHelper.refreshPlayerButton();// 点击曲目后立即停止图标跳动
				adapter.notifyDataSetChanged();

				String uri = list.get(childPosition).getUri(WatchDog.currentListType, WatchDog.currentListId);
				playMusic(uri);

				return true;
			}
		});

	}

	private void playMusic(String uri) {
		Player p = new Player();
		p.play(uri);
	}

	class ChildHolder {
		private TextView tvNum;
		private TextView tvName;
		private TextView tvArtist;
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private TextView ivPlaying;

		ChildHolder(View convertView) {
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
			ivSavingState = (ImageView) convertView.findViewById(R.id.iv_saving_state);
			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);
			ivPlaying = (TextView) convertView.findViewById(R.id.iv_playing);
		}
	}
	public String getAlbumName(){
		//专辑名
		String albumName = "";
		if(null!=albumdetail){
			albumName = albumdetail.getAlbumname();
		}else{
			Log.e(TAG, "专辑名称为空");
			albumName = "未知";
		}
		return albumName;
	}
	private void initArguments() {
//		initBtnPlayer();
		diskli = AlbumDetailFragment.this.albumdetail.getDisklist();
		if (diskli != null&&diskli.size()>0) {
			
			theFirstDiskList = (ArrayList<Music>) diskli.get(0).getMusicList();
		}

		adapter = new BaseExpandableListAdapter() {
			TextView getTextView() {
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
				TextView textView = new TextView(AlbumDetailFragment.this.attachedActivity);
				textView.setLayoutParams(lp);
				textView.setBackgroundResource(R.color.groupview_bg);
				textView.setGravity(Gravity.CENTER_VERTICAL);
				textView.setPadding(40, 0, 0, 0);
				textView.setTextSize(30);
				textView.setTextColor(Color.WHITE);

				return textView;
			}

			// 重写expandlistadapter中的各个方法
			@Override
			public int getGroupCount() {
				return diskli != null ? diskli.size() : 0;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				if (diskli != null) {
					if (diskli.get(groupPosition) != null) {
						List<Music> li = diskli.get(groupPosition).getMusicList();
						if (li != null) {
							return li.size();
						}
					}

				}
				return 0;
			}

			@Override
			public Object getGroup(int groupPosition) {
				return diskli.get(groupPosition).getName();
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				if (diskli != null) {
					if (diskli.get(groupPosition) != null) {
						List<Music> li = diskli.get(groupPosition).getMusicList();
						if (li != null) {
							return li.get(childPosition).getName();
						}
					}
				}
				return null;
			}

			@Override
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public boolean hasStableIds() {
				return true;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				convertView = LayoutInflater.from(attachedActivity).inflate(R.layout.albums_group_item, null);
				TextView tvDiscNo = (TextView) convertView.findViewById(R.id.tv_disc_no);
				tvDiscNo.setText("Disc" + (groupPosition + 1));

				return convertView;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

				ChildHolder holder;
				List<Music> li = diskli.get(groupPosition).getMusicList();

				if (convertView == null || convertView.getTag() == null) {
					convertView = LayoutInflater.from(attachedActivity).inflate(R.layout.musics_item, null);
					holder = new ChildHolder(convertView);
					convertView.setTag(holder);

					if (li == null) {
						return convertView;
					}

				} else {
					holder = (ChildHolder) convertView.getTag();
				}

				holder.tvNum.setVisibility(View.VISIBLE);
				holder.tvNum.setText(li.get(childPosition).getTrack_no());
				holder.tvName.setText(li.get(childPosition).getName());
				holder.tvArtist.setText(li.get(childPosition).getArtistName());

				long id = li.get(childPosition).getId();
				if (WatchDog.cacheStateMap.containsKey(id) == false) {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				}

				else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_WAIT)) {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
					holder.ivSavingState.setBackgroundResource(R.drawable.downloaded);
				} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADING)) {
					holder.ivSavingState.setBackgroundResource(R.drawable.downloading);
				} else {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				}

				if (WatchDog.currentPlayingId.equals(li.get(childPosition).getId())) {
					holder.ivChosen.setVisibility(View.VISIBLE);
					holder.ivPlaying.setVisibility(View.VISIBLE);

					if (!PlayerFragment.PLAYING.equals(WatchDog.currentState)) {
						// 显示载入中...
						holder.ivPlaying.setText("加载中");
						holder.ivPlaying.setBackgroundResource(R.color.transparent);
					} else {
						holder.ivPlaying.setText("");
						holder.ivPlaying.setBackgroundResource(R.drawable.playing_icon);
					}

				} else {
					holder.ivChosen.setVisibility(View.INVISIBLE);
					holder.ivPlaying.setVisibility(View.INVISIBLE);
				}

				return convertView;
			}

			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
				return true;
			}

		};

		expandablelistview.setAdapter(adapter);
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			expandablelistview.expandGroup(i);
		}
	}

	@Override
	public void onDetach() {
		Log.i(TAG, "onDetach");
		unregisterReceivers();
		WatchDog.keybackRefersExitPro = true;// 返回键指向退出程序
		super.onDetach();
	}

	private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		albumd = new AlbumDao().getAlbumDetailData(AlbumDetailFragment.this.albumdetail.getAlbumId());
			if (albumd != null) {
				if (albumd.getDisklist() != null && albumd.getDisklist().size() > 0) {
					AlbumDetailFragment.this.albumdetail = albumd;
					diskli=AlbumDetailFragment.this.albumdetail.getDisklist();
					adapter.notifyDataSetChanged();
				} else {
					FragmentManager fragmentManager = getFragmentManager();
					fragmentManager.popBackStack();
				}
			}
		}
	};

	private void registerReceivers() {
		attachedActivity.registerReceiver(updateListReceiver, new IntentFilter(BroadcastManager.FILTER_UPDATE_LOCALLIST));
	}

	private void unregisterReceivers() {
		attachedActivity.unregisterReceiver(updateListReceiver);
	}

}
