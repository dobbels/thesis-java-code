package message;

import hidra.Utility;

import java.util.ArrayList;


public class PolicyUpdateMessage extends TrustedServerResourceMessage {
	
	enum UpdateType {
		BLACKLIST
	}
	
	byte id;
	UpdateType type;
	
	public PolicyUpdateMessage(byte id, UpdateType type) {
		super(MessageType.POLICY_UPDATE);
		this.type = type;
		this.id = id;
	}
	
	/**
	 * Return 4 bits encoding the type of policy adaptation
	 *///TODO meer bits, als alle soorten onderverdeling van UPDATE en ADD hier ook komen?
	public static ArrayList<Boolean> updateTypeToBoolList(UpdateType type) {
		ArrayList<Boolean> result = new ArrayList<>();
		
		if (type == UpdateType.BLACKLIST) {
			result.addAll(Utility.byteToBoolList((byte) 0, 3));
		} else {
			System.out.println("Error: did not find update type.");
		}
		
		return result;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(Utility.byteToBoolList(id));
		codification.addAll(updateTypeToBoolList(this.type));
		Utility.printBoolList(codification);
		return codification;
	}
}