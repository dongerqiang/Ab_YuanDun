package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

class BluxPeripheral extends BluxObject{

	// Public types
	static class Delegate {
		void peripheralConnected(BluxPeripheral peripheral) {};
		void peripheralDisconnected(BluxPeripheral peripheral, boolean closed) {};
	}

	Delegate 					delegate;


	//Private types
	private class GattCallback extends BluetoothGattCallback {
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			//  http://stackoverflow.com/questions/22214254/android-ble-connect-slowly
			//  
			//	Passing true to connectGatt() autoconnect argument requests a background connection,
			//	while passing false requests a direct connection. BluetoothGatt#connect() always requests a background connection.
			//	Background connection (according to Bluedroid sources from 4.4.2 AOSP) has scan interval of 1280ms and a window of 11.25ms. 
			//	This corresponds to about 0.9% duty cycle which explains why connections, when not scanning, can take a long time to complete.
			//	Direct connection has interval of 60ms and window of 30ms so connections complete much faster.
			//	Additionally there can only be one direct connection request pending at a time and it times out after 30 seconds.
			//	onConnectionStateChange() gets called with state=2, status=133 to indicate this timeout.
			//	I have verified this behavior on Nexus5 but obviously YMMV.
			//	I should mention that there is a race condition in BluetoothGatt.
			//	java that can cause a direct connection request even if autoconnect=true is passed into BluetoothDevice#connectGatt().

			Log.w("BLUX", "<conn: " + newState + " : " + status);

			if(newState == BluetoothProfile.STATE_CONNECTED) {
				delayAction(new ConnectedDelayed(status), 0);
			}
			else if(newState == BluetoothProfile.STATE_DISCONNECTED){
				delayAction(new DisconnectedDelayed(), 0);
			}
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.w("BLUX", "<serdis: " + status);

			// all services discovered.
			delayAction(new ServiceDiscoveredDelayed(status), 0);
		}

		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.w("BLUX", "<dscrd: " + status);

