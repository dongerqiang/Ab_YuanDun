//
// DO NOT EDIT THIS FILE.Generated using AndroidAnnotations 3.3.
//  You can create a larger work that contains this file and distribute that work under terms of your choice.
//


package com.wang.android.mode.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.wang.android.R.layout;
import org.androidannotations.api.builder.FragmentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class KeyRemoteFragment_
    extends com.wang.android.mode.fragment.KeyRemoteFragment
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();
    private View contentView_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
    }

    @Override
    public View findViewById(int id) {
        if (contentView_ == null) {
            return null;
        }
        return contentView_.findViewById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView_ = super.onCreateView(inflater, container, savedInstanceState);
        if (contentView_ == null) {
            contentView_ = inflater.inflate(layout.frament_key_layout, container, false);
        }
        return contentView_;
    }

    @Override
    public void onDestroyView() {
        contentView_ = null;
        super.onDestroyView();
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static KeyRemoteFragment_.FragmentBuilder_ builder() {
        return new KeyRemoteFragment_.FragmentBuilder_();
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        stopImg = ((ImageView) hasViews.findViewById(com.wang.android.R.id.stopImg));
        loakImg = ((ImageView) hasViews.findViewById(com.wang.android.R.id.loakImg));
        {
            View view = hasViews.findViewById(com.wang.android.R.id.gpsImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        KeyRemoteFragment_.this.gpsImg();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.wang.android.R.id.checkImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        KeyRemoteFragment_.this.checkImg();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.wang.android.R.id.carImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        KeyRemoteFragment_.this.carImg();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.wang.android.R.id.locationImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        KeyRemoteFragment_.this.locationImg();
                    }

                }
                );
            }
        }
        if (loakImg!= null) {
            loakImg.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    KeyRemoteFragment_.this.loakImg();
                }

            }
            );
        }
        {
            View view = hasViews.findViewById(com.wang.android.R.id.blueImg);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        KeyRemoteFragment_.this.blueImg();
                    }

                }
                );
            }
        }
        if (stopImg!= null) {
            stopImg.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    KeyRemoteFragment_.this.stopImg();
                }

            }
            );
        }
        initViews();
    }

    public static class FragmentBuilder_
        extends FragmentBuilder<KeyRemoteFragment_.FragmentBuilder_, com.wang.android.mode.fragment.KeyRemoteFragment>
    {


        @Override
        public com.wang.android.mode.fragment.KeyRemoteFragment build() {
            KeyRemoteFragment_ fragment_ = new KeyRemoteFragment_();
            fragment_.setArguments(args);
            return fragment_;
        }

    }

}