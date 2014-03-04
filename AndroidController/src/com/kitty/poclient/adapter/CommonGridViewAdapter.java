package com.kitty.poclient.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.kitty.poclient.R;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.fragment.store.ColumnDetailFragment;
import com.kitty.poclient.util.BitmapUtil;
import com.kitty.poclient.util.PowerfulBigMan;

public class CommonGridViewAdapter extends BaseAdapter {

	private final String TAG = "CommonGridViewAdapter: ";

	private Context context;
	private List<Album> albums = new ArrayList<Album>();
	private String imgUrl;// 用于购买后更新本地界面
	private ColumnDetailFragment fragment;

    private ImageLoader loader ;
    private DisplayImageOptions options;
	
	// public CommonGridViewAdapter(Context context, List<Album> albums) {
	// super();
	// this.context = context;
	// this.albums = albums;
	// }

	public CommonGridViewAdapter(Context context, List<Album> albums, ColumnDetailFragment fragment) {
		super();
		this.context = context;
		this.albums = albums;
		this.fragment = fragment;
        loader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.pic)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	@Override
	public int getCount() {
		// return albums.size() + 1;
		return albums.size();
	}

	@Override
	public Object getItem(int position) {
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		return albums.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.albums_item_for_gridview, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		if (position < albums.size()) {
			convertView.setVisibility(View.VISIBLE);
			final Album album = albums.get(position);

/*			if (album.getBitmap() != null && !album.getBitmap().isRecycled()) {
				holder.ivAlbumCover.setImageBitmap(album.getBitmap());// 设置专辑封面，不设则使用布局中定义的默认背景
			}else{
				holder.ivAlbumCover.setImageBitmap(Constant.albumCover);
			}*/
			
			loader.displayImage(album.getImgUrl(), holder.ivAlbumCover, options);
			
			holder.tvAlbumName.setText(album.getName());
			holder.tvArtistName.setText(album.getArtistli().get(0).getName());

			holder.ivAlbumCover.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (PowerfulBigMan.testClickInterval() == false) {
						return;
					}

					// long id = album.getId();
					// String name = album.getName();
					// Bitmap bitmap = album.getCoverBitmap();
					// showAlbumContent(id, album.getName(), bitmap);
					imgUrl = album.getImgUrl();
					fragment.recordCurrentDataAndPosition();//在跳转之前记录数据和位置

					showAlbumContent(album.getId(), album.getName(), album.getBitmap());
				}
			});

		}

		// else {
		// if (position == fragment.columnDetail.getTotal()) {
		// convertView.setVisibility(View.GONE);
		// return convertView;
		// }else{
		// convertView.setVisibility(View.VISIBLE);
		// // holder.tvAlbumName.setVisibility(View.VISIBLE);
		// // holder.tvArtistName.setVisibility(View.VISIBLE);
		// // holder.ivAlbumCover.setVisibility(View.VISIBLE);
		//
		// holder.tvAlbumName.setText("加载更多");
		// holder.tvArtistName.setText("");
		// holder.ivAlbumCover.setImageBitmap(Constant.albumCover);
		// // holder.ivAlbumCover.setEnabled(true);
		//
		// holder.ivAlbumCover.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (PowerfulBigMan.testClickInterval() == false) {
		// return;
		// }
		//
		// // 加载更多数据
		// getMoreData();
		// }
		// });
		// }
		// }

		return convertView;
	}

	protected void getMoreData() {
//		CustomToast.makeText(context, "更多数据加载中...", Toast.LENGTH_SHORT).show();
		UpnpApp.mainHandler.showInfo(R.string.loading_more_info);

		if (fragment != null) {
			fragment.getData(albums.size(), 30);
		}
	}

	protected void showAlbumContent(long id, String name, Bitmap bitmap) {
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic);// oom
		}

		if (bitmap.getByteCount() > 250000) {// nullporinter*2
			byte[] bmBytes = BitmapUtil.Bitmap2Bytes(bitmap);
			bitmap = BitmapUtil.Bytes2Bimap(bmBytes, 2);

			// 如此仁至义尽了
			if (bitmap.getByteCount() > 250000) {// nullpointer
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic1);
			}
		}

		Intent intent = new Intent("showAlbumContentReceiver");
		intent.putExtra("albumId", id);
		intent.putExtra("albumName", name);
		intent.putExtra("bitmap", bitmap);
		intent.putExtra("imgUrl", imgUrl);
		intent.putExtra("layout", R.id.ll_web_root);

		// UpnpApp.context.sendBroadcast(intent);
		WatchDog.tabWebFragment.showAlbumContentReceiverOnReceive(intent);
	}

	class Holder {
		private ImageView ivAlbumCover;
		private TextView tvAlbumName;
		private TextView tvArtistName;

		public Holder(View convertView) {
			ivAlbumCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvAlbumName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtistName = (TextView) convertView.findViewById(R.id.tv_artist_name);
		}
	}

}
