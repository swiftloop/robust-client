package com.github.robust.client.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author sorata 2021/2/1 3:45 下午
 */
public abstract class DigestUtil {

    public static final String CHARSET = "UTF-8";

    public static byte[] encode(byte[] origin, String normalVI, String keyForBase64) {
        if (origin == null) {
            return new byte[0];
        }
        try {
            final SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(keyForBase64, Base64.DEFAULT), "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(normalVI.getBytes(CHARSET)));
            return cipher.doFinal(origin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] decode(byte[] srcEncode, String normalVI, String keyForBase64) {
        if (srcEncode == null) {
            return new byte[0];
        }
        try {
            final SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(keyForBase64, 2), "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(normalVI.getBytes(CHARSET)));
            return cipher.doFinal(srcEncode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }


    public static String signWithMacSha256(String signChar, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSha256");
            mac.init(new SecretKeySpec(Base64.decode(key, 2), "HmacSha256"));
            return Base64.encodeToString(mac.doFinal(signChar.getBytes(CHARSET)), 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String rsaEncode(String origin, String publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey, 2));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec));
            return Base64.encodeToString(cipher.doFinal(origin.getBytes(CHARSET)), 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String rsaDecode(String encodeStr, String privateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey, 2));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey generatePrivate = factory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, generatePrivate);
            return new String(cipher.doFinal(Base64.decode(encodeStr, 2)),CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String generatorKey(int len) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(len, new SecureRandom());
            return Base64.encodeToString(keyGenerator.generateKey().getEncoded(), 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
