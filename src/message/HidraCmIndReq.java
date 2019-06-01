package message;

import hidra.TrustedServer;
import hidra.Utility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HidraCmIndReq extends HidraProtocolMessage {
	private byte[] idR = new byte[2];
	private byte[] nonce3 = new byte[8];
	private byte[] mac = new byte[4];
	private byte[] hash;

	public HidraCmIndReq(byte[] message) {
		idR = Arrays.copyOfRange(message, 0, 2);
		nonce3 = Arrays.copyOfRange(message, 2, 10);
		mac = Arrays.copyOfRange(message, 10, 14);
		
		hash = Utility.compute4ByteMac(Arrays.copyOfRange(message, 0, 10));
		for (int i = 0; i < mac.length; i ++) {
			if (hash[i] != mac[i]) {
				System.out.println("Error: violated integrity");
			}
		}
	}
	
	public HidraCmIndRep processAndConstructReply() {
		if (Arrays.equals(hash, mac)) {
			return new HidraCmIndRep(idR, nonce3);
		} else {
			return null;
		}
	}
}
