package com.dfim.app.common;

import java.util.List;

import org.fourthline.cling.model.action.ActionInvocation;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UpnpHelper {
	private final static String TAG = UpnpHelper.class.getSimpleName();
	
	public static final int GetPageUnSyn_PAGE_NUM = 100;
	
	/**
	 * 
	 * @param controlKeyValue 物理地址
	 * @param jsonValue		       数据(详见<真现智能控制器UPnP接口定义.doc>)
	 * @return ActionInvocation for "GetPageUnSyn"
	 */
	@SuppressWarnings("rawtypes")
	public static ActionInvocation generateAction_GetPageUnSyn(String controlKeyValue, String jsonValue){
		
		@SuppressWarnings("unchecked")
		ActionInvocation ai = new ActionInvocation(UpnpApp.boxControlService.getAction("GetPageUnSyn"));
		ai.setInput("Controlkey", controlKeyValue);
		ai.setInput("Json", jsonValue);
		
		return ai;
	}
	
	public static String[] getKeys_GetPageUnSyn(){
		String[] keys = {"libarayid", "max"};
		return keys;
	}
	
	public static JSONObject generateJSONObject(String[] keys, List<Object> values){
		JSONObject jsonObject = new JSONObject();
		try {
			for(int i = 0; i < keys.length; i++){
				jsonObject.put(keys[i], values.get(i));
			}
		} catch (JSONException e) {
			Log.e(TAG, "error in generateJSONObject:" + e.getMessage());
			e.printStackTrace();
		}	
		return jsonObject;
	}
}
