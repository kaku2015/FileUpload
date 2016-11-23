package com.skr.fileupload.repository.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author hyw
 * @since 2016/11/22
 */
@Entity
public class UploadFileInfo {
    private String filePath;
    private String sourceId;
    @Generated(hash = 402957748)
    public UploadFileInfo(String filePath, String sourceId) {
        this.filePath = filePath;
        this.sourceId = sourceId;
    }
    @Generated(hash = 771965119)
    public UploadFileInfo() {
    }
    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getSourceId() {
        return this.sourceId;
    }
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
