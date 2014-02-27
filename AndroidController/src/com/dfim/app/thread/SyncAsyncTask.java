package com.dfim.app.thread;//new SyncAsyncTask

import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dfim.app.activity.LoginActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.CrashHandler;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.db.DBHelper;
import com.dfim.app.upnp.BoxControl;

public class SyncAsyncTask extends AsyncTask<Handler, Integer, Integer> {
	private static final String TAG = SyncAsyncTask.class.getSimpleName();
	
	public static final int SYN_TYPE_GetUserInfo = 101;
	public static final int SYN_TYPE_GetAllUnSyn = 102;
	public static final int SYN_TYPE_GetCloudStates = 103;
	public static final int SYN_TYPE_InitLocalData = 104;
	public static final int SYN_TYPE_StartMainActivity = 105;
	
	private static final double WEIGHT_GetUserInfo = 0.1;
	private static final double WEIGHT_GetAllUnSyn = 0.6;
	private static final double WEIGHT_GetCloudStates = 0.1;
	private static final double WEIGHT_InitLocalData = 0.1;
	private static final double WEIGHT_StartMainActivity = 0.1;
	
	private static double currentProgress = 0;
	
	private static Handler progressHandler;
	
	@Override
	protected Integer doInBackground(Handler... params) {
		System.out.println("doInBackground");
		progressHandler = params[0];
		
		countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 1);
		getUserInfo(params[0]);
		
		return null;
	}
	
	public static Handler getProgressHandler(){
		return progressHandler;
	}
	
	/**
	 * 
	 */
	public static void countProgress(int synType, int moduleProress) {
		Log.i(TAG, "synType:" + synType + ",moduleProress:" + moduleProress + "%");
		if (progressHandler != null) {
			double weight = -1;
			switch(synType){
			//1
				case SYN_TYPE_GetUserInfo:
					weight = WEIGHT_GetUserInfo;
					currentProgress = 0 ;
					break;
			//2
				case SYN_TYPE_GetAllUnSyn:
					weight = WEIGHT_GetAllUnSyn;
					if(currentProgress < WEIGHT_GetUserInfo){
						currentProgress = WEIGHT_GetUserInfo;
					} else {
						
					}
					break;
			//3
				case SYN_TYPE_GetCloudStates:
					weight = WEIGHT_GetCloudStates;
					currentProgress = WEIGHT_GetUserInfo + WEIGHT_GetAllUnSyn;
					break;
			//4
				case SYN_TYPE_InitLocalData:
					weight = WEIGHT_InitLocalData;
					currentProgress = WEIGHT_GetUserInfo + WEIGHT_GetAllUnSyn + WEIGHT_GetCloudStates;
					break;
			//5
				case SYN_TYPE_StartMainActivity:
					weight = WEIGHT_StartMainActivity;
					currentProgress = WEIGHT_GetUserInfo + WEIGHT_GetAllUnSyn + WEIGHT_GetCloudStates + WEIGHT_InitLocalData;
					break;
			}
			
			if(weight != -1){
				
				Message msg = progressHandler.obtainMessage();
				msg.what = LoginActivity.UPGRADE_PROGRESS;
				if(weight == WEIGHT_GetAllUnSyn){
					currentProgress = currentProgress + moduleProress * weight;
				} else {
					currentProgress = currentProgress * 100 + moduleProress * weight;
				}
				msg.arg1 = (int) (currentProgress);
				progressHandler.sendMessage(msg);
				
				Log.i(TAG, "currentProgress =" + currentProgress + "%");
				
			} else {
				Log.e(TAG, "Wrong synType in countProgress(). weight:" + weight);
			}
			
			
		} else {
			
			Log.e(TAG, "参数未初始化 - SyncAsyncTask:progressHandler=" + progressHandler);
		
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}
	
	private void getUserInfo(final Handler handler) {
		ActionInvocation ai = null;
		try {
			SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 10);
			ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetBoxUserInfo"));//异常：空指针
		} catch (Exception e) {
			System.out.println("exception:e="+e);
			return;
		}
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			@Override
			public void success(ActionInvocation arg0) {				
				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 20);
				
				String userid = (String) arg0.getOutput("Userid").getValue();
				String password = (String) arg0.getOutput("Password").getValue();
				String Host = (String) arg0.getOutput("Host").getValue();
				
				if (!WatchDog.currentUserId.equals(userid)) {
					WatchDog.buySubState = 0;
				}
				WatchDog.currentUserId = userid;
				WatchDog.currentPassword = password;
				Constant.apikey=WatchDog.currentUserId;

				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 30);
				
				WatchDog.currentHost = Host;
				if (WatchDog.currentUserId == "" || WatchDog.currentUserId == null) {
					WatchDog.currentUserId = "0";
				}

				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 40);
				
				String host = "";
				if (WatchDog.currentHost != null) {
					host = WatchDog.currentHost.replaceAll("[.,/,:]", "");
				}

				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 50);
				
				String dbname = WatchDog.currentUserId + host;
				CrashHandler.exceptionAlreadyGiven=false;
				try {
					
					SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 60);
					
					WatchDog.l=System.currentTimeMillis();
					createDBOrderZxno(handler,22);
					
					SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 80);
					
				    Map<String ,Long> musicmap=getMusicCountAndMaxLibid(DBHelper.getSqLitedatabase());

					SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 90);
					
					WatchDog.synBoxOrservice=1; 
					String musicLibid = musicmap.get("musicLibid")+"";
					long musicAccount = musicmap.get("count");
					if (musicLibid == "" || musicLibid == null) {
						musicLibid = "-1";
						musicAccount = 0;
					} 

					SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetUserInfo, 100);
					
				    new BoxControl().isTrueBoxDate(musicAccount, handler, musicLibid);
				} catch (Exception e) {
					Log.i("ex", e.getMessage());
				}
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1,String arg2) {
				System.out.println("GetBoxUserInfo failure");
			}
		});
	}
	
	private void createDBOrderZxno(final Handler handler,int version) {
	SQLiteDatabase sqlite = DBHelper.getSqLitedatabase();
		if (sqlite != null) {
			if (sqlite.isOpen()) {
				sqlite.close();
			}
		}
		String host = "";
		if (WatchDog.currentHost != null) {
			host = WatchDog.currentHost.replaceAll("[.,/,:]", "");
		}
		String dbname = WatchDog.currentUserId + host;
		DBHelper db = new DBHelper(UpnpApp.context, null, null, version, dbname + ".db",handler);
		db.getReadableDatabase();
	}
	
	/**
	 * 获取本地单曲总数
	 * @return
	*/
	public static Map<String, Long> getMusicCountAndMaxLibid(
			SQLiteDatabase sqlite) {
		Cursor cur = sqlite.rawQuery("select count(*) ,max(lib_id) from db_music", null);
		long count = 0;
		long musicLibid = 0L;
		Map<String, Long> map = new HashMap<String, Long>();
		if (cur != null) {
			if (cur.getCount() == 0) {
//				map.put("count", count);
//				map.put("musicLibid", musicLibid);
				return null;
			} else {
				while (cur.moveToNext()) {
					count = cur.getInt(0);
					musicLibid = cur.getLong(1);
				}
			}
			map.put("count", (long) count);
			map.put("musicLibid", musicLibid);
			cur.close();

		}
		return map;
	}

}
