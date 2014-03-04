package com.kitty.poclient.adapter;

import java.lang.ref.SoftReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.kitty.poclient.R;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.domain.SearchDataObject;
import com.kitty.poclient.fragment.store.SearchFragment;
import com.kitty.poclient.http.HttpPoster;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.BitmapUtil;
import com.kitty.poclient.util.LoadImageAysnc.ImageCallBack;

public class SearchResultXListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private SearchDataObject sdo;
	private SearchFragment fragment;

	private ImageLoader loader;
	private DisplayImageOptions options;

	public SearchResultXListAdapter(Context context, SearchFragment fragment) {
		this.context = context;
		this.fragment = fragment;
		loader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.pic).cacheInMemory(true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public SearchDataObject getSdo() {
		return sdo;
	}

	public void setSdo(SearchDataObject sdo) {
		this.sdo = sdo;
	}

	@Override
	public int getGroupCount() {
		return 3;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int childCount = 0;

		switch (groupPosition) {
		case 0:
			childCount = sdo.getAlbums().size();// null pointer
			break;
		case 1:
			childCount = sdo.getMusics().size();
			break;
		case 2:
			childCount = sdo.getArtists().size();
			break;
		}

		return childCount;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
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
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.search_result_group_item, null);
		TextView tvDiscNo = (TextView) convertView.findViewById(R.id.tv_disc_no);

		String groupName = "";
		switch (groupPosition) {
		case 0:// 专辑
			if (sdo.getAlbums().size() >= HttpPoster.MAX_SEARCH_ITEM) {
				groupName = "专辑（大于" + sdo.getAlbums().size() + "个结果）";
			} else {
				groupName = "专辑（" + sdo.getAlbums().size() + "个结果）";
			}

			break;

		case 1:// 单曲
			if (sdo.getMusics().size() >= HttpPoster.MAX_SEARCH_ITEM) {
				groupName = "单曲（大于" + sdo.getMusics().size() + "个结果）";
			} else {
				groupName = "单曲（" + sdo.getMusics().size() + "个结果）";
			}
			break;

		case 2:// 演出者
			if (sdo.getArtists().size() >= HttpPoster.MAX_SEARCH_ITEM) {
				groupName = "演出者（大于" + sdo.getArtists().size() + "个结果）";
			} else {
				groupName = "演出者（" + sdo.getArtists().size() + "个结果）";
			}
			break;
		}
		tvDiscNo.setText(groupName);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		ChildHolder holder;
		List list = null;
		switch (groupPosition) {
		case 0:
			list = sdo.getAlbums();
			break;
		case 1:
			list = sdo.getMusics();
			break;
		case 2:
			list = sdo.getArtists();
			break;
		}

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.search_result_item, null);
			holder = new ChildHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}

		switch (groupPosition) {
		case 0:
			Album album = (Album) list.get(childPosition);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.tvName.setText(album.getName());
			holder.tvArtist.setText(album.getArtistName());

			loader.displayImage(album.getImgUrl(), holder.ivCover, options);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					fragment.onAlbumItemClick(childPosition);
				}
			});
			break;

		case 1:
			Music music = (Music) list.get(childPosition);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.tvName.setText(((Music) list.get(childPosition)).getName());
			holder.tvArtist.setText(((Music) list.get(childPosition)).getArtistName() + " - " + ((Music) list.get(childPosition)).getAlbumName());

			loader.displayImage(music.getImgUrl(), holder.ivCover, options);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					fragment.onMusicItemClick(childPosition);
				}
			});
			break;

		case 2:
			Artist artist = (Artist) list.get(childPosition);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.ivCover.setImageBitmap(Constant.albumCover);
			holder.tvName.setText(artist.getName());
			holder.tvArtist.setText(artist.getName());
			/*
			 * if (artist.getBitmap() != null &&
			 * !artist.getBitmap().isRecycled() &&
			 * !artist.getBitmap().equals(Constant.albumCover)) {
			 * holder.ivCover.setImageBitmap(artist.getBitmap()); } else {
			 * holder.ivCover.setImageBitmap(Constant.albumCover);
			 * downloadBitmap(artist); }
			 */

			loader.displayImage(artist.getImgUrl(), holder.ivCover, options);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					fragment.onArtistItemClick(childPosition);
				}
			});
			break;
		}

		return convertView;
	}

	private void downloadBitmap(final Album album) {

		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = album.getImgUrl() + "150";
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, album.getImgUrl(), 150, false, new ImageCallBack() {

					// 下载得到专辑封面后刷新界面
					@Override
					public void imageLoaded(Bitmap bitmap) {
						if (bitmap != null && !bitmap.isRecycled()) {
							album.setBitmap(new SoftReference<Bitmap>(bitmap));
							fragment.bitmaps.add(bitmap);
							notifyDataSetChanged();
						}
					}
				});

				// 从缓存中得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					fragment.bitmaps.add(bitmap);
					notifyDataSetChanged();
				}

				bitmap = null;
				// Looper.loop();
			}
		});

	}

	private void downloadBitmap(final Music music) {

		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// System.out.println("music.getImgUrl()="+music.getImgUrl());
				String imageKey = music.getImgUrl() + "150";
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, music.getImgUrl(), 150, false, new ImageCallBack() {

					// 下载得到专辑封面后刷新界面
					@Override
					public void imageLoaded(Bitmap bitmap) {
						if (bitmap != null && !bitmap.isRecycled()) {
							music.setBitmap(new SoftReference<Bitmap>(bitmap));
							fragment.bitmaps.add(bitmap);
							notifyDataSetChanged();
						}
					}
				});

				// 从缓存中得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					music.setBitmap(new SoftReference<Bitmap>(bitmap));
					fragment.bitmaps.add(bitmap);
					notifyDataSetChanged();
				}

				bitmap = null;
			}
		});

	}

	/*
	 * private void downloadBitmap(final Artist artist) {
	 * 
	 * Pools.executorService2.submit(new Runnable() {
	 * 
	 * @Override public void run() { // Looper.prepare(); String imageKey =
	 * artist.getImgUrl() + "150"; Bitmap bitmap =
	 * BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey,
	 * artist.getImgUrl(), 150, false, new ImageCallBack() {
	 * 
	 * // 下载得到专辑封面后刷新界面
	 * 
	 * @Override public void imageLoaded(Bitmap bitmap) { if (bitmap != null &&
	 * !bitmap.isRecycled()) { artist.setBitmap(new
	 * SoftReference<Bitmap>(bitmap)); fragment.bitmaps.add(bitmap);
	 * notifyDataSetChanged(); } } });
	 * 
	 * // 从缓存中得到封面后刷新界面 if (bitmap != null && !bitmap.isRecycled()) {
	 * artist.setBitmap(new SoftReference<Bitmap>(bitmap));
	 * fragment.bitmaps.add(bitmap); notifyDataSetChanged(); }
	 * 
	 * bitmap = null; // Looper.loop(); } });
	 * 
	 * }
	 */

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	class ChildHolder {
		private ImageView ivCoverBg;
		private ImageView ivCover;
		private TextView tvName;
		private TextView tvArtist;

		ChildHolder(View convertView) {
			ivCoverBg = (ImageView) convertView.findViewById(R.id.iv_cover_bg);
			ivCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_album_artist);
		}
	}

}
