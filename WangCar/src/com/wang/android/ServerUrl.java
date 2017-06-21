package com.wang.android;

public class ServerUrl {
	//主服务器
	public final static String MAIN_URL="http://gps.qdigo.net:13080/";
	//布防
	public final static String ARM_ORDER = MAIN_URL+"ops/lockBike/";
	//撤防
	public final static String UNARM_ORDER = MAIN_URL+"ops/unlockBike/";
	//上电
	public final static String START_ORDER = MAIN_URL+"ops/startBike/";
	//断电
	public final static String CLOSE_ORDER = MAIN_URL+"ops/closeBike/";
	
	//详情
	public final static String DETAIL_URL = MAIN_URL+"bike/getBikeDetail/";
	//寻车
	public final static String ALARM_URL = MAIN_URL+"ops/seekStart/";
	//寻车
	public final static String DISALARM_URL = MAIN_URL+"ops/seekEnd/";
	//车辆轨迹
	public final static String TRACK_URL = MAIN_URL+"bike/getTrackByTime/";
	//故障检测
	public final static String ERROR_CHECK = MAIN_URL+"ops/faultDetect/";
}
