package com.wang.android.mode.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.wang.android.mode.activity.DeviceDB;
import com.widget.android.utils.SettingShareData;

import android.content.Context;
import android.text.TextUtils;

/***
 * 存储设备记录
 * 
 * @author fu
 *
 */
public class DeviceNotes {

	public final String DEVICE_HISTORY_KEY = "device_history_key";
	public final String DEVICE_LOCK_KEY = "device_lock_key";// 上锁
	public final String DEVICE_UNLOCK_KEY = "device_unlock_key";// 解锁
	public final String DEVICE_ALARM_KEY = "device_alarm_key";// 报警
	public final String DEVICE_SEAT_KEY = "device_seat_key";// 鞍座开启
	public final String DEVICE_AUTO_GUARD_KEY = "device_auto_guard_key";// 自动设防
	public final String DEVICE_MUTE_GUARD_KEY = "device_mute_guard_key";// 静音布防
	public final String WHEEL_R_KEY = "wheel_r_key";// 轮径
	public final String MOTOR_JDS_KEY = "motor_jds_key";// 极对数
	public final String BATTERY_TYPE_key = "battery_type_key";
	public final String MOTOR_DANGWEI = "motor_dangwei_key";//档位
	public final String USE_DEVICE_KEY = "use_device_key";

	SettingShareData set;
	private Context ctx;
	DeviceHistory deviceHistory = new DeviceHistory();
	private static DeviceNotes notes;

	public static DeviceNotes getInstance() {
		return notes == null ? notes = new DeviceNotes() : notes;
	}

	private DeviceNotes() {
	}

	public void init(Context ctx) {
		this.ctx = ctx;
		set = SettingShareData.getInstance(ctx);
		readDeviceList();
	}

	private void readDeviceList() {
		String notes = set.getKeyValueString(DEVICE_HISTORY_KEY, "");
		if (!TextUtils.isEmpty(notes)) {
			deviceHistory = new Gson().fromJson(notes, DeviceHistory.class);
		}

		if (deviceHistory == null)
			deviceHistory = new DeviceHistory();

	}

	public void saveList() {
		String str = new Gson().toJson(deviceHistory);
		set.setKeyValue(DEVICE_HISTORY_KEY, str);
	}

	public void save(DeviceDB.Record record) {
		deviceHistory.add(record);
		saveList();
	}

	public int enabledLock(boolean setOrGet, int state) {
		if (setOrGet) {
			set.setKeyValue(DEVICE_LOCK_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_LOCK_KEY, 1);
	}

	public int enabledUnLock(boolean setOrGet, int state) {
		if (setOrGet) {
			set.setKeyValue(DEVICE_UNLOCK_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_UNLOCK_KEY, 1);
	}

	public int enabledAlarm(boolean setOrGet, int state) {
		if (setOrGet) {
			set.setKeyValue(DEVICE_ALARM_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_ALARM_KEY, 1);
	}

	public int enabledSeat(boolean setOrGet, int state) {
		if (setOrGet) {
			set.setKeyValue(DEVICE_SEAT_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_SEAT_KEY, 1);
	}

	public int enabledAutoGuard(boolean setOrGet, int state) {
		if (setOrGet) {
			set.setKeyValue(DEVICE_AUTO_GUARD_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_AUTO_GUARD_KEY, 1);
	}
	
	public int enabledMuteGuard(boolean setOrGet, int state){
		if (setOrGet) {
			set.setKeyValue(DEVICE_MUTE_GUARD_KEY, state);
		}
		return set.getKeyValueInt(DEVICE_MUTE_GUARD_KEY, 1);
	}
	
	
	public int optWheelR(boolean setOrGet, int value){
		if (setOrGet) {
			set.setKeyValue(WHEEL_R_KEY, value);
		}
		return set.getKeyValueInt(WHEEL_R_KEY, 16);
	}
	
	public int optMotorJds(boolean setOrGet, int value){
		if (setOrGet) {
			set.setKeyValue(MOTOR_JDS_KEY, value);
		}
		return set.getKeyValueInt(MOTOR_JDS_KEY, 23);
	}
	
	public int opeMotorVol(boolean setOrGet, int value){
		if (setOrGet) {
			set.setKeyValue(BATTERY_TYPE_key, value);
		}
		return set.getKeyValueInt(BATTERY_TYPE_key, 60);
	}
	
	public String opeMotorDang(boolean setOrGet, String value){
		if (setOrGet) {
			set.setKeyValue(MOTOR_DANGWEI, value);
		}
		return set.getKeyValueString(MOTOR_DANGWEI, "低");
	}
	
	public void saveUseDevice(DeviceDB.Record record){
		set.setKeyValue(DEVICE_SEAT_KEY, new Gson().toJson(record));
	}
	
	public void delDeviceHistory(String mac){
		if(deviceHistory != null){
			
		}
		for (DeviceDB.Record record : deviceHistory.deviceList) {
			if(record.identifier.equals(mac)){
				deviceHistory.deviceList.remove(record);
				saveList();
				return;
			}
		}
	}

	private class DeviceHistory {
		public List<DeviceDB.Record> deviceList;

		public void add(DeviceDB.Record record) {
			if (deviceList == null)
				deviceList = new ArrayList<DeviceDB.Record>();
			deviceList.add(record);
		}
	}
	
	public float getWheel(){
		  switch(optWheelR(false,1)) {
          case 10: return 0.8007f;
          case 12: return 0.9577f;
          case 14: return 1.1147f;
          case 16: return 1.2717f;
          case 18: return 1.4287f;
          case 20: return 1.6014f;
          case 22: return 1.7584f;
          case 24: return 1.9154f;
          case 26: return 2.0724f;
          default: return 1.2717f;
		  }
	}
}
