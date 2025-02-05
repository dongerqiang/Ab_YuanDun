package com.wang.android.mode.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.wang.android.MyApplication;
import com.wang.android.R;
import com.wang.android.mode.activity.DeviceDB;
import com.wang.android.mode.utils.BroadcastUtils;
import com.xiaofu_yan.blux.smart_bike.SmartBike;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

@EFragment(R.layout.frament_key_layout)
public class KeyRemoteFragment extends BaseFragment {
	@ViewById
	ImageView loakImg;
	@ViewById
	ImageView stopImg;
	
	@ViewById
	ImageView connect_flag;
	
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
		if(!app.ble.isSmartBikeAvailable()){
			 MyApplication.app.showToast("请先连接设备!");
		}
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
		if(!app.ble.isSmartBikeAvailable()){
			 MyApplication.app.showToast("请先连接设备!");
		}
		
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
		if(!app.ble.isSmartBikeAvailable()){
			 MyApplication.app.showToast("请先连接设备!");
		}
		app.ble.findCarAlarm();
	}
	@Click
	public void carImg(){
		if(!app.ble.isSmartBikeAvailable()){
			 MyApplication.app.showToast("请先连接设备!");
		}
		app.ble.openSeat();
	}
	@Click
	public void blueImg(){
		if(app.ble.isSmartBikeAvailable()){
//            app.ble.disConnectDevice();
			com.wang.android.mode.activity.ScanActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
	       
        }else if(app.ble.getSmartBike() !=null){
            SmartBike smartBike = app.ble.getSmartBike();
            boolean connected = app.ble.getSmartBike().connected();
            if(connected){
                smartBike.cancelConnect();
            }else{
                DeviceDB.Record load = DeviceDB.load(getActivity());
                app.ble.initBle(getActivity());
                if(load != null){
                    if(!TextUtils.isEmpty(load.key)){
                        smartBike.setConnectionKey(load.key);
                        smartBike.connect();
                    }else{
                    	com.wang.android.mode.activity.ScanActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();}
                }

                com.wang.android.mode.activity.ScanActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
            }
        }else{
        	com.wang.android.mode.activity.ScanActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
        }
		
		
	}
	
	class BleStateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals(BroadcastUtils.BLE_CONNECT_STATE)){
				int state = intent.getIntExtra(BroadcastUtils.KEY_BLE_STATE, 0);
				if(state ==1){
					app.showToast("连接成功");
					connect_flag.setVisibility(View.VISIBLE);
					
				}else if(state == 0){
//					app.showToast("连接失败");
					connect_flag.setVisibility(View.INVISIBLE);
				}
				
				if(state == BroadcastUtils.BLE_ARMED){
					isLock = true;
				}else if(state == BroadcastUtils.BLE_DISARMED){
					isLock = false;
				}
				
				if(state == BroadcastUtils.BLE_START){
					isStart = true;
				}else if(state == BroadcastUtils.BLE_STTOPED){
					isStart = false;
				}
				
				if(isLock){
					loakImg.setImageResource(R.drawable.select_lock_bg);
				}else{
					loakImg.setImageResource(R.drawable.select_unlocak_bg);
				}
				if(isStart){
					stopImg.setImageResource(R.drawable.select_stop_bg);
				}else{
					stopImg.setImageResource(R.drawable.select_start_bg);
				}
				if(state == BroadcastUtils.BLE_ALARM_WARNING){
					//报警
					if(dd!=null && dd.isShowing()){
						dd.dismiss();
					}
					showWarnigDialog();
					
				}
			}
		}
		
	}
	private AlertDialog dd;
	public void showWarnigDialog() {
		// TODO Auto-generated method stub
		if(dd == null){
			AlertDialog.Builder warningDialog = new AlertDialog.Builder(getActivity());
			warningDialog.setTitle("车辆报警！").setMessage("您的车辆正在报警中，请确保车辆安全");
			warningDialog.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			warningDialog.setNegativeButton("取消", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			dd = warningDialog.create();
			dd.show();
		}else{
			dd.show();
		}
		
		
	}
}
