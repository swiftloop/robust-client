package com.github.robust.client;

import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;


/**
 * @author sorata 2021/2/2 10:27 上午
 */
public final class RobustSdkManager {

    private final AppMetaData appMetaData;
    private PatchInfo patchInfo = null;

    public RobustSdkManager() {
        appMetaData = new AppMetaData();
    }

    public static void setPatchInfo(PatchInfo patchInfo){
        RobustSdkManager instance = getInstance();
        instance.patchInfo = patchInfo;
    }

    public static PatchInfo getPatchInfo(){
        RobustSdkManager instance = getInstance();
        return instance.patchInfo;
    }


    public AppMetaData getAppMetaData() {
        return appMetaData;
    }



    public static void init(String appId, String url, String reportUrl, String accessKey) {
        RobustSdkManager manager = getInstance();
        manager.getAppMetaData().setUrl(url);
        manager.getAppMetaData().setReportUrl(reportUrl);
        manager.getAppMetaData().setAccessKey(accessKey);
        manager.getAppMetaData().setAppId(appId);
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();
            manager.getAppMetaData().setPublicKey(keyPair.getPublic());
            manager.getAppMetaData().setPrivateKey(keyPair.getPrivate());
        } catch (Exception e) {
            Log.e("RobustSdkManager", "无法生成RSA的密钥，那么Robust将不执行任何操作");
            e.printStackTrace();
        }
    }


    public static RobustSdkManager getInstance() {
        return Holder.MANAGER;
    }


    private static class Holder {
        private static final RobustSdkManager MANAGER = new RobustSdkManager();
    }


}
