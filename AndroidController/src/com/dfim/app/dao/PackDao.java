package com.dfim.app.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dfim.app.db.DBHelper;
import com.dfim.app.domain.Music;
import com.dfim.app.domain.Pack;

public class PackDao {
   /**
    *根据id检测是否存pack
    */
	public boolean checkPackId(long id){
		Cursor cur=DBHelper.getSqLitedatabase().rawQuery("select id from db_pack where id="+id,null);
		   if(cur.getCount()>0){
			   return true;
		   } 
		   
			if(cur!=null){
    			cur.close();
    		}
			   return false;
		   
	}
	/**
	 * 插入pack
	 */
	public void insertPack(Pack pack){
		if(pack==null){
			return ;
		}
		try{
			DBHelper.getSqLitedatabase().beginTransaction();
		if(!checkPackId(pack.getId())){
			   ContentValues addv=new ContentValues();
			   addv.put("id",pack.getId());
			   addv.put("name",pack.getName());
			   addv.put("image_url",pack.getImgurl());
			   addv.put("buytime",pack.getBuytime());
			   addv.put("libraryid",pack.getLibraryid());
			   addv.put("isclearcache",pack.getIsCloud());
			 if(DBHelper.getSqLitedatabase().insert("db_pack",null,addv)>0){
				addv.clear();
				 addv.put("pack_id", pack.getId());
			
				 for(String musicid:pack.getMusicids()){
					 addv.put("product_id",musicid);
					
					 DBHelper.getSqLitedatabase().insert("product_pack", null,addv );
					 String updateMusicSql="update db_music set pack_id = "+pack.getId() +" where id ="+musicid;
					 DBHelper.getSqLitedatabase().execSQL(updateMusicSql);
				   
			   }
			 }
			 
		 
		}else{
			 DBHelper.getSqLitedatabase().execSQL("update db_pack set isclearcache ="+pack.getIsCloud()+" where id="+pack.getId());
		}
		}finally{
			DBHelper.getSqLitedatabase().setTransactionSuccessful();
			 DBHelper.getSqLitedatabase().endTransaction();
		}
	}
	/**
	 *查找所有主题
	 */
	
	public List<Pack> getAllPack(){
	
		SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
		Cursor cur=  mdb.rawQuery("select d.id ,d.name,d.image_url ,count(*) from db_pack d inner join product_pack pp on pp.pack_id =d.id inner join db_music pm on pm.id=pp.product_id where d.isclearcache = 5 group by d.id order by d.buytime desc", null);
		List<Pack> li= new ArrayList<Pack>();
		if(cur!=null){
			if(cur.getCount()==0){
				return null;
			}
			Pack pack=null;
			while (cur.moveToNext()){//
				pack=new Pack();
				pack.setId(cur.getLong(0));
				pack.setName(cur.getString(1));
				pack.setImgurl(cur.getString(2));
				pack.setMcount(cur.getInt(3));
				pack.setIsCloud(5);
				li.add(pack);
			}
			
			if(cur!=null){
    			cur.close();
    		}
		}
		return li;
		
	}
	/**
	 * 根据id查看主题详情
	 */
	public Pack getPackDetailById(Pack pack ){
		if(pack==null){
			return null;
		}
     SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
     String sql="select tempsmus.name ,tempsmus.media_url,tempsmus.id,tempsmus.img_url ,tempsmus.artistname  from  (select db_music.name,db_music.id,db_music.media_url,db_music.disk_id,db_music.first_char,db_album.img_url ,dba.name artistname,db_music.isclearcache from  db_music inner join db_disk on db_music.disk_id =db_disk.id inner join db_album on db_album.id =db_disk.album_id inner join product_artist pa on pa.product_id =db_music.id left join db_artist dba on dba.id =pa.artist_id  where db_music.isclearcache = 5  group by db_music.id order by db_music.id desc ) tempsmus inner join product_pack pp on pp.product_id = tempsmus.id inner join db_pack dbp on dbp.id=pp.pack_id where dbp.id="+pack.getId();
     Cursor cur=  mdb.rawQuery(sql, null);

	 List<Music> musicli=new ArrayList<Music>();
	 if(cur!=null){
		 Music music=null;
		while(cur.moveToNext()){
		  music=new Music();
		  music.setName(cur.getString(0));
		  music.setMediaurl(cur.getString(1));
		  music.setId(cur.getLong(2));
		  music.setImgUrl(cur.getString(3));
		  music.setArtistName(cur.getString(4));
		  music.setIscloud(5);
		  musicli.add(music);
		}
		pack.setLi(musicli);
		pack.setIsCloud(5);
		if(cur!=null){
			cur.close();
		}
	 }
	 return pack;
		
	}
	
