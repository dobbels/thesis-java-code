package message;

import java.util.ArrayList;

import message.HidraPolicyAdaptMessage.UpdateType;
import hidra.HidraExpression;
import hidra.HidraUtility;
import hidra.HidraUtility.Action;

public class HidraAccessRequest extends HidraSubjectMessage {

	Action action;
	HidraExpression exp;
	
	public HidraAccessRequest(byte id, Action action, HidraExpression exp) {
		super(HidraSubjectMessage.MessageType.ACCESS_REQUEST, id);
		this.action = action;
		this.exp = exp;
	}
	
	@Override
	public ArrayList<Boolean> constructBoolMessage() {
		ArrayList<Boolean> codification = super.constructBoolMessage();
		codification.addAll(HidraUtility.actionToBoolList(action));
		codification.addAll(exp.codifyUsingAPBR());
		return codification;
	}

}
