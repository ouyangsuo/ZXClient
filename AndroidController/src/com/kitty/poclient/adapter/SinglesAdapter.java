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
		holder.tvArtist.setText(mlist.get(position).getArtistName());

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
			
//			singlePosition = (TextView) convertView.findViewById(R.id.single_position);	
			
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
