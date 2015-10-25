import org.apache.commons.io.FilenameUtils;
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

    public static byte[] saltz;
    public static byte[] iv;

    private Encryption() {
    }

    public static byte[] encryptFile(char[] password, Path filePath) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidParameterSpecException {

        System.out.println("Encrypting file: " + FilenameUtils.getBaseName(filePath.getFileName().toString()));

        SecretKey secretKey = generateSecretKey(password, saltz = getSalt());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        AlgorithmParameters params = cipher.getParameters();
        iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        return cipher.doFinal(Files.readAllBytes(filePath));
    }

    public static byte[] decryptFile(char[] password, Path filePath) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {

        System.out.println("Decrypting file: " + FilenameUtils.getBaseName(filePath.getFileName().toString()));

        SecretKey secretKey = generateSecretKey(password, saltz);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return cipher.doFinal(Files.readAllBytes(filePath));
    }

    private static SecretKey generateSecretKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password, salt, NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKey secretKey = factory.generateSecret(keySpec);
        byte[] hash = secretKey.getEncoded();

        return new SecretKeySpec(hash, "AES");
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        (new SecureRandom()).nextBytes(salt);
        return salt;
    }
}
