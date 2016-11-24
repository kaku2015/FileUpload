package com.skr.fileupload.di.module;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerFragment;

import dagger.Module;
import dagger.Provides;

/**
 * @author hyw
 * @since 2016/11/24
 */
@Module
public class FragmentModule {
    private Fragment mFragment;

    public FragmentModule(Fragment fragment) {
        mFragment = fragment;
    }

    @Provides
    @PerFragment
    @ContextLife("Activity")
    public Context provideActivityContext() {
        return mFragment.getActivity();
    }

    @Provides
    @PerFragment
    public Activity provideActivity() {
        return mFragment.getActivity();
    }

    @Provides
    @PerFragment
    public Fragment provideFragment() {
        return mFragment;
    }
}
