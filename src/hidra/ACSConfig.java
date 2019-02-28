package hidra;

/**
 * A class containing the basic set-up parameters for our system.
 */
public class ACSConfig {
	
	//TODO Question
	// MAC address necessary? does it change? 
	
	private static final int CLIENT_PORT = 1234;
	private static String clientIP = "127.0.0.96";
	
	//TODO denk eraan dat je toen twee aparte testen had. 1 met jou als server, 1 met lab als server
	private static final int SERVER_PORT = 60001; 
	private static String serverIP = "fd00::c30c:0:0:1"; 	
	
	
	
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
