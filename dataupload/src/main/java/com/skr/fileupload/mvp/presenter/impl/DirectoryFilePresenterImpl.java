package com.skr.fileupload.mvp.presenter.impl;

import android.content.Context;
import android.os.storage.StorageManager;

import com.skr.fileupload.base.BasePresenterImpl;
import com.skr.fileupload.di.scope.ContextLife;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.mvp.presenter.IDirectoryFilePresenter;
import com.skr.fileupload.mvp.ui.view.IDirectoryFileView;
import com.socks.library.KLog;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author hyw
 * @since 2016/11/30
 */
public class DirectoryFilePresenterImpl extends BasePresenterImpl<IDirectoryFileView, List<DirectoryFile>>
        implements IDirectoryFilePresenter {
    private static final String LOG_TAG = "DirectoryFilePresenterImpl";

    private String[] mRootPaths;

    @Inject
    @ContextLife("Application")
    Context mAppContext;

    @Inject
    DirectoryFilePresenterImpl() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mView.setData(getSdLists(), mRootPaths);
    }

    private List<DirectoryFile> getSdLists() {
        List<DirectoryFile> list = new ArrayList<>();
        String[] result;
        StorageManager storageManager = (StorageManager) mAppContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            result = (String[]) method.invoke(storageManager);
            mRootPaths = new String[result.length];

            for (int i = 0; i < result.length; i++) {
                KLog.d(LOG_TAG, "path----> " + result[i] + "\n");

                File file = new File(result[i]);
                DirectoryFile directoryFile = new DirectoryFile();
                directoryFile.setName(file.getName());
                directoryFile.setPath(file.getAbsolutePath());
                directoryFile.setDirectory(file.isDirectory());
                list.add(directoryFile);
                mRootPaths[i] = file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
}
