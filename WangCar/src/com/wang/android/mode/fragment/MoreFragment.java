package com.wang.android.mode.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import com.wang.android.R;

import android.content.Intent;

@EFragment(R.layout.fragment_more_layout)
public class MoreFragment extends BaseFragment {
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
	}
	
	@Click
	public void setLayout(){
		com.wang.android.mode.activity.SettingActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
	}
}
