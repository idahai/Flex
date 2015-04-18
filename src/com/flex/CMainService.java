package com.flex;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class CMainService extends Service {
	public static CMainService mInstance = null;
	public static Context mContext = null;
	public static String tag = CMainService.class.getName();
	public CLogU mInstanceLogger;
	public static String serviceName;
	private static boolean reged = false;
	private static Handler mSendMsg;
	private final static long delayMillis = 1000*60*60*5;
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
				if(CUI.isShowing == false){
					CLogU.Log(tag, "not showing,send message");
					CFuncMod fm = CFuncMod.getCmInstance();
					boolean safeOrNot = fm.isCurrentActivityInBmd(CDataDef.gWhiteList, mContext);
					Bundle bd = new Bundle();
					bd.putBoolean("safe", safeOrNot);
					Message msg = Message.obtain();
					msg.what = CDataDef.MSG_ID_SHOW_UI;
					msg.obj = CDataDef.gPictureDatas;
					msg.setData(bd);
					CUI.recMsg.sendMessage(msg);
					mSendMsg.postDelayed(ThreadRunnable, delayMillis);
				}else{
					CLogU.Log(tag, "showing.");
				}
			}	
		};
		mSendMsg.postDelayed(ThreadRunnable, delayMillis);
	}
}
