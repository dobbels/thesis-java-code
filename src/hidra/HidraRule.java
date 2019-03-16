package hidra;

import hidra.HidraUtility.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HidraRule {
	
	private byte id;
	private Effect effect;
	private byte periodicity;
	private byte iteration;
	private byte resource;
	private Action action;
	private ArrayList<HidraExpression> conditionset;
	private ArrayList<HidraObligation> obligationset;
	
	/**
	 * Indices 0 to 4: periodicity, iteration, resource, action, obligationset
	 * At the moment on receiving a zero-byte or null, this flag is set to false, otherwise true 
	 */
	private ArrayList<Boolean> optionFlags = new ArrayList<Boolean>(Arrays.asList(false,false,false,false,false));
	
	public HidraRule(byte id, Effect effect, byte periodicity, byte iteration, byte resource, 
			Action action, ArrayList<HidraExpression> conditionset, ArrayList<HidraObligation> obligationset) {
		
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
		
		
		
		if (last) {
			System.out.println(startingIndentation + "}");
		} else {
			System.out.println(startingIndentation + "},");
		}
	}
}
