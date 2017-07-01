package com.xiaofu_yan.blux.smart_guard;

import java.util.UUID;

import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;

import android.os.Bundle;

public class SmartGuardServerConnection extends BlueGuardServerConnection {
	// Public interface
	public static class Delegate {
		public void smartGuardServerConnected(SmartGuardManager smartGuardManager) {}
		public void smartGuardServerDisconnected() {}
	}

	public Delegate delegate;

	// Private members.
	SmartGuardManager mSmartGuardManager;
	
	// Superclass override
	@Override
	protected void onDisconnected() {
		if(delegate != null) {
			delegate.smartGuardServerDisconnected();
		}
	}

	@Override
	protected void foundServerRootObject(UUID serverId, UUID clientId, Bundle msgData) {
		mSmartGuardManager = new SmartGuardManager(this, serverId, clientId, msgData);
		if(delegate != null && mSmartGuardManager != null) {
			delegate.smartGuardServerConnected(mSmartGuardManager);
		}
	}
}
