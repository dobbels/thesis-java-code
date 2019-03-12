package hidra;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;



public class DHCPOptions {

	// Different types of DHCP transactions
	// The DHCPDECLINE transaction (id = 4) should not be implemented for this assignment. 
	public static final int DHCPDISCOVER = 1;
	public static final int DHCPOFFER = 2;
	public static final int DHCPREQUEST = 3;
	public static final int DHCPACK = 5;
	public static final int DHCPNAK = 6;
	public static final int DHCPRELEASE = 7;

	// The DHCP-server for testing purposes.
	private static final byte[] serverIP = null;



	// Different DHCP options 
	// Chronologic Usage: https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol
	// Explanation: https://tools.ietf.org/html/rfc2132
	public static final int MESSAGETYPE = 53;
	public static final int REQUESTEDIP = 50;
	public static final int PARAMREQLIST = 55;
	public static final int SUBNETMASK = 1;		
	public static final int ROUTEROPTION = 3;	
	public static final int IPLEASETIME = 51;	
	public static final int SERVERID = 54;		
	public static final int DNSOPTION = 6; 
	public static final int MESSAGE = 55;
	public static final int END = 255;
	
	//Magic cookies were used in BOOTP protocol, which was a precursor to DHCP. In order to provide backward compatibility, DHCP still uses the magic cookie from BOOTP protocol.
	//See magic cookie RFC 2131 page 13. 
	public static final byte[] magicCookie = new byte[] {99, (byte)130, 83, 99};
	public static final int lengthMagicCookie = magicCookie.length;

	public static final int LEASETIME_DEFAULT_INT = 0; // seconds = 1 day
	public static final byte[] LEASETIME_DEFAULT_4BYTES = null;

	private ConcurrentHashMap<Integer,byte[]> optionsList;

	
	
	/**
	 * Constructs a DHCPOptions-object, containing a ConcurrentHashMap to map options.
	 */
	public DHCPOptions(){
		optionsList = new ConcurrentHashMap<Integer, byte[]>(); 
	}
	
	
	
	/**
	 * Set message type discover as an option.
	 */
	public void setDiscoverOptions() {
		setOption(MESSAGETYPE, new byte[]{DHCPDISCOVER});
	}
	
	/**
	 * Set message type discover as an option and leaseTime.
	 */
	public void setDiscoverOptions(byte[] leaseTime){
		setOption(MESSAGETYPE, new byte[]{DHCPDISCOVER});
		setOption(IPLEASETIME, leaseTime);
	}

	/**
	 * Set message type discover, the requested IP and the receiving server ID as an option.
	 * @param requestedIP	The IP Address the client received from the server and wants to confirm in order to definitely obtain it.
	 */
	public void setRequestOptions(byte[] requestedIP, byte[] siaddr) {
		setOption(MESSAGETYPE, new byte[]{DHCPREQUEST});
		setOption(REQUESTEDIP, requestedIP);
		//Server identifier = the IP address of the selected server.
		setOption(SERVERID, siaddr);
	}
	
	
	public void setRequestOptions(byte[] requestedIP, byte[] siaddr, byte[] leaseTime) { //TODO deze mogelijkheid is er nu. Gebruiken of verwijderen.
		setRequestOptions(requestedIP,siaddr);
		setOption(IPLEASETIME, leaseTime);
	}
	
	public void setRenewRequestOptions() {
		optionsList.clear();
		setOption(MESSAGETYPE, new byte[]{DHCPREQUEST});
	}

	/**
	 * Set message type release, the receiving server ID and a message as an option.
	 * The string -> ASCII conversion is copied from http://stackoverflow.com/questions/5688042/how-to-convert-a-java-string-to-an-ascii-byte-array
	 */
	public void setReleaseOptions() {
		setOption(MESSAGETYPE, new byte[]{DHCPRELEASE});
		setOption(SERVERID, serverIP);
	}


	/**
	 * Sets the options for an DHCP offer message created by the server. 
	 */
	public void setOfferOptions(byte[] leaseTime){
		setOption(MESSAGETYPE, new byte[]{DHCPOFFER});
		setOption(SERVERID, null); 
		setOption(IPLEASETIME, leaseTime);
	}
	
	/**
	 * Sets the options for an DHCP offer message created by the server to default leaseTime. 
	 */
	public void setOfferOptions() {
		setOfferOptions(LEASETIME_DEFAULT_4BYTES);
	}
	

	/**
	 * Sets the options for an DHCP ACK message created by the server. 
	 */
	public void setAcknowledgeOptions(byte[] leaseTime) {
		setOption(MESSAGETYPE, new byte[]{DHCPACK});
		setOption(SERVERID, null); 
		setOption(IPLEASETIME, leaseTime);
	}
	
	public void setAcknowledgeOptions() {
		setAcknowledgeOptions(LEASETIME_DEFAULT_4BYTES);
	}

	/**
	 * Sets the options for an DHCP NAK message created by the server. 
	 */
	public void setNacknowledgeOptions() {
		setOption(MESSAGETYPE, new byte[]{DHCPNAK});
		setOption(SERVERID, null); 
	}

	/**
	 * Returns the DHCP option from the optionsList dictionary with the give optionKey.
	 * @param optionKey is the id for the options specified above (e.g. DHCP option MESSAGETYPE has id 53)
	 * @return | result = byte[] DHCPoption
	 */
	// An option consists of 1 byte, which contains the option id, followed by 1 byte for the length 
	// and only thereafter the specific option data. Thereby the data size is incremented with 2 bytes. 
	// More info @ RFC 2132: DHCP Options and BOOTP Vendor Extensions
	public byte[] getOption(int optionKey) {
		byte[] data = optionsList.get(optionKey);
			byte[] option = new byte[data.length - 2];
			for (int i = 0; i < option.length; i++)
				option[i] = data[2 + i];	
			return option;
		
	}

