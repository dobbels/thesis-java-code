package message;

import hidra.HidraUtility;

import java.util.ArrayList;


public class HidraBlacklistMessage extends HidraPolicyAdaptMessage{
	
	private byte id;
	
	public HidraBlacklistMessage(byte id) {
		super();
		this.id = id;
	}
	
	@Override
	public ArrayList<Boolean> constructByteMessage() {
		ArrayList<Boolean> codification = super.constructByteMessage();
		codification.addAll(HidraUtility.byteToBoolList(id));
		return codification;
	}
}