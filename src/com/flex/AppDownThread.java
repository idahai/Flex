package com.flex;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class AppDownThread extends Thread {
	private Context mContext;
	private String mAppUrl;
	public static AppDownThread mADTInstance;
	private static String tag;
	
	static{
		tag = AppDownThread.class.getName();
	}
	
	public static AppDownThread getAdtInstance(Context context ,String _url){
		if(mADTInstance == null)
			mADTInstance = new AppDownThread(context,_url);
		return mADTInstance;
	}
	
	public AppDownThread(Context context, String _url){
		mContext = context;
		mAppUrl = _url;
	}
	
	public void run(){
		if (remoteFileExists(mAppUrl) == true) {
			if (true == downloadApk()){
				LogU.Log(tag, mAppUrl+" download success,report!");
				
			}else{
				LogU.Log(tag, mAppUrl+" download failed.");
			}
		}
	}
	
	private synchronized boolean downloadApk() {
		boolean downloadOk = true;
		try {
			DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			String location = getLocationMethod(mAppUrl);
			String fileName = "";
			Uri resource;
			if(location == null){
				fileName = mAppUrl.substring(mAppUrl.lastIndexOf("/") + 1, mAppUrl.length());
				resource = Uri.parse(encodeGB(mAppUrl));
			}else{
				fileName = location.substring(location.lastIndexOf("/") + 1, location.length());
				resource = Uri.parse(encodeGB(location));
			}
			
			DownloadManager.Request request = new DownloadManager.Request(resource);
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(location));
			request.setMimeType(mimeString);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
			request.setVisibleInDownloadsUi(true);
			request.setDestinationInExternalPublicDir("/download/", fileName);
			request.setTitle(fileName);
			downloadManager.enqueue(request);
		} catch (Exception e) {
			e.printStackTrace();
			downloadOk = false;
		}
		return downloadOk;
	}

	private String encodeGB(String string) {
		String split[] = string.split("/");
		for (int i = 1; i < split.length; i++) {
			try {
				split[i] = URLEncoder.encode(split[i], "GB2312");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			split[0] = split[0] + "/" + split[i];
		}
		split[0] = split[0].replaceAll("\\+", "%20");
		return split[0];
	}
	
	private boolean remoteFileExists(String address){
		boolean bExists = false;
		InputStream inputstream = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(address);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5 * 1000);
			inputstream = connection.getInputStream();
			if(inputstream != null)
				bExists = true;
			inputstream.close();
			connection.disconnect();
		} catch (Exception e) {
		}
		return bExists;
	}
	
	private String getLocationMethod(String reqUrl) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String location = null;
		int responseCode = 0;
		try {
			final HttpGet request = new HttpGet(reqUrl);
			HttpParams params = new BasicHttpParams();
			params.setParameter("http.protocol.handle-redirects", false);
			request.setParams(params);
			HttpResponse response = httpclient.execute(request);
			responseCode = response.getStatusLine().getStatusCode();
			//Header[] headers = response.getAllHeaders();
			if (responseCode == 302) {
				Header locationHeader = response.getFirstHeader("Location");
				if (locationHeader != null) {
					location = locationHeader.getValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}
}
