package message;

import java.util.ArrayList;

import hidra.HidraACS;
import hidra.HidraPolicy;
import hidra.HidraUtility;

public class HidraCmReq extends HidraACSSubjectMessage {
	private byte[] idR = new byte[2];
	private byte[] idS = new byte[2];
	private byte[] lifetimeTR = new byte[1];
	private byte[] nonce2 = new byte[8];
	private byte[] ticket = new byte[26];
	private byte[] authN = new byte[10];
	private char[] Kscm = new char[16]; 
	private int i;
	
	//Assuming the right structure is given by the subject
	public HidraCmReq(byte subjectId, byte[] message) {
		super();
		System.out.println("Expected length 47 == "+message.length);
		System.out.println("Received HidraCmReq message: "+ HidraUtility.byteArrayToHexString(message));
		for (int i = 0; i < idR.length ; i++) {
			idR[i] = message[i];
		}
		lifetimeTR[0] = message[2];
		for (int i = 0; i < nonce2.length ; i++) {
			nonce2[i] = message[idR.length + lifetimeTR.length + i];
		}		

		System.out.println("Nonce2: " + HidraUtility.byteArrayToHexString(nonce2));
		
		System.out.println("Getting properties of subject " + subjectId);
		HidraACS.securityProperties.get(subjectId).setNonce2(nonce2);
		
		for (int i = 0; i < ticket.length ; i++) {
			ticket[i] = message[idR.length + lifetimeTR.length + nonce2.length + i];
		}
		this.ticket = HidraUtility.xcrypt(this.ticket, HidraACS.Kcm);
		// Through the ticket, the credential manager acquires Kscm 
		//Keys all have values below 0x7f now. Normally other values will work when casted to char, but this remains untested.
		for (int i = 0; i < Kscm.length ; i++) {
			Kscm[i] = (char) ticket[i];
		}
		byte[] temp_ugly_kscm = new byte[16];
		for (int i = 0; i < temp_ugly_kscm.length ; i++) {
			temp_ugly_kscm[i] = (byte) Kscm[i];
		}
//		System.out.println("Kscm after decryption: " + HidraUtility.bytesToHex(temp_ugly_kscm));
//		System.out.println("Stored Kscm , for comparison: " + HidraUtility.bytesToHex(HidraACS.properties.get(3).getKSCM()));

		for (int i = 0; i < authN.length ; i++) {
			authN[i] = message[idR.length + lifetimeTR.length + nonce2.length + ticket.length + i];
		}
		
//		System.out.println("authN before decryption: " + HidraUtility.bytesToHex(this.authN));
		this.authN = HidraUtility.xcrypt(this.authN, Kscm);  
//		System.out.println("authN after decryption: " + HidraUtility.bytesToHex(this.authN));
		
		for (int i = 0; i < idS.length ; i++) {
//			System.out.println("authN[" + i +"] = "+authN[i]);
			idS[i] = authN[i];
		}
		System.out.println("subjectId: " + subjectId + " == " + idS[1]);
		
		byte[] noncescm = new byte[8];
		for (int i = 0; i < noncescm.length ; i++) {
			noncescm[i] = ticket[18 + i];
		}
		byte[] noncescm_i = new byte[8];
		for (int i = 0; i < noncescm_i.length ; i++) {
//			System.out.println("authN[" + (2 + i) +"] = "+authN[2+i]);
			noncescm_i[i] = authN[2 + i];
		}
		i = calculateI(noncescm, noncescm_i);
		System.out.println("i: 1 == " + i);
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
	
	public byte[] processAndConstructReply(HidraPolicy hp){
		if (!properlyAuthenticated() || !preliminaryAuthorized()) {
			return null;//TODO handle this: no message to resource, maybe a nack (= null-byte) to the subject
		} else {
			return (new HidraCmInd(idR, idS, lifetimeTR, i, hp.codify())).constructCmInd();
		}
	}
}
