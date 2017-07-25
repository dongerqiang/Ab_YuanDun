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
import android.widget.ImageView;
import android.widget.TextView;
import com.wang.android.R.id;
import com.wang.android.R.layout;
import org.androidannotations.api.builder.ActivityIntentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class CarCheckActivity_
    extends CarCheckActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.activity_car_check_layout);
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

    public static CarCheckActivity_.IntentBuilder_ intent(Context context) {
        return new CarCheckActivity_.IntentBuilder_(context);
    }

    public static CarCheckActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new CarCheckActivity_.IntentBuilder_(fragment);
    }

    public static CarCheckActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new CarCheckActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        checkItemTv = ((TextView) hasViews.findViewById(id.checkItemTv));
        zbImg = ((ImageView) hasViews.findViewById(id.zbImg));
        scImg = ((ImageView) hasViews.findViewById(id.scImg));
        asrTv = ((TextView) hasViews.findViewById(id.asrTv));
        djScaleImg = ((ImageView) hasViews.findViewById(id.djScaleImg));
        allScaleImg = ((ImageView) hasViews.findViewById(id.allScaleImg));
        allImg = ((ImageView) hasViews.findViewById(id.allImg));
        zbScaleImg = ((ImageView) hasViews.findViewById(id.zbScaleImg));
        djImg = ((ImageView) hasViews.findViewById(id.djImg));
        scScaleImg = ((ImageView) hasViews.findViewById(id.scScaleImg));
        {
            View view = hasViews.findViewById(id.repertImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        CarCheckActivity_.this.repertImg();
                    }

                }
                );
            }
        }
        initViews();
    }

    public static class IntentBuilder_
        extends ActivityIntentBuilder<CarCheckActivity_.IntentBuilder_>
    {

        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            super(context, CarCheckActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            super(fragment.getActivity(), CarCheckActivity_.class);
            fragment_ = fragment;
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            super(fragment.getActivity(), CarCheckActivity_.class);
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
