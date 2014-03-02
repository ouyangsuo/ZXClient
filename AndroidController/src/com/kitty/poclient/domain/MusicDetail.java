package com.kitty.poclient.domain;

public class MusicDetail {

//	 { “id”: MUSICID, 
//	      “name”:MUSICNAME,
//	      “albumid”:ALBUMID,
//	      “albumname”:ALBUMNAME,
//	      “albumimg”:ALBUMBIGIMG,
//	“publishtime”: PUBLISHTIME,
//	“language”:LANGUAGE,
//	“artistname”:ARTISTNAME,
//	“companyname”:COMPANYNAME,
//	“size”:SIZE,
//	 “playtimes”:PALYTIMES,
//	“technology”:TECHNOLOGY,
//	 “listenurl”:LISTENURL,
//	 “lyrics”:LYRICS,
//	“price”:PRICE,
//	“score”:SCORE,
//	“state”:STATE
//	} 
	
	private String listenUrl="";
	private String name="";
	private String artist="";
	private String duration="";
	private String imgUrl="";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getListenUrl() {
		return listenUrl;
	}

	public void setListenUrl(String listenUrl) {
		this.listenUrl = listenUrl;
	}

}
