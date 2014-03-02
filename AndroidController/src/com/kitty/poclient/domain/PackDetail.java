package com.kitty.poclient.domain;

import java.util.List;

import android.graphics.Bitmap;

public class PackDetail {
	/*
	 * “id”: PACKID, 
	 * “name”: PACKNAME, 
	 * “bigimg”: BIGIMG, 
	 * “price”:PRICE,
	 * “introduction”: INTRODUCTION, 
	 * “state”:STATE, 
	 * “score”:SCORE 
	 * “musics:”[{}]
	 */
	private long packId;
	private String packName;
	private Bitmap bitmap;
	private String imgUrl;
	private double price;
	private String introduction;
	private String state;
	private String score;
	private int musicCount;
	private List<Music> musics;

	public long getPackId() {
		return packId;
	}

	public void setPackId(long id) {
		this.packId = id;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String name) {
		this.packName = name;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgurl) {
		this.imgUrl = imgurl;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public int getMusicCount() {
		return musicCount;
	}

	public void setMusicCount(int musicCount) {
		this.musicCount = musicCount;
	}

	public List<Music> getMusics() {
		return musics;
	}

	public void setMusics(List<Music> musics) {
		this.musics = musics;
	}

}
