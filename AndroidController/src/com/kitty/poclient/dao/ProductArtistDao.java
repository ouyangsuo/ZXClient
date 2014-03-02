package com.kitty.poclient.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.kitty.poclient.db.DBHelper;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.ProductArtist;

public class ProductArtistDao  {
       
	/**
	 * 查询是否商品演出者是否存在
	 * @return
	 */
	public synchronized  boolean checkProductArtist(ProductArtist pa){
		if(pa==null){
			return false;
		}
		Cursor cur= DBHelper.getSqLitedatabase().rawQuery("select * from product_artist pa where pa.product_id ="+pa.getProductId() +" and pa.artist_id ="+pa.getArtistId(), null);
	   if( cur.getCount()>0){
		   return true;
	   }
		if(cur!=null){
			cur.close();
		}
	   return false;
	}
	
	/**
	 *插入关联表 
	 */
	public synchronized void insertProductArtist(ProductArtist pa){
		try{
			DBHelper.getSqLitedatabase().beginTransaction();
		if(!checkProductArtist(pa)){
			String sql="insert into product_artist (product_id,artist_id) values ("+pa.getProductId()+","+pa.getArtistId()+")";
	    	DBHelper.getSqLitedatabase().execSQL(sql);
	    	DBHelper.getSqLitedatabase().setTransactionSuccessful();
		}
		}finally{
			DBHelper.getSqLitedatabase().endTransaction();
		}
	}
	/**
	 * 插入关联表 默认给商品插入未知演出者
	 */
	public synchronized void inserProudctArtistDefault(Long musicid){
		ProductArtist pa =new ProductArtist();
		pa.setProductId(musicid);
		Artist artist=new Artist();
		String selArtist="select id from  db_artist  where name like '未知%'";
		try{
			DBHelper.getSqLitedatabase().beginTransaction();
	    Cursor cur=DBHelper.getSqLitedatabase().rawQuery(selArtist, null);
		   if(cur.getCount()>0){
			   while(cur.moveToNext()){
				   pa.setArtistId(cur.getLong(0));
				   Cursor c=DBHelper.getSqLitedatabase().rawQuery("select product_id from product_artist where product_id = "+musicid +" and artist_id = "+pa.getArtistId(),null);
				   if(c.getCount()>0){
					   c.close();
					   break;
					   
				   }else{
				   String inserProArt="insert into product_artist (product_id,artist_id) values ("+pa.getProductId()+","+pa.getArtistId()+")";
				   DBHelper.getSqLitedatabase().execSQL(inserProArt);
				   }
			   }
		   }else{
			   artist.setId(System.currentTimeMillis());
			   artist.setName("未知演出者");
			   artist.setFirstChar("w");
			   ContentValues content=new ContentValues();
			   content.put("id",artist.getId());
			   content.put("name","未知演出者");
			   content.put("firstchar", "w");
			   content.put("img_url", "");
			   DBHelper.getSqLitedatabase().insert("db_artist", null,content);
			   pa.setArtistId(artist.getId());
			   Cursor c=DBHelper.getSqLitedatabase().rawQuery("select product_id from product_artist where product_id = "+musicid +" and artist_id = "+pa.getArtistId(),null);
			   if(c.getCount()>0){
				   c.close();
			   }else{
			   String inserProArt="insert into product_artist (product_id,artist_id) values ("+pa.getProductId()+","+pa.getArtistId()+")";
			   DBHelper.getSqLitedatabase().execSQL(inserProArt);
		 
			   }}
		    DBHelper.getSqLitedatabase().setTransactionSuccessful();
		}finally{
			DBHelper.getSqLitedatabase().endTransaction();
		}
		
	}
	
}
