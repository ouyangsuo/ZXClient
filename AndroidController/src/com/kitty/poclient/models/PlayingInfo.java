package com.kitty.poclient.models;

public class PlayingInfo {

	String name = "";
	String artist = "";
	String imgUrl = "";
	String duration = "";
	
	

	public PlayingInfo(String name, String artist, String imgUrl, String duration) {
		super();
		this.name = name;
		this.artist = artist;
		this.imgUrl = imgUrl;
		this.duration = duration;
	}

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

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	@Override
	public String toString() {
		return "currentPlaying:"+name+","+artist+","+duration;
	}

}
