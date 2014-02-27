package com.dfim.app.fragment.mymusic;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.common.BroadcastManager;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.TabMusicFragment;
import com.dfim.app.upnp.BoxControl;
import com.dfim.app.widget.StandardCustomDialog;
import com.union.cellremote.R;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Disk;
import com.union.cellremote.domain.Music;

public class CacheAlbumDetailFragment extends BaseFragment {

	private final String TAG = CacheAlbumDetailFragment.class.getSimpleName();

	private StandardCustomDialog dialog;
	private Activity context;
	private TabMusicFragment tabMusicFragment;
	
	private AlbumDetail albumdetail;
	private List<Disk> diskli;
	private List<Music> musiclistall;
	private ArrayList<Music> theFirstDiskList;
	private ExpandableListView expandablelistview;
	private BaseExpandableListAdapter adapter;
	private View view;
	private LinearLayout cancelcache;// 取消缓存
	private CancelClearListener cancelClearListener;
	private LinearLayout sure_clearcache;// 确定清楚缓存
	private LinearLayout clearAllBtn;// 全选linearLayout
	
	@SuppressLint("HandlerLeak")
	private Handler deleteHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			showToast(msg.what);
//			UpnpApp.mainHandler.showInfo(R.string.mymusic_cache_clear_success_info);
			
