package hidra;

import hidra.HidraUtility.*;

import java.util.ArrayList;

public class HidraExpression {
	
	private byte function;
	private ArrayList<HidraAttribute> inputset;
	private boolean attributeExistenceMask = false;
	
	public HidraExpression(byte function, ArrayList<HidraAttribute> inputset) {
		this.function = function;
		if (inputset != null) {
			this.inputset = inputset;
			attributeExistenceMask = true;
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
