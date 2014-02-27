package com.dfim.app.domain;

import java.lang.ref.SoftReference;
import java.util.List;

import android.graphics.Bitmap;

import com.dfim.app.common.Constant;

/**
 * @ClassName:Album
 * @Description: 专辑模型
 * @author xuzuyi
 * @date 2013-7-12 上午10:19:34
 */
public class Album {

	private Long id;// 专辑的id
	private String name; // 专辑的名字
	private List<Artist> artistli;// 专辑的演出者列表
	private String artistName;
	private List<Disk> diskLi;// 专辑的碟的列表
	private String buytime;// 专辑的购买时间
	private int orderType;// 专辑的购买类型
	private String imgUrl; // 专辑的图片
	private SoftReference<Bitmap> srCoverBitmap;
	private int isCloud = -1;// 是否在云端，5本地，0云端

	public int getIsCloud() {
		return isCloud;
	}

	public void setIsCloud(int isCloud) {
		this.isCloud = isCloud;
	}

	/**
	 * 简介
	 */
	private String introduction;
	/**
	 * 发行时间
	 */
	private String publishTime;
	/**
	 * 语言
	 */
	private String language;

	/**
	 * 文件大小
	 */
	private String size;
	/**
	 * 播放时长
	 */
	private String playTimes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getPlayTimes() {
		return playTimes;
	}

	public void setPlayTimes(String playTimes) {
		this.playTimes = playTimes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Artist> getArtistli() {
		return artistli;
	}

	public void setArtistli(List<Artist> artistli) {
		this.artistli = artistli;
	}

	public List<Disk> getDiskLi() {
		return diskLi;
	}

	public void setDiskLi(List<Disk> diskLi) {
		this.diskLi = diskLi;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getIntroduction() {
		return introduction;
	}

	public String getBuytime() {
		return buytime;
	}

	public void setBuytime(String buytime) {
		this.buytime = buytime;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public void setBitmap(SoftReference<Bitmap> srBitmap) {
		this.srCoverBitmap = srBitmap;
	}

	public Bitmap getBitmap() {
		if (srCoverBitmap == null) {
			return Constant.albumCover;
		}
		return (srCoverBitmap.get() == null) ? Constant.albumCover : srCoverBitmap.get();
	}

	public void recyleBitmap() {
		if (getBitmap()!=null && !getBitmap().equals(Constant.albumCover)) {
			getBitmap().recycle();
		}
	}
	
	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

}
