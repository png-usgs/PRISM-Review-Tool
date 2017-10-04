/*******************************************************************************
 * Name: Java class VxChartsBin.java
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

package gov.usgs.smapp;

import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmTemplate;
import gov.usgs.smcommon.smutilities.SmUtils;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.joda.time.DateTime;

/**
 * This abstract class defines the general properties of a bin for storing charts
 * of various COSMOS data file types (i.e., V1, V2).
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class VxChartsBin {
    protected ArrayList<JPanel> chartsSeismicAcc = new ArrayList<>();
    protected ArrayList<JPanel> chartsSeismicVel = new ArrayList<>();
    protected ArrayList<JPanel> chartsSeismicDis = new ArrayList<>();
    protected ArrayList<JPanel> chartsSeismicAll = new ArrayList<>();
    protected ArrayList<JPanel> chartsSpectral = new ArrayList<>();
    
    protected ArrayList<SmSeries> smSeriesSeismicList = new ArrayList<>();
    protected ArrayList<SmSeries> smSeriesSpectralList = new ArrayList<>();
    
    protected XYBounds xyBoundsSeismicAcc;
    protected XYBounds xyBoundsSeismicVel;
    protected XYBounds xyBoundsSeismicDis;
    
    protected String networkCode = "";  //network code found in record header
    protected String stationCode = "";  //station code found in record header
    protected String stationName = "";  //station name found in record header
    protected String eventName = "";
    
    protected String stationTitle = "";
    protected String eventTitle = "";
    protected String chartAPI;
    protected ArrayList<String> filePaths;
    protected SmTemplate smTemplate;
    protected boolean adjustPoints;
    protected boolean padded = false; //false creates unpadded adjusted points array;
    protected Object owner;
    
    protected String event;
    protected String station;

    protected DateTime earliestStartTime;
    protected DateTime latestStopTime;
    protected double minDeltaT;
    
    /**
     * Constructor.
     * @param chartAPI name of chart API to use for charting.
     * @param filePaths list of file paths.
     * @param smTemplate SmTemplate object representing the template to use for charting.
     * @param adjustPoints
     * @param owner
     */
    public VxChartsBin(final String chartAPI, final ArrayList<String> filePaths, 
        final SmTemplate smTemplate, final boolean adjustPoints, final Object owner)
    {
        this.chartAPI = chartAPI;
        this.filePaths = filePaths;
        this.smTemplate = smTemplate;
        this.adjustPoints = adjustPoints;
        this.owner = owner;
        
        // Set member variables.
        if (filePaths != null && filePaths.size() > 0) {
            SmFile smFile = new SmFile(new File(filePaths.get(0)));
            SmRec smRec = smFile.getSmRecs().get(0);
            
            networkCode = smRec.getNetworkCode();
            stationCode = smRec.getStationCode();
            stationName = smRec.getStationName();
            eventName = smRec.getEventName();
            
            StringBuilder sbStationTitle = new StringBuilder();
            sbStationTitle.append(networkCode).append(".").append(stationCode);
            if (stationName.isEmpty()) {
                sbStationTitle.append(" - ").append(stationName);
            }
            
            StringBuilder sbEventTitle = new StringBuilder();
            sbEventTitle.append(smRec.getEventDateTime()).append(" ").append(eventName);
            
            this.stationTitle = sbStationTitle.toString();
            this.eventTitle = sbEventTitle.toString();
            
            // Get earliest start and latest end times and minimum delta time.
            this.event = SmCore.extractEventName(filePaths.get(0));
            this.station = SmCore.extractStationName(filePaths.get(0));
            
            //earliestStartTime = SmCore.getEarliestStartTime(event, station);
            //latestStopTime = SmCore.getLatestStopTime(event, station);
            //minDeltaT = SmCore.getMinimumDeltaT(event, station);
            
            earliestStartTime = SmUtils.getEarliestStartTime(filePaths);
            latestStopTime = SmUtils.getLatestStopTime(filePaths);
            minDeltaT = SmUtils.getMinimumDeltaT(filePaths);
        }
    }
    
    public VxChartsBin(final String chartAPI, final ArrayList<String> filePaths, 
        final DateTime earliestStartTime, final DateTime latestStopTime, final double minDeltaT,
        final SmTemplate smTemplate, final boolean adjustPoints, final Object owner)
    {
        this.chartAPI = chartAPI;
        this.filePaths = filePaths;
        this.smTemplate = smTemplate;
        this.adjustPoints = adjustPoints;
        this.owner = owner;
        
        // Set member variables.
        if (filePaths != null && filePaths.size() > 0) {
            SmFile smFile = new SmFile(new File(filePaths.get(0)));
            SmRec smRec = smFile.getSmRecs().get(0);
            
            networkCode = smRec.getNetworkCode();
            stationCode = smRec.getStationCode();
            stationName = smRec.getStationName();
            eventName = smRec.getEventName();
            
            StringBuilder sbStationTitle = new StringBuilder();
            sbStationTitle.append(networkCode).append(".").append(stationCode);
            if (stationName.isEmpty()) {
                sbStationTitle.append(" - ").append(stationName);
            }
            
            StringBuilder sbEventTitle = new StringBuilder();
            sbEventTitle.append(smRec.getEventDateTime()).append(" ").append(eventName);
            
            this.stationTitle = sbStationTitle.toString();
            this.eventTitle = sbEventTitle.toString();
            
            // Get earliest start and latest end times and minimum delta time.
            this.event = SmCore.extractEventName(filePaths.get(0));
            this.station = SmCore.extractStationName(filePaths.get(0));
            
            this.earliestStartTime = earliestStartTime;
            this.latestStopTime = latestStopTime;
            this.minDeltaT = minDeltaT;
        }
    }

    /**
     * Gets the list of charts of seismic acceleration data.
     * @return list of JPanel objects, each panel containing a chart of seismic 
     * acceleration data.
     */
    public ArrayList<JPanel> getChartsSeismicAcc()
    {
        return this.chartsSeismicAcc;
    }

    /**
     * Gets the list of charts of seismic velocity data.
     * @return list of JPanel objects, each panel containing a chart of seismic 
     * velocity data.
     */
    public ArrayList<JPanel> getChartsSeismicVel()
    {
        return this.chartsSeismicVel;
    }

    /**
     * Gets the list of charts of seismic displacement data.
     * @return list of JPanel objects, each panel containing a chart of seismic 
     * displacement data.
     */
    public ArrayList<JPanel> getChartsSeismicDis()
    {
        return this.chartsSeismicDis;
    }

    /**
     * Gets the list of charts of all seismic data (i.e., acceleration, velocity,
     * and displacement).
     * @return list of JPanel objects, each panel containing a chart of all seismic 
     * acceleration data (i.e., acceleration, velocity, and displacement).
     */
    public ArrayList<JPanel> getChartsSeismicAll()
    {
        return this.chartsSeismicAll;
    }

    /**
     * Gets the list of charts of spectral acceleration data.
     * @return list of JPanel objects, each panel containing a chart of spectral 
     * acceleration data.
     */
    public ArrayList<JPanel> getChartsSpectral()
    {
        return this.chartsSpectral;
    }
    
    public ArrayList<SmSeries> getSmSeriesSeismicList() {return this.smSeriesSeismicList;}
    public ArrayList<SmSeries> getSmSeriesSpectralList() {return this.smSeriesSpectralList;}
    public XYBounds getXYBoundsSeismicAcc() {return this.xyBoundsSeismicAcc;}
    public XYBounds getXYBoundsSeismicVel() {return this.xyBoundsSeismicVel;}
    public XYBounds getXYBoundsSeismicDis() {return this.xyBoundsSeismicDis;}
    
    public String getStationTitle() {return this.stationTitle;}
    public String getEventTitle() {return this.eventTitle;}
    public String getChartAPI() {return this.chartAPI;}
    public ArrayList<String> getFilePaths() {return this.filePaths;}
    public SmTemplate getSmTemplate() {return this.smTemplate;}
    public boolean getAdjustPoints() {return this.adjustPoints;}
    public Object getOwner() {return this.owner;}
    
    public DateTime getEarliestStartTime() {return this.earliestStartTime;}
    public DateTime getLatestStopTime() {return this.latestStopTime;}
    public double getMinDeltaT() {return this.minDeltaT;}
    
    public void setSmSeriesSeismicList(ArrayList<SmSeries> smSeriesSeismicList) {
        this.smSeriesSeismicList = smSeriesSeismicList;
    }
    
    public void setSmSeriesSpectralList(ArrayList<SmSeries> smSeriesSpectralList) {
        this.smSeriesSpectralList = smSeriesSpectralList;
    }
    
    public final void clearArrays() {
        // Clear array variables.
        chartsSeismicAcc.clear();
        chartsSeismicVel.clear();
        chartsSeismicDis.clear();
        chartsSeismicAll.clear();
        chartsSpectral.clear();
        smSeriesSeismicList.clear();
        smSeriesSpectralList.clear();
    }
    
}
