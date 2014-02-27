package com.union.cellremote.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.WatchDog;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.dfim.app.util.PowerfulBigMan;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.Column;

public class ColumnsListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "BotiquesListAdapter";
	private Context context;
	private List<Column> columns;
	private Fragment fragment;
	// private String currentMenuItem = "";
	// private Handler handler;
	// private Handler handler2;
	// private int handlerMsgWhat;
	private ListView lv;

	public ColumnsListAdapter(Context context, List<Column> botiques, ListView lv, Fragment fragment) {
		super();
		this.context = context;
		this.columns = botiques;
		this.fragment = fragment;
	}

	// public ColumnsListAdapter(Context context, List<Column> botiques, Handler
	// handler, int what) {
	// super();
	// this.context = context;
	// this.columns = botiques;
	// this.handler = handler;
	// this.handlerMsgWhat = what;
	// }

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	@Override
	public int getGroupCount() {
		// Log.e(TAG,
		// "getGroupCount: botiques.size()="+botiques.size());//step3:确认列表groupsize不为零
		return columns.size();// 空指针
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return columns.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.botiques_group_item, null);
		TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
		tvName.setText(columns.get(groupPosition).getName());

		convertView.findViewById(R.id.ll_parent).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goColumnDetailFragment(groupPosition);
			}
		});

		convertView.findViewById(R.id.ll_column_list_group).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goColumnDetailFragment(groupPosition);
			}
		});

		convertView.findViewById(R.id.ib_arrow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goColumnDetailFragment(groupPosition);
			}
		});
		// convertView.setFocusable(false);
		// convertView.setFocusableInTouchMode(false);

		return convertView;
	}

	private void goColumnDetailFragment(int groupPosition) {
		if (PowerfulBigMan.testClickInterval() == false) {
			return;
		}

		((NobleMan) fragment).recordCurrentDataAndPosition();// 跳栏目详情前先记录当前数据和位置

		String name = columns.get(groupPosition).getName();
		long id = columns.get(groupPosition).getId();
		showColumnDetail(id, name);
	}

	protected void showColumnDetail(long id, String name) {
		Intent intent = new Intent("showBotiqueContentReceiver");
		intent.putExtra("botiqueId", id);
		intent.putExtra("botiqueName", name);

		// UpnpApp.context.sendBroadcast(intent);
		((TabWebActivity) context).showBotiqueContentReceiverOnReceive(intent);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.botiques_child_item, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		if (columns.get(groupPosition).getDetail() != null) {
			int num = columns.get(groupPosition).getDetail().getNum();

			/* 没有数据，隐藏全部，不到4时隐藏第二行 */
			if (num == 0) {
				convertView.findViewById(R.id.ll_line1).setVisibility(View.GONE);
				convertView.findViewById(R.id.ll_line2).setVisibility(View.GONE);
			} else if (num < 4) {
				convertView.findViewById(R.id.ll_line1).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.ll_line2).setVisibility(View.GONE);
			} else {
				convertView.findViewById(R.id.ll_line1).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.ll_line2).setVisibility(View.VISIBLE);
			}

			List<Album> albumsHere = columns.get(groupPosition).getDetail().getAlbums();
			for (int i = 0; i < holder.items.size(); i++) {
				if (i < num) {
					holder.items.get(i).setVisibility(View.VISIBLE);
					showAlbumHere(albumsHere, i, holder, groupPosition);
				} else {
					holder.items.get(i).setVisibility(View.INVISIBLE);
				}
			}

		}

		return convertView;
	}

	private void showAlbumHere(List<Album> albumsHere, int i, Holder holder, int groupPosition) {
		final long id = albumsHere.get(i).getId();
		final String name = albumsHere.get(i).getName();
		final String imgUrl = albumsHere.get(i).getImgUrl();

		// 设置专辑名称和演出者
		((TextView) holder.items.get(i).findViewById(R.id.tv_name)).setText(albumsHere.get(i).getName());
		((TextView) holder.items.get(i).findViewById(R.id.tv_artist)).setText(albumsHere.get(i).getArtistli().get(0).getName());

		/* 加载屏幕区域内的图片 */
		if (2 * groupPosition + 1 >= ((NobleMan) fragment).getFistVisiblePosition() && 2 * groupPosition - 1 <= ((NobleMan) fragment).getLastVisiblePosition()) {
			/* 设置专辑图片 */
			showAlbumCover(albumsHere, i, holder, id, name, imgUrl);
		} else {
			((ImageView) holder.items.get(i).findViewById(R.id.iv_cover)).setImageBitmap(Constant.albumCover);
		}
	}

	private void showAlbumCover(List<Album> albumsHere, int i, Holder holder, final long id, final String name, final String imgUrl) {
		// final Bitmap bitmap = albumsHere.get(i).getCoverBitmap();
		final Bitmap bitmap = downloadAlbumImage(albumsHere.get(i), albumsHere.get(i).getImgUrl());
		if (bitmap != null) {
			((ImageView) holder.items.get(i).findViewById(R.id.iv_cover)).setImageBitmap(bitmap);
		} else {
			((ImageView) holder.items.get(i).findViewById(R.id.iv_cover)).setImageBitmap(Constant.albumCover);
		}

		// 图片点击事件
		((ImageView) holder.items.get(i).findViewById(R.id.iv_radiance)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (WatchDog.isSlidingMenuShown == true || PowerfulBigMan.testClickInterval() == false) {
					return;
				}

				((NobleMan) fragment).recordCurrentDataAndPosition();// 跳专辑详情前先记录当前数据和位置
				showAlbumDetail(id, name, bitmap, imgUrl);
			}
		});
	}

	private Bitmap downloadAlbumImage(final Album album, final String tag) {

		String imageKey = album.getImgUrl() + "150";

		Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, album.getImgUrl(), 150, false, new ImageCallBack() {
			@Override
			public void imageLoaded(final Bitmap bitmap) {
				((ImageView) lv.findViewWithTag(tag)).setImageBitmap(bitmap);
			}
		});

		// 得到封面后刷新界面
		if (bitmap != null && !bitmap.isRecycled()) {
			return bitmap;
		}

		return null;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	protected void showAlbumDetail(long id, String name, Bitmap bitmap, String imgUrl) {
		// System.out.println("showAlbumContent of"+name+" id="+id);
		if (bitmap == null) {
			bitmap = Constant.albumCover;
		} else if (bitmap.getByteCount() > 250000) {
			byte[] bmBytes = BitmapUtil.Bitmap2Bytes(bitmap);
			bitmap = BitmapUtil.Bytes2Bimap(bmBytes, 2);

			// 如此仁至义尽了
			if (bitmap.getByteCount() > 250000) {
				bitmap = Constant.albumCover;
			}
		}

		Intent intent = new Intent("showAlbumContentReceiver");
		intent.putExtra("albumId", id);
		intent.putExtra("albumName", name);
		intent.putExtra("bitmap", bitmap);
		intent.putExtra("imgUrl", imgUrl);

		// UpnpApp.context.sendBroadcast(intent);
		((TabWebActivity) context).showAlbumContentReceiverOnReceive(intent);
	}

	class Holder {

		private List<LinearLayout> items = new ArrayList<LinearLayout>();

		private LinearLayout llItem1;
		private LinearLayout llItem2;
		private LinearLayout llItem3;
		private LinearLayout llItem4;
		private LinearLayout llItem5;
		private LinearLayout llItem6;

		Holder(View convertView) {
			llItem1 = (LinearLayout) convertView.findViewById(R.id.ll_item1);
			items.add(llItem1);

			llItem2 = (LinearLayout) convertView.findViewById(R.id.ll_item2);
			items.add(llItem2);

			llItem3 = (LinearLayout) convertView.findViewById(R.id.ll_item3);
			items.add(llItem3);

			llItem4 = (LinearLayout) convertView.findViewById(R.id.ll_item4);
			items.add(llItem4);

			llItem5 = (LinearLayout) convertView.findViewById(R.id.ll_item5);
			items.add(llItem5);

			llItem6 = (LinearLayout) convertView.findViewById(R.id.ll_item6);
			items.add(llItem6);
		}
	}

}
