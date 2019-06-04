package hidra;

import hidra.PolicyRule.Action;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import encryption.AliceEncryption;
import encryption.AliceContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.text.Utilities;
 
/**
 * Shared utilities to be use by different classes
 */
public class Utility {
	
	private static final int ACS_RESOURCE_PORT = 1234;
	private static final int ACS_SUBJECT_PORT = 4321;
	private static String resourceIP = "fd00::c30c:0:0:2"; // If the mote is a z1 mote (and the 2nd that was added to the simulation)
	
	public static int getServerResourcePort() {
		return ACS_RESOURCE_PORT;
	}

	public static String getResourceIP() {
		return resourceIP;
	}

	public static void setResourceIP(String resourceIP) {
		Utility.resourceIP = resourceIP;
	}

	public static int getServerSubjectPort() {
		return ACS_SUBJECT_PORT;
	}

	// Very hardcoded, but should work for a demo on Z1 devices
	public static String getSubjectIP(int id) {
		return ("fd00::c30c:0:0:" + id);
	}
	
	public static char[] getSubjectKey(char id) {
		char[] Ks = { 0x7e, 0x2b,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  id };
		return Ks;
	}
	
	private static byte zeroByte = 0;
	
	
	// All reference tables originate from the resource and are shared with the ACS.
	public static HashMap<Byte, String> expressionRereferences = new HashMap<Byte, String>() {{
		byte a = 4;
		put(a, "low_battery");
		put(++a,"<");
		put(++a,"contains");
		put(++a, "isTrue");
	}};
	
	public static HashMap<Byte, String> taskRereferences = new HashMap<Byte, String>() {{
		byte a = 8;
		put(a, "activate");
		put(++a, "log_request");
		put(++a,"++");
	}};
	
	public static HashMap<Byte, String> systemRereferences = new HashMap<Byte, String>() {{
		byte a = 16;
		put(a, "onMaintenance");
		put(++a,"bios_upgrades");
		put(++a,"switch_light_on");
		put(++a,"switch_light_off");
		put(++a,"nb_of_access_requests_made");
	}};
	
	public static HashMap<Byte, String> requestRereferences = new HashMap<Byte, String>() {{
		byte a = 32;
		put(a, "roles");
	}};
	
	public static byte getId(HashMap<Byte,String> referenceTable, String name) {
		Set<Entry<Byte, String>> entrySet = referenceTable.entrySet();
		Iterator<Entry<Byte, String>> it = entrySet.iterator();
		
		Entry<Byte, String> temp;
		
		while (it.hasNext()) {
			temp = it.next();
			if (temp.getValue().equals(name)) {
				return temp.getKey();
			}
		}
		System.out.println(name);
		System.out.println("Error: no match for given expression name");
		return zeroByte;
	}
	
	/**
	 * Pad ending of boolean array and convert to bytes.
	 */
	public static byte[] booleanArrayToByteArray(ArrayList<Boolean> input){
		int nbOfBytes = (input.size() + 7) / 8;
		byte[] bytes = new byte[nbOfBytes]; 
		
		// Pad bitset so its length l mod 8 = 0. 
		// 	Extra bits come at the end, so that they can be easily ignored by the receiver.
		int emptyRemainder = nbOfBytes*8 - input.size();
		for (int r = 0 ; r < emptyRemainder ; r ++) {
			input.add(false);  
		}

		ArrayList<Boolean> nextPart = new ArrayList<Boolean>();
		for (int i = 0 ; i < 8 ; i++) {nextPart.add(false);};
		for (int currentByte = 0 ; currentByte < nbOfBytes ; currentByte++) {
			for (int bit = 0 ; bit < 8 ; bit++) {
				nextPart.set(bit, input.get(currentByte*8 + bit));
			}
			bytes[currentByte] = booleanArrayToByte(nextPart);
		}
		return bytes;
	}
	
	public static byte booleanArrayToByte(ArrayList<Boolean> input){
		if (input.size() != 8) {
			System.out.println("Error in codification: booleanArrayToByte");
		}
		
		byte result = 0;
		byte increment = 64;
		
		// A byte is signed in Java! First the other bits are handled.
		for (int i = 1 ; i < 8 ; i++) {
			if (input.get(i)) {
				result += increment;
			}
			increment /= 2;
		}
		
		// Then the first bit is handled. 
		if (input.get(0)) {
			result = (byte) (result - 128);
		}
		
		return result;
	}
	
	/**
	 * Limitation: From -127 to 127, not 0 - 255!
	 * Java's two complement representation is copied exactly. 
	 */
	public static ArrayList<Boolean> byteToBoolList(byte b) {
		ArrayList<Boolean> result = new ArrayList<>();
		
		if (b < 0) {
			result.add(true);
		} else {
			result.add(false);
		}
		
		// Make room to set later
		for (int j = 0 ; j < 7 ; j++) {
			result.add(false);
		}
		
		for (int i = 7; i > 0; i--) {
        	result.set(i, ((b & 1) == 1) ? true : false);
            b >>= 1;
        }
        
		return result;
	}
	
