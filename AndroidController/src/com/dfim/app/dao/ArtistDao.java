package com.dfim.app.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.dfim.app.db.DBHelper;
import com.dfim.app.domain.Artist;

public class ArtistDao {

	/**
	 * @de 检查演出者是否存在
	 * @param id
	 * @return
	 */
	public  boolean checkArtistById(Long id) {
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery(
				"select id from db_artist where id= " + id, null);
		if (cur.getCount() > 0) {
			return true;
		}
		if(cur!=null){
			cur.close();
		}
		return false;
		
	}

	public void  insertArtist(Artist artist) {
		if(artist==null){
			return ;
		}
		if (checkArtistById(artist.getId())) {
			return;
		} else {
			ContentValues addcv = new ContentValues();// 实例化数据库容器
		
			addcv.put("id", artist.getId());
			addcv.put("name", artist.getName());
			addcv.put("img_url", artist.getImgUrl());
			addcv.put("firstchar", artist.getFirstChar());
			try{
			DBHelper.getSqLitedatabase().beginTransaction();
			DBHelper.getSqLitedatabase().insert("db_artist", null, addcv);
			DBHelper.getSqLitedatabase().setTransactionSuccessful();
			}finally{
				DBHelper.getSqLitedatabase().endTransaction();
			}
		}
	}

}
