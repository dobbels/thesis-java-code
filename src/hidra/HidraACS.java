package hidra;

import hidra.HidraUtility.*;

import java.beans.Expression;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import encryption.Alice;
import encryption.AliceContext;

import message.HidraAnsReq;
import message.HidraBlacklistMessage;
import message.HidraCmIndRepMessage;
import message.HidraACSResourceMessage;
import message.HidraPolicyProvisionMessage;


/**
 * Class representing a Access Control Server, executing the Hidra protocol.
 */
public class HidraACS {
	public static final int ACS_RESOURCE_PORT = HidraConfig.getAcsResourcePort();
	public static final int ACS_SUBJECT_PORT = HidraConfig.getAcsSubjectPort();
	
	private static String resourceIP = HidraConfig.getResourceIP();
	
	private static DatagramSocket socketForResource = null;
	private static DatagramSocket socketForSubject = null;

	public static final byte zeroByte = 0; 
	public static final byte[] testPacket = "Test message".getBytes(); 	
	
	public static final char[] Kcm = { 0x15, 0x15,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final char[] Ks = { 0x7e, 0x2b,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final char[] Kr =  { 0x2b,  0x7e,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final byte[] Initial_Vector = { 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f };
	public static AliceContext ctx = new AliceContext(AliceContext.Algorithm.AES, AliceContext.Mode.CTR, 
			AliceContext.Padding.NO_PADDING, AliceContext.KeyLength.BITS_128, 
			AliceContext.Pbkdf.NONE, AliceContext.MacAlgorithm.NONE, 
			16, AliceContext.GcmTagLength.BITS_128, 10000);
	
	public static HashMap<Integer, HidraSubjectsSecurityProperties> properties = new HashMap<>();
	
	public HidraACS(){
		System.out.println("Server opens socket, both for resource and subject");
		try{
			socketForResource = new DatagramSocket(ACS_RESOURCE_PORT);
			System.out.println("Opened socket on port "+ ACS_RESOURCE_PORT);
			
			socketForSubject = new DatagramSocket(ACS_SUBJECT_PORT);
			System.out.println("Opened socket on port "+ ACS_SUBJECT_PORT);
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
		byte[] buffer = new byte[1023]; //TODO normally never a packet longer than 127 bytes?  Variabele in HidraMessage, zodat zelfde hier als in Subject?
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
		if (packet.length > 84) {
			System.out.println("Length of data packet is: " + packet.length + ".");
			System.out.println("This will probably be too much too handle in one datagram for the 802.15.4 network.");
		}
		sendDataPacket(packet,resourceIP, socketForResource, ACS_RESOURCE_PORT);
	}
	
	public static void sendDataToSubject(byte[] packet, int subjectId){
		sendDataPacket(packet,HidraConfig.getSubjectIP(subjectId), socketForSubject, ACS_SUBJECT_PORT);
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

	//Construct local representation of policy instance 4 out of the Hidra feasibility paper, p. 12. 
	private static HidraPolicy constructInstanceSample4() {
		// r2e1
		ArrayList<HidraAttribute> e1inputset = new ArrayList<>();
		e1inputset.add(new HidraAttribute(
				AttributeType.SYSTEM_REFERENCE, 
				HidraUtility.getId(HidraUtility.systemRereferences, "bios_upgrades")));
		e1inputset.add(new HidraAttribute(
				AttributeType.BYTE, 
				(byte) 3));
		
		HidraExpression r2e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "<"), e1inputset); 
		
		// r2e2
		ArrayList<HidraAttribute> e2inputset = new ArrayList<>();
		e2inputset.add(new HidraAttribute(
				AttributeType.REQUEST_REFERENCE, 
				HidraUtility.getId(HidraUtility.requestRereferences, "roles")));
		e2inputset.add(new HidraAttribute("admin"));
		
		HidraExpression r2e2 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "contains"), e2inputset); 
		
		// r2e3
		ArrayList<HidraAttribute> e3inputset = new ArrayList<>();
		e3inputset.add(new HidraAttribute(
				AttributeType.SYSTEM_REFERENCE, 
				HidraUtility.getId(HidraUtility.systemRereferences, "onMaintenance")));
		e3inputset.add(new HidraAttribute(
				AttributeType.LOCAL_REFERENCE, 
				(byte) 0));
		e3inputset.add(new HidraAttribute(
				AttributeType.LOCAL_REFERENCE, 
				(byte) 1));
		
		HidraExpression r2e3 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "isTrue"), e3inputset); 
		
