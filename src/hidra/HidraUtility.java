package hidra;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import encryption.Alice;

/**
 * Shared utilities to be use by different classes
 */
public class HidraUtility {
	
	private static byte zeroByte = 0;
	
	enum Effect { 
		  DENY,
		  PERMIT
		}
	
	public enum Action {
		GET,
		POST,
		PUT,
		DELETE,
		ANY
	}
	
	enum AttributeType {
		BOOLEAN,
		BYTE,
		INTEGER,
		FLOAT,
		STRING,
		REQUEST_REFERENCE,
		SYSTEM_REFERENCE,
		LOCAL_REFERENCE
	}
	
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
		
		//TODO check that the last byte is handled well, i.e. is padded up  
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
	
	public static ArrayList<Boolean> actionToBoolList(Action action) {
		ArrayList<Boolean> result = new ArrayList<>();
		byte actionId = 8; 
		if (action == Action.GET) {
			actionId = 0;
		} else if (action == Action.POST) {
			actionId = 1;
		} else if (action == Action.PUT) {
			actionId = 2;
		} else if (action == Action.DELETE) {
			actionId = 3;
		} else if (action == Action.ANY) {
			actionId = 4;
		} else {
			System.out.println("Error: did not find action type.");
		}
		
		// Should be a number between 0 and 7 => only add last 3 booleans
		ArrayList<Boolean> actionIdList = byteToBoolList(actionId);
		for (int i = 5 ; i < 8 ; i++) {
			result.add(actionIdList.get(i));
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
	
//	public static byte[] intToByteArray( final short value ) {
//		return new byte[] {
//			      (byte) (value >> 8), (byte) (value) };
//	}
	
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
	
	/**
	 * Method copied from source: http://stackoverflow.com/questions/5399798/byte-array-and-int-conversion-in-java
	 * @param b	byte array
	 * @return	integer corresponding to the given byte array
	 */
	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	/**
	 * Analog to byteArrayToInt()
	 * @param b	byte array
	 * @return	short corresponding to the given byte array
	 */
	public static short byteArrayToShort(byte[] b) 
	{
	    return   (short) (b[1] & 0xFF |
	            (b[0] & 0xFF) << 8); // the cast should keep the least significant 16 bits
	}
	
	/**
	 * Converts the first 4 byte values of a byte array to an ip string.
	 * @param ba - a byte array of 4 bytes
	 * @return - IP String representation
	 */
	public static String printIP(byte[] ba) {
		assert(ba.length >= 4);
		return printIP(ba[0], ba[1], ba[2], ba[3]);
	}

	/** TODO dit voor IPv6 ipv IPv4 !
	 * Converts 4 byte values to an ip string
	 * @param a - 1st byte value
	 * @param b - 2nd byte value
	 * @param c - 3rd byte value
	 * @param d - 4th byte value
	 * @return - IP String representation
	 */
	public static String printIP(byte a, byte b, byte c, byte d) {
		String str = "";
		str += (a & 0xff) + "." + (b & 0xff) + "." + (c & 0xff) + "." + (d & 0xff);
		return str;
	}

	//http://stackoverflow.com/questions/16253077/string-to-hexadecimal
	public static byte[] strIPtoByteArray(String stringIP){
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(stringIP);
		} catch (Exception e) {
			System.out.println("Couldn't convert stringIP into byteArray");
		}
		byte[] bytes = ip.getAddress();
		return bytes;
		
	}
	
	public static String bytesToHex(byte[] bytes) {
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
	
	/**
	 * Checks if two byte arrays are the same.
	 * If the arrays have different lenghts, only the a subarray of
	 * the bigger array will be checked for equality. 
	 */
	public static boolean areEqual(byte[] a, byte[] b) {
		boolean areEqual = true;
		for (int i=0; i < Math.min(a.length, b.length); i++) {
			if (a[i] != b[i]) {
				areEqual = false;
			}
		}
		return areEqual;
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
	
	public static byte[] xcrypt(byte[] plain_text, char[] key) {		
		byte[] encrypted_text = null; 
		Alice encryptor = new Alice(HidraACS.ctx);
		try {
			//Ignore the generated initial vector of size 16
			byte [] result = encryptor.encrypt(plain_text, key);
			encrypted_text = Arrays.copyOfRange(result, 16, result.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encrypted_text;
	}
}