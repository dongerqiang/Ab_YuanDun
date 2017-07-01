package com.xiaofu_yan.blux.le.client;

import java.util.UUID;

import android.os.Bundle;
import android.os.Message;

public class BluxCsClient {
	
	// Subclass methods.
	protected BluxCsConnection getConnection() {
		return mConnection;
	}
	
	protected boolean sendCommand(int cmd, Bundle data) {
		if(mConnection != null) {
			Message msg = Message.obtain(null, cmd);
			data.putString("from", mClientId.toString());
			if(mServerId != null)
				data.putString("to", mServerId.toString());
			return mConnection.sendCommand(msg, data);
		}
		return false;
	}

	protected boolean sendCommand(int cmd) {
		return sendCommand(cmd, new Bundle());
	}

	
	// Subclass overrides.
	protected BluxCsClient(BluxCsConnection connection, UUID serverId, UUID clientId) {
		mConnection = connection;
		mServerId = serverId;
		mClientId = clientId;
		mConnection.registerClient(mClientId, this);
	}

	protected void handleMessage(Message msg) {
	}

	
	// Private members.
	private UUID mClientId;
	private UUID mServerId;
	private BluxCsConnection mConnection;
	
}
