package com.dfim.app.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.PackDao;
import com.dfim.app.data.VirtualData;
import com.union.cellremote.R;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Disk;
import com.union.cellremote.domain.Music;
import com.union.cellremote.domain.Pack;
import com.union.cellremote.service.impl.AlbumDetailServiceImpl;

public class MediaInfoUtil {
	public static String TAG = "MediaInfoUtil";
	public static boolean getCurrentPlayingInfo(String currengURI,String mediaDuration){
		Log.i(TAG, "currengURI=" + currengURI);
		URIParams uriParams=new URIParams(currengURI);
		int type=uriParams.getType();
		long musicId=0;
		
		switch (type) {
		case 1://allmusic
			WatchDog.mediaOutOfService=false;
			
			//初始化播放列表
			WatchDog.currentList=(ArrayList<Music>) VirtualData.musics;
			WatchDog.currentListType = Constant.URI_MUSIC;
			WatchDog.currentListId = 0L;
			
			musicId=Long.parseLong(uriParams.getParams().get("musicid"));
			break;
			
		case 2://music
			WatchDog.mediaOutOfService=false;
			
			//初始化播放列表
			WatchDog.currentList=(ArrayList<Music>) VirtualData.musics;
			WatchDog.currentListType = Constant.URI_MUSIC;
			WatchDog.currentListId = 0L;
			
			musicId=Long.parseLong(uriParams.getParams().get("id"));
			break;
			
		case 3://album
			WatchDog.mediaOutOfService=false;
			
			long albumId=Long.parseLong(uriParams.getParams().get("id"));
			for (Album album:VirtualData.albums) {
				if (album.getId()==albumId) {
					List<Music> list =new ArrayList<Music>();
					AlbumDetail detail=new AlbumDetailServiceImpl().getAblumDetail(albumId);
					for(Disk d:detail.getDisklist()){
						list.addAll(d.getMusicList());
					}
					
					//初始化播放列表
					WatchDog.currentList=(ArrayList<Music>) list;
					WatchDog.currentListType = Constant.URI_ALBUM;
					WatchDog.currentListId=albumId;
				}
			}
			musicId=Long.parseLong(uriParams.getParams().get("musicid"));		
			break;
			
		case 4://theme
			WatchDog.mediaOutOfService=false;
			
			long themeId=Long.parseLong(uriParams.getParams().get("id"));
			for (Pack theme:VirtualData.packs) {
				if (theme.getId()==themeId) {
					
					//初始化播放列表
					WatchDog.currentList=(ArrayList<Music>) theme.getLi();//没有拿到主题音乐列表，主动操作以外根本拿不到
					if (WatchDog.currentList==null) {
						WatchDog.currentList=(ArrayList<Music>) new PackDao().getPackDetailById(theme).getLi();
					}
					WatchDog.currentListType = Constant.URI_THEME;
					WatchDog.currentListId=themeId;
				}
			}
			musicId=Long.parseLong(uriParams.getParams().get("musicid"));		
			break;
			
		case 5://playlist
			WatchDog.mediaOutOfService=true;
			WatchDog.mediaOutOfServiceReson=UpnpApp.context.getResources().getString(R.string.mosReasonPlaylist);
//			UpnpApp.context.sendBroadcast(new Intent("mediaOutOfServiceReceiver"));			
			musicId=Long.parseLong(uriParams.getParams().get("musicid"));		
			break;
			
		case URIParams.URI_TYPE_USB://usb
			//TODO
			break;
		case URIParams.URI_TYPE_CUE://usb
			//TODO cue child uri:"xxbox://cue?source=/mnt/usb_storage/USB_DISK0/udisk0/music%20and%20video/cue/CDImage.cue&title=零时十分&start=239210&end=440430"
			break;
		}
		
		if (WatchDog.currentList==null) {
			WatchDog.currentList=(ArrayList<Music>) VirtualData.musics;
		}
		
		for (Music music:WatchDog.currentList) {
			if (music.getId()==musicId) {
				WatchDog.currentPlayingMusic=music;
				WatchDog.currentPlayingId=WatchDog.currentPlayingMusic.getId();
				WatchDog.currentPlayingName=music.getName();
				WatchDog.currentArtistName=music.getArtistName();
			}
		}
		
		return true;
		
	}

}
