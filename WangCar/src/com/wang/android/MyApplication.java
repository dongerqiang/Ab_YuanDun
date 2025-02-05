package com.wang.android;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fu.baseframe.FrameApplication;
import com.fu.baseframe.net.NoHttpRequest;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wang.android.mode.ble.BleInterface;
import com.wang.android.mode.net.data.Constants;
import com.wang.android.mode.net.data.UserInfo;
import com.wang.android.mode.utils.BroadcastUtils;
import com.wang.android.mode.utils.DeviceNotes;
import com.wang.android.mode.utils.SPUtils;
import com.wang.android.mode.utils.ToastB;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MyApplication extends FrameApplication {

	public static MyApplication app;
	public BleInterface ble;
	public DeviceNotes deviceNotes;
	public BroadcastUtils broadUtils;
	public String asrStr = "";
	public static RequestQueue queue;
	public UserInfo userInfo;
	@Override
	public void onCreate() {
		super.onCreate();

		app = this;

		MyApplication.app.ble = BleInterface.getInstance();
		MyApplication.app.ble.startSmartBikeService(this);
		
		deviceNotes = DeviceNotes.getInstance();
		deviceNotes.init(this);
		
		broadUtils = BroadcastUtils.getInstance();
		broadUtils.init(this);
		NoHttpRequest.dialogLayout = R.layout.dialog_loading_layout;
		queue = Volley.newRequestQueue(getApplicationContext());
		userInfo = getLocalUserinfo();
		
	}
	
	public void setSystemBar(Activity activity){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			setTranslucentStatus(activity,true);
			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintEnabled(true);
			//tintManager.setStatusBarTintColor(activity.getResources().getColor(R.color.bg));//通知栏所需颜色
//			tintManager.setStatusBarAlpha(0f);
			tintManager.statusTextColor(activity,true);
		}
	}
	
	/*public void setTranslucentStatus(Activity activity, boolean on) {
//		Window win = activity.getWindow();
//		WindowManager.LayoutParams winParams = win.getAttributes();
//		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//		if (on) {
//			winParams.flags |= bits;
//		} else {
//			winParams.flags &= ~bits;
//		}
//		win.setAttributes(winParams);
		try{
	      if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
	      Window window = activity.getWindow();
	      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
	              );
	      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                
	                      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
	      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	      window.setStatusBarColor(Color.TRANSPARENT);
	     // window.setNavigationBarColor(Color.TRANSPARENT);
	      }
		}catch (Exception e) {
			// TODO: handle exception
		}
	}*/
	

	public static void logBug(String msg) {
		Log.d("com.wang.android", msg);
	}
	
	public void showToast(String str){
		ToastB.showToast(this, str);
	}
	
	public UserInfo getLocalUserinfo(){
        String str = (String) SPUtils.get(this, Constants.LOCAL_USERINFO, "");
        if(str.isEmpty()) return null;
        UserInfo userInfo = new UserInfo();
        userInfo.password = str.split(":")[1].trim();
        userInfo.userName =str.split(":")[1].trim();
        return userInfo;
    }
	
	public boolean isLogin(){
		return userInfo != null;
	}
}
