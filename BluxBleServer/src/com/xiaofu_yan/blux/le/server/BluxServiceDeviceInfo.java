package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

class BluxServiceDeviceInfo extends BluxService {

	private final static UUID UUID_MANUFACTURE_NAME =
			UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_MODEL_NUMBER =
			UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_SERIAL_NUMBER =
			UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_HARDWARE_REVISION =
			UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_FIRMWARE_REVISION =
			UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_SOFTWARE_REVISION =
			UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_SYSTEM_ID =
			UUID.fromString("00002A23-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_IEEE_REGULATORY_CERTIFICATION =
			UUID.fromString("00002A2A-0000-1000-8000-00805F9B34FB");
	private final static UUID UUID_PNP_ID =
			UUID.fromString("00002A50-0000-1000-8000-00805F9B34FB");

	private class ReadTransfer extends BluxPeripheral.ReadCharTransfer {
		ReadTransfer(BluetoothGattCharacteristic ch) {
			super(ch);
		}

		@Override
		protected void finished(boolean success) {
			if(mChar.getUuid().compareTo(UUID_MANUFACTURE_NAME) == 0) {
				mManufactureName = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_MODEL_NUMBER) == 0) {
				mModelNumber = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_SERIAL_NUMBER) == 0) {
				mSerialNumber = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_HARDWARE_REVISION) == 0) {
				mHardwareRevision = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_FIRMWARE_REVISION) == 0) {
				mFirmwareRevision = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_SOFTWARE_REVISION) == 0) {
				mSoftwareRevision = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_SYSTEM_ID) == 0) {
				mSystemId = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_IEEE_REGULATORY_CERTIFICATION) == 0) {
				mIeeeRegulatoryCertification = mChar.getStringValue(0);
				charsRead ++;
			}
			else if(mChar.getUuid().compareTo(UUID_PNP_ID) == 0) {
				mPnpId = mChar.getStringValue(0);
				charsRead ++;
			}

			if(charsRead == charCount && delegate != null) {
				delegate.serviceStarted(BluxServiceDeviceInfo.this, true);
			}
		}
	}
	
	// Member variables
	String mManufactureName;
	String mModelNumber;
	String mSerialNumber;
	String mHardwareRevision;
	String mFirmwareRevision;
	String mSoftwareRevision;
	String mSystemId;
	String mIeeeRegulatoryCertification;
	String mPnpId;

	private int charsRead;
	private int charCount;

	@Override
	protected void onConnected(BluetoothGatt btGatt, BluetoothGattService btService) {
		super.onConnected(btGatt, btService);

		charCount = 0;
		charsRead = 0;
		BluetoothGattCharacteristic ch;

		ch = mBtService.getCharacteristic(UUID_MANUFACTURE_NAME);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_MODEL_NUMBER);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_SERIAL_NUMBER);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_HARDWARE_REVISION);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_FIRMWARE_REVISION);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_SOFTWARE_REVISION);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_SYSTEM_ID);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_IEEE_REGULATORY_CERTIFICATION);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
		ch = mBtService.getCharacteristic(UUID_PNP_ID);
		if(ch != null && mPeripheral != null) {
			mPeripheral.putTransfer(new ReadTransfer(ch));
			charCount++;
		}
	}

	@Override
	protected void onDisconnected() {
		super.onDisconnected();
	}

	// Constructor
	BluxServiceDeviceInfo() {
		mBtServiceUUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
	}
}
