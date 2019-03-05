package hidra;

import java.util.Random;


/**
 * This class implements a Hidra message, containing the necessary identifiers, token, keys and possibly an EBNF policy (codified with APBR codification).
 */
public class HidraMessage {

	
	// Om dit allemaal aan te passen
	// TODO kijk in paper of berichten echt deftig beschreven
	// TODO anders: code van een char om aan te geven welk bericht van het protocol het is
	// TODO maak alle velden aan die nodig zijn in bepaalde berichten en schrijf de beschrijving over van de paper
	// Maak methoden als 'isAnAnsReqMessage' alleen op een as-needed basis. Je weet dat anders veel dingen onnodig gemaakt zullen zijn
			// !! Want echt veel verspilde tijd om altijd voor te bereiden op dingen. Werk van PoC naar PoC!
	// TODO (in constructor zonder argumenten: zet al alle gemeenschappelijke delen. -> obviously)
	
	public static final int MAX_BYTE_SIZE = 1023; 
	public static final byte REQUEST = 1;
	public static final byte REPLY = 2;
	public static final byte ETHERNET10MB = 1;
	public static final byte[] zeroByte = new byte[] {0,0,0,0}; 


	private byte op;				// Message op code / message type. 1 = BOOTREQUEST, 2 = BOOTREPLY / 1 octet
	private byte htype;				// Hardware address type / 1 octet
	private byte hlen;				// Hardware address length, equal to 6 for 10mb ethernet / 1 octet
	private byte hops;				// Client sets to zero / 1 octet
	private int  xid;				// Transaction ID, randomly chosen by client / 4 octets
	private short secs;		 		// Seconds elapsed since client began address acquisition or renewal process / 2 octets
	private short flags;			// flag if client requires broadcast reply / 2 octets
	private byte[] ciaddr;			// Client IP address, filled by client, only used in BOUND, RENEW and REBINDING state / 4 octets
	private byte[] yiaddr;			// 'your' (client) IP address, filled by server and sent in DHCPOffer and DHCPAck / 4 octets
	private byte[] siaddr;			// IP address of next server, returned in DHCPOFFER, DHCPACK by server / 4 octets
	private byte[] giaddr;			// Relay agent IP address, if booting via a relay agent. / 4 octets
	private byte[] chaddr;     		// Client hardware address, filled by client (only MAC-address, 6 bytes, the rest is filled with 0's) / 16 octets 
	private byte[] sname;			// Optional server host name / 64 octets
	private byte[] file;       		// Boot file name / 128 octets
	private DHCPOptions optionsList;  // Optional parameters field / variable length

	private static Random randomXid = new Random();	// Instance of Random class, which will be used for creating a random xid. 


	/**
	 * Construct an DHCPMessage object, to be filled in differently in the stages of the protocol.
	 */
	public HidraMessage(){
		setHtype(ETHERNET10MB); 
		setHlen((byte) 6);
		setHops((byte) 0);
		setFlags((short) 0); 	// Broadcasting isn't allowed
		ciaddr = new byte[4]; 	// Is always zero in the initialization-protocol
		yiaddr = new byte[4];
		siaddr = new byte[4];
		giaddr = new byte[4];
		chaddr = new byte[16];
		sname = new byte[64];
		file = new byte[128];
		optionsList = new DHCPOptions();
	}

	/**
	 * Construct a DHCPMessage object out of a byte array.
	 */
	public HidraMessage(byte[] dataByte){
		bytesToDHCPMessage(dataByte);
	}



	/**
	 * Make a discover DHCP message. Must be converted to byte array. 
	 * @param mac 	The MAC address of client
	 * @param leaseTime	lease time as requested by client
	 * @return	discoverDHCPMessage as an array of bytes.
	 */
	public byte[] discoverDHCPMessage(byte[] mac, byte[] leaseTime){
		setOp(REQUEST); 
		setXid(randomXid.nextInt()); 
//		setSecs((short) (System.currentTimeMillis()-DHCPClient.start));
		setCiaddr(zeroByte); 
		setYiaddr(zeroByte); 
		setSiaddr(zeroByte);
		setGiaddr(zeroByte);

		for (int i = 0; i < mac.length; i++){ 	// First mac.length bytes of client hardware address field equals MAC address client
			chaddr[i] = mac[i];				 	
		}

		optionsList = new DHCPOptions();
		if (leaseTime != null){
			optionsList.setDiscoverOptions(leaseTime); 
		}
		else optionsList.setDiscoverOptions(); 

		return this.convertToBytes();

	}
	
