package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HidraSubject { 
	
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
	 */
	public static void sendDataPacket(byte[] data, String receiverIP, DatagramSocket socket, int port){
		try{
			DatagramPacket dataPack = new DatagramPacket(data, data.length, InetAddress.getByName(receiverIP), port);
			socket.send(dataPack);
		}
		catch(Exception e){
			System.out.println("HidraSubject.sendDataPacket() Exception ");
			e.printStackTrace();
		}
	}
	
	public static void sendDataPackToACS(byte[] data){ 
		sendDataPacket(data, acsIP, socketForACS, HidraConfig.getAcsPortForCommWithSubject());
	}
	
	public static void sendDataPackToResource(byte[] data){ 
		sendDataPacket(data, resourceIP, socketForResource, SUBJECT_TO_RESOURCE_PORT);
	}

	public static DatagramPacket receiveDataPacket(DatagramSocket socket){
		byte[] buffer = new byte[HidraMessage.MAX_BYTE_SIZE]; 
		DatagramPacket dataPack = new DatagramPacket(buffer, buffer.length);
		try{
			socket.receive(dataPack); 
		}
		catch(Exception e){
			System.out.println("HidraSubject.receiveDataPacket() Exception");
			e.printStackTrace();
		}
		return dataPack;
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
	
	/**
	 * Current functionality: 
	 * 	Execute empty Hidra protocol when a user indicates this through the console by pressing Enter 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Start Subject");
		
		// Initialize Datagram Socket
		HidraSubject acs = new HidraSubject();
		
		while(true) {
			getUserInput("Enter to send start Hidra protocol");
			
			sendDataPackToACS("HID_ANS_REQ".getBytes());
			
			DatagramPacket receivedDatagram = receiveDataPacket(socketForACS);
			byte[] actualMessage = null; 
			
			// Filter on the port the datagram was sent from 
			if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				System.out.println("Received " + (new String(actualMessage)));
				if((new String(actualMessage)).equals("HID_ANS_REP")) {
					sendDataPackToACS("HID_CM_REQ".getBytes());
					
					receivedDatagram = receiveDataPacket(socketForACS);
					// Filter on the port the datagram was sent from 
					if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
						System.out.println("Received " + (new String(actualMessage)));
						if((new String(actualMessage)).equals("HID_CM_REP")) {
							try {  
								// Because otherwise this message tends to arrive too early in hidra-r. TODO delete again? Shouldn't be necessary?
								TimeUnit.MILLISECONDS.sleep(500);
								sendDataPackToResource("HID_S_R_REQ".getBytes());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							receivedDatagram = receiveDataPacket(socketForResource);
							if (receivedDatagram.getPort() == SUBJECT_TO_RESOURCE_PORT) {
								actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
								System.out.println("Received " + (new String(actualMessage)));
								if((new String(actualMessage)).equals("HID_S_R_REP")) {
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
	}	
}

