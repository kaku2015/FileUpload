package com.skr.fileupload.repository.network;

/**
 * @author hyw
 * @since 2016/11/22
 */
public class ApiConstants {
    public static final String HOST = "192.168.1.106";
    //    public static final String HOST = "192.168.0.132";
//    public static final String HOST = "115.29.49.67";
//    public static final String HOST = "192.168.0.106";
    public static final int PORT = 7882;

    public static final String CLIENT_KET_PASSWORD = "123456";//私钥密码
    public static final String CLIENT_TRUST_PASSWORD = "123456";//信任证书密码
    public static final String CLIENT_AGREEMENT = "TLS";//使用协议
    public static final String CLIENT_KEY_MANAGER = "X509";//密钥管理器
    public static final String CLIENT_TRUST_MANAGER = "X509";//
    public static final String CLIENT_KEY_KEYSTORE = "BKS";//密库，这里用的是BouncyCastle密库
    public static final String CLIENT_TRUST_KEYSTORE = "BKS";//
}

