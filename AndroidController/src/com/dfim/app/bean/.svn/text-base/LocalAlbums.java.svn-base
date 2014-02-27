package com.dfim.app.bean;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.AlbumDetail;
import com.dfim.app.domain.Disk;
import com.dfim.app.domain.Music;
import com.union.cellremote.service.impl.AlbumDetailServiceImpl;

public class LocalAlbums extends ArrayList<LocalAlbum> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2970790984264603754L;

	public static LocalAlbums translateAlbumList(List<Album> albumList) {
		
		LocalAlbums localAlbums = new LocalAlbums();
		
		for (int albumIndex = 0; albumIndex < albumList.size(); albumIndex++) {
			
			LocalAlbum localAlbum = new LocalAlbum();
			
			Album album = albumList.get(albumIndex);
			localAlbum.setName(album.getName()); // name
			
			AlbumDetail albumDetail = new AlbumDetailServiceImpl().getAblumDetail(album.getId());
			if(albumDetail == null){
				albumList.remove(albumIndex);
				continue;
			}
			List<Disk> diskList = albumDetail.getDisklist();
			List<Music> musicList = new ArrayList<Music>();
			for (Disk disk : diskList) {
//				Log.i("AlbumListCache", "disk:size=" + diskList.size());
				musicList.addAll(disk.getMusicList());
			}
			Log.i("AlbumListCache", "musicList:size=" + musicList.size());

			
			int totalMusicNum = musicList.size();
			
			if(totalMusicNum == 0){
				albumList.remove(albumIndex);
				continue;
			}
			
			localAlbum.setTotalMusicNum(totalMusicNum); // total music number

			int waitMusicNum = 0;
			int loadedMusicNum = 0;
			int loadingMusicNum = 0;
			int cacheFailMusicNum = 0;
			for (int musicIndex = 0; musicIndex < totalMusicNum; musicIndex++) {
				
				long id = musicList.get(musicIndex).getId();
				if (WatchDog.cacheStateMap.containsKey(id) == false) { 
					waitMusicNum++;
					
				} else{
					if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_WAIT)) {
						waitMusicNum++;
						
					} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
						loadedMusicNum++;
						
					} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADING)) {
						loadingMusicNum++;
						
					} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_FAILURE_NOSPACE)){
						cacheFailMusicNum++;
						
					} else {
						//不明状态
						waitMusicNum++;
					}
				} 
			}
			
			localAlbum.setWaitMusicNum(waitMusicNum);			// wait    music number
			localAlbum.setdownloadingMusicNum(loadingMusicNum);	// loading music number
			localAlbum.setDownloadedMusicNum(loadedMusicNum);	// loaded  music number
			localAlbum.setFailCachedMusicNum(cacheFailMusicNum); // failcache music number
			
			int cacheStatus;
			if(loadedMusicNum == totalMusicNum){
				cacheStatus = LocalCache.CACHE_STATUS_DOWNLOADED;
				
			} else {
				if(loadingMusicNum > 0){
					cacheStatus = LocalCache.CACHE_STATUS_DOWNLOADING;

				} else if(cacheFailMusicNum > 0){
					cacheStatus = LocalCache.CACHE_STATUS_FAILURE_NOSPACE;
					
				} else {
					cacheStatus = LocalCache.CACHE_STATUS_WAIT;

				}
			} 
			
			localAlbum.setCacheStatus(cacheStatus); // cache status
//			Log.i("AlbumListCache", "localAlbum:cacheStatus"+cacheStatus);
//			Log.i("AlbumListCache", "localAlbum:totalMusicNum"+totalMusicNum);
//			Log.i("AlbumListCache", "localAlbum:loadedMusicNum"+loadedMusicNum);
//			Log.i("AlbumListCache", "localAlbum:loadingMusicNum"+loadingMusicNum);
			
			localAlbums.add(localAlbum);
		}

		return localAlbums;
	}
	
	public void refreshAlbumCacheStatus(int albumIndex){
		Album album = VirtualData.albums.get(albumIndex);
		AlbumDetail albumDetail = new AlbumDetailServiceImpl().getAblumDetail(album.getId());
		List<Disk> diskList = albumDetail.getDisklist();
		List<Music> musicList = new ArrayList<Music>();
		for (Disk disk : diskList) {
			musicList.addAll(disk.getMusicList());
		}
		
		int totalMusicNum = musicList.size();
		int waitMusicNum = 0;
		int loadedMusicNum = 0;
		int loadingMusicNum = 0;
		int cacheFailMusicNum = 0;
		for (int musicIndex = 0; musicIndex < totalMusicNum; musicIndex++) {
			
			long id = musicList.get(musicIndex).getId();
			if (WatchDog.cacheStateMap.containsKey(id) == false) { 
				waitMusicNum++;
				
			} else{
				if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_WAIT)) {
					waitMusicNum++;
					
				} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADED)) {
					loadedMusicNum++;
					
				} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_DOWNLOADING)) {
					loadingMusicNum++;
					
				} else if (WatchDog.cacheStateMap.get(id).equals(Music.CACHE_FAILURE_NOSPACE)){
					cacheFailMusicNum++;
					
				} else {
					//不明状态
					waitMusicNum++;
				}
			} 
		}
		
		
		int cacheStatus;
		if(loadedMusicNum == totalMusicNum){
			cacheStatus = LocalCache.CACHE_STATUS_DOWNLOADED;
			
		} else {
			if(loadingMusicNum > 0){
				cacheStatus = LocalCache.CACHE_STATUS_DOWNLOADING;

			} else if(cacheFailMusicNum > 0){
				cacheStatus = LocalCache.CACHE_STATUS_FAILURE_NOSPACE;
				
			} else {
				cacheStatus = LocalCache.CACHE_STATUS_WAIT;

			}
		} 
		
		LocalAlbum relevantAlbum = get(albumIndex);
		relevantAlbum.setWaitMusicNum(waitMusicNum);			// wait    music number
		relevantAlbum.setdownloadingMusicNum(loadingMusicNum);	// loading music number
		relevantAlbum.setDownloadedMusicNum(loadedMusicNum);	// loaded  music number
		relevantAlbum.setFailCachedMusicNum(cacheFailMusicNum); // failcache music number
		relevantAlbum.setCacheStatus(cacheStatus);
		
	}
	
	
	public List<Music> getAllMusicOfAlbum(Album album){
		AlbumDetail albumDetail = new AlbumDetailServiceImpl().getAblumDetail(album.getId());
		List<Disk> diskList = albumDetail.getDisklist();
		List<Music> musicList = new ArrayList<Music>();
		for (Disk disk : diskList) {
			musicList.addAll(disk.getMusicList());
		}
		return musicList;
	}
	
	public int getParentAlbumIndex(long musicId){
		int albumIndex = -1;
		for (int index = 0; index < VirtualData.albums.size(); index++) {
			Album album = VirtualData.albums.get(index);
			
			List<Music> musicList = getAllMusicOfAlbum(album);
			for (int musicIndex = 0; musicIndex < musicList.size(); musicIndex++) {
				if(musicList.get(musicIndex).getId() == musicId){
					albumIndex = index;
					break;
				}
			}
		}
		
		return albumIndex;
	}
}
