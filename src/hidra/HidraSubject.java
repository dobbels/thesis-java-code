package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HidraSubject { //TODO scheid config van subject en acs? of hoe anders het ook uitkomt 
	
	private static String acsIP = HidraConfig.getLocalIP();
	private static String resourceIP = HidraConfig.getResourceIP();
	public static final int SUBJECT_TO_RESOURCE_PORT = HidraConfig.getSujectToResourcePort();
	private static String subjectIP = HidraConfig.getLocalIP();
	private static DatagramSocket socketForACS = null; 
	private static DatagramSocket socketForResource = null;
	
	public static final byte[] testPacket = "Test message".getBytes(); 
//			new byte[] {4,3,2,1};
	
	public HidraSubject(){
		try{
			socketForACS = new DatagramSocket(HidraConfig.getSubjectPortForCommWithAcs());
			socketForResource = new DatagramSocket(SUBJECT_TO_RESOURCE_PORT);
		}

		catch(SocketException e) {
			System.out.println("Socket FAIL");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sending a datagram packet over the socket to the given IP.
	 * @param data		| a byteArray which contains the packet to send
	 * @param receiverIP	| a string containing the IP to which the data is send
	 */
	public static void sendDataPacket(byte[] data, String receiverIP, DatagramSocket socket, int port){
		try{
			DatagramPacket dataPack = new DatagramPacket(data, data.length, InetAddress.getByName(receiverIP), port);
			socket.send(dataPack);
//			System.out.println("Sending data to port: "+ dataPack.getPort() +" with address: "+ dataPack.getAddress());
//			System.out.println("Length of data right before it leaves: "+ dataPack.getData().length);
		}
		catch(Exception e){
			System.out.println("HidraSubject.sendDataPacket() Exception ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sending a datagram packet over the socket to the IP of the test server.
	 * @param data
	 */
	public static void sendDataPackToACS(byte[] data){ 
		sendDataPacket(data, acsIP, socketForACS, HidraConfig.getAcsPortForCommWithSubject());
	}
	
	public static void sendDataPackToResource(byte[] data){ 
		sendDataPacket(data, resourceIP, socketForResource, SUBJECT_TO_RESOURCE_PORT);
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
			System.out.println("DHCPServer.receiveDataPacket() Exception");
			e.printStackTrace();
		}
		return dataPack;
	}
	
	/**
	 * Method to ask user for answer to a question
	 */
	public static String getUserInput(String question){
		Scanner scan = new Scanner(System.in);
		System.out.println(question);
		return scan.nextLine();
	}
	
	/**
	 * Current functionality: 
	 * 	Send UDP message to ACS and catch+print response
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Start Subject");
		
		// Initialize Datagram Socket
		HidraSubject acs = new HidraSubject();
		
		// From earlier test TODO delete when general connection is achieved.
//		String address = ACSConfig.getServerIP();
//		System.out.println("Trying if " + address + " is reachable");
//		System.out.println(InetAddress.getByName(address).isReachable(10000)); // for 10 seconds
		
		while(true) {
			getUserInput("Enter to send start Hidra protocol");
			
			sendDataPackToACS("HID_ANS_REQ".getBytes());
			
			DatagramPacket receivedDatagram = receiveDataPacket(socketForACS);
			byte[] actualMessage = null; 
			
			// Filter on the port the datagram was sent from 
			if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				System.out.println("Received " + (new String(actualMessage)));
				if(actualMessage == "HID_ANS_REP".getBytes()) {
					sendDataPackToACS("HID_CM_REQ".getBytes());
					
					receivedDatagram = receiveDataPacket(socketForACS);
					// Filter on the port the datagram was sent from 
					if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
						System.out.println("Received " + (new String(actualMessage)));
						if(actualMessage == "HID_CM_REP".getBytes()) {
							sendDataPackToResource("HID_S_R_REQ".getBytes());
							receivedDatagram = receiveDataPacket(socketForResource);
							if (receivedDatagram.getPort() == SUBJECT_TO_RESOURCE_PORT) {
								actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
								System.out.println("Received " + (new String(actualMessage)));
								if(actualMessage == "HID_S_R_REP".getBytes()) {
									System.out.println("Succesful Empty Hidra protocol exchange");
								}
							} else {
								System.out.println("Error in main server: Received datagram on the wrong port: " + receivedDatagram.getPort());
							}
						}
					} else {
						System.out.println("Error in main server: Received datagram on the wrong port: " + receivedDatagram.getPort());
					}
				}
			} else {
				System.out.println("Error in main server: Received datagram on the wrong port: " + receivedDatagram.getPort());
			}
		}


		/////////////////////////////////////////////////:
		
		
//		HidraMessage message = new HidraMessage();		
//		sendDataPackToSavedIP(message.convertToBytes());
		
		//TODO use a similar method to receive reply on message. (don't worry about having multiple resource to attend to, you're writing a PoC) 
//		System.out.println("Client sent following DHCPDiscover to test server: " + message.toString());
//		HidraMessage answerOnDiscover = new HidraMessage(receiveDataPacket());
//		System.out.println("Client received following DHCPOFFER from test server: "+ answerOnDiscover.toString());
//		System.out.println("Buiding a request message and sending it to the test server\n");
//		
//		sendDataPackToSavedIP(message.convertToBytes());
//		System.out.println("Client sent following DHCPREQUEST to test server: " + message.toString());
//		HidraMessage answerOnRequest = new HidraMessage(receiveDataPacket());
//		if (DHCPUtility.areEqual(answerOnRequest.getOptionsList().getOption(DHCPOptions.MESSAGETYPE),DHCPUtility.intToByteArray(DHCPOptions.DHCPACK))) {
//			System.out.println("Client received following DHCPACK from test server: "+ answerOnRequest.toString());
//			setClientIP(DHCPUtility.printIP(answerOnRequest.getYiaddr()));
//		}
//		else {
//			System.out.println("Client received following DHCPNAK from test server: "+ answerOnRequest.toString());
//		}		
		
	}	
}

