package com.xiaofu_yan.blux.le.server;

import java.util.UUID;


class BluxVDGeneric extends BluxVirtualDevice {

	static class Delegate {
		void didRead(Object id, int address, boolean success, byte[] data) {};
		void didWrite(Object id, int address, boolean success) {};
		void irq(byte[] data) {};
	}

	// Member variables
	Delegate delegate;

	static boolean isKindOf(UUID uuidType) {
		return true;
	}

	BluxVDGeneric(BluxServiceVirtualDevice service,
			Descriptor desc) {
		super(service, desc);
	}

	@Override
	protected void serviceStateChange(BluxServiceVirtualDevice.State state) {
	}

	@Override
	protected void didReadRegister(Object id, int address, boolean success, byte[] data) {
		if(delegate != null) {
			delegate.didRead(id, address, success, data);
		}
	}

	@Override
	protected void didWriteRegister(Object id, int address, boolean success){
		if(delegate != null) {
			delegate.didWrite(id, address, success);
		}
	}

	@Override
	protected void irq(byte[] data) {
		if(delegate != null) {
			delegate.irq(data);
		}
	}
}
