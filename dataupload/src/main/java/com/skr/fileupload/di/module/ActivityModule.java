package com.skr.fileupload.di.module;

import android.app.Activity;
import android.content.Context;

import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * @author hyw
 * @since 2016/11/24
 */
@Module
public class ActivityModule {
    private Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    @PerActivity
    @ContextLife("Activity")
    public Context ProvideActivityContext() {
        return mActivity;
    }

    @Provides
    @PerActivity
    public Activity ProvideActivity() {
        return mActivity;
    }
}
