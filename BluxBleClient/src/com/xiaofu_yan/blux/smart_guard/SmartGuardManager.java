package com.xiaofu_yan.blux.smart_guard;


import java.util.UUID;
import android.os.Bundle;

import com.xiaofu_yan.blux.blue_guard.BlueGuard;
import com.xiaofu_yan.blux.blue_guard.BlueGuardManager;
import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;

public class SmartGuardManager extends BlueGuardManager {

	// Public types
	public static class Delegate extends BlueGuard.Delegate{
		public void smartGuardManagerFoundSmartGuard(String identifier, String name) {}
		public void smartGuardManagerGotSmartGuard(SmartGuard smartGuard) {}
	}

	public Delegate delegate;

	// Public methods.
	public boolean scanSmartGuard() {
		return super.startScan(SMART_GUARD_BROADCAST_UUID);
	}

	public boolean stopScan() {
		return super.stopScan();
	}

	
	// Private constants
	private final static String  SMART_GUARD_BROADCAST_UUID  		="78667579-FEB2-4a37-8672-B67FC391F49E";
	
	
	// Private methods
	SmartGuardManager(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId, data);
	}


	protected void onFoundBlueGuard(String identifier, String name) {
		if(delegate != null) {
			delegate.smartGuardManagerFoundSmartGuard(identifier, name);
		}
	}

	protected void onGotBlueGuard(BlueGuardServerConnection conn, UUID serverId, UUID clientId, Bundle data) {
		if(delegate != null) {
			SmartGuard smartGuard = new SmartGuard((BlueGuardServerConnection) getConnection(),
					serverId, clientId, data);
			delegate.smartGuardManagerGotSmartGuard(smartGuard);
		}
	}

}
