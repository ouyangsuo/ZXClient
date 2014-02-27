package com.union.cellremote.adapter;

import java.util.List;

import com.dfim.app.upnp.BoxControl;
import com.dfim.app.util.PowerfulBigMan;
import com.dfim.app.util.SingletonUtil;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.store.PurchasedFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 已购音乐 列表 Adapter.
 * @author Administrator
 *
 */
public class ExtendsAlbumListAdapter extends BaseAdapter {
	private Context context;
	private List<Album> lialbum;
    private ListView listView;
    private int flag;
	public ExtendsAlbumListAdapter(Context context, List<Album> li ,ListView listview,int flag) {
		this.context = context;
		this.lialbum = li;
		this.listView=listview;
		this.flag=flag;
	}

	public ExtendsAlbumListAdapter() {

	}

	public  void setLialbum(List<Album> lialbum) {
		this.lialbum = lialbum;
	}

	@Override
	public int getCount() {

		return lialbum != null ? lialbum.size() : 0;
	}

	@Override
	public Object getItem(int position) {

		return lialbum != null ? lialbum.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		Album album = lialbum.get(position);

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.albums_item_for_cloud, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);
			
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.imbutton.setTag(position);
		
		// 同步云端专辑
		holder.imbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!PowerfulBigMan.testClickInterval()){
					return;
				}
				
				int pos = ((Integer) v.getTag()).intValue();
				PurchasedFragment.Trigger_From_Cellremote=true;
				v.setVisibility(View.GONE);
				Album album = lialbum.get(pos);
				if (album != null && album.getIsCloud() == 0) {
					/*lialbum.remove(album);
					lialbum=new AlbumServiceImpl().getAllAlbumList(0, 100);
					listView.setAdapter(ExtendsAlbumListAdapter.this);*/
					//listView.findViewWithTag(pos).setVisibility(View.GONE);
					if(flag!=0){						
						lialbum.remove(album);
					}else{
						album.setIsCloud(5);					
					}
					new BoxControl().notifyBoxUpdateCloud(album.getId()+"",1);
				}
				notifyDataSetChanged();
				System.gc();
			}
		});
		
		
		if (lialbum == null) {
			return convertView;
		}

		if (album.getIsCloud() == 0) {
			holder.imbutton.setVisibility(View.VISIBLE);
		} else {
			holder.imbutton.setVisibility(View.GONE);
		}
		holder.tvName.setText(album.getName());
		List<Artist> artistLi = lialbum.get(position).getArtistli();
		String artistName = "";
		if (artistLi != null && artistLi.size() > 0) {
			for (Artist ar : artistLi) {
				artistName += ar.getName() + ",";
			}
			holder.tvArtist.setText(artistName.substring(0,
					artistName.length() - 1));
		} else {
			holder.tvArtist.setText("未知演出者");

		}
		holder.ivCover.setTag(album.getImgUrl());
		if (SingletonUtil.imagflag) {
			holder.ivCover.setBackgroundResource(R.drawable.pic);

		}
	    SingletonUtil.getSingletonUtil().loadAlbumImage(album,listView, holder.ivCover);

		return convertView;
	}

	class Holder {
		private ImageView ivCover;
		private TextView tvName;
		private TextView tvArtist;
		private ImageButton imbutton;

		public Holder(View convertView) {
			ivCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
			tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			tvArtist = (TextView) convertView
					.findViewById(R.id.tv_album_artist);
			imbutton = (ImageButton) convertView
					.findViewById(R.id.cloud_download_bt);

		}
	}

}
