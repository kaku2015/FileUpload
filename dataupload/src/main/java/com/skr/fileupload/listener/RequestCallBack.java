package com.skr.fileupload.listener;

/**
 * @author hyw
 * @since 2016/11/24
 */
public interface RequestCallBack<T> {

    void beforeRequest();

    void success(T data);

    void onError(String errorMsg);
}