	/**
	 * Make a discover DHCP message when no lease time is specified by the client. Must be converted to byte array.
	 * @param mac 	The MAC address of client
	 * @return discoverDHCPMessage as an array of bytes.
	 */
	public byte[] discoverDHCPMessage(byte[] mac){
		return discoverDHCPMessage(mac, null);
	}

	/**
	 * Make a request DHCP message, following the RFC 2131 p.37. Must be converted to byte array. 
	 * @param mac			The MAC address of client
	 * @param ipToRequest 	The IP Address the client received from the server and wants to confirm in order to definitely obtain it.  
	 * @param transactionID The number that the server gave in the offer-message in the field 'xid'.
	 * @return requestDHCPMessage as an array of bytes.
	 */
	public byte[] requestDHCPMessage(byte[] mac, HidraMessage answerOnDiscover, byte[] newLeaseTime){
		if (answerOnDiscover.getOptionsList().getOption(DHCPOptions.MESSAGETYPE)[0] == DHCPOptions.DHCPOFFER){
			byte[] ipToRequest = answerOnDiscover.getYiaddr();
			setOp(REQUEST);
//			setSecs((short) (System.currentTimeMillis()-DHCPClient.start));
			setCiaddr(zeroByte); 
			setYiaddr(zeroByte);
			setSiaddr(zeroByte);
			setGiaddr(zeroByte);
	
			for (int i = 0; i < mac.length; i++){ 	// First mac.length bytes of client hardware address field equals MAC address client
				chaddr[i] = mac[i];				 	
			}
			sname = new byte[64];
			file = new byte[128];
	
			optionsList = new DHCPOptions();
			if (newLeaseTime != null){
				optionsList.setRequestOptions(ipToRequest, answerOnDiscover.getSiaddr(), newLeaseTime); 
			}
			else optionsList.setRequestOptions(ipToRequest, answerOnDiscover.getSiaddr(), answerOnDiscover.getOptionWithKey(DHCPOptions.IPLEASETIME)); 

	
			return this.convertToBytes();
		}
		else {
			System.out.println("ERROR: answerOnDiscover was not an offer");
			return null;
		}
	}
	
	public byte[] requestDHCPMessage(byte[] mac, HidraMessage answerOnDiscover){
		return requestDHCPMessage(mac, answerOnDiscover, null);
	}

	

	public byte[] renewRequestDHCPMessage(byte[] mac, byte[] ownIP){
		setOp(REQUEST);
		setXid(randomXid.nextInt()); 
//		setSecs((short) (System.currentTimeMillis()-DHCPClient.start));
		setCiaddr(ownIP); 
		setYiaddr(zeroByte);
		setSiaddr(zeroByte);
		setGiaddr(zeroByte);

		for (int i = 0; i < mac.length; i++){ 	// First mac.length bytes of client hardware address field equals MAC address client
			chaddr[i] = mac[i];				 	
		}
		sname = new byte[64];
		file = new byte[128];

		optionsList.setRenewRequestOptions(); 
		
		return this.convertToBytes();

	}

	/**
	 * Make a release DHCP message. Must be converted to byte array.
	 * @param mac	The MAC address of client
	 * @param currentIP The IP from which the client wishes to be released
	 * @return releaseDHCPMessage as an array of bytes.
	 */
	public byte[] releaseDHCPMessage(byte[] mac, byte[] ipToDisclaim){
		setOp(REQUEST);
		setXid(randomXid.nextInt());
		setSecs((short) 0);
		setCiaddr(ipToDisclaim);
		setYiaddr(zeroByte);
		setSiaddr(zeroByte);
		setGiaddr(zeroByte);
		for (int i = 0; i < mac.length; i++){ 	
			chaddr[i] = mac[i];	
		}
		sname = new byte[64];
		file = new byte[128];

		optionsList = new DHCPOptions();
		optionsList.setReleaseOptions();
		

		return this.convertToBytes();
	}


	/**
	 * Converts the current (discover)message to an offer. 
	 * @param offeredIP
	 * @return
	 */
	public byte[] offerDHCPMessage(byte[] offeredIP, byte[] serverIP, byte[] leaseTime){	
		setOp(REPLY);
		setSecs((short) 0);
		setYiaddr(offeredIP);
		setSiaddr(serverIP);
		setCiaddr(zeroByte); 	// Ciaddr is explicitly set to zero, as described in the RFC documentation

	   	sname = new byte[64]; 
	   	file = new byte[128];

	   	optionsList = new DHCPOptions();
		if (leaseTime != null){
			optionsList.setOfferOptions(leaseTime);
		}
		else optionsList.setOfferOptions();
		setOptionsList(optionsList);


		return this.convertToBytes();

	}
	
