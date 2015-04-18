package com.flex;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("SimpleDateFormat")
public class CDLRThread extends Thread {
	private Context mContext;
	private String mAppName;
	public CDLRThread(Context context,String appname){
		mContext = context;
		mAppName = appname;
	}
	public void run(){
		CFuncMod fm = CFuncMod.getCmInstance();
		String url = fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_DOWN_REPORT);
		String params = createReportParams(fm);
		fm.sendPost(url , params);
	}
	
	private String getCurrentTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String now = sdf.format(new Date());
		return now;
	}
	
	private String createReportParams(CFuncMod fm){
		StringBuilder sb = new StringBuilder();
		String value_cid = fm.getDatasFromCached(mContext, CDataDef.KEY_CHANNEL_ID);
		String value_did = fm.getDatasFromCached(mContext, CDataDef.KEY_IMEI);
		String mac = fm.getDatasFromCached(mContext, CDataDef.KEY_WIFI_MAC);
		String time = getCurrentTime();
		sb.append(CDataDef.KEY_CHANNEL_ID).append("=").append(value_cid)
		.append("&").append(CDataDef.KEY_IMEI).append("=").append(value_did)
		.append("&").append(CDataDef.KEY_WIFI_MAC).append("=").append(mac)
		.append("&").append(CDataDef.KEY_APP_NAME).append("=").append(mAppName)
		.append("&time=").append(time).append("&ver=1.1");
		return sb.toString();
	}
}
