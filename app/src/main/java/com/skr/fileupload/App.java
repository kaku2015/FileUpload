package com.skr.fileupload;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;

import com.skr.fileupload.di.component.ApplicationComponent;
import com.skr.fileupload.di.component.DaggerApplicationComponent;
import com.skr.fileupload.di.module.ApplicationModule;
import com.skr.fileupload.fileupload.BuildConfig;
import com.skr.fileupload.repository.db.GreenDaoManager;
import com.socks.library.KLog;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class App extends Application {

    public static Context getAppContext() {
        return sAppContext;
    }

    private static Context sAppContext;

    private static ApplicationComponent mApplicationComponent;
    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        initLeakCanary();

        sAppContext = this;
        initActivityLifecycleLogs();
        initStrictMode();
        KLog.init(BuildConfig.LOG_DEBUG, "file_upload");
        GreenDaoManager.init();
        initApplicationComponent();
    }


    private void initLeakCanary() {
/*        if (BuildConfig.DEBUG) {
            refWatcher = LeakCanary.install(this);
        } else {*/
            refWatcher = installLeakCanary();
//        }
    }

    /**
     * release版本使用此方法
     */
    protected RefWatcher installLeakCanary() {
        return RefWatcher.DISABLED;
    }

    private void initActivityLifecycleLogs() {
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                KLog.v("=========", activity + "  onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                KLog.v("=========", activity + "  onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                KLog.v("=========", activity + "  onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                KLog.v("=========", activity + "  onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                KLog.v("=========", activity + "  onActivityStopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                KLog.v("=========", activity + "  onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                KLog.v("=========", activity + "  onActivityDestroyed");
            }
        });
    }

    private void initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
//                            .penaltyDialog() // 弹出违规提示对话框
                            .penaltyLog() // 在logcat中打印违规异常信息
                            .build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());
        }
    }

    private void initApplicationComponent() {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }


    public static ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }
}
