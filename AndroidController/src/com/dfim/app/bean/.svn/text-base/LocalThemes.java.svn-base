package com.dfim.app.bean;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.domain.Pack;
import com.union.cellremote.service.impl.PackServiceImpl;

public class LocalThemes extends ArrayList<LocalTheme> {

	private static final String TAG = LocalThemes.class.getSimpleName() + ":";
	private static final long serialVersionUID = -6046819392530389977L;

	public static LocalThemes translatePacks(List<Pack> packs) {
		LocalThemes themes = new LocalThemes();
		for (int themeIndex = 0; themeIndex < packs.size(); themeIndex++) {
			LocalTheme theme = new LocalTheme();
			Pack pack = packs.get(themeIndex);
			pack = new PackServiceImpl().getPackDetailById(pack); // @see: MymusicThemesFragment.initListeners()
			if(pack == null){
				packs.remove(themeIndex);
				continue;
			}
			List<Music> musicList = pack.getLi();

			theme.setName(pack.getName()); // name
			
			int totalMusicNum = musicList.size();
			if(totalMusicNum == 0){
				packs.remove(themeIndex);
				continue;
			}
			theme.setTotalMusicNum(totalMusicNum); // total music number

			int waitMusicNum = 0;
			int loadedMusicNum = 0;
			int loadingMusicNum = 0;
			int cacheFailMusicNum = 0;
			
			Log.e("BUG580", TAG+ pack.getName()+":WatchDog.cacheStateMap.size()="+WatchDog.cacheStateMap.size());
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
			
			Log.e("BUG580", TAG+ pack.getName()+":loadedMusicNum="+loadedMusicNum);
			Log.e("BUG580", TAG+ pack.getName()+":waitMusicNum="+waitMusicNum);
			Log.e("BUG580", TAG+ pack.getName()+":loadingMusicNum="+loadingMusicNum);
			Log.e("BUG580", TAG+ pack.getName()+":cacheFailMusicNum="+cacheFailMusicNum);
			
			theme.setWaitMusicNum(waitMusicNum);			// wait    music number
			theme.setdownloadingMusicNum(loadingMusicNum);	// loading music number
			theme.setDownloadedMusicNum(loadedMusicNum);	// loaded  music number
			theme.setFailCachedMusicNum(cacheFailMusicNum); // failcache music number
			
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
			
			theme.setCacheStatus(cacheStatus); // theme cache status
			
			themes.add(theme);
		}

		return themes;
	}

	public void refreshThemeCacheStatus(int position) {
		
		Pack pack = VirtualData.packs.get(position);
		pack = new PackServiceImpl().getPackDetailById(pack); // @see: MymusicThemesFragment.initListeners()
		List<Music> musicList = pack.getLi();

//		theme.setName(pack.getName()); // name
		
		int totalMusicNum = musicList.size();
//		theme.setTotalMusicNum(totalMusicNum); // total music number

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
		
		LocalTheme relevantTheme = get(position);
		relevantTheme.setWaitMusicNum(waitMusicNum);			// wait    music number
		relevantTheme.setdownloadingMusicNum(loadingMusicNum);	// loading music number
		relevantTheme.setDownloadedMusicNum(loadedMusicNum);	// loaded  music number
		relevantTheme.setFailCachedMusicNum(cacheFailMusicNum); // failcache music number
		relevantTheme.setCacheStatus(cacheStatus); // theme cache status
	}
	
	public int getParentPackIndex(long musicId){
		int packIndex = -1;
		for (int index = 0; index < VirtualData.packs.size(); index++) {
			Pack pack = VirtualData.packs.get(index);
			pack = new PackServiceImpl().getPackDetailById(pack);
			List<Music> musicList = pack.getLi();
			for(int musicIndex = 0; musicIndex < musicList.size(); musicIndex++){
				if(musicList.get(musicIndex).getId() == musicId){
					packIndex = index;
					break;
				}
			}
		}
		return packIndex;
	}
}
