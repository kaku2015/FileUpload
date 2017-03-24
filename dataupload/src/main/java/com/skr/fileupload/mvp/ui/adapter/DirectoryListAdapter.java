package com.skr.fileupload.mvp.ui.adapter;


import android.app.Activity;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skr.fileupload.App;
import com.skr.fileupload.R;
import com.skr.fileupload.base.BaseRecyclerViewAdapter;
import com.skr.fileupload.common.Constants;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.mvp.presenter.impl.DirectoryFilePresenterImpl;
import com.skr.fileupload.receiver.NetworkConnectChangedReceiver;
import com.skr.fileupload.repository.file.DataRecordManager;
import com.skr.fileupload.repository.network.ApiConstants;
import com.skr.fileupload.utils.DesUtil;
import com.skr.fileupload.utils.FileDesUtil;
import com.skr.fileupload.utils.NetworkUtils;
import com.skr.fileupload.utils.StreamTool;
import com.skr.fileupload.utils.ToastUtil;
import com.skr.fileupload.utils.TransformUtils;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_AGREEMENT;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_KET_PASSWORD;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_KEY_KEYSTORE;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_KEY_MANAGER;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_TRUST_KEYSTORE;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_TRUST_MANAGER;
import static com.skr.fileupload.repository.network.ApiConstants.CLIENT_TRUST_PASSWORD;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class DirectoryListAdapter extends BaseRecyclerViewAdapter<DirectoryFile> {
    private static final String LOG_TAG = "DirectoryListAdapter";

    private static final int TYPE_FOLDER = 10001;
    private static final int TYPE_FILE = 10002;

    private static SparseBooleanArray mPaths = new SparseBooleanArray();

    private String mParentPath;

    public String getCurrentPath() {
        return mCurrentPath;
    }

    private String mCurrentPath;

    @Inject
    Activity mContext;

    @Inject
    DirectoryListAdapter() {
        super(null);
    }

    @Inject
    DirectoryFilePresenterImpl mDirectoryFilePresenter;

    @Override
    public int getItemViewType(int position) {
        DirectoryFile item = mData.get(position);
        if (item.isDirectory()) {
            return TYPE_FOLDER;
        } else {
            return TYPE_FILE;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_FOLDER:
                view = getView(parent, R.layout.item_directory_folder);
                return new FolderHolder(view);
            case TYPE_FILE:
                view = getView(parent, R.layout.item_directory_file);
                return new FileHolder(view);
            default:
                throw new RuntimeException("there is no type that matches the type " +
                        viewType + " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final DirectoryFile item = mData.get(position);

        if (holder instanceof FolderHolder) {
            setFolderItem((FolderHolder) holder, item);
        } else if (holder instanceof FileHolder) {
            setFileItem((FileHolder) holder, position, item);
        }
    }

    private void setFolderItem(FolderHolder folderHolder, DirectoryFile item) {
        folderHolder.mFolderNameTv.setText(item.getName());
        folderHolder.itemView.setOnClickListener(view ->
                Observable.timer(200, TimeUnit.MILLISECONDS)
                        .compose(TransformUtils.defaultSchedulers())
                        .subscribe(aLong -> {
                            mCurrentPath = item.getPath();
                            mParentPath = new File(mCurrentPath).getParent();
                            openFolder(false);
                        }));
    }

    public void openFolder(boolean isOpenParentFolder) {
        mData.clear();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File sdPath = new File(isOpenParentFolder ? mParentPath : mCurrentPath);
                File[] files = sdPath.listFiles();
                if (sdPath.listFiles().length > 0) {
                    for (File file : files) {
                        DirectoryFile directoryFile = new DirectoryFile();
                        directoryFile.setName(file.getName());
                        directoryFile.setPath(file.getAbsolutePath());
                        directoryFile.setDirectory(file.isDirectory());
                        mData.add(directoryFile);
                    }
                }
            } catch (Exception e) {
                KLog.e(e.toString());
            }
        }

        notifyDataSetChanged();

        if (isOpenParentFolder) {
            mParentPath = new File(mParentPath).getParent();
            mCurrentPath = new File(mCurrentPath).getParent();
        }
    }

    private void setFileItem(FileHolder fileHolder, int position, DirectoryFile item) {
        fileHolder.mFileNameTv.setText(item.getName());

        fileHolder.mUploadBtn.setOnClickListener(view -> {
            startUpload(fileHolder, position, item);


//            // wifi可用
//            if (NetworkUtils.isWifiAvailable(mContext)) {
//                KLog.i(LOG_TAG, "wifi can use");
//                // wifi已打开
//                if (NetworkUtils.getWifiEnabled(mContext)) {
//                    KLog.i(LOG_TAG, "wifi is Opened");
//                    startUpload(fileHolder, position, item);
//                } else {
//                    KLog.i(LOG_TAG, "wifi is unOpened");
//                    openWifi();
//                }
//            } else {
//                KLog.i(LOG_TAG, "wifi can not use");
//                openWifi();
//            }
        });

        fileHolder.mPauseBtn.setOnClickListener(view -> mPaths.put(position, true));
    }

    private void openWifi() {
        NetworkUtils.setWifiEnabled(mContext, true);
        ToastUtil.showShortToast("wifi准备中...");
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(new NetworkConnectChangedReceiver(), filter);
    }


    private void startUpload(final FileHolder fileHolder, int position, DirectoryFile item) {
//        NetworkUtils.setWifiEnabled(mContext, true);

        String path = item.getPath();
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        File file = new File(path);
        if (file.exists()) {
            fileHolder.mUploadPb.setMax((int) file.length());
            mPaths.put(position, false);
            uploadFile(file, position, new UploadProgressHandler(fileHolder));
        } else {
            KLog.e(LOG_TAG, "file not found： " + path);
//                Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
        }
//        } else {
//            KLog.e(LOG_TAG, "sd card error");
////            Toast.makeText(mContext, "sd卡错误", Toast.LENGTH_SHORT).show();
//        }
    }

    private static class UploadProgressHandler extends Handler {
        private FileHolder mFileHolder;

        UploadProgressHandler(FileHolder fileHolder) {
            mFileHolder = fileHolder;
        }

        @Override
        public void handleMessage(Message msg) {
            mFileHolder.mUploadPb.setProgress(msg.getData().getInt("length"));
            String uploadedProgress = getUploadedProgress();
            mFileHolder.mProgressTv.setText(uploadedProgress);
            if (isUploadCompleted()) {
                Toast.makeText(App.getAppContext(), mFileHolder.mFileNameTv.getText().toString() +
                        "上传成功", Toast.LENGTH_SHORT).show();
            }
        }

        private String getUploadedProgress() {
            int UploadPercent = (int) (((float) mFileHolder.mUploadPb.getProgress() / (float) mFileHolder.mUploadPb.getMax())
                    * 100);
            return UploadPercent + "%";
        }

        private boolean isUploadCompleted() {
            return mFileHolder.mUploadPb.getProgress() == mFileHolder.mUploadPb.getMax();
        }
    }

    private void uploadFile(final File sourceFile, final int p, final Handler handler) {
        new Thread(() -> {
            OutputStream outStream = null;
            PushbackInputStream inStream = null;
            RandomAccessFile fileOutStream = null;
            Socket socket = init();
            try {
//                File encryptedFile = encryptFile(sourceFile);

//                socket = new Socket(ApiConstants.HOST, ApiConstants.PORT);
                outStream = socket.getOutputStream();

//                String sourceId = GreenDaoManager.getInstance().getSourceIdByPath(encryptedFile.getAbsolutePath());
                String sourceId = DataRecordManager.getSourceId(sourceFile.getAbsolutePath());
                outStream.write(getHeadBytes(sourceFile, sourceId));

                inStream = new PushbackInputStream(socket.getInputStream());
                String response = StreamTool.readLine(inStream);
                KLog.i("head from server: " + response);
                String[] items = response.split(";");
                String responseSourceId = items[0].substring(items[0].indexOf("=") + 1);
                String position = items[1].substring(items[1].indexOf("=") + 1);
                if (sourceId == null) {//如果是第一次上传文件，在数据库中不存在该文件所绑定的资源id
//                    GreenDaoManager.getInstance().saveUploadFileInfo(encryptedFile.getAbsolutePath(), responseSourceId);
                    DataRecordManager.saveSourceId(responseSourceId, sourceFile.getAbsolutePath());
                }

                fileOutStream = new RandomAccessFile(sourceFile, "r");
                fileOutStream.seek(Integer.valueOf(position));
                byte[] buffer = new byte[1024];
                int len = -1;
                int length = Integer.valueOf(position);
                KLog.i(LOG_TAG, "position: " + length);
                int uploadCount = 0;
                int fileLength = (int) sourceFile.length();
                while (isUploading(p) &&
                        ((len = fileOutStream.read(buffer)) != -1)) {
                    outStream.write(buffer, 0, len);
                    length += len;//累加已经上传的数据长度

                    if (isUploadPercentInteger(length, uploadCount, fileLength)) {
                        uploadCount += 1;
                        Message msg = new Message();
                        msg.getData().putInt("length", length);
                        handler.sendMessage(msg);
                    }
                }
                KLog.i(LOG_TAG, "uploaded file length: " + length);

                if (length == sourceFile.length()) {
                    boolean result = sourceFile.delete();
                    KLog.i(LOG_TAG, "delete encrypt file is success: " + result);
//                    GreenDaoManager.getInstance().deleteUploadFileInfo(encryptedFile.getAbsolutePath());
                }
            } catch (Exception e) {
                KLog.e(LOG_TAG, "uploadFile error: " + e.toString());
//                mContext.runOnUiThread(() -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (fileOutStream != null) {
                        fileOutStream.close();
                    }
                } catch (IOException e) {
                    KLog.e(LOG_TAG, e.toString());
                }
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                } catch (IOException e) {
                    KLog.e(LOG_TAG, e.toString());
                }
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (IOException e) {
                    KLog.e(LOG_TAG, e.toString());
                }
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    KLog.e(LOG_TAG, e.toString());
                }
            }
        }).start();
    }

    private SSLSocket init() {
        SSLSocket socket = null;
        try {
            //取得SSL的SSLContext实例
            SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
            //取得KeyManagerFactory和TrustManagerFactory的X509密钥管理器实例
            KeyManagerFactory keyManager = KeyManagerFactory.getInstance(CLIENT_KEY_MANAGER);
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance(CLIENT_TRUST_MANAGER);
            //取得BKS密库实例
            KeyStore kks = KeyStore.getInstance(CLIENT_KEY_KEYSTORE);
            KeyStore tks = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
            //加客户端载证书和私钥,通过读取资源文件的方式读取密钥和信任证书
            kks.load(App.getAppContext()
                    .getResources()
                    .openRawResource(R.raw.kclient), CLIENT_KET_PASSWORD.toCharArray());
            tks.load(App.getAppContext()
                    .getResources()
                    .openRawResource(R.raw.tclient), CLIENT_TRUST_PASSWORD.toCharArray());
            //初始化密钥管理器
            keyManager.init(kks, CLIENT_KET_PASSWORD.toCharArray());
            trustManager.init(tks);
            //初始化SSLContext
            sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
            KLog.i(LOG_TAG, "start ssl connecting");
            //生成SSLSocket
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(ApiConstants.HOST, ApiConstants.PORT);
            KLog.i(LOG_TAG, "ssl connected");
        } catch (Exception e) {
            KLog.e(LOG_TAG, e.toString());
        }
        return socket;
    }

    @NonNull
    private File encryptFile(File sourceFile) throws Exception {
        String targetRootPath = mContext.getFilesDir() + "/encrypt_file/";
//        String targetRootPath = StorageUtils.getSdPath() + "/encrypt_file/";
        String fileName = DesUtil.encrypt(sourceFile.getName());
        String targetPath = targetRootPath + fileName;
        File file = new File(targetPath);
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }

        FileDesUtil.encrypt(sourceFile.getAbsolutePath(), targetPath);
        return file;
    }

    private boolean isUploading(int p) {
        return !mPaths.get(p);
    }

    private boolean isUploadPercentInteger(int length, int uploadCount, int fileLength) {
        return (length * 100 / fileLength) > uploadCount;
    }

    @NonNull
    private byte[] getHeadBytes(File file, String sourceId) throws UnsupportedEncodingException {
        String head = "Content-Length=" + file.length() + ";fileName=" + URLEncoder
                .encode(file.getName(), Constants.UTF_8)
                + ";sourceId=" + (sourceId != null ? sourceId : "") + "\r\n";
        KLog.i("head to server: " + head);

        return head.getBytes();
    }

    public void openRootFolder(List<DirectoryFile> directoryFiles) {
        if (mData != null) {
            mData.clear();
            mData.addAll(directoryFiles);
        } else {
            mData = directoryFiles;
        }
        notifyDataSetChanged();

        mParentPath = null;
        mCurrentPath = null;
    }

    class FileHolder extends ViewHolder {
        @BindView(R.id.file_name_tv)
        TextView mFileNameTv;
        @BindView(R.id.upload_pb)
        ProgressBar mUploadPb;
        @BindView(R.id.progress_tv)
        TextView mProgressTv;
        @BindView(R.id.upload_btn)
        Button mUploadBtn;
        @BindView(R.id.pause_btn)
        Button mPauseBtn;

        FileHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class FolderHolder extends ViewHolder {
        @BindView(R.id.folder_name_tv)
        TextView mFolderNameTv;

        FolderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
