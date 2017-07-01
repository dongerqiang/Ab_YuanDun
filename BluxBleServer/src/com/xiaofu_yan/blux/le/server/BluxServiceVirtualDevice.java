package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.xiaofu_yan.blux.le.server.BluxPeripheral.WriteDescTransfer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

class BluxServiceVirtualDevice extends BluxService {

	// Public types
	enum State {
		DISCONNECTED, CONNECTED, BUSY, READY
	};

	static class VDServiceDelegate {
		void vdIrqReceived(int address, byte[] data) {};
		void vdServiceStateChanged(State state) {};
	};

	VDServiceDelegate vdDelegate;


	// Private constants
	private final static int BLUX_TU_ADDR_MASK           			= 0x40;
	private final static int BLUX_TU_READ_MASK           			= 0x20;
	private final static int BLUX_TU_SEQ_MASK            			= 0x1f;

	private final static int BLUX_TU_TYPE_PACKET         			= 0x00;

	private final static int BLUX_TU_SIZE_MAX            			= 20;
	private final static int BLUX_TU_PAYLOAD_SIZE_MAX    			= BLUX_TU_SIZE_MAX - 1;
	private final static int BLUX_TRANSFER_PACKET_SIZE_MAX 			= (BLUX_TU_SEQ_MASK + 1) * BLUX_TU_PAYLOAD_SIZE_MAX; /*608 Bytes*/
	private final static int BLUX_TRANSFER_PACKET_PAYLOAD_SIZE_MAX 	= BLUX_TRANSFER_PACKET_SIZE_MAX - 2;

	private static final String BLUX_DEVICE_UUID_STRING = "78667579-4E28-477f-9EF3-44C041A1AC5F";
	private final static UUID VIRTUAL_DEVICE_CHAR_WRITE_UUID = 
			UUID.fromString("78667579-66B6-4755-AF51-8937D87D4251");
	private final static UUID VIRTUAL_DEVICE_CHAR_READ_UUID =
			UUID.fromString("78667579-CC60-4E25-B1CE-6FB511B90785");
	private final static UUID VIRTUAL_DEVICE_CHAR_IRQ_UUID =
			UUID.fromString("78667579-1DF0-447D-95E0-5E5E2A9C01E2");

	private final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = 
			UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");


	// Private types
	private class ReadWriteTransfer {
		// Private Types
		private class WriteTransfer extends BluxPeripheral.WriteCharTransfer {
			WriteTransfer(BluetoothGattCharacteristic ch, byte[] data) {
				super(ch, data);
			}
			@Override
			protected void finished(boolean success) {
				if(success && send() != 0)
					return;

				if(!mRead) {
					readWriteFinished(success);
					completeReadWrite();
				}
				else {
					if(success){
						mBuffer = new byte[BLUX_TRANSFER_PACKET_PAYLOAD_SIZE_MAX];
						mBufferOffset = 0;
						mLastSeq = 0;

						ReadTransfer transfer = new ReadTransfer(BluxServiceVirtualDevice.this.mBtCharRead);
						mPeripheral.putTransfer(transfer);
					}
					else {
						readWriteFinished(false);
						completeReadWrite();
					}
				}
			}
		}

		private class ReadTransfer extends BluxPeripheral.ReadCharTransfer {
			ReadTransfer(BluetoothGattCharacteristic ch) {
				super(ch);
			}
			@Override
			protected void finished(boolean success) {
				if(success) {
					int more = receive(mChar.getValue());
					if(more == 0) {
						readWriteFinished(true);
						completeReadWrite();
					}
					else if(more < 0) {
						readWriteFinished(false);
						completeReadWrite();
					}
				}
				else {
					readWriteFinished(false);
					completeReadWrite();
				}
			}
		}

		// Private members
		private BluxVirtualDevice mDevice;
		private short 	mRegister;
		private boolean mRead;
		private Object 	mId;
		private byte[] 	mBuffer;
		private int 	mBufferOffset;
		private int		mLastSeq;

