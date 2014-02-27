package com.dfim.app.fragment.store;

//import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.ViewFactory;
import com.dfim.app.common.WatchDog;
import com.dfim.app.fragment.TabWebFragment.TitlebarUpdateFragment;
import com.dfim.app.interfaces.SelfReloader;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.JsonUtil;
import com.union.cellremote.R;
import com.union.cellremote.http.HttpGetter;

public class PurseFragment extends Fragment implements TitlebarUpdateFragment,SelfReloader {
	// Looper.prepare

	private final String TAG = "PurseFragment";
	private Context context;

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llContent;
	private LinearLayout llNoData;
	private TextView tvBalance;
	private EditText etAmount;
	private Button btnRecharge;

	private AnimationDrawable ad;
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	private int firstVisibleItemPosition = -1;// 记录停止卷动时第一个ITEM的序号
	private int scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量

	private double balance = -1;

	private final int MSG_LETS_GET_DATA = 0;
	private final int MSG_DATA_GOT = 1;
	private final int MSG_DATA_LOAD_FAILD = 2;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LETS_GET_DATA:
				getData();
				break;

			case MSG_DATA_GOT:
				updateUI();
				break;
				
			case MSG_DATA_LOAD_FAILD:
				uiShowNoData();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void getData() {
		Pools.executorService1.submit(new Runnable() {

			@Override
			public void run() {
				String json = new HttpGetter(context).getBalance();
				balance = new JsonUtil().getBalance(json);

				if (balance != -1) {
					handler.sendEmptyMessage(MSG_DATA_GOT);
				}
			}
		});
	}

	protected void updateUI() {
		if (loadingRunning == true) {
			endLoading();
		}

		tvBalance.setText(balance + "0");
	}

	public PurseFragment() {

	}

	public PurseFragment(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		parentActivityChangeButton();
//		parentActivityChangeTitle();
		updateTitlebar();
		view = inflater.inflate(R.layout.purse_fragment, null);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		initComponents();
		startLoading();
		getDataWhenActive();
//		registerReceivers();// 暂时啥也没有

		return view;
	}

	private void getDataWhenActive() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (fragmentIsActive == false) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(MSG_LETS_GET_DATA);
			}
		}).start();
	}

	private void startLoading() {
		Log.e(TAG, "startLoading()");
		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);

		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();

		loadingRunning = true;
	}

	protected void endLoading() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}

		llContent.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);

		loadingRunning = false;
	}
	
	public void uiShowNoData() {
		System.out.println(TAG+"showNoData");
		stopLoadingAnimation();

		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);

		View loadFailureView=new ViewFactory().createLoadFailureView(this);
		llNoData.removeAllViews();
		llNoData.addView(loadFailureView);
	}
	
	private void stopLoadingAnimation() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}
		loadingRunning = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		// listViewGetFormerPosition();
		WatchDog.currentSelfReloader = this;
		fragmentIsActive = true;
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("我的钱包");
		TabWebActivity.currentMenuItem = "我的钱包";
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton() {
		TabWebActivity.changeButton("btnMenu");
	}

	private void initComponents() {
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);

		tvBalance = (TextView) view.findViewById(R.id.tv_balance);
		etAmount = (EditText) view.findViewById(R.id.et_amount);
		btnRecharge = (Button) view.findViewById(R.id.btn_recharge);

		etAmount.setHint("即将开放");
		etAmount.setEnabled(false);
		btnRecharge.setEnabled(false);
	}

	@Override
	public void onDetach() {
//		unregisterReceivers();
		super.onDetach();
	}

	private void registerReceivers() {

	}

	private void unregisterReceivers() {

	}

	@Override
	public void updateTitlebar() {
		WatchDog.tabWebFragment.setPopbackable(false);
		WatchDog.tabWebFragment.setTitle("我的钱包");
	}
	
	@Override
	public void reload() {
		System.out.println(TAG+"reloading...");
		startLoading();
		getDataWhenActive();
	}

	@Override
	public void onDataLoadFailed() {
		handler.sendEmptyMessage(MSG_DATA_LOAD_FAILD);
	}

}
