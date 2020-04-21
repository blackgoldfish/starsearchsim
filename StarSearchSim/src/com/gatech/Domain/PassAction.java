/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


/**
 * Represents a 'pass' by the drone
 */
public class PassAction extends Action {
	public PassAction() {
		super(ActionType.PASS);
	}

	public String toString()
	{
		return "Pass";
	}
}
    
