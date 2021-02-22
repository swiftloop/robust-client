package com.github.robust.client.utils;

import android.util.Log;
import com.meituan.robust.RobustCallBack;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author sorata 2021/2/1 2:36 下午
 */
public abstract class NetUtil {

    public static final String CONTENT_TYPE_DOWNLOAD = "application/octet-stream;charset=utf8";
    public static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_NAME = "utf-8";

    public static boolean download(String url, String savePath, RobustCallBack noticeCall) {
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setDoInput(true);
            connection.addRequestProperty("Content-Type", CONTENT_TYPE_DOWNLOAD);
            connection.addRequestProperty("User-Agent", "Robust-Client");
            byte[] buff = new byte[1024 * 4];
            fileOutputStream = new FileOutputStream(new File(savePath));
            inputStream = connection.getInputStream();
            int flag;
            while ((flag = inputStream.read(buff)) > 0 ) {
                fileOutputStream.write(buff, 0, flag);
            }
            Log.i("FILE_DOWNLOAD","文件下载完成:" + savePath);
            return true;
        } catch (IOException e) {
            if (noticeCall != null) {
                noticeCall.exceptionNotify(e, "下载补丁异常");
            }
            e.printStackTrace();
        } finally {
            FileUtil.closeStream(fileOutputStream,inputStream);
        }
        return false;
    }


    public static String doPost(String url, String body) throws Exception{
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", CONTENT_TYPE_JSON);
            connection.addRequestProperty("User-Agent", "Robust-Client");
            outputStream = connection.getOutputStream();
            outputStream.write(body.getBytes(CHARSET_NAME));
            StringBuilder stringBuilder = new StringBuilder();
            inputStream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String strBuff;
            while ((strBuff = bufferedReader.readLine()) != null){
                stringBuilder.append(strBuff);
            }
            return stringBuilder.toString();
        } finally {
            FileUtil.closeStream(bufferedReader,outputStream,inputStream);
        }
    }


}
