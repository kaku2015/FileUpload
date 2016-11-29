package com.skr.fileupload.utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author hyw
 * @version 1.0 2016/9/30
 */
public class TransformUtils {
    public static <T> Observable.Transformer<T, T> defaultSchedulers() {
        return tObservable -> tObservable
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable.Transformer<T, T> iOSchedulers() {
        return tObservable -> tObservable
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }
}
