package com.dfim.app.domain;

import java.util.List;


/**
 * @ClassName:Disk
 * @Description: 碟的模型
 * @author xuzuyi
 * @date 2013-7-12 上午10:19:34
 */
public class Disk {
	
	private Long id ;

	private String name;
	
	private String disk_no;
	private long album_id;
    private List<Music> musicList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Music> getMusicList() {
		return musicList;
	}

	public void setMusicList(List<Music> musicList) {
		this.musicList = musicList;
	}

	public String getDisk_no() {
		return disk_no;
	}

	public void setDisk_no(String disk_no) {
		this.disk_no = disk_no;
	}

	public long getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(long album_id) {
		this.album_id = album_id;
	}

    
}
