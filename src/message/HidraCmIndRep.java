package message;

import hidra.TrustedServer;
import hidra.Utility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class HidraCmIndRep extends HidraProtocolMessage {

	private byte[] idR = new byte[2];
	private byte[] Kircm = new byte[16];
	private byte[] nonce3 = new byte[8];
	private byte[] mac = new byte[4];
	
	public HidraCmIndRep(byte[] idR, byte[] nonce3) {
		super();
		this.idR = idR;
		this.nonce3 = nonce3;
		this.Kircm = TrustedServer.getNextKeyChainValue();

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
		this.mac = Utility.compute4ByteMac(messageToMac);
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(Utility.byteArrayToBooleanList(idR));
		codification.addAll(Utility.byteArrayToBooleanList(Kircm));
		codification.addAll(Utility.byteArrayToBooleanList(mac));
		return codification;
	}
}
