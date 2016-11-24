package com.skr.fileupload.di.component;

import android.content.Context;

import com.skr.fileupload.di.module.ApplicationModule;
import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerApp;

import dagger.Component;

/**
 * @author hyw
 * @since 2016/11/24
 */
@PerApp
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    @ContextLife("Application")
    Context getApplication();

}

