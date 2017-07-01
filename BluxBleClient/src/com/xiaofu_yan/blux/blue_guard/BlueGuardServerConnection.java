package com.xiaofu_yan.blux.blue_guard;

import java.util.UUID;

import android.content.Context;
import android.os.Bundle;

import com.xiaofu_yan.blux.le.client.BluxCsConnection;

public class BlueGuardServerConnection extends BluxCsConnection {

	public boolean connect(Context context) {
		return connectServer(context);
	}

	public void disconnect() {
		disconnectServer();
	}


	// BluxCsConnection overrides
	@Override
	protected void onConnected() {
		getServerRootObject();
	}

	@Override
	protected void onDisconnected() {
	}

	@Override
	protected void foundServerRootObject(UUID serverId, UUID clientId, Bundle msgData) {
	}
	
}
