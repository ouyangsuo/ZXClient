package com.kitty.poclient.util;

import com.kitty.poclient.bean.LocalAlbum;
import com.kitty.poclient.dao.AlbumDao;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Album;

public class Collector {

	public void collectAlbum(Album album) {
		//写入VirtualData
		VirtualData.albums.add(album);
		//写入数据库
		new AlbumDao().insertAlbum(album);
	}

}