	/**
	 * Converts the current (discover)message to an offer. 
	 * @param offeredIP
	 * @return
	 */
	public byte[] offerDHCPMessage(byte[] offeredIP, byte[] serverIP){	
		return offerDHCPMessage(offeredIP, serverIP, null);
	}

	/**
	 * Converts the current (request)message to an acknowledge.
	 * @param requestedIP (sent as an option in DHCPRequest)
	 * @return	ackDHCPMessage.convertToBytes()
	 */
	public byte[] ackDHCPMessage(byte[] requestedIP,byte[] leaseTime){
		setOp(REPLY);
		setSecs((short) 0);
		setYiaddr(requestedIP);
		
		optionsList = new DHCPOptions();
		if (leaseTime != null){
			optionsList.setAcknowledgeOptions(leaseTime);
		}
		else optionsList.setAcknowledgeOptions();
		setOptionsList(optionsList);
		System.out.println(this.toString());
		return this.convertToBytes();
	}
	
	public byte[] ackDHCPMessage(byte[] requestedIP){
		return ackDHCPMessage(requestedIP, null);
	}

	/**
	 * Converts the current (request)message to an not-acknowledge.
	 * @return nakDHCPMessage.convertToBytes()
	 */
	public byte[] nakDHCPMessage(){
		setOp(REPLY);
		setSecs((short) 0);
		setCiaddr(zeroByte);
		setYiaddr(zeroByte);
		setSiaddr(zeroByte);
		
		optionsList = new DHCPOptions();
		optionsList.setNacknowledgeOptions();
		setOptionsList(optionsList);

		return this.convertToBytes();
	}

	/**
	 * Converts a message into one large byteArray.
	 * Layout of code is analog to the lay-out specified in the RFC 2131. 
	 * @return A byteArray, representing a DHCPMessage
	 */
	public byte[] convertToBytes() {
		byte[] optionData = optionsList.convertToBytes();
		int fixedLength = 236;
		int varLength = optionData.length;
		int totalLength = fixedLength + varLength;
		byte[] byteMessage = new byte[totalLength];

		//----------------------/----------------------------/---------------------------/----------------------------// 4x 1 byte 
		byteMessage[0] = getOp(); byteMessage[1] = getHtype(); byteMessage[2] = getHlen(); byteMessage[3] = getHops();
		//------------------------------------------------------------------------------------------------------------// 1x 4 bytes (int)
		for (int i=0; i < 4; i++) { byteMessage[4+i] = HidraUtility.intToByteArray(getXid())[i];}
		//---------------------------------------------------/--------------------------------------------------------// 2x 2 bytes (short) 
		for (int i=0; i < 2; i++) { byteMessage[8+i] = HidraUtility.shortToByteArray(getSecs())[i];} 
		for (int i=0; i < 2; i++) { byteMessage[10+i] = HidraUtility.shortToByteArray(getFlags())[i];}
		//---------------------------------------------------/--------------------------------------------------------// 1x X bytes (byte[X])
		for (int i=0; i < 4; i++) { byteMessage[12+i] = getCiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[16+i] = getYiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[20+i] = getSiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[24+i] = getGiaddr()[i];}
		for (int i=0; i < 16; i++) { byteMessage[28+i] = getChaddr()[i];}
		for (int i=0; i < 64; i++) { byteMessage[44+i] = getSname()[i];}
		for (int i=0; i < 128; i++) { byteMessage[108+i] = file[i];}
		for (int i=0; i < varLength; i++) {
			byteMessage[fixedLength+i] = optionData[i];
		}

		return byteMessage;
	}

	/**
	 * Adapt this DHCPMessage object to a byte array.
	 */
	public void bytesToDHCPMessage(byte[] byteArray) {

		optionsList = new DHCPOptions();

		int fixedLength = 236;
		int totalLength = byteArray.length;
		int optionsLength = totalLength - fixedLength;

		setOp(byteArray[0]);    
		setHtype(byteArray[1]); 
		setHlen(byteArray[2]);  
		setHops(byteArray[3]);  

		setXid(HidraUtility.byteArrayToInt(new byte[]{byteArray[4],byteArray[5],byteArray[6],byteArray[7]})); 	
		setSecs(HidraUtility.byteArrayToShort(new byte[]{byteArray[8],byteArray[9]})); 							
		setFlags(HidraUtility.byteArrayToShort(new byte[]{byteArray[10], byteArray[11]})); 

		setCiaddr(new byte[]{byteArray[12],byteArray[13],byteArray[14],byteArray[15]});  
		setYiaddr(new byte[]{byteArray[16],byteArray[17],byteArray[18],byteArray[19]}); 
		setSiaddr(new byte[]{byteArray[20],byteArray[21],byteArray[22],byteArray[23]});
		setGiaddr(new byte[]{byteArray[24],byteArray[25],byteArray[26],byteArray[27]}); 

		chaddr = new byte[16];
		sname = new byte[64];
		file = new byte[128];

		for (int i=0; i < 16; i++) chaddr[i] = byteArray[28+i];   //16 bytes
		for (int i=0; i < 64; i++) sname[i] = byteArray[44+i];    //64 bytes
		for (int i=0; i < 128; i++) file[i] = byteArray[108+i];   //128 bytes

		//Assumption: There are always options (to declare the message type, for example)
		byte[] options = new byte[optionsLength];
		for (int i=0; i < optionsLength; i++) {
			options[i] = byteArray[fixedLength+i];
		}

		this.optionsList.bytesToDHCPOptions(options);
	}
	
