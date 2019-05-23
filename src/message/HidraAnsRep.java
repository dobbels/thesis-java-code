package message;

import hidra.TrustedServer;
import hidra.SubjectSecurityProperties;
import hidra.Utility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class HidraAnsRep extends TrustedServerSubjectMessage {
	private byte[] idS;
	private byte[] idCm;
	private byte[] nonce1;
	private byte[] ticketCm = new byte[26]; 
	private byte[] nonceSCm = new byte[8];
	private byte[] Kscm = new byte[16];
	private byte[] pseudonym = new byte[2];
	
	public HidraAnsRep(byte[] idS, byte[] idCm, byte[] nonce1) {
		super();
		this.idS = idS;
		this.idCm = idCm;
		this.nonce1 = nonce1;
		
		// Noncescm generation
        new Random().nextBytes(this.nonceSCm);

		//Kscm generation
        new Random().nextBytes(this.Kscm);
        
        // Store information for this subject 
        TrustedServer.securityProperties.put(idS[1], new SubjectSecurityProperties(this.Kscm, this.nonceSCm));
        
        //Generate and store unique pseudonym
        do {
        	new Random().nextBytes(this.pseudonym);
        } while (!TrustedServer.isUniquePseudonym(this.pseudonym));
//        System.out.println("Pseudonym for subject " + idS[1] + " is: " + Utility.byteArrayToHexString(this.pseudonym));
        TrustedServer.securityProperties.get(idS[1]).setPseudonym(this.pseudonym);
        
		//TGT generation ciphered with Kcm
        ticketCm = constructUnEncryptedTicket();
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		byte[] restOfMessage = constructUnencryptedRestOfMessage();
		
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(Utility.byteArrayToBooleanList(idS));
		codification.addAll(Utility.byteArrayToBooleanList(Utility.xcrypt(this.ticketCm, TrustedServer.Kcm)));
		codification.addAll(Utility.byteArrayToBooleanList(Utility.xcrypt(restOfMessage, Utility.getSubjectKey((char)idS[1]))));
		return codification;
	}
	
	private byte[] constructUnEncryptedTicket() {
		byte[] ticket = new byte [26];
		for(int i = 0 ; i < Kscm.length; i++) {
			ticket[i] = Kscm[i];
		}
		for(int i = 0 ; i < pseudonym.length; i++) {
			ticket[16+i] = pseudonym[i];
		}
		for(int i = 0 ; i < nonceSCm.length; i++) {
			ticket[18+i] = nonceSCm[i];
		}
		return ticket;
	}
	
	private byte[] constructUnencryptedRestOfMessage() {
		byte[] ticket = new byte [36];
		for(int i = 0 ; i < Kscm.length; i++) {
			ticket[i] = Kscm[i];
		}
		for(int i = 0 ; i < nonceSCm.length; i++) {
			ticket[16+i] = nonceSCm[i];
		}
		for(int i = 0 ; i < nonce1.length; i++) {
			ticket[24+i] = nonce1[i];
		}
		for(int i = 0 ; i < idCm.length; i++) {
			ticket[32+i] = idCm[i];
		}
		for(int i = 0 ; i < pseudonym.length; i++) {
			ticket[34+i] = pseudonym[i];
		}
		return ticket;
	}
}
