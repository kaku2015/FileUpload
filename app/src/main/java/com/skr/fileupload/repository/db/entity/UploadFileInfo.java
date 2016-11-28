package com.skr.fileupload.repository.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * @author hyw
 * @since 2016/11/22
 */
@Entity
public class UploadFileInfo {
    @Id
    private Long id;
    private String filePath;
    private String sourceId;
    @Generated(hash = 467298615)
    public UploadFileInfo(Long id, String filePath, String sourceId) {
        this.id = id;
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
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
