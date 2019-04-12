package message;

import hidra.HidraUtility;

import java.util.ArrayList;

public class HidraCmIndRepMessage extends HidraProtocolMessage {

	private byte id;
	
	public HidraCmIndRepMessage(byte id) {
		super();
		this.id = id;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteToBoolList(id));
		return codification;
	}
}
