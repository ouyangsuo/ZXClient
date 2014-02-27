package com.union.cellremote.service.impl;

import java.util.List;

import android.util.Log;

import com.dfim.app.bean.LocalThemes;
import com.dfim.app.dao.PackDao;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Pack;
import com.union.cellremote.service.IPackService;

public class PackServiceImpl implements IPackService {

	private static final String TAG = PackServiceImpl.class.getSimpleName() + ":";
	
	@Override
	public List<Pack> getAllPack() {
		List<Pack> packList = new PackDao().getAllPack();
//		if(packList != null){
//			Log.e("BUG580", TAG+"getAllPack()"+"LocalThemes.translatePacks() calling...");
//			VirtualData.localThemes = LocalThemes.translatePacks(packList);
//		}
		return packList;
	}

	@Override
	public Pack getPackDetailById(Pack pack) {
        
		return new PackDao().getPackDetailById(pack);
	}

	@Override
	public List<Pack> getPackListForCloud(long pageIndex,int pageSize) {
		
		return new PackDao().getPackListForCloud(pageIndex, pageSize);
	}

	@Override
	public List<Pack> getAllPackList(long pageIndex,int pageSize) {
		
		return new PackDao().getAllPackList(pageIndex, pageSize);
	}

	@Override
	public List<Pack> getAllPack(long pageIndex, int pageSize) {

		return new PackDao().getAllPack(pageIndex, pageSize);
	}

}
