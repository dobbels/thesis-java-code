package message;

import hidra.Policy;
import hidra.TrustedServer;
import hidra.Utility;

import java.util.ArrayList;
import java.util.Random;


public class PolicyUpdateMessage extends TrustedServerResourceMessage {
	
	byte[] idR = {0,2};
	byte[] pseudonym = new byte[2];
	byte[] Kircm = new byte[16];
	private ArrayList<Boolean> policy;
	private byte[] encrypted_byte_policy;
	byte[] MAC;
	
	public PolicyUpdateMessage(byte[] id, Policy policy) {
		super(MessageType.POLICY_UPDATE);
		pseudonym = id;
		this.policy = policy.codifyUsingAPBR();
		
		// Pad policy bitset so its length l mod 8 = 0. 
		// 	Extra bits come at the end, so that they can be easily ignored by the receiver.
		int nbOfBytes = (this.policy.size() + 7) / 8;
		int emptyRemainder = nbOfBytes*8 - this.policy.size();
		for (int r = 0 ; r < emptyRemainder ; r ++) {
			this.policy.add(false);  
		}
		
		encrypted_byte_policy = Utility.xcrypt(Utility.booleanArrayToByteArray(this.policy), TrustedServer.Kr);
      
		this.Kircm = TrustedServer.getNextKeyChainValue();
        
        byte[] messageForMAC = constructMessageForIntegrity();
    	this.MAC = Utility.compute4ByteMac(messageForMAC);
	}
	
	private byte[] constructMessageForIntegrity() {
		ArrayList<Boolean> codification = Utility.byteArrayToBooleanList(pseudonym);
		codification.addAll(Utility.byteArrayToBooleanList(Kircm));
		codification.addAll(Utility.byteArrayToBooleanList(encrypted_byte_policy));
		return Utility.booleanArrayToByteArray(codification);
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();		
		codification.addAll(Utility.byteArrayToBooleanList(idR));
		codification.addAll(Utility.byteArrayToBooleanList(pseudonym));
		codification.addAll(Utility.byteArrayToBooleanList(Kircm));
		codification.addAll(Utility.byteArrayToBooleanList(encrypted_byte_policy));
		codification.addAll(Utility.byteArrayToBooleanList(MAC));
		return codification;
	}
}