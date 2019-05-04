package message;

import hidra.HidraACS;
import hidra.HidraUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class HidraCmInd extends HidraProtocolMessage {
	
	byte[] idR = new byte[2];
	byte[] idS = new byte[2];
	byte[] nonceSR = new byte[8];
	byte[] lifeTimeTR = new byte[1];
	byte[] Kircm = new byte[16];
	private ArrayList<Boolean> policy;
	private byte[] encrypted_byte_policy;
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
		
		encrypted_byte_policy = HidraUtility.xcrypt(HidraUtility.booleanArrayToByteArray(policy), HidraACS.Kr);
		
		// Noncesr generation
        new Random().nextBytes(this.nonceSR);
		
//		To assure the freshness of this message, it embeds a new key value K i S,CM from a previously 
//		generated oneway key chain [K 1 S,CM ...K N S,CM ]. The purpose of these oneway functions on the enclosed key, 
//		F(K i S,CM ) = K i+1 S,CM , is to make it computationally unfeasible to calculate the inverse function using a 
//		transmitted and potentially sniffed key.
        this.Kircm = HidraUtility.computeAndStoreOneWayHashChain();
        
        //Quickfix, because hmac doesn't work in Contiki at the moment
        byte[] messageForMAC = constructQuickMessageForIntegrity();
        System.out.println("messageForMAC: " + HidraUtility.byteArrayToHexString(messageForMAC) + " with length " + messageForMAC.length);
        //Differences between murmur3 implementations => always take the first 20 bytes as a comparison for now.
        this.MAC = HidraUtility.hashTo4Bytes(Arrays.copyOfRange(messageForMAC, 0, 20));
//        this.MAC = HidraUtility.hashTo4Bytes(messageForMAC);
        
      //Construct array to compute mac
//        byte[] messageForMAC = constructMessageForIntegrity();
//        this.MAC = HidraUtility.hashTo4Bytes(HidraUtility.computeMac(messageForMAC));
	}
	
	public byte[] constructQuickMessageForIntegrity() {
		byte[] kr = new byte[16];
		for (int i = 0; i < HidraACS.Kr.length ; i++ ) {
			kr[i] = (byte) HidraACS.Kr[i];
		}
		ArrayList<Boolean> codification = HidraUtility.byteArrayToBooleanList(kr); 
		codification.addAll(HidraUtility.byteArrayToBooleanList(idS));
		codification.addAll(HidraUtility.byteArrayToBooleanList(nonceSR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(HidraUtility.byteArrayToBooleanList(encrypted_byte_policy));
		return HidraUtility.booleanArrayToByteArray(codification);
	}
	
	public byte[] constructMessageForIntegrity() {
		ArrayList<Boolean> codification = HidraUtility.byteArrayToBooleanList(idS);
		codification.addAll(HidraUtility.byteArrayToBooleanList(nonceSR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(policy);
		return HidraUtility.booleanArrayToByteArray(codification);
	}
	
	
	public byte[] constructCmInd() {
		ArrayList<Boolean> codification = super.constructBoolMessage();

		System.out.println(HidraUtility.byteArrayToHexString(idR));
		System.out.println(HidraUtility.byteArrayToHexString(idS));
		System.out.println(HidraUtility.byteArrayToHexString(nonceSR));
		System.out.println(HidraUtility.byteArrayToHexString(lifeTimeTR));
		
		System.out.println(HidraUtility.byteArrayToHexString(Kircm));

		System.out.println("Unencrypted policy: " + HidraUtility.byteArrayToHexString(HidraUtility.booleanArrayToByteArray(policy)));
		System.out.println("Encrypted policy: " + HidraUtility.byteArrayToHexString(encrypted_byte_policy));

		System.out.println(HidraUtility.byteArrayToHexString(MAC));
		
		codification.addAll(HidraUtility.byteArrayToBooleanList(idR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(idS));
		codification.addAll(HidraUtility.byteArrayToBooleanList(nonceSR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(lifeTimeTR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(HidraUtility.byteArrayToBooleanList(encrypted_byte_policy));
		codification.addAll(HidraUtility.byteArrayToBooleanList(MAC));
		byte[] result = HidraUtility.booleanArrayToByteArray(codification); 
		System.out.println("About to send HidraCmInd: " + HidraUtility.byteArrayToHexString(result));
		return result;
	}
}