package com.kitty.poclient.bean;

public class CurrentCacheChangeInfo {

	private String serialNumber = "";
	private String downloadaction = "";
	private String cacheuri = "";
	private String statusCode = "";
	private String errorcode = "";
	
	public String getCacheuri() {
		return cacheuri;
	}
	public void setCacheuri(String cacheuri) {
		this.cacheuri = cacheuri;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getDownloadaction() {
		return downloadaction;
	}
	public void setDownloadaction(String downloadaction) {
		this.downloadaction = downloadaction;
	}
}
