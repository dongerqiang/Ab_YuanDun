package com.xiaofu_yan.blux.le.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

class BluxSsProxy extends BluxObject{

	// Private constants.
	private final static int 	CMD_BROADCAST_MAX 				= -1;
	protected final static int	CMD_BROADCAST_TERMINATE			= -1;
	private final static int 	CMD_BROADCAST_PROCESS_DETACHED 	= -100;
	private final static int 	CMD_BROADCAST_MIN 				= -200;


	// BluxSsProxy.Manager
	static class Manager {
		private HashMap<UUID, BluxSsProxy> mProxies = new HashMap<UUID, BluxSsProxy>();

		void registerProxy(BluxSsProxy proxy) {
			if (mProxies.get(proxy.uuid()) == null) {
				mProxies.put(proxy.uuid(), proxy);
			}
		}

		void unregisterProxy(BluxSsProxy proxy) {
			mProxies.remove(proxy.uuid());
		}

		void clearAllProxies() {
			Message msg = Message.obtain(null, CMD_BROADCAST_TERMINATE);
			deliverMessage(msg);
			mProxies.clear();
		}

		void deliverMessage(Message msg) {
			if(msg.what >= CMD_BROADCAST_MIN && msg.what <= CMD_BROADCAST_MAX) {
				Set<UUID> set = mProxies.keySet();
				for (UUID e : set) {
					mProxies.get(e).handleMessage(msg);
				}
				clearTerminatedProxies();
			}
			else {
				BluxSsProxy proxy;
				proxy = mProxies.get(getMessageReceiver(msg));
				if (proxy != null) {
					proxy.handleMessage(msg);
				}
			}
		}

		void notifyProcessDetach(UUID processId) {
			Bundle data = new Bundle();
			data.putString("process_uuid", processId.toString());
			Message msg = Message.obtain(null, CMD_BROADCAST_PROCESS_DETACHED);
			msg.setData(data);
			deliverMessage(msg);
		}

		private void clearTerminatedProxies() {
			Set<Entry<UUID, BluxSsProxy>> set = mProxies.entrySet();
			for (Iterator<Entry<UUID, BluxSsProxy>> iterator = set.iterator(); iterator.hasNext(); ) {
				Entry<UUID, BluxSsProxy> e = iterator.next();
				if (e.getValue().mTerminated) {
					iterator.remove();
				}
			}
		}
	}

	static Manager 								mManager;
	static Manager manager() {
		if(mManager == null)
			mManager = new Manager();
		return mManager;
	}


	// Public helper
	static UUID getMessageProcess(Message msg) {
		String s = msg.getData().getString("process_uuid");
		if(s == null)
			return null;
		return UUID.fromString(s);
	}

	static UUID getMessageReceiver(Message msg) {
		String s = msg.getData().getString("to");
		if(s == null)
			return null;
		return UUID.fromString(s);
	}

	static UUID getMessageSender(Message msg) {
		String s = msg.getData().getString("from");
		if(s == null)
			return null;
		return UUID.fromString(s);
	}



	// Private members.
	private UUID		mClientId = UUID.randomUUID();
	private UUID		mProcessId;
	private Messenger	mReply;
	private boolean		mTerminated;


	// Public methods.
	BluxSsProxy(UUID processId, Messenger reply) {
		mProcessId = processId;
		mReply = reply;
		mTerminated = false;
		if(processId != null && reply != null)
			manager().registerProxy(this);
	}

	UUID clientId() {
		return mClientId;
	}

	// Subclass overrides.
	protected void terminate() {
		mTerminated = true;
		super.terminate();
	}

	// will handle life management message.
	protected boolean handleMessage(Message cmd) {
		UUID processId;

		switch (cmd.what) {
		case CMD_BROADCAST_PROCESS_DETACHED:
			processId = getMessageProcess(cmd);
			if(processId.compareTo(mProcessId) != 0) {
				break;
			}//else fall through			
		case CMD_BROADCAST_TERMINATE:
			terminate();
			return false;
		}
		return false;
	}


	// Subclass helper.
	void notifyClient(int what, Bundle data) {
		Message msg = Message.obtain(null, what);
		if(data == null)
			data = new Bundle();
		data.putString("from", uuid().toString());

		if(mReply != null) {
			try {
				data.putString("to", mClientId.toString());
				msg.setData(data);
				mReply.send(msg);
			}
			catch (Exception e) {
				delayAction(new DelayedAction() {
					protected void act() {
						terminate();
					}
				}, 0);
			}
		}
	}

}
