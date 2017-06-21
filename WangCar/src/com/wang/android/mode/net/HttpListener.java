package com.wang.android.mode.net;

public interface HttpListener<T> {
	public void onStart();
	public void onFinish();
	public void onResult(T result);
	public void onFail(int code);
}
