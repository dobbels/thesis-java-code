package hidra;

import hidra.HidraUtility.*;

import java.util.ArrayList;
import java.util.BitSet;

import org.json.*;

/**
 * Implements a Hidra policy and specifies functions to codify them using APBR codification, as described in https://ieeexplore.ieee.org/abstract/document/7990134. 
 * 
 * In the paper these are represented using EBNF. In Java and C, they will be repeated using custom implementations.
 * This can be extended to be parsed from a file with EBNF instance specification as this eases testing a lot.
 * 
 * At the moment lengths of constructs are fixed, together with the APBR codification to fit with the current use case. 
 * In a real scenario, the PDM can be parametrized and length of policy and codification can be adapted to the particular situation. 
 * 
 * WARNING: in Java byte runs from -127 tot 127, not from 0 to 255. This of course limits the current implementation in terms of number of policies and such.
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
	
	public ArrayList<Boolean> codify() {
		return codifyUsingAPBR();
	}
	
	private ArrayList<Boolean> codifyUsingAPBR() { 
		//Policy id
		ArrayList<Boolean> codification  = HidraUtility.byteToBoolList(id);
		
		//Policy effect
		if (effect == Effect.PERMIT) {
			codification.add(true);
		} else {
			codification.add(false);
		}
		
		//RuleExistenceMask
		if (ruleset == null) {
			codification.add(false);
		} else {
			codification.add(true);
			
			// MaxRuleIndex 
			byte maxRuleIndex = (byte) (ruleset.size() - 1);
			// Should be a number between 0 and 7 => only add last 3 booleans
			codification.addAll(HidraUtility.byteToBoolList(maxRuleIndex, 3));
			
			// Get codification of rules
			for (HidraRule r : ruleset) {
				codification.addAll(r.codifyUsingAPBR());
			}
		}		
		// For debugging
		System.out.println("Policy after codification (with length " + codification.size() +"):");
		HidraUtility.printBoolList(codification);
		return codification;
	}
	
	// Print the policy instance in a JSON-like structure 
	public void prettyPrint() {
		System.out.println("{");
		System.out.println("\t\"id\" : " + id + ",");
		if (ruleset != null) {
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
		} else {
			System.out.println("\t\"effect\" : \"" + effect.name() + "\"");
			System.out.println("}");
		}
		
	}	
	
//	private JSONObject codifyUsingJSON() { //TODO this + JSON pretty print would have been waaaay cleaner than the pretty printing you implemented yourself. Remember
//		JSONObject obj = new JSONObject();
//
//	      obj.put("id", id);
//	      obj.put("effect", effect.name());
//	      
//	      if (!ruleset.isEmpty()) {
//	    	  System.out.println("Insert rules, not yet implemented");
//			  //Put rules array in the object
//	      }
//
//	      return obj;
//	}
}
