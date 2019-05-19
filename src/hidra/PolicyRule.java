package hidra;

import hidra.Utility.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PolicyRule {
	
	private byte id;
	private Effect effect;
	private byte periodicity;
	private byte iteration;
	private byte resource;
	private Action action;
	private ArrayList<PolicyExpression> conditionset;
	private ArrayList<PolicyObligation> obligationset;
	
	/**
	 * Indices 0 to 4: periodicity, iteration, resource, action, obligationset
	 * At the moment on receiving a zero-byte or null, this flag is set to false, otherwise true 
	 */
	private ArrayList<Boolean> optionFlags = new ArrayList<Boolean>(Arrays.asList(false,false,false,false,false));
	
	public PolicyRule(byte id, Effect effect, byte periodicity, byte iteration, byte resource, 
			Action action, ArrayList<PolicyExpression> conditionset, ArrayList<PolicyObligation> obligationset) {
		
		// Mandatory fields
		this.id = id;
		this.effect = effect;
		this.conditionset = conditionset;

		// Optional fields		
		if (periodicity != 0) {
			this.periodicity = periodicity;
			optionFlags.set(0, true);
		}
		if (iteration != 0) {
			this.iteration = iteration;
			optionFlags.set(1, true);
		}
		if (resource != 0) {
			this.resource = resource;
			optionFlags.set(2, true);
		}
		if (action != null) {
			this.action = action;
			optionFlags.set(3, true);
		}
		if (obligationset != null) {
			this.obligationset = obligationset;
			optionFlags.set(4, true);
		}
	}
	
	public void prettyPrint(String startingIndentation, boolean last) {
		System.out.println(startingIndentation + "{"); 
		
		System.out.println(startingIndentation + "\t\"id\" : " + id + ",");
		
		System.out.println(startingIndentation + "\t\"effect\" : \"" + effect.name() + "\",");
		
		if (optionFlags.get(0)) {
			System.out.println(startingIndentation + "\t\"periodicity\" : " + periodicity + ",");
		}
		if (optionFlags.get(1)) {
			System.out.println(startingIndentation + "\t\"iteration\" : " + iteration + ",");
		}
		if (optionFlags.get(2)) {
			System.out.println(startingIndentation + "\t\"resource\" : " + resource + ",");
		}
		if (optionFlags.get(3)) {
			System.out.println(startingIndentation + "\t\"action\" : \"" + action.name() + "\",");
		}
		
		System.out.println(startingIndentation + "\t\"conditionset\" : [");
		
		for (PolicyExpression e : conditionset) {
			// To print like JSON, the last rule should not include a comma. 
			if (conditionset.indexOf(e) == conditionset.size() - 1) {
				e.prettyPrint(startingIndentation + "\t\t", false, true);
			} else {
				e.prettyPrint(startingIndentation + "\t\t", false, false);
			}
		}
		
		System.out.println(startingIndentation + "\t]");

		if (optionFlags.get(4)) {
			System.out.println(startingIndentation + "\t\"obligationset\" : [");
			
			for (PolicyObligation o : obligationset) {
				// To print like JSON, the last rule should not include a comma. 
				if (obligationset.indexOf(o) == obligationset.size() - 1) {
					o.prettyPrint(startingIndentation + "\t\t", true);
				} else {
					o.prettyPrint(startingIndentation + "\t\t", false);
				}
			}
			
			System.out.println(startingIndentation + "\t]");
		}
		
		if (last) {
			System.out.println(startingIndentation + "}");
		} else {
			System.out.println(startingIndentation + "},");
		}
	}
	
	public ArrayList<Boolean> codifyUsingAPBR() {
		//Rule id
		ArrayList<Boolean> codification  = Utility.byteToBoolList(id);
		
		//Policy effect
		if (effect == Effect.PERMIT) {
			codification.add(true);
		} else {
			codification.add(false);
		}
		
		//Logical jumpers
		codification.addAll(optionFlags);
		
		// Periodicity in minutes		
		if (optionFlags.get(0)) {
			codification.addAll(Utility.byteToBoolList(periodicity));
		}
		
		// Repetitions
		if (optionFlags.get(1)) {
			codification.addAll(Utility.byteToBoolList(iteration));
		}
		
		// Resource id
		if (optionFlags.get(2)) {
			codification.addAll(Utility.byteToBoolList(resource));
		}
		
		// Action id [0-4]
		if (optionFlags.get(3)) {
			byte actionId = 8;
			if (action == Action.GET) {
				actionId = 0;
			} else if (action == Action.POST) {
				actionId = 1;
			} else if (action == Action.PUT) {
				actionId = 2;
			} else if (action == Action.DELETE) {
				actionId = 3;
			} else if (action == Action.ANY) {
				actionId = 4;
			} else {
				System.out.println("Error: did not find action type.");
			}
			
			// Should be a number between 0 and 7 => only add last 3 booleans
			ArrayList<Boolean> actionIdList = Utility.byteToBoolList(actionId);
			for (int i = 5 ; i < 8 ; i++) {
				codification.add(actionIdList.get(i));
			}
		}
		
		// MaxExpressionIndex 
		byte maxExpressionIndex = (byte) (conditionset.size() - 1);
		// Should be a number between 0 and 7 => only add last 3 booleans
		codification.addAll(Utility.byteToBoolList(maxExpressionIndex, 3));
		
		// At least one expression
		for (PolicyExpression e : conditionset) {
			codification.addAll(e.codifyUsingAPBR());
		}
		
		if (optionFlags.get(4)) {
			// MaxExpressionIndex 
			byte maxObligationIndex = (byte) (obligationset.size() - 1);
			// Should be a number between 0 and 7 => only add last 3 booleans
			codification.addAll(Utility.byteToBoolList(maxObligationIndex, 3));
			
			// At least one obligation
			for (PolicyObligation o : obligationset) {
				codification.addAll(o.codifyUsingAPBR());
			}
		}
		return codification;
	}
}
