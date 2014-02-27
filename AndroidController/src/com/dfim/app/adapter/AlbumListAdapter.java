package com.dfim.app.adapter;

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

import com.dfim.app.bean.LocalAlbum;
import com.dfim.app.bean.LocalCache;
import com.dfim.app.data.VirtualData;
import com.dfim.app.util.SingletonUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.Artist;

//Toast
public class AlbumListAdapter extends BaseAdapter {
	private ImageLoadingListener animateFirstListener =new AnimateFirstDisplayListener();
    private DisplayImageOptions options;
	private Context context;
	private ListView listview;
	public static  int timce=0;
    public Bitmap preBitmap;
    
	public AlbumListAdapter(Context context, ListView listview) {
		this.context = context;
		this.listview = listview;
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
		
		LocalAlbum currentAlbum = VirtualData.localAlbums.get(position);
		int totalMusicNum = currentAlbum.getTotalMusicNum();
		int resourseId = R.drawable.wait;
//		Log.i("AlbumListCache", "currentAlbum.getCacheStatus()=" + currentAlbum.getCacheStatus());
		switch(currentAlbum.getCacheStatus()){
			case LocalCache.CACHE_STATUS_DOWNLOADED:
				resourseId = R.drawable.downloaded;
				break;
			case LocalCache.CACHE_STATUS_DOWNLOADING:
				resourseId = R.drawable.downloading;
				break;
			case LocalCache.CACHE_STATUS_WAIT:
				resourseId = R.drawable.wait;
				break;
			case LocalCache.CACHE_STATUS_FAILURE_NOSPACE:
//				resourseId = R.drawable.alert;
				resourseId = R.drawable.wait;
				break;
			default:
				resourseId = R.drawable.wait;
				break;
		}
		holder.cacheStatusImageView.setImageDrawable(context.getResources().getDrawable(resourseId)); //2-3缓存状态图标
		
		String subTitle = "";
		if(currentAlbum.getCacheStatus()==LocalCache.CACHE_STATUS_DOWNLOADED){
//			subTitle = totalMusicNum + "首音乐";
			
			List<Artist> artistLi = VirtualData.albums.get(position).getArtistli();
			String artistName = "";
			if (artistLi != null&&artistLi.size()>0) {
				for (Artist ar : artistLi) {
					if( ar.getName().equals("未知")){
						ar.setName("未知演出者");
					}
				 	artistName += ar.getName() + ",";
				}
				subTitle = artistName.substring(0, artistName.length() - 1);
			}else{
//				holder.subTitleTextView.setText("未知演出者");
				subTitle = "未知演出者";
				
			}
		}else{
			subTitle = "已缓存" + currentAlbum.getDownloadedMusicNum() + "/" + totalMusicNum;
		}
		
		// 副标题
		holder.subTitleTextView.setText(subTitle); 	
		
		// 主标题
		Album album = VirtualData.albums.get(position);
//		holder.mainTitleTextView.setText(album.getName());
		holder.mainTitleTextView.setText(currentAlbum.getName());
		
		holder.albumCoverImageView.setTag(album.getImgUrl());
		if(SingletonUtil.imagflag){
		holder.albumCoverImageView.setBackgroundResource(R.drawable.pic);
		}
		SingletonUtil.getSingletonUtil().loadAlbumImage(album, listview,holder.albumCoverImageView);
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