			switch(msg.what){
				case BoxControl.CLEAR_CACHE_SUCESS:
					Log.e("BUG975", "8>>"+TAG+"handleMessage() CLEAR_CACHE_SUCESS");
					
					UpnpApp.mainHandler.showInfo(R.string.mymusic_cache_clear_success_info);
					break;
				case BoxControl.CLEAR_CACHE_FAILURE:
					UpnpApp.mainHandler.showAlert(R.string.mymusic_cache_clear_failure_alert);
					break;
			}
			cancelClearListener.setEnableCancel(true);
			cancelcache.setOnClickListener(cancelClearListener);
		}

	};
	
	public CacheAlbumDetailFragment(Activity context, TabMusicFragment tabMusicFragment, AlbumDetail albumdetail) {
		this.context = context;
		this.tabMusicFragment = tabMusicFragment;
		
		if (albumdetail == null) {
			albumdetail = new AlbumDetail();
		}
		WatchDog.clearCacheProductType = 5;
		this.albumdetail = albumdetail;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(context).inflate(R.layout.album_detail_cache_fragment, null);// 异常：曾报空指针
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
		cancelcache = (LinearLayout) view.findViewById(R.id.cancelcache);
		sure_clearcache = (LinearLayout) view.findViewById(R.id.sure_clearcache);
		
		clearAllBtn = (LinearLayout) view.findViewById(R.id.circle_btn);
		
		expandablelistview = (ExpandableListView) view.findViewById(R.id.cache_expandlistview);
		expandablelistview.setGroupIndicator(null);

	}

	private void initListeners() {
		// 取消缓存
		cancelClearListener = new CancelClearListener();
		cancelcache.setOnClickListener(cancelClearListener);

		// 确认清除缓存
		sure_clearcache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("BUG975", "2>>"+TAG+"sure_clearcache onClick()");
				
				String ids = "";
				List<Music> li = null;
				if (albumdetail != null) {
					BoxControl bc = new BoxControl();
					if (albumdetail.isFlag()) {
						showExitDialog(bc, albumdetail, deleteHandler);
					} else {
						diskli = albumdetail.getDisklist();
						for (Disk d : diskli) {
							li = new ArrayList<Music>();
							musiclistall = d.getMusicList();
							for (Music m : musiclistall) {
								if (m.isFlag()) {
									ids = ids + m.getId() + ",";
								} else {
									li.add(m);
								}
							}

							li = null;
						}
						if (ids.length() > 0) {
							ids = ids.substring(0, ids.length() - 1);
							showExitDialog(bc, diskli, ids.split(",").length, deleteHandler);
						} else {
							// showExitDialog();
//							CustomToast.makeText(context, "请选择需要清除缓存的单曲", 1000).show();
							UpnpApp.mainHandler.showAlert(R.string.mymusic_cache_select_empty_alert);
						}

					}
				}

			}
		});

		// 全选
		clearAllBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout Lin = (LinearLayout) v;
				CheckBox chb = (CheckBox) Lin.findViewById(R.id.checkbox_all);
				if (albumdetail != null) {

					if (chb.isChecked()) {
						diskli = albumdetail.getDisklist();
						for (Disk d : diskli) {
							musiclistall = d.getMusicList();
							chb.setChecked(false);
							albumdetail.setFlag(false);
							for (Music m : musiclistall) {
								m.setFlag(false);
							}
						}
					} else {
						chb.setChecked(true);
						albumdetail.setFlag(true);
						diskli = albumdetail.getDisklist();
						for (Disk d : diskli) {
							musiclistall = d.getMusicList();
							for (Music m : musiclistall) {
								m.setFlag(true);
							}
						}
					}

				}

				adapter.notifyDataSetChanged();

			}
		});

		expandablelistview.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

				System.out.println("child click:" + groupPosition + ":"+ childPosition);
				ArrayList<Music> list = (ArrayList<Music>) diskli.get(groupPosition).getMusicList();
				CheckBox c = (CheckBox) view.findViewById(R.id.checkbox_item);
				c.setChecked(!list.get(childPosition).isFlag());
				list.get(childPosition).setFlag(c.isChecked());

				boolean noItemSelected = true;
				List<Disk> dl = albumdetail.getDisklist();

				adapter.notifyDataSetChanged();
				for (Disk d : dl) {
					for (Music m : d.getMusicList()) {
						if (!m.isFlag()) {
							noItemSelected = false;
						}
					}
				}
				if (noItemSelected) {//true:
					CheckBox cb = (CheckBox) clearAllBtn.findViewById(R.id.checkbox_all);
					cb.setChecked(true);
					albumdetail.setFlag(true);
				} else {
					CheckBox cb = (CheckBox) clearAllBtn.findViewById(R.id.checkbox_all);
					cb.setChecked(false);
					albumdetail.setFlag(false);
				}

				return true;
			}
		});

	}
	class ChildHolder {
		private TextView tvNum;
		private TextView tvName;
		private TextView tvArtist;
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private ImageView ivPlaying;
		private CheckBox checkbox;

		ChildHolder(View convertView) {
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView
					.findViewById(R.id.tv_music_artist);
			ivSavingState = (ImageView) convertView
					.findViewById(R.id.iv_saving_state);
			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);
			checkbox = (CheckBox) convertView.findViewById(R.id.checkbox_item);
		}
	}

	private void initArguments() {
		// initBtnPlayer();
		// tvAlbumName.setText(albumdetail.getAlbumname());
		diskli = CacheAlbumDetailFragment.this.albumdetail.getDisklist();
		if (diskli != null) {
			theFirstDiskList = (ArrayList<Music>) diskli.get(0).getMusicList();
		}
		adapter = new BaseExpandableListAdapter() {
			TextView getTextView() {
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT, 64);
				TextView textView = new TextView(context);
				textView.setLayoutParams(lp);
				textView.setBackgroundResource(R.color.groupview_bg);
				textView.setGravity(Gravity.CENTER_VERTICAL);
				textView.setPadding(40, 0, 0, 0);
				textView.setTextSize(20);
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
						List<Music> li = diskli.get(groupPosition)
								.getMusicList();
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
						List<Music> li = diskli.get(groupPosition)
								.getMusicList();
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
				convertView = LayoutInflater.from(context).inflate(R.layout.albums_group_item, null);
				TextView tvDiscNo = (TextView) convertView.findViewById(R.id.tv_disc_no);
				tvDiscNo.setText("Disc" + (groupPosition + 1));
				return convertView;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {

				ChildHolder holder;
				List<Music> li = diskli.get(groupPosition).getMusicList();
				if (convertView == null) {
					convertView = LayoutInflater.from(context).inflate(R.layout.cache_musics_item, null);
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
				// musicselcmap.put(li.get(childPosition).getId()+"",holder.checkbox);
				long id = li.get(childPosition).getId();

				holder.checkbox.setChecked(li.get(childPosition).isFlag());
				if (WatchDog.cacheStateMap.containsKey(id) == false) {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				}

				else if (WatchDog.cacheStateMap.get(id)
						.equals(Music.CACHE_WAIT)) {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				} else if (WatchDog.cacheStateMap.get(id).equals(
						Music.CACHE_DOWNLOADED)) {
					holder.ivSavingState
							.setBackgroundResource(R.drawable.downloaded);
				} else if (WatchDog.cacheStateMap.get(id).equals(
						Music.CACHE_DOWNLOADING)) {
					holder.ivSavingState
							.setBackgroundResource(R.drawable.downloading);
				} else {
					holder.ivSavingState.setBackgroundResource(R.drawable.wait);
				}

				return convertView;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
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
		unregisterReceivers();
		super.onDetach();
	}

	private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
			System.out.println("expandlistadapter.notifyDataSetChanged();");
			System.out.println("WatchDog.currentPlayingName="
					+ WatchDog.currentPlayingName);
		}
	};
	
	private void registerReceivers() {
		context.registerReceiver(updateListReceiver, new IntentFilter(BroadcastManager.FILTER_UPDATE_LOCALLIST));
	}

	private void unregisterReceivers() {
		context.unregisterReceiver(updateListReceiver);
	}

	private void showExitDialog(final BoxControl bc, final List<Disk> diskli, int ls, final Handler handler) {
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(
				context);

		builder.setTitle("清除歌曲缓存");
		builder.setMessage("确认清除选中的" + ls + "首歌曲？");
		builder.setPositiveButton("取消",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}

				});
		builder.setNegativeButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						cancelClearListener.setEnableCancel(false);
						
						dialog.dismiss();
						
						List<Music> ml = null;
						String musicIds = "";
						for (Disk d : diskli) {
							ml = new ArrayList<Music>();
							musiclistall = d.getMusicList();
							for (Music m : musiclistall) {
								if (m.isFlag()) {
									musicIds = musicIds + m.getId() + ",";
								} else {
									ml.add(m);
								}

							}

							d.setMusicList(ml);
							adapter.notifyDataSetChanged();

						}
						if (musicIds.length() > 0) {
							musicIds = musicIds.substring(0, musicIds.length() - 1);
						}

						Log.i("tongbu", "开始清理单曲");

