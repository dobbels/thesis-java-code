package encryption;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import hidra.HidraUtility;

public class HMAC {

  public static void main(String[] args) throws Exception {
	  
//	  key =         0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b
//	  key_len =     16 bytes
//	  data =        "Hi There"
//	  data_len =    8  bytes
//	  digest =      0x9294727a3638bb1c13f48ef8158bfc9d
//
//	  key =         "Jefe"
//	  data =        "what do ya want for nothing?"
//	  data_len =    28 bytes
//	  digest =      0x750c783e6ab0b503eaa86e310a5db738
	  byte[] testVector = 
			{ 
				(byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff,
				0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 
				(byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd
		};
	  
//	  char[] Kr =  { 0x2b,  0x7e,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	  char[] Kr =  { 0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b,  0x0b};
	  
	  System.out.println(HidraUtility.byteArrayToHexString("Hi There".getBytes()));
	  System.out.println(HidraUtility.byteArrayToHexString(hmacDigest("Hi There".getBytes(), Kr, "HmacMD5")));
  }

  public static byte[] hmacDigest(byte[] msg, char[] charBytes, String algo) {
	  byte[] keyBytes = new byte[16];
	  
	  for (int i = 0 ; i < keyBytes.length ; i++) {
		  keyBytes[i] = (byte) charBytes[i];		  
	  }
	  byte[] bytes = new byte[16];
	  
	  String digest = null;
	  try {
		  SecretKeySpec key = new SecretKeySpec(keyBytes, algo);
		  Mac mac = Mac.getInstance(algo);
	      mac.init(key);
	
	      bytes = mac.doFinal(msg);
	      
	      
	
	      } catch (InvalidKeyException e) {
	    } catch (NoSuchAlgorithmException e) {
	   }
	  return bytes;
  }
}