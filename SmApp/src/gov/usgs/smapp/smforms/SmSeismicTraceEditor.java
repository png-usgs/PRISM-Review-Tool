/*******************************************************************************
 * Name: Java class SmSeismicTraceEditor.java
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
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.DISPLACE;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.SPECTRA;
import static SmConstants.VFileConstants.UNCORACC;
import SmConstants.VFileConstants.V2DataType;
import SmConstants.VFileConstants.V2Status;
import static SmConstants.VFileConstants.VELOCITY;
import SmControl.SmProductGUI;
import SmControl.SmQueue;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.ArrayStats;
import SmProcessing.V2ProcessGUI;
import SmProcessing.V2ProcessGUI.BLCStep;
import SmProcessing.V3Process;
import SmUtilities.BuildAPKtable;
import SmUtilities.SmTimeFormatter;
import com.quinncurtis.chart2djava.ChartAttribute;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smchartingapi.SmCharts_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.QCChart2D_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.SingleChartView;
import gov.usgs.smapp.smchartingapi.qcchart2d.SingleChartView.LinePlotType;
import gov.usgs.smapp.smchartingapi.qcchart2d.SmChartView;
import gov.usgs.smcommon.smclasses.SmChannel;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmEditOperation;
import gov.usgs.smcommon.smclasses.SmEditOperation.OperationType;
import gov.usgs.smcommon.smclasses.SmEditOperation.Parm;
import gov.usgs.smcommon.smclasses.SmEpoch;
import gov.usgs.smcommon.smclasses.SmGlobal.CosmosFileType;
import gov.usgs.smcommon.smclasses.SmMarker;
import gov.usgs.smcommon.smclasses.SmMarker.MarkerType;
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
import gov.usgs.smcommon.smutilities.VerticalLabelUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.joda.time.DateTime;
import org.openide.NotifyDescriptor;

/**
 *
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmSeismicTraceEditor extends javax.swing.JFrame {
    private final JFrame owner  = this;
    private final String chartAPI;
    private final SmFile v1SmFile;
    private final SmTemplate smTemplate;
    
    private final DateTime earliestStartTime;
    private final DateTime latestStopTime;
    
    private final JPopupMenu editPopupMenu;
    
    private final TextFieldMouseListener textFieldMouseListener = 
        new TextFieldMouseListener();
    private final TextFieldPropertyListener textFieldPropertyListener = 
        new TextFieldPropertyListener();
    private final RBtnTypeListener rbtnTypeListener = new RBtnTypeListener();
    
    private final DecimalFormat decimalFormatter = new DecimalFormat("#,##0.######");
    
    private String eventName;
    private String networkCode;
    private String stationCode;
    private String channel;
    private String seed;
    private String lCode;
    private double deltaT;
    
    private double initFunctionRangeStart;
    private double initFunctionRangeStop;
    private double initApplicationRangeStart;
    private double initApplicationRangeStop;
    private double initEventOnset;
    private double initFilterRangeLow;
    private double initFilterRangeHigh;
    
    private String accFunction;
    private double accFunctionRangeStart;
    private double accFunctionRangeStop;
    private double accApplicationRangeStart;
    private double accApplicationRangeStop;
    private double accEventOnset;
    private double accFilterRangeLow;
    private double accFilterRangeHigh;
    
    private String velFunction;
    private double velFunctionRangeStart;
    private double velFunctionRangeStop;
    private double velApplicationRangeStart;
    private double velApplicationRangeStop;
    private double velEventOnset;
    private double velFilterRangeLow;
    private double velFilterRangeHigh;
    
    private String disFunction;
    private double disFunctionRangeStart;
    private double disFunctionRangeStop;
    private double disApplicationRangeStart;
    private double disApplicationRangeStop;
    private double disEventOnset;
    private double disFilterRangeLow;
    private double disFilterRangeHigh;
    
    private String logTime;
    
    private double xMax=0;
   
    private ArrayList<V2ProcessGUI> v2ProcessGUIs;
    private final ArrayList<SmChartView> chartViews = new ArrayList<>();
    
    private JPanel curChartViewer;
    private BLF_ButtonState blfAcc;
    private BLF_ButtonState blfVel;

    private Hashtable<String,SmMarker> accChartViewerMarkers;
    private Hashtable<String,SmMarker> velChartViewerMarkers;
    private Hashtable<String,SmMarker> disChartViewerMarkers;
    
    private V2ProcessState v2ProcessState;
    private boolean status = true;
    
    private int correctedCnt=0, filteredCnt=0;
    
    public static enum V2ProcessState {RESET,EDIT,CORRECTED,FILTERED};
    public static enum BLF_ButtonState {Show,Hide};
    
    public static enum FunctionOption {
        NONE (SmGlobal.NO_SELECTION),
        MEAN ("Mean"), 
        LINEAR ("Linear Trend"),
        POLYNOMIAL ("Polynomial Trend");
        
        private final String name;
        
        FunctionOption(String name) {
            this.name = name;
        }
        
        public String Name() {return this.name;}
    }
    
    /**
     * Creates new form SmChartEditor
     * @param chartAPI
     * @param srcSmFile
     * @param smTemplate
     */
    public SmSeismicTraceEditor(String chartAPI, SmFile srcSmFile, SmTemplate smTemplate) {
        
        initComponents();
        
        // Set form icon.
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
            getResource("/resources/icons/prism_review_tool_16.png")));
        
        // Set member variables.
        this.chartAPI = chartAPI;
        
        this.v1SmFile = (srcSmFile.getFileDataType().equals(UNCORACC)) ? new SmFile(srcSmFile) :
            createV1SmFile(srcSmFile);
        this.smTemplate = smTemplate;
        
        // Set earliest and latest start times and minimum delta time 
        // based on the event and station in which the source file belongs.
        //File file = srcSmFile.getFile();
        //String filePath = file.getPath();
        //String event = SmCore.extractEventName(filePath);
        //String station = SmCore.extractStationName(filePath);
        
        //this.earliestStartTime = SmCore.getEarliestStartTime(event, station);
        //this.latestStopTime = SmCore.getLatestStopTime(event, station);
        
        ArrayList<String> filePaths = new ArrayList<>();
        filePaths.add(srcSmFile.getFile().getPath());
        
        // Set earliest and latest start times and minimum delta time.
        this.earliestStartTime = SmUtils.getEarliestStartTime(filePaths);
        this.latestStopTime = SmUtils.getLatestStopTime(filePaths);
        
        // Create CCP popup menu.
        editPopupMenu = SmGUIUtils.createCCPPopupMenu();
        
        // Retrieve and set V1 file member variables.
        retrieveFileAttributes(this.v1SmFile);
        
        // Initialize interface components.
        initInterface();
        initChartViewerPanels();
        initChartViewerLabels();
        initRecorder();
        initEditorPanel();
        
        // Call reset() to create and display initial charts.
        reset();
    }
    
    public boolean getStatus() {return this.status;}
    
    public JPanel getCurrentChartViewer() {return this.curChartViewer;}
    
    /**
     * Sets the current chart viewer to the chart viewer that is clicked on. 
     * @param chartViewer the chart viewer JPanel clicked on.
     */
    public void setCurrentChartViewer(JPanel chartViewer) {
        setChartViewer(chartViewer);
        
        // Restore editor panel inputs associated with specified chart viewer.
        restoreEditorPanelInputs(chartViewer);
    }
    
    private void retrieveFileAttributes(SmFile v1SmFile) {
        try {
            if (v1SmFile == null)
                return;
            
            if (!v1SmFile.getFileDataType().equals(UNCORACC))
                return;
            
            File v1File = v1SmFile.getFile();
            
            if (v1File == null)
                return;
            
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(v1File,logTime,new File(logDir));
            queue.readInFile(v1File);
            queue.parseVFile(v1SmFile.getFileDataType());

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();

            if (smList.size() > 0) {
                COSMOScontentFormat rec = smList.get(0);
                
                final String[] textHeaders = rec.getTextHeader();
                this.eventName = textHeaders[1].substring(0, 40).
                    replaceAll("(?i)record", "").replaceAll("(?i)of","").trim();
                this.networkCode = textHeaders[4].substring(25, 27).trim();
                this.stationCode = textHeaders[4].substring(28, 34).trim();
                this.channel = rec.getChannel();
                this.deltaT = rec.getRealHeaderValue(DELTA_T);
                
//                Pattern p = Pattern.compile("^(\\w{3})(\\.)(\\w{2})$");
                Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
                Matcher m = p.matcher(channel);
                if (m.find()) {
                    this.seed = m.group(1);
                    this.lCode = m.group(3);
                }
            }
        }
        catch (IOException | FormatException | SmException ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void initInterface() {
        this.addWindowListener(new SmChartEditorWindowListener());
    }
    
    private void initChartViewerPanels() {   
        // Create and add mouse listener to the chart viewer panels.
        ChartViewerMouseListener listener = new ChartViewerMouseListener();
        
        this.pnlViewerAcc.addMouseListener(listener);
        this.pnlViewerVel.addMouseListener(listener);
        this.pnlViewerDis.addMouseListener(listener);
        
        // Set current chart viewer.
        //setChartViewer(pnlViewerAcc);
    }
    
    private void initChartViewerLabels() {
        lblViewerAcc.setText("Acc");
        lblViewerVel.setText("Vel");
        lblViewerDis.setText("Dis");
        lblViewerAcc.setUI(new VerticalLabelUI(false));
        lblViewerVel.setUI(new VerticalLabelUI(false));
        lblViewerDis.setUI(new VerticalLabelUI(false));
    }
    
    private void initRecorder() {
        this.tblRecorder.setSelectionMode(SINGLE_SELECTION);
        this.tblRecorder.setRowSelectionAllowed(true);
        this.tblRecorder.setColumnSelectionAllowed(false);
        this.tblRecorder.setShowGrid(true);
        
        //this.tblRecorder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //double factor = this.scrollpaneRecorder.getViewport().getWidth();
        double factor = this.scrollpaneRecorder.getPreferredSize().getWidth();
        this.tblRecorder.getColumnModel().getColumn(0).setPreferredWidth((int)(.30*factor));
        this.tblRecorder.getColumnModel().getColumn(1).setPreferredWidth((int)(.70*factor));
        
        // Add table header mouse motion listener to display header value in a tooltip.
        tblRecorder.getTableHeader().addMouseMotionListener(new TableHeaderMouseMotionListener(tblRecorder));
        
        // Set column cell renderer to display cell value in a tooltip.
        tblRecorder.getColumnModel().getColumn(0).setCellRenderer(new RecorderTableCellRenderer());
        tblRecorder.getColumnModel().getColumn(1).setCellRenderer(new RecorderTableCellRenderer());
        
        this.tblRecorder.addMouseListener(new RecorderMouseListener());
    }
    
    private void initEditorPanel() {  
        this.rbtnFunctionRangeStart.setText("Start");
        this.rbtnFunctionRangeStop.setText("Stop");
        
        // Updating the function list is performed in setChartViewer().
        //updateFunctionSelectionList();
        
        // Set baseline function button text and enable state.
        this.btnShowBLF.setText(BLF_ButtonState.Show.toString());
        this.btnShowBLF.setEnabled(true);
        //this.btnBLF.setEnabled(!curChartViewer.getName().equals(pnlViewerDis.getName()));
        
        // Set baseline function button state for ACC and VEL chart viewers.
        this.blfAcc = this.blfVel = BLF_ButtonState.Show;
        
        // Initialize editior panel related variables to preference settings.
        this.initFunctionRangeStart = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStart();
        this.initFunctionRangeStop = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStop();
        this.initApplicationRangeStart = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStart();
        this.initApplicationRangeStop = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStop();
        this.initEventOnset = SmPreferences.SmSeismicTraceEditor.getEventOnset();
        this.initFilterRangeLow = SmPreferences.SmSeismicTraceEditor.getFilterRangeLow();
        this.initFilterRangeHigh = SmPreferences.SmSeismicTraceEditor.getFilterRangeHigh();
        
        // Format range text fields.
        NumberFormat rangeFormat = NumberFormat.getNumberInstance();
        rangeFormat.setMinimumFractionDigits(1);
        rangeFormat.setMaximumFractionDigits(4);

        this.ftxtFunctionRangeStart.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtFunctionRangeStop.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtApplicationRangeStart.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtApplicationRangeStop.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtEventOnset.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtFilterRangeLow.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        this.ftxtFilterRangeHigh.setFormatterFactory(
            new DefaultFormatterFactory(new NumberFormatter(rangeFormat),
            new NumberFormatter(rangeFormat),new NumberFormatter(rangeFormat)));
        
        // Set editor panel input text fields to corresponding init variables.
        this.ftxtFunctionRangeStart.setValue(this.initFunctionRangeStart);
        this.ftxtFunctionRangeStop.setValue(this.initFunctionRangeStop);
        this.ftxtApplicationRangeStart.setValue(this.initApplicationRangeStart);
        this.ftxtApplicationRangeStop.setValue(this.initApplicationRangeStop);
        this.ftxtEventOnset.setValue(this.initEventOnset);
        this.ftxtFilterRangeLow.setValue(this.initFilterRangeLow);
        this.ftxtFilterRangeHigh.setValue(this.initFilterRangeHigh);
        
        // Add editor panel control listeners.
        setTextFieldListeners(true);
        setRadioButtonListeners(true);
    }
    
    private void updateFunctionSelectionList() {
        // Clear combobox list.
        this.cboxFunction.removeAllItems();
        
        // Populate combobox list.
        if (this.curChartViewer.getName().equals(this.pnlViewerAcc.getName())) {
            this.cboxFunction.addItem(FunctionOption.NONE.Name());
            this.cboxFunction.addItem(FunctionOption.MEAN.Name());
            this.cboxFunction.addItem(FunctionOption.LINEAR.Name());
            this.cboxFunction.addItem(FunctionOption.POLYNOMIAL.Name());
        }
        else {
            this.cboxFunction.addItem(FunctionOption.NONE.Name());
            this.cboxFunction.addItem(FunctionOption.LINEAR.Name());
            this.cboxFunction.addItem(FunctionOption.POLYNOMIAL.Name());
        }  
    }
    
    private void setTextFieldListeners(boolean add) {
        if (add) {
            this.ftxtFunctionRangeStart.addMouseListener(textFieldMouseListener);
            this.ftxtFunctionRangeStop.addMouseListener(textFieldMouseListener);
            this.ftxtApplicationRangeStart.addMouseListener(textFieldMouseListener);
            this.ftxtApplicationRangeStop.addMouseListener(textFieldMouseListener);
            this.ftxtEventOnset.addMouseListener(textFieldMouseListener);

            this.ftxtFunctionRangeStart.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtFunctionRangeStop.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtApplicationRangeStart.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtApplicationRangeStop.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtEventOnset.addPropertyChangeListener("value",textFieldPropertyListener);
        }
        else {
            this.ftxtFunctionRangeStart.removeMouseListener(textFieldMouseListener);
            this.ftxtFunctionRangeStop.removeMouseListener(textFieldMouseListener);
            this.ftxtApplicationRangeStart.removeMouseListener(textFieldMouseListener);
            this.ftxtApplicationRangeStop.removeMouseListener(textFieldMouseListener);
            this.ftxtEventOnset.removeMouseListener(textFieldMouseListener);

            this.ftxtFunctionRangeStart.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtFunctionRangeStop.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtApplicationRangeStart.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtApplicationRangeStop.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtEventOnset.removePropertyChangeListener("value",textFieldPropertyListener);
        }
    }
    
    private void setRadioButtonListeners(boolean add) {
        if (add) {
            this.rbtnFunctionRangeStart.addItemListener(rbtnTypeListener);
            this.rbtnFunctionRangeStop.addItemListener(rbtnTypeListener);
            this.rbtnApplicationRangeStart.addItemListener(rbtnTypeListener);
            this.rbtnApplicationRangeStop.addItemListener(rbtnTypeListener);
            this.rbtnEventOnset.addItemListener(rbtnTypeListener);
        }
        else {
            this.rbtnFunctionRangeStart.removeItemListener(rbtnTypeListener);
            this.rbtnFunctionRangeStop.removeItemListener(rbtnTypeListener);
            this.rbtnApplicationRangeStart.removeItemListener(rbtnTypeListener);
            this.rbtnApplicationRangeStop.removeItemListener(rbtnTypeListener);
            this.rbtnEventOnset.removeItemListener(rbtnTypeListener);
        }
    }
    
    private void reset() {
        try {
            if (v1SmFile == null)
                return;
            
            // Set log time.
            SmTimeFormatter timer = new SmTimeFormatter();
            this.logTime = timer.getGMTdateTime();

            // Adjust initial application range and event onset variables.
            File v1File = v1SmFile.getFile();
            File v2File = (v1File == null) ? null : fetchV2File(v1File);

            if (v1File != null) {
                this.xMax = calcXMax(v1File);
                this.initApplicationRangeStop = this.xMax;
            }
            
            if (v2File != null) {
                this.initEventOnset = fetchEventOnset(v2File);
            }

            this.accChartViewerMarkers = null;
            this.velChartViewerMarkers = null;
            this.disChartViewerMarkers = null;

            // Reset range text fields.
            this.ftxtFunctionRangeStart.setValue(this.initFunctionRangeStart);
            this.ftxtFunctionRangeStop.setValue(this.initFunctionRangeStop);
            this.ftxtApplicationRangeStart.setValue(this.initApplicationRangeStart);
            this.ftxtApplicationRangeStop.setValue(this.initApplicationRangeStop);
            this.ftxtEventOnset.setValue(this.initEventOnset);
            this.ftxtFilterRangeLow.setValue(this.initFilterRangeLow);
            this.ftxtFilterRangeHigh.setValue(this.initFilterRangeHigh);

            // Reset function selection.
            if (this.cboxFunction.getItemCount() > 0)
                this.cboxFunction.setSelectedIndex(0);

            // Reset BLF button.
            resetBLF_Button();
            
            // Reset initial radio button selection.
            this.rbtnFunctionRangeStart.setSelected(true);
            
            // Reset recorder table.
            DefaultTableModel model = (DefaultTableModel)tblRecorder.getModel();
            model.setRowCount(0);
            
            // Reset the width of second column in the recorder table.
            //double factor = this.scrollpaneRecorder.getViewport().getWidth();
            //double factor = this.scrollpaneRecorder.getPreferredSize().getWidth();
            //this.tblRecorder.getColumnModel().getColumn(0).setPreferredWidth((int)(.30*factor));
            //this.tblRecorder.getColumnModel().getColumn(1).setPreferredWidth((int)(.70*factor));

            // Reset V2ProcessGUI array, which should contain only one V2ProcessGUI 
            // object since there's only one record in V1 file. Future versions of
            // V* files may contain more than one record, hence this is why an array
            // is used.
            this.v2ProcessGUIs = createV2ProcessGUIs(v1SmFile);
            
            // Reset filter range values.
            for (V2ProcessGUI v2ProcessGUI : this.v2ProcessGUIs) {
                this.ftxtFilterRangeLow.setValue(v2ProcessGUI.getLowFilterCorner());
                this.ftxtFilterRangeHigh.setValue(v2ProcessGUI.getHighFilterCorner());
                
                // Display result of re-sampling boolean indicator.
                String msg = v2ProcessGUI.getResampleIndicator() ? 
                    "V2 Re-sample Rate: " + v2ProcessGUI.getSampleRate() :
                    "V2 Sample Rate: " + v2ProcessGUI.getSampleRate();
                SmCore.addMsgToStatusViewer(msg);
            }
        
            // Create seismic charts.
            createSeismicCharts(this.v2ProcessGUIs);
            
            // Set current chart viewer.
            setChartViewer(pnlViewerAcc);
            
            // Reset editor panel inputs.
            resetEditorPanelInputs();
            
            // Reset counters.
            this.correctedCnt = this.filteredCnt = 0;
            
            // Reset V2Process state.
            v2ProcessState = V2ProcessState.RESET;
            
            SmCore.addMsgToStatusViewer("Seismic Trace Editor reset at: " + this.logTime);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void resetBLF_Button() {
        // Reset BLF button.
        this.btnShowBLF.setText(BLF_ButtonState.Show.toString());
        
        if (curChartViewer != null) {
            this.btnShowBLF.setEnabled(!curChartViewer.getName().equals(pnlViewerDis.getName()));
        }
        
        // Reset BLF button state for ACC and VEL chart viewers.
        this.blfAcc = this.blfVel = BLF_ButtonState.Show;
    }
    
    /**
     * Creates a V1 SmFile object based on another SmFile object. 
     * @param smFile
     * @return SmFile object.
     */
    private SmFile createV1SmFile(SmFile smFile) {
        if (smFile.getFileDataType().equals(UNCORACC))
            return new SmFile(smFile);
        else { //V2 SmFile object
            // Check validity of the file's parent path.
            String parent = smFile.getFile().getParent();
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
            Path v1Dir = Paths.get(path.getParent().getParent().toString(),
                SmGlobal.CosmosFileType.V1.toString());
            
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
                    CosmosFileType.V1.toString() + fileExt.substring(2) : 
                    CosmosFileType.V1.toString();
                
                m.appendReplacement(result, fileName + "." + v1FileExt);
            }
            String v1FileName = result.toString();
            
            // Create V1 File object.
            Path v1FilePath = Paths.get(v1Dir.toString(),v1FileName);
            
            if (Files.exists(v1FilePath) && Files.isRegularFile(v1FilePath)) {
                // Create and return V1 SmFile object.
                return new SmFile(v1FilePath.toFile());
            }
            
            return null;
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
    
    private double calcXMax(File v1File) {
        double xMaxVal = 0;
        
        try {
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(v1File,logTime,new File(logDir));
            queue.readInFile(v1File);
            queue.parseVFile(UNCORACC);

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();

            for (COSMOScontentFormat rec : smList) {
                if (rec instanceof V1Component) {
                    V1Component v1Component = (V1Component)rec;
                    double[] points = v1Component.getDataArray();
                    double xVal = (this.deltaT*MSEC_TO_SEC)*(points.length-1);
                    
                    if (xVal > xMaxVal)
                        xMaxVal = xVal;
                }
            }
        }
        catch (IOException | FormatException | SmException ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return xMaxVal;
    }
    
    private double fetchEventOnset(File v2File) {
        double eventOnset = 0;
        
        try {
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(v2File,logTime,new File(logDir));
            
            String fileName = v2File.getName();
            String regExCosmosFileType = String.format("(?i)(^.+)([\\.](%s|%s|%s))([\\.](V[\\d][cC]?)$)",
                SmGlobal.CosmosV2DataType.ACC.toString(),
                SmGlobal.CosmosV2DataType.VEL.toString(),
                SmGlobal.CosmosV2DataType.DIS.toString());
            Pattern p = Pattern.compile(regExCosmosFileType);
            
            Matcher m = p.matcher(fileName);
            
            String fileDataType = "";
            
            if (m.find()) {
                if (m.group(3).equalsIgnoreCase(SmGlobal.CosmosV2DataType.ACC.toString()))
                    fileDataType = CORACC;
                else if (m.group(3).equalsIgnoreCase(SmGlobal.CosmosV2DataType.VEL.toString()))
                    fileDataType = VELOCITY;
                else if (m.group(3).equalsIgnoreCase(SmGlobal.CosmosV2DataType.DIS.toString()))
                    fileDataType = DISPLACE;
            }
            
            queue.readInFile(v2File);
            queue.parseVFile(fileDataType);

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();
            
            if (smList.size() > 0) {
                V2Component v2Component = (V2Component)smList.get(0);
                eventOnset = v2Component.extractEONSETfromComments();
            }
        }
        catch (IOException | FormatException | SmException ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return eventOnset;
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
    
    private ArrayList<V2ProcessGUI> createV2ProcessGUIs(SmFile v1SmFile) {
        
        if (v1SmFile == null)
            return null;
        
        ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
        
        if (v1Components == null)
            return null;
        
        File v1File = v1SmFile.getFile();
        int filterOrder = SmPreferences.PrismParams.getButterworthFilterOrder();
        double threshold = SmPreferences.PrismParams.getStrongMotionThresholdPcnt();
        double taperLength = SmPreferences.PrismParams.getButterworthFilterTaperLength();
        double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
        int diffOrder = SmPreferences.PrismParams.getDifferentialOrder();
                
        ArrayList<V2ProcessGUI> v2PGUIs = new ArrayList<>();
        
        for (V1Component v1Component : v1Components) {
            try {
                v2PGUIs.add(new V2ProcessGUI(v1Component,
                    v1File,filterOrder,threshold,taperLength,eventOnset,diffOrder));
            }
            catch (Exception ex ) {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
        }
        
        return v2PGUIs;
    }
    
    private void createSeismicCharts(ArrayList<V2ProcessGUI> v2PGUIs) {
        // Clear chart viewer panels.
        this.pnlViewerAcc.removeAll();
        this.pnlViewerVel.removeAll();
        this.pnlViewerDis.removeAll();
        
        // Clear chartViews array.
        this.chartViews.clear();
        
        try { 
            // Iterate array, creating chart for each V2ProceesGUI object.
            for (V2ProcessGUI v2PGUI : v2PGUIs) {
                createSeismicChart(v2PGUI,VFileConstants.V2DataType.ACC);
                createSeismicChart(v2PGUI,VFileConstants.V2DataType.VEL);
                createSeismicChart(v2PGUI,VFileConstants.V2DataType.DIS);
            }
            
            // Set chart view properties.
            setChartViewProperties(this.chartViews);
        }
        catch(Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void createSeismicChart(V2ProcessGUI v2ProcessGUI, VFileConstants.V2DataType v2DataType) {
        try {
            ArrayList<SmPoint> smPoints = new ArrayList<>();
            double[] points = v2ProcessGUI.getV2Array(v2DataType);
            
            double xVal = 0;
            for (int i=0; i<points.length; i++){
                smPoints.add(new SmPoint(xVal,points[i]));
                xVal += this.deltaT*MSEC_TO_SEC;
            }
                    
            SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                new QCChart2D_API() : null;

            if (smCharts_API == null)
                return;
            
            // Initialize SmStation and SmChannel objects.
            SmStation smStation = (smTemplate != null) ? 
                smTemplate.getSmStation() : null;
            SmEpoch smEpoch = (smStation != null) ?
                smStation.getSmEpoch(earliestStartTime, latestStopTime) : null;
            SmChannel smChannel = (smEpoch != null) ?
                smEpoch.getSmChannel(channel) : null;
            
            // Set series title and description.
            String title = channel;
            String description = eventName + "_" + networkCode + "." + 
                stationCode + "." + seed + "." + lCode;

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
            
            int dataParmCode;
            
            if (v2DataType == VFileConstants.V2DataType.ACC)
                dataParmCode = 1;
            else if (v2DataType == VFileConstants.V2DataType.VEL)
                dataParmCode = 2;
            else
                dataParmCode = 3;

            // Create SmSeries object.
            SmSeries smSeries = new SmSeries(smPoints,dataParmCode,
                title,description,color,null);
            String plotType = SmGlobal.PLOT_TYPE_SEISMIC;
            SmCharts_API.XYBounds xyBounds = null;

            // Set chart labels.
            String chartTitle = smSeries.getDecription();
            String xAxisTitle = "Time (sec)";
            String yAxisTitle = "";

            switch (dataParmCode)
            {
                case 1:
                    yAxisTitle = "Accelaration (cm/sec" + "\u00B2" + ")";
                    break;
                case 2:
                    yAxisTitle = "Velocity (cm/sec)";
                    break;
                case 3:
                    yAxisTitle = "Displacement (cm)";
                    break;
            }

            // Create chart.
            final SingleChartView chartView = 
                (SingleChartView)smCharts_API.createChart(smSeries,plotType,
                xyBounds,chartTitle,xAxisTitle,yAxisTitle,true,owner);

            if (chartView != null) {
                 chartViews.add(chartView);

                 switch (dataParmCode)
                 {
                     case 1:
                         pnlViewerAcc.add(chartView);
                         break;
                     case 2:
                         pnlViewerVel.add(chartView);
                         break;
                     case 3:
                         pnlViewerDis.add(chartView);
                         break;
                 }
                 pnlViewer.updateUI();
             }
        }
        catch (Exception ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void plotBaselineFunction(V2ProcessGUI v2ProcessGUI,
        V2DataType v2DataType, double fStart, double fStop, String fName) {
        
        int dataParmCode;
        
        if (v2DataType.equals(V2DataType.ACC))
            dataParmCode = 1;
        else if (v2DataType.equals(V2DataType.VEL))
            dataParmCode = 2;
        else
            return;
        
        double[] inArr = v2ProcessGUI.getV2Array(v2DataType);
        double dTime = this.deltaT*MSEC_TO_SEC;
        VFileConstants.CorrectionOrder inCType;
        
        if (fName.equals(FunctionOption.MEAN.Name()))
            inCType = VFileConstants.CorrectionOrder.MEAN;
        else if (fName.equals(FunctionOption.LINEAR.Name()))
            inCType = VFileConstants.CorrectionOrder.ORDER1;
        else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
            inCType = VFileConstants.CorrectionOrder.ORDER2;
        else
            return;
        
        try {
            double[] points = v2ProcessGUI.getBaselineFunction(inArr, dTime, fStart, fStop, inCType);
            
            ArrayStats stats = new ArrayStats(inArr);
            double peakVal = stats.getPeakVal();
           
            ArrayList<SmPoint> smPoints = new ArrayList<>();
            
            double xVal = 0;
            for (int i=0; i<points.length; i++){
                if (Math.abs(points[i]) <= Math.abs(peakVal)) {
                    smPoints.add(new SmPoint(xVal,points[i]));
                }
                
                xVal += dTime;
            }
            
            // Create baseline function attributes.
            Color color = new Color(SmPreferences.SmSeismicTraceEditor.getBaselineFunctionColor());
            double width = SmPreferences.SmSeismicTraceEditor.getBaselineFunctionWidth();
            int style = SmPreferences.SmSeismicTraceEditor.getBaselineFunctionStyle();
            
            SmSeries smSeries = new SmSeries(smPoints,dataParmCode,"","",color,null);
            
            // Create line plot.
            for (Component component : curChartViewer.getComponents()) {
                if (component instanceof SingleChartView) {
                    SingleChartView singleChartView = (SingleChartView)component;
                    
                    // Remove previously created baseline function.
                    singleChartView.removeLinePlot(LinePlotType.BASELINE);
                    
                    ChartAttribute lineAttr = new ChartAttribute (color,width,style);
                    
                    // Add new baseline function.
                    singleChartView.addLinePlot(smSeries,LinePlotType.BASELINE,lineAttr);
                }
            }
            
            curChartViewer.updateUI();
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private double[] getBaselineFunction(V2ProcessGUI v2ProcessGUI, V2DataType v2DataType,
        double fStart, double fStop, String fName) {
        
        double[] inArr;
        double[] baseline = null;
        
        inArr = v2ProcessGUI.getV2Array(v2DataType);
        
        VFileConstants.CorrectionOrder cType;
        
        if (fName.equals(FunctionOption.MEAN.Name()))
            cType = VFileConstants.CorrectionOrder.MEAN;
        else if (fName.equals(FunctionOption.LINEAR.Name()))
            cType = VFileConstants.CorrectionOrder.ORDER1;
        else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
            cType = VFileConstants.CorrectionOrder.ORDER2;
        else 
            return null;
        
        double dTime = this.deltaT*MSEC_TO_SEC;
        
        try {
            baseline = v2ProcessGUI.getBaselineFunction(inArr, dTime, fStart, fStop, cType);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return baseline;
    }
    
    private ArrayList<V2ProcessGUI> runBLC(ArrayList<V2ProcessGUI> v2ProcessGUIs_In,
        V2DataType v2DataType, VFileConstants.CorrectionOrder cOrder,
        double fStart, double fStop, String fName, double aStart, double aStop, 
        double eventOnset, boolean isPreview) {
        
        VFileConstants.CorrectionOrder cTypeOrig = cOrder;
        VFileConstants.CorrectionOrder cTypeFinal;
        V2DataType v2DataTypeOrig = v2DataType;
        V2DataType v2DataTypeFinal;
        
        ArrayList<V2ProcessGUI> v2ProcessGUIs_Out = new ArrayList();
        
        try {
            for (V2ProcessGUI v2ProcessGUI_In : v2ProcessGUIs_In) {
                double dTime = this.deltaT*MSEC_TO_SEC;
                v2ProcessGUI_In.setEventOnset(eventOnset, dTime);
                
                double[] baseline = getBaselineFunction(v2ProcessGUI_In,v2DataType,fStart,fStop,fName);
                
                V2ProcessGUI v2ProcessGUI_Out = V2ProcessGUI.makeBaselineCorrection(v2DataType, baseline, 
                    v2ProcessGUI_In, dTime, aStart, aStop, isPreview);
                
                if (!isPreview) { //Remove
                    // Trend has been removed from acceleration. So, integrate and
                    // integrate to get velocity and displacement, respectively.
                    double[] acc = v2ProcessGUI_Out.getAcceleration();
                    double[] vel = v2ProcessGUI_Out.integrateAnArray(acc, dTime, eventOnset);
                    double[] dis = v2ProcessGUI_Out.integrateAnArray(vel, dTime, eventOnset);
                    v2ProcessGUI_Out.setVelocity(vel);
                    v2ProcessGUI_Out.setDisplacement(dis);
                    
                    // Set final V2DataType.
                    v2DataTypeFinal = V2DataType.ACC;

                    // Set final correction type.
                    if (v2DataType.equals(V2DataType.VEL)) {
                        if (cTypeOrig == VFileConstants.CorrectionOrder.ORDER2)
                            cTypeFinal = VFileConstants.CorrectionOrder.ORDER1;
                        else if (cTypeOrig == VFileConstants.CorrectionOrder.ORDER1)
                            cTypeFinal = VFileConstants.CorrectionOrder.MEAN;
                        else
                            cTypeFinal = null;
                    }
                    else {
                        cTypeFinal = cTypeOrig;
                    }
                    
                    v2ProcessGUI_Out.addBaselineProcessingStep(fStart, fStop, aStart, aStop, 
                        v2DataTypeOrig, v2DataTypeFinal, VFileConstants.BaselineType.BESTFIT, 
                        cTypeOrig, cTypeFinal, 0);
                }
                
                v2ProcessGUIs_Out.add(v2ProcessGUI_Out);
            }
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return v2ProcessGUIs_Out;
    }
    
    private void filter(double filterRangeLow, double filterRangeHigh, double eventOnset) {
        
        try {
            // Iterate V2ProcessGUI array to set event onset.
            double dTime = this.deltaT*MSEC_TO_SEC;
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                v2ProcessGUI.setEventOnset(eventOnset,dTime);
            }
            
            // Filter the V2ProcessGUI array.
            v2ProcessGUIs = V2ProcessGUI.filterV2ProcessGUIList(v2ProcessGUIs,
                filterRangeLow,filterRangeHigh);
            
            // Set V2Process state.
            v2ProcessState = V2ProcessState.FILTERED;
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private RecorderStep createRecorderStep(OperationType operationType) {
        
        RecorderStep recorderStep = null;
        
        try {        
            // Create SmEditOperation object.
            SmEditOperation smEditOperation = createSmEditOperation(operationType);
            
            // Create RecorderStep object.
            recorderStep = new RecorderStep(smEditOperation);
            
            // Store editor panel inputs associated with specified chart viewer.
            storeEditorPanelInputs(curChartViewer);
        }
        catch (Exception ex) {
        }
        
        return recorderStep;
    }
    
    private SmEditOperation createSmEditOperation(OperationType operationType) {
        SmEditOperation op = new SmEditOperation(operationType);
        
        if (operationType.equals(OperationType.DrawMarker)) {
            
            MarkerType markerType = null;
            
            if (this.rbtnFunctionRangeStart.isSelected()) 
                markerType = MarkerType.FSTART;
            else if (this.rbtnFunctionRangeStop.isSelected())
                markerType = MarkerType.FSTOP;
            else if (this.rbtnApplicationRangeStart.isSelected())
                markerType = MarkerType.ASTART;
            else if (this.rbtnApplicationRangeStop.isSelected())
                markerType = MarkerType.ASTOP;
            else if (this.rbtnEventOnset.isSelected())
                markerType = MarkerType.EVENT_ONSET;
            
            if (markerType != null) {
                String keyMarkerType = "MarkerType";
                MarkerType valueMarkerType = markerType;
                
                op.addParm(new SmEditOperation.Parm<>(keyMarkerType,valueMarkerType));
            }
        }
        
        String keyViewer = "ChartViewer";
        String valueViewer = curChartViewer.getName();
        op.addParm(new SmEditOperation.Parm<>(keyViewer,valueViewer));
        
        String keyRangeField = "RangeField";
        String valueRangeField = "";
        if (this.rbtnFunctionRangeStart.isSelected()) 
            valueRangeField = this.rbtnFunctionRangeStart.getName();
        else if (this.rbtnFunctionRangeStop.isSelected())
            valueRangeField = this.rbtnFunctionRangeStop.getName();
        else if (this.rbtnApplicationRangeStart.isSelected())
            valueRangeField = this.rbtnApplicationRangeStart.getName();
        else if (this.rbtnApplicationRangeStop.isSelected())
            valueRangeField = this.rbtnApplicationRangeStop.getName();
        else if (this.rbtnEventOnset.isSelected())
            valueRangeField = this.rbtnEventOnset.getName();
        op.addParm(new SmEditOperation.Parm<>(keyRangeField,valueRangeField));
    
        String keyFStart = ftxtFunctionRangeStart.getName();
        Double valueFStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyFStart,valueFStart));

        String keyFStop = ftxtFunctionRangeStop.getName();
        Double valueFStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyFStop,valueFStop));

        String keyFunction = cboxFunction.getName();
        String valueFunction = this.cboxFunction.getSelectedItem().toString();
        op.addParm(new SmEditOperation.Parm<>(keyFunction,valueFunction));
        
        String keyFunctionShowHide = btnShowBLF.getName();
        String valueFunctionShowHide = btnShowBLF.getText();
        op.addParm(new SmEditOperation.Parm<>(keyFunctionShowHide,valueFunctionShowHide));

        String keyAStart = ftxtApplicationRangeStart.getName();
        Double valueAStart = ((Number)ftxtApplicationRangeStart.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyAStart,valueAStart));

        String keyAStop = ftxtApplicationRangeStop.getName();
        Double valueAStop = ((Number)ftxtApplicationRangeStop.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyAStop,valueAStop));

        String keyEventOnset = ftxtEventOnset.getName();
        Double valueEventOnset = ((Number)ftxtEventOnset.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyEventOnset,valueEventOnset));
        
        String keyFilterRangeLow = ftxtFilterRangeLow.getName();
        Double valueFilterRangeLow = ((Number)ftxtFilterRangeLow.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyFilterRangeLow,valueFilterRangeLow));
        
        String keyFilterRangeHigh = ftxtFilterRangeHigh.getName();
        Double valueFilterRangeHigh = ((Number)ftxtFilterRangeHigh.getValue()).doubleValue();
        op.addParm(new SmEditOperation.Parm<>(keyFilterRangeHigh,valueFilterRangeHigh));
        
        if (operationType.equals(OperationType.RemoveBLC)) {
            String keyCorrectedCnt = "correctedCnt";
            Integer valueCorrectedCnt = this.correctedCnt;
            op.addParm(new SmEditOperation.Parm<>(keyCorrectedCnt,valueCorrectedCnt));
        }
        
        if (operationType.equals(OperationType.Filter)) {
            String keyFilteredCnt = "filteredCnt";
            Integer valueFilteredCnt = this.filteredCnt;
            op.addParm(new SmEditOperation.Parm<>(keyFilteredCnt,valueFilteredCnt));
        }
        
        return op;
    }
    
    private void addRecorderStep(RecorderStep step) {
        DefaultTableModel model = (DefaultTableModel)tblRecorder.getModel();
        
        // Remove any rows that exist after currently selected row.
        int selectedRowIndx = tblRecorder.getSelectedRow();
    
        if (selectedRowIndx != -1) {
            int endRowIndx = tblRecorder.getRowCount()-1;
            if (selectedRowIndx < endRowIndx) {
                for (int i=endRowIndx; i>selectedRowIndx; i--) {
                    model.removeRow(i);
                }
            }
        }
        
        // Add row to the end of table.
        model.addRow(new Object[]{model.getRowCount()+1, step});
        
        /*
        // Adjust size of table columns.
        for (int col = 0; col < tblRecorder.getColumnCount(); col++) {
            int width = 0;
            
            DefaultTableColumnModel colModel = (DefaultTableColumnModel)tblRecorder.getColumnModel();
            TableColumn tblCol = colModel.getColumn(col);
            
            // Get width of column header
            TableCellRenderer renderer = tblCol.getHeaderRenderer();
            if (renderer == null) {
                renderer = tblRecorder.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(
                tblRecorder, tblCol.getHeaderValue(), false, false, 0, 0);
            
            width = comp.getPreferredSize().width;
            
            // Iterate table rows to adjust width.
            for (int row = 0; row < tblRecorder.getRowCount(); row++) {
                renderer = tblRecorder.getCellRenderer(row, col);
                comp = tblRecorder.prepareRenderer(renderer, row, col);
                width = Math.max (comp.getPreferredSize().width + 
                    tblRecorder.getColumnModel().getColumnMargin(), width);
            }
            tblRecorder.getColumnModel().getColumn(col).setPreferredWidth(width);
        }
        */
        
        // Clear row selection.
        tblRecorder.clearSelection();
    }
    
    private void runRecorderSteps() {
        // Turn off editor panel control listeners.
        setTextFieldListeners(false);
        setRadioButtonListeners(false);
  
        // Reset range text fields.
        this.ftxtFunctionRangeStart.setValue(this.initFunctionRangeStart);
        this.ftxtFunctionRangeStop.setValue(this.initFunctionRangeStop);
        this.ftxtApplicationRangeStart.setValue(this.initApplicationRangeStart);
        this.ftxtApplicationRangeStop.setValue(this.initApplicationRangeStop);
        this.ftxtEventOnset.setValue(this.initEventOnset);
        this.ftxtFilterRangeLow.setValue(this.initFilterRangeLow);
        this.ftxtFilterRangeHigh.setValue(this.initFilterRangeHigh);

        // Reset function selection.
        if (this.cboxFunction.getItemCount() > 0)
            this.cboxFunction.setSelectedIndex(0);
        
        // Reset BLF button.
        resetBLF_Button();

        // Reset V2ProcessGUI array.
        this.v2ProcessGUIs = createV2ProcessGUIs(v1SmFile);

        // Reset filter range values.
        for (V2ProcessGUI v2ProcessGUI : this.v2ProcessGUIs) {
            this.ftxtFilterRangeLow.setValue(v2ProcessGUI.getLowFilterCorner());
            this.ftxtFilterRangeHigh.setValue(v2ProcessGUI.getHighFilterCorner());
        }

        // Create seismic charts.
        createSeismicCharts(this.v2ProcessGUIs);

        // Set current chart viewer.
        setChartViewer(pnlViewerAcc);

        // Reset editor panel inputs.
        resetEditorPanelInputs();
        
        // Reset counters.
        this.correctedCnt = this.filteredCnt = 0;
        
        // Reset SmMarker lists.
        accChartViewerMarkers = new Hashtable<>();
        velChartViewerMarkers = new Hashtable<>();
        disChartViewerMarkers = new Hashtable<>();
        
        // Run recorder steps from first to selected row.
        int selectedRowIndx = tblRecorder.getSelectedRow();
        
        for (int i=0; i <= selectedRowIndx; i++) {
            RecorderStep recorderStep = (RecorderStep)tblRecorder.getValueAt(i,1);
            runRecorderStep(recorderStep);
        }
        
        // Set properties for each SmChartView object to reflect
        // field states from the last recorder step.
        setChartViewProperties(chartViews);
        
        // Turn back on editor panel control listeners.
        setTextFieldListeners(true);
        setRadioButtonListeners(true);
    }
    
    private void runRecorderStep(RecorderStep recorderStep) {
        SmEditOperation smEditOperation = recorderStep.getSmEditOperation();
        OperationType operationType = smEditOperation.getOperationType();
        ArrayList<Parm<?,?>> parms = smEditOperation.getParms();
        MarkerType markerType = null;
        
        // Iterate Parm list to set editor fields.
        for (Parm parm : parms) {
            String key = parm.getKey().toString();

            if (key.equals("MarkerType")) {
                markerType = (MarkerType)parm.getValue();
            }
            else if (key.equals("ChartViewer")) {
                String chartViewer = parm.getValue().toString();
                
                if (chartViewer.equals(pnlViewerAcc.getName())) 
                    setChartViewer(pnlViewerAcc);
                else if (chartViewer.equals(pnlViewerVel.getName()))
                    setChartViewer(pnlViewerVel);
                else if (chartViewer.equals(pnlViewerDis.getName()))
                    setChartViewer(pnlViewerDis);  
            }
            else if (key.equals("RangeField")) {
                String rangeField = parm.getValue().toString();
                
                if (rangeField.equals(this.rbtnFunctionRangeStart.getName()))
                    this.rbtnFunctionRangeStart.setSelected(true);
                else if (rangeField.equals(this.rbtnFunctionRangeStop.getName()))
                    this.rbtnFunctionRangeStop.setSelected(true);
                else if (rangeField.equals(this.rbtnApplicationRangeStart.getName()))
                    this.rbtnApplicationRangeStart.setSelected(true);
                else if (rangeField.equals(this.rbtnApplicationRangeStop.getName()))
                    this.rbtnApplicationRangeStop.setSelected(true);
                else if (rangeField.equals(this.rbtnEventOnset.getName()))
                    this.rbtnEventOnset.setSelected(true);
            }
            else if (key.equals(ftxtFunctionRangeStart.getName())) {
                double fStart = ((Number)parm.getValue()).doubleValue();
                ftxtFunctionRangeStart.setValue(fStart);
            }
            else if (key.equals(ftxtFunctionRangeStop.getName())) {
                double fStop = ((Number)parm.getValue()).doubleValue();
                ftxtFunctionRangeStop.setValue(fStop);
            }
            else if (key.equals(cboxFunction.getName())) {
                String function = parm.getValue().toString();
                for (int i=0; i<cboxFunction.getItemCount();i++) {
                    if (cboxFunction.getItemAt(i).toString().equals(function)) {
                        cboxFunction.setSelectedIndex(i);
                        break;
                    }
                }
            }
            else if (key.equals(btnShowBLF.getName())) {
                String functionShowHide = parm.getValue().toString();
                btnShowBLF.setText(functionShowHide);
            }
            else if (key.equals(ftxtApplicationRangeStart.getName())) {
                double aStart = ((Number)parm.getValue()).doubleValue();
                ftxtApplicationRangeStart.setValue(aStart);
            }
            else if (key.equals(ftxtApplicationRangeStop.getName())) {
                double aStop = ((Number)parm.getValue()).doubleValue();
                ftxtApplicationRangeStop.setValue(aStop);
            }
            else if (key.equals(ftxtEventOnset.getName())) {
                double eventOnset = ((Number)parm.getValue()).doubleValue();
                ftxtEventOnset.setValue(eventOnset);
            }
            else if (key.equals(ftxtFilterRangeLow.getName())) {
                double filterRangeLow = ((Number)parm.getValue()).doubleValue();
                ftxtFilterRangeLow.setValue(filterRangeLow);
            }
            else if (key.equals(ftxtFilterRangeHigh.getName())) {
                double filterRangeHigh = ((Number)parm.getValue()).doubleValue();
                ftxtFilterRangeHigh.setValue(filterRangeHigh);
            }
        }
        
        // Set BLF button state.
        btnShowBLF.setEnabled(!curChartViewer.getName().equals(pnlViewerDis.getName()));
        // Set basline function button state for current chart viewer.
        if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
            blfAcc = btnShowBLF.getText().equals(BLF_ButtonState.Show.toString()) ?
                BLF_ButtonState.Show : BLF_ButtonState.Hide;
        else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
            blfVel = btnShowBLF.getText().equals(BLF_ButtonState.Show.toString()) ?
                BLF_ButtonState.Show : BLF_ButtonState.Hide;
        
        // Perform operation.
        if (operationType.equals(OperationType.DrawMarker)) {
            
            SmMarker smMarker = null;
            double x;
            double y = 0;
            
            // Create marker for this operation step.
            if (markerType.equals(MarkerType.FSTART)) {
                x = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
                smMarker = new SmMarker(MarkerType.FSTART,x,y);
            }
            else if (markerType.equals(MarkerType.FSTOP)) {
                x = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
                smMarker = new SmMarker(MarkerType.FSTOP,x,y);
            }
            else if (markerType.equals(MarkerType.ASTART)) {
                x = ((Number)ftxtApplicationRangeStart.getValue()).doubleValue();
                smMarker = new SmMarker(MarkerType.ASTART,x,y);
            }
            else if (markerType.equals(MarkerType.ASTOP)) {
                x = ((Number)ftxtApplicationRangeStop.getValue()).doubleValue();
                smMarker = new SmMarker(MarkerType.ASTOP,x,y);
            }
            else if (markerType.equals(MarkerType.EVENT_ONSET)) {
                x = ((Number)ftxtEventOnset.getValue()).doubleValue();
                smMarker = new SmMarker(MarkerType.EVENT_ONSET,x,y);
            }
            else
                return;
            
            // Set the marker list based on selected chart viewer.
            Hashtable<String,SmMarker> tbl = null;
            
            // Add the marker to appropriate marker list.
            if (curChartViewer.getName().equals(this.pnlViewerAcc.getName())) 
                tbl = this.accChartViewerMarkers;
            else if (curChartViewer.getName().equals(this.pnlViewerVel.getName()))
                tbl = this.velChartViewerMarkers;
            else
                tbl = this.disChartViewerMarkers;
            
            // Put marker in list.
            tbl.put(smMarker.getMarkerType().toString(), smMarker);
            
            for (Component component : curChartViewer.getComponents()) {
                if (component instanceof SingleChartView) {
                    SingleChartView chartView = (SingleChartView)component;

                    // Draw all markers in corresponding marker list.
                    Enumeration markers = tbl.elements();

                    while (markers.hasMoreElements()) {
                        smMarker = (SmMarker)markers.nextElement();

                        // Draw marker.
                        chartView.setSmDataCursorStyle(smMarker.getMarkerStyle());
                        chartView.setSmDataCursorColor(smMarker.getMarkerColor());
                        chartView.setSmDataCursorWidth(smMarker.getMarkerWidth());        
                        chartView.getSmDataCursor().drawMarker(smMarker.getX(),0);
                    }
                }
            }
        }
        else if (operationType.equals(OperationType.HideBaselineFunction)) {
            
            for (Component component : curChartViewer.getComponents()) {
                if (component instanceof SingleChartView) {
                    SingleChartView chartView = (SingleChartView)component;

                    // Remove previously created baseline function.
                    chartView.removeLinePlot(LinePlotType.BASELINE);
                    chartView.updateDraw();
                }
            }
            
            // Toggle button.
            btnShowBLF.setText(BLF_ButtonState.Show.toString());
            btnShowBLF.setEnabled(!curChartViewer.getName().equals(pnlViewerDis.getName()));
            
            // Set basline function button state for current chart viewer.
            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                this.blfAcc = BLF_ButtonState.Show;
            else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
                this.blfVel = BLF_ButtonState.Show;
        }
        else if (operationType.equals(OperationType.ShowBaselineFunction)) {
            
            V2DataType v2DataType;
            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                v2DataType = V2DataType.ACC;
            else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
                v2DataType = V2DataType.VEL;
            else 
                v2DataType = V2DataType.DIS;

            double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            String fName = this.cboxFunction.getSelectedItem().toString();

            // Plot baseline function.
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                plotBaselineFunction(v2ProcessGUI,v2DataType,fStart,fStop,fName);
            }
            
            // Toggle button.
            btnShowBLF.setText(BLF_ButtonState.Hide.toString());
            btnShowBLF.setEnabled(!curChartViewer.getName().equals(pnlViewerDis.getName()));
            
            // Set basline function button state for current chart viewer.
            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                this.blfAcc = BLF_ButtonState.Hide;
            else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
                this.blfVel = BLF_ButtonState.Hide;
        }
        else if (operationType.equals(OperationType.PreviewBLC)) {
            V2DataType v2DataType;
            VFileConstants.CorrectionOrder cOrder;
            double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            String fName = this.cboxFunction.getSelectedItem().toString();
            double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
            double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
            double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
            boolean isPreview = true;

            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                v2DataType = V2DataType.ACC;
            else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
                v2DataType = V2DataType.VEL;
            else 
                return;

            if (v2DataType.equals(V2DataType.VEL) && fName.equals(FunctionOption.MEAN.Name()))
                return;

            // Get selected correction order function.
            if (fName.equals(FunctionOption.MEAN.Name()))
                cOrder = VFileConstants.CorrectionOrder.MEAN;
            else if (fName.equals(FunctionOption.LINEAR.Name()))
                cOrder = VFileConstants.CorrectionOrder.ORDER1;
            else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
                cOrder = VFileConstants.CorrectionOrder.ORDER2;
            else 
                return;

            ArrayList<V2ProcessGUI> v2ProcessGUIs_Out = runBLC(v2ProcessGUIs,
                v2DataType,cOrder,fStart,fStop,fName,aStart,aStop,eventOnset,isPreview);

            // Recreate the charts.
            createSeismicCharts(v2ProcessGUIs_Out);
        }
        else if (operationType.equals(OperationType.RemoveBLC)) {
            V2DataType v2DataType;
            VFileConstants.CorrectionOrder cOrder;
            double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            String fName = this.cboxFunction.getSelectedItem().toString();
            double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
            double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
            double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
            boolean isPreview = false;

            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                v2DataType = V2DataType.ACC;
            else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
                v2DataType = V2DataType.VEL;
            else 
                return;

            if (v2DataType.equals(V2DataType.VEL) && fName.equals(FunctionOption.MEAN.Name()))
                return;

            // Get selected correction order function.
            if (fName.equals(FunctionOption.MEAN.Name()))
                cOrder = VFileConstants.CorrectionOrder.MEAN;
            else if (fName.equals(FunctionOption.LINEAR.Name()))
                cOrder = VFileConstants.CorrectionOrder.ORDER1;
            else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
                cOrder = VFileConstants.CorrectionOrder.ORDER2;
            else 
                return;

            ArrayList<V2ProcessGUI> v2ProcessGUIs_Out = runBLC(v2ProcessGUIs,
                v2DataType,cOrder,fStart,fStop,fName,aStart,aStop,eventOnset,isPreview);

            // Recreate the charts.
            createSeismicCharts(v2ProcessGUIs_Out);
            
            // Reset BLF button.
            resetBLF_Button();
        
            // Increment counter.
            this.correctedCnt++;
        }
        else if (operationType.equals(OperationType.Filter)) {
            double filterRangeLow = ((Number)this.ftxtFilterRangeLow.getValue()).doubleValue();
            double filterRangeHigh = ((Number)this.ftxtFilterRangeHigh.getValue()).doubleValue();
            double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
            
            filter(filterRangeLow,filterRangeHigh,eventOnset);

            // Recreate the charts.
            createSeismicCharts(v2ProcessGUIs);
        
            // Increment counter.
            this.filteredCnt++;
        }
        
        // Store editor panel inputs associated with specified chart viewer.
        storeEditorPanelInputs(curChartViewer);
    }
    
    private void setChartViewer(JPanel chartViewer) {
        JPanel[] viewers = {pnlViewerAcc,pnlViewerVel,pnlViewerDis};
        
        for (JPanel viewer : viewers) {
            if (viewer.getName().equals(chartViewer.getName())) {
                viewer.setEnabled(true);
                viewer.setBorder(BorderFactory.createLineBorder(new Color(0,0,128), 3));
                curChartViewer = viewer;
            }
            else {
                viewer.setEnabled(false);
                viewer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
        }
        
        // Update function list.
        updateFunctionSelectionList();
        
        // Update BLF button state and text.
        updateEditorPanelButtons();
        
        // Set chart view properties.
        setChartViewProperties(chartViews);
    }
    
    private void resetEditorPanelInputs() {
        storeEditorPanelInputs(pnlViewerAcc);
        storeEditorPanelInputs(pnlViewerVel);
        storeEditorPanelInputs(pnlViewerDis);
    }
    
    private void storeEditorPanelInputs(JPanel chartViewer) {

        if (chartViewer.getName().equals(this.pnlViewerAcc.getName())) {
            accFunction = this.cboxFunction.getSelectedItem().toString();
            accFunctionRangeStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            accFunctionRangeStop =((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            accApplicationRangeStart = ((Number)ftxtApplicationRangeStart.getValue()).doubleValue();
            accApplicationRangeStop = ((Number)ftxtApplicationRangeStop.getValue()).doubleValue();
            accEventOnset = ((Number)ftxtEventOnset.getValue()).doubleValue();
            accFilterRangeLow = ((Number)ftxtFilterRangeLow.getValue()).doubleValue();
            accFilterRangeHigh = ((Number)ftxtFilterRangeHigh.getValue()).doubleValue();
        }
        else if (chartViewer.getName().equals(this.pnlViewerVel.getName())) {
            velFunction = this.cboxFunction.getSelectedItem().toString();
            velFunctionRangeStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            velFunctionRangeStop =((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            velApplicationRangeStart = ((Number)ftxtApplicationRangeStart.getValue()).doubleValue();
            velApplicationRangeStop = ((Number)ftxtApplicationRangeStop.getValue()).doubleValue();
            velEventOnset = ((Number)ftxtEventOnset.getValue()).doubleValue();
            velFilterRangeLow = ((Number)ftxtFilterRangeLow.getValue()).doubleValue();
            velFilterRangeHigh = ((Number)ftxtFilterRangeHigh.getValue()).doubleValue();
        }
        else {
            disFunction = this.cboxFunction.getSelectedItem().toString();
            disFunctionRangeStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            disFunctionRangeStop =((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            disApplicationRangeStart = ((Number)ftxtApplicationRangeStart.getValue()).doubleValue();
            disApplicationRangeStop = ((Number)ftxtApplicationRangeStop.getValue()).doubleValue();
            disEventOnset = ((Number)ftxtEventOnset.getValue()).doubleValue();
            disFilterRangeLow = ((Number)ftxtFilterRangeLow.getValue()).doubleValue();
            disFilterRangeHigh = ((Number)ftxtFilterRangeHigh.getValue()).doubleValue();
        }
    }
    
    private void restoreEditorPanelInputs(JPanel chartViewer) {
        // Turn off editor panel control listeners.
        setTextFieldListeners(false);
        setRadioButtonListeners(false);
        
        if (chartViewer.getName().equals(this.pnlViewerAcc.getName())) {
            
            for (int i=0; i<cboxFunction.getItemCount();i++) {
                if (cboxFunction.getItemAt(i).toString().equals(accFunction)) {
                    cboxFunction.setSelectedIndex(i);
                    break;
                }
            }
            
            ftxtFunctionRangeStart.setValue(accFunctionRangeStart);
            ftxtFunctionRangeStop.setValue(accFunctionRangeStop);
            ftxtApplicationRangeStart.setValue(accApplicationRangeStart);
            ftxtApplicationRangeStop.setValue(accApplicationRangeStop);
            ftxtEventOnset.setValue(accEventOnset);
            ftxtFilterRangeLow.setValue(accFilterRangeLow);
            ftxtFilterRangeHigh.setValue(accFilterRangeHigh);
        }
        else if (chartViewer.getName().equals(this.pnlViewerVel.getName())) {
            for (int i=0; i<cboxFunction.getItemCount();i++) {
                if (cboxFunction.getItemAt(i).toString().equals(velFunction)) {
                    cboxFunction.setSelectedIndex(i);
                    break;
                }
            }
            
            ftxtFunctionRangeStart.setValue(velFunctionRangeStart);
            ftxtFunctionRangeStop.setValue(velFunctionRangeStop);
            ftxtApplicationRangeStart.setValue(velApplicationRangeStart);
            ftxtApplicationRangeStop.setValue(velApplicationRangeStop);
            ftxtEventOnset.setValue(velEventOnset);
            ftxtFilterRangeLow.setValue(velFilterRangeLow);
            ftxtFilterRangeHigh.setValue(velFilterRangeHigh);
        }
        else {
            for (int i=0; i<cboxFunction.getItemCount();i++) {
                if (cboxFunction.getItemAt(i).toString().equals(disFunction)) {
                    cboxFunction.setSelectedIndex(i);
                    break;
                }
            }
            
            ftxtFunctionRangeStart.setValue(disFunctionRangeStart);
            ftxtFunctionRangeStop.setValue(disFunctionRangeStop);
            ftxtApplicationRangeStart.setValue(disApplicationRangeStart);
            ftxtApplicationRangeStop.setValue(disApplicationRangeStop);
            ftxtEventOnset.setValue(disEventOnset);
            ftxtFilterRangeLow.setValue(disFilterRangeLow);
            ftxtFilterRangeHigh.setValue(disFilterRangeHigh);
        }
        
        // Turn back on editor panel control listeners.
        setTextFieldListeners(true);
        setRadioButtonListeners(true);
    }
    
    private void updateEditorPanelButtons() {
        // Set Show/Hide Baseline Function button state.
        if (this.curChartViewer.getName().equals(pnlViewerAcc.getName())) {
            // Initialize blfAcc, if null.
            if (blfAcc == null)
                blfAcc = BLF_ButtonState.Show;
            
            btnShowBLF.setText(blfAcc == BLF_ButtonState.Show ? BLF_ButtonState.Show.name() :
                BLF_ButtonState.Hide.name());
        }  
        else if (this.curChartViewer.getName().equals(pnlViewerVel.getName())) {
            // Initialize blfVel, if null.
            if (blfVel == null)
                blfVel = BLF_ButtonState.Show;
            
            btnShowBLF.setText(blfVel == BLF_ButtonState.Show ? BLF_ButtonState.Show.name() :
                BLF_ButtonState.Hide.name());
        }
            
        btnShowBLF.setEnabled(!this.curChartViewer.getName().equals(pnlViewerDis.getName()));

        // Set enable/disable state of filter button.
        //btnFilter.setEnabled(this.curChartViewer.getName().equals(pnlViewerAcc.getName()));
    }
    
    private void setChartViewProperties(ArrayList<SmChartView> chartViews) {
        if (chartViews == null || chartViews.isEmpty())
            return;
        
        JFormattedTextField field;
        MarkerStyle markerStyle;
        Color color;
        double width;
        
        if (this.rbtnFunctionRangeStart.isSelected()) {
            field = this.ftxtFunctionRangeStart;
            markerStyle = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerStyle();
            color = new Color(SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerColor());
            width = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStartMarkerWidth();
        }
        else if (this.rbtnFunctionRangeStop.isSelected()) {
            field = this.ftxtFunctionRangeStop;
            markerStyle = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerStyle();
            color = new Color(SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerColor());
            width = SmPreferences.SmSeismicTraceEditor.getFunctionRangeStopMarkerWidth();
        }
        else if (this.rbtnApplicationRangeStart.isSelected()) {
            field = this.ftxtApplicationRangeStart;
            markerStyle = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerStyle();
            color = new Color(SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerColor());
            width = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStartMarkerWidth();
        }
        else if (this.rbtnApplicationRangeStop.isSelected()) {
            field = this.ftxtApplicationRangeStop;
            markerStyle = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerStyle();
            color = new Color(SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerColor());
            width = SmPreferences.SmSeismicTraceEditor.getApplicationRangeStopMarkerWidth();
        }
        else if (this.rbtnEventOnset.isSelected()) {
            field = this.ftxtEventOnset;
            markerStyle = SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerStyle();
            color = new Color(SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerColor());
            width = SmPreferences.SmSeismicTraceEditor.getEventOnsetMarkerWidth();
        }
        else
            return;
                    
        for (SmChartView chartView : chartViews) {
            chartView.setBoundedTextField(field);
            chartView.setSmDataCursorStyle(markerStyle);
            chartView.setSmDataCursorColor(color);
            chartView.setSmDataCursorWidth(width);
        }
    }
    
    private boolean validateFunctionInputs() {
        boolean result = true;
        StringBuilder msg = new StringBuilder();
        
        if (this.ftxtFunctionRangeStart.getText().isEmpty())
            msg.append("  -Missing Function Start value\n");
        else if (!isDouble(this.ftxtFunctionRangeStart.getText()))
            msg.append("  -Invalid Function Start value\n");
        
        if (this.ftxtFunctionRangeStop.getText().isEmpty())
            msg.append("  -Missing Function Stop value\n");
        else if (!isDouble(this.ftxtFunctionRangeStop.getText()))
            msg.append("  -Invalid Function Stop value\n");
        
        if (isDouble(this.ftxtFunctionRangeStart.getText()) &&
            isDouble(this.ftxtFunctionRangeStop.getText())) {
            double fStart = ((Number)this.ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)this.ftxtFunctionRangeStop.getValue()).doubleValue();
            
            if (fStart < 0)
                msg.append("  -Function Start value is out of bounds\n");
            else if (fStart > this.xMax)
                msg.append("  -Function Start value is greater than max value " + this.xMax + "\n");
            else if (fStop < 0 || fStop > this.xMax)
                msg.append("  -Function Stop value is out of bounds\n");
            else if (fStart > fStop)
                msg.append("  -Function Start value cannot be greater than its Stop value\n");
        }
        
        if (this.cboxFunction.getSelectedIndex() == 0)
            msg.append("  -Function Type not selected\n");
        
        if (msg.length() > 0) {
            result = false;
             
            JOptionPane.showMessageDialog(this,msg.toString(),"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg.toString());
        }
            
        return result;
    }
    
    private boolean validateApplicationInputs() {
        boolean result = true;
        StringBuilder msg = new StringBuilder();
        
        if (this.ftxtFunctionRangeStart.getText().isEmpty())
            msg.append("  -Missing Function Range Start value\n");
        else if (!isDouble(this.ftxtFunctionRangeStart.getText()))
            msg.append("  -Invalid Function Range Start value\n");
        
        if (this.ftxtFunctionRangeStop.getText().isEmpty())
            msg.append("  -Missing Function Range Stop value\n");
        else if (!isDouble(this.ftxtFunctionRangeStop.getText()))
            msg.append("  -Invalid Function Range Stop value\n");
        
        if (isDouble(this.ftxtFunctionRangeStart.getText()) &&
            isDouble(this.ftxtFunctionRangeStop.getText())) {
            double fStart = ((Number)this.ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)this.ftxtFunctionRangeStop.getValue()).doubleValue();
            
            if (fStart < 0 || fStart > this.xMax)
                msg.append("  -Function Start value is out of bounds\n");
            else if (fStop < 0 || fStop > this.xMax)
                msg.append("  -Function Stop value is out of bounds\n");
            else if (fStart > fStop)
                msg.append("  -Function Start value cannot be greater than its Stop value\n");
        }
        
        if (this.ftxtApplicationRangeStart.getText().isEmpty())
            msg.append("  -Missing Application Range Start value\n");
        else if (!isDouble(this.ftxtApplicationRangeStart.getText()))
            msg.append("  -Invalid Application Range Start value\n");
        
        if (this.ftxtApplicationRangeStop.getText().isEmpty())
            msg.append("  -Missing Application Range Stop value\n");
        else if (!isDouble(this.ftxtApplicationRangeStop.getText()))
            msg.append("  -Invalid Application Range Stop value\n");
        
        if (isDouble(this.ftxtApplicationRangeStart.getText()) &&
            isDouble(this.ftxtApplicationRangeStop.getText())) {
            double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
            double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
            
            if (aStart < 0 || aStart > this.xMax)
                msg.append("  -Application Start value is out of bounds\n");
            else if (aStop < 0 || aStop > this.xMax)
                msg.append("  -Application Stop value is out of bounds\n");
            else if (aStart > aStop)
                msg.append("  -Application Start value cannot be greater than its Stop value\n");
        }
        
        if (this.ftxtEventOnset.getText().isEmpty())
            msg.append("  -Missing Event Onset value\n");
        else if (!isDouble(this.ftxtEventOnset.getText()))
            msg.append("  -Invalid Event Onset value value\n");
        
        if (isDouble(this.ftxtEventOnset.getText())) {
            double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
            
            if (eventOnset < 0 || eventOnset > this.xMax)
                msg.append("  -Event Onset value is out of bounds\n");
        }
        
        if (this.cboxFunction.getSelectedIndex() == 0)
            msg.append("  -Function Type not selected\n");
        
        if (msg.length() > 0) {
            result = false;
            
            JOptionPane.showMessageDialog(this,msg.toString(),"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg.toString());
        }
            
        return result;
    }
    
    private boolean validateCommitRequest() {
        boolean result = true;
        StringBuilder msg = new StringBuilder();
        
        if (v2ProcessState != V2ProcessState.FILTERED || this.correctedCnt != this.filteredCnt ) {
            msg.append("Baseline correction and filtering (in order) must be performed before committing.");
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

        btngrpRange = new javax.swing.ButtonGroup();
        splitpaneMain = new javax.swing.JSplitPane();
        splitpaneViewerRecorder = new javax.swing.JSplitPane();
        scrollpaneRecorder = new javax.swing.JScrollPane();
        tblRecorder = new javax.swing.JTable();
        scrollpaneViewer = new javax.swing.JScrollPane();
        pnlViewer = new javax.swing.JPanel();
        pnlViewerAcc = new javax.swing.JPanel();
        pnlViewerVel = new javax.swing.JPanel();
        pnlViewerDis = new javax.swing.JPanel();
        lblViewerVel = new javax.swing.JLabel();
        lblViewerAcc = new javax.swing.JLabel();
        lblViewerDis = new javax.swing.JLabel();
        pnlEditor = new javax.swing.JPanel();
        pnlApplicationRange = new javax.swing.JPanel();
        rbtnApplicationRangeStart = new javax.swing.JRadioButton();
        rbtnApplicationRangeStop = new javax.swing.JRadioButton();
        btnRemoveBLC = new javax.swing.JButton();
        ftxtApplicationRangeStart = new javax.swing.JFormattedTextField();
        ftxtApplicationRangeStop = new javax.swing.JFormattedTextField();
        btnPreviewBLC = new javax.swing.JButton();
        pnlFunctionRange = new javax.swing.JPanel();
        rbtnFunctionRangeStart = new javax.swing.JRadioButton();
        cboxFunction = new javax.swing.JComboBox();
        lblFunction = new javax.swing.JLabel();
        ftxtFunctionRangeStart = new javax.swing.JFormattedTextField();
        ftxtFunctionRangeStop = new javax.swing.JFormattedTextField();
        btnShowBLF = new javax.swing.JButton();
        rbtnFunctionRangeStop = new javax.swing.JRadioButton();
        pnlEvent = new javax.swing.JPanel();
        ftxtEventOnset = new javax.swing.JFormattedTextField();
        rbtnEventOnset = new javax.swing.JRadioButton();
        pnlFilter = new javax.swing.JPanel();
        btnFilter = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        ftxtFilterRangeLow = new javax.swing.JFormattedTextField();
        lblFilterRangeHigh = new javax.swing.JLabel();
        ftxtFilterRangeHigh = new javax.swing.JFormattedTextField();
        jPanel1 = new javax.swing.JPanel();
        btnReset = new javax.swing.JButton();
        btnCommit = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(850, 700));
        setSize(new java.awt.Dimension(1300, 728));

        splitpaneMain.setDividerLocation(525);
        splitpaneMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitpaneMain.setResizeWeight(0.9);

        splitpaneViewerRecorder.setBackground(new java.awt.Color(255, 255, 255));
        splitpaneViewerRecorder.setBorder(null);
        splitpaneViewerRecorder.setDividerLocation(1000);
        splitpaneViewerRecorder.setResizeWeight(0.8);

        scrollpaneRecorder.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        scrollpaneRecorder.setPreferredSize(new java.awt.Dimension(400, 402));

        tblRecorder.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Step", "Operation"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblRecorder.setGridColor(javax.swing.UIManager.getDefaults().getColor("Table.dropLineColor"));
        scrollpaneRecorder.setViewportView(tblRecorder);
        if (tblRecorder.getColumnModel().getColumnCount() > 0) {
            tblRecorder.getColumnModel().getColumn(0).setMinWidth(15);
            tblRecorder.getColumnModel().getColumn(0).setMaxWidth(60);
            tblRecorder.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.tblRecorder.columnModel.title0")); // NOI18N
            tblRecorder.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.tblRecorder.columnModel.title1")); // NOI18N
        }

        splitpaneViewerRecorder.setRightComponent(scrollpaneRecorder);

        scrollpaneViewer.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        scrollpaneViewer.setPreferredSize(new java.awt.Dimension(900, 500));

        pnlViewer.setPreferredSize(new java.awt.Dimension(950, 645));

        pnlViewerAcc.setBackground(new java.awt.Color(255, 255, 255));
        pnlViewerAcc.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlViewerAcc.setName("pnlViewerAcc"); // NOI18N
        pnlViewerAcc.setPreferredSize(new java.awt.Dimension(2, 140));
        pnlViewerAcc.setLayout(new javax.swing.BoxLayout(pnlViewerAcc, javax.swing.BoxLayout.PAGE_AXIS));

        pnlViewerVel.setBackground(new java.awt.Color(255, 255, 255));
        pnlViewerVel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlViewerVel.setName("pnlViewerVel"); // NOI18N
        pnlViewerVel.setPreferredSize(new java.awt.Dimension(2, 140));
        pnlViewerVel.setVerifyInputWhenFocusTarget(false);
        pnlViewerVel.setLayout(new javax.swing.BoxLayout(pnlViewerVel, javax.swing.BoxLayout.PAGE_AXIS));

        pnlViewerDis.setBackground(new java.awt.Color(255, 255, 255));
        pnlViewerDis.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlViewerDis.setName("pnlViewerDis"); // NOI18N
        pnlViewerDis.setPreferredSize(new java.awt.Dimension(2, 140));
        pnlViewerDis.setLayout(new javax.swing.BoxLayout(pnlViewerDis, javax.swing.BoxLayout.PAGE_AXIS));

        lblViewerVel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblViewerVel.setLabelFor(pnlViewerVel);
        org.openide.awt.Mnemonics.setLocalizedText(lblViewerVel, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.lblViewerVel.text")); // NOI18N

        lblViewerAcc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblViewerAcc.setLabelFor(pnlViewerVel);
        org.openide.awt.Mnemonics.setLocalizedText(lblViewerAcc, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.lblViewerAcc.text")); // NOI18N

        lblViewerDis.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblViewerDis.setLabelFor(pnlViewerVel);
        org.openide.awt.Mnemonics.setLocalizedText(lblViewerDis, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.lblViewerDis.text")); // NOI18N

        javax.swing.GroupLayout pnlViewerLayout = new javax.swing.GroupLayout(pnlViewer);
        pnlViewer.setLayout(pnlViewerLayout);
        pnlViewerLayout.setHorizontalGroup(
            pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblViewerDis, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblViewerAcc, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblViewerVel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlViewerVel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 887, Short.MAX_VALUE)
                    .addComponent(pnlViewerAcc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlViewerDis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        pnlViewerLayout.setVerticalGroup(
            pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewerLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlViewerAcc, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                    .addComponent(lblViewerAcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlViewerVel, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                    .addComponent(lblViewerVel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlViewerDis, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                    .addComponent(lblViewerDis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 27, 27))
        );

        scrollpaneViewer.setViewportView(pnlViewer);

        splitpaneViewerRecorder.setLeftComponent(scrollpaneViewer);

        splitpaneMain.setTopComponent(splitpaneViewerRecorder);

        pnlApplicationRange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.pnlApplicationRange.border.title"))); // NOI18N

        btngrpRange.add(rbtnApplicationRangeStart);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnApplicationRangeStart, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.AStart.text")); // NOI18N
        rbtnApplicationRangeStart.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.rbtnApplicationRangeStart.toolTipText")); // NOI18N
        rbtnApplicationRangeStart.setName("AStart"); // NOI18N

        btngrpRange.add(rbtnApplicationRangeStop);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnApplicationRangeStop, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.AStop.text")); // NOI18N
        rbtnApplicationRangeStop.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.rbtnApplicationRangeStop.toolTipText")); // NOI18N
        rbtnApplicationRangeStop.setName("AStop"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveBLC, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnRemoveBLC.text")); // NOI18N
        btnRemoveBLC.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnRemoveBLC.toolTipText")); // NOI18N
        btnRemoveBLC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveBLCActionPerformed(evt);
            }
        });

        ftxtApplicationRangeStart.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtApplicationRangeStart.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.ftxtApplicationRangeStart.toolTipText")); // NOI18N
        ftxtApplicationRangeStart.setName("AStart"); // NOI18N

        ftxtApplicationRangeStop.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtApplicationRangeStop.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.ftxtApplicationRangeStop.toolTipText")); // NOI18N
        ftxtApplicationRangeStop.setName("AStop"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnPreviewBLC, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnPreviewBLC.text")); // NOI18N
        btnPreviewBLC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewBLCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlApplicationRangeLayout = new javax.swing.GroupLayout(pnlApplicationRange);
        pnlApplicationRange.setLayout(pnlApplicationRangeLayout);
        pnlApplicationRangeLayout.setHorizontalGroup(
            pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlApplicationRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlApplicationRangeLayout.createSequentialGroup()
                        .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbtnApplicationRangeStop)
                            .addComponent(rbtnApplicationRangeStart))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ftxtApplicationRangeStart, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(ftxtApplicationRangeStop)))
                    .addGroup(pnlApplicationRangeLayout.createSequentialGroup()
                        .addComponent(btnPreviewBLC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveBLC)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        pnlApplicationRangeLayout.setVerticalGroup(
            pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlApplicationRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtnApplicationRangeStart)
                    .addComponent(ftxtApplicationRangeStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rbtnApplicationRangeStop)
                    .addComponent(ftxtApplicationRangeStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlApplicationRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemoveBLC)
                    .addComponent(btnPreviewBLC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFunctionRange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.pnlFunctionRange.border.title"))); // NOI18N

        btngrpRange.add(rbtnFunctionRangeStart);
        rbtnFunctionRangeStart.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFunctionRangeStart, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.FStart.text")); // NOI18N
        rbtnFunctionRangeStart.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.FStart.toolTipText")); // NOI18N
        rbtnFunctionRangeStart.setName("FStart"); // NOI18N

        cboxFunction.setName("Function"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFunction, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.lblFunction.text")); // NOI18N

        ftxtFunctionRangeStart.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFunctionRangeStart.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.ftxtFunctionRangeStart.toolTipText")); // NOI18N
        ftxtFunctionRangeStart.setName("FStart"); // NOI18N

        ftxtFunctionRangeStop.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFunctionRangeStop.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.ftxtFunctionRangeStop.toolTipText")); // NOI18N
        ftxtFunctionRangeStop.setName("FStop"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnShowBLF, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.BLF_Btn.text")); // NOI18N
        btnShowBLF.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.BLF_Btn.toolTipText")); // NOI18N
        btnShowBLF.setName("BLF_Btn"); // NOI18N
        btnShowBLF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowBLFActionPerformed(evt);
            }
        });

        btngrpRange.add(rbtnFunctionRangeStop);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnFunctionRangeStop, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.FStop.text")); // NOI18N
        rbtnFunctionRangeStop.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.rbtnFunctionRangeStop.toolTipText")); // NOI18N
        rbtnFunctionRangeStop.setName("FStop"); // NOI18N

        javax.swing.GroupLayout pnlFunctionRangeLayout = new javax.swing.GroupLayout(pnlFunctionRange);
        pnlFunctionRange.setLayout(pnlFunctionRangeLayout);
        pnlFunctionRangeLayout.setHorizontalGroup(
            pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFunctionRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFunctionRangeLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblFunction)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxFunction, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowBLF, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFunctionRangeLayout.createSequentialGroup()
                        .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(rbtnFunctionRangeStop, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                            .addComponent(rbtnFunctionRangeStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ftxtFunctionRangeStart, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(ftxtFunctionRangeStop))))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlFunctionRangeLayout.setVerticalGroup(
            pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFunctionRangeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtnFunctionRangeStart)
                    .addComponent(ftxtFunctionRangeStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftxtFunctionRangeStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbtnFunctionRangeStop))
                .addGap(18, 18, 18)
                .addGroup(pnlFunctionRangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFunction)
                    .addComponent(cboxFunction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowBLF))
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pnlEvent.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.pnlEvent.border.title"))); // NOI18N

        ftxtEventOnset.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtEventOnset.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.ftxtEventOnset.toolTipText")); // NOI18N
        ftxtEventOnset.setName("EOnset"); // NOI18N

        btngrpRange.add(rbtnEventOnset);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnEventOnset, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmChartEditor.EOnset.text")); // NOI18N
        rbtnEventOnset.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.rbtnEventOnset.toolTipText")); // NOI18N
        rbtnEventOnset.setName("EOnset"); // NOI18N

        javax.swing.GroupLayout pnlEventLayout = new javax.swing.GroupLayout(pnlEvent);
        pnlEvent.setLayout(pnlEventLayout);
        pnlEventLayout.setHorizontalGroup(
            pnlEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEventLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbtnEventOnset)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ftxtEventOnset, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(126, Short.MAX_VALUE))
        );
        pnlEventLayout.setVerticalGroup(
            pnlEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEventLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftxtEventOnset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbtnEventOnset))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pnlFilter.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.pnlFilter.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnFilter, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnFilter.text")); // NOI18N
        btnFilter.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnFilter.toolTipText")); // NOI18N
        btnFilter.setAutoscrolls(true);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterEditPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.jLabel1.text")); // NOI18N

        ftxtFilterRangeLow.setEditable(false);
        ftxtFilterRangeLow.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeLow.setText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.FilterRangeLow.text")); // NOI18N
        ftxtFilterRangeLow.setName("FilterRangeLow"); // NOI18N

        lblFilterRangeHigh.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(lblFilterRangeHigh, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.lblFilterRangeHigh.text")); // NOI18N

        ftxtFilterRangeHigh.setEditable(false);
        ftxtFilterRangeHigh.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtFilterRangeHigh.setText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.FilterRangeHigh.text")); // NOI18N
        ftxtFilterRangeHigh.setName("FilterRangeHigh"); // NOI18N

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(lblFilterRangeHigh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ftxtFilterRangeLow, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(ftxtFilterRangeHigh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(btnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFilterLayout.createSequentialGroup()
                        .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(ftxtFilterRangeLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFilterRangeHigh)
                            .addComponent(ftxtFilterRangeHigh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFilterLayout.createSequentialGroup()
                        .addComponent(btnFilter)
                        .addGap(45, 45, 45))))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnReset.text")); // NOI18N
        btnReset.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnReset.toolTipText")); // NOI18N
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCommit, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnCommit.text")); // NOI18N
        btnCommit.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnCommit.toolTipText")); // NOI18N
        btnCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnExit, org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnExit.text")); // NOI18N
        btnExit.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceEditor.class, "SmSeismicTraceEditor.btnExit.toolTipText")); // NOI18N
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
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCommit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCommit, btnExit, btnReset});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset)
                    .addComponent(btnCommit)
                    .addComponent(btnExit))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlEditorLayout = new javax.swing.GroupLayout(pnlEditor);
        pnlEditor.setLayout(pnlEditorLayout);
        pnlEditorLayout.setHorizontalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFunctionRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlApplicationRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEditorLayout.createSequentialGroup()
                        .addComponent(pnlEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(312, Short.MAX_VALUE))
                    .addGroup(pnlEditorLayout.createSequentialGroup()
                        .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))))
        );
        pnlEditorLayout.setVerticalGroup(
            pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlEditorLayout.createSequentialGroup()
                        .addComponent(pnlEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE))
                    .addComponent(pnlFunctionRange, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlApplicationRange, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        splitpaneMain.setRightComponent(pnlEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1281, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitpaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
 
        if (!validateCommitRequest())
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
            
            String inV1FilePath = v1SmFile.getFile().getPath();
           
            String eventsRootDir = SmCore.extractEventsRootDir(inV1FilePath);
            String event = SmCore.extractEventName(inV1FilePath);
            String station = SmCore.extractStationName(inV1FilePath);
            
            File fileStation = Paths.get(eventsRootDir,event,station).toFile();
            
            double fStart = ((Number)this.ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)this.ftxtFunctionRangeStop.getValue()).doubleValue();
            String function = this.cboxFunction.getSelectedItem().toString();
            double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
            double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
            double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
            
            SmTimeFormatter timer = new SmTimeFormatter();
            String startTime = timer.getGMTdateTime();
            
            addToLogList(logList,"Seismic trace commit processing started at " + startTime);
            addToLogList(logList,"");
            addToLogList(logList,"Event: " + event);
            addToLogList(logList,"Station: " + station);
            addToLogList(logList,"Function Start Time (sec): " + decimalFormatter.format(fStart));
            addToLogList(logList,"Function Stop Time (sec): " + decimalFormatter.format(fStop));
            addToLogList(logList,"Function: " + function);
            addToLogList(logList,"Application Start Time (sec): " + decimalFormatter.format(aStart));
            addToLogList(logList,"Application Stop Time (sec): " + decimalFormatter.format(aStop));
            addToLogList(logList,"Event Onset Time (sec): " + decimalFormatter.format(eventOnset));
            addToLogList(logList,"Processing in progress ...");
            addToLogList(logList,"");
            
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Create SmProductGUI object.
                SmProductGUI smProductGUI = new SmProductGUI();
                smProductGUI.updateDirectoriesGUI(fileStation);
        
                addToLogList(logList,"Source V1 File: " + v2ProcessGUI.getV1File().getPath());
                addToLogList(logList,"Event Onset Time (sec): " + decimalFormatter.format(v2ProcessGUI.getEventOnset()));
                addToLogList(logList,"Delta Time (sec): " + v2ProcessGUI.getDTime());
                addToLogList(logList,"Baseline Correction Steps:");
                
                for (BLCStep blcStep : v2ProcessGUI.getBLCSteps())
                    addToLogList(logList,"  " + blcStep.toString());
                
                // Commit processing.
                if (v2ProcessGUI.commitProcessing() == V2Status.GOOD) {
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
                    
                    // Check if padded acceleration array is null (required in V3Process processing).
                    double[] paccel = v2ProcessGUI.getPaddedAccel();
                    if (paccel == null) {
                        JOptionPane.showMessageDialog(this, 
                            "Padded acceleration array, which is required for V3 processing, is null",
                            "Padded acceleration array null", JOptionPane.PLAIN_MESSAGE);
                        continue;
                    }
                    
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
                        //    append(".").append(segments[1]);
                        //File csvFile = Paths.get(logsDir,event,station,fileName.toString()).toFile();
                        //smProductGUI.updateUploadParms(v3Component, v1Component, 
                        //    v2ComponentAcc, v2ComponentVel, v2ComponentDis, csvFile);
                        
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
            addToLogList(logList,"Seismic trace commit processing ended at " + timer.getGMTdateTime());
            logList.add("");
            logList.add("");

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

    private void btnRemoveBLCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBLCActionPerformed
        if (!validateApplicationInputs())
            return;
        
        V2DataType v2DataType;
        VFileConstants.CorrectionOrder cOrder;
        double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
        double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
        String fName = this.cboxFunction.getSelectedItem().toString();
        double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
        double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
        double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
        boolean isPreview = false;
        
        if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
            v2DataType = V2DataType.ACC;
        else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
            v2DataType = V2DataType.VEL;
        else {
            String msg = "Operation cannot be performed on selected viewer.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        if (v2DataType.equals(V2DataType.VEL) && fName.equals(FunctionOption.MEAN.Name()))
        {
            String msg = "Mean function cannot be used with velocity data.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        // Get selected correction order function.
        if (fName.equals(FunctionOption.MEAN.Name()))
            cOrder = VFileConstants.CorrectionOrder.MEAN;
        else if (fName.equals(FunctionOption.LINEAR.Name()))
            cOrder = VFileConstants.CorrectionOrder.ORDER1;
        else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
            cOrder = VFileConstants.CorrectionOrder.ORDER2;
        else {
            String msg = "Correction order could not be determined.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        v2ProcessGUIs = runBLC(v2ProcessGUIs,v2DataType,cOrder,fStart,fStop,fName,
            aStart,aStop,eventOnset,isPreview);
            
        // Set V2Process state.
        v2ProcessState = V2ProcessState.CORRECTED;
                
        // Increment corrected counter.
        this.correctedCnt++;
        
        // Add recorder step to table.
        addRecorderStep(createRecorderStep(OperationType.RemoveBLC));
        
        // Recreate the charts.
        createSeismicCharts(v2ProcessGUIs);
        
        // Reset BLF button.
        resetBLF_Button();
        
        //DecimalFormat df = new DecimalFormat("0.0###");
        //df.setRoundingMode(RoundingMode.CEILING);

        SmCore.addMsgToStatusViewer(String.format("Baseline correction applied using parameters: " +
            "fName=%s, v2Datatype=%s, fStart=%s, fStop=%s, aStart=%s, aStop=%s, eventOnset=%s", 
            fName,v2DataType.toString(),decimalFormatter.format(fStart),decimalFormatter.format(fStop),
            decimalFormatter.format(aStart),decimalFormatter.format(aStop),
            decimalFormatter.format(eventOnset)));
    }//GEN-LAST:event_btnRemoveBLCActionPerformed

    private void btnShowBLFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowBLFActionPerformed
        if (curChartViewer.getName().equals(pnlViewerDis.getName())) {
            JOptionPane.showMessageDialog(this,
                "Baseline function not valid for displacement trace.",
                "Invalid Operation",JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (btnShowBLF.getText().equals(BLF_ButtonState.Show.toString())) {
            if (!validateFunctionInputs())
                return;
        
            V2DataType v2DataType = curChartViewer.getName().equals(pnlViewerAcc.getName()) ?
                V2DataType.ACC : V2DataType.VEL;
            
            double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
            double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
            String fName = this.cboxFunction.getSelectedItem().toString();
            
            // Plot baseline function.
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Perform operation.
                plotBaselineFunction(v2ProcessGUI,v2DataType,fStart,fStop,fName);
                
                // Add recorder step to table.
                addRecorderStep(createRecorderStep(OperationType.ShowBaselineFunction));
            }
            
            // Toggle button.
            btnShowBLF.setText(BLF_ButtonState.Hide.toString());
            
            // Set basline function button state for current chart viewer.
            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                this.blfAcc = BLF_ButtonState.Hide;
            else 
                this.blfVel = BLF_ButtonState.Hide;   
        }
        else {
            for (Component component : curChartViewer.getComponents()) {
                if (component instanceof SingleChartView) {
                    SingleChartView singleChartView = (SingleChartView)component;
                    
                    // Remove previously created baseline function.
                    singleChartView.removeLinePlot(LinePlotType.BASELINE);
                    singleChartView.updateDraw();
                    
                    // Add recorder step to table.
                    addRecorderStep(createRecorderStep(OperationType.HideBaselineFunction));
                }
            }

            // Toggle button.
            btnShowBLF.setText(BLF_ButtonState.Show.toString());
            
            // Set basline function button state for current chart viewer.
            if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
                this.blfAcc = BLF_ButtonState.Show;
            else 
                this.blfVel = BLF_ButtonState.Show;
        }
    }//GEN-LAST:event_btnShowBLFActionPerformed

    private void btnFilterEditPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterEditPerformed
        
        try {
            SmFilterEditor filterEditor = new SmFilterEditor(this,true,
                SmGlobal.SM_CHARTS_API_QCCHART2D,this.v2ProcessGUIs,this.smTemplate);
            filterEditor.setLocationRelativeTo(this);
            
            if (filterEditor.showDialog()) {
                // Get filter range values.
                double filterRangeLow = filterEditor.getFilterRangeLow();
                double filterRangeHigh = filterEditor.getFilterRangeHigh();
                
                // Update filter range text fields.
                this.ftxtFilterRangeLow.setValue(filterRangeLow);
                this.ftxtFilterRangeHigh.setValue(filterRangeHigh);
                
                // Get event onset.
                double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
                
                // Filter the V2ProcessGUI array.
                filter(filterRangeLow, filterRangeHigh, eventOnset);
                
                // Increment filtered counter.
                this.filteredCnt++;
            
                // Add recorder step to table.
                addRecorderStep(createRecorderStep(OperationType.Filter));
                
                // Re-create seismic charts.
                createSeismicCharts(this.v2ProcessGUIs);

                //DecimalFormat df = new DecimalFormat("0.0###");
                //df.setRoundingMode(RoundingMode.CEILING);
                    
                SmCore.addMsgToStatusViewer(String.format("Filter processing applied using low and high " +
                    "filter corners of %s and %s, respectively.", 
                    decimalFormatter.format(filterRangeLow),decimalFormatter.format(filterRangeHigh)));
            }
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnFilterEditPerformed

    private void btnPreviewBLCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewBLCActionPerformed
        if (!validateApplicationInputs())
            return;
        
        V2DataType v2DataType;
        VFileConstants.CorrectionOrder cOrder;
        double fStart = ((Number)ftxtFunctionRangeStart.getValue()).doubleValue();
        double fStop = ((Number)ftxtFunctionRangeStop.getValue()).doubleValue();
        String fName = this.cboxFunction.getSelectedItem().toString();
        double aStart = ((Number)this.ftxtApplicationRangeStart.getValue()).doubleValue();
        double aStop = ((Number)this.ftxtApplicationRangeStop.getValue()).doubleValue();
        double eventOnset = ((Number)this.ftxtEventOnset.getValue()).doubleValue();
        boolean isPreview = true;
        
        if (curChartViewer.getName().equals(pnlViewerAcc.getName()))
            v2DataType = V2DataType.ACC;
        else if (curChartViewer.getName().equals(pnlViewerVel.getName()))
            v2DataType = V2DataType.VEL;
        else {
            String msg = "Operation cannot be performed on selected viewer.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        if (v2DataType.equals(V2DataType.VEL) && fName.equals(FunctionOption.MEAN.Name()))
        {
            String msg = "Mean function cannot be used with velocity data.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        // Get selected correction order function.
        if (fName.equals(FunctionOption.MEAN.Name()))
            cOrder = VFileConstants.CorrectionOrder.MEAN;
        else if (fName.equals(FunctionOption.LINEAR.Name()))
            cOrder = VFileConstants.CorrectionOrder.ORDER1;
        else if (fName.equals(FunctionOption.POLYNOMIAL.Name()))
            cOrder = VFileConstants.CorrectionOrder.ORDER2;
        else {
            String msg = "Correction order could not be determined.";
            JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Error(s):\n" + msg);
            return;
        }
        
        ArrayList<V2ProcessGUI> v2ProcessGUIs_Out = runBLC(v2ProcessGUIs,
            v2DataType,cOrder,fStart,fStop,fName,aStart,aStop,eventOnset,isPreview);
        
        // Add recorder step to table.
        addRecorderStep(createRecorderStep(OperationType.PreviewBLC));
        
        // Recreate the charts.
        createSeismicCharts(v2ProcessGUIs_Out);
    }//GEN-LAST:event_btnPreviewBLCActionPerformed

    private final class RecorderStep {
        private final SmEditOperation smEditOperation;
        
        public RecorderStep(SmEditOperation smEditOperation) {
            this.smEditOperation = smEditOperation;
        }
        
        public SmEditOperation getSmEditOperation() {return this.smEditOperation;}
        
        @Override
        public String toString() {

            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(this.smEditOperation.getOperationType());

            ArrayList<Parm<?,?>> parms = this.smEditOperation.getParms();
            
            if (parms.size() > 0) {
                strBuilder.append(" (");

                for (int i=0; i < parms.size(); i++) {
                    if (i > 0)
                        strBuilder.append(",");

                    strBuilder.append(parms.get(i).toString());
                }

                strBuilder.append(")");
            }

            return strBuilder.toString();
        }
    }
    
    private class CreateSeismicChartsTask extends SwingWorker<Void, Void>
    {
        private final ArrayList<V2ProcessGUI> v2PGUIs;
        
        public CreateSeismicChartsTask(final ArrayList<V2ProcessGUI> v2PGUIs) {
            this.v2PGUIs = v2PGUIs;
        }
        
        @Override
        public Void doInBackground()
        {
            try { 
                for (V2ProcessGUI v2PGUI : v2PGUIs) {
                    createSeismicChart(v2PGUI,VFileConstants.V2DataType.ACC);
                    createSeismicChart(v2PGUI,VFileConstants.V2DataType.VEL);
                    createSeismicChart(v2PGUI,VFileConstants.V2DataType.DIS);
                }
            }
            catch(Exception ex) {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
            
            return null;
        }
        
        @Override
        public void done() {
            // Set chart view properties.
            setChartViewProperties(chartViews);
        }
    }
    
    private class CreateSeismicChartTask extends SwingWorker<SingleChartView,Void> 
    {
        private final String chartAPI;
        private final SmRec smRec;
        private final SmTemplate smTemplate;
        private final ArrayList<SmPoint> smPoints;
        private final int dataParmCode;
        private final String channel;
        
        public CreateSeismicChartTask(final String chartAPI,
            final SmRec smRec, final SmTemplate smTemplate) 
        {
            this.chartAPI = chartAPI;
            this.smRec = smRec;
            this.smTemplate = smTemplate;
            this.smPoints = smRec.getSmPoints();
            this.dataParmCode = smRec.getDataParmCode();
            this.channel = smRec.getChannel();
        }
        
        @Override
        public SingleChartView doInBackground() {
            try 
            {  
                SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                    new QCChart2D_API() : null;

                if (smCharts_API == null)
                    return null;

                Pattern p = Pattern.compile("^(\\w{3})(\\.)(\\w{2})$");
                Matcher m = p.matcher(channel);
                m.find();

                String seed = m.group(1);
                String lCode = m.group(3);

                // Initialize SmStation and SmChannel objects.
                SmStation smStation = (smTemplate != null) ? 
                    smTemplate.getSmStation() : null;
                SmEpoch smEpoch = (smStation != null) ?
                    smStation.getSmEpoch(earliestStartTime, latestStopTime) : null;
                SmChannel smChannel = (smEpoch != null) ?
                    smEpoch.getSmChannel(channel) : null;
                
                // Set series title and description.
                String title = channel;
                String description = smRec.getEventName() + "_" + 
                    smRec.getStationCode() + "." + seed + "." +
                    smRec.getNetworkCode() + "." + lCode;

//                    // Initialize random color values.
//                    Random rand = new Random(); 
//                    int redValue = rand.nextInt(255); 
//                    int greenValue = rand.nextInt(255); 
//                    int blueValue = rand.nextInt(255); 
//
//                    // Create initial color
//                    Color color = new Color(redValue, greenValue, blueValue);

                // Create color for series.
                Color color;
                if (smTemplate == null)
                    color = new Color(SmPreferences.ChartOptions.getColorPlot());
                else {
                    color = (smChannel != null) ? smChannel.getColor() :
                        new Color(SmPreferences.ChartOptions.getColorPlot());
                }

                // Create SmSeries object.
                SmSeries smSeries = new SmSeries(smPoints,dataParmCode,
                    title,description,color,null);
                String plotType = SmGlobal.PLOT_TYPE_SEISMIC;
                SmCharts_API.XYBounds xyBounds = null;

                String chartTitle = smSeries.getDecription();
                String xAxisTitle = "Time (sec)";
                String yAxisTitle = "";
                 
                switch (dataParmCode)
                {
                    case 1:
                        yAxisTitle = "Accelaration (cm/sec" + "\u00B2" + ")";
                        break;
                    case 2:
                        yAxisTitle = "Velocity (cm/sec)";
                        break;
                    case 3:
                        yAxisTitle = "Displacement (cm)";
                        break;
                }
                
                final SingleChartView chartView = 
                    (SingleChartView)smCharts_API.createChart(smSeries,plotType,
                    xyBounds,chartTitle,xAxisTitle,yAxisTitle,true,owner);
                    
                return chartView;
            } 
            catch (Exception ex) 
            {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            } 
            
            return null;
        }
        
        @Override
        public void done() {
            try {
                final SingleChartView chartView = get();
                
                if (chartView != null) {
                    
                    chartViews.add(chartView);
                    
                    switch (dataParmCode)
                    {
                        case 1:
                            pnlViewerAcc.add(chartView);
                            break;
                        case 2:
                            pnlViewerVel.add(chartView);
                            break;
                        case 3:
                            pnlViewerDis.add(chartView);
                            break;
                    }
                    pnlViewer.updateUI();
                }
            }
            catch (InterruptedException ex) {}
            catch (ExecutionException ex) {
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
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
            if (e.getComponent() instanceof SmSeismicTraceEditor) {
                SmSeismicTraceEditor chartEditor = (SmSeismicTraceEditor)e.getComponent();
                SmCore.addMsgToStatusViewer("Seismic Chart Editor exited with status: " + chartEditor.getStatus());
            }
        }
    }
    
    private class ChartViewerMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            /*
            Object src = event.getSource();
            if (src instanceof JPanel) {
                JPanel pnl = (JPanel)src;
                
                if (!getCurrentChartViewer().getName().equals(pnl.getName())) {
                    setCurrentChartViewer(pnl);
                }
            }
            */
        }
    }
    
    private class RecorderMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            runRecorderSteps();
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
                
                if (fieldName != null) {
                    if (fieldName.equals(ftxtFunctionRangeStart.getName()))
                        rbtnFunctionRangeStart.setSelected(true);
                    else if (fieldName.equals(ftxtFunctionRangeStop.getName()))
                        rbtnFunctionRangeStop.setSelected(true);
                    else if (fieldName.equals(ftxtApplicationRangeStart.getName()))
                        rbtnApplicationRangeStart.setSelected(true);
                    else if (fieldName.equals(ftxtApplicationRangeStop.getName()))
                        rbtnApplicationRangeStop.setSelected(true);
                    else if (fieldName.equals(ftxtEventOnset.getName()))
                        rbtnEventOnset.setSelected(true);
                }
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
                
                if (curChartViewer == null)
                    return;
                
                if (curChartViewer.getComponentCount() > 0) {
                    for (Component component : curChartViewer.getComponents()) {
                        if (component instanceof SmChartView) {
                            SmChartView chartView = (SmChartView)component;
                            
                            double x = ((Number)field.getValue()).doubleValue();
                            double y = 0;

                            // Draw marker.
                            chartView.getSmDataCursor().drawMarker(x, y);
                            
                            // Set V2Process state.
                            v2ProcessState = V2ProcessState.EDIT;
                            
                            // Add recorder step to table.
                            addRecorderStep(createRecorderStep(OperationType.DrawMarker));
                        }
                    }
                }
            }
        }
    }
    
    private class TableHeaderMouseMotionListener extends MouseMotionAdapter {
        JTable table = null;
        Map tips = new HashMap();
        
        TableColumn curCol;
        
        
        public TableHeaderMouseMotionListener(JTable table) {
            this.table = table;
            setToolTips();
        }
        
        private void setToolTips() {
            if (table == null)
                return;
            
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn col = table.getColumnModel().getColumn(i);
                String toolTip = col.getHeaderValue().toString();
                tips.put(col, toolTip);
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent evt) {
            TableColumnModel colModel = table.getColumnModel();
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            TableColumn col = null;
            
            if (vColIndex >= 0) {
                col = colModel.getColumn(vColIndex);
            }
            if (col != curCol) {
                table.getTableHeader().setToolTipText((String) tips.get(col));
                curCol = col;
            }
        }
    }
    
    private class RecorderTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            
            if (value == null || value.toString().trim().isEmpty()) {
                return null;
            }
                
            JLabel label = (JLabel)super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            label.setToolTipText(label.getText());
            
            return label;
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnPreviewBLC;
    private javax.swing.JButton btnRemoveBLC;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnShowBLF;
    private javax.swing.ButtonGroup btngrpRange;
    private javax.swing.JComboBox cboxFunction;
    private javax.swing.JFormattedTextField ftxtApplicationRangeStart;
    private javax.swing.JFormattedTextField ftxtApplicationRangeStop;
    private javax.swing.JFormattedTextField ftxtEventOnset;
    private javax.swing.JFormattedTextField ftxtFilterRangeHigh;
    private javax.swing.JFormattedTextField ftxtFilterRangeLow;
    private javax.swing.JFormattedTextField ftxtFunctionRangeStart;
    private javax.swing.JFormattedTextField ftxtFunctionRangeStop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblFilterRangeHigh;
    private javax.swing.JLabel lblFunction;
    private javax.swing.JLabel lblViewerAcc;
    private javax.swing.JLabel lblViewerDis;
    private javax.swing.JLabel lblViewerVel;
    private javax.swing.JPanel pnlApplicationRange;
    private javax.swing.JPanel pnlEditor;
    private javax.swing.JPanel pnlEvent;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlFunctionRange;
    private javax.swing.JPanel pnlViewer;
    private javax.swing.JPanel pnlViewerAcc;
    private javax.swing.JPanel pnlViewerDis;
    private javax.swing.JPanel pnlViewerVel;
    private javax.swing.JRadioButton rbtnApplicationRangeStart;
    private javax.swing.JRadioButton rbtnApplicationRangeStop;
    private javax.swing.JRadioButton rbtnEventOnset;
    private javax.swing.JRadioButton rbtnFunctionRangeStart;
    private javax.swing.JRadioButton rbtnFunctionRangeStop;
    private javax.swing.JScrollPane scrollpaneRecorder;
    private javax.swing.JScrollPane scrollpaneViewer;
    private javax.swing.JSplitPane splitpaneMain;
    private javax.swing.JSplitPane splitpaneViewerRecorder;
    private javax.swing.JTable tblRecorder;
    // End of variables declaration//GEN-END:variables
}
