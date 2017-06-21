package com.wang.android.mode.net.data;

/**
 * Created by jpj on 2017-03-07.
 */

public class UserInfo {

    public String userId;
    public String userName;
    public String password;
    public String userImgurl;
    public String mobileNo;

    /*"longitude":121.3498306274414,
       "latitude":31.221223339863737,
       "radius":2*/
    @Override
    public String toString() {
        return "{" +"username"+":"+userName+","+
                    "password"+":"+password+
                "}";
    }
}
