package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

class BluxVDSmartGuard extends BluxVirtualDevice{

	// Public types
	enum State {
		UNKNOWN,
		ARMED,
		STOPPED,
		STARTED,
		RUNNING,
		SILENCE
	};

	enum Command {
		ARM,
		SILENT_ARM,
		DISARM
	};

	static class Delegate{
		protected void alarm(byte type) {};
		protected void updateADC(short mv) {};
		protected void updateState(State state) {};
		protected void updateAlarmConfig(boolean alarmDevice, boolean notifyPhone) {};
		protected void updateSpeedConfig(int monitorPeriod, int hallSensorCounter) {};
		protected void updateShockSensitivity(int level) {};
		protected void updateShockLevelTable(byte[] table) {};
		protected void updateAlarmMusicConfig(int musicCount, int musicSize, int noteCount, int noteSize) {};
		protected void updateAlarmNote(int noteID, byte[] data) {};
		protected void updateAlarmMusic(int musicID, byte[] data) {};
	}

	Delegate delegate;


	// Private constants
	final static UUID VD_TYPE_UUID = UUID
			.fromString("78667579-3E4B-4BF2-9D52-5FFE97D10C2A");

	private final static int REG_SMART_GUARD_COMMAND                     = 0x11;  /*WO*/
	private final static int REG_SMART_GUARD_PLAY_MUSIC                  = 0x12;  /*WO*/
	private final static int REG_SMART_GUARD_OPEN_TRUNK					 = 0x13;  /*WO*/
	private final static int REG_SMART_GUARD_ADC                         = 0x21;  /*RO*/
	private final static int REG_SMART_GUARD_MILEAGE					 = 0x22;  /*RW*/
	private final static int REG_SMART_GUARD_STATE                       = 0x32;  /*RW & IRQ*/
	private final static int REG_SMART_GUARD_SYNC_STATE                  = 0x33;  /*WO*/
	private final static int REG_SMART_GUARD_AUTO_ARM                    = 0x34;  /*WO*/
	private final static int REG_SMART_GUARD_ALARM                       = 0x35;  /*RW & IRQ*/
	private final static int REG_SMART_GUARD_SPEED_CONFIG                = 0x41;  /*RW*/
	private final static int REG_SMART_GUARD_SHOCK_SENSITIVITY           = 0x42;  /*RW*/
	private final static int REG_SMART_GUARD_SHOCK_LEVEL_TABLE           = 0x43;  /*RW*/
	private final static int REG_SMART_GUARD_ALARM_MUSIC_CONFIG          = 0x44;  /*RO*/
	private final static int REG_SMART_GUARD_ALARM_NOTE_TABLE            = 0x45;  /*RW*/
	private final static int REG_SMART_GUARD_ALARM_MUSIC_TABLE           = 0x46;  /*RW*/
	private final static int REG_SMART_GUARD_SILENCE_MODE                = 0x47;  /*RW*/

	/*REG_SMART_GUARD_COMMAND*/
	private final static int VD_SMART_GUARD_COMMAND_ARM                  = 0x10;
	private final static int VD_SMART_GUARD_COMMAND_SILENT_ARM           = 0x11;
	private final static int VD_SMART_GUARD_COMMAND_DISARM               = 0x12;

	/*REG_SMART_GUARD_STATE*/
	private final static int VD_SMART_GUARD_STATE_ARMED                  = 0x10;
	private final static int VD_SMART_GUARD_STATE_STOPPED                = 0x11;
	private final static int VD_SMART_GUARD_STATE_STARTED                = 0x12;
	private final static int VD_SMART_GUARD_STATE_RUNNING                = 0x13;
	private final static int VD_SMART_GUARD_STATE_SILENCE                = 0x14;

	private final static int ALARM_SENSITIVITY_LEVEL_MAX                 = 4;


	// Public methods
	BluxVDSmartGuard(BluxServiceVirtualDevice service, Descriptor desc) {
		super(service, desc);
	}

	@Override
	protected void terminate() {
		delegate = null;
		super.terminate();
	}

	void sendCommand(Command command) {
		byte[] data = new byte[1];

		if(command == Command.ARM)
			data[0] = VD_SMART_GUARD_COMMAND_ARM;
		else if(command == Command.SILENT_ARM)
			data[0] = VD_SMART_GUARD_COMMAND_SILENT_ARM;
		else if(command == Command.DISARM)
			data[0] = VD_SMART_GUARD_COMMAND_DISARM;

		writeRegister(REG_SMART_GUARD_COMMAND, data);
	}

	void playMusic(int id) {
		byte[] data = {(byte) id};

		writeRegister(REG_SMART_GUARD_PLAY_MUSIC, data);
	}

