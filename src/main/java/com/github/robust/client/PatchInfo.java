package com.github.robust.client;

/**
 * @author sorata 2021/2/1 6:18 下午
 */
public class PatchInfo {

    private String downloadUrl;

    private String md5;

    private String patchId;

    private String secret;

    private String norIV;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPatchId() {
        return patchId;
    }

    public void setPatchId(String patchId) {
        this.patchId = patchId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getNorIV() {
        return norIV;
    }

    public void setNorIV(String norIV) {
        this.norIV = norIV;
    }


    @Override
    public String toString() {
        return "PatchInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", md5='" + md5 + '\'' +
                ", patchId='" + patchId + '\'' +
                ", secret='" + secret + '\'' +
                ", norIV='" + norIV + '\'' +
                '}';
    }
}
