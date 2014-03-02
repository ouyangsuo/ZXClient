package com.kitty.poclient.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.kitty.poclient.db.DBHelper;
import com.kitty.poclient.domain.Disk;

public class DiskDao {
	  /**
	   * 检查碟是否存在
	   * @param d
	   * @return
	   */
     public boolean  getDisk(Disk d){
    	 if(d==null){
    		 return false;
    	 }
    	 String sql="select id from db_disk where id= " +d.getId();
    		Cursor cur = DBHelper.getSqLitedatabase().rawQuery(sql, null);
    	 int i=cur.getCount();
    		if (cur.getCount() > 0) {
    			return true;
    		}
    		if(cur!=null){
    			cur.close();
    		}
    		return false;
    	 
     }
     
     /**
      * 添加die
      */
     public synchronized  boolean insertDisk(Disk d){
           if(getDisk(d)){
        	   return false ;
           }else{
          try{ 
        	   DBHelper.getSqLitedatabase().beginTransaction();
        	   ContentValues con=new ContentValues();
        	   con.put("id", d.getId());
        	   con.put("name", d.getName());
        	   con.put("disk_no", d.getDisk_no());
        	   con.put("album_id", d.getAlbum_id());
        	  if( DBHelper.getSqLitedatabase().insert("db_disk", null,con)>0){
        		DBHelper.getSqLitedatabase().setTransactionSuccessful();
        		  return true;
        		  
        	  };
        	  
        	  }finally{
        		 DBHelper.getSqLitedatabase().endTransaction();
        	  }
           }
    	 
    	   return false;
     }
     
}
