package com.union.cellremote.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.upnp.BoxControl;
import com.union.cellremote.R;

public class ExtendsMusicListAdapter extends BaseAdapter {

	private static final String TAG="ExtendsMusicListAdapter:";
	
	private List<Music> liMusic;
	private Context context;
	private int flag;
	private boolean showCacheState = true;
	private int theChosenPosition = -1;

	public ExtendsMusicListAdapter() {

	}

	public ExtendsMusicListAdapter(Context context, List<Music> limusic, int flag) {
		this.context = context;
		this.liMusic = limusic;
		this.flag = flag;//用来判断是【全部】还是【云端】
		this.showCacheState=true;
	}
	
	public ExtendsMusicListAdapter(Context context, List<Music> limusic, int flag, boolean showCacheState) {
		this.context = context;
		this.liMusic = limusic;
		this.flag = flag;
		this.showCacheState = showCacheState;
	}

	public void setLiMusic(List<Music> liMusic) {
		this.liMusic = liMusic;
	}

	@Override
	public int getCount() {

		return liMusic != null ? liMusic.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return liMusic != null ? liMusic.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.musics_item_for_cloud, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);

			if (liMusic == null) {
				return convertView;
			}

			// 同步云端的单曲
			holder.imbutton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.e("BUG944", TAG+" imbutton onClick");
					int pos = ((Integer) v.getTag()).intValue();
					Music music = liMusic.get(pos);

					if (music != null && music.getIscloud() == 0) {
						//flag: 0=单曲全部、1=单曲云端、2=单曲本地
						if (flag == 0) {
//							liMusic.remove(music);
							music.setIscloud(5);
							VirtualData.setMusicContainerAlbumLocal(music);// 包装该单曲的专辑须同步设置为在本地
						} else if (flag == 1){
							liMusic.remove(music);
							VirtualData.setMusicContainerAlbumLocal(music);// 包装该单曲的专辑须同步设置为在本地
						}else if(flag == 2){
							music.setIscloud(5);
							VirtualData.setMusicContainerAlbumLocal(music);// 包装该单曲的专辑须同步设置为在本地
						}

						notifyDataSetChanged();
						new BoxControl().notifyBoxUpdateCloud(music.getId() + "", 5);// 通知盒子同步取回的音乐
					}
					System.gc();
				}
			});

		} else {
			holder = (Holder) convertView.getTag();
		}
		
		// 点击时显示选中状态
		holder.llParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("llparent onClick");
				theChosenPosition = position;
				notifyDataSetChanged();
			}
		});
		
		if (position == theChosenPosition) {
			holder.llParent.setBackgroundColor(context.getResources().getColor(R.color.translucent_black_alpha2));
		} else {
			holder.llParent.setBackgroundColor(context.getResources().getColor(R.color.transparent));
		}
		
		holder.imbutton.setTag(position);
		Music music = liMusic.get(position);
		if (music == null) {
			return null;
		} else if (music.getIscloud() == 0) {
			holder.imbutton.setVisibility(View.VISIBLE);
		} else {
			holder.imbutton.setVisibility(View.GONE);
		}

		holder.tvName.setText(music.getName());
		String artistname = music.getArtistName();
		if (artistname == "" || artistname == null) {
			artistname = "未知演出者";
		}
		holder.tvArtist.setText(artistname);
		holder.tvNum.setText((position + 1) + "");

		// 查询显示缓存状态
		long id = liMusic.get(position).getId();
		if (WatchDog.cacheStateMap.containsKey(id) == false) {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
		}else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_WAIT)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.downloaded);
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADING)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.downloading);
		} else {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
		}

		if (WatchDog.currentPlayingId.equals(music.getId())) {
			holder.ivChosen.setVisibility(View.VISIBLE);
		} else {
			holder.ivChosen.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	class Holder {
		private LinearLayout llParent;
		private TextView tvName;
		private TextView tvArtist;
		private TextView tvNum;
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private ImageButton imbutton;

		public Holder(View convertView) {
			llParent = (LinearLayout) convertView.findViewById(R.id.ll_parent);
			
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			
			ivSavingState = (ImageView) convertView.findViewById(R.id.iv_saving_state);
			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);
			imbutton = (ImageButton) convertView.findViewById(R.id.cloud_download_bt);
			
			if(showCacheState){
				ivSavingState.setVisibility(View.VISIBLE);
			}else{
				ivSavingState.setVisibility(View.GONE);
			}
		}
		
	}

}