	/**
	 * Sets a DHCP option with id = optionKey to the given option.
	 * @param optionKey
	 * @param option
	 */
	// An option consists of 1 byte, which contains the option id, followed by 1 byte for the length 
	// and only thereafter the specific option data. Thereby the data size is incremented with 2 bytes. 
	// More info @ RFC 2132: DHCP Options and BOOTP Vendor Extensions
	public void setOption(int optionKey, byte[] option) {
		byte[] data = new byte[option.length + 2];
		data[0] = (byte) optionKey;
		data[1] = (byte) option.length;
		for (int i = 0; i < option.length; i++){
			data[2 + i] = option[i];}
		optionsList.put(optionKey, data);	

	}

	/**
	 * Convert the mapped options to a byte-format, required for DHCP communication.
	 */
	public byte[] convertToBytes() {
		int bytesLength = lengthMagicCookie + 1; // make space for magic cookie and end option
		
		for (byte[] option : optionsList.values()) {
			bytesLength += option.length;
		}
		byte[] options = new byte[bytesLength];
		for (int i = 0; i < lengthMagicCookie; i++) {
			options[i] = magicCookie[i];
		}
		int endPos = lengthMagicCookie;
		for (byte[] option : optionsList.values()) {
			for (int i = 0; i < option.length; i++) {
				options[endPos + i] = option[i];
			}
			endPos += option.length;
		}

		// set end option at the end of the optionList
		options[endPos] = (byte) 255;

		
		return options;
	}

	/**
	 * Convert a given DHCP-format byte array into a DHCPOptions map.
	 * @param options
	 */
	public void bytesToDHCPOptions(byte[] options) {
		options = removeSuperfluousZeros(options);
		int size = options.length;
		int bytes = 0;
		optionsList.clear();
		for (int i = 0; i<lengthMagicCookie; i++) {
			if (options[i] != magicCookie[i]) {
				System.out.println("A DHCP message should contain the magic cookie 99.130.83.99. We got this: "+ Arrays.copyOfRange(options, 0, 3));
				break;
			}
		}
		
		for (int i = lengthMagicCookie; i < size; i += bytes){
			bytes = 1;

			if (options[i] != (byte) 0 && options[i] != (byte) 255) {
				// size of specific option as described in length field: First byte always CODE, second byte LEN 
				int optionLength = options[i + 1]; 

				// new byte array of size in length field
				byte[] option = new byte[optionLength];
				
				// for each option data byte
				for (int j = 0; j < optionLength; j++) {
					// set data
					option[j] = options[i+2+j];
				}
				bytes = 2 + optionLength; // opcode + length + optionData.length = 2 bytes + bytes for optionData
				// set option data in this dhcpOptions object
				this.setOption(options[i], option);

			} 
			else if (options[i] == (byte) 0) { // padding, should continue reading
				System.out.println("Padding");
			}
			else if (options[i] == (byte) 255) { // end options
				assert (i == size - 1);
			}  
			else {
				System.out.println("Option not following the RFC2131 format. Code: "+ options[i]);
			}
		}
	}

		
		
	/**
	 * Remove additional (superfluous) zeros at the end of byte representation of DHCPMessage.
	 * The useful end of this message is where the end option (=255) lies.
	 * @param options
	 * @return truncated version of byte representation
	 */
	public byte[] removeSuperfluousZeros(byte [] options){
		int i;
		for (i = options.length - 1; options[i] == 0; i--);
		int newLength = i + 1;
		byte[] truncated = Arrays.copyOf(options, newLength);
		return truncated;
	}
	
	/**
	 * Returns a string representation (structured and readable) of all options. 
	 * 
	 * @return a string to print options
	 */
	public String printOptions() {
		String stringToPrint = "";
		for (byte[] option : optionsList.values()) {
			stringToPrint += printOption(option[0]);
			stringToPrint += "\n";
		}
		return stringToPrint;
	}
	
	/**
	 * Returns a string representation (structured and readable) of a single option. 
	 * @param optionID | the key of the option
	 * @return a string to print an option
	 */
	public String printOption(int optionID) {
		String stringToPrint = new String("");
		if (optionsList.get(optionID) != null) {
			byte[] option = optionsList.get(optionID);
			if (optionID == REQUESTEDIP) {
				stringToPrint = "Requested IP: ";
			} else if (optionID == MESSAGETYPE) {
				stringToPrint = "Message type: " + stringifyType(option[2]);
			} else if (optionID == SERVERID) {
				stringToPrint = "Server ID (IP address): ";
			} else if (optionID == IPLEASETIME ){
				stringToPrint = "IP lease time: blabla secs";
			}
		} else {
			stringToPrint = "<Empty>";
		}
		return stringToPrint;
	}
	
	/**
	 * Returns a string representation of the DHCPMessage type, corresponding to the given typeNumber
	 * @param typeNumber
	 * @return a string representation of the DHCPMessage type
	 */
	private String stringifyType(byte typeNumber) {
		String[] possibilities = {"DHCPDISCOVER","DHCPOFFER","DHCPREQUEST","ERROR: DHCPDECLINE shouldn't be used","DHCPACK","DHCPNAK","DHCPRELEASE"};
		if (typeNumber <= possibilities.length){
			return possibilities[typeNumber-1];
			}
		else {
			String error = "Unknown DHCP Message Type: " + typeNumber;
			return error;
		}
	}
	
	public ConcurrentHashMap<Integer,byte[]> getOptionsList() {
		return optionsList;
	}

}
