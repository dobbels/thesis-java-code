package message;

import hidra.HidraACS;
import hidra.HidraUtility;

import java.util.ArrayList;

//Upon reception of every authentication request message, first, it checks if the message is for the ANS; 
//cthen, it checks the identity of the subject on the
//repository, as shown in Listing 5. In the positive case, it generates a long-term ticket granting
//ticket (TGT) ciphered with the CM’s secret key KCM. It also cyphers with the subject’s secret
//key KS a data block containing the shared key KS,CM and a set of nonces for further sequence
//validation. It also records such a TGT in the active connection repository with a NonceS,CM
//and the corresponding lifetime.
public class HidraAnsRep extends HidraACSSubjectMessage {
	private int idS;
	private int ticketCm;
	private String kSCm; //TODO of byte[16]
	private int nonceSCm;
	private int nonce1;
	private int idCm;
	
	public HidraAnsRep(int idS, int idCm, int nonce1) {
		super();
		this.idS = idS;
		this.idCm = idCm;
		this.nonce1 = nonce1;
		//TGT generation ciphered with Kcm
		
		//Kscm and Noncescm generation
		//TODO https://www.tutorialspoint.com/java/util/random_nextbytes.htm of andere java util random
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		//TODO 
		return codification;
	}
}
