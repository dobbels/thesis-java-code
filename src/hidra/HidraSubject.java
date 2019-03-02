package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HidraSubject { //TODO scheid config van subject en acs? of hoe anders het ook uitkomt 
	
	private static String acsIP = HidraConfig.getLocalIP();
	public static final int ACS_PORT = HidraConfig.getSubjectToAcsPort(); 
	public static final int SUBJECT_PORT = HidraConfig.getAcsToSubjectPort(); 
	private static String clientIP = HidraConfig.getLocalIP(); //TODO maak meteen socket met deze ip?! zei server 
	private static DatagramSocket socket = null; 
	public static long start; 
	
	public static final byte[] testPacket = "Test message".getBytes(); 
//			new byte[] {4,3,2,1};
	
	public HidraSubject(){
		start = System.currentTimeMillis();
		System.out.println("Trying to connect to WSN using socket on port = "+SUBJECT_PORT);

		try{
			socket = new DatagramSocket(SUBJECT_PORT);
		}

		catch(SocketException e) {
			System.out.println("Socket FAIL");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sending a datagram packet over the socket to the given IP.
	 * @param data		| a byteArray which contains the packet to send
	 * @param serverIP	| a string containing the IP to which the data is send
	 */
	public static void sendDataPacketTo(byte[] data, String serverIP){
		
		try{
			DatagramPacket packet = new DatagramPacket(data,data.length,InetAddress.getByName(serverIP) , ACS_PORT);
			System.out.println("Sending packet to address " + InetAddress.getByName(serverIP) + " on port " + ACS_PORT + " with data " + data + " on socket with"); 
			System.out.println("local port: " + socket.getLocalPort());
//			System.out.println("port: " + socket.getPort());
//			System.out.println("ip: " + socket.getInetAddress());
//			System.out.println("local address: " + socket.getLocalAddress());
//			System.out.println("local socket address: " + socket.getLocalSocketAddress());
//			System.out.println("remote socket address: " + socket.getRemoteSocketAddress());
//			System.out.println("reuse address: " + socket.getReuseAddress());
			socket.send(packet);	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Sending a datagram packet over the socket to the IP of the test server.
	 * @param data
	 */
	public static void sendDataPackToServerIP(byte[] data){ //TODO naam: border router
		sendDataPacketTo(data,acsIP);
	}
	
	/**
	 * Receiving a datagram packet from the socket.
	 * @return | result == dataPack
	 */
	public static byte[] receiveDataPacket(){ 
		byte[] data = new byte[1024]; 
		DatagramPacket packet = new DatagramPacket(data, data.length);		
		try {
			socket.receive(packet);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		return packet.getData();
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
	 * Execute the ACS functionalities.
	 * Current aspired functionality: 
	 * 	Set up connection with Cooja WSN border router
	 * 	Send UDP message to Border Router
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
			sendDataPackToServerIP(testPacket);
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

