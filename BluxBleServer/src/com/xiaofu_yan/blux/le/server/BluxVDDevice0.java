package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


class BluxVDDevice0 extends BluxVirtualDevice {

	// Public types
	static class Delegate {
		protected void device0Started(boolean success) {};
	};

	Delegate 				delegate;


	// Private member variables
	private List<BluxVirtualDevice> mVirtualDevices;
	private PacketChannel			mAuthChannel;
	private PacketChannel			mAccountManageChannel;
	private int 					mTempCounter;


	// Public methods
	BluxVDDevice0(BluxServiceVirtualDevice service) {
		super(service, null);
		mAccountManageChannel = new PacketChannel(REG_DEVICE0_USER_MANAGEMENT);
		mAuthChannel = new PacketChannel(REG_DEVICE0_AUTH);
		service.vdDelegate = new VDServiceDelegate();

		byte[] data = {0, // Device0: address is 0, uuid is 00000000-0000-0000-0000-000000000000
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0};
		mDescriptor = new Descriptor(data);
	}

	@Override
	protected void terminate() {
		if(mVirtualDevices != null) {
			for(BluxVirtualDevice device : mVirtualDevices)
				device.terminate();
		}
		delegate = null;
		mVirtualDevices = null;
		mAuthChannel = null;
		mAccountManageChannel = null;
		super.terminate();
	}

	PacketChannel getAuthorizeChannel() {
		return mAuthChannel;
	}

	PacketChannel getAccountManageChannel() {
		return mAccountManageChannel;
	}

	BluxVirtualDevice getDevice(int address) {
		for(BluxVirtualDevice dev : mVirtualDevices) {
			if(address == dev.address())
				return dev;
		}
		return null;
	}

	BluxVirtualDevice getDevice(UUID uuidType) {
		for(BluxVirtualDevice dev : mVirtualDevices) {
			if(dev.typeUuid().compareTo(uuidType) == 0)
				return dev;
		}
		return null;
	}

	BluxVirtualDevice[] getDevices() {
		BluxVirtualDevice[] vds = new BluxVirtualDevice[mVirtualDevices.size()];
		vds = mVirtualDevices.toArray(vds);
		return vds;
	}

	void start() {
		updateVirtualDevices();
	}


	// Base class override.
	@Override
	protected void didReadRegister(Object id, int register, boolean success, byte[] data) {
		switch(register) {
		case REG_DEVICE0_QUERY:
			byte cmd = ((byte[])id)[0];
			if(cmd == DEVICE_GET_COUNT && success) {
				mTempCounter = data[0];
				mTempCounter &= 0xff;
				for(int i = 0; i < mTempCounter; i++) {
					readDeviceDescriptor(i);
				}
			}
			else if(cmd == DEVICE_GET_DESC) {
				if(success) {
					if(data.length == 17) {
						BluxVirtualDevice.Descriptor desc = new BluxVirtualDevice.Descriptor(data);
						BluxVirtualDevice dev = createVirtualDevice(desc);
						mVirtualDevices.add(dev);
					}
				}
				if(mTempCounter != 0)
					mTempCounter --;
				if(mTempCounter == 0 && delegate != null) {
					delegate.device0Started(true);
				}
			}
			break;

		case REG_DEVICE0_AUTH:
		default:
			super.didReadRegister(id, register, success, data);
			break;
		}
	}


	// Private constants.
	private final static short REG_DEVICE0_AUTH					= 0x10;
	private final static short REG_DEVICE0_QUERY					= 0x11;
	private final static short REG_DEVICE0_USER_MANAGEMENT			= 0x21;

	private final static int  DEVICE_GET_COUNT						= 0x01;
	private final static int  DEVICE_GET_DESC						= 0x02;


	// Private types
	// BluxServiceVirtualDevice delegate
	private class VDServiceDelegate extends BluxServiceVirtualDevice.VDServiceDelegate {
		@Override
		protected void vdServiceStateChanged(BluxServiceVirtualDevice.State state) {
			for(BluxVirtualDevice dev : mVirtualDevices)
				dev.serviceStateChange(state);
		}

		@Override
		protected void vdIrqReceived(int address, byte[] data) {
			if(mVirtualDevices != null) {
				for(BluxVirtualDevice dev : mVirtualDevices) {
					if(dev.address() == address) {
						dev.irq(data);
						break;
					}
				}
			}
		}
	}


	// Private methods
	private void readDeviceCount() {
		byte[] buffer = {DEVICE_GET_COUNT};
		readRegister(REG_DEVICE0_QUERY, buffer, buffer);
	}

	private void readDeviceDescriptor(int index) {
		byte[] buffer = new byte[2];
		buffer[0] = DEVICE_GET_DESC;
		buffer[1] = (byte) (index & 0xff);
		readRegister(REG_DEVICE0_QUERY, buffer, buffer);
	}

	private void updateVirtualDevices() {
		if(mVirtualDevices == null || mVirtualDevices.size() == 0) {
			mVirtualDevices = new ArrayList<BluxVirtualDevice>();
			readDeviceCount();
		}
		else if(delegate != null){
			// If already initialized will not read again.
			delegate.device0Started(true);
		}
	}

	private BluxVirtualDevice createVirtualDevice(BluxVirtualDevice.Descriptor desc) {
		if(BluxVDSmartGuard.isKindOf(desc.uuid))
			return new BluxVDSmartGuard(mService, desc);
		else if(BluxVDPrivate.isKindOf(desc.uuid)) 
			return new BluxVDPrivate(mService, desc);
		else
			return new BluxVDGeneric(mService, desc);
	}

}
