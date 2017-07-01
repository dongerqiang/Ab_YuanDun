package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import com.xiaofu_yan.blux.le.server.BluxDevice.DisconnectReason;
import com.xiaofu_yan.blux.le.server.BluxDevice.PairResult;


class BluxBlueGuard extends BluxObject{

	// Public types
	enum State {
		UNKNOWN,
		ARMED,
		STOPPED,
		STARTED,
		RUNNING,
		SILENCE
	};
	
	enum AlarmType {
		LOW_ALARM,
		HIGH_ALARM,
		POWER_LEFT_ON
	}

	static class Delegate {
		protected void blueGuardConnected(BluxBlueGuard bg) {};
		protected void blueGuardDisconnected(BluxBlueGuard bg, BluxDevice.DisconnectReason reason) {};

		protected void blueGuardCurrentRange(BluxBlueGuard bg, int percentRange) {};
		protected void blueGuardLeaveFence(BluxBlueGuard bg) {};
		protected void blueGuardEnterFence(BluxBlueGuard bg) {};

		protected void blueGuardAlarm(BluxBlueGuard bg, AlarmType type) {};
		protected void blueGuardAlarmConfig(BluxBlueGuard bg, boolean alarmDevice, boolean notifyPhone) {};
		protected void blueGuardState(BluxBlueGuard bg, State state) {};
		protected void blueGuardName(BluxBlueGuard bg, String name) {};
		protected void blueGuardShockLevel(BluxBlueGuard bg, int level) {};
		protected void blueGuardSerialNumber(BluxBlueGuard bg, String sn) {};
		protected void blueGuardPairPasskey(BluxBlueGuard bg, String passkey) {};
		protected void blueGuardDiscoverIdentifier(BluxBlueGuard bg, String identifier) {};
		protected void blueGuardUpdateData(BluxBlueGuard bg, byte[] data) {};

		protected void blueGuardAccountManager(BluxBlueGuard bg, BluxAccountManager bam) {};
		protected void blueGuardPairResult(BluxBlueGuard bg, BluxDevice.PairResult result, String key) {};
	};


	// Private constants
	private final static int HEART_BEAT_PERIOD 			= 500;
	private final static int OPEN_TRUNK_PERIOD			= 800;
	private final static int RSSI_BUFFER_SIZE			= 5;
	private final static int FENCE_RSSI_MIN             = -75;
	private final static int FENCE_RSSI_MAX             = -30;
	private final static int FENCE_RIBBON_PERCENT       = 4;

	// AD type[0x06]: 128-bit Service UUIDs 
	private final static int AD_TYPE_128_BIT_SERVICE_UUID_MORE = 0x06;
	
	// Private members
	private BluxDevice			mDevice;
	private BluxVDPrivate 		mVDPrivate;
	private BluxVDSmartGuard 	mVDSmartGuard;
	private List<Delegate> 		mDelegates;

	private String 				mName;
	private String 				mIdentifier;
	private State 				mState;

	private boolean 			mArmDeviceOnDisconnect;
	private int 				mFenceRangePercent;
	private int 				mLastRangePercent;
	private List<Integer>		mRssiBuffer;

	//自动布防的时候只响一声
	private boolean				mAutoArmSilent = false;
	private boolean				mAutoDisarmSilent = false;
		
	// Public methods
	BluxBlueGuard(BluxPeripheral peripheral) {
		mDevice = new BluxDevice(peripheral);
		mDevice.delegate = new DeviceDelegate();
		
		mRssiBuffer = new ArrayList<Integer>();
		mDelegates = new ArrayList<Delegate>();

		mName = peripheral.name();
		mIdentifier = peripheral.identifier();
		mState = State.UNKNOWN;

		mFenceRangePercent = -1;
		mArmDeviceOnDisconnect = false;
	}

	protected void terminate() {
		if(mDevice != null) {
			mDevice.terminate();
			mDevice = null;
		}
		if(mRssiBuffer != null) {
			mRssiBuffer = null;
		}
		mDelegates = null;
		mName = null;
		mIdentifier = null;
		super.terminate();
	}

	void registerDelegate(Delegate delegate) {
		if(!mDelegates.contains(delegate))
			mDelegates.add(delegate);
	}

	void unregisterDelegate(Delegate delegate) {
		mDelegates.remove(delegate);
	}

	// property access
	String identifier() {
		return mIdentifier;
	}

	boolean connected() {
		if(mDevice != null)
			return mDevice.connected();
		return false;
	}

	State state() {
		return mState;
	}

	void setState(State state, boolean silent) {
		if(mVDSmartGuard != null)
			mVDSmartGuard.writeState(stateToVdState(state), silent);
	}

	String name() {
		return mName;
	}

