/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

package com.gatech.Domain;

/**
 * Represents a relative displacement in a region.
 * Example: (-1, 1) represents a displacement of one cell to the left and up
 */

 public class Displacement
{
	public Displacement(Integer x, Integer y)
	{
		m_dx = x;
		m_dy = y;
	}
	
	public int getDeltaX() {
		return m_dx;
	}

	public int getDeltaY() {
		return m_dy;
	}

	private final Integer m_dx;
	private final Integer m_dy;
}

