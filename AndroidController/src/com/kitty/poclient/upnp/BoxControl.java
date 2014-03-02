package com.kitty.poclient.upnp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.kitty.poclient.R;
import com.kitty.poclient.activity.LoginActivity;
import com.kitty.poclient.activity.MainActivity;
import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.MainHandler;
import com.kitty.poclient.common.MymusicManager;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.UpnpHelper;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.dao.AlbumDao;
import com.kitty.poclient.dao.ArtistDao;
import com.kitty.poclient.dao.MusicDao;
import com.kitty.poclient.dao.PackDao;
import com.kitty.poclient.dao.ProductArtistDao;
import com.kitty.poclient.data.VirtualData;
import com.kitty.poclient.db.DBHelper;
import com.kitty.poclient.domain.Album;
import com.kitty.poclient.domain.Artist;
import com.kitty.poclient.domain.Disk;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.domain.Pack;
import com.kitty.poclient.domain.ProductArtist;
import com.kitty.poclient.thread.SyncAsyncTask;
import com.kitty.poclient.util.JsonUtil;
import com.kitty.poclient.util.LoadImageAysnc;
import com.kitty.poclient.util.SingletonUtil;

public class BoxControl {
	// 000,new ActionInvocation(
	private final String TAG = "BoxControl";
	private static int numFailure = 0;
	
	//	1专辑 ，5单曲 ，15主题
	public static final int MEDIA_TYPE_ALBUM = 1;
	public static final int MEDIA_TYPE_SINGLE = 5;
	public static final int MEDIA_TYPE_THEME = 15;
	public static final int CLEAR_CACHE_SUCESS = 1;
	public static final int CLEAR_CACHE_FAILURE = 2;
	
	public void buyHandler() {

		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetNewSyn"));// IsFirstControl

		ai.setInput("Controlkey", WatchDog.macAddress);

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e(TAG, "GetNewSyn failure");
				Log.i("content", "not update");
				Looper.prepare();
//				CustomToast.makeText(UpnpApp.context, "新购数据同步失败：通信异常", 1000).show();
				UpnpApp.mainHandler.showAlert(R.string.store_syn_new_music_failure_alert);
				
				// Intent i = new Intent(UpnpApp.context, LoginActivity.class);
				// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// UpnpApp.context.startActivity(i);
				Looper.loop();
			}

