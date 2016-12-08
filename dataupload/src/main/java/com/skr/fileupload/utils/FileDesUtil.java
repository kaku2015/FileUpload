package com.skr.fileupload.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author hyw
 * @since 2016/12/8
 */
public class FileDesUtil {
    //秘钥算法
    private static final String KEY_ALGORITHM = "DES";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
    private static final byte[] KEY = {87, 116, 102, 101, 83, 120, 118, 68};//DES 秘钥长度必须是8 位或以上

    /**
     * 文件进行加密并保存加密后的文件到指定目录
     *
     * @param fromFile 要加密的文件 如c:/test/待加密文件.txt
     * @param toFile   加密后存放的文件 如c:/加密后文件.txt
     */
    public static void encrypt(String fromFile, String toFile) throws Exception {
        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY, KEY_ALGORITHM);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        InputStream is = new FileInputStream(fromFile);
        OutputStream out = new FileOutputStream(toFile);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();
    }

    /**
     * 文件进行解密并保存解密后的文件到指定目录
     *
     * @param fromFile 已加密的文件 如c:/加密后文件.txt
     * @param toFile   解密后存放的文件 如c:/ test/解密后文件.txt
     */
    public static void decrypt(String fromFile, String toFile) throws Exception {
        SecretKey secretKey = new SecretKeySpec(KEY, KEY_ALGORITHM);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        InputStream is = new FileInputStream(fromFile);
        OutputStream out = new FileOutputStream(toFile);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();
    }
}
