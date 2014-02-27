package com.union.cellremote.domain;

import java.lang.ref.SoftReference;
import java.util.List;

import android.graphics.Bitmap;

import com.dfim.app.common.Constant;

public class Pack {
	private long id;
	private String name;
	private String imgurl;
	private String buytime;// 购买时间
	private int totaltracks;
	private String artistName;
	private long libraryid;// 已购买库id
	private List<Music> li;
	private int mcount;// 单曲的数量
	private String[] musicids;// 主题所有的单曲id
	private int isCloud;// 是否在云端
	private SoftReference<Bitmap> srBitmap;// 封面

	public int getIsCloud() {
		return isCloud;
	}

	public void setIsCloud(int isCloud) {
		this.isCloud = isCloud;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public String getBuytime() {
		return buytime;
	}

	public void setBuytime(String buytime) {
		this.buytime = buytime;
	}

	public int getTotaltracks() {
		return totaltracks;
	}

	public void setTotaltracks(int totaltracks) {
		this.totaltracks = totaltracks;
	}

	public long getLibraryid() {
		return libraryid;
	}

	public void setLibraryid(long libraryid) {
		this.libraryid = libraryid;
	}

	public List<Music> getLi() {
		return li;
	}

	public void setLi(List<Music> li) {
		this.li = li;
	}

	public String[] getMusicids() {
		return musicids;
	}

	public void setMusicids(String[] musicids) {
		this.musicids = musicids;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public int getMcount() {
		return mcount;
	}

	public void setMcount(int mcount) {
		this.mcount = mcount;
	}

	public Bitmap getBitmap() {
		if (srBitmap != null) {
			Bitmap bmp = srBitmap.get();
			return bmp == null || bmp.isRecycled() ? Constant.packCover : bmp;
		} else {
			return Constant.packCover;
		}
	}

	public void setBitmap(SoftReference<Bitmap> srBitmap) {
		this.srBitmap = srBitmap;
	}

	public void recyleBitmap() {
		if (getBitmap() != null && !getBitmap().equals(Constant.packCover)) {
			getBitmap().recycle();
		}
	}

	/*
	 * public void setCoverBitmap(SoftReference<Bitmap> srBitmap) {
	 * this.srCoverBitmap = srBitmap; }
	 * 
	 * public Bitmap getCoverBitmap() { if (srCoverBitmap == null) { return
	 * Constant.albumCover; } return (srCoverBitmap.get() == null)
	 * ?Constant.albumCover : srCoverBitmap.get(); }
	 * 
	 * public void recyleBitmap() { if (getCoverBitmap()!=null &&
	 * !getCoverBitmap().equals(Constant.albumCover)) {
	 * getCoverBitmap().recycle(); } }
	 */

}
