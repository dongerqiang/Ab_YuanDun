package com.xiaofu_yan.smartbikedemo;

import com.xiaofu_yan.blux.blue_guard.BlueGuard;
import com.xiaofu_yan.blux.smart_bike.SmartBike;
import com.xiaofu_yan.blux.smart_bike.SmartBikeManager;
import com.xiaofu_yan.blux.smart_bike.SmartBikeServerConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class BlueGuardActivity extends Activity {

	private SmartBikeServerConnection	mServerConnection;
	private SmartBikeManager			mSmartBikeManager;
	private SmartBike					mSmartBike;
	private String						mKey;
	
	private EditText					mPassEditView;

	// UI event
	public void onButtonClick(View button) {
		if(button.getId() != R.id.connect_switch && !mSmartBike.connected())
			return;

		switch(button.getId()) {
		case R.id.button_arm:
			mSmartBike.setState(BlueGuard.State.ARMED);
			break;

		case R.id.button_stop:
			mSmartBike.setState(BlueGuard.State.STOPPED);
			break;

		case R.id.button_start:
			//mSmartBike.setState(BlueGuard.State.STARTED);
			mSmartBike.openTrunk();
			break;

		case R.id.button_find:
			mSmartBike.playSound(BlueGuard.Sound.FIND);
			break;
		}
	}

	// activity overrides.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mServerConnection = new SmartBikeServerConnection();
		mServerConnection.delegate = new ServerConnectionDelegate();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setContentView(R.layout.layout_smart_guard);
		SeekBar sb = (SeekBar)findViewById(R.id.seek_position);
		sb.setMax(100);
		Switch sw;
		sw = (Switch)findViewById(R.id.connect_switch);
		sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mSmartBike == null)
					return;
				if(isChecked) {
					if(mKey != null) {
						mSmartBike.setConnectionKey(mKey);
						mSmartBike.connect();
					}
					else {
						LayoutInflater inflater = (LayoutInflater) BlueGuardActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
						View layout = inflater.inflate(R.layout.pair_pass_input, (ViewGroup)findViewById(R.id.pair_pass_word_layout));
						mPassEditView = (EditText) layout.findViewById(R.id.pair_pass_key_input);
						mPassEditView.setText("000000");
						
						AlertDialog dialog = new AlertDialog.Builder(BlueGuardActivity.this).
								setTitle("Pair").
								setMessage("Pair key:").
								setPositiveButton("OK", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String password = BlueGuardActivity.this.mPassEditView.getText().toString();
										mSmartBike.pair(Integer.decode(password));
									}
								}).setView(layout).create();
						dialog.show();
					}
				}
				else
					mSmartBike.cancelConnect();

			}
		});
		sw = (Switch)findViewById(R.id.auto_arm_switch);
		sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mSmartBike == null)
					return;
				if(isChecked)
					mSmartBike.setAutoArmRangePercent(mSmartBike.currentRangePercent());
				else
					mSmartBike.setAutoArmRangePercent(-1);
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mServerConnection.connect(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mServerConnection.disconnect();
	}

	private void uiUpdateConnection() {
		Switch sw;
		sw = (Switch) findViewById(R.id.connect_switch);
		sw.setChecked(mSmartBike.connected());
	}

	private void uiUpdateState() {
		Button btnArm, btnFind, btnStart, btnStop;

		BlueGuard.State state = mSmartBike.state();
		boolean connected = mSmartBike.connected();

		Switch swAutoArm = (Switch) findViewById(R.id.auto_arm_switch);
		swAutoArm.setEnabled(connected);

		btnArm = (Button)findViewById(R.id.button_arm);
		btnArm.setEnabled(connected);
		btnFind = (Button)findViewById(R.id.button_find);
		btnFind.setEnabled(connected);
		btnStart = (Button)findViewById(R.id.button_start);
		btnStart.setEnabled(connected);
		btnStop = (Button)findViewById(R.id.button_stop);
		btnStop.setEnabled(connected);


		TextView tView = (TextView)findViewById(R.id.text_view_state);
		if(connected) {
			swAutoArm.setChecked(mSmartBike.autoArmRangePercent() >= 0 && mSmartBike.autoArmRangePercent() <= 100);

			if(state == BlueGuard.State.ARMED) {
				tView.setText("Armed");
				btnArm.setEnabled(false);
			}
			else if(state == BlueGuard.State.STOPPED) {
				tView.setText("Stopped");
				btnStop.setEnabled(false);
			}
			else if(state == BlueGuard.State.STARTED) {
				tView.setText("Started");
				btnStart.setEnabled(false);
			}
			else if(state == BlueGuard.State.RUNNING) {
				tView.setText("Running");
				btnArm.setEnabled(false);
				btnStop.setEnabled(false);
				btnStart.setEnabled(false);
			}
		}
		else {
			tView.setText("not connected");
		}
	}

	private void uiUpdateName(String name) {
		TextView tView = (TextView)findViewById(R.id.text_view_device_info);
		tView.setText(name);
	}

	// BluxConnection delegate
	class ServerConnectionDelegate extends SmartBikeServerConnection.Delegate {
		@Override
		public void smartBikeServerConnected(SmartBikeManager smartBikeManager) {
			mSmartBikeManager = smartBikeManager;
			mSmartBikeManager.delegate = new SmartBikeManagerDelegate();
			mSmartBikeManager.setContext(getBaseContext());

			Intent intent = getIntent();
			String identifier = intent.getStringExtra("identifier");
			mKey = intent.getStringExtra("key");
			smartBikeManager.getDevice(identifier);
		}
	}

	// SmartGuardManager delegate
	class SmartBikeManagerDelegate extends SmartBikeManager.Delegate {
		@Override
		public void smartBikeManagerFoundSmartBike(String identifier, String name, int nMode) {
		}

		@Override
		public void smartBikeManagerGotSmartBike(SmartBike smartBike) {
			mSmartBike = smartBike;
			mSmartBike.delegate = new SmartBikeDelegate();
			uiUpdateConnection();
			uiUpdateState();
			uiUpdateName(mSmartBike.name());
		}
	}

	// SmartBike delegate
	private class SmartBikeDelegate extends SmartBike.Delegate {
		@Override
		public void blueGuardConnected(BlueGuard blueGuard) {
            mSmartBike.getAccountManager(); //

			uiUpdateConnection();
			uiUpdateState();
		}

		@Override
		public void blueGuardDisconnected(BlueGuard blueGuard, BlueGuard.DisconnectReason reason) {
			uiUpdateConnection();
			uiUpdateState();
		}

		@Override
		public void blueGuardName(BlueGuard blueGuard, String name) {
			uiUpdateName(name);
		}

		@Override
		public void blueGuardState(BlueGuard blueGuard, BlueGuard.State state) {
			uiUpdateState();
		}
		
		@Override
		public void blueGuardPairResult(BlueGuard blueGuard, BlueGuard.PairResult result, String key) {
			mKey = key;
			DeviceDB.Record rec = new DeviceDB.Record(blueGuard.name(), blueGuard.identifier(), key);
			DeviceDB.save(BlueGuardActivity.this, rec);
		}
	}

}
