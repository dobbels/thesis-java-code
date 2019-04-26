package hidra;

/**
 * A class containing the basic set-up parameters for our system.
 */
public class HidraConfig {
	
	private static final int ACS_RESOURCE_PORT = 1234;
	private static final int ACS_SUBJECT_PORT = 4321;
//	private static String resourceIP = "fd00::212:7402:2:202"; // If the mote is a sky mote (and the 2nd that was added to the simulation)
	private static String resourceIP = "fd00::c30c:0:0:2"; // If the mote is a z1 mote (and the 2nd that was added to the simulation)
//	private static String[] subjectIPs = {"fd00::c30c:0:0:3", "fd00::c30c:0:0:4", "fd00::c30c:0:0:5"};
	
	public static int getAcsResourcePort() {
		return ACS_RESOURCE_PORT;
	}

	public static String getResourceIP() {
		return resourceIP;
	}

	public static void setResourceIP(String resourceIP) {
		HidraConfig.resourceIP = resourceIP;
	}

	public static int getAcsSubjectPort() {
		return ACS_SUBJECT_PORT;
	}

	// Very hardcoded, but should work for a demo on Z1 devices
	public static String getSubjectIP(int id) {
		return ("fd00::c30c:0:0:" + id);
	}
}
