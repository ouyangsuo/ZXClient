package com.dfim.app.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.union.cellremote.R;

public class SearchHistoryListAdapter extends BaseAdapter {

	private Context context = null;
	private ArrayList<String> historyList = new ArrayList<String>();

	public SearchHistoryListAdapter(Context context) {
		super();
		this.context = context;
	}

	public ArrayList<String> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(ArrayList<String> historyList) {
		this.historyList = historyList;
	}

	@Override
	public int getCount() {
		return historyList.size();
	}

	@Override
	public Object getItem(int position) {
		return (String) historyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.search_history_list_item, null);
			
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.tvRecord.setText(historyList.get(position));

		return convertView;
	}
	
	class Holder {
		private TextView tvRecord;

		public Holder(View convertView) {
			tvRecord = (TextView) convertView.findViewById(R.id.tv_record);
		}
	}

}
