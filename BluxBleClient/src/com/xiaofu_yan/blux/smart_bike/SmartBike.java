package com.xiaofu_yan.blux.smart_bike;

import java.util.UUID;

import com.xiaofu_yan.blux.blue_guard.BlueGuard;
import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;
import android.os.Bundle;
import android.util.Log;

public class SmartBike extends BlueGuard{

	// Public types
	public static class Delegate extends BlueGuard.Delegate {
		public void smartBikeUpdateBattery(SmartBike smartBike, int energyLevel, float currentInAmpere, int batteryCapacity, int batteryChargeCounter) {};
		public void smartBikeUpdateData(SmartBike smartBike, float tempterature, float speedInKmph, float mileageInKm) {};
		public void smartBikeUpdateController(SmartBike smartBike, int ARS, int gear, boolean speedLimited, boolean cruise) {};
		public void smartBikeUpdateMopedLevel(SmartBike smartBike, byte level, byte batteryType) {};
		public void smartBikeUpdateControllerMode(SmartBike smartBike, byte mopedMode, byte electricMode) {};
		public void smartBikeUpdateSineData(SmartBike smartBike, byte[] data) {};
	}

	public enum BatteryType {
		BATTERY12V, BATTERY36V, BATTERY48V, BATTERY60V, BATTERY64V, BATTERY72V, BATTERY80V
	}
	
	public enum WheelDiameter {
		DIAMETER10, DIAMETER16, DIAMETER12, DIAMETER14, DIAMETER18, DIAMETER20, DIAMETER22, DIAMETER24, DIAMETER26
	}

