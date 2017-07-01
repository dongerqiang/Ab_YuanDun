package com.xiaofu_yan.blux.le.server;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class BluxSsService extends Service {

	// Private members.
	private Messenger				mMessenger;
	private BluxSsManager			mManager;


	// Incoming message handler.
	static private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			BluxSsProxy.manager().deliverMessage(msg);
		}
	}


	// Service overrides
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		mMessenger = new Messenger(new IncomingHandler());
		mManager = new BluxSsManager(this);
		mManager.startManager();
		
		setToForeGround();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		mManager.stopManager();
		mManager = null;
		super.onDestroy();
	}
	
	// Private methods
	private void setToForeGround() {
		Notification notification = BluxSsServer.getNotification();
		if(notification == null) {
			Notification.Builder nb = new Notification.Builder(this);
//			nb.setSmallIcon(R.drawable.ic_launcher);
			nb.setTicker("SmartGuard");
			nb.setContentTitle("SmartGuard");
			nb.setContentText("SmartGuard server");
			notification = nb.build();
		}
		startForeground(100, notification);
	}

}
