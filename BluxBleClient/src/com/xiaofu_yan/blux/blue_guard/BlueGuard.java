/**
 * V2.07.03
 */
package com.xiaofu_yan.blux.blue_guard;

import java.util.UUID;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;

import com.xiaofu_yan.blux.le.client.BluxCsClient;

public class BlueGuard extends BluxCsClient{

	// Public types
	public static class Delegate {
		public void blueGuardConnected(BlueGuard blueGuard) {};
		public void blueGuardDisconnected(BlueGuard blueGuard, DisconnectReason reason) {};
		public void blueGuardRSSI(BlueGuard blueGuard, int rssi) {};

		public void blueGuardAlarm(BlueGuard blueGuard, Alarm type) {};
		public void blueGuardState(BlueGuard blueGuard, State state) {};
		
		public void blueGuardSerialNumber(BlueGuard blueGuard, String sn) {};
		public void blueGuardPairPasskey(BlueGuard blueGuard, String passkey) {};
		public void blueGuardName(BlueGuard blueGuard, String name) {};
		public void blueGuardAlarmConfig(BlueGuard blueGuard, boolean deviceAlarm, boolean notifyPhone) {};
		public void blueGuardShockSensitivity(BlueGuard blueGuard, int level) {};

		public void blueGuardGotAccountManager(BlueGuard blueGuard, BlueGuardAccountManager accountManager) {};
		public void blueGuardPairResult(BlueGuard blueGuard, PairResult result, String key) {};
	}

	public enum DisconnectReason {
		UNKNOWN,
		CLOSED,
		ERROR_PERMISSION,
		ERROR_KEY,
		LINK_LOST
	}

	public enum PairResult {
		SUCCESS,
		ERROR,
		ERROR_PERMISSION,
		ERROR_KEY
	}

	public enum State {
		UNKNOWN,
		ARMED,
		STOPPED,
		STARTED,
		RUNNING,
		SILENCE
	}

	public enum Sound {
		FIND,
		LOW_ALARM,
		HIGH_ALARM
	}
	
	public enum Alarm {
		LOW,
		HIGH,
		POWER_LEFT_ON
	}

	public Delegate delegate;


