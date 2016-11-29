package com.skr.fileupload.mvp.ui.adapter;


import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skr.fileupload.base.BaseRecyclerViewAdapter;
import com.skr.fileupload.fileupload.R;
import com.skr.fileupload.mvp.entity.DirectoryFile;
import com.skr.fileupload.repository.db.GreenDaoManager;
import com.skr.fileupload.repository.network.ApiConstants;
import com.skr.fileupload.utils.StreamTool;
import com.skr.fileupload.utils.TransformUtils;
import com.socks.library.KLog;

import java.io.File;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class DirectoryListAdapter extends BaseRecyclerViewAdapter<DirectoryFile> {
    private static final String LOG_TAG = "DirectoryListAdapter";

    private static final int TYPE_FOLDER = 10001;
    private static final int TYPE_FILE = 10002;

    private static SparseBooleanArray mPaths = new SparseBooleanArray();

    private Activity mContext;
    private String mParentPath;

    public String getCurrentPath() {
        return mCurrentPath;
    }

    private String mCurrentPath;

    private boolean mIsUploading;

    public DirectoryListAdapter(List<DirectoryFile> list, Activity context) {
        super(list);
        mContext = context;
    }

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
        folderHolder.itemView.setOnClickListener(view -> Observable.timer(200, TimeUnit.MILLISECONDS)
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
        });

        fileHolder.mPauseBtn.setOnClickListener(view -> mPaths.put(position, true));
    }

    private void startUpload(final FileHolder fileHolder, int position, DirectoryFile item) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
/*                        if (MainActivity.sIsScrolling && !(viewHolder.mUploadPb.getProgress() == viewHolder.mUploadPb.getMax())) {
                return;
            }*/
                fileHolder.mUploadPb.setProgress(msg.getData().getInt("length"));
                float num = (float) fileHolder.mUploadPb.getProgress() / (float) fileHolder.mUploadPb.getMax();
                int result = (int) (num * 100);
                fileHolder.mProgressTv.setText(result + " %");
                if (fileHolder.mUploadPb.getProgress() == fileHolder.mUploadPb.getMax()) {
                    Toast.makeText(mContext, fileHolder.mFileNameTv.getText().toString() +
                            " 上传成功", Toast.LENGTH_SHORT).show();
                }
            }
        };

        String path = item.getPath();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(path);
            if (file.exists()) {
                fileHolder.mUploadPb.setMax((int) file.length());
                mPaths.put(position, false);
                uploadFile(file, position, fileHolder, handler);
            } else {
                KLog.e(LOG_TAG, "文件路径不存在： " + path);
                Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
            }
        } else {
            KLog.e(LOG_TAG, "sd卡错误");
            Toast.makeText(mContext, "sd卡错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(final File file, final int p, final FileHolder viewHolder, final Handler handler) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                String sourceid = GreenDaoManager.getInstance().getSourceIdByPath(file.getAbsolutePath());
                Socket socket = new Socket(ApiConstants.HOST, ApiConstants.PORT);
                OutputStream outStream = socket.getOutputStream();
                String head = "Content-Length=" + file.length() + ";filename=" + URLEncoder
                        .encode(file.getName(), "UTF-8")
                        + ";sourceid=" + (sourceid != null ? sourceid : "") + "\r\n";
                outStream.write(head.getBytes("utf-8"));

                PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());
                String response = StreamTool.readLine(inStream);
                String[] items = response.split(";");
                String responseSourceid = items[0].substring(items[0].indexOf("=") + 1);
                String position = items[1].substring(items[1].indexOf("=") + 1);
                if (sourceid == null) {//如果是第一次上传文件，在数据库中不存在该文件所绑定的资源id
                    GreenDaoManager.getInstance().saveUploadFileInfo(file.getAbsolutePath(), responseSourceid);
                }
                RandomAccessFile fileOutStream = new RandomAccessFile(file, "r");
                fileOutStream.seek(Integer.valueOf(position));
                byte[] buffer = new byte[1024];
                int len = -1;
                int length = Integer.valueOf(position);
                KLog.w(LOG_TAG, "position: " + length);
                while (!mPaths.get(p) &&
                        ((len = fileOutStream.read(buffer)) != -1)) {
                    outStream.write(buffer, 0, len);
                    length += len;//累加已经上传的数据长度
                    Message msg = new Message();
                    msg.getData().putInt("length", length);
                    handler.sendMessage(msg);

                }

                KLog.w(LOG_TAG, "累加已经上传的数据长度: " + length);

                if (length == file.length()) {
                    GreenDaoManager.getInstance().deleteUploadFileInfo(file.getAbsolutePath());
                }
                fileOutStream.close();
                outStream.close();
                inStream.close();
                socket.close();

            } catch (final Exception e) {
                KLog.e(LOG_TAG, e.toString());
                mContext.runOnUiThread(() -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void openRootFolder(List<DirectoryFile> directoryFiles) {
        mData.clear();
        mData.addAll(directoryFiles);
        notifyDataSetChanged();

        mParentPath = null;
        mCurrentPath = null;
    }

    class FileHolder extends RecyclerView.ViewHolder {
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

    class FolderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.folder_name_tv)
        TextView mFolderNameTv;

        FolderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
