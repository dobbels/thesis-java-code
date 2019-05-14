package message;

import hidra.HidraTrustedServer;
import hidra.HidraUtility;

import java.util.Arrays;

public class HidraCmIndReq extends HidraProtocolMessage {
	private byte[] idR = new byte[2];
	private byte[] nonce3 = new byte[8];
	private byte[] mac = new byte[4];

	public HidraCmIndReq(byte[] message) {
		idR = Arrays.copyOfRange(message, 0, 2);
		nonce3 = Arrays.copyOfRange(message, 2, 10);
		mac = Arrays.copyOfRange(message, 10, 14);
		
		byte[] key_and_message = new byte[20];
		for (int i = 0; i < HidraTrustedServer.Kr.length ; i++ ) {
			key_and_message[i] = (byte) HidraTrustedServer.Kr[i];
		}
		byte[] m = Arrays.copyOfRange(message, 0, 4);
		for (int i = 0; i < m.length ; i++ ) {
			key_and_message[i+16] = m[i];
		}
		byte[] hash = HidraUtility.hashTo4Bytes(key_and_message);
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
