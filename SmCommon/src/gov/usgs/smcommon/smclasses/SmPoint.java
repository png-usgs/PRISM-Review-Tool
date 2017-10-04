/*******************************************************************************
 * Name: Java class SmPoint.java
 * Project: PRISM Review Tool
 * Written by: Peter Ng, USGS, png@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package gov.usgs.smcommon.smclasses;

/**
 * This class defines a structure for storing a pair of x and y points representing
 * a single data point from a given COSMOS record. An SmRec object contains an array 
 * of SmPoint objects to represent all the point data from the COSMOS record. 
 * 
 * @author png
 */
public class SmPoint {
    
    private final double x;
    private final double y;

    /**
     * Constructor
     * @param x point to plot along the x-axis
     * @param y point to plot along the y-axis
     */
    public SmPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    // Copy constructor
    public SmPoint(SmPoint smPoint)
    {
        this.x = smPoint.getX();
        this.y = smPoint.getY();
    }

    /**
     * Gets the x point
     * @return x point
     */
    public double getX()
    {
        return this.x;
    }

    /**
     * Gets the y point
     * @return y point
     */
    public double getY()
    {
        return this.y;
    }
}
