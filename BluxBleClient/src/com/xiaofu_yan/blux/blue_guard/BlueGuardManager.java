package com.xiaofu_yan.blux.blue_guard;

import java.util.UUID;
import android.os.Bundle;
import android.os.Message;

import com.xiaofu_yan.blux.le.client.BluxCsClient;

public class BlueGuardManager extends BluxCsClient {

	public boolean isScanning() {
		return mScanning;
	}

	protected boolean startScan(String devId) {
		boolean ret = true;
		if(!mScanning) {
			Bundle data = new Bundle();
			data.putString("device_uuid", devId);
			if(ret = sendCommand(CMD_START_SCAN, data))
				mScanning = true;
		}
		return ret;
	}

	protected boolean stopScan() {
		boolean ret = true;
		if(mScanning) {
			if(ret = sendCommand(CMD_STOP_SCAN))
				mScanning = false;
		}
		return ret;
	}

	public boolean getDevice(String identifier) {
		Bundle data = new Bundle();
		data.putString("identifier", identifier);
		return sendCommand(CMD_GET_SMART_GUARD, data);
	}


	// Private constants
	private final static int CMD_START_SCAN 		= 1;
	private final static int CMD_STOP_SCAN 			= 2;
	private final static int CMD_GET_SMART_GUARD	= 3;

	private final static int RSP_SCAN_SMART_GUARD 	= 1;
	private final static int RSP_GET_SMART_GUARD 	= 3;


	// Private member
	private boolean mScanning;

	protected BlueGuardManager(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId);
		mScanning = data.getBoolean("scanning");
	}

	//BluxCsClient overrides
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case RSP_SCAN_SMART_GUARD:
			String name = msg.getData().getString("name");
			String identifier = msg.getData().getString("identifier");
			onFoundBlueGuard(identifier, name);
			break;

		case RSP_GET_SMART_GUARD:
			String sServerId = msg.getData().getString("server_id");
			String sClientId = msg.getData().getString("client_id");
			UUID serverId = UUID.fromString(sServerId);
			UUID clientId = UUID.fromString(sClientId);
			onGotBlueGuard((BlueGuardServerConnection) getConnection(),
					serverId, clientId, msg.getData());
		}
	}

	// Subclass override
	protected void onFoundBlueGuard(String identifier, String name) {};
	protected void onGotBlueGuard(BlueGuardServerConnection conn, UUID serverId, UUID clientId, Bundle data) {};
}
