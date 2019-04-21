package hidra;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import message.HidraAccessRequest;
import message.HidraHidSRReq;
import message.HidraACSMessage;
import message.HidraPolicyProvisionMessage;
import message.HidraSubjectMessage;

import hidra.HidraUtility.*;

//TODO beter is om verschillende subjects te hebben met verschillende levels of access om te tonen. Dit ga je wel rechtstreeks in Contiki doen! Time's up  
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
	
	public static void sendDataToACS(byte[] data){ 
		sendDataPacket(data, acsIP, socketForACS, HidraConfig.getAcsPortForCommWithSubject());
	}
	
	public static void sendDataToResource(byte[] data){ 
		sendDataPacket(data, resourceIP, socketForResource, SUBJECT_TO_RESOURCE_PORT);
	}

	public static DatagramPacket receiveDataPacket(DatagramSocket socket){
		byte[] buffer = new byte[1023]; 
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
	
	public static void runHidraProtocol(byte id) {
		byte[] idArray = {id};
		sendDataToACS(idArray);
		
		DatagramPacket receivedDatagram = receiveDataPacket(socketForACS);
		byte[] actualMessage = null; 
		
		// Filter on the port the datagram was sent from 
		if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
			actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			System.out.println("Received " + (new String(actualMessage)));
			if((new String(actualMessage)).equals("HID_ANS_REP")) {
				sendDataToACS("HID_CM_REQ".getBytes());
				
				receivedDatagram = receiveDataPacket(socketForACS);
				// Filter on the port the datagram was sent from 
				if (receivedDatagram.getPort() == HidraConfig.getAcsPortForCommWithSubject()) {				
					actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
					System.out.println("Received " + (new String(actualMessage)));
					if((new String(actualMessage)).equals("HID_CM_REP")) {
						try {  
							// Because otherwise this message tends to arrive too early in hidra-r. TODO delete again? Shouldn't be necessary?
							TimeUnit.MILLISECONDS.sleep(500);
							
							HidraSubjectMessage hm = new HidraHidSRReq(id);
							sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
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
	
	public static void processAckNackResponseFromResource() {
		DatagramPacket receivedDatagram = receiveDataPacket(socketForResource);
		if (receivedDatagram.getPort() == SUBJECT_TO_RESOURCE_PORT) {
			byte[] actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			if (actualMessage[0] == 0) {
				System.out.println("Request denied");
			} else if (actualMessage[0] == 1) {
				System.out.println("Request successful");
			} else {
				System.out.println("Unknown answer to request");
			}
		} else {
			System.out.println("Error in main server: Received datagram on the wrong port: " + receivedDatagram.getPort());
		}
	}
	
	public static void switchRemoteLightOn(byte id) {
		HidraExpression exp = new HidraExpression(HidraUtility.getId(HidraUtility.systemRereferences, "switch_light_on"), null);
		HidraAccessRequest haq = new HidraAccessRequest(id, Action.PUT, exp);
		sendDataToResource(HidraUtility.booleanArrayToByteArray(haq.constructBoolMessage()));

		processAckNackResponseFromResource();
	}
	
	public static void switchRemoteLightOff(byte id) {
		HidraExpression exp = new HidraExpression(HidraUtility.getId(HidraUtility.systemRereferences, "switch_light_off"), null);
		HidraAccessRequest haq = new HidraAccessRequest(id, Action.PUT, exp);
		sendDataToResource(HidraUtility.booleanArrayToByteArray(haq.constructBoolMessage()));

		processAckNackResponseFromResource();
	}
	
	/**
	 * Current functionality: 
	 * 	Execute empty Hidra protocol when a user indicates this through the console by pressing Enter 
	 */
	public static void main(String[] args) {
		System.out.println("Start Subject");
		
		// Initialize Datagram Socket
		HidraSubject acs = new HidraSubject();
		
//		getUserInput("Enter to send start Hidra protocol");

		byte id = 1;
		byte id2 = 2;
		byte id3 = 3;

		runHidraProtocol(id);
		runHidraProtocol(id2);
		runHidraProtocol(id3);

		// For demo purposes: Request with non-existent id; should be denied
//		getUserInput("Enter to request access (with wrong id)");
//		switchRemoteLightOn((byte) 27);

		while(true) {
			getUserInput("Enter to request access");
			
			switchRemoteLightOn(id);
			
			getUserInput("Enter to request access");
			
			switchRemoteLightOff(id);
			
			getUserInput("Enter to request access");
			
			switchRemoteLightOn(id2);
			
			getUserInput("Enter to request access");
			
			switchRemoteLightOff(id2);
			
			getUserInput("Enter to request access");
			
			switchRemoteLightOn(id3);
			
			getUserInput("Enter to request access");
			
			switchRemoteLightOff(id3);
		}
	}	
}

