package com.skr.fileupload.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.skr.fileupload.App;
import com.skr.fileupload.R;
import com.skr.fileupload.di.component.ActivityComponent;
import com.skr.fileupload.di.component.DaggerActivityComponent;
import com.skr.fileupload.di.module.ActivityModule;
import com.skr.fileupload.utils.MyUtils;
import com.socks.library.KLog;
import com.squareup.leakcanary.RefWatcher;

import butterknife.ButterKnife;
import rx.Subscription;

/**
 * @author hyw
 * @since 2016/11/24
 */
public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity {
    protected ActivityComponent mActivityComponent;

    public ActivityComponent getActivityComponent() {
        return mActivityComponent;
    }

    protected T mPresenter;

    /**
     * Provide your layout ID
     */
    public abstract int getLayoutId();

    /**
     * Use case:
     * <p>
     * mActivityComponent.inject(this);
     */
    public abstract void initInjector();

    /**
     * Use case:
     * <p>
     * mPresenter = xxxPresenter;
     * <p>
     * mPresenter.attachView(this);
     */
    public abstract void initPresenter();

    /**
     * Initialize your layout components here
     */
    public abstract void initViews();

    protected Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KLog.i(getClass().getSimpleName());
        initActivityComponent();
        setContentView(getLayoutId());
        initInjector();
        ButterKnife.bind(this);
        initToolBar();
        initPresenter();
        initViews();
        if (mPresenter != null) {
            mPresenter.onCreate();
        }
    }

    private void initActivityComponent() {
        mActivityComponent = DaggerActivityComponent.builder()
                .applicationComponent(App.getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(this);
        refWatcher.watch(this);

        if (mPresenter != null) {
            mPresenter.onDestroy();
        }

        MyUtils.cancelSubscription(mSubscription);
        MyUtils.fixInputMethodManagerLeak(this);
    }
}
