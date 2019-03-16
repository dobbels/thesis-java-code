package hidra;

import hidra.HidraUtility.*;

public class HidraObligation {
	/*
	 * A task can be constructed in the same way as a condition expression,
	 * but points to a different reference table in the resource. 
	 */
	private HidraExpression task;
	private Effect fulfillOn;
	private boolean alwaysExecuteTask;
	
	public HidraObligation(HidraExpression task, Effect fulfillOn) {
		this.task = task;
		if (fulfillOn == null) {
			alwaysExecuteTask = true;
		} else {
			alwaysExecuteTask = false;
			this.fulfillOn = fulfillOn;
		}
	}
	
	public void prettyPrint(String startingIndentation, boolean last) {
		System.out.println(startingIndentation + "{"); 
		
		System.out.println(startingIndentation + "\t\"task\" :");
		task.prettyPrint(startingIndentation + "\t\t", true, false);

		System.out.println(startingIndentation + "\t\"fulfillOn\" : \"" + fulfillOn.name() + "\"");
		
		if (last) {
			System.out.println(startingIndentation + "}");
		} else {
			System.out.println(startingIndentation + "},");
		}
	}
}
