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
	
	public byte[] codify() {
		return codifyUsingAPBR();
	}
	
	private byte[] codifyUsingAPBR() { //TODO Using BitSet? Using boolean[]? ArrayList<Boolean>? 
		// Get codification of rules
		
		// Make codification of id, effect and RuleExistenceMask
		
		// Merge codifications
		ArrayList<Boolean> finalCodification;
		
		
		
		---------------------------------------------------------
		// TODO zie paper + convertToBytes in HidraMessage
		byte[] optionData = optionsList.convertToBytes();
		int fixedLength = 236;
		int varLength = optionData.length;
		int totalLength = fixedLength + varLength;
		byte[] byteMessage = new byte[totalLength];

		//----------------------/----------------------------/---------------------------/----------------------------// 4x 1 byte 
		byteMessage[0] = getOp(); byteMessage[1] = getHtype(); byteMessage[2] = getHlen(); byteMessage[3] = getHops();
		//------------------------------------------------------------------------------------------------------------// 1x 4 bytes (int)
		for (int i=0; i < 4; i++) { byteMessage[4+i] = HidraUtility.intToByteArray(getXid())[i];}
		//---------------------------------------------------/--------------------------------------------------------// 2x 2 bytes (short) 
		for (int i=0; i < 2; i++) { byteMessage[8+i] = HidraUtility.shortToByteArray(getSecs())[i];} 
		for (int i=0; i < 2; i++) { byteMessage[10+i] = HidraUtility.shortToByteArray(getFlags())[i];}
		//---------------------------------------------------/--------------------------------------------------------// 1x X bytes (byte[X])
		for (int i=0; i < 4; i++) { byteMessage[12+i] = getCiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[16+i] = getYiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[20+i] = getSiaddr()[i];}
		for (int i=0; i < 4; i++) { byteMessage[24+i] = getGiaddr()[i];}
		for (int i=0; i < 16; i++) { byteMessage[28+i] = getChaddr()[i];}
		for (int i=0; i < 64; i++) { byteMessage[44+i] = getSname()[i];}
		for (int i=0; i < 128; i++) { byteMessage[108+i] = file[i];}
		for (int i=0; i < varLength; i++) {
			byteMessage[fixedLength+i] = optionData[i];
		}

		
		//TODO Pad bitset so its length l mod 8 = 0. Extra bits come at the end, so that they can be easily ignored by the receiver.
		return HidraUtility.booleanArrayToByteArray(finalCodification);
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