		// rule 2 obligation
		HidraAttribute r2o1att = new HidraAttribute(AttributeType.SYSTEM_REFERENCE, 
				HidraUtility.getId(HidraUtility.systemRereferences, "bios_upgrades"));
		ArrayList<HidraAttribute> o2inputset = new ArrayList<>();
		o2inputset.add(r2o1att);
				
		Effect r2fulfillOn = Effect.DENY;
		HidraObligation r2o1 = new HidraObligation(
				new HidraExpression(HidraUtility.getId(HidraUtility.taskRereferences, "++"), o2inputset), 
				r2fulfillOn);
		
		// rule 2
		ArrayList<HidraExpression> r2expressions = new ArrayList<>();
		r2expressions.add(r2e1);
		r2expressions.add(r2e2);
		r2expressions.add(r2e3);
		ArrayList<HidraObligation> r2obligations = new ArrayList<>();
		r2obligations.add(r2o1);
		HidraRule r2 = new HidraRule((byte) 1, Effect.PERMIT, zeroByte, zeroByte, zeroByte, null, r2expressions, r2obligations);
		
		// rule 1 task
		HidraAttribute r1att = new HidraAttribute(AttributeType.SYSTEM_REFERENCE, 
				HidraUtility.getId(HidraUtility.systemRereferences, "onMaintenance"));
		ArrayList<HidraAttribute> o1inputset = new ArrayList<>();
		o1inputset.add(r1att);
		
		// rule 1 obligations
		HidraExpression r1o1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.taskRereferences, "activate"), o1inputset);		
		Effect r1fulfillOn = Effect.DENY;
		HidraObligation r1o1 = new HidraObligation(r1o1e1, r1fulfillOn);
//		HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		HidraExpression r1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<HidraExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<HidraObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, zeroByte, zeroByte, null, r1expressions, r1obligations);
//		HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, (byte) 5, (byte) 6, HidraUtility.Action.DELETE, r1expressions, r1obligations);
		
		// policy
		ArrayList<HidraRule> rules = new ArrayList<>();
		rules.add(r1);
		rules.add(r2);
		
		return (new HidraPolicy((byte) 104, Effect.PERMIT, rules));
	}
	
	private static HidraPolicy constructPartOfInstanceSample4() {
		// rule 1 task
		HidraAttribute r1att = new HidraAttribute(AttributeType.SYSTEM_REFERENCE, 
				HidraUtility.getId(HidraUtility.systemRereferences, "onMaintenance"));
		ArrayList<HidraAttribute> o1inputset = new ArrayList<>();
		o1inputset.add(r1att);
		
		// rule 1 obligations
		HidraExpression r1o1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.taskRereferences, "activate"), o1inputset);		
		Effect r1fulfillOn = Effect.DENY;
		HidraObligation r1o1 = new HidraObligation(r1o1e1, r1fulfillOn);
//				HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		HidraExpression r1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<HidraExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<HidraObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, zeroByte, zeroByte, null, r1expressions, r1obligations);
//				HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, (byte) 5, (byte) 6, HidraUtility.Action.DELETE, r1expressions, r1obligations);
		
		// policy
		ArrayList<HidraRule> rules = new ArrayList<>();
		rules.add(r1);
		
		return (new HidraPolicy((byte) 104, Effect.PERMIT, rules));
	}
	

	private static HidraPolicy constructDemoPolicy3() {
		return (new HidraPolicy((byte) 106, Effect.PERMIT, null));
	}
	private static HidraPolicy constructDemoPolicy2() {
		return (new HidraPolicy((byte) 105, Effect.DENY, null));
	}
	
	private static HidraPolicy constructDemoPolicy() {
//		// rule 1 obligations
		HidraExpression r1o1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.taskRereferences, "log_request"), null);		
		HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
