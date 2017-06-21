package com.wang.android.mode.activity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.wang.android.R;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

@EActivity(R.layout.activity_car_check_layout)
public class CarCheckActivity extends BaseActivity {
	
	@ViewById
	TextView checkItemTv,asrTv;
	@ViewById
	ImageView djScaleImg,djImg,zbScaleImg,zbImg,scScaleImg,scImg,allScaleImg,allImg;
	Map<ImageView, Integer> imgResSelect = new HashMap<ImageView, Integer>();
	Map<ImageView, Integer> imgResNormal = new HashMap<ImageView, Integer>();
	Map<ImageView, String> checkItem = new HashMap<ImageView, String>();
	
	LinkedList<Map<String,ImageView>> linkList = new LinkedList<Map<String,ImageView>>();
	
	Map<String,ImageView>  optMap = null;
	
	Thread thread;
	
    boolean isRun;
    
    boolean show;
    
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		
		titleTv.setText("故障检测");
		backImg.setVisibility(View.VISIBLE);
		
		imgResNormal.put(djImg,R.drawable.icon_check_motor_d);
		imgResNormal.put(zbImg,R.drawable.icon_check_zb_d);
		imgResNormal.put(scImg,R.drawable.icon_check_sc_d);
		imgResNormal.put(allImg,R.drawable.icon_check_all_d);
		
		imgResSelect.put(djImg,R.drawable.icon_check_motor_s);
		imgResSelect.put(zbImg,R.drawable.icon_check_zb_s);
		imgResSelect.put(scImg,R.drawable.icon_check_sc_s);
		imgResSelect.put(allImg,R.drawable.icon_check_all_s);
		
		checkItem.put(djImg,"电机检测");
		checkItem.put(zbImg,"转把检测");
		checkItem.put(scImg,"刹车检测");
		checkItem.put(allImg,"综合检测");
		
		startCheck();
	}
	
	private void startCheck(){
		
		asrTv.setText("");
		
		Map<String,ImageView> diMap = new HashMap<String, ImageView>();
		diMap.put("r", djScaleImg);
		diMap.put("s", djImg);
		linkList.add(diMap);
		
		Map<String,ImageView> zbMap = new HashMap<String, ImageView>();
		zbMap.put("r", zbScaleImg);
		zbMap.put("s", zbImg);
		linkList.add(zbMap);
		
		Map<String,ImageView> scMap = new HashMap<String, ImageView>();
		scMap.put("r", scScaleImg);
		scMap.put("s", scImg);
		linkList.add(scMap);
		
		Map<String,ImageView> allMap = new HashMap<String, ImageView>();
		allMap.put("r", allScaleImg);
		allMap.put("s", allImg);
		linkList.add(allMap);
		
		isRun = true;
		
		thread = new Thread(new Runnable() {
			public void run() {
				while(isRun && !linkList.isEmpty()){
					if(optMap != null){
						optMap.get("r").getAnimation().cancel();
						optMap.get("r").setAnimation(null);
					}
					optMap = linkList.removeFirst();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							rotateCheck(optMap.get("r"),optMap.get("s"));
						}
					});
					
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(optMap != null){
					if(optMap.get("r").getAnimation() != null){
						optMap.get("r").getAnimation().cancel();
					}
					optMap.get("r").setAnimation(null);
					optMap = null;
				}
				isRun = false;
				
				runOnUiThread(new Runnable() {
					public void run() {
						showResult();
					}
				});
			}
		});
		
		thread.start();
		
	}
	
	public void showResult(){
		if(show && !isRun){
			asrTv.setText(app.asrStr);
			checkItemTv.setText("检测完成");
		}
	}
	
	@Click
	public void repertImg(){
		if(!isRun){
			startCheck();
		}
	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		show = true;
		showResult();
	}
	@Override
	protected void onStop() {
		super.onStop();
		show = false;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		show = false;
		if(isRun){
			isRun = false;
			thread = null;
			linkList.clear();
		}
	}
	
	public void rotateCheck(final ImageView rotateImg,final ImageView selectImg){
		RotateAnimation rotate =new RotateAnimation(0f,360f,Animation.RELATIVE_TO_SELF, 
				0.5f,Animation.RELATIVE_TO_SELF,0.5f); 
		rotate.setDuration(1000);
		rotate.setRepeatCount(-1);
		rotate.setInterpolator(new LinearInterpolator());
		rotateImg.startAnimation(rotate);
		rotate.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				rotateImg.setVisibility(View.VISIBLE);
				checkItemTv.setText(checkItem.get(selectImg));
				selectImg.setImageResource(imgResSelect.get(selectImg));
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				//rotateImg.setVisibility(View.INVISIBLE);
				selectImg.setImageResource(imgResNormal.get(selectImg));
			}
		});
		
	}
	
	@Override
	public boolean isLogin() {
		// TODO Auto-generated method stub
		return true;
	}
}
