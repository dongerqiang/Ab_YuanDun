package com.wang.android.mode.utils;

import com.xiaofu_yan.blux.blue_guard.BlueGuard;

import android.content.Context;
import android.content.Intent;

public class BroadcastUtils {
	public static final String BLE_CONNECT_STATE = "ble_connect_state";
	public static final int BLE_CONNECTED = 1;
	public static final int BLE_DISCONNECT = 0;
	public static final String KEY_BLE_STATE = "ble_state";
	
	public static final String MILEAGE_ACTION = "mileage_action";
	public static final String MILEAGE_VALUE_KEY = "mileage_value_key";
	public static final String SPEED_VALUE_KEY = "speed_value_key";
	public static final String BATTERY_VALUE_KEY = "battery_value_key";
	public static final String MILEAGE_VALUE_INCREASE_KEY = "mileage_increase_value_key";
	
	private Context ctx;
	private static BroadcastUtils broadcastUtils;
	public static BroadcastUtils getInstance(){
		return broadcastUtils == null ? broadcastUtils = new BroadcastUtils() : broadcastUtils;
	}
	
	private BroadcastUtils(){}
	
	public void init(Context ctx){
		this.ctx = ctx;
	}
	
	public void sendBleState(int state){
		Intent intent = new Intent(BLE_CONNECT_STATE);
		intent.putExtra(KEY_BLE_STATE, state);
		ctx.sendBroadcast(intent);
	}
	
	public void sendMileage(int value){
		Intent intent = new Intent(MILEAGE_ACTION);
		intent.putExtra(MILEAGE_VALUE_KEY, value);
		ctx.sendBroadcast(intent);
	}
	
	public void sendSpeed(int value){
		Intent intent = new Intent(MILEAGE_ACTION);
		intent.putExtra(SPEED_VALUE_KEY, value);
		ctx.sendBroadcast(intent);
	}
	
	public void sendBattery(float value){
		Intent intent = new Intent(MILEAGE_ACTION);
		intent.putExtra(BATTERY_VALUE_KEY, value);
		ctx.sendBroadcast(intent);
	}
	
	public void sendMileageDm(int value){
		Intent intent = new Intent(MILEAGE_ACTION);
		intent.putExtra(MILEAGE_VALUE_INCREASE_KEY, value);
		ctx.sendBroadcast(intent);
	}
	
}
