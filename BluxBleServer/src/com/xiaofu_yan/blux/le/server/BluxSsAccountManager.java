package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

public class BluxSsAccountManager extends BluxSsProxy {

	// Public methods
	BluxSsAccountManager(BluxAccountManager bam, UUID processId, Messenger reply) {
		super(processId, reply);
		mAccountManager = bam;
		mAccountManagerDelegate = new AccountManagerDelegate();
		mAccountManager.registerDelegate(mAccountManagerDelegate);
	}

	@Override
	protected void terminate() {
		if(mAccountManager != null) {
			mAccountManager.unregisterDelegate(mAccountManagerDelegate);
			mAccountManager = null;
			mAccountManagerDelegate = null;
		}
		super.terminate();
	}

	void setStateData(Bundle data) {
		data.putInt("count", mAccountManager.count());
	}


	// Private members
	private final static int CMD_GET_USER			= 1;
	private final static int CMD_SET_USER			= 2;
	private final static int RSP_UPDATE_USER	 	= 1;

	private BluxAccountManager					mAccountManager;
	private AccountManagerDelegate				mAccountManagerDelegate;

	// Base class override
	@Override
	protected boolean handleMessage(Message cmd) {
		if (super.handleMessage(cmd))
			return true;

		switch (cmd.what) {
		case CMD_SET_USER:
			if(mAccountManager != null) {
				int index = cmd.getData().getInt("index");
				int control = cmd.getData().getInt("control");
				int passkey = cmd.getData().getInt("passkey");
				String name = cmd.getData().getString("name");
				BluxAccountManager.Account account = new BluxAccountManager.Account(index, control, passkey, name);
				mAccountManager.setAccount(account);
			}
			break;

		case CMD_GET_USER:
			if(mAccountManager != null) {
				int index = cmd.getData().getInt("index");
				mAccountManager.getAccount(index);
			}
			break;
		default:
			return false;
		}
		return true;
	}

	// BluxAccountManager delegate
	private class AccountManagerDelegate extends BluxAccountManager.Delegate {
		@Override
		protected void updateAccount(BluxAccountManager.Account account){
			Bundle data = new Bundle();
			data.putInt("index", account.index);
			data.putInt("control", account.control);
			data.putInt("passkey", account.passkey);
			if(account.name != null) {
				data.putString("name", account.name);
			}
			notifyClient(RSP_UPDATE_USER, data);
		}
	}

}