//				HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		HidraExpression r1e1 = new HidraExpression(
				HidraUtility.getId(HidraUtility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<HidraExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<HidraObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, zeroByte, zeroByte, zeroByte, HidraUtility.Action.PUT, r1expressions, r1obligations);
		
		// policy
		ArrayList<HidraRule> rules = new ArrayList<>();
		rules.add(r1);
		
		return (new HidraPolicy((byte) 104, Effect.PERMIT, rules));
	}
	
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

			// Wait to be sure the connection is set up and the WSN RPL has converged. 
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (true) {
			runHidraProtocolDemo();
		}
		
	}
	
	private static void provideResourceWithPolicy(byte subjectId) {
		HidraACSResourceMessage hm = new HidraPolicyProvisionMessage(subjectId, constructDemoPolicy().codify());
		sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
		constructDemoPolicy().prettyPrint();		
		
		DatagramPacket receivedDatagram = receiveDataPacket(socketForResource);
		if (receivedDatagram.getPort() == ACS_RESOURCE_PORT) {
			byte[] actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			System.out.println("Content of datagram: " + new String(actualMessage));
			if((new String(actualMessage)).equals("HID_CM_IND_REQ")) {
				hm = new HidraCmIndRepMessage(subjectId);
				sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
				sendDataToSubject("HID_CM_REP".getBytes(), subjectId);
			}
		} else {
			System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
		}
	}
	
	private static void sendPolicyProvisionMessage(byte subjectId) {
		HidraPolicy hp;
		if (subjectId == 5) {
			hp = constructDemoPolicy3();
//			constructDemoPolicy3().prettyPrint();
		} else if (subjectId == 4) {
			hp = constructDemoPolicy2();
//			constructDemoPolicy2().prettyPrint();
		} else {
			hp = constructDemoPolicy();
//			constructDemoPolicy().prettyPrint();
		}
		HidraACSResourceMessage hm = new HidraPolicyProvisionMessage(subjectId, hp.codify());
		sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
	}
	
	private static void runHidraProtocolDemo() {
		//Server starts listening for messages from a Subject
		DatagramPacket receivedDatagram = receiveDataPacket(socketForSubject);
		byte[] actualMessage = null; 
		
		if (receivedDatagram.getPort() == ACS_SUBJECT_PORT) {
			actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			byte subjectId = actualMessage[1];
			System.out.println("Received id: " + subjectId);
			HidraAnsReq haq = new HidraAnsReq(actualMessage);
			sendDataToSubject(HidraUtility.booleanArrayToByteArray(haq.processAndConstructReply()), subjectId);
			////////////////////IMPLEMENTED SECURITY UNTIL HERE
			receivedDatagram = receiveDataPacket(socketForSubject); 
			if (receivedDatagram.getPort() == ACS_SUBJECT_PORT) { 
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				System.out.println("Content of datagram: " + actualMessage[0]);
				if (subjectId == actualMessage[0]) {
				
				//Decoupled, because in the demo different subjects might need different policies.
					sendPolicyProvisionMessage(subjectId);
					
					receivedDatagram = receiveDataPacket(socketForResource);
					if (receivedDatagram.getPort() == ACS_RESOURCE_PORT) {
						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
						System.out.println("Content of datagram: " + new String(actualMessage));
						if((new String(actualMessage)).equals("HID_CM_IND_REQ")) {
							HidraCmIndRepMessage hm = new HidraCmIndRepMessage(subjectId);
							sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
							// To be sure the subject doesn't send the access request too early
							try {TimeUnit.MILLISECONDS.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
							sendDataToSubject("HID_CM_REP".getBytes(), subjectId);
							System.out.println("End of hidra protocol with subject " + subjectId);
						}
					} else {
						System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
					}
				} else {
					System.out.println("Instead of message from same subject, received: " + new String(actualMessage) + " with length " + actualMessage.length);
				}
			} else {
				System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
			}
		} else {
			System.out.println("Instead of HID_ANS_REQ, received: " + new String(actualMessage) + " with length " + actualMessage.length);
		}
	}
	
	public static void blackListSubject(byte subjectId) {
		//Update policy for subject | Handle one test at a time, because resource handles only 1 subject
//		getUserInput("Enter to blacklist the subject");
		HidraBlacklistMessage hbm = new HidraBlacklistMessage(subjectId);
		sendDataToResource(HidraUtility.booleanArrayToByteArray(hbm.constructBoolMessage()));
		
		DatagramPacket receivedDatagram = receiveDataPacket(socketForResource);
		if (receivedDatagram.getPort() == ACS_RESOURCE_PORT) {
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


