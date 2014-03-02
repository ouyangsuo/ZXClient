package com.kitty.poclient.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;

public class SystemUtil {
	public static void getLocalMacAddress() {
		WifiManager wifi = (WifiManager) UpnpApp.getContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		WatchDog.macAddress = info.getMacAddress();
		if (WatchDog.macAddress != null && WatchDog.macAddress != "") {
			WatchDog.macAddress = WatchDog.macAddress.replaceAll(":", "");
		}
		Log.i("address", WatchDog.macAddress);
	}
}
