package com.kitty.poclient.service;

import java.util.List;

import com.kitty.poclient.domain.Album;

public interface IAlbumService {

	public List<Album> getalbumlist();// 在本地

	public List<Album> getalbumlist(long pageIndex, int pageSize);// 在本地，分页

	public List<Album> getAlbumListForCloud(long pageIndex, int pageSize); // 在云端，分页

	public List<Album> getAllAlbumList(long pageIndex, int pageSize);// 全部数据，分页
	public void updateAlbumCouldState(long id,int state);

}
