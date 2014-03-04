package com.kitty.poclient.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.kitty.poclient.R;
import com.kitty.poclient.bean.LocalAlbum;
import com.kitty.poclient.bean.LocalCache;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.util.SingletonUtil;

//Toast
public class AlbumListAdapter extends BaseAdapter {
	private ImageLoadingListener animateFirstListener =new AnimateFirstDisplayListener();
	private ImageLoader loader;
    private DisplayImageOptions options;
	private Context context;
	private ListView listview;
	public static  int timce=0;
    public Bitmap preBitmap;
    
	public AlbumListAdapter(Context context, ListView listview) {
		this.context = context;
		this.listview = listview;
		loader = ImageLoader.getInstance();
		options=new DisplayImageOptions.Builder()
		   .showImageOnLoading(R.drawable.pic1)
		   .cacheInMemory(true)
		   .cacheOnDisc(true)
		   .displayer(new RoundedBitmapDisplayer(0))
		   .build();
	}

	@Override
	public int getCount() {
		return VirtualData.albums != null ? VirtualData.albums.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return VirtualData.albums != null ? VirtualData.albums.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		
		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mymusic_albums_item, null);
			holder = new Holder(convertView);
			// holder.ivCover.setImageResource(VirtualData.albums.get(position).getImgUrl());
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (VirtualData.albums == null) {
			return convertView;
		}
		
		Album album = VirtualData.albums.get(position);	
		
		// 主标题
		holder.mainTitleTextView.setText(album.getName());		
		loader.displayImage(album.getImgUrl(), holder.albumCoverImageView, options);

		return convertView;
	}

	class Holder {
		private ImageView albumCoverImageView;
		private TextView mainTitleTextView;
		private TextView subTitleTextView;
		private ImageView cacheStatusImageView;

		public Holder(View convertView) {
			albumCoverImageView = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			mainTitleTextView = (TextView) convertView.findViewById(R.id.tv_album_name);
			subTitleTextView = (TextView) convertView.findViewById(R.id.tv_album_artist);
			cacheStatusImageView = (ImageView) convertView.findViewById(R.id.cache_status);
		}
	}
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener{
		
		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
		
	}

}
