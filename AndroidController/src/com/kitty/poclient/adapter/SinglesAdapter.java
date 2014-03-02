package com.kitty.poclient.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.fragment.PlayerFragment;
import com.kitty.poclient.fragment.mymusic.SinglesFragment;

public class SinglesAdapter extends BaseAdapter {
	private static String TAG = SinglesAdapter.class.getSimpleName();
	private Context context;
	private List<Music> mlist;

	// private AnimationDrawable ad;

	public List<Music> getMlist() {
		return mlist;
	}

	public void setMlist(List<Music> mlist) {
		this.mlist = mlist;
	}

	public SinglesAdapter(Context context, List<Music> list) {
		this.context = context;
		if (list != null) {

			this.mlist = list;

		}
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
			convertView = LayoutInflater.from(context).inflate(R.layout.singles_item, null);
			holder = new Holder(convertView, position);
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
		
		//序号
		holder.singlePosition.setText((position + 1) + "");
		
		//选择框
		switch(SinglesFragment.SINGLE_MODE){
			case SinglesFragment.SINGLE_MODE_PLAY:
				holder.itemSelectLayout.setVisibility(View.GONE);
				holder.ivSavingState.setVisibility(View.VISIBLE);
				break;
			case SinglesFragment.SINGLE_MODE_MANAGE:
				
				holder.itemSelectLayout.setVisibility(View.VISIBLE);
				holder.ivSavingState.setVisibility(View.GONE);
				
				boolean isSelected = VirtualData.localSingles.get(position).isSelected();
				holder.itemSelectBox.setChecked(isSelected);
				break;
			default:
				//TODO
				break;
		}
		
		// 查询显示缓存状态
		long id = mlist.get(position).getId();
		
		int artistTextColor = context.getResources().getColor(R.color.artist_textcolor);
		holder.tvArtist.setTextColor(artistTextColor);
		boolean noSpace = false;
		
		// 【我的音乐】更新缓存状态
//		String logMsg = "id:"+id;
//		if (WatchDog.cacheStateMap.containsKey(id)) {
//			logMsg += ",WatchDog.cacheStateMap.get(id):" + WatchDog.cacheStateMap.get(id);
//			Log.i("SinglesCache", logMsg);
//		} else {
//			Log.i("SinglesCache", "WatchDog.cacheStateMap.get(id):" + WatchDog.cacheStateMap.get(id));
//		}
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
			Log.i("SinglesCache", "CACHE_FAILURE_NOSPACE, id="+id);
			
		} else {
			holder.ivSavingState.setBackgroundResource(R.drawable.wait);
		}
		
		//<test
/*		ArrayList<String> testIdList = new ArrayList<String>();
		testIdList.add("1368178209079");
		testIdList.add("1368163427846");
		testIdList.add("1368178208795");
		if(testIdList.contains(""+id)){
			noSpace = true;
			artistTextColor = context.getResources().getColor(R.color.crouton_alert_color);
			holder.ivSavingState.setBackgroundResource(R.drawable.alert);
			holder.ivPlaying.setVisibility(View.INVISIBLE);
			holder.tvArtist.setText("空间不足");
			holder.tvArtist.setTextColor(artistTextColor);
			Log.i("SinglesCache", "CACHE_FAILURE_NOSPACE, id="+id);
		}*/
		//test>

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
		private int position;
		
		private TextView tvName;
		private TextView tvArtist;
		private TextView singlePosition;
		private CheckBox itemSelectBox;
		private LinearLayout itemSelectLayout;
		private LinearLayout singlesItemLayout;
		
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private TextView ivPlaying;

		public Holder(View convertView, int position) {
			this.position = position;

			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);

			singlesItemLayout = (LinearLayout) convertView.findViewById(R.id.singles_item_layout);
			itemSelectLayout = (LinearLayout) convertView.findViewById(R.id.item_select_layout);
			itemSelectBox = (CheckBox) convertView.findViewById(R.id.item_select_box); //选择框
			
			singlePosition = (TextView) convertView.findViewById(R.id.single_position);	
			
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
			ivSavingState = (ImageView) convertView.findViewById(R.id.iv_saving_state);
			ivPlaying = (TextView) convertView.findViewById(R.id.iv_playing);
			

//			LinearClickListener linearClickListener = new LinearClickListener(position);
//			itemSelectLayout.setOnClickListener(linearClickListener);
			
//			CheckedChangeListener checkListener = new CheckedChangeListener(position);
//			itemSelectBox.setOnCheckedChangeListener(checkListener);
		}
	}
	
	class LinearClickListener implements OnClickListener {
		private int position;
		
		public LinearClickListener(int position){
			this.position = position;
		}
		
		@Override
		public void onClick(View v) {
			VirtualData.localSingles.get(position).switchSelectStatus();
		}
	}
	
	class CheckedChangeListener implements OnCheckedChangeListener {
		private int position;
		public CheckedChangeListener(int position){
			this.position = position;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			VirtualData.localSingles.get(position).switchSelectStatus();
		}
	}

}