			@Override
			public void success(ActionInvocation arg0) {
				Log.e(TAG, "GetNewSyn success");
				String sycontent = (String) arg0.getOutput("SynchoContent").getValue();
				Log.i("content", sycontent);
				Log.e(TAG, "sycontent=" + sycontent);
				SingletonUtil.imagflag = true;
				LoadImageAysnc.unlock();
				if (sycontent != null && sycontent != "") {
					String sysnids = contentHandler1(sycontent);
					if (sysnids != "") {
						Log.e(TAG, "VirtualData.clearcacheinitData()");
						VirtualData.clearcacheinitData();
						// VirtualData.initData();
						// getCloudStates(5);
						buySycontentAfterHandler(sysnids);

						WatchDog.clearPurchasingMaps();// 清空正在缓存中的数据
						webDetailPageUpdateUI();// 购买详情页刷新界面显示

						Message msg = UpnpApp.mainHandler.obtainMessage(MainHandler.SHOW_INFO);
						msg.obj = "数据同步完成";
						UpnpApp.mainHandler.sendMessage(msg);
					}
				}

			}

		});
	}

	private void webDetailPageUpdateUI() {
		UpnpApp.context.sendBroadcast(new Intent("webDetailPageUpdateUIReceiver"));
	}

	// 购买后发消息处理
	public static void buySycontentAfterHandler(String id) {
		if (UpnpApp.upnpService != null && UpnpApp.boxControlService != null) {
			ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("ClearNewSyn"));
			ai.setInput("Controlkey", WatchDog.macAddress);
			ai.setInput("Synids", id);
			UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {

				}

				@Override
				public void success(ActionInvocation arg0) {

				}

			});
		}

	}

	public boolean contentHandler(String sycontent) {
		// System.out.println("解析数据内容为: "+sycontent);
		// String[] objectArray=sycontent.split("*???*");//*???*
		// List<String> newStudentNames = new
		// ArrayList<String>(sycontent.Split(","));
		JSONObject inidbdata = null;
		JSONArray artists = null;
		JSONArray albumlist = null;
		JSONArray musicList = null;
		JSONArray technology = null;
		JSONArray packlist = null;
		Artist artist = null;
		Album album = null;
		ProductArtist pa = null;
		Disk disk = null;
		Music music = null;
		Pack pack = null;
		try {

			JSONArray jsonarray = new JSONArray(sycontent);
			Log.i("json", jsonarray.length() + "");
			for (int i = 0; i < jsonarray.length(); i++) {

				inidbdata = jsonarray.getJSONObject(i);
				if (inidbdata != null) {
					if (!inidbdata.isNull("musiclist")) {
						musicList = inidbdata.getJSONArray("musiclist");
					}
					if (!inidbdata.isNull("artists")) {
						artists = inidbdata.getJSONArray("artists");
					}
					if (!inidbdata.isNull("albumlist")) {
						albumlist = inidbdata.getJSONArray("albumlist");
					}
					if (!inidbdata.isNull("packlist")) {
						packlist = inidbdata.getJSONArray("packlist");
					}
					if (artists != null && artists.length() > 0) {
						for (int j = 0; j < artists.length(); j++) { // 获得演出者列表
							JSONObject jsonobj = artists.getJSONObject(i);
							artist = new Artist();
							artist.setId(jsonobj.getLong("id"));
							artist.setName(jsonobj.getString("name"));
							if (artist.getName() == null || artist.getName() == "") {
								artist.setName("未知演出者");
							}
							artist.setImgUrl(jsonobj.getString("smallimg"));
							artist.setFirstChar(jsonobj.getString("firstchar"));
							new ArtistDao().insertArtist(artist);
						}
					}

					if (albumlist != null && albumlist.length() > 0) {
						for (int k = 0; k < albumlist.length(); k++) {
							JSONObject jsonobj = albumlist.getJSONObject(k);
							album = new Album();
							album.setId(jsonobj.getLong("id"));
							album.setName(jsonobj.getString("albumname"));
							album.setImgUrl(jsonobj.getString("albumcovers"));
							album.setBuytime(jsonobj.getString("buytime"));
							if (album.getBuytime() == null || album.getBuytime() == "" || album.getBuytime().equals("null")) {
								album.setBuytime("0");
							}
							String[] artistli = jsonobj.getString("artistids") != null ? jsonobj.getString("artistids").split(",") : null;

							if (artistli != null && artistli.length > 0) {
								for (String str : artistli) { // 关联专辑下的演出者
									pa = new ProductArtist();
									pa.setArtistId(Long.parseLong(str));
									pa.setProductId(album.getId());
									new ProductArtistDao().insertProductArtist(pa);

								}
							}
							// / 这里根据不同的情况查找碟 ，最后保存专辑
							new AlbumDao().insertAlbum(album);

						}
					}

					if (musicList != null && musicList.length() > 0) {
						for (int m = 0; m < musicList.length(); m++) {
							JSONObject jsonmusic = musicList.getJSONObject(m);
							music = new Music();
							music.setId(jsonmusic.getLong("id"));
							music.setName(jsonmusic.getString("musicname"));
							music.setMediaurl(jsonmusic.getString("mediaurl"));
							music.setDiskId(jsonmusic.getLong("disk_id"));
							music.setDisk_no(jsonmusic.getString("serial_number"));
							music.setFile_size("");
							music.setFirstChar(jsonmusic.getString("firstchar"));
							music.setPlay_time(jsonmusic.getString("playtime"));
							music.setTrack_no(jsonmusic.getString("track_no"));
							music.setBuytime(jsonmusic.getString("buytime"));
							music.setAlbumId(jsonmusic.getString("albumid"));
							music.setDiskName(jsonmusic.getString("diskname"));
							JSONArray musicartistli = jsonmusic.getJSONArray("artists");
							if (musicartistli != null && musicartistli.length() > 0) {
								for (int k = 0; k < musicartistli.length(); k++) {
									JSONObject jsonmusicartistli = musicartistli.getJSONObject(k);
									pa = new ProductArtist();
									pa.setArtistId(jsonmusicartistli.getLong("id"));
									pa.setProductId(music.getId());
									new ProductArtistDao().insertProductArtist(pa);
								}
							}
							new MusicDao().insertMusic(music);
							System.gc();
						}

					}

					if (packlist != null && packlist.length() > 0) {
						String[] musicids = null;
						for (int n = 0; n < packlist.length(); n++) {
							JSONObject jsonpack = packlist.getJSONObject(n);
							pack = new Pack();
							pack.setId(jsonpack.getLong("id"));
							pack.setName(jsonpack.getString("name"));
							pack.setImgurl(jsonpack.getString("bigimg"));
							pack.setBuytime(jsonpack.getString("buytime"));
							pack.setLibraryid(jsonpack.getLong("libraryid"));
							musicids = jsonpack.getString("musicids") != null ? jsonpack.getString("musicids").split(",") : null;
							pack.setMusicids(musicids);
							new PackDao().insertPack(pack);

						}
					}

				}

			}

		} catch (JSONException e) {
			// System.out.println("解析的内容为："+sycontent);
			// System.out.println("解析错误");
			e.printStackTrace();

		} finally {
			// System.out.println("解析完毕！");
			return true;
		}

	}

	/**
	 * ken 2013.10.22
	 * 
	 * @param sycontent
	 * @return
	 */
	@SuppressWarnings("finally")
	public boolean contentHandler2(String sycontent) {
		JSONObject inidbdata = null;
		JSONArray arrList = null;
		Artist artist = null;
		Album album = null;
		ProductArtist pa = null;
		Music music = null;
		Pack pack = null;

		try {
			JSONArray jsonarray = new JSONArray(sycontent);
			Log.i("json", jsonarray.length() + "");
			int type = 0;

			for (int i = 0; i < jsonarray.length(); i++) {
				inidbdata = jsonarray.getJSONObject(i);

				if (inidbdata != null) {
					if (!inidbdata.isNull("musiclist")) {
						type = 1;
						arrList = inidbdata.getJSONArray("musiclist");
					}
					if (!inidbdata.isNull("artists")) {
						type = 2;
						arrList = inidbdata.getJSONArray("artists");
					}
					if (!inidbdata.isNull("albumlist")) {
						type = 3;
						arrList = inidbdata.getJSONArray("albumlist");
					}
					if (!inidbdata.isNull("packlist")) {
						type = 4;
						arrList = inidbdata.getJSONArray("packlist");
					}

					if (arrList != null && arrList.length() > 0) {

						switch (type) {
						case 1:// musicList
							JSONObject jsonmusic = null;
							JSONArray musicartistli = null;
							JSONObject jsonmusicartistli = null;

							for (int m = 0; m < arrList.length(); m++) {
								jsonmusic = arrList.getJSONObject(m);
								music = new Music();
								music.setId(jsonmusic.getLong("id"));
								music.setName(jsonmusic.getString("musicname"));
								music.setMediaurl(jsonmusic.getString("mediaurl"));
								music.setDiskId(jsonmusic.getLong("disk_id"));
								music.setDisk_no(jsonmusic.getString("serial_number"));
								music.setFile_size("");
								music.setFirstChar(jsonmusic.getString("firstchar"));
								music.setPlay_time(jsonmusic.getString("playtime"));
								music.setTrack_no(jsonmusic.getString("track_no"));
								music.setBuytime(jsonmusic.getString("buytime"));
								music.setAlbumId(jsonmusic.getString("albumid"));
								music.setDiskName(jsonmusic.getString("diskname"));
								musicartistli = jsonmusic.getJSONArray("artists");

								if (musicartistli != null && musicartistli.length() > 0) {
									for (int k = 0; k < musicartistli.length(); k++) {
										jsonmusicartistli = musicartistli.getJSONObject(k);
										pa = new ProductArtist();
										pa.setArtistId(jsonmusicartistli.getLong("id"));
										pa.setProductId(music.getId());
										new ProductArtistDao().insertProductArtist(pa);
									}
								}
								new MusicDao().insertMusic(music);
								System.gc();
							}
							break;
						case 2:
							JSONObject jsonobj = null;
							for (int j = 0; j < arrList.length(); j++) { // 获得演出者列表
								jsonobj = arrList.getJSONObject(i);
								artist = new Artist();
								artist.setId(jsonobj.getLong("id"));
								artist.setName(jsonobj.getString("name"));
								if (artist.getName() == null || artist.getName() == "") {
									artist.setName("未知演出者");
								}
								artist.setImgUrl(jsonobj.getString("smallimg"));
								artist.setFirstChar(jsonobj.getString("firstchar"));
								new ArtistDao().insertArtist(artist);
							}
							break;
						case 3:
							String[] artistli;
							JSONObject json_obj = null;

							for (int k = 0; k < arrList.length(); k++) {
								json_obj = arrList.getJSONObject(k);
								album = new Album();
								album.setId(json_obj.getLong("id"));
								album.setName(json_obj.getString("albumname"));
								album.setImgUrl(json_obj.getString("albumcovers"));
								album.setBuytime(json_obj.getString("buytime"));
								if (album.getBuytime() == null || album.getBuytime() == "" || album.getBuytime().equals("null")) {
									album.setBuytime("0");
								}
								artistli = json_obj.getString("artistids") != null ? json_obj.getString("artistids").split(",") : null;

								if (artistli != null && artistli.length > 0) {
									for (String str : artistli) { // 关联专辑下的演出者
										pa = new ProductArtist();
										pa.setArtistId(Long.parseLong(str));
										pa.setProductId(album.getId());
										new ProductArtistDao().insertProductArtist(pa);

									}
								}
								// / 这里根据不同的情况查找碟 ，最后保存专辑
								new AlbumDao().insertAlbum(album);
							}
							break;
						case 4:// packlist
							String[] musicids = null;
							JSONObject jsonpack = null;
							for (int n = 0; n < arrList.length(); n++) {
								jsonpack = arrList.getJSONObject(n);
								pack = new Pack();
								pack.setId(jsonpack.getLong("id"));
								pack.setName(jsonpack.getString("name"));
								pack.setImgurl(jsonpack.getString("bigimg"));
								pack.setBuytime(jsonpack.getString("buytime"));
								pack.setLibraryid(jsonpack.getLong("libraryid"));
								musicids = jsonpack.getString("musicids") != null ? jsonpack.getString("musicids").split(",") : null;
								pack.setMusicids(musicids);
								new PackDao().insertPack(pack);
							}
							break;
						}// end switch(type)
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();

		} finally {
			return true;
		}

	}

	public synchronized String contentHandler1(String sycontent) {
		System.out.println("解析数据内容为: " + sycontent);
		JSONObject inidbdata = null;
		JSONArray artists = null;
		JSONArray albumlist = null;
		JSONArray musicList = null;
		JSONArray technology = null;
		JSONArray packlist = null;
		Artist artist = null;
		Album album = null;
		Disk disk = null;
		Music music = null;
		Pack pack = null;
		String synids = "";
		try {
			JSONArray jsonarray = new JSONArray(sycontent);
			Log.i("json", jsonarray.length() + "");

			for (int i = 0; i < jsonarray.length(); i++) {
				String synid = null;// 记录每次购买操作id；
				String syndata = null;// 每次购买需要同步的数据
				inidbdata = jsonarray.getJSONObject(i);
				if (inidbdata != null) {
					synid = inidbdata.getString("synid");
					syndata = inidbdata.getString("syndata");
					if (synid != null && syndata != null) {
						JSONObject obj = new JSONObject(syndata);

						inidbdata = obj;
						if (!inidbdata.isNull("musiclist")) {
							musicList = inidbdata.getJSONArray("musiclist");
						}
						if (!inidbdata.isNull("artists")) {
							artists = inidbdata.getJSONArray("artists");
						}
						if (!inidbdata.isNull("albumlist")) {
							albumlist = inidbdata.getJSONArray("albumlist");
						}
						if (!inidbdata.isNull("packlist")) {
							packlist = inidbdata.getJSONArray("packlist");
						}
						if (artists != null && artists.length() > 0) {
							for (int j = 0; j < artists.length(); j++) { // 获得演出者列表
								JSONObject jsonobj = artists.getJSONObject(j);
								artist = new Artist();
								artist.setId(jsonobj.getLong("id"));
								artist.setName(jsonobj.getString("name"));
								if (artist.getName() == null || artist.getName() == "") {
									artist.setName("未知演出者");
								}
								artist.setImgUrl(jsonobj.getString("smallimg"));
								artist.setFirstChar(jsonobj.getString("firstchar"));
								new ArtistDao().insertArtist(artist);
							}
						}

						if (albumlist != null && albumlist.length() > 0) {
							ProductArtist pa = null;
							for (int k = 0; k < albumlist.length(); k++) {
								JSONObject jsonobj = albumlist.getJSONObject(k);
								album = new Album();
								album.setId(jsonobj.getLong("id"));
								album.setName(jsonobj.getString("albumname"));
								album.setImgUrl(jsonobj.getString("albumcovers"));
								album.setBuytime(jsonobj.getString("buytime"));
								album.setIsCloud(5);
								if (album.getBuytime() == null || album.getBuytime() == "" || album.getBuytime().equals("null")) {
									album.setBuytime("0");
								}
								String[] artistli = jsonobj.getString("artistids") != null ? jsonobj.getString("artistids").split(",") : null;

								if (artistli != null && artistli.length > 0) {
									for (int q = 0; q < artistli.length; q++) {// 关联专辑下的演出者
										pa = new ProductArtist();
										pa.setArtistId(Long.parseLong(artistli[q]));
										pa.setProductId(album.getId());
										new ProductArtistDao().insertProductArtist(pa);
									}
								}

								// / 这里根据不同的情况查找碟 ，最后保存专辑
								new AlbumDao().insertAlbum(album);

							}
						}

						if (musicList != null && musicList.length() > 0) {
							ProductArtist pa1 = null;
							for (int m = 0; m < musicList.length(); m++) {
								JSONObject jsonmusic = musicList.getJSONObject(m);
								music = new Music();
								music.setId(jsonmusic.getLong("id"));
								music.setName(jsonmusic.getString("musicname"));
								music.setMediaurl(jsonmusic.getString("mediaurl"));
								music.setDiskId(jsonmusic.getLong("disk_id"));
								music.setDisk_no(jsonmusic.getString("serial_number"));
								music.setFile_size("");
								music.setFirstChar(jsonmusic.getString("firstchar"));
								music.setPlay_time(jsonmusic.getString("playtime"));
								music.setTrack_no(jsonmusic.getString("track_no"));
								music.setBuytime(jsonmusic.getString("buytime"));
								music.setAlbumId(jsonmusic.getString("albumid"));
								music.setDiskName(jsonmusic.getString("diskname"));
								music.setIscloud(5);
								// JSONArray musicartistli =
								// jsonmusic.getJSONArray("artists");
								// if (musicartistli != null &&
								// musicartistli.length() > 0) {
								// for (int k = 0; k < musicartistli.length();
								// k++) {
								// JSONObject jsonmusicartistli =
								// musicartistli.getJSONObject(k);
								// pa1 = new ProductArtist();
								// pa1.setArtistId(jsonmusicartistli.getLong("id"));
								// pa1.setProductId(music.getId());
								// new
								// ProductArtistDao().insertProductArtist(pa1);
								// }
								// }
								//
								String artistidss = "";
								if (!jsonmusic.isNull("artistids")) {
									artistidss = jsonmusic.getString("artistids");
								} else {
									new ProductArtistDao().inserProudctArtistDefault(music.getId());
								}

								/*
								 * if (musicartistli != null &&
								 * musicartistli.length() > 0) { for (int k = 0;
								 * k < musicartistli.length(); k++) { JSONObject
								 * jsonmusicartistli =
								 * musicartistli.getJSONObject(k); pa1 = new
								 * ProductArtist();
								 * pa1.setArtistId(jsonmusicartistli
								 * .getLong("id"));
								 * pa1.setProductId(music.getId()); new
								 * ProductArtistDao().insertProductArtist(pa1);
								 * } }
								 */
								if (artistidss != null && artistidss != "") {
									String[] str = artistidss.split(",");
									if (str != null && str.length > 0) {
										for (int q = 0; q < str.length; q++) {
											pa1 = new ProductArtist();
											pa1.setArtistId(Long.parseLong(str[q]));
											pa1.setProductId(music.getId());
											new ProductArtistDao().insertProductArtist(pa1);
										}
									}
								} else {
									new ProductArtistDao().inserProudctArtistDefault(music.getId());
								}
								new MusicDao().insertMusic(music);
								System.gc();
							}

						}

						if (packlist != null && packlist.length() > 0) {
							String[] musicids = null;
							for (int n = 0; n < packlist.length(); n++) {
								JSONObject jsonpack = packlist.getJSONObject(n);
								pack = new Pack();
								pack.setId(jsonpack.getLong("id"));
								pack.setName(jsonpack.getString("name"));
								pack.setImgurl(jsonpack.getString("bigimg"));
								pack.setBuytime(jsonpack.getString("buytime"));
								pack.setLibraryid(jsonpack.getLong("libraryid"));
								pack.setIsCloud(5);
								musicids = jsonpack.getString("musicids") != null ? jsonpack.getString("musicids").split(",") : null;
								pack.setMusicids(musicids);
								new PackDao().insertPack(pack);

							}
						}
						synids = synids + synid + ",";

					}
				}

			}
			if (synids != "") {
				synids = synids.substring(0, synids.length() - 1);
			}
		} catch (JSONException e) {
			// System.out.println("解析的内容为：" + sycontent);
			// System.out.println("解析错误");
			e.printStackTrace();

		} finally {
			return synids;
		}

	}

	public void getBoxHeartBeat() {
		Log.e("BUG982", "getBoxHeartBeat()");
		ActionInvocation ai = null;
		try {
			ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetBoxUserInfo"));
		} catch (Exception e) {
			e.printStackTrace();
			Looper.prepare();
//			CustomToast.makeText(UpnpApp.context, UpnpApp.context.getString(R.string.errorUpnp), Toast.LENGTH_SHORT).show();
			UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
			Looper.loop();
			return;
		}

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void success(ActionInvocation arg0) {
				Log.e(TAG, "GetBoxUserInfo>>success");
				Log.e("BUG982", "getBoxHeartBeat()>>success");
				
				WatchDog.isBoxAlive = true;
				numFailure = 0;
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				numFailure++;
				Log.e(TAG, "getBoxHeartBeat()>>failure");
				Log.e("BUG982", "GetBoxUserInfo>>failure");

				if (numFailure < 3) {
					getBoxHeartBeat();
				} else {
					WatchDog.isBoxAlive = false;
					numFailure = 0;
				}
			}
		});

	}

	/**
	 * 更改本地云信息状态
	 * 
	 * @param type
	 * @param oper
	 * @param ids
	 */
	public void getCloudStates(int type, int oper, String ids) {
		Log.e("BUG975", "10>>"+TAG+"getCloudStates("+type+","+oper+","+ids+")");
		/*
		 * <Event xmlns="urn:schemas-upnp-org:metadata-1-0/AVT/"><InstanceID
		 * val="0"><Hassyn
		 * val="cloudchange:1380191845606,type:1,5,10,ids:11233-754554,oper:1,5"
		 * /></InstanceID></Event> 说明：type :1专辑 5单曲 15主题 oper:1删除 5同步本地 String
		 * cloudChange=str[0]; String type=str[1]; String ids=str[2]; String
		 * oper=str[3];
		 */
		String[] id = ids.split("-");
		Log.i(TAG , "主题BBBBB -------------------------");
		Log.i(TAG , "主题BBBBB ids:" + ids);
		
		int locationStateToSet = Constant.LOCATION_STATE_REMOTE;
		if (oper == Constant.LOCATION_OPERATION_FETCH) {
			locationStateToSet = Constant.LOCATION_STATE_LOCAL;
		}
		
		switch (type) {

			// 同步云专辑
			case 1: {
				if (WatchDog.flag) {
					new AlbumDao().updateCloudState(id != null ? Long.parseLong(id[0]) : 0, locationStateToSet);
					VirtualData.clearcacheinitData();
					
					if (oper == Constant.LOCATION_OPERATION_DELETE) {
//						// 2切换到新框架(总共两点)
//						// Intent intent = new Intent(UpnpApp.context,
//						// MainActivity.class);//old
//						Intent intent = new Intent(UpnpApp.context, com.dfim.app.activity.MainActivity.class);// new
//						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						UpnpApp.context.startActivity(intent);
						MymusicManager.tabMusicFragment.back();
						MymusicManager.tabMusicFragment.back();
					}
					
				} else {
					WatchDog.flag = true;
				}
				break;
			}
	
			// 同步云单曲：单曲已经回归本地数据库，但没有专辑可以在专辑列表中显示
			case 5: {
				new MusicDao().updateMusicState(id, locationStateToSet);
				VirtualData.clearcacheinitData();
				
				if (oper == Constant.LOCATION_OPERATION_DELETE) {
					MymusicManager.tabMusicFragment.back();
					MymusicManager.tabMusicFragment.back();
				}
				
				break;
			}
	
			// 同步云主题
			case 15: {
//				new PackDao().updatePackState(id != null ? Long.parseLong(id[0]) : 0, tmp);
				/*
				 * for(int i=0; i< id.length; i++){
						Log.i(TAG , "主题BBBBB id[i]:" + id[i]);
					}
					Log.i(TAG , "主题BBBBB tmp:" + tmp);
					new PackDao().updatePackState(id, tmp);
					VirtualData.clearcacheinitData();
				*/
				new BoxControl().getCloudStates(1, null);
				break;
			}
		}
		// TabMusicActivity.getCacheInfo();
		MymusicManager.getCacheInfo();
		
		if (WatchDog.activity != null) {
			WatchDog.activity.finish();
		}

	}
	
	private void synAlbums(int oper, String[] id, int tmp){
		if (WatchDog.flag) {
			new AlbumDao().updateCloudState(id != null ? Long.parseLong(id[0]) : 0, tmp);
			VirtualData.clearcacheinitData();
			
			if (oper == 1) {
				// 2切换到新框架(总共两点)
				// Intent intent = new Intent(UpnpApp.context,
				// MainActivity.class);//old
				Intent intent = new Intent(UpnpApp.context, com.kitty.poclient.activity.MainActivity.class);// new
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				UpnpApp.context.startActivity(intent);
			}
			
		} else {
			WatchDog.flag = true;
		}
	}
	private void synSingles(String[] id, int tmp){
		new MusicDao().updateMusicState(id, tmp);
		VirtualData.clearcacheinitData();
	}
	private void synThemes(String[] id, int tmp){
		
	}
	
	/**
	 * 优化同步云端数据
	 */
	public void getCloudStates(final int type, final Handler handler) {
		/*
		 * <Event xmlns="urn:schemas-upnp-org:metadata-1-0/AVT/"><InstanceID
		 * val="0"><Hassyn
		 * val="cloudchange:1380191845606,type:1,5,10,ids:11233-754554,oper:1,5"
		 * /></InstanceID></Event> 说明：type :1专辑 5单曲 15主题 oper:1删除 5同步本地 String
		 * cloudChange=str[0]; String type=str[1]; String ids=str[2]; String
		 * oper=str[3];
		 */
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetCloudState"));
		ai.setInput("ControlKey", WatchDog.macAddress);
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e("BUG982", "GetCloudState>>failure");
				
				VirtualData.initData();
				startMainActivity(handler);
			}

			@Override
			public void success(ActionInvocation arg0) {
				Log.e("BUG982", "GetCloudState>>success");
				Log.i("tongbu", "云信息放回成功！");
				
				WatchDog.clearCacheStateMap.clear();
				String sycontent = (String) arg0.getOutput("Json").getValue();
				try {
					JSONObject ob = new JSONObject(sycontent);
					setStateMap("albums", ob);
					new AlbumDao().updateCloudStates(WatchDog.clearCacheStateMap);
					
					//专辑 判断专辑是否为空
					boolean noAlbum = false;
					List<Long> albumIds = WatchDog.clearCacheStateMap.get("albums");
					if(albumIds==null || albumIds.size()==0){
						noAlbum = true;
					}
					
					//单曲
					setStateMap("musics", ob);
					new MusicDao().updateCloudStates(WatchDog.clearCacheStateMap);
					
					//主题
					setStateMap("themes", ob);
					new PackDao().updateCloudStates(WatchDog.clearCacheStateMap, noAlbum);
					
				} catch (Exception e) {

					e.printStackTrace();
				}
				VirtualData.clearcacheinitData();
				Log.i("tongbu", "同步云信息成功");
				Log.i("tongbu", type + "");
				if (type != 5) {

					startMainActivity(handler);

				}
//				for (int i = 0; i < 500; i++) {
//
//				}
//				TabMusicActivity.getCacheInfo();
				// MyMusicActivity.getCacheInfo();
				MymusicManager.getCacheInfo();
				
				if (WatchDog.activity != null) {
					WatchDog.activity.finish();
				}
			}

		});
	}

	private void startMainActivity(Handler handler) {
//		Message msg = handler.obtainMessage();
//		msg.arg1 = 99;
//		msg.what = LoginActivity.UPGRADE_PROGRESS;
//		handler.sendMessage(msg);
		
		SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_StartMainActivity, 100);
		
		// 1切换到新框架(总共两点)
		// Intent intent = new Intent(UpnpApp.context, MainActivity.class);//old
		Intent intent = new Intent(UpnpApp.context, MainActivity.class);// new
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		UpnpApp.context.startActivity(intent);
		handler.sendEmptyMessage(LoginActivity.SYNC_FINISHED);
	}

	/**
	 * ken 2013.10.23
	 * 
	 * @param tag
	 * @param ob
	 * @throws JSONException
	 */
	private void setStateMap(String tag, JSONObject ob) throws JSONException {
		JSONArray marray = ob.getJSONArray(tag);
		List<Long> list = new ArrayList<Long>();

		if (null != marray && marray.length() > 0 && !marray.getString(0).equals("null")) {
			for (int i = 0; i < marray.length(); i++) {
				list.add(marray.getLong(i));
			}
		}
		WatchDog.clearCacheStateMap.put(tag, list);
	}

	/**
	 * @decription 清除缓存
	 * @param idStr
	 * @param type
	 *            1专辑 ，5单曲 ，15主题
	 */
	public void clearcache(final String idStr, final int type, final Handler handler) {
		Log.i("SinglesCache", "clearcache, BoxControl, type:" + type);
		Log.i("SinglesCache", "clearcache, BoxControl, ids:" + idStr);
		Log.e("BUG975", "6>>"+TAG+"clearcache("+idStr+","+type+"hanler)");
		
		if (UpnpApp.upnpService != null && UpnpApp.cacheControlService != null) {
			ActionInvocation ai = new ActionInvocation(UpnpApp.cacheControlService.getAction("ClearCache"));

/*			if (type == 1) {
				ai.setInput("CacheURI", "cache://album?id=" + ids);
			} else if (type == 5) {
				ai.setInput("CacheURI", "cache://music?ids=" + ids);
			} else if (type == 15) {
				ai.setInput("CacheURI", "cache://theme?id=" + ids);
			}*/
			switch(type){
				case MEDIA_TYPE_ALBUM:
					ai.setInput("CacheURI", "cache://album?id=" + idStr);
					break;
				case MEDIA_TYPE_SINGLE:
					ai.setInput("CacheURI", "cache://music?ids=" + idStr);
					break;
				case MEDIA_TYPE_THEME:
					ai.setInput("CacheURI", "cache://theme?id=" + idStr);
					break;
			}

			UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
					Log.i("SinglesCache", "clearcache, BoxControl, CLEAR_CACHE_FAILURE");
					Message msg = handler.obtainMessage();
					msg.what = CLEAR_CACHE_FAILURE;
					handler.sendMessage(msg);
				}

				@Override
				public void success(ActionInvocation arg0) {
					Log.i("SinglesCache", "clearcache, BoxControl, CLEAR_CACHE_SUCESS");
					WatchDog.clearCacheProductType = type;
					Message msg = handler.obtainMessage();
					msg.what = CLEAR_CACHE_SUCESS;
					msg.obj = idStr;
					handler.sendMessage(msg);
				}

			});
		}
	}

	/*
	 * 通知盒子端同步新购买数据
	 */
	public void notifyBoxToSyn() {
		Log.e(TAG, "notifyBoxToSyn");
		ActionInvocation ai = null;
		try {
			ai = new ActionInvocation(UpnpApp.boxControlService.getAction("NotifyBoxToSyn"));
			ai.setInput("ControlKey", WatchDog.macAddress);
		} catch (Exception e) {
			Looper.prepare();
//			CustomToast.makeText(UpnpApp.context, "未能同步：与设备通信失败", Toast.LENGTH_SHORT).show();
			UpnpApp.mainHandler.showAlert(R.string.syn_failure);
			Looper.loop();

			e.printStackTrace();
			return;
		}

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e("BUG982", "NotifyBoxToSyn>>failure");
				Log.e(TAG, "notifyBoxToSyn>>failure=" + WatchDog.upnpActionPerformCount);
				
				WatchDog.upnpActionPerformCount++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (WatchDog.upnpActionPerformCount < 3) {
					notifyBoxToSyn();
				} else {
					Looper.prepare();
//					CustomToast.makeText(UpnpApp.context, "未能同步：与设备通信失败", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
					Looper.loop();

					WatchDog.upnpActionPerformCount = 0;
				}
			}

			@Override
			public void success(ActionInvocation arg0) {
				Log.e(TAG, "notifyBoxToSyn>>success");
				Log.e("BUG982", "NotifyBoxToSyn>>success");
				
				WatchDog.hasNewBought = false;
				WatchDog.upnpActionPerformCount = 0;
			}

		});
	}

	/**
	 * @decription 同步到本地
	 * @param ids
	 * @param type
	 *            1专辑 ，5单曲 ，15主题
	 */
	public void synCloud(String ids, final int type) {
		if (UpnpApp.upnpService != null && UpnpApp.cacheControlService != null) {
			ActionInvocation ai = new ActionInvocation(UpnpApp.cacheControlService.getAction("ClearCache"));
			if (type == 1) {
				ai.setInput("CacheURI", "cache://album?id=" + ids);
			} else if (type == 5) {
				ai.setInput("CacheURI", "cache://music?ids=" + ids);
			} else if (type == 15) {
				ai.setInput("CacheURI", "cache://theme?id=" + ids);
			}

			UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {

				}

				@Override
				public void success(ActionInvocation arg0) {
					getCloudStates(type, null);

				}

			});
		}
	}
	
	/**
	 * 增量同步box数据传入libid
	 * 
	 */
