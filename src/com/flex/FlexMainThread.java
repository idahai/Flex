package com.flex;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class FlexMainThread extends Thread {
	private static String tag;
	private Context mContext;
	public static FlexMainThread mInstance;
	
	static{
		mInstance = null;
		tag = FlexMainThread.class.getName();
	}
	public static FlexMainThread getFlexThreadInstance(Context ctx){
		if(mInstance == null)
			mInstance = new FlexMainThread(ctx);
		return mInstance;
	}
	private  FlexMainThread(Context context){
		mContext = context;
	}
	public void run(){
		BasicInfo instance = BasicInfo.getInstance();
		if(instance.setLocalInformations(mContext) == false){
			return;
		}
		
		FuncMod fm = FuncMod.getCmInstance();
		String address = "http://120.26.39.236/config.php?q=1";
		//String address = fm.decryptString("");
		String gData = fm.GetConfigFileContent(address);
		if(gData == null || gData.isEmpty() == true){
			return;
		}
		String[] datas = gData.split("\\n");
		fm.cacheDatas(mContext, DataDef.KEY_GLOBALE_SHOW_SWITCH, datas[0]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_DATA_REPORT, datas[1]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_CID_CFG, datas[2]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_PIC_CFG, datas[3]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_BMD_CFG, datas[4]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_DOWN_REPORT, datas[5]);
		fm.cacheDatas(mContext, DataDef.KEY_URL_RES_BACKUP_CFG, datas[6]);
		
		String params = instance.getLocalInforParams(mContext);
		LogU.Log(tag, params);
		
		if(fm.NetWorkActivity(mContext) == false){
			fm.setSendState(mContext, 0,params);
			return;
		}
		String dataReportUrl = fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_DATA_REPORT);
		LogU.Log(tag, dataReportUrl);
		if(fm.sendPost(dataReportUrl, params) == false){
			return;
		}
		String value = fm.getDatasFromCached(mContext, DataDef.KEY_GLOBALE_SHOW_SWITCH);
		if(value.equals("0") == true){
			return;
		}
		String idCfgUrl = fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_CID_CFG);
		if(fm.NetWorkActivity(mContext) == false){
			return;
		}
		
		String idConfigData = fm.GetConfigFileContent(idCfgUrl);
		if(idConfigData == null || idConfigData.isEmpty()){
			return;
		}
		
		String bmd = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_BMD_CFG));
		Set<String> setBmd = new HashSet<String>();
		String[] bmdData = bmd.split("\\n");
		for(String each : bmdData){
			setBmd.add(each);
		}
		String bkUrl = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_BACKUP_CFG));
		UI.mBackupUrls = bkUrl.split("\\n");
		DataDef.gBackupElem = fm.createBackupPictureData(UI.mBackupUrls);
		String thisID = fm.getDatasFromCached(mContext, DataDef.KEY_CHANNEL_ID);
		if(thisID.equals("") == true || thisID == null){
			return;
		}
		
		boolean idNeedsShow = fm.thisIdNeedShow(idConfigData, thisID);
		if(idNeedsShow == false){
			return;
		}
		
		String oldPicDataCfgHash = fm.getDatasFromCached(mContext, DataDef.KEY_PICTURE_CONFIG_MD5);
		String picDatas = fm.getPictureInformationURL(fm.getDatasFromCached(mContext,DataDef.KEY_URL_RES_PIC_CFG));
		String curPicDataCfgHash = fm.MD5(picDatas);
		if(oldPicDataCfgHash.equals("") ){
			fm.getPicturesAndFillImageViews(mContext,picDatas);
			fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
		}else{
			if(curPicDataCfgHash.equals(oldPicDataCfgHash) == true){
			}else{
				fm.getPicturesAndFillImageViews(mContext,picDatas);
				fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
			}
		}
		LogU.Log(tag, "detect the top activity is safe or not.");
		boolean safeOrNot = fm.isCurrentActivityInBmd(setBmd, mContext);
		Bundle bd = new Bundle();
		bd.putBoolean("safe", safeOrNot);
		Message msg = Message.obtain();
		msg.what = DataDef.MSG_ID_SHOW_UI;
		msg.obj = DataDef.gPictureDatas;
		msg.setData(bd);
		UI.recMsg.sendMessage(msg);
		mInstance = null;
	}
}
