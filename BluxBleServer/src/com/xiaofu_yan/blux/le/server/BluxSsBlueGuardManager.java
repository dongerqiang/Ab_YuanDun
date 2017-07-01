package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;


class BluxSsBlueGuardManager extends BluxSsProxy{

	// Public methods.
	BluxSsBlueGuardManager(BluxBlueGuardManager bgm, UUID processId, Messenger reply) {
		super(processId, reply);
		mBlueGuardManager = bgm;
		mBlueGuardManagerDelegate = new BlueGuardManagerDelegate();
		mBlueGuardManager.registerDelegate(mBlueGuardManagerDelegate);
	}

	void setStateData(Bundle data) {
		data.putBoolean("scanning", mBlueGuardManager.isScannning());
	}

	@Override
	protected void terminate() {
		if(mBlueGuardManager != null) {
			mBlueGuardManager.unregisterDelegate(mBlueGuardManagerDelegate);
			mBlueGuardManagerDelegate = null;
			mBlueGuardManager = null;
		}
		super.terminate();
	}

	// Private constants.
	private final static int CMD_START_SCAN 		= 1;
	private final static int CMD_STOP_SCAN 			= 2;
	private final static int CMD_GET_SMART_GUARD	= 3;

	private final static int RSP_SCAN_SMART_GUARD 	= 1;
	private final static int RSP_GET_SMART_GUARD 	= 3;

	// Private members.
	private BluxBlueGuardManager 				mBlueGuardManager;
	private BlueGuardManagerDelegate			mBlueGuardManagerDelegate;

	// Private helper.
	private BluxSsBlueGuard getSsBlueGuard(String identifier, UUID processId, Messenger reply) {
		BluxSsBlueGuard ssBlueGuard = null;
		BluxBlueGuard blueGuard = mBlueGuardManager.getBlueGuard(identifier);
		if(blueGuard != null) {
			ssBlueGuard = new BluxSsBlueGuard(blueGuard, processId, reply);
		}
		return ssBlueGuard;
	}


	// BluxBleProxySs base class override.
	@Override
	protected boolean handleMessage(Message cmd) {
		if (super.handleMessage(cmd) || mBlueGuardManager == null)
			return true;

		switch (cmd.what) {
		case CMD_START_SCAN:
			String sUuid = cmd.getData().getString("device_uuid");
			try{
				UUID uuid = UUID.fromString(sUuid);
				mBlueGuardManager.scan(uuid);
			}
			catch(Exception exp) {
			}
			break;

		case CMD_STOP_SCAN:
			mBlueGuardManager.stopScan();
			break;

		case CMD_GET_SMART_GUARD:
			String identifier = cmd.getData().getString("identifier");
			UUID processId = BluxSsProxy.getMessageProcess(cmd);
			if(processId != null && cmd.replyTo != null && BluetoothAdapter.checkBluetoothAddress(identifier)) {
				BluxSsBlueGuard ssmg = getSsBlueGuard(identifier, processId, cmd.replyTo);
				if (ssmg != null) {
					Bundle data = new Bundle();
					data.putString("server_id", ssmg.uuid().toString());
					data.putString("client_id", ssmg.clientId().toString());
					ssmg.setStateData(data);
					notifyClient(RSP_GET_SMART_GUARD, data);
				}
			}
			break;

		default:
			return false;
		}
		return true;
	}

	// BluxBlueGuardManager delegate
	private class BlueGuardManagerDelegate extends BluxBlueGuardManager.Delegate {
		@Override
		protected void blueGuardManagerFoundBlueGuard(
				BluxBlueGuardManager blueGuardManager, BluxBlueGuard blueGuard) {

			Bundle data = new Bundle();
			data.putString("identifier", blueGuard.identifier());
			data.putString("name", blueGuard.name());
			notifyClient(RSP_SCAN_SMART_GUARD, data);
		}
	}

}
