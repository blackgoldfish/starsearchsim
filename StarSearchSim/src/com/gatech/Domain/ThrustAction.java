/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


/**
 * Represents a 'thrust' action taken by the drone
 */
public class ThrustAction extends Action {
    public ThrustAction(int thrust) {
        super(ActionType.THRUST);
        m_thrust = thrust;
    }

    public int getThrust() {
        return m_thrust;
    }

    public String toString()
	{
		return "Thrust";
	}

    private int m_thrust;
}