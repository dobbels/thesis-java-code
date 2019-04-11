package message;

import hidra.HidraUtility;

import java.util.ArrayList;


public class HidraPolicyProvisionMessage extends HidraProtocolMessage {
	
	private byte id;
	private ArrayList<Boolean> policy;
	
	public HidraPolicyProvisionMessage(byte id, ArrayList<Boolean> policy) {
		super();
		this.id = id;
		this.policy = policy;
	}
	
	@Override
	public ArrayList<Boolean> constructByteMessage() {
		ArrayList<Boolean> codification = super.constructByteMessage();
		codification.addAll(HidraUtility.byteToBoolList(id));
		codification.addAll(policy);
		return codification;
	}
}