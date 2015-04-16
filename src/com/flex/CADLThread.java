/*
 * the class which download the application.
 */
package com.flex;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
		if (remoteFileExists(mAppUrl) == true) {
			downloadApk();
		}
	}
	
	private synchronized void downloadApk() {
		try {
			CLogU.Log(tag, "downloading...");
			DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			String location = getLocationMethod(mAppUrl);
			String fileName = "";
			Uri resource = null;
			Uri resource1 = null;
			if(location == null){
				fileName = mAppUrl.substring(mAppUrl.lastIndexOf("/") + 1, mAppUrl.length());
				resource = Uri.parse(new String(mAppUrl.getBytes("ISO-8859-1"), "gbk"));
			}else{
				resource = Uri.parse(new String(location.getBytes("ISO-8859-1"), "UTF-8"));
				resource1 = Uri.parse(CFuncMod.encodeGB(resource.toString()));
				String temp = resource.toString();
				fileName = temp.substring(temp.lastIndexOf("/") + 1, temp.length());
			}
			CLogU.Log(tag, "downloadURL:"+resource1.toString());
			DownloadManager.Request request = new DownloadManager.Request(resource1);
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resource1.toString()));
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
