package hidra;

import hidra.Utility.*;

import java.util.ArrayList;

public class PolicyExpression {
	
	private byte function;
	private ArrayList<PolicyAttribute> inputset;
	private boolean attributesExist = false;
	
	public PolicyExpression(byte function, ArrayList<PolicyAttribute> inputset) {
		this.function = function;
		if (inputset != null) {
			this.inputset = inputset;
			attributesExist = true;
		}
	}
	
	public void prettyPrint(String startingIndentation, boolean isTask, boolean last) {
		System.out.println(startingIndentation + "{"); 

		if (attributesExist) {
			// With the comma at the end
			if (isTask) {
				System.out.println(startingIndentation + "\t\"function\" : \"" + Utility.taskRereferences.get(function) + "\",");
			} else {
				System.out.println(startingIndentation + "\t\"function\" : \"" + Utility.expressionRereferences.get(function) + "\",");
			}
			
			System.out.println(startingIndentation + "\t\"inputset\" : [");
			
			for (PolicyAttribute e : inputset) {
				// To print like JSON, the last rule should not include a comma. 
				if (inputset.indexOf(e) == inputset.size() - 1) {
					e.prettyPrint(startingIndentation + "\t\t", true);
				} else {
					e.prettyPrint(startingIndentation + "\t\t", false);
				}
			}
			
			System.out.println(startingIndentation + "\t]");
		} else {
			//Without the comma at the end. 
			if (isTask) {
				System.out.println(startingIndentation + "\t\"function\" : \"" + Utility.taskRereferences.get(function) + "\",");
			} else {
				System.out.println(startingIndentation + "\t\"function\" : \"" + Utility.expressionRereferences.get(function) + "\",");
			}
		}
		
		if (last) {
			System.out.println(startingIndentation + "}");
		} else {
			System.out.println(startingIndentation + "},");
		}
	}
	
	public ArrayList<Boolean> codifyUsingAPBR() {
		//Function id
		ArrayList<Boolean> codification  = Utility.byteToBoolList(function);
		
		//InputExistenceMask
		if (!attributesExist) {
			codification.add(false);
		} else {
			codification.add(true);
		
			// MaxInputIndex 
			byte maxInputIndex = (byte) (inputset.size() - 1);
			// Should be a number between 0 and 7 => only add last 3 booleans
			codification.addAll(Utility.byteToBoolList(maxInputIndex, 3));
			
			// At least one obligation
			for (PolicyAttribute a : inputset) {
				codification.addAll(a.codifyUsingAPBR());
			}
		}
		return codification;
	}
}
