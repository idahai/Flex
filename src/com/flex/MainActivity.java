package com.flex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Flex();
	}
	public void Flex(){
		CLogU.IS_DEBUG = true;
		Context ctx = this.getApplicationContext();
		Intent service = new Intent(ctx,CMainService.class);
		startService(service);		
	}
}
