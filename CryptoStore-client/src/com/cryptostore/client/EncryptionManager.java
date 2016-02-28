package com.cryptostore.client;

import org.apache.commons.io.FilenameUtils;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class EncryptionManager {
    private static final int NUMBER_OF_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 32*8;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16; // IV should always be 16 bytes long. DO NOT modify

    private EncryptionManager() {
    }

    public static byte[] encryptFile(char[] password, Path filePath) throws Exception {
        try {
            System.out.println("\nEncrypting file: " + FilenameUtils.getBaseName(filePath.getFileName().toString()));

            byte[] salt = getRandomBytes(SALT_LENGTH);
            SecretKey secretKey = generateSecretKey(password, salt);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(getRandomBytes(IV_LENGTH)));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);

            AlgorithmParameters params = cipher.getParameters();
            outputStream.write(params.getParameterSpec(IvParameterSpec.class).getIV());

            outputStream.write(cipher.doFinal(Files.readAllBytes(filePath)));

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new Exception(Error.CANNOT_ENCRYPT.getDescription());
        }
    }

    public static byte[] decryptFile(char[] password, byte[] fullFile) throws Exception {
        try {
            System.out.println("\nDecrypting file. . .");

            byte[] salt = Arrays.copyOf(fullFile, SALT_LENGTH);
            byte[] iv = Arrays.copyOfRange(fullFile, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(fullFile, SALT_LENGTH + IV_LENGTH, fullFile.length);

            SecretKey secretKey = generateSecretKey(password, salt);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            System.out.println("File decrypted!");
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new Exception(Error.CANNOT_DECRYPT.getDescription());
        }
    }

    private static SecretKey generateSecretKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password, salt, NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKey secretKey = factory.generateSecret(keySpec);
        byte[] hash = secretKey.getEncoded();

        return new SecretKeySpec(hash, "AES");
    }

    private static byte[] getRandomBytes(int length) throws NoSuchAlgorithmException {
        byte[] salt = new byte[length];
        (new SecureRandom()).nextBytes(salt);

        return salt;
    }
}
