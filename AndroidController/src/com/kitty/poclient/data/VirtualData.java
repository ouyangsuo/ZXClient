package com.kitty.poclient.data;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.util.Log;

import com.kitty.poclient.bean.LocalAlbums;
import com.kitty.poclient.bean.LocalSingles;
import com.kitty.poclient.bean.LocalThemes;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UIHelper;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.dao.AlbumDao;
import com.kitty.poclient.dao.MusicDao;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.domain.Pack;
import com.kitty.poclient.service.impl.AlbumServiceImpl;
import com.kitty.poclient.service.impl.MusicServiceImpl;
import com.kitty.poclient.service.impl.PackServiceImpl;

/**
 * 控制端本地数据，已经加载到程序。
 * 
 */
public class VirtualData {

	public static final String TAG = "VirtualData";

	public static boolean dataInitiated = false;
	public static List<Album> albums = new ArrayList<Album>();// VirtualData.albums.add
	public static List<Music> musics = new ArrayList<Music>();
	public static List<Pack> packs = new ArrayList<Pack>();//VirtualData.packs.add

	public static Album refetchingMusicContainerAlbum;
	
	public static LocalAlbums localAlbums;
	public static LocalThemes localThemes;//VirtualData.localThemes=,VirtualData.localThemes =
	public static LocalSingles localSingles;
//	public static Local
	
	public static void clearcacheinitData() {
		albums = new AlbumServiceImpl().getalbumlist();
//		if(albums!=null){
//			localAlbums = LocalAlbums.translateAlbumList(albums);
//		}
		
		musics = new MusicServiceImpl().getAllMusic();
//		localMusics = LocalMusics.translateMusics(musics);
		
		packs = new PackServiceImpl().getAllPack();
//		themes = LocalThemes.translatePacks(VirtualData.packs);
		
		dataInitiated = true;
		
		// 刷新“我的音乐” 专辑、单曲、主题
		UIHelper.refreshAllLocalView();
		
		UpnpApp.context.sendBroadcast(new Intent("updateListPurchased")); // 刷新“应用商店-已购音乐”
	}

	private static void printAlbums(List<Album> albums) {
		Log.e(TAG, "albums.size()=" + albums.size());
		for (Album album : albums) {
			Log.e(TAG, album.getName());
		}
	}

	public static void initData() {// VirtualData.initData()
		
		albums = new AlbumServiceImpl().getalbumlist();
//		localAlbums = LocalAlbums.translateAlbumList(albums);
		
		musics = new MusicServiceImpl().getAllMusic();
//		localMusics = LocalMusics.translateMusics(musics);
		
		packs = new PackServiceImpl().getAllPack();
//		themes = LocalThemes.translatePacks(VirtualData.packs);
		
		dataInitiated = true;
		
		UIHelper.refreshAllLocalView();
		
		UpnpApp.context.sendBroadcast(new Intent("updateListPurchased"));
	}

	public static void setMusicContainerAlbumLocal(Music music) {
		// 查询该单曲的包装专辑ID
		long albumId = -1;
		try {
			albumId = new MusicDao().getMusicContainerAlbumId(music.getId());
		} catch (SQLDataException e) {
			e.printStackTrace();
			Log.e(TAG, "查询单曲的包装专辑时发生异常");
			return;
		}
		Log.e(TAG, "refetchingMusicContainerAlbumId=" + albumId);

		// 将数据库中对应专辑的状态修改为在本地
		new AlbumDao().updateAlbumLocationState(albumId, Constant.LOCATION_STATE_LOCAL);
		Log.e(TAG, "refetchingMusicContainerAlbum locationState set done");
	}

}
