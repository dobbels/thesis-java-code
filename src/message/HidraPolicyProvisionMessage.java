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
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteToBoolList(id));
		codification.addAll(policy);
		return codification;
	}
}