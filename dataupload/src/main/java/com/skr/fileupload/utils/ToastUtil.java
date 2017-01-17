package com.skr.fileupload.utils;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skr.fileupload.App;
import com.skr.fileupload.R;
import com.socks.library.KLog;

/**
 * @author hyw
 * @since 2016/12/5
 */
public class ToastUtil {

    /**
     * Log tag ：ToastUtil
     */
    private static final String LOG_TAG = "ToastUtil";

    private static Toast sToast;

    private static final Handler sHandler = new Handler();

    private static final Runnable sRun = new Runnable() {
        public void run() {
            try {
                sToast.cancel();
                // toast隐藏后，将其置为null
                sToast = null;
            } catch (Exception e) {
                KLog.e(LOG_TAG, "run方法出现错误：" + e.toString());
            }
        }
    };

    /**
     * 短时间显示Toast
     *
     * @param context context
     * @param msg     需要显示的信息
     */
    public static void showShortToast(Context context, String msg) {
        showToast(context.getApplicationContext(), msg, Toast.LENGTH_SHORT, 1500);

    }

    public static void showShortToast(int msgId) {
        showToast(App.getAppContext(), App.getAppContext().getString(msgId), Toast.LENGTH_SHORT, 1500);

    }

    public static void showShortToast(String msg) {
        showToast(App.getAppContext(), msg, Toast.LENGTH_SHORT, 1500);

    }

    /**
     * 长时间显示Toast
     *
     * @param context context
     * @param msg     需要显示的信息
     */
    public static void showLongToast(Context context, String msg) {
        showToast(context.getApplicationContext(), msg, Toast.LENGTH_LONG, 3000);

    }

    /**
     * 显示toast
     *
     * @param appContext  appContext
     * @param msg         需要显示的信息
     * @param duration    显示时间
     * @param delayMillis 延迟关闭时间
     */
    private static void showToast(Context appContext, String msg, int duration, int delayMillis) {
        LayoutInflater inflater = (LayoutInflater) appContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast, new LinearLayout(appContext),
                false);
        TextView tv = (TextView) view.findViewById(R.id.toast_tv);
        tv.setText(msg);
        // 删除指定的Runnable对象，使线程对象停止运行。
        sHandler.removeCallbacks(sRun);
        // 只有mToast==null时才重新创建，否则只需更改提示文字
        if (sToast == null) {
            sToast = new Toast(appContext);
            sToast.setDuration(duration);
            int density = (int) appContext.getResources().getDisplayMetrics().density;
            sToast.setGravity(Gravity.BOTTOM, 0, 80 * density);
            sToast.setView(view);
        } else {
            sToast.setView(view);
        }
        // 延迟1.5秒隐藏toast
        sHandler.postDelayed(sRun, delayMillis);
        sToast.show();
    }
}
