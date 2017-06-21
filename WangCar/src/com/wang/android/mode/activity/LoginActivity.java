package com.wang.android.mode.activity;

import java.util.HashMap;
import java.util.Map;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.fu.baseframe.utils.LogUtils;
import com.wang.android.R;
import com.wang.android.mode.utils.DialogUtils;
import com.wang.android.mode.utils.KeyBoardUtils;
import com.wang.android.mode.utils.ToastUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

@EActivity(R.layout.activity_login_layout)
public class LoginActivity extends BaseActivity implements OnClickListener {
	@ViewById(R.id.email)
	AutoCompleteTextView mEmailView;
	@ViewById(R.id.password)
	AutoCompleteTextView mPasswordView;
	@ViewById(R.id.email_sign_in_button)
	Button mEmailSignInButton;
	@ViewById(R.id.register)
	TextView register;

	private Dialog loadingDialog;
	private Context mContext;
	    
	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		mContext = this;
		titleTv.setText("登录");
		backImg.setVisibility(View.VISIBLE);
		 mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	            @Override
	            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
	                if (id ==KeyEvent.KEYCODE_ENTER ||id  == EditorInfo.IME_ACTION_DONE) {
	                    KeyBoardUtils.closeKeybord(mPasswordView,mContext);
	                    attemptLogin();
	                    return true;
	                }
	                return false;
	            }
	        });

	        mEmailSignInButton.setOnClickListener(this);

	        register.setOnClickListener(this);
	        
	}
	
	/**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            ToastUtils.showShort(mContext,"手机号为空");
            return;
        }
        if (TextUtils.isEmpty(password)){
            ToastUtils.showShort(mContext,"密码为空");
            return;
        }
        /*if(!DiandiUtils.isEmail(email)){
            ToastUtils.showShort(mContext,getString(R.string.error_invalid_email));
            return;
        }
        if(!DiandiUtils.isValidPassword(password)){
            ToastUtils.showShort(mContext,getString(R.string.error_invalid_password));
            return;
        }*/

        goLogin();
    }

    private void goLogin() {
    	com.wang.android.MainActivity_.intent(LoginActivity.this).start();
        
        loadingDialog = DialogUtils.createLoadingDialog(mContext, "登录中");
        loadingDialog.show();
        final String email = mEmailView.getText().toString().trim();
        final String password = mPasswordView.getText().toString().trim();
        // TODO: 2017-03-07
        Map<String,String> param = new HashMap();

        param.put("userName",email);
        param.put("password",password);
        LogUtils.logDug("username: "+email+"\n"+
                "password: "+password
        );
        
       /* RetorfitRe.getInstance().getRestApi().getUserInfo(param).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if(response.body()==null)return;
                Gson gson = new Gson();
                try {
                    BaseResponse useInfo =gson.fromJson(response.body().string(), BaseResponse.class);
                    LogUtils.w(useInfo.statusCode +"\n"+ useInfo.message);
                    if(useInfo.statusCode == 200){
                        userInfo = new UserInfo();
                        userInfo.password = password;
                        userInfo.userName = email;
                        SPUtils.put(mContext, Constants.LOCAL_USERINFO,email+":"+password);
                        ToastUtils.showShort(mContext,"登陆成功");
                        // TODO: 2017-03-15
                        finish();
                    }else{
                        ToastUtils.showShort(mContext,useInfo.message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.register:
                //
//                ToastUtils.showShort(mContext,"暂时不支持注册");
                com.wang.android.mode.activity.RegisteActivity_.intent(LoginActivity.this).start();
                break;
        }
    }
    
    
}
