package com.skr.fileupload.repository.db;

import com.skr.fileupload.App;
import com.skr.fileupload.fileupload.BuildConfig;
import com.skr.fileupload.repository.db.entity.DaoMaster;
import com.skr.fileupload.repository.db.entity.DaoSession;
import com.skr.fileupload.repository.db.entity.UploadFileInfo;
import com.skr.fileupload.repository.db.entity.UploadFileInfoDao;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * @author hyw
 * @since 2016/11/23
 */
public class GreenDaoManager {
    private static GreenDaoManager sGreenDaoManager;
    private UploadFileInfoDao mUploadFileInfoDao;

    private GreenDaoManager() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(App.getAppContext(), DBConstants.DB_NAME, null);
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        DaoSession daoSession = mDaoMaster.newSession();
        mUploadFileInfoDao = daoSession.getUploadFileInfoDao();
        // 在 QueryBuilder 类中内置两个 Flag 用于方便输出执行的 SQL 语句与传递参数的值
        QueryBuilder.LOG_SQL = BuildConfig.DEBUG;
        QueryBuilder.LOG_VALUES = BuildConfig.DEBUG;
    }

    public static void init() {
        getInstance();
    }

    public static GreenDaoManager getInstance() {
        if (sGreenDaoManager == null) {
            sGreenDaoManager = new GreenDaoManager();
        }
        return sGreenDaoManager;
    }

    public String getSourceIdByPath(String filePath) {
        UploadFileInfo uploadFileInfo = mUploadFileInfoDao.queryBuilder()
                .where(UploadFileInfoDao.Properties.FilePath.eq(filePath))
                .unique();
        if (uploadFileInfo != null) {
            return uploadFileInfo.getSourceId();
        }
        return null;
    }

    public long getId(String filePath) {
        UploadFileInfo uploadFileInfo = mUploadFileInfoDao.queryBuilder()
                .where(UploadFileInfoDao.Properties.FilePath.eq(filePath))
                .unique();
        if (uploadFileInfo != null) {
            return uploadFileInfo.getId();
        }
        return -1;
    }

    public void saveUploadFileInfo(String filePath, String sourceId) {
        UploadFileInfo uploadFileInfo = new UploadFileInfo(null, filePath, sourceId);
        mUploadFileInfoDao.insert(uploadFileInfo);
    }

    public void deleteUploadFileInfo(String filePath) {
        mUploadFileInfoDao.deleteByKey(getId(filePath));
    }
}
