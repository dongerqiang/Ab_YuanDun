package com.wang.android.mode.activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.wang.android.R;
import com.wang.android.mode.interfaces.DialogCallback;
import com.wang.android.mode.utils.DialogUtils;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

@EActivity(R.layout.activity_settings_layout)
public class SettingActivity extends BaseActivity {
	@ViewById
	TextView ljTv,jdsTv,volTv,dangTv;
	
	@ViewById
	CheckBox lockCb,unlockCb,findCb,anzhuoCb,autolockCb,mutelockCb;
	
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		
		backImg.setVisibility(View.VISIBLE);
		titleTv.setText("设置");
		
		ljTv.setText(app.deviceNotes.optWheelR(false, 1)+"寸");
		jdsTv.setText(app.deviceNotes.optMotorJds(false, 1)+"");
		volTv.setText(app.deviceNotes.opeMotorVol(false, 1)+"V");
		dangTv.setText(app.deviceNotes.opeMotorDang(false, "低"));
		
		lockCb.setChecked(app.deviceNotes.enabledLock(false, 1) == 1 ? true : false);
		unlockCb.setChecked(app.deviceNotes.enabledUnLock(false, 1) == 1 ? true : false);
		findCb.setChecked(app.deviceNotes.enabledAlarm(false, 1) == 1 ? true : false);
		anzhuoCb.setChecked(app.deviceNotes.enabledSeat(false, 1) == 1 ? true : false);
		autolockCb.setChecked(app.deviceNotes.enabledAutoGuard(false, 1) == 1 ? true : false);
		mutelockCb.setChecked(app.deviceNotes.enabledMuteGuard(false, 1) == 1 ? true : false);
		
		lockCb.setOnCheckedChangeListener(checkBoxListener);
		unlockCb.setOnCheckedChangeListener(checkBoxListener);
		findCb.setOnCheckedChangeListener(checkBoxListener);
		anzhuoCb.setOnCheckedChangeListener(checkBoxListener);
		autolockCb.setOnCheckedChangeListener(checkBoxListener);
		mutelockCb.setOnCheckedChangeListener(checkBoxListener);
	}
	
	OnCheckedChangeListener checkBoxListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton cb, boolean state) {
			if(cb == lockCb){
				app.deviceNotes.enabledLock(true,state ? 1: 0);
				if(state){
					app.ble.lock();
				}
			}else if(cb == unlockCb){
				app.deviceNotes.enabledUnLock(true,state ? 1: 0);
				if(state){
					app.ble.unlock();
				}
			}else if(cb == findCb){
				app.deviceNotes.enabledAlarm(true,state ? 1: 0);
				if(state){
					app.ble.findCarAlarm();
				}
			}else if(cb == anzhuoCb){
				app.deviceNotes.enabledSeat(true,state ? 1: 0);
				if(state){
					app.ble.openSeat();
				}
			}else if(cb == autolockCb){
				app.deviceNotes.enabledAutoGuard(true,state ? 1: 0);
				app.ble.setAutoArmRangePercent(state);
			}else if(cb == mutelockCb){
				app.deviceNotes.enabledMuteGuard(true,state ? 1: 0);
				if(state){
					app.ble.setClientArm();
				}else{
					app.ble.openVoice();
				}
			}
		}
	};
	
	
	@Click
	public void ljLayout(){
		DialogUtils.getInstance().showSelectList(this, new String[]{"10","12","14","16","18","20","22","24","26"}, new DialogCallback() {
			@Override
			public void typeStr(String type) {
				ljTv.setText(type+"寸");
				app.deviceNotes.optWheelR(true, Integer.valueOf(type));
			}
		});
	}
	
	@Click
	public void jdsLayout(){
		DialogUtils.getInstance().showSelectList(this, new String[]{"20","23","28","30"}, new DialogCallback() {
			@Override
			public void typeStr(String type) {
				jdsTv.setText(type);
				app.deviceNotes.optMotorJds(true, Integer.valueOf(type));
			}
		});
	}
	@Click
	public void volLayout(){
		DialogUtils.getInstance().showSelectList(this, new String[]{"12","26","48","60","64","72","80"}, new DialogCallback() {
			@Override
			public void typeStr(String type) {
				volTv.setText(type);
				app.deviceNotes.opeMotorVol(true, Integer.valueOf(type));
			}
		});
	}
	
	@Click
	public void speedLayout(){
		DialogUtils.getInstance().showSelectList(this, new String[]{"低","中","高"}, new DialogCallback() {
			@Override
			public void typeStr(String type) {
				dangTv.setText(type);
				app.deviceNotes.opeMotorDang(true, type);
				app.ble.setSensity();
			}
		});
	}
	
	@Override
	public boolean isLogin() {
		// TODO Auto-generated method stub
		return true;
	}
}
