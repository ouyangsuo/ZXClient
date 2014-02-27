package com.union.cellremote.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.union.cellremote.R;
import com.union.cellremote.domain.Album;

//Toast
public class ArtistAlbumListAdapter extends BaseAdapter {

	private Context context;
	private String artistName;
	private List<Album> albums = new ArrayList<Album>();

	public ArtistAlbumListAdapter(Context context, String artistName) {
		this.context = context;
		this.artistName = artistName;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	@Override
	public int getCount() {
		return albums.size();//nullpointer
	}

	@Override
	public Object getItem(int position) {
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.albums_item, null);
			convertView.setPadding(context.getResources().getInteger(R.integer.artist_detail_album_item_paddingleft), 0, context.getResources().getInteger(R.integer.artist_detail_album_item_paddingright), 0);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		Album album = albums.get(position);

		holder.tvName.setText(album.getName());
		holder.tvArtist.setText(artistName);
		holder.ivCover.setBackgroundResource(R.drawable.pic1);
		
		if (album.getBitmap()!=null) {
			holder.ivCover.setImageBitmap(album.getBitmap());			
		}else{
			holder.ivCover.setImageBitmap(null);
		}

		return convertView;
	}

	class Holder {
		private ImageView ivCover;
		private TextView tvName;
		private TextView tvArtist;

		public Holder(View convertView) {
			ivCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_album_artist);
		}
	}

}
