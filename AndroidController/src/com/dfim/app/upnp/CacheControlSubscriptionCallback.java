package com.dfim.app.upnp;

import java.util.Date;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;

import android.util.Log;

import com.dfim.app.bean.CurrentCacheChangeInfo;
import com.dfim.app.common.CrashHandler;
import com.dfim.app.common.MymusicManager;
import com.dfim.app.common.UIHelper;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Music;
import com.dfim.app.util.IDUtil;
import com.dfim.app.util.JsonUtil;
import com.union.cellremote.R;

public class CacheControlSubscriptionCallback extends SubscriptionCallback {

	String TAG = "CacheControlSubscriptionCallback";

	private LastChange lastChange;

	public CacheControlSubscriptionCallback(Service service) {
		super(service);
	}

	@Override
	protected void established(GENASubscription arg0) {
		Log.e(TAG, "CacheControlSubscriptionCallback established");
		WatchDog.cacheSubFailCount = 0;
		CrashHandler.getInstance().saveCrashInfo2FileII("CacheControlSubscriptionCallback established: time=" + new Date(System.currentTimeMillis()));
	}

	@Override
	protected void ended(GENASubscription arg0, CancelReason arg1, UpnpResponse arg2) {
		Log.e(TAG, "CacheControlSubscriptionCallback ended");
		CrashHandler.getInstance().saveCrashInfo2FileII("CacheControlSubscriptionCallback ended: CancelReason=" + arg1 + ";UpnpResponse=" + arg2);
//		TabMusicActivity.receiveCacheSub();
//		MyMusicActivity.receiveCacheSub();
		MymusicManager.receiveCacheSub();
	}

	@Override
	protected void eventsMissed(GENASubscription arg0, int arg1) {
		Log.e(TAG, "CacheControlSubscriptionCallback eventsMissed");
		CrashHandler.getInstance().saveCrashInfo2FileII("CacheControlSubscriptionCallback eventsMissed: time=" + new Date(System.currentTimeMillis()));
	}

	// Called when establishing a local or remote subscription failed.
	@Override
	protected void failed(GENASubscription arg0, UpnpResponse arg1, Exception ex, String arg3) {

		Log.e(TAG, "CacheControlSubscriptionCallback failed: ex=" + ex);
		CrashHandler.getInstance().saveCrashInfo2FileII("CacheControlSubscriptionCallback failed: ex=" + ex + " time=" + new Date(System.currentTimeMillis()));

		WatchDog.cacheSubFailCount++;
		if (WatchDog.cacheSubFailCount <= 3) {
//			TabMusicActivity.receiveCacheSub();
//			MyMusicActivity.receiveCacheSub();
		} else {
			UpnpApp.reconnect();
		}
	}

	@Override
	protected void eventReceived(GENASubscription sub) {
		Log.e(TAG, "CacheControlSubscriptionCallback eventReceived");

		UnsignedIntegerFourBytes currentSequence = sub.getCurrentSequence();
		Log.e(TAG, "currentSequence=" + currentSequence);

		String xml = sub.getCurrentValues().get("LastChange").toString();
		Log.e(TAG, "currentValuesXml=" + xml);
		
		//缓存更新信息接收 　　　
		/**		
		 * downloadaction：缓存的动作，开始下载：start，下载结束：finish，下载空间不足：NotspaceEnough
		 * cacheuri：缓存uri
		　* statusCode:缓存状态，1=等待中，2=正在缓存，3=暂停,4=完成
		　* errorcode：缓存错误码
		　* 注：新盒子有serialNumber，以标注该订阅信息的有效性，旧盒子没此字段
		 */
		if (xml.contains("CurrentCacheChange")) {
			
			//解释xml
			CurrentCacheChangeInfo cachInfo = new JsonUtil().getCurrentCacheChangeInfo(xml);
			
			String uri = cachInfo.getCacheuri();
			String statusCode = cachInfo.getStatusCode();
			String errorCode = cachInfo.getErrorcode();
			
			long id = Long.parseLong(uri.substring(uri.lastIndexOf("=") + 1));
			int serialNumber = 0;
			
			if(!cachInfo.getSerialNumber().equals("")){
				serialNumber = Integer.valueOf(cachInfo.getSerialNumber());
			} 
			
			//更新local缓存
			updateCacheState(id, uri, statusCode);
			
			//更新播放器状态
			updateBtnPlayer();
			
            //显示错误信息		
			if (!"200".equals(errorCode)) {
				/**99-文件验证失败 100-连接服务器错误 101-网络故障中断缓存 102-用户原因中断缓存 103-存储空间不足 104-内存不足 */
				showErrorMsg(id, errorCode);
			}

			WatchDog.formerCacheSubSerialNumber = serialNumber;
			
		}

	}

