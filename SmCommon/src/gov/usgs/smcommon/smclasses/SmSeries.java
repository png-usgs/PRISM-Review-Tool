/*******************************************************************************
 * Name: Java class SmSeries.java
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author png
 */
public class SmSeries {
    private final ArrayList<SmPoint> smPoints;
    private final int dataParmCode;
    private final String title;
    private final String description;
    private final Color color;
    private final Object tag;   //reference to some object such as a file path.


    public SmSeries(ArrayList<SmPoint> smPoints, int dataParmCode, 
        String title, String description, Color color, Object tag)
    {
        this.smPoints = smPoints;
        this.dataParmCode = dataParmCode;
        this.title = title;
        this.description = description;
        this.color = color;
        this.tag = tag; //stores reference to source file pathname or other object
    }
    
    // Copy constructor
    public SmSeries(SmSeries smSeries)
    {
        this.smPoints = new ArrayList<>();
        for (SmPoint smPoint : smSeries.getSmPoints()) {
            this.smPoints.add(new SmPoint(smPoint));
        }
        
        this.dataParmCode = smSeries.getDataParmCode();
        this.title = smSeries.getTitle();
        this.description = smSeries.getDecription();
        this.color = new Color(smSeries.getColor().getRGB());
        this.tag = smSeries.getTag();
    }

    public ArrayList<SmPoint> getSmPoints() {return this.smPoints;}
    public int getDataParmCode() {return this.dataParmCode;}
    public String getTitle() {return this.title;}
    public String getDecription() {return this.description;}
    public Color getColor() {return this.color;}
    public Object getTag() {return this.tag;}
    
    @Override
    public String toString() {
        return "[ Title=" + title + ", Description=" + description + ", Color=" + color + "]";
    }
    
    public static Comparator<SmSeries> SmSeriesComparator = new Comparator<SmSeries>() {

        @Override
	public int compare(SmSeries s1, SmSeries s2) {
	   String title1 = s1.getTitle().toUpperCase();
	   String title2 = s2.getTitle().toUpperCase();

	   //ascending order
	   return title1.compareTo(title2);

	   //descending order
	   //return title2.compareTo(title1);
    }};
}
