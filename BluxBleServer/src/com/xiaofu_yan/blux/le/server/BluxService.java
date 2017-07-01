package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

class BluxService extends BluxObject {

	// Public Types
	static class Delegate {
		void serviceStarted(BluxService service, boolean success) {};
	};

	Delegate delegate;


	// Private members
	UUID					mBtServiceUUID;
	BluetoothGatt			mBtGatt;
	BluetoothGattService	mBtService;
	BluxPeripheral			mPeripheral;


	// Public methods
	BluxService() {
	}

	protected void terminate() {
		mBtServiceUUID = null;
		mBtGatt = null;
		mBtService = null;
		mPeripheral = null;
		super.terminate();
	}

	UUID btServiceUUID() {
		return mBtServiceUUID;
	}


	// Methods to be override
	protected void onAttached(BluxPeripheral peripheral) {
		mPeripheral = peripheral;
	}

	protected void onDetached() {
		mPeripheral = null;
		mBtGatt = null;
		mBtService = null;
	}

	protected void onConnected(BluetoothGatt btGatt, BluetoothGattService btService) {
		mBtGatt = btGatt;
		mBtService = btService;
	}

	protected void onDisconnected() {
		mBtGatt = null;
		mBtService = null;
	}
	
	protected void onCharacteristicChanged(BluetoothGattCharacteristic ch) {
	}


}
