package com.dfim.app.common;

import com.dfim.app.interfaces.SelfReloader;
import com.union.cellremote.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class ViewFactory {
	
	public View createLoadFailureView(final SelfReloader selfReloader){
		View loadFailureView = LayoutInflater.from(UpnpApp.context).inflate(R.layout.load_failure_layout, null);
		loadFailureView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		loadFailureView.findViewById(R.id.btn_reload).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selfReloader.reload();
			}
		});
		
		return loadFailureView;
	}
	
}
