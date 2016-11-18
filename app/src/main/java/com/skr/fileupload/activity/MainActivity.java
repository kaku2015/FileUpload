package com.skr.fileupload.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.skr.fileupload.adapter.DirectoryListAdapter;
import com.skr.fileupload.entity.DirectoryFile;
import com.skr.fileupload.fileupload.R;
import com.skr.fileupload.server.FileServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity";
    FileServer fileServer = new FileServer(7878);
    @BindView(R.id.directory_list_rv)
    RecyclerView mDirectoryListRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getSdPaths(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        initRecycleView();

        startServer();

        getSdPaths();
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
                Log.d(LOG_TAG, "path----> " + result[i] + "\n");
                paths += "path----> " + result[i] + "\n";
            }
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void initRecycleView() {
        mDirectoryListRv.setHasFixedSize(true);
        mDirectoryListRv.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mDirectoryListRv.setAdapter(new DirectoryListAdapter(getList(), this));
        mDirectoryListRv.setNestedScrollingEnabled(false);
    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<DirectoryFile> getList() {
        List<DirectoryFile> list = new ArrayList<>();
        String sDStateString = Environment.getExternalStorageState();
        if (sDStateString.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File SDFile = Environment.getExternalStorageDirectory();
                File sdPath = new File(SDFile.getAbsolutePath());
                File[] files = sdPath.listFiles();
                if (sdPath.listFiles().length > 0) {
                    for (File file : files) {
                        if (!file.isDirectory() && file.getName().endsWith(".apk")) {
                            DirectoryFile directoryFile = new DirectoryFile();
                            directoryFile.setName(file.getName());
                            directoryFile.setPath(file.getAbsolutePath());
                            list.add(directoryFile);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return list;
    }

}
