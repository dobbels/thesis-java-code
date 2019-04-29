package hidra;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Class to encrypt and decrypt messages 
 * 
 * Based on 
 * https://gist.github.com/SimoneStefani/99052e8ce0550eb7725ca8681e4225c5
 * and https://stackoverflow.com/questions/14413169/which-java-library-provides-base64-encoding-decoding
 */
public class AESenc {
  private static final String ALGO = "AES";
//  private static final byte[] keyValue =
//            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    /**
     * Encrypt a byte array with AES algorithm.
     */
    public static byte[] encrypt(byte[] data, byte[] keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encVal = c.doFinal(data);
//        return DatatypeConverter.printBase64Binary(encVal);
        return c.doFinal(data);
    }

    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     */
    public static byte[] decrypt(byte [] encryptedData, byte[] keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(encryptedData);
    }

    /**
     * Generate a new encryption key.
     */
//    private static Key generateKey() throws Exception {
//        return new SecretKeySpec(keyValue, ALGO);
//    }

}