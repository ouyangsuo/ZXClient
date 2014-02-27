package com.dfim.app.util;

import java.util.Arrays;

import android.util.Log;


public class VersionNumberComparator {
	
	private static final String TAG=VersionNumberComparator.class.getSimpleName()+":";

	/**
	 * @author Administrator
	 * @return true if the first one is bigger
	 * */
	public boolean compare(String version1, String version2) {
		String[] version1Array = version1.split("\\.");
		String[] version2Array = version2.split("\\.");
		Log.e(TAG,"version1Array="+Arrays.toString(version1Array));
		Log.e(TAG,"version2Array="+Arrays.toString(version2Array));
		
		if(!validate(version1Array) || !validate(version2Array)){
			throw new IllegalArgumentException("make sure your version strings to be compared is consist of numbers and dot");
		}

		if (Integer.valueOf(version1Array[0]) != Integer.valueOf(version2Array[0])) {
			return Integer.valueOf(version1Array[0]) > Integer.valueOf(version2Array[0]);
		}

		else if (Integer.valueOf(version1Array[1]) != Integer.valueOf(version2Array[1])) {
			return Integer.valueOf(version1Array[1]) > Integer.valueOf(version2Array[1]);
		}

		else if (Integer.valueOf(version1Array[2]) != Integer.valueOf(version2Array[2])) {
			return Integer.valueOf(version1Array[2]) > Integer.valueOf(version2Array[2]);
		}               

		return false;
	}

	private boolean validate(String[] array) {
		for(int i=0;i<array.length;i++){
			if(!array[i].matches("[\\d]+")){
				return false;
			}
		}
		return true;
	}

}
