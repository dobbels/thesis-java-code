package message;

public class HidraHidSRReq extends HidraSubjectMessage {

	public HidraHidSRReq(byte id) {
		super(HidraSubjectMessage.MessageType.HIDRA_PROTOCOL, id);
	}

}
