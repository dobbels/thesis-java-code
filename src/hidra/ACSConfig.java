package hidra;

/**
 * A class containing the basic set-up parameters for our system.
 */
public class ACSConfig {
	
	// Config client
	private static final int CLIENT_PORT = 1668;
//	private static String clientIP = "10.43.82.76"; //STEVEN
//	private static String clientIP = "192.168.0.113"; //MICHIEL
	private static String clientIP = "10.33.14.97"; //MICHIEL


	private static String MAC = "a8:bb:cf:1f:82:48";  // MAC address Macbook Pro Michiel
	
	// Config server
	private static final int SERVER_PORT = 1234; 
	private static String serverIP = "10.33.14.246"; // IP of testServer in lab
//	private static String serverIP = "10.43.82.76"; //STEVEN
//	private static String serverIP = "10.33.14.97"; //Aubel

	
	
	// Config pool
	private static String lowestIpInPool = "192.172.16.1";
	private static int poolWidth = 20;  // This default-value can be altered, when constructing this object.
										// Caution: last byte of last IP in Pool (i.e. lowestIPInPool[4] + poolWidth)
										// can not be greater than 250. 
	

	private static int standardLeaseTime = 86400000; // standard lease-time in milliseconds (= 1 day) 
	
	public static int getClientPort() {
		return CLIENT_PORT;
	}
	
	public static String getClientIP() {
		return clientIP;
	}
	
	public static String getMAC() {
		return MAC;
	}
	
	public static int getServerPort() {
		return SERVER_PORT;
	}
	
	public static String getServerIP() {
		return serverIP;
	}
	
	public static String getLowestIpInPool() {
		return lowestIpInPool;
	}
	
	public static int getPoolWidth() {
		return poolWidth;
	}
	
	public static int getStandardLeaseTime() {
		return standardLeaseTime;
	}
}
