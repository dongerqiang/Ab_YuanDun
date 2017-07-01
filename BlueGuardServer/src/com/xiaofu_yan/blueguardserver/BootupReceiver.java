package com.xiaofu_yan.blueguardserver;

import com.xiaofu_yan.blueguardserver.*;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("XIAOFU", "POWER UP");
		Notification notification;
		Notification.Builder nb = new Notification.Builder(context);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setTicker("SmartGuard");
		nb.setContentTitle("SmartGuard");
		nb.setContentText("SmartGuard Sever is running!");
		notification = nb.build();
		
		Intent scActivityIntent = new Intent();
		scActivityIntent.setClass(context, ServerControlActivity.class);
		notification.contentIntent = PendingIntent.getActivity(context, 0, scActivityIntent, 0);
		
		BluxSsServer.sharedInstance().setForeGroundNotification(notification);
		BluxSsServer.sharedInstance().start(context);
	}
	
}
