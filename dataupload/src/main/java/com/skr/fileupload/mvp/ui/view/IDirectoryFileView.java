package com.skr.fileupload.mvp.ui.view;

import com.skr.fileupload.base.BaseView;
import com.skr.fileupload.mvp.entity.DirectoryFile;

import java.util.List;

/**
 * @author hyw
 * @since 2016/11/28
 */
public interface IDirectoryFileView extends BaseView {
    void setData(List<DirectoryFile> directoryFiles, String[] rootPaths);
}
