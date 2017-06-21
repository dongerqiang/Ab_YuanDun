package com.wang.android.mode.activity;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.wang.android.R;
import com.wang.android.mode.activity.DeviceDB.Record;
import com.wang.android.mode.interfaces.IBlueCallback;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

@EActivity(R.layout.activity_scan_device_layout)
public class ScanActivity extends BaseActivity {
	@ViewById
	ListView listView;
	@ViewById
	TextView hintTv;
	
	private BlueGuardList mDeviceList;
	private BleCallBack bleCallBack;

	@Override
	@AfterViews
	public void initViews(){
		super.initViews();
		
		backImg.setVisibility(View.VISIBLE);
		titleTv.setText("搜索设备");
		
		mDeviceList = new BlueGuardList();
		mDeviceList.reset();
		
		listView.setAdapter(mDeviceList);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				DeviceDB.Record record = mDeviceList.getDevice(arg2);
				app.ble.closeDisconverBleDevice();
				app.ble.disConnectDevice();
				app.ble.connectDevice(record.identifier);
				finish();
			}
		});
		
		app.ble.addBlueCallback(bleCallBack = new BleCallBack());
		
		app.ble.disconverBleDevice();
	}
	

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.ble.closeDisconverBleDevice();
		app.ble.removeBlueCallBack(bleCallBack);
	}
	
	
	@SuppressLint({ "InflateParams", "ViewHolder" })
	private class BlueGuardList extends BaseAdapter {
		@Override
		public int getCount() {
			return mDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			// General ListView optimization code.
			LayoutInflater inflater = ScanActivity.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.listitem_device, null);//viewGroup);
			updateListItem(row, getDevice(i));
			return row;
		}

		// Public members.
		void addDevice(DeviceDB.Record r) {
			for(int i=0; i<mDevices.size(); i++) {
				//ListItem t = mDevices.get(i);
				//if(t.rec.identifier.equals(r.identifier)) return;
				DeviceDB.Record t = mDeviceList.getDevice(i);
				if(t.identifier.equals(r.identifier)) return;
			}
			
			ListItem item = new ListItem(r);
			mDevices.add(item);
			notifyDataSetChanged();
		}
		
		DeviceDB.Record getDevice(int index) {
			return mDevices.get(index).rec;
		}

		void reset() {
			mDevices.clear();
//			DeviceDB.Record rec = DeviceDB.load(ScanActivity.this);
//			if(rec != null)
//				mDeviceList.addDevice(rec);
//			notifyDataSetChanged();
		}

		// Private members.
		private class ListItem {
			DeviceDB.Record rec;
			ListItem(DeviceDB.Record r) {
				rec = r;
			}
		}
		private List<ListItem> mDevices = new ArrayList<ListItem>();

		private void updateListItem(View row, DeviceDB.Record rec) {
			TextView txt;
			txt = (TextView)row.findViewById(R.id.device_name);
			txt.setText(rec.name);
			txt = (TextView)row.findViewById(R.id.device_address);
			txt.setText(rec.identifier);
		}
	}


	class BleCallBack extends IBlueCallback{
		@Override
		public void discoverDevice(Record record) {
			hintTv.setVisibility(View.GONE);
			mDeviceList.addDevice(record);
		}
	}
	
	@Override
	public boolean isLogin() {
		// TODO Auto-generated method stub
		return true;
	}
}
