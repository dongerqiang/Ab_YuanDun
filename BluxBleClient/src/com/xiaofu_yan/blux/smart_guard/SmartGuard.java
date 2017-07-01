package com.xiaofu_yan.blux.smart_guard;

import java.util.UUID;

import com.xiaofu_yan.blux.blue_guard.BlueGuard;
import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;

import android.os.Bundle;

public class SmartGuard extends BlueGuard{

	// Public types
	public static class Delegate extends BlueGuard.Delegate {
		public void smartGuardUpdateData(SmartGuard blueGuard, float temperatureInDegree, float speedInKmph, float mileageInKm) {};
		public void smartGuardUpdateBattery(SmartGuard blueGuard, float batteryVoltage, int percentCapacity) {};
	}

	public enum BatteryType {
		BATTERY12V, BATTERY36V, BATTERY48V, BATTERY60V, BATTERY64V, BATTERY72V, BATTERY80V
	}

	public enum WheelDiameter {
		DIAMETER10, DIAMETER16
	}

	// Public methods
	SmartGuard(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId, data);
	}

	// Property accessor
	public WheelDiameter wheelDiameter() {
		return mWheelDiameter;
	}

	public void setWheelDiameter(WheelDiameter wheel) {
		mWheelDiameter = wheel;
	}

	public BatteryType batteryType() {
		return mBatteryType;
	}

	public void setBatteryType(BatteryType battery) {
		mBatteryType = battery;
	}

	// Actions
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
	
	// Private constants
	private final static int CMD_OPEN_TRUNK = 4;
	private final static int CMD_SET_MILEAGE = 10;
	private final static int CMD_SET_HOME_STATE = 11;

	// Private members
	private WheelDiameter mWheelDiameter = WheelDiameter.DIAMETER16;
	private BatteryType mBatteryType = BatteryType.BATTERY48V;

	// data conversion.
	private static class InterpolateItem {
		int idx;
		float v;

		InterpolateItem(int index, float value) {
			idx = index;
			v = value;
		}
	}

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

	// subclass override
	protected void processHeartBeat(byte[] data) {
		if(!(super.delegate instanceof SmartGuard.Delegate))
			return;
		
		SmartGuard.Delegate delegate = (SmartGuard.Delegate)super.delegate;
		if (delegate != null) {
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

			delegate.smartGuardUpdateData(this, temperature, speed / 1000, mileage / 1000);
			delegate.smartGuardUpdateBattery(this, batteryVoltage, capacity);
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

	private int battery_get_capacity(float voltage, BatteryType type) {
		for (int i = 0; i < kBatteryTable.length; i++) {
			if (kBatteryTable[i].type == type) {
				return (int) ((voltage - kBatteryTable[i].min) * 100 / (kBatteryTable[i].max - kBatteryTable[i].min));
			}
		}
		return 50;  //battery type not found.
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

	private float adc_get_battery_voltage(int mv) {
		double vBat = mv;

		//Vadc = Vbat * 10kOhm / (1000kOhm + 10kOhm) : Vbat = 36v, 48v, 60v, 64v, 72v
		//Vadc = Vbat / 101;
		//Vbat = 101 * Vadc
		vBat /= 1000;
		vBat = (vBat * 101) + 0.7;

		return (float) vBat;
	}


	private long pulsesToMeter(WheelDiameter wheel, long pulses) {
		double meter = 0;
		// 28 pulses / circle
		// diameter10: perimeter is 1.319m	diameter12: perimeter is 1.602m	diameter16: perimeter is 1.445m
		if (wheel == WheelDiameter.DIAMETER10) {
			meter = pulses / 28.0 * 1.319;
		} else if (wheel == WheelDiameter.DIAMETER16) {
			meter = pulses / 28.0 * 1.445;
		}
		return (long) meter;
	}

	private long meterToPulses(WheelDiameter wheel, double meter) {
		double pulses = 0;
		// 28 pulses / circle
		// diameter10: perimeter is 1.319m	diameter12: perimeter is 1.602m	diameter16: perimeter is 1.445m
		if (wheel == WheelDiameter.DIAMETER10) {
			pulses = meter / 1.319 * 28.0;
		} else if (wheel == WheelDiameter.DIAMETER16) {
			pulses = meter / 1.445 * 28.0;
		}
		return (long) pulses;
	}
	
	// Package level helper methods
	static short le2s(byte[] buffer, int offset) {
		short s;
		s = buffer[offset + 1];
		s = (short) ((s << 8) | (buffer[offset] & 0xff));
		return s;
	}

	static int le2l(byte[] buffer, int offset) {
		int l;
		l = buffer[offset + 3];
		l = ((l << 8) | (buffer[offset + 2] & 0xff));
		l = ((l << 8) | (buffer[offset + 1] & 0xff));
		l = ((l << 8) | (buffer[offset] & 0xff));
		return l;
	}
}
