package com.wang.android.mode.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.wang.android.R;
import com.wang.android.mode.utils.BroadcastUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ImageView;

@EFragment(R.layout.frament_key_layout)
public class KeyRemoteFragment extends BaseFragment {
	@ViewById
	ImageView loakImg;
	@ViewById
	ImageView stopImg;
	
	boolean isLock = false;
	boolean isStart = false;
	
	BleStateReceiver bleStateReceiver;
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		
		if(isLock){
			loakImg.setImageResource(R.drawable.select_unlocak_bg);
		}else{
			loakImg.setImageResource(R.drawable.select_lock_bg);
		}
		if(isStart){
			stopImg.setImageResource(R.drawable.select_stop_bg);
		}else{
			stopImg.setImageResource(R.drawable.select_start_bg);
		}
		getActivity().registerReceiver(bleStateReceiver = new BleStateReceiver(), new IntentFilter(BroadcastUtils.BLE_CONNECT_STATE));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().unregisterReceiver(bleStateReceiver);
	}
	@Click
	public void loakImg(){
		if(isLock){
			app.ble.unlock();
		}else{
			app.ble.lock();
		}
		isLock = !isLock;
		
		if(isLock){
			loakImg.setImageResource(R.drawable.select_unlocak_bg);
		}else{
			loakImg.setImageResource(R.drawable.select_lock_bg);
		}
	}
	
	@Click
	public void gpsImg(){
		com.wang.android.mode.activity.CloudSmartControlActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
	}
	@Click
	public void stopImg(){
		if(isStart){
			app.ble.stop();
		}else{
			app.ble.start();
		}
		
		isStart = !isStart;
		
		if(isStart){
			stopImg.setImageResource(R.drawable.select_stop_bg);
		}else{
			stopImg.setImageResource(R.drawable.select_start_bg);
		}
	}
	@Click
	public void checkImg(){
		com.wang.android.mode.activity.CarCheckActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
	}
	@Click
	public void locationImg(){
		app.ble.findCarAlarm();
	}
	@Click
	public void carImg(){
		app.ble.openSeat();
	}
	@Click
	public void blueImg(){
		com.wang.android.mode.activity.ScanActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
	}
	
	class BleStateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals(BroadcastUtils.BLE_CONNECT_STATE)){
				int state = intent.getIntExtra(BroadcastUtils.KEY_BLE_STATE, 0);
				if(state ==1){
					app.showToast("已连接");
				}else{
					app.showToast("连接失败");
				}
			}
		}
		
	}
}
