package com.union.cellremote.service;

import java.util.List;

import com.union.cellremote.domain.Music;

public interface IMusicService {

	/**
	 *获取所有bendi单曲
	 */
	  
	public List<Music> getAllMusic();
	
	public List<Music> getAllMusic(long pageIndex,int pageSize);
	/**
	 * 获取云端数据
	 */
	public List<Music> getMusicListForCloud( long pageIndex,int pageSize);
	/**
	 * 获取本地和云端所有数据
	 */
	public List<Music> getAllMusicList(long pageIndex,int pageSize);
}
