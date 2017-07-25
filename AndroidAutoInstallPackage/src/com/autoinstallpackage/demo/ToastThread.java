package com.autoinstallpackage.demo;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastThread extends Thread{
	
	private Context mContext;
	private String msg;
	
	public ToastThread(Context context,String msg){
		this.mContext = context;
		this.msg = msg;
	}
	
	@Override
	public void run(){
		try{
			Looper.prepare();
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			Looper.loop();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

}
