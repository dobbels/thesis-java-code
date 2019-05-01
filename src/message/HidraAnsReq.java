package message;

import hidra.HidraACS;
import hidra.HidraUtility;

import java.util.ArrayList;
import java.util.Arrays;

import encryption.Alice;

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
		// Some shortcuts because some bytes are not used. They are included to copy the Hidra protocol as good as possible. 
		for (int i = 0; i < idS.length ; i++) {
			idS[i] = message[i];
		}
		for (int i = 0; i < idCm.length ; i++) {
			idCm[i] = message[i+2];
		}		
		for (int i = 0; i < lifetimeTGT.length ; i++) {
			lifetimeTGT[i] = message[i+4];
		}
		for (int i = 0; i < nonce1.length ; i++) {
			nonce1[i] = message[i+7];
		}
	}
	
	public ArrayList<Boolean> processAndConstructReply(){
		return (new HidraAnsRep(idS, idCm, nonce1)).constructBoolMessage();
	}
}