package hidra;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared utilities to be use by different classes
 */
public class HidraUtility {
	
	private static byte zeroByte = 0;
	
	enum Effect { 
		  DENY,
		  PERMIT
		}
	
	enum Action {
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
		byte a = 17;
		put(a, "lowBattery");
		put(++a,"<");
		put(++a,"contains");
		put(++a, "isTrue");
	}};
	
	public static HashMap<Byte, String> taskRereferences = new HashMap<Byte, String>() {{
		byte a = 27;
		put(a, "activate");
		put(++a, "++");
	}};
	
	public static HashMap<Byte, String> systemRereferences = new HashMap<Byte, String>() {{
		byte a = 37;
		put(a, "onMaintenance");
		put(++a,"bios_upgrades");
	}};
	
	public static HashMap<Byte, String> requestRereferences = new HashMap<Byte, String>() {{
		byte a = 47;
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
	 * Method copied from source: http://stackoverflow.com/questions/1936857/convert-integer-into-byte-array-java
	 * @param value is integer
	 * @return a byte array
	 */
	public static byte[] intToByteArray( final int value ) {
	    BigInteger bigInt = BigInteger.valueOf(value);      
	    return bigInt.toByteArray();
	}
	
	public static byte[] intToByteArray( final int value, int size){
		byte[] small = intToByteArray(value);
		byte[] result = new byte[size];
		System.arraycopy(small, 0, result, size-small.length, small.length);
		return result;
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
	
	/**
	 * Convert a IP-string (of 4 bytes) to an integer (4 bytes).
	 * @param stringIP
	 */
	public static int strIPToInt(String stringIP) {
		return byteArrayToInt(strIPtoByteArray(stringIP));
	}
	
	/**
	 * Convert an integer (4 bytes) to a IP-string (of 4 bytes).
	 * @param nb
	 */
	public static String intToStrIP(int nb) {
		return printIP(intToByteArray(nb));
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
}