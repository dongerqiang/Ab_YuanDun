package com.wang.android.mode.utils;


import com.wang.android.MyApplication;
import com.wang.android.R;
import com.wang.android.mode.interfaces.DialogCallback;
import com.wang.android.mode.view.wheel.AbstractWheelTextAdapter;
import com.wang.android.mode.view.wheel.WheelView;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/***
 * dialog  工具类。
 * @author fu
 *
 */
public class DialogUtils {
	private static DialogUtils dialogUtils;
	public static DialogUtils getInstance(){
		return dialogUtils == null ? dialogUtils = new DialogUtils() : dialogUtils;
	}
	
	public Dialog createDialog(Context ctx,int resLayout){
		Dialog dialog = new Dialog(ctx, R.style.custom_dialog);
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = MyApplication.app.widthPixels;
		lp.height = MyApplication.app.heightPixels;
		dialog.setContentView(resLayout);
		return dialog;
	}
	

	
	public void showSelectList(Context ctx,String[] items,final DialogCallback callback){
		final Dialog dialog = createDialog(ctx, R.layout.dialog_select_list_layout);
		final WheelView wheelView = (WheelView) dialog.findViewById(R.id.wheelView);
		final StringAdapter adapter = new StringAdapter(ctx, items);
		wheelView.setViewAdapter(adapter);
		dialog.findViewById(R.id.cancelBtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.findViewById(R.id.confirmBtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(callback!= null){
					callback.typeStr(adapter.getItemText(wheelView.getCurrentItem()).toString());
				}
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	/**
	 * Adapter for countries
	 */
	private class StringAdapter extends AbstractWheelTextAdapter {
		String[] items;
		
		
		public StringAdapter(Context context, String[] items) {
			super(context,R.layout.modify_item, NO_RESOURCE);
			this.items = items;
			
			setItemTextResource(R.id.item);
		}


		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		public int getItemsCount() {
			return items.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return items[index];
		}
	}
	
	 public static Dialog createLoadingDialog(Context context, String msg) {

	        LayoutInflater inflater = LayoutInflater.from(context);
	        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
//	        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
	        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
	        tipTextView.setText(msg);// 设置加载信息

	        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

	        loadingDialog.setCancelable(false);// 不可以用“返回键”取消

	        WindowManager.LayoutParams lp = loadingDialog.getWindow().getAttributes();
	        lp.width = MyApplication.getInstance().widthPixels;
	        lp.height = MyApplication.getInstance().heightPixels;

	        loadingDialog.setContentView(v);// 设置布局
	        return loadingDialog;

	    }
	 
	 
	 public static Dialog createSearchImei(Context ctx,final DialogCallback callback){
		 
		 LayoutInflater inflater = LayoutInflater.from(ctx);
	     View v = inflater.inflate(R.layout.dialoglayout, null);// 得到加载view
	     EditText input = (EditText)v.findViewById(R.id.edtInput);
	     Button unConfirm = (Button) v.findViewById(R.id.unconfirm);
	     Button confirm = (Button)v.findViewById(R.id.confirm);
	     final Dialog dialog = new Dialog(ctx, R.style.imei_dialog);// 创建自定义样式dialog
	     
	     unConfirm.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			
	     confirm.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(callback!= null){
						callback.confirm();
					}
					dialog.dismiss();
				}
			});
			dialog.setContentView(v);
			return dialog;
	 }
}



