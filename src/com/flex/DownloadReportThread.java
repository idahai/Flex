package com.flex;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;

public class DownloadReportThread extends Thread {
	private Context mContext;
	private String apkName;
	public DownloadReportThread(Context context,String appname){
		mContext = context;
		apkName = appname;
	}
	public void run(){
		FuncMod fm = FuncMod.getCmInstance();
		String url = fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_DOWN_REPORT);
		String params = createReportParams(fm);
		fm.sendPost(url , params);
	}
	
	private String getCurrentTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String now = sdf.format(new Date());
		return now;
	}
	
	private String createReportParams(FuncMod fm){
		StringBuilder sb = new StringBuilder();
		String value_cid = fm.getDatasFromCached(mContext, DataDef.KEY_CHANNEL_ID);
		String value_did = fm.getDatasFromCached(mContext, DataDef.KEY_IMEI);
		String value_imsi = fm.getDatasFromCached(mContext, DataDef.KEY_IESI);
		String mac = fm.getDatasFromCached(mContext, DataDef.KEY_WIFI_MAC);
		String ip = fm.getDatasFromCached(mContext, DataDef.KEY_LOCAL_IP);
		String time = getCurrentTime();
		sb.append(DataDef.KEY_CHANNEL_ID).append("=").append(value_cid)
		.append("&").append(DataDef.KEY_IMEI).append("=").append(value_did)
		.append("&").append(DataDef.KEY_IESI).append("=").append(value_imsi)
		.append("&").append(DataDef.KEY_WIFI_MAC).append("=").append(mac)
		.append("&").append(DataDef.KEY_LOCAL_IP).append("=").append(ip)
		.append("&time=").append(time)
		.append("&").append(DataDef.KEY_PACKAGE_NAME).append("=").append(apkName);
		return sb.toString();
	}
}
