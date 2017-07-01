package com.xiaofu_yan.blux.smart_bike;

import java.util.UUID;

import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;

import android.os.Bundle;

public class SmartBikeServerConnection extends BlueGuardServerConnection {
	// Public interface
	public static class Delegate {
		public void smartBikeServerConnected(SmartBikeManager smartBikeManager) {}
		public void smartBikeServerDisconnected() {}
	}

	public Delegate delegate;

	// Private members.
	SmartBikeManager mSmartBikeManager;
	
	// Superclass override
	@Override
	protected void onDisconnected() {
		if(delegate != null) {
			delegate.smartBikeServerDisconnected();
		}
	}

	@Override
	protected void foundServerRootObject(UUID serverId, UUID clientId, Bundle msgData) {
		mSmartBikeManager = new SmartBikeManager(this, serverId, clientId, msgData);
		if(delegate != null && mSmartBikeManager != null) {
			delegate.smartBikeServerConnected(mSmartBikeManager);
		}
	}
}