	/**
	 * Stringify HidraMessage to a structured, easy readable overview. 
	 */
	public String toString() { //TODO wrs wel handig voor hidra message ook. Maak in HidraPolicy dan ook zo'n methode (naar JSON achtige structuur)
		String printString = new String();
		String[] row = new String[10];

		row[0] =  "op: " + getOp() + " | ";
		row[0] += "htype: " + getHtype() + " | ";
		row[0] += "hlen: " + getHlen() + " | ";
		row[0] += "hops: " + getHops();
		
		row[1] = "xid: " + getXid();
		
		row[2] = "secs: " +getSecs() + " miliseconds  | flags: ";
		row[2] += getFlags() + " --> UNICAST";
		
		row[3] = "ciaddr: " + HidraUtility.printIP(getCiaddr());
		row[4] = "yiaddr: " + HidraUtility.printIP(getYiaddr());
		row[5] = "siaddr: " + HidraUtility.printIP(getSiaddr());
		row[6] = "giaddr: " + HidraUtility.printIP(getGiaddr());
		row[7] = "chaddr: ";
		row[8] = "sname: 	There is no server host name provided.";
		row[9] = "file:	No file added.";
		
		printString+="\n" + "------------------------------------------------------" + "\n";
		for(int i=0 ; i< 10 ; i ++){
			printString+= row[i] + "\n" ;
		}
		
		printString+= "OPTIONS: \n";
		printString+= getOptionsList().printOptions();
		printString+="\n" +"------------------------------------------------------" + "\n";

		return printString;
	}
	
	/**
	 * Prints the DHCPMessage, which is already converted to a readable overview in string format. 
	 * @param string
	 */
	public void printMessage(String string){
		System.out.println(string);
	}
	
	public boolean hasOptionWithKey(int key) {
		return this.getOptionsList().getOptionsList().containsKey(key);
	}
	
	public byte[] getOptionWithKey(int key) {
		return this.getOptionsList().getOption(key);
	}

	public byte getOp() {
		return op;
	}

	public byte getHtype() {
		return htype;
	}

	public byte getHlen() {
		return hlen;
	}

	public byte getHops() {
		return hops;
	}

	public int getXid() {
		return xid;
	}

	public short getSecs() {
		return secs;
	}

	public short getFlags() {
		return flags;
	}

	public byte[] getCiaddr() {
		return ciaddr;
	}

	public byte[] getYiaddr() {
		return yiaddr;
	}

	public byte[] getSiaddr() {
		return siaddr;
	}

	public byte[] getGiaddr() {
		return giaddr;
	}

	public byte[] getChaddr() {
		return chaddr;
	}

	public byte[] getSname() {
		return sname;
	}

	public byte[] getFile() {
		return file;
	}

	public DHCPOptions getOptionsList() {
		return optionsList;
	}

	public void setOp(byte op) {
		this.op = op;
	}

	public void setHtype(byte htype) {
		this.htype = htype;
	}

	public void setHlen(byte hlen) {
		this.hlen = hlen;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public void setSecs(short secs) {
		this.secs = secs;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	public void setCiaddr(byte[] ciaddr) {
		this.ciaddr = ciaddr;
	}

	public void setYiaddr(byte[] yiaddr) {
		this.yiaddr = yiaddr;
	}

	public void setSiaddr(byte[] siaddr) {
		this.siaddr = siaddr;
	}

	public void setGiaddr(byte[] giaddr) {
		this.giaddr = giaddr;
	}

	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	public void setSname(byte[] sname) {
		this.sname = sname;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public void setOptionsList(DHCPOptions optionsList) {
		this.optionsList = optionsList;
	}



}
