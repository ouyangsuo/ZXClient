package com.union.cellremote.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.Artist;
import com.dfim.app.util.SingletonUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.union.cellremote.R;

//Toast
public class ArtistsListAdapter extends BaseAdapter {

	private Context context;	
	private List<Artist> artists=new ArrayList<Artist>();	

	public ArtistsListAdapter(Context context) {
		this.context = context;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	@Override
	public int getCount() {
		return artists.size();
	}

	@Override
	public Object getItem(int position) {
		return artists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		
		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.artists_item, null);
			holder = new Holder(convertView);
			// holder.ivCover.setImageResource(VirtualData.albums.get(position).getImgUrl());
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		Artist artist = artists.get(position);
		holder.tvName.setText(artist.getName());
/*		if (artist.getBitmap()!=null) {
			holder.ivArtist.setImageBitmap(artist.getBitmap());
		}		*/

		return convertView;
	}

	class Holder {
		private ImageView ivArtist;
		private TextView tvName;

		public Holder(View convertView) {
			ivArtist = (ImageView) convertView.findViewById(R.id.iv_artist);
			tvName = (TextView) convertView.findViewById(R.id.tv_name);
		}
	}

}
