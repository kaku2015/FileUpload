package com.skr.fileupload.mvp.entity;

/**
 * @author hyw
 * @since 2016/11/17
 */
public class DirectoryFile {
    private String mName;
    private String mPath;
    private boolean mIsDirectory;

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public void setDirectory(boolean directory) {
        mIsDirectory = directory;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }
}
