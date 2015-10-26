import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class HashGenerator {
    private static final int NUMBER_OF_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 32*8;

    public static String getHash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, NUMBER_OF_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashBytes = f.generateSecret(spec).getEncoded();

        return DatatypeConverter.printHexBinary(hashBytes);
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        (new SecureRandom()).nextBytes(salt);
        return salt;
    }
}