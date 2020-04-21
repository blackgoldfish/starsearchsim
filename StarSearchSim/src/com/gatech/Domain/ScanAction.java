/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


/**
 * Represents a 'scan' action performed by the drone
 */
public class ScanAction extends Action {
	public ScanAction() {
		super(ActionType.SCAN);
	}

	public String toString()
	{
		return "scan";
	}
}