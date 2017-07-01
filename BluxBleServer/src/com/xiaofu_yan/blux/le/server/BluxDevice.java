package com.xiaofu_yan.blux.le.server;

import java.util.UUID;
import android.util.Log;
import com.xiaofu_yan.blux.le.server.BluxBioSecure.Result;
import com.xiaofu_yan.blux.le.server.BluxBioSecure.Ticket;


class BluxDevice extends BluxObject{

	// Public types
	enum DisconnectReason {
		UNKNOWN,
		LINKLOST,
		CLOSED,
		PERMISSION,
		KEY,
	};
	
	enum PairResult {
		SUCCESS,
		ERROR_UNKNOWN,
		ERROR_PERMISSION,
		ERROR_KEY
	};
	
	static class Delegate {
		protected void deviceConnected(BluxDevice device) {};
		protected void deviceDisconnected(BluxDevice device, DisconnectReason reason) {};
		protected void devicePairResult(BluxDevice device, PairResult result, String key) {};
	};

	Delegate delegate;

	
	// Private constants
	enum AuthType {
		NONE,
		KEY_PAIR,
		PASS_PAIR,
		CONNECT
	};

	private enum State {
		DISCONNECTED,
		CONNECTING,
		AUTHORIZING,
		AMINITIALIZING,
		VDINITIALIZING,
		CONNECTED,
	}
	
	private final static int PAIR_TIMEOUT_SECONDS		= 30;

	
	// Private members
	private State						mState;
	private BluxPeripheral 				mPeripheral;
	private BluxServiceVirtualDevice	mService;
	private BluxVDDevice0				mVDDevice0;
	private BluxBioSecure				mBioSecure;
	private BluxAccountManager			mAccountManager;

	private AuthType					mAuthType;
	private String						mKey;
	private int							mPasskey;
	private BluxBioSecure.Result 		mAuthResult;
	private PairTimeout					mPairTimeout;


	// Public methods
	BluxDevice(BluxPeripheral peripheral) {
		mPeripheral = peripheral;
		mPeripheral.delegate = new PeripheralDelegate();

		mService = new BluxServiceVirtualDevice();
		mService.delegate = new ServiceDelegate();
		mPeripheral.attachService(mService);

		mVDDevice0 = new BluxVDDevice0(mService);
		mVDDevice0.delegate = new VDDevice0Delegate();

		mBioSecure = new BluxBioSecure();
		mBioSecure.delegate = new BiosecDelegate();

		mAccountManager = new BluxAccountManager();
		mAccountManager.initDelegate = new AccountManagerInitDelegate();

		mState = State.DISCONNECTED;
	}

	protected void terminate() {
		delegate = null;
		if(mPeripheral != null) {
			mPeripheral.terminate();
			mPeripheral = null;
		}
		if(mService != null) {
			mService.terminate();
			mService = null;
		}
		if(mVDDevice0 != null) {
			mVDDevice0.terminate();
			mVDDevice0 = null;
		}
		if(mBioSecure != null) {
			mBioSecure.terminate();
			mBioSecure = null;
		}
		if(mAccountManager != null) {
			mAccountManager.terminate();
			mAccountManager = null;
		}
		
		super.terminate();
	}

	boolean connected() {
		return mState == State.CONNECTED;
	}

	BluxAccountManager getAccountManager() {
		return mAccountManager;
	}

	void connect() {
		Log.w("BLUX", "[BD:conn:" + mState +"]");
		
		if(mState == State.DISCONNECTED && mPeripheral != null
				&& !mPeripheral.connected() && mPeripheral.connect()) {
			mAuthResult = BluxBioSecure.Result.SUCCESS;
			mAuthType = AuthType.CONNECT;
			mState = State.CONNECTING;
		}
	}

	void cancelConnect() {
		Log.w("BLUX", "[BD:clconn:" + mState +"]");
		
		if(mState != State.DISCONNECTED && mPeripheral != null) {
			if(mAuthType == AuthType.CONNECT) {
				mPeripheral.cancelConnect();
			}
		}
	}

	void passPair(int pass) {
		Log.w("BLUX", "[BD:pp:" + mState +"]");
		
		if(mState == State.DISCONNECTED && mPeripheral != null
				&& !mPeripheral.connected() && mPeripheral.connect()) {
			mAuthResult = BluxBioSecure.Result.SUCCESS;
			mAuthType = AuthType.PASS_PAIR;
			mPasskey = pass;
			startPairTimer();
			mState = State.CONNECTING;
		}
	}
	
	void cancelPair() {
		Log.w("BLUX", "[BD:cp:" + mState +"]");
		
		if(mState != State.DISCONNECTED && mState != State.CONNECTED && mPeripheral != null) {
			if(mAuthType == AuthType.PASS_PAIR || mAuthType == AuthType.KEY_PAIR) {
				mAuthType = AuthType.NONE;
				mPeripheral.cancelConnect();
				stopPairTimer();

				if(delegate != null) {
					delayAction(new DelayedAction(){
						@Override
						protected void act() {
							delegate.devicePairResult(BluxDevice.this, PairResult.ERROR_UNKNOWN, null);
						}
					}, 0);
				}
			}
		}
	}
	
	void setKey(String key) {
		mKey = key;
	}
	
