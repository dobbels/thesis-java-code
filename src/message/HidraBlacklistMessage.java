package message;

import hidra.Utility;

import java.util.ArrayList;

import message.PolicyUpdateMessage.UpdateType;


public class HidraBlacklistMessage extends PolicyUpdateMessage{
	
	public HidraBlacklistMessage(byte id) {
		super(id, UpdateType.BLACKLIST);
	}
}