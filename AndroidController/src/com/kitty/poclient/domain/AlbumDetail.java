package com.kitty.poclient.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:AlbumDetail
 * @Description: 专辑详情，
 * @author xuzuyi
 * @date 2013-7-12 上午10:19:34
 */
public class AlbumDetail {

	/*
	 * 专辑状态 
	 * "未提交" "1" 
	 * "未通过" "5" 
	 * "已提交" "10" 
	 * "已删除" "15" 
	 * "已上架" "20" 
	 * "已下架" "25"
	 * "等待自动上架" "30" 
	 * "已定价" "35" 
	 * "已发布" "40" 
	 * "不单卖" "45"
	 */
	public static final String STATE_BOUGHT = "";

	private Long albumId;
	private String albumname;
	private List<Disk> disklist;
	private Long artistId;
	private String artistName;
	private String smallImg;
	private String state;
	private int musicCount;// 单曲总数
	private double price;
	private boolean flag=false;//是否选中
	
	private String introduction = "";
	private String publishTime = "";
	private String language = "";
	private String companyName = "";
	

	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}

	public List<Disk> getDisklist() {
		return disklist;
	}

	public void setDisklist(List<Disk> disklist) {
		this.disklist = disklist;
	}

	public Long getArtistId() {
		return artistId;
	}

	public void setArtistId(Long artistId) {
		this.artistId = artistId;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getAlbumname() {
		return albumname;
	}

	public void setAlbumname(String albumname) {
		this.albumname = albumname;
	}

	public int getMusicCount() {
		return musicCount;
	}

	public void setMusicCount(int musicCount) {
		this.musicCount = musicCount;
	}

	public String getSmallImg() {
		return smallImg;
	}

	public void setSmallImg(String smallImgUrl) {
		this.smallImg = smallImgUrl;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "albumId=" + this.albumId + " " + "albumName=" + this.albumname + " " + "artistName=" + this.artistName + " " + "musicCount=" + this.musicCount;
	}
	
	public Album getAlbumForLocalUse(String imgUrl){
		List<Artist> artists=new ArrayList<Artist>();
		Artist artist=new Artist();
//		artist.setId(getArtistId());
		artist.setName(getArtistName());
		artists.add(artist);
		
		Album album = new Album();
		album.setId(getAlbumId());
		album.setName(getAlbumname());
		album.setImgUrl(imgUrl);
		album.setArtistli(artists);		
		album.setDiskLi(getDisklist());
		
		return album;
	}

	public String getIntroduction() {
		return introduction;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

}
