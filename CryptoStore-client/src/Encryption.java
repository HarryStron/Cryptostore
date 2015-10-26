import org.apache.commons.io.FilenameUtils;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Encryption {
    private static final int NUMBER_OF_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 32*8;
    private static final int SALT_LENGTH = 16;

    public static byte[] iv;

    private Encryption() {
    }

    public static byte[] encryptFile(char[] password, Path filePath) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidParameterSpecException {

        System.out.println("Encrypting file: " + FilenameUtils.getBaseName(filePath.getFileName().toString()));

        byte[] salt = getSalt();
        SecretKey secretKey = generateSecretKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        AlgorithmParameters params = cipher.getParameters();
        iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(cipher.doFinal(Files.readAllBytes(filePath)));

        return outputStream.toByteArray();
    }

    public static byte[] decryptFile(char[] password, Path filePath) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {

        System.out.println("Decrypting file: " + FilenameUtils.getBaseName(filePath.getFileName().toString()));

        byte[] salt = Arrays.copyOf(Files.readAllBytes(filePath), SALT_LENGTH);
        SecretKey secretKey = generateSecretKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] fullFile = Files.readAllBytes(filePath);
        byte[] ciphertext = Arrays.copyOfRange(fullFile, SALT_LENGTH, fullFile.length);

        return cipher.doFinal(ciphertext);
    }

    private static SecretKey generateSecretKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password, salt, NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKey secretKey = factory.generateSecret(keySpec);
        byte[] hash = secretKey.getEncoded();

        return new SecretKeySpec(hash, "AES");
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[SALT_LENGTH];
        (new SecureRandom()).nextBytes(salt);

        return salt;
    }
}
