package com.wang.android.mode.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.wang.android.R;
import com.wang.android.mode.utils.DialogUtils;
import com.wang.android.mode.utils.KeyBoardUtils;
import com.wang.android.mode.utils.ToastUtils;
import com.wang.android.mode.utils.WangUtils;

@EActivity(R.layout.activity_registe)
public class RegisteActivity extends BaseActivity implements View.OnClickListener {

    @ViewById(R.id.edt_register_phoneNumber)
    AutoCompleteTextView phone;
    @ViewById(R.id.edt_register_pwd)
    AutoCompleteTextView password;
    @ViewById(R.id.edt_register_code)
    AutoCompleteTextView code;
    @ViewById(R.id.tv_register_submit)
    Button submit;
    @ViewById(R.id.tv_register_getCode)
    TextView tvCode;

    public Timer timer;
    int time = 0;
	private Context mContext;

    @Override
    @AfterViews
    public void initViews() {
    	super.initViews();
    	mContext = this;
    	titleTv.setText("注册");
		backImg.setVisibility(View.VISIBLE);
		
        submit.setOnClickListener(this);
        tvCode.setOnClickListener(this);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id ==KeyEvent.KEYCODE_ENTER || id  == EditorInfo.IME_ACTION_DONE) {
                    KeyBoardUtils.closeKeybord(password,mContext);
                    goRegister();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_register_submit:
                //注册
                goRegister();
                break;
            case R.id.tv_register_getCode:
                //获取验证码
                getCode();
                startTimer();
                break;
        }
    }

    private void getCode() {
        final Dialog loadingDialog = DialogUtils.createLoadingDialog(mContext, "获取二维码...");
        loadingDialog.show();
        //// TODO: 2017-03-15  

    }

    private void goRegister() {
        if(checkRegister()){
            final Dialog loadingDialog = DialogUtils.createLoadingDialog(mContext, "注册中...");
            loadingDialog.show();

            Map<String,String> param = new HashMap<>();
            param.put("","");
            param.put("","");
            param.put("","");

        }
    }

    private boolean checkRegister() {
        boolean isRegisterOk = false;
        String phoneNum = phone.getText().toString().trim();
        String psd = password.getText().toString().trim();
        String codeNum = code.getText().toString().trim();

        if(TextUtils.isEmpty(phoneNum)){
            ToastUtils.showShort(mContext,"输入手机号");
            return isRegisterOk;
        }
        if(TextUtils.isEmpty(psd)){
            ToastUtils.showShort(mContext,"输入密码");
            return isRegisterOk;
        }
        if(TextUtils.isEmpty(codeNum)){
            ToastUtils.showShort(mContext,"验证码没有填写");
            return isRegisterOk;
        }

        if(!WangUtils.isTelPhone(phoneNum)){
            ToastUtils.showShort(mContext,"手机号格式错误");
            return isRegisterOk;
        }

        if(psd.length()<6){
            ToastUtils.showShort(mContext,"请输入六位密码");
            return isRegisterOk;
        }

        if(!WangUtils.isValidCode(codeNum)){
            ToastUtils.showShort(mContext,"验证码格式错误");
            return isRegisterOk;
        }
        isRegisterOk = true;
        return isRegisterOk;
    }

    public void startTimer(){
        time = 60;
        tvCode.setEnabled(false);

        if(timer != null){
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(time < 0){
                            stopTimer();
                            return;
                        }
                        tvCode.setText((time--)+"s");
                    }
                });
            }
        }, 0, 1000);
    }

    public void stopTimer(){
        tvCode.setEnabled(true);
        tvCode.setText("获取验证码");
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
