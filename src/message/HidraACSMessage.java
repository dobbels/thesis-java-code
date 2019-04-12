package message;

import hidra.HidraUtility;

import java.util.ArrayList;

public class HidraACSMessage extends HidraMessage {
	
	enum MessageType {
		HIDRA_PROTOCOL,
		POLICY_UPDATE
	}
	
	public MessageType messageType;

	public HidraACSMessage(MessageType messageType) {
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
			result.add(false);
		} else if (type == MessageType.POLICY_UPDATE) {
			result.add(true);
		} else {
			System.out.println("Error: did not find message type.");
		}
		
		return result;
	}
}
