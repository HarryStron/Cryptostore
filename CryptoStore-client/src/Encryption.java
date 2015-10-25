import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

public class Encryption {
    private static final int NUMBER_OF_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 32*8;
    private final char[] password;

    public static SecretKey secretKeySpec;
    public static byte[] iv;

    public Encryption(String password) {
        this.password = password.toCharArray();
    }

    public byte[] encryptFile(Path filePath) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException,
            IOException, BadPaddingException, IllegalBlockSizeException, InvalidParameterSpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password, getSalt(), NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKey secretKey = factory.generateSecret(keySpec);
        byte[] salt = secretKey.getEncoded();
        secretKeySpec = new SecretKeySpec(salt, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        AlgorithmParameters params = cipher.getParameters();
        iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        return cipher.doFinal(Files.readAllBytes(filePath));
    }

    public byte[] decryptFile(Path filePath) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        /* Decrypt the message, given derived key and initialization vector. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

        return cipher.doFinal(Files.readAllBytes(filePath));
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        (new SecureRandom()).nextBytes(salt);
        return salt;
    }
}
