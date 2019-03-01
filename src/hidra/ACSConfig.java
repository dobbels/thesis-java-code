package hidra;

/**
 * A class containing the basic set-up parameters for our system.
 */
public class ACSConfig {
	
	private static final int CLIENT_PORT = 1996;
	private static String clientIP = "127.0.0.1";
	
	
	// Tun0 interface
	// inet : 127.0.1.1
	// inet6 : fd00::1/64 (Global)
	//TODO denk eraan dat je toen twee aparte testen had. 1 met jou als server, 1 met lab als server
	private static final int SERVER_PORT = 1234; 
	private static String serverIP = "fd00::212:7402:2:202"; 
//	private static String serverIP = "fd00::c30c:0:0:1"; 	
	
	
	public static int getClientPort() {
		return CLIENT_PORT;
	}
	
	public static String getClientIP() {
		return clientIP;
	}
	
	public static int getServerPort() {
		return SERVER_PORT;
	}
	
	public static String getServerIP() {
		return serverIP;
	}
}
