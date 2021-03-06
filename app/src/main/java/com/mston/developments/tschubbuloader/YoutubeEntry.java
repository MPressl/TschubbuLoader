//Copywrite (C) Martin Pressl 2017
package com.mston.developments.tschubbuloader;

/**
* Model class for a Youtube search result
**/
public class YoutubeEntry {


	private String url;
	private String name;
	private String imgUrl;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
}
