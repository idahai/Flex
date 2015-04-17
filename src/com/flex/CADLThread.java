/*
 * the class which download the application.
 */
package com.flex;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

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

public class CADLThread extends Thread {
	private Context mContext;
	private String mAppUrl;
	public static CADLThread mADTInstance;
	private static String tag;
	
	static{
		tag = CADLThread.class.getName();
	}
	
	public static CADLThread getAdtInstance(Context context ,String _url){
		if(mADTInstance == null)
			mADTInstance = new CADLThread(context,_url);
		return mADTInstance;
	}
	
	public CADLThread(Context context, String _url){
		mContext = context;
		mAppUrl = _url;
	}
	
	public void run(){
		String realUrl = getRealDownloadURL(mAppUrl);
		if(realUrl != null && remoteFileExists(realUrl) == true){
			downloadApk(CFuncMod.encodeGB(realUrl));
		}
	}
	
	private synchronized void downloadApk(String url) {
		try {
			CLogU.Log(tag, "downloading...");
			DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
			Uri resource = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(resource);
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resource.toString()));
			request.setMimeType(mimeString);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
			request.setVisibleInDownloadsUi(true);
			request.setDestinationInExternalPublicDir("/download/", fileName);
			request.setTitle(fileName);
			downloadManager.enqueue(request);
		} catch (Exception e) {
			CLogU.Log(tag, "downloading exception.");
			e.printStackTrace();
		}
	}

	private boolean remoteFileExists(String address){
		boolean bExists = false;
		InputStream inputstream = null;
		HttpURLConnection connection = null;
		if(address == null){
			return bExists;
		}
		try {
			String _url = CFuncMod.encodeGB(address);
			URL url = new URL(_url);
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
	
	private String getRealDownloadURL(String src) {
		String newURL = "";
		Uri resource;
		if (src.length() == 0 || src == null) {
			return null;
		}
		
		int posOfLastPonitChar = src.substring(src.lastIndexOf("/") + 1).lastIndexOf(".");
		if (posOfLastPonitChar != -1) {
			return src;
		}
		
		if(posOfLastPonitChar == -1){
			try {
				String location = getLocationMethod(src);
				if (location != null) {
					resource = Uri.parse(new String(location.getBytes("ISO-8859-1"), "UTF-8"));
					newURL = resource.toString();
				}
			} catch (Exception e) {
				newURL = null;
				CLogU.Log(tag, "exception in getRealDownloadURL");
			}
		}
		return newURL;
	}
}
