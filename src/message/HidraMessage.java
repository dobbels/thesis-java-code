package message;

import hidra.HidraUtility;

import java.util.ArrayList;


/**
 * This class implements a Hidra message, containing the necessary identifiers, token, keys and possibly an EBNF policy (codified with APBR codification).
 */
public class HidraMessage {
	
	enum MessageType {
		HIDRA_PROTOCOL,
		POLICY_UPDATE
	}
	
	public MessageType messageType;

	public HidraMessage(MessageType messageType) {
		this.messageType = messageType;
	}
	
	public ArrayList<Boolean> constructByteMessage() {
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
