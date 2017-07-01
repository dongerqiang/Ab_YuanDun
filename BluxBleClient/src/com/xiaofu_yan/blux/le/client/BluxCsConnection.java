package com.xiaofu_yan.blux.le.client;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class BluxCsConnection {
	
	// Subclass methods.
	protected boolean connectServer(Context context) {
		mContext = context;
		try {
			Intent intent = new Intent(serverAction());
			mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		} catch (Exception except) {
			return false;
		}
		return true;
	}

	protected void disconnectServer() {
		Bundle data = new Bundle();
		Message msg = Message.obtain(null, CMD_DETACH_PROCESS);
		
		data.putString("to", BLUX_SS_CLIENT_MANAGER_UUID.toString());
		sendCommand(msg, data);
		
		mContext.unbindService(mConnection);
	}
	
	protected void getServerRootObject() {
		Bundle data = new Bundle();
		Message cmd = Message.obtain(null, CMD_GET_ROOT_SERVER);
		data.putString("from", mConnectionUuid.toString());
		data.putString("to", BLUX_SS_CLIENT_MANAGER_UUID.toString());
		sendCommand(cmd, data);
	}
	
	// Subclass overrides.
	protected void onConnected() {
	}
	
	protected void onDisconnected() {
	}
	
	protected void foundServerRootObject(UUID serverUuid, UUID clientUuid, Bundle msgData) {
	}
	
	protected String serverAction() {
		return SERVER_ACTION;
	}
	
	
	// Package level helper.
	static UUID getMessageSender(Message msg) {
		return UUID.fromString(msg.getData().getString("from"));
	}

	static UUID getMessageReceiver(Message msg) {
		return UUID.fromString(msg.getData().getString("to"));
	}

	void registerClient(UUID clientUuid, BluxCsClient client) {
		if (mClients.get(clientUuid) == null)
			mClients.put(clientUuid, client);
	}

	void unregisterClient(UUID clientUuid) {
		mClients.remove(clientUuid);
	}

	boolean sendCommand(Message cmd, Bundle data) {
		data.putString("process_uuid", mConnectionUuid.toString());
		cmd.setData(data);
		cmd.replyTo = mReplyMessenger;
		try {
			mCommandMessenger.send(cmd);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	

	// Private constants.
	private final static String SERVER_ACTION = "com.xiaofu_yan.blux.le.server.BluxSsService.ACTION";

	private final static UUID	BLUX_SS_CLIENT_MANAGER_UUID = UUID.fromString("78667579-347D-43D8-9E68-CA92CF2FE7B6");
	private final static int	CMD_GET_ROOT_SERVER	= 1;
	private final static int	CMD_DETACH_PROCESS	= 2;
	
	private final static int	RSP_GET_ROOT_SERVER	= 1;
	
	// Private variables.
	private UUID				mConnectionUuid = UUID.randomUUID();
	private Context 			mContext;
	private Messenger			mCommandMessenger;
	private Messenger 			mReplyMessenger = new Messenger(new ReplyHandler(this));
	private ServiceConnection 	mConnection = new BServiceConnection();
	private HashMap<UUID, BluxCsClient> 	mClients = new HashMap<UUID, BluxCsClient>();

	// Private classes
	private class BServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mCommandMessenger = new Messenger(service);
			
			onConnected();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mCommandMessenger = null;
			
			onDisconnected();
		}
	};

	static private class ReplyHandler extends Handler {
		private final WeakReference<BluxCsConnection> mConnection;

		public ReplyHandler(BluxCsConnection connection) {
			mConnection = new WeakReference<BluxCsConnection>(connection);
		}

		@Override
		public void handleMessage(Message msg) {
			BluxCsConnection connection = mConnection.get();
			
			if (connection != null) {
				if(getMessageSender(msg).compareTo(BLUX_SS_CLIENT_MANAGER_UUID) == 0){
					connection.handleClientManagerMessage(msg);
				}
				else {
					BluxCsClient proxy = connection.mClients.get(getMessageReceiver(msg));
					if (proxy != null)
						proxy.handleMessage(msg);
					else
						super.handleMessage(msg);
				}
			}
		}
	};
	
	// Private helper
	private void handleClientManagerMessage(Message msg) {
		if(msg.what == RSP_GET_ROOT_SERVER) {
			Bundle data = msg.getData();
			UUID serverId = UUID.fromString(data.getString("server_id"));
			UUID clientId = UUID.fromString(data.getString("client_id"));
			foundServerRootObject(serverId, clientId, data);
		}
	}
	
}
