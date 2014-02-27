package com.dfim.app.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.util.Log;

import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.fragment.usb.ExternalDeviceFragment;
import com.union.cellremote.R;

public class URIParams {

	private static String TAG = "URIParams";

	private int type;
	private Map<String, String> params;

	public static final int URI_TYPE_ALLMUSIC = 1;
	public static final int URI_TYPE_MUSIC = 2;
	public static final int URI_TYPE_ALBUM = 3;
	public static final int URI_TYPE_THEME = 4;
	public static final int URI_TYPE_PLAYLIST = 5;
	public static final int URI_TYPE_USB = 6;
	public static final int URI_TYPE_CUE = 7;

	public URIParams(String url) {
		String[] urls = url.split("[?]");
		if (urls[0].endsWith("allmusic")) {
			type = 1;
		} else if (urls[0].endsWith("music")) {
			type = 2;
		} else if (urls[0].endsWith("album")) {
			type = 3;
		} else if (urls[0].endsWith("theme")) {
			type = 4;
		} else if (urls[0].endsWith("playlist")) {
			type = 5;
		} else if (urls[0].endsWith("usb")) {
			type = URI_TYPE_USB; // 6
		} else if (urls[0].endsWith("cue")) {
			type = URI_TYPE_CUE; // 7
		}

		if (type == URI_TYPE_CUE) {
			// TODO source
			// TODO
			// uri=xxbox://cue?source=/mnt/usb_storage/USB_DISK0/udisk0/music%20and%20video/cue/CDImage.cue&title=零时十分&start=239210&end=440430
		}
		if (urls.length > 1) {
			String[] parms = urls[1].split("&");
			String[] keyValue;
			params = new HashMap<String, String>();
			for (String p : parms) {
				keyValue = p.split("=");
				params.put(keyValue[0], keyValue[1]);
			}
		}
	}

	public int getType() {
		return type;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public static void setCurrentTrackUri(String currentTrackURI) {
		WatchDog.currentUri = currentTrackURI;

		int uriType = new URIParams(currentTrackURI).getType();
		String str = "";
		// String state="-1";

		// 拿到歌曲的URI
		switch (uriType) {
		case 1:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			// state=new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 2:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("id");
			// state=new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 3:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			// state=new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 4:
			WatchDog.mediaOutOfService = false;

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			// state=new URIParams(currentTrackURI).getParams().get("state");
			break;
		case 5:
			WatchDog.mediaOutOfService = true;
			WatchDog.mediaOutOfServiceReson = UpnpApp.context.getResources().getString(R.string.mosReasonPlaylist);
			UpnpApp.context.sendBroadcast(new Intent("mediaOutOfServiceReceiver"));

			str = new URIParams(currentTrackURI).getParams().get("musicid");
			// state=new URIParams(currentTrackURI).getParams().get("state");
			break;
		case URI_TYPE_USB: // 6
			WatchDog.currentListType = URI_TYPE_USB;
			ExternalDeviceFragment.currentListType = URI_TYPE_USB;
			Log.i(TAG, "URI_TYPE_USB: setCurrentTrackUri > do nothing");
			break;
		case URI_TYPE_CUE: // 7
			WatchDog.currentListType = URI_TYPE_CUE;
			ExternalDeviceFragment.currentListType = URI_TYPE_CUE;
			Log.i(TAG, "URI_TYPE_CUE: setCurrentTrackUri > do nothing");
			break;
		}

		if (uriType == URI_TYPE_USB || uriType == URI_TYPE_CUE) { // 播放“外联设备”,
																	// 刷新当前播放单曲信息
			Log.i(TAG, "USB/CUE:WatchDog.currentListType" + WatchDog.currentListType);
			Log.i(TAG, "USB/CUE:UsbFragment.currentListType" + ExternalDeviceFragment.currentListType);
			Log.i(TAG, "USB/CUE:WatchDog.currentPlayingName" + WatchDog.currentPlayingName);
			Log.i(TAG, "USB/CUE:WatchDog.currentPlayingId" + WatchDog.currentPlayingId);
			// 获取当前播放歌在USB当前文件夹的音乐列表
			// 更新当前播放列表
			// Log.i(TAG, "usb/cue file, str = " + str);
			// UsbFileUtil.updateMusicListFromPlayUri(str);
		} else {
			if (str.equals("")) {
				return;
			}

			long musicId = Long.parseLong(str);
			WatchDog.currentPlayingId = musicId;
			Log.i(TAG, "WatchDog.currentPlayingId=" + WatchDog.currentPlayingId);

			List<Music> list;
			if (WatchDog.currentList != null) {
				list = WatchDog.currentList;
			} else {
				list = VirtualData.musics;
			}

			for (int i = 0; i < list.size(); i++) {
				Music music = list.get(i);
				if (music.getId() == musicId) {
					WatchDog.currentPlayingMusic = music;
					WatchDog.currentPlayingName = music.getName();
					System.out.println("URIParams:WatchDog.currentPlayingName=" + WatchDog.currentPlayingName);
					WatchDog.currentArtistName = music.getArtistName();
					WatchDog.currentPlayingIndex = i;
					return;
				}
			}
		}
	}

	public static void ensureTheBabyMine(String uri) {
		System.out.println("ensureTheBabyBeMine: uri=" + uri);
		if (!uri.startsWith("xxbox")) {
			WatchDog.babyNotMine = true;
			// 提示设备连接已被其它控制端占用，返回登录界面
			UpnpApp.context.sendBroadcast(new Intent("babyNotMineReceiver"));
		} else {
			if (WatchDog.babyNotMine == true) {
				WatchDog.babyNotMine = false;
			}
		}

		if (uri.startsWith("xxbox://listen?")) {
			WatchDog.mediaOutOfService = true;
			WatchDog.mediaOutOfServiceReson = UpnpApp.context.getResources().getString(R.string.mosReasonListen);
			UpnpApp.context.sendBroadcast(new Intent("mediaOutOfServiceReceiver"));
		}
	}

}
