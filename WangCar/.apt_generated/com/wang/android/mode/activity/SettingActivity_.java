//
// DO NOT EDIT THIS FILE.Generated using AndroidAnnotations 3.3.
//  You can create a larger work that contains this file and distribute that work under terms of your choice.
//


package com.wang.android.mode.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;
import com.wang.android.R.id;
import com.wang.android.R.layout;
import org.androidannotations.api.builder.ActivityIntentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class SettingActivity_
    extends SettingActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.activity_settings_layout);
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static SettingActivity_.IntentBuilder_ intent(Context context) {
        return new SettingActivity_.IntentBuilder_(context);
    }

    public static SettingActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new SettingActivity_.IntentBuilder_(fragment);
    }

    public static SettingActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new SettingActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        anzhuoCb = ((CheckBox) hasViews.findViewById(id.anzhuoCb));
        dangTv = ((TextView) hasViews.findViewById(id.dangTv));
        jdsTv = ((TextView) hasViews.findViewById(id.jdsTv));
        ljTv = ((TextView) hasViews.findViewById(id.ljTv));
        volTv = ((TextView) hasViews.findViewById(id.volTv));
        autolockCb = ((CheckBox) hasViews.findViewById(id.autolockCb));
        unlockCb = ((CheckBox) hasViews.findViewById(id.unlockCb));
        mutelockCb = ((CheckBox) hasViews.findViewById(id.mutelockCb));
        lockCb = ((CheckBox) hasViews.findViewById(id.lockCb));
        findCb = ((CheckBox) hasViews.findViewById(id.findCb));
        {
            View view = hasViews.findViewById(id.ljLayout);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        SettingActivity_.this.ljLayout();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.volLayout);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        SettingActivity_.this.volLayout();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.jdsLayout);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        SettingActivity_.this.jdsLayout();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.speedLayout);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        SettingActivity_.this.speedLayout();
                    }

                }
                );
            }
        }
        initViews();
    }

    public static class IntentBuilder_
        extends ActivityIntentBuilder<SettingActivity_.IntentBuilder_>
    {

        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            super(context, SettingActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            super(fragment.getActivity(), SettingActivity_.class);
            fragment_ = fragment;
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            super(fragment.getActivity(), SettingActivity_.class);
            fragmentSupport_ = fragment;
        }

        @Override
        public void startForResult(int requestCode) {
            if (fragmentSupport_!= null) {
                fragmentSupport_.startActivityForResult(intent, requestCode);
            } else {
                if (fragment_!= null) {
                    fragment_.startActivityForResult(intent, requestCode, lastOptions);
                } else {
                    if (context instanceof Activity) {
                        Activity activity = ((Activity) context);
                        ActivityCompat.startActivityForResult(activity, intent, requestCode, lastOptions);
                    } else {
                        context.startActivity(intent, lastOptions);
                    }
                }
            }
        }

    }

}