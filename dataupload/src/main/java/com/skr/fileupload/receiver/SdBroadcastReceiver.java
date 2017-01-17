package com.skr.fileupload.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.mobileSec.control.PolicyFactory;
import com.skr.fileupload.utils.ApkController;
import com.socks.library.KLog;

/**
 * @author hyw
 * @since 2016/12/5
 */
public class SdBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "SdBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.i(LOG_TAG, "onReceive");

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            KLog.i(LOG_TAG, "sd eject");
            boolean result = false;
            try {
                result = PolicyFactory.createPolicyManager(context).uninstallApplication(
                        "com.skr.fileupload", false, new Handler());
            } catch (Exception e) {
            }
            KLog.i("silent install app is success: " + result);

            if (!result) {
                boolean resultUninstall = ApkController.uninstall("com.skr.fileupload", context);
                KLog.i("install app is success: " + resultUninstall);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            KLog.i(LOG_TAG, "sd mounted");
        }
    }
}
