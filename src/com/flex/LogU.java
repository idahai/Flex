package com.flex;
import android.util.Log;
public class LogU {
	public static LogU mInstance;
	public static boolean IS_DEBUG;
	static{
		mInstance = null;
	}
	public static void Log(String tag ,String msg){
		if(IS_DEBUG == true){
			Log.i(tag, msg);
		}
	}
}
