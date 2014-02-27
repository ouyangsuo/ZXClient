package com.dfim.app.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dfim.app.db.DBHelper;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.domain.Disk;
import com.union.cellremote.domain.Music;

public class AlbumDao {
	
	public static final String TAG="AlbumDao:";

	/**
	 * 检查专辑是否存在
	 */
	public boolean checkAlbum(Album album) {
		if (album != null) {
			Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select id from db_album where id = " + album.getId(), null);
			if (cur.getCount() > 0) {
				return true;
			}
			if (cur != null) {
				cur.close();
			}

		}
		return false;
	}

	/**
	 * 根据id获取专辑
	 */
	public Album getAlbumById(Long id) {
		if (id == null) {
			return null;
		}
		Album album = null;
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		Cursor cur = mdb.rawQuery("select dba.id albumId,dba.name albumName,dba.img_url albumImgurl,group_concat(dbr.id) artistId,group_concat(dbr.name) artistname " + "from db_album dba inner join product_artist pa on dba.id=pa.product_id left join db_artist dbr on dbr.id=pa.artist_id where   dba.id =" + id + "  group by albumId order by dba.buytime desc", null);
		// String temp="";
		List<Album> li = new ArrayList<Album>();
		if (cur != null) {
			if (cur.getCount() == 0) {
				// temp="";
				return null;
			}

			String[] artistId;
			String[] artistName;
			List<Artist> artistLi;
			Artist artist = null;
			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾

					album = new Album();
					album.setId(cur.getLong(0));
					album.setName(cur.getString(1));
					album.setImgUrl(cur.getString(2));
					artistId = cur.getString(3) != null ? cur.getString(3).split(",") : null;
					artistName = cur.getString(4) != null ? cur.getString(4).split(",") : null;
					artistLi = new ArrayList<Artist>();
					if (artistId != null && artistName != null && artistId.length > 0 && artistName.length > 0) {
						for (int i = 0; i < artistId.length; i++) {
							artist = new Artist();
							artist.setId(Long.parseLong(artistId[i]));
							artist.setName(artistName[i]);
							artistLi.add(artist);
						}
						album.setArtistli(artistLi);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return album;
	}

	/**
	 * 插入专辑
	 */
	public void insertAlbum(Album album) {
		if (!checkAlbum(album)) {
			ContentValues addcv = new ContentValues();
			addcv.put("id", album.getId());
			addcv.put("name", album.getName());
			addcv.put("img_url", album.getImgUrl());
			addcv.put("buytime", album.getBuytime());
			addcv.put("orderType", album.getOrderType());
			addcv.put("isclearcache", album.getIsCloud());
			DBHelper.getSqLitedatabase().insert("db_album", null, addcv);
		} else {
			DBHelper.getSqLitedatabase().execSQL("update db_album set isclearcache =" + album.getIsCloud() + " where id =" + album.getId());
		}

	}

	/**
	 * 根据id获得专辑下的所有单曲id
	 */
	public List<Long> getMusicIdListByAlbumId(Long id) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		List<Long> musicIdList = null;
		Cursor cur = mdb.rawQuery("select d.id from db_music d inner join db_disk dbd on d.disk_id =dbd.id inner join db_album dba on dba.id=dbd.album_id  where dba.id =" + id, null);
		if (cur != null) {
			if (cur.getCount() == 0) {
				return null;
			}
			musicIdList = new ArrayList();
			while (cur.moveToNext()) {
				musicIdList.add(cur.getLong(0));
			}
			cur.close();
		}
		return musicIdList;
	}

	/**
	 * 根据专辑id获得专辑下所有单曲
	 */
	public List<Music> getMusicListByAlbumId(Long id) {
		List<Music> musicli = null;
		Music music = null;

		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url,dbm.isclearcache  from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id inner  join db_artist dba on dba.id= pa.artist_id where dbm.isclearcache = 5 and db_album.id=" + id + "   group by dbm.id order by dbm.buytime", null);
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
	 * 获得多有专辑列表
	 * 
	 */
	public List<Album> albumlist() {

		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		Cursor cur = mdb.rawQuery("select dba.id albumId,dba.name albumName,dba.img_url albumImgurl,group_concat(dbr.id) artistId,group_concat(dbr.name) artistname " + "from db_album dba inner join product_artist pa on dba.id=pa.product_id left join db_artist dbr on dbr.id=pa.artist_id where dba.isclearcache = 5 group by albumId order by dba.buytime desc", null);
		// String temp="";
		List<Album> li = new ArrayList<Album>();
		if (cur != null) {
			if (cur.getCount() == 0) {
				// temp="";
				return null;
			}
			Album album = null;
			String[] artistId;
			String[] artistName;
			List<Artist> artistLi;
			Artist artist = null;

			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾

					album = new Album();
					album.setId(cur.getLong(0));
					album.setName(cur.getString(1));
					album.setImgUrl(cur.getString(2));
					artistId = cur.getString(3) != null ? cur.getString(3).split(",") : null;
					artistName = cur.getString(4) != null ? cur.getString(4).split(",") : null;
					artistLi = new ArrayList<Artist>();
					if (artistId != null && artistName != null && artistId.length > 0 && artistName.length > 0) {
						for (int i = 0; i < artistId.length; i++) {
							artist = new Artist();
							artist.setId(Long.parseLong(artistId[i]));
							artist.setName(artistName[i]);
							artistLi.add(artist);
						}
						album.setArtistli(artistLi);
						album.setIsCloud(5);
					}
					li.add(album);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return li;
	}

	/**
	 * 根据专辑的id查询某张专辑
	 */
	public AlbumDetail getAlbumDetailData(Long id) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		String sql = "select alb.id  albumid,alb.name albumname,dbd.id diskid ,dbd.name diskname,group_concat(tempmusic.musicid ,'&&%') musicId ,group_concat(tempmusic.musicname,'&&%') musicname,count(tempmusic.musicid) musicCount,group_concat(tempmusic.artistid ,'&&%') artistId,group_concat(tempmusic.artistname,'&&%') artistname ,alb.img_url" + " , group_concat(tempmusic.track_no,'&&%')  track_no from (select dbm.id musicid ,dbm.name musicname,dbm.track_no ,dbm.disk_id,group_concat(dba.id) artistid ,group_concat(dba.name) artistname " + " from db_music dbm inner join product_artist pa on pa.product_id = dbm.id  left join db_artist dba on dba.id=pa.artist_id where dbm.isclearcache = 5  group by musicid  order by track_no) tempmusic "
				+ " inner join db_disk dbd on  dbd.id= tempmusic.disk_id inner join db_album alb on alb.id=dbd.album_id where alb.id=" + id + " group by diskid order by dbd.disk_no";
		Cursor cur = mdb.rawQuery(sql, null);
		AlbumDetail albumdetail = new AlbumDetail();
		List<Disk> diskLi = new ArrayList<Disk>();
		String imgUrl = "";
		int musicCount = 0;
		if (cur != null) {

			if (cur.getCount() == 0) {
				return null;
			}
			String[] arraymusicid = null;
			String[] arraymusicname = null;
			String[] arrayartistname = null;
			String[] arrayartistId = null;
			String[] arraymusictrackno = null;
			Music music = null;
			Disk disk = null;
			while (cur.moveToNext()) {// 每条数据是一张碟
				List<Music> musicLi = new ArrayList<Music>();
				albumdetail.setAlbumId(cur.getLong(0));
				albumdetail.setAlbumname(cur.getString(1));
				imgUrl = cur.getString(9);

				disk = new Disk();
				disk.setId(cur.getLong(2));
				disk.setName(cur.getString(3));
				arraymusicid = cur.getString(4).split("&&%");
				arraymusicname = cur.getString(5) != null ? cur.getString(5).split("&&%") : null;
				arrayartistname = cur.getString(8) != null ? cur.getString(8).split("&&%") : null;
				arrayartistId = cur.getString(7) != null ? cur.getString(7).split("&&%") : null;
				arraymusictrackno = cur.getString(10) != null ? cur.getString(10).split("&&%") : null;
				if (arraymusicid != null) {
					for (int i = 0; i < arraymusicid.length; i++) {
						music = new Music();
						music.setId(Long.parseLong(arraymusicid[i]));
						music.setName(arraymusicname[i]);
						music.setIscloud(5);
						if (arrayartistId != null) {
							if (i < arrayartistId.length) {
								music.setArtistId(arrayartistId[i] == null ? "0" : arrayartistId[i]);
							}
						} else {
							music.setArtistId("0");
						}
						if (arrayartistname != null) {
							if (i < arrayartistname.length) {
								music.setArtistName(arrayartistname[i].equals("未知") ? "未知演出者" : arrayartistname[i]);
							}
						} else {
							music.setArtistName("");
						}
						if (arraymusictrackno != null) {
							music.setTrack_no(arraymusictrackno[i] == null ? "0" : arraymusictrackno[i]);
						}
						music.setImgUrl(imgUrl);
						musicLi.add(music);

					}

				}
				disk.setMusicList(musicLi);
				diskLi.add(disk);
				musicCount += cur.getInt(6);

			}
			albumdetail.setDisklist(diskLi);
			albumdetail.setMusicCount(musicCount);
		}

		if (cur != null) {
			cur.close();
		}
		return albumdetail;

	}

	/**
	 * 更新清楚缓存状态信息
	 * 
	 * @param map
	 */
	public boolean updateCloudStates(Map<String, List<Long>> map) {
		List<Long> li = map.get("albums");
		ContentValues addv = new ContentValues();
		boolean flag = true;
		try {
			// addv.put("isclearcache", 5);//5表示需要显示的
			DBHelper.getSqLitedatabase().beginTransaction();
			String ids = "";
			String sqlall = "update db_album set isclearcache = 0";
			DBHelper.getSqLitedatabase().execSQL(sqlall); // 全部显示
			for (Long l : li) {
				ids = ids + l + ",";

			}
			
			if(!ids.equals("")){
				ids = ids.substring(0, ids.length() - 1);
				String updateisclear = "update db_album set isclearcache = 5 where id in ( " + ids + " )";
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
	 * 查询在云端的音乐
	 * 
	 * @return
	 */
	public List<Album> getAlbumListForCloud(long pageIndex, int pageSize) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		Cursor cur = mdb.rawQuery("select dba.id albumId,dba.name albumName,dba.img_url albumImgurl,group_concat(dbr.id) artistId,group_concat(dbr.name) artistname ,dba.isclearcache " + "from db_album dba inner join product_artist pa on dba.id=pa.product_id left join db_artist dbr on dbr.id=pa.artist_id where dba.isclearcache != 5 group by albumId order by dba.buytime desc limit " + pageIndex + " , " + pageSize, null);
		// String temp="";
		List<Album> li = new ArrayList<Album>();
		if (cur != null) {
			if (cur.getCount() == 0) {
				// temp="";
				return null;
			}
			Album album = null;
			String[] artistId;
			String[] artistName;
			List<Artist> artistLi;
			Artist artist = null;

			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾
					album = new Album();
					album.setId(cur.getLong(0));
					album.setName(cur.getString(1));
					album.setImgUrl(cur.getString(2));
					album.setIsCloud(cur.getInt(5));
					artistId = cur.getString(3) != null ? cur.getString(3).split(",") : null;
					artistName = cur.getString(4) != null ? cur.getString(4).split(",") : null;
					artistLi = new ArrayList<Artist>();
					if (artistId != null && artistName != null && artistId.length > 0 && artistName.length > 0) {
						for (int i = 0; i < artistId.length; i++) {
							artist = new Artist();
							artist.setId(Long.parseLong(artistId[i]));
							artist.setName(artistName[i]);
							artistLi.add(artist);
						}
						album.setArtistli(artistLi);
					}
					li.add(album);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return li;
	}

	/**
	 * 根据专辑id查询所有
	 */
	public AlbumDetail getAlbumDetailForPurchased(Long id) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		String sql = "select alb.id  albumid,alb.name albumname,dbd.id diskid ,dbd.name diskname,group_concat(tempmusic.musicid ,'&&%') musicId ,group_concat(tempmusic.musicname,'&&%') musicname,count(tempmusic.musicid) musicCount,group_concat(tempmusic.artistid ,'&&%') artistId,group_concat(tempmusic.artistname,'&&%') artistname ,alb.img_url" + " , group_concat(tempmusic.track_no,'&&%')  track_no ,group_concat(tempmusic.isclearcache,'&&%') isclearcache ,group_concat(tempmusic.media_url,'&&%') media_url  from (select dbm.id musicid ,dbm.name musicname,dbm.track_no ,dbm.disk_id,group_concat(dba.id) artistid ,group_concat(dba.name) artistname ,dbm.isclearcache  ,dbm.media_url "
				+ " from db_music dbm inner join product_artist pa on pa.product_id = dbm.id left join db_artist dba on dba.id=pa.artist_id   group by musicid  order by track_no) tempmusic " + " inner join db_disk dbd on  dbd.id= tempmusic.disk_id inner join db_album alb on alb.id=dbd.album_id where alb.id=" + id + " group by diskid order by dbd.disk_no";
		Cursor cur = mdb.rawQuery(sql, null);
		AlbumDetail albumdetail = new AlbumDetail();
		List<Disk> diskLi = new ArrayList<Disk>();
		String imgUrl = "";
		int musicCount = 0;
		if (cur != null) {

			if (cur.getCount() == 0) {
				return null;
			}
			String[] arraymusicid = null;
			String[] arraymusicname = null;
			String[] arrayartistname = null;
			String[] arrayartistId = null;
			String[] arraymusictrackno = null;
			String[] arraymusicIsCloud = null;
			String[] arraymusicMediaUrl = null;
			Music music = null;
			Disk disk = null;
			while (cur.moveToNext()) {// 每条数据是一张碟
				List<Music> musicLi = new ArrayList<Music>();
				albumdetail.setAlbumId(cur.getLong(0));
				albumdetail.setAlbumname(cur.getString(1));
				imgUrl = cur.getString(9);

				disk = new Disk();
				disk.setId(cur.getLong(2));
				disk.setName(cur.getString(3));
				arraymusicid = cur.getString(4).split("&&%");
				arraymusicname = cur.getString(5) != null ? cur.getString(5).split("&&%") : null;
				arrayartistname = cur.getString(8) != null ? cur.getString(8).split("&&%") : null;
				arrayartistId = cur.getString(7) != null ? cur.getString(7).split("&&%") : null;
				arraymusictrackno = cur.getString(10) != null ? cur.getString(10).split("&&%") : null;
				arraymusicIsCloud = cur.getString(11) != null ? cur.getString(11).split("&&%") : null;
				arraymusicMediaUrl = cur.getString(12) != null ? cur.getString(12).split("&&%") : null;
				if (arraymusicid != null) {
					for (int i = 0; i < arraymusicid.length; i++) {
						music = new Music();
						music.setId(Long.parseLong(arraymusicid[i]));
						music.setName(arraymusicname[i]);
						music.setIscloud(Integer.parseInt(arraymusicIsCloud[i]));
						if (arrayartistId != null) {
							if (i < arrayartistId.length) {
								music.setArtistId(arrayartistId[i] == null ? "0" : arrayartistId[i]);
							}
						} else {
							music.setArtistId("0");
						}
						if (arraymusicMediaUrl != null) {
							music.setMediaurl(arraymusicMediaUrl[i] == null ? "0" : arraymusicMediaUrl[i]);
						}
						if (arrayartistname != null) {
							if (i < arrayartistname.length) {
								music.setArtistName(arrayartistname[i]);
							}
						} else {
							music.setArtistName("");
						}
						if (arraymusictrackno != null) {
							music.setTrack_no(arraymusictrackno[i] == null ? "0" : arraymusictrackno[i]);
						}
						music.setImgUrl(imgUrl);
						musicLi.add(music);

					}

				}
				disk.setMusicList(musicLi);
				diskLi.add(disk);
				musicCount += cur.getInt(6);

			}
			albumdetail.setDisklist(diskLi);
			albumdetail.setMusicCount(musicCount);
		}

		if (cur != null) {
			cur.close();
		}
		return albumdetail;

	}

	/**
	 * 查询全部数据，云端和本地
	 */
	public List<Album> getAllAlbumList(long pageIndex, int pageSize) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		Cursor cur = mdb.rawQuery("select dba.id albumId,dba.name albumName,dba.img_url albumImgurl,group_concat(dbr.id) artistId,group_concat(dbr.name) artistname ,dba.isclearcache " + "from db_album dba inner join product_artist pa on dba.id=pa.product_id left join db_artist dbr on dbr.id=pa.artist_id  group by albumId order by dba.buytime desc limit " + pageIndex + " , " + pageSize, null);
		// String temp="";
		List<Album> li = new ArrayList<Album>();
		if (cur != null) {
			if (cur.getCount() == 0) {
				// temp="";
				return null;
			}
			Album album = null;
			String[] artistId;
			String[] artistName;
			List<Artist> artistLi;
			Artist artist = null;

			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾

					album = new Album();
					album.setId(cur.getLong(0));
					album.setName(cur.getString(1));
					album.setImgUrl(cur.getString(2));
					artistId = cur.getString(3) != null ? cur.getString(3).split(",") : null;
					artistName = cur.getString(4) != null ? cur.getString(4).split(",") : null;
					album.setIsCloud(cur.getInt(5));
					artistLi = new ArrayList<Artist>();
					if (artistId != null && artistName != null && artistId.length > 0 && artistName.length > 0) {
						for (int i = 0; i < artistId.length; i++) {
							artist = new Artist();
							artist.setId(Long.parseLong(artistId[i]));
							artist.setName(artistName[i]);
							artistLi.add(artist);
						}
						album.setArtistli(artistLi);
					}
					li.add(album);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return li;
	}

	/**
	 * 获得本地专辑列表
	 * 
	 */
	public List<Album> albumlist(long pageIndex, int pageSize) {

		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		Cursor cur = mdb.rawQuery("select dba.id albumId,dba.name albumName,dba.img_url albumImgurl,group_concat(dbr.id) artistId,group_concat(dbr.name) artistname,dba.isclearcache " + "from db_album dba inner join product_artist pa on dba.id=pa.product_id left join db_artist dbr on dbr.id=pa.artist_id where dba.isclearcache = 5 group by albumId order by dba.buytime desc limit " + pageIndex + " , " + pageSize, null);
		// String temp="";
		List<Album> li = new ArrayList<Album>();
		if (cur != null) {
			if (cur.getCount() == 0) {
				// temp="";
				return null;
			}
			Album album = null;
			String[] artistId;
			String[] artistName;
			List<Artist> artistLi;
			Artist artist = null;

			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾

					album = new Album();
					album.setId(cur.getLong(0));
					album.setName(cur.getString(1));
					album.setImgUrl(cur.getString(2));
					artistId = cur.getString(3) != null ? cur.getString(3).split(",") : null;
					artistName = cur.getString(4) != null ? cur.getString(4).split(",") : null;
					album.setIsCloud(cur.getInt(5));
					artistLi = new ArrayList<Artist>();
					if (artistId != null && artistName != null && artistId.length > 0 && artistName.length > 0) {
						for (int i = 0; i < artistId.length; i++) {
							artist = new Artist();
							artist.setId(Long.parseLong(artistId[i]));
							artist.setName(artistName[i]);
							artistLi.add(artist);
						}
						album.setArtistli(artistLi);
					}
					li.add(album);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return li;
	}

	/**
	 * @see 更新专辑云状态;如果是设置为在云端，其下所有单曲将一并被置为在云端
	 * @param state 0=在云端 5=在本地
	 */
	public void updateCloudState(long id, int state) {
		Log.e("BUG975", "11>>"+TAG+"updateCloudState("+id+","+state+")");
		
		try {
			// addv.put("isclearcache", 5);//5表示同步云端 0表示清除本地缓存
			DBHelper.getSqLitedatabase().beginTransaction();
			String sqlall = "update db_album set isclearcache =" + state + " where id =" + id;
			String sqlupdate = "update db_music set isclearcache = " + state + " where db_music.id in (select d.id from db_music d inner join db_disk dbd on d.disk_id =dbd.id inner join db_album dba on dba.id=dbd.album_id  where dba.id =" + id + " )";
			DBHelper.getSqLitedatabase().execSQL(sqlall);
			DBHelper.getSqLitedatabase().execSQL(sqlupdate);
			DBHelper.getSqLitedatabase().setTransactionSuccessful();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBHelper.getSqLitedatabase().endTransaction();
		}
	}

	/**
	 * @description get state of album by id
	 * @return int -1未购买，0在云端，5在本地
	 */
	public int getAlbumStateById(long id, int musiccount) {
		SQLiteDatabase mdb = DBHelper.getSqLitedatabase();
		int state = -1;
		Cursor curs = mdb.rawQuery("select count(dbm.id) from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  where db_album.id =" + id, null);
		if (curs != null) {
			if (curs.getCount() > 0) {
				while (curs.moveToNext()) {
					if (musiccount > curs.getInt(0)) {
						return state;
					}
				}
			} else {
				return state;
			}
			curs.close();

		}

		Cursor cur = mdb.rawQuery("select isclearcache from db_album where id=" + id, null);
		if (cur != null) {
			if (cur.getCount() == 0) {
				return state;
			}

			try {
				while (cur.moveToNext()) {// 知道返回false说明表到了数据末尾
					state = cur.getInt(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			} finally {
				if (cur != null)
					cur.close();

			}
		}
		return state;

	}

	/**
	 * @description get musiclist of album by id
	 * @return List<Music>
	 */
	public List<Music> getAllMusicListById(long id) {
		List<Music> musicli = null;
		Music music = null;
		Cursor cur = DBHelper.getSqLitedatabase().rawQuery("select dbm.id,dbm.name,dbm.play_time,dbm.file_size,dbm.first_char,dbm.track_no,dbm.media_url,group_concat(dba.name) artitname,dba.id,db_album.img_url,dbm.isclearcache  from db_album inner join db_disk on db_disk.album_id =db_album.id inner join   db_music dbm on dbm.disk_id=db_disk.id  inner join product_artist pa on pa.product_id =dbm.id inner  join db_artist dba on dba.id= pa.artist_id where  db_album.id=" + id + "   group by dbm.id order by dbm.buytime", null);
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
	 * @author: ouyang
	 * @description: 修改数据库中单个专辑的云状态
	 */
	public void updateAlbumLocationState(long albumId, int locationState) {
		String updateSql = "update db_album set isclearcache=" + locationState + " where id=" + albumId;
		DBHelper.getSqLitedatabase().execSQL(updateSql);
	}

}