	/**
	 * 更新清楚缓存状态信息
	 * @param map
	 */
	public boolean updateCloudStates(Map<String,List<Long>> map, boolean noAlbum){
		   List<Long> li=map.get("themes");
		   ContentValues addv=new ContentValues();
		   boolean flag=true;
			try{
				DBHelper.getSqLitedatabase().beginTransaction();
				
				// addv.put("isclearcache", 5);//5表示需要显示的
				 String ids="";
				 String sqlall="update db_pack set isclearcache = 0";
				 DBHelper.getSqLitedatabase().execSQL(sqlall); //全部显示
			   for(Long l:li){
				   ids=ids+l+",";
				 
			   }
			   
			   //如果专辑已清空，不显示主题
			   if(!ids.equals("")&&!noAlbum){
				   Log.i("UpdateCloudTest", "ids:" + ids);
				   ids= ids.substring(0,ids.length()-1);
				   String updateisclear="update db_pack set isclearcache = 5 where id in ( "+ids +" )";
				   DBHelper.getSqLitedatabase().execSQL( updateisclear);
			   }
			   
		       DBHelper.getSqLitedatabase().setTransactionSuccessful();
			}catch(Exception e){
			 e.printStackTrace();
			 flag=false;
		  }finally{
				DBHelper.getSqLitedatabase().endTransaction();
		  }
			return  flag; 
			
		}
	/**
	 * 在云端
	 */
	public List<Pack> getPackListForCloud(long pageIndex,int pageSize){
		SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
		Cursor cur=  mdb.rawQuery("select d.id ,d.name,d.image_url ,count(*),d.isclearcache  from db_pack d inner join product_pack pp on pp.pack_id =d.id inner join db_music pm on pm.id=pp.product_id where d.isclearcache != 5 group by d.id order by d.buytime desc limit  "+pageIndex+" , "+pageSize, null);
		List<Pack> li= new ArrayList<Pack>();
		if(cur!=null){
			if(cur.getCount()==0){
				return null;
			}
			Pack pack=null;
			while (cur.moveToNext()){//
				pack=new Pack();
				pack.setId(cur.getLong(0));
				pack.setName(cur.getString(1));
				pack.setImgurl(cur.getString(2));
				pack.setMcount(cur.getInt(3));
				pack.setIsCloud(cur.getInt(4));
				li.add(pack);
			}
			
			if(cur!=null){
    			cur.close();
    		}
		}
		return li;
	}
	/**
	 * 全部数据 本地和云端 
	 */
	public List<Pack> getAllPackList(long pageIndex,int pageSize){
		SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
		Cursor cur=  mdb.rawQuery("select d.id ,d.name,d.image_url ,count(*),d.isclearcache from db_pack d inner join product_pack pp on pp.pack_id =d.id inner join db_music pm on pm.id=pp.product_id  group by d.id order by d.buytime desc limit "+pageIndex+" , "+pageSize, null);
		List<Pack> li= new ArrayList<Pack>();
		if(cur!=null){
			if(cur.getCount()==0){
				return null;
			}
			Pack pack=null;
			while (cur.moveToNext()){//
				pack=new Pack();
				pack.setId(cur.getLong(0));
				pack.setName(cur.getString(1));
				pack.setImgurl(cur.getString(2));
				pack.setMcount(cur.getInt(3));
				pack.setIsCloud(cur.getInt(4));
				li.add(pack);
			}
			
			if(cur!=null){
    			cur.close();
    		}
		}
		return li;
	}
	/**
	 *查找所有主题
	 */
	
