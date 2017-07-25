package com.autoinstallpackage.demo;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InstallActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn1 = (Button) findViewById(R.id.btn1);
		btn1.setOnClickListener(listener1);
		Button btn2 = (Button) findViewById(R.id.btn2);
		btn2.setOnClickListener(listener2);
	}
	//��װ
	private OnClickListener listener1=new  OnClickListener(){
		public void onClick(View v) {
			String fileName = Environment.getExternalStorageDirectory() + File.separator + "baidu"+File.separator +"com.example.bluetooth.le_3.apk";
			Uri uri = Uri.fromFile(new File(fileName));
			int installFlags = 0;
			PackageManager pm = getPackageManager();
			try {
				PackageInfo pi = pm.getPackageInfo("com.example.bluetooth.le_3",PackageManager.GET_UNINSTALLED_PACKAGES);
				if(pi != null) {
					installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
				}
			} catch (NameNotFoundException e) {
			}
			MyPakcageInstallObserver observer = new MyPakcageInstallObserver();
			pm.installPackage(uri, observer, installFlags, "com.example.bluetooth.le_3");
		}
	};
	//ж��
	private OnClickListener listener2=new  OnClickListener(){
		public void onClick(View v) {
			PackageManager pm = InstallActivity.this.getPackageManager();
			IPackageDeleteObserver observer = new MyPackageDeleteObserver();
			pm.deletePackage("com.example.bluetooth.le_3", observer, 0);
		}
	};
	
	/*��Ĭ��װ�ص�*/
	class MyPakcageInstallObserver extends IPackageInstallObserver.Stub{
		
		@Override
		public void packageInstalled(String packageName, int returnCode) {
			if (returnCode == 1) {
				new ToastThread(InstallActivity.this,"安装成功").start();
			}else{
				Log.e("DEMO","��װʧ��,��������:"+returnCode);
				new ToastThread(InstallActivity.this,"安装失败:"+returnCode).start();
			}
		}
	}
	 
	/* ��Ĭж�ػص� */
	class MyPackageDeleteObserver extends IPackageDeleteObserver.Stub {

		@Override
		public void packageDeleted(String packageName, int returnCode) {
			if (returnCode == 1) {
				new ToastThread(InstallActivity.this,"ж�سɹ�").start();
			}else{
				Log.e("DEMO","ж��ʧ��...������:"+returnCode);
				new ToastThread(InstallActivity.this,"ж��ʧ��...������:"+returnCode).start();
			}
		}
	}


}