		// Private methods
		private int send() {
			byte[] tu;
			int size;
			short hdr = BLUX_TU_TYPE_PACKET;

			if(mBufferOffset >= mBuffer.length)
				return 0;

			size = mBuffer.length - mBufferOffset;
			size = size > BLUX_TU_PAYLOAD_SIZE_MAX ? BLUX_TU_PAYLOAD_SIZE_MAX : size;

			if(mBufferOffset == 0)
				hdr |= BLUX_TU_ADDR_MASK;
			if(mRead)
				hdr |= BLUX_TU_READ_MASK;
			hdr |= (mBuffer.length - mBufferOffset - size + BLUX_TU_PAYLOAD_SIZE_MAX - 1) / BLUX_TU_PAYLOAD_SIZE_MAX;

			tu = new byte[size + 1];
			tu[0] = (byte)hdr;
			BluxVirtualDevice.arrayCopyToArray(tu, 1, mBuffer, mBufferOffset, size);
			mBufferOffset += size;

			Log.i("BLUX", "[" + mDevice.address() + ":" + mRegister + (mRead ? "R" : "W") +"]" + ": " + Arrays.toString(tu));


			WriteTransfer transfer = new WriteTransfer(BluxServiceVirtualDevice.this.mBtCharWrite, tu);
			mPeripheral.putTransfer(transfer);

			return tu.length;
		}

		private int receive(byte[] data) {
			if(data == null || data.length == 0)
				return -1;

			int seq = ((int)data[0] & BLUX_TU_SEQ_MASK);
			if(mBufferOffset == 0) {
				mLastSeq = seq; 
			}
			else if(seq >= mLastSeq){
				return -1;
			}

			BluxVirtualDevice.arrayCopyToArray(mBuffer, mBufferOffset, data, 1, data.length - 1);
			mBufferOffset += data.length - 1;

			//TU left.
			if(seq != 0) {
				ReadTransfer transfer = new ReadTransfer(mBtCharRead);
				mPeripheral.putTransfer(transfer);
			}
			return seq;
		}

		// Public methods
		ReadWriteTransfer(BluxVirtualDevice device, short register, boolean read, byte[] data, Object id) {
			mDevice = device;
			mRegister = register;
			mRead = read;
			mId = id;
			mBufferOffset = 0;

			int size = (data == null) ? 0 : data.length;
			size = size > BLUX_TRANSFER_PACKET_PAYLOAD_SIZE_MAX ? BLUX_TRANSFER_PACKET_PAYLOAD_SIZE_MAX : size;

			mBuffer = new byte[size + 2];
			mBuffer[0] = (byte) (device.address() & 0xff);
			mBuffer[1] = (byte) (register & 0xff);

			BluxVirtualDevice.arrayCopyToArray(mBuffer, 2, data, 0, size);
		}
		void readWriteStart() {
			send();
		}

		void readWriteFinished(boolean success) {
			if(mRead) {
				byte[] data = null;
				if(success){
					if(mBufferOffset != 0) {
						data = Arrays.copyOfRange(mBuffer, 0, mBufferOffset);
					}
					else {
						data = new byte[0];
					}
				}
				Log.i("BLUX", "[" + mDevice.address() + ":" + mRegister + "RD]: " + ((data == null) ? "" : Arrays.toString(data)));
				
				mDevice.didReadRegister(mId, mRegister, success, data);
			}
			else {
				mDevice.didWriteRegister(mId, mRegister, success);
			}
		}

	}

	// Private members
	private BluetoothGattCharacteristic mBtCharRead;
	private BluetoothGattCharacteristic mBtCharWrite;
	private BluetoothGattCharacteristic mBtCharIrq;
	private List<ReadWriteTransfer> 	mRegisterReadWrites;

