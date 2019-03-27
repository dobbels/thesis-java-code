package hidra;

/**
 * A class containing the basic set-up parameters for our system.
 */
public class HidraConfig {
	
	/*
	 * All the hassle with the names and ports is because at the moment 
	 * Subject and ACS have the same loopback address and communication needs to be separated TODO true?
	 */
	private static final int SUBJECT_PORT_FOR_COMM_WITH_ACS = 1996;
	private static final int ACS_PORT_FOR_COMM_WITH_SUBJECT = 1995;
	private static String localIP = "127.0.0.1";
	
	//TODO poorten voor resource <-> subject communicatie (en dan ook naar fd00::1(?))
	
	private static final int ACS_RESOURCE_PORT = 1234;
	private static final int SUJECT_RESOURCE_PORT = 4321;
	private static String globalACSIP = "fd00::1";
//	private static String resourceIP = "fd00::212:7402:2:202"; // If the mote is a sky mote (and the 2nd that was added to the simulation)
	private static String resourceIP = "fd00::c30c:0:0:2"; // If the mote is a z1 mote (and the 2nd that was added to the simulation)
	
	
	public static String getLocalIP() {
		return localIP;
	}

	public static void setLocalIP(String localIP) {
		HidraConfig.localIP = localIP;
	}

	public static int getAcsResourcePort() {
		return ACS_RESOURCE_PORT;
	}

	public static int getSujectToResourcePort() {
		return SUJECT_RESOURCE_PORT;
	}

	public static String getGlobalACSIP() {
		return globalACSIP;
	}

	public static void setGlobalACSIP(String globalACSIP) {
		HidraConfig.globalACSIP = globalACSIP;
	}

	public static String getResourceIP() {
		return resourceIP;
	}

	public static void setResourceIP(String resourceIP) {
		HidraConfig.resourceIP = resourceIP;
	}

	public static int getSubjectPortForCommWithAcs() {
		return SUBJECT_PORT_FOR_COMM_WITH_ACS;
	}

	public static int getAcsPortForCommWithSubject() {
		return ACS_PORT_FOR_COMM_WITH_SUBJECT;
	}
}
