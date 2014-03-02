package com.kitty.poclient.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.fragment.PlayerFragment;

public class MusicListAdapter extends BaseAdapter {
	private static String TAG = "MusicListAdapter";
	private Context context;
	private List<Music> mlist;

	// private AnimationDrawable ad;

	public List<Music> getMlist() {
		return mlist;
	}

	public void setMlist(List<Music> mlist) {
		this.mlist = mlist;
	}

	public MusicListAdapter(Context context, List<Music> list) {
		this.context = context;
		if (list != null) {

			this.mlist = list;

		}
		// ad = (AnimationDrawable)
		// context.getResources().getDrawable(R.anim.animatior_searchbox_list);
	}

	@Override
	public int getCount() {
		return mlist != null ? mlist.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mlist != null ? mlist.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.musics_item, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);

			if (mlist == null) {
				return convertView;
			}

		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.tvName.setText(mlist.get(position).getName());
		String artistname = mlist.get(position).getArtistName();
		if (artistname == "" || artistname == null || artistname.equals("未知")) {
			artistname = "未知演出者";
		}
		holder.tvArtist.setText(artistname);
		holder.tvNum.setText((position + 1) + "");

		// 查询显示缓存状态
		long id = mlist.get(position).getId();
		
		int artistTextColor = context.getResources().getColor(R.color.artist_textcolor);
		holder.tvArtist.setTextColor(artistTextColor);
		boolean noSpace = false;
		
		// 【我的音乐】更新缓存状态
		if (WatchDog.cacheStateMap.containsKey(id) == false) {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
			
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_WAIT)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
			
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.downloaded);
			
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADING)) {
			holder.ivSavingState.setBackgroundResource(R.drawable.downloading);
			
		} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_FAILURE_NOSPACE)){
			noSpace = true;
			artistTextColor = context.getResources().getColor(R.color.crouton_alert_color);
			holder.ivSavingState.setBackgroundResource(R.drawable.alert);
			holder.ivPlaying.setVisibility(View.INVISIBLE);
			holder.tvArtist.setText("空间不足");
			holder.tvArtist.setTextColor(artistTextColor);
			Log.i("CacheMsg", "CacheMsg id="+id);
			
		} else {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
		}

		// 【我的音乐】更新list item
		// 当前选中item
		if (WatchDog.currentPlayingId.equals(mlist.get(position).getId())) {

			holder.ivChosen.setVisibility(View.VISIBLE);
			holder.ivPlaying.setVisibility(View.VISIBLE);
			
			if (!Music.CACHE_DOWNLOADED.equals(WatchDog.cacheStateMap.get(id))) {
				if(noSpace){
					//do nothing
				} else {
					holder.ivSavingState.setBackgroundResource(R.drawable.downloading);
				}
			}

			if (!PlayerFragment.PLAYING.equals(WatchDog.currentState)) {
				// 显示加载中...
				if(noSpace){
					//do nothing
				}else{

					holder.ivPlaying.setText("加载中");
					holder.ivPlaying.setBackgroundResource(R.color.transparent);
				}
				
			} else {
				holder.ivPlaying.setText("");
				holder.ivPlaying.setBackgroundResource(R.drawable.playing_icon);
			}

		} else {
			holder.ivChosen.setVisibility(View.INVISIBLE);
			holder.ivPlaying.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	class Holder {
		private TextView tvName;
		private TextView tvArtist;
		private TextView tvNum;
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private TextView ivPlaying;

		public Holder(View convertView) {
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			ivSavingState = (ImageView) convertView.findViewById(R.id.iv_saving_state);
			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);
			ivPlaying = (TextView) convertView.findViewById(R.id.iv_playing);
		}
	}

}
