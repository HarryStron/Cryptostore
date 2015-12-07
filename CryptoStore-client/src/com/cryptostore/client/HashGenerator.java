package com.cryptostore.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

    public static String getSHA256(byte[] file) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] mdbytes = md.digest(file);

        StringBuilder hexString = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            hexString.append(Integer.toHexString(0xFF & mdbyte));
        }

        return hexString.toString();
    }
}