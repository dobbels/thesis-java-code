package message;

import hidra.HidraUtility;

import java.util.ArrayList;

public class HidraAnsReq extends HidraACSSubjectMessage {

	//int type to be sure of unsigned handling up to 3 bytes
	private int idS;
	private int idCm;
	private int lifetimeTGT;
	private int nonce1;
	
	//Assuming the right structure is given by the subject
	public HidraAnsReq(byte[] message) {
		super();
		System.out.println("Expected length 4 == "+message.length);
		this.idS = message[0];
		System.out.println("idS: "+idS);
		this.idCm = message[1];
		System.out.println("idCM: "+idCm);
		this.lifetimeTGT = message[2];
		System.out.println("lifetimeTGT: "+lifetimeTGT);
		this.nonce1 = message[3];
		System.out.println("nonce1: "+nonce1);
	}
	
	public ArrayList<Boolean> processMessage(){
		return (new HidraAnsRep(idS, idCm, nonce1)).constructBoolMessage();
	}
}