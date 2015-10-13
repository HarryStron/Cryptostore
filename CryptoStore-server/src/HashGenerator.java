import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class HashGenerator {
    public static byte[] getHash(String password, byte[] salt,  int iterations,  int derivedKeyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength * 8);

        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return f.generateSecret(spec).getEncoded();
    }

    public static String getHashString(byte[] bytes) throws UnsupportedEncodingException {
        String helloHex = DatatypeConverter.printHexBinary(bytes);

        //convert hex-encoded string back to original string
        byte[] decodedHex = DatatypeConverter.parseHexBinary(helloHex);
        return new String(decodedHex, "UTF-8");
    }
}
