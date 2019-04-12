package message;

import hidra.HidraUtility;

import java.util.ArrayList;


public class HidraPolicyAdaptMessage extends HidraMessage {
	
	enum UpdateType {
		BLACKLIST,
		UPDATE,
		ADD
	}
	
	byte id;
	UpdateType type;
	
	public HidraPolicyAdaptMessage(byte id, UpdateType type) {
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
			result.addAll(HidraUtility.byteToBoolList((byte) 0, 4));
		} else if (type == UpdateType.UPDATE) {
			result.addAll(HidraUtility.byteToBoolList((byte) 1, 4));
		} else if (type == UpdateType.ADD) {
			result.addAll(HidraUtility.byteToBoolList((byte) 2, 4));
		} else {
			System.out.println("Error: did not find update type.");
		}
		
		return result;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteToBoolList(id));
		codification.addAll(updateTypeToBoolList(this.type));
		HidraUtility.printBoolList(codification);
		return codification;
	}
}