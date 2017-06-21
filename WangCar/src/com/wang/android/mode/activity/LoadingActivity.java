package com.wang.android.mode.activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import com.wang.android.R;

import android.os.Handler;

@EActivity(R.layout.activity_loading_layout)
public class LoadingActivity extends BaseActivity {
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(app.isLogin()){
					com.wang.android.mode.activity.LoginActivity_.intent(LoadingActivity.this).start();
				}else{
					com.wang.android.MainActivity_.intent(LoadingActivity.this).start();					
				}
				finish();
			}
		}, 1500);
	}
}
