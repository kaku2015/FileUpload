package com.skr.fileupload.repository.file;

import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author hyw
 * @since 2016/12/8
 */
public class DataRecordManager {
    public static String getSourceId(String filePath) {
        String position = null;
        FileInputStream fileInputStream = null;
        try {
            File logFile = new File(filePath + ".log");

            if (logFile.exists()) {
                Properties properties = new Properties();
                fileInputStream = new FileInputStream(logFile);
                properties.load(fileInputStream);
                position = properties.getProperty("soourceId");
            } else {
                return null;
            }
        } catch (IOException e) {
            KLog.e(e.toString());
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                KLog.e(e.toString());
            }
        }
        return position;
    }

    public static void saveSourceId(String sourceId, String filePath) {
        try {
            Properties properties = new Properties();
            properties.put("soourceId", sourceId);
            FileOutputStream logFile = new FileOutputStream(filePath + ".log");
            properties.store(logFile, null);
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
