package com.branes.partysync.helper;

import org.apache.commons.lang3.StringUtils;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class SecurityHelper {

    private static String password;
    private static SecretKey secretKey;
    private static AlgorithmParameterSpec algorithmParameterSpec;

    public static void initialize(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        SecurityHelper.password = password;
        if (password.length() < 16) {
            SecurityHelper.password = StringUtils.rightPad(SecurityHelper.password, 16, '*');
        }
        SecurityHelper.secretKey = generateKey();
        SecurityHelper.algorithmParameterSpec = generateParameterSpec();

    }

    private static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(password.getBytes(), "AES");
    }

    private static AlgorithmParameterSpec generateParameterSpec() {
        //TODO:replace with a random generated vector
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        return new IvParameterSpec(iv);
    }

    public static byte[] encryptMsg(String message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            UnsupportedEncodingException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameterSpec);
        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    public static String decryptMsg(byte[] cipherText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameterSpec);
        return new String(cipher.doFinal(cipherText), "UTF-8");
    }
}

