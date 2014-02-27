package com.dfim.app.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;

import com.dfim.app.bean.LocalAlbums;
import com.dfim.app.bean.LocalThemes;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.fragment.PlayerFragment;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.widget.StandardCustomDialog;
import com.union.cellremote.R;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * @author fangyishuai@dfim.com.cn
 * @version 1.0
 * @since 0.0.4
 * @created 2013-12-04
 */
public class UIHelper {
	public static void showExitDialog(Activity parentActivity) {
		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(parentActivity);
		builder.setTitle("退出程序");
		builder.setMessage("确认退出程序吗？");
		
		StandardCustomDialog dialog;
		builder.setPositiveButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				ExitApplication.getInstance().exit();
			}
		});

		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	
	/**
	 * 刷新“播放器切换”按钮：播放状态显示
	 * @param btnPlayer “播放切换”按钮  
	 * @param resources The Android resource system keeps track of all non-code assets associated with an application. 
	 * @param adapter   Adapter to refresh
	 */
	public static void initMusicFragmentBtnPlayer(ImageButton btnPlayer, Resources resources, BaseExpandableListAdapter adapter) {
		String currentCacheState = Music.CACHE_WAIT;
		if (WatchDog.currentPlayingMusic != null) {
			currentCacheState = WatchDog.cacheStateMap.get(WatchDog.currentPlayingMusic.getId());// 查到当前曲目缓存状态
//			System.out.println(TAG + "currentCacheState=" + currentCacheState);
		}

		if (WatchDog.mediaOutOfService == true && WatchDog.currentState.equals(PlayerFragment.PLAYING)) {
//			AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.playing);
			AnimationDrawable animationDrawable = (AnimationDrawable) resources.getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
			if (adapter!=null) {
				adapter.notifyDataSetChanged();
			}			

		} else if (!WatchDog.currentState.equals(PlayerFragment.PLAYING) || !Music.CACHE_DOWNLOADED.equals(currentCacheState)) {// 空指针
			btnPlayer.setImageResource(R.drawable.btn_player);
		} else {
			AnimationDrawable animationDrawable = (AnimationDrawable) resources.getDrawable(R.anim.playing);
			btnPlayer.setImageDrawable(animationDrawable);
			animationDrawable.start();
			if (adapter != null) {
				adapter.notifyDataSetChanged();// 小喇叭开始广播啦
			}
		}
		
		//外联设备文件播放情况
        if(WatchDog.currentListType == Constant.URI_USB || WatchDog.currentListType == Constant.URI_CUE){
			if(WatchDog.currentState.equals(PlayerFragment.PLAYING)){
				AnimationDrawable animationDrawable = (AnimationDrawable) resources.getDrawable(R.anim.playing);
				btnPlayer.setImageDrawable(animationDrawable);
				animationDrawable.start();
			}else{
				btnPlayer.setImageResource(R.drawable.btn_player);
			}
		}
	}
	
	/**
	 * 刷新我的音乐列表-专辑、单曲和主题。
	 */
	public static void refreshAllLocalView(){
		Intent intent = new Intent(BroadcastManager.FILTER_UPDATE_LOCALLIST);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALALBUM, BroadcastManager.EXTRA_BOOLEAN_DEFAULT);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALTHEME, BroadcastManager.EXTRA_BOOLEAN_DEFAULT);
		UpnpApp.context.sendBroadcast(intent);
	}
	public static void refreshLocalAlbumsView(){
		Intent intent = new Intent(BroadcastManager.FILTER_UPDATE_LOCALLIST);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALALBUM, BroadcastManager.EXTRA_BOOLEAN_DEFAULT);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALTHEME, BroadcastManager.EXTRA_BOOLEAN_IGNORE);
		UpnpApp.context.sendBroadcast(intent);
	}
	public static void refreshLocalThemesView(){
		Intent intent = new Intent(BroadcastManager.FILTER_UPDATE_LOCALLIST);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALALBUM, BroadcastManager.EXTRA_BOOLEAN_IGNORE);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALTHEME, BroadcastManager.EXTRA_BOOLEAN_DEFAULT);
		UpnpApp.context.sendBroadcast(intent);
	}
	public static void refreshLocalSinglesView(){
		Intent intent = new Intent(BroadcastManager.FILTER_UPDATE_LOCALLIST);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALTHEME, BroadcastManager.EXTRA_BOOLEAN_IGNORE);
		intent.putExtra(BroadcastManager.EXTRA_INCLUDE_LOCALALBUM, BroadcastManager.EXTRA_BOOLEAN_IGNORE);
		UpnpApp.context.sendBroadcast(intent);
	}
	
	public static void refreshPlayerButton(){
		Intent intent = new Intent("initBtnPlayerReceiver");
		UpnpApp.context.sendBroadcast(intent);
	}
	
	public static void refreshAllMusicsCasheState(){
		//此时AlbumListFragment和MymusicThemesFragment两个类尚未onCreate,是收不到广播的
		Intent intent = new Intent("onCacheStateMapEstablishedReceiver");
		UpnpApp.context.sendBroadcast(intent);
		
		VirtualData.localAlbums = LocalAlbums.translateAlbumList(VirtualData.albums);
		VirtualData.localThemes = LocalThemes.translatePacks(VirtualData.packs);
	}
}
