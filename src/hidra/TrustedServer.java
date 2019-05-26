package hidra;

import hidra.Utility.*;
import hidra.Policy.Effect;
import hidra.PolicyAttribute.AttributeType;
import hidra.PolicyRule.Action;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import encryption.AliceContext;

import message.HidraAnsReq;
import message.HidraBlacklistMessage;
import message.HidraCmIndReq;
import message.HidraCmRep;
import message.HidraCmReq;

/**
 * Class representing a Access Control Server, executing the Hidra protocol.
 */
public class TrustedServer {
	public static final int SERVER_RESOURCE_PORT = Utility.getServerResourcePort();
	public static final int SERVER_SUBJECT_PORT = Utility.getServerSubjectPort();
	
	public static String resourceIP = Utility.getResourceIP();
	
	public static DatagramSocket socketForResource = null;
	public static DatagramSocket socketForSubject = null;

	public static final byte zeroByte = 0; 
	public static final byte[] testPacket = "Test message".getBytes(); 	
	
	public static final char[] Kcm = { 0x15, 0x15,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final char[] Kr =  { 0x2b,  0x7e,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final byte[] Initial_Vector = { 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f };
	public static AliceContext ctx = new AliceContext(AliceContext.Algorithm.AES, AliceContext.Mode.CTR, 
			AliceContext.Padding.NO_PADDING, AliceContext.KeyLength.BITS_128, 
			AliceContext.Pbkdf.NONE, AliceContext.MacAlgorithm.NONE, 
			16, AliceContext.GcmTagLength.BITS_128, 10000);
	
	public static HashMap<Byte, SubjectSecurityProperties> securityProperties = new HashMap<>();
	
	public static boolean isUniquePseudonym(byte[] pseudonym) {
		for (SubjectSecurityProperties hsp : securityProperties.values()) {
			if (hsp.getPseudonym()[1] == pseudonym[1]) {
				return false;
			}
		}
		return true;
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
			System.out.println("HidraServer.receiveDataPacket() Exception");
			e.printStackTrace();
		}
		return dataPack;
	}

	/**
	 * Send a datagram packet over the given socket to the given IP and port.
	 * 
	 * Limit on packet length in one test: 84 bytes. Packets longer than that don't get forwarded by the border router, probably because the maximum
	 * 802.15.4 link layer length is 127 bytes. This means that is this test MAC headers + IP headers + UDP header were 127-84 = 43 bytes. 
	 */
	public static void sendDataPacket(byte[] data, String receiverIP, DatagramSocket socket, int port){
		try{
			DatagramPacket dataPack = new DatagramPacket(data, data.length, InetAddress.getByName(receiverIP), port);
			socket.send(dataPack);
//			System.out.println("Server sent datagram with UDP payload of length " + dataPack.getLength());
		}
		catch(Exception e){
			System.out.println("HidraServer.sendDataPacket() Exception ");
			e.printStackTrace();
		}
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
	private static Policy constructInstanceSample4() {
		// r2e1
		ArrayList<PolicyAttribute> e1inputset = new ArrayList<>();
		e1inputset.add(new PolicyAttribute(
				AttributeType.SYSTEM_REFERENCE, 
				Utility.getId(Utility.systemRereferences, "bios_upgrades")));
		e1inputset.add(new PolicyAttribute(
				AttributeType.BYTE, 
				(byte) 3));
		
		PolicyExpression r2e1 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "<"), e1inputset); 
		
		// r2e2
		ArrayList<PolicyAttribute> e2inputset = new ArrayList<>();
		e2inputset.add(new PolicyAttribute(
				AttributeType.REQUEST_REFERENCE, 
				Utility.getId(Utility.requestRereferences, "roles")));
		e2inputset.add(new PolicyAttribute("admin"));
		
		PolicyExpression r2e2 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "contains"), e2inputset); 
		
		// r2e3
		ArrayList<PolicyAttribute> e3inputset = new ArrayList<>();
		e3inputset.add(new PolicyAttribute(
				AttributeType.SYSTEM_REFERENCE, 
				Utility.getId(Utility.systemRereferences, "onMaintenance")));
		e3inputset.add(new PolicyAttribute(
				AttributeType.LOCAL_REFERENCE, 
				(byte) 0));
		e3inputset.add(new PolicyAttribute(
				AttributeType.LOCAL_REFERENCE, 
				(byte) 1));
		
		PolicyExpression r2e3 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "isTrue"), e3inputset); 
		
		// rule 2 obligation
		PolicyAttribute r2o1att = new PolicyAttribute(AttributeType.SYSTEM_REFERENCE, 
				Utility.getId(Utility.systemRereferences, "bios_upgrades"));
		ArrayList<PolicyAttribute> o2inputset = new ArrayList<>();
		o2inputset.add(r2o1att);
				
		Effect r2fulfillOn = Effect.DENY;
		PolicyObligation r2o1 = new PolicyObligation(
				new PolicyExpression(Utility.getId(Utility.taskRereferences, "++"), o2inputset), 
				r2fulfillOn);
		
		// rule 2
		ArrayList<PolicyExpression> r2expressions = new ArrayList<>();
		r2expressions.add(r2e1);
		r2expressions.add(r2e2);
		r2expressions.add(r2e3);
		ArrayList<PolicyObligation> r2obligations = new ArrayList<>();
		r2obligations.add(r2o1);
		PolicyRule r2 = new PolicyRule((byte) 1, Effect.PERMIT, zeroByte, zeroByte, zeroByte, null, r2expressions, r2obligations);
		
		// rule 1 task
		PolicyAttribute r1att = new PolicyAttribute(AttributeType.SYSTEM_REFERENCE, 
				Utility.getId(Utility.systemRereferences, "onMaintenance"));
		ArrayList<PolicyAttribute> o1inputset = new ArrayList<>();
		o1inputset.add(r1att);
		
		// rule 1 obligations
		PolicyExpression r1o1e1 = new PolicyExpression(
				Utility.getId(Utility.taskRereferences, "activate"), o1inputset);		
		Effect r1fulfillOn = Effect.DENY;
		PolicyObligation r1o1 = new PolicyObligation(r1o1e1, r1fulfillOn);
