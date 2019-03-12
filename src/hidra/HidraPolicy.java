package hidra;

import org.json.*;

/**
 * Implements a Hidra policy and specifies functions to codify them using APBR codification, as described in https://ieeexplore.ieee.org/abstract/document/7990134. 
 * 
 * In the paper these are represented using EBNF. In Java and C, they will be repeated using custom implementations.
 * EBNF only has a use in the paper for specification of the policy. 
 * 
 * First codify using JSON? (Makes no sense to first do it using JSON, as packets will be far too big, >> 127 bytes?)
 * Then APBR? 
 */
public class HidraPolicy {
	//TODO codificatie hier als functie? Best aparte klasse? 
	
	private byte id; // Use of ints is avoided, as on the hidra resource, an integer consists of 2 bytes, while in java that is 4 bytes. 
	private Effect effect;
	private HidraRuleSet ruleset; 
	
	enum Effect {
		  DENY,
		  PERMIT
		}
	
	public HidraPolicy(){
		// id is constant for now
		id = 1;
		effect = Effect.PERMIT;
		ruleset = new HidraRuleSet();
	}
	
	public byte[] codify() {
		return codifyUsingJSON().toString().getBytes();
	}
	
//	private byte[] codifyUsingAPBR(byte id, Effect effect, HidraRuleSet ruleset) {
		//TODO zie convertToBytes in HidraMessage
//	}
	
	private JSONObject codifyUsingJSON() {
		JSONObject obj = new JSONObject();

	      obj.put("id", id);
	      obj.put("effect", effect.name());
	      
	      if (!ruleset.isEmpty()) {
	    	  System.out.println("Insert rules, not yet implemented");
	      }

	      return obj;
	}
}
