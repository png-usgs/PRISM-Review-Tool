/*******************************************************************************
 * Name: Java class SmFilterDialog.java
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

package gov.usgs.smapp.smforms;

import COSMOSformat.V1Component;
import static SmConstants.VFileConstants.DATA_PHYSICAL_PARAM_CODE;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.V_UNITS_INDEX;
import SmProcessing.V2ProcessGUI;
import SmUtilities.SmTimeFormatter;
//import gov.usgs.prism.V2ProcessGUI;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smchartingapi.SmCharts_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.GroupChartView;
import gov.usgs.smapp.smchartingapi.qcchart2d.QCChart2D_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.SmChartView;
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
import static gov.usgs.smcommon.smutilities.MathUtils.isDouble;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import gov.usgs.smcommon.smutilities.SmGUIUtils;
import gov.usgs.smcommon.smutilities.SmUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.joda.time.DateTime;
import org.openide.NotifyDescriptor;

/**
 *
 * @author png-pr
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmFilterEditor extends javax.swing.JDialog {

    private final double EPSILON = 0.000001;
    
    private final String chartAPI;
    private final ArrayList<V2ProcessGUI> v2ProcessGUIs;
    private final SmTemplate smTemplate;
   
    private final JPopupMenu editPopupMenu;
    
    private final TextFieldMouseListener textFieldMouseListener = 
        new TextFieldMouseListener();
    private final TextFieldPropertyListener textFieldPropertyListener = 
        new TextFieldPropertyListener();
    private final RBtnTypeListener rbtnTypeListener = new RBtnTypeListener();
    
    private double initFilterRangeLow;
    private double initFilterRangeHigh;
    
    private String logTime;
    
    private double xMax=0;
   
    private final ArrayList<SmChartView> chartViews = new ArrayList<>();
    
    private boolean filtered = false;
    
    /**
     * Creates new form SmFilterEditor
     * @param parent
     * @param modal
     * @param chartAPI
     * @param v2ProcessGUIs
     * @param smTemplate
     */
    public SmFilterEditor(java.awt.Frame parent, boolean modal, String chartAPI, 
        ArrayList<V2ProcessGUI> v2ProcessGUIs, SmTemplate smTemplate) {
        
        super(parent, modal);
        initComponents();
        
        // Set form icon.
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
            getResource("/resources/icons/prism_review_tool_16.png")));
        
        // Set member variables.
        this.chartAPI = chartAPI;
        this.v2ProcessGUIs = v2ProcessGUIs;
        this.smTemplate = smTemplate;
        
        // Create CCP popup menu.
        editPopupMenu = SmGUIUtils.createCCPPopupMenu();
        
        // Initialize interface components.
        initInterface();
        initChartViewerPanel();
        initChannelsTable();
        initEditorPanel();
        
        // Call reset() to create and display spectral chart.
        reset();
    }

    public boolean showDialog()
    {
        this.setVisible(true);
        return filtered;
    }
    
    public boolean getFilterStatus() {return this.filtered;}
    
    public double getFilterRangeLow() {
        return ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
    }
    
    public double getFilterRangeHigh() {
        return ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
    }
    
    private String retrieveEventName() {
        try {
            File v1File = this.v2ProcessGUIs.get(0).getV1File();
            SmFile smFile = new SmFile(v1File);
            String filePath = smFile.getFile().getPath();
            
            return SmCore.extractEventName(filePath);
        }
        catch (Exception ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return null;
    }
    
    private String retrieveStationName() {
        try {
            File v1File = this.v2ProcessGUIs.get(0).getV1File();
            SmFile smFile = new SmFile(v1File);
            String filePath = smFile.getFile().getPath();
            
            return SmCore.extractStationName(filePath);
        }
        catch (Exception ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return null;
    }
    
    private void initInterface() {
        this.addWindowListener(new SmChartEditorWindowListener());
    }
    
    private void initChartViewerPanel() {
        this.pnlViewerAcc.addMouseListener(new ChartViewerMouseListener());
    }
    
    private void initChannelsTable() {
        
        this.tblChannels.setModel(new ChannelsTableModel());
//        this.tblChannels.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
        this.tblChannels.setRowSelectionAllowed(false);
        this.tblChannels.setColumnSelectionAllowed(true);
        this.tblChannels.setDefaultRenderer(ChannelBox.class, new ChannelBoxRenderer());
        this.tblChannels.setDefaultEditor(ChannelBox.class, new ChannelBoxEditor());
        this.tblChannels.setDefaultRenderer(HasV2.class, new HasV2Renderer());
        this.tblChannels.setShowGrid(true);
    }
    
    private void initEditorPanel() {  
        
        // Initialize range variables using preferences.
        //this.initFilterRangeLow = SmPreferences.SmFilter_Editor.getFilterRangeLow();
        //this.initFilterRangeHigh = SmPreferences.SmFilter_Editor.getFilterRangeHigh();
        this.initFilterRangeLow = this.v2ProcessGUIs.get(0).getLowFilterCorner();
        this.initFilterRangeHigh = this.v2ProcessGUIs.get(0).getHighFilterCorner();

        // Format range text fields.
        NumberFormat rangeFormat = NumberFormat.getNumberInstance();
        rangeFormat.setMinimumFractionDigits(1);
        rangeFormat.setMaximumFractionDigits(4);

        this.ftxtFilterRangeLow.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtFilterRangeHigh.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));

        // Set range text fields.
        this.ftxtFilterRangeLow.setValue(this.initFilterRangeLow);
        this.ftxtFilterRangeHigh.setValue(this.initFilterRangeHigh);

        // Set filter-related conrol listeners.
        setFilterRangeTextFieldListeners(true);
        setFilterRangeRadioButtonListeners(true);
    }
    
    private void setFilterRangeTextFieldListeners(boolean add) {
        if (add) {
            this.ftxtFilterRangeLow.addMouseListener(textFieldMouseListener);
            this.ftxtFilterRangeHigh.addMouseListener(textFieldMouseListener);
            
            this.ftxtFilterRangeLow.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtFilterRangeHigh.addPropertyChangeListener("value",textFieldPropertyListener);
        }
        else {
            this.ftxtFilterRangeLow.removeMouseListener(textFieldMouseListener);
            this.ftxtFilterRangeHigh.removeMouseListener(textFieldMouseListener);
            
            this.ftxtFilterRangeLow.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtFilterRangeHigh.removePropertyChangeListener("value",textFieldPropertyListener);
        }
    }
    
    private void setFilterRangeRadioButtonListeners(boolean add) {
        if (add) {
            this.rbtnFilterRangeLow.addItemListener(rbtnTypeListener);
            this.rbtnFilterRangeHigh.addItemListener(rbtnTypeListener);
        }
        else {
            this.rbtnFilterRangeLow.removeItemListener(rbtnTypeListener);
            this.rbtnFilterRangeHigh.removeItemListener(rbtnTypeListener);
        }
    }
    
    private void reset() {

        try {
            // Set log time.
            SmTimeFormatter timer = new SmTimeFormatter();
            this.logTime = timer.getGMTdateTime();
            
            // Create initial SmSeries array.
            ArrayList<SmSeries> smSeriesSpectralList = createSmSeriesSpectralList(this.v2ProcessGUIs);
            
            // Calculate maximum X value (in Hz).
            this.xMax = calcXMax(smSeriesSpectralList);

            // Update SelectBySeed list.
            updateFilterSeedList(smSeriesSpectralList);

            // Update SelectByLCode list.
            updateFilterLCodeList(smSeriesSpectralList);

            // Update Channels table.
            updateChannelsTable(smSeriesSpectralList);

            // Update Chart Viewer.
            updateChartViewer(smSeriesSpectralList);
            
            // Reset filter range text fields.
            setFilterRangeTextField(this.ftxtFilterRangeLow,this.initFilterRangeLow);
            setFilterRangeTextField(this.ftxtFilterRangeHigh,this.initFilterRangeHigh);
            
            // Reset initial radio button selection.
            if (this.rbtnFilterRangeLow.isSelected()) {
                this.rbtnFilterRangeHigh.setSelected(true);
                this.rbtnFilterRangeLow.setSelected(true);
            }
            else {
                this.rbtnFilterRangeLow.setSelected(true);
            }
            
            SmCore.addMsgToStatusViewer("Filter editor reset at: " + this.logTime);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void setFilterRangeTextField(JFormattedTextField field, double value) {
        
        // Turn off filter-related control listeners.
        setFilterRangeTextFieldListeners(false);
        setFilterRangeRadioButtonListeners(false);
        
        // Set the field value.
        field.setValue(value);

        // Set the marker style, color, and width based on specified field (i.e., low or high).
        SmPreferences.MarkerStyle markerStyle;
        Color color;
        double width;

        if (field.getName().equals(this.ftxtFilterRangeLow.getName())) {
            markerStyle = SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerStyle();
            color = new Color(SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerColor());
            width = SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerWidth();
        }
        else {
            markerStyle = SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerStyle();
            color = new Color(SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerColor());
            width = SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerWidth();
        }

        // Draw marker.
        for (SmChartView chartView : this.chartViews) {
            chartView.setBoundedTextField(field);
            chartView.setSmDataCursorStyle(markerStyle);
            chartView.setSmDataCursorColor(color);
            chartView.setSmDataCursorWidth(width);

            if (value - 0 >= EPSILON)
                chartView.getSmDataCursor().drawMarker(value, 0);
        }
        
        // Turn on filter-related control listeners.
        setFilterRangeTextFieldListeners(true);
        setFilterRangeRadioButtonListeners(true);
    }
    
    private File fetchV2File(File v1File) {
        // Build V1 filename string without file extension.
        String fileName = v1File.getName();
        String regExCosmosFileType = String.format("(?i)(^.+)([\\.](V[\\d][cC]?)$)");
        Pattern p = Pattern.compile(regExCosmosFileType);
        Matcher m = p.matcher(fileName);
        
        String baseFileName = "";
        
        if (m.find())
            baseFileName = m.group(1);
        
        Path path = v1File.toPath();
        String v2PathStr = path.getParent().getParent().toString() + 
            File.separator + "V2";
        File v2Dir = new File(v2PathStr);
        
        if (Files.isDirectory(v2Dir.toPath()))
        {
            File[] files = v2Dir.listFiles();
            
            for (File file : files) {
                if (file.getName().startsWith(baseFileName)) {
                    return file;
                }
            }
        }
        
        return null;
    }
    
    private double calcXMax(ArrayList<SmSeries> smSeriesList) {
        
        double xMaxVal = 0;
        
        try {
            for (SmSeries smSeries : smSeriesList) {
                ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                for (SmPoint smPoint : smPoints) {
                    double xVal = smPoint.getX();
                    if (xVal > xMaxVal)
                        xMaxVal = xVal;
                }
            }
        }
        catch (Exception ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return xMaxVal;
    }
    
    private SmRec createSmRec(V2ProcessGUI v2ProcessGUI) {
        try {
            V1Component rec = v2ProcessGUI.getV1Component();
            
            final String[] textHeaders = rec.getTextHeader();
            final String eventName = textHeaders[1].substring(0, 40).
                replaceAll("(?i)record", "").replaceAll("(?i)of","").trim();
            final String eventDateTime = textHeaders[1].substring(40).trim();
            String networkCode = textHeaders[4].substring(25, 27).trim();
            String stationCode = textHeaders[4].substring(28, 34).trim();
            String stationName = textHeaders[4].substring(40).trim();
            final int dataParmCode = rec.getIntHeaderValue(DATA_PHYSICAL_PARAM_CODE);
            final int dataUnitCode = rec.getIntHeaderValue(V_UNITS_INDEX);
            final String channel = rec.getChannel();
            final String sensorLocation = rec.getSensorLocation();

            final int startDateYr = rec.getIntHeaderValue(39);
            final int startDateMth = rec.getIntHeaderValue(41);
            final int startDateDay = rec.getIntHeaderValue(42);
            final int startTimeHr = rec.getIntHeaderValue(43);
            final int startTimeMin = rec.getIntHeaderValue(44);
            final double startTimeSec = rec.getRealHeaderValue(29);
            final int startSec = (int)Math.floor(startTimeSec);
            final int startMs = (int) Math.round((startTimeSec-Math.floor(startTimeSec))*1000);

            final DateTime startDateTime = new DateTime(startDateYr,
                startDateMth,startDateDay,startTimeHr,startTimeMin,startSec,startMs);
            final double deltaT = rec.getRealHeaderValue(DELTA_T);
            final double maxVal = rec.getRealHeaderValue(63);

            ArrayList<SmPoint> smPoints = new ArrayList<>();

            double[] points = v2ProcessGUI.getAcceleration();

            double xVal = 0;
            for (int i=0; i<points.length; i++){
                smPoints.add(new SmPoint(xVal,points[i]));
                xVal += deltaT*MSEC_TO_SEC;
            }

            SmRec smRec = new SmRec(smPoints, channel, sensorLocation, stationCode, 
                stationName, networkCode, startDateTime, deltaT, MSEC_TO_SEC, maxVal, 
                eventName, eventDateTime, dataParmCode, dataUnitCode);
            
            return smRec;
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return null;
    }
    
    private ArrayList<SmSeries> createSmSeriesSpectralList(ArrayList<V2ProcessGUI> v2ProcessGUIs) 
        throws Exception {
        
        ArrayList<SmSeries> smSeriesSpectralList = new ArrayList<>();
    
        try {
            // Create file path list.
            ArrayList<String> filePaths = new ArrayList<>();
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                filePaths.add(v2ProcessGUI.getV1File().getPath());
            }
            
            // Get earliest, latest date times and min delta time.
            DateTime earliestStartTime = SmUtils.getEarliestStartTime(filePaths);
            DateTime latestStopTime = SmUtils.getLatestStopTime(filePaths);
            double minDeltaT = SmUtils.getMinimumDeltaT(filePaths);
            
            // Retrieve network and station codes from the first list item.
            SmRec smRec1st = createSmRec(v2ProcessGUIs.get(0));
            String networkCode = smRec1st.getNetworkCode();
            String stationCode = smRec1st.getStationCode();
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Create SmRec object from v2ProcessGUI object.
                SmRec smRec = createSmRec(v2ProcessGUI);
                
                // Create seismic data list.
                //ArrayList<SmPoint> dataSeismic = smRec.createAdjustedSmPointsPadded(
                    //earliestStartTime, latestStopTime);
                ArrayList<SmPoint> dataSeismic = smRec.createAdjustedSmPointsUnpadded(
                    earliestStartTime, latestStopTime);
                
                // Create spectral data list.
                ArrayList<SmPoint> adjustedSmPointsFFT = SmRec.createSmPointsFFT(dataSeismic, 
                    minDeltaT, MSEC_TO_SEC);

                ArrayList<SmPoint> dataSpectral = new ArrayList<>();

                for (int i=1; i<adjustedSmPointsFFT.size()/2; i++) //skip 1st point, frequency=0Hz
                {
                    SmPoint smPointFFT = adjustedSmPointsFFT.get(i);
                    dataSpectral.add(new SmPoint(smPointFFT.getX(),
                        smPointFFT.getY()));
                }
                
                int dataParmCode = smRec.getDataParmCode();
                String channel = smRec.getChannel();
                String seed = "";
                String lCode = "";

                Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
                Matcher m = p.matcher(channel);

                if (m.find()) {
                    seed = m.group(1);
                    lCode = m.group(3);
                }

                // Initialize SmStation and SmChannel objects.
                SmStation smStation = (smTemplate != null) ? 
                    smTemplate.getSmStation() : null;
                SmEpoch smEpoch = (smStation != null) ?
                    smStation.getSmEpoch(latestStopTime, latestStopTime) : null;
                SmChannel smChannel = (smEpoch != null) ?
                    smEpoch.getSmChannel(channel) : null;

                // Set series title and description.
                String title = channel;
                String description = smRec.getEventName() + "_" + 
                    smRec.getNetworkCode() + "." + smRec.getStationCode() + "." + 
                    seed + "." + lCode;
                
                // Create color for series.
                Color color;
                
                if (smTemplate != null) {
                    String tmplNetworkCode = smTemplate.getSmStation().getNetworkCode();
                    String tmplStationCode = smTemplate.getSmStation().getStationCode();
                    
                    if (networkCode.equals(tmplNetworkCode) && stationCode.equals(tmplStationCode)) {
                        color = (smChannel != null) ? smChannel.getColor() :
                            new Color(SmPreferences.ChartOptions.getColorPlot());
                    }
                    else {
                        color = new Color(SmPreferences.ChartOptions.getColorPlot());
                    }
                }
                else {
                    color = new Color(SmPreferences.ChartOptions.getColorPlot());
                }
                
                if (!dataSpectral.isEmpty())
                    smSeriesSpectralList.add(new SmSeries(dataSpectral,dataParmCode,
                    title,description,color,v2ProcessGUI));
            }
            
            // Use template, if supplied, to order the series. Otherwise, use default sort.
            if (smTemplate != null) {
                String tmplNetworkCode = smTemplate.getSmStation().getNetworkCode();
                String tmplStationCode = smTemplate.getSmStation().getStationCode();
                
                if (networkCode.equals(tmplNetworkCode) && stationCode.equals(tmplStationCode)) {
                    smSeriesSpectralList = SmUtils.createTemplateBasedList(smSeriesSpectralList,smTemplate,
                        earliestStartTime, latestStopTime);
                }
                else {
                    Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
                }
            }
            else {
                Collections.sort(smSeriesSpectralList, SmSeries.SmSeriesComparator);
            }
            
            // Remove SmSeries objects with null SmPoints from smSeriesSpectralList.
            ArrayList<SmSeries> tempSmSeriesSpectralList = new ArrayList<>();
            for (SmSeries smSeriesSpectral : smSeriesSpectralList) {
                if (smSeriesSpectral.getSmPoints() != null)
                    tempSmSeriesSpectralList.add(smSeriesSpectral);
            }
            smSeriesSpectralList = tempSmSeriesSpectralList;
            
            return smSeriesSpectralList;
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private ArrayList<SmSeries> getSelectedSmSeriesSpectralList() {
        ArrayList<SmSeries> smSeriesSpectralList = new ArrayList<>();
        
        try {
            // Create list of SmSeries objects for selected ChannelWidgets.
            ChannelsTableModel model = (ChannelsTableModel)tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            for (ChannelWidget channelWidget : channelWidgets) {
                if (channelWidget.getChannelBox().getSelect()) {
                    Object tag = channelWidget.getTag();
                    if (tag instanceof SmSeries)
                        smSeriesSpectralList.add((SmSeries)tag);
                }
            }

            return smSeriesSpectralList;
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private void updateFilterSeedList(ArrayList<SmSeries> smSeriesList) {
        
        DefaultListModel model = new DefaultListModel();
        listFilterSeed.setModel(model);
//        model.addElement(SmGlobal.NO_SELECTION);
        
        // Define search pattern to parse title (i.e., channel).
        Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
        
        // Create list.
        ArrayList<String> seeds = new ArrayList<>();
        for (SmSeries smSeries :smSeriesList) {
            String channel = smSeries.getTitle();
            
            // Parse channel string to retrieve seed and lCode.
            Matcher m = p.matcher(channel);
            
            if (m.find()) {
                String seed = m.group(1);
                
                if (!seeds.contains(seed))
                    seeds.add(seed);
            }
        }
        
        // Sort list.
        Collections.sort(seeds);
        
        // Add list to model
        for (String seed : seeds) {
            model.addElement(seed);
        }
    }
    
    private void updateFilterLCodeList(ArrayList<SmSeries> smSeriesList) {
        DefaultListModel model = new DefaultListModel();
        listFilterLCode.setModel(model);
        
//        model.addElement(SmGlobal.NO_SELECTION);
        
        // Define search pattern to parse title (i.e., channel).
        Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
        
        // Create list.
        ArrayList<String> lCodes = new ArrayList<>();
        for (SmSeries smSeries :smSeriesList) {
            String channel = smSeries.getTitle();
            
            // Parse channel string to retrieve seed and lCode.
            Matcher m = p.matcher(channel);
            
            if (m.find()) {
                String lCode = m.group(3);
                
                if (!lCodes.contains(lCode))
                    lCodes.add(lCode);
            }
        }
        
        // Sort list.
        Collections.sort(lCodes);
        
        // Add list to model.
        for (String lCode : lCodes) {
            model.addElement(lCode);
        }
    }
    
    private void updateChannelsTable(ArrayList<SmSeries> smSeriesList) {
        
        // Get table model.
        ChannelsTableModel model = (ChannelsTableModel)tblChannels.getModel();
        
        // Clear table.
        model.clearData();

        // Define search pattern to parse title (i.e., channel).
        Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
        
        // Iterate SmSeries list, creating a row for each series (i.e., channel).
        for (SmSeries smSeries :smSeriesList) {
            boolean select = true;
            Color color = smSeries.getColor();
            String channel = smSeries.getTitle();
            String seed = "";
            String lCode = "";
            
            // Determine if V1 file has corresponding V2 file.
            V2ProcessGUI v2ProcessGUI = (V2ProcessGUI)smSeries.getTag();
            File v1File = v2ProcessGUI.getV1File();
            String v1FilePath = v1File.getPath();
            File v2File = fetchV2File(new File(v1FilePath));
            boolean v2Exists = (v2File != null);
            HasV2 hasV2 = new HasV2(v2Exists);
            
            // Parse channel string to retrieve seed and lCode.
            Matcher m = p.matcher(channel);
            
            if (m.find()) {
                seed = m.group(1);
                lCode = m.group(3);
            }
            
            model.addChannelWidget(new ChannelWidget(new ChannelBox(select,color),
                model.getRowCount()+1,seed,lCode,hasV2,smSeries));
        }
    }
    
    private void updateChartViewer(ArrayList<SmSeries> smSeriesSpectralList) throws Exception {
        try
        {
            // Clear chart viewer panels.
            this.pnlViewerAcc.removeAll();
           
            // Clear chartViews array.
            this.chartViews.clear();
        
            SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                new QCChart2D_API() : null;

            if (smCharts_API == null)
                return;
            
            // Create a grouped spectral chart.
            if (!smSeriesSpectralList.isEmpty())
            {
                String eventName = retrieveEventName();
                String stationName = retrieveStationName();
                
                String chartTitle = eventName + "_" + stationName;
                String xAxisTitle = "Frequency (Hz)";
                String yAxisTitle = "Fourier Amplitude of Accelaration (cm/sec" + "\u00B2" + ")";

                GroupChartView chartView = (GroupChartView)smCharts_API.createGroupChartLog10_XYScale(
                    smSeriesSpectralList,SmGlobal.PLOT_TYPE_SPECTRAL,chartTitle,
                    xAxisTitle,yAxisTitle,true,null);
                
                if (chartView != null) {
                    chartViews.add(chartView);
                    pnlViewerAcc.add(chartView);
                }
                
                // Set chart view properties.
                setChartViewProperties(chartViews);
            }
        }
        catch (Exception ex) {
            throw ex;
        }
        finally
        {
            pnlViewer.updateUI();
        }
    }
     
    private void setChartViewProperties(ArrayList<SmChartView> chartViews) {
        if (chartViews == null || chartViews.isEmpty())
            return;
        
        JFormattedTextField field;
        SmPreferences.MarkerStyle markerStyle;
        Color color;
        double width;
        
        if (this.rbtnFilterRangeLow.isSelected()) {
            field = this.ftxtFilterRangeLow;
            markerStyle = SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerStyle();
            color = new Color(SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerColor());
            width = SmPreferences.SmFilter_Editor.getFilterRangeLowMarkerWidth();
        }
        else if (this.rbtnFilterRangeHigh.isSelected()) {
            field = this.ftxtFilterRangeHigh;
            markerStyle = SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerStyle();
            color = new Color(SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerColor());
            width = SmPreferences.SmFilter_Editor.getFilterRangeHighMarkerWidth();
        }
        else
            return;
                    
        for (SmChartView chartView : this.chartViews) {
            chartView.setBoundedTextField(field);
            chartView.setSmDataCursorStyle(markerStyle);
            chartView.setSmDataCursorColor(color);
            chartView.setSmDataCursorWidth(width);
        }
    }
    
    private boolean validateFilterInputs() {
        boolean result = true;
        StringBuilder msg = new StringBuilder();
        
        if (this.ftxtFilterRangeLow.getText().isEmpty())
            msg.append("  -Missing Filter Low value\n");
        else if (!isDouble(this.ftxtFilterRangeLow.getText()))
            msg.append("  -Invalid Filter Low value\n");
        
        if (this.ftxtFilterRangeHigh.getText().isEmpty())
            msg.append("  -Missing Filter High value\n");
        else if (!isDouble(this.ftxtFilterRangeHigh.getText()))
            msg.append("  -Invalid Filter High value\n");
        
        if (isDouble(this.ftxtFilterRangeLow.getText()) &&
            isDouble(this.ftxtFilterRangeHigh.getText())) {
            double fLow = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
            double fHigh = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
            
            if (fLow < 0 || fLow > this.xMax)
                msg.append("  -Filter Low value is out of bounds\n");
            else if (fHigh < 0 || fHigh > this.xMax)
                msg.append("  -Filter High value is out of bounds\n");
            else if (fLow > fHigh)
                msg.append("  -Filter Low value cannot be greater than its High value\n");
        }
        
        if (msg.length() > 0) {
            result = false;
             
            JOptionPane.showMessageDialog(this,msg.toString(),"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg.toString());
        }
            
        return result;
    }
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngrpFilterRange = new javax.swing.ButtonGroup();
        splitpaneMain = new javax.swing.JSplitPane();
        splitpaneViewerChannels = new javax.swing.JSplitPane();
        scrollpaneViewer = new javax.swing.JScrollPane();
        pnlViewer = new javax.swing.JPanel();
        pnlViewerAcc = new javax.swing.JPanel();
        splitpaneChannelsControllerTable = new javax.swing.JSplitPane();
        pnlChannelsController = new javax.swing.JPanel();
        btnSelectAll = new javax.swing.JButton();
        btnClearAll = new javax.swing.JButton();
        pnlSelectBy = new javax.swing.JPanel();
        lblFilterSeed = new javax.swing.JLabel();
        lblFilterLCode = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listFilterSeed = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        listFilterLCode = new javax.swing.JList();
        btnFilterSelect = new javax.swing.JButton();
        btnFilterClear = new javax.swing.JButton();
        scrollpaneChannelsTable = new javax.swing.JScrollPane();
        tblChannels = new javax.swing.JTable();
        pnlEditor = new javax.swing.JPanel();
        pnlFilterRange = new javax.swing.JPanel();
        ftxtFilterRangeLow = new javax.swing.JFormattedTextField();
        ftxtFilterRangeHigh = new javax.swing.JFormattedTextField();
        rbtnFilterRangeLow = new javax.swing.JRadioButton();
        rbtnFilterRangeHigh = new javax.swing.JRadioButton();
        btnPreview = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnExit = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.title")); // NOI18N
        setSize(new java.awt.Dimension(1200, 750));

        splitpaneMain.setDividerLocation(520);
        splitpaneMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitpaneMain.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.splitpaneMain.toolTipText")); // NOI18N
        splitpaneMain.setPreferredSize(new java.awt.Dimension(1200, 765));

        splitpaneViewerChannels.setBackground(new java.awt.Color(255, 255, 255));
        splitpaneViewerChannels.setBorder(null);
        splitpaneViewerChannels.setDividerLocation(875);
        splitpaneViewerChannels.setResizeWeight(0.8);

        scrollpaneViewer.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        scrollpaneViewer.setPreferredSize(new java.awt.Dimension(851, 500));

        pnlViewer.setPreferredSize(new java.awt.Dimension(1000, 547));

        pnlViewerAcc.setBackground(new java.awt.Color(255, 255, 255));
        pnlViewerAcc.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlViewerAcc.setName("pnlViewerAcc"); // NOI18N
        pnlViewerAcc.setPreferredSize(new java.awt.Dimension(2, 140));
        pnlViewerAcc.setLayout(new javax.swing.BoxLayout(pnlViewerAcc, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout pnlViewerLayout = new javax.swing.GroupLayout(pnlViewer);
        pnlViewer.setLayout(pnlViewerLayout);
        pnlViewerLayout.setHorizontalGroup(
            pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlViewerAcc, javax.swing.GroupLayout.DEFAULT_SIZE, 936, Short.MAX_VALUE)
                .addGap(65, 65, 65))
        );
        pnlViewerLayout.setVerticalGroup(
            pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlViewerAcc, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                .addContainerGap())
        );

        scrollpaneViewer.setViewportView(pnlViewer);

        splitpaneViewerChannels.setLeftComponent(scrollpaneViewer);

        splitpaneChannelsControllerTable.setBorder(null);
        splitpaneChannelsControllerTable.setDividerLocation(225);
        splitpaneChannelsControllerTable.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        org.openide.awt.Mnemonics.setLocalizedText(btnSelectAll, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnSelectAll.text")); // NOI18N
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnClearAll, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnClearAll.text")); // NOI18N
        btnClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAllActionPerformed(evt);
            }
        });

        pnlSelectBy.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.pnlSelectBy.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFilterSeed, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.lblFilterSeed.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFilterLCode, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.lblFilterLCode.text")); // NOI18N

        jScrollPane2.setViewportView(listFilterSeed);

        jScrollPane1.setViewportView(listFilterLCode);

        org.openide.awt.Mnemonics.setLocalizedText(btnFilterSelect, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnFilterSelect.text")); // NOI18N
        btnFilterSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnFilterClear, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnFilterClear.text")); // NOI18N
        btnFilterClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSelectByLayout = new javax.swing.GroupLayout(pnlSelectBy);
        pnlSelectBy.setLayout(pnlSelectByLayout);
        pnlSelectByLayout.setHorizontalGroup(
            pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectByLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSelectByLayout.createSequentialGroup()
                        .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblFilterSeed)
                            .addComponent(lblFilterLCode))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelectByLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnFilterClear, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFilterSelect)))
                .addContainerGap())
        );

        pnlSelectByLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnFilterClear, btnFilterSelect});

        pnlSelectByLayout.setVerticalGroup(
            pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelectByLayout.createSequentialGroup()
                .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFilterSeed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFilterLCode)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSelectByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilterSelect)
                    .addComponent(btnFilterClear))
                .addGap(55, 55, 55))
        );

        javax.swing.GroupLayout pnlChannelsControllerLayout = new javax.swing.GroupLayout(pnlChannelsController);
        pnlChannelsController.setLayout(pnlChannelsControllerLayout);
        pnlChannelsControllerLayout.setHorizontalGroup(
            pnlChannelsControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlChannelsControllerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlChannelsControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSelectBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlChannelsControllerLayout.createSequentialGroup()
                        .addComponent(btnSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearAll)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlChannelsControllerLayout.setVerticalGroup(
            pnlChannelsControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlChannelsControllerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSelectBy, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlChannelsControllerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectAll)
                    .addComponent(btnClearAll))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        splitpaneChannelsControllerTable.setTopComponent(pnlChannelsController);

        scrollpaneChannelsTable.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        tblChannels.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Select", "Number", "Seed", "LCode"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblChannels.setGridColor(javax.swing.UIManager.getDefaults().getColor("Table.dropLineColor"));
        scrollpaneChannelsTable.setViewportView(tblChannels);

        splitpaneChannelsControllerTable.setRightComponent(scrollpaneChannelsTable);

        splitpaneViewerChannels.setRightComponent(splitpaneChannelsControllerTable);

        splitpaneMain.setTopComponent(splitpaneViewerChannels);

        pnlFilterRange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.pnlFilterRange.border.title"))); // NOI18N

        ftxtFilterRangeLow.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeLow.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.ftxtFilterRangeLow.toolTipText")); // NOI18N
        ftxtFilterRangeLow.setName("FStart"); // NOI18N

        ftxtFilterRangeHigh.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeHigh.setText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.ftxtFilterRangeHigh.text")); // NOI18N
        ftxtFilterRangeHigh.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.ftxtFilterRangeHigh.toolTipText")); // NOI18N
        ftxtFilterRangeHigh.setName("FStop"); // NOI18N

        btngrpFilterRange.add(rbtnFilterRangeLow);
        rbtnFilterRangeLow.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFilterRangeLow, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.rbtnFilterRangeLow.text")); // NOI18N
        rbtnFilterRangeLow.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.rbtnFilterRangeLow.toolTipText")); // NOI18N

        btngrpFilterRange.add(rbtnFilterRangeHigh);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFilterRangeHigh, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.rbtnFilterRangeHigh.text")); // NOI18N
        rbtnFilterRangeHigh.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.rbtnFilterRangeHigh.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnPreview, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnPreview.text")); // NOI18N
        btnPreview.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnPreview.toolTipText")); // NOI18N
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFilterRangeLayout = new javax.swing.GroupLayout(pnlFilterRange);
        pnlFilterRange.setLayout(pnlFilterRangeLayout);
        pnlFilterRangeLayout.setHorizontalGroup(
            pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbtnFilterRangeLow)
                    .addComponent(rbtnFilterRangeHigh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ftxtFilterRangeLow, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(ftxtFilterRangeHigh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(btnPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        pnlFilterRangeLayout.setVerticalGroup(
            pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterRangeLayout.createSequentialGroup()
                .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFilterRangeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ftxtFilterRangeLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbtnFilterRangeLow))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ftxtFilterRangeHigh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbtnFilterRangeHigh)))
                    .addGroup(pnlFilterRangeLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(btnPreview)))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(btnExit, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnExit.text")); // NOI18N
        btnExit.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnExit.toolTipText")); // NOI18N
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnSave.toolTipText")); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnReset.text")); // NOI18N
        btnReset.setToolTipText(org.openide.util.NbBundle.getMessage(SmFilterEditor.class, "SmFilterEditor.btnReset.toolTipText")); // NOI18N
        btnReset.setOpaque(false);
        btnReset.setPreferredSize(new java.awt.Dimension(67, 23));
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExit)
                .addGap(31, 31, 31))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnExit, btnReset, btnSave});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnSave)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlEditorLayout = new javax.swing.GroupLayout(pnlEditor);
        pnlEditor.setLayout(pnlEditorLayout);
        pnlEditorLayout.setHorizontalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditorLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(pnlFilterRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 535, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        pnlEditorLayout.setVerticalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditorLayout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditorLayout.createSequentialGroup()
                        .addComponent(pnlFilterRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditorLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        splitpaneMain.setRightComponent(pnlEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed

        try {
            ChannelsTableModel model = (ChannelsTableModel)this.tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            for (ChannelWidget channelWidget : channelWidgets) {
                channelWidget.getChannelBox().setSelect(true);
            }

            // Notify table that data has changed.
            model.fireTableDataChanged();

            // Update Chart Viewer.
            updateChartViewer(getSelectedSmSeriesSpectralList());
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnClearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAllActionPerformed

        try {
            ChannelsTableModel model = (ChannelsTableModel)this.tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            for (ChannelWidget channelWidget : channelWidgets) {
                channelWidget.getChannelBox().setSelect(false);
            }

            // Notify table that data has changed.
            model.fireTableDataChanged();

            // Update Chart Viewer.
            updateChartViewer(getSelectedSmSeriesSpectralList());
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnClearAllActionPerformed

    private void btnFilterSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterSelectActionPerformed
        try {
            List listSeed = listFilterSeed.getSelectedValuesList();
            List listLCode = listFilterLCode.getSelectedValuesList();

            ChannelsTableModel model = (ChannelsTableModel)this.tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            for (ChannelWidget channelWidget : channelWidgets) {
                // Initialize select state to false.
                channelWidget.getChannelBox().setSelect(false);

                String seed = channelWidget.getSeed();
                String lCode = channelWidget.getLCode();

                if (listSeed.isEmpty() && listLCode.isEmpty()) {
                    channelWidget.getChannelBox().setSelect(true);
                }
                else if (listSeed.isEmpty() && !listLCode.isEmpty()) {
                    if (listLCode.contains(lCode))
                    channelWidget.getChannelBox().setSelect(true);
                }
                else if (!listSeed.isEmpty() && listLCode.isEmpty()) {
                    if (listSeed.contains(seed)){
                        channelWidget.getChannelBox().setSelect(true);
                    }
                }
                else if (!listSeed.isEmpty() && !listLCode.isEmpty()) {
                    if (listSeed.contains(seed) && listLCode.contains(lCode))
                    channelWidget.getChannelBox().setSelect(true);
                }
            }

            // Notify table that data has changed.
            model.fireTableDataChanged();

            // Update Chart Viewer.
            updateChartViewer(getSelectedSmSeriesSpectralList());
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnFilterSelectActionPerformed

    private void btnFilterClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterClearActionPerformed
        listFilterSeed.clearSelection();
        listFilterLCode.clearSelection();
    }//GEN-LAST:event_btnFilterClearActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed

        if (!validateFilterInputs())
            return;

        try {
            this.filtered = true;
            this.setVisible(false);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewActionPerformed

        if (!validateFilterInputs())
            return;

        try {
            // Create copy of V2ProcessGUI array to be filtered.
            ArrayList<V2ProcessGUI> v2ProcessGUIs_Copy = new ArrayList<>();
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                v2ProcessGUIs_Copy.add(new V2ProcessGUI(v2ProcessGUI));
            }
            
            // Set filter corners.
            double filterRangeLow = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
            double filterRangeHigh = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
            
            // Filter the V2ProcessGUI array.
            ArrayList<V2ProcessGUI> v2ProcessGUIs_Filtered = 
                V2ProcessGUI.filterV2ProcessGUIList(v2ProcessGUIs_Copy,filterRangeLow,filterRangeHigh);
            
            // Launch Seismic Chart Viewer to view plots for V2ProcessGUI objects.
            SmSeismicTraceViewer chartViewer =
                new SmSeismicTraceViewer(SmGlobal.SM_CHARTS_API_QCCHART2D,v2ProcessGUIs_Filtered,smTemplate);
            chartViewer.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
            chartViewer.setLocationRelativeTo(this);
            chartViewer.pack();
            chartViewer.setVisible(true);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnPreviewActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        reset();
    }//GEN-LAST:event_btnResetActionPerformed

    private class ChartViewerMouseListener extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent e) {
        }
    }
    
    private class RBtnTypeListener implements ItemListener 
    {  
        @Override
        public void itemStateChanged(ItemEvent e) {
            
            JRadioButton rbtn = (JRadioButton)e.getItem();
            if (rbtn.isSelected())
            {
                // Set properties for each SmChartView object.
                setChartViewProperties(chartViews);
            }
        }
    }
    
    private class SmChartEditorWindowListener extends WindowAdapter
    {
        @Override
        public void windowClosed(WindowEvent e) { 
            if (e.getComponent() instanceof SmFilterEditor) {
                SmFilterEditor chartEditor = (SmFilterEditor)e.getComponent();
                SmCore.addMsgToStatusViewer("Filter edit status: " + chartEditor.getFilterStatus());
            }
        }
    }
    
    private class TextFieldMouseListener extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                return;
            
            Object source = e.getSource();
            
            if (source instanceof JFormattedTextField) {
                
                JFormattedTextField field = (JFormattedTextField)source;
                String fieldName = field.getName();
                
                if (fieldName.equals(ftxtFilterRangeLow.getName()))
                    rbtnFilterRangeLow.setSelected(true);
                else if (fieldName.equals(ftxtFilterRangeHigh.getName()))
                    rbtnFilterRangeHigh.setSelected(true);
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                Object source = e.getSource();
                if (source instanceof JFormattedTextField) {
                    JFormattedTextField field = (JFormattedTextField)source;
                    editPopupMenu.show(field,e.getX(),e.getY());
                }
            }
        }
    }
    
    private class TextFieldPropertyListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            
            if (source instanceof JFormattedTextField) {
                JFormattedTextField field = (JFormattedTextField)source;
                
                for (Component component : pnlViewerAcc.getComponents()) {
                    if (component instanceof SmChartView) {
                        SmChartView chartView = (SmChartView)component;

                        double x = ((Number)field.getValue()).doubleValue();
                        
                        // Draw marker.
                        if (x - 0 >= EPSILON)
                            chartView.getSmDataCursor().drawMarker(x, 0);
                    }
                }
            }
        }
    }
    
    private class ChannelWidget {
        private ChannelBox channelBox;
        private int number;
        private String seed;
        private String lCode;
        private HasV2 hasV2;
        private Object tag;
        
        public ChannelWidget(ChannelBox channelBox, int number, String seed, String lCode,
            HasV2 hasV2, Object tag) {
            this.channelBox = channelBox;
            this.number = number;
            this.seed = seed;
            this.lCode = lCode;
            this.hasV2 = hasV2;
            this.tag = tag;
        }
        
        public ChannelBox getChannelBox() {return this.channelBox;}
        public int getNumber() {return this.number;}
        public String getSeed() {return this.seed;}
        public String getLCode() {return this.lCode;}
        public HasV2 getHasV2() {return this.hasV2;}
        public Object getTag() {return this.tag;}
        public void setChannelBox(ChannelBox channelBox) {this.channelBox = channelBox;}
        public void setNumber(int number) {this.number = number;}
        public void setSeed(String seed) {this.seed = seed;}
        public void setLCode(String lCode) {this.lCode = lCode;}
        public void setHasV2(HasV2 hasV2) {this.hasV2 = hasV2;}
        public void setTag(Object tag) {this.tag = tag;}
    }
    
    private class ChannelsTableModel extends AbstractTableModel {
//        private final String[] columnNames = {"Select","Number","Seed","LCode","V2"};
        private final String[] columnNames = {"Select","Number","Seed","LCode","BLC Src"};
        private final ArrayList<ChannelWidget> data = new ArrayList<>();
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public int getRowCount() {
            return data.size();
        }
        
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            ChannelWidget widget = data.get(row);
            
            switch (col) {
                case 0:
                    return widget.getChannelBox();
                case 1:
                    return widget.getNumber();
                case 2:
                    return widget.getSeed();
                case 3:
                    return widget.getLCode();
                case 4:
                    return widget.getHasV2();
                default:
                    return null;
            }
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0,c).getClass();
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0)   // Select column is editable
                return true;
            else
                return false;
        }
        
        @Override
        public void setValueAt(Object value, int row, int col) {
            ChannelWidget widget = data.get(row);
            
            if (col==0)
                widget.setChannelBox((ChannelBox)value);
        
            fireTableCellUpdated(row,col);
        }
        
        public void clearData() {
            data.clear();
        }
        
        public void addChannelWidget(ChannelWidget widget) {
            data.add(widget);
            fireTableDataChanged();
        }
        
        public void addChannelWidgetList(List l) {
            data.addAll(l);
            fireTableDataChanged();
        }
        
        public ChannelWidget getChannelWidget(int row) {
            return data.get(row);
        }
        
        public ArrayList<ChannelWidget> getChannelWidgets() {
            return data;
        }
    }
    
    private class ChannelBox {
        
        private Boolean select;
        private Color color;
        
        public ChannelBox(Boolean select, Color color) {
            this.select = select;
            this.color = color;
        }
        
        public Boolean getSelect() {return this.select;}
        public Color getColor() {return this.color;}
        public void setSelect(Boolean select) {this.select = select;}
        public void setColor(Color color) {this.color = color;}
        
        @Override
        public String toString() {
            return String.valueOf(select);
        }
    }
    
    private class HasV2 {
        
        private Boolean exists;
        
        public HasV2(Boolean exists) {
            this.exists = exists;
        }
        
        public Boolean getExists() {return this.exists;}
        public void setExists(Boolean exists) {this.exists = exists;}
        
        @Override
        public String toString() {
            return String.valueOf(this.exists);
        }
    }
    
    private class ChannelBoxRenderer extends JCheckBox implements TableCellRenderer {
        
	public ChannelBoxRenderer() {
            setOpaque(true);
	}
	
        @Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int col) {

            this.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (value instanceof ChannelBox) {
                ChannelBox channelBox = (ChannelBox)value;
                this.setSelected(channelBox.getSelect());
                this.setBackground(channelBox.getColor());
            }

            return this;
	}
    } 
    
    private class ChannelBoxEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        
        JCheckBox checkBox;
        ChannelBox channelBox;
        
        public ChannelBoxEditor() {
            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.addActionListener(this);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                channelBox.setSelect(checkBox.isSelected());

                // Deactivate editor to let cell be handled by renderer again.
                fireEditingStopped();

                // Update Chart Viewer.
                updateChartViewer(getSelectedSmSeriesSpectralList());
            }
            catch (Exception ex) {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
        }
        
        @Override
        public Object getCellEditorValue() {
            return channelBox;
        }
        
        @Override
	public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int col) {

            channelBox = (ChannelBox)value;
            checkBox.setSelected(channelBox.getSelect());
            checkBox.setBackground(channelBox.getColor());

            return checkBox;
	}
    }
    
    private class HasV2Renderer extends JLabel implements TableCellRenderer {
        
	public HasV2Renderer() {
            setOpaque(true);
	}
	
        @Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int col) {

//            String checkmark = "\u2714";
//            String xmark = "\u2715";
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setBackground(Color.WHITE);
            
            if (value instanceof HasV2) {
                HasV2 hasV2 = (HasV2)value;
                
                if (hasV2.getExists()) {
//                    this.setForeground(Color.GREEN);
//                    this.setText(checkmark);
                    this.setText(SmGlobal.CosmosFileType.V2.toString());
                }
                else {
//                    this.setForeground(Color.RED);
//                    this.setText(xmark);
                    this.setText(SmGlobal.CosmosFileType.V1.toString());
                }
            }

            return this;
	}
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearAll;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFilterClear;
    private javax.swing.JButton btnFilterSelect;
    private javax.swing.JButton btnPreview;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.ButtonGroup btngrpFilterRange;
    private javax.swing.JFormattedTextField ftxtFilterRangeHigh;
    private javax.swing.JFormattedTextField ftxtFilterRangeLow;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFilterLCode;
    private javax.swing.JLabel lblFilterSeed;
    private javax.swing.JList listFilterLCode;
    private javax.swing.JList listFilterSeed;
    private javax.swing.JPanel pnlChannelsController;
    private javax.swing.JPanel pnlEditor;
    private javax.swing.JPanel pnlFilterRange;
    private javax.swing.JPanel pnlSelectBy;
    private javax.swing.JPanel pnlViewer;
    private javax.swing.JPanel pnlViewerAcc;
    private javax.swing.JRadioButton rbtnFilterRangeHigh;
    private javax.swing.JRadioButton rbtnFilterRangeLow;
    private javax.swing.JScrollPane scrollpaneChannelsTable;
    private javax.swing.JScrollPane scrollpaneViewer;
    private javax.swing.JSplitPane splitpaneChannelsControllerTable;
    private javax.swing.JSplitPane splitpaneMain;
    private javax.swing.JSplitPane splitpaneViewerChannels;
    private javax.swing.JTable tblChannels;
    // End of variables declaration//GEN-END:variables
}