	private void dealWithMissingSub() {
		Log.e(TAG, "dealWithMissingSub");
/*		TabMusicActivity.getCacheInfo();
		TabMusicActivity.getMediaInfo();
		TabMusicActivity.getTransportInfo();*/
//		MyMusicActivity.getCacheInfo();
//		MyMusicActivity.getMediaInfo();
//		MyMusicActivity.getTransportInfo();
		MymusicManager.getCacheInfo();
		MymusicManager.getMediaInfo();
		MymusicManager.getTransportInfo();
		
	}

	/* 99-文件验证失败 100-连接服务器错误 101-网络故障中断缓存 102-用户原因中断缓存 103-存储空间不足 104-内存不足 */
	private void showErrorMsg(long id, String errorCode) {
		// 通知界面提示出错信息
		if (!"200".equals(errorCode)) {
			// System.out.println("errorCode=" + errorCode);
			String musicName = IDUtil.getMusicNameFromId(id, null);
			String errorMessage = "";
			String croutonText = "";
			
			if ("99".equals(errorCode)) {
				errorMessage = "校验失败";
				
			} else if ("100".equals(errorCode)) {
				errorMessage = "连接服务器错误";
				
			} else if ("101".equals(errorCode)) {
				errorMessage = "网络故障";
				
			} else if ("102".equals(errorCode)) {
				errorMessage = "WHAT U A STUPID ASSHOLE...";
				
			} else if ("103".equals(errorCode)) {
				errorMessage = "存储空间不足";
				croutonText = UpnpApp.mainHandler.getString(R.string.cache_space_not_enough_alert);

			} else if ("104".equals(errorCode)) {
				errorMessage = "内存不足";
			} else {
				errorMessage = "";
			}
			
			if(!croutonText.equals("")){
				 UpnpApp.mainHandler.showAlert(croutonText);
			}
			
			Log.e(TAG, musicName + "缓存失败：" + errorMessage);
			
		}
	}

	private void updateBtnPlayer() {
		// 通知界面刷新播放图标
		UIHelper.refreshPlayerButton();
	}

	private void updateCacheState(long id, String uri, String statusCode) {
		// 更新缓存状态存储MAP
		WatchDog.cacheStateMap.put(id, statusCode);
		Log.e(TAG, "put: id=" + id + ";statusCode=" + statusCode);
		if (Music.CACHE_DOWNLOADING.equals(statusCode)) {
			WatchDog.clearFormerCaching(id);// 取消其他曲目的 正在缓存 状态
			Log.i("CacheMsg", "WatchDog.clearFormerCaching id:" + id);
		} else if (Music.CACHE_FAILURE_NOSPACE.equals(statusCode)) {
			WatchDog.clearFormerCachingNospace(id);// 取消其他曲目的 空间不足 状态 
			Log.i("CacheMsg", "WatchDog.clearFormerCachingNospace id:" + id);
		} else {
			//TODO 
		}
		Log.i("CacheMsg", "WatchDog.cacheStateMap.containsKey(id):"+WatchDog.cacheStateMap.containsKey(id));
		
		//更新localAlbums缓存状态
		if(VirtualData.albums!=null && VirtualData.albums.size()>0){
			int albumIndex = VirtualData.localAlbums.getParentAlbumIndex(id);
			if(albumIndex!=-1){
				VirtualData.localAlbums.refreshAlbumCacheStatus(albumIndex);
				UIHelper.refreshLocalAlbumsView();
			}
		}
		
		//更新localThemes缓存状态
		if(VirtualData.localThemes!=null && VirtualData.localThemes.size()>0){
			int packIndex = VirtualData.localThemes.getParentPackIndex(id);
			if(packIndex!=-1){
				VirtualData.localThemes.refreshThemeCacheStatus(packIndex);
				UIHelper.refreshLocalThemesView();
			}
		}

		//更新localSingles缓存状态
		// do nothing
		// 通知界面刷新缓存状态
		UIHelper.refreshLocalSinglesView();
	}

	private void onCurrentCacheChange(int instanceId, String currentCacheChange) {
		System.out.println(TAG + "currentCacheChange=" + currentCacheChange);
	}

}
