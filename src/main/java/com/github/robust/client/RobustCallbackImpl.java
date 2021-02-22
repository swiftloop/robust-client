package com.github.robust.client;


import android.util.Log;
import com.github.robust.client.utils.FileUtil;
import com.github.robust.client.utils.NetUtil;
import com.google.gson.Gson;
import com.meituan.robust.Patch;
import com.meituan.robust.RobustCallBack;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sorata 2021/1/29 4:09 下午
 */
public class RobustCallbackImpl implements RobustCallBack {

    private static final String TAG = "Robust-Callback";
    private static final Gson GSON = new Gson();


    @Override
    public void onPatchListFetched(boolean result, boolean isNet, List<Patch> patches) {
        final HashMap<String, String> map = new HashMap<>(8);
        map.put("code", "1000");
        map.put("msg", "加载补丁" + (result ? "成功" : "失败"));
        postBody(map);
    }

    @Override
    public void onPatchFetched(boolean result, boolean isNet, Patch patch) {
        final HashMap<String, String> map = new HashMap<>(8);
        map.put("code", "1000");
        map.put("msg", "加载补丁" + (result ? "成功" : "失败"));
        postBody(map);
    }

    @Override
    public void onPatchApplied(boolean result, Patch patch) {
        FileUtil.deleteFile(patch.getTempPath());
        final HashMap<String, String> map = new HashMap<>(8);
        map.put("code", result ? "1001" : "1002");
        map.put("msg", "补丁应用" + (result ? "成功" : "失败"));
        map.put("patchId", patch.getName());
        postBody(map);
    }

    @Override
    public void logNotify(String log, String where) {
        Log.i(TAG, "补丁加载日志：" + log + " 出处： " + where);
    }

    @Override
    public void exceptionNotify(Throwable throwable, String where) {
        Log.i(TAG, "补丁加载异常：" + throwable.getMessage() + " 出处： " + where);
        final HashMap<String, String> map = new HashMap<>(8);
        map.put("code", "1003");
        map.put("msg", "补丁应用加载失败" + where);
        map.put("exp", printException(throwable));
        PatchInfo patchInfo = RobustSdkManager.getPatchInfo();
        map.put("patchId", patchInfo == null ? "unknown" : patchInfo.getPatchId());
        postBody(map);
    }

    private void postBody(final Map<String, String> body) {
        final String reportUrl = RobustSdkManager.getInstance().getAppMetaData().getReportUrl();
        if (reportUrl == null) {
            return;
        }
        try {
            NetUtil.doPost(reportUrl, GSON.toJson(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String printException(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        PrintWriter printWriter = null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            printWriter = new PrintWriter(stream);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            return stream.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        return "";
    }


}