	/**
	 * @param nb | a number between 1 and 8 specifying how many bits wanted (counted from least to most significant)
	 */
	public static ArrayList<Boolean> byteToBoolList(byte b, int nb) {
		ArrayList<Boolean> boolList = new ArrayList<>();
		
		if (b < 0) {
			boolList.add(true);
		} else {
			boolList.add(false);
		}
		
		// Make room to set later
		for (int j = 0 ; j < 7 ; j++) {
			boolList.add(false);
		}
		
		for (int i = 7; i > 0; i--) {
        	boolList.set(i, ((b & 1) == 1) ? true : false);
            b >>= 1;
        }
		
		ArrayList<Boolean> necessaryBits = new ArrayList<>();
		
		for (int i = (8-nb) ; i < 8 ; i++) {
			necessaryBits.add(boolList.get(i));
		}
        
		return necessaryBits;
	}
	
	public static void printBoolList(ArrayList<Boolean> bl) {
		StringBuilder sb = new StringBuilder();

        for (boolean b : bl) {
            sb.append((b) ? '1' : '0');
        }

        System.out.println(sb.toString());
	}
	
	public static ArrayList<Boolean> byteArrayToBooleanList(byte[] byteArray) {
		ArrayList<Boolean> boolList = new ArrayList<>();
		
		for (int i=0; i < byteArray.length; i++) {
			boolList.addAll(byteToBoolList(byteArray[i]));
		}
		return boolList;
	}
	
	/**
	 * From https://www.baeldung.com/java-convert-float-to-byte-array
	 */
	public static byte[] floatToByteArray(float value) {
//		FloatBuffer fb = ByteBuffer.allocateDirect(size*4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
	    int intBits =  Float.floatToIntBits(value);
	    return new byte[] {
	      (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
	}
	
	/**
	 * Analog method for short data types. Copied from http://www.java2s.com/Tutorials/Java/Data_Type/Array_Convert/Convert_short_to_byte_array_in_Java.htm
	 * @param value is short
	 * @return a byte array
	 */
	public static byte[] shortToByteArray(short value) {
	    byte[] bytes = new byte[2];
	    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
	    buffer.putShort(value);
	    return buffer.array();

	}
	
	public static byte[] longToByteArray(long value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
	    buffer.putLong(value);
	    return buffer.array();
	}
	
	public static String byteArrayToHexString(byte[] bytes) {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	public static String byteToHexString(byte b) {
		String str = new String(Integer.toHexString(new Integer(b & 0xff)));
		return str;
	}

	public static ArrayList<Boolean> intToBoolList(short intValue) {
		return byteArrayToBooleanList(shortToByteArray(intValue));
	}

	public static ArrayList<Boolean> floatToBoolList(float floatValue) {
		return byteArrayToBooleanList(floatToByteArray(floatValue));
	}

	public static ArrayList<Boolean> stringToBoolList(String str) {
		return byteArrayToBooleanList(str.getBytes());
	}
	
	/*
	 * Uses Alice to encrypt and decrypt with AES in CTR mode and 
	 * returns the result, leaving out the generated initial vector. 
	 */
	public static byte[] xcrypt(byte[] plain_text, char[] key) {		
		byte[] encrypted_text = null; 
		AliceEncryption encryptor = new AliceEncryption(TrustedServer.ctx);
		try {
			//Ignore the generated initial vector of size 16
			byte [] result = encryptor.encrypt(plain_text, key);
			encrypted_text = Arrays.copyOfRange(result, 16, result.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encrypted_text;
	}
	
	public static byte[] computeAndStoreOneWayHashChain() {		
		byte[] base_key = new byte[16];
        new Random().nextBytes(base_key);
		byte[] next_key = base_key;
		int N = 20;
		ArrayList<byte[]> keyChain = new ArrayList<byte[]>(); 
		for (int i = 0 ; i < N ; i++) {
			try {
				next_key = getMD5Hash(next_key);
//				System.out.println(byteArrayToHexString(next_key));
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			keyChain.add(next_key);
		}
		TrustedServer.setKeyChain(keyChain);
		return TrustedServer.getNextKeyChainValue();
	}
	
	public static byte[] getMD5Hash(byte[] passwordBytes) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(passwordBytes);
    }
	
	public static byte[] compute4ByteMac(byte[] bytes) {
		return Utility.hashTo4Bytes(computeMac(bytes));
	}
	
	//Compute MAC of the given byte array based on the secret resource key.  
	public static byte[] computeMac(byte[] bytes) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			output.write(AliceEncryption.getMac(AliceContext.MacAlgorithm.HMAC_SHA_256, TrustedServer.Kr).doFinal(bytes));
		} catch (IllegalStateException | IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
        return output.toByteArray();
	}
	
	public static byte[] hashTo4Bytes(byte[] bytes) {
		byte[] result = longToByteArray(Murmur3.hash_x86_32(bytes, bytes.length, 17));
		return  Arrays.copyOfRange(result, 4, 8);
	}
}