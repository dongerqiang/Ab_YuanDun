package com.xiaofu_yan.blux.blue_guard;

import java.util.UUID;

import android.os.Bundle;
import android.os.Message;

import com.xiaofu_yan.blux.le.client.BluxCsClient;
import com.xiaofu_yan.blux.le.client.BluxCsConnection;

class BlueGuardAccountManager extends BluxCsClient{

	// Public types
	public static class Delegate {
		public void updateAccount(Account account) {};
	}

	public Delegate delegate;


	// Private members
	private final static int CMD_GET_USER			= 1;
	private final static int CMD_SET_USER			= 2;
	private final static int RSP_UPDATE_USER	 	= 1;

	private int                 mAccountCount;

	// Public methods
	BlueGuardAccountManager(BluxCsConnection connection,
			UUID serverId, UUID clientId, Bundle data) {
		super(connection, serverId, clientId);
		mAccountCount = data.getInt("count");
		if(mAccountCount > 0) {
			// access to account #0 is not permitted.
			mAccountCount--;
		}
	}

	public int accountCount() {
		return mAccountCount;
	}

	public boolean getAccount(int index) {
		if(index >= 0 && index < mAccountCount) {
			index ++;
			Bundle data = new Bundle();
			data.putInt("index", index);
			return sendCommand(CMD_GET_USER, data);
		}
		return false;
	}

	public void setAccount(Account account) {
		if(account != null) {
			Bundle data = new Bundle();
			data.putInt("index", account.index + 1);
			data.putInt("control", account.control);
			data.putInt("passkey", account.password);
			if (account.name != null) {
				data.putString("name", account.name);
			}
			sendCommand(CMD_SET_USER, data);
		}
	}

	//BluxCsClient overrides
	@Override
	protected void handleMessage(Message msg) {
		if(delegate == null)
			return;

		switch (msg.what) {
		case RSP_UPDATE_USER:
			Bundle data = msg.getData();
			if(delegate != null && data != null) {
				Account account = new Account();
				account.index = msg.getData().getInt("index");
				account.control = msg.getData().getInt("control");
				account.password = msg.getData().getInt("passkey");
				account.name = msg.getData().getString("name");
				if(account.index > 0) {
					account.index --;
					delegate.updateAccount(account);
				}
			}
			break;
		}
	}


	static public class Account {

		// Public types
		public enum PairType {
			KEY, PASSWORD
		}
		
		// Private Members
		private final static int NVM_USER_CONTROL_PAIR           	=(0x1000);
		private final static int NVM_USER_CONTROL_CONN           	=(0x8000);

		private final static int NVM_USER_CONTROL_PAIR_TYPE         =(0x0c00);
		private final static int NVM_USER_CONTROL_PAIR_PASS         =(0x0400);
		private final static int NVM_USER_CONTROL_PAIR_KEY          =(0x0800);
		
		private int 	index;
		private int 	password;
		private int 	control;
		private String 	name;

		// Public methods
		public String name(){
			if(name == null)
				return null;
			return new String(name);
		}

		public void setName(String name) {
			if(name != null)
				name = new String(name);
		}
		public int index() {
			return this.index;
		}

		public PairType pairType() {
			PairType type;
			if((control & NVM_USER_CONTROL_PAIR_TYPE) == NVM_USER_CONTROL_PAIR_PASS)
				type = PairType.PASSWORD;
			else
				type = PairType.KEY;
			return type;
		}

		public void setPairType(PairType type) {
			if(type == PairType.KEY)
				control = (control & ~NVM_USER_CONTROL_PAIR_TYPE) | NVM_USER_CONTROL_PAIR_KEY;
			else
				control = (control & ~NVM_USER_CONTROL_PAIR_TYPE) | NVM_USER_CONTROL_PAIR_PASS;
		}

		public boolean pairEnabled() {
			return (control & NVM_USER_CONTROL_PAIR) != 0;
		}

		public void setPairEnabled(boolean enabled) {
			if(enabled)
				control |= NVM_USER_CONTROL_PAIR;
			else
				control &= ~NVM_USER_CONTROL_PAIR;
		}

		public boolean connectEnabled() {
			return (control & NVM_USER_CONTROL_CONN) != 0;
		}

		public void setConnectEnabled(boolean enabled) {
			if(enabled)
				control |= NVM_USER_CONTROL_CONN;
			else
				control &= ~NVM_USER_CONTROL_CONN;
		}
	}
}
