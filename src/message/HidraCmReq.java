package message;

import java.util.Arrays;

import hidra.TrustedServer;
import hidra.Policy;
import hidra.Utility;

public class HidraCmReq extends TrustedServerSubjectMessage {
	private byte[] idR = new byte[2];
	private byte[] pseudonym = new byte[2];
	private byte[] lifetimeTR = new byte[1];
	private byte[] nonce2 = new byte[8];
	private byte[] ticket = new byte[26];
	private byte[] authN = new byte[10];
	private char[] Kscm = new char[16]; 
	private int i;
	
	//Assuming the right structure is given by the subject
	public HidraCmReq(byte subjectId, byte[] message) {
		super();
//		System.out.println("Received HidraCmReq message: "+ Utility.byteArrayToHexString(message));
		for (int i = 0; i < idR.length ; i++) {
			idR[i] = message[i];
		}
		lifetimeTR[0] = message[2];
		for (int i = 0; i < nonce2.length ; i++) {
			nonce2[i] = message[idR.length + lifetimeTR.length + i];
		}		
		
		TrustedServer.securityProperties.get(subjectId).setNonce2(nonce2);
		
		for (int i = 0; i < ticket.length ; i++) {
			ticket[i] = message[idR.length + lifetimeTR.length + nonce2.length + i];
		}
		this.ticket = Utility.xcrypt(this.ticket, TrustedServer.Kcm);
		
		// Through the ticket, the credential manager acquires Kscm 
		//Keys all have values below 0x7f now. Normally other values will work when casted to char, but this remains untested.
		for (int i = 0; i < Kscm.length ; i++) {
			Kscm[i] = (char) ticket[i];
		}
		byte[] temp_ugly_kscm = new byte[16];
		for (int i = 0; i < temp_ugly_kscm.length ; i++) {
			temp_ugly_kscm[i] = (byte) Kscm[i];
		}

		for (int i = 0; i < authN.length ; i++) {
			authN[i] = message[idR.length + lifetimeTR.length + nonce2.length + ticket.length + i];
		}
		
		this.authN = Utility.xcrypt(this.authN, Kscm);  

		for (int i = 0; i < pseudonym.length ; i++) {
			pseudonym[i] = authN[i];
		}
//		System.out.println("subjectPseudonym: " + Utility.byteArrayToHexString(Arrays.copyOfRange(ticket, 16, 18)) + " == " + Utility.byteArrayToHexString(pseudonym));
		
		byte[] noncescm = new byte[8];
		for (int i = 0; i < noncescm.length ; i++) {
			noncescm[i] = ticket[18 + i];
		}
		byte[] noncescm_i = new byte[8];
		for (int i = 0; i < noncescm_i.length ; i++) {
			noncescm_i[i] = authN[2 + i];
		}
		i = calculateI(noncescm, noncescm_i);
//		System.out.println("i: 1 == " + i);
	}
	
	//PoC value of i: max 255 in subject, where it is generated.
	private int calculateI(byte[] n, byte[] ni) {
		int a = (((ni[6] & 0xFF) << 8) |(ni[7] & 0xFF));
		int b = (((n[6] & 0xFF) << 8)|(n[7] & 0xFF));
		System.out.println(a + " and " + b);
		return (a - b);
	}
	
	//Indicates if subject is properly authenticated based on the given AuthN
	private boolean properlyAuthenticated() {
		return true;
	}
	
	private boolean preliminaryAuthorized() {
		return true;
	}
	
	public HidraCmInd processAndConstructReply(Policy hp, byte subjectId){
		if (!properlyAuthenticated() || !preliminaryAuthorized()) {
			return null;//TODO handle this: no message to resource, maybe a nack (= null-byte) to the subject
		} else {
			return (new HidraCmInd(subjectId, idR, lifetimeTR, i, hp.codifyUsingAPBR()));
		}
	}
}
