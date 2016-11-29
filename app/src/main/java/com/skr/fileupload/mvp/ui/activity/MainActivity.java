package com.skr.fileupload.mvp.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.AbsListView;

import com.skr.fileupload.fileupload.R;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.mvp.ui.adapter.DirectoryListAdapter;
import com.skr.fileupload.repository.network.ApiConstants;
import com.skr.fileupload.server.FileServer;
import com.socks.library.KLog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity";
    private final static String[] EXTENSIONS = {".png", ".jpg", ".mp3", ".mp4", ".avi", ".doc", ".pdf", ".txt", ".apk"};
    private FileServer fileServer = new FileServer(ApiConstants.PORT);

    public static boolean sIsScrolling;
    private String[] mRootPaths;

    @BindView(R.id.directory_list_rv)
    RecyclerView mDirectoryListRv;
    private DirectoryListAdapter mDirectoryListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, getSdPaths(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        initRecycleView();

        startServer();
    }

    private String getSdPaths() {
        String paths = "";
        String[] result = null;
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            try {
                result = (String[]) method.invoke(storageManager);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < result.length; i++) {
                KLog.d(LOG_TAG, "path----> " + result[i] + "\n");
                paths += "path----> " + result[i] + "\n";
            }
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<DirectoryFile> getSdLists() {
        List<DirectoryFile> list = new ArrayList<>();
        String[] result;
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
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

    private void initRecycleView() {
        mDirectoryListRv.setHasFixedSize(true);
        mDirectoryListRv.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mDirectoryListAdapter = new DirectoryListAdapter(getSdLists(), this);
        mDirectoryListRv.setAdapter(mDirectoryListAdapter);
        mDirectoryListRv.setNestedScrollingEnabled(false);
        mDirectoryListRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                sIsScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
            }
        });
    }

    private void startServer() {
        new Thread(() -> {
            try {
                fileServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 判断当前文件是否为特定格式的文件
     */
    @DebugLog
    private boolean isNeededFile(File file) {
        for (String suffix : EXTENSIONS) {
            if (file.getName().endsWith(suffix))
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryListAdapter.getCurrentPath() == null) {
            super.onBackPressed();
        } else {
            if (isRoot(mDirectoryListAdapter.getCurrentPath())) {
                mDirectoryListAdapter.openRootFolder(getSdLists());
            } else {
                mDirectoryListAdapter.openFolder(true);
            }
        }
    }

    private boolean isRoot(String path) {
        for (String rootPath : mRootPaths) {
            if (rootPath.equals(path)) {
                return true;
            }
        }
        return false;
    }
}
