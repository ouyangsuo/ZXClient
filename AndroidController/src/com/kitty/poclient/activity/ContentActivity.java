package com.kitty.poclient.activity;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.kitty.poclient.R;
import com.kitty.poclient.common.UpnpApp;
import com.kitty.poclient.common.WatchDog;
import com.kitty.poclient.util.ExitApplication;

public class ContentActivity extends Activity{
	
	private ListView lvContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 ExitApplication.getInstance().addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        getWindow().getDecorView().setSystemUiVisibility(4);
        
		WatchDog.currentActivities.add(this);
		setContentView(R.layout.content);
		initComponents();
		initData();
		initListeners();
	}
	
	private void initComponents(){
		lvContent=(ListView) findViewById(R.id.lv_content);
	}
	
	private void initData(){
		UpnpApp.upnpService.getControlPoint().execute(new Browse(UpnpApp.directoryService, "", BrowseFlag.DIRECT_CHILDREN) {
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				System.out.println("browse failure");
				initData();
			}
			
			@Override
			public void updateStatus(Status arg0) {
				System.out.println("browse updateStatus");
			}
			
			@Override
			public void received(ActionInvocation arg0, DIDLContent arg1) {
				System.out.println("browse received="+arg1);
			}
		});
	}

	private void initListeners(){
		lvContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long id) {
				
			}
			
		});
	}
}