			delayAction(new CharOrDescReadWriteDelayed(descriptor, null, status == BluetoothGatt.GATT_SUCCESS), 0);
		}

		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.w("BLUX", "<dscwr: " + status);

			delayAction(new CharOrDescReadWriteDelayed(descriptor, null, status == BluetoothGatt.GATT_SUCCESS), 0);
		}

		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.w("BLUX", "<chrrd: " + status);

			delayAction(new CharOrDescReadWriteDelayed(null, characteristic, status == BluetoothGatt.GATT_SUCCESS), 0);
		}

		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.w("BLUX", "<chrwr: " + status);

			delayAction(new CharOrDescReadWriteDelayed(null, characteristic, status == BluetoothGatt.GATT_SUCCESS), 0);
		}

		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.w("BLUX", "<chrchg");

			delayAction(new CharChangeDelayed(characteristic), 0);
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
		}

		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
		}
	}

	static class Transfer {
		protected int 							mRetry;
		protected BluetoothGattCharacteristic 	mChar;
		protected BluetoothGattDescriptor 		mDesc;
		
		void setRetry(int retry) {
			mRetry = retry;
		}
		protected void start(BluetoothGatt gatt) {};
		protected void finished(boolean success) {};
	}
	
	static class ReadCharTransfer extends Transfer {
		ReadCharTransfer(BluetoothGattCharacteristic ch) {
			mChar = ch;
		}
		protected void start(BluetoothGatt gatt) {
			if(gatt != null) {
				gatt.readCharacteristic(mChar);
			}
		}
	}
	
	static class WriteCharTransfer extends Transfer {
		private byte[] mData;
		WriteCharTransfer(BluetoothGattCharacteristic ch, byte[] data) {
			mChar = ch;
			mData = data;
		}
		protected void start(BluetoothGatt gatt) {
			if(gatt != null) {
				mChar.setValue(mData);
				gatt.writeCharacteristic(mChar);
			}
		}
	}
	
	static class ReadDescTransfer extends Transfer {
		ReadDescTransfer(BluetoothGattDescriptor desc) {
			mDesc = desc;
		}
		protected void start(BluetoothGatt gatt) {
			if(gatt != null) {
				gatt.readDescriptor(mDesc);
			}
		}
	}
	
	static class WriteDescTransfer extends Transfer {
		private byte[] mData;
		WriteDescTransfer(BluetoothGattDescriptor desc, byte[] data) {
			mDesc = desc;
			mData = data;
		}
		protected void start(BluetoothGatt gatt) {
			if(gatt != null) {
				mDesc.setValue(mData);
				gatt.writeDescriptor(mDesc);
			}
		}
		protected void finished(boolean success) {};
	}

	private enum State {
		CONNECTING,
		CONNECTED,
		DISCONNECTING,
		DISCONNECTED,
	};


	// Member variables
	private State 						mConnectState;
	private Context 					mContext;
	private BluetoothDevice 			mBtDevice;
	private BluetoothGatt 				mBtGatt;
	private List<BluetoothGattService> 	mBtServices;
	private HashMap<UUID, BluxService> 	mServices;
	private List<Transfer> 				mTransfers;


	// Public methods
	BluxPeripheral(Context context, BluetoothDevice device) {
		mContext = context;
		mBtDevice = device;
		mTransfers = new ArrayList<Transfer>();
		mServices = new HashMap<UUID, BluxService>();
		mConnectState = State.DISCONNECTED;
	}

	protected void terminate() {
		if(mTransfers != null) {
			cancelTransfers();
			mTransfers = null;
		}
		if(mBtGatt != null) {
			mBtGatt.close();
			mBtGatt = null;
		}
		delegate = null;
		mConnectState = State.DISCONNECTED;
		mContext = null;
		mBtDevice = null;
		mBtServices = null;
		mServices = null;
		super.terminate();
	}
	
	static String bluetoothDeviceIdentifier(BluetoothDevice device) {
		return device.getAddress();
	}

	// Connection
	boolean connect() {
		if (mConnectState == State.DISCONNECTED) {
			if (mServices != null && mBtGatt == null) {
				Log.w("BLUX", "conn>");

				GattCallback cb = new GattCallback();
				mBtGatt = mBtDevice.connectGatt(mContext, false, cb);
				mConnectState = State.CONNECTING;
				return true;
			}
		}
		return false;
	}

	void cancelConnect() {
		if (mConnectState == State.CONNECTED || mConnectState == State.CONNECTING) {
			Log.w("BLUX", "cnclconn>");
			
			if (mBtGatt != null) {
				mBtGatt.disconnect();
			}
			if(mConnectState == State.CONNECTING) {
				delayAction(new DisconnectedDelayed(), 0);
			}
			mConnectState = State.DISCONNECTING;
		}
	}

	// Service attachment.
	void attachService(BluxService service) {
		if(mServices == null)
			return;
		
		if(mServices.get(service.btServiceUUID()) == null) {
			mServices.put(service.btServiceUUID(), service);
			service.onAttached(this);
			if(connected()) {
				notifyServiceConnected(service);
			}
		}
	}

	void detachService(BluxService service) {
		if(mServices == null)
			return;
		
		if(mServices.get(service.btServiceUUID()) != null) {
			service.onDetached();
			mServices.remove(service.btServiceUUID());
		}
	}

	void detachAllServices() {
		if(mServices == null)
			return;
		
		Collection<BluxService> services = mServices.values();
		for(BluxService service : services) {
			service.onDetached();
		}
		mServices.clear();
	}

	// Properties.
	boolean connected() {
		return (mConnectState == State.CONNECTED || mConnectState == State.DISCONNECTING);
	}

	String identifier() {
		if(mBtDevice != null)
			return bluetoothDeviceIdentifier(mBtDevice);
		return null;
	}

	String name() {
		if(mBtDevice != null)
			return mBtDevice.getName();
		return null;
	}

	
	// Transfer serialization
	void putTransfer(Transfer transfer) {
		if(mTransfers != null) {
			if(mTransfers.size() == 0)
				transfer.start(mBtGatt);
			mTransfers.add(transfer);
		}
	}
	
	void cancelTransfers() {
		if(mTransfers != null && mTransfers.size() != 0) {
			for(Transfer transfer : mTransfers) {
				transfer.finished(false);
			}
			mTransfers.clear();
		}
	}

	
	// Delayed action from BluetoothGattCallback thread
	private class CharOrDescReadWriteDelayed extends DelayedAction {
		private BluetoothGattDescriptor 	mDesc;
		private BluetoothGattCharacteristic mChar;
		boolean mSuccess;
		CharOrDescReadWriteDelayed(BluetoothGattDescriptor desc, BluetoothGattCharacteristic ch, boolean success) {
			mDesc = desc;
			mChar = ch;
			mSuccess = success;
		}
		@Override
		protected void act() {
			if(mBtGatt != null && mTransfers != null & mTransfers.size() != 0) {
				Transfer transfer = mTransfers.get(0);
				if(transfer.mRetry != 0 && !mSuccess) {
					transfer.mRetry--;
					transfer.start(mBtGatt);
				}
				else {
					transfer.mDesc = mDesc;
					transfer.mChar = mChar;
					transfer.finished(mSuccess);
					
					mTransfers.remove(0);
					if(mTransfers.size() != 0) {
						mTransfers.get(0).start(mBtGatt);
					}
				}
			}
		};
	}
	
	private class CharChangeDelayed extends DelayedAction {
		private BluetoothGattCharacteristic mChar;
		CharChangeDelayed(BluetoothGattCharacteristic ch) {
			mChar = ch;
		}
		@Override
		protected void act() {
			if(mServices == null)
				return;
			
			BluxService service = mServices.get(mChar.getService().getUuid());
			if (service != null) {
				service.onCharacteristicChanged(mChar);
			}
		};
	}

	private class ServiceDiscoveredDelayed extends DelayedAction {
		int mStatus;
		ServiceDiscoveredDelayed(int status) {
			mStatus = status;
		}
		@Override
		protected void act() {
			if(mServices == null || mBtGatt == null)
				return;
			
			if(mStatus != BluetoothGatt.GATT_SUCCESS)
				return;	//TODO: shall we disconnect on error.
			
			mBtServices = mBtGatt.getServices();
			for(BluetoothGattService btService : mBtServices) {
				BluxService service = mServices.get(btService.getUuid());
				if(service != null) {
					// notify BluxService connected after services discovered successfully.
					service.onConnected(mBtGatt, btService);
				}
			}
		}
	}

	private class ConnectedDelayed extends DelayedAction {
		private int mStatus;
		ConnectedDelayed(int status) {
			mStatus = status;
		}
		@Override
		protected void act() {
			if(mServices == null)
				return;
			
			if(mStatus != BluetoothGatt.GATT_SUCCESS){ //if(mStatus == 133) {
				if(mBtGatt != null) {
					mBtGatt.close();
					mBtGatt = null;
				}
				delayAction(new DisconnectedDelayed(), 0);
				return;
			}

			Log.w("BLUX", "conndcvsvr>");
			mConnectState = State.CONNECTED;
			mBtGatt.discoverServices();

			if(BluxPeripheral.this.delegate != null) {
				BluxPeripheral.this.delegate.peripheralConnected(BluxPeripheral.this);
			}
		}
	}

	private class DisconnectedDelayed extends DelayedAction {
		@Override
		protected void act() {
			if(mServices == null)
				return;

			boolean closed = (mConnectState == State.DISCONNECTING) ? true : false;

			if(mBtGatt != null) {
				mBtGatt.close();
				mBtGatt = null;
			}
			mBtServices = null;
			mConnectState = State.DISCONNECTED;

			cancelTransfers();

			Collection<BluxService> services = mServices.values();
			for(BluxService service : services) {
				service.onDisconnected();
			}
			if(delegate != null) {
				delegate.peripheralDisconnected(BluxPeripheral.this, closed);
			}
		}
	}

	// Private helper
	private void notifyServiceConnected(BluxService service) {
		if(mBtServices != null) {
			for(BluetoothGattService btService : mBtServices) {
				if(btService.getUuid().compareTo(service.btServiceUUID()) == 0) {
					service.onConnected(mBtGatt, btService);
					break;
				}
			}
		}
	}

}
