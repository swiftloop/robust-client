package com.github.robust.client.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author sorata 2021/2/2 3:01 下午
 */
public abstract class FileUtil {


    public static boolean createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                return parentFile.mkdir();
            }
        }
        return true;
    }

    public static void deleteFile(String path) {
        File temp = new File(path);
        if (temp.exists()) {
            boolean del = temp.delete();
            if (del) {
                Log.i("FileUtil", "删除文件成功,源文件路径：" + path);
            }else {
                Log.e("FileUtil", "删除文件失败,源文件路径：" + path);
            }
        }
    }

    public static void closeStream(Closeable... closeables){
        if (closeables == null){
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
