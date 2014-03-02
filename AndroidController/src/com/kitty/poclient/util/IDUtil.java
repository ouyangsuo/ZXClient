package com.kitty.poclient.util;

import java.util.List;

import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.domain.Music;

public class IDUtil {
	
	public static String getMusicNameFromId(long musicId, List<Music> list) {
		String TAG="IDUtil: ";
		String musicName="UNKNOWN_MUSIC";
		
		if (list==null) {
			list=VirtualData.musics;
		}		

		for (Music music : list) {			
			if (music.getId() == musicId) {
				musicName=music.getName();			
				return musicName;
			}
		}
		
		return musicName;
	}

}