	public List<Pack> getAllPack(long pageIndex,int pageSize){
	
		SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
		Cursor cur=  mdb.rawQuery("select d.id ,d.name,d.image_url ,count(*),d.isclearcache from db_pack d inner join product_pack pp on pp.pack_id =d.id inner join db_music pm on pm.id=pp.product_id where d.isclearcache = 5 group by d.id order by d.buytime desc limit "+pageIndex+" , "+pageSize, null);
		List<Pack> li= new ArrayList<Pack>();
		if(cur!=null){
			if(cur.getCount()==0){
				return null;
			}
			Pack pack=null;
			while (cur.moveToNext()){//
				pack=new Pack();
				pack.setId(cur.getLong(0));
				pack.setName(cur.getString(1));
				pack.setImgurl(cur.getString(2));
				pack.setMcount(cur.getInt(3));
				pack.setIsCloud(cur.getInt(4));
				li.add(pack);
			}
			
			if(cur!=null){
    			cur.close();
    		}
		}
		return li;
		
	}
	
	public void updatePackState(String[] idArray,int state){
		  try{
			  	for(int i=0; i< idArray.length; i++){
				  	long id = Long.parseLong(idArray[i]);
					// addv.put("isclearcache", 5);//5表示需要显示的
			         DBHelper.getSqLitedatabase().beginTransaction();
					 String sqlall="update db_pack set isclearcache =" + state + "  where id =" + id;
					 DBHelper.getSqLitedatabase().execSQL(sqlall); 
				     DBHelper.getSqLitedatabase().setTransactionSuccessful();
			  	}
			  }catch(Exception e){
				 e.printStackTrace();
				
			  }finally{
				  DBHelper.getSqLitedatabase().endTransaction();
			  }
			
	}
	
	public void updatePackState(long id,int state){
		  try{
				// addv.put("isclearcache", 5);//5表示需要显示的
		         DBHelper.getSqLitedatabase().beginTransaction();
				 String sqlall="update db_pack set isclearcache =" + state + "  where id =" + id;
				 DBHelper.getSqLitedatabase().execSQL(sqlall); 
		
			     DBHelper.getSqLitedatabase().setTransactionSuccessful();
			  }catch(Exception e){
				 e.printStackTrace();
				
			  }finally{
				  DBHelper.getSqLitedatabase().endTransaction();
			  }
			
	}
	/**
	 * 根据id查看主题详情
	 */
	public Pack getPackDetailById(long id ){
	 Pack pack=null;
	 int iscloud=-1;
     SQLiteDatabase  mdb=DBHelper.getSqLitedatabase();
     String sql="select tempsmus.name ,tempsmus.media_url,tempsmus.id,tempsmus.img_url ,tempsmus.artistname,tempsmus.isclearcache,dbp.isclearcache from  (select db_music.name,db_music.id,db_music.media_url,db_music.disk_id,db_music.first_char,db_album.img_url ,dba.name artistname,db_music.isclearcache from  db_music inner join db_disk on db_music.disk_id =db_disk.id inner join db_album on db_album.id =db_disk.album_id inner join product_artist pa on pa.product_id =db_music.id left join db_artist dba on dba.id =pa.artist_id  where db_music.isclearcache = 5  group by db_music.id order by db_music.id desc ) tempsmus inner join product_pack pp on pp.product_id = tempsmus.id inner join db_pack dbp on dbp.id=pp.pack_id where dbp.id="+id;
     Cursor cur=  mdb.rawQuery(sql, null);
	 List<Music> musicli=new ArrayList<Music>();
	 if(cur!=null&&cur.getCount()>0){
		 pack=new Pack();
		 Music music=null;
		while(cur.moveToNext()){
		  music=new Music();
		  music.setName(cur.getString(0));
		  music.setMediaurl(cur.getString(1));
		  music.setId(cur.getLong(2));
		  music.setImgUrl(cur.getString(3));
		  music.setArtistName(cur.getString(4));
		  music.setIscloud(cur.getInt(5));
		  musicli.add(music);
		  pack.setIsCloud(cur.getInt(6));
		}
		
		pack.setLi(musicli);
	
		if(cur!=null){
			cur.close();
		}
	 }
	 return pack;
		
	}
}
