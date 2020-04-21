package com.gatech.Model;

/**
 * @author      Leonardo Puy <lpuy3@gatech.edu>
 */

/**
 * Contains statistics about the drones' exploration of the space region
 *
 *
 */
public class Stats {
    public Stats(int regionSize, int numSuns, int numStars, int potentialCut, int actualCut) {
        m_regionSize = regionSize;
        m_numSuns = numSuns;
        m_numStars = numStars;
        m_potentialCut = potentialCut;
        m_actualCut = actualCut;
    }

    public int getRegionSize()
    {
        return m_regionSize;
    }

    public int getNumSuns()
    {
        return m_numSuns;
    }

    public int getNumStars()
    {
        return m_numStars;
    }

    public int getPotentialCut()
    {
        return m_potentialCut;
    }

    public int getActualCut()
    {
        return m_actualCut;
    }

    private int m_regionSize;
    private int m_numSuns;
    private int m_numStars;
    private int m_potentialCut;
    private int m_actualCut;
}

