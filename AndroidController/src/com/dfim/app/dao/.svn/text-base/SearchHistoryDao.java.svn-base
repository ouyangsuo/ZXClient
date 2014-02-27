package com.dfim.app.dao;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.SQLException;

import com.dfim.app.common.UpnpApp;
import com.dfim.app.db.DBHelper;
import com.union.cellremote.R;

public class SearchHistoryDao {

	/**
	 * 插入一条搜索数据
	 */
	public void insertSearchRecord(String str) {
		try {
//			ContentValues cv = new ContentValues();
//			cv.put("search_text", str);
//			cv.put("timemills", System.currentTimeMillis());
//			DBHelper.getSqLitedatabase().insert("db_search_history", null, cv);
			
			String sql="replace into db_search_history(search_text,timemillis) values('"+str+"',"+System.currentTimeMillis()+")";
			DBHelper.getSqLitedatabase().execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询数据表，按条件获取搜索记录
	 */
	public ArrayList<String> getSearchHistory(String customInput, int searchHistoryItemsShown) {
		ArrayList<String> arrayList=new ArrayList<String>();
		
		String sql="";
		if("".equals(customInput.trim())){
			sql="select search_text from db_search_history where in_use=1 order by timemillis desc limit 0,"+searchHistoryItemsShown;
		}else{
			sql="select search_text from db_search_history where in_use=1 and search_text like '"+customInput+"%' order by timemillis desc limit 0,"+searchHistoryItemsShown;
		}
		
		Cursor c = DBHelper.getSqLitedatabase().rawQuery(sql, null);
		while (c.moveToNext()) {
			arrayList.add(c.getString(0));
		}
		if(c!=null){
			c.close();
		}	
		System.out.println("arrayList got from db="+arrayList);
		
		return arrayList;
	}

	public boolean clearclearSearchHistory() {
		String sql="update db_search_history set in_use=0";
		try {
			DBHelper.getSqLitedatabase().execSQL(sql);
		} catch (SQLException e) {
			//showAlert
			UpnpApp.mainHandler.showAlert(R.string.clear_search_history_failure);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
