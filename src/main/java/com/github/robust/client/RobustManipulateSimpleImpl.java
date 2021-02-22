package com.github.robust.client;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import com.github.robust.client.utils.DigestUtil;
import com.github.robust.client.utils.FileUtil;
import com.github.robust.client.utils.NetUtil;
import com.github.robust.client.utils.SignUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.meituan.robust.Patch;
import com.meituan.robust.PatchManipulate;
import com.meituan.robust.RobustApkHashUtils;
import com.meituan.robust.RobustCallBack;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author sorata 2021/2/1 3:02 下午
 */
public class RobustManipulateSimpleImpl extends PatchManipulate {

    private static final String TAG = "robust-Manipulate";

    private final AppMetaData appMetaData;
    private final RobustCallBack robustCallBack;
    private final String patchesInfoImplClassFullName;
    public RobustManipulateSimpleImpl(AppMetaData appMetaData,RobustCallBack robustCallBack,String fullName) {
        this.appMetaData = appMetaData;
        this.robustCallBack = robustCallBack;
        this.patchesInfoImplClassFullName = fullName;
    }


    /**
     * 检索补丁
     * <p>
     * 这里检索的是单个补丁 当打了补丁之后，又出现一个问题，那么就需要打第二次补丁，这个时候打包出来的补丁应该是包含第一次的补丁，
     * 和第二次的补丁，也就是后面的补丁包应该是包含前几次的补丁的 那么服务器针对一个apk——hash应该是有且只有最新的一个补丁包
     *
     * @param context 上下文
     * @return 补丁列表
     */
    @Override
    protected List<Patch> fetchPatchList(Context context) {
        if (appMetaData == null) {
            return new ArrayList<>();
        }
        if (appMetaData.getPrivateKey() == null || appMetaData.getPublicKey() == null) {
            return new ArrayList<>();
        }
        String apkHash = RobustApkHashUtils.readRobustApkHash(context);
        if (apkHash == null || apkHash.isEmpty()) {
            Log.e(TAG, "无法获取APK-HASH，可能是Robust集成出现问题，请检查");
            return new ArrayList<>();
        }
        Patch patch = null;
        Log.i("robust-Manipulate", "获取到APK-HASH:" + apkHash);
        //网络请求 获取到APPId下的APK-HASH的补丁
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(8);
        map.put("apkHash", apkHash);
        map.put("appId", appMetaData.getAppId());
        map.put("publicKey", Base64.encodeToString(appMetaData.getPublicKey().getEncoded(), 2));
        map.put("tx", System.currentTimeMillis());
        map.put("sign", SignUtil.getSign(appMetaData.getAccessKey(), map));
        Gson gson = new Gson();
        String doPost = null;
        int count = 0;
        Throwable exception = null;
        do {
            count++;
            try {
                doPost = NetUtil.doPost(appMetaData.getUrl(), gson.toJson(map));
                //短暂的休眠
            } catch (Exception e) {
                exception = e;
                try {
                    Thread.sleep(100 * count);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        } while (doPost == null && count < 2);

        if (doPost == null) {
            if (robustCallBack != null){
                robustCallBack.exceptionNotify(exception,
                        "RobustManipulateSimpleImpl#fetchPatchList，获取补丁信息失败,url:"+appMetaData.getUrl());
            }
            Log.e(TAG, "无法获取补丁信息，请检查网络请求：" + appMetaData.getUrl(),exception);
            return new ArrayList<>();
        }
        try {
            Log.i(TAG, "获取到的数据为：" +doPost);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(doPost).getAsJsonObject();
            if (jsonObject.get("code").getAsInt() == 200) {
                JsonObject dataJson = jsonObject.get("data").getAsJsonObject();
                PatchInfo data = gson.fromJson(dataJson.toString(), PatchInfo.class);
                patch = new Patch();
                patch.setAppHash(apkHash);
                patch.setMd5(data.getMd5());
                patch.setName(data.getPatchId());
                patch.setUrl(data.getDownloadUrl());
                String path = Environment.getExternalStorageDirectory().getPath();
                patch.setPatchesInfoImplClassFullName(patchesInfoImplClassFullName);
                patch.setLocalPath(path + File.separator + "robust" + File.separator + "store" + File.separator + "patch");
                patch.setTempPath(path + File.separator + "robust" + File.separator + "tmp" + File.separator + "patch");
                RobustSdkManager.setPatchInfo(data);
            } else {
                Log.e(TAG, "无法解析数据：" + doPost);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            if (robustCallBack != null){
                robustCallBack.exceptionNotify(e,"fetchPatchList,解析服务器数据失败");
            }
            e.printStackTrace();
        }
        return patch == null ? new ArrayList<Patch>() : Collections.singletonList(patch);
    }

    @Override
    protected boolean verifyPatch(Context context, Patch patch) {
        Log.i(TAG, "开始处理源文件");
        PatchInfo pathInfo = RobustSdkManager.getPatchInfo();
        if (pathInfo == null) {
            return false;
        }
        File file = new File(patch.getLocalPath());
        if (file.exists() && file.isFile()) {
            Log.i(TAG, "源文件存在，开始解密源文件");
            ByteArrayOutputStream byteArrayOutputStream = null;
            FileOutputStream fileOutputStream = null;
            FileInputStream fileInputStream = null;
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] buff = new byte[1024 * 4];
                fileOutputStream = new FileOutputStream(new File(patch.getTempPath()));
                fileInputStream = new FileInputStream(file);
                int len;
                byteArrayOutputStream = new ByteArrayOutputStream();
                while ((len = fileInputStream.read(buff)) > 0) {
                    byteArrayOutputStream.write(buff,0,len);
                }
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String secret = DigestUtil.rsaDecode(pathInfo.getSecret(),
                        Base64.encodeToString(RobustSdkManager.getInstance().getAppMetaData().getPrivateKey().getEncoded(), 2));
                byte[] dec = DigestUtil.decode(bytes, pathInfo.getNorIV(), secret);
                if (Arrays.equals(new byte[0], dec)) {
                    throw new IllegalStateException("解密失败，请检查密钥");
                }
                fileOutputStream.write(dec, 0, dec.length);
                digest.update(dec, 0, dec.length);
                String fileMd5 = Base64.encodeToString(digest.digest(), 2);
                return fileMd5.equals(patch.getMd5());
            } catch (Exception e) {
                if (robustCallBack != null){
                    robustCallBack.exceptionNotify(e,"解密验证补丁包失败");
                }
                //删除下载的jar
                try {
                    FileUtil.deleteFile(patch.getLocalPath());
                    FileUtil.deleteFile(patch.getTempPath());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } finally {
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return false;
    }

    @Override
    protected boolean ensurePatchExist(Patch patch) {
        //判断补丁是否已存在 如果不存在那么就联网下载
        boolean localPathCreated = FileUtil.createDir(patch.getLocalPath());
        if (!localPathCreated) {
            return false;
        }
        boolean tmpdirCreated = FileUtil.createDir(patch.getTempPath());
        if (!tmpdirCreated) {
            return false;
        }
        File file = new File(patch.getLocalPath());
        if (!file.exists()) {
            //补丁包不存在 那么就去联网下载 加入重试
            boolean download;
            int count = 0;
            String privateKey = Base64.encodeToString(RobustSdkManager.getInstance().getAppMetaData().getPrivateKey().getEncoded(), 2);
            String url = DigestUtil.rsaDecode(patch.getUrl(), privateKey);
            do {
                count++;
                download = NetUtil.download(url, patch.getLocalPath(), robustCallBack);
                if (!download){
                    try {
                        Thread.sleep(100* count);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (!download && count < 3);
            if (!download){
                //删除下载的jar
                try {
                    FileUtil.deleteFile(patch.getLocalPath());
                    FileUtil.deleteFile(patch.getTempPath());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            return download;
        } else {
            return true;
        }
    }
}
