package com.wang.android.mode.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;

import android.content.Context;
import android.text.TextUtils;

public class WangUtils {
	/***
     * 密码格式
     *
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) return false;
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    /**
     * 号码格式
     * @param phone
     * @return
     */
    public static boolean isTelPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String regex = "^(1(([357][0-9])|(47)|[8][012356789]))\\d{8}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * 邮箱格式
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        String regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 验证码
     * @param codeNum
     * @return
     */
    public static boolean isValidCode(String codeNum) {
        if (TextUtils.isEmpty(codeNum)) return false;

        return true;
    }

    public static boolean isValidBikeCode(String bikeCode){
        if (TextUtils.isEmpty(bikeCode)) return false;

        return true;
    }

    /**
     * 转换GPS 装华为搞的坐标
     * @param gpsLatLng
     * @return
     */
    public static LatLng gpsConvertGD(Context context,LatLng gpsLatLng){
        CoordinateConverter converter  = new CoordinateConverter(context);
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标点 DPoint类型
        converter.coord(gpsLatLng);

        return converter.convert();
    }
}
