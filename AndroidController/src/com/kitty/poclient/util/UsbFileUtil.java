package com.kitty.poclient.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.item.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.kitty.poclient.common.UIHelper;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.domain.Music;
import com.kitty.poclient.fragment.usb.ExternalDeviceFragment;

public class UsbFileUtil implements Comparator<File>{
	
	private static String TAG = "UsbFileUtil";
	
	@Override
	public int compare(File lhs, File rhs) {
		// TODO Auto-generated method stub
		int flag=0;
		try{
			int lhstype = 0;
			int rhstype = 0;
			if(lhs.isFile()){
				String lhsname = lhs.getName().toLowerCase();
				if(lhsname.endsWith(".wma")){
					lhstype = 1;
				}else if(lhsname.endsWith(".aac")){
					lhstype = 2;
				}else if(lhsname.endsWith(".mp3")){
					lhstype = 3;
				}else if(lhsname.endsWith(".ape")){
					lhstype = 4;
				}else if(lhsname.endsWith(".wav")){
					lhstype = 5;
				}else if(lhsname.endsWith(".flac")){
					lhstype = 6;
				}else if(lhsname.endsWith(".cue")){
					lhstype = 7;
				}else if(lhsname.endsWith(".mp4")){
					lhstype = 8;
				}else if(lhsname.endsWith(".wov")){
					lhstype = 9;
				}else if(lhsname.endsWith(".mov")){
					lhstype = 10;
				}else if(lhsname.endsWith(".mkv")){
					lhstype = 11;
				}else if(rhs.getName().endsWith(".mpg")){
					lhstype = 12;
				}else if(rhs.getName().endsWith(".wmv")){
					lhstype = 13;
				}
			}
			if(rhs.isFile()){
				String rhsname = rhs.getName().toLowerCase();
				if(rhsname.endsWith(".wma")){
					rhstype = 1;
				}else if(rhsname.endsWith(".aac")){
					rhstype = 2;
				}else if(rhsname.endsWith(".mp3")){
					rhstype = 3;
				}else if(rhsname.endsWith(".ape")){
					rhstype = 4;
				}else if(rhsname.endsWith(".wav")){
					rhstype = 5;
				}else if(rhsname.endsWith(".flac")){
					rhstype = 6;
				}else if(rhsname.endsWith(".cue")){
					rhstype = 7;
				}else if(rhsname.endsWith(".mp4")){
					rhstype = 8;
				}else if(rhsname.endsWith(".wov")){
					rhstype = 9;
				}else if(rhsname.endsWith(".mov")){
					rhstype = 10;
				}else if(rhsname.endsWith(".mkv")){
					rhstype = 11;
				}else if(rhsname.endsWith(".mpg")){
					rhstype = 12;
				}else if(rhs.getName().endsWith(".wmv")){
					rhstype = 13;
				}
			}			
			if(lhstype!=rhstype){
				flag = lhstype - rhstype;
			}else{
				if(lhstype==0){
					String lh = "";
					String rs = "";
					if(lhs.getName().lastIndexOf(".")>=0){
						lh = lhs.getName().substring(0, lhs.getName().lastIndexOf(".")).toLowerCase();
					}else{
						lh = lhs.getName().toLowerCase();
					}
					if(rhs.getName().lastIndexOf(".")>=0){
						rs = rhs.getName().substring(0, rhs.getName().lastIndexOf(".")).toLowerCase();
					}else{
						rs = rhs.getName().toLowerCase();
					}
					flag=lh.compareTo(rs);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}		
		return flag;
	}
	
	public static boolean isCue(String filename){
		return filename.toLowerCase().endsWith(".cue");
	}
	
	public static boolean isCueChild(String filename){
		filename = filename.toLowerCase();
		if(filename.indexOf(".cue&title")!=-1){
			return true;
		}
		return false;
	}
	public static boolean isCueChildFromMediaObjectID(String objectID){
		boolean flag = false;
		if(objectID.indexOf("cue&title")!=-1){
			flag = true;
		}
		return flag;
	}
	public static String getCueChildName(String filename){
		return filename.split("=")[1].split("&")[0];
	}
	
	public static boolean isMusic(String filename){
		filename = filename.toLowerCase();
		if(filename.endsWith(".aac")|filename.endsWith(".wma")||filename.endsWith(".mp3")||filename.endsWith(".ape")||filename.endsWith(".wav")||filename.endsWith(".flac")){
			return true;
		}
		return false;
	}
	
	public static boolean isVideo(String filename){
		filename = filename.toLowerCase();
		if(filename.endsWith(".wmv")||filename.endsWith(".mp4")||filename.endsWith(".wov")||filename.endsWith(".mov")||filename.endsWith(".mpg")||filename.endsWith(".mkv")){
			return true;
		}
		return false;
	}
	
	public static String getFileNameFromAbsolutePath(String absolutePath){
		String fileName = "";
		if(absolutePath!=null){
			String s[] = absolutePath.split("/");
			fileName = s[s.length-1];
		}
		return fileName;
	}
	
	public static boolean isUsbRootDir(String dir){
		boolean flag = false;
		
		if(dir!=null){
			String path = dir.trim().toLowerCase();
			if(dir.equals(ExternalDeviceFragment.FIRST_DIR) || dir.equals(ExternalDeviceFragment.ROOT_DIR)){
				flag = true;
			}
		}
	
		return flag;
	}
	
	public static String getParentPathFromAbsolutePath(String currentPath){
		String parentPath = null;
		String s[] = currentPath.split("/");
		String currentDirectoryName = s[s.length-1];
		parentPath = currentPath.substring(0, currentPath.length()-currentDirectoryName.length()-1);
		return parentPath;
	}
	
	public static void updateMusicListFromDIDLContent(DIDLContent didl){
//		Log.i(TAG,"updateMusicListFromUdisk:received, item size:" + didl.getItems().size());
		ArrayList<Music> musicList = new ArrayList<Music>();
		List<Item> fileList = didl.getItems(); //文件列表
		int indexOfMusicList = 0;
		for(int i=0;i<fileList.size();i++){
			Log.i(TAG,"updateMusicListFromSource,曲目名称：" + fileList.get(i).getTitle());
			if(UsbFileUtil.isMusic(fileList.get(i).getId()) 
					|| UsbFileUtil.isCueChildFromMediaObjectID(fileList.get(i).getId())){
				String uri = null;
				if(UsbFileUtil.isMusic(fileList.get(i).getId())){//usb
					uri = "xxbox://usb?source=" + fileList.get(i).getId();
				}else{//cue child
					uri = fileList.get(i).getId();
				}
//				indexOfMusicList ++; 
				Music music = new Music();
				music.setName(fileList.get(i).getTitle()); //曲目名称
				music.setId((long)indexOfMusicList);
				music.setUri(uri); //
				music.setArtistId("0");
				music.setArtistName("");
				music.setTrack_no(null);
				music.setImgUrl("");
				if(music.getUri().equals(WatchDog.currentUri)){
					WatchDog.currentPlayingMusic = music;				
					WatchDog.currentPlayingName = music.getName();
					WatchDog.currentArtistName = music.getArtistName();				
					WatchDog.currentPlayingIndex=indexOfMusicList;		
					WatchDog.currentPlayingId=music.getId();
					Log.i(TAG,"URIParams:WatchDog.currentPlayingName="+WatchDog.currentPlayingName);
				}
				musicList.add(music);
				indexOfMusicList ++;
			}else {
				//TODO
				Log.i(TAG,"!!!!!!!!!!!!!un support file type!!!!!!!!!!!!!!!!!!fileList.get(i).getId():" + fileList.get(i).getId());
			}
		}
//		Log.i(TAG,"WatchDog.currentList.size" + WatchDog.currentList.size());
		WatchDog.currentList = musicList;
		
		//通知列表更新
		UIHelper.refreshLocalSinglesView();
	}
	
	public static boolean isUsbMediaURI(String uri){
		boolean flag = false;
		if(uri !=null && !uri.equals("")){
			if(isUsbMediaURI_UsbType(uri) || isUsbMediaURI_CueType(uri)){
				flag = true;
			}
		}
		return flag;
	}
	public static boolean isUsbMediaURI_UsbType(String uri){
		boolean flag = false;
		if(uri !=null && !uri.equals("")){
			if(uri.indexOf("xxbox://usb?source=/mnt/usb_storage")!=-1){
				flag = true;
			}
		}
		return flag;
	}
	public static boolean isUsbMediaURI_CueType(String uri){
		boolean flag = false;
		if(uri !=null && !uri.equals("")){
			if(uri.indexOf("xxbox://cue?source=/mnt/usb_storage")!=-1){
				flag = true;
			}
		}
		return flag;
	}
	public static void GetNowPlaylist(){
		 ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetNowPlaylist"));
		 //TODO 分页获取
		 String params = "{}";//"{\"startItem \":START,\"itemNums\":ITEMNUMS}";
 		 ai.setInput("Json",params);
 		 UpnpApp.upnpService.getControlPoint().execute(new ActionCallback(ai){
 			@Override
 			public void failure(ActionInvocation arg0, UpnpResponse arg1,
 					String arg2) {
 				Log.i(TAG, "error in GetNowPlaylist");
 			}
 			@Override
 			public void success(ActionInvocation arg0) {
 				Log.i(TAG, "success in GetNowPlaylist");
 				ArrayList<Music> musicList = new ArrayList<Music>();
 				String jsonString = (String) arg0.getOutput("Json").getValue();
 				try {
 					JSONObject jsonObject = new JSONObject(jsonString);
 					String type = jsonObject.getString("type");
 					String title = jsonObject.getString("title");
 					Log.i(TAG, "type=" + type);
 					Log.i(TAG, "title=" + title);
 					JSONArray songs = jsonObject.getJSONArray("songs");
 					for(int i=0;i<songs.length();i++){
 						JSONObject song = songs.getJSONObject(i);
 						String playtime = song.getString("playtime");
 						String screamurl = song.getString("screamurl");
 						String actorname = song.getString("actorname");
 						String name = song.getString("name");
 						String imageUrl = song.getString("image");
 	 					Log.i(TAG, "playtime=" + playtime);
 	 					Log.i(TAG, "screamurl=" + screamurl);
 	 					Log.i(TAG, "actorname=" + actorname);
 	 					Log.i(TAG, "name=" + name);
 	 					Music music = new Music();
 	 					music.setName(name); //曲目名称
 	 					music.setId((long)i);
 	 					music.setUri(screamurl); 
 	 					music.setArtistName(actorname);
 	 					music.setArtistId("0");
 	 					music.setTrack_no(null);
 	 					music.setImgUrl(imageUrl);/////播放器 - 图片
 	 					if(music.getUri().equals(ExternalDeviceFragment.currentUri)){
 	 						ExternalDeviceFragment.currentPlayingMusic = music;				
 	 						ExternalDeviceFragment.currentPlayingName = music.getName();
 	 						ExternalDeviceFragment.currentArtistName = music.getArtistName();				
 	 						ExternalDeviceFragment.currentPlayingIndex=i;		
 	 						ExternalDeviceFragment.currentPlayingId=music.getId();
 	 						
 	 						WatchDog.currentPlayingMusic = music;				
 	 						WatchDog.currentPlayingName = music.getName();
 	 						WatchDog.currentArtistName = music.getArtistName();				
 	 						WatchDog.currentPlayingIndex= i;		
 	 						WatchDog.currentPlayingId=music.getId();

 	 						//通知播放器更新
 	 						Intent intent=new Intent("updateMediaInfo");
 	 						intent.putExtra("is_usb_music", true);
 	 						intent.putExtra("music_uri", music.getUri());
 	 						intent.putExtra("music_name", music.getName());
 	 						intent.putExtra("artist_name", music.getArtistName());
 	 						intent.putExtra("image_url", music.getImgUrl());
 	 						UpnpApp.context.sendBroadcast(intent);
 	 						Log.i(TAG,"ExternalDeviceFragment.currentPlayingName="+ExternalDeviceFragment.currentPlayingName);
 	 					}
 	 					musicList.add(music);
 					}
 					Log.i(TAG, "musicList.size():" + musicList.size());
 					
 					ExternalDeviceFragment.currentList = musicList;
 					WatchDog.currentList = musicList;
 					
 					
 					Log.i(TAG, "UsbFragment.currentList.size():" + ExternalDeviceFragment.currentList.size());
 					
				} catch (JSONException e) {
					e.printStackTrace();
					Log.i(TAG, "error in read result:" + e.getMessage());
				}
 			}
 		});
	}
}
