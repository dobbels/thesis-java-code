package message;

import hidra.Utility;
import hidra.TrustedServer;

import java.util.ArrayList;

public class TrustedServerResourceMessage extends Message {
	
	enum MessageType {
		HIDRA_PROTOCOL,
		POLICY_UPDATE
	}
	
	public MessageType messageType;

	public TrustedServerResourceMessage(MessageType messageType) {
		super();
		this.messageType = messageType;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = messageTypeToBoolList(messageType);
		return codification;
	}
	
	public static ArrayList<Boolean> messageTypeToBoolList(MessageType type) {
		ArrayList<Boolean> result = new ArrayList<>();
		
		if (type == MessageType.HIDRA_PROTOCOL) {
			result.addAll(Utility.byteToBoolList((byte) 0));
		} else if (type == MessageType.POLICY_UPDATE) {
			result.addAll(Utility.byteToBoolList((byte) 1));
		} else {
			System.out.println("Error: did not find message type.");
		}
		
		return result;
	}
	

	public void send(){
		byte[] packet = Utility.booleanArrayToByteArray(constructBoolMessage());
		if (packet.length > 84) {
			System.out.println("Length of data packet is: " + packet.length + ".");
			System.out.println("This will probably be too much too handle in one datagram for the 802.15.4 network.");
		}
		TrustedServer.sendDataPacket(packet,Utility.getResourceIP(), TrustedServer.socketForResource, Utility.getServerResourcePort());
	}
}
