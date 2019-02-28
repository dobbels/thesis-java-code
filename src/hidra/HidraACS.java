package hidra;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HidraACS {
	
	private static String borderRouterIP = ACSConfig.getServerIP();
	public static final int SERVER_PORT = ACSConfig.getServerPort();		
	public static final int CLIENT_PORT = ACSConfig.getClientPort(); 
	private static String clientIP;
	private static DatagramSocket socket = null;
	public static long start;
	
	public static final byte[] testPacket = new byte[] {4,3,2,1};
	
	public HidraACS(){
		start = System.currentTimeMillis();
		System.out.println("Trying to connect to Border Router at IP = "+borderRouterIP+" on port = "+SERVER_PORT);

		try{
			socket = new DatagramSocket(CLIENT_PORT);
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
			DatagramPacket packet = new DatagramPacket(data,data.length,InetAddress.getByName(serverIP), SERVER_PORT);
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
	public static void sendDataPackToSavedIP(byte[] data){ //TODO naam: border router
		sendDataPacketTo(data,borderRouterIP);
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
	
	public static String getClientIP() {
		return clientIP;
	}

	private static void setClientIP(String ip) {
		clientIP = ip;
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
		System.out.println("Start ACS");
		
		// Set up connection with RPL border router
		
		//TODO In Terminal.java Create a Runtime to run each of the process. Get the OutputStream from the first Runtime and copy it into the InputStream from the second one.
		//TODO Feed askpass program and/or script that spits out password to the call 
		//TODO voeg toe echo "user\n" | sudo -S
		//TODO sudo -A /home/user/bin/pwd
		//TODO als je een simpel script maakt en dit uitvoert, dan gebeurt alles binnen een shell die daarna weer gesloten wordt. Is dat erg? 
		//TODO User ProcessBuilder : better interface, it uses Process itself
//					Maybe with .inheritIO
//		Terminal.execute("mkdir /home/user/thesis-code/test");
		Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");
		
		// Initialize Datagram Socket
		HidraACS acs = new HidraACS();
		
		String input = null;
		
//		HidraMessage message = new HidraMessage();		
//		sendDataPackToSavedIP(message.convertToBytes());
		
		// Wait for connection to be set up
		TimeUnit.SECONDS.sleep(5);
		
		while(true) {
			getUserInput("Enter to send another standard package; write a line to send a specific message: ");
			if (input == null) {
				sendDataPackToSavedIP(testPacket);
			} else {
				sendDataPackToSavedIP(input.getBytes());
			}
			input = null;
		}
		
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

