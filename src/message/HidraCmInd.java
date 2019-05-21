package message;

import hidra.SubjectSecurityProperties;
import hidra.TrustedServer;
import hidra.Utility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class HidraCmInd extends HidraProtocolMessage {
	
	byte[] idR = new byte[2];
	byte[] pseudonym = new byte[2];
	byte[] nonceSR = new byte[8];
	byte[] lifeTimeTR = new byte[1];
	byte[] Kircm = new byte[16];
	private ArrayList<Boolean> policy;
	private byte[] encrypted_byte_policy;
	byte[] MAC;
	
	public HidraCmInd(byte subjectId, byte[] idR, byte[] lifeTimeTR, int i, ArrayList<Boolean> policy) {
		super();
		this.idR = idR;
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
		
		encrypted_byte_policy = Utility.xcrypt(Utility.booleanArrayToByteArray(policy), TrustedServer.Kr);
		
		// Noncesr generation
        new Random().nextBytes(this.nonceSR);
        System.out.println("NonceSR: " + Utility.byteArrayToHexString(this.nonceSR));
        
        //Store for later use
        TrustedServer.securityProperties.get(subjectId).setNonceSR(this.nonceSR);
        
        //Get generated pseudonym for this subject
  		pseudonym = TrustedServer.securityProperties.get(subjectId).getPseudonym();
      
		
//		To assure the freshness of this message, it embeds a new key value K i S,CM from a previously 
//		generated oneway key chain [K 1 S,CM ...K N S,CM ]. The purpose of these oneway functions on the enclosed key, 
//		F(K i S,CM ) = K i+1 S,CM , is to make it computationally unfeasible to calculate the inverse function using a 
//		transmitted and potentially sniffed key.
        this.Kircm = Utility.computeAndStoreOneWayHashChain();
        
        byte[] messageForMAC = constructMessageForIntegrity();
    	this.MAC = Utility.compute4ByteMac(messageForMAC);
	}
	
	private byte[] constructMessageForIntegrity() {
		ArrayList<Boolean> codification = Utility.byteArrayToBooleanList(pseudonym);
		codification.addAll(Utility.byteArrayToBooleanList(nonceSR));
		codification.addAll(Utility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(Utility.byteArrayToBooleanList(Kircm));
		codification.addAll(Utility.byteArrayToBooleanList(encrypted_byte_policy));
		return Utility.booleanArrayToByteArray(codification);
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();		
		codification.addAll(Utility.byteArrayToBooleanList(idR));
		codification.addAll(Utility.byteArrayToBooleanList(pseudonym));
		codification.addAll(Utility.byteArrayToBooleanList(nonceSR));
		codification.addAll(Utility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(Utility.byteArrayToBooleanList(Kircm));
		codification.addAll(Utility.byteArrayToBooleanList(encrypted_byte_policy));
		codification.addAll(Utility.byteArrayToBooleanList(MAC));
		return codification;
	}
}