package com.xiaofu_yan.blux.le.server;

import android.annotation.SuppressLint;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

class BluxVDPrivate extends BluxVirtualDevice{

	// Public types.
	static class Delegate{
		protected void heartBeat(byte[] data) {};
		protected void updateHeartBeat(short period) {};
		protected void updateSerialNumber(String serialNumber) {};
		protected void updatePairPasskey(String passKey) {};
		protected void updatePeerRole(byte role) {};
		protected void updateName(String name) {};
		protected void updateConnectionParam(short timeOut, short minMs, short maxMs, short latency) {};
		protected void updateBroadcastAD(byte[] ad) {};
		protected void updateUserStorageConfig(short size) {};
		protected void updateUserStorage(short offset, byte[] words) {};
		protected void updatePrivateNvm(short offset, byte[] words) {};
	}

	Delegate delegate;


	// Private constants.
	final static UUID VD_TYPE_UUID =
			UUID.fromString("78667579-BDAC-48A0-AB46-869A3D2F8493");

	private final static int REG_RESET                             = 0x11;  /*WO*/
	private final static int REG_BLE_ROLE                          = 0x12;  /*RW*/
	private final static int REG_HEART_BEAT                        = 0x13;  /*RW*/
	private final static int REG_CONNECTION_PARAMETER              = 0x21;  /*RW*/
	private final static int REG_NAME                              = 0x31;  /*RW*/
	private final static int REG_SERIAL_NUMBER                     = 0x32;  /*RO*/
	private final static int REG_PAIR_PASSKEY                      = 0x33;  /*RO*/
	private final static int REG_BROADCAST_AD                      = 0x34;  /*RW*/
	private final static int REG_USER_STORAGE_CONFIG               = 0x35;  /*RO*/
	private final static int REG_USER_STORAGE                      = 0x36;  /*RW*/
	private final static int REG_PRIVATE_NVM                       = 0x37;  /*RW*/


	// Public methods
	BluxVDPrivate(BluxServiceVirtualDevice service, Descriptor desc) {
		super(service, desc);
	}

	@Override
	protected void terminate() {
		delegate = null;
		super.terminate();
	}

	void resetPeer(short delay) {
		byte[] data = new byte[2];

		s2le(delay, data, 0);
		writeRegister(REG_RESET, data);
	}

	void getSerialNumber() {
		readRegister(REG_SERIAL_NUMBER, null);
	}

	void getPairPasskey() {
		readRegister(REG_PAIR_PASSKEY, null);
	}

	void readPeerRole() {
		readRegister(REG_BLE_ROLE, null);
	}

	void writePeerRole(byte role) {
		byte[] data ={role};

		writeRegister(REG_BLE_ROLE, data);
	}

	void readPeerHeartBeat() {
		readRegister(REG_HEART_BEAT, null);
	}

	void writePeerHeartBeat(short period) {
		byte[] data = new byte[2];

		s2le(period, data, 0);
		writeRegister(REG_HEART_BEAT, data);
	}

	void readName() {
		readRegister(REG_NAME, null);
	}

	void writeName(String name) {
		byte[] data;
		try {
			data = name.getBytes("UTF-8");
			writeRegister(REG_NAME, data);
		}
		catch (UnsupportedEncodingException e) {
		}
	}

	void readConnectionParam() {
		readRegister(REG_CONNECTION_PARAMETER, null);
	}

	void writeConnectionParam(int minMs, int maxMs, int n, int timeOut) {
		byte[] data = new byte[8];

		s2le((short) minMs, data, 0);
		s2le((short) maxMs, data, 2);
		s2le((short) n, data, 4);
		s2le((short) timeOut, data, 6);

		writeRegister(REG_CONNECTION_PARAMETER, data);
	}

	void readBroadcastAD(byte[] ad) {
		readRegister(REG_BROADCAST_AD, ad);
	}

	void writeBroadcastAD(byte[] ad) {
		writeRegister(REG_BROADCAST_AD, ad);
	}

	void readUserStorageConfig() {
		readRegister(REG_USER_STORAGE_CONFIG, null);
	}

	void readUserStorage(short offset, byte words) {
		byte data[] = new byte[3];

		s2le(offset, data, 0);
		data[2] = words;

		readRegister(REG_USER_STORAGE, null);
	}

	void writeUserStorage(short offset, byte[] words) {
		byte[] data = new byte[words.length + 2];

		s2le(offset, data, 0);
		arrayCopyToArray(data, 2, words, 0, words.length);

		writeRegister(REG_USER_STORAGE, data);
	}

	void readPrivateNvm(short offset, byte words) {
		byte[] data = new byte[3];

		s2le(offset, data, 0);
		data[2] = words;

		readRegister(REG_PRIVATE_NVM, data);
	}

	void writePrivateNvm(short offset, byte[] words) {
		byte[] data = new byte[words.length + 2];

		s2le(offset, data, 0);
		arrayCopyToArray(data, 2, words, 0, words.length);

		writeRegister(REG_PRIVATE_NVM, data);
	}


	// BluxVD Overrides
	static boolean isKindOf(UUID uuidType) {
		return (VD_TYPE_UUID.compareTo(uuidType) == 0);
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected void didReadRegister(Object id, int register, boolean success, byte[] data) {
		if(delegate == null || !success || data == null)
			return;

		switch(register) {
		case REG_BLE_ROLE:
			if(data.length != 0) {
				byte role = data[0];
				delegate.updatePeerRole(role);
			}
			break;

		case REG_HEART_BEAT:
			if(data.length == 2) {
				short period = le2s(data, 0);
				delegate.updateHeartBeat(period);
			}
			break;

		case REG_CONNECTION_PARAMETER:
			if(data.length == 8) {
				short minInterval = le2s(data, 0);
				short maxInterval = le2s(data, 2);
				short latency = le2s(data, 4);
				short timeOut = le2s(data, 6);
				delegate.updateConnectionParam(timeOut, minInterval, maxInterval, latency);
			}
			break;

		case REG_NAME:
			String name = new String(data, Charset.forName("UTF-8"));
			delegate.updateName(name);
		break;

		case REG_SERIAL_NUMBER:
			if(data.length != 0) {
				String str = new String(data, Charset.forName("UTF-8"));
				delegate.updateSerialNumber(str);
			}
			break;

		case REG_PAIR_PASSKEY:
			if(data.length == 4) {
				int key = le2l(data, 0);
				String str = String.format("%06d", key);
				delegate.updatePairPasskey(str);
			}
			break;

		case REG_BROADCAST_AD:
			delegate.updateBroadcastAD(data);
			break;

		case REG_USER_STORAGE_CONFIG:
			if(data.length == 2) {
				short count = le2s(data, 0);
				delegate.updateUserStorageConfig(count);
			}
			break;

		case REG_USER_STORAGE:
			if(data.length > 2) {
				short offset = le2s(data, 0);
				byte[] d = Arrays.copyOfRange(data, 2, data.length);
				delegate.updateUserStorage(offset, d);
			}
			break;

		case REG_PRIVATE_NVM:
			if(data.length > 2) {
				short offset = le2s(data, 0);
				byte[] d = Arrays.copyOfRange(data, 2, data.length);
				delegate.updatePrivateNvm(offset, d);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void irq(byte[] data) {
		short register;

		if(data != null && data.length > 0 && delegate != null) {
			register = (short)(((short)data[0]) & 0xff);
			if(register == REG_HEART_BEAT) {
				byte[] d = Arrays.copyOfRange(data, 1, data.length);
				delegate.heartBeat(d);
			}
		}
	}

}
