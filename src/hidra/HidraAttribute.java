package hidra;

import java.util.ArrayList;

import hidra.HidraUtility.*;

public class HidraAttribute {
	
	private AttributeType type; //TODO gebruik polymorfisme. Dit lijkt handig om handlen van attribute doenbaar te houden.
	private boolean boolValue;
	private String stringValue;
	private short intValue; // should be 2 bytes according to Hidra documentation 
	private float floatValue;
	
	// Used for all remaining types, as in the current codification, they all take a byte. 
	// Note: local references should be codified in the end using only 3 bits, so this number should be between 0 and 7
	private byte byteValue;

	
	public HidraAttribute(boolean b) {
		this.type = AttributeType.BOOLEAN;
		this.boolValue = b;
	}
	
	public HidraAttribute(String s) {
		if (s.length() <= 6) {
			this.type = AttributeType.STRING;
			this.stringValue = s;
		} else {
			System.out.println("Error: String should have maximum length 6");
		}
	}
	
	public HidraAttribute(short i) {
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
	
	public ArrayList<Boolean> codifyUsingAPBR() {
		
		// Type id [0-7]
		byte typeId = 8;
		switch(type) {
		  case BOOLEAN:
			  typeId = 0;
			  break;
		  case BYTE:
			  typeId = 1;
			  break;
		  case INTEGER:
			  typeId = 2;
			  break;
		  case FLOAT:
			  typeId = 3;
			  break;
		  case STRING:
			  typeId = 4;
			  break;
		  case REQUEST_REFERENCE:
			  typeId = 5;
			  break;
		  case SYSTEM_REFERENCE:
			  typeId = 6;
			  break;
		  case LOCAL_REFERENCE:
			  typeId = 7;
			  break;
		  default:
			  System.out.println("Error: Atrribute type");
		}
		
		ArrayList<Boolean> codification  = HidraUtility.byteToBoolList(typeId, 3);
		
		// Value of attribute
		switch(type) {
		  case BOOLEAN:
			  codification.add(boolValue);
			  break;
		  case INTEGER:
			  codification.addAll(HidraUtility.intToBoolList(intValue));
			  break;
		  case FLOAT:
			  codification.addAll(HidraUtility.floatToBoolList(floatValue));
			  System.out.println("Float representation of 69.456");
			  HidraUtility.printBoolList(HidraUtility.floatToBoolList(floatValue));
			  break;
		  case STRING:
			  codification.addAll(HidraUtility.byteToBoolList((byte)stringValue.length(),3));
			  codification.addAll(HidraUtility.stringToBoolList(stringValue));
			  break;
		  case LOCAL_REFERENCE:
			  codification.addAll(HidraUtility.byteToBoolList(byteValue,3));
			  break;
		  default:
			  codification.addAll(HidraUtility.byteToBoolList(byteValue));
		}
		
		return codification;
	}
}
