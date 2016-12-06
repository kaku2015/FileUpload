package com.skr.fileupload.base;

import android.support.annotation.NonNull;

/**
 * @author hyw
 * @since 2016/11/24
 */
public interface BasePresenter {

    void onCreate();

    void attachView(@NonNull BaseView view);

    void onDestroy();

}
