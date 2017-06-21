package com.wang.android.mode.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fu.baseframe.utils.LogUtils;
import com.fu.baseframe.utils.SystemOpt;
import com.google.gson.Gson;
import com.wang.android.MyApplication;
import com.wang.android.R;
import com.wang.android.ServerUrl;
import com.wang.android.mode.interfaces.DialogCallback;
import com.wang.android.mode.net.HttpExecute;
import com.wang.android.mode.net.HttpRequest;
import com.wang.android.mode.net.HttpResponseListener;
import com.wang.android.mode.net.data.BikeTrackBean;
import com.wang.android.mode.net.data.WangCarDetail;
import com.wang.android.mode.net.response.BaseResponse;
import com.wang.android.mode.net.response.BikeTrackResponse;
import com.wang.android.mode.net.response.WangCarResponse;
import com.wang.android.mode.utils.DialogUtils;
import com.widget.android.utils.ConvertUtils;
import com.yolanda.nohttp.Response;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.activity_cloud_smart_control_layout)
public class CloudSmartControlActivity extends BaseActivity implements LocationSource,AMapLocationListener, OnGeocodeSearchListener {

	@ViewById
	MapView mMapView = null;
	private OnLocationChangedListener mListener;
	private AMapLocation amapLocationCurr;
	//声明AMapLocationClient类对象
	private AMapLocationClient mLocationClient = null;
	//声明AMapLocationClientOption对象
	private AMapLocationClientOption mLocationOption = null;
	AMap aMap;
	protected Bundle savedInstanceState;
	float normalZoom = 18;
	@ViewById
	Button controlBtn;
	@ViewById
	LinearLayout controlLayout,startLL,lockLL,unlockLL,findBikeLL;
	
	@ViewById
	TextView findTV,startTV,lockTV,unlockTV;
	@ViewById
	ImageView startImageView;
	
