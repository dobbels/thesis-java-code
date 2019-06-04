package message;

import hidra.TrustedServer;

public class WhitelistMessage extends PolicyUpdateMessage {
	
	public WhitelistMessage(byte id) {
		super(TrustedServer.securityProperties.get(id).getPseudonym(), TrustedServer.constructPermitPolicy());
	}
}