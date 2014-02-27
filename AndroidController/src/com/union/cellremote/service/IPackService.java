package com.union.cellremote.service;

import java.util.List;

import com.union.cellremote.domain.Pack;

public interface IPackService {
    
	public List<Pack> getAllPack();//查询所有本地主题
	public List<Pack> getAllPack(long pageIndex,int pageSize);//查询所有本地主题，分页
	public Pack getPackDetailById(Pack pack);//根据id查询pack
	public List<Pack> getPackListForCloud(long pageIndex,int pageSize);//获取云端数据
	public List<Pack> getAllPackList(long pageIndex,int pageSize);//获取本地和云端所有专辑
	
}
