package com.dfim.app.util;

import java.util.List;

import com.dfim.app.data.VirtualData;
import com.union.cellremote.domain.Music;

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