	// Public methods
	BluxServiceVirtualDevice() {
		mRegisterReadWrites = new ArrayList<ReadWriteTransfer>();
		mBtServiceUUID = UUID.fromString(BLUX_DEVICE_UUID_STRING);
	}

	@Override
	protected void terminate() {
		if(mRegisterReadWrites != null) {
			cancelAllReadWrites();
			mRegisterReadWrites = null;
		}
		mBtCharRead = mBtCharWrite = mBtCharIrq = null;
		delegate = null;
		super.terminate();
	}

	boolean writeDevice(BluxVirtualDevice device, short address,  byte[] data, Object id)
	{
		if(mBtCharWrite == null)
			return false;

		ReadWriteTransfer transfer = new ReadWriteTransfer(device, address, false, data, id);
		addRegisterReadWrite(transfer);
		return true;
	}

	boolean readDevice(BluxVirtualDevice device, short address, byte[] data, Object id)
	{
		if(mBtCharWrite == null)
			return false;

		ReadWriteTransfer transfer = new ReadWriteTransfer(device, address, true, data, id);
		addRegisterReadWrite(transfer);
		return true;
	}

	// BluxService override.
	@Override
	protected void onCharacteristicChanged(
			BluetoothGattCharacteristic characteristic) {
		if(mBtCharIrq == null)
			return;

		if(mBtCharIrq.getUuid().compareTo(characteristic.getUuid()) == 0) {
			byte[] data = characteristic.getValue();

			if(vdDelegate != null && data != null && data.length > 0) {
				byte[] d = Arrays.copyOfRange(data, 1, data.length);
				int device = ((int)data[0]) & 0xff;
				vdDelegate.vdIrqReceived(device, d);
			}
		}
	}

	@Override
	protected void onDetached() {
		super.onDetached();
		cancelAllReadWrites();
		mBtCharRead = null;
		mBtCharWrite = null;
		mBtCharIrq = null;
	}

	@Override
	protected void onDisconnected() {
		super.onDisconnected();
		cancelAllReadWrites();
		mBtCharRead = null;
		mBtCharWrite = null;
		mBtCharIrq = null;
	}

	@Override
	protected void onConnected(BluetoothGatt btGatt, BluetoothGattService btService) {
		super.onConnected(btGatt, btService);

		mBtCharRead = btService.getCharacteristic(VIRTUAL_DEVICE_CHAR_READ_UUID);
		mBtCharWrite = btService.getCharacteristic(VIRTUAL_DEVICE_CHAR_WRITE_UUID);
		mBtCharIrq = btService.getCharacteristic(VIRTUAL_DEVICE_CHAR_IRQ_UUID);

		enableNotification(true);

		if(delegate != null)
			delegate.serviceStarted(this, true);
	}

	// Private methods
	private void addRegisterReadWrite(ReadWriteTransfer transfer) {
		if(mRegisterReadWrites != null){
			if(mRegisterReadWrites.size() == 0)
				transfer.readWriteStart();
			mRegisterReadWrites.add(transfer);
		}
	}

	private void completeReadWrite() {
		if(mRegisterReadWrites != null){
			mRegisterReadWrites.remove(0);
			if(mRegisterReadWrites.size() != 0)
				mRegisterReadWrites.get(0).readWriteStart();
		}
	}
	
	private void cancelAllReadWrites() {
		if(mRegisterReadWrites != null) {
			for(ReadWriteTransfer transfer : mRegisterReadWrites) {
				transfer.readWriteFinished(false);
			}
			mRegisterReadWrites.clear();
		}
	}

	private void enableNotification(boolean enable) {
		if(mPeripheral != null && mBtCharIrq != null) {
			BluetoothGattDescriptor desc = mBtCharIrq.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
			mBtGatt.setCharacteristicNotification(mBtCharIrq, enable);
			WriteDescTransfer transfer = new WriteDescTransfer(desc,
					enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			mPeripheral.putTransfer(transfer);
		}
	}

}
