package com.wang.android.mode.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.wang.android.MyApplication;
import com.wang.android.R;
import com.wang.android.mode.interfaces.IBlueCallback;
import com.wang.android.mode.utils.BroadcastUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@EFragment(R.layout.fragment_yibiao_layout)
public class YiBiaoFragment extends BaseFragment {
	@ViewById
	TextView speedTv;
	@ViewById
	ImageView speedPointImg;
	@ViewById
	LinearLayout mileageLayout;
	@ViewById
	LinearLayout mileageIncreace;
	@ViewById
	FrameLayout bateryFl;
	@ViewById
	ImageView bateryProImg;
	@ViewById
	TextView dangWei;
	TextView[] mileAgeTv ;
	TextView[] mileAgeCurrentTv;
	BleStateReceiver bleStateReceiver;
	
	int tempSpeed = 0;
	
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		app.ble.addBlueCallback(new BleCallBack());
		
		findMileAgeTextViewChild();
		findMileAgeCurrentTextViewChild();
		animationPoint(0,0);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		getActivity().registerReceiver(bleStateReceiver = new BleStateReceiver(), new IntentFilter(BroadcastUtils.MILEAGE_ACTION));

	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String sensitive = MyApplication.app.deviceNotes.opeMotorDang(false, "低");
		int vibrationLevel = 1;
		if("低".equals(sensitive)){
			vibrationLevel = 1;
		}else if("中".equals(sensitive)){
			vibrationLevel = 2;
		}else if("高".equals(sensitive)){
			vibrationLevel = 3;
		}
		
		dangWei.setText(vibrationLevel+"");
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		getActivity().unregisterReceiver(bleStateReceiver);
	}
	
	public void findMileAgeTextViewChild(){
		mileAgeTv = new TextView[6];
		int j = 0;
		for (int i = 0; i < mileageLayout.getChildCount(); i++) {
			View v = mileageLayout.getChildAt(i);
			if(v instanceof TextView){
				mileAgeTv[j++] = (TextView) v;
			}
		}
	}
	
	public void findMileAgeCurrentTextViewChild(){
		mileAgeCurrentTv = new TextView[6];
		int j = 0;
		for (int i = 0; i < mileageIncreace.getChildCount(); i++) {
			View v = mileageIncreace.getChildAt(i);
			if(v instanceof TextView){
				mileAgeCurrentTv[j++] = (TextView) v;
			}
		}
	}
	
	public void animationPoint(int curr,int speed){
		RotateAnimation rotate =new RotateAnimation(-128f+(curr*(260f/60f)),-128f+(speed * (260f/60f)),Animation.RELATIVE_TO_SELF, 
				0.5f,Animation.RELATIVE_TO_SELF,0.5f); 
		rotate.setDuration(500);
		rotate.setFillAfter(true);
		rotate.setInterpolator(new LinearInterpolator());
		speedPointImg.startAnimation(rotate);
		
		
	}
	

	class BleCallBack extends IBlueCallback{
		@Override
		public void deviceUpdate(int speed, int battery) {

		}
	}
	
	private void setBateryPro(float pro){
		
		bateryFl.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		
		int wid = bateryFl.getMeasuredWidth();
		
		wid = wid - wid / 4 ;
		
		int proWid = (int) (pro * 100 * (int)wid /100f);
		
		FrameLayout.LayoutParams params = (LayoutParams) bateryProImg.getLayoutParams();
		params.width = proWid + wid / 4 /3;
	}
	
	int currentSpeed = 0;
	int currentMileRunning = 0;
	
	class BleStateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(isHidden()){
				return;
			}
			if(intent.getAction().equals(BroadcastUtils.MILEAGE_ACTION)){
				if(intent.hasExtra(BroadcastUtils.MILEAGE_VALUE_KEY)){
					int km = intent.getIntExtra(BroadcastUtils.MILEAGE_VALUE_KEY, 0);
					mileAgeTv[0].setText(String.valueOf(km / 100000));
					mileAgeTv[1].setText(String.valueOf(km % 100000 / 10000));
					mileAgeTv[2].setText(String.valueOf(km % 10000 / 1000));
					mileAgeTv[3].setText(String.valueOf(km % 1000 / 100));
					mileAgeTv[4].setText(String.valueOf(km % 100 / 10));
					mileAgeTv[5].setText(String.valueOf(km % 10));
				}else if(intent.hasExtra(BroadcastUtils.SPEED_VALUE_KEY)){
					int speeddKm = intent.getIntExtra(BroadcastUtils.SPEED_VALUE_KEY, 0);
					if(tempSpeed != speeddKm ){
						tempSpeed = speeddKm;
						if(speeddKm >=0 && speeddKm <= 60){
							animationPoint(currentSpeed,speeddKm);
							speedTv.setText(speeddKm+"");
							currentSpeed = speeddKm;
						}
						if(speeddKm >60){
							
							animationPoint(currentSpeed,60);
							speedTv.setText(speeddKm+"");
							currentSpeed = speeddKm;
						}
					}
					
				}else if(intent.hasExtra(BroadcastUtils.BATTERY_VALUE_KEY)){
					float vol = intent.getFloatExtra(BroadcastUtils.BATTERY_VALUE_KEY, 0);
					setBateryPro(vol);
				}else if(intent.hasExtra(BroadcastUtils.MILEAGE_VALUE_INCREASE_KEY)){
					int kmDh = intent.getIntExtra(BroadcastUtils.MILEAGE_VALUE_INCREASE_KEY, 0);
					currentMileRunning+=kmDh;
					mileAgeCurrentTv[0].setText(String.valueOf(currentMileRunning / 100000));
					mileAgeCurrentTv[1].setText(String.valueOf(currentMileRunning % 100000 / 10000));
					mileAgeCurrentTv[2].setText(String.valueOf(currentMileRunning % 10000 / 1000));
					mileAgeCurrentTv[3].setText(String.valueOf(currentMileRunning % 1000 / 100));
					mileAgeCurrentTv[4].setText(String.valueOf(currentMileRunning % 100 / 10));
					mileAgeCurrentTv[5].setText(String.valueOf(currentMileRunning % 10));
				}
				
			}
			
			if(intent.getAction().equals(BroadcastUtils.BLE_CONNECT_STATE)){
				int state = intent.getIntExtra(BroadcastUtils.KEY_BLE_STATE, 0);
				if(state ==1){
					app.showToast("连接成功");
					currentMileRunning =0;
					currentSpeed = 0;
				}else if(state == 0){
//					app.showToast("连接失败");
				}
			}
		}
		
	}
	
	@Click
	public void dangWei(){
		String dang = dangWei.getText().toString().trim();
		if(!TextUtils.isEmpty(dang)){
			int dangInt = Integer.parseInt(dang);
			if(dangInt == 3){
				dangInt =1;
			}else if(dangInt ==1){
				dangInt =2;
			}else if(dangInt ==2){
				dangInt =3;
			}
			dangWei.setText(String.valueOf(dangInt));
			String ss = "低";
			if(dangInt == 1){
				 ss = "低";
			}else if(dangInt == 2){
				 ss = "中";
			}else if(dangInt == 3){
				 ss = "高";
			}else{
				
			}
			app.deviceNotes.opeMotorDang(true, ss);
			MyApplication.app.ble.setSensity();
			
		}
	}
}
