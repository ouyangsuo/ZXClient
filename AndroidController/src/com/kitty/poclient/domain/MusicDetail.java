package com.kitty.poclient.domain;

public class MusicDetail {

//	 { “id”: MUSICID, 
//	      “name”:MUSICNAME,
//	      “albumid”:ALBUMID,
//	      “albumname”:ALBUMNAME,
//	      “albumimg”:ALBUMBIGIMG,
//	“publishtime”: PUBLISHTIME,
//	“language”:LANGUAGE,
//	“artistname”:ARTISTNAME,
//	“companyname”:COMPANYNAME,
//	“size”:SIZE,
//	 “playtimes”:PALYTIMES,
//	“technology”:TECHNOLOGY,
//	 “listenurl”:LISTENURL,
//	 “lyrics”:LYRICS,
//	“price”:PRICE,
//	“score”:SCORE,
//	“state”:STATE
//	} 
	
	private String listenUrl="";

	public String getListenUrl() {
		return listenUrl;
	}

	public void setListenUrl(String listenUrl) {
		this.listenUrl = listenUrl;
	}

}
