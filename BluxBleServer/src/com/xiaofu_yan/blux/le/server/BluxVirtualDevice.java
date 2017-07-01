package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

class BluxVirtualDevice extends BluxObject {
	// Public static helper methods
	static protected void arrayCopyToArray(byte[] dest, int offsetDest, byte[] src, int offsetSrc, int size) {
		if(dest == null || src == null)
			return;

		for(int i = 0; i < size; i++) {
			dest[offsetDest + i] = src[offsetSrc + i];
		}
	}

	static protected void s2le(short s, byte[] buffer, int offset) {
		buffer[offset] = (byte) (s & 0xff);
		buffer[offset + 1] = (byte) ((s >> 8) & 0xff);
	}

	static protected void l2le(int l, byte[] buffer, int offset) {
		buffer[offset] = (byte) (l & 0xff);
		buffer[offset + 1] = (byte) ((l >> 8) & 0xff);
		buffer[offset + 2] = (byte) ((l >> 16) & 0xff);
		buffer[offset + 3] = (byte) ((l >> 24) & 0xff);
	}

	static protected short le2s(byte[] buffer, int offset) {
		short s;
		s = buffer[offset + 1];
		s = (short) ((s << 8) | (buffer[offset] & 0xff));
		return s;
	}

	static protected int le2l(byte[] buffer, int offset) {
		int l;
		l = buffer[offset + 3];
		l = ((l << 8) | (buffer[offset + 2] & 0xff));
		l = ((l << 8) | (buffer[offset + 1] & 0xff));
		l = ((l << 8) | (buffer[offset] & 0xff));
		return l;
	}

	static int le2uc(byte[] buffer, int offset) {
		int uc = buffer[offset];
		uc &= 0xff;
		return uc;
	}


	// Public types
	static class Descriptor {
		short address;
		UUID uuid;

		Descriptor(byte[] data) {
			this.address = data[0];
			this.address &= 0xff;

			String s = String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X",
					data[16], data[15], data[14], data[13], data[12], data[11], data[10], data[9], data[8],
					data[7], data[6], data[5], data[4], data[3], data[2], data[1]);
			this.uuid = UUID.fromString(s);
		}
	}

	static class PacketChannelReceiver {
		protected void received(boolean success, byte[] packet) {};
		protected void sent(boolean success) {};
	}

	class PacketChannel {
		private short mRegister;
		PacketChannelReceiver mReceiver;

		PacketChannel(short register) {
			mRegister = register;
		}

		boolean send(byte[] packet, boolean read) {
			if(read)
				return readRegister(mRegister, packet, mReceiver);
			else
				return writeRegister(mRegister, packet, mReceiver);
		}
	}

	protected BluxServiceVirtualDevice	mService;
	protected Descriptor 				mDescriptor;


	// Public methods
	BluxVirtualDevice(BluxServiceVirtualDevice service, Descriptor desc) {
		mService = service;
		mDescriptor = desc;
	}

	protected void terminate() {
		mService = null;
		mDescriptor = null;
		super.terminate();
	}

	protected short address() {
		if(mDescriptor != null)
			return mDescriptor.address;
		return 0;
	}

	protected UUID typeUuid() {
		if(mDescriptor != null)
			return mDescriptor.uuid;
		return null;
	}


	// Helper methods for subclass
	boolean readRegister(int register, byte[] data, Object id) {
		return mService.readDevice(this, (short) register, data, id);
	}

	boolean writeRegister(int register, byte[] data, Object id) {
		return mService.writeDevice(this, (short) register, data, id);
	}

	boolean readRegister(int register, byte[] data) {
		return mService.readDevice(this, (short) register, data, null);
	}

	boolean writeRegister(int register, byte[] data) {
		return mService.writeDevice(this, (short) register, data, null);
	}

	// Methods for subclass override
	protected void serviceStateChange(BluxServiceVirtualDevice.State state) {
	}

	protected void didReadRegister(Object id, int register, boolean success, byte[] data) {
		PacketChannelReceiver receiver = (PacketChannelReceiver)id;
		if(receiver != null) {
			receiver.received(success, data);
		}
	}

	protected void didWriteRegister(Object id, int register, boolean success){
		PacketChannelReceiver receiver = (PacketChannelReceiver)id;
		if(receiver != null) {
			receiver.sent(success);
		}
	}

	protected void irq(byte[] data) {
	}
}
