package message;

import hidra.HidraACS;
import hidra.HidraSubjectsSecurityProperties;
import hidra.HidraUtility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class HidraCmRep extends HidraACSSubjectMessage {
	//Totaal bytes van bericht: 62 bytes. Crypto door subject op 34
	private byte[] idS = new byte[2];
	byte[] idR = {0,2};
	//ksr + ids + noncesr ( + attributes)
	private byte[] ticketR = new byte[26]; 
	private byte[] Ksr = new byte[16];
	private byte[] nonceSR = new byte[8];
	// Ksr + ids + noncesr + nonce2 + idr
	private byte[] restOfMessage = new byte[34];

	public HidraCmRep(byte subjectId) {
		super();
		this.idS[0] = 0;
		this.idS[1] = subjectId;
		
		new Random().nextBytes(this.nonceSR);
		
		new Random().nextBytes(this.Ksr);

        ticketR = constructEncryptedTicket();
        
        restOfMessage = constructEncryptedRestOfMessage();
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteArrayToBooleanList(idS));
		codification.addAll(HidraUtility.byteArrayToBooleanList(ticketR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(restOfMessage));
		return codification;
	}
	
	private byte[] constructEncryptedTicket() {
		byte[] ticket = new byte [26];
		for(int i = 0 ; i < Ksr.length; i++) {
			ticket[i] = Ksr [i];
		}
		for(int i = 0 ; i < idS.length; i++) {
			ticket[16+i] = idS [i];
		}
		for(int i = 0 ; i < nonceSR.length; i++) {
			ticket[18+i] = nonceSR[i];
		}
		//No attributes in this implementation
		return HidraUtility.xcrypt(ticket, HidraACS.Kr);
	}
	
	private byte[] constructEncryptedRestOfMessage() {
		byte[] message = new byte [34];
		for(int i = 0 ; i < Ksr.length; i++) {
			message[i] = Ksr [i];
		}
		for(int i = 0 ; i < nonceSR.length; i++) {
			message[16+i] = nonceSR[i];
		}
		byte[] nonce2 = HidraACS.securityProperties.get(idS[1]).getNonce2();
		System.out.println("Nonce2 before encryption: " +HidraUtility.byteArrayToHexString(nonce2));
		for(int i = 0 ; i < nonce2.length; i++) {
			message[24+i] = nonce2[i];
		}
		for(int i = 0 ; i < idR.length; i++) {
			message[32+i] = idR[i];
		}
		
		char[] Kscm = new char[16];
		byte [] byteKscm = HidraACS.securityProperties.get(idS[1]).getKSCM();
		for (int i = 0 ; i < 16 ; i++) {
			Kscm[i] = (char) byteKscm[i];
		}
		return HidraUtility.xcrypt(message, Kscm);
	}
}
