/*******************************************************************************
 * Name: Java class SmCharts_API.java
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

package gov.usgs.smapp.smchartingapi;


import gov.usgs.smcommon.smclasses.SmSeries;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * This abstract class defines the charting API to be implemented by 
 * inherited classes. 
 * @author png
 */
public abstract class SmCharts_API {
    
    public SmCharts_API()
    {
    }
    
    public abstract JPanel createChart(final SmSeries smSeries, 
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, boolean editMode, final Object owner);
    
    public abstract JPanel createChart(final SmSeries smSeries, 
        final String plotType, final XYBounds xyBounds, final String chartTitle, 
        final String xAxisTitle, final String yAxisTitle, final boolean editMode, final Object owner);
    
    public abstract JPanel createChartLog10_XYScale(final SmSeries smSeries,
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner);
    
    public abstract JPanel createChartLog10_XYScale(final SmSeries smSeries,
        final String plotType, final XYBounds xyBounds, final String chartTitle, 
        final String xAxisTitle, final String yAxisTitle, final boolean editMode, 
        final Object owner);
    
    public abstract JPanel createGroupChart(final ArrayList<SmSeries> smSeriesList,
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner);
    
    public abstract JPanel createGroupChartLog10_XYScale(final ArrayList<SmSeries> smSeriesList, 
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner);
    
    public abstract XYBounds calcXYBounds(final ArrayList<SmSeries> smSeriesList,
        final String plotType);
    
    public abstract XYBounds calcXYBounds(final ArrayList<SmSeries> smSeriesList,
        final String plotType, final int dataParmCode);
    
    public class XYBounds
    {
        private double xMin;
        private double yMin;
        private double xMax;
        private double yMax;
        
        public XYBounds()
        {
        }
        
        public XYBounds(double xMin, double yMin, double xMax, double yMax)
        {
            this.xMin = xMin;
            this.yMin = yMin;
            this.xMax = xMax;
            this.yMax = yMax;
        }
        
        
        public double getXMin()
        {
            return this.xMin;
        }
        
        public void setXMin(double xMin)
        {
            this.xMin = xMin;
        }
        
        public double getYMin()
        {
            return this.yMin;
        }
        
        public void setYMin(double yMin)
        {
            this.yMin = yMin;
        }
        
        public double getXMax()
        {
            return this.xMax;
        }
        
        public void setXMax(double xMax)
        {
            this.xMax = xMax;
        }
        
        public double getYMax()
        {
            return this.yMax;
        }
        
        public void setYMax(double yMax)
        {
            this.yMax = yMax;
        }
    }
}
