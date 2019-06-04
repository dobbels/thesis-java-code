package message;

import hidra.TrustedServer;

public class MediumPolicyUpdateMessage extends PolicyUpdateMessage {
	
	public MediumPolicyUpdateMessage(byte id) {
		super(TrustedServer.securityProperties.get(id).getPseudonym(), TrustedServer.constructSubset3());
	}
}