package com.union.cellremote.store;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.union.cellremote.R;

public class ComingSoonFragment extends Fragment{

	private View view;
	private Context context;
//	private TextView tvTabname;
//	private ImageButton btnPlayer;	
//	private String tabname;
	
	public ComingSoonFragment() {
		
	}
	
    public ComingSoonFragment(Context context) {
    	this.context=context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	view=LayoutInflater.from(context).inflate(R.layout.coming_soon_fragment, null);
//    	initComponent();
//    	initBtnPlayer();
//    	 registerReceivers();
    	return view;
	}

	private void initComponent() {

	}
	
	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}	

	private void registerReceivers() {
	}

	private void unregisterReceivers() {
	}
	
}
