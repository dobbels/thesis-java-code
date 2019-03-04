package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Class representing a DHCPServer.
 */
public class HidraACS {
	public static final int ACS_RESOURCE_PORT = HidraConfig.getAcsResourcePort();
//	public static final int SUBJECT_TO_ACS_PORT = HidraConfig.getSubjectToAcsPort();
//	public static final int ACS_SUBJECT_PORT = HidraConfig.getAcsToSubjectPort();

	//TODO misschien pas veel later handig, want nu reageer je normaal alleen op berichten.
	private static String resourceIP = HidraConfig.getResourceIP();
	private static String globalACSIP = HidraConfig.getGlobalACSIP(); 
	private static String subjectIP = HidraConfig.getLocalIP();
	
	private static DatagramSocket socketForResource = null;
	private static DatagramSocket socketForSubject = null;

	public static final byte[] zeroByte = new byte[] {0,0,0,0}; 
	public static final byte[] testPacket = "Test message".getBytes(); 	

	public HidraACS(){
		System.out.println("Server opens socket, both for resource and subject");
		try{
			socketForResource = new DatagramSocket(ACS_RESOURCE_PORT);
			System.out.println("Opened socket on port "+ ACS_RESOURCE_PORT);
			
			socketForSubject = new DatagramSocket(HidraConfig.getAcsPortForCommWithSubject());
			System.out.println("Opened socket on port "+ HidraConfig.getAcsPortForCommWithSubject());
		}
		catch(SocketException e){
			e.getMessage();
		}
	}

	/**
	 * Receiving a datagram packet from the socket.
	 * @return | result == dataPack
	 */
	public static DatagramPacket receiveDataPacket(){
		byte[] buffer = new byte[HidraMessage.MAX_BYTE_SIZE]; 
		DatagramPacket dataPack = new DatagramPacket(buffer, buffer.length);
		try{
			socketForResource.receive(dataPack); 
			//TODO luister op allebei de sockets wanneer je ze allebei al eens apart hebt getest
			// ZIE https://www.baeldung.com/a-guide-to-java-sockets
			//     https://www.codeproject.com/Questions/312389/Listening-on-multiple-ports-on-a-single-server
		}
		catch(Exception e){
			System.out.println("DHCPServer.receiveDataPacket() Exception");
			e.printStackTrace();
		}
		return dataPack;
	}

	/**
	 * Sending a datagram packet over the socket to the given IP.
	 * @param data		| a byteArray which contains the packet to send
	 * @param receiverIP	| a string containing the IP to which the data is send
	 */
	public static void sendDataPacket(byte[] data, String receiverIP, DatagramSocket socket, int port){
		//TODO Zet limiet op lengte van payload die je wsn in stuurt? Max 1280 voor IPv6, soms max 88 op MAC layer (dus voor volledige UDP pakket)?
				// https://www.threadgroup.org/Portals/0/documents/support/6LoWPANUsage_632_2.pdf

				
		try{
			DatagramPacket dataPack = new DatagramPacket(data, data.length, InetAddress.getByName(receiverIP), port);
			socket.send(dataPack);
//			System.out.println("Sending data to port: "+ dataPack.getPort() +" with address: "+ dataPack.getAddress());
//			System.out.println("Length of data right before it leaves: "+ dataPack.getData().length);
		}
		catch(Exception e){
			System.out.println("DHCPServer.sendDataPacket() Exception ");
			e.printStackTrace();
		}
	}

	/**
	 * Sending a datagram packet over the socket to the resource.
	 */
	public static void sendDataToResource(byte[] packet){
		sendDataPacket(packet,resourceIP, socketForResource, ACS_RESOURCE_PORT);
	}
	
	/**
	 * Sending a datagram packet over the socket to the subject.
	 */
	public static void sendDataToSubject(byte[] packet){
		sendDataPacket(packet,subjectIP, socketForSubject, HidraConfig.getSubjectPortForCommWithAcs());
	}

	/**
	 * TODO
	 * This method transforms the received  HID_ANS_REQ message from the subject into a HID_ANS_REP message, 
	 * which contains/offers ...... (or denies the access and ...) 
	 * TODO finish this text and implement the empty protocol as it is stated in the paper
	 */
	private static byte[] replyOnDiscover(HidraMessage discoverMessage){
		//Constructs a new message from the bytecode of the received discover from the client
		HidraMessage replyMessage = new HidraMessage(discoverMessage.convertToBytes());

		// Offers the client with its unique MAC address an available IP address
		if (true) {
			byte[] offerIP;
			if (replyMessage.hasOptionWithKey(DHCPOptions.IPLEASETIME)){
//				offerIP = DHCPUtility.strIPtoByteArray(getPool().claimIP(replyMessage.getChaddr(), DHCPUtility.byteArrayToInt(replyMessage.getOptionWithKey(DHCPOptions.IPLEASETIME)))); 
//				replyMessage.offerDHCPMessage(offerIP,DHCPUtility.strIPtoByteArray(serverIP), replyMessage.getOptionWithKey(DHCPOptions.IPLEASETIME));
				System.out.println("qsmldfjqsldfkjmqlsfjdmqlsjkfdmlqskfjdmlqsdjflqmsjflqmskfjd");
			}
			else{
//				offerIP = DHCPUtility.strIPtoByteArray(getPool().claimIP(replyMessage.getChaddr()));
//				replyMessage.offerDHCPMessage(offerIP,DHCPUtility.strIPtoByteArray(serverIP));
			}
			return replyMessage.convertToBytes();
		}
		else {
			System.out.println("No IP available in pool");
			return null;
		}
	}

	/**
	 * This method transforms the received HID_R_IND message from the resource into HID_R_ACK
	 * TODO of gewoon antwoorden ipv 'berichten te tranformeren'. Was bij dhcp zeker handig. Nu nog altijd? 
	 */
	private static byte[] replyOnRequest(HidraMessage requestMessage){
		byte[] requestedIP = requestMessage.getOptionsList().getOption(DHCPOptions.REQUESTEDIP);
		String strIP = DHCPUtility.printIP(requestedIP);
		ConcurrentHashMap<String, IPPair> claimedIPs = null;

		if (claimedIPs.containsKey(strIP) && 
				DHCPUtility.areEqual(claimedIPs.get(strIP).getMac(), requestMessage.getChaddr())){
			//Constructs a new message from the bytecode of the received discover from the client
			HidraMessage ackMessage = new HidraMessage(requestMessage.convertToBytes());
			if (requestMessage.hasOptionWithKey(DHCPOptions.IPLEASETIME)){
				ackMessage.ackDHCPMessage(requestedIP, requestMessage.getOptionWithKey(DHCPOptions.IPLEASETIME));
			}
			else ackMessage.ackDHCPMessage(requestedIP);
			return ackMessage.convertToBytes();
		}
		else { //No acknowledgement
			HidraMessage nackMessage = new HidraMessage(requestMessage.convertToBytes());
			return nackMessage.nakDHCPMessage(); 
		}
	}
	

	/**
	 * This method transforms the received DHCPRequest message from the client either a DHCPACK or DHCPNACK
	 * in the case that it's a RENEW-request. 
	 * @param 	renewMessage 	| The received DHCPRequest from the client, on which the server will respond.
	 * @return	result == returns a DHCPNAK or DHCPACK
	 */
	public static byte[] replyOnRenew(HidraMessage renewMessage) {  
		byte[] renewRequestedIP = renewMessage.getCiaddr();
		String strIP = DHCPUtility.printIP(renewRequestedIP);
		String check = null; // claimIP will return null if the operation didn't succeed.

		if (check != null){ 
			//Constructs a new message from the bytecode of the received discover from the client
			HidraMessage ackMessage = new HidraMessage(renewMessage.convertToBytes());
			ackMessage.ackDHCPMessage(renewRequestedIP);
			return ackMessage.convertToBytes();
		}
		else { //No acknowledgement
			HidraMessage nackMessage = new HidraMessage(renewMessage.convertToBytes());
			return nackMessage.nakDHCPMessage();
		}
	}

	/**
	 * Execute the ACS functionalities.
	 * Current aspired functionality: 
	 * 	Set up connection with Cooja WSN border router
	 * 	Send UDP message to Cooja/Contiki motes through this router
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	
	public static void main(String[] args){
		new HidraACS();
		
		// Set up connection with RPL border router
		try {
			Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");

			// Wait for connection to be set up
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (true){ //Server starts listening to see if it receives any messages
			DatagramPacket receivedDatagram = receiveDataPacket();
			byte[] actualMessage = null; 
			
			if (receivedDatagram.getPort() == ACS_RESOURCE_PORT) {
				System.out.println("Received datagram from resource with IP " + receivedDatagram.getAddress() + 
						" on port " + receivedDatagram.getPort() + " with length: " + receivedDatagram.getLength());
				
				// TODO Waar ook getLength wordt gezegd bij receive: Relevant voor jou? Note that this means that if you're calling receive() in a loop, you have to recreate the DatagramPacket each time around the loop, or reset its length to the maximum before the receive. Otherwise getLength() keeps shrinking to the size of the smallest datagram received so far.
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				System.out.println("Content of datagram: " + new String(actualMessage));
				handleResourceMessage(actualMessage);
			} 
			//TODO filter op juiste poort? of net die van subject zijde?
			else if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) { 
				System.out.println("Received datagram from subject with IP " + receivedDatagram.getAddress() + 
						" on port " + receivedDatagram.getPort() + " with content: ");
				System.out.println(receivedDatagram.getData());
			}

			else {
				System.out.println("Error in main server: Received datagram on a wrong port.");
			}

		}
	}

	/**
	 * At the moment: simply bounce back the same message.
	 * @param dataBytes
	 */
	private static void handleResourceMessage(byte[] dataBytes) {
		//TODO how about reliability? Will socket.send make sure it is resend until and ack is received?
		//TODO does the border router have a size limit? -> weird that in some case only a part of the packets arrive?
		//		TODO Internet: suggestions? Sven: suggestions?  
		// Later test: packet of length 10 is received from hidra-r, multiplied (with factor a) and sent back.
		//				if 1 =< a =< 6 
		//				if 7 =< a =< 8, then 2 of the 3 packets are received in hidra-r,
		// 					when only one packet is sent, it was always received hidra-r in 5 consecutive tests 
		//				if 9 =< a (so the data length is 90), nothing is received, 
		// 					also when only one packet is sent, so the immediate following of other packets is not a cause.
		//	Not directly to do with this, but TODO check max buffer length for simple-udp in hidra!
		
		sendDataToResource(dataBytes);
		
		//TODO voor data size tests als je er nog doet
//		int a = 9;
//		byte[] multiplied = new byte[dataBytes.length*a];
//		for (int i = 0 ; i < a ; i++) {
//			for (int j = 0 ; j < dataBytes.length ; j++) {
//				multiplied[i*dataBytes.length + j] = dataBytes[j];
//				// so that full buffer is processed in hidra-r and 
//				if (j == dataBytes.length - 1 && i < a-1) {
//					multiplied[i*dataBytes.length + j] = 32; // 32 is een spatie
//				}
//			}
//		}
//		System.out.println(new String(multiplied));
//		for (int i = 0 ; i < multiplied.length ; i++) {
//			System.out.println(multiplied[i]);
//		}
		
//		for (int i = 0 ; i < 3 ; i++) { 
//			try {
//				TimeUnit.SECONDS.sleep(1);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			sendDataToResource(multiplied);
//		}
	}
	
	/**
	 * Method to ask user for answer to a question or simply to make the user decide when something happens. 
	 * In the latter case, the user input is not used.  
	 */
	public static String getUserInput(String question){
		Scanner scan = new Scanner(System.in);
		System.out.println(question);
		return scan.nextLine();
	}

	/**
	 * Handles a given byteArray, which contains a received message. This received message has to be analyzed, in order
	 * to form a proper reply message. This method will send this reply thereafter over the socket to the client.
	 * @param dataBytes	| A byteArray containing a message which has to be converted to a new byteArray
	 * 					| which represents the answer from the server  
	 */
	private static void handling(byte[] dataBytes) {
		HidraMessage message = new HidraMessage(dataBytes);

		//Determine the type of the received message. Necessary for further operations
		byte type = message.getOptionsList().getOption(DHCPOptions.MESSAGETYPE)[0];

		if (message.getOp() == HidraMessage.REQUEST){

			if (type == DHCPOptions.DHCPDISCOVER){
				System.out.println("\nServer received following DHCPDISCOVER from client" + message.toString());
				// Server needs to create an OFFER now
				byte[] offerMessage = replyOnDiscover(message);
				if (offerMessage != null) {
					HidraMessage offerMsg = new HidraMessage(offerMessage);
					System.out.println("Server unicasts following DHCPOFFER to client" + offerMsg.toString()); 
//					sendDataToClient(offerMessage);
				}
				else {
					System.out.println("No reply message constructed");
				}
			}

			else if (type == DHCPOptions.DHCPREQUEST){
				System.out.println("\nServer received following DHCPREQUEST from client" + message.toString());
				// Server needs to create an ACK (or a NAK) now. This is done by replyOnRequest() or replyOnRenew()
				System.out.println("Server constructs ACK or NAK reply to client with MAC: "+DHCPUtility.printMACAsString(message.getChaddr()));
				byte[] replyMessage;
				if (DHCPUtility.areEqual(message.getCiaddr(), zeroByte)) // if ciaddr is zero, this message is request out of the init-phase
					replyMessage = replyOnRequest(message);
				else {
					replyMessage = replyOnRenew(message);
				}

				HidraMessage replyMsg = new HidraMessage(replyMessage);
				try{
					byte[] serverID = message.getOptionsList().getOption(DHCPOptions.SERVERID); //ServerID is the IP address of the server, specified in the message
					byte[] serverIPBytes = DHCPUtility.strIPtoByteArray("haha");
					if ((serverID != null) && (serverID[0] == serverIPBytes[0] && serverID[1] == serverIPBytes[1] && serverID[2] == serverIPBytes[2] 
							&& serverID[3] == serverIPBytes[3])){ // The server, specified in the message matches the operating server.
						System.out.println("Server unicasts following DHCPACK or DHCPNAK to client with MAC: "+DHCPUtility.printMACAsString(message.getChaddr()) + 
								replyMsg.toString());
//						sendDataToClient(replyMessage);
					}
					else {
						System.out.println("Server could not send reply to client with MAC: "+DHCPUtility.printMACAsString(message.getChaddr()));
					}
				}
				catch (NullPointerException e){
//					sendDataToClient(replyMessage);
				}
			}
			else if (type == DHCPOptions.DHCPRELEASE){
				System.out.println("\nServer received following DHCPRELEASE from client" + message.toString());
				// The clients gives back its IP address to the server
				// Server needs to remove that IP address from the list of IP addresses being in use a the IP pool. 
				String ipToDisclaim = DHCPUtility.printIP(message.getCiaddr());
//				getPool().disclaimIP(ipToDisclaim);
			}
			else {
				System.out.println("\n The DHCPServer couldn't handle the message because its MESSAGETYPE: " + type + " is unknown.");
			}
		}
		else{
			System.out.println("\n ERROR: The DHCPServer received a reply, but a request was expected!");
		}
	}

}


