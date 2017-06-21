package com.wang.android.mode.ble;

import java.util.ArrayList;
import java.util.List;

import com.wang.android.MainActivity;
import com.wang.android.MyApplication;
import com.wang.android.mode.activity.DeviceDB;
import com.wang.android.mode.interfaces.IBlueCallback;
import com.wang.android.mode.utils.BroadcastUtils;
import com.wang.android.mode.utils.ParserData;
import com.xiaofu_yan.blux.blue_guard.BlueGuard;
import com.xiaofu_yan.blux.le.server.BluxSsServer;
import com.xiaofu_yan.blux.smart_bike.SmartBike;
import com.xiaofu_yan.blux.smart_bike.SmartBikeManager;
import com.xiaofu_yan.blux.smart_bike.SmartBikeServerConnection;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

/***
 * 蓝牙接口通讯
 * @author fu
 *
 */
public class BleInterface {
	public SmartBikeServerConnection mConnection;
	public SmartBikeManager mBlueGuardManager;
	public SmartBike mSmartBike;
	private List<IBlueCallback> blueCallbacks = new ArrayList<IBlueCallback>();
	private Context mCtx;
	
	private DeviceDB.Record lastDevice;
	private ParserData parserData = new ParserData();
	
	private static BleInterface bleInterface;
	public static BleInterface getInstance(){
		return bleInterface == null ? bleInterface = new BleInterface() : bleInterface;
	}
	
	private BleInterface(){
		
	}
	
	public void initBle(Context context){
		mCtx = context;
		mConnection = new SmartBikeServerConnection();
		mConnection.delegate = new ServerConnectionDelegate();
		
		lastDevice = DeviceDB.load(mCtx);
		
				
	}
	
	public void disconverBleDevice(){
		if(mBlueGuardManager != null){
			mBlueGuardManager.scanSmartBike();
		}
	}
	
	public void closeDisconverBleDevice(){
		if(mBlueGuardManager != null){
			mBlueGuardManager.stopScan();
		}
	}
	
	public void connectDevice(String mac){
		if(mBlueGuardManager != null){
			mBlueGuardManager.getDevice(mac);
		}
	}
	
