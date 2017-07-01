package com.xiaofu_yan.blux.smart_bike;


import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.xiaofu_yan.blux.blue_guard.BlueGuardManager;
import com.xiaofu_yan.blux.blue_guard.BlueGuardServerConnection;

public class SmartBikeManager extends BlueGuardManager {
	private static final boolean bInDebug = false;
	private static final String TAG = "SmartBike";
	
	// Public types
	public static class Delegate {
		public void smartBikeManagerFoundSmartBike(String identifier, String name, int nMode) {}
		public void smartBikeManagerGotSmartBike(SmartBike smartBike) {}
	}

	public Delegate delegate;	
	
	public void setContext(Context context) {
		mContext = context;
	}

	// Public methods.
	public boolean scanSmartBike() {
		mCurrentScanMode = SMART_BIKE_BROADCAST_UUID;
		if(bInDebug) Log.e(TAG, "start runnable and scan smartbike");
		startRunnable();		
		return super.startScan(SMART_BIKE_BROADCAST_UUID);
	}

	public boolean stopScan() {
		stopRunnable();		
		return super.stopScan();
	}

	
	// Private constants
	private final static String  SMART_BIKE_BROADCAST_UUID ="78667579-9C74-44a3-917A-18BAF46C7277";
	private final static String  SMART_GUARD_BROADCAST_UUID="78667579-FEB2-4a37-8672-B67FC391F49E";
	
	public Context mContext;
	private static Handler mHandler = null;
	private static final int mHandleSeconds = 3000;
	private static String mCurrentScanMode = SMART_BIKE_BROADCAST_UUID;
	
	// Private methods
	SmartBikeManager(BlueGuardServerConnection connection, UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId, data);
	}


	protected void onFoundBlueGuard(String identifier, String name) {
		if(delegate != null) {
			if(mCurrentScanMode.equalsIgnoreCase(SMART_BIKE_BROADCAST_UUID)) {
				delegate.smartBikeManagerFoundSmartBike(identifier, name, 1);
			} else {
				delegate.smartBikeManagerFoundSmartBike(identifier, name, 0);
			}
		}
	}

	protected void onGotBlueGuard(BlueGuardServerConnection conn, UUID serverId, UUID clientId, Bundle data) {
		if(delegate != null) {
			SmartBike smartBike = new SmartBike((BlueGuardServerConnection) getConnection(),
					serverId, clientId, data);
			delegate.smartBikeManagerGotSmartBike(smartBike);
		}
	}

	void changeScanMode() {
		if(mCurrentScanMode.equalsIgnoreCase(SMART_BIKE_BROADCAST_UUID)) {
			mCurrentScanMode = SMART_GUARD_BROADCAST_UUID;
			if(bInDebug) Log.e(TAG, "change scan mode for smartguard");
		} else {
			mCurrentScanMode = SMART_BIKE_BROADCAST_UUID;
			if(bInDebug) Log.e(TAG, "change scan mode for smartbike");
		}
				
		if(super.isScanning()) super.stopScan();
		super.startScan(mCurrentScanMode);
	}
	
	void startRunnable() {
		if(null == mHandler) {
			mHandler = new Handler(mContext.getMainLooper());
		}
		if(null != mHandler) {
			mHandler.postDelayed(runnable, mHandleSeconds);//每xx秒执行一次runnable.
		}
	}
	
	void stopRunnable() {
		if(null != mHandler) {
			mHandler.removeCallbacks(runnable);
		}
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			changeScanMode();			
			mHandler.postDelayed(this, mHandleSeconds);
		}
	};
}
