package com.skr.fileupload;

import android.app.Application;
import android.content.Context;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class App extends Application {

    public static Context getAppContext() {
        return sAppContext;
    }

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
    }
}
