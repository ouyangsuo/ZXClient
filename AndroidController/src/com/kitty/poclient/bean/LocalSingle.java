package com.kitty.poclient.bean;


public class LocalSingle {
	
	private Long id;
	private boolean isSelected = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public void switchSelectStatus(){
		isSelected = !isSelected;
	}
}
