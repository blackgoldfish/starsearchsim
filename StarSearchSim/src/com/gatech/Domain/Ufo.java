/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;

import java.util.Random;
import java.util.Scanner;

/**
 * Represents a drone in our simulation system
 */

public class Ufo {
	public Ufo(Point point, Navigation.Orientation orientation, Region region) {
		m_point = point;
		m_orientation = orientation;
        m_status = Status.OK;
        m_action = null;

        m_region = region;
	}
    
    /**
     * Gets the drone's position in space
     *
     * @return Drone's location
     */
    public Point getPoint()
	{
		return m_point;
	}

    /**
     * Sets a drone's position in space. Also updates the internal map to reflect the change in position.
     *
     * @param  newPoint New position
     */
	public void setPosition(Point newPoint)
	{
        m_point = newPoint;
	}

    /**
     * Steers the drone in a given direction
     *
     * @param  orientation New direction the drone will point to
     */
	public void setDirection(Navigation.Orientation orientation)
	{
		m_orientation = orientation;
	}

    /**
     * Gets current orientation for the drone
     *
     * @return Drone orientation
     */
	public Navigation.Orientation getDirection()
	{
		return m_orientation;
	}
	
   /**
     * Returns the status for the drone (currently, OK and CRASH)
     *
     * @return Drone status
     */
    public Status getStatus()
	{
		return m_status;
	}

    /**
     * Used by the simulator to inform the drone it's its turn to move and ask for its next action. 
     * 
     * The action taken by the drone will depend upon the current strategy:
     * 1. Smart strategy: Drone uses combined knowledge about the space region in order to identify unexplored parts.
     * 2. Human input: Operators are able to instruct each drone directly.
     * 3. Drone will choose an action at random.
     *
     * @return nextAction taken by the drone.
     */
    public Action getNextAction() {
        Action nextAction = null;
    
        // // Human input

        // // generate a move by asking the user - DIAGNOSTIC ONLY
        // System.out.println("UFO " + m_id + " Dir: " + m_orientation);
        // System.out.print("action?: ");
        // String userAction = m_scanner.nextLine();

        // DroneAction.Action droneAction = DroneAction.fromString(userAction);
        // if (droneAction != null)
        // {
        //     switch (droneAction) {
        //         case STEER:
        //             System.out.print("direction?: ");
        //             action = new SteerAction(Navigation.fromString(m_scanner.nextLine()));
        //             break;

        //         case THRUST:
        //             System.out.print("distance?: ");
        //             action = new ThrustAction(Integer.parseInt(m_scanner.nextLine()));
        //             break;

        //         case SCAN:
        //             action = new ScanAction();
        //             break;

        //         case PASS:
        //             action = new PassAction();
        //             break;
        //     }
        // }

        // Drone scans and decides
        if (m_subStrategy == SubStrategy.STEER) {
            if (m_randGenerator.nextInt(5) > 0) {
                // Perform thrust
                nextAction = new ThrustAction(1);
                m_subStrategy = SubStrategy.ASSESS;
            } else {
                nextAction = new PassAction();
            }
            m_action = nextAction;

            return nextAction;
        }

        if (m_subStrategy == SubStrategy.ASSESS) {
            // Consult the map and see if there are any drones nearby to zap
            Navigation.Orientation orientation = findNearestDrone();

            if (orientation != null) {
                // Drone found, head towards it
                if (orientation != m_orientation) {
                    nextAction = new SteerAction(orientation);
                    m_subStrategy = SubStrategy.STEER;
                    m_action = nextAction;

                    return nextAction;
                } else {
                    if (m_randGenerator.nextInt(5) > 0) {
                        // Perform thrust
                        nextAction = new ThrustAction(1);
                        m_subStrategy = SubStrategy.ASSESS;
                    } else {
                        nextAction = new PassAction();
                    }
                    m_action = nextAction;

                    return nextAction;
                }
            } else {
                // No drones nearby, pick a safe one to move to
                orientation = chooseSafeSquare();
                if (orientation != null) {
                    nextAction = new SteerAction(orientation);
                    m_subStrategy = SubStrategy.STEER;
                    m_action = nextAction;

                    return nextAction;
                } else {
                    // No safe square to move to, pass
                    nextAction = new PassAction();

                    // Get the UFO to reevaluate its strategy at the next turn
                    m_subStrategy = SubStrategy.ASSESS;
                }
            }
        }
        m_action = nextAction;

        return nextAction;
    }
    /**
     * Gets the UFO's stored action without repeating getNextAction()
     */
    public Action getAction(){return m_action;}

    /**
     * Gets the UFO's status in string form. Used for outputting drone data.
     */
    public String getStatusString()
    {
        return m_status.toString().toLowerCase();
    }

    /**
     * Finds the first square that contains stars around the drone, starting from the northside square and
     * proceeding clockwise.
     *
     * @return First square with a drone found, or null if no square contains a drone.
     */
    private Navigation.Orientation findNearestDrone() {        
        for (Navigation.Orientation lookThisWay : Navigation.Orientation.values()) {
            Point point = m_point.getTranslation(lookThisWay, 1);

            if (m_region.inRange(point)) {
                if (m_region.isDrone(point)) {
                    return lookThisWay;
                } 
            }
        }
    
        return null;
    }

    /**
     * Chooses a safe square to move to in the vicinity of the UFO. A safe square is one that is empty or contains stars.
     * The square to move to is chosen at random. 
     * This is used when the drone has complete knowledge of what's around it and doesn't have any star fields to explore.
     *
     * @return A safe square chosen at random, or null if no movement is possible. For example, a drone is completely
     * surrounded by suns and other drones.
     */
    private Navigation.Orientation chooseSafeSquare()
    {
        Navigation.Orientation[] orientations = new Navigation.Orientation[Navigation.Orientation.values().length];

        int i = 0;
        for (Navigation.Orientation lookThisWay : Navigation.Orientation.values()) {
            Point point = m_point.getTranslation(lookThisWay, 1);

            if (m_region.inRange(point)) {
                if (m_region.isEmpty(point) || m_region.isStars(point) || m_region.isWormhole(point)) {                    
                    orientations[i] = lookThisWay;
                    i++;
                } 
            }
        }

        if (i > 0) {
            return orientations[m_randGenerator.nextInt(i)];
        }

        return null;    
    }

	public enum Status { OK, CRASH }

    private Point m_point;
    private Navigation.Orientation m_orientation;
    private Status m_status;
    private static Random m_randGenerator = new Random();
    private Region m_region;
    private Action m_action;

    private enum SubStrategy { ASSESS, STEER, SCAN }
    private SubStrategy m_subStrategy = SubStrategy.ASSESS;
}
