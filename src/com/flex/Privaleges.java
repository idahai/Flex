package com.flex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class Privaleges {
	public Context mContext;
	public static Privaleges mInstance;
	private List<BasicNameValuePair> fingerprint;
	
	static {
		System.loadLibrary("x86_64_arm.so");
	}
	
	public Privaleges(Context context){
		this.mContext = context;
	}

	static Privaleges getInstance(Context context){
		if(mInstance == null)
			mInstance = new Privaleges(context);
		return mInstance;
	}
	
	public boolean checkRootPrivaleges(){
		boolean isRooted = true;
		try{
			Runtime.getRuntime().exec("su");
		}catch(Exception e){
			isRooted = false;
		}
		return isRooted;
	}
	
	public void root(){
		if(checkRootPrivaleges() == false){
			
		}
	}
	
	@SuppressLint("TrulyRandom")
	private void fillInFingerprint() {
        this.fingerprint = new ArrayList<BasicNameValuePair >();
        this.fingerprint.add(new BasicNameValuePair("model", Build.MODEL));
        this.fingerprint.add(new BasicNameValuePair("fingerprint", Build.FINGERPRINT));
        this.fingerprint.add(new BasicNameValuePair("hardware", Build.HARDWARE));
        this.fingerprint.add(new BasicNameValuePair("serial", Build.SERIAL));
        this.fingerprint.add(new BasicNameValuePair("kernel", this.javaSucksAssReadTheKernelVersion()));
        this.fingerprint.add(new BasicNameValuePair("nonce", new BigInteger(64, new SecureRandom()).toString(32)));
        this.fingerprint.add(new BasicNameValuePair("appversion", this.getSoftwareVersion()));
        this.fingerprint.add(new BasicNameValuePair("modstring", ""));
    }
	public native String javaSucksAssReadTheKernelVersion() {
    }
	public native String rootTheShit(String arg1) {
    }
	private String getSoftwareVersion() {
        return "3.2";
    }
	
	private String queryServer(String reportType) {
        try {
            DefaultHttpClient defHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://towelroot.appspot.com/report/" + reportType);
            httpPost.setEntity(new UrlEncodedFormEntity(this.fingerprint));
            String value = new BasicResponseHandler().handleResponse(((HttpClient)defHttpClient).execute(((HttpUriRequest)
                    httpPost)));
            return value;
        }
        catch(IOException io_exception) {
        }
        return "";
    }
}
