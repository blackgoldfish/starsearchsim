/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;

/**
 * Represents a point or location in the space region
 */
public class Point
{
	public Point(Integer x, Integer y)
	{
		m_x = x;
		m_y = y;
	}
	
	public int getX() {
		return m_x;
	}

	public int getY() {
		return m_y;
	}



    /**
     * Returns the current point shifted by the deltas resulting from a single step in the specified orientation.
	 * Calling this method doesn't modify the object.
     *
     * @param  orientation Orientation for the displacement from the current point
     * @return New point with recalculated coordinates from the current location
     */
	public Point getTranslation(Navigation.Orientation orientation, int numSquares)
	{
		Displacement displacement = Navigation.getDisplacement(orientation, numSquares);
		return new Point(m_x + displacement.getDeltaX(), m_y + displacement.getDeltaY());
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * m_x + m_y;  
        return result;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
			return true;
		}

        if (obj == null) {
			return false;
		}

        if (getClass() != obj.getClass()) {
			return false;
		}

        Point other = (Point) obj;
		return this.m_x == other.m_x && this.m_y == other.m_y;
	}


	private Integer m_x;
	private Integer m_y;
}

