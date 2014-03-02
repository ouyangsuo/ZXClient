package com.kitty.poclient.bean;

import java.util.List;

import com.kitty.poclient.domain.Pack;

/**
 * 【我的音乐-主题】模型
 *
 */
public class LocalTheme {
	
	private String name = "";
	
	private int totalMusicNum = 0;
	
	private int waitMusicNum = 0;
	private int downloadingMusicNum = 0;
	private int downloadedMusicNum = 0;
	private int failCachedMusicNum = 0;
	
	public int getFailCachedMusicNum() {
		return failCachedMusicNum;
	}

	public void setFailCachedMusicNum(int cacheFailMusicNum) {
		this.failCachedMusicNum = cacheFailMusicNum;
	}
	private int cacheStatus;
//	public static final int CACHE_STATUS_WAIT = 0;
//	public static final int CACHE_STATUS_DOWNLOADING = 1;
//	public static final int CACHE_STATUS_FAILURE_NOSPACE = 2;
//	public static final int CACHE_STATUS_DOWNLOADED = 3;
	
	public static LocalTheme translatePack(Pack pack) {
		LocalTheme theme = new LocalTheme();
		
		return null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCacheStatus() {
		return cacheStatus;
	}
	public void setCacheStatus(int cacheStatus) {
		this.cacheStatus = cacheStatus;
	}
	

	public int getTotalMusicNum() {
		return totalMusicNum;
	}
	public void setTotalMusicNum(int totalMusicNum) {
		this.totalMusicNum = totalMusicNum;
	}
	public int getDownloadedMusicNum() {
		return downloadedMusicNum;
	}
	public void setDownloadedMusicNum(int loadedMusicNum) {
		this.downloadedMusicNum = loadedMusicNum;
	}

	public int getWaitMusicNum() {
		return waitMusicNum;
	}
	public void setWaitMusicNum(int waitMusicNum) {
		this.waitMusicNum = waitMusicNum;
	}
	public int getDowloadingMusicNum() {
		return downloadingMusicNum;
	}
	public void setdownloadingMusicNum(int loadingMusicNum) {
		this.downloadingMusicNum = loadingMusicNum;
	}
}
