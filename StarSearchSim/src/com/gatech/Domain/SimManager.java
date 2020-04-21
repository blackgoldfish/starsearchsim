//import java.util.Scanner;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.io.*;
//
///**
// * This class performs the general coordination of the drones and overall control of the simulation process, including validating drone actions.
// */
//public class SimManager {
//    public SimManager(String configurationFile, boolean verbose) {
//    	m_verbose = verbose;
//        m_drones = new ArrayList<>();
//        m_ufos = new ArrayList<>();
//        m_turnLimit = -1;
//        m_turnsCompleted = 0;
//
//        uploadStartingFile(configurationFile);
//
//        // Temp code until we read this from the config file
//        m_region.setWormholes(new Point(3, 2), new Point(2, 0));
//        m_region.setUfo(new Point(2, 2), 0);
//        m_ufos.add(new Ufo(0, new Point(2, 2), Navigation.Orientation.N, m_region));
//    }
//
//    /**
//     * Reads in a configuration file with initial configuration for the simulation
//     *
//     * @param  testFileName File name that contains simulation parameters
//     */
//    private void uploadStartingFile(String testFileName) {
//        final String DELIMITER = ",";
//
//        try {
//            Scanner takeCommand = new Scanner(new File(testFileName));
//
//            // read in the region information
//            String[] tokens;
//            tokens = takeCommand.nextLine().split(DELIMITER);
//            int regionWidth = Integer.parseInt(tokens[0]);
//            tokens = takeCommand.nextLine().split(DELIMITER);
//            int regionHeight = Integer.parseInt(tokens[0]);
//
//            m_region = new Region(regionWidth, regionHeight);
//
//            // read in the drone starting information
//            tokens = takeCommand.nextLine().split(DELIMITER);
//            int numberOfDrones = Integer.parseInt(tokens[0]);
//            int k;
//            for (k = 0; k < numberOfDrones; k++) {
//                tokens = takeCommand.nextLine().split(DELIMITER);
//                Navigation.Orientation orientation = Navigation.fromString(tokens[2]);
//                if (orientation == null) {
//                    orientation = Navigation.Orientation.N;
//                }
//
//                Point point = new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
//            	m_drones.add(new Drone(k, point, orientation, Integer.parseInt(tokens[3])));
//
//                // Explore the stars at the initial location
//                m_region.setDrone(point, k);
//            }
//
//            // read in the sun information
//            tokens = takeCommand.nextLine().split(DELIMITER);
//            int numSuns = Integer.parseInt(tokens[0]);
//            for (k = 0; k < numSuns; k++) {
//                tokens = takeCommand.nextLine().split(DELIMITER);
//
//                // place a sun at the given location
//                m_region.setSun(new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])));
//            }
//
//            tokens = takeCommand.nextLine().split(DELIMITER);
//            m_turnLimit = Integer.parseInt(tokens[0]);
//
//            takeCommand.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println();
//        }
//    }
//
//    /**
//     * Runs the simulation for the number of specified turns.
//     * Simulation may end up early if all drones are destroyed or if the whole space region has been explored.
//     */
//    public void run() {
//	    // run the simulation for a fixed number of steps
//	    for(int turns = 0; turns < m_turnLimit; turns++) {
//	        m_turnsCompleted = turns;
//
//	        if (allDronesStopped() || m_region.getStats().getNumStars() == 0) {
//	        	break;
//	        }
//
//            // Move drones
//	        for (int k = 0; k < m_drones.size(); k++) {
//	            if (droneStopped(k)) {
//	            	continue;
//	            }
//
//                // Ask drone for its next action
//	            DroneAction action = m_drones.get(k).getNextAction();
//	            validateDroneAction(k, action);
//	            displayActionAndResponses(k, action);
//            }
//
//            // Move UFOs
//	        for (int k = 0; k < m_ufos.size(); k++) {
//                // Ask ufo for its next action
//	            DroneAction action = m_ufos.get(k).getNextAction();
//	            validateUfoAction(k, action);
//	            displayActionAndResponses(k, action);
//            }
//        }
//    }
//
//    /**
//     * Validates proposed action by the drone, including chosen orientation and thrust length.
//     *
//     * @param id     Drone ID
//     * @param action Action proposed by the drone
//     */
//    public void validateDroneAction(int id, DroneAction action) {
//        Drone drone = m_drones.get(id);
//
//        if (action != null) {
//            if (action.getAction() == DroneAction.Action.SCAN) {
//                // in the case of a scan, return the information for the eight surrounding squares
//                // always use a northbound orientation
//                m_trackScanResults = m_region.scan(drone.getPoint());
//                m_drones.get(id).sendScanResults(m_trackScanResults);
//            } else if (action.getAction() == DroneAction.Action.PASS) {
//                // If drone is over a wormhole, send it back.
//                if (m_region.isWormhole(drone.getPoint()))
//                {
//                    sendThroughWormhole(drone, drone.getPoint());
//                }
//            } else if (action.getAction() == DroneAction.Action.STEER) {
//                Navigation.Orientation orientation = ((SteerAction)action).getOrientation();
//                if (orientation != null) {
//                    m_drones.get(id).setDirection(orientation);
//                } else {
//                    System.out.println("action_not_recognized");
//                }
//            } else if (action.getAction() == DroneAction.Action.THRUST) {
//                int thrust = ((ThrustAction)action).getThrust();
//                if (thrust > 0 && thrust <= MAX_THRUST) {
//                    // in the case of a thrust, ensure that the move doesn't cross suns or barriers
//                    Navigation.Orientation orientation = drone.getDirection();
//                    int remainingThrust = thrust;
//
//                    while (remainingThrust > 0 && drone.getStatus() == Drone.Status.OK) {
//                        Point newPoint = drone.getPoint().getTranslation(orientation, 1);
//
//                        if (m_region.isBarrier(newPoint)) {
//                            // drone hit a barrier and simply doesn't move (do nothing)
//                        } else if (m_region.isSun(newPoint)) {
//                            // drone hit a sun
//                            drone.informVaporization(newPoint);
//                            m_region.setEmpty(drone.getPoint());
//                        } else if (m_region.isDrone(newPoint)) {
//                            int id2 = m_region.getDroneID(newPoint);
//                            if (id2 >= 0)
//                            {
//                                // Another drone is here, destroy both drones
//                                drone.informCrash();
//                                m_drones.get(id2).informCrash();
//
//                                // Update region
//                                m_region.setEmpty(drone.getPoint());
//                                m_region.setEmpty(m_drones.get(id2).getPoint());
//                            }
//                        } else if (m_region.isUfo(newPoint)) {
//                            // Destroy drone
//                            drone.informCrash();
//
//                            // Update region
//                            m_region.setEmpty(drone.getPoint());
//                        } else if (m_region.isWormhole(newPoint)) {
//                            sendThroughWormhole(drone, newPoint);
//                        } else {
//                            // Update region status
//                            m_region.setEmpty(drone.getPoint());
//                            drone.setPosition(newPoint);
//                            m_region.setDrone(newPoint, id);
//                        }
//
//                        remainingThrust--;
//                    }
//                } else {
//                    System.out.println("action_not_recognized");
//                }
//            } else {
//                System.out.println("action_not_recognized");
//            }
//        }
//    }
//
//    /**
//     * Validates proposed action by the UFO, including chosen orientation and thrust length.
//     *
//     * @param id     UFO ID
//     * @param action Action proposed by the UFO
//     */
//    public void validateUfoAction(int id, DroneAction action) {
//        Ufo ufo = m_ufos.get(id);
//
//        if (action != null) {
//            if (action.getAction() == DroneAction.Action.PASS) {
//                // If UFO is over a wormhole, send it back.
//                if (m_region.isWormhole(ufo.getPoint()))
//                {
//                    sendThroughWormhole(ufo, ufo.getPoint());
//                }
//            } else if (action.getAction() == DroneAction.Action.STEER) {
//                Navigation.Orientation orientation = ((SteerAction)action).getOrientation();
//                if (orientation != null) {
//                    m_ufos.get(id).setDirection(orientation);
//                } else {
//                    System.out.println("action_not_recognized");
//                }
//            } else if (action.getAction() == DroneAction.Action.THRUST) {
//                int thrust = ((ThrustAction)action).getThrust();
//                if (thrust == 1) {
//                    // in the case of a thrust, ensure that the move doesn't cross suns or barriers
//                    Navigation.Orientation orientation = ufo.getDirection();
//
//                    Point newPoint = ufo.getPoint().getTranslation(orientation, 1);
//
//                    if (m_region.isBarrier(newPoint)) {
//                        // UFO hit a barrier and simply doesn't move (do nothing)
//                    } else if (m_region.isDrone(newPoint)) {
//                        int droneID = m_region.getDroneID(newPoint);
//                        if (droneID >= 0)
//                        {
//                            // Destroy drone
//                            m_drones.get(droneID).informCrash();
//
//                            // Update region
//                            m_region.setEmpty(newPoint);
//                            ufo.setPosition(newPoint);
//                            m_region.setUfo(newPoint, id);
//                        }
//                    } else if (m_region.isWormhole(newPoint)) {
//                        sendThroughWormhole(ufo, newPoint);
//                    } else {
//                        // Update region status
//                        ufo.setPosition(newPoint);
//                        m_region.setUfo(newPoint, id);
//                    }
//                } else {
//                    System.out.println("action_not_recognized");
//                }
//            } else {
//                System.out.println("action_not_recognized");
//            }
//        }
//    }
//
//    private void sendThroughWormhole(Drone drone, Point entryPoint) {
//        m_region.setEmpty(drone.getPoint());
//        Point exitPoint = m_region.getWormholeExitPos(entryPoint);
//        drone.informWormhole(entryPoint, exitPoint);
//
//        if (m_region.isDrone(exitPoint) ) {
//            int id2 = m_region.getDroneID(exitPoint);
//            if (id2 >= 0)
//            {
//                // Another drone is here, destroy both drones
//                drone.informCrash();
//                m_drones.get(id2).informCrash();
//
//                // Update region
//                m_region.setEmpty(m_drones.get(id2).getPoint());
//            }
//        } else if (m_region.isUfo(exitPoint)) {
//            // Destroy drone
//            drone.informCrash();
//        } else {
//            // Exit wormhole is clear, update position
//            drone.setPosition(exitPoint);
//            m_region.setDrone(exitPoint, drone.getID());
//        }
//    }
//
//    private void sendThroughWormhole(Ufo ufo, Point entryPoint) {
//        Point exitPoint = m_region.getWormholeExitPos(entryPoint);
//
//        if (m_region.isDrone(exitPoint)) {
//            int droneID = m_region.getDroneID(exitPoint);
//            if (droneID >= 0)
//            {
//                // A drone is here, destroy it
//                m_drones.get(droneID).informCrash();
//
//                // Update region
//                m_region.setEmpty(m_drones.get(droneID).getPoint());
//            }
//        } else if (m_region.isUfo(exitPoint)) {
//            // Are we gonna have 2+ UFOs?
//        }
//
//        ufo.setPosition(exitPoint);
//        m_region.setUfo(exitPoint, ufo.getID());
//    }
//
//    /**
//     * Outputs action taken by the drone and system responses
//     *
//     * @param id     Drone ID
//     * @param action Action proposed by the drone
//     */
//    public void displayActionAndResponses(int id, DroneAction action) {
//        if (action != null)
//            {
//            // Display the drone's actions
//            System.out.print("d" + String.valueOf(id) + "," + action.toString());
//
//
//            if (action.getAction() == DroneAction.Action.STEER) {
//                System.out.println("," + Navigation.toString(((SteerAction)action).getOrientation()));
//            } else if (action.getAction() == DroneAction.Action.THRUST) {
//                System.out.println("," + ((ThrustAction)action).getThrust());
//            } else {
//                System.out.println();
//            }
//
//            // display the simulation checks and/or responses
//            if (action.getAction() == DroneAction.Action.THRUST || action.getAction() == DroneAction.Action.STEER || action.getAction() == DroneAction.Action.PASS) {
//                System.out.println(m_drones.get(id).getStatusString());
//            } else if (action.getAction() == DroneAction.Action.SCAN) {
//                System.out.println(Region.formatScanResults(m_trackScanResults));
//            } else {
//                System.out.println("action_not_recognized");
//            }
//
//            if (m_verbose) {
//                // Render the state of the space region after each command
//                m_region.render();
//
//                // Display the drones' directions
//                for (int k = 0; k < m_drones.size(); k++) {
//                    if (m_drones.get(k).getStatus() == Drone.Status.OK) {
//                        System.out.println("Drone " + String.valueOf(k) + ": " + m_drones.get(k).getDirection() +
//                                        " (" + m_drones.get(k).getPoint().getX() + ", " + m_drones.get(k).getPoint().getY() + ")");
//                    }
//                }
//
//                // Display the UFO's directions
//                for (int k = 0; k < m_ufos.size(); k++) {
//                    if (m_ufos.get(k).getStatus() == Ufo.Status.OK) {
//                        System.out.println("UFO " + String.valueOf(k) + ": " + m_ufos.get(k).getDirection() +
//                                        " (" + m_ufos.get(k).getPoint().getX() + ", " + m_ufos.get(k).getPoint().getY() + ")");
//                    }
//                }
//
//                System.out.println("");
//
//                // Display shared map
//                System.out.println("Shared information:");
//                if (m_drones.size() > 0)
//                {
//                    m_drones.get(0).renderRegion();
//                }
//            }
//        } else {
//            System.out.println("action_not_recognized");
//        }
//    }
//
//    /**
//     * Checks if all drones have been destroyed
//     *
//     * @return True if all drones have been destroyed, false if at least one drone is active
//     */
//    public Boolean allDronesStopped() {
//        for(int k = 0; k < m_drones.size(); k++) {
//            if (m_drones.get(k).getStatus() == Drone.Status.OK) {
//            	return Boolean.FALSE;
//            }
//        }
//
//        return Boolean.TRUE;
//    }
//
//    /**
//     * Checks if a drone has crashed
//     *
//     * @param  id Drone ID
//     * @return True if drone has crashed, false otherwise
//     */
//    public Boolean droneStopped(int id) {
//        return m_drones.get(id).getStatus() == Drone.Status.CRASH;
//    }
//
//    /**
//     * Shows a final report with statistics on the space explored by the drones
//     */
//    public void showFinalReport() {
//        Stats stats = m_region.getStats();
//        System.out.println(String.valueOf(stats.getRegionSize()) + "," + String.valueOf(stats.getPotentialCut()) + "," +
//                           String.valueOf(stats.getActualCut()) + "," + String.valueOf(m_turnsCompleted));
//
//        if (m_verbose) {
//            System.out.println("");
//            System.out.println("-- Final Report --");
//            System.out.println("Region Size   : " + String.valueOf(stats.getRegionSize()));
//            System.out.println("Potential Cut : " + String.valueOf(stats.getPotentialCut()));
//            System.out.println("Actual Cut    : " + String.valueOf(stats.getActualCut()));
//            System.out.println("Complete Turns: " + String.valueOf(m_turnsCompleted));
//        }
//    }
//
//    private final int MAX_THRUST = 3;
//
//    private boolean m_verbose;
//    private Region m_region;
//    private List<Drone> m_drones;
//    private List<Ufo> m_ufos;
//
//    private int[] m_trackScanResults;
//
//    private Integer m_turnLimit;
//    private int m_turnsCompleted;
//}