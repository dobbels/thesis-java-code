package message;

import hidra.HidraTrustedServer;
import hidra.HidraUtility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HidraCmIndReq extends HidraProtocolMessage {
	private byte[] idR = new byte[2];
	private byte[] nonce3 = new byte[8];
	private byte[] mac = new byte[4];

	public HidraCmIndReq(byte[] message) {
		idR = Arrays.copyOfRange(message, 0, 2);
		nonce3 = Arrays.copyOfRange(message, 2, 10);
		mac = HidraUtility.xcrypt(Arrays.copyOfRange(message, 10, 14), HidraTrustedServer.Kr);
		
		byte[] hash = new byte[4];
		try {
			hash = HidraUtility.hashTo4Bytes(HidraUtility.getMD5Hash(Arrays.copyOfRange(message, 0, 10)));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < mac.length; i ++) {
			if (hash[i] != mac[i]) {
				System.out.println("Error: violated integrity");
			}
		}
	}
	
	public HidraCmIndRepMessage constructResponse() {
		return new HidraCmIndRepMessage(idR, nonce3);
	}
}
