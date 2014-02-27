package com.dfim.app.dao;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dfim.app.db.DBHelper;
import com.dfim.app.domain.Music;
import com.dfim.app.util.UniqueId;

public class MusicDao {
	/**
	 * 根据id检测是否存在id
	 * 
	 * @param id
	 * @return
	 */

	public boolean getMusicById(Long id) {
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select id from db_music where id = " + id, null);
		if (cur.getCount() > 0) {
			return true;
		}
		if (cur != null) {
			cur.close();
		}

		return false;

	}

	/**
	 * 插入id
	 */
	public synchronized void insertMusic(Music music) {
		if (music == null) {
			return;
		}
		try {
			DBHelper.getSqLitedatabase().beginTransaction();
			if (!getMusicById(music.getId())) {
				String sql = "select db.id from db_disk db where id= " + music.getDiskId();
				// Cursor cur= DBHelper.getSqLitedatabase().rawQuery(sql, null);

				ContentValues addv = new ContentValues();

				// while(cur.moveToNext()){
				// music.setDiskId(cur.getLong(0));
				// }

				if (music.getDiskId() == 0) {
					music.setDiskId(UniqueId.genId());
					addv.put("id", music.getDiskId());
					addv.put("name", music.getDiskName());
					addv.put("disk_no", music.getDisk_no());
					addv.put("album_id", music.getAlbumId());
					DBHelper.getSqLitedatabase().insert("db_disk", null, addv);

				} else {
					Cursor cur = DBHelper.getSqLitedatabase().rawQuery(sql, null);
					if (cur.getCount() > 0) {

					} else {
						addv.put("id", music.getDiskId());
						addv.put("name", music.getDiskName());
						addv.put("disk_no", music.getDiskName());
						addv.put("album_id", music.getAlbumId());
						DBHelper.getSqLitedatabase().insert("db_disk", null, addv);
					}
					if (cur != null) {
						cur.close();
					}

				}

				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				addv.clear();
				addv.put("buytime", music.getBuytime());
				DBHelper.getSqLitedatabase().update("db_album", addv, "id=" + music.getAlbumId(), null);
				addv.clear();
				addv.put("id", music.getId());
				addv.put("name", music.getName());
				addv.put("media_url", music.getMediaurl());
				addv.put("disk_id", music.getDiskId());
				addv.put("first_char", music.getFirstChar());
				addv.put("track_no", music.getTrack_no());
				addv.put("play_time", music.getPlay_time());
				addv.put("file_size", music.getFile_size());
				addv.put("buytime", music.getBuytime());
				addv.put("lib_id", music.getLibid());
				addv.put("isclearcache", music.getIscloud());
				DBHelper.getSqLitedatabase().insert("db_music", null, addv);

			} else {
				DBHelper.getSqLitedatabase().execSQL("update db_music set isclearcache = " + music.getIscloud() + " where id=" + music.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBHelper.getSqLitedatabase().setTransactionSuccessful();
			DBHelper.getSqLitedatabase().endTransaction();
		}

	}

	/**
	 * 获取所有本地单曲
	 */
	public List<Music> getAllMusic() {
		List<Music> musicli = null;
		Music music = null;
		long ctime = System.currentTimeMillis();
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id left join db_artist dba on dba.id= pa.artist_id where dbm.isclearcache = 5 and db_album.isclearcache=5  group by dbm.id order by dbm.buytime desc", null);
		if (cur.getCount() > 0) {
			musicli = new ArrayList<Music>();
			while (cur.moveToNext()) {
				music = new Music();
				music.setId(cur.getLong(0));
				music.setName(cur.getString(1));
				music.setPlay_time(cur.getString(2));
				music.setFile_size(cur.getString(3));
				music.setFirstChar(cur.getString(4));
				music.setTrack_no(cur.getString(5));
				music.setMediaurl(cur.getString(6));
				music.setArtistName(cur.getString(7));
				music.setArtistId(cur.getString(8));
				music.setImgUrl(cur.getString(9));
				music.setIscloud(5);
				// music.set
				musicli.add(music);
			}
		}

		if (cur != null) {
			cur.close();
		}
		return musicli;

	}

	/**
	 * 更新清楚缓存状态信息
	 * 
	 * @param map
	 */
	public boolean updateCloudStates(Map<String, List<Long>> map) {
		List<Long> li = map.get("musics");
		ContentValues addv = new ContentValues();
		boolean flag = true;
		try {
			DBHelper.getSqLitedatabase().beginTransaction();
			
			// addv.put("isclearcache", 5);//5表示需要显示的
			String ids = "";
			String sqlall = "update db_music set isclearcache = 0";
			DBHelper.getSqLitedatabase().execSQL(sqlall); // 全部显示
			for (Long l : li) {
				ids = ids + l + ",";

			}
			if(!ids.equals("")){
				ids = ids.substring(0, ids.length() - 1);
				String updateisclear = "update db_music set isclearcache = 5 where id in ( " + ids + " )";
				DBHelper.getSqLitedatabase().execSQL(updateisclear);
			}
			
			DBHelper.getSqLitedatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		} finally {
			DBHelper.getSqLitedatabase().endTransaction();
		}
		return flag;

	}

	/**
	 * 在云端
	 */
	public List<Music> getMusicListForCloud(long pageIndex, int pageSize) {
		List<Music> musicli = null;
		Music music = null;
		long ctime = System.currentTimeMillis();
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url,dbm.isclearcache  from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id inner join db_artist dba on dba.id= pa.artist_id where dbm.isclearcache = 0   group by dbm.id order by dbm.buytime desc limit " + pageIndex + " , " + pageSize, null);

		if (cur.getCount() > 0) {
			musicli = new ArrayList<Music>();
			while (cur.moveToNext()) {
				music = new Music();
				music.setId(cur.getLong(0));
				music.setName(cur.getString(1));
				music.setPlay_time(cur.getString(2));
				music.setFile_size(cur.getString(3));
				music.setFirstChar(cur.getString(4));
				music.setTrack_no(cur.getString(5));
				music.setMediaurl(cur.getString(6));
				music.setArtistName(cur.getString(7));
				music.setArtistId(cur.getString(8));
				music.setImgUrl(cur.getString(9));
				music.setIscloud(cur.getInt(10));
				// music.set
				musicli.add(music);
			}
		}
		if (cur != null) {
			cur.close();
		}
		Log.i("PurchasedFragment", "毫秒：" + System.currentTimeMillis() + "用了" + (System.currentTimeMillis() - ctime) + "毫秒");
		return musicli;

	}

	/**
	 * 全部单曲，本地和云端
	 */
	public List<Music> getAllMusicList(long pageIndex, int pageSize) {
		List<Music> musicli = null;
		Music music = null;
		long ctime = System.currentTimeMillis();

		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url,dbm.isclearcache  from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id  inner join db_artist dba on dba.id= pa.artist_id   group by dbm.id order by dbm.buytime desc limit " + pageIndex + " , " + pageSize, null);
		if (cur.getCount() > 0) {
			musicli = new ArrayList<Music>();
			while (cur.moveToNext()) {
				music = new Music();
				music.setId(cur.getLong(0));
				music.setName(cur.getString(1));
				music.setPlay_time(cur.getString(2));
				music.setFile_size(cur.getString(3));
				music.setFirstChar(cur.getString(4));
				music.setTrack_no(cur.getString(5));
				music.setMediaurl(cur.getString(6));
				music.setArtistName(cur.getString(7));
				music.setArtistId(cur.getString(8));
				music.setImgUrl(cur.getString(9));
				music.setIscloud(cur.getInt(10));
				// music.set
				musicli.add(music);
			}
		}
		if (cur != null) {
			cur.close();
		}
		return musicli;
	}

	/**
	 * 获取所有本地单曲
	 */
	public List<Music> getAllMusic(long pageIndex, int pageSize) {
		List<Music> musicli = null;
		Music music = null;
		long ctime = System.currentTimeMillis();
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url,dbm.isclearcache  from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id inner  join db_artist dba on dba.id= pa.artist_id where dbm.isclearcache = 5   group by dbm.id order by dbm.buytime desc limit  " + pageIndex + " , " + pageSize, null);

		if (cur.getCount() > 0) {
			musicli = new ArrayList<Music>();
			while (cur.moveToNext()) {
				music = new Music();
				music.setId(cur.getLong(0));
				music.setName(cur.getString(1));
				music.setPlay_time(cur.getString(2));
				music.setFile_size(cur.getString(3));
				music.setFirstChar(cur.getString(4));
				music.setTrack_no(cur.getString(5));
				music.setMediaurl(cur.getString(6));
				music.setArtistName(cur.getString(7));
				music.setArtistId(cur.getString(8));
				music.setImgUrl(cur.getString(9));
				music.setIscloud(cur.getInt(10));
				// music.set
				musicli.add(music);
			}
		}

		if (cur != null) {
			cur.close();
		}

		return musicli;

	}

	/**
	 * 更新云音乐状态
	 * 
	 * @param id
	 * @param state
	 */
	public void updateMusicState(String[] ids, int state) {
		try {
			// addv.put("isclearcache", 5);//5表示同步云端，0表示清除本地缓存
			DBHelper.getSqLitedatabase().beginTransaction();
			for (String id : ids) {
				DBHelper.getSqLitedatabase().execSQL("update db_music set isclearcache =" + state + "  where id =" + id);
			}
			DBHelper.getSqLitedatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			DBHelper.getSqLitedatabase().endTransaction();
		}
	}

	/**
	 * @description 根据单曲ID查询单曲的云状态：0在云端 5在本地 -1未购买
	 * @return int
	 */
	public int getMusicStateById(long id) {
		int state = -1;
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select  isclearcache from db_music where id=" + id, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				state = cur.getInt(0);
			}
		}
		if (cur != null) {
			cur.close();
		}
		return state;
	}

	/**
	 * @author: ouyang
	 * @throws SQLDataException
	 * @description: 查询单曲的专辑ID
	 */
	public long getMusicContainerAlbumId(long musicId) throws SQLDataException {
		long albumId = -1;

		String sql = "select album_id from db_disk where id=(select disk_id from db_music where id=" + musicId + ")";
		Cursor cursor = DBHelper.getSqLitedatabase().rawQuery(sql, null);

		if (cursor.getCount() != 1) {
			throw new SQLDataException("该单曲的包装专辑查询结果个数为：" + cursor.getCount());
		}

		while (cursor.moveToNext()) {
			albumId = cursor.getLong(0);
		}

		if (cursor != null) {
			cursor.close();
		}
		
		return albumId;
	}

}
