package message;

import hidra.HidraUtility;

import java.util.ArrayList;

public class HidraSubjectMessage extends HidraMessage {
	
	enum MessageType {
		HIDRA_PROTOCOL,
		ACCESS_REQUEST
	}
	
	public MessageType messageType;
	public byte id;

	public HidraSubjectMessage(MessageType messageType, byte id) {
		super();
		this.messageType = messageType;
		this.id = id;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = messageTypeToBoolList(messageType);
		codification.addAll(HidraUtility.byteToBoolList(id));
		return codification;
	}
	
	public static ArrayList<Boolean> messageTypeToBoolList(MessageType type) {
		ArrayList<Boolean> result = new ArrayList<>();
		
		if (type == MessageType.HIDRA_PROTOCOL) {
			result.add(false);
		} else if (type == MessageType.ACCESS_REQUEST) {
			result.add(true);
		} else {
			System.out.println("Error: did not find message type.");
		}
		
		return result;
	}
}
