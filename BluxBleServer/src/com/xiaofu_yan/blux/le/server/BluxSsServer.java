package com.xiaofu_yan.blux.le.server;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;


public class BluxSsServer {
	private Context 				mContext;
	private boolean					mServiceRunning;
	private static Notification 	sNotification;
	private static BluxSsServer		sServer;
	
	public static BluxSsServer sharedInstance() {
		if(sServer == null)
			sServer = new BluxSsServer();
		return sServer;
	}

	public void setForeGroundNotification(Notification notification) {
		sNotification = notification;
	}
	
	public void start(Context context) {
		if(!mServiceRunning && sNotification != null) {
			mContext = context;
			Intent intent = new Intent(context, BluxSsService.class);
			mContext.startService(intent);
			mServiceRunning = true;
		}
	}
	public void stop() {
		if(mServiceRunning && mContext != null) {
			Intent intent = new Intent(mContext, BluxSsService.class);
			mContext.stopService(intent);
			mServiceRunning = false;
		}
	}
	
	public boolean isStarted() {
		return mServiceRunning;
	}

	static Notification getNotification() {
		return sNotification;
	}
}