	void setName(String name) {
		if(mVDPrivate != null)
			mVDPrivate.writeName(name);
	}

	int fenceRangePercent()	{
		return mFenceRangePercent;
	}

	int currentRangePercent() {
		float percent = 0;
		float rssi = 0;

		if(mRssiBuffer != null && mRssiBuffer.size() > 0) {
			for(Integer n: mRssiBuffer)
				rssi += n;
			rssi /= (int)mRssiBuffer.size();
		}
		else {
			rssi = -100;
		}
		percent = (FENCE_RSSI_MAX - rssi) * 100 / (FENCE_RSSI_MAX - FENCE_RSSI_MIN);

		return (int)percent;
	}

	// operations
	BluxAccountManager getAccountManager() {
		if(mDevice != null) {
			return mDevice.getAccountManager();
		}
		return null;
	}

	void connect() {
		if(mDevice != null) {
			mDevice.connect();
		}
	}

	void cancelConnect() {
		if(mDevice != null) {
			mDevice.cancelConnect();
		}
	}
	
	void passPair(int pass) {
		if(mDevice != null) {
			mDevice.passPair(pass);
		}
	}
	
	void cancelPair() {
		if(mDevice != null) {
			mDevice.cancelPair();
		}
	}
	
	void setKey(String key) {
		if(mDevice != null) {
			mDevice.setKey(key);
		}
	}
	
