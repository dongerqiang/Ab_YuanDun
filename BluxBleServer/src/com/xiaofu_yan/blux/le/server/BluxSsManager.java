package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

class BluxSsManager extends BluxSsProxy {

	// Private constants.
	private final static UUID	BLUX_SS_CLIENT_MANAGER_UUID = UUID.fromString("78667579-347D-43D8-9E68-CA92CF2FE7B6");

	private final static int	CMD_GET_ROOT_SERVER	= 1;
	private final static int	CMD_DETACH_PROCESS	= 2;

	private final static int	RSP_GET_ROOT_SERVER	= 1;

	// Member variables
	private Context					mContext;
	private BluxBlueGuardManager	mBlueGuardManager;

	// Constructor
	BluxSsManager(Context context) {
		super(null, null);
		mContext = context;
		// Put BluxObject's delay action handler in main thread.
		BluxObject.initThread();
	}

	void startManager() {
		mBlueGuardManager = new BluxBlueGuardManager(mContext);
		BluxSsProxy.manager().registerProxy(this);
		sActiveManager = this;
	}

	void stopManager() {
		BluxSsProxy.manager().clearAllProxies();
		mBlueGuardManager.terminate();
		sActiveManager = null;
	}

	// BluxBleProxySs base class override.
	@Override
	protected boolean handleMessage(Message cmd) {
		Bundle data;

		switch (cmd.what) {
		case CMD_GET_ROOT_SERVER:
			UUID processId = BluxSsProxy.getMessageProcess(cmd);
			if(processId != null && cmd.replyTo != null) {
				data = new Bundle();
				BluxSsBlueGuardManager bgm = new BluxSsBlueGuardManager(mBlueGuardManager, processId, cmd.replyTo);
				String to = cmd.getData().getString("from");
				if(to != null)
					data.putString("to", to);
				data.putString("from", BLUX_SS_CLIENT_MANAGER_UUID.toString());
				data.putString("server_id", bgm.uuid().toString());
				data.putString("client_id", bgm.clientId().toString());
				bgm.setStateData(data);
				try {
					Message msg = Message.obtain(null, RSP_GET_ROOT_SERVER);
					msg.setData(data);
					cmd.replyTo.send(msg);
				} catch (Exception ignored) {}
			}
			break;

		case CMD_DETACH_PROCESS:
			data = cmd.getData();
			String uuid = data.getString("process_uuid");
			BluxSsProxy.manager().notifyProcessDetach(UUID.fromString(uuid));
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	protected UUID uuid() {
		return BLUX_SS_CLIENT_MANAGER_UUID;
	}

	// Pulbic broadcast methods
	private void broadcastIntent(Bundle extra) {
		Intent intent = new Intent("com.xiaofu_yan.blux.le.server.action.broadcast");
		intent.putExtras(extra);
		mContext.sendBroadcast(intent);
	}

	static BluxSsManager sActiveManager;
	static void broadcast(Bundle msg) {
		if(sActiveManager != null) {
			sActiveManager.broadcastIntent(msg);
		}
	}
}
