package message;

import hidra.HidraTrustedServer;
import hidra.HidraSubjectsSecurityProperties;
import hidra.HidraUtility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class HidraCmRep extends HidraACSSubjectMessage {
	//Totaal bytes van bericht: 62 bytes. Crypto door subject op 34
	byte[] idR = {0,2};
	byte[] pseudonym = new byte[2];
	byte subjectId;
	//ksr + ids + noncesr ( + attributes)
	private byte[] ticketR = new byte[26]; 
	private byte[] Ksr = new byte[16];
	private byte[] nonceSR = new byte[8];
	// Ksr + ids + noncesr + nonce2 + idr
	private byte[] restOfMessage = new byte[34];

	public HidraCmRep(byte subjectId) {
		super();
		this.subjectId = subjectId;
		//Get generated pseudonym for this subject
		pseudonym = HidraTrustedServer.securityProperties.get(subjectId).getPseudonym();
		
		this.nonceSR = HidraTrustedServer.securityProperties.get(subjectId).getNonceSR();
//		System.out.println("NonceSR: " + HidraUtility.byteArrayToHexString(this.nonceSR));
		
		new Random().nextBytes(this.Ksr);
//		System.out.println("Ksr: " + HidraUtility.byteArrayToHexString(this.Ksr));

        ticketR = constructEncryptedTicket();
//        System.out.println("Encrypted ticketR: " + HidraUtility.byteArrayToHexString(this.ticketR));
//        System.out.println("Encrypted ticketR, bit 8: " + HidraUtility.byteArrayToHexString(Arrays.copyOfRange(this.ticketR, 8, 9)));
        
        restOfMessage = constructEncryptedRestOfMessage();
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		System.out.println("Pseudonym for subject " + subjectId + " is: " + HidraUtility.byteArrayToHexString(this.pseudonym));
		codification.addAll(HidraUtility.byteArrayToBooleanList(pseudonym));
		codification.addAll(HidraUtility.byteArrayToBooleanList(ticketR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(restOfMessage));
		return codification;
	}
	
	private byte[] constructEncryptedTicket() {
		byte[] ticket = new byte [26];
		for(int i = 0 ; i < Ksr.length; i++) {
			ticket[i] = Ksr [i];
		}
		for(int i = 0 ; i < pseudonym.length; i++) {
			ticket[16+i] = pseudonym [i];
		}
		for(int i = 0 ; i < nonceSR.length; i++) {
			ticket[18+i] = nonceSR[i];
		}
//		System.out.println("Unencrypted ticketR: " + HidraUtility.byteArrayToHexString(ticket));
		//No attributes in this implementation
		return HidraUtility.xcrypt(ticket, HidraTrustedServer.Kr);
	}
	
	private byte[] constructEncryptedRestOfMessage() {
		byte[] message = new byte [34];
		for(int i = 0 ; i < Ksr.length; i++) {
			message[i] = Ksr [i];
		}
		for(int i = 0 ; i < nonceSR.length; i++) {
			message[16+i] = nonceSR[i];
		}
		byte[] nonce2 = HidraTrustedServer.securityProperties.get(subjectId).getNonce2();
//		System.out.println("Nonce2 before encryption: " +HidraUtility.byteArrayToHexString(nonce2));
		for(int i = 0 ; i < nonce2.length; i++) {
			message[24+i] = nonce2[i];
		}
		for(int i = 0 ; i < idR.length; i++) {
			message[32+i] = idR[i];
		}
		
		char[] Kscm = new char[16];
		byte [] byteKscm = HidraTrustedServer.securityProperties.get(subjectId).getKSCM();
		for (int i = 0 ; i < 16 ; i++) {
			Kscm[i] = (char) byteKscm[i];
		}
		return HidraUtility.xcrypt(message, Kscm);
	}
}
