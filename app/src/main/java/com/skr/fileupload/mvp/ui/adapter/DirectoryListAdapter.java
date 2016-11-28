package com.skr.fileupload.mvp.ui.adapter;


import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
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
import com.socks.library.KLog;

import java.io.File;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class DirectoryListAdapter extends BaseRecyclerViewAdapter<DirectoryFile> {
    private static final String LOG_TAG = "DirectoryListAdapter";

    private static SparseBooleanArray mPaths = new SparseBooleanArray();

    private Activity mContext;

    public DirectoryListAdapter(List<DirectoryFile> list, Activity context) {
        super(list);
        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_directory_file, parent, false);
        return new DirectoryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final DirectoryFile item = mData.get(position);

        final DirectoryListViewHolder viewHolder = (DirectoryListViewHolder) holder;
        viewHolder.mFileNameTv.setText(item.getName());

        viewHolder.mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
/*                        if (MainActivity.sIsScrolling && !(viewHolder.mUploadPb.getProgress() == viewHolder.mUploadPb.getMax())) {
                            return;
                        }*/
                        viewHolder.mUploadPb.setProgress(msg.getData().getInt("length"));
                        float num = (float) viewHolder.mUploadPb.getProgress() / (float) viewHolder.mUploadPb.getMax();
                        int result = (int) (num * 100);
                        viewHolder.mProgressTv.setText(result + " %");
                        if (viewHolder.mUploadPb.getProgress() == viewHolder.mUploadPb.getMax()) {
                            Toast.makeText(mContext, viewHolder.mFileNameTv.getText().toString() +
                                    " 上传成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                String path = item.getPath();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(path);
                    if (file.exists()) {
                        viewHolder.mUploadPb.setMax((int) file.length());
                        mPaths.put(position, false);
                        uploadFile(file, position, viewHolder, handler);
                    } else {
                        KLog.e(LOG_TAG, "文件路径不存在： " + path);
                        Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    KLog.e(LOG_TAG, "sd卡错误");
                    Toast.makeText(mContext, "sd卡错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPaths.put(position, true);
            }
        });
    }

    private void uploadFile(final File file, final int p, final DirectoryListViewHolder viewHolder, final Handler handler) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                    String sourceid = GreenDaoManager.getInstance().getSourceIdByPath(file.getAbsolutePath());
                    Socket socket = new Socket(ApiConstants.HOST, ApiConstants.PORT);
                    OutputStream outStream = socket.getOutputStream();
                    String head = "Content-Length=" + file.length() + ";filename=" + file.getName()
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
            }
        }).start();
    }

    class DirectoryListViewHolder extends RecyclerView.ViewHolder {
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

        DirectoryListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