/*	public void asyAddBoxData(final Handler handler, String libid) {
		if (libid == "" || libid == null) {
			libid = "-1";
		}
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetAllUnSyn"));
		ai.setInput("Controlkey", WatchDog.macAddress);
		
//		JSONObject jsonOject = new JSONObject();
//		try {
//			Log.e("GetAllUnSyn", "asyAddBoxData,jsonOject.toString():" + jsonOject.toString());
//			jsonOject.put("libarayid", libid);
//			jsonOject.put("max", PAGE_NUM);
//			
//		} catch (JSONException e1) {
//			Log.e("GetAllUnSyn", "jsonOject create error:" + e1.getMessage());
//			e1.printStackTrace();
//		}
		ai.setInput("Json", libid);
//		ai.setInput("Json", jsonOject.toString());
		//TODO 分页同步数据。

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				upnpFailureHandle();
			}

			@Override
			public void success(ActionInvocation arg0) {
				String sycontent = (String) arg0.getOutput("SynchoContent").getValue();//接收数据
				Log.i("GetAllUnSyn", "sycontent=" + sycontent);
				if (sycontent != null && sycontent != "") {
					WatchDog.l = System.currentTimeMillis();
					
					if (analyseBoxData(sycontent, handler)) {//解释数据
						
						new BoxControl().getCloudStates(1, handler); //同步云信息
						
						VirtualData.clearcacheinitData(); //获取本地 专辑、单曲和主题 信息
						
						startMainActivity(handler);  //链接到新界面 MainActivity
					
					} else {
						
						upnpFailureHandle();
					}
					
					try {
						Thread.sleep(500L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					TabMusicActivity.getCacheInfo();
					// MyMusicActivity.getCacheInfo();
					MymusicManager.getCacheInfo();
					
					if (WatchDog.activity != null) {
						WatchDog.activity.finish();
					}
				} else {
					upnpFailureHandle();
				}
			}

		});

	}*/
	/**
	 * 增量同步box数据传入libid (大于或等于-1的整数)
	 * 
	 */
	public void getPageUnSyn(final Handler handler, final String musicLibid) {
		String controlKeyValue = WatchDog.macAddress;

		List<Object> values = new ArrayList<Object>();
		values.add(musicLibid); // 起始 music library id
		values.add(UpnpHelper.GetPageUnSyn_PAGE_NUM); // pageNum

		String jsonValue = UpnpHelper.generateJSONObject(UpnpHelper.getKeys_GetPageUnSyn(), values).toString();
		ActionInvocation<?> action = UpnpHelper.generateAction_GetPageUnSyn(controlKeyValue, jsonValue);
		
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(action) {

			@Override
			@SuppressWarnings("rawtypes")
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				upnpFailureHandle();
			}

			@Override
			@SuppressWarnings("rawtypes")
			public void success(ActionInvocation arg0) {
				
//				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, GetAllUnSynProgress + 0.05);
				
				String sycontent = (String) arg0.getOutput("SynchoContent").getValue();// 接收数据
				Log.i("GetPageUnSyn", "sycontent=" + sycontent);
				if (sycontent != null && sycontent != "") {
					// {"syndata":{},"synid":203}
					JSONObject resultJO;
					try {
						resultJO = new JSONObject(sycontent);
						if (!resultJO.getString("syndata").equals("{}")) {
							// 解释数据
							readDataFrom_GetAllUnSyn(sycontent, handler);

							// TODO 处理进度显示

							// 循环获取数据
//							int nextBeginNum = beginNum + UpnpHelper.GetPageUnSyn_PAGE_NUM;
							//TODO 每次要获取最后一首音乐的 musicLibId
						    Map<String ,Long> musicmap=SyncAsyncTask.getMusicCountAndMaxLibid(DBHelper.getSqLitedatabase());
							
							getPageUnSyn(handler, musicmap.get("musicLibid")+"");

						} else {// 完成同步
							SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 100);
							getCloudStates(1, handler); // 同步云信息
							SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetCloudStates, 100);
							VirtualData.clearcacheinitData(); // 获取本地 专辑、单曲和主题 信息
							SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_InitLocalData, 100);
							
							startMainActivity(handler); // 链接到新界面 MainActivity
						}
					} catch (JSONException e) {
						Log.e(TAG, "error in 解释数据:" + e.getMessage());
						e.printStackTrace();
						upnpFailureHandle();
					}

					MymusicManager.getCacheInfo();
					if (WatchDog.activity != null) {
						WatchDog.activity.finish();
					}
					
				} else {
					upnpFailureHandle();
				}
			}

		});

	}
	
	/**
	 * 同步所有box数据
	 */
