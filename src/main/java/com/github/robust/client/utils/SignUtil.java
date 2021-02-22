package com.github.robust.client.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author sorata 2021/2/1 6:00 下午
 */
public abstract class SignUtil {


    public static String getSign(String key, LinkedHashMap<String,Object> hashMap){
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return DigestUtil.signWithMacSha256(stringBuilder.toString(),key);
    }

}
