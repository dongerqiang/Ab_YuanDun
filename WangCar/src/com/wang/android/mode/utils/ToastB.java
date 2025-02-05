package com.wang.android.mode.utils;

import android.content.Context;
import android.widget.Toast;
public class ToastB {
	private static Toast mToast;  
    public static void showToast(Context context,String text) {    
        if(mToast == null) {    
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);    
        } else {    
            mToast.setText(text);      
            mToast.setDuration(Toast.LENGTH_SHORT);    
        }    
        mToast.show();    
    }    
        
    public void cancelToast() {    
            if (mToast != null) {    
                mToast.cancel();    
            }    
        }     
}

