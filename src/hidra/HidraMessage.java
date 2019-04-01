package hidra;

import hidra.HidraUtility.Effect;

import java.util.ArrayList;
import java.util.Random;


/**
 * This class implements a Hidra message, containing the necessary identifiers, token, keys and possibly an EBNF policy (codified with APBR codification).
 */
public class HidraMessage {
	
	private byte id;

	//TODO constructor met bvb alleen id en dan subklasses voor elk soort bericht? Of zelfs geen gemeenschappelijke velden?
	// 		OF
	//TODO gewoon static functies maken : constructAnsReq, constructAnsRep enz -> natuurlijk alleen van diegene die je in Subject en ACS nodig hebt! 

	public HidraMessage(){
		
	}
	
	public byte[] constructByteMessage() {
		return null;
	}//TODO in Utility (?) : HidraMessage parse()
}