	// Public methods
	SmartBike(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId, data);
	}


	// Actions
	public void startARS() {
		byte[] packet = {1};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_START_ARS, packet);
	}
	
	// Actions
	public void setMopedLevel(byte level) {
		byte[] packet = {level};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_MOPED_LEVEL, packet);
	}
	
	public void setMopedMode(byte mode) {
		byte[] packet = {mode};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_MOPED_MODE, packet);
	}

	public void setElectricMode(byte mode) {
		byte[] packet = {mode};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_ELECTRIC_MODE, packet);
	}
	
	public void setMotorPower() {
		byte[] packet = {1};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_MOTOR_POWER, packet);
	}
	
	public void enableFullSpeed() {
		byte[] packet = {1};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_FULL_SPEED, packet);
	}
	
	public void setCruise(boolean on) {
		byte v = (byte) (on ? 1 : 0);
		byte[] packet = {v};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_CRUISE, packet);
	}
	
	public void setGear(int gear) {
		byte[] packet = {(byte) gear};
		writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_ANANDA_GEAR, packet);
	}

	public BatteryType batteryType() {
		return mBatteryType;
	}

	public void setBatteryType(BatteryType battery) {
		mBatteryType = battery;
	}
	
	public void openTrunk() {
		sendCommand(CMD_OPEN_TRUNK);
	}
	
	public void setMileage(float km) {
		Bundle data = new Bundle();
		long pulses = (meterToPulses(mWheelDiameter, km * 1000) + 99) / 100;
		data.putLong("pulses", pulses);
		sendCommand(CMD_SET_MILEAGE, data);
	}
	
	public void setHomeState(int nState) {
		Bundle data = new Bundle();
		data.putInt("homeState", nState);
		sendCommand(CMD_SET_HOME_STATE, data);
	}
	
	public WheelDiameter wheelDiameter() {
		return mWheelDiameter;
	}

	public void setWheelDiameter(WheelDiameter wheel) {
		mWheelDiameter = wheel;
	}
	
	public int WheelPairParameter() {
		return mWheelPairParameter;
	}
	
	public void setWheelPairParameter(int pairs) {
		mWheelPairParameter = pairs;
	}
	
	public int PolePairs() {
		return mPolePairs;
	}
	
	public void setPolePairs(int nPolePairs) {
		mPolePairs = nPolePairs;
	}

	public int ControllerType() {
		return mControllerType;
	}
	
	public void setControllerType(int nControllerType) {
		mControllerType = nControllerType;
	}
	
	public void sendSineCmd(byte cmd, byte arg0, byte arg1, byte arg2, byte arg3, byte arg4) {
		switch(cmd) {
			case 0x01:
				byte[] packet1 = {arg0, arg1, arg2, arg3, arg4};
				writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_SINE_SET, packet1);
			break;
			case 0x03:
				byte[] packet3 = {arg0};
				writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_SINE_METER_SWITCH, packet3);
			break;
			case 0x05:
				byte[] packet5 = {};
				writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_SINE_FAULT_GET, packet5);
			break;
			case 0x07:
				byte[] packet7 = {arg0};
				writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_SINE_ALARM_SET, packet7);
			break;
			case 0x10:
				byte[] packet9 = {};
				writeVirtualDevice(SMART_BIKE_VD_UUID, REG_SMART_BIKE_SINE_BATTERY_GET, packet9);
			break;
		}
	}
	
	// Private constants
	private final static String SMART_BIKE_VD_UUID 						="78667579-5CDE-483e-A5ED-E9AB6D23A66C";
	private final static int REG_SMART_BIKE_ANANDA_FULL_SPEED        	= 0x10;  /*RW*/
	private final static int REG_SMART_BIKE_ANANDA_START_ARS         	= 0x11;  /*WO*/
	private final static int REG_SMART_BIKE_ANANDA_CRUISE         		= 0x12;  /*WO*/
	private final static int REG_SMART_BIKE_ANANDA_GEAR         		= 0x13;  /*WO*/
	private final static int REG_SMART_BIKE_ANANDA_MOPED_LEVEL         	= 0x20;  /*WO*/
	private final static int REG_SMART_BIKE_ANANDA_MOPED_MODE			= 0x21;  /*WO*/
	private final static int REG_SMART_BIKE_ANANDA_ELECTRIC_MODE		= 0x22;  /*WO*/
	
	private final static int REG_SMART_BIKE_SINE_SET					= 0x30; /*WO 机车设置*/
	private final static int REG_SMART_BIKE_SINE_METER_SWITCH			= 0x31; /*WO 仪表数据开关*/
	private final static int REG_SMART_BIKE_SINE_FAULT_GET				= 0x32; /*WO 错误代码查询*/
	private final static int REG_SMART_BIKE_SINE_ALARM_SET				= 0x33; /*WO 防盗设置*/
	private final static int REG_SMART_BIKE_SINE_BATTERY_GET			= 0x34; /*WO 电池信息查询*/
	
	private final static int REG_SMART_BIKE_MOTOR_POWER					= 0x48;  /*RW*/
	
	private final static int CMD_OPEN_TRUNK = 4;
	private final static int CMD_SET_MILEAGE = 10;
	private final static int CMD_SET_HOME_STATE = 11;

	protected void processHeartBeat(byte[] data) {
		if(!(super.delegate instanceof SmartBike.Delegate))
			return;
		
		SmartBike.Delegate delegate = (SmartBike.Delegate)super.delegate;
		if (delegate != null) {
			if(18 == data.length) {
				/*data[0] = (byte) 0x21;
				data[1] = (byte) 0x24;
				data[3] = (byte) data[3];*/
				
				/*data[2] = (byte) 0x02;
				data[4] = (byte) 60;
				data[5] = (byte) 0xf8;
				data[6] = (byte) 22;
				data[7] = (byte) 130;
				data[8] = (byte) 10;*/
				
				/*data[2] = (byte) 0x84;
				data[4] = (byte) 0x19;
				data[5] = (byte) 0x00;
				data[6] = (byte) 0x01;
				data[7] = (byte) 0xF4;
				data[8] = (byte) 0x0B;
				data[9] = (byte) 0xB8;
				data[10] = (byte) 0x09;
				data[11] = (byte) 0xF6;
				data[12] = (byte) 0x03;
				data[13] = (byte) 0xE8;*/
				
				/*data[2] = (byte) 0x85;
				data[4] = (byte) 0xff;
				data[5] = (byte) 0xff;
				data[6] = (byte) 0xff;
				data[7] = (byte) 0xff;
				data[8] = (byte) 0xff;
				data[9] = (byte) 0xff;
				data[10] = (byte) 0xff;
				data[11] = (byte) 0xff;*/
				
				/*data[2] = (byte) 0x80;
				data[4] = (byte) 0x02;
				data[5] = (byte) 0x02;
				data[6] = (byte) 0xff;
				data[7] = (byte) 0x02;
				data[8] = (byte) 0xff;
				data[9] = (byte) 0x01;
				data[10] = (byte) 0x55;
				data[11] = (byte) 0x50;*/
				
				if(0x24 == data[1]) {
					delegate.smartBikeUpdateSineData(this, data);
				}
				return;
			}
			
			if(0 == this.nDeviceMode) {
				int batteryADC = le2s(data, 3);
				int temperatureADC = le2s(data, 5);
				long speed = le2s(data, 7);
				long mileage = le2l(data, 9);

				batteryADC &= 0xffff;
				temperatureADC &= 0xffff;
				speed &= 0xffffffff;
				mileage &= 0xffffffff;

				float temperature = adc_get_temperature(temperatureADC);
				speed = pulsesToMeter(mWheelDiameter, speed * 3600);
				mileage = pulsesToMeter(mWheelDiameter, mileage * 100);

				float batteryVoltage = adc_get_battery_voltage(batteryADC);
				int capacity = battery_get_capacity(batteryVoltage, mBatteryType);

				delegate.smartBikeUpdateData(this, temperature, speed / 1000, mileage / 1000);
				delegate.smartBikeUpdateBattery(this, capacity, batteryVoltage, 999, 999);
			}
			else if ((data.length == 11) || (data.length == 13)) {
				int chargeCounter = le2s(data, 1);
				float speed = le2s(data, 3);
				float mileage = le2s(data, 5);
				float current = le2s(data, 7);
				int batCap = data[9] & 0xff;
				int temperature = data[10] & 0xff;				

				int el = data[11] & 0xff;
				int ars = (el >> 3) & 7;
				int gear = (el >> 6) & 3;
				el &= 7;

				boolean speedLimited = (data[12] & 0x01) == 0 ? false : true;

				speed *= 0.1;
				mileage *= 0.1;
				current *= 0.01;

				delegate.smartBikeUpdateBattery(this, el, current, batCap, chargeCounter);
				delegate.smartBikeUpdateController(this, ars, gear, speedLimited, false);
				delegate.smartBikeUpdateData(this, temperature, speed, mileage);
			}
			else if ((data.length == 16) || (data.length == 17)) {
				int time_counter = le2l(data, 1);
				int hall_counter = le2l(data, 5);
				int current_counter = le2l(data, 9);				
				Log.w("jinghui", "[RAW  time:" + time_counter + "]" + " [hall:" + hall_counter + "]" + " [current:" + current_counter + "]");
				
				float speed = convert_speed(time_counter, hall_counter);
				float mileage = convert_mileage(hall_counter);
				float current = convert_current(time_counter, current_counter);
				int el = convert_el(time_counter, current_counter);
				
				int batCap = data[13] & 0xff;
				Log.w("jinghui", "[RAW  battery:" + batCap + "]" + " [fault:" + data[14] + "]" + " [state:" + data[15] + "]");

				int ars = data[14] & 0x0f;
				//int ars = convert_ars(data[14]);
				
				byte mopedMode = (byte)((data[14] & 0x10) == 0 ? 0 : 1);
				byte electricMode = (byte)((data[14] & 0x20) == 0 ? 0 : 1);
				delegate.smartBikeUpdateControllerMode(this, mopedMode, electricMode);
				
				int gear = data[15] & 0x03;
				boolean speedLimited = (data[15] & 0x04) == 0 ? false : true;
				boolean cruise = (data[15] & 0x08) == 0 ? false : true;

				mLastTimeCounter = time_counter;
				mLastHallCounter = hall_counter;
				mLastCurrentCounter = current_counter;
				
				if(data.length == 17) {
					byte batteryType = (byte) (data[16] & 0x08);
					byte mopedLevel = (byte) (data[16] & 0x07);
					delegate.smartBikeUpdateMopedLevel(this, mopedLevel, batteryType);
				}
				
				delegate.smartBikeUpdateBattery(this, el, current, batCap, 0/*chargeCounter*/);
				delegate.smartBikeUpdateController(this, ars, gear, speedLimited, cruise);
				delegate.smartBikeUpdateData(this, 0/*temperature*/, speed, mileage);
			}
		}
	}
	
	private static short le2s(byte[] buffer, int offset) {
		short s;
		s = buffer[offset + 1];
		s = (short) ((s << 8) | (buffer[offset] & 0xff));
		return s;
	}
	
	private static int le2l(byte[] buffer, int offset) {
		int l;
		l = buffer[offset + 3];
		l = (int) ((l << 8) | (buffer[offset + 2] & 0xff));
		l = (int) ((l << 8) | (buffer[offset + 1] & 0xff));
		l = (int) ((l << 8) | (buffer[offset] & 0xff));
		return l;
	}

	//Jinghui controller
	private WheelDiameter mWheelDiameter = WheelDiameter.DIAMETER16;
	private BatteryType mBatteryType = BatteryType.BATTERY48V;
	private int mPolePairs = 28;
	private int mControllerType = 0;
	
	private int mWheelPairParameter = 23;
	private int mLastTimeCounter;
	private int mLastHallCounter;
	private int mLastCurrentCounter;
	private float mLastSpeed = 0;
	private float mLastCurrent = 0;
	private int mLastEnergyLevel = 0;
	
	public int nDeviceMode = 0;
	
	private float convert_speed(int time_counter, int hall_counter) {
		if(mLastTimeCounter > time_counter || mLastHallCounter > hall_counter) {
			mLastTimeCounter = 0;
			mLastHallCounter = 0;
		}
		int time = time_counter - mLastTimeCounter;
		int hall = hall_counter - mLastHallCounter;
		
		if(time == 0)
			return mLastSpeed;
		
		// time unit is in .5 second.
		double speed = convert_mileage(hall) * 2 * 3600 / time;
		Log.w("jinghui", "[dT:" + time + " dHall:" + hall + "]");
		Log.w("jinghui", "[speed:" + speed + "]");
		mLastSpeed = (float) speed;
		return (float) speed;
	}
	
	private float convert_mileage(int hall_counter) {
		double km = 0;
		// diameter10: perimeter is 1.319m	diameter12: perimeter is 1.602m	diameter16: perimeter is 1.445m
		// (Pi * D * N) / (6 * P)
		/*if (mWheelDiameter == WheelDiameter.DIAMETER10) {
			km = 1.319 / 1000.0 * hall_counter / 6.0 / mWheelPairParameter;
		} else if (mWheelDiameter == WheelDiameter.DIAMETER16) {
			km = 1.445 / 1000.0 * hall_counter / 6.0 / mWheelPairParameter;
		}*/
		float circumference = wheelDiameterToCircumference(mWheelDiameter);
		km = circumference / 1000.0 * hall_counter / 6.0 / mWheelPairParameter;
		Log.w("jinghui", "[mileage:" + km + "]");
		return (float) (km);
	}
	
	private float convert_current(int time_counter, int current_counter) {
		if(mLastTimeCounter > time_counter || mLastCurrentCounter > current_counter) {
			mLastTimeCounter = 0;
			mLastCurrentCounter = 0;
		}
		int time = time_counter - mLastTimeCounter;
		if(time == 0)
			return mLastCurrent;
		
		int current = (current_counter - mLastCurrentCounter) / time;
		mLastCurrent = current;
		return current;
	}
	
	private int convert_el(int time_counter, int current_counter) {
		if(time_counter == 0)
			return mLastEnergyLevel;
		mLastEnergyLevel = current_counter / time_counter + 1;
		return mLastEnergyLevel;
	}
	
	/*private int convert_ars(int fault) {
		if((fault & 0x0f) == 0)
			return 0;
		if((fault & 0x0f) == 1)
			return 0x01;
		if((fault & 0x0f) == 2)
			return 0x02;
		if((fault & 0x0f) == 4)
			return 0x03;
		if((fault & 0x0f) == 8)
			return 0x05;
		return 0x04;		
	}*/

	private long meterToPulses(WheelDiameter wheel, double meter) {
		double pulses = 0;
		// 28 pulses / circle
		// diameter10: perimeter is 1.319m	diameter12: perimeter is 1.602m	diameter16: perimeter is 1.445m
		/*if (wheel == WheelDiameter.DIAMETER10) {
			pulses = meter / 1.319 * 28.0;
		} else if (wheel == WheelDiameter.DIAMETER16) {
			pulses = meter / 1.445 * 28.0;
		}*/
		float circumference = wheelDiameterToCircumference(wheel);
		pulses = meter / circumference * mPolePairs;
		return (long) pulses;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//this below is copyed from smartguard
	// data conversion.
	private static class InterpolateItem {
		int idx;
		float v;
		InterpolateItem(int index, float value) {
			idx = index;
			v = value;
		}
	}
		
	private static final InterpolateItem[] kTemperatureTable = {
		//NCP??XV103
				new InterpolateItem(36, 125.0f),
				new InterpolateItem(41, 120.0f),
				new InterpolateItem(46, 115.0f),
				new InterpolateItem(53, 110.0f),
				new InterpolateItem(61, 105.0f),
				new InterpolateItem(70, 100.0f),
				new InterpolateItem(81, 95.0f),
				new InterpolateItem(94, 90.0f),
				new InterpolateItem(110, 85.0f),
				new InterpolateItem(128, 80.0f),
				new InterpolateItem(151, 75.0f),
				new InterpolateItem(178, 70.0f),
				new InterpolateItem(211, 65.0f),
				new InterpolateItem(252, 60.0f),
				new InterpolateItem(302, 55.0f),
				new InterpolateItem(363, 50.0f),
				new InterpolateItem(440, 45.0f),
				new InterpolateItem(536, 40.0f),
				new InterpolateItem(656, 35.0f),
				new InterpolateItem(807, 30.0f),
				new InterpolateItem(1000, 25.0f),
				new InterpolateItem(1247, 20.0f),
				new InterpolateItem(1565, 15.0f),
				new InterpolateItem(1978, 10.0f),
				new InterpolateItem(2519, 5.0f),
				new InterpolateItem(3233, 0.0f),
				new InterpolateItem(4181, -5.0f),
				new InterpolateItem(5456, -10.0f),
				new InterpolateItem(7175, -15.0f),
				new InterpolateItem(9533, -20.0f),
				new InterpolateItem(12777, -25.0f),
				new InterpolateItem(17319, -30.0f),
				new InterpolateItem(23739, -35.0f),
				new InterpolateItem(32900, -40.0f)
	};
	
	private static class BatteryRange {
		BatteryType type;
		float min;
		float max;

		BatteryRange(BatteryType battery, float minVolt, float maxVolt) {
			type = battery;
			min = minVolt;
			max = maxVolt;
		}
	}
	
	private static final BatteryRange[] kBatteryTable = {
		new BatteryRange(BatteryType.BATTERY12V, 8.0f, 20.0f),    //don't care for motor cycle.
		new BatteryRange(BatteryType.BATTERY36V, 32.0f, 43.0f),
		new BatteryRange(BatteryType.BATTERY48V, 42.0f, 59.0f),
		new BatteryRange(BatteryType.BATTERY60V, 52.0f, 73.5f),
		new BatteryRange(BatteryType.BATTERY64V, 56.0f, 78.8f),
		new BatteryRange(BatteryType.BATTERY72V, 64.0f, 79.0f),
		new BatteryRange(BatteryType.BATTERY80V, 72.0f, 89.0f)
	};
	
	private float interpolate(int idx, InterpolateItem[] table) {
		float value = table[0].v;
		float last_value = table[0].v;
		int index = table[0].idx;
		int last_index = table[0].idx;

		for (int i = 0; i < table.length; i++) {
			value = table[i].v;
			index = table[i].idx;
			if (idx < table[i].idx) {
				break;
			}
			last_value = value;
			last_index = index;
		}
		if (index > last_index) {
			value = last_value + (value - last_value) * (idx - last_index) / (index - last_index);
		}
		return value;
	}
	
	private float adc_get_temperature(int mv) {
		//Vadc = Vcc * Rx / (200kOhm + Rx) : Vcc = 3300mv
				//-> Vadc * 200kOhm + Vadc * Rx = Vcc * Rx
		//-> Rx(Vcc - Vadc) = 200kOhm * Vadc
		//-> Rx = 200kOhm * Vadc / (Vcc - Vadc)
		float temperature = -1;
		float rx;

		rx = (float) mv * 200 / (3300 - mv);
		if (rx > 0)
			temperature = interpolate((int) (rx * 100), kTemperatureTable);

		return temperature;
	}
	
	private long pulsesToMeter(WheelDiameter wheel, long pulses) {
		double meter = 0;
		// 28 pulses / circle
		// diameter10: perimeter is 1.319m	diameter12: perimeter is 1.602m	diameter16: perimeter is 1.445m
		/*if (wheel == WheelDiameter.DIAMETER10) {
			meter = pulses / 28.0 * 1.319;
		} else if (wheel == WheelDiameter.DIAMETER16) {
			meter = pulses / 28.0 * 1.445;
		}*/
		float circumference = wheelDiameterToCircumference(wheel);
		meter = pulses / mPolePairs * circumference;
		return (long) meter;
	}
	
	private float wheelDiameterToCircumference(WheelDiameter wheel) {
		if(0 == mControllerType) {
			switch(wheel) {
			case DIAMETER10: return 1.319f;
			case DIAMETER16:
			default: return 1.445f;
			}
		} else {
			switch(wheel) {
			case DIAMETER10: return 0.8007f;
			case DIAMETER12: return 0.9577f;
			case DIAMETER14: return 1.1147f;
			case DIAMETER16: return 1.2717f;
			case DIAMETER18: return 1.4287f;
			case DIAMETER20: return 1.6014f;
			case DIAMETER22: return 1.7584f;
			case DIAMETER24: return 1.9154f;
			case DIAMETER26: return 2.0724f;
			default: return 0.8007f;
			}
		}
	}
	
	private float adc_get_battery_voltage(int mv) {
		double vBat = mv;

		//Vadc = Vbat * 10kOhm / (1000kOhm + 10kOhm) : Vbat = 36v, 48v, 60v, 64v, 72v
		//Vadc = Vbat / 101;
		//Vbat = 101 * Vadc
		vBat /= 1000;
		vBat = (vBat * 101) + 0.7;

		return (float) vBat;
	}
	
	private int battery_get_capacity(float voltage, BatteryType type) {
		for (int i = 0; i < kBatteryTable.length; i++) {
			if (kBatteryTable[i].type == type) {
				return (int) ((voltage - kBatteryTable[i].min) * 100 / (kBatteryTable[i].max - kBatteryTable[i].min));
			}
		}
		return 50;  //battery type not found.
	}
}
