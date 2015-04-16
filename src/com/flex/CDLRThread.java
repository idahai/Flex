package com.flex;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;

public class CDLRThread extends Thread {
	private Context mContext;
	private String apkName;
	public CDLRThread(Context context,String appname){
		mContext = context;
		apkName = appname;
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
		String value_imsi = fm.getDatasFromCached(mContext, CDataDef.KEY_IESI);
		String mac = fm.getDatasFromCached(mContext, CDataDef.KEY_WIFI_MAC);
		String ip = fm.getDatasFromCached(mContext, CDataDef.KEY_LOCAL_IP);
		String time = getCurrentTime();
		sb.append(CDataDef.KEY_CHANNEL_ID).append("=").append(value_cid)
		.append("&").append(CDataDef.KEY_IMEI).append("=").append(value_did)
		.append("&").append(CDataDef.KEY_IESI).append("=").append(value_imsi)
		.append("&").append(CDataDef.KEY_WIFI_MAC).append("=").append(mac)
		.append("&").append(CDataDef.KEY_LOCAL_IP).append("=").append(ip)
		.append("&time=").append(time)
		.append("&").append(CDataDef.KEY_PACKAGE_NAME).append("=").append(apkName);
		return sb.toString();
	}
}
