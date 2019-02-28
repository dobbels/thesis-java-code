package hidra;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class representing a DHCPClient
 */
public class DHCPClient {

	private static String testIP = ACSConfig.getServerIP();
	public static final int SERVER_PORT = ACSConfig.getServerPort();		
	public static final int CLIENT_PORT = ACSConfig.getClientPort(); 
	private static String MAC = ACSConfig.getMAC();
	private static String clientIP;
	private static int leaseTime = ACSConfig.getStandardLeaseTime();
	private static Date endLeaseTime = new Date();
	private static DatagramSocket socket = null;
	public static long start;
	private static boolean useDefaultLeaseTime = true;

	// running timer task as background thread
	private static Timer timer = new Timer(true);
	private static boolean timerAlive = false;

	/**
	 * Initialize a DHCPClient. 
	 * A timer 'Start' is started. This timer which is used for the the 'secs' field in a DHCPMessage, which is set only by the DHCPClient. 
	 */

	public DHCPClient(){
		start = System.currentTimeMillis();
		System.out.println("Trying to connect to DHCPServer at IP = "+testIP+" on port = "+SERVER_PORT);

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
	public static void sendDataPacket(byte[] data, String serverIP){

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
	public static void sendDataPackToTestServer(byte[] data){
		sendDataPacket(data,testIP);
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
	 * Method to ask user if he wants the server to request a lease time different from the default lease time
	 */
	public static void askLeaseTime(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Do you want to ask the server for an IP with the default lease time = "+(ACSConfig.getStandardLeaseTime()/1000)
				+ " sec?\nYes (Y) / No (N): ");
		String answer = scan.nextLine();
		switch (answer){
		case "Y":
			break;
		case "N": 
			useDefaultLeaseTime = false;
			System.out.println("Fill in the amount of seconds you want to obtain for the IP lease time: ");
			int answer2 = scan.nextInt();
			newLeaseTime = DHCPUtility.intToByteArray(answer2, 4);
			break;
		}
	}

	/**
	 * Asks the user if he immediately wants to release its IP. 
	 */
	public static void askToRelease(DHCPMessage ackMessage, byte[] mac){
		Scanner scan = new Scanner(System.in);
		System.out.println("Do you want to release your IP? Yes (Y) / No (N): ");
		System.out.println("P.S. If 'No', you will get the default Lease Time when 3/4 of your original lease time is expired. (Demo)");
		String answer = scan.nextLine();
		switch (answer){
		    case "Y": 
		    		DHCPMessage release = new DHCPMessage();
		    		release.releaseDHCPMessage(mac, ackMessage.getYiaddr());
		    		sendDataPackToTestServer(release.convertToBytes());
		    		setClientIP(null);
		    		System.out.println("Client sent following DHCPRELEASE to test server: "+ release.toString());
		    		killTimer(); // no point in checking lease time, if there is no IP.
		    		socket.close();
		            break;
		    case "N":
//		    		killTimer();		//TODO
	    			System.out.println("Client stays active to check its IP lease time validity.");
		            break;
		    default :
		             System.out.println("invalid choice")   ;
		             askToRelease(ackMessage, mac);
		    }
	}

	/**
	 * This method is for testing the correct working of the client with the test server 
	 * provided by the KULeuven CW department. 
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String args[]) throws InterruptedException{
		DHCPClient client = new DHCPClient();
		DHCPMessage message = new DHCPMessage();
		byte[] mac = DHCPUtility.macToByteArray(MAC);
		askLeaseTime();
		System.out.println("Buiding a discover message and sending it to the test server");
		if (!useDefaultLeaseTime){
			message.discoverDHCPMessage(mac, newLeaseTime);
		}
		else {message.discoverDHCPMessage(mac);}
		sendDataPackToTestServer(message.convertToBytes());
		System.out.println("Client sent following DHCPDiscover to test server: " + message.toString());
		DHCPMessage answerOnDiscover = new DHCPMessage(receiveDataPacket());
		System.out.println("Client received following DHCPOFFER from test server: "+ answerOnDiscover.toString());
		System.out.println("Buiding a request message and sending it to the test server\n");
		//message.requestDHCPMessage(mac, answerOnDiscover); //TODO Uncomment for testing newLeaseTime on own PC
		message.requestDHCPMessage(mac, answerOnDiscover,newLeaseTime); //TODO Uncomment for testing IP lease with default lease time @ lab
		sendDataPackToTestServer(message.convertToBytes());
		System.out.println("Client sent following DHCPREQUEST to test server: " + message.toString());
		DHCPMessage answerOnRequest = new DHCPMessage(receiveDataPacket());
		if (DHCPUtility.areEqual(answerOnRequest.getOptionsList().getOption(DHCPOptions.MESSAGETYPE),DHCPUtility.intToByteArray(DHCPOptions.DHCPACK))) {
			System.out.println("Client received following DHCPACK from test server: "+ answerOnRequest.toString());
			setClientIP(DHCPUtility.printIP(answerOnRequest.getYiaddr()));
			//setLeaseTime(DHCPUtility.byteArrayToInt(answerOnRequest.getOptionsList().getOption(DHCPOptions.IPLEASETIME))); //TODO Uncomment for testing newLeaseTime on own PC
			setLeaseTime(DHCPUtility.byteArrayToInt(newLeaseTime));	//TODO Uncomment for testing IP lease with default lease time @ lab
			startTimer(); // start regulating lease time when ip is assigned.
			askToRelease(answerOnRequest,mac);
		}
		else {
			System.out.println("Client received following DHCPNAK from test server: "+ answerOnRequest.toString());
		}
		while (timerAlive) { // main has to stay awake during the timer checks
			Thread.sleep(1000);
		}
	}

	/**
	 * Checks if any lease times have run out. 
	 * If so, deletes corresponding IPs from the claimedIP-map.
	 */
	public static void checkLeaseTime() {
		Date now = new Date();
		Date renewLease = new Date();
		renewLease.setTime(getEndLeaseTime().getTime() - (getLeaseTime()*1000/4)); // Renew if over 3/4 of lease time
		if (now.after(renewLease)) {
			renewLease();
		}
	}

	/**
	 *  Renews lease in case of a expired lease time.
	 */
	public static void renewLease() {
		if (socket.isClosed()) {
			try{
				socket = new DatagramSocket(CLIENT_PORT);
			}

			catch(SocketException e) {
				System.out.println("Socket FAIL");
				e.printStackTrace();
			}
		}
		DHCPMessage message = new DHCPMessage();
		byte[] mac = DHCPUtility.macToByteArray(MAC);
		message.renewRequestDHCPMessage(mac, DHCPUtility.strIPtoByteArray(getClientIP()));
		sendDataPackToTestServer(message.convertToBytes());
		System.out.println("Client sent following DHCPREQUEST to test server: " + message.toString());
		DHCPMessage answerOnRequest = new DHCPMessage(receiveDataPacket());
		if (DHCPUtility.areEqual(answerOnRequest.getOptionsList().getOption(DHCPOptions.MESSAGETYPE),DHCPUtility.intToByteArray(DHCPOptions.DHCPACK))) {
			System.out.println("Client received following DHCPACK from test server: "+ answerOnRequest.toString());
			setClientIP(DHCPUtility.printIP(answerOnRequest.getYiaddr()));
			setLeaseTime(DHCPUtility.byteArrayToInt(answerOnRequest.getOptionsList().getOption(DHCPOptions.IPLEASETIME)));

			askToRelease(answerOnRequest,mac); // steeds opnieuw na renew, zodat je weer de kans hebt om te releasen. (lijkt handig voor de demo)
		}
		else {
			if (getClientIP() != null)
				setClientIP(null);
			System.out.println("Client received following DHCPNAK from test server: "+ answerOnRequest.toString());
		}
		socket.close();
	}

	public static String getClientIP() {
		return clientIP;
	}

	private static void setClientIP(String ip) {
		clientIP = ip;
	}

	/**
	 * Return current lease time in seconds.
	 */
	public static int getLeaseTime() {
		return leaseTime;
	}

	public static Date getEndLeaseTime() {
		return endLeaseTime;
	}

	private static void setLeaseTime(int leaseT) {
		leaseTime = leaseT;
		Date now = new Date();
		endLeaseTime.setTime(now.getTime() + leaseT*1000);
	}

	/**
	 * Start the timer that checks the values of the lease times of
	 * allocated IP-addresses regularly.
	 */
	private static void startTimer() { 
		timerAlive = true;
		timer.scheduleAtFixedRate(timerTask, 0, 2*1000); // Rate = 2 seconds (in milliseconds)
	}

	private static void killTimer() {
		timerAlive = false;
		timer.cancel();
	}	
}
