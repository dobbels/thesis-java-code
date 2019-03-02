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
		sendDataPacket(data,acsIP, socketForACS, HidraConfig.getAcsPortForCommWithSubject());
	}
	
	/**
	 * Receiving a datagram packet from the socket.
	 * @return | result == dataPack
	 */
	public static DatagramPacket receiveDataPacket(){ //TODO meer dan alleen de data terug geven, omdat je meerdere contacten gaat hebben?
		byte[] data = new byte[1024]; 
		DatagramPacket packet = new DatagramPacket(data, data.length);		
		try {
			socketForACS.receive(packet);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return packet;
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
			getUserInput("Enter to send another standard package");
			
			sendDataPackToACS(testPacket);
			
			DatagramPacket receivedDatagram = receiveDataPacket();
			byte[] actualMessage = null; 
			
			if (receivedDatagram.getPort() == HidraConfig.getSubjectPortForCommWithAcs()) {
				System.out.println("Received datagram from resource with IP " + receivedDatagram.getAddress() + 
						" on port " + receivedDatagram.getPort() + " with length: " + receivedDatagram.getLength());
				
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				handleACSMessage(actualMessage);
			} 
			else {
				System.out.println("Error in main server: Received datagram on a wrong port.");
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

	private static void handleACSMessage(byte[] message) {
		System.out.println("Content of datagram: " + new String(message));
	}
	
	
}