//						startActivity(new Intent(context, ClearCacheTranslucentActivity.class));
						bc.clearcache(musicIds, BoxControl.MEDIA_TYPE_SINGLE, handler);
					}

				});

		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	private void showExitDialog(final BoxControl bc, final AlbumDetail albumdetail, final Handler handler) {
		Log.e("BUG975", "3>>"+TAG+"showExitDialog()//实际上是确认清除缓存对话框");
		
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle("清除专辑缓存");
		builder.setMessage("确定清除专辑《" + albumdetail.getAlbumname() + "》吗?");
		builder.setPositiveButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});
		builder.setNegativeButton("确定",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.e("BUG975", "4>>"+TAG+" 确认 onClick()");
						
						cancelClearListener.setEnableCancel(false);						
						dialog.dismiss();
						bc.clearcache(albumdetail.getAlbumId() + "", 1, handler);
						
						//通知列表更新
						UIHelper.refreshLocalAlbumsView();
						
						//清除缓存成功提示
//						UpnpApp.mainHandler.showInfo(R.string.mymusic_cache_clear_success_info);
						};
				});
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

/*	private void showToast(int what) {
		if (what == 1) {
//			CustomToast.makeText(context, "成功清理缓存", 1000).show();
			UpnpApp.mainHandler.showInfo(R.string.mymusic_cache_clear_success_info);
		}

	}*/
	private class CancelClearListener implements OnClickListener {
		private boolean enable = true;
		
		public void setEnableCancel(boolean isEnable){
			enable = isEnable;
		}
		
		@Override
		public void onClick(View v) {
			//切换到状态： play
			if(enable){
				tabMusicFragment.back();
			}
		}
	}
}
