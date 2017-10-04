/*******************************************************************************
 * Name: Java class SmMarker.java
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

import gov.usgs.smcommon.smclasses.SmPreferences.MarkerStyle;
import java.awt.Color;

/**
 * This class defines a line marker that is drawn on a plot to denote some type
 * of function or event. The attributes of each marker include marker type, style, 
 * color, and width as well as a pair of  x and y points to denote the location
 * of the marker on the plot.
 * @author png
 */
public class SmMarker {
    
    /**
     * This enumeration defines the various marker types that can be added to a
     * plot, including function start and stop, application start and stop, event
     * onset, and start and stop time.
     */
    public static enum MarkerType {FSTART,FSTOP,ASTART,ASTOP,EVENT_ONSET,START_TIME,STOP_TIME}
    
    private MarkerType markerType;
    private MarkerStyle markerStyle;
    private Color markerColor;
    private double markerWidth;
    private double x;
    private double y;
    
    /**
     * Constructor
     * @param markerType
     * @param x point along the x-axis of a plot in which to draw the marker
     * @param y point along the y-axis of a plot in which to draw the marker
     */
    public SmMarker(MarkerType markerType, double x, double y) {
        this.markerType = markerType;
        this.x = x;
        this.y = y;
        
        if (markerType.equals(MarkerType.FSTART)) {
            this.markerStyle = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerColor());
            this.markerWidth = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerWidth();
        }
        else if (markerType.equals(MarkerType.FSTOP)) {
            this.markerStyle = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerColor());
            this.markerWidth = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerWidth();
        }
        else if (markerType.equals(MarkerType.ASTART)) {
            this.markerStyle = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerColor());
            this.markerWidth = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerWidth();
        }
        else if (markerType.equals(MarkerType.ASTOP)) {
            this.markerStyle = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerColor());
            this.markerWidth = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerWidth();
        }
        else if (markerType.equals(MarkerType.EVENT_ONSET)) {
            this.markerStyle = SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerColor());
            this.markerWidth = SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerWidth();
        }
        else if (markerType.equals(MarkerType.START_TIME)) {
            this.markerStyle = SmPreferences.SmTrimTool.getStartTimeMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmTrimTool.getStartTimeMarkerColor());
            this.markerWidth = SmPreferences.SmTrimTool.getStartTimeMarkerWidth();
        }
        else if (markerType.equals(MarkerType.STOP_TIME)) {
            this.markerStyle = SmPreferences.SmTrimTool.getStopTimeMarkerStyle();
            this.markerColor = new Color(SmPreferences.SmTrimTool.getStopTimeMarkerColor());
            this.markerWidth = SmPreferences.SmTrimTool.getStopTimeMarkerWidth();
        }
    }
    
    /**
     * Gets the enumerated marker type
     * @return enumerated marker type
     */
    public MarkerType getMarkerType() {return this.markerType;}
    
    /**
     * Gets the MarkerStyle object that describes the styling attributes of
     * the marker
     * @return MarkerStyle object that describes the styling attributes of 
     * the marker
     */
    public MarkerStyle getMarkerStyle() {return this.markerStyle;}
    
    /**
     * Gets the color used to draw the marker
     * @return color used to draw the marker
     */
    public Color getMarkerColor() {return this.markerColor;}
    
    /**
     * Gets the width of the marker
     * @return width of the marker
     */
    public double getMarkerWidth() {return this.markerWidth;}
    
    /**
     * Gets the point along the x-axis of a plot in which to draw the marker
     * @return point along the x-axis of a plot in which to draw the marker
     */
    public double getX() {return this.x;}
    
    /**
     * Gets the point along the y-axis of a plot in which to draw the marker
     * @return point along the y-axis of a plot in which to draw the marker
     */
    public double getY() {return this.y;}
    
}
