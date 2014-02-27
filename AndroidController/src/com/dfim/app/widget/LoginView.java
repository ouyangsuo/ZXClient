package com.dfim.app.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.common.UpnpApp;
import com.dfim.app.events.Event;
import com.dfim.app.events.EventListener;
import com.dfim.app.models.StateModel;
import com.dfim.app.util.AnimUtil;
import com.union.cellremote.R;
import com.union.cellremote.adapter.LoginAdapter;


/*
 * mvc -> View
 */
public class LoginView extends LinearLayout{

	private static final boolean DEBUG = true;
	private static final String TAG = LoginView.class.getSimpleName();

    public static final int MESSAGE_CHOSSE_DEVICE = 0;
    public static final int MESSAGE_CONNECT = 1;
    public static final int MESSAGE_START_SYNC = 2;
    public static final int MESSAGE_RECONNECT = 3;
    public static final int MESSAGE_CANCEL = 4;


	private LinearLayout viewStub;
	private LinearLayout llBtn;
	private LinearLayout llGradientCover;
	private Button loginBtn;
	private TextView loginHintTv;
	private TextView tvDeviderLine;
	private LayoutInflater inflater;
	private StateModel model;
	private LoginAdapter adapter;
	private SyncView syncView;
	private View footerView;
	
	private LoginViewListener listener;
	public void setListener(LoginViewListener listener){
		this.listener = listener;
	}
	
	public interface LoginViewListener{//new LoginView.LoginViewListener()
        void onViewChange(int message, Object data);
	}
	
	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		model = StateModel.getInstance();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		viewStub = (LinearLayout)findViewById(R.id.login_viewstub);
		llGradientCover = (LinearLayout)findViewById(R.id.ll_gradient_cover);
		llBtn = (LinearLayout)findViewById(R.id.ll_btn);
		loginBtn = (Button)findViewById(R.id.login_btn);
		loginHintTv = (TextView)findViewById(R.id.tv_login_hint);
		tvDeviderLine = (TextView)findViewById(R.id.tv_divider_line);
		model.addListener(StateModel.ChangeEvent.STATE_CHANGED, stateChangeListener);
	}

	/*
	 * received event from model
	 */
	private EventListener stateChangeListener = new EventListener() {
		
		@Override
		public void onEvent(Event event) {
			changeState();
		}
	};
	
	
	protected void changeState() {
		int state = model.getState();
		switch(state){
			case StateModel.STATE_LOADING:
				Log.i(TAG, "changeState-StateModel.STATE_LOADING");
				setLoadingViews();
				break;
			case StateModel.STATE_CHOOSE:
				Log.i(TAG, "changeState-StateModel.STATE_CHOOSE");
				setChooseViews();
				break;
			case StateModel.STATE_SYNC:
				Log.i(TAG, "changeState-StateModel.STATE_SYNC");
				setSyncViews();
				break;
			case StateModel.STATE_ERROR:
				Log.i(TAG, "changeState-StateModel.STATE_ERROR");
				setErrorViews();
				break;
			case StateModel.STATE_BOX_VERSION_LOW:
				Log.i(TAG, "changeState-StateModel.STATE_BOX_VERSION_LOW");
				setBoxVersionLowViews();
				break;
			default:
				//TODO
				Log.i(TAG, "changeState-default");
		}
	}

	private void setBoxVersionLowViews() {
		tvDeviderLine.setVisibility(View.GONE);
		llGradientCover.setVisibility(View.GONE);
		setHint(null, View.INVISIBLE);
		
		llBtn.setVisibility(View.VISIBLE);
		loginBtn.setVisibility(View.VISIBLE);
		loginBtn.setText(R.string.login_retry);
	
		loginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                listener.onViewChange(MESSAGE_RECONNECT, null);
			}
		});
		
		LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.layout_login_boxversion_low, null);
		ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		viewStub.removeAllViews();
		viewStub.addView(ll);
	}

	private void setErrorViews() {
		tvDeviderLine.setVisibility(View.GONE);
		llGradientCover.setVisibility(View.GONE);
		loginBtn.setVisibility(View.VISIBLE);
		loginBtn.setText(R.string.login_reconnect);
		loginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                listener.onViewChange(MESSAGE_RECONNECT, null);
			}
		});
		setHint(null, View.INVISIBLE);
		LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.layout_login_error, null);
		ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		viewStub.removeAllViews();
		viewStub.addView(ll);
	}

	private void setSyncViews() {
		Context context = UpnpApp.getContext();
		tvDeviderLine.setVisibility(View.VISIBLE);
		loginBtn.setVisibility(View.INVISIBLE);
		setHint(context.getString(R.string.login_hint_synchronizing), View.VISIBLE);
		syncView = (SyncView) inflater.inflate(R.layout.layout_login_sync, null);
		
		viewStub.removeAllViews();
		viewStub.addView(syncView);
        listener.onViewChange(MESSAGE_START_SYNC, syncView);
	}

	private void setChooseViews() {
		Context context = UpnpApp.getContext();
		tvDeviderLine.setVisibility(View.VISIBLE);
		loginBtn.setVisibility(View.VISIBLE);
		loginBtn.setText(context.getString(R.string.connect));
		setHint(context.getString(R.string.login_hint_choose), View.VISIBLE);
		ListView deviceList = (ListView) inflater.inflate(R.layout.layout_login_listview, null);
		
//		View v=initHeaderView(context);
//		deviceList.addHeaderView(v);
		initFooterView(context);
		deviceList.addFooterView(footerView);
		
		deviceList.setAdapter(adapter);
		deviceList.setOnItemClickListener(onItemClickListener);
		viewStub.removeAllViews();
		viewStub.addView(deviceList);
		llBtn.setVisibility(View.GONE);
		llGradientCover.setVisibility(View.VISIBLE);
		
//		loginBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(listener != null){
//                    listener.onViewChange(MESSAGE_CONNECT, null);
//				}
//			}
//		});
	}

