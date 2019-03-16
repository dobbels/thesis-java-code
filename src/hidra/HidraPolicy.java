package hidra;

import hidra.HidraUtility.*;

import java.util.ArrayList;

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
	
	private byte id; // Use of ints is avoided, as on the hidra resource, an integer consists of 2 bytes, while in java that is 4 bytes. 
	private Effect effect;
	private ArrayList<HidraRule> ruleset; 
	
	public HidraPolicy(byte id, Effect e, ArrayList<HidraRule> ruleset){
		this.id = id;
		this.effect = e;
		this.ruleset = ruleset;
	}
	
	public byte[] codify() {
		return codifyUsingJSON().toString().getBytes();
	}
	
//	private byte[] codifyUsingAPBR(byte id, Effect effect, HidraRuleSet ruleset) {
		//TODO zie paper + convertToBytes in HidraMessage
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
	
	// Print the policy instance in a JSON-like structure 
	public void prettyPrint() {
		System.out.println("{");
		System.out.println("\t\"id\" : " + id + ",");
		System.out.println("\t\"effect\" : \"" + effect.name() + "\",");
		System.out.println("\t\"rules\" : [");
		
		for (HidraRule r : ruleset) {
			// To print like JSON, the last rule should not include a comma. 
			if (ruleset.indexOf(r) == ruleset.size() - 1) {
				r.prettyPrint("\t\t", true);
			} else {
				r.prettyPrint("\t\t", false);
			}
		}
		
		System.out.println("\t]");
		System.out.println("}");
	}	
}
