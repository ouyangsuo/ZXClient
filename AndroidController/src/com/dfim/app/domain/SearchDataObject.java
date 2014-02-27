package com.dfim.app.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchDataObject {

	private List<Album> albums = new ArrayList<Album>();
	private List<Music> musics = new ArrayList<Music>();
	private List<Artist> artists = new ArrayList<Artist>();

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	public List<Music> getMusics() {
		return musics;
	}

	public void setMusics(List<Music> musics) {
		this.musics = musics;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

}
