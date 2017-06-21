package com.wang.android.mode.activity;

import com.wang.android.MyApplication;
import com.wang.android.R;
import com.wang.android.mode.interfaces.ILogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;


public class BaseActivity extends Activity implements ILogin{
	public MyApplication app = MyApplication.app;
	TextView titleTv;
	ImageView backImg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//app.setSystemBar(this);
	}
	
	public void initViews(){
		 if(isLogin()){
	            if(app.isLogin()){
	                finish();
	                com.wang.android.mode.activity.LoginActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
	            }
	        }
		 
		titleTv = (TextView) findViewById(R.id.titleTv);
		backImg = (ImageView) findViewById(R.id.backImg);
		
		if(backImg == null) return;
		backImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	public boolean isLogin() {
		// TODO Auto-generated method stub
		return false;
	}
}