	// Public methods
	protected BlueGuard(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId);
		mName = data.getString("name");
		mIdentifier = data.getString("identifier");
		mConnected = data.getBoolean("connected");
		mFenceRangePercent = data.getInt("fence_range");
		mState = intToState(data.getInt("state"));
	}

	// Property accessor
	public String identifier() {
		return mIdentifier;
	}

	public boolean connected() {
		return mConnected;
	}

	public State state() {
		return mState;
	}

	public void setState(State state) {
		int s = STATE_UNKNOWN;

		if (state == State.ARMED)
			s = STATE_ARMED;
		else if (state == State.STOPPED)
			s = STATE_STOPPED;
		else if (state == State.STARTED)
			s = STATE_STARTED;
		else if (state == State.SILENCE)
			s = STATE_SILENCE;

		if (s != STATE_UNKNOWN) {
			Bundle d = new Bundle();
			d.putInt("state", s);
			sendCommand(CMD_SET_STATE, d);
		}
	}

	public String name() {
		return mName;
	}

	public void setName(String name) {
		Bundle d = new Bundle();
		d.putString("name", name);
		sendCommand(CMD_SET_NAME, d);
	}

	public int autoArmRangePercent() {
		return mFenceRangePercent;
	}

	public int currentRangePercent() {
		return mCurrentRangePercent;
	}

	public void setAutoArmRangePercent(int percent) {
		Bundle d = new Bundle();
		d.putInt("range", percent);
		sendCommand(CMD_SET_FENCE_RANGE, d);
		mFenceRangePercent = percent;
	}

	// Actions
	public void getAccountManager() {
		sendCommand(CMD_GET_ACCOUNT_MANAGER);
	}

	public void connect() {
		sendCommand(CMD_CONNECT);
	}

	public void cancelConnect() {
		sendCommand(CMD_CANCEL_CONNECT);
	}

	public void pair(int passkey) {
		Bundle d = new Bundle();
		d.putInt("pass", passkey);
		sendCommand(CMD_PAIR, d);
	}
	
	public void cancelPair() {
		sendCommand(CMD_CANCEL_PAIR);
	}

	public void setConnectionKey(String key) {
		Bundle d = new Bundle();
		d.putString("key", key);
		sendCommand(CMD_SET_CONNECTION_KEY, d);
	}

	public void playSound(Sound id) {
		int s = -1;
		if (id == Sound.FIND)
			s = 7;
		else if (id == Sound.LOW_ALARM)
			s = 3;
		else if (id == Sound.HIGH_ALARM)
			s = 2;

		if (s > 0) {
			Bundle d = new Bundle();
			d.putInt("id", s);
			sendCommand(CMD_PLAY_SOUND, d);
		}
	}

	public void getPairPasskey() {
		sendCommand(CMD_GET_PAIR_PASS_KEY);
	}

	public void getSerialNumber() {
		sendCommand(CMD_GET_SERIAL_NUMBER);
	}

	public void getAlarmConfig() {
		sendCommand(CMD_GET_ALARM_CONFIG);
	}

	public void setAlarmConfig(boolean alarmDevice, boolean notifyPhone) {
		Bundle data = new Bundle();
		data.putBoolean("alarm_device", alarmDevice);
		data.putBoolean("notify_phone", notifyPhone);
		sendCommand(CMD_SET_ALARM_CONFIG, data);
	}

	public void getShockSensitivity() {
		sendCommand(CMD_GET_SHOCK_SENSITIVITY);
	}

	public void setShockSensitivity(int level) {
		Bundle data = new Bundle();
		data.putInt("level", level);
		sendCommand(CMD_SET_SHOCK_SENSITIVITY, data);
	}

	@SuppressLint("DefaultLocale")
	public static String serialNumberToFirmwareVersion(String serialNumber) {
		int vMajor = 0;
		int vMinor = 0;

		if (serialNumber != null && serialNumber.length() == 12) {
			char major = serialNumber.charAt(5);
			char minor = serialNumber.charAt(6);

			if (major >= '0' && major <= '9')
				vMajor = major - '0';
			else if (major >= 'a' && major <= 'z')
				vMajor = major - 'a' + 10;
			else if (major >= 'A' && major <= 'Z')
				vMajor = major - 'A' + 10;

			if (minor >= '0' && minor <= '9')
				vMinor = minor - '0';
			else if (minor >= 'a' && minor <= 'z')
				vMinor = minor - 'a' + 10;
			else if (minor >= 'A' && minor <= 'Z')
				vMinor = minor - 'A' + 10;
		}

		String firmwareVersion = String.format("%d.%02d", vMajor, vMinor);
		return firmwareVersion;
	}


	// Private constants
	private final static int CMD_CONNECT                    = 1;
	private final static int CMD_CANCEL_CONNECT             = 2;
	private final static int CMD_PLAY_SOUND                 = 3;
	private final static int CMD_SET_NAME                   = 5;
	private final static int CMD_SET_STATE                  = 6;
	private final static int CMD_SET_FENCE_RANGE            = 7;
	private final static int CMD_SET_ALARM_CONFIG           = 8;
	private final static int CMD_SET_SHOCK_SENSITIVITY      = 9;

	private final static int CMD_GET_ALARM_CONFIG           = 12;
	private final static int CMD_GET_SERIAL_NUMBER          = 13;
	private final static int CMD_GET_SHOCK_SENSITIVITY      = 14;
	private final static int CMD_GET_PAIR_PASS_KEY          = 18;

	private final static int CMD_SET_CONNECTION_KEY         = 21;
	private final static int CMD_PAIR                       = 22;
	private final static int CMD_CANCEL_PAIR				= 23;
	private final static int CMD_GET_ACCOUNT_MANAGER        = 24;
	
	private final static int CMD_WRITE_VIRTUAL_DEVICE		= 100;

	private final static int RSP_ALARM                      = 1;
	private final static int RSP_STATE                      = 2;
	private final static int RSP_NAME                       = 3;
	private final static int RSP_CURRENT_RANGE              = 4;
	private final static int RSP_REPORT_DATA                = 5;

	private final static int RSP_CONNECTED                  = 11;
	private final static int RSP_DISCONNECTED               = 12;
	private final static int RSP_ALARM_CONFIG               = 14;
	private final static int RSP_PAIR                       = 15;
	private final static int RSP_SERIAL_NUMBER              = 16;
	private final static int RSP_SHOCK_SENSITIVITY          = 17;
	private final static int RSP_PAIR_PASS_KEY              = 18;
	private final static int RSP_ACCOUNT_MANAGER            = 19;

	private final static int STATE_UNKNOWN                  = 0;
	private final static int STATE_ARMED                    = 1;
	private final static int STATE_STARTED                  = 2;
	private final static int STATE_STOPPED                  = 3;
	private final static int STATE_RUNNING                  = 4;
	private final static int STATE_SILENCE                  = 5;

	private final static int DISCONNECT_REASON_CLOSED		= 0;
	private final static int DISCONNECT_REASON_PERMISSION	= 1;
	private final static int DISCONNECT_REASON_KEY			= 2;
	private final static int DISCONNECT_REASON_LINK_LOST	= 3;
	
	private final static int PAIR_RESULT_SUCCESS            = 0;
	private final static int PAIR_RESULT_ERROR_PERMISSION   = 2;
	private final static int PAIR_RESULT_ERROR_KEY          = 3;

	private final static int ALARM_TYPE_LOW					= 0;
	private final static int ALARM_TYPE_HIGH				= 1;
	private final static int ALARM_TYPE_POWER_LEFT_ON		= 2;


	// Private members
	private String mName;
	private String mIdentifier;
	private State mState;
	private boolean mConnected;
	private int mCurrentRangePercent;
	private int mFenceRangePercent;


	// Private methods
	private State intToState(int s) {
		State state;
		if (s == STATE_ARMED)
			state = State.ARMED;
		else if (s == STATE_STOPPED)
			state = State.STOPPED;
		else if (s == STATE_STARTED)
			state = State.STARTED;
		else if (s == STATE_RUNNING)
			state = State.RUNNING;
		else if (s == STATE_SILENCE)
			state = State.SILENCE;
		else
			state = State.UNKNOWN;
		return state;
	}

	// BluxCsClient override
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case RSP_ACCOUNT_MANAGER:
			if (delegate != null) {
				String sServerId = msg.getData().getString("server_id");
				String sClientId = msg.getData().getString("client_id");
				UUID serverId = UUID.fromString(sServerId);
				UUID clientId = UUID.fromString(sClientId);
				BlueGuardAccountManager accountManager = new BlueGuardAccountManager((BlueGuardServerConnection) getConnection(),
						serverId, clientId, msg.getData());
				delegate.blueGuardGotAccountManager(this, accountManager);
			}
			break;

		case RSP_CONNECTED:
			mConnected = true;
			if (delegate != null)
				delegate.blueGuardConnected(this);
			break;

		case RSP_DISCONNECTED:
			mConnected = false;
			if (delegate != null) {
				DisconnectReason reason = DisconnectReason.UNKNOWN;
				int r = msg.getData().getInt("reason");
				if (r == DISCONNECT_REASON_CLOSED)
					reason = DisconnectReason.CLOSED;
				else if (r == DISCONNECT_REASON_KEY)
					reason = DisconnectReason.ERROR_KEY;
				else if (r == DISCONNECT_REASON_PERMISSION)
					reason = DisconnectReason.ERROR_PERMISSION;
				else if (r == DISCONNECT_REASON_LINK_LOST)
					reason = DisconnectReason.LINK_LOST;
				delegate.blueGuardDisconnected(this, reason);
			}
			break;

		case RSP_ALARM:
			int type = msg.getData().getInt("type");
			if (delegate != null) {
				if(type == ALARM_TYPE_LOW)
					delegate.blueGuardAlarm(this, Alarm.LOW);
				else if(type == ALARM_TYPE_HIGH)
					delegate.blueGuardAlarm(this, Alarm.HIGH);
				else if(type == ALARM_TYPE_POWER_LEFT_ON)
					delegate.blueGuardAlarm(this, Alarm.POWER_LEFT_ON);
			}
			break;

		case RSP_STATE:
			mState = intToState(msg.getData().getInt("state"));
			if (delegate != null) {
				delegate.blueGuardState(this, mState);
			}
			break;

		case RSP_NAME:
			mName = msg.getData().getString("name");
			if (delegate != null) {
				delegate.blueGuardName(this, mName);
			}
			break;

		case RSP_CURRENT_RANGE:
			mCurrentRangePercent = msg.getData().getInt("range");
			break;

		case RSP_ALARM_CONFIG:
			boolean deviceAlarm = msg.getData().getBoolean("device_alarm");
			boolean notifyPhone = msg.getData().getBoolean("notify_phone");
			if (delegate != null) {
				delegate.blueGuardAlarmConfig(this, deviceAlarm, notifyPhone);
			}
			break;

		case RSP_PAIR:
			if (delegate != null) {
				String key = msg.getData().getString("key");
				int result = msg.getData().getInt("result");
				PairResult pr = PairResult.ERROR;
				if (result == PAIR_RESULT_SUCCESS)
					pr = PairResult.SUCCESS;
				else if (result == PAIR_RESULT_ERROR_PERMISSION)
					pr = PairResult.ERROR_PERMISSION;
				else if (result == PAIR_RESULT_ERROR_KEY)
					pr = PairResult.ERROR_KEY;
				delegate.blueGuardPairResult(this, pr, key);
			}
			break;

		case RSP_PAIR_PASS_KEY:
			if (delegate != null) {
				String passkey = msg.getData().getString("passkey");
				delegate.blueGuardPairPasskey(this, passkey);
			}
			break;

		case RSP_SERIAL_NUMBER:
			String sn = msg.getData().getString("serial_number");
			if (delegate != null) {
				delegate.blueGuardSerialNumber(this, sn);
			}
			break;

		case RSP_SHOCK_SENSITIVITY:
			int sensitivity = msg.getData().getInt("level");
			if (delegate != null) {
				delegate.blueGuardShockSensitivity(this, sensitivity);
			}
			break;

		case RSP_REPORT_DATA:
			byte[] data = msg.getData().getByteArray("data");
			handleRssi(data);
			if (delegate != null) {
				processHeartBeat(data);
			}
			break;

		default:
			break;
		}
	}

	// Private methods.
	private void handleRssi(byte[] data) {
		if(data != null && data.length > 0 && delegate != null) {
			delegate.blueGuardRSSI(this, data[0]);
		}
	}

	// Subclass helper
	protected void writeVirtualDevice(String id, int register, byte[] packet) {
		Bundle data = new Bundle();
		data.putString("device", id);
		data.putInt("register", register);
		data.putByteArray("data", packet);
		sendCommand(CMD_WRITE_VIRTUAL_DEVICE, data);
	}
	
	// Subclass override
	protected void processHeartBeat(byte[] data) {
	}
	
}
