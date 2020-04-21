
/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


import java.util.HashMap;

/**
 * Represents a space region
 */
public class Region {
	public Region(int width, int height) {
		m_width = width;
		m_height = height;
		
        // generate the region information
        m_region = new int[m_width][m_height];
        for (int i = 0; i < m_width; i++) {
            for (int j = 0; j < m_height; j++) {
                m_region[i][j] = OBJ_STARS;
            }
        }
	}

	public int getHeight()
    {
        return m_height;
    }

    public int getWidth()
    {
        return m_width;
    }

    /**
     * Determines if a point is within the space region
     *
     * @param  point Location in the space region
     * @return True if point is within the region, false otherwise
     */
    public Boolean inRange(Point point) {
        return (point.getX() >= 0 && point.getX() < m_width && point.getY() >= 0 && point.getY() < m_height);
    }

    /**
     * Sets a space object in the specified position
     *
     * @param  point  Location in the space region where the object will be placed
     * @param  object Space object to be placed in the location
     */
	protected void set(Point point, int object){
        if (inRange(point)) {
            m_region[point.getX()][point.getY()] = object;
        }
    }
    


    /**
     * Sets a sun in the specified position
     *
     * @param  point Location in the space region
     */
    public void setSun(Point point){
        set(point, OBJ_SUN);
    }

    /**
     * Sets an empty (explored) square in the specified position
     * If a wormhole was here, it is restored.
     *
     * @param  point Location in the space region
     */
	public void setEmpty(Point point){
        if (m_wormholes.get(point) != null)
        {
            restoreWormhole(point);
        }
        else{
            set(point, OBJ_EMPTY);
        }
    }

    /**
     * Sets a wormhole in the specified position
     * This is used *only* when a drone or UFO occupied this square before
     * and we want to restore the wormhole marker that was here
     *
     * @param  point Location in the space region
     */
    private void restoreWormhole(Point point)
    {
        set(point, OBJ_WORMHOLE);
    }


    /**
     * Sets a drone in the specified position
     *
     * @param  point Location in the space region
     * @param  id    Drone ID
     */
    public void setDrone(Point point, int id)
    {
        set(point, id);
    }

    /**
     * Sets a UFO in the specified position
     *
     * @param  point Location in the space region
     */
    public void setUfo(Point point)
    {
        m_ufo = point;
    }

    /**
     * Sets a pair of wormholes in the specified position
     *
     * @param  point1 Location of first wormhole
     * @param  point2 Location of second wormhole
     */
    public void setWormholes(Point point1, Point point2)
    {
        set(point1, OBJ_WORMHOLE);
        set(point2, OBJ_WORMHOLE);

        m_wormholes.put(point1, point2);
        m_wormholes.put(point2, point1);
    }

    public void setWormhole(Point point)
    {
        set(point, OBJ_WORMHOLE);
        m_wormholes.put(point, null);
    }

    /**
     * Gets the space object in the specified position
     *
     * @param  point  Location in the space region where the object resides
     */
	protected int get(Point point){
		return m_region[point.getX()][point.getY()];
    }

    /**
     * Tests a square to see if it contains a star field
     *
     * @param  point Location in the space region
     * @return True if it contains a star field, false otherwise
     */
    public boolean isStars(Point point){
        return get(point) == OBJ_STARS && isUfo(point) == false;
    }

    /**
     * Tests a square to see if it contains a sun
     *
     * @param  point Location in the space region
     * @return True if it contains a sun, false otherwise
     */
    public boolean isSun(Point point){
        return get(point) == OBJ_SUN;
    }

    /**
     * Tests a square to see if it's empty (explored)
     *
     * @param  point Location in the space region
     * @return True if empty, false otherwise
     */
    public boolean isEmpty(Point point){
        return get(point) == OBJ_EMPTY && isUfo(point) == false;
    }

    /**
     * Tests a square to see if it contains a drone
     *
     * @param  point Location in the space region
     * @return True if it contains a drone, false otherwise
     */
    public boolean isDrone(Point point){
        return get(point) >= 0 && isUfo(point) == false;
    }

    /**
     * Gets the ID of a drone in a specified position.
     * Note: Call isDrone() first to make sure the cell contains a drone.
     *
     * @param  point Location in the space region
     * @return Drone ID, or null if the cell doesn't contain a drone.
     */
    public Integer getDroneID(Point point) {
        if (get(point) >= 0)
        {
            return get(point);
        }

        return null;
    }

    /**
     * Tests a square to see if it contains a wormhole
     *
     * @param  point Location in the space region
     * @return True if it contains a wormhole, false otherwise
     */
    public boolean isWormhole(Point point){
        return m_wormholes.containsKey(point);
    }

    /**
     * Tests a square to see if it contains a UFO
     *
     * @param  point Location in the space region
     * @return True if it contains a UFO, false otherwise
     */
    public boolean isUfo(Point point){
        return m_ufo != null && m_ufo.equals(point);
    }

    /**
     * Gets the destination wormhole's coordinates
     * Note: Call isWormhole() first to make sure the cell contains a wormhole.
     *
     * @param  point Location in the space region
     * @return point for the destination wormhole, or null if the cell doesn't contain a wormhole.
     */
    public Point getWormholeExitPos(Point point) {
        if (isWormhole(point))
        {
            return m_wormholes.get(point);
        }

        return null;
    }

