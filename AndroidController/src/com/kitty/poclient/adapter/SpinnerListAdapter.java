package com.kitty.poclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kitty.poclient.R;

public class SpinnerListAdapter extends BaseAdapter {
   
	private Context context;
	private String[] str;
	
	public SpinnerListAdapter (){
		
	}
	public SpinnerListAdapter (Context context,String [] str){
	   this.context=context;
	   this.str=str;
	}
	@Override
	public int getCount() {
	
		return str!=null?str.length:0;
	}

	@Override
	public Object getItem(int position) {
		return str!=null?str[position]:null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	   Holder holder;
		if(convertView == null||convertView.getTag()==null){
		   convertView =LayoutInflater.from(context).inflate(R.layout.drop_list_hover, null);
		   convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,context.getResources().getInteger(R.integer.popup_listitem_height_in_purchase)));
		   holder=new Holder(convertView);
	       convertView.setTag(holder);
		}else {
			holder =(Holder) convertView.getTag();
		}
		if(str==null){
			return null;
		}
		String s=str[position];
		holder.tvName.setText(s);
		
		return convertView;
	}
	class Holder {
		private TextView tvName;
	

		public Holder(View convertView) {
			tvName = (TextView) convertView.findViewById(R.id.spinnertest);
		
		}
	}

}
