package com.skr.fileupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.socks.library.KLog;

/**
 * @author hyw
 * @since 2016/12/5
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.i(LOG_TAG, "onReceive");

        String action = intent.getAction();

        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            KLog.d(LOG_TAG, "screen on");
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            KLog.d(LOG_TAG, "screen off");
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            KLog.d(LOG_TAG, "screen unlock");
        } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
            KLog.i(LOG_TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
        }
    }
}