//		HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		PolicyExpression r1e1 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<PolicyExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<PolicyObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		PolicyRule r1 = new PolicyRule((byte) 0, Effect.DENY, (byte) 4, zeroByte, zeroByte, null, r1expressions, r1obligations);
//		HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, (byte) 5, (byte) 6, HidraUtility.Action.DELETE, r1expressions, r1obligations);
		
		// policy
		ArrayList<PolicyRule> rules = new ArrayList<>();
		rules.add(r1);
		rules.add(r2);
		
		return (new Policy((byte) 104, Effect.PERMIT, rules));
	}
	
	private static Policy constructPartOfInstanceSample4() {
		// rule 1 task
		PolicyAttribute r1att = new PolicyAttribute(AttributeType.SYSTEM_REFERENCE, 
				Utility.getId(Utility.systemRereferences, "onMaintenance"));
		ArrayList<PolicyAttribute> o1inputset = new ArrayList<>();
		o1inputset.add(r1att);
		
		// rule 1 obligations
		PolicyExpression r1o1e1 = new PolicyExpression(
				Utility.getId(Utility.taskRereferences, "activate"), o1inputset);		
		Effect r1fulfillOn = Effect.DENY;
		PolicyObligation r1o1 = new PolicyObligation(r1o1e1, r1fulfillOn);
//				HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		PolicyExpression r1e1 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<PolicyExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<PolicyObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		PolicyRule r1 = new PolicyRule((byte) 0, Effect.DENY, (byte) 4, zeroByte, zeroByte, null, r1expressions, r1obligations);
//				HidraRule r1 = new HidraRule((byte) 0, Effect.DENY, (byte) 4, (byte) 5, (byte) 6, HidraUtility.Action.DELETE, r1expressions, r1obligations);
		
		// policy
		ArrayList<PolicyRule> rules = new ArrayList<>();
		rules.add(r1);
		
		return (new Policy((byte) 104, Effect.PERMIT, rules));
	}
	

	private static Policy constructDemoPolicy3() {
		return (new Policy((byte) 106, Effect.PERMIT, null));
	}
	private static Policy constructDemoPolicy2() {
		return (new Policy((byte) 105, Effect.DENY, null));
	}
	
	private static Policy constructDemoPolicy() {
//		// rule 1 obligations
		PolicyExpression r1o1e1 = new PolicyExpression(
				Utility.getId(Utility.taskRereferences, "log_request"), null);		
		PolicyObligation r1o1 = new PolicyObligation(r1o1e1, null);
//				HidraObligation r1o1 = new HidraObligation(r1o1e1, null);
		
		// rule 1 expressions
		PolicyExpression r1e1 = new PolicyExpression(
				Utility.getId(Utility.expressionRereferences, "low_battery"), null); 
		
		// rule 1
		ArrayList<PolicyExpression> r1expressions = new ArrayList<>();
		r1expressions.add(r1e1);
		ArrayList<PolicyObligation> r1obligations = new ArrayList<>();
		r1obligations.add(r1o1);
		PolicyRule r1 = new PolicyRule((byte) 0, Effect.DENY, zeroByte, zeroByte, zeroByte, Action.PUT, r1expressions, r1obligations);
		
		// policy
		ArrayList<PolicyRule> rules = new ArrayList<>();
		rules.add(r1);
		
		return (new Policy((byte) 104, Effect.PERMIT, rules));
	}
	
	/**
	 * Execute the Server functionalities.
	 * Only one thread is listening for incoming Subject requests and from there on executing the Hidra protocol. 
	 * As for now, the ANS and CM are not implemented separately. 
	 * This implementation is kept to the basics, because it is not the bottleneck of this thesis. 
	 * Only when multiple resources and subjects are introduced, should the server start listening for messages continuously and keep more state.
	 * When accountability messages are included, an extra thread will be introduced to listen for exchanged initiated by the resource.  
	 */
	public static void main(String[] args){
		System.out.println("Server opens socket, both for resource and subject");
		try{
			socketForResource = new DatagramSocket(SERVER_RESOURCE_PORT);
			System.out.println("Opened socket on port "+ SERVER_RESOURCE_PORT);
			
			socketForSubject = new DatagramSocket(SERVER_SUBJECT_PORT);
			System.out.println("Opened socket on port "+ SERVER_SUBJECT_PORT);
		}
		catch(SocketException e){
			e.getMessage();
		}
		
		try {
			// Set up connection with RPL border router
			Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {TimeUnit.MILLISECONDS.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
//		sendPolicy(constructDemoPolicy2());
//		try {TimeUnit.MILLISECONDS.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
//		sendPolicy(constructDemoPolicy());
//		try {TimeUnit.MILLISECONDS.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
//		sendPolicy(constructInstanceSample4());
				
        Utility.computeAndStoreOneWayHashChain();
		runFirstHidraProtocolDemo();
		while (true) {
			runHidraProtocolDemo();
		}
	}
	
	private static Policy getPolicy(byte subjectId) {
		Policy hp;
		if (subjectId == 5) {
			hp = constructDemoPolicy3();
		} else if (subjectId == 4) {
			hp = constructDemoPolicy2();
		} else {
			hp = constructDemoPolicy();
		}
		return hp;
	}
	
	private static void sendPolicy(Policy policy) {
		sendDataPacket(Utility.booleanArrayToByteArray(policy.codifyUsingAPBR()), Utility.getResourceIP(), socketForResource, Utility.getServerResourcePort());
	}
	
	private static void runFirstHidraProtocolDemo() {
		//Server starts listening for messages from a Subject
		DatagramPacket receivedDatagram = receiveDataPacket(socketForSubject);
		byte[] actualMessage = null; 
		long timestamp  = System.currentTimeMillis();
		if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) {
			actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			byte subjectId = actualMessage[1];
//			System.out.println("Received HID_ANS_REQ from subject id: " + subjectId);
			HidraAnsReq har = new HidraAnsReq(actualMessage);
			har.processAndConstructReply().send(subjectId);
			System.out.println("Computation time ANS_REQ -> ANS_REP: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
			timestamp  = System.currentTimeMillis();			
			receivedDatagram = receiveDataPacket(socketForSubject); 
			System.out.println("Time CM_REQ reception: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
			timestamp  = System.currentTimeMillis();
			if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) {
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				// Resource id is hardcoded to 2
				if (actualMessage[1] == 2) {
//					System.out.println("Received HID_CM_REQ from subject id " + subjectId + " for resource id 2.");

					//Unpack incoming message
					HidraCmReq hcr = new HidraCmReq(subjectId, actualMessage);
					
					//Process message, include policy based on subject id and send to resource
					Policy hp = getPolicy(subjectId);
//					hp.prettyPrint();
					hcr.processAndConstructReply(hp, subjectId).send();
					System.out.println("Computation time CM_REQ -> CM_IND: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
					timestamp  = System.currentTimeMillis();
					receivedDatagram = receiveDataPacket(socketForResource);
					System.out.println("Time CM_IND_REQ reception: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
					timestamp  = System.currentTimeMillis();
					if (receivedDatagram.getPort() == SERVER_RESOURCE_PORT) {
						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
//						System.out.println("Received HID_CM_IND_REQ");
						if(actualMessage[1] == 2) {
							
							HidraCmIndReq hcir = new HidraCmIndReq(actualMessage);
							
							hcir.processAndConstructReply().send();
							System.out.println("Computation time CM_IND_REQ -> CM_IND_REP: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
							timestamp  = System.currentTimeMillis();
							
							//If the simulation runs at 1000%, 100ms should result in a 1000ms from its perspective
//							try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
//							System.out.println("Delay of : " + (System.currentTimeMillis() - timestamp) + " milliseconds");
//							timestamp  = System.currentTimeMillis();
							
							(new HidraCmRep(subjectId)).send(subjectId);
							System.out.println("Computation time to send CM_REP: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
							timestamp  = System.currentTimeMillis();

							System.out.println("End of hidra protocol with subject " + subjectId);
						}
					} else {
						System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
					}
				} else {
					System.out.println("Error: Instead of message from same subject, received: " + new String(actualMessage) + " with length " + actualMessage.length);
				}
			} else {
				System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
			}
		} else {
			System.out.println("Error: Instead of HID_ANS_REQ, received: " + new String(actualMessage) + " with length " + actualMessage.length);
		}
	}
	
	private static void runHidraProtocolDemo() {
		//Server starts listening for messages from a Subject
		DatagramPacket receivedDatagram = receiveDataPacket(socketForSubject);
		byte[] actualMessage = null; 
		long timestamp  = System.currentTimeMillis();
		if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) {
			actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			byte subjectId = actualMessage[1];
//			System.out.println("Received HID_ANS_REQ from subject id: " + subjectId);
			HidraAnsReq har = new HidraAnsReq(actualMessage);
			har.processAndConstructReply().send(subjectId);
			System.out.println("Computation time ANS_REQ -> ANS_REP: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
			timestamp  = System.currentTimeMillis();			
			receivedDatagram = receiveDataPacket(socketForSubject); 
			System.out.println("Time CM_REQ reception: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
			timestamp  = System.currentTimeMillis();
			if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) {
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				// Resource id is hardcoded to 2
				if (actualMessage[1] == 2) {
//					System.out.println("Received HID_CM_REQ from subject id " + subjectId + " for resource id 2.");

					//Unpack incoming message
					HidraCmReq hcr = new HidraCmReq(subjectId, actualMessage);
					
					//Process message, include policy based on subject id and send to resource
					Policy hp = getPolicy(subjectId);
//					hp.prettyPrint();
					hcr.processAndConstructReply(hp, subjectId).send();
					System.out.println("Computation time CM_REQ -> CM_IND: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
					timestamp  = System.currentTimeMillis();
					
					//If the simulation runs at 1000%, 100ms should result in a 1s wait from its perspective
//					try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
//					System.out.println("Delay of : " + (System.currentTimeMillis() - timestamp) + " milliseconds");
//					timestamp  = System.currentTimeMillis();
					
					(new HidraCmRep(subjectId)).send(subjectId);
					System.out.println("Computation time to send CM_REP: " + (System.currentTimeMillis() - timestamp) + " milliseconds");
					timestamp  = System.currentTimeMillis();

					System.out.println("End of hidra protocol with subject " + subjectId);
				} else {
					System.out.println("Error: Instead of message from same subject, received: " + new String(actualMessage) + " with length " + actualMessage.length);
				}
			} else {
				System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
			}
		} else {
			System.out.println("Error: Instead of HID_ANS_REQ, received: " + new String(actualMessage) + " with length " + actualMessage.length);
		}
	}
	
	
	public static void blackListSubject(byte subjectId) {
		//Update policy for subject | Handle one test at a time, because resource handles only 1 subject
//		getUserInput("Enter to blacklist the subject");
		(new HidraBlacklistMessage(subjectId)).send();
		
		DatagramPacket receivedDatagram = receiveDataPacket(socketForResource);
		if (receivedDatagram.getPort() == SERVER_RESOURCE_PORT) {
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
	
	//Assumption: only one resource
	private static ArrayList<byte[]> keyChain;
	private static int currentKeyIndex;
	
	public static byte[] getNextKeyChainValue() {
		if (currentKeyIndex < 0) {
			System.out.println("Error: all keys have been used");
			return new byte[16];
		}
		return getKeyChain().get(currentKeyIndex--);
	}
	
	public static ArrayList<byte[]> getKeyChain() {
		return keyChain;
	}
	
	public static void setKeyChain(ArrayList<byte[]> keyChain) {
		TrustedServer.currentKeyIndex = keyChain.size() - 1;
		TrustedServer.keyChain = keyChain;
	}
}


