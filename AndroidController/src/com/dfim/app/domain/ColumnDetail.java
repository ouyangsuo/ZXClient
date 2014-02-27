package com.dfim.app.domain;

import java.util.List;

public class ColumnDetail {

	private int total;
	private int num;
	private List<Album> albums;

	public ColumnDetail() {
		super();
	}

	public ColumnDetail(int total, int num, List<Album> albums) {
		super();
		this.total = total;
		this.num = num;
		this.albums = albums;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

}
