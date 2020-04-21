/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;

import java.util.Random;
import java.util.Scanner;

/**
 * Represents a drone in our simulation system
 */

public class Drone {
	public Drone(Integer id, Point point, Navigation.Orientation orientation, Integer strategy) {
        m_id = id;
		m_point = point;
		m_orientation = orientation;
		m_strategy = strategy;
        m_status = Status.OK;
        m_action = null;
        m_region.setDrone(point, id);
	}
    public void setAction(Action action){
	    m_action = action;
    }
    public int getID(){
        return m_id;
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
        m_region.setEmpty(m_point);
        m_point = newPoint;
        m_region.setDrone(newPoint, m_id);
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
     * Used by the simulator to inform the drone that it has hit a sun.
     * This updates the drone's status and updates shared region map.
     *
     * @param  targetPoint Location of the sun that destroyed the drone
     */
    public void informVaporization(Point targetPoint)
	{
        m_status = Status.CRASH;
        
        // Let everyone know that a sun was here
        m_region.setSun(targetPoint);

        // Erase drone from map
        m_region.setEmpty(m_point);
	}

    /**
     * Used by the simulator to inform a drone that it has crashed into another drone.
     * This updates the drone's status and updates shared region map.
     */
    public void informCrash()
	{
        m_status = Status.CRASH;
        
        // Erase drone from map
        m_region.setEmpty(m_point);

        // m_scanner.close();
	}

    /**
     * Used by the simulator to inform the drone that it has entered a wormhole.
     * This updates the drone's status and updates shared region map.
     *
     * @param  point1 Location of entry wormhole
     * @param  point1 Location of exit wormhole
     */
    public void informWormhole(Point point1, Point point2) {
        m_region.setWormholes(point1, point2);
    }

    public Action getAction(){
        return m_action;               //next action has been calculated but do not want to repeat calculation
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
        int moveRandomChoice, thrustRandomChoice, steerRandomChoice;

        Action nextAction = null;
        
        if (m_strategy == 2) {
            // Manual strategy, managed by simulator
        } else if (m_strategy == 1) {
            // Strategy 1: Drone scans and decides

            if (m_subStrategy == SubStrategy.STEER) {
                // See if a drone just moved into the target square before proceeding with the thrust
                Point target = m_point.getTranslation(m_orientation, 1);

                if (m_region.isDrone(target) || m_region.isUfo(target)) {
                    // Another drone is there, reevaluate where to move now
                    m_subStrategy = SubStrategy.ASSESS;
                } else {
                    // Perform thrust
                    int thrust = determineMaxThrust();
                    nextAction = new ThrustAction(thrust);
                    m_subStrategy = SubStrategy.ASSESS;
                    m_action = nextAction;

                    return nextAction;
                }
            }

            if (m_subStrategy == SubStrategy.ASSESS) {
                // Consult the map and see if there are any known squares left to explore around the drone
                Navigation.Orientation orientation = findFirstUnexploredCachedSquare();

                if (orientation != null) {
                    if (orientation != m_orientation) {
                        // At least one unexplored square found, explore it
                        nextAction = new SteerAction(orientation);
                        m_subStrategy = SubStrategy.STEER;
                        m_action = nextAction;

                        return nextAction;
                    } else {
                        // Perform thrust
                        int thrust = determineMaxThrust();
                        nextAction = new ThrustAction(thrust);
                        m_subStrategy = SubStrategy.ASSESS;
                        m_action = nextAction;

                        return nextAction;
                    }
                } else if (anyCachedUnknownSquares()) {
                    m_subStrategy = SubStrategy.SCAN;
                } else {
                    // All cached squares known, pick a safe one to move to
                    orientation = chooseSafeSquare();
                    if (orientation != null) {
                        nextAction = new SteerAction(orientation);
                        m_subStrategy = SubStrategy.STEER;
                        m_action = nextAction;

                        return nextAction;
                    } else {
                        // No safe square to move to, pass
                        nextAction = new PassAction();

                        // Get the drone to reevaluate its strategy at the next turn
                        m_subStrategy = SubStrategy.ASSESS;
                    }
                }
            }

            // Cached information didn't help, switch to scan mode
            if (m_subStrategy == SubStrategy.SCAN)
            {
                nextAction = new ScanAction();
                m_subStrategy = SubStrategy.ASSESS;
                m_action = nextAction;

                return nextAction;

            }
        } else {
            // generate a move randomly
            moveRandomChoice = m_randGenerator.nextInt(100);
            
            if (moveRandomChoice < 5) {
                // Do nothing
                nextAction = new PassAction();
            } else if (moveRandomChoice < 20) {
                // Check your surroundings
                nextAction = new ScanAction();
            } else if (moveRandomChoice < 50) {
                // Change direction
                steerRandomChoice = m_randGenerator.nextInt(8);
                nextAction = new SteerAction(Navigation.Orientation.values()[steerRandomChoice]);
            } else {
                // Thrust forward
                thrustRandomChoice = m_randGenerator.nextInt(3);
                nextAction = new ThrustAction(thrustRandomChoice + 1);
            }
        }
        m_action = nextAction;
        
        return nextAction;
    }

    private int determineMaxThrust() {
        int maxThrust = 1;

        for (int thrust = 2; thrust <= MAX_THRUST; thrust++) {
            Point point = m_point.getTranslation(m_orientation, thrust);

            if (m_region.inRange(point)) {
                if (m_region.isStars(point)) {
                    maxThrust = thrust;
                } else if (m_region.isEmpty(point)) {
                    maxThrust = thrust;
                    break;
                } else {
                    // Any other object/unknown found, we don't want to venture there. Break loop.
                    break;
                }
            } else {
                // Found a barrier
                break;
            }
        }

        return maxThrust;
    }

    /**
     * Used by the simulator to send scan results to the drone that requested it
     *
     * @param  scan List of space objects around the drone in clockwise fashion, beginning with North.
     */
    public void sendScanResults(int[] scan)
    {
        // Add knowledge gained by the scan to the shared region
        m_region.addScanResults(m_point, scan);

        // Reassess the drone's situation on the next turn
        m_subStrategy = SubStrategy.ASSESS;
    }

    /**
     * Displays the shared knowledge of the space region. For debugging purposes only.
     */
    public void render() {
        m_region.render();
    }

    /**
     * Gets the drone's status in string form. Used for outputting drone data.     
     */
    public String getStatusString()
    {
        return m_status.toString().toLowerCase();
    }

    /**
     * Finds the first square that contains stars around the drone, starting from the northside square and
     * proceeding clockwise.
     *
     * @return First square with stars found, or null if no square contains a star field.
     */
    private Navigation.Orientation findFirstUnexploredCachedSquare() {
        // First explore squares in the direction we're facing, starting from the drone's position
        // until the location that can be reached by using maximum thrust
        boolean foundStars = false;
        for (int thrust = 1; thrust <= MAX_THRUST; thrust++) {
            Point point = m_point.getTranslation(m_orientation, thrust);

            if (m_region.inRange(point)) {
                if (m_region.isStars(point)) {
                    foundStars = true;
                } else if (m_region.isSun(point) || m_region.isDrone(point)){
                    // Any other object found, we don't want to venture there. Break loop.
                    break;
                } else if (m_region.isEmpty(point) || m_region.isWormhole(point)) {
                    // We're not that interested into empty space or wormholes, keep looking
                    continue;
                }
            }
            else
            {
                // Found a barrier
                break;
            }
        }

        // If we found stars in the current direction, keep it.
        if (foundStars)
        {
            return m_orientation;
        }

        for (Navigation.Orientation lookThisWay : Navigation.Orientation.values()) {
            Point point = m_point.getTranslation(lookThisWay, 1);

            if (m_region.inRange(point)) {
                if (m_region.isStars(point)) {
                    return lookThisWay;
                } 
            }
        }
    
        return null;
    }

    /**
     * Looks for the first unknown square around the drone. An unknown square is defined as one for which
     * the drone still doesn't have information on what's in it. This is used in order to find nearby regions
     * to explore after scanning.
     *
     * @return First unknown square found, or null if no unknown squares are nearby.
     */
    private Boolean anyCachedUnknownSquares() {        
        for (Navigation.Orientation lookThisWay : Navigation.Orientation.values()) {
            Point point = m_point.getTranslation(lookThisWay, 1);

            if (m_region.inRange(point)) {
                if (m_region.isUnknown(point)) {
                    return Boolean.TRUE;
                } 
            }
        }
    
        return Boolean.FALSE;
    }

    /**
     * Chooses a safe square to move to in the vicinity of the drone. A safe square is one that is empty or contains stars.
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
                if (m_region.isDrone(point) == false && (m_region.isEmpty(point) || m_region.isStars(point) || m_region.isWormhole(point))) {
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

    public int getStrategy() {
        return m_strategy;
    }

    public enum Status { OK, CRASH }

    private final int MAX_THRUST = 3;

    //private Scanner m_scanner = new Scanner(System.in);
    private int m_id;
    private Action m_action;
    private Point m_point;
    private Navigation.Orientation m_orientation;
    private Integer m_strategy;
    private Status m_status;
    private static Random m_randGenerator = new Random();
    private DroneRegion m_region = DroneRegion.getRegion();

    private enum SubStrategy { ASSESS, STEER, SCAN }
    private SubStrategy m_subStrategy = SubStrategy.ASSESS;
}