	void playSound(int id) {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.playMusic((byte)id);
		}
	}

	void openTrunk() {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.openTrunk(OPEN_TRUNK_PERIOD);
		}
	}

	void getSerialNumber() {
		if(mVDPrivate != null) {
			mVDPrivate.getSerialNumber();
		}
	}

	void getPairPasskey() {
		if(mVDPrivate != null) {
			mVDPrivate.getPairPasskey();
		}
	}

	void getAlarmConfig() {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.readAlarmConfig();
		}
	}

	void setAlarmConfig(boolean alarmDevice, boolean notifyPhone) {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.writeAlarmConfig(alarmDevice, notifyPhone);
		}
	}

	void getShockSensitivity() {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.readShockSensitivity();
		}
	}

	void setShockSensitivity(int level) {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.writeShockSensitivity((byte) level);
		}
	}
	
	void getDiscoverable() {
		mVDPrivate.readBroadcastAD(null);
	}
	
	void setDiscoverable(boolean enable) {
		byte[] ad = null;
		if(enable) {
			UUID uuid = UUID.randomUUID();
			long lsb, msb;
			lsb = uuid.getLeastSignificantBits();
			msb = uuid.getMostSignificantBits();
			ad = new byte[18];
			ad[0] = 17;
			ad[1] = AD_TYPE_128_BIT_SERVICE_UUID_MORE;
			ad[2] = (byte) (lsb & 0xff);
			ad[3] = (byte) ((lsb >> 8) & 0xff);
			ad[4] = (byte) ((lsb >> 16) & 0xff);
			ad[5] = (byte) ((lsb >> 24) & 0xff);
			ad[6] = (byte) ((lsb >> 32) & 0xff);
			ad[7] = (byte) ((lsb >> 40) & 0xff);
			ad[8] = (byte) ((lsb >> 48) & 0xff);
			ad[9] = (byte) ((lsb >> 56) & 0xff);
			ad[10] = (byte) (msb & 0xff);
			ad[11] = (byte) ((msb >> 8) & 0xff);
			ad[12] = (byte) ((msb >> 16) & 0xff);
			ad[13] = (byte) ((msb >> 24) & 0xff);
			ad[14] = (byte) ((msb >> 32) & 0xff);
			ad[15] = (byte) ((msb >> 40) & 0xff);
			ad[16] = (byte) ((msb >> 48) & 0xff);
			ad[17] = (byte) ((msb >> 56) & 0xff);
		}
		mVDPrivate.readBroadcastAD(ad);
	}

	void setMileage(long pulses) {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.writeMileage(pulses);
		}
	}
	
	void setHomeState(int nState) {
		if(mVDSmartGuard != null) {
			mVDSmartGuard.setHomeState(nState);
		}
	}

	void setFenceRangePercent(int fenceRangePercent) {
		fenceRangePercent = (fenceRangePercent >= 0 && fenceRangePercent <= 100) ? fenceRangePercent : -1;

		if(fenceRangePercent != mFenceRangePercent) {
			mFenceRangePercent = fenceRangePercent;
			setArmDeviceOnDisconnect(mFenceRangePercent != -1);
		}
	}

	boolean writeVirtualDevice(UUID uuidType, int register, byte[] data) {
		if(mDevice != null) {
			BluxVirtualDevice vd = mDevice.getVirtualDevice(uuidType);
			if(vd != null)
				return vd.writeRegister(register, data);
		}
		return false;
	}

	// Private methods
	private BluxVDSmartGuard.State stateToVdState(State state) {
		if(state == State.ARMED)
			return BluxVDSmartGuard.State.ARMED;
		else if(state == State.STOPPED)
			return BluxVDSmartGuard.State.STOPPED;
		else if(state == State.STARTED)
			return BluxVDSmartGuard.State.STARTED;
		else if(state == State.RUNNING)
			return BluxVDSmartGuard.State.RUNNING;
		else if(state == State.SILENCE)
			return BluxVDSmartGuard.State.SILENCE;
		return BluxVDSmartGuard.State.UNKNOWN;
	}

	private State vdStateToState(BluxVDSmartGuard.State state) {
		if(state == BluxVDSmartGuard.State.ARMED)
			return State.ARMED;
		else if(state == BluxVDSmartGuard.State.STOPPED)
			return State.STOPPED;
		else if(state == BluxVDSmartGuard.State.STARTED)
			return State.STARTED;
		else if(state == BluxVDSmartGuard.State.RUNNING)
			return State.RUNNING;
		else if(state == BluxVDSmartGuard.State.SILENCE)
			return State.SILENCE;
		return State.UNKNOWN;
	}

	private void setArmDeviceOnDisconnect(boolean armDeviceOnDisconnect) {
		if(mArmDeviceOnDisconnect != armDeviceOnDisconnect) {
			mArmDeviceOnDisconnect = armDeviceOnDisconnect;
			if(mVDSmartGuard != null)
				mVDSmartGuard.setAutoArm(mArmDeviceOnDisconnect);
		}
	}

	private void updateRSSI(int RSSI) {
		int range;

		mRssiBuffer.add(RSSI);
		if(mRssiBuffer.size() > RSSI_BUFFER_SIZE)
			mRssiBuffer.remove(0);

		range = currentRangePercent();
		if(mFenceRangePercent >= 0 && mFenceRangePercent <= 100 && mRssiBuffer.size() == RSSI_BUFFER_SIZE) {
			int rangeEnter, rangeLeave;
			rangeEnter = mFenceRangePercent - FENCE_RIBBON_PERCENT;
			rangeLeave = mFenceRangePercent + FENCE_RIBBON_PERCENT;
			if(range >= rangeLeave && mLastRangePercent < rangeLeave) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardLeaveFence(this);
				if(mState == State.STOPPED) {
					setState(State.ARMED, mAutoArmSilent);
					mAutoArmSilent = true;
				}
			}
			else if(range <= rangeEnter && mLastRangePercent > rangeEnter) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardEnterFence(this);
				if(mState == State.ARMED) {
					setState(State.STOPPED, mAutoDisarmSilent);
					mAutoDisarmSilent = true;
				}
			}
		}
		mLastRangePercent = range;

		for(Delegate delegate : mDelegates)
			delegate.blueGuardCurrentRange(this, range);
	}

	private void broadcastConnection(boolean connected) {
		Bundle msg = new Bundle();
		msg.putString("sender", identifier());
		msg.putString("action", "connection");
		msg.putBoolean("connected", connected);
		BluxSsManager.broadcast(msg);
	}

	private void broadcastState(State state) {
		Bundle msg = new Bundle();
		msg.putString("sender", identifier());
		msg.putString("action", "state");
		
		if((state == State.STARTED) || (state == State.RUNNING)) {
			mAutoArmSilent = false;
			mAutoDisarmSilent = false;
		}
		
		if(state == State.ARMED)
			msg.putString("state", "armed");
		else if(state == State.RUNNING)
			msg.putString("state", "running");
		else if(state == State.STARTED)
			msg.putString("state", "started");
		else if(state == State.STOPPED)
			msg.putString("state", "stopped");
		else if(state == State.SILENCE)
			msg.putString("state", "silence");
		else
			msg.putString("state", "unknown");
		BluxSsManager.broadcast(msg);
	}

	private void broadcastAlarm(int level) {
		Bundle msg = new Bundle();
		msg.putString("sender", identifier());
		msg.putString("action", "connection");
		msg.putInt("level", level);
		BluxSsManager.broadcast(msg);
	}

	// Private types
	// BluxDevice delegate
	private class DeviceDelegate extends BluxDevice.Delegate {
		@Override
		protected void deviceConnected(BluxDevice device) {
			mVDPrivate = (BluxVDPrivate)device.getVirtualDevice(BluxVDPrivate.VD_TYPE_UUID);
			if(mVDPrivate != null) {
				mVDPrivate.delegate = new VDPrivateDelegate();
				mVDPrivate.writePeerHeartBeat((short) HEART_BEAT_PERIOD);
			}
			
			mVDSmartGuard = (BluxVDSmartGuard)device.getVirtualDevice(BluxVDSmartGuard.VD_TYPE_UUID);
			if(mVDSmartGuard != null) {
				mVDSmartGuard.delegate = new VDSmartGuardDelegate();
				mVDSmartGuard.syncState(mArmDeviceOnDisconnect);
			}

			if(mVDPrivate != null && mVDSmartGuard != null) {
				//mVDPrivate.writeConnectionParam(80, 100, 0, 1000);
				broadcastConnection(true);

				if(mDelegates != null) {
					for(Delegate delegate : mDelegates)
						delegate.blueGuardConnected(BluxBlueGuard.this);
				}
			}
		}
		
		@Override
		protected void deviceDisconnected(BluxDevice device, DisconnectReason reason) {
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardDisconnected(BluxBlueGuard.this, reason);
			}
			mVDPrivate = null;
			mVDSmartGuard = null;
		}
		
		@Override
		protected void devicePairResult(BluxDevice device, PairResult result, String key) {
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardPairResult(BluxBlueGuard.this, result, key);
			}
		}
	}
	
	// BluxVDPrivate delegate
	private class VDPrivateDelegate extends BluxVDPrivate.Delegate {
		@Override
		protected void heartBeat(byte[] data) {
			if(data.length >= 13) {
				int rssi;

				rssi = data[0];
				updateRSSI(rssi);

				if(mDelegates != null) {
					for(Delegate delegate : mDelegates)
						delegate.blueGuardUpdateData(BluxBlueGuard.this, data);
				}
			}
		}

		@Override
		protected void updateSerialNumber(String serialNumber) {
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardSerialNumber(BluxBlueGuard.this, serialNumber);
			}
		}

		@Override
		protected void updateName(String name) {
			mName = name;
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardName(BluxBlueGuard.this, name);
			}
		}

		@Override
		protected void updateHeartBeat(short period) {
		}

		@Override
		protected void updatePeerRole(byte role) {
		}

		@Override
		protected void updateConnectionParam(short timeOut, short minMs, short maxMs,
				short latency) {
		}

		@Override
		protected void updatePairPasskey(String passKey) {
		}

		@Override
		protected void updateBroadcastAD(byte[] ad) {
			String s = null;
			if(ad.length == 18 && ad[0] == 17 && ad[1] == AD_TYPE_128_BIT_SERVICE_UUID_MORE) {
				s = String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X",
						ad[17], ad[16], ad[15], ad[14], ad[13], ad[12], ad[11], ad[10],
						ad[9], ad[8], ad[7], ad[6], ad[5], ad[4], ad[3], ad[2]);
			}
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates) {
					delegate.blueGuardDiscoverIdentifier(BluxBlueGuard.this, s);
				}
			}
		}

		@Override
		protected void updateUserStorageConfig(short size) {
		}

		@Override
		protected void updateUserStorage(short offset, byte[] words) {
		}

		@Override
		protected void updatePrivateNvm(short offset, byte[] words) {
		}
	}

	// BluxVDSmartGuard delegate
	private class VDSmartGuardDelegate extends BluxVDSmartGuard.Delegate {
		@Override
		protected void alarm(byte type) {
			AlarmType t = AlarmType.LOW_ALARM;
			if(type == 1)
				t = AlarmType.HIGH_ALARM;
			else if(type == 2)
				t = AlarmType.POWER_LEFT_ON;
			
			broadcastAlarm(type);

			if(mDelegates != null) {
				for(Delegate delegate : mDelegates) {
					delegate.blueGuardAlarm(BluxBlueGuard.this, t);
				}
			}
		}

		@Override
		protected void updateState(BluxVDSmartGuard.State state) {
			mState = vdStateToState(state);
			broadcastState(mState);

			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardState(BluxBlueGuard.this, mState);
			}
		}

		@Override
		protected void updateShockSensitivity(int level) {
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardShockLevel(BluxBlueGuard.this, level);
			}
		}

		@Override
		protected void updateADC(short mv) {
		}

		@Override
		protected void updateAlarmConfig(boolean alarmDevice, boolean notifyPhone) {
			if(mDelegates != null) {
				for(Delegate delegate : mDelegates)
					delegate.blueGuardAlarmConfig(BluxBlueGuard.this, alarmDevice, notifyPhone);
			}
		}

		@Override
		protected void updateSpeedConfig(int monitorPeriod, int hallSensorCounter) {
		}

		@Override
		protected void updateShockLevelTable(byte[] table) {
		}

		@Override
		protected void updateAlarmMusicConfig(int musicCount, int musicSize,
				int noteCount, int noteSize) {
		}

		@Override
		protected void updateAlarmNote(int noteID, byte[] data) {
		}

		@Override
		protected void updateAlarmMusic(int musicID, byte[] data) {
		}
	}

}
