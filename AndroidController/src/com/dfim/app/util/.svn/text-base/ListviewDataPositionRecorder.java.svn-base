package com.dfim.app.util;

import java.util.List;
import java.util.Map;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ListviewDataPositionRecorder {
	
	public static final String TAG = "ListviewPositionRecorder:";
	private int firstVisibleItemPosition = 0;
	private int scrollTop = 0;
	
	private Object[] dataArray;
	private List dataList;
	private Map dataMap;

	public int getFirstVisibleItemPosition() {
		return firstVisibleItemPosition;
	}

	public void setFirstVisibleItemPosition(int firstVisibleItemPosition) {
		this.firstVisibleItemPosition = firstVisibleItemPosition;
	}

	public int getScrollTop() {
		return scrollTop;
	}

	public void setScrollTop(int scrollTop) {
		this.scrollTop = scrollTop;
	}
	
	public List getDataList() {
		return dataList;
	}

	public void setDataList(List dataList) {
		this.dataList = dataList;
	}

	public Map getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map dataMap) {
		this.dataMap = dataMap;
	}

	public Object[] getDataArray() {
		return dataArray;
	}

	public void setDataArray(Object[] dataArray) {
		this.dataArray = dataArray;
	}

	public ListviewDataPositionRecorder registerListviews(ListView[] listViews) {
		for (final ListView lv : listViews) {
			lv.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						Log.e("BUG913", TAG+"onScrollStateChanged:SCROLL_STATE_IDLE");
						setFirstVisibleItemPosition(lv.getFirstVisiblePosition());
						if (lv.getChildAt(0) != null) {
							setScrollTop(lv.getChildAt(0).getTop());
						}
						
						Log.e("BUG913", TAG+" lv.getFirstVisiblePosition()="+lv.getFirstVisiblePosition());
						Log.e("BUG913", TAG+" lv.getChildAt(0).getTop()="+lv.getChildAt(0).getTop());
					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub

				}
			});
		}
		
		Log.e("BUG913", "registerListviews:this="+this);
		return this;
	}
	
	@Override
	public String toString() {
		return "{LPRecorder: firstVisibleItemPosition="+firstVisibleItemPosition+",scrollTop="+scrollTop+",data="+dataArray+",dataMap="+dataMap+"}";
	}

}
