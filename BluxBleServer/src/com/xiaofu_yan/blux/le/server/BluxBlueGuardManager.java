package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.content.Context;


class BluxBlueGuardManager extends BluxObject{

	// Public types
	static class Delegate {
		protected void blueGuardManagerFoundBlueGuard(BluxBlueGuardManager blueGuardManager, BluxBlueGuard blueGuard) {};
	}


	// Private members.
	private BluxCentral 		mCentral;
	private List<Delegate> 		mDelegates;
	private List<BluxBlueGuard> mBlueGuardPool;


	// Public methods
	BluxBlueGuardManager(Context context) {
		mCentral = new BluxCentral(context);
		mCentral.delegate = new BluxCentralDelegate();
		mBlueGuardPool = new ArrayList<BluxBlueGuard>();
		mDelegates = new ArrayList<Delegate>();
	}

	protected void terminate() {
		if(mBlueGuardPool != null) {
			for(BluxBlueGuard blugGuard : mBlueGuardPool)
				blugGuard.terminate();
			mBlueGuardPool = null;
			mCentral.terminate();
			mCentral = null;
			mDelegates = null;
		}
		super.terminate();
	}

	void registerDelegate(Delegate delegate) {
		if(!mDelegates.contains(delegate)) {
			mDelegates.add(delegate);
		}
	}

	void unregisterDelegate(Delegate delegate) {
		mDelegates.remove(delegate);
		if(mDelegates.size() == 0 && mCentral.isScanning()) {
			mCentral.stopScan();
		}
	}

	boolean isScannning() {
		if(mCentral != null) {
			return mCentral.isScanning();
		}
		return false;
	}

	void scan(UUID uuid) {
		if(mCentral != null && uuid != null) {
			UUID[] uuids = new UUID[1];
			uuids[0] = uuid;
			mCentral.startScan(uuids);
		}
	}

	void stopScan() {
		if(mCentral != null) {
			mCentral.stopScan();
		}
	}

	BluxBlueGuard getBlueGuard(String identifier) {
		if(identifier == null || mCentral == null || mBlueGuardPool == null)
			return null;
		
		BluxBlueGuard blueGuard = findInPool(identifier);
		if(blueGuard != null)
			return blueGuard;

		BluxPeripheral peripheral = mCentral.getPeripheral(identifier);
		if(peripheral != null) {
			blueGuard = new BluxBlueGuard(peripheral);
			mBlueGuardPool.add(blueGuard);
		}
		return blueGuard;
	}


	// BluxCentral delegate
	private class BluxCentralDelegate extends BluxCentral.Delegate {
		@Override
		protected void reportPeripheral(BluxPeripheral peripheral, int rssi) {
			if(mBlueGuardPool == null || mDelegates == null)
				return;

			BluxBlueGuard blueGuard = findInPool(peripheral.identifier());
			if(blueGuard == null) {
				blueGuard = new BluxBlueGuard(peripheral);
				mBlueGuardPool.add(blueGuard);
			}
			
			for(Delegate delegate : mDelegates)
				delegate.blueGuardManagerFoundBlueGuard(BluxBlueGuardManager.this, blueGuard);
		}

		@Override
		protected void stateChangeTo(int state) {
		}
	}
	
	// Private methods
	private BluxBlueGuard findInPool(String identifier) {
		for(BluxBlueGuard sg  : mBlueGuardPool) {
			if(sg.identifier().compareTo(identifier) == 0)
				return sg;
		}
		return null;
	}
}
