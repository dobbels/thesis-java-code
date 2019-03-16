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
		
		System.out.println(startingIndentation + "\t\"type\" : \"" + type.name() + "\",");
		
		switch(type) {
		  case BOOLEAN:
			  System.out.println(startingIndentation + "\t\"value\" : " + boolValue);
		    break;
		  case BYTE:
			  System.out.println(startingIndentation + "\t\"value\" : " + byteValue);
			    break;
		  case INTEGER:
			  System.out.println(startingIndentation + "\t\"value\" : " + intValue);
			    break;
		  case FLOAT:
			  System.out.println(startingIndentation + "\t\"value\" : " + floatValue);
			    break;
		  case STRING:
			  System.out.println(startingIndentation + "\t\"value\" : \"" + stringValue + "\"");
			  break;
		  case REQUEST_REFERENCE:
			  System.out.println(startingIndentation + "\t\"value\" : \"" + HidraUtility.requestRereferences.get(byteValue) + "\"");
			  break;
		  case SYSTEM_REFERENCE:
			  System.out.println(startingIndentation + "\t\"value\" : \"" + HidraUtility.systemRereferences.get(byteValue) + "\"");
			  break;
		  case LOCAL_REFERENCE:
		    System.out.println(startingIndentation + "\t\"value\" : " + byteValue);
		    break;
		  default:
			  System.out.println("Error: Atrribute type");
		}
		
		if (last) {
			System.out.println(startingIndentation + "}");
		} else {
			System.out.println(startingIndentation + "},");
		}
	}
}
