package com.wang.android;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.wang.android.mode.ble.BleInterface;
import com.wang.android.mode.fragment.KeyRemoteFragment;
import com.wang.android.mode.fragment.MoreFragment;
import com.wang.android.mode.fragment.YiBiaoFragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity {
	@ViewById
	LinearLayout bottomLayout;
	private KeyRemoteFragment  keyRemoteFragment;
	private YiBiaoFragment yiBiaoFragment;
	private MoreFragment moreFragment;
	private static final int REQUEST_ENABLE_BT = 1;
	@ViewById
	View bottomLayoutTemp,titleLayoutTemp;//
	@ViewById
	ImageView logImg;
	private long mExitTime;
	@AfterViews
	public void initViews(){
		
//		MyApplication.app.setSystemBar(this);
		
		keyRemoteFragment = com.wang.android.mode.fragment.KeyRemoteFragment_.builder().build();
		yiBiaoFragment = com.wang.android.mode.fragment.YiBiaoFragment_.builder().build();
		moreFragment = com.wang.android.mode.fragment.MoreFragment_.builder().build();
		
		getSupportFragmentManager().beginTransaction().add(R.id.pageLayout, yiBiaoFragment).
		add( R.id.pageLayout,keyRemoteFragment).add(R.id.pageLayout, moreFragment).
		show(keyRemoteFragment).hide(yiBiaoFragment).hide(moreFragment).commit();
		MyApplication.app.ble.initBle(this);
		openBlueTooth();
		
	}
	
	private void openBlueTooth() {
		//判断设备是否支持ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        // 初始化蓝牙适配器
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        // 确保蓝牙在手机上可以开启
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        	
        }
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		BleInterface.getInstance().binder(this);
		
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyApplication.app.ble.mConnection.disconnect();
	}
	
	@Click
	public void remoteImg(){
//		titleLayoutTemp.setVisibility(View.GONE);
//		bottomLayoutTemp.setVisibility(View.GONE);
		getSupportFragmentManager().beginTransaction().hide(yiBiaoFragment).hide(moreFragment).commit();
		getSupportFragmentManager().beginTransaction().show(keyRemoteFragment).commit();
		bottomLayout.setBackgroundResource(R.drawable.icon_page_remote);
	}
	@Click
	public void yibiaoImg(){
		getSupportFragmentManager().beginTransaction().hide(keyRemoteFragment).hide(moreFragment).commit();
		getSupportFragmentManager().beginTransaction().show(yiBiaoFragment).commit();
		
//		titleLayoutTemp.setVisibility(View.INVISIBLE);
//		bottomLayoutTemp.setVisibility(View.INVISIBLE);
		bottomLayout.setBackgroundResource(R.drawable.icon_page_yibiao);
		
	}
	@Click
	public void serverImg(){
		bottomLayout.setBackgroundResource(R.drawable.icon_page_server);
	}
	@Click
	public void moreImg(){
		getSupportFragmentManager().beginTransaction().hide(keyRemoteFragment).hide(yiBiaoFragment).commit();
		getSupportFragmentManager().beginTransaction().show(moreFragment).commit();
		
//		titleLayoutTemp.setVisibility(View.INVISIBLE);
//		bottomLayoutTemp.setVisibility(View.INVISIBLE);
		
		bottomLayout.setBackgroundResource(R.drawable.icon_page_more);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else{
				MyApplication.app.ble.closeDisconverBleDevice();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		return true;
	}
}