/*	deprecated on 20140123 by eason
 * public void anyBoxData(final Handler handler) {
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetAllSyn"));

		ai.setInput("Controlkey", WatchDog.macAddress);
		// ai.setInput("Json",productIds);
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				upnpFailureHandle();
			}

			@Override
			public void success(ActionInvocation arg0) {
				String sycontent = (String) arg0.getOutput("SynchoContent").getValue();
				Log.i("content", sycontent);
				if (analyseBoxData(sycontent, handler)) {
					getCloudStates(1, handler);
				}

			}

		});
	}*/

	/**
	 * 判断是否要同步box数据
	 * handler 进度条显示控制
	 */
//	private static double GetAllUnSynProgress = 0;
	public void isTrueBoxDate(final Long musicAccount, final Handler handler, final String musicLibid) {
//		GetAllUnSynProgress = 0;
//		SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, GetAllUnSynProgress + 0.1);
		
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetAllSynCount"));
		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e("BUG982", "GetAllSynCount>>failure");
				upnpFailureHandle();
			}

			@Override
			public void success(ActionInvocation arg0) {
				Log.e("BUG982", "GetAllSynCount>>success");
				String sycontent = (String) arg0.getOutput("SynchoContent").getValue();

				try {

					JSONObject jobj = new JSONObject(sycontent);
					int count = jobj.getInt("count");
					if (count != musicAccount && count != 0) {
						
						//<Eason: replace "asyAddBoxData" with "synNewData" 
							//asyAddBoxData(handler, libid); // 增量同步数据
							SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 5);
							getPageUnSyn(handler, musicLibid);
						//>
					} else if (musicAccount == count) {
						getCloudStates(1, handler);
					} else {
						upnpFailureHandle();
					}

				} catch (Exception e) {
					upnpFailureHandle();
				}

			}

		});

	}

	/**
	 * Replace by readDataFrom_GetAllUnSyn
	 * 解析box传递过来的所有数据
	 */
	/*public boolean analyseBoxData(String sycontent, Handler handler) {
		JSONObject inidbdata = null;
		JSONArray artists = null;
		JSONArray albumlist = null;
		JSONArray musicList = null;
		JSONArray packlist = null;
		Artist artist = null;
		Album album = null;
		Music music = null;
		Pack pack = null;
		try {
			int synid = 0;// 数据库单曲总数
			String syndata = null;// 所有数据
			inidbdata = new JSONObject(sycontent);
			if (inidbdata != null) {
				synid = inidbdata.getInt("synid");
				syndata = inidbdata.getString("syndata");
				if (synid != 0 && syndata != null) {
					JSONObject obj = new JSONObject(syndata);
					inidbdata = obj;
					if (!inidbdata.isNull("musiclist")) {
						musicList = inidbdata.getJSONArray("musiclist");
					}
					if (!inidbdata.isNull("artists")) {
						artists = inidbdata.getJSONArray("artists");
					}
					if (!inidbdata.isNull("albumlist")) {
						albumlist = inidbdata.getJSONArray("albumlist");
					}
					if (!inidbdata.isNull("packlist")) {
						packlist = inidbdata.getJSONArray("packlist");
					}
					
					//统计数据长度
					int dataLength = 0;
					int processData = 1;
					ArrayList<JSONArray> dataList = new ArrayList<JSONArray>();
					dataList.add(musicList);
					dataList.add(artists);
					dataList.add(albumlist);
					dataList.add(packlist);
					for(int start=0;start<dataList.size();start++){
						JSONArray jsonArray = dataList.get(start);
						if (jsonArray != null && jsonArray.length() > 0) {
							dataLength += jsonArray.length();
						}
						Log.i("PersentHandler", "dataLength=" + dataLength);
					}
					
					if(dataLength == 0){
						countProgress(handler, 1, 1);
					}
					
					if (artists != null && artists.length() > 0) {
						for (int j = 0; j < artists.length(); j++, processData++) { // 获得演出者列表
							JSONObject jsonobj = artists.getJSONObject(j);
							artist = new Artist();
							artist.setId(jsonobj.getLong("id"));
							artist.setName(jsonobj.getString("name"));
							if (artist.getName() == null || artist.getName() == "") {
								artist.setName("未知演出者");
							}
							artist.setImgUrl(jsonobj.getString("smallimg"));
							artist.setFirstChar(jsonobj.getString("firstchar"));
							new ArtistDao().insertArtist(artist);
							
							countProgress(handler, processData, dataLength);
						}
						Log.i("PersentHandler", "processData=" + processData);
					}

					if (albumlist != null && albumlist.length() > 0) {
						ProductArtist pa = null;
						for (int k = 0; k < albumlist.length(); k++,processData++) {
							JSONObject jsonobj = albumlist.getJSONObject(k);
							album = new Album();
							album.setId(jsonobj.getLong("id"));
							album.setName(jsonobj.getString("albumname"));
							album.setImgUrl(jsonobj.getString("albumcovers"));
							album.setBuytime(jsonobj.getString("buytime"));
							album.setIsCloud(jsonobj.getInt("showstate") == 0 ? 5 : 0);
							if (album.getBuytime() == null || album.getBuytime() == "" || album.getBuytime().equals("null")) {
								album.setBuytime("0");
							}
							String[] artistli = jsonobj.getString("artistids") != null ? jsonobj.getString("artistids").split(",") : null;

							if (artistli != null && artistli.length > 0) {
								for (String str : artistli) { // 关联专辑下的演出者
									pa = new ProductArtist();
									pa.setArtistId(Long.parseLong(str));
									pa.setProductId(album.getId());
									new ProductArtistDao().insertProductArtist(pa);
								}
							}
							// / 这里根据不同的情况查找碟 ，最后保存专辑
							new AlbumDao().insertAlbum(album);

							countProgress(handler, processData, dataLength);
						}
						Log.i("PersentHandler", "processData=" + processData);
					}

					if (musicList != null && musicList.length() > 0) {
						int handlerTotal = 0;// 发送handlerTotal次数
						ProductArtist pa1 = null;

						for (int m = 0; m < musicList.length(); m++,processData++) {
							JSONObject jsonmusic = musicList.getJSONObject(m);
							music = new Music();
							music.setId(jsonmusic.getLong("id"));
							music.setName(jsonmusic.getString("musicname"));
							music.setMediaurl(jsonmusic.getString("mediaurl"));
							music.setDiskId(jsonmusic.getLong("disk_id"));
							music.setDisk_no(jsonmusic.getString("serial_number"));
							music.setFile_size("");
							music.setLibid(jsonmusic.getLong("libraryid"));
							music.setFirstChar(jsonmusic.getString("firstchar"));
							music.setPlay_time(jsonmusic.getString("playtime"));
							music.setTrack_no(jsonmusic.getString("track_no"));
							music.setBuytime(jsonmusic.getString("buytime"));
							music.setAlbumId(jsonmusic.getString("albumid"));
							music.setDiskName(jsonmusic.getString("diskname"));
							music.setIscloud(jsonmusic.getInt("showstate") == 0 ? 5 : 0);
							String artistidss = "";
							if (!jsonmusic.isNull("artistid")) {
								artistidss = jsonmusic.getString("artistid");
							} else {
								new ProductArtistDao().inserProudctArtistDefault(music.getId());
							}

							if (artistidss != null && artistidss != "") {
								String[] str = artistidss.split(",");
								if (str != null && str.length > 0) {
									for (int q = 0; q < str.length; q++) {
										pa1 = new ProductArtist();
										pa1.setArtistId(Long.parseLong(str[q]));
										pa1.setProductId(music.getId());
										new ProductArtistDao().insertProductArtist(pa1);
									}
								}
							} else {
								new ProductArtistDao().inserProudctArtistDefault(music.getId());
							}
							new MusicDao().insertMusic(music);

							countProgress(handler, processData, dataLength);
						}
						Log.i("PersentHandler", "processData=" + processData);
					}

					if (packlist != null && packlist.length() > 0) {
						String[] musicids = null;
						for (int n = 0; n < packlist.length(); n++,processData++) {
							JSONObject jsonpack = packlist.getJSONObject(n);
							pack = new Pack();
							pack.setId(jsonpack.getLong("id"));
							pack.setName(jsonpack.getString("name"));
							pack.setImgurl(jsonpack.getString("bigimg"));
							pack.setBuytime(jsonpack.getString("buytime"));
							pack.setIsCloud(jsonpack.getInt("showstate") == 0 ? 5 : 0);
							// pack.setLibraryid(jsonpack.getLong("libraryid"));
							pack.setLibraryid(0L);// 同步盒子没有没有库id，默认为0
							musicids = jsonpack.getString("musicids") != null ? jsonpack.getString("musicids").split(",") : null;
							pack.setMusicids(musicids);
							new PackDao().insertPack(pack);

							countProgress(handler, processData, dataLength);
						}
						Log.i("PersentHandler", "processData=" + processData);
					}
					
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}*/
	
	/**
	 * 解析box传递过来的所有数据
	 * @throws JSONException 
	 */
	public boolean readDataFrom_GetAllUnSyn(String sycontent, Handler handler) throws JSONException {
		JSONObject inidbdata = null;
		JSONArray artists = null;
		JSONArray albumlist = null;
		JSONArray musicList = null;
		JSONArray packlist = null;
		Artist artist = null;
		Album album = null;
		Music music = null;
		Pack pack = null;
//		try {
		int synid = 0;// 数据库单曲总数
		String syndata = null;// 所有数据
		inidbdata = new JSONObject(sycontent);
		if (inidbdata != null) {
			synid = inidbdata.getInt("synid");
			syndata = inidbdata.getString("syndata");
			if (synid != 0 && syndata != null) {
				JSONObject obj = new JSONObject(syndata);
				inidbdata = obj;
				if (!inidbdata.isNull("musiclist")) {
					musicList = inidbdata.getJSONArray("musiclist");
				}
				if (!inidbdata.isNull("artists")) {
					artists = inidbdata.getJSONArray("artists");
				}
				if (!inidbdata.isNull("albumlist")) {
					albumlist = inidbdata.getJSONArray("albumlist");
				}
				if (!inidbdata.isNull("packlist")) {
					packlist = inidbdata.getJSONArray("packlist");
				}
				
				//统计数据长度
				int dataLength = 0;
				int processData = 1;
				ArrayList<JSONArray> dataList = new ArrayList<JSONArray>();
				dataList.add(musicList);
				dataList.add(artists);
				dataList.add(albumlist);
				dataList.add(packlist);
				for(int start=0;start<dataList.size();start++){
					JSONArray jsonArray = dataList.get(start);
					if (jsonArray != null && jsonArray.length() > 0) {
						dataLength += jsonArray.length();
					}
					Log.i("PersentHandler", "dataLength=" + dataLength);
				}
				
				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 1);
				
				if (artists != null && artists.length() > 0) {
					for (int j = 0; j < artists.length(); j++, processData++) { // 获得演出者列表
						JSONObject jsonobj = artists.getJSONObject(j);
						artist = new Artist();
						artist.setId(jsonobj.getLong("id"));
						artist.setName(jsonobj.getString("name"));
						if (artist.getName() == null || artist.getName() == "") {
							artist.setName("未知演出者");
						}
						artist.setImgUrl(jsonobj.getString("smallimg"));
						artist.setFirstChar(jsonobj.getString("firstchar"));
						new ArtistDao().insertArtist(artist);
					}
					
					Log.i("PersentHandler", "artists finish, artists.length()=" + artists.length());
				}

				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 1);
				
				if (albumlist != null && albumlist.length() > 0) {
					ProductArtist pa = null;
					for (int k = 0; k < albumlist.length(); k++,processData++) {
						JSONObject jsonobj = albumlist.getJSONObject(k);
						album = new Album();
						album.setId(jsonobj.getLong("id"));
						album.setName(jsonobj.getString("albumname"));
						album.setImgUrl(jsonobj.getString("albumcovers"));
						album.setBuytime(jsonobj.getString("buytime"));
						album.setIsCloud(jsonobj.getInt("showstate") == 0 ? 5 : 0);
						if (album.getBuytime() == null || album.getBuytime() == "" || album.getBuytime().equals("null")) {
							album.setBuytime("0");
						}
						String[] artistli = jsonobj.getString("artistids") != null ? jsonobj.getString("artistids").split(",") : null;

						if (artistli != null && artistli.length > 0) {
							for (String str : artistli) { // 关联专辑下的演出者
								pa = new ProductArtist();
								pa.setArtistId(Long.parseLong(str));
								pa.setProductId(album.getId());
								new ProductArtistDao().insertProductArtist(pa);
							}
						}
						// / 这里根据不同的情况查找碟 ，最后保存专辑
						new AlbumDao().insertAlbum(album);

					}
					Log.i("PersentHandler", "albumlist finish, albumlist.length()=" + albumlist.length());
				}
				
				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 1);

				if (musicList != null && musicList.length() > 0) {
					int handlerTotal = 0;// 发送handlerTotal次数
					ProductArtist pa1 = null;

					for (int m = 0; m < musicList.length(); m++,processData++) {
						JSONObject jsonmusic = musicList.getJSONObject(m);
						music = new Music();
						music.setId(jsonmusic.getLong("id"));
						music.setName(jsonmusic.getString("musicname"));
						music.setMediaurl(jsonmusic.getString("mediaurl"));
						music.setDiskId(jsonmusic.getLong("disk_id"));
						music.setDisk_no(jsonmusic.getString("serial_number"));
						music.setFile_size("");
						music.setLibid(jsonmusic.getLong("libraryid"));
						music.setFirstChar(jsonmusic.getString("firstchar"));
						music.setPlay_time(jsonmusic.getString("playtime"));
						music.setTrack_no(jsonmusic.getString("track_no"));
						music.setBuytime(jsonmusic.getString("buytime"));
						music.setAlbumId(jsonmusic.getString("albumid"));
						music.setDiskName(jsonmusic.getString("diskname"));
						music.setIscloud(jsonmusic.getInt("showstate") == 0 ? 5 : 0);
						String artistidss = "";
						if (!jsonmusic.isNull("artistid")) {
							artistidss = jsonmusic.getString("artistid");
						} else {
							new ProductArtistDao().inserProudctArtistDefault(music.getId());
						}

						if (artistidss != null && artistidss != "") {
							String[] str = artistidss.split(",");
							if (str != null && str.length > 0) {
								for (int q = 0; q < str.length; q++) {
									pa1 = new ProductArtist();
									pa1.setArtistId(Long.parseLong(str[q]));
									pa1.setProductId(music.getId());
									new ProductArtistDao().insertProductArtist(pa1);
								}
							}
						} else {
							new ProductArtistDao().inserProudctArtistDefault(music.getId());
						}
						new MusicDao().insertMusic(music);

//						countProgress(handler, processData, dataLength);
						if( m % 13 == 0){
							SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 1);
						}
					}
					Log.i("PersentHandler", "musicList finish, musicList.length()=" + musicList.length());
				}

				if (packlist != null && packlist.length() > 0) {
					String[] musicids = null;
					for (int n = 0; n < packlist.length(); n++) {
						JSONObject jsonpack = packlist.getJSONObject(n);
						pack = new Pack();
						pack.setId(jsonpack.getLong("id"));
						pack.setName(jsonpack.getString("name"));
						pack.setImgurl(jsonpack.getString("bigimg"));
						pack.setBuytime(jsonpack.getString("buytime"));
						pack.setIsCloud(jsonpack.getInt("showstate") == 0 ? 5 : 0);
						// pack.setLibraryid(jsonpack.getLong("libraryid"));
						pack.setLibraryid(0L);// 同步盒子没有没有库id，默认为0
						musicids = jsonpack.getString("musicids") != null ? jsonpack.getString("musicids").split(",") : null;
						pack.setMusicids(musicids);
						new PackDao().insertPack(pack);
					}
					Log.i("PersentHandler", "packlist finish, packlist.length()=" + packlist.length());
				}
				
				SyncAsyncTask.countProgress(SyncAsyncTask.SYN_TYPE_GetAllUnSyn, 1);
			}

		}
		return true;

	}