	public void disConnectDevice(){
		if(!isSmartBikeAvailable()) return;
		mSmartBike.cancelConnect();
	}
	
	
	public void lock(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			mSmartBike.setState(BlueGuard.State.ARMED);
		}else{
			MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}
		
	}
	
	public void unlock(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			mSmartBike.setState(BlueGuard.State.STOPPED);
		}else{
			 MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}
		
	}
	
	public void openSeat(){
		
		/*if(!isRunning()){
			mSmartBike.openTrunk();
		}else{
			 MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}*/
		mSmartBike.openTrunk();
	}
	
	public void setAutoArmRangePercent(boolean state){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			if(state){
				mSmartBike.setAutoArmRangePercent(50);					
			}else{
				mSmartBike.setAutoArmRangePercent(-1);
			}
		}
		
	}
	
	public  void closeVoice(){
		if(mSmartBike != null){
			mSmartBike.setAlarmConfig(false, true);
		}else{
			MyApplication.app.showToast( "设备未连接！");
		}
	}

	public  void openVoice(){
		if(mSmartBike != null){
			mSmartBike.setAlarmConfig(true, true);
		}else{
			MyApplication.app.showToast( "设备未连接！");
		}
	}
	
	public void setClientArm(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			closeVoice();
			lock();
		}
	}
	
	public void start(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			mSmartBike.setState(BlueGuard.State.STARTED);
		}else{
			 MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}
	}
	
	public void stop(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			mSmartBike.setState(BlueGuard.State.STOPPED);
		}else{
			 MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}
	}
	
	public void setSmartBikeArmConfigTrue(){
		if(mSmartBike != null){
			mSmartBike.setAlarmConfig(true, true);
		}
	}
	
	public void setSensity(){
		if(!isSmartBikeAvailable()) return;
		setSmartBikeArmConfigTrue();
		String sensitive = MyApplication.app.deviceNotes.opeMotorDang(false, "低");
		int vibrationLevel = 1;
		if("低".equals(sensitive)){
			vibrationLevel = 1;
		}else if("中".equals(sensitive)){
			vibrationLevel = 2;
		}else if("高".equals(sensitive)){
			vibrationLevel = 3;
		}
		if(mSmartBike != null){
			mSmartBike.setShockSensitivity(Integer.valueOf(vibrationLevel));			
		}
		
	}
	
	public void findCarAlarm(){
		if(!isSmartBikeAvailable()) return;
		if(!isRunning()){
			mSmartBike.playSound(BlueGuard.Sound.FIND);
		}else{
			 MyApplication.app.showToast("行驶中，无法使用遥控功能!");
		}
	}
	
	public boolean isSmartBikeAvailable(){
		return mSmartBike != null && mSmartBike.connected();
	}
	
	
	
	protected boolean isRunning(){
		if((mSmartBike.state() == BlueGuard.State.RUNNING) /*|| (mSmartBike.state() == BlueGuard.State.STARTED)*/){
			return true;
		}else{
			return false;
		}
	}
	public  void startSmartBikeService(Context context){		
		Notification notification;
		Notification.Builder nb = new Notification.Builder(context);
		nb.setSmallIcon(com.wang.android.R.drawable.ic_launcher);
		nb.setTicker("远盾");
		nb.setContentTitle("远盾");
		nb.setContentText("蓝牙服务正在运行!");
		notification = nb.build();
		Intent settingViewIntent = new Intent();
		settingViewIntent.setClass(context, MainActivity.class);
		notification.contentIntent = PendingIntent.getActivity(context, 0, settingViewIntent, 0);
		System.out.println("---------------------startBluxSsServer");
		BluxSsServer.sharedInstance().setForeGroundNotification(notification);
		BluxSsServer.sharedInstance().start(context);
	}
	
	public void binder(Activity activity){
		if(mBlueGuardManager == null){
			boolean b = mConnection.connect(activity);
			MyApplication.logBug(b ? "mConnection success" :"mConnection fail" );
		}
	}
	
	public void addBlueCallback(IBlueCallback blueCallback) {
		blueCallbacks.add(blueCallback);
	}
	
	public void removeBlueCallBack(IBlueCallback blueCallback){
		blueCallbacks.remove(blueCallback);
	}
	
	// BluxConnection delegate
	class ServerConnectionDelegate extends SmartBikeServerConnection.Delegate {
		@Override
		public void smartBikeServerConnected(SmartBikeManager smartBikeManager) {
			MyApplication.logBug("binder success");
			mBlueGuardManager = smartBikeManager;
			mBlueGuardManager.delegate = new SmartBikeManagerDelegate();
			//mBlueGuardManager.setContext(mCtx);
			
			if(lastDevice != null){
				if(!TextUtils.isEmpty(lastDevice.identifier)){
					mBlueGuardManager.getDevice(lastDevice.identifier);
				}
			}
		}
	}

	// SmartGuardManager delegate
	class SmartBikeManagerDelegate extends SmartBikeManager.Delegate {
		@Override
		public void smartBikeManagerFoundSmartBike(String identifier, String name) {
			if(!TextUtils.isEmpty(identifier)){
				MyApplication.logBug("device identifier:"+identifier);
			}
			
			if(!TextUtils.isEmpty(name)){
				MyApplication.logBug("device name:"+name);
			}
			
			DeviceDB.Record rec = new DeviceDB.Record(name, identifier, null);
			for(IBlueCallback back : blueCallbacks){
				back.discoverDevice(rec);
			}
				
		}

		@Override
		public void smartBikeManagerGotSmartBike(SmartBike smartBike) {
			MyApplication.logBug("smartBikeManagerGotSmartBike");
			mSmartBike = smartBike;
			mSmartBike.delegate = new SmartBikeDelegate();
			
			if(lastDevice != null){
				if(!TextUtils.isEmpty(lastDevice.key)){
					mSmartBike.setConnectionKey(lastDevice.key);
					mSmartBike.connect();
				}else{
					mSmartBike.pair(Integer.decode("000000"));
				}
			}else{
				mSmartBike.pair(Integer.decode("000000"));
			}
			
		}
		
	}
	
	int currentMileage=0;
	// SmartBike delegate
	private class SmartBikeDelegate extends SmartBike.Delegate {
		@Override
		public void blueGuardConnected(BlueGuard blueGuard) {
            mSmartBike.getAccountManager(); //
            MyApplication.app.broadUtils.sendBleState(BroadcastUtils.BLE_CONNECTED);
            
		}
	
		@Override
		public void blueGuardDisconnected(BlueGuard blueGuard, BlueGuard.DisconnectReason reason) {
			 MyApplication.app.broadUtils.sendBleState(BroadcastUtils.BLE_DISCONNECT);
		}

		@Override
		public void blueGuardName(BlueGuard blueGuard, String name) {
			
		}

		@Override
		public void blueGuardState(BlueGuard blueGuard, BlueGuard.State state) {
			
		}
		@Override
		public void smartBikeUpdateData(SmartBike smartBike, byte[] data) {
		//	[-56, -57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 84, 0, 2]
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				if(i == data.length -1){
					sb.append(Integer.toHexString(data[i]));
				}else{
					sb.append(Integer.toHexString(data[i])+":");
				}
				
			}
			
			MyApplication.logBug("data="+sb.toString());
			
			if(data.length == 16){
				int mile = parserData.parserMileage(new byte[]{data[12],data[11],data[10],data[9]});
				MyApplication.app.broadUtils.sendMileage(mile);
				
				if(currentMileage ==0){
					currentMileage = mile;
				}
				if(mile-currentMileage>=0){
					MyApplication.logBug("--increase mile ="+(mile-currentMileage)+" km");
					MyApplication.app.broadUtils.sendMileageDm(mile-currentMileage);
				}
				
				
				currentMileage = mile;
				int signal = data[0] & 0xff;
				signal = signal | 0xffffff00;
				
				MyApplication.app.asrStr = parserData.parserASR(data[13]);
				
				int speed = parserData.parserSpeed(new byte[]{data[8],data[7],data[15],data[14]});
				MyApplication.app.broadUtils.sendSpeed(speed);
				
				float vol = parserData.parserElectricQuantity(new byte[]{data[4],data[3]});
				MyApplication.app.broadUtils.sendBattery(vol);
				
				MyApplication.logBug("--vol="+vol+"v");
				MyApplication.logBug("--signal="+signal+"db");
			}
			MyApplication.logBug("---------");
		}
		
		@Override
		public void blueGuardPairResult(BlueGuard blueGuard, BlueGuard.PairResult result, String key) {
			if(result == BlueGuard.PairResult.SUCCESS){
				DeviceDB.Record rec = new DeviceDB.Record(blueGuard.name(), blueGuard.identifier(), key);
				DeviceDB.save(mCtx, rec);
			}else{
				mSmartBike.pair(Integer.decode("000000"));
			}
			
		}
	}
}
