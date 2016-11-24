package com.skr.fileupload.di.component;

import android.content.Context;

import com.skr.fileupload.di.module.ServiceModule;
import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.di.scope.PerService;

import dagger.Component;

/**
 * @author hyw
 * @since 2016/11/24
 */
@PerService
@Component(dependencies = ApplicationComponent.class, modules = ServiceModule.class)
public interface ServiceComponent {
    @ContextLife("Service")
    Context getServiceContext();

    @ContextLife("Application")
    Context getApplicationContext();
}
