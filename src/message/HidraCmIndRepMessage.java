package message;

import hidra.HidraACS;
import hidra.HidraUtility;

import java.util.ArrayList;
import java.util.Arrays;

public class HidraCmIndRepMessage extends HidraProtocolMessage {

	private byte[] idR = new byte[2];
	private byte[] Kircm = new byte[16];
	private byte[] nonce3 = new byte[8];
	private byte[] mac = new byte[4];
	
	public HidraCmIndRepMessage(byte[] idR, byte[] nonce3) {
		super();
		this.idR = idR;
		this.nonce3 = nonce3;
		this.Kircm = HidraACS.getNextKeyChainValue();
		System.out.println("idR in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(idR));
		System.out.println("nonce3 in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(nonce3));
		System.out.println("Kircm in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(Kircm));

		byte[] key_and_message = new byte[20];
		for (int i = 0; i < HidraACS.Kr.length ; i++ ) {
			key_and_message[i] = (byte) HidraACS.Kr[i];
		}
		for (int i = 0; i < this.idR.length ; i++ ) {
			key_and_message[i+16] = this.idR[i];
		}
		for (int i = 0; i < 2 ; i++ ) {
			key_and_message[i+18] = this.nonce3[i];
		}

//		for (int i = 0; i < nonce3.length ; i++ ) {
//			key_and_message[i+18] = nonce3[i];
//		}
//		for (int i = 0; i < Kircm.length ; i++ ) {
//			key_and_message[i+26] = Kircm[i];
//		}
		this.mac = HidraUtility.hashTo4Bytes(key_and_message);
		System.out.println("MAC in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(mac));
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.byteArrayToBooleanList(idR));
		codification.addAll(HidraUtility.byteArrayToBooleanList(Kircm));
		codification.addAll(HidraUtility.byteArrayToBooleanList(mac));
		return codification;
	}
}