/*	private void countProgress(Handler progressHandler, int processData, int dataLength){
		Message msg = progressHandler.obtainMessage();
		msg.what = LoginActivity.UPGRADE_PROGRESS;
		msg.arg1 = processData*100/dataLength;
//		progressHandler.sendMessage(msg);
	}*/
	
	/**
	 * 通知盒子更新云信息
	 */
	public void notifyBoxUpdateCloud(String ids, final int typeUri) {

		if (UpnpApp.upnpService != null && UpnpApp.cacheControlService != null) {
			
			ActionInvocation ai = new ActionInvocation(UpnpApp.cacheControlService.getAction("Syncloud"));

			if (typeUri == 1) {
				ai.setInput("CacheURI", "cache://album?id=" + ids);
			} else if (typeUri == 5) {
				ai.setInput("CacheURI", "cache://music?ids=" + ids);
			} else if (typeUri == 15) {
				ai.setInput("CacheURI", "cache://theme?id=" + ids);
			}

			UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
					if (WatchDog.activity != null) {
						WatchDog.activity.finish();
					}
				}

				@Override
				public void success(ActionInvocation arg0) {
					// getCloudStates(typestate);
					if (WatchDog.activity != null) {
						WatchDog.activity.finish();// WatchDog.activity=,WatchDog.activity
					}
//					UpnpApp.showToastMessage("云数据同步成功！");
					/**屏蔽后台数据同步消息显示 eshine 20140106
					 * UpnpApp.mainHandler.showInfo(R.string.cloud_syn_success_info);*/
					Log.i(TAG, "云数据同步成功！");
				}

			});
		}
	}

	/**
	 * 封装失败处理信息
	 */
	public void upnpFailureHandle() {
		Looper.prepare();
//		CustomToast.makeText(UpnpApp.context, "通信异常", 1000).show();
		UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
		// Intent i = new Intent(UpnpApp.context, LoginActivity.class);
		// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// UpnpApp.context.startActivity(i);
		Looper.loop();

	}

	public void getBoxVersion(final LoginActivity loginActivity) {
		Log.e("软件升级", TAG+"getBoxVersion()");
		ActionInvocation ai = null;
		
		try {
			ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetVersion"));
		} catch (Exception e) {	
			Log.e("0221", TAG+"getBoxVersion():e="+e);	
			e.printStackTrace();
			
			//对话框提示用户升级盒子
			loginActivity.mHandler.sendEmptyMessage(LoginActivity.BOX_VERSION_LOW);
			
			return;
		}

		UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai) {

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				Log.e("软件升级", TAG+"getBoxVersion()>>failure=" + WatchDog.upnpActionPerformCount);
				WatchDog.upnpActionPerformCount++;
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (WatchDog.upnpActionPerformCount < 3) {
					getBoxVersion(loginActivity);
				} else {
					Looper.prepare();
					UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
					Looper.loop();

					WatchDog.upnpActionPerformCount = 0;
				}
			}

			// 在success中会触发LoginActivity开始同步或显示“盒子版本低”
			@Override
			public void success(ActionInvocation arg0) {
				Log.e("0221", TAG+"getBoxVersion()>>success");
				String json=(String) arg0.getOutput("Json").getValue();
				Log.e("0221", TAG+"getBoxVersion():json="+json);
				new JsonUtil().getBoxVersion(json,loginActivity);
			}

		});
	}

}
