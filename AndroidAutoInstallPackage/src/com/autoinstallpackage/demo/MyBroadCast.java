package com.autoinstallpackage.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadCast extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if(Intent.ACTION_PACKAGE_INSTALL.equals(arg1.getAction())){
			Log.e("DEMO","�а���װ��");
		}
		
		if(Intent.ACTION_PACKAGE_REMOVED.equals(arg1.getAction())){
			Log.e("DEMO","�а���ɾ����");
		}
	}

}
