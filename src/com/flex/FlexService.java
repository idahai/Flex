package com.flex;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

public class FlexService extends Service {
	public static FlexService mInstance;
	public static Context mContext;
	public static String tag = "";
	public LogU mInstanceLogger;
	public static String serviceName;
	private static boolean reged = false;
	private static Handler mSendMsg;
	private final static long delayMillis = 1000*60*10;
	public static Runnable ThreadRunnable;
	static {
		mInstance = null;
		serviceName = FlexService.class.getName();
		tag = FlexService.class.getName();
	}

	public static FlexService getInstance() {
		LogU.Log(tag, "FlexService::getInstance");
		if (mInstance == null)
			mInstance = new FlexService();
		return mInstance;
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogU.Log(tag, "FlexService::onBind");
		return null;
	}

	@Override
	public void onCreate() {
		LogU.Log(tag, "FlexService::onCreate");
		if (mContext == null) {
			mContext = getApplicationContext();
		}
		UI ui = UI.getUiInstance(mContext);
		if(UI.recMsg == null){
			ui.setMessage();
		}
		if(reged == false){
			regAppDownCompleteReceiver(mContext);
			reged = true;
		}
		
		if(mSendMsg == null){
			mSendMsg = new Handler();
		}
		FlexMainThread fmt = FlexMainThread.getFlexThreadInstance(mContext);
		fmt.start();
		StartHeartBeat();
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		LogU.Log(tag, "FlexService::onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogU.Log(tag, "FlexService::onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void regAppDownCompleteReceiver(Context context){
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		context.registerReceiver(new DownloadBroadcast(context), filter);
	}
	
	
	public void StartHeartBeat(){
		ThreadRunnable = new Runnable(){
			public void run() {
				FlexMainThread fmt = FlexMainThread.getFlexThreadInstance(mContext);
				if(fmt.isAlive()){
					fmt.start();
				}else{
					fmt.start();
				}
				mSendMsg.postDelayed(ThreadRunnable, delayMillis);
			}	
		};
		mSendMsg.postDelayed(ThreadRunnable, delayMillis);
	}
}
