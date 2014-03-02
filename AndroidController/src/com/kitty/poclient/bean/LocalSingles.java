package com.kitty.poclient.bean;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kitty.poclient.domain.Music;

public class LocalSingles extends ArrayList<LocalSingle> {

	private static final long serialVersionUID = 781178238546017359L;
	
	private boolean isSelectAll = false;
	
	public static LocalSingles translateMusics(List<Music> musics) {
		LocalSingles localSingles = new LocalSingles();

		for (int mIndex = 0; mIndex < musics.size(); mIndex++) {
			LocalSingle localSingle = new LocalSingle();
			Music music = musics.get(mIndex);
			Long id = music.getId();
			
			localSingle.setId(id);
			localSingle.setSelected(false);
			localSingles.add(localSingle);
		}

		return localSingles;
	}
	
	public void clearAllSelect(){
		isSelectAll = false;
		updateAllStatus(isSelectAll);
	}
	public boolean isSelectAll(){
		return isSelectAll;
	}
	public void cancelSelectAll(){
		isSelectAll = false;
	}
	
	public void switchAllSelectStatus(){
		updateAllStatus(!isSelectAll);
		Log.i("SinglesCache", "switchAllSelectStatus-isSelectAll:"+isSelectAll);
	}
	private void updateAllStatus(boolean newStatus){
		isSelectAll = newStatus;
		for (int mIndex = 0; mIndex < size(); mIndex++) {
			LocalSingle localMusic = get(mIndex);
			localMusic.setSelected(newStatus);
		}
		Log.i("SinglesCache", "updateAllStatus-isSelectAll:"+isSelectAll);
	}
	
	public int getSelectedItemNum(){
		int selectedNum = 0;
		for (int mIndex = 0; mIndex < size(); mIndex++) {
			LocalSingle localMusic = get(mIndex);
			if(localMusic.isSelected()){
				selectedNum++;
			}
		}
		
		return selectedNum;
	}
	public LocalSingles getSelectedSingles(){
		LocalSingles localSingles = new LocalSingles();
		for (int mIndex = 0; mIndex < size(); mIndex++) {
			LocalSingle localSingle = get(mIndex);
			if(localSingle.isSelected()){
				localSingles.add(localSingle);
			}
		}
		return localSingles;
	}
	
	public LocalSingle removeSingle(Long id){
		int postion = getPositionById(id);
		return remove(postion);
	}
	public int getPositionById(Long id){
		int position = -1;
		int index = 0;
		while(index < size()){
			if(get(index).getId().equals(id)){
				position = index;
				break;
			}
			index++;
		}
		return position;
	}
}
