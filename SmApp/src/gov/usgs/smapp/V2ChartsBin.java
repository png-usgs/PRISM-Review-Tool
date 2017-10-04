/*******************************************************************************
 * Name: Java class V2ChartsBin.java
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

import static SmConstants.VFileConstants.MSEC_TO_SEC;
import gov.usgs.smapp.smchartingapi.SmCharts_API;
import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smapp.smchartingapi.qcchart2d.QCChart2D_API;
import gov.usgs.smcommon.smclasses.SmChannel;
import gov.usgs.smcommon.smclasses.SmEpoch;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPoint;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmStation;
import gov.usgs.smcommon.smclasses.SmTemplate;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import gov.usgs.smcommon.smutilities.SmUtils;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import org.joda.time.DateTime;
import org.openide.NotifyDescriptor;

/**
 *
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class V2ChartsBin extends VxChartsBin {
    
    public V2ChartsBin(final String chartAPI, final ArrayList<String> filePaths,
        final SmTemplate smTemplate, final boolean adjustPoints, final Object owner)
    {
        super(chartAPI,filePaths,smTemplate,adjustPoints,owner);
        padded = false;
        createCharts();
    }
    
    public V2ChartsBin(final String chartAPI, final ArrayList<String> filePaths,
        DateTime earliestStartTime, DateTime latestStopTime, double minDeltaT,
        final SmTemplate smTemplate, final boolean adjustPoints, final Object owner)
    {
        //super(chartAPI,filePaths,smTemplate,adjustPoints,owner);
        super(chartAPI,filePaths,earliestStartTime,latestStopTime,minDeltaT,
            smTemplate,adjustPoints,owner);
        padded = false;
        createCharts();
    }
    
    public void updateCharts(XYBounds xyBndsSeismicAcc, XYBounds xyBndsSeismicVel, 
        XYBounds xyBndsSeismicDis) {
        try 
        {
            // Clear charts lists.
            chartsSeismicAcc.clear();
            chartsSeismicVel.clear();
            chartsSeismicDis.clear();
            chartsSeismicAll.clear();
            chartsSpectral.clear();
            
            SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                new QCChart2D_API() : null;

            if (smCharts_API == null)
                return;

            // Use template, if supplied, to order the series. Otherwise, use
            // default sort.
            if (smTemplate != null) {
                String tmplNetworkCode = smTemplate.getSmStation().getNetworkCode();
                String tmplStationCode = smTemplate.getSmStation().getStationCode();
                
                if (tmplNetworkCode.equals(networkCode) && tmplStationCode.equals(stationCode)) {
                    // Create lists based on template.
                    smSeriesSeismicList = SmUtils.createTemplateBasedList(smSeriesSeismicList,
                        smTemplate,earliestStartTime,latestStopTime);
                    smSeriesSpectralList = SmUtils.createTemplateBasedList(smSeriesSpectralList,
                        smTemplate,earliestStartTime,latestStopTime);
                }
                else {
                    // Sort the lists using default SmSeries comparator.
                    Collections.sort(smSeriesSeismicList, SmSeries.SmSeriesComparator);
                    Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
                }
            }
            else {
                // Sort the lists using default SmSeries comparator.
                Collections.sort(smSeriesSeismicList, SmSeries.SmSeriesComparator);
                Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
            }
            
            // Initialize chart variables.
            String chartTitle;
            String xAxisTitle;
            String yAxisTitle;

            // Create individual seismic charts.
            for (SmSeries smSeriesSeismic : smSeriesSeismicList)
            {
                chartTitle = smSeriesSeismic.getDecription();
                xAxisTitle = "Time (sec)";

                switch (smSeriesSeismic.getDataParmCode())
                {
                    case 1:
                        yAxisTitle = "Accelaration (cm/sec" + "\u00B2" + ")";
                        JPanel panelAcc = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBndsSeismicAcc,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelAcc != null)
                            this.chartsSeismicAcc.add(panelAcc);
                        break;
                    case 2:
                        yAxisTitle = "Velocity (cm/sec)";
                        JPanel panelVel = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBndsSeismicVel,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelVel != null)
                            this.chartsSeismicVel.add(panelVel);
                        break;
                    case 3:
                        yAxisTitle = "Displacement (cm)";
                        JPanel panelDis = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBndsSeismicDis,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelDis != null)
                            this.chartsSeismicDis.add(panelDis);
                        break;
                }
            }
            
            // Remove SmSeries objects with null SmPoints from smSeriesSpectralList.
            ArrayList<SmSeries> tempSmSeriesSpectralList = new ArrayList<>();
            for (SmSeries smSeriesSpectral : smSeriesSpectralList) {
                if (smSeriesSpectral.getSmPoints() != null)
                    tempSmSeriesSpectralList.add(smSeriesSpectral);
            }
            smSeriesSpectralList = tempSmSeriesSpectralList;

            // Create a grouped spectral chart.
            if (!smSeriesSpectralList.isEmpty())
            {
                chartTitle = eventName + "_" + stationName;
                xAxisTitle = "Frequency (Hz)";
                yAxisTitle = "Fourier Amplitude of Acceleration";
                
                JPanel panel = smCharts_API.createGroupChartLog10_XYScale(
                    smSeriesSpectralList,SmGlobal.PLOT_TYPE_SPECTRAL,
                    chartTitle,xAxisTitle,yAxisTitle,false,owner);
                
                if (panel != null)
                    this.chartsSpectral.add(panel);
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    public void reset() {
        clearArrays();
        createCharts();
    }
    
    private void createCharts() {
        try 
        {
            SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                new QCChart2D_API() : null;

            if (smCharts_API == null || filePaths == null || filePaths.isEmpty())
                return;
            
            for (String filePath : filePaths)
            {
                SmFile smFile = new SmFile(new File(filePath));
                
                for (SmRec smRec : smFile.getSmRecs())
                {
                    // Create seismic data list.
                    //ArrayList<SmPoint> dataSeismic = adjustPoints ? 
                        //smRec.createAdjustedSmPointsPadded(
                        //earliestStartTime, latestStopTime, minDeltaT) : 
                        //smRec.getSmPoints();
                    
                    ArrayList<SmPoint> dataSeismic = smRec.getSmPoints();
     
                    if (adjustPoints) {
                        dataSeismic = padded ? smRec.createAdjustedSmPointsPadded(
                            earliestStartTime, latestStopTime) : 
                            smRec.createAdjustedSmPointsUnpadded(earliestStartTime, 
                            latestStopTime);
                    }
                    
                    // Create spectral data list.
                    ArrayList<SmPoint> smPointsFFT = SmRec.createSmPointsFFT(dataSeismic, 
                        minDeltaT, MSEC_TO_SEC);

                    ArrayList<SmPoint> dataSpectral = new ArrayList<>();
                    
                    for (int i=1; i<smPointsFFT.size()/2; i++) //skip 1st point, frequency=0Hz
                    {
                        SmPoint smPointFFT = smPointsFFT.get(i);
                        dataSpectral.add(new SmPoint(smPointFFT.getX(),
                            smPointFFT.getY()));
                    }
                    
                    int dataParmCode = smRec.getDataParmCode();
                    String channel = smRec.getChannel();
                    String sensorLocation = smRec.getSensorLocation();
                    String seed = "";
                    String lCode = "";
                    
                    Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
                    Matcher m = p.matcher(channel);
                    
                    if (m.find()) {
                        seed = m.group(1);
                        lCode = m.group(3);
                    }
                    
                    // Set series title and description.
                    String title = sensorLocation.isEmpty() ? channel : 
                        channel + " (" + sensorLocation + ")";
                    String description = smRec.getEventName() + "_" + 
                        smRec.getNetworkCode() + "." + smRec.getStationCode() + "." + 
                        seed + "." + lCode;

                    // Create color for series.
                    Color color;
                    if (smTemplate == null)
                        color = new Color(SmPreferences.ChartOptions.getColorPlot());
                    else {
                        SmStation smStation = smTemplate.getSmStation();
                        SmEpoch smEpoch = (smStation != null) ?
                            smStation.getSmEpoch(earliestStartTime, latestStopTime) : null;
                        SmChannel smChannel = (smEpoch != null) ?
                            smEpoch.getSmChannel(channel) : null;
                        
                        color = (smChannel != null) ? smChannel.getColor() :
                            new Color(SmPreferences.ChartOptions.getColorPlot());
                    }
                    
                    // Add seismic records to corresponding list.
                    smSeriesSeismicList.add(new SmSeries(dataSeismic,dataParmCode,
                        title,description,color,filePath));

                    // Add spectral (acceleration only) records to corresponding list.
                    if (!dataSpectral.isEmpty() && dataParmCode == 1)
                        smSeriesSpectralList.add(new SmSeries(dataSpectral,dataParmCode,
                        title,description,color,filePath));
                }
            }

            // Use template, if supplied, to order the series. Otherwise, use
            // default sort.
            if (smTemplate != null) {
                String tmplNetworkCode = smTemplate.getSmStation().getNetworkCode();
                String tmplStationCode = smTemplate.getSmStation().getStationCode();
                
                if (tmplNetworkCode.equals(networkCode) && tmplStationCode.equals(stationCode)) {
                    // Create lists based on template.
                    smSeriesSeismicList = SmUtils.createTemplateBasedList(smSeriesSeismicList,
                        smTemplate,earliestStartTime,latestStopTime);
                    smSeriesSpectralList = SmUtils.createTemplateBasedList(smSeriesSpectralList,
                        smTemplate,earliestStartTime,latestStopTime);
                }
                else {
                    // Sort the lists using default SmSeries comparator.
                    Collections.sort(smSeriesSeismicList, SmSeries.SmSeriesComparator);
                    Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
                }
            }
            else {
                // Sort the lists using default SmSeries comparator.
                Collections.sort(smSeriesSeismicList, SmSeries.SmSeriesComparator);
                Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
            }
            
            // Create XYBound objects for seismic plots.
            xyBoundsSeismicAcc = smCharts_API.calcXYBounds(smSeriesSeismicList,
                SmGlobal.PLOT_TYPE_SEISMIC,1);
            xyBoundsSeismicVel = smCharts_API.calcXYBounds(smSeriesSeismicList,
                SmGlobal.PLOT_TYPE_SEISMIC,2);
            xyBoundsSeismicDis = smCharts_API.calcXYBounds(smSeriesSeismicList,
                SmGlobal.PLOT_TYPE_SEISMIC,3);
            
            // Initialize chart variables.
            String chartTitle;
            String xAxisTitle;
            String yAxisTitle;

            // Create individual seismic charts.
            for (SmSeries smSeriesSeismic : smSeriesSeismicList)
            {
                chartTitle = smSeriesSeismic.getDecription();
                xAxisTitle = "Time (sec)";

                switch (smSeriesSeismic.getDataParmCode())
                {
                    case 1:
                        yAxisTitle = "Accelaration (cm/sec" + "\u00B2" + ")";
                        JPanel panelAcc = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBoundsSeismicAcc,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelAcc != null)
                            this.chartsSeismicAcc.add(panelAcc);
                        break;
                    case 2:
                        yAxisTitle = "Velocity (cm/sec)";
                        JPanel panelVel = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBoundsSeismicVel,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelVel != null)
                            this.chartsSeismicVel.add(panelVel);
                        break;
                    case 3:
                        yAxisTitle = "Displacement (cm)";
                        JPanel panelDis = smCharts_API.createChart(
                            smSeriesSeismic,SmGlobal.PLOT_TYPE_SEISMIC,xyBoundsSeismicDis,
                            chartTitle,xAxisTitle,yAxisTitle,false,owner);
                        if (panelDis != null)
                            this.chartsSeismicDis.add(panelDis);
                        break;
                }
            }
            
            // Remove SmSeries objects with null SmPoints from smSeriesSpectralList.
            ArrayList<SmSeries> tempSmSeriesSpectralList = new ArrayList<>();
            for (SmSeries smSeriesSpectral : smSeriesSpectralList) {
                if (smSeriesSpectral.getSmPoints() != null)
                    tempSmSeriesSpectralList.add(smSeriesSpectral);
            }
            smSeriesSpectralList = tempSmSeriesSpectralList;

            // Create a grouped spectral chart.
            if (!smSeriesSpectralList.isEmpty())
            {
                chartTitle = eventName + "_" + stationName;
                xAxisTitle = "Frequency (Hz)";
                yAxisTitle = "Fourier Amplitude of Acceleration";
                
                JPanel panel = smCharts_API.createGroupChartLog10_XYScale(
                    smSeriesSpectralList,SmGlobal.PLOT_TYPE_SPECTRAL,
                    chartTitle,xAxisTitle,yAxisTitle,false,owner);
                
                if (panel != null)
                    this.chartsSpectral.add(panel);
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
}