	boolean isLock =false;
	boolean isStart =false;
	boolean isAlarm = false;
	String currentImei;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
	}
	
	@Override
	@AfterViews
	public void initViews(){
		super.initViews();
		
		titleTv.setText("GPS");
		backImg.setVisibility(View.VISIBLE);
		
		initMap();
	}
	

	
	public void initMap(){
		aMap = mMapView.getMap();
		
		mMapView.onCreate(savedInstanceState);
		
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.icon_location));// 设置小蓝点的图标
		
		aMap.setMyLocationStyle(myLocationStyle);
		
		aMap.setOnMarkerClickListener(markerListener);
		aMap.setInfoWindowAdapter(infoWindowAdapter);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		UiSettings uiSettings = aMap.getUiSettings();
		uiSettings.setMyLocationButtonEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		aMap.setOnMapLoadedListener(new OnMapLoadedListener() {
			
			@Override
			public void onMapLoaded() {
				
				aMap.moveCamera(CameraUpdateFactory.zoomTo(normalZoom));
			}
		});
		
		aMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng arg0) {
				hideWindow();
			}
		});
		
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
	}

	private void hideWindow() {
        if (currMarker != null && currMarker.isInfoWindowShown()) {
            currMarker.hideInfoWindow();
        }
    }
	
	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null&& amapLocation.getErrorCode() == 0) {
				amapLocationCurr = amapLocation;
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
			
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		startPostion();
	}


	@Override
	public void deactivate() {
		mListener = null;
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
			mLocationClient.onDestroy();
		}
		mLocationClient = null;
	}
	
	private Marker currMarker;
	OnMarkerClickListener markerListener = new OnMarkerClickListener() {
		
		@Override
		public boolean onMarkerClick(Marker marker) {
			currMarker = marker;
			if (marker.getObject() instanceof WangCarDetail) {
				WangCarDetail detail = (WangCarDetail)(marker.getObject());
				getAddress(new LatLonPoint(detail.latitude, detail.longitude));
			}else{
				return false;
			}
			return true;
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
		mMapView.onResume();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState
		// (outState)，实现地图生命周期管理
		mMapView.onSaveInstanceState(outState);
	}
	
	public void startPostion(){
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mLocationClient.setLocationListener(this);
			
			//该方法默认为false。
			mLocationOption.setOnceLocation(false);
			
			mLocationOption.setInterval(5000);
			
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//设置定位参数
			mLocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mLocationClient.startLocation();
		}
	}
	
	@Click
	public void lockLL(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,0);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,0);
				return;
			}
		}
		if(isLock){
			Toast.makeText(CloudSmartControlActivity.this, "已在布防状态", Toast.LENGTH_SHORT).show();
			
			return ;
		}
		/*HttpRequest<BaseResponse> httpRequest = new HttpRequest<BaseResponse>(this, ServerUrl.ARM_ORDER+imei, new HttpResponseListener<BaseResponse>() {

			@Override
			public void onResult(BaseResponse result) {
				if(result != null && result.statusCode == 200){
					LogUtils.logDug(result.message);
					
				}else{
					Toast.makeText(CloudSmartControlActivity.this, result.message, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onFail(int code) {
				
			}
		}, BaseResponse.class, null, "POST", true);
				
		HttpExecute.getInstance().addRequest(httpRequest);*/
		
		if(loadingDialog == null){
	           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "布防中......");
	        }else{
		    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
		    	 tipTv.setText("布防中......");
		     }
			loadingDialog.show();
			 // 创建StringRequest，定义字符串请求的请求方式为POST，
	        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.ARM_ORDER+imei, new Listener<String>() {
	            // 请求成功后执行的函数
	            @Override
	            public void onResponse(String s) {
	                // 打印出POST请求返回的字符串
	            	loadingDialog.dismiss();
	                Gson gson = new Gson();
	                try {
	                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
	                    if(bikeDetailResponse.statusCode == 200){
	                    	isLock = true;
	                    	if(isLock){
	                    		setDrawableStart(lockTV,R.drawable.dot);
	                    		setDrawableStart(unlockTV,R.drawable.dot_n);
	                    	}else{
	                    		setDrawableStart(lockTV,R.drawable.dot_n);
	                    		setDrawableStart(unlockTV,R.drawable.dot);
	                    	}
	                    	LogUtils.logDug(bikeDetailResponse.message);
	                    }else{
	                    	if(bikeDetailResponse.statusCode == 400){
	                    		//查无此车
	                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
		                        
	                    	}else if(bikeDetailResponse.statusCode == 401){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
		                        
	                    	}else if(bikeDetailResponse.statusCode == 402){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
		                        
	                    	}else{	                    		
	                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
	                    	}
	                    	
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }, new ErrorListener() {
	            // 请求失败时执行的函数
	            @Override
	            public void onErrorResponse(VolleyError volleyError) {
	            	loadingDialog.dismiss();
	            }
	        }
	        ){

	            // 定义请求数据
	            @Override
	            protected Map<String, String> getParams() throws AuthFailureError {
	                Map<String, String> hashMap = new HashMap<String, String>();
//	                hashMap.put("phone", "11111");
	                return hashMap;
	            }
	        };
	        // 设置该请求的标签
	        request.setTag("lockPost");

	        // 将请求添加到队列中
	        MyApplication.queue.add(request);
	}
	
	@Click
	public void unlockLL(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,1);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,1);
				return;
			}
		}
		if(!isLock){
			Toast.makeText(CloudSmartControlActivity.this, "已在撤防状态", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		/*HttpRequest<BaseResponse> httpRequest = new HttpRequest<BaseResponse>(this, ServerUrl.UNARM_ORDER+imei, new HttpResponseListener<BaseResponse>() {

			@Override
			public void onResult(BaseResponse result) {
				if(result != null && result.statusCode == 200){
					LogUtils.logDug(result.message);
					
				}else{
					Toast.makeText(CloudSmartControlActivity.this, result.message, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onFail(int code) {
				
			}
		}, BaseResponse.class, null, "POST", true);
				
		HttpExecute.getInstance().addRequest(httpRequest);*/
		
		
		if(loadingDialog == null){
	           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "撤防中......");
	        }else{
		    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
		    	 tipTv.setText("撤防中......");
		     }
			loadingDialog.show();
			 // 创建StringRequest，定义字符串请求的请求方式为POST，
	        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.UNARM_ORDER+imei, new Listener<String>() {
	            // 请求成功后执行的函数
	            @Override
	            public void onResponse(String s) {
	                // 打印出POST请求返回的字符串
	            	loadingDialog.dismiss();
	                Gson gson = new Gson();
	                try {
	                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
	                    if(bikeDetailResponse.statusCode == 200){
	                    	isLock = false;
	                    	if(isLock){
	                    		setDrawableStart(lockTV,R.drawable.dot);
	                    		setDrawableStart(unlockTV,R.drawable.dot_n);
	                    	}else{
	                    		setDrawableStart(lockTV,R.drawable.dot_n);
	                    		setDrawableStart(unlockTV,R.drawable.dot);
	                    	}
	                    	LogUtils.logDug(bikeDetailResponse.message);
	                    }else{
	                    	if(bikeDetailResponse.statusCode == 400){
	                    		//查无此车
	                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 401){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 402){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else{	                    		
	                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
	                    	}
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }, new ErrorListener() {
	            // 请求失败时执行的函数
	            @Override
	            public void onErrorResponse(VolleyError volleyError) {
	            	loadingDialog.dismiss();
	            }
	        }
	        ){

	            // 定义请求数据
	            @Override
	            protected Map<String, String> getParams() throws AuthFailureError {
	                Map<String, String> hashMap = new HashMap<String, String>();
//	                hashMap.put("phone", "11111");
	                return hashMap;
	            }
	        };
	        // 设置该请求的标签
	        request.setTag("unlockPost");

	        // 将请求添加到队列中
	        MyApplication.queue.add(request);
	}
	
	
	
	@Click
	public void startLL(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,3);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,3);
				return;
			}
		}
		if(isStart){
			closeOrder(imei);
		}else{
			startOrder(imei);
		}
		
		/*HttpRequest<BaseResponse> httpRequest = new HttpRequest<BaseResponse>(this, ServerUrl.START_ORDER+imei, new HttpResponseListener<BaseResponse>() {

			@Override
			public void onResult(BaseResponse result) {
				if(result != null && result.statusCode == 200){
					LogUtils.logDug(result.message);
					
				}else{
					Toast.makeText(CloudSmartControlActivity.this, result.message, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onFail(int code) {
				
			}
		}, BaseResponse.class, null, "POST", true);
				
		HttpExecute.getInstance().addRequest(httpRequest);*/
		
		
	        
	}
	 Dialog  loadingDialog;
	
	public void startOrder(String imei){
		
		if(loadingDialog == null){
	           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "上电中......");
	        }else{
		    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
		    	 tipTv.setText("上电中......");
		     }
			loadingDialog.show();
			 // 创建StringRequest，定义字符串请求的请求方式为POST，
	        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.START_ORDER+imei, new Listener<String>() {
	            // 请求成功后执行的函数
	            @Override
	            public void onResponse(String s) {
	                // 打印出POST请求返回的字符串
	            	loadingDialog.dismiss();
	                Gson gson = new Gson();
	                try {
	                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
	                    if(bikeDetailResponse.statusCode == 200){
	                    	isStart = true;
	                    	if(isStart){
	                   		 	startTV.setText("断电");
	                   		 	startImageView.setImageResource(R.drawable.power_off);
	                   		 	setDrawableStart(startTV, R.drawable.dot);
	                        	
	                        }else{
	                       	 	startTV.setText("上电");
	                       	 	startImageView.setImageResource(R.drawable.power);
	                       	 	setDrawableStart(startTV, R.drawable.dot_n);
	                        }
	                    	LogUtils.logDug(bikeDetailResponse.message);
	                    }else{
	                    	if(bikeDetailResponse.statusCode == 400){
	                    		//查无此车
	                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 401){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 402){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else{	                    		
	                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
	                    	}
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }, new ErrorListener() {
	            // 请求失败时执行的函数
	            @Override
	            public void onErrorResponse(VolleyError volleyError) {
	            	loadingDialog.dismiss();
	            }
	        }
	        ){

	            // 定义请求数据
	            @Override
	            protected Map<String, String> getParams() throws AuthFailureError {
	                Map<String, String> hashMap = new HashMap<String, String>();
//	                hashMap.put("phone", "11111");
	                return hashMap;
	            }
	        };
	        // 设置该请求的标签
	        request.setTag("startPost");

	        // 将请求添加到队列中
	        MyApplication.queue.add(request);
		
	}
	
	public void closeOrder(String imei){
		/*HttpRequest<BaseResponse> httpRequest = new HttpRequest<BaseResponse>(this, ServerUrl.CLOSE_ORDER+imei, new HttpResponseListener<BaseResponse>() {

			@Override
			public void onResult(BaseResponse result) {
				if(result != null && result.statusCode == 200){
					LogUtils.logDug(result.message);
					
				}else{
					Toast.makeText(CloudSmartControlActivity.this, result.message, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onFail(int code) {
				
			}
		}, BaseResponse.class, null, "POST", true);
				
		HttpExecute.getInstance().addRequest(httpRequest);*/
		
		if(loadingDialog == null){
           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "断电中......");
        }else{
	    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
	    	 tipTv.setText("断电中......");
	     }
		loadingDialog.show();
		 // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.CLOSE_ORDER+imei, new Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
            	loadingDialog.dismiss();
                Gson gson = new Gson();
                try {
                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
                    if(bikeDetailResponse.statusCode == 200){
                    	LogUtils.logDug(bikeDetailResponse.message);
                    	isStart = false;
                    	if(isStart){
                   		 	startTV.setText("断电");
                   		 	startImageView.setImageResource(R.drawable.power_off);
                   		 	setDrawableStart(startTV, R.drawable.dot);
                        	
                        }else{
                       	 	startTV.setText("上电");
                       	 	startImageView.setImageResource(R.drawable.power);
                       	 	setDrawableStart(startTV, R.drawable.dot_n);
                        }
                    }else{
                    	if(bikeDetailResponse.statusCode == 400){
                    		//查无此车
                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 401){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 402){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
                        
                    	}else{	                    		
                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
                    	}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            	loadingDialog.dismiss();
            }
        }
        ){

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> hashMap = new HashMap<String, String>();
//                hashMap.put("phone", "11111");
                return hashMap;
            }
        };
        // 设置该请求的标签
        request.setTag("closePost");

        // 将请求添加到队列中
        MyApplication.queue.add(request);
        
	}
	
	@Click
	public void findBikeLL(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,2);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,2);
				return;
			}
		}
		if(loadingDialog == null){
			String title ="";
			if(isAlarm){
				title="关闭寻车......";
	    	}else{
	    		title="开启寻车......";
	    	}
           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, title);
	    }else{
	    	TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
	    	if(isAlarm){
	    		tipTv.setText("关闭寻车......");
	    	}else{
	    		tipTv.setText("开启寻车......");
	    	}
		    	 
		    	 
		}
		loadingDialog.show();
		if(isAlarm){
			disAlarmCar(imei);
		}else{
			alarmCar(imei);
		}
			
	}
	
	public void alarmCar(String imei){
		 // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.ALARM_URL+imei, new Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
            	loadingDialog.dismiss();
                Gson gson = new Gson();
                try {
                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
                    if(bikeDetailResponse.statusCode == 200){
                    	LogUtils.logDug(bikeDetailResponse.message);
                    	isAlarm = true;
                    	if(isAlarm){
                     		 setDrawableStart(findTV, R.drawable.dot);
                       }else{
                     		 setDrawableStart(findTV, R.drawable.dot_n);
                       }
                    }else{
                    	if(bikeDetailResponse.statusCode == 400){
                    		//查无此车
                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 401){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 402){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
                        
                    	}else{	                    		
                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
                    	}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            	loadingDialog.dismiss();
            }
        }
        ){

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> hashMap = new HashMap<String, String>();
//                hashMap.put("phone", "11111");
                return hashMap;
            }
        };
        // 设置该请求的标签
        request.setTag("findPost");

        // 将请求添加到队列中
        MyApplication.queue.add(request);
	}
	
	public void disAlarmCar(String imei){
		 // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.DISALARM_URL+imei, new Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
            	loadingDialog.dismiss();
                Gson gson = new Gson();
                try {
                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
                    if(bikeDetailResponse.statusCode == 200){
                    	LogUtils.logDug(bikeDetailResponse.message);
                    	isAlarm = false;
                    	if(isAlarm){
                     		 setDrawableStart(findTV, R.drawable.dot);
                       }else{
                     		 setDrawableStart(findTV, R.drawable.dot_n);
                       }
                    }else{
                    	if(bikeDetailResponse.statusCode == 400){
                    		//查无此车
                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 401){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 402){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
                        
                    	}else{	                    		
                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
                    	}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            	loadingDialog.dismiss();
            }
        }
        ){

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> hashMap = new HashMap<String, String>();
//                hashMap.put("phone", "11111");
                return hashMap;
            }
        };
        // 设置该请求的标签
        request.setTag("findPost");

        // 将请求添加到队列中
        MyApplication.queue.add(request);
	}

	public void searchBtn(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,5);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,5);
				return;
			}
		}
		LogUtils.logDug("url == "+ServerUrl.DETAIL_URL+imei);
		
		/*HttpRequest<BaseResponse> httpRequest = new HttpRequest<BaseResponse>(this, ServerUrl.DETAIL_URL+imei, new HttpResponseListener<BaseResponse>() {

			@Override
			public void onResult(BaseResponse result) {
				if(result != null && result.statusCode == 200){
					LogUtils.logDug(result.message);
					
				}else{
					Toast.makeText(CloudSmartControlActivity.this, result.message, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onFail(int code) {
				
			}
		}, BaseResponse.class, null, "GET", true);
		HttpExecute.getInstance().addRequest(httpRequest);*/
		
		if(loadingDialog == null){
	           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "搜索车辆......");
	     }else{
	    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
	    	 tipTv.setText("搜索车辆......");
	     }
			loadingDialog.show();
		 // 定义请求地址
        StringRequest request = new StringRequest(Request.Method.GET, ServerUrl.DETAIL_URL+imei, new Listener<String>() {
            @Override
            public void onResponse(String s) {
                // 打印出GET请求返回的字符串
                loadingDialog.dismiss();
                Gson gson = new Gson();
                try {
                    WangCarResponse bikeDetailResponse =gson.fromJson(s, WangCarResponse.class);
                    if(bikeDetailResponse.statusCode == 200){
                    	aMap.clear(true);
                        WangCarDetail data = bikeDetailResponse.data;
                        StringBuffer sb = new StringBuffer();
                        if(data !=null){
                        	isLock = data.locked;
                            isStart=data.fired;
                            sb.append("编号:"+data.imei+"\n");
                            sb.append("电量:"+data.battery+"%\n");
                            sb.append("灵敏度:"+data.grade+"等\n");
                            sb.append("维度:"+data.latitude+"\n");
                            sb.append("经度:"+data.longitude+"\n");
                            
                            sb.append("上电状态 :"+(isStart?"上电":"断电")+"\n");
                            sb.append("布防状态:"+(isLock?"布防":"撤防")+"\n");
                            sb.append("充电状态:"+(data.charging?"充电中":"未充电")+"\n");
                            sb.append("睡眠状态:"+(data.sleep?"睡眠":"未睡眠"));
                            
                            LogUtils.logDug(sb.toString());
                            
                            if(isLock){
	                    		setDrawableStart(lockTV,R.drawable.dot);
	                    		setDrawableStart(unlockTV,R.drawable.dot_n);
	                    	}else{
	                    		setDrawableStart(lockTV,R.drawable.dot_n);
	                    		setDrawableStart(unlockTV,R.drawable.dot);
	                    	}
                            if(isLock){
	                    		setDrawableStart(lockTV,R.drawable.dot);
	                    		setDrawableStart(unlockTV,R.drawable.dot_n);
	                    	}else{
	                    		setDrawableStart(lockTV,R.drawable.dot_n);
	                    		setDrawableStart(unlockTV,R.drawable.dot);
	                    	}
                            
                            if(isStart){
                       		 	startTV.setText("断电");
                       		 	startImageView.setImageResource(R.drawable.power_off);
                       		 	setDrawableStart(startTV, R.drawable.dot);
                            	
                            }else{
                           	 	startTV.setText("上电");
                           	 	startImageView.setImageResource(R.drawable.power);
                           	 	setDrawableStart(startTV, R.drawable.dot_n);
                            }
                            if(data.latitude>=0 && data.longitude>=0){
                            	LatLng changeLatlng = gpsToGaode(new LatLng(data.latitude, data.longitude));
                            	aMap.moveCamera(CameraUpdateFactory.newLatLng(changeLatlng));
    							aMap.moveCamera(CameraUpdateFactory.zoomTo(normalZoom));
    							MarkerOptions option = new MarkerOptions();
    							option.position(changeLatlng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_location));
    							Marker marker =aMap.addMarker(option);
    							setMarkerAnim(marker);
    							marker.setObject(data);
                            	
                            }
                        }
                        

                    }else {
                    	if(bikeDetailResponse.statusCode == 400){
                    		//查无此车
                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 401){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
                        
                    	}else if(bikeDetailResponse.statusCode == 402){
                    		//有故障
                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
                        
                    	}else{	                    		
                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
                    	}
                    }


                } catch (Exception e) {
                	resetUI();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            	loadingDialog.dismiss();
            	resetUI();
            }
        });

        // 设置该请求的标签
        request.setTag("Get");

        // 将请求添加到队列中
        MyApplication.queue.add(request);
		
	}
	
	@Click
	public void phoneIv(){
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMap.getMyLocation().getLatitude(),aMap.getMyLocation().getLongitude()),normalZoom));
		
	}
	
	@Click
	public void changeCarIv(){
		showDialog_Layout(CloudSmartControlActivity.this,5);
		
	}
	private Dialog iemiDialog;
	
	private void showDialog_Layout(Context context,final int code) {  
        

			iemiDialog = DialogUtils.createSearchImei(this, new DialogCallback(){
				@Override
				public void confirm() {
					// TODO Auto-generated method stub
					super.confirm();
					EditText edtInput=(EditText)(iemiDialog.findViewById(R.id.edtInput));
					String imei =edtInput.getText().toString().trim();
					if(TextUtils.isEmpty(imei)){
						Toast.makeText(CloudSmartControlActivity.this, "请输入IMEI号！", Toast.LENGTH_SHORT).show();
						resetUI();
						return ;
					}else{
						if(imei.length()<15){
							Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
							resetUI();
							return;
						}else{
							currentImei = imei;
							switch (code) {
							case 0:
								//上锁
								lockLL();
								break;
							case 1:
								//解锁
								unlockLL();
								break;
							case 2:
								//寻车
								findBikeLL();
								break;
							case 3:
								//上断电
								startLL();
								break;
							case 4:
								//轨迹查询	
								trackIv();
								break;
							case 5:
								//车辆切换	
								searchBtn();
								break;
							case 6:
								//故障检测	
								errorCheckIv();
								break;		

							default:
								break;
							}
							
						}
					}
					
				}
			});
			if(!TextUtils.isEmpty(currentImei) && currentImei.length() == 15){
				((EditText)iemiDialog.findViewById(R.id.edtInput)).setText(currentImei.toCharArray(), 0, currentImei.length());				
			}
			iemiDialog.show();
    }  
	
	@Click
	public void controlBtn(){
		
		if(controlLayout.getVisibility() == View.GONE){
			if(!TextUtils.isEmpty(currentImei)){
				updateUI();
			}
			
			controlBtn.setText("隐藏面板");
			AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.animator.anim_down_in);
			controlLayout.startAnimation(animationSet);
			controlLayout.setVisibility(View.VISIBLE);
		}else{
			
			controlBtn.setText("控制面板");
			AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.animator.anim_down_out);
			controlLayout.startAnimation(animationSet);
			controlLayout.setVisibility(View.GONE);
		}
	}
	
	private void updateUI() {
		LogUtils.logDug("isStart = "+isStart+"\n"
				+"isLock = "+isLock+"\n"
				+"isAlarm = "+isAlarm
				);
		if(isStart){
   		 	startTV.setText("断电");
   		 	startImageView.setImageResource(R.drawable.power_off);
   		 	setDrawableStart(startTV, R.drawable.dot);
        	
        }else{
       	 	startTV.setText("上电");
       	 	startImageView.setImageResource(R.drawable.power);
       	 	setDrawableStart(startTV, R.drawable.dot_n);
        }
		
		if(isLock){
    		setDrawableStart(lockTV,R.drawable.dot);
    		setDrawableStart(unlockTV,R.drawable.dot_n);
    	}else{
    		setDrawableStart(lockTV,R.drawable.dot_n);
    		setDrawableStart(unlockTV,R.drawable.dot);
    	}
		if(isAlarm){
      		 setDrawableStart(findTV, R.drawable.dot);
        }else{
      		 setDrawableStart(findTV, R.drawable.dot_n);
        }
		
	}
	
	public void resetUI(){
		controlBtn.setText("控制面板");
		AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.animator.anim_down_out);
		controlLayout.startAnimation(animationSet);
		controlLayout.setVisibility(View.GONE);
		
		startTV.setText("上电");
   	 	startImageView.setImageResource(R.drawable.power);
   	 	setDrawableStart(startTV, R.drawable.dot_n);
   	 	
   	 	setDrawableStart(lockTV,R.drawable.dot_n);
		setDrawableStart(unlockTV,R.drawable.dot_n);
		setDrawableStart(findTV, R.drawable.dot_n);
	}

	@SuppressWarnings("deprecation")
	public void setDrawableStart(TextView view,int res){
		
		Drawable drawable= getResources().getDrawable(res); 
		/// 这一步必须要做,否则不会显示.  
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
		view.setCompoundDrawables(drawable,null,null,null);
	}
	
	
	Polyline polyline;
	private GeocodeSearch geocoderSearch;
	
	
	public void clearMarker(){
		List<Marker> markers = aMap.getMapScreenMarkers();
        for (Marker marker : markers) {
            
                marker.remove();
            
        }
        if(polyline !=null){
        	polyline.remove();
        }
	}
	
	@Click
	public void trackIv(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,4);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,4);
				return;
			}
		}
		
		if(loadingDialog == null){
	           loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "获取轨迹中......");
	     }else{
	    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
	    	 tipTv.setText("获取轨迹中......");
	     }
			loadingDialog.show();
		 // 定义请求地址
		StringRequest request = new StringRequest(Request.Method.GET, ServerUrl.TRACK_URL+imei, new Listener<String>() {
        
		@Override
         public void onResponse(String s) {
             // 打印出GET请求返回的字符串
             loadingDialog.dismiss();
             Gson gson = new Gson();
             try {
                 BikeTrackResponse trackResponse =gson.fromJson(s, BikeTrackResponse.class);
                 if(trackResponse.statusCode == 200){
                	 if(aMap == null){
                		 return;
                	 }
                	 aMap.clear(true);
                	 
                     List<BikeTrackBean> data = trackResponse.data;
                     if(data ==null){
                    	 Toast.makeText(CloudSmartControlActivity.this, "没有轨迹信息", Toast.LENGTH_SHORT).show();
                         return ;
                     }
                     
                     //GPS 坐标系
                     BikeTrackBean startPosition =  data.get(0);
                     BikeTrackBean endPosition =  data.get(data.size() - 1);
                     
                     LatLng latLngStart = new  LatLng(startPosition.lat,startPosition.lng);
                     if(latLngStart.latitude > 0 && latLngStart.longitude > 0){
                    	 
                    	LatLng desLatLngStart=gpsToGaode(latLngStart);
 						aMap.moveCamera(CameraUpdateFactory.newLatLng(desLatLngStart));
 						aMap.moveCamera(CameraUpdateFactory.zoomTo(normalZoom));

 						MarkerOptions optionsStart = new MarkerOptions();
// 				        optionsStart.title("");//title不设infowindow不显示
 						optionsStart.position(desLatLngStart).
 								icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_start));
 						Marker startMarker = aMap.addMarker(optionsStart);
 						setMarkerAnim(startMarker);
 					}
                     
                    LatLng latLngStop = new  LatLng(endPosition.lat,endPosition.lng);

 					if(latLngStop.latitude > 0 && latLngStop.longitude > 0){
 						LatLng desLatLngStop=gpsToGaode(latLngStop);

 						MarkerOptions optionsStop = new MarkerOptions();
// 		                optionsStop.title("");//title不设infowindow不显示
 						optionsStop.position(desLatLngStop).
 								icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_stop));
 						Marker endMarker = aMap.addMarker(optionsStop);
 						setMarkerAnim(endMarker);
 					} 
                    
 					List<LatLng> listPath = new ArrayList<LatLng>();

					for (BikeTrackBean pathInfo : data) {
						LatLng latLng = new LatLng(pathInfo.lat,pathInfo.lng);

						if(latLng.latitude <= 0 || latLng.longitude <=0 ){
							continue;
						}
						LatLng midPath = gpsToGaode(latLng);

						listPath.add(midPath);
					}
					
					polyline =aMap.addPolyline(new PolylineOptions().addAll(listPath).width(ConvertUtils.dip2px(CloudSmartControlActivity.this, 3f)).color(Color.argb(255, 255, 170,37)));

                     
                 }else {
                	 if(trackResponse.statusCode == 400){
                 		//查无此车
                 		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
                     
                 	}else if(trackResponse.statusCode == 401){
                 		//有故障
                 		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
                     
                 	}else if(trackResponse.statusCode == 402){
                 		//有故障
                 		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
                     
                 	}else{	                    		
                 		Toast.makeText(CloudSmartControlActivity.this, trackResponse.message, Toast.LENGTH_LONG).show();
                 	}
                 }


                 
                 } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }, new ErrorListener() {
         @Override
         public void onErrorResponse(VolleyError volleyError) {
         	loadingDialog.dismiss();
         }
     });

     // 设置该请求的标签
     request.setTag("trackGet");

     // 将请求添加到队列中
     MyApplication.queue.add(request);
		
	}
	
	@Click
	public void errorCheckIv(){
		String imei = currentImei;
		if(TextUtils.isEmpty(imei)){
			showDialog_Layout(CloudSmartControlActivity.this,6);
			return ;
		}else{
			if(imei.length()<15){
				Toast.makeText(CloudSmartControlActivity.this, "请重新输入15位的IMEI号！", Toast.LENGTH_SHORT).show();
				showDialog_Layout(CloudSmartControlActivity.this,6);
				return;
			}
		}
		
		if(loadingDialog == null){
	       loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "故障检测中......");
	    }else{
	    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
	    	 tipTv.setText("故障检测中......");
		}
			loadingDialog.show();
			 // 创建StringRequest，定义字符串请求的请求方式为POST，
	        StringRequest request = new StringRequest(Request.Method.POST, ServerUrl.ERROR_CHECK+imei, new Listener<String>() {
	            // 请求成功后执行的函数
	            @Override
	            public void onResponse(String s) {
	                // 打印出POST请求返回的字符串
	            	loadingDialog.dismiss();
	                Gson gson = new Gson();
	                try {
	                	BaseResponse bikeDetailResponse =gson.fromJson(s, BaseResponse.class);
	                    if(bikeDetailResponse.statusCode == 200){
	                    	Toast.makeText(CloudSmartControlActivity.this, "车辆正常", Toast.LENGTH_LONG).show();
	                         
	                    }else{
	                    	if(bikeDetailResponse.statusCode == 400){
	                    		//查无此车
	                    		Toast.makeText(CloudSmartControlActivity.this, "查无此车辆", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 401){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "车辆有故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else if(bikeDetailResponse.statusCode == 402){
	                    		//有故障
	                    		Toast.makeText(CloudSmartControlActivity.this, "硬件未知故障", Toast.LENGTH_LONG).show();
	                        
	                    	}else{	                    		
	                    		Toast.makeText(CloudSmartControlActivity.this, bikeDetailResponse.message, Toast.LENGTH_LONG).show();
	                    	}
	                         
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }, new ErrorListener() {
	            // 请求失败时执行的函数
	            @Override
	            public void onErrorResponse(VolleyError volleyError) {
	            	loadingDialog.dismiss();
	            }
	        }
	        ){

	            // 定义请求数据
	            @Override
	            protected Map<String, String> getParams() throws AuthFailureError {
	                Map<String, String> hashMap = new HashMap<String, String>();
//	                hashMap.put("phone", "11111");
	                return hashMap;
	            }
	        };
	        // 设置该请求的标签
	        request.setTag("erroCheckPost");

	        // 将请求添加到队列中
	        MyApplication.queue.add(request);
	}
	
	public LatLng gpsToGaode(LatLng latLngStart){
		CoordinateConverter converter  = new CoordinateConverter(CloudSmartControlActivity.this);
			// CoordType.GPS 待转换坐标类型
			converter.from(CoordinateConverter.CoordType.GPS);
			// sourceLatLng待转换坐标点 DPoint类型
			converter.coord(latLngStart);
			// 执行转换操作
			LatLng desLatLngStart = converter.convert();
			return desLatLngStart;
	}
	
	/**
     * 设置生长动画
     *
     * @param marker
     */
    private void setMarkerAnim(Marker marker) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1);
        long duration = 500L;
        scaleAnimation.setDuration(duration);
        scaleAnimation.setInterpolator(new LinearOutSlowInInterpolator());
        marker.setAnimation(scaleAnimation);
        marker.startAnimation();
    }

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		LogUtils.logDug("onRegeocodeSearched() result"+result+"\n rCode = "+rCode);
		if(loadingDialog!=null && loadingDialog.isShowing()){
			loadingDialog.dismiss();
		}
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				String addressName = result.getRegeocodeAddress().getFormatAddress();
				 if (currMarker.getObject() instanceof WangCarDetail) {
					 WangCarDetail dd = (WangCarDetail )currMarker.getObject();
					 dd.address = addressName;
				}
				currMarker.showInfoWindow();
			} else {
				Toast.makeText(CloudSmartControlActivity.this, "加载失败!", Toast.LENGTH_LONG).show();
                
			}
		} else {
			Toast.makeText(CloudSmartControlActivity.this, "加载失败!", Toast.LENGTH_LONG).show();
            
		}
	}
	
	public void getAddress(final LatLonPoint latLonPoint) {
		if(loadingDialog == null){
		       loadingDialog = DialogUtils.createLoadingDialog(CloudSmartControlActivity.this, "加载中......");
		    }else{
		    	 TextView tipTv = (TextView)loadingDialog.findViewById(R.id.tipTextView);
		    	 tipTv.setText("加载中......");
			}
		loadingDialog.show();
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.GPS);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
	}
	
	
	AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter() {

		@Override
		public View getInfoContents(Marker arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public View getInfoWindow(Marker arg0) {
			View bikesView=null;
			// TODO Auto-generated method stub
			if (currMarker.getObject() instanceof WangCarDetail) {
				 WangCarDetail data = (WangCarDetail )currMarker.getObject();
				 StringBuffer sb = new StringBuffer();
                 if(data !=null){
                     sb.append("编号:"+data.imei+"\n");
                     sb.append("电量:"+data.battery+"%\n");
                     sb.append("灵敏度:"+data.grade+"等\n");
//                     sb.append("维度:"+data.latitude+"\n");
//                     sb.append("经度:"+data.longitude+"\n");
                     
                     sb.append("上电状态 :"+(data.fired?"上电":"断电")+"\n");
                     sb.append("布防状态:"+(data.locked?"布防":"撤防")+"\n");
                     sb.append("充电状态:"+(data.charging?"充电中":"未充电")+"\n");
                     sb.append("睡眠状态:"+(data.sleep?"睡眠":"未睡眠"));
                     
                     LogUtils.logDug(sb.toString());
					 bikesView = LayoutInflater.from(CloudSmartControlActivity.this).inflate(R.layout.carinfo_window_layout, null);
					 ((TextView)bikesView.findViewById(R.id.addressTV)).setText(data.address);
					 ((TextView)bikesView.findViewById(R.id.car_type)).setText(data.imei);
					 ((TextView)bikesView.findViewById(R.id.batteryTV)).setText(data.battery+"%");
					 ((TextView)bikesView.findViewById(R.id.sensitiveTV)).setText(data.grade+"级");
					 ((TextView)bikesView.findViewById(R.id.startStateTV)).setText((data.fired?"上电":"断电"));
					 ((TextView)bikesView.findViewById(R.id.chargeStateTV)).setText(data.charging?"充电中":"未充电");
					 ((TextView)bikesView.findViewById(R.id.lockStateTV)).setText(data.locked?"布防":"撤防");
					 ((TextView)bikesView.findViewById(R.id.sleepStateTV)).setText(data.sleep?"睡眠":"未睡眠");
					 ((TextView)bikesView.findViewById(R.id.outBatteryStateTV)).setText(data.address);
					 ((TextView)bikesView.findViewById(R.id.alarmStateTV)).setText(data.address);
				 
                 }
			}
			return bikesView;
		}
		
	};
	
	@Override
	public boolean isLogin() {
		// TODO Auto-generated method stub
		return true;
	}
}
