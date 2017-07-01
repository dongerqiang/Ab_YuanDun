package com.xiaofu_yan.blux.le.server;

import java.util.UUID;

import android.os.Handler;
import android.os.Message;

class BluxObject {
	protected UUID mUUID;

	protected void terminate() {
		mUUID = null;
	}

	protected UUID uuid() {
		if(mUUID == null)
			mUUID = UUID.randomUUID();
		return mUUID;
	}

	class DelayedAction {
		protected void act() {};
		void cancel() {
			if(sDelayHandler != null) {
				sDelayHandler.removeMessages(100, this);
			}
		}
	}

	void delayAction(DelayedAction action, int delayMillis) {
		if(action != null)
			sDelayHandler.queueAction(action, delayMillis);
	}

	// Private classes
	private static class DelayHandler extends Handler {
		void queueAction(DelayedAction action, int delayMillis) {
			Message msg = obtainMessage(100, action);
			if(delayMillis == 0)
				sendMessage(msg);
			else
				sendMessageDelayed(msg, delayMillis);
		}

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 100) {
				DelayedAction action = (DelayedAction) msg.obj;
				action.act();
			}
			super.handleMessage(msg);
		}
	}

	private static DelayHandler sDelayHandler;

	static void initThread() {
		if(sDelayHandler == null)
			sDelayHandler = new DelayHandler();
	}

}
