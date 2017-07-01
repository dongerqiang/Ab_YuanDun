package com.xiaofu_yan.blux.le.server;

import java.util.HashMap;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

class BluxCentral extends BluxObject{

	static class Delegate {
		protected void reportPeripheral(BluxPeripheral peripheral, int rssi) {}
		protected void stateChangeTo(int state) {}
	}

	Delegate delegate;


	// Public methods
	BluxCentral(Context context) {
		mScanDevicePool = new HashMap<String, BluetoothDevice>();
		mContext = context;
		BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager != null)
			mBluetoothAdapter = manager.getAdapter();
	}

	protected void terminate() {
		if(mScanDevicePool != null) {
			stopScan();
			mBluetoothAdapter = null;
			mScanCallback = null;
			mScanDevicePool = null;
			mScanningServiceUuids = null;
			mContext = null;
			delegate = null;
		}
		super.terminate();
	}

	boolean isScanning() {
		return mScanCallback != null;
	}

	boolean startScan(UUID[] serviceUuids) {
		boolean ret = false;
		if (mBluetoothAdapter != null && mScanCallback == null) {
			synchronized (mScanDevicePool) {
				mScanningServiceUuids = serviceUuids;
			}
			mScanCallback = new ScanCallback();
			ret = mBluetoothAdapter.startLeScan(mScanCallback);
			if(!ret)
				mScanCallback = null;
		}
		return ret;
	}

	void stopScan() {
		if (mBluetoothAdapter != null && mScanCallback != null) {
			mBluetoothAdapter.stopLeScan(mScanCallback);
			synchronized (mScanDevicePool) {
				mScanDevicePool.clear();
				mScanningServiceUuids = null;				
				mScanCallback = null;
			}
		}
	}

	BluxPeripheral getPeripheral(String address) {
		BluxPeripheral peripheral = null;
		if (mBluetoothAdapter != null) {
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			if(device != null)
				peripheral = new BluxPeripheral(mContext, device);
		}
		return peripheral;
	}


	// Private constants.
	final static int CMD_STOP_SCAN 					= 1;
	final static int CMD_START_SCAN					= 2;
	final static int CMD_LIST_SCANNED_DEVICE		= 3;
	final static int CMD_GET_DEVICE					= 4;

	final static int RSP_DEVICE						= 2;
	final static int RSP_LIST_SCANNED_DEVICE		= 3;
	final static int RSP_GET_DEVICE					= 4;


	// Private members.
	private Context 							mContext;
	private BluetoothAdapter 					mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback 	mScanCallback;
	private HashMap<String, BluetoothDevice> 	mScanDevicePool;
	private UUID[] 								mScanningServiceUuids;

	private class ScanReportDelayed extends DelayedAction {
		private int mRssi;
		private BluetoothDevice mDevice;

		ScanReportDelayed(BluetoothDevice device, int rssi) {
			mDevice = device;
			mRssi = rssi;
		}
		protected void act() {
			BluxPeripheral peripheral = new BluxPeripheral(mContext, mDevice);
			if(delegate != null) {
				delegate.reportPeripheral(peripheral, mRssi);
			}
		}
	}

	private class ScanCallback implements BluetoothAdapter.LeScanCallback {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if(mScanDevicePool == null)
				return;

			synchronized(mScanDevicePool) {
				if(!mScanDevicePool.containsKey(device.getAddress()) && compareScanningServices(scanRecord)) {
					mScanDevicePool.put(device.getAddress(), device);
					delayAction(new ScanReportDelayed(device, rssi), 0);
				}
			}
		}

		private boolean compareScanningServices(byte[] ad) {
			UUID[] servericeUuids;

			synchronized(mScanDevicePool) {
				servericeUuids = mScanningServiceUuids;
			}

			if(servericeUuids == null || servericeUuids.length == 0)
				return true;

			int offset;
			for(offset = 0; ad[offset] != 0 && offset < ad.length; ) {
				if(ad[offset] == (byte)17 && ad[offset + 1] == (byte)6) {
					String s = String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X",
							ad[offset + 17], ad[offset + 16], ad[offset + 15], ad[offset + 14], ad[offset + 13], ad[offset + 12], ad[offset + 11], ad[offset + 10], ad[offset + 9],
							ad[offset + 8], ad[offset + 7], ad[offset + 6], ad[offset + 5], ad[offset + 4], ad[offset + 3], ad[offset + 2]);
					UUID uuid = UUID.fromString(s);
					for(int i = 0; i < servericeUuids.length; i++){
						if(servericeUuids[i].compareTo(uuid) == 0) {
							return true;
						}
					}
				}
				offset += ad[offset] + 1;
			}
			return false;
		}
	}


}
