package message;

import hidra.HidraUtility;

import java.util.ArrayList;

public class HidraAnsReq extends HidraACSSubjectMessage {
	private byte[] idS = new byte[2];
	private byte[] idCm = new byte[2];
	private byte[] lifetimeTGT = new byte[3];
	private byte[] nonce1 = new byte[8];
	
	//Assuming the right structure is given by the subject
	public HidraAnsReq(byte[] message) {
		super();
		System.out.println("Expected length 15 == "+message.length);
		System.out.println("Received HidraAnsReq message: "+ HidraUtility.bytesToHex(message));
		for (int i = 0; i < idS.length ; i++) {
			idS[i] = message[i];
		}
		for (int i = 0; i < idCm.length ; i++) {
			idCm[i] = message[idS.length+i];
		}		
		for (int i = 0; i < lifetimeTGT.length ; i++) {
			lifetimeTGT[i] = message[idS.length + idCm.length + i];
		}
		for (int i = 0; i < nonce1.length ; i++) {
			nonce1[i] = message[idS.length + idCm.length + lifetimeTGT.length + i];
		}
	}
	
	public ArrayList<Boolean> processAndConstructReply(){
		return (new HidraAnsRep(idS, idCm, nonce1)).constructBoolMessage();
	}
}