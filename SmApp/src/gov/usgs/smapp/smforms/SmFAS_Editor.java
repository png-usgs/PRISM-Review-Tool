/*******************************************************************************
 * Name: Java class SmFAS_Editor.java
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

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import COSMOSformat.V3Component;
import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.CORACC;
import static SmConstants.VFileConstants.DATA_PHYSICAL_PARAM_CODE;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.DISPLACE;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.SPECTRA;
import static SmConstants.VFileConstants.UNCORACC;
import SmConstants.VFileConstants.V2DataType;
import static SmConstants.VFileConstants.VELOCITY;
import static SmConstants.VFileConstants.V_UNITS_INDEX;
import SmControl.SmProductGUI;
import SmControl.SmQueue;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.ArrayOps;
import SmProcessing.V2ProcessGUI;
import SmProcessing.V3Process;
import SmUtilities.BuildAPKtable;
import SmUtilities.SmTimeFormatter;
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
import gov.usgs.smcommon.smclasses.SmPreferences.MarkerStyle;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmFAS_Editor extends javax.swing.JFrame {
    private final String chartAPI;
    private final ArrayList<SmFile> srcSmFiles;
    private final SmTemplate smTemplate;
    private final ArrayList<SmFile> v1SmFiles;
    
    private final DateTime earliestDateTime;
    private final DateTime latestDateTime;
    private final double minDeltaT;
    
    private final JPopupMenu editPopupMenu;
    
    private final TextFieldMouseListener textFieldMouseListener = 
        new TextFieldMouseListener();
    private final TextFieldPropertyListener textFieldPropertyListener = 
        new TextFieldPropertyListener();
    private final RBtnTypeListener rbtnTypeListener = new RBtnTypeListener();
    
    private final DecimalFormat decimalFormatter = new DecimalFormat("#,##0.######");
    
    private double initFilterRangeLow;
    private double initFilterRangeHigh;
    
    private String logTime;
    
    private double xMax=0;
   
    private final ArrayList<SmChartView> chartViews = new ArrayList<>();
    
    private boolean status = true;
    
    
    /**
     * Creates new form SmChartEditor
     * @param chartAPI
     * @param srcSmFiles
     * @param smTemplate
     */
    public SmFAS_Editor(String chartAPI, ArrayList<SmFile> srcSmFiles, SmTemplate smTemplate) {
        
        initComponents();
        
        // Set form icon.
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
            getResource("/resources/icons/prism_review_tool_16.png")));
            
        // Set member variables.
        this.chartAPI = chartAPI;
        this.srcSmFiles = srcSmFiles;
        this.smTemplate = smTemplate;
        this.v1SmFiles = createV1SmFiles(this.srcSmFiles);  
        
        // Set earliest and latest start times and minimum delta time 
        // based on the event and station in which the source files belong to.
        //File file = srcSmFiles.get(0).getFile();
        //String filePath = file.getPath();
        //String event = SmCore.extractEventName(filePath);
        //String station = SmCore.extractStationName(filePath);
        
        //this.earliestDateTime = SmCore.getEarliestStartTime(event, station);
        //this.latestDateTime = SmCore.getLatestStopTime(event, station);
        //this.minDeltaT = SmCore.getMinimumDeltaT(event, station);
        
        ArrayList<String> filePaths = new ArrayList<>();
        for (SmFile smFile : srcSmFiles) {
            filePaths.add(smFile.getFile().getPath());
        }
        
        // Set earliest and latest start times and minimum delta time.
        this.earliestDateTime = SmUtils.getEarliestStartTime(filePaths);
        this.latestDateTime = SmUtils.getLatestStopTime(filePaths);
        this.minDeltaT = SmUtils.getMinimumDeltaT(filePaths);

        // Create CCP popup menu.
        editPopupMenu = SmGUIUtils.createCCPPopupMenu();
        
        // Initialize interface components.
        initInterface();
        initChartViewerPanels();
        initChannelsTable();
        initEditorPanel();
        
        // Call reset() to create and display spectral chart.
        reset();
    }
    
    public boolean getStatus() {return this.status;}
    
    private String retrieveEventName() {
        try {
            SmFile smFile = srcSmFiles.get(0);
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
            SmFile smFile = srcSmFiles.get(0);
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
        // Add window listener.
        this.addWindowListener(new SmChartEditorWindowListener());
    }
    
    private void initChartViewerPanels() {
        ChartViewerMouseListener listener = new ChartViewerMouseListener();
        
        this.pnlViewerAcc.addMouseListener(listener);
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
        this.initFilterRangeLow = SmPreferences.SmFAS_Editor.getFilterRangeLow();
        this.initFilterRangeHigh = SmPreferences.SmFAS_Editor.getFilterRangeHigh();

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

        // Add text field listners.
        setTextFieldListeners(true);
        
        // Add radio button listeners.
        setRadioButtonListeners(true);
    }
    
    private void setTextFieldListeners(boolean add) {
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
    
    private void setRadioButtonListeners(boolean add) {
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

            // Reset range text fields.
            this.ftxtFilterRangeLow.setValue(this.initFilterRangeLow);
            this.ftxtFilterRangeHigh.setValue(this.initFilterRangeHigh);

            // Reset initial radio button selection.
            this.rbtnFilterRangeLow.setSelected(true);

            // Create V2ProcessGUI array.
            ArrayList<V2ProcessGUI> v2ProcessGUIs = createV2ProcessGUIs(this.v1SmFiles);
            
            // Create initial SmSeries array.
            ArrayList<SmSeries> smSeriesSpectralList = createSmSeriesSpectralList(v2ProcessGUIs);
            
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

            SmCore.addMsgToStatusViewer("Spectral chart (re)initialized at: " + this.logTime);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private ArrayList<SmFile> createV1SmFiles(ArrayList<SmFile> inSmFiles) {
        ArrayList<SmFile> outSmFiles = new ArrayList<>();
        
        for (SmFile smFile : inSmFiles) {
            if (smFile.getFileDataType().equals(UNCORACC))
                outSmFiles.add(smFile);
            else {
                // Check validity of the file's parent path.
                String parent = smFile.getFile().getParent();
                //String[] parts = parent.split("\\\\");
                String[] parts = parent.split(Pattern.quote(File.separator));
                if (parts.length < 4 || !parts[parts.length-1].matches("V[1,2]")) {
                    String msg = "File is in folder that does not comply with expected folder structure.";
                    showMessage("Invalid Folder Structure",msg,
                        NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                    SmCore.addMsgToStatusViewer("Invalid Folder Structure: " + msg);
                    return null;
                }

                // Form V1 directory path name.
                Path path = smFile.getFile().toPath();
                Path v1Dir = Paths.get(path.getParent().getParent().toString(),"V1");

                if (!Files.isDirectory(v1Dir))
                {
                    showMessage("Path Not Found","No matching V1 directory found.",
                        NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.INFORMATION_MESSAGE);
                    SmCore.addMsgToStatusViewer("No matching V1 directory found.");

                    return null;
                }

                // Build V1 filename string based on V2 file.
                String input = smFile.getFileName();
                String regExCosmosFileType = String.format("(?i)(^.+)([\\.](%s|%s|%s))([\\.](V[\\d][cC]?)$)",
                    SmGlobal.CosmosV2DataType.ACC.toString(),
                    SmGlobal.CosmosV2DataType.VEL.toString(),
                    SmGlobal.CosmosV2DataType.DIS.toString());
                Pattern p = Pattern.compile(regExCosmosFileType);
                Matcher m = p.matcher(input);
                
                StringBuffer result = new StringBuffer();
                while (m.find()) {

                    String fileName = m.group(1);
                    String fileExt = m.group(5);

                    String v1FileExt = fileExt.length() > 2 ? 
                        SmGlobal.CosmosFileType.V1.toString() + fileExt.substring(2) : 
                        SmGlobal.CosmosFileType.V1.toString();

                    m.appendReplacement(result, fileName + "." + v1FileExt);
                }
                String v1FileName = result.toString();
                
                // Create V1 File object.
                Path v1FilePath = Paths.get(v1Dir.toString(),v1FileName);
                File v1File = new File(v1FilePath.toString());

                if (!Files.isRegularFile(v1File.toPath()))
                    return null;

                // Create and return V1 SmFile object.
                outSmFiles.add(new SmFile(v1File));
            }
        }
        
        return outSmFiles;
    }
    
    private ArrayList<V1Component> createV1Components(SmFile v1SmFile) {
        ArrayList<V1Component> v1Components = new ArrayList<>();
        
        try {
            if (v1SmFile == null)
                return null;
            
            if (!v1SmFile.getFileDataType().equals(UNCORACC))
                return null;
            
            File v1File = v1SmFile.getFile();
            
            if (v1File == null)
                return null;
            
            String logDir = SmPreferences.General.getLogsDir();

            SmQueue queue = new SmQueue(v1File,logTime,new File(logDir));
            queue.readInFile(v1File);
            queue.parseVFile(v1SmFile.getFileDataType());

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();

            for (COSMOScontentFormat rec : smList) {
                rec.setFileName(v1SmFile.getFileName());
                rec.setChannel(v1SmFile.getSmRecs().get(0).getChannel());
                v1Components.add((V1Component)rec);
            }
        }
        catch (IOException | FormatException | SmException ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return v1Components;
    }
    
    private ArrayList<V2Component> createV2Components(SmFile v2SmFile) {
        ArrayList<V2Component> v2Components = new ArrayList<>();
        
        try {
            File file = v2SmFile.getFile();
            String fileName = file.getName().toUpperCase();
            
            if (fileName.contains(SmGlobal.CosmosV2DataType.ACC.toString()))
            {
                String logDir = SmPreferences.General.getLogsDir();
            
                SmQueue queue = new SmQueue(file,logTime,new File(logDir));
                queue.readInFile(file);
                queue.parseVFile(CORACC);

                ArrayList<COSMOScontentFormat> smlist = queue.getSmList();
                for (COSMOScontentFormat rec : smlist) { 
                    v2Components.add((V2Component)rec);
                }   
            }
        }
        catch (IOException | FormatException | SmException ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return v2Components;
    }
    
    private ArrayList<V2ProcessGUI> createV2ProcessGUIs_Old(ArrayList<SmFile> v1SmFiles) 
        throws Exception {
                
        int filterOrder = SmPreferences.PrismParams.getButterworthFilterOrder();
        double threshold = SmPreferences.PrismParams.getStrongMotionThresholdPcnt();
        double taperLength = SmPreferences.PrismParams.getButterworthFilterTaperLength();
        
        ArrayList<V2ProcessGUI> v2ProcessGUIs = new ArrayList<>();
        
        String rxBLC = 
            "(?i)(^|<([AV])(BL[\\w]+)>)([\\s]*)" +
            "(SF:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(EF:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(SA:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(EA:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(ORDER:[\\s]*([\\w]+))([\\s]*)";
        Pattern pBLC = Pattern.compile(rxBLC);
        
        try {
            for (SmFile v1SmFile : v1SmFiles) {
                File v1File = v1SmFile.getFile();
                
                if (v1File == null)
                    continue;
                
                File v2File = fetchV2AccFile(v1File);
                
                if (v2File != null) {
                    SmFile v2SmFile = new SmFile(v2File);

                    // Create V1 and V2 component lists.
                    ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
                    ArrayList<V2Component> v2Components = createV2Components(v2SmFile);

                    if (v1Components.isEmpty() || v2Components.isEmpty())
                        continue;

                    // Retrieve first element from V1 and V2 lists.
                    V1Component v1Component = v1Components.get(0);
                    V2Component v2Component = v2Components.get(0);

                    // Extract event onset.
                    double eventOnset = v2Component.extractEONSETfromComments();
                    
                    // Extract deltaT and convert to seconds.
                    double deltaT = v2Component.getRealHeaderValue(DELTA_T);
                    double dTime = deltaT*MSEC_TO_SEC;
                    int diffOrder = SmPreferences.PrismParams.getDifferentialOrder();

                    // Create V2ProcessGUI.
                    V2ProcessGUI v2ProcessGUI = new V2ProcessGUI(v1Component,v1File,
                        filterOrder,threshold,taperLength,eventOnset,diffOrder);

                    // Create and initialize ABC-related variables.
                    //double[] abcArr = null; //baseline function results
                    int break1 = -999; //index value of EA from ABC1
                    int break2 = -999; //index value of SA from ABC2
                    
                    // Apply baseline correction as annotated in comments.
                    for (String comment : v2Component.getComments()) {
                        Matcher m = pBLC.matcher(comment);
                        if (m.find()) {
                            String v2DataType = m.group(2); // V or A
                            String blType = m.group(3);   // BLC or BLABC#, where # is a number
                            double fStart = Double.parseDouble(m.group(6));
                            double fStop = Double.parseDouble(m.group(9));
                            double aStart = Double.parseDouble(m.group(12));
                            double aStop = Double.parseDouble(m.group(15));
                            String order = m.group(18);
                            
                            int aStartIndx = (int)(aStart / dTime);
                            int aStopIndx = (int)(aStop / dTime);
                            int aLength = aStopIndx-aStartIndx;
                            
                            // Get correction order type.
                            VFileConstants.CorrectionOrder cType;
                            if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.MEAN.toString()))
                                cType = VFileConstants.CorrectionOrder.MEAN;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER1.toString()))
                                cType = VFileConstants.CorrectionOrder.ORDER1;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER2.toString()))
                                cType = VFileConstants.CorrectionOrder.ORDER2;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER3.toString()))
                                cType = VFileConstants.CorrectionOrder.ORDER3;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.SPLINE.toString()))
                                cType = VFileConstants.CorrectionOrder.SPLINE;
                            else
                                continue;

                            // Perform baseline correction.
                            if (blType.equals("BLC")) {
                                if (v2DataType.equalsIgnoreCase("A")) {
                                    double[] inArr = v2ProcessGUI.getV2Array(VFileConstants.V2DataType.ACC);
                                    double[] baseline = v2ProcessGUI.getBaselineFunction(inArr, 
                                        dTime, fStart, fStop, cType);

                                    v2ProcessGUI = V2ProcessGUI.makeBaselineCorrection(V2DataType.ACC, 
                                        baseline, v2ProcessGUI, dTime, aStart, aStop, false);
                                    v2ProcessGUI.addBaselineProcessingStep(fStart, fStop, aStart, aStop, 
                                        V2DataType.ACC, V2DataType.ACC, VFileConstants.BaselineType.BESTFIT, 
                                        cType, cType, 0);

                                    double[] vel = v2ProcessGUI.integrateAnArray(inArr, dTime, eventOnset);
                                    double[] dis = v2ProcessGUI.integrateAnArray(vel, dTime, eventOnset);
                                    v2ProcessGUI.setVelocity(vel);
                                    v2ProcessGUI.setDisplacement(dis);
                                }
                                else if (v2DataType.equalsIgnoreCase("V")){
                                    double[] inArr = v2ProcessGUI.getV2Array(VFileConstants.V2DataType.VEL);
                                    double[] baseline = v2ProcessGUI.getBaselineFunction(inArr, 
                                        dTime, fStart, fStop, cType);

                                    //v2ProcessGUI.makeBaselineCorrection(inArr, baseline, dTime, aStart, aStop);
                                    v2ProcessGUI = V2ProcessGUI.makeBaselineCorrection(V2DataType.VEL, 
                                        baseline, v2ProcessGUI, dTime, aStart, aStop, false);
                                    v2ProcessGUI.addBaselineProcessingStep(fStart, fStop, aStart, aStop, 
                                        V2DataType.VEL, V2DataType.VEL, VFileConstants.BaselineType.BESTFIT, 
                                        cType, cType, 0);

                                    double[] dis = v2ProcessGUI.integrateAnArray(inArr, dTime, eventOnset);
                                    double[] acc = ArrayOps.differentiate(inArr, dTime);
                                    //double[] acc = ArrayOps.CentDiff(inArr, dTime);
                                    v2ProcessGUI.setDisplacement(dis);
                                    v2ProcessGUI.setAcceleration(acc);
                                }
                            }
                        }
                    }
                    
                    // Add V2ProcessGUI object to list.
                    v2ProcessGUIs.add(v2ProcessGUI);
                }
                else {
                    // Create V1 and V2 component lists.
                    ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
                    
                    // Retrieve first element from V1 list.
                    V1Component v1Component = v1Components.get(0);
                    
                    // Set Event Onset to taper length.
                    //double eventOnset = taperLength;
                    
                    double eventOnset = 0;
                    int diffOrder = SmPreferences.PrismParams.getDifferentialOrder();
                    
                    // Create V2ProcessGUI.
                    V2ProcessGUI v2ProcessGUI = new V2ProcessGUI(v1Component,v1File,
                        filterOrder,threshold,taperLength,eventOnset,diffOrder);
                    
                    // Add V2ProcessGUI object to list.
                    v2ProcessGUIs.add(v2ProcessGUI);
                }
            }
            
            return v2ProcessGUIs;
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private ArrayList<V2ProcessGUI> createV2ProcessGUIs(ArrayList<SmFile> v1SmFiles) 
        throws Exception {
                
        // Creates an array of V2ProcessGUI objects. 
        // A V2ProcessGUI object is created for each V1 file. An attempt is made to 
        // fetch the associated V2 acc file. If found, a V2ProcessGUI is created and
        // initiated using the V1 file, then BLC steps extracted from 
        // comments in the V2 file are applied to the V2ProcessGUI. If not found,
        // a V2ProcessGUI is created simply using the V1 file.
        
        int filterOrder = SmPreferences.PrismParams.getButterworthFilterOrder();
        double threshold = SmPreferences.PrismParams.getStrongMotionThresholdPcnt();
        double taperLength = SmPreferences.PrismParams.getButterworthFilterTaperLength();
        int diffOrder = SmPreferences.PrismParams.getDifferentialOrder();
        
        ArrayList<V2ProcessGUI> v2ProcessGUIs = new ArrayList<>();
        
        String rxBLC = 
            "(?i)(^|<([AV])(BL[\\w]+)>)([\\s]*)" +
            "(SF:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(EF:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(SA:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(EA:[\\s]*([\\d]+.[\\d]+))(,[\\s]*)" +
            "(ORDER:[\\s]*([\\w]+))([\\s]*)";
        Pattern pBLC = Pattern.compile(rxBLC);
        
        try {
            for (SmFile v1SmFile : v1SmFiles) {
                File v1File = v1SmFile.getFile();
                
                if (v1File == null)
                    continue;
                
                File v2File = fetchV2AccFile(v1File);
                
                if (v2File != null) {
                    SmFile v2SmFile = new SmFile(v2File);
                    
                    // Create V1 and V2 component lists.
                    ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
                    ArrayList<V2Component> v2Components = createV2Components(v2SmFile);

                    if (v1Components.isEmpty() || v2Components.isEmpty())
                        continue;
                    
                    // Retrieve first element from V1 and V2 lists.
                    V1Component v1Component = v1Components.get(0);
                    V2Component v2Component = v2Components.get(0);

                    // Extract event onset.
                    double eventOnset = v2Component.extractEONSETfromComments();
                    
                    // Extract deltaT and convert to seconds.
                    double deltaT = v2Component.getRealHeaderValue(DELTA_T);
                    double dTime = deltaT*MSEC_TO_SEC;
                    
                    // Create V2ProcessGUI.
                    V2ProcessGUI v2ProcessGUI = new V2ProcessGUI(v1Component,v1File,
                        filterOrder,threshold,taperLength,eventOnset,diffOrder);
                
                    // Apply BLC steps to V2ProcessGUI object.
                    for (String comment : v2Component.getComments()) {
                        Matcher m = pBLC.matcher(comment);
                        if (m.find()) {
                            String v2DataType = m.group(2); // V or A
                            String blType = m.group(3);   // BLC or BLABC#, where # is a number
                            double fStart = Double.parseDouble(m.group(6));
                            double fStop = Double.parseDouble(m.group(9));
                            double aStart = Double.parseDouble(m.group(12));
                            double aStop = Double.parseDouble(m.group(15));
                            String order = m.group(18);
                            
                            // Get correction order type.
                            VFileConstants.CorrectionOrder cTypeOrig;
                            if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.MEAN.toString()))
                                cTypeOrig = VFileConstants.CorrectionOrder.MEAN;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER1.toString()))
                                cTypeOrig = VFileConstants.CorrectionOrder.ORDER1;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER2.toString()))
                                cTypeOrig = VFileConstants.CorrectionOrder.ORDER2;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.ORDER3.toString()))
                                cTypeOrig = VFileConstants.CorrectionOrder.ORDER3;
                            else if (order.equalsIgnoreCase(VFileConstants.CorrectionOrder.SPLINE.toString()))
                                cTypeOrig = VFileConstants.CorrectionOrder.SPLINE;
                            else
                                continue;
                            
                            // Perform baseline correction.
                            if (blType.equals("BLC")) {
                                if (v2DataType.equalsIgnoreCase("A")) {
                                    double[] inArr = v2ProcessGUI.getV2Array(VFileConstants.V2DataType.ACC);
                                    
                                    double[] baseline = v2ProcessGUI.getBaselineFunction(inArr, 
                                        dTime, fStart, fStop, cTypeOrig);
                                    
                                    v2ProcessGUI = V2ProcessGUI.makeBaselineCorrection(V2DataType.ACC, 
                                        baseline, v2ProcessGUI, dTime, aStart, aStop, false);
                                    
                                    // Integrate and integrate to get velocity and displacement, respectively.
                                    double[] acc = v2ProcessGUI.getAcceleration();
                                    double[] vel = v2ProcessGUI.integrateAnArray(acc, dTime, eventOnset);
                                    double[] dis = v2ProcessGUI.integrateAnArray(vel, dTime, eventOnset);
                                    v2ProcessGUI.setVelocity(vel);
                                    v2ProcessGUI.setDisplacement(dis);

                                    // Set original and final V2DataType variables.
                                    V2DataType v2DataTypeOrig = V2DataType.ACC;
                                    V2DataType v2DataTypeFinal = V2DataType.ACC;

                                    // Set final correction type variable.
                                    VFileConstants.CorrectionOrder cTypeFinal = cTypeOrig;
                                    
                                    v2ProcessGUI.addBaselineProcessingStep(fStart, fStop, aStart, aStop, 
                                        v2DataTypeOrig, v2DataTypeFinal, VFileConstants.BaselineType.BESTFIT, 
                                        cTypeOrig, cTypeFinal, 0);
                                }
                                else if (v2DataType.equalsIgnoreCase("V")){
                                    double[] inArr = v2ProcessGUI.getV2Array(VFileConstants.V2DataType.VEL);
                                    double[] baseline = v2ProcessGUI.getBaselineFunction(inArr, 
                                        dTime, fStart, fStop, cTypeOrig);

                                    v2ProcessGUI = V2ProcessGUI.makeBaselineCorrection(V2DataType.VEL, 
                                        baseline, v2ProcessGUI, dTime, aStart, aStop, false);
                                    
                                    // Integrate and integrate to get velocity and displacement, respectively.
                                    double[] acc = v2ProcessGUI.getAcceleration();
                                    double[] vel = v2ProcessGUI.integrateAnArray(acc, dTime, eventOnset);
                                    double[] dis = v2ProcessGUI.integrateAnArray(vel, dTime, eventOnset);
                                    v2ProcessGUI.setVelocity(vel);
                                    v2ProcessGUI.setDisplacement(dis);
                                    
                                    // Set original and final V2DataType variables.
                                    V2DataType v2DataTypeOrig = V2DataType.VEL;
                                    V2DataType v2DataTypeFinal = V2DataType.ACC;
                                    
                                    // Set final correction type variable.
                                    VFileConstants.CorrectionOrder cTypeFinal;
                                    
                                    if (cTypeOrig == VFileConstants.CorrectionOrder.ORDER2)
                                        cTypeFinal = VFileConstants.CorrectionOrder.ORDER1;
                                    else if (cTypeOrig == VFileConstants.CorrectionOrder.ORDER1)
                                        cTypeFinal = VFileConstants.CorrectionOrder.MEAN;
                                    else
                                        cTypeFinal = null;
                                    
                                    v2ProcessGUI.addBaselineProcessingStep(fStart, fStop, aStart, aStop, 
                                        v2DataTypeOrig, v2DataTypeFinal, VFileConstants.BaselineType.BESTFIT, 
                                        cTypeOrig, cTypeFinal, 0);
                                }
                            }
                        }
                    }
                    
                    // Add V2ProcessGUI object to list.
                    v2ProcessGUIs.add(v2ProcessGUI);
                    
                }
                else {
                    // Create V1 component list.
                    ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
                    
                    // Retrieve first element from V1 list.
                    V1Component v1Component = v1Components.get(0);
                    
                    // Set Event Onset to zero.
                    double eventOnset = 0;
                    
                    // Create V2ProcessGUI.
                    V2ProcessGUI v2ProcessGUI = new V2ProcessGUI(v1Component,v1File,
                        filterOrder,threshold,taperLength,eventOnset,diffOrder);
                    
                    // Add V2ProcessGUI object to list.
                    v2ProcessGUIs.add(v2ProcessGUI);
                }
            }
            
            return v2ProcessGUIs;
        }
        catch (Exception ex) {
            throw new Exception("Unable to process relative V2 files. " +
                "The V2 files may not be current relative to their V1 files. " +
                "If so, perform PRISM processing on the V1s to generate new V2s.");
        }
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
    
    private File fetchV2AccFile(File v1File) {
        // Build V1 filename string without file extension.
        String fileName = v1File.getName();
        String regExCosmosFileType = String.format("(?i)(^.+)([\\.](V[\\d][cC]?)$)");
        Pattern p = Pattern.compile(regExCosmosFileType);
        Matcher m = p.matcher(fileName);
        
        String baseFileName = "";
     
        if (m.find())
            baseFileName = m.group(1) + "." + SmGlobal.CosmosV2DataType.ACC.toString().toLowerCase();
        
        Path path = v1File.toPath();
        String v2PathStr = path.getParent().getParent().toString() + File.separator + "V2";
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
            //DateTime earliestDateTime = SmUtils.getEarliestDateTime(filePaths);
            //DateTime latestDateTime = SmUtils.getLatestDateTime(filePaths);
            //double minDeltaT = SmUtils.getMinimumDeltaT(filePaths);
            
            // Retrieve network and station codes from the first list item.
            SmRec smRec1st = createSmRec(v2ProcessGUIs.get(0));
            String networkCode = smRec1st.getNetworkCode();
            String stationCode = smRec1st.getStationCode();
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Create SmRec object from v2ProcessGUI object.
                SmRec smRec = createSmRec(v2ProcessGUI);
               
                // Create seismic data list.
                //ArrayList<SmPoint> dataSeismic = smRec.createAdjustedSmPointsPadded(
                    //earliestDateTime, latestDateTime);
                ArrayList<SmPoint> dataSeismic = smRec.createAdjustedSmPointsUnpadded(
                    earliestDateTime, latestDateTime);

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
                    smStation.getSmEpoch(latestDateTime, latestDateTime) : null;
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
                        earliestDateTime, latestDateTime);
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
                    xAxisTitle,yAxisTitle,true,this);
                
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
        MarkerStyle markerStyle;
        Color color;
        double width;
        
        if (this.rbtnFilterRangeLow.isSelected()) {
            field = this.ftxtFilterRangeLow;
            markerStyle = SmPreferences.SmFAS_Editor.getFilterRangeLowMarkerStyle();
            color = new Color(SmPreferences.SmFAS_Editor.getFilterRangeLowMarkerColor());
            width = SmPreferences.SmFAS_Editor.getFilterRangeLowMarkerWidth();
        }
        else if (this.rbtnFilterRangeHigh.isSelected()) {
            field = this.ftxtFilterRangeHigh;
            markerStyle = SmPreferences.SmFAS_Editor.getFilterRangeHighMarkerStyle();
            color = new Color(SmPreferences.SmFAS_Editor.getFilterRangeHighMarkerColor());
            width = SmPreferences.SmFAS_Editor.getFilterRangeHighMarkerWidth();
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
    
    private ArrayList<V2ProcessGUI> createFilteredV2ProcessGUIList_Old() throws Exception {
        
        try {
            // Get table model data.
            ChannelsTableModel model = (ChannelsTableModel)tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            // Create V2ProcessGUI array.
            ArrayList<V2ProcessGUI> v2ProcessGUIs = new ArrayList<>();

            for (ChannelWidget channelWidget : channelWidgets) {
                if (this.rbtnApplyToSelectedChannels.isSelected()) {
                    ChannelBox channelBox = channelWidget.getChannelBox();
                    if (channelBox.getSelect()) {
                        SmSeries smSeries = (SmSeries)channelWidget.getTag();
                        V2ProcessGUI v2ProcessGUI = (V2ProcessGUI)smSeries.getTag();
                        v2ProcessGUIs.add(v2ProcessGUI);
                    }
                }
                else {
                    SmSeries smSeries = (SmSeries)channelWidget.getTag();
                    V2ProcessGUI v2ProcessGUI = (V2ProcessGUI)smSeries.getTag();
                    v2ProcessGUIs.add(v2ProcessGUI);
                }
            }

            // Filter acceleration data for each V2ProcessGUI object.
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Set filter corners.
                double low = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
                double high = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
                v2ProcessGUI.setLowFilterCorner(low);
                v2ProcessGUI.setHighFilterCorner(high);
                
                // Call preview processing to apply filter.
                v2ProcessGUI.previewProcessing(V2DataType.ACC);
            }
            
            return v2ProcessGUIs;
        }
        catch (Exception ex ) {
            throw ex;
        }
    }
    
    private ArrayList<V2ProcessGUI> createFilteredV2ProcessGUIList() throws Exception {
        
        try {
            // Get table model data.
            ChannelsTableModel model = (ChannelsTableModel)tblChannels.getModel();
            ArrayList<ChannelWidget> channelWidgets = model.getChannelWidgets();

            // Create V2ProcessGUI array.
            ArrayList<V2ProcessGUI> v2ProcessGUIs = new ArrayList<>();

            for (ChannelWidget channelWidget : channelWidgets) {
                if (this.rbtnApplyToSelectedChannels.isSelected()) {
                    ChannelBox channelBox = channelWidget.getChannelBox();
                    if (channelBox.getSelect()) {
                        SmSeries smSeries = (SmSeries)channelWidget.getTag();
                        V2ProcessGUI v2ProcessGUI = (V2ProcessGUI)smSeries.getTag();
                        v2ProcessGUIs.add(new V2ProcessGUI(v2ProcessGUI));
                    }
                }
                else {
                    SmSeries smSeries = (SmSeries)channelWidget.getTag();
                    V2ProcessGUI v2ProcessGUI = (V2ProcessGUI)smSeries.getTag();
                    v2ProcessGUIs.add(new V2ProcessGUI(v2ProcessGUI));
                }
            }

            // Filter acceleration data for each V2ProcessGUI object.
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Set filter corners.
                double low = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
                double high = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
                v2ProcessGUI.setLowFilterCorner(low);
                v2ProcessGUI.setHighFilterCorner(high);
                
                // Call filter processing.
                v2ProcessGUI.filterProcessing();
            }
            
            return v2ProcessGUIs;
        }
        catch (Exception ex ) {
            throw ex;
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
    
    private void addToLogList(ArrayList<String> logList, String msg) {
        logList.add(msg);
        SmCore.addMsgToStatusViewer(msg);
    }
    
    private void cleanUpFiles(ArrayList<String> logList) {
        try {
            addToLogList(logList,"Cleaning up files ...");
            
            String trashDir = SmPreferences.General.getTrashDir();
            
            for (SmFile v1SmFile : v1SmFiles) {
                String inV1FilePath = v1SmFile.getFile().getPath();
                boolean trouble = SmCore.inTroubleFolder(inV1FilePath);

                if (trouble) {
                    String eventsRootDir = SmCore.extractEventsRootDir(inV1FilePath);
                    String event = SmCore.extractEventName(inV1FilePath);
                    String station = SmCore.extractStationName(inV1FilePath);
                    String v1FileName = SmCore.extractFileName(inV1FilePath);
                
                    String regExCosmosFileType = String.format("(?i)(^.+)([\\.](V[\\d][cC]?)$)");
                    Pattern p = Pattern.compile(regExCosmosFileType);
                    Matcher m = p.matcher(v1FileName);

                    String v1BaseFileName = "";
                    String v1FileExt = "";
                    String v1FileExtC = "";

                    if (m.find()) {
                        v1BaseFileName = m.group(1);
                        v1FileExt = m.group(3);
                        v1FileExtC = v1FileExt.length() > 2 ? v1FileExt.substring(2) : "";
                    }

                    String v0FileName = v1BaseFileName + ".V0" + v1FileExtC;
                    String v2AccFileName = v1BaseFileName + ".acc.V2" + v1FileExtC;
                    String v2VelFileName = v1BaseFileName + ".vel.V2" + v1FileExtC;
                    String v2DisFileName = v1BaseFileName + ".dis.V2" + v1FileExtC;
                    String v3FileName = v1BaseFileName + ".V3" + v1FileExtC;
                    
                    Path pathInV0File = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V0",v0FileName);
                    Path pathInV1File = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V1",v1FileName);
                    Path pathInV2AccFile = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V2",v2AccFileName);
                    Path pathInV2VelFile = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V2",v2VelFileName);
                    Path pathInV2DisFile = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V2",v2DisFileName);
                    Path pathInV3File = Paths.get(eventsRootDir,event,station,
                        SmGlobal.TROUBLE,"V3",v3FileName);
                    
                    if (!trashDir.isEmpty()) {
                        // Copy V0 file to trash folder.
                        Path pathTrashV0File = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V0",v0FileName);
                        if (SmUtils.copyFileToTrash(pathInV0File.toFile(), pathTrashV0File.toFile())) {
                            addToLogList(logList,String.format("%s copied to trash.", pathInV0File.getFileName()));
                        }
                        
                        // Move V0 file from trouble to regular folder.
                        Path pathOutV0File = Paths.get(eventsRootDir,event,station,
                            "V0",v0FileName);
                        if (SmUtils.moveFile(pathInV0File.toFile(),pathOutV0File.toFile())) {
                            addToLogList(logList,String.format("%s moved.", pathInV0File.getFileName()));
                        }
                        
                        // Copy V1 file to trash folder.
                        Path pathTrashV1File = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V1",v1FileName);
                        if (SmUtils.copyFileToTrash(pathInV1File.toFile(), pathTrashV1File.toFile())) {
                            addToLogList(logList,String.format("%s copied to trash.", pathInV1File.getFileName()));
                        }
                        
                        // Move V1 file from trouble to regular folder.
                        Path pathOutV1File = Paths.get(eventsRootDir,event,station,
                           "V1",v1FileName);
                        if (SmUtils.moveFile(pathInV1File.toFile(),pathOutV1File.toFile())) {
                            // Update v1SmFile to reflect new V1 file location.
                            v1SmFile.setFile(new File(pathOutV1File.toFile().getPath()));
                            addToLogList(logList,String.format("%s moved.", pathInV1File.getFileName()));
                        }
                        
                        // Move V2 files from trouble folder to the trash folder.
                        Path pathTrashV2AccFile = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V2",v2AccFileName);
                        Path pathTrashV2VelFile = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V2",v2VelFileName);
                        Path pathTrashV2DisFile = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V2",v2DisFileName);

                        if (SmUtils.moveFileToTrash(pathInV2AccFile.toFile(), pathTrashV2AccFile.toFile())) {
                            addToLogList(logList,String.format("%s moved to trash.", pathInV2AccFile.getFileName()));
                        }
                        if (SmUtils.moveFileToTrash(pathInV2VelFile.toFile(), pathTrashV2VelFile.toFile())) {
                            addToLogList(logList,String.format("%s moved to trash.", pathInV2VelFile.getFileName()));
                        }
                        if (SmUtils.moveFileToTrash(pathInV2DisFile.toFile(), pathTrashV2DisFile.toFile())) {
                            addToLogList(logList,String.format("%s moved to trash.", pathInV2DisFile.getFileName()));
                        }
                        
                        // Move V3 file from trouble folder to the trash folder.
                        Path pathTrashV3File = Paths.get(trashDir,event,station,
                            SmGlobal.TROUBLE,"V3",v3FileName);

                        if (SmUtils.moveFileToTrash(pathInV3File.toFile(), pathTrashV3File.toFile())) {
                            addToLogList(logList,String.format("%s moved to trash.", pathInV3File.getFileName()));
                        }
                    }
                    else {
                        // Move V0 file from trouble to regular folder.
                        Path pathOutV0File = Paths.get(eventsRootDir,event,station,
                            "V0",v0FileName);
                        if (SmUtils.moveFile(pathInV0File.toFile(),pathOutV0File.toFile())) {
                            addToLogList(logList,String.format("%s moved.", pathInV0File.getFileName()));
                        }
                        
                        // Move V1 file from trouble to regular folder.
                        Path pathOutV1File = Paths.get(eventsRootDir,event,station,
                           "V1",v1FileName);
                        if (SmUtils.moveFile(pathInV1File.toFile(),pathOutV1File.toFile())) {
                            // Update v1SmFile to reflect new V1 file location.
                            v1SmFile.setFile(new File(pathOutV1File.toFile().getPath()));
                            addToLogList(logList,String.format("%s moved.", pathInV1File.getFileName()));
                        }
                        
                        // Delete V2 files.
                        if (SmUtils.deleteFile(pathInV2AccFile.toFile())) {
                            addToLogList(logList,String.format("%s deleted.", pathInV2AccFile.getFileName()));
                        }
                        if (SmUtils.deleteFile(pathInV2VelFile.toFile())) {
                            addToLogList(logList,String.format("%s deleted.", pathInV2VelFile.getFileName()));
                        }
                        if (SmUtils.deleteFile(pathInV2DisFile.toFile())) {
                            addToLogList(logList,String.format("%s deleted.", pathInV2DisFile.getFileName()));
                        }
                        
                        // Delete V3 file.
                        if (SmUtils.deleteFile(pathInV3File.toFile())) {
                            addToLogList(logList,String.format("%s deleted.", pathInV3File.getFileName()));
                        }
                    }
                }
            }
            
            addToLogList(logList,"Clean-up completed.");
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
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
        btngrpApplyTo = new javax.swing.ButtonGroup();
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
        pnlApplyTo = new javax.swing.JPanel();
        rbtnApplyToSelectedChannels = new javax.swing.JRadioButton();
        rbtnApplyToAllChannels = new javax.swing.JRadioButton();
        btnPreview = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnReset = new javax.swing.JButton();
        btnCommit = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(850, 650));

        splitpaneMain.setDividerLocation(500);
        splitpaneMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitpaneMain.setResizeWeight(0.9);

        splitpaneViewerChannels.setBackground(new java.awt.Color(255, 255, 255));
        splitpaneViewerChannels.setBorder(null);
        splitpaneViewerChannels.setDividerLocation(875);

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
                .addComponent(pnlViewerAcc, javax.swing.GroupLayout.DEFAULT_SIZE, 925, Short.MAX_VALUE)
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

        org.openide.awt.Mnemonics.setLocalizedText(btnSelectAll, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnSelectAll.text")); // NOI18N
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnClearAll, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnClearAll.text")); // NOI18N
        btnClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAllActionPerformed(evt);
            }
        });

        pnlSelectBy.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.pnlSelectBy.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFilterSeed, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.lblFilterSeed.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFilterLCode, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.lblFilterLCode.text")); // NOI18N

        jScrollPane2.setViewportView(listFilterSeed);

        jScrollPane1.setViewportView(listFilterLCode);

        org.openide.awt.Mnemonics.setLocalizedText(btnFilterSelect, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnFilterSelect.text")); // NOI18N
        btnFilterSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnFilterClear, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnFilterClear.text")); // NOI18N
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
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelectByLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnFilterClear)
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
                .addContainerGap(20, Short.MAX_VALUE))
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
        if (tblChannels.getColumnModel().getColumnCount() > 0) {
            tblChannels.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.tblChannels.columnModel.title0")); // NOI18N
            tblChannels.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.tblChannels.columnModel.title1")); // NOI18N
            tblChannels.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.tblChannels.columnModel.title2")); // NOI18N
            tblChannels.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.tblChannels.columnModel.title3")); // NOI18N
        }

        splitpaneChannelsControllerTable.setRightComponent(scrollpaneChannelsTable);

        splitpaneViewerChannels.setRightComponent(splitpaneChannelsControllerTable);

        splitpaneMain.setTopComponent(splitpaneViewerChannels);

        pnlFilterRange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.pnlFilterRange.border.title"))); // NOI18N

        ftxtFilterRangeLow.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeLow.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.ftxtFilterRangeLow.toolTipText")); // NOI18N
        ftxtFilterRangeLow.setName("FStart"); // NOI18N

        ftxtFilterRangeHigh.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeHigh.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.ftxtFilterRangeHigh.toolTipText")); // NOI18N
        ftxtFilterRangeHigh.setName("FStop"); // NOI18N

        btngrpFilterRange.add(rbtnFilterRangeLow);
        rbtnFilterRangeLow.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFilterRangeLow, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnFilterRangeLow.text")); // NOI18N
        rbtnFilterRangeLow.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnFilterRangeLow.toolTipText")); // NOI18N

        btngrpFilterRange.add(rbtnFilterRangeHigh);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFilterRangeHigh, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnFilterRangeHigh.text")); // NOI18N
        rbtnFilterRangeHigh.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnFilterRangeHigh.toolTipText")); // NOI18N

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
                .addContainerGap(37, Short.MAX_VALUE))
        );
        pnlFilterRangeLayout.setVerticalGroup(
            pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftxtFilterRangeLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbtnFilterRangeLow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilterRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftxtFilterRangeHigh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbtnFilterRangeHigh))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pnlApplyTo.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.pnlApplyTo.border.title"))); // NOI18N

        btngrpApplyTo.add(rbtnApplyToSelectedChannels);
        rbtnApplyToSelectedChannels.setText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnApplyToSelectedChannels.text")); // NOI18N

        btngrpApplyTo.add(rbtnApplyToAllChannels);
        rbtnApplyToAllChannels.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnApplyToAllChannels, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.rbtnApplyToAllChannels.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnPreview, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnPreview.text")); // NOI18N
        btnPreview.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnPreview.toolTipText")); // NOI18N
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlApplyToLayout = new javax.swing.GroupLayout(pnlApplyTo);
        pnlApplyTo.setLayout(pnlApplyToLayout);
        pnlApplyToLayout.setHorizontalGroup(
            pnlApplyToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlApplyToLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlApplyToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbtnApplyToAllChannels)
                    .addComponent(rbtnApplyToSelectedChannels))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(btnPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        pnlApplyToLayout.setVerticalGroup(
            pnlApplyToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlApplyToLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(rbtnApplyToAllChannels)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbtnApplyToSelectedChannels)
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlApplyToLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPreview)
                .addGap(21, 21, 21))
        );

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnReset.text")); // NOI18N
        btnReset.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnReset.toolTipText")); // NOI18N
        btnReset.setPreferredSize(new java.awt.Dimension(67, 23));
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCommit, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnCommit.text")); // NOI18N
        btnCommit.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnCommit.toolTipText")); // NOI18N
        btnCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnExit, org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnExit.text")); // NOI18N
        btnExit.setToolTipText(org.openide.util.NbBundle.getMessage(SmFAS_Editor.class, "SmFAS_Editor.btnExit.toolTipText")); // NOI18N
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCommit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCommit, btnExit, btnReset});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCommit)
                    .addComponent(btnExit))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlEditorLayout = new javax.swing.GroupLayout(pnlEditor);
        pnlEditor.setLayout(pnlEditorLayout);
        pnlEditorLayout.setHorizontalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditorLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(pnlFilterRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlApplyTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 406, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlEditorLayout.setVerticalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlFilterRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlApplyTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        splitpaneMain.setRightComponent(pnlEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed

        if (!validateFilterInputs())
                return;
        
        int response = JOptionPane.showOptionDialog(this, 
            "A commit will overwrite existing files. Do you wish to proceed?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, 
            null,null,null);
        
        if (response == JOptionPane.NO_OPTION) {
            status = false;
            return;
        }
        
        String logsDir = SmPreferences.General.getLogsDir();
        String trashDir = SmPreferences.General.getTrashDir();

        if (logsDir.isEmpty()) {
            response = JOptionPane.showOptionDialog(this, 
                "Logs directory not set. Log output will not be written out.\n" +
                "Do you wish to proceed?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, 
                null,null,null);

            if (response == JOptionPane.NO_OPTION) {
                status = false;
                return;
            }
        }
    
        if (trashDir.isEmpty()) {
            response = JOptionPane.showOptionDialog(this, 
                "Trash directory not set. Removed files will not be recoverable.\n" +
                "Do you wish to proceed?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, 
                null,null,null);

            if (response == JOptionPane.NO_OPTION) {
                status = false;
                return;
            }
        }
        
        try {
            SmCore.clearStatusViewer();
            
            //DecimalFormat formatter = new DecimalFormat("###,###.####");
            
            ArrayList<String> logList = new ArrayList<>();
            ArrayList<V2ProcessGUI> v2ProcessGUIs = createFilteredV2ProcessGUIList();
 
            V2ProcessGUI v2ProcessGUI1st = v2ProcessGUIs.get(0);
          
            File v1File1st = v2ProcessGUI1st.getV1File();
            String v1FilePath1st = v1File1st.getPath();
            
            String eventsRootDir = SmCore.extractEventsRootDir(v1FilePath1st);
            String event = SmCore.extractEventName(v1FilePath1st);
            String station = SmCore.extractStationName(v1FilePath1st);
            double low = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
            double high = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
            
            SmTimeFormatter timer = new SmTimeFormatter();
            String startTime = timer.getGMTdateTime();
            
            addToLogList(logList,"FAS commit processing started at " + startTime);
            addToLogList(logList,"");
            addToLogList(logList,"Event: " + event);
            addToLogList(logList,"Station: " + station);
            addToLogList(logList,"Low Filter Corner (Hz): " + decimalFormatter.format(low));
            addToLogList(logList,"High Filter Corner (Hz): " + decimalFormatter.format(high));
            addToLogList(logList,"Processing in progress ...");
            addToLogList(logList,"");
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                File fileStation = Paths.get(eventsRootDir,event,station).toFile();
                double eventOnset = v2ProcessGUI.getEventOnset();
                double dTime = v2ProcessGUI.getDTime();
            
                addToLogList(logList,"Source V1 File: " + v2ProcessGUI.getV1File().getPath());
                addToLogList(logList,"Event Onset Time (sec): " + decimalFormatter.format(eventOnset));
                addToLogList(logList,"Delta Time (sec): " + dTime);

                // Create SmProductGUI object.
                SmProductGUI smProductGUI = new SmProductGUI();
                smProductGUI.updateDirectoriesGUI(fileStation);

                // Commit processing.
                if (v2ProcessGUI.commitProcessing() == VFileConstants.V2Status.GOOD) {
                    // Run process steps recorder.
                    v2ProcessGUI.runProcessStepsRecorder();
                    
                    // Get V1 component.
                    V1Component v1Component = v2ProcessGUI.getV1Component();
                    
                    addToLogList(logList,"V1 component fetched.");
                    
                    // Create V2 components.
                    V2Component v2ComponentAcc = new V2Component(CORACC,v1Component);
                    V2Component v2ComponentVel = new V2Component(VELOCITY,v1Component);
                    V2Component v2ComponentDis = new V2Component(DISPLACE,v1Component);

                    // Set the V2 components' station directory.
                    v2ComponentAcc.setStationDir(fileStation);
                    v2ComponentVel.setStationDir(fileStation);
                    v2ComponentDis.setStationDir(fileStation);

                    // Set the V2 components' channel.
                    v2ComponentAcc.setChannel(v1Component.getChannel());
                    v2ComponentVel.setChannel(v1Component.getChannel());
                    v2ComponentDis.setChannel(v1Component.getChannel());
                
                    // Build the V2 components.
                    v2ComponentAcc.buildV2(V2DataType.ACC,v2ProcessGUI,v2ProcessGUI.getProcessSteps());
                    v2ComponentVel.buildV2(V2DataType.VEL,v2ProcessGUI,v2ProcessGUI.getProcessSteps());
                    v2ComponentDis.buildV2(V2DataType.DIS,v2ProcessGUI,v2ProcessGUI.getProcessSteps());

                    // Add V2 components to SmProductGUI object.
                    smProductGUI.addProduct(v2ComponentAcc, "V2");
                    smProductGUI.addProduct(v2ComponentVel, "V2");
                    smProductGUI.addProduct(v2ComponentDis, "V2");

                    addToLogList(logList,"V2 components built and added to product queue.");
                    
                    // Create V3 process.
                    V3Process v3Process = new V3Process(v2ComponentAcc,v2ProcessGUI);
                    v3Process.processV3Data();

                    // Create V3 component.
                    V3Component v3Component = new V3Component(SPECTRA,
                        v2ComponentAcc,v2ComponentVel,v2ComponentDis);
                    v3Component.setStationDir(fileStation);
                    v3Component.buildV3(v3Process);

                    // Add V3 component to SmProductGUI object.
                    smProductGUI.addProduct(v3Component, "V3");

                    addToLogList(logList,"V3 component built and added to product queue.");

                    // Write out products.
                    String[] logItems = smProductGUI.writeOutProducts(trashDir);
                    for (String logItem : logItems) {
                        addToLogList(logList,logItem);
                    }
                    addToLogList(logList,"");
                    
                    // Write out parameters to CSV file.
                    if (!logsDir.isEmpty()) {
                        //StringBuilder fileName = new StringBuilder();
                        //String[] segments = SmGlobal.APK_TABLE_FILE.split("\\.");
                        //String fileTime = startTime.replace("-","_").replace(" ", "_").replace(":","_");
                        //fileName.append(segments[0]).append("_").append(fileTime).
                            //append(".").append(segments[1]);
                        //File csvFile = Paths.get(logsDir,event,station,fileName.toString()).toFile();
                        //smProductGUI.updateUploadParms(v3Component, v1Component, 
                            //v2ComponentAcc, v2ComponentVel, v2ComponentDis, csvFile);
                        
                        File csvFolder = Paths.get(logsDir,event,station).toFile();
                        
                        if (!csvFolder.exists())
                            csvFolder.mkdirs();
                        
                        BuildAPKtable bat = new BuildAPKtable();
                        bat.buildTable(v3Component, v1Component, v2ComponentAcc, v2ComponentVel, 
                            v2ComponentDis, csvFolder, startTime);
                    }
                }
            }
            
            // Clean up files.
            cleanUpFiles(logList);

            // Regenerate plots.
            //reset();

            // Update SmGraphing Tool.
            SmCore.updateSmGraphingTool();
            
            addToLogList(logList,"");
            addToLogList(logList,"FAS commit processing ended at " + timer.getGMTdateTime());
            addToLogList(logList,"");
            addToLogList(logList,"");

            // Write to log file.
            if (!logsDir.isEmpty()) {
                StringBuilder fileName = new StringBuilder();
                String[] segments = SmGlobal.EDIT_LOG_FILE.split("\\.");
                String fileTime = startTime.replace("-","_").replace(" ", "_").replace(":","_");
                fileName.append(segments[0]).append("_").append(fileTime).
                    append(".").append(segments[1]);
                File logFile = Paths.get(logsDir,event,station,fileName.toString()).toFile();
                SmUtils.writeToFile(logList,logFile);
            }

            JOptionPane.showMessageDialog(this, "Processing completed.",
                "Done", JOptionPane.PLAIN_MESSAGE);
            
            status = true;
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnCommitActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        reset();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewActionPerformed
        
        if (!validateFilterInputs())
                return;
        
        try {
            ArrayList<V2ProcessGUI> v2ProcessGUIs = createFilteredV2ProcessGUIList();
            
            // Launch Seismic Chart Viewer to view plots for V2ProcessGUI objects.
            SmSeismicTraceViewer chartViewer = 
                new SmSeismicTraceViewer(SmGlobal.SM_CHARTS_API_QCCHART2D,v2ProcessGUIs,smTemplate);
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

    private void btnFilterClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterClearActionPerformed
        listFilterSeed.clearSelection();
        listFilterLCode.clearSelection();
    }//GEN-LAST:event_btnFilterClearActionPerformed

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
            if (e.getComponent() instanceof SmFAS_Editor) {
                SmFAS_Editor chartEditor = (SmFAS_Editor)e.getComponent();
                SmCore.addMsgToStatusViewer("Filter Selection exited with status: " + chartEditor.getStatus());
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
                        double y = 0;

                        // Draw marker.
                        chartView.getSmDataCursor().drawMarker(x, y);
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
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFilterClear;
    private javax.swing.JButton btnFilterSelect;
    private javax.swing.JButton btnPreview;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.ButtonGroup btngrpApplyTo;
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
    private javax.swing.JPanel pnlApplyTo;
    private javax.swing.JPanel pnlChannelsController;
    private javax.swing.JPanel pnlEditor;
    private javax.swing.JPanel pnlFilterRange;
    private javax.swing.JPanel pnlSelectBy;
    private javax.swing.JPanel pnlViewer;
    private javax.swing.JPanel pnlViewerAcc;
    private javax.swing.JRadioButton rbtnApplyToAllChannels;
    private javax.swing.JRadioButton rbtnApplyToSelectedChannels;
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
