package com.skr.fileupload.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import com.skr.fileupload.App;

import java.lang.reflect.Method;

/**
 * @author hyw
 * @since 2016/12/8
 */
public class StorageUtils {

    public static String getSdPath() {
        String[] paths = getStoragePaths();
        return paths != null ? paths[1] : "";
    }

    private static String[] getStoragePaths() {
        String[] result;
        StorageManager storageManager = (StorageManager) App.getAppContext().getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            result = (String[]) method.invoke(storageManager);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
