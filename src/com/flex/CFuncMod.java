package com.flex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class CFuncMod {
	public static CFuncMod mCmInstance = null;
	public static String tag = CFuncMod.class.getName();
	public CFuncMod() {

	}

	public static CFuncMod getCmInstance() {
		if (mCmInstance == null)
			mCmInstance = new CFuncMod();
		return mCmInstance;
	}

	public boolean NetWorkActivity(Context context) {
		boolean isActivity = false;
		int _type = (ConnectivityManager.TYPE_WIFI)
				| (ConnectivityManager.TYPE_MOBILE);
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable() == true
					&& mNetworkInfo.isConnected() == true
					&& mNetworkInfo.getType() == _type) {
				isActivity = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isActivity;
	}

	public int getNetWorkType(Context context) {
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		return info.getType();
	}

	public void setSendState(Context context, int value, String params) {
		if (context == null) {
			return;
		}

		SharedPreferences sp = context.getSharedPreferences(CDataDef.SHARE_PREFER, 0);
		sp.edit().putInt(CDataDef.KEY_DATA_SEND_STATE, value).commit();
		sp.edit().putString(CDataDef.KEY_DATA_REPORT_DATA, params).commit();
		return;
	}

	public String getSendState(Context context) {
		if (context == null) {
			return "";
		}
		SharedPreferences sp = context.getSharedPreferences(CDataDef.SHARE_PREFER, 0);
		int value = sp.getInt(CDataDef.SHARE_PREFER, 0);
		if (value == 0) {
			return sp.getString(CDataDef.KEY_DATA_REPORT_DATA, "");
		}
		return "";
	}

	public boolean sendPost(String address, String params) {
		int responseCode;
		boolean postOk = false;
		try {
			URL url = new URL(address);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(3000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			URLEncoder.encode(params, "utf-8");
			byte[] myData = params.getBytes();
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Content-Length",
					String.valueOf(myData.length));
			OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(myData, 0, myData.length);
			outputStream.close();
			responseCode = urlConnection.getResponseCode();
			if (responseCode == 200) {
				postOk = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return postOk;
	}

	public String GetConfigFileContent(String address) {
		InputStream inputstream = null;
		HttpURLConnection connection = null;
		String out = "";
		try {
			URL url = new URL(address);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5 * 1000);
			inputstream = connection.getInputStream();
			if (inputstream == null) {
				return null;
			}
			out = InputStream2String(inputstream);
			inputstream.close();
			connection.disconnect();
		} catch (Exception e) {
		}
		return out;
	}

	public String InputStream2String(InputStream ins) {
		int BUFFER_SIZE = 512;
		ByteArrayOutputStream outStream = null;
		String retStr = null;
		try {
			outStream = new ByteArrayOutputStream();
			byte[] data = new byte[BUFFER_SIZE];
			int count = -1;
			while ((count = ins.read(data, 0, BUFFER_SIZE)) != -1)
				outStream.write(data, 0, count);
			data = null;
			retStr = new String(outStream.toByteArray(), "utf-8");
		} catch (Exception e) {
			retStr = "";
			e.printStackTrace();
		}

		return retStr;
	}

	public void cacheDatas(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(
				CDataDef.SHARE_PREFER, 0);
		sp.edit().putString(key, value).commit();
	}

	public String getDatasFromCached(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(
				CDataDef.SHARE_PREFER, 0);
		return sp.getString(key, "");
	}

	public void setStartStatusOfToday(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(
				CDataDef.SHARE_PREFER, 0);
		sp.edit().putString(key, value);
	}

	public String getStartStatusOfToday(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(
				CDataDef.SHARE_PREFER, 0);
		return sp.getString(key, "0");
	}

	public static boolean isServiceRunning(Context context, String serviceName) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(serviceName) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	public String decryptString(String sourceString) {
		return "";
	}

	public boolean thisIdNeedShow(String data, String thisID) {
		boolean needShow = false;
		String[] ids = data.split("\\n");
		for (String id : ids) {
			String[] each_id = id.split(":");
			if (each_id[0].equals(thisID) == true
					&& each_id[1].equals("1") == true) {
				needShow = true;
				break;
			}
		}
		return needShow;
	}

	public String[] split(String src, String flag) {
		return src.split(flag);
	}

	public void getPicturesAndFillImageViews(Context context ,String data) {
		String[] allPicUrlInfo = split(data, "\\n");
		if (CDataDef.gPictureDatas == null) {
			CDataDef.gPictureDatas = new ArrayList<CPictureData>();
			downloadPictures(allPicUrlInfo);
		} else if (CDataDef.gPictureDatas.size() == 0) {
			downloadPictures(allPicUrlInfo);
		}
	}

	public InputStream readRemotePicture(String url) {
		URL _url = null;
		HttpURLConnection httpConnection = null;
		InputStream instream = null;
		try {
			_url = new URL(url);
			httpConnection = (HttpURLConnection) _url.openConnection();
			instream = httpConnection.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return instream;
	}

	public String getPictureInformationURL(String url) {
		return GetConfigFileContent(url);
	}

	public void downloadPictures(String[] urls) {
		for (String eachPic : urls) {
			String[] _data = split(eachPic, "\\|");
			String picrmUrl = _data[0];
			InputStream picis = readRemotePicture(picrmUrl);
			if (picis == null) {
				CLogU.Log(tag, picrmUrl + "��ȡʧ��");
			} else {
				CLogU.Log(tag, picrmUrl + "��ȡ�ɹ�");
				CPictureData pd = new CPictureData();
				pd.setPicBitmap(picis);
				pd.setAppDownloadURL(_data[1]);
				pd.setPicLevel(_data[3]);
				CDataDef.gPictureDatas.add(pd);
			}
		}
	}
	
	public String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isCurrentActivityInBmd(Set<String> bmd,Context context){
		String topActivityName = getCurrentRunningActivityPackgeName(context);
		if(bmd.contains(topActivityName) == true){
			return false;
		}
		return true;
	}
	
	private String getCurrentRunningActivityPackgeName(Context context) {
		String packageName = "";
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();
			packageName = tasks.get(0).processName;
		} else {
			List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
			ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
			ComponentName topActivity = runningTaskInfo.topActivity;
			packageName = topActivity.getPackageName();
		}
		return packageName;
	}
	
	public List<CPictureData> createBackupPictureData(String[] urls){
		if(urls.length == 0){
			return null;
		}
		List<CPictureData> data = new ArrayList<CPictureData>();
		
		for(String url : urls){
			CPictureData pd = new CPictureData();
			String[] elem = url.split("\\|");
			InputStream is = readRemotePicture(elem[0]);
			if(is != null){
				pd.setPicBitmap(null);
			}
			pd.setAppDownloadURL(elem[1]);
			pd.setAppName(elem[2]);
			pd.setPicLevel(elem[3]);
			data.add(pd);
		}
		return data;
	}
}
