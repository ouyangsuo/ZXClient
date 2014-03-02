package com.kitty.poclient.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.upnp.BoxDevice;

public class LoginAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<BoxDevice> devices;
	
	public LoginAdapter(Context context, ArrayList<BoxDevice> devices){
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.devices = devices;
	}
	
	@Override
	public int getCount() {
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.layout_login_list_item, null);
			holder = new Holder();
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_device);
			holder.rbChoose = (RadioButton) convertView.findViewById(R.id.rb_choose);
			convertView.setTag(holder);
		}else{
			holder = (Holder) convertView.getTag();
		}
		
		BoxDevice device = devices.get(position);
		holder.tvName.setText(device.toString());
		if(device.equals(WatchDog.choseboxObj))
			holder.rbChoose.setBackgroundResource(R.drawable.select);
		else
			holder.rbChoose.setBackgroundResource(R.drawable.non_select);
		return convertView;
	}
	
	class Holder{
		private TextView tvName;
		private RadioButton rbChoose;
	}

}
