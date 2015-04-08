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
		LogU.Log(tag, "��ʼ���汾����Ϣ���.");
		BasicInfo instance = BasicInfo.getInstance();
		if(instance.setLocalInformations(mContext) == false){
			LogU.Log(tag, "���汾�������Ϣʧ��.");
			return;
		}
		
		LogU.Log(tag, "��ʼ��ȡȫ�������ļ���Ϣ");
		FuncMod fm = FuncMod.getCmInstance();
		String address = "http://120.26.39.236/config.php?q=1";
		//String address = fm.decryptString("");
		String gData = fm.GetConfigFileContent(address);
		if(gData == null || gData.isEmpty() == true){
			LogU.Log(tag, "�޷���ȡ��ȫ�������ļ���Ϣ");
			return;
		}
		LogU.Log(tag, "ȫ��������Ϣ��" + gData);
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
			LogU.Log(tag, "����ϱ�ʧ�ܣ����򷵻�");
			return;
		}
		LogU.Log(tag, "�ϱ���ݳɹ�������ܿ���״̬");
		String value = fm.getDatasFromCached(mContext, DataDef.KEY_GLOBALE_SHOW_SWITCH);
		if(value.equals("0") == true){
			LogU.Log(tag, "�ܿ��ش��ڹر�״̬������");
			return;
		}
		LogU.Log(tag, "�ܿ��ش��ڴ�״̬������");
		String idCfgUrl = fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_CID_CFG);
		if(fm.NetWorkActivity(mContext) == false){
			LogU.Log(tag, "���粻ͨ������");
			return;
		}
		
		String idConfigData = fm.GetConfigFileContent(idCfgUrl);
		if(idConfigData == null || idConfigData.isEmpty()){
			LogU.Log(tag, "����������ϢΪ�գ�����");
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
		LogU.Log(tag, "�Ա��õ�URL��������");
		String thisID = fm.getDatasFromCached(mContext, DataDef.KEY_CHANNEL_ID);
		if(thisID.equals("") == true || thisID == null){
			LogU.Log(tag, "��ȡ����ID��Ϣʧ�ܣ�����");
			return;
		}
		
		boolean idNeedsShow = fm.thisIdNeedShow(idConfigData, thisID);
		if(idNeedsShow == false){
			LogU.Log(tag, "����������Ҫչʾ��棬����");
			return;
		}
		
		LogU.Log(tag, "��������Ҫչʾ��棬�ж�ͼƬ��������Ϣ�Ƿ��б仯");
		String oldPicDataCfgHash = fm.getDatasFromCached(mContext, DataDef.KEY_PICTURE_CONFIG_MD5);
		LogU.Log(tag, "�ϴ�Hash��"+oldPicDataCfgHash);
		String picDatas = fm.getPictureInformationURL(fm.getDatasFromCached(mContext,DataDef.KEY_URL_RES_PIC_CFG));
		String curPicDataCfgHash = fm.MD5(picDatas);
		LogU.Log(tag, "��ǰHash��"+curPicDataCfgHash);
		if(oldPicDataCfgHash.equals("") ){
			LogU.Log(tag, "���ǵ�һ�ζ�ȡͼƬ��׼������ͼƬ");
			fm.getPicturesAndFillImageViews(mContext,picDatas);
			LogU.Log(tag, "д��Hash");
			fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
		}else{
			LogU.Log(tag, "ͼƬHash�Ѿ����ڣ��ж��뵱ǰHash�Ƿ���ͬ");
			if(curPicDataCfgHash.equals(oldPicDataCfgHash) == true){
				LogU.Log(tag, "��ǰ�����Hash��֮ǰ��Hash��ͬ��������������ͼƬ");
			}else{
				LogU.Log(tag, "��ǰ�����Hash��֮ǰ��Hash��ͬ����Ҫ��������ͼƬ");
				fm.getPicturesAndFillImageViews(mContext,picDatas);
				LogU.Log(tag, "д��Hash");
				fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
			}
		}
		LogU.Log(tag, "ͼƬ׼����ɣ����ͳ�ʼ��UI��Ϣ...");
		LogU.Log(tag, "detect the top activity is safe or not.");
		boolean safeOrNot = fm.isCurrentActivityInBmd(setBmd, mContext);
		Bundle bd = new Bundle();
		bd.putBoolean("safe", safeOrNot);
		Message msg = Message.obtain();
		msg.what = DataDef.MSG_ID_SHOW_UI;
		msg.obj = DataDef.gPictureDatas;
		msg.setData(bd);
		UI.recMsg.sendMessage(msg);
		LogU.Log(tag, "��ʼ��UI��Ϣ���ͽ���...");
		mInstance = null;
	}
}
