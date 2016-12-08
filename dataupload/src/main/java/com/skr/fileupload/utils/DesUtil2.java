package com.skr.fileupload.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author hyw
 * @since 2016/12/6
 */
public class DesUtil2 {
    // 加密后的结果是字节数组，这些被加密后的字节在码表（例如UTF-8 码表）上找不到对应字符，会出现乱码，当乱码字符串再次转换为字节数组时，长度会变化，导致解密失败，所以转换后的数据是不安全的。

    // 使用Base64 对字节数组进行编码，任何字节都能映射成对应的Base64 字符，之后能恢复到字节数组，利于加密后数据的保存于传输，所以转换是安全的。同样，字节数组转换成16 进制字符串也是安全的。

    //秘钥算法
    private static final String KEY_ALGORITHM = "DES";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
    private static final String CIPHER_ALGORITHM_CBC = "DES/CBC/PKCS5Padding";
    //秘钥
    private static final String KEY = "12345678";//DES 秘钥长度必须是8 位或以上
    //private static final String KEY = "1234567890123456";//AES 秘钥长度必须是16 位

    public static void main(String args[]) {
/*        String data = "加密解密";
        System.out.println("加密数据：" + data);
        byte[] encryptData = encrypt(data);
        System.out.println("加密后的数据：" + new String(encryptData));
        System.out.println("加密后的数据经过base64解密：" + new String(Base64.getDecoder().decode(encryptData)));
        byte[] decryptData = decrypt(encryptData);
        System.out.println("解密后的数据：" + new String(decryptData));*/

    }

    public static byte[] encrypt(byte[] data) {
        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(data);
            return Base64.encode(result, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] data) {
        byte[] resultBase64 = Base64.decode(data,Base64.DEFAULT);

        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(resultBase64);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/*    public static byte[] encrypt(String data) {
        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

*//*
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey securekey = keyFactory.generateSecret(dks);*//*


        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encode(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] data) {
        byte[] resultBase64 = Base64.getDecoder().decode(data);

        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(resultBase64);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    public static byte[] encryptWithCBCMode(String data) {
        //AES、DES 在CBC 操作模式下需要iv 参数
        IvParameterSpec iv = new IvParameterSpec(KEY.getBytes());

        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(data.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptWithCBCMode(byte[] data) {
        //AES、DES 在CBC 操作模式下需要iv 参数
        IvParameterSpec iv = new IvParameterSpec(KEY.getBytes());

        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return cipher.doFinal(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}