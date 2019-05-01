package message;

import hidra.HidraACS;
import hidra.HidraSubjectsSecurityProperties;
import hidra.HidraUtility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class HidraAnsRep extends HidraACSSubjectMessage {
	private byte[] idS;
	private byte[] idCm;
	private byte[] nonce1;
	private byte[] ticketCm = new byte[26]; 
	private byte[] nonceSCm = new byte[8];
	private byte[] Kscm = new byte[16];
	
	public HidraAnsRep(byte[] idS, byte[] idCm, byte[] nonce1) {
		super();
		this.idS = idS;
		this.idCm = idCm;
		this.nonce1 = nonce1;
		
		// Noncescm generation
        new Random().nextBytes(this.nonceSCm);
//        System.out.println("nonceSCm: "+HidraUtility.bytesToHex(nonceSCm));

		//Kscm generation
        new Random().nextBytes(this.Kscm);
//        System.out.println("Kscm: "+HidraUtility.bytesToHex(Kscm));
        
        // Store information for this subject 
        HidraACS.properties.put((int) idS[1], new HidraSubjectsSecurityProperties(this.Kscm, this.nonceSCm));
		
		//TGT generation ciphered with Kcm
        ticketCm = constructUnEncryptedTicket(Kscm, idS, nonceSCm);
        
//		System.out.println("Initial text: " + HidraUtility.bytesToHex(ticketCm));
//		byte[] encrypted_text = HidraUtility.xcrypt(ticketCm, HidraACS.Ks);
//		System.out.println("(En/De)crypted text: " + HidraUtility.bytesToHex(encrypted_text));
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		byte[] restOfMessage = constructUnencryptedRestOfMessage(this.Kscm, this.nonceSCm, this.nonce1, this.idCm);
		
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteArrayToBooleanList(idS));
		codification.addAll(HidraUtility.byteArrayToBooleanList(HidraUtility.xcrypt(this.ticketCm, HidraACS.Kcm)));
//		System.out.println("restOfMessage before encryption: "+HidraUtility.bytesToHex(restOfMessage));
		codification.addAll(HidraUtility.byteArrayToBooleanList(HidraUtility.xcrypt(restOfMessage, HidraACS.Ks)));
//		System.out.println(HidraUtility.bytesToHex(HidraUtility.booleanArrayToByteArray(codification)));
		return codification;
	}
	
	private byte[] constructUnEncryptedTicket(byte[] kscm, byte[] ids, byte[] noncescm) {
		byte[] ticket = new byte [26];
		for(int i = 0 ; i < kscm.length; i++) {
			ticket[i] = kscm [i];
		}
		for(int i = 0 ; i < ids.length; i++) {
			ticket[16+i] = ids [i];
		}
		for(int i = 0 ; i < noncescm.length; i++) {
			ticket[18+i] = noncescm [i];
		}
		return ticket;
	}
	
	private byte[] constructUnencryptedRestOfMessage(byte[] kscm, byte[] noncescm, byte[] nonce1, byte[] idcm) {
		byte[] ticket = new byte [34];
		for(int i = 0 ; i < kscm.length; i++) {
			ticket[i] = kscm [i];
		}
		for(int i = 0 ; i < noncescm.length; i++) {
			ticket[16+i] = noncescm [i];
		}
		for(int i = 0 ; i < nonce1.length; i++) {
			ticket[24+i] = nonce1 [i];
		}
		for(int i = 0 ; i < idcm.length; i++) {
			ticket[32+i] = idcm [i];
		}
		return ticket;
	}
}
