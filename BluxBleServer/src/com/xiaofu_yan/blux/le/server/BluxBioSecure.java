package com.xiaofu_yan.blux.le.server;

import android.annotation.SuppressLint;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;



class BluxBioSecure extends BluxObject {

	// Public types
	enum Result {
		SUCCESS,
		ERROR_USER,
		ERROR_KEY,
		ERROR_VERSION,
	};

	static class Delegate {
		protected void pairResult(Result result, Ticket stub, Ticket ticket) {};
		protected void connectResult(Result result) {};
	};

	Delegate delegate;


	static class Ticket {
		static final byte[] KEY = {'X','i','a','o','f','u','Y','a','n','(','c',')','2','0','1','4'};//XiaofuYan(c)2014 
		int userId;
		byte[] data;
		Ticket(int userId, byte[] data) {
			this.userId = userId;
			if(data != null) {
				this.data = Arrays.copyOf(data, 8);
			}
			else {
				this.data = new byte[8];
				Arrays.fill(this.data, (byte) 0);
			}
		}

		public String toString() {
			byte[] d = Arrays.copyOf(data, 16);
			d[9] = (byte) userId;
			d[10] = 'x';
			d[11] = 'f';
			d[12] = 'u';
			d[13] = 'y';
			d = aes_encrypt(KEY, d);
			String str = new String();
			for(int i = 0; i < d.length; i++)
			{
				char c;
				c = (char) ('A' + (d[i] & 0x0f));
				str = str + c;
				c = (char) ('K' + ((d[i] >> 4) & 0x0f));
				str = str + c;
			}
			return str;
		}

		static public Ticket fromString(String str) {
			if(str.length() != 32)
				return null;
			byte[] d = new byte[16];
			for(int i = 0; i < str.length(); i += 2) {
				char c;
				byte b;
				c = str.charAt(i + 1);
				if(c < 'K' || c > 'Z')
					return null;
				b = (byte) (c - 'K');
				c = str.charAt(i);
				if(c < 'A' || c > 'P')
					return null;
				b = (byte) ((b << 4) | (byte) (c - 'A'));
				d[i/2] = b;
			}
			d = aes_decrypt(KEY, d);
			if(d[10] != 'x' || d[11] != 'f' || d[12] != 'u' || d[13] != 'y')
				return null;
			return new Ticket(d[9], d);
		}
		
		static byte[] makePassPairKey(Ticket ticket) {
			byte[] key = Arrays.copyOf(ticket.data, 16);
			return key;
		}

		static byte[] makeConnectionKey(Ticket ticket, Ticket stub) {
			byte[] key = Arrays.copyOf(ticket.data, 16);
			for(int i = 0; i < 8; i++) {
				key[i + 8] = stub.data[i];
			}
			return key;
		}
	}

	// Public methods.
	BluxBioSecure() {
		mStub = new Ticket(0, null);
	}
	
	protected void terminate() {
		delegate = null;
		mPacketChannel = null;
		mProcedure = null;
		mTicket = null;
		mStub = null;
	}

	void startPassPair(int userId, int pass, BluxVirtualDevice.PacketChannel packetChannel) {
		if(mState != State.READY)
			return;
		mPacketChannel = packetChannel;
		byte[] data = new byte[8];
		Arrays.fill(data, (byte) 0);
		data[0] = (byte) (pass & 0xff);
		data[1] = (byte) ((pass >> 8) & 0xff);
		data[2] = (byte) ((pass >> 16) & 0xff);
		data[3] = (byte) ((pass >> 24) & 0xff);
		mTicket = new Ticket(userId, data);
		mProcedure = new PassPairProcedure();
		mProcedure.start();
	}

	void startKeyPair(Ticket ticket, BluxVirtualDevice.PacketChannel packetChannel) {
		if(mState != State.READY)
			return;
		mPacketChannel = packetChannel;
		mTicket = ticket;
		mProcedure = new KeyPairProcedure();
		mProcedure.start();
	}

	void startConnect(Ticket ticket, BluxVirtualDevice.PacketChannel packetChannel) {
		if(mState != State.READY)
			return;
		mPacketChannel = packetChannel;
		mTicket = ticket;
		mProcedure = new ConnectionProcedure();
		mProcedure.start();
	}
	
	void reset() {
		mState = State.READY;
	}


	// Private
	//	private static final int BIOSEC_CLIENT_REQUEST_VERSION			= 0xff;
	private static final int BIOSEC_CLIENT_REQUEST_CONNECT			= 0x01;
	private static final int BIOSEC_CLIENT_REQUEST_PASS_PAIR		= 0x10;
	private static final int BIOSEC_CLIENT_REQUEST_KEY_PAIR			= 0x11;

	private State			mState;
	private Ticket			mStub;
	private Ticket			mTicket;
	private Procedure		mProcedure;
	private BluxVirtualDevice.PacketChannel	mPacketChannel;

	private enum State {
		READY,
		AUTHORIZED,
		CHALLENGE,
		CHECK,
		PP_KEY_EXCHANGE,
		ERROR_USER,
		ERROR_KEY,
	};

	private class Procedure {
		Procedure() {
			mPacketChannel.mReceiver = new PacketReceiver();
		}

		void send(byte[] packet) {
			mPacketChannel.send(packet, true);
		}
		private class PacketReceiver extends BluxVirtualDevice.PacketChannelReceiver {
			@Override
			protected void received(boolean success, byte[] packet) {
				if(success && packet != null) {
					packetReceived(packet);
				}
			}
		}
		protected void start() {};
		protected void packetReceived(byte[] packet) {};
	}

	private class PassPairProcedure extends Procedure {
		@Override
		protected void start() {
			byte[] data = {BIOSEC_CLIENT_REQUEST_PASS_PAIR, (byte) mTicket.userId};
			send(data);
			mState = State.CHALLENGE;
		}
		@Override
		protected void packetReceived(byte[] packet) {
			if(packet == null || packet.length == 0) {
				if(mState == State.CHALLENGE && delegate != null) {
					delegate.connectResult(Result.SUCCESS);
				}
				mState = State.AUTHORIZED;
				return;
			}
				
			switch(mState) {
			case CHALLENGE:
				if(packet.length != 16) {
					mState = State.ERROR_USER;
					if(delegate != null) {
						delegate.pairResult(Result.ERROR_USER, null, null);
					}
				}
				else {
					byte[] key = Ticket.makePassPairKey(mTicket);
					packet = aes_encrypt(key, packet);
					send(packet);
					mState = State.CHECK;
				}
				break;

			case CHECK:
				if(packet[0] == 0) {
					mState = State.ERROR_KEY;
					if(delegate != null) {
						delegate.pairResult(Result.ERROR_KEY, null, null);
					}
				}
				else {
					send(mStub.data);
					mState = State.PP_KEY_EXCHANGE;
				}
				break;

			case PP_KEY_EXCHANGE:
				if(packet.length == 16) {
					byte[] key = Ticket.makePassPairKey(mTicket);
					packet = aes_decrypt(key, packet);
					Ticket ticket = new Ticket(mTicket.userId, packet);
					if(delegate != null) {
						delegate.pairResult(Result.SUCCESS, mStub, ticket);
					}
				}
				mState = State.AUTHORIZED;
				break;

			default:
				break;
			}
		}
	}

	private class KeyPairProcedure extends Procedure {
		@Override
		protected void start() {
			byte[] data = new byte[10];
			data[0] = BIOSEC_CLIENT_REQUEST_KEY_PAIR;
			data[1] = (byte) mTicket.userId;
			for(int i = 0; i < 8; i++) {
				data[2 + i] = mStub.data[i];
			}
			send(data);
			mState = State.CHALLENGE;
		}
		@Override
		protected void packetReceived(byte[] packet) {
			if(packet == null || packet.length == 0) {
				if(mState == State.CHALLENGE && delegate != null) {
					delegate.connectResult(Result.SUCCESS);
				}
				mState = State.AUTHORIZED;
				return;
			}
				
			switch(mState) {
			case CHALLENGE:
				if(packet.length != 16) {
					mState = State.ERROR_USER;
					if(delegate != null) {
						delegate.pairResult(Result.ERROR_USER, null, null);
					}
				}
				else {
					byte[] key = Ticket.makeConnectionKey(mTicket, mStub);
					packet = aes_encrypt(key, packet);
					send(packet);
					mState = State.CHECK;
				}
				break;

			case CHECK:
				if(packet[0] == 0) {
					mState = State.ERROR_KEY;
					if(delegate != null) {
						delegate.pairResult(Result.ERROR_KEY, null, null);
					}
				}
				else {
					mState = State.AUTHORIZED;
					delegate.pairResult(Result.SUCCESS, mStub, mTicket);
				}
				break;

			default:
				break;
			}
		}
	}

	private class ConnectionProcedure extends Procedure {
		protected void start() {
			byte[] data = {BIOSEC_CLIENT_REQUEST_CONNECT, (byte) mTicket.userId};
			send(data);
			mState = State.CHALLENGE;
		}
		@Override
		protected void packetReceived(byte[] packet) {
			
			switch(mState) {
			case CHALLENGE:
				if(packet.length == 0) {
					if(delegate != null) {
						delegate.connectResult(Result.SUCCESS);
					}
					mState = State.AUTHORIZED;
				}
				else if(packet.length != 16) {
					mState = State.ERROR_USER;
					if(delegate != null) {
						delegate.connectResult(Result.ERROR_USER);
					}
				}
				else {
					byte[] key = Ticket.makeConnectionKey(mTicket, mStub);
					packet = aes_encrypt(key, packet);
					send(packet);
					mState = State.CHECK;
				}
				break;

			case CHECK:
				if(packet[0] == 0) {
					mState = State.ERROR_KEY;
					if(delegate != null) {
						delegate.connectResult(Result.ERROR_KEY);
					}
				}
				else {
					mState = State.AUTHORIZED;
					delegate.connectResult(Result.SUCCESS);
				}
				break;

			default:
				break;
			}
		}
	}

	// AES ECB encrypt/decrypt
	@SuppressLint("TrulyRandom")
	static byte[] aes_encrypt(byte[] key, byte[] clear) {
		byte[] encrypted = null;
		key = reverse_bytes(key);
		clear = reverse_bytes(clear);
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			encrypted = cipher.doFinal(clear);
			encrypted = reverse_bytes(encrypted);
		}
		catch (Exception e) {
		}
		return encrypted;
	}

	static byte[] aes_decrypt(byte[] key, byte[] encrypted) {
		byte[] decrypted = null;
		key = reverse_bytes(key);
		encrypted = reverse_bytes(encrypted);
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decrypted = cipher.doFinal(encrypted);
			decrypted = reverse_bytes(decrypted);
		}
		catch (Exception e) {
		}
		return decrypted;
	}

	// Private methods
	private static byte[] reverse_bytes(byte[] data) {
		byte[] reversed = new byte[data.length];
		for(int i = 0; i < data.length; i++)
			reversed[data.length - i - 1] = data[i];
		return reversed;
	}
}
