package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.xiaofu_yan.blux.le.server.BluxBlueGuard.State;

class BluxSsBlueGuard extends BluxSsProxy{

	// Public methods
	BluxSsBlueGuard(BluxBlueGuard blueGuard, UUID processId, Messenger reply) {
		super(processId, reply);
		mBlueGuard = blueGuard;
		mBlueGuardDelegate = new BlueGuardDelegate();
		mBlueGuard.registerDelegate(mBlueGuardDelegate);
	}

	@Override
	protected void terminate() {
		if(mBlueGuard != null) {
			mBlueGuard.unregisterDelegate(mBlueGuardDelegate);
			mBlueGuard = null;
			mBlueGuardDelegate = null;
		}
		super.terminate();
	}

	void setStateData(Bundle data) {
		data.putString("identifier", mBlueGuard.identifier());
		data.putString("name", mBlueGuard.name());
		data.putBoolean("connected", mBlueGuard.connected());
		data.putInt("fence_range", mBlueGuard.fenceRangePercent());
		data.putInt("state", stateToInt(mBlueGuard.state()));
	}

	// Private constants
	private final static int CMD_CONNECT                    = 1;
	private final static int CMD_CANCEL_CONNECT             = 2;
	private final static int CMD_PLAY_SOUND                 = 3;
	private final static int CMD_OPEN_TRUNK                 = 4;
	private final static int CMD_SET_NAME                   = 5;
	private final static int CMD_SET_STATE                  = 6;
	private final static int CMD_SET_FENCE_RANGE            = 7;
	private final static int CMD_SET_ALARM_CONFIG           = 8;
	private final static int CMD_SET_SHOCK_SENSITIVITY      = 9;
	private final static int CMD_SET_MILEAGE                = 10;
	private final static int CMD_SET_HOME_STATE             = 11;

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
	private final static int DISCONNECT_REASON_UNKNOWN		= 4;
	
	private final static int PAIR_RESULT_SUCCESS            = 0;
	private final static int PAIR_RESULT_ERROR              = 1;
	private final static int PAIR_RESULT_ERROR_PERMISSION   = 2;
	private final static int PAIR_RESULT_ERROR_KEY          = 3;
	
	private final static int ALARM_TYPE_LOW					= 0;
	private final static int ALARM_TYPE_HIGH				= 1;
	private final static int ALARM_TYPE_POWER_LEFT_ON		= 2;

	// Private members.
	private BluxBlueGuard 		mBlueGuard;
	private BlueGuardDelegate 	mBlueGuardDelegate;

	private int stateToInt(State state) {
		int s = STATE_UNKNOWN;
		if (state == BluxBlueGuard.State.ARMED)
			s = STATE_ARMED;
		else if (state == BluxBlueGuard.State.STOPPED)
			s = STATE_STOPPED;
		else if (state == BluxBlueGuard.State.STARTED)
			s = STATE_STARTED;
		else if (state == BluxBlueGuard.State.RUNNING)
			s = STATE_RUNNING;
		else if (state == BluxBlueGuard.State.SILENCE)
			s = STATE_SILENCE;
		return s;
	}

