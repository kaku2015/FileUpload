package com.skr.fileupload.mvp.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import com.skr.fileupload.R;
import com.skr.fileupload.base.BaseActivity;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.mvp.presenter.impl.DirectoryFilePresenterImpl;
import com.skr.fileupload.mvp.ui.adapter.DirectoryListAdapter;
import com.skr.fileupload.mvp.ui.view.IDirectoryFileView;
import com.skr.fileupload.repository.network.ApiConstants;
import com.skr.fileupload.server.FileServer;
import com.skr.fileupload.wigets.DividerItemDecoration;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements IDirectoryFileView {
    public static final String LOG_TAG = "MainActivity";
    private final static String[] EXTENSIONS = {".png", ".jpg", ".mp3", ".mp4", ".avi", ".doc", ".pdf", ".txt", ".apk"};
    private FileServer fileServer = new FileServer(ApiConstants.PORT);

    public static boolean sIsScrolling;
    private String[] mRootPaths;
    private List<DirectoryFile> mDirectoryFiles = new ArrayList<>();

    @BindView(R.id.directory_list_rv)
    RecyclerView mDirectoryListRv;
    @BindView(R.id.fab)
    FloatingActionButton mFabBtn;

    @Inject
    DirectoryFilePresenterImpl mIDirectoryFilePresenter;
    @Inject
    DirectoryListAdapter mDirectoryListAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
    }

    @Override
    public void initPresenter() {
        mPresenter = mIDirectoryFilePresenter;
        mPresenter.attachView(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initViews() {
        mFabBtn.setOnClickListener(view -> Snackbar.make(view, getSdPaths(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        initRecycleView();

        startServer();
    }

    private String getSdPaths() {
        String paths = null;
        for (String mRootPath : mRootPaths) {
            paths += "path----> " + mRootPath + "\n";
        }
        return paths;
    }

    private void initRecycleView() {
        mDirectoryListRv.setHasFixedSize(true);
        mDirectoryListRv.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        mDirectoryListRv.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
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
                KLog.e(LOG_TAG, "startServer :" + e);
            }
        }).start();
    }

/*    */

    /**
     * 判断当前文件是否为特定格式的文件
     *//*
    @DebugLog
    private boolean isNeededFile(File file) {
        for (String suffix : EXTENSIONS) {
            if (file.getName().endsWith(suffix))
                return true;
        }
        return false;
    }*/
    @Override
    public void onBackPressed() {
        // 当当前已经是根目录，退出
        if (mDirectoryListAdapter.getCurrentPath() == null) {
            super.onBackPressed();
        } else {
            // 当上一级是根目录，重新显示根目录文件列表
            if (isRoot(mDirectoryListAdapter.getCurrentPath())) {
                mDirectoryListAdapter.openRootFolder(mDirectoryFiles);
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

    @Override
    public void setData(List<DirectoryFile> directoryFiles, String[] rootPaths) {
        mDirectoryFiles.addAll(directoryFiles);
        mRootPaths = rootPaths;

        mDirectoryListAdapter.openRootFolder(directoryFiles);
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showMsg(String message) {

    }
}
