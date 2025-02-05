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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amap.api.maps.MapView;
import com.wang.android.R.id;
import com.wang.android.R.layout;
import org.androidannotations.api.builder.ActivityIntentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class CloudSmartControlActivity_
    extends CloudSmartControlActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.activity_cloud_smart_control_layout);
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

    public static CloudSmartControlActivity_.IntentBuilder_ intent(Context context) {
        return new CloudSmartControlActivity_.IntentBuilder_(context);
    }

    public static CloudSmartControlActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new CloudSmartControlActivity_.IntentBuilder_(fragment);
    }

    public static CloudSmartControlActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new CloudSmartControlActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        lockTV = ((TextView) hasViews.findViewById(id.lockTV));
        lockLL = ((LinearLayout) hasViews.findViewById(id.lockLL));
        startImageView = ((ImageView) hasViews.findViewById(id.startImageView));
        unlockTV = ((TextView) hasViews.findViewById(id.unlockTV));
        findTV = ((TextView) hasViews.findViewById(id.findTV));
        findBikeLL = ((LinearLayout) hasViews.findViewById(id.findBikeLL));
        startTV = ((TextView) hasViews.findViewById(id.startTV));
        startLL = ((LinearLayout) hasViews.findViewById(id.startLL));
        controlBtn = ((Button) hasViews.findViewById(id.controlBtn));
        unlockLL = ((LinearLayout) hasViews.findViewById(id.unlockLL));
        controlLayout = ((LinearLayout) hasViews.findViewById(id.controlLayout));
        mMapView = ((MapView) hasViews.findViewById(id.mMapView));
        {
            View view = hasViews.findViewById(id.changeCarIv);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        CloudSmartControlActivity_.this.changeCarIv();
                    }

                }
                );
            }
        }
        if (controlBtn!= null) {
            controlBtn.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    CloudSmartControlActivity_.this.controlBtn();
                }

            }
            );
        }
        if (lockLL!= null) {
            lockLL.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    CloudSmartControlActivity_.this.lockLL();
                }

            }
            );
        }
        {
            View view = hasViews.findViewById(id.errorCheckIv);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        CloudSmartControlActivity_.this.errorCheckIv();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.phoneIv);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        CloudSmartControlActivity_.this.phoneIv();
                    }

                }
                );
            }
        }
        if (startLL!= null) {
            startLL.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    CloudSmartControlActivity_.this.startLL();
                }

            }
            );
        }
        if (unlockLL!= null) {
            unlockLL.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    CloudSmartControlActivity_.this.unlockLL();
                }

            }
            );
        }
        if (findBikeLL!= null) {
            findBikeLL.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    CloudSmartControlActivity_.this.findBikeLL();
                }

            }
            );
        }
        {
            View view = hasViews.findViewById(id.trackIv);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        CloudSmartControlActivity_.this.trackIv();
                    }

                }
                );
            }
        }
        initViews();
    }

    public static class IntentBuilder_
        extends ActivityIntentBuilder<CloudSmartControlActivity_.IntentBuilder_>
    {

        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            super(context, CloudSmartControlActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            super(fragment.getActivity(), CloudSmartControlActivity_.class);
            fragment_ = fragment;
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            super(fragment.getActivity(), CloudSmartControlActivity_.class);
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
