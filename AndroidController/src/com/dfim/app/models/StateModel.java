package com.dfim.app.models;

import com.dfim.app.events.EventDispatcher;
import com.dfim.app.events.SimpleEvent;

public class StateModel extends EventDispatcher{
	public static final int STATE_LOADING = 0;
	public static final int STATE_CHOOSE = 1;
	public static final int STATE_SYNC = 2;
	public static final int STATE_ERROR = 3;
	public static final int STATE_BOX_VERSION_LOW = 4;
	
	public static class ChangeEvent extends SimpleEvent{
		public static final String STATE_CHANGED = "stateChanged";

		public ChangeEvent(String type) {
			super(type);
		}
	}
	
	private static StateModel instance;
	
	private StateModel(){
		super();
	}
	
	public static StateModel getInstance(){
		if(instance == null) instance = new StateModel();
		return instance;
	}
	
	private int state = STATE_LOADING;
	public int getState(){
		return state;
	}
	
	public void setState(int state){
		this.state = state;
		notifyChange(ChangeEvent.STATE_CHANGED);
	}

	private void notifyChange(String stateChanged) {
		dispatchEvent(new ChangeEvent(stateChanged));
	}
	
}
