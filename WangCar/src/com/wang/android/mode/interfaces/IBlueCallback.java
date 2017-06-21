package com.wang.android.mode.interfaces;

import com.wang.android.mode.activity.DeviceDB;

public abstract class IBlueCallback {
	public void discoverDevice(DeviceDB.Record record){}
	public void deviceUpdate(int speed,int battery){}
}