	void openTrunk(int period) {
		byte[] data = new byte[2];

		s2le((short) period, data, 0);

		writeRegister(REG_SMART_GUARD_OPEN_TRUNK, data);
	}

	void getADC(byte channel) {
		byte[] data = {channel};

		readRegister(REG_SMART_GUARD_ADC, data);
	}

	void readState() {
		readRegister(REG_SMART_GUARD_STATE, null);
	}

	void writeState(State state, boolean silent) {
		byte[] data = new byte[2];

		data[0] = VD_SMART_GUARD_STATE_RUNNING;

		if(state == State.STARTED)
			data[0] = VD_SMART_GUARD_STATE_STARTED;
		else if(state == State.STOPPED)
			data[0] = VD_SMART_GUARD_STATE_STOPPED;
		else if(state == State.ARMED)
			data[0] = VD_SMART_GUARD_STATE_ARMED;
		else if(state == State.SILENCE)
			data[0] = VD_SMART_GUARD_STATE_SILENCE;

		data[1] = (byte) (silent ? 0 : 1);
		writeRegister(REG_SMART_GUARD_STATE, data);
	}

	void syncState(boolean autoArm) {
		byte[] data = new byte[1];

		data[0] = (byte) (autoArm ? 1 : 0);
		writeRegister(REG_SMART_GUARD_SYNC_STATE, data);
	}

	void setAutoArm(boolean autoArm) {
		byte[] data = new byte[1];

		data[0] = (byte) (autoArm ? 1 : 0);
		writeRegister(REG_SMART_GUARD_AUTO_ARM, data);
	}

	void readAlarmConfig() {
		readRegister(REG_SMART_GUARD_ALARM, null);
	}

	void writeAlarmConfig(boolean alarmDevice, boolean notifyPhone) {
		byte[] data = new byte[2];

		data[0] = (byte) (alarmDevice ? 1 : 0);
		data[1] = (byte) (notifyPhone ? 1 : 0);
		writeRegister(REG_SMART_GUARD_ALARM, data);
	}

	void readSpeedConfig() {
		readRegister(REG_SMART_GUARD_SPEED_CONFIG, null);
	}

	void writeSpeedConfig(int monitorPeriod, int hallSensorCounter) {
		byte[] data = new byte[2];

		hallSensorCounter = (hallSensorCounter <= 0) ? 1 : hallSensorCounter;
		hallSensorCounter = (hallSensorCounter > 255) ? 255 : hallSensorCounter;

		monitorPeriod /= 100;
		monitorPeriod = monitorPeriod <= 0 ? 1 : monitorPeriod;
		monitorPeriod = monitorPeriod > 255 ? 255 : monitorPeriod;

		data[0] = (byte) hallSensorCounter;
		data[1] = (byte) (monitorPeriod/100);
		writeRegister(REG_SMART_GUARD_SPEED_CONFIG, data);
	}

	void readShockSensitivity() {
		readRegister(REG_SMART_GUARD_SHOCK_SENSITIVITY, null);
	}

	void writeShockSensitivity(int level) {
		byte[] data = new byte[1];

		level = level < 0 ? 0 : level;
		level = level > ALARM_SENSITIVITY_LEVEL_MAX ? ALARM_SENSITIVITY_LEVEL_MAX : level;

		data[0] = (byte)level;
		readRegister(REG_SMART_GUARD_SHOCK_SENSITIVITY, data);
	}

	void readShockLevelTable() {
		readRegister(REG_SMART_GUARD_SHOCK_LEVEL_TABLE, null);
	}

	void writeShockLevelTable(byte[] table) {
		writeRegister(REG_SMART_GUARD_AUTO_ARM, table);
	}

	void readAlarmMusicConfig() {
		readRegister(REG_SMART_GUARD_ALARM_MUSIC_CONFIG, null);
	}

	void readAlarmMusic(int musicID) {
		byte[] data = new byte[1];

		data[0] = (byte)musicID;

		readRegister(REG_SMART_GUARD_ALARM_MUSIC_TABLE, data);
	}

	void writeAlarmMusic(int musicID, byte[] data) {
		byte[] d = new byte[data.length + 1];

		d[0] = (byte)musicID;
		arrayCopyToArray(d, 1, data, 0, data.length);

		writeRegister(REG_SMART_GUARD_ALARM_MUSIC_TABLE, d);
	}

	void readAlarmNote(int noteID) {
		byte[] data = new byte[2];

		data[0] = (byte)noteID;
		data[1] = 1;

		readRegister(REG_SMART_GUARD_ALARM_NOTE_TABLE, data);
	}

