package message;

import hidra.TrustedServer;

public class BigPolicyUpdateMessage extends PolicyUpdateMessage {
	
	public BigPolicyUpdateMessage(byte id) {
		super(TrustedServer.securityProperties.get(id).getPseudonym(), TrustedServer.constructSuperSet1());
	}
}
