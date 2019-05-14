package message;

import hidra.HidraTrustedServer;
import hidra.HidraUtility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
		this.Kircm = HidraTrustedServer.getNextKeyChainValue();
//		System.out.println("idR in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(idR));
//		System.out.println("nonce3 in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(nonce3));
//		System.out.println("Kircm in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(Kircm));

		byte[] messageToMac = new byte[26];
		for (int i = 0; i < this.idR.length ; i++ ) {
			messageToMac[i] = this.idR[i];
		}
		for (int i = 0; i < nonce3.length ; i++ ) {
			messageToMac[i+2] = this.nonce3[i];
		}

		for (int i = 0; i < Kircm.length ; i++ ) {
			messageToMac[i+10] = this.Kircm[i];
		}
		try {
			this.mac = HidraUtility.xcrypt(HidraUtility.hashTo4Bytes(HidraUtility.getMD5Hash(messageToMac)), HidraTrustedServer.Kr);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("MAC in HID_CM_IND_REP: "+ HidraUtility.byteArrayToHexString(mac));
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
