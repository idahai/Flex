package com.flex;

import android.content.Context;
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
		LogU.Log(tag, "开始保存本地信息数据.");
		BasicInfo instance = BasicInfo.getInstance();
		if(instance.setLocalInformations(mContext) == false){
			LogU.Log(tag, "保存本地数据信息失败.");
			return;
		}
		
		LogU.Log(tag, "开始获取全局配置文件信息");
		FuncMod fm = FuncMod.getCmInstance();
		String address = "http://120.26.39.236/config.php?q=1";
		//String address = fm.decryptString("");
		String gData = fm.GetConfigFileContent(address);
		if(gData == null || gData.isEmpty() == true){
			LogU.Log(tag, "无法获取到全局配置文件信息");
			return;
		}
		LogU.Log(tag, "全局配置信息：" + gData);
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
			LogU.Log(tag, "数据上报失败，程序返回");
			return;
		}
		LogU.Log(tag, "上报数据成功，检查总开关状态");
		String value = fm.getDatasFromCached(mContext, DataDef.KEY_GLOBALE_SHOW_SWITCH);
		if(value.equals("0") == true){
			LogU.Log(tag, "总开关处于关闭状态，返回");
			return;
		}
		LogU.Log(tag, "总开关处于打开状态，继续");
		String idCfgUrl = fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_CID_CFG);
		if(fm.NetWorkActivity(mContext) == false){
			LogU.Log(tag, "网络不通，返回");
			return;
		}
		
		String idConfigData = fm.GetConfigFileContent(idCfgUrl);
		if(idConfigData == null || idConfigData.isEmpty()){
			LogU.Log(tag, "渠道配置信息为空，返回");
			return;
		}
		
		UI.bmd = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_BMD_CFG));
		UI.bakurl = fm.GetConfigFileContent(fm.getDatasFromCached(mContext, DataDef.KEY_URL_RES_BACKUP_CFG));
		LogU.Log(tag, "对备用的URL进行排序");
		String thisID = fm.getDatasFromCached(mContext, DataDef.KEY_CHANNEL_ID);
		if(thisID.equals("") == true || thisID == null){
			LogU.Log(tag, "获取渠道ID信息失败，返回");
			return;
		}
		
		boolean idNeedsShow = fm.thisIdNeedShow(idConfigData, thisID);
		if(idNeedsShow == false){
			LogU.Log(tag, "此渠道不需要展示广告，返回");
			return;
		}
		
		LogU.Log(tag, "此渠道需要展示广告，判断图片的配置信息是否有变化");
		String oldPicDataCfgHash = fm.getDatasFromCached(mContext, DataDef.KEY_PICTURE_CONFIG_MD5);
		LogU.Log(tag, "上次Hash："+oldPicDataCfgHash);
		String picDatas = fm.getPictureInformationURL(fm.getDatasFromCached(mContext,DataDef.KEY_URL_RES_PIC_CFG));
		String curPicDataCfgHash = fm.MD5(picDatas);
		LogU.Log(tag, "当前Hash："+curPicDataCfgHash);
		if(oldPicDataCfgHash.equals("") ){
			LogU.Log(tag, "这是第一次读取图片，准备下载图片");
			fm.getPicturesAndFillImageViews(mContext,picDatas);
			LogU.Log(tag, "写入Hash");
			fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
		}else{
			LogU.Log(tag, "图片Hash已经存在，判断与当前Hash是否相同");
			if(curPicDataCfgHash.equals(oldPicDataCfgHash) == true){
				LogU.Log(tag, "当前计算的Hash与之前的Hash相同，无需重新下载图片");
			}else{
				LogU.Log(tag, "当前计算的Hash与之前的Hash不同，需要重新下载图片");
				fm.getPicturesAndFillImageViews(mContext,picDatas);
				LogU.Log(tag, "写入Hash");
				fm.cacheDatas(mContext, DataDef.KEY_PICTURE_CONFIG_MD5, curPicDataCfgHash);
			}
		}
		LogU.Log(tag, "图片准备完成，发送初始化UI消息...");
		Message msg = Message.obtain();
		msg.what = DataDef.MSG_ID_SHOW_UI;
		msg.obj = DataDef.gPictureDatas;
		UI.recMsg.sendMessage(msg);
		LogU.Log(tag, "初始化UI消息发送结束...");
		mInstance = null;
	}
}
