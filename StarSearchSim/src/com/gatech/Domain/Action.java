/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


import java.util.HashMap;

/**
 * This abstract class represents an action to be taken by a drone. Possible actions are:
 * 1. Scan
 * 2. Pass
 * 3. Steer
 * 4. Thrust
 */
public abstract class Action {
	public Action(ActionType action) {
		m_action = action;
	}

    /**
     * Returns the action to be taken by a drone
     *
     * @return Action
     */
	public ActionType getAction() {
		return m_action;
	}

    /**
     * Returns an action type based on a string description.
     *
     * @param  str Action in string format
     * @return Action type, or null if no matches were found.
     */
	public static ActionType fromString(String str) {
        return STRING_TO_ACTION.get(str);
    }

    /**
     * Returns a string representation for the action. Used for output purposes.
     */
	public String toString()
	{
		if (m_action == ActionType.SCAN) {
			return "Scan";
		}

		return "Pass";
    }
    
	public enum ActionType { SCAN, PASS, STEER, THRUST }

	private ActionType m_action;
    private static final String[] ACTION_STR = {"Scan", "Pass", "Steer", "Thrust"};
	private static HashMap<String, ActionType> STRING_TO_ACTION;

	static {
        STRING_TO_ACTION = new HashMap<>();
        for (int i = 0; i < ActionType.values().length; i++) {
            STRING_TO_ACTION.put(ACTION_STR[i], ActionType.values()[i]);
        }
	}
}