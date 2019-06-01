package message;

import hidra.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class HidraAnsReq extends TrustedServerSubjectMessage {
	private byte[] idS = new byte[2];
	private byte[] idCm = new byte[2];
	private byte[] lifetimeTGT = new byte[3];
	private byte[] nonce1 = new byte[8];
	
	//Assuming the right structure is given by the subject
	public HidraAnsReq(byte[] message) {
		super();
//		System.out.println("Expected length 15 == "+message.length);
//		System.out.println("HID_ANS_REQ content: "+ Utility.byteArrayToHexString(message));
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
	
	public HidraAnsRep processAndConstructReply(){
		byte[] supposedIdCm = {1,1}; //idCM == 257
		if (Arrays.equals(idCm, supposedIdCm)) {
			return (new HidraAnsRep(idS, idCm, nonce1));
		} else {
			return null;
		}
	}
}