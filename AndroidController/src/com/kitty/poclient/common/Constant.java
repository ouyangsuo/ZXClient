package com.kitty.poclient.common;

import com.kitty.poclient.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constant {

	// "连接中断",CustomToast.makeText,缓存失败,getMediaInfo，未知错误,设备

	// 控制端可支持的盒子最低版本
	public static final int LEAST_SUPPORT_BOX_VERSIONCODE = 2;

	// 盒子端局域网IP
	public static String DEVICE_IP_ADDRESS = "192.168.1.0";

	// 屏幕参数
	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 480;

	// 定义网络连接和数据读取超时时间
	public static final int MY_CONNECTION_TIMEOUT_MILLIS = 10000;
	public static final int CONNECTION_TIMEOUT_MILLIS = 10000;
	public static final int SOCKET_TIMEOUT_MILLIS = 10000;
	public static final int UPNP_TIMEOUT_MILLIS = 40;

	// APK下载更新保存地址
	// public static final String APK_DOWNLOAD_ADDRESS =
	// "http://m.dfim.com.cn/app/zhenxianRemote.apk";
	public static final String APK_DOWNLOAD_PATH = "/mnt/sdcard/cellremote/zhenxianRemote.apk";
	public static final String P_KEY = "8920f3346d2da84d49e73b9612655c36";
	
	// 盒子设备号、KEY
	public static final String DEVICE_NUMBER = "500A6B15-A20F-5C45-A053-64002E98791E";
	public static final String DEVICE_KEY = "34a061dc06db485685ef6ca7634fe9bc";
	public static final int DEVICE_TERMINAL_TYPE = 0;

	// 播放状态
	private static final String PLAYING = "PLAYING";
	private static final String PAUSED = "PAUSED";
	private static final String STOPPED = "STOPPED";
	private static final String PREPARED = "PREPARED";

	// 获取URI模式(与当前播放列表类型一一对应)
	public static final int URI_MUSIC = 1;
	public static final int URI_ALBUM = 2;
	public static final int URI_THEME = 3;
	public static final int URI_FAVORITE = 4;
	public static final int URI_ALL = 5;
	public static final int URI_USB = 6;
	public static final int URI_CUE = 7;

	// 缓存状态请求URI
	public static final String regCacheUriAlbum = "cache://album?id=";
	public static final String regCacheUriTheme = "cache://theme?id=";
	public static final String regCacheUriFavorite = "cache://playlist?id=";
	public static final String regCacheUriMusic = "cache://music?id=";

	public static final String cacheUriAllAlbum = "cache://album";
	public static final String cacheUriAllTheme = "cache://theme";
	public static final String cacheUriAllFavorite = "cache://playlist";
	public static final String cacheUriAllMusic = "cache://allmusic";

	// 网络请求参数
	public static String apikey = "244987";// WatchDog.currentUserId
											// =,Constant.apikey
	// public static String apikeyTemp = "245010";//fuck
	public static final String terminaltype = "10";
	public static final String protocolver = "zx/1.1";
	public static final String ordertype_audio = "5";
	public static final String ordertype_album = "1";
	public static final String ordertype_pack = "ordertype_pack";

	// 栏目专辑列表查询类型,1=精品聚焦，2-排行榜，3=演出者，4=音乐风格
	public static final int COLUMN_ALBUMS_4_BOTIQUES = 1;
	public static final int COLUMN_ALBUMS_4_TOPS = 2;
	public static final int COLUMN_ALBUMS_4_ARTISTS = 3;
	public static final int COLUMN_ALBUMS_4_GENRES = 4;

	// 搜索类型： 0-全部，1-专辑，5-单曲，10-演出者
	public static final int SEARCH_TYPE_ALL = 0;
	public static final int SEARCH_TYPE_ALBUMS = 1;
	public static final int SEARCH_TYPE_MUSICS = 5;
	public static final int SEARCH_TYPE_ARTISTS = 10;

	// 待模糊处理的图片原始拿取尺寸（原始尺寸为120，越小越模糊）
	public static final int READY_TO_BLUR_BITMAP_HEIGHT = 10;

	// 默认的专辑图片、主题图片
	public static final Bitmap albumCover = BitmapFactory.decodeResource(UpnpApp.context.getResources(), R.drawable.pic);
	public static final Bitmap packCover = BitmapFactory.decodeResource(UpnpApp.context.getResources(), R.drawable.theme_cover_bg);

	// 专辑单曲主题的云状态
	public static final int LOCATION_STATE_UNBOUGHT = -1;
	public static final int LOCATION_STATE_REMOTE = 0;
	public static final int LOCATION_STATE_INTRANSIT = 3;
	public static final int LOCATION_STATE_LOCAL = 5;

	// 专辑单曲主题的云操作
	public static final int LOCATION_OPERATION_DELETE = 1;// 删除
	public static final int LOCATION_OPERATION_FETCH = 5;// 同步

	// 搜索记录的显示条数
	public static final int SEARCH_HISTORY_ITEMS_SHOWN = 20;
	// 商店-栏目详情每页加载的专辑数量
	public static final int COLUMN_DETAIL_ITEMS_COUNT_PER_PAGE = 30;

	// 坑爹的已购音乐中弹出菜单的纵轴偏移量
	public static final int POPUP_Y_OFFSET_IN_PURCHASED = -12;

	// 操作系统类型：1=android 5=ios 10=windows
	public static final int APP_OS_TYPE = 1;
	// 设备类型：1=手机 5=平板 10=真现盒
	public static final int APP_DEVICE_TYPE = 1;
	// 临时参数：androidph=安卓手机 androidpad=安卓平板 iosph=苹果手机 iospad=苹果平板 box=真现盒
	public static final String APP_TEMP_TYPE = "androidpad";

	// 公共广播ACTION
	public static final String ACTION_PLAYLIST_SEEK_POSITION = "playlistFragmentSeekToCurrentPlayingPosition";
	public static final String ACTION_DEAL_STREAMCLIENT_TIMEOUT_OR_FAILURE = "dealWithSocketTimeoutExceptionReceiver";

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}

	public static String getBaseUrl() {
		return WatchDog.currentHost + "zhenxianwang/ws/";// fuck
		// return WatchDog.currentHostTemp + "zhenxianwang/ws/";//测试版
	}

	public static String getApikey() {
		return WatchDog.currentUserId;
	}

}
