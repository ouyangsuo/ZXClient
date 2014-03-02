package com.kitty.poclient.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

import com.kitty.poclient.activity.LoginActivity;

public class ExitApplication extends Application {
	private static List<Activity> activityList = new LinkedList();
	private static ExitApplication instance;

	private ExitApplication() {
	}

	public static ExitApplication getInstance() {
		if (null == instance) {
			instance = new ExitApplication();
		}
		return instance;

	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public List<Activity> getActivityList() {
		return activityList;
	}
	
//	public Activity getTabWebActivity() {
//		for (Activity activity : activityList) {
//			if (activity instanceof TabWebActivity) {
//				return activity;
//			}
//		}
//		return null;
//	}
	
	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		System.exit(0);
	}

	public static Activity getLoginActivity() {
		for (Activity activity : activityList) {
			if (activity instanceof LoginActivity) {
				return activity;
			}
		}
		return null;
	}

}
