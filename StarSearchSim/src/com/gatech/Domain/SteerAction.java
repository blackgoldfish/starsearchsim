/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


/**
 * Represents a 'steer' action taken by the drone
 */

public class SteerAction extends Action {
    public SteerAction(Navigation.Orientation orientation) {
        super(ActionType.STEER);
        m_orientation = orientation;
    }

    public Navigation.Orientation getOrientation() {
        return m_orientation;
    }

    public String toString() {
		return "Steer";
	}

    private Navigation.Orientation m_orientation;
}