//	private View initHeaderView(Context context) {
//		ImageView iv=new ImageView(context);
//		iv.setLayoutParams(new android.widget.AbsListView.LayoutParams(Constant.SCREEN_WIDTH, 2) );	
//		iv.setBackgroundColor(context.getResources().getColor(R.color.login_hint_textcolor));
//		
//		return iv;
//	}

	private void initFooterView(Context context) {
		footerView = LayoutInflater.from(context).inflate(R.layout.device_list_footerview, null);
		ImageView ivAnim=(ImageView) footerView.findViewById(R.id.iv_loading_more);	
		new AnimUtil(context).initAnim(ivAnim,R.anim.login_seraching_anim);
	}

	private void setLoadingViews() {
		setHint(null,View.INVISIBLE);
		tvDeviderLine.setVisibility(View.GONE);
		llGradientCover.setVisibility(View.GONE);
		
		loginBtn.setVisibility(View.VISIBLE);
		loginBtn.setText(R.string.cancel);
		loginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                listener.onViewChange(MESSAGE_CANCEL, null);
			}
		});
		View loadingView = inflater.inflate(R.layout.layout_login_loading, null);
		loadingView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		viewStub.removeAllViews();
		viewStub.addView(loadingView);
	}

	public void setAdapter(LoginAdapter adapter) {
		this.adapter = adapter;
	}
	
	/**
	 * set login hint textview
	 * @param text 
	 * @param visible
	 */
	public void setHint(String text,int visible){
		if(loginHintTv != null){
			loginHintTv.setVisibility(visible);
			loginHintTv.setText(text);
		}
	}
	
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parten, View view, int position,
				long id) {
            listener.onViewChange(MESSAGE_CHOSSE_DEVICE, String.valueOf(position));
            listener.onViewChange(MESSAGE_CONNECT, null);
		}
	};
	
	public void destroy(){
		model.removeListener(StateModel.ChangeEvent.STATE_CHANGED, stateChangeListener);
	}
}
