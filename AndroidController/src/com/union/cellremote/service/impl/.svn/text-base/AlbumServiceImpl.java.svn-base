package com.union.cellremote.service.impl;

import java.util.List;

import com.dfim.app.bean.LocalAlbums;
import com.dfim.app.dao.AlbumDao;
import com.dfim.app.data.VirtualData;
import com.union.cellremote.domain.Album;
import com.union.cellremote.service.IAlbumService;

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
