package com.xiaofu_yan.blux.le.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BluxAccountManager extends BluxObject {

	// Public types
	static class Delegate {
		protected void updateAccount(Account account) {};
	}

	static class InitDelegate {
		protected void started(boolean success) {};
	}

	static class Account {
		int index;
		int control;
		int passkey;
		String name;

		private byte[] toStream() {
			byte[] name = this.name.getBytes();
			byte[] data = new byte[1 + 2 + 4 + 1 + name.length];

			data[0] = (byte) index;
			BluxVirtualDevice.s2le((short) control, data, 1);
			BluxVirtualDevice.l2le(passkey, data, 3);
			data[7] = (byte) name.length;
			BluxVirtualDevice.arrayCopyToArray(data, 8, name, 0, name.length);

			return data;
		}

		Account() {
		}

		Account(int index, int control, int passkey, String name) {
			this.index = index;
			this.control = control;
			this.passkey = passkey;
			this.name = name;
		}

		static Account fromStream(byte[] stream) {
			if (stream.length < 8 || stream.length != ((stream[7] + 8) & 0xff))
				return null;

			Account account = new Account();
			account.index = stream[0] & 0xff;
			account.control = BluxVirtualDevice.le2s(stream, 1);
			account.passkey = BluxVirtualDevice.le2l(stream, 3);
			if (stream[7] != 0) {
				byte[] name = Arrays.copyOfRange(stream, 8, stream.length);
				try {
					account.name = new String(name, "UTF-8");
				} catch (Exception exception) {
					account.name = null;
				}
			}
			return account;
		}
	}

	InitDelegate initDelegate;


	// Public methods
	BluxAccountManager() {
		mDelegates = new ArrayList<Delegate>();
		mAccountCount = 0;
	}

	@Override
	protected void terminate() {
		mDelegates = null;
		initDelegate = null;
		mPacketChannel = null;
		super.terminate();
	}

	void registerDelegate(Delegate delegate) {
		if (!mDelegates.contains(delegate))
			mDelegates.add(delegate);
	}

	void unregisterDelegate(Delegate delegate) {
		mDelegates.remove(delegate);
	}

	int count() {
		return mAccountCount;
	}

	void start(BluxVirtualDevice.PacketChannel channel) {
		mPacketChannel = channel;
		mPacketChannel.mReceiver = new PacketReceiver();
		mInitialized = false;
		getInfo();
	}

	void setAccount(Account account) {
		if (account != null) {
			byte[] data = account.toStream();
			byte[] packet = new byte[data.length + 1];
			packet[0] = USERMAN_SET_USER;
			BluxVirtualDevice.arrayCopyToArray(packet, 1, data, 0, data.length);
			mPacketChannel.send(packet, true);
		}
	}

	void getAccount(int index) {
		byte[] packet = {USERMAN_GET_USER, (byte) index};
		mPacketChannel.send(packet, true);
	}

	// Private members.
	private List<Delegate> mDelegates;
	private BluxVirtualDevice.PacketChannel mPacketChannel;
	private boolean mInitialized;
	private int mAccountCount;
	@SuppressWarnings("unused")
	private int mActiveAccountIndex;

	private static final int USERMAN_GET_INFO = 1;
	private static final int USERMAN_GET_USER = 2;
	private static final int USERMAN_SET_USER = 3;

	// Private methods
	private void getInfo() {
		byte[] packet = {USERMAN_GET_INFO};
		mPacketChannel.send(packet, true);
	}

	private class PacketReceiver extends BluxVirtualDevice.PacketChannelReceiver {
		@Override
		protected void received(boolean success, byte[] packet) {
			if(!success || packet == null)
				return;
			
			if (!mInitialized && packet.length == 0) {
				if (initDelegate != null)
					initDelegate.started(false);
				mInitialized = true;
			}

			
			switch (packet[0]) {
			case USERMAN_GET_INFO:
				mAccountCount = packet[2] & 0xff;
				mActiveAccountIndex = packet[3] & 0xff;
				mInitialized = true;
				if (initDelegate != null)
					initDelegate.started(true);
				break;

			case USERMAN_GET_USER:
				if (mDelegates != null && mDelegates.size() != 0 && packet.length > 3 && packet[1] != 0) {
					Account account = Account.fromStream(Arrays.copyOfRange(packet, 2, packet.length));
					if (account != null) {
						for(Delegate delegate : mDelegates)
							delegate.updateAccount(account);
					}
				}
				break;

			case USERMAN_SET_USER:
				if (mDelegates != null && mDelegates.size() != 0 && packet.length > 3) {
					Account account = Account.fromStream(Arrays.copyOfRange(packet, 2, packet.length));
					if(account != null) {
						for (Delegate delegate : mDelegates) {
							delegate.updateAccount(account);
						}
					}
				}
				break;
			}
		}
	}

}
