package com.kitty.poclient.service.impl;

import java.util.List;

import com.kitty.poclient.bean.LocalAlbums;
import com.kitty.poclient.dao.AlbumDao;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.service.IAlbumService;

public class AlbumServiceImpl implements IAlbumService {

	@Override
	public List<Album> getalbumlist() { 
		List<Album> albumList = new AlbumDao().albumlist();
		if(albumList!=null){
			VirtualData.localAlbums = LocalAlbums.translateAlbumList(albumList);
		}
		return albumList;
	}

	@Override
	public List<Album> getAlbumListForCloud(long pageIndex, int pageSize) {
	
		return  new AlbumDao().getAlbumListForCloud(pageIndex,pageSize);
	}

	@Override
	public List<Album> getAllAlbumList(long pageIndex, int pageSize) {
		
		return new AlbumDao().getAllAlbumList(pageIndex,pageSize);
	}

	@Override
	public List<Album> getalbumlist(long pageIndex, int pageSize) {
		
		return new AlbumDao().albumlist(pageIndex,pageSize);
	}

	@Override
	public void updateAlbumCouldState(long id,int state) {
        
		 new AlbumDao().updateCloudState(id,state);
		
	}

}
