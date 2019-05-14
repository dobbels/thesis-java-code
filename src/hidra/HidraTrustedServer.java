package hidra;

import hidra.HidraUtility.*;

import java.beans.Expression;
import java.io.ByteArrayOutputStream;
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

import encryption.HMAC;
import encryption.Alice;
import encryption.AliceContext;

import message.HidraAnsReq;
import message.HidraBlacklistMessage;
import message.HidraCmIndRepMessage;
import message.HidraACSResourceMessage;
import message.HidraCmInd;
import message.HidraCmIndReq;
import message.HidraCmRep;
import message.HidraCmReq;


/**
 * Class representing a Access Control Server, executing the Hidra protocol.
 */
public class HidraTrustedServer {
	public static final int SERVER_RESOURCE_PORT = HidraUtility.getAcsResourcePort();
	public static final int SERVER_SUBJECT_PORT = HidraUtility.getAcsSubjectPort();
	
	private static String resourceIP = HidraUtility.getResourceIP();
	
	private static DatagramSocket socketForResource = null;
	private static DatagramSocket socketForSubject = null;

	public static final byte zeroByte = 0; 
	public static final byte[] testPacket = "Test message".getBytes(); 	
	
	public static final char[] Kcm = { 0x15, 0x15,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final char[] Kr =  { 0x2b,  0x7e,  0x15,  0x16,  0x28,  0x2b,  0x2b,  0x2b,  0x2b,  0x2b,  0x15,  0x2b,  0x09,  0x2b,  0x4f,  0x3c };
	public static final byte[] Initial_Vector = { 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f };
	public static AliceContext ctx = new AliceContext(AliceContext.Algorithm.AES, AliceContext.Mode.CTR, 
			AliceContext.Padding.NO_PADDING, AliceContext.KeyLength.BITS_128, 
			AliceContext.Pbkdf.NONE, AliceContext.MacAlgorithm.NONE, 
			16, AliceContext.GcmTagLength.BITS_128, 10000);
	
	public static HashMap<Byte, HidraSubjectsSecurityProperties> securityProperties = new HashMap<>();	
	
	public HidraTrustedServer(){
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
			System.out.println("ACS sent datagram with UDP payload of length " + dataPack.getLength());
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
		sendDataPacket(packet,resourceIP, socketForResource, SERVER_RESOURCE_PORT);
	}
	
	public static void sendDataToSubject(byte[] packet, int subjectId){
		sendDataPacket(packet,HidraUtility.getSubjectIP(subjectId), socketForSubject, SERVER_SUBJECT_PORT);
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
		new HidraTrustedServer();
		
//		byte[] test_vector = 
//			{ 
//				(byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff,
//				0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 
//				(byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd
//		};
		
//		byte[] test_vector = 
//			{ 
//				(byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff
//		};
//		byte[] test_vector = 
//			{ 
//				0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 
//				0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 
//				0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c
//		};
		
//		byte[] test_vector = {0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x68, 0x65, 0x6c, 0x6c, 0x6f};
		
//		byte[] test_vector = {0x5f, (byte) 0xf5, 0x5f, (byte) 0xf5, 0x5f};
		
//		System.out.println(HidraUtility.byteArrayToHexString(test_vector) + " with length " + test_vector.length);
		
//		System.out.println("with key: " + HidraUtility.byteArrayToHexString());
		
//		System.out.println(HidraUtility.byteArrayToHexString(HidraUtility.computeMac(test_vector)));
//		
//		System.out.println(HidraUtility.byteArrayToHexString(HidraUtility.hashTo4Bytes(HidraUtility.computeMac(test_vector))));
		
//		System.out.println(HidraUtility.byteArrayToHexString(HidraUtility.hashTo4Bytes(test_vector)));
//		
		try {
			// Set up connection with RPL border router
			Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (true) {
			runHidraProtocolDemo();
		}
	}
	
	private static HidraPolicy getPolicy(byte subjectId) {
		HidraPolicy hp;
		if (subjectId == 5) {
			hp = constructDemoPolicy3();
		} else if (subjectId == 4) {
			hp = constructDemoPolicy2();
		} else {
			hp = constructDemoPolicy();
		}
		return hp;
	}
	
	private static void runHidraProtocolDemo() {
		//Server starts listening for messages from a Subject
		DatagramPacket receivedDatagram = receiveDataPacket(socketForSubject);
		byte[] actualMessage = null; 
		
		if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) {
			actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
			byte subjectId = actualMessage[1];
			System.out.println("Received HID_ANS_REQ from subject id: " + subjectId);
			HidraAnsReq har = new HidraAnsReq(actualMessage);
			sendDataToSubject(HidraUtility.booleanArrayToByteArray(har.processAndConstructReply()), subjectId);
			receivedDatagram = receiveDataPacket(socketForSubject); 
			if (receivedDatagram.getPort() == SERVER_SUBJECT_PORT) { 
				actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
				// Resource id is hardcoded to 2
				if (actualMessage[1] == 2) {
					System.out.println("Received HID_CM_REQ from subject id " + subjectId + " for resource id 2.");

					//Unpack incoming message
					HidraCmReq hcr = new HidraCmReq(subjectId, actualMessage);
					
					//Process message, include policy based on subject id and send to resource
					HidraPolicy hp = getPolicy(subjectId);
					hp.prettyPrint();
					byte[] cmInd = hcr.processAndConstructReply(hp);
					System.out.println("Length of CM_IND: " + cmInd.length);
					sendDataToResource(cmInd);
					
					receivedDatagram = receiveDataPacket(socketForResource);
					if (receivedDatagram.getPort() == SERVER_RESOURCE_PORT) {
						actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
						System.out.println("Received HID_CM_IND_REQ");
						if(actualMessage[1] == 2) {
							
							HidraCmIndReq hcir = new HidraCmIndReq(actualMessage);
							
							HidraCmIndRepMessage hm = hcir.constructResponse();
							
							sendDataToResource(HidraUtility.booleanArrayToByteArray(hm.constructBoolMessage()));
							
							receivedDatagram = receiveDataPacket(socketForResource);//TODO fix die send_ack nog als je wil/zou weten hoe dit komt. Vreemd dat dat ene bericht gewoon niet verstuurd wordt
																					// Inspecteer eens de packets die worden verstuurd. De payload lijkt bvb raar te doen + 
																					// + wat is een packet dat puur IPv6 is?? 614	01:26.950	2	1	 73: 15.4 D 81:E6:A1:D8:C8:FB:D3:E7 C1:0C:00:00:00:00:00:01|IPv6|04D204D2 00093827 01
							if (receivedDatagram.getPort() == SERVER_RESOURCE_PORT) {
								actualMessage = Arrays.copyOfRange(receivedDatagram.getData(), 0, receivedDatagram.getLength());
								System.out.println(actualMessage[0]);
								if(actualMessage[0] == 1) {
									System.out.println("Received ACK");
									HidraCmRep hcm = new HidraCmRep(subjectId);
//									try {TimeUnit.MILLISECONDS.sleep(1500);} catch (InterruptedException e) {e.printStackTrace();}
									sendDataToSubject(HidraUtility.booleanArrayToByteArray(hcm.constructBoolMessage()), subjectId);
									System.out.println("End of hidra protocol with subject " + subjectId);
								} else {
									System.out.println("Error: Association denied by resource after HID_CM_IND_REP");
								}
							} else {
								System.out.println("Error: Received datagram on the wrong port: " + receivedDatagram.getPort());
							}	
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
	
	public static void blackListSubject(byte subjectId) {
		//Update policy for subject | Handle one test at a time, because resource handles only 1 subject
//		getUserInput("Enter to blacklist the subject");
		HidraBlacklistMessage hbm = new HidraBlacklistMessage(subjectId);
		sendDataToResource(HidraUtility.booleanArrayToByteArray(hbm.constructBoolMessage()));
		
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
		HidraTrustedServer.currentKeyIndex = keyChain.size() - 1;
		HidraTrustedServer.keyChain = keyChain;
	}
}


