package com.union.cellremote.service.impl;

import java.util.List;

import com.dfim.app.dao.MusicDao;
import com.dfim.app.domain.Music;
import com.union.cellremote.service.IMusicService;

public class MusicServiceImpl implements IMusicService {

	@Override
	public List<Music> getAllMusic() {
        
		return  new  MusicDao().getAllMusic();
	}

	@Override
	public List<Music> getMusicListForCloud(long pageIndex,int pageSize) {
		
		return new MusicDao().getMusicListForCloud(pageIndex,pageSize);
	}

	@Override
	public List<Music> getAllMusicList(long pageIndex,int pageSize) {
		return new MusicDao().getAllMusicList(pageIndex,pageSize);
	}

	@Override
	public List<Music> getAllMusic(long pageIndex, int pageSize) {
		// TODO Auto-generated method stub
		return new MusicDao().getAllMusic(pageIndex,pageSize);
	}

}
