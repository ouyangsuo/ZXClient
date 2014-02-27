package com.union.cellremote.domain;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;

import com.dfim.app.common.Constant;

/**
 * @ClassName:Music
 * @Description: 单曲模型
 * @author xuzuyi
 * @date 2013-7-12 上午10:19:34
 */
public class Music {
	private Long Id;
	private String name;
	private String artistId;
	private String artistName;
	private long diskId;
	private String mediaurl;
	private String firstChar;
	private String play_time;
	private String cacheState;
	private String imgUrl;
	private String albumId;
	private String albumName;
	private String diskName;
	private String disk_no;
	private Long libid;
	private int iscloud = -1;//5本地，0云端
	private SoftReference<Bitmap> srCoverBitmap;

	public int getIscloud() {
		return iscloud;
	}

	public void setIscloud(int iscloud) {
		this.iscloud = iscloud;
	}

	private String price="";// 单曲价格
	private String purchaseState = "未购买";// 交易状态："未提交"	"1"，"未通过"	"5"，"已提交"	"10"，"已删除"	"15"，"已上架"	"20"，"已下架"	"25"，"等待自动上架"	"30"，"已定价"	"35"，"已发布"	"40"，"不单卖"	"45"


	private boolean flag = false;
	private String file_size;
	private String track_no;
	private String buytime;
	private String uri;
	private int clearcachestate = 0;// 0未清除缓存，5表示清楚缓存

	// 定义缓存状态
	public final static String CACHE_WAIT = "1";// 正在等待缓存
	public final static String CACHE_DOWNLOADING = "2";// 正在下载
	public final static String CACHE_FAILURE_NOSPACE = "3";// 正在下载
	public final static String CACHE_DOWNLOADED = "4";// 下载完成

	public int getClearcachestate() {
		return clearcachestate;
	}

	public void setClearcachestate(int clearcachestate) {
		this.clearcachestate = clearcachestate;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getDisk_no() {
		return disk_no;
	}

	public void setDisk_no(String disk_no) {
		this.disk_no = disk_no;
	}

	public Long getLibid() {
		return libid;
	}

	public void setLibid(Long libid) {
		this.libid = libid;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getBuytime() {
		return buytime;
	}

	public void setBuytime(String buytime) {
		this.buytime = buytime;
	}

	public String getFirstChar() {
		return firstChar;
	}

	public void setFirstChar(String firstChar) {
		this.firstChar = firstChar;
	}

	public String getPlay_time() {
		return play_time;
	}

	public void setPlay_time(String play_time) {
		this.play_time = play_time;
	}

	public String getFile_size() {
		return file_size;
	}

	public void setFile_size(String file_size) {
		this.file_size = file_size;
	}

	public String getTrack_no() {
		return track_no;
	}

	public void setTrack_no(String track_no) {
		this.track_no = track_no;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtistId() {
		return artistId;
	}

	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getMediaurl() {
		return mediaurl;
	}

	public void setMediaurl(String mediaurl) {
		this.mediaurl = mediaurl;
	}

	public long getDiskId() {
		return diskId;
	}

	public void setDiskId(long diskId) {
		this.diskId = diskId;
	}

	public String getUri(int uriMode, long containerId) {
		// String uri="file://mnt/sdcard/xxbox/music/"+getId()+".flac";
		// System.out.println("uri="+uri);
		// return uri;
		String uri = "";
		switch (uriMode) {
		case Constant.URI_MUSIC:
			uri = "xxbox://music?id=" + getId();
			break;
		case Constant.URI_ALBUM:
			uri = "xxbox://album?id=" + containerId + "&musicid=" + getId();
			break;
		case Constant.URI_THEME:
			uri = "xxbox://theme?id=" + containerId + "&musicid=" + getId();
			break;
		case Constant.URI_FAVORITE:
			uri = "xxbox://music?id=" + getId();
			break;
		case Constant.URI_ALL:
			uri = "xxbox://music?id=" + getId();
			break;
		case Constant.URI_USB:
			uri = "xxbox://usb?source=" + getUri();
			break;
		case Constant.URI_CUE:
			uri = "xxbox://cue?source=" + getUri();
			break;
		}
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUri() {
		return this.uri;
	}

	public String getCacheState() {
		return cacheState;
	}

	public void setCacheState(String cacheState) {
		this.cacheState = cacheState;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPurchaseState() {
		return purchaseState;
	}

	public void setPurchaseState(String purchaseState) {
		this.purchaseState = purchaseState;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
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
		if (getBitmap() != null && !getBitmap().equals(Constant.albumCover)) {
			getBitmap().recycle();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof Music)) {
			return false;
		}

		Music m = (Music) o;
		if (m.getId().equals(getId())) {
			return true;
		}

		return false;
	}

}
