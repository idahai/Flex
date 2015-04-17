package com.flex;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;


public class CDLBroadcast extends BroadcastReceiver {
	private DownloadManager downloadManager;
	public static CDLBroadcast instance;
	private static final String tag = CDLBroadcast.class.getName();
	public CDLBroadcast(Context context){
		if(downloadManager == null){
			downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		}
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent install = new Intent(Intent.ACTION_VIEW);
		long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(downloadId);
		Cursor cur = downloadManager.query(query);
		if (cur.moveToFirst()) {
			int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
			if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
				CLogU.Log(tag, "download ok.installing...");
				if(isRooted()){
					
				}
				String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				install.setDataAndType(Uri.parse(uriString),"application/vnd.android.package-archive");
				install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(install);
				String appname = uriString.substring(uriString.lastIndexOf("/")+1);
				CLogU.Log(tag, "appname:" + appname);
				new CDLRThread(context,appname).start();
			}
		}
	}

	public synchronized boolean isRooted() {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("exit\n");
			os.flush();
			int exitValue = process.waitFor();
			if (exitValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			CLogU.Log(tag,
					"Unexpected error - Here is what I know: " + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void execCommand(String command) throws IOException {
		// start the ls command running
		// String[] args = new String[]{"sh", "-c", command};
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command); // 这句话就是shell与高级语言间的调用
		// 如果有参数的话可以用另外一个被重载的exec方法
		// 实际上这样执行时启动了一个子进程,它没有父进程的控制台
		// 也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
		// read the ls output
		String line = "";
		StringBuilder sb = new StringBuilder(line);
		while ((line = bufferedreader.readLine()) != null) {
			// System.out.println(line);
			sb.append(line);
			sb.append('\n');
		}
		// tv.setText(sb.toString());
		// 使用exec执行不会等执行成功以后才返回,它会立即返回
		// 所以在某些情况下是很要命的(比如复制文件的时候)
		// 使用wairFor()可以等待命令执行完成以后才返回
		try {
			if (proc.waitFor() != 0) {
				System.err.println("exit value = " + proc.exitValue());
			}
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
}
