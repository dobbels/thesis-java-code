package message;

import hidra.TrustedServer;
import hidra.SubjectSecurityProperties;
import hidra.Utility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class HidraCmRep extends TrustedServerSubjectMessage {
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
		pseudonym = TrustedServer.securityProperties.get(subjectId).getPseudonym();
		
		this.nonceSR = TrustedServer.securityProperties.get(subjectId).getNonceSR();
		
		new Random().nextBytes(this.Ksr);

        ticketR = constructEncryptedTicket();
        
        restOfMessage = constructEncryptedRestOfMessage();
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		System.out.println("Pseudonym for subject " + subjectId + " is: " + Utility.byteArrayToHexString(this.pseudonym));
		codification.addAll(Utility.byteArrayToBooleanList(pseudonym));
		codification.addAll(Utility.byteArrayToBooleanList(ticketR));
		codification.addAll(Utility.byteArrayToBooleanList(restOfMessage));
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
		//No attributes in this implementation
		return Utility.xcrypt(ticket, TrustedServer.Kr);
	}
	
	private byte[] constructEncryptedRestOfMessage() {
		byte[] message = new byte [34];
		for(int i = 0 ; i < Ksr.length; i++) {
			message[i] = Ksr [i];
		}
		for(int i = 0 ; i < nonceSR.length; i++) {
			message[16+i] = nonceSR[i];
		}
		byte[] nonce2 = TrustedServer.securityProperties.get(subjectId).getNonce2();
//		System.out.println("Nonce2 before encryption: " +HidraUtility.byteArrayToHexString(nonce2));
		for(int i = 0 ; i < nonce2.length; i++) {
			message[24+i] = nonce2[i];
		}
		for(int i = 0 ; i < idR.length; i++) {
			message[32+i] = idR[i];
		}
		
		char[] Kscm = new char[16];
		byte [] byteKscm = TrustedServer.securityProperties.get(subjectId).getKSCM();
		for (int i = 0 ; i < 16 ; i++) {
			Kscm[i] = (char) byteKscm[i];
		}
		return Utility.xcrypt(message, Kscm);
	}
}