	// Baseclass override
	@Override
	protected boolean handleMessage(Message cmd) {
		if (super.handleMessage(cmd) || mBlueGuard == null)
			return true;

		switch (cmd.what) {
		case CMD_GET_ACCOUNT_MANAGER:
			UUID processId = BluxSsProxy.getMessageProcess(cmd);
			BluxAccountManager bam = mBlueGuard.getAccountManager();
			if(bam != null && processId != null && cmd.replyTo != null) {
				Bundle data;
				data = new Bundle();
				BluxSsAccountManager ssbam = new BluxSsAccountManager(bam, processId, cmd.replyTo);
				data.putString("server_id", ssbam.uuid().toString());
				data.putString("client_id", ssbam.clientId().toString());
				ssbam.setStateData(data);
				notifyClient(RSP_ACCOUNT_MANAGER, data);
			}
			break;

		case CMD_CONNECT:
			mBlueGuard.connect();
			break;

		case CMD_SET_CONNECTION_KEY:
			String key = cmd.getData().getString("key");
			if (key != null) {
				mBlueGuard.setKey(key);
			}
			break;

		case CMD_PAIR:
			int pass = cmd.getData().getInt("pass");
			mBlueGuard.passPair(pass);
			break;
			
		case CMD_CANCEL_PAIR:
			mBlueGuard.cancelPair();
			break;

		case CMD_CANCEL_CONNECT:
			mBlueGuard.cancelConnect();
			break;

		case CMD_PLAY_SOUND:
			int id = cmd.getData().getInt("id");
			mBlueGuard.playSound(id);
			break;

		case CMD_OPEN_TRUNK:
			mBlueGuard.openTrunk();
			break;

		case CMD_SET_NAME:
			String name = cmd.getData().getString("name");
			if (name != null) {
				mBlueGuard.setName(name);
			}
			break;

		case CMD_SET_FENCE_RANGE:
			int range = cmd.getData().getInt("range");
			mBlueGuard.setFenceRangePercent(range);
			break;

		case CMD_SET_ALARM_CONFIG:
			boolean alarmDevice = cmd.getData().getBoolean("alarm_device");
			boolean notifyPhone = cmd.getData().getBoolean("notify_phone");
			mBlueGuard.setAlarmConfig(alarmDevice, notifyPhone);
			break;

		case CMD_SET_SHOCK_SENSITIVITY:
			int level = cmd.getData().getInt("level");
			mBlueGuard.setShockSensitivity(level);
			break;

		case CMD_SET_MILEAGE:
			long pulses = cmd.getData().getLong("pulses");
			mBlueGuard.setMileage(pulses);
			break;
		
		case CMD_SET_HOME_STATE:
			int nState = cmd.getData().getInt("homeState");
			mBlueGuard.setHomeState(nState);
			break;

		case CMD_GET_PAIR_PASS_KEY:
			mBlueGuard.getPairPasskey();
			break;

		case CMD_GET_ALARM_CONFIG:
			mBlueGuard.getAlarmConfig();
			break;

		case CMD_GET_SERIAL_NUMBER:
			mBlueGuard.getSerialNumber();
			break;

		case CMD_GET_SHOCK_SENSITIVITY:
			mBlueGuard.getShockSensitivity();
			break;

		case CMD_SET_STATE:
			int state = cmd.getData().getInt("state");
			if (state == STATE_ARMED)
				mBlueGuard.setState(BluxBlueGuard.State.ARMED, false);
			else if (state == STATE_STARTED)
				mBlueGuard.setState(BluxBlueGuard.State.STARTED, false);
			else if (state == STATE_STOPPED)
				mBlueGuard.setState(BluxBlueGuard.State.STOPPED, false);
			else if (state == STATE_SILENCE)
				mBlueGuard.setState(BluxBlueGuard.State.SILENCE, false);
			break;
			
		case CMD_WRITE_VIRTUAL_DEVICE:
			String sDevice = cmd.getData().getString("device");
			int register = cmd.getData().getInt("register");
			byte[] data = cmd.getData().getByteArray("data");
			try{
				UUID idDevice = UUID.fromString(sDevice);
				if(data != null) {
					mBlueGuard.writeVirtualDevice(idDevice, register, data);
				}
			}
			catch(Exception except) {
			}
			break;

		default:
			return false;
		}
		return true;
	}


	// BluxBlueGuard delegate
	private class BlueGuardDelegate extends BluxBlueGuard.Delegate {
		@Override
		protected void blueGuardConnected(BluxBlueGuard bg) {
			notifyClient(RSP_CONNECTED, null);
		}

		@Override
		protected void blueGuardDisconnected(BluxBlueGuard bg, BluxDevice.DisconnectReason reason) {
			Bundle data = new Bundle();

			if (reason == BluxDevice.DisconnectReason.KEY)
				data.putInt("reason", DISCONNECT_REASON_KEY);
			else if (reason == BluxDevice.DisconnectReason.PERMISSION)
				data.putInt("reason", DISCONNECT_REASON_PERMISSION);
			else if (reason == BluxDevice.DisconnectReason.CLOSED)
				data.putInt("reason", DISCONNECT_REASON_CLOSED);
			else if(reason == BluxDevice.DisconnectReason.LINKLOST)
				data.putInt("reason", DISCONNECT_REASON_LINK_LOST);
			else
				data.putInt("reason", DISCONNECT_REASON_UNKNOWN);

			notifyClient(RSP_DISCONNECTED, data);
		}

		@Override
		protected void blueGuardCurrentRange(BluxBlueGuard bg, int percentRange) {
			Bundle data = new Bundle();
			data.putInt("range", percentRange);

			notifyClient(RSP_CURRENT_RANGE, data);
		}

		@Override
		protected void blueGuardPairPasskey(BluxBlueGuard bg, String passkey) {
			Bundle data = new Bundle();
			data.putString("passkey", passkey);

			notifyClient(RSP_PAIR_PASS_KEY, data);
		}

		@Override
		protected void blueGuardAlarm(BluxBlueGuard bg, BluxBlueGuard.AlarmType type) {
			Bundle data = new Bundle();
			int t = ALARM_TYPE_LOW;
			if(type == BluxBlueGuard.AlarmType.HIGH_ALARM)
				t = ALARM_TYPE_HIGH;
			else if(type == BluxBlueGuard.AlarmType.POWER_LEFT_ON)
				t = ALARM_TYPE_POWER_LEFT_ON;
			data.putInt("type", t);
			notifyClient(RSP_ALARM, data);
		}

		@Override
		protected void blueGuardAlarmConfig(BluxBlueGuard bg, boolean deviceAlarm,
				boolean notifyPhone) {
			Bundle data = new Bundle();
			data.putBoolean("device_alarm", deviceAlarm);
			data.putBoolean("notify_phone", notifyPhone);

			notifyClient(RSP_ALARM_CONFIG, data);
		}

		@Override
		protected void blueGuardState(BluxBlueGuard bg, State state) {
			Bundle data = new Bundle();
			data.putInt("state", stateToInt(state));
			notifyClient(RSP_STATE, data);
		}

		@Override
		protected void blueGuardName(BluxBlueGuard bg, String name) {
			Bundle data = new Bundle();
			data.putString("name", name);

			notifyClient(RSP_NAME, data);
		}

		@Override
		protected void blueGuardShockLevel(BluxBlueGuard bg, int level) {
			Bundle data = new Bundle();
			data.putInt("level", level);

			notifyClient(RSP_SHOCK_SENSITIVITY, data);
		}

		@Override
		protected void blueGuardSerialNumber(BluxBlueGuard bg, String sn) {
			Bundle data = new Bundle();
			data.putString("serial_number", sn);

			notifyClient(RSP_SERIAL_NUMBER, data);
		}

		@Override
		protected void blueGuardUpdateData(BluxBlueGuard bg, byte[] data) {
			Bundle d = new Bundle();
			d.putByteArray("data", data);

			notifyClient(RSP_REPORT_DATA, d);
		}

		@Override
		protected void blueGuardPairResult(BluxBlueGuard bg, BluxDevice.PairResult result, String key) {
			Bundle d = new Bundle();
			if (key != null)
				d.putString("key", key);

			int r;
			if (result == BluxDevice.PairResult.SUCCESS)
				r = PAIR_RESULT_SUCCESS;
			else if (result == BluxDevice.PairResult.ERROR_KEY)
				r = PAIR_RESULT_ERROR_KEY;
			else if (result == BluxDevice.PairResult.ERROR_PERMISSION)
				r = PAIR_RESULT_ERROR_PERMISSION;
			else
				r = PAIR_RESULT_ERROR;
			d.putInt("result", r);
			notifyClient(RSP_PAIR, d);
		}
	}

}
