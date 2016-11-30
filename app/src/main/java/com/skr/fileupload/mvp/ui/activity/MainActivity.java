package com.skr.fileupload.mvp.ui.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import com.skr.fileupload.base.BaseActivity;
import com.skr.fileupload.fileupload.R;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.mvp.presenter.impl.DirectoryFilePresenterImpl;
import com.skr.fileupload.mvp.ui.adapter.DirectoryListAdapter;
import com.skr.fileupload.mvp.ui.view.IDirectoryFileView;
import com.skr.fileupload.repository.network.ApiConstants;
import com.skr.fileupload.server.FileServer;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import hugo.weaving.DebugLog;


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
    public void initViews() {
        mPresenter = mIDirectoryFilePresenter;
        mPresenter.attachView(this);

        mFabBtn.setOnClickListener(view -> Snackbar.make(view, getSdPaths(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        initRecycleView();

        startServer();
    }

    private void initRecycleView() {
        mDirectoryListRv.setHasFixedSize(true);
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

    private String getSdPaths() {
        String paths = null;
        for (String mRootPath : mRootPaths) {
            paths += "path----> " + mRootPath + "\n";
        }
        return paths;
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
