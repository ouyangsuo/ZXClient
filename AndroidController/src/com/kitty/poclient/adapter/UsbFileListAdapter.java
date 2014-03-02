package com.kitty.poclient.adapter;

import java.util.List;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.fragment.usb.ExternalDeviceFragment;
import com.kitty.poclient.util.UsbFileUtil;

public class UsbFileListAdapter extends BaseAdapter{
	private static String TAG = "UsbFileListAdapter";
	
	private LayoutInflater mInflater;
	private final List<DIDLObject> didlObjectList;
	public UsbFileListAdapter(Context context, final List<DIDLObject> files){
	    /* 参数初始化 */
	    mInflater = LayoutInflater.from(context);
	    this.didlObjectList = files;
	}
	
	@Override
	public int getCount() {
		return didlObjectList.size();
	}

	@Override
	public Object getItem(int position) {
		return didlObjectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private void updatePlayStutasImage(String playingUri, Holder holder, DIDLObject didlObject){
		if (playingUri.equals(didlObject.getId()) // 在文件列表点击即显示“小喇叭” cue文件
				|| playingUri.equals("xxbox://usb?source=" + didlObject.getId()) //音乐文件
				|| playingUri.equals(didlObject.getId().split("&start")[0]) )
		{
			holder.ivChosen.setVisibility(View.VISIBLE);
			holder.ivPlaying.setVisibility(View.VISIBLE);	
			holder.ivPlaying.setText("");
			holder.ivPlaying.setBackgroundResource(R.drawable.playing_icon);
		}else{
			holder.ivChosen.setVisibility(View.INVISIBLE);
			holder.ivPlaying.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null)
	    {
	      /* 使用自定义的wailianitems作为Layout */
		  convertView = mInflater.inflate(R.layout.file_item, null);
		  holder = new Holder(convertView);	
		  holder.fileicon = (ImageView) convertView.findViewById(R.id.fileicon);
	      holder.filepath = (TextView) convertView.findViewById(R.id.filepath);
	      holder.filename = (TextView) convertView.findViewById(R.id.filename);
	      convertView.setTag(holder);
	    }else{
	    	holder = (Holder) convertView.getTag();
	    }

		if(didlObjectList != null 
				&& didlObjectList.size()>0){
			DIDLObject didlObject = didlObjectList.get(position);
			try {
				// 判断是否为cue的子文件
				if (UsbFileUtil.isCue(didlObject.getParentID())) { // cue child
					holder.fileicon.setImageResource(R.drawable.usb_music);
					holder.filepath.setText(didlObject.getParentID() + "/" + didlObject.getTitle());
					holder.filename.setText(didlObject.getTitle());
				} else {
					holder.filepath.setText(didlObject.getId());
					String filename = UsbFileUtil.getFileNameFromAbsolutePath(didlObject.getId());
					holder.filename.setText(filename);
					String lowerfilename = filename.toLowerCase();
					if (didlObject instanceof Container) {
						if (UsbFileUtil.isUsbRootDir(didlObject.getParentID())) {
							holder.fileicon.setImageResource(R.drawable.usb_disk);// USB根目录
						} else {
							holder.fileicon.setImageResource(R.drawable.usb_folder);// USB子目录
						}
					} else if (didlObject instanceof Item) {
						if (UsbFileUtil.isMusic(lowerfilename)) {
							holder.fileicon.setImageResource(R.drawable.usb_music);
						} else if (UsbFileUtil.isVideo(lowerfilename)) {
							holder.fileicon.setImageResource(R.drawable.usb_video);
						} else {
							// TODO 其他未支持类型
						}
					}
				}
		    	
		    	//小啦叭
				//主动显示 控制端
				if(ExternalDeviceFragment.currentUri!=null){
					updatePlayStutasImage(ExternalDeviceFragment.currentUri, holder, didlObject);
				}
			} catch (Exception e) {
				holder.fileicon.setImageResource(R.drawable.usb_folder);
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
		}
		return convertView;
	}
	
    class Holder{
    	public ImageView fileicon;
    	public TextView filename;
    	public TextView filepath;
    	

//		private TextView tvName;
		private TextView tvArtist;
		private TextView tvNum;
		private ImageView ivSavingState;
		private ImageView ivChosen;
		private TextView ivPlaying;

		public Holder(View convertView) {
//			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
//			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist); 
			ivSavingState = (ImageView) convertView.findViewById(R.id.iv_saving_state);
			ivChosen = (ImageView) convertView.findViewById(R.id.iv_chosen);
			ivPlaying = (TextView) convertView.findViewById(R.id.iv_playing);
			
//			tvArtist.setVisibility(View.GONE);
//			tvArtist.setVisibility(View.GONE);
//			tvArtist.setVisibility(View.GONE);
//			tvArtist.setVisibility(View.GONE);
		}
    }
}
