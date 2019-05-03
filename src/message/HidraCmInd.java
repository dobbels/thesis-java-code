package message;

import hidra.HidraUtility;

import java.util.ArrayList;
import java.util.Random;


public class HidraCmInd extends HidraProtocolMessage {
	
	byte[] idR = new byte[2];
	byte[] idS = new byte[2];
	byte[] nonceSR = new byte[8];
	byte[] lifeTimeTR = new byte[1];
	byte[] Kircm = new byte[16];
	private ArrayList<Boolean> policy;
	byte[] MAC;
	
	public HidraCmInd(byte[] idR, byte[] idS, byte[] lifeTimeTR, int i, ArrayList<Boolean> policy) {
		super();
		this.idR = idR;
		this.idS = idS;
		this.lifeTimeTR = lifeTimeTR;
		this.policy = policy;
		
		// Pad policy bitset so its length l mod 8 = 0. 
		// 	Extra bits come at the end, so that they can be easily ignored by the receiver.
		int nbOfBytes = (policy.size() + 7) / 8;
		byte[] bytes = new byte[nbOfBytes]; 
		int emptyRemainder = nbOfBytes*8 - policy.size();
		for (int r = 0 ; r < emptyRemainder ; r ++) {
			policy.add(false);  
		}
		
		// Noncesr generation
        new Random().nextBytes(this.nonceSR);
		
//		To assure the freshness of this message, it embeds a new key value K i S,CM from a previously 
//		generated oneway key chain [K 1 S,CM ...K N S,CM ]. The purpose of these oneway functions on the enclosed key, 
//		F(K i S,CM ) = K i+1 S,CM , is to make it computationally unfeasible to calculate the inverse function using a 
//		transmitted and potentially sniffed key.
        this.Kircm = HidraUtility.computeAndStoreOneWayHashChain();
        
        //Construct array to compute mac
        byte[] messageForMAC = constructMessageForIntegrity();
        this.MAC = HidraUtility.hashTo4Bytes(HidraUtility.computeMac(messageForMAC));
	}
	
	public byte[] constructMessageForIntegrity() {
		ArrayList<Boolean> codification = HidraUtility.byteArrayToBooleanList(idS);
		codification.addAll(HidraUtility.byteArrayToBooleanList(nonceSR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(policy);
		return HidraUtility.booleanArrayToByteArray(codification);
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteArrayToBooleanList(idR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(idS));
		codification.addAll(HidraUtility.byteArrayToBooleanList(nonceSR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(policy);
		codification.addAll(HidraUtility.byteArrayToBooleanList(MAC));
		return codification;
	}
}