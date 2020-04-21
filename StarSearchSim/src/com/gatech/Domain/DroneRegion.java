/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;


/**
 * Represents the shared knowledge among the drones. The drones build up this knowledge as they explore.
 * One big difference with the space region as seen by the simulator is that this region expands as the drones explore,
 * as the drones have no previous knowledge about its size.
 * 
 * To enforce the uniqueness of this object, it's implemented using the Singleton design pattern.
 */

public class DroneRegion extends Region {
    private DroneRegion() {
        // Start with a minimal region
        super(1, 1);
        set(new Point(0, 0), OBJ_UNKNOWN);
    }

    /**
     * Returns the sole instance for the shared region
     *
     * @return Region instance
     */
    public static DroneRegion getRegion() {
        if (m_instance == null) {
            m_instance = new DroneRegion();
        }

        return m_instance;
    }

    /**
     * Tests if a point in the space region corresponds to a barrier
     *
     * @param  point Location in the space region
     * @return True if location contains a barrier or is out of the left/lower range, false otherwise.
     */
    public boolean isBarrier(Point point) {
		return point.getX() < 0 || point.getY() < 0 || m_region[point.getX()][point.getY()] == OBJ_BARRIER;
	}

    /**
     * Incorporates scan information to the shared space region
     *
     * @param  point Location in the space region
     * @param  scan  Scan results
     */
    public void addScanResults(Point point, int[] scan) {
        // Update internal region
        for (int k = 0; k < Navigation.Orientation.values().length; k++) {
            Navigation.Orientation lookThisWay = Navigation.Orientation.values()[k];
            Point scanPoint = point.getTranslation(lookThisWay, 1);

            if (scan[k] == OBJ_WORMHOLE)
            {
                setWormhole(scanPoint);
            }
            else
            {
                set(scanPoint, scan[k]);
            }
        }
    }

    /**
     * Displays the shared space region. Used for debugging purposes.
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
                renderObject(m_region[i][j]);
            }

            System.out.println("|");
        }
    }

    protected void renderObject(int object) {
        if (object == OBJ_UNKNOWN) {
            System.out.print("?");
        } else {
            super.renderObject(object);
        }
    }

    /**
     * Sets an object in a position, resizing as necessary.
     * If the object is not a barrier then the new size is increased by one in order to accommodate a barrier when it is found.
     *
     * @param  point  Location in the space region
     * @param  object Space object to be placed in the space region
     */
	protected void set(Point point, int object) {
        if (object != OBJ_BARRIER) {
            if (requiresResize(point))
            {
                resizeRegion(point.getX() + 1, point.getY() + 1);
            }
        }

        super.set(point, object);
    }

    /**
     * Determines if the region needs to be resized in order to place a new object in it
     *
     * @param  point Location in the space region
     * @return True if it needs to be resized, false otherwise.
     */
    private Boolean requiresResize(Point point) {
        return point.getX() + 1 >= m_width || point.getY() + 1 >= m_height;
    }

    /**
     * Tests if a point in the space region is unknown, meaning the drone doesn't know what's in there.
     *
     * @param  point Location in the space region
     * @return True if unknown square, false otherwise.
     */
    public boolean isUnknown(Point point) {
        return get(point) == OBJ_UNKNOWN;
    }

    /**
     * Resizes the space region and copies over previous content.
     * Delta area created is marked as Unknown.
     *
     * @param  
     * @return 
     */
    private void resizeRegion(int sizeX, int sizeY) {
        int newWidth = sizeX + 1;
        if (newWidth < m_width) {
            newWidth = m_width;
        }

        int newHeight = sizeY + 1;
        if (newHeight < m_height) {
            newHeight = m_height;
        }

        // Resize and copy array
        int[][] newRegion = new int[newWidth][newHeight];
        for (int i = 0; i < newWidth; i++){
            for (int j = 0; j < newHeight; j++){
                newRegion[i][j] = OBJ_UNKNOWN;
            }    
        }

        for (int i = 0; i < m_width; i++){
            for (int j = 0; j < m_height; j++){
                newRegion[i][j] = m_region[i][j];
            }    
        }

        // Set new array and boundaries
        m_region = newRegion;
        m_height = newHeight;
        m_width = newWidth;
    }

    public void clear()
    {
        m_width = 1;
        m_height = 1;

        int[][] newRegion = new int[m_width][m_height];
        for (int i = 0; i < m_width; i++){
            for (int j = 0; j < m_height; j++){
                newRegion[i][j] = OBJ_UNKNOWN;
            }
        }
    }

    private static final int OBJ_UNKNOWN = -5;

    private static DroneRegion m_instance = null;
}