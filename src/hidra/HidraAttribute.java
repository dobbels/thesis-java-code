package hidra;

import hidra.HidraUtility.*;

public class HidraAttribute {
	
	private AttributeType type; //TODO gebruik polymorfisme. Dit lijkt handig om handlen van attribute doenbaar te houden.
	private boolean boolValue;
	private String stringValue;
	private int intValue;
	private float floatValue;
	
	// Used for all remaining types, as in the current codification, they all take a byte. 
	// Note: local references should be codified in the end using only 3 bits, so this number should be between 0 and 7
	private byte byteValue;

	
	public HidraAttribute(boolean b) {
		this.type = AttributeType.BOOLEAN;
		this.boolValue = b;
	}
	
	public HidraAttribute(String s) {
		this.type = AttributeType.STRING;
		this.stringValue = s;
	}
	
	public HidraAttribute(int i) {
		this.type = AttributeType.INTEGER;
		this.intValue = i;
	}
	
	public HidraAttribute(float f) {
		this.type = AttributeType.FLOAT;
		this.floatValue = f;
	}
	
	public HidraAttribute(AttributeType type, byte b) {
		this.type = type;
		this.byteValue = b;
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
