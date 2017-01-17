package com.skr.fileupload.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import com.socks.library.KLog;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

/**
 * @author hyw
 * @since 2017/1/13
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "NetworkConnectChangedReceiver";

    private static long mLastConnectTime = 0;
    private static final int SPACE_TIME = 500;

    public static boolean isRedundant() {
        long time = SystemClock.elapsedRealtime();
        if (time - mLastConnectTime <= SPACE_TIME) {
            return true;
        } else {
            mLastConnectTime = time;
            return false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.d(LOG_TAG, "网络状态改变");
        switch (intent.getAction()) {
            case NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) return;

                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    KLog.i(LOG_TAG, "wifi网络连接断开");
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    if (isRedundant()) return;
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前网络名称
                    KLog.i(LOG_TAG, "连接到wifi网络 " + wifiInfo.getSSID());

                }
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    KLog.i(LOG_TAG, "系统关闭wifi");
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    KLog.i(LOG_TAG, "系统开启wifi");
                }
                break;

        }

    }
}