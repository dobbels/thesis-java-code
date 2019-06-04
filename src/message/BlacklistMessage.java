package message;

import hidra.TrustedServer;
import hidra.Utility;

public class BlacklistMessage extends PolicyUpdateMessage {
	
	public BlacklistMessage(byte id) {
		super(TrustedServer.securityProperties.get(id).getPseudonym(), TrustedServer.constructDenyPolicy());
//		System.out.println("TrustedServer.securityProperties.get(id).getPseudonym(): " 
//		+ Utility.byteArrayToHexString(TrustedServer.securityProperties.get(id).getPseudonym()));
	}
}