	void writeAlarmNote(int noteID, byte[] data) {
		byte[] d = new byte[data.length + 1];

		d[0] = (byte)noteID;
		arrayCopyToArray(d, 1, data, 0, data.length);

		writeRegister(REG_SMART_GUARD_ALARM_NOTE_TABLE, d);
	}

	//	void readMileage() {
		//	    readRegister(REG_SMART_GUARD_MILEAGE, null);
		//	}
	//	
	void writeMileage(long pulses) {
		byte[] data = new byte[4];

		l2le((int) pulses, data, 0);

		writeRegister(REG_SMART_GUARD_MILEAGE, data);
	}
	
	void setHomeState(int nState) {
		byte[] data = new byte[4];
		l2le(nState, data, 0);
		writeRegister(REG_SMART_GUARD_SILENCE_MODE, data);
	}

	// BluxVD Overrides
	static boolean isKindOf(UUID uuidType) {
		return (VD_TYPE_UUID.compareTo(uuidType) == 0);
	}

	@Override
	protected void didReadRegister(Object id, int register, boolean success, byte[] data) {
		if(delegate == null || !success || data == null)
			return;

		switch(register) {
		case REG_SMART_GUARD_ADC:
			if(data.length >= 3) {
				short mv = le2s(data, 1);
				delegate.updateADC(mv);
			}
			break;

		case REG_SMART_GUARD_STATE:
			if(data.length != 0) {
				State state = State.UNKNOWN;
				if(data[0] == VD_SMART_GUARD_STATE_ARMED)
					state = State.ARMED;
				else if(data[0] == VD_SMART_GUARD_STATE_STOPPED)
					state = State.STOPPED;
				else if(data[0] == VD_SMART_GUARD_STATE_STARTED)
					state = State.STARTED;
				else if(data[0] == VD_SMART_GUARD_STATE_RUNNING)
					state = State.RUNNING;
				else if(data[0] == VD_SMART_GUARD_STATE_SILENCE)
					state = State.SILENCE;
				delegate.updateState(state);
			}
			break;

		case REG_SMART_GUARD_ALARM:
			if(data.length == 2) {
				delegate.updateAlarmConfig(data[0] != 0 ? true : false, data[1] != 0 ? true : false);
			}
			break;

		case REG_SMART_GUARD_SPEED_CONFIG:
			if(data.length == 2) {
				int count = (int)data[0] & 0xff;
				int period = (int)data[1] & 0xff;
				period *= 100;
				delegate.updateSpeedConfig(period, count);
			}
			break;

		case REG_SMART_GUARD_SHOCK_SENSITIVITY:
			if(data.length != 0) {
				delegate.updateShockSensitivity(le2uc(data, 0));
			}
			break;

		case REG_SMART_GUARD_SHOCK_LEVEL_TABLE:
			if(data.length != 0) {
				delegate.updateShockLevelTable(data);
			}
			break;

		case REG_SMART_GUARD_ALARM_MUSIC_CONFIG:
			if(data.length == 4) {
				delegate.updateAlarmMusicConfig(le2uc(data, 0), le2uc(data, 1), le2uc(data, 2), le2uc(data, 3));
			}
			break;

		case REG_SMART_GUARD_ALARM_NOTE_TABLE:
			if(data.length > 1) {
				byte[] d = new byte[data.length - 1];
				arrayCopyToArray(d, 0, data, 1, data.length - 1);
				delegate.updateAlarmNote(le2uc(data, 0), d);
			}
			break;

		case REG_SMART_GUARD_ALARM_MUSIC_TABLE:
			if(data.length > 1) {
				byte[] d = new byte[data.length - 1];
				arrayCopyToArray(d, 0, data, 1, data.length - 1);
				delegate.updateAlarmMusic(le2uc(data, 0), d);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void irq(byte[] data) {
		int register;

		if(data.length > 0 && delegate != null) {
			register = (((short)data[0]) & 0xff);
			if(register == REG_SMART_GUARD_ALARM) {
				delegate.alarm(data[1]);
			}
			else if(register == REG_SMART_GUARD_STATE) {
				State state = State.UNKNOWN;
				if(data[1] == VD_SMART_GUARD_STATE_ARMED)
					state = State.ARMED;
				else if(data[1] == VD_SMART_GUARD_STATE_STOPPED)
					state = State.STOPPED;
				else if(data[1] == VD_SMART_GUARD_STATE_STARTED)
					state = State.STARTED;
				else if(data[1] == VD_SMART_GUARD_STATE_RUNNING)
					state = State.RUNNING;
				else if(data[1] == VD_SMART_GUARD_STATE_SILENCE)
					state = State.SILENCE;
				delegate.updateState(state);
			}
		}
	}

}