    /**
     * Tests a square to see if it's on or beyond a barrier
     *
     * @param  point Location in the space region
     * @return True if it's on or beyond a barrier, false otherwise
     */
	public boolean isBarrier(Point point) {
		return point.getX() < 0 || point.getX() >= m_width || point.getY() < 0 || point.getY() >= m_height;
	}
    
    /**
     * Scans the space region around a point and returns the result in an array.
     * Results contain the eight squares around the given position, sorted clockwise and starting from the 
     * North location.
     *
     * @param  point Location in the space region
     * @return Area around the space region.
     */
    public int[] scan(Point point) {
        int nextSquare;
        int[] scan = new int[Navigation.Orientation.values().length];

        for (int k = 0; k < Navigation.Orientation.values().length; k++) {
            Navigation.Orientation lookThisWay = Navigation.Orientation.values()[k];
            Point checkPoint = point.getTranslation(lookThisWay, 1);

            if (isBarrier(checkPoint)) {
                nextSquare = OBJ_BARRIER;
            } else if (isUfo(checkPoint)) {
                nextSquare = OBJ_UFO;
            } else {
                nextSquare = get(checkPoint);
            } 

            scan[k] = nextSquare;
        }

        return scan;
    }


    /**
     * Displays the space region. For debugging purposes only.
     */
    public void render() {
        int i, j;
        int charWidth = 2 * m_width + 2;

        // display the rows of the region from top to bottom
        for (j = m_height - 1; j >= 0; j--) {
            renderHorizontalBar(charWidth);

            // display the Y-direction identifier
            System.out.print(j);

            // display the contents of each square on this row
            for (i = 0; i < m_width; i++) {
                System.out.print("|");
                if (m_ufo.equals(new Point(i, j))) {
                    renderObject(OBJ_UFO);
                } else {
                    renderObject(m_region[i][j]);
                }
            }

            System.out.println("|");
        }
        
        renderHorizontalBar(charWidth);

        // display the column X-direction identifiers
        System.out.print(" ");
        for (i = 0; i < m_width; i++) {
            System.out.print(" " + i);
        }
        
        System.out.println();
    }

    protected void renderObject(int object) {
        switch (object)
        {
            case OBJ_BARRIER:
                System.out.print("B");
                break;

            case OBJ_EMPTY:
                System.out.print(" ");
                break;

            case OBJ_STARS:
                System.out.print(".");
                break;

            case OBJ_SUN:
                System.out.print("s");
                break;

            case OBJ_WORMHOLE:
                System.out.print("w");
                break;

            case OBJ_UFO:
                System.out.print("X");
                break;
            default:
                System.out.print(object);
                break;
            
        }
    }

    /**
     * Displays a horizontal bar. For debugging purposes only.
     */
    protected void renderHorizontalBar(int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println();
    }
    
    /**
     * Gets statistics from the space region. Used by the simulator to assess explored vs. unexplored areas.
     *
     * @return Statistics for the space region
     */
    public com.gatech.Model.Stats getStats() {
        int regionSize = m_width * m_height;
        int numSuns = 0;
        int numStars = 0;

        for (int i = 0; i < m_width; i++) {
            for (int j = 0; j < m_height; j++) {
                if (m_region[i][j] == OBJ_SUN) { 
                	numSuns++; 
                }
                
                if (m_region[i][j] == OBJ_STARS) { 
                	numStars++; 
                }
            }
        }
        
        int potentialCut = regionSize - numSuns;
        int actualCut = potentialCut - numStars;
        
		return new com.gatech.Model.Stats(regionSize, numSuns, numStars, potentialCut, actualCut);
    }

    /**
     * Formats scan results into a string representation. Used for console output.
     *
     * @param  scan Array of space objects obtained as the result of a previous scan operation.
     * @return Scan results in string form
     */
    public static String formatScanResults(int[] scan) {
        String square = "";
        String resultString = "";

        for (int k = 0; k < scan.length; k++) {
            switch (scan[k])
            {
                case OBJ_BARRIER:
                    square = "Barrier";
                    break;
    
                case OBJ_EMPTY:
                    square = "Empty";
                    break;
    
                case OBJ_STARS:
                    square = "Stars";
                    break;
    
                case OBJ_SUN:
                    square = "Sun";
                    break;

                case OBJ_WORMHOLE:
                    square = "Wormhole";
                    break;

                case OBJ_UFO:
                    square = "UFO";
                    break;

                default:
                    if (scan[k] >= 0) {
                        square = "Drone";
                    }

                    break;
                
                }
        
            if (resultString.isEmpty()) { 
            	resultString = square; 
            }
            else {
            	resultString = resultString + "," + square; 
            }
        }    

        return resultString;
    }

	protected static final int OBJ_EMPTY = -1;
	protected static final int OBJ_STARS = -2;
    protected static final int OBJ_SUN = -3;
    protected static final int OBJ_BARRIER = -4;
    protected static final int OBJ_WORMHOLE = -6;
    protected static final int OBJ_UFO = -7;

    private HashMap<Point, Point> m_wormholes = new HashMap<Point, Point>();
    private Point m_ufo = null;

    protected int m_width;
    protected int m_height;
    protected int[][] m_region;

}
