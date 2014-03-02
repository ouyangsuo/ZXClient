package com.kitty.poclient.adapter;

import java.lang.ref.SoftReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.kitty.poclient.R;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.store.SearchFragment;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.BitmapUtil;
import com.kitty.poclient.util.LoadImageAysnc.ImageCallBack;

public class SearchResultListAdapter extends BaseAdapter {

	private Context context;
	private SearchFragment fragment;
	private int groupPosition = 0;
	private List list = null;
	private ImageLoader loader;
	private DisplayImageOptions options;

	public SearchResultListAdapter(Context context, SearchFragment fragment,
			int groupPosition) {
		this.context = context;
		this.fragment = fragment;
		this.groupPosition = groupPosition;
		initImageLoader();
	}

	public SearchResultListAdapter(Context context, SearchFragment fragment, int groupPosition, List list) {
		this.context = context;
		this.fragment = fragment;
		this.groupPosition = groupPosition;
		this.list = list;
		initImageLoader();
	}

	private void initImageLoader() {
		loader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.pic)
			.cacheInMemory(true).cacheOnDisc(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ChildHolder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.search_result_item, null);
			holder = new ChildHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}

		switch (groupPosition) {
		case 0:
			// System.out.println("list.get(position).getClass().getName()=" +
			// list.get(position).getClass().getName());
			Album album = (Album) list.get(position);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.tvName.setText(album.getName());
			holder.tvArtist.setText(album.getArtistName());
			loader.displayImage(album.getImgUrl(), holder.ivCover,options);
			break;

		case 1:
			Music music = (Music) list.get(position);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.tvName.setText(((Music) list.get(position)).getName());
			holder.tvArtist.setText(((Music) list.get(position)).getArtistName() + " - " + ((Music) list.get(position)).getAlbumName());
			// holder.ivCover.setImageBitmap(null);
			loader.displayImage(music.getImgUrl(), holder.ivCover,options);
			break;

		case 2:
			Artist artist = (Artist) list.get(position);
			holder.ivCoverBg.setVisibility(View.VISIBLE);
			holder.ivCover.setImageBitmap(Constant.albumCover);
			holder.tvName.setText(artist.getName());
			holder.tvArtist.setText(artist.getName());
			loader.displayImage(artist.getImgUrl(), holder.ivCover, options);
			break;
		}

		return convertView;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	private void downloadBitmap(final Album album) {

		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = album.getImgUrl() + "150";
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(
						imageKey, album.getImgUrl(), 150, false,
						new ImageCallBack() {

							// 下载得到专辑封面后刷新界面
							@Override
							public void imageLoaded(Bitmap bitmap) {
								if (bitmap != null && !bitmap.isRecycled()) {
									album.setBitmap(new SoftReference<Bitmap>(
											bitmap));
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
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(
						imageKey, music.getImgUrl(), 150, false,
						new ImageCallBack() {

							// 下载得到专辑封面后刷新界面
							@Override
							public void imageLoaded(Bitmap bitmap) {
								if (bitmap != null && !bitmap.isRecycled()) {
									music.setBitmap(new SoftReference<Bitmap>(
											bitmap));
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

	class ChildHolder {
		private ImageView ivCoverBg;
		private ImageView ivCover;
		private TextView tvName;
		private TextView tvArtist;

		ChildHolder(View convertView) {
			ivCoverBg = (ImageView) convertView.findViewById(R.id.iv_cover_bg);
			ivCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtist = (TextView) convertView
					.findViewById(R.id.tv_album_artist);
		}
	}
}
