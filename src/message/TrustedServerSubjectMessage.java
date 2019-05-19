package message;

import hidra.Utility;
import hidra.TrustedServer;

public class TrustedServerSubjectMessage extends Message {

	public TrustedServerSubjectMessage() {
		super();
	}
	
	public void send(int subjectId){
		byte[] packet = Utility.booleanArrayToByteArray(constructBoolMessage());
		TrustedServer.sendDataPacket(packet,Utility.getSubjectIP(subjectId), TrustedServer.socketForSubject, Utility.getServerSubjectPort());
	}
}