	BluxVirtualDevice getVirtualDevice(UUID uuidType) {
		if(mState == State.CONNECTED && mPeripheral != null && mVDDevice0 != null) {
			return mVDDevice0.getDevice(uuidType);
		}
		return null;
	}
	
	
	// Private methods
	private void startPairTimer() {
		stopPairTimer();
		mPairTimeout = new PairTimeout();
		delayAction(mPairTimeout, PAIR_TIMEOUT_SECONDS * 1000);
	}
	
	private void stopPairTimer() {
		if(mPairTimeout != null)
			mPairTimeout.cancel();
	}

	
	// Private types
	private class PairTimeout extends DelayedAction {
		@Override
		protected void act() {
			Log.w("BLUX", "[BD:pto:" + mState +"]");
			
			mPairTimeout = null;
			cancelPair();
		}
	}
	
	// BluxServiceVirtualDevice delegate
	private class ServiceDelegate extends BluxServiceVirtualDevice.Delegate {
		@Override
		protected void serviceStarted(BluxService service, boolean success) {
			// START AUTHORIZE ON SERVICE STARTED.
			Log.w("BLUX", "[BD:clconn:" + mState + " " + mAuthType +"]");
			
			if(mAuthType == AuthType.PASS_PAIR) {
				mBioSecure.startPassPair(0, mPasskey, mVDDevice0.getAuthorizeChannel());
			}
			else {
				BluxBioSecure.Ticket ticket = BluxBioSecure.Ticket.fromString(mKey); 
				if(mAuthType == AuthType.CONNECT)
					mBioSecure.startConnect(ticket, mVDDevice0.getAuthorizeChannel());
				else if(mAuthType == AuthType.KEY_PAIR)
					mBioSecure.startKeyPair(ticket, mVDDevice0.getAuthorizeChannel());
				else
					Log.e("BLUX", "unknown auth type!!");
			}
			mState = State.AUTHORIZING;
		}
	}

	// BluxBiosecAuthorize delegate
	private class BiosecDelegate extends BluxBioSecure.Delegate {
		@Override
		protected void pairResult(Result result, Ticket stub, Ticket ticket) {
			Log.w("BLUX", "[BD:pr:" + result + "]");
			
			mAuthResult = result;
			if(ticket != null) {
				mKey = ticket.toString();
				mAuthType = AuthType.CONNECT;
			}
			stopPairTimer();
			
			if(result == Result.SUCCESS) {
				mState = State.AMINITIALIZING;
				mAccountManager.start(mVDDevice0.getAccountManageChannel());
			}
			
			if(delegate != null) {
				PairResult pr;
				if(result == Result.SUCCESS)
					pr = PairResult.SUCCESS;
				else if(result == Result.ERROR_USER)
					pr = PairResult.ERROR_PERMISSION;
				else if(result == Result.ERROR_KEY)
					pr = PairResult.ERROR_KEY;
				else
					pr = PairResult.ERROR_UNKNOWN;
				delegate.devicePairResult(BluxDevice.this, pr, (ticket == null) ? null : ticket.toString());
			}
		}
		@Override
		protected void connectResult(Result result) {
			Log.w("BLUX", "[BD:connr:" + result +"]");
			
			mAuthResult = result;
			if(result == Result.SUCCESS) {
				mState = State.AMINITIALIZING;
				mAccountManager.start(mVDDevice0.getAccountManageChannel());
			}
		}
	}

	// BluxAccountManager InitDelegate
	private class AccountManagerInitDelegate extends BluxAccountManager.InitDelegate {
		@Override
		protected void started(boolean success) {
			Log.w("BLUX", "[BD:am:" + success +"]");
			
			mState = State.VDINITIALIZING;
			mVDDevice0.start();
		}
	}

	// BluxVDDevice0 delegate
	private class VDDevice0Delegate extends BluxVDDevice0.Delegate {
		@Override
		protected void device0Started(boolean success) {
			Log.w("BLUX", "[BD:dv0:" + success +"]");
			
			if(success && mService != null && mVDDevice0 != null) {
				mState = State.CONNECTED;
				
				if(delegate != null) {
					delegate.deviceConnected(BluxDevice.this);
				}
			}
		}
	}

	// BluxPeripheral delegate
	private class PeripheralDelegate extends BluxPeripheral.Delegate {
		@Override void peripheralConnected(BluxPeripheral peripheral) {
			mBioSecure.reset();
		}
		@Override
		protected void peripheralDisconnected(BluxPeripheral peripheral, boolean closed) {
			Log.w("BLUX", "[BD:pdis:" + mAuthResult + " " + closed +"]");
			
			DisconnectReason dr = DisconnectReason.UNKNOWN;
			if(closed) {
				dr = DisconnectReason.CLOSED;
			}
			else {
				if(mState == State.AUTHORIZING && mAuthResult != BluxBioSecure.Result.SUCCESS) {
					if(mAuthResult == BluxBioSecure.Result.ERROR_USER)
						dr = DisconnectReason.PERMISSION;
					else if(mAuthResult == BluxBioSecure.Result.ERROR_KEY)
						dr = DisconnectReason.KEY;
				}
				else {
					dr = DisconnectReason.LINKLOST;
				}
			}
			
			if(dr == DisconnectReason.LINKLOST) {
				peripheral.connect();
			}
			
			State lastState = mState;
			mState = State.DISCONNECTED;
			if(delegate != null) {
				if(!(dr == DisconnectReason.LINKLOST && lastState != State.CONNECTED)) {
					delegate.deviceDisconnected(BluxDevice.this, dr);
				}
			}
		}
	}

}
