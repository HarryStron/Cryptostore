package com.cryptostore.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class HashGenerator {
    private static final int NUMBER_OF_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 32*8;

    public static String getPBKDF2(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashBytes = f.generateSecret(spec).getEncoded();

        return DatatypeConverter.printHexBinary(hashBytes);
    }

    /** Used for hashing files to check them against the other side of the app for bandwidth efficiency **/
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