package com.flex;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CPictureData {
	private String appDownloadURL;
	private int picLevel;
	private Bitmap picBitmap;
	private String appName;
	private boolean hasClicked = false;
	
	public void setAppName(String appname){
		appName = appname;
	}
	
	public String getAppName(){
		return appName;
	}
	
	public void setHasClicked(boolean clicked){
		hasClicked = clicked;
	}
	
	public boolean getHasClicked(){
		return hasClicked;
	}
	
	public void setPicBitmap(InputStream is){
		picBitmap = BitmapFactory.decodeStream(is);
	}
	public void setAppDownloadURL(String url) {
		this.appDownloadURL = url;
	}

	public void setPicLevel(String str) {
		this.picLevel = Integer.valueOf(str).intValue();
	}

	public String getAppDownloadURL() {
		return this.appDownloadURL;
	}

	public int getPicLevel() {
		return this.picLevel;
	}
	
	public Bitmap getPicBitmap(){
		return this.picBitmap;
	}
}
