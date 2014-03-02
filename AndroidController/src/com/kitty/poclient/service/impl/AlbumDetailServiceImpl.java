package com.kitty.poclient.service.impl;

import com.kitty.poclient.dao.AlbumDao;
import com.kitty.poclient.domain.AlbumDetail;
import com.kitty.poclient.service.IAlbumDetailService;

public class AlbumDetailServiceImpl implements IAlbumDetailService {

	@Override
	public AlbumDetail getAblumDetail(Long albumId) {
		 
		return new AlbumDao().getAlbumDetailData(albumId);
	}
    
}
