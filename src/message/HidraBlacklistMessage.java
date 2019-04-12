package message;

import hidra.HidraUtility;

import java.util.ArrayList;

import message.HidraPolicyAdaptMessage.UpdateType;


public class HidraBlacklistMessage extends HidraPolicyAdaptMessage{
	
	public HidraBlacklistMessage(byte id) {
		super(id, UpdateType.BLACKLIST);
	}
}