package message;

import hidra.TrustedServer;

public class DemoPolicyUpdateMessage extends PolicyUpdateMessage {
	
	public DemoPolicyUpdateMessage(byte id) {
		super(TrustedServer.securityProperties.get(id).getPseudonym(), TrustedServer.constructDemoPolicy());
	}
}