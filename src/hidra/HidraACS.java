package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Class representing a Access Control Server, executing the Hidra protocol.
 */
public class HidraACS {
	public static final int ACS_RESOURCE_PORT = HidraConfig.getAcsResourcePort();
	
	private static String resourceIP = HidraConfig.getResourceIP();
	private static String subjectIP = HidraConfig.getLocalIP();
	
	private static DatagramSocket socketForResource = null;
	private static DatagramSocket socketForSubject = null;

	public static final byte[] zeroByte = new byte[] {0,0,0,0}; 
	public static final byte[] testPacket = "Test message".getBytes(); 	
	
	public static final HidraPolicy testPolicy = new HidraPolicy();

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
	 * Receiving a datagram packet from a given socket.
	 * @return | result == dataPack
	 */
	public static DatagramPacket receiveDataPacket(DatagramSocket socket){
		byte[] buffer = new byte[HidraMessage.MAX_BYTE_SIZE]; 
		DatagramPacket dataPack = new DatagramPacket(buffer, buffer.length);
		try{
			socket.receive(dataPack); 
		}
		catch(Exception e){
			System.out.println("HidraACS.receiveDataPacket() Exception");
			e.printStackTrace();
		}
		return dataPack;
	}

	/**
	 * Send a datagram packet over the given socket to the given IP and port.
	 */
	public static void sendDataPacket(byte[] data, String receiverIP, DatagramSocket socket, int port){
		//TODO Zet limiet op lengte van payload die je wsn in stuurt? Max 1280 voor IPv6, soms max 88 op MAC layer (dus voor volledige UDP pakket)?
				// https://www.threadgroup.org/Portals/0/documents/support/6LoWPANUsage_632_2.pdf
		try{
			DatagramPacket dataPack = new DatagramPacket(data, data.length, InetAddress.getByName(receiverIP), port);
			socket.send(dataPack);
			System.out.println("ACS sent datagram with UDP payload " + (new String(data)) + " of length " + dataPack.getLength());
		}
		catch(Exception e){
			System.out.println("HidraACS.sendDataPacket() Exception ");
			e.printStackTrace();
		}
	}

	/**
	 * Limit on packet length in one test: 84 bytes. Packets longer than that don't get forwarded by the border router, probably because the maximum
	 * 802.15.4 link layer length is 127 bytes. This means that is this test MAC headers + IP headers + UDP header were 127-84 = 43 bytes. 
	 */
	public static void sendDataToResource(byte[] packet){
		sendDataPacket(packet,resourceIP, socketForResource, ACS_RESOURCE_PORT);
	}
	
	public static void sendDataToSubject(byte[] packet){
		sendDataPacket(packet,subjectIP, socketForSubject, HidraConfig.getSubjectPortForCommWithAcs());
	}	

	/**TODO
	 * Over het algemeen te doen bij elke protocolstap:
	 * 
	 * Je krijgt byte array. Vorm een HidraMessage met 
	 * DHCPMessage message = new DHCPMessage(dataBytes);
	 * 
	 * Doe checks met hulp van HidraMessage macro en stel nieuw HidraMessage op
	 * 	waarbij je impliciet dingen hergebruikt uit vorige bericht als dat zo uitkomt
	 *  evt. een NACK wanneer fout in protocol/subject krijgt geen access, ...
	 *  
	 *  (Denk eraan dat isEqual voor byte arrays wel handig is in HidraUtility + dat je daar nog wel handig methoden kan verzinnen) 
	 */

	
	/**
	 * Execute the ACS functionalities.
	 * Only one thread is listening for incoming Subject requests and from there on executing the Hidra protocol. 
	 * As for now, the ANS and CM are not implemented separately. 
	 * This implementation is kept to the basics, because it is not the bottleneck of this thesis. 
	 * Only when multiple resources and subjects are introduced, should the server start listening for messages continuously and keep more state.
	 * When accountability messages are included, an extra thread will be introduced to listen for exchanged initiated by the resource.  
	 */
	public static void main(String[] args){
		new HidraACS();
		
		try {
			// Set up connection with RPL border router
			Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");

			// Wait for connection to be set up
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Sending test-policy-instances 
		sendDataToResource(testPolicy.codify());
		
//		while (true){
//			//Server starts listening for messages from a Subject
//			DatagramPacket receivedDatagram = receiveDataPacket(socketForSubject);
//			byte[] actualMessage = null; 
//			
//			if (receivedDatagram.getPort() == HidraConfig.getSubjectPortForCommWithAcs()) {
//				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
//				System.out.println("Received " + (new String(actualMessage)));
//				if((new String(actualMessage)).equals("HID_ANS_REQ")) {
//					sendDataToSubject("HID_ANS_REP".getBytes());
//					receivedDatagram = receiveDataPacket(socketForSubject); 
//					if (receivedDatagram.getPort() == HidraConfig.getSubjectPortForCommWithAcs()) { 
//						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
//						System.out.println("Content of datagram: " + new String(actualMessage));
//						if((new String(actualMessage)).equals("HID_CM_REQ")) {
//							sendDataToResource("HID_CM_IND".getBytes());							
//							receivedDatagram = receiveDataPacket(socketForResource);
//							if (receivedDatagram.getPort() == ACS_RESOURCE_PORT) {
//								actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
//								System.out.println("Content of datagram: " + new String(actualMessage));
//								if((new String(actualMessage)).equals("HID_CM_IND_REQ")) {
//									sendDataToResource("HID_CM_IND_REP".getBytes());
//									sendDataToSubject("HID_CM_REP".getBytes());
//								}
//							} else {
//								System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
//							}
//						} else {
//							System.out.println("Instead of HID_CM_REQ, received: " + new String(actualMessage) + " with length " + actualMessage.length);
//						}
//					} else {
//						System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
//					}
//				} else {
//					System.out.println("Instead of HID_ANS_REQ, received: " + new String(actualMessage) + " with length " + actualMessage.length);
//				}
//			} else {
//				System.out.println("Error in main server: Received datagram on the wrong port: " + receivedDatagram.getPort());
//			}
//		}
	}
	
	/**
	 * Method to ask user for answer to a question or simply to make the user control when something happens, by asking to 'Enter to continue'. 
	 * In the latter case, the user input is not used.  
	 */
	public static String getUserInput(String question){
		Scanner scan = new Scanner(System.in);
		System.out.println(question);
		return scan.nextLine();
	}
	
}


