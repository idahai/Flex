package com.flex;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

public class CMainService extends Service {
	public static CMainService mInstance = null;
	public static Context mContext = null;
	public static String tag = CMainService.class.getName();
	public CLogU mInstanceLogger;
	public static String serviceName;
	private static boolean reged = false;
	private static Handler mSendMsg;
	private final static long delayMillis = 1000*60*10;
	public static Runnable ThreadRunnable;
	public static CMainService getInstance() {
		if (mInstance == null)
			mInstance = new CMainService();
		return mInstance;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if (mContext == null) {
			mContext = getApplicationContext();
		}
		CUI ui = CUI.getUiInstance(mContext);
		if(CUI.recMsg == null){
			ui.setMessage();
		}
		if(reged == false){
			regAppDownCompleteReceiver(mContext);
			reged = true;
		}
		
		if(mSendMsg == null){
			mSendMsg = new Handler();
		}
		CMainThread fmt = CMainThread.getFlexThreadInstance(mContext);
		fmt.start();
		StartHeartBeat();
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void regAppDownCompleteReceiver(Context context){
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		context.registerReceiver(new CDLBroadcast(context), filter);
	}
	
	
	public void StartHeartBeat(){
		ThreadRunnable = new Runnable(){
			public void run() {
				new CMainThread(mContext).start();
				mSendMsg.postDelayed(ThreadRunnable, delayMillis);
			}	
		};
		mSendMsg.postDelayed(ThreadRunnable, delayMillis);
	}
}
