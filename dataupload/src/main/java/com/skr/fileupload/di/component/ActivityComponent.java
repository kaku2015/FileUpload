package com.skr.fileupload.di.component;

import android.app.Activity;
import android.content.Context;

import com.skr.fileupload.di.module.ActivityModule;
import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerActivity;
import com.skr.fileupload.mvp.ui.activity.MainActivity;

import dagger.Component;

/**
 * @author hyw
 * @since 2016/11/24
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    @ContextLife("Activity")
    Context getActivityContext();

    @ContextLife("Application")
    Context getApplicationContext();

    Activity getActivity();

    void inject(MainActivity mainActivity);
}
