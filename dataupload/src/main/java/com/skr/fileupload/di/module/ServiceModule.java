package com.skr.fileupload.di.module;

import android.app.Service;
import android.content.Context;

import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerService;

import dagger.Module;
import dagger.Provides;

/**
 * @author hyw
 * @since 2016/11/24
 */
@Module
public class ServiceModule {
    private Service mService;

    public ServiceModule(Service service) {
        mService = service;
    }

    @Provides
    @PerService
    @ContextLife("Service")
    public Context ProvideServiceContext() {
        return mService;
    }
}
