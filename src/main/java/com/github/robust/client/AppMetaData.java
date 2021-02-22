package com.github.robust.client;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author sorata 2021/2/1 3:06 下午
 */
public class AppMetaData {

    /**
     * 应用id
     */
    private String appId;

    /*
    RSA公钥 用于发送给服务端 服务端加密AES密钥
     */
    private PublicKey publicKey;

    /*
    用于解密服务端数据
     */
    private PrivateKey privateKey;

    /**
     * 签名key
     */
    private String accessKey;

    private String url;

    private String reportUrl;


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
}
