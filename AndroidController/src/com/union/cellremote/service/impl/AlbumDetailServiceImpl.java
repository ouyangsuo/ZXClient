package com.union.cellremote.service.impl;

import com.dfim.app.dao.AlbumDao;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.service.IAlbumDetailService;

public class AlbumDetailServiceImpl implements IAlbumDetailService {

	@Override
	public AlbumDetail getAblumDetail(Long albumId) {
		 
		return new AlbumDao().getAlbumDetailData(albumId);
	}
    
}
