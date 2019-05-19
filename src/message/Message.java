package message;

import java.util.ArrayList;

import message.TrustedServerResourceMessage.MessageType;

/**
 * This class implements a Hidra message, containing the necessary identifiers, token, keys and possibly an EBNF policy (codified with APBR codification).
 */
public class Message {

	public Message() {
		
	}
	
	public ArrayList<Boolean> constructBoolMessage() {
		return new ArrayList<>();
	}
}
