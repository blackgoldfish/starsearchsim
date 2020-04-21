/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


import java.util.HashMap;

/**
 * Contains orientation and methods to translate them to/from string representations.
 * Also provides a way to get a relative displacement given a direction.
 */
public class Navigation {
    /**
     * Returns the orientation given a description in text form.
     * This is useful when converting a direction sent to a drone by
     * the operator.
     *
     * @param  str Orientation in string form (e.g., "north")
     * @return Orientation, or null if string cannot be converted into a valid orientation
     */
    public static Orientation fromString(String str) {
        return STRING_TO_ORIENTATION.get(str);
    }

    /**
     * Returns a description in text form given an orientation.
     * This can be used to display the orientation in a more user-friendly way.
     *
     * @param  orientation Orientation to be converted to string
     * @return String representation for the orientation
     */
    public static String toString(Orientation orientation) {
        return ORIENTATION_TO_STRING.get(orientation);
    }

    /**
     * Returns the displacement in x, y to achieve a certain direction.
     * For example, to move NW the displacement would be (-1, 1)
     *
     * @param  orientation Orientation to be converted to string
     * @return Displacement instance containing the x, y deltas.
     */
    public static Displacement getDisplacement(Orientation orientation, int numSquares)
    {
        Displacement unitDisp = DIR_MAP.get(orientation);
        return new Displacement(unitDisp.getDeltaX() * numSquares, unitDisp.getDeltaY() * numSquares);
    }

    public enum Orientation { N, NE, E, SE, S, SW, W, NW }

    private static final String[] ORIENTATION_STR = {"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};
    private static HashMap<Orientation, String> ORIENTATION_TO_STRING;
    private static HashMap<String, Orientation> STRING_TO_ORIENTATION;
    private static HashMap<Orientation, Displacement> DIR_MAP;

    // Initialize static data structures
    static {
        ORIENTATION_TO_STRING = new HashMap<>();
        STRING_TO_ORIENTATION = new HashMap<>();
        for (int i = 0; i < ORIENTATION_STR.length; i++) {
            ORIENTATION_TO_STRING.put(Orientation.values()[i], ORIENTATION_STR[i]);
            STRING_TO_ORIENTATION.put(ORIENTATION_STR[i], Orientation.values()[i]);
        }

        DIR_MAP = new HashMap<>();
        DIR_MAP.put(Orientation.N, new Displacement(0, 1));
        DIR_MAP.put(Orientation.NE, new Displacement(1, 1));
        DIR_MAP.put(Orientation.E, new Displacement(1, 0));
        DIR_MAP.put(Orientation.SE, new Displacement(1, -1));
        DIR_MAP.put(Orientation.S, new Displacement(0, -1));
        DIR_MAP.put(Orientation.SW, new Displacement(-1, -1));
        DIR_MAP.put(Orientation.W, new Displacement(-1, 0));
        DIR_MAP.put(Orientation.NW, new Displacement(-1, 1));
    }

}