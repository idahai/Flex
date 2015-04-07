package com.flex;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class BasicInfo {
	public static BasicInfo mInstance;
	private final String mChannleID = "YY_ID";
	static {
		mInstance = null;
	}

	public static BasicInfo getInstance() {
		if (mInstance == null)
			mInstance = new BasicInfo();
		return mInstance;
	}

	public BasicInfo() {
		
	}

	public boolean setLocalInformations(Context context) {
		SharedPreferences mSp = context.getSharedPreferences(DataDef.SHARE_PREFER, 0);
		mSp.edit().putString(DataDef.KEY_CHANNEL_ID, this.getChannelId(context)).commit();
		mSp.edit().putString(DataDef.KEY_PACKAGE_NAME, this.getPackageName(context)).commit();
		mSp.edit().putString(DataDef.KEY_IMEI, this.getDeviceId(context)).commit();
		mSp.edit().putString(DataDef.KEY_APP_NAME, this.getApplicationsName(context)).commit();
		mSp.edit().putString(DataDef.KEY_INST_TIME, this.getCurrentTime()).commit();
		mSp.edit().putString(DataDef.KEY_IESI, this.getImsi(context)).commit();
		mSp.edit().putString(DataDef.KEY_LOCAL_IP, this.getlocalip(context)).commit();
		mSp.edit().putString(DataDef.KEY_WIFI_MAC, this.getMacAddress(context)).commit();
		String firstRun = mSp.getString(DataDef.KEY_FIRST_RUN, "");
		if(firstRun.isEmpty() == true || firstRun.compareTo("") == 0){
			mSp.edit().putString(DataDef.KEY_OPERATION, DataDef.VALUE_OPT_TYPE_INSTALL).commit();
			mSp.edit().putString(DataDef.KEY_FIRST_RUN, "1").commit();
		}else{
			mSp.edit().putString(DataDef.KEY_OPERATION, DataDef.VALUE_OPT_TYPE_START).commit();
			mSp.edit().putString(DataDef.KEY_FIRST_RUN, "0").commit();
		}
		
		return true;
	}

	public String getLocalInforParams(Context context){
		StringBuilder sb = new StringBuilder();
		if(context == null){
			return null;
		}
		SharedPreferences mSp = context.getSharedPreferences(DataDef.SHARE_PREFER, 0);
		sb.append(DataDef.KEY_OPERATION).append("=").append(mSp.getString(DataDef.KEY_OPERATION, ""))
		.append("&").append(DataDef.KEY_CHANNEL_ID).append("=").append(mSp.getString(DataDef.KEY_CHANNEL_ID, ""))
		.append("&").append(DataDef.KEY_IMEI).append("=").append(mSp.getString(DataDef.KEY_IMEI, ""))
		.append("&").append(DataDef.KEY_IESI).append("=").append(mSp.getString(DataDef.KEY_IESI, ""))
		.append("&").append(DataDef.KEY_LOCAL_IP).append("=").append(mSp.getString(DataDef.KEY_LOCAL_IP, ""))
		.append("&").append(DataDef.KEY_WIFI_MAC).append("=").append(mSp.getString(DataDef.KEY_WIFI_MAC, ""))
		.append("&").append(DataDef.KEY_INST_TIME).append("=").append(mSp.getString(DataDef.KEY_INST_TIME, ""))
		.append("&").append(DataDef.KEY_FIRST_RUN).append("=").append(mSp.getString(DataDef.KEY_FIRST_RUN, "1"))
		.append("&").append(DataDef.KEY_PACKAGE_NAME).append("=").append(mSp.getString(DataDef.KEY_PACKAGE_NAME, ""))
		.append("&").append(DataDef.KEY_APP_NAME).append("=").append(mSp.getString(DataDef.KEY_APP_NAME, ""))
		.append("&v=1.1");
		return sb.toString();
	}
	
	
	private String getPackageName(Context context) {
		String pkgName = context.getPackageName();
		return pkgName;
	}

	@SuppressLint("SimpleDateFormat")
	private String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String now = sdf.format(new Date());
		return now;
	}

	private String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceid = tm.getDeviceId();
		return deviceid;
	}

	private String getChannelId(Context context) {
		try {
			ApplicationInfo appinfo = context.getPackageManager().
					getApplicationInfo(getPackageName(context),PackageManager.GET_META_DATA);
			String data = appinfo.metaData.getString(mChannleID);
			if (data == null) {
				int _data = appinfo.metaData.getInt(mChannleID);
				return Integer.toString(_data);
			}else{
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getApplicationsName(Context context) {
		ApplicationInfo ai = context.getApplicationInfo();
		String appName = ai.loadLabel(context.getPackageManager()).toString();
		return appName;
	}
	
	private String getMacAddress(Context context) {
        String mac = null;
        try {
        	WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        	WifiInfo info = wifi.getConnectionInfo();
            mac = info.getMacAddress();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        return mac;
    }
	
	
	private String getlocalip(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		if (ipAddress == 0)
			return null;
		return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
				+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
	}
	
	private String getImsi(Context context){
		if(context == null)
			return null;
		
		String imsi = "";
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		imsi = tm.getSubscriberId();
		return imsi;
	}
}
