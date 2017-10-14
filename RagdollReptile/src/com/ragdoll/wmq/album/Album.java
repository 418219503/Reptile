package com.ragdoll.wmq.album;

/*
 * ×¨¼­
 */
public class Album {
	private String title;
	private String anchorman;
	private String info;
	private String totalPlayCount;
	
	Album(String title,String anchorman,String info,String totalPlayCount){
		this.title = title;
		this.anchorman = anchorman;
		this.info = info;
		this.totalPlayCount = totalPlayCount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAnchorman() {
		return anchorman;
	}

	public void setAnchorman(String anchorman) {
		this.anchorman = anchorman;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getTotalPlayCount() {
		return totalPlayCount;
	}

	public void setTotalPlayCount(String totalPlayCount) {
		this.totalPlayCount = totalPlayCount;
	}
	
	@Override
	public String toString() {
		return "{\"title\":\"" + title + "\", \"anchorman\":\"" + anchorman + "\",\"info\":\""
				+ info + "\", \"totalPlayCount\":\"" + totalPlayCount + "\"}";
	}
}
