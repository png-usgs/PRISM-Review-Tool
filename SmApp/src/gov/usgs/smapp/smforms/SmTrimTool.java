/*******************************************************************************
 * Name: Java class SmTrimTool.java
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
import static SmConstants.VFileConstants.CORACC;
import static SmConstants.VFileConstants.DISPLACE;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.RAWACC;
import static SmConstants.VFileConstants.UNCORACC;
import static SmConstants.VFileConstants.VELOCITY;
import SmControl.SmProductGUI;
import SmControl.SmQueue;
import SmException.FormatException;
import SmException.SmException;
import SmUtilities.SmTimeFormatter;
import SmUtilities.TextFileReader;
import com.quinncurtis.chart2djava.Marker;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.V1ChartsBin;
import gov.usgs.smapp.V2ChartsBin;
import gov.usgs.smapp.VxChartsBinGroup;
import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smapp.smchartingapi.qcchart2d.SingleChartView;
import gov.usgs.smapp.smchartingapi.qcchart2d.SmChartView;
import gov.usgs.smapp.smchartingapi.qcchart2d.SmDataCursor;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmNode;
import gov.usgs.smcommon.smclasses.SmPoint;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmTemplate;
import static gov.usgs.smcommon.smutilities.MathUtils.isDouble;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import gov.usgs.smcommon.smutilities.SmGUIUtils;
import gov.usgs.smcommon.smutilities.SmUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.NotifyDescriptor;

/**
 *
 * @author png-pr
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmTrimTool extends javax.swing.JFrame {
    private final ArrayList<SmNode> eventNodes = 
        SmCore.getEvents(SmCore.CheckedState.CHECKED);

    private final ArrayList<TraceSet> traceSets = new ArrayList<>();
    
    private final JPopupMenu editPopupMenu = SmGUIUtils.createCCPPopupMenu(); ;
    
    private final DateTimeFormatter dateTimeFormatter = 
        DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSSS");
    
    private final Color colorEqual = new Color(SmPreferences.SmTrimTool.getDateTimeEqualTextColor());
    private final Color colorUnequal = new Color(SmPreferences.SmTrimTool.getDateTimeUnequalTextColor());
    
    //Font font = new Font(fontName, (fontIsBold ? Font.BOLD : 0) +
                //(fontIsItal ? Font.ITALIC : 0), fontSize);
    
    private final TextFieldMouseListener textFieldMouseListener = 
        new TextFieldMouseListener();
    private final TextFieldPropertyListener textFieldPropertyListener = 
        new TextFieldPropertyListener();
    private final RBtnTypeListener rbtnTypeListener = new RBtnTypeListener();

    // Stores SmChartView objects for current TraceSet.
    private final ArrayList<SmChartView> smChartViews = new ArrayList<>();
    
    // Store earliest and latest start and stop DateTime for current TraceSet.
    private DateTime earliestStartTime;
    private DateTime latestStartTime;
    private DateTime earliestStopTime;
    private DateTime latestStopTime;
    
    private int traceSetsIndx = 0;
    
    /**
     * Creates new form SmTrimTool
     */
    public SmTrimTool() {
        initComponents();
        
        initForm();
    }
    
    private void initForm() {
        try
        {       
            // Set form icon.
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/resources/icons/prism_review_tool_16.png")));
            
            // Initialize data type controls.
            initDataTypeControls();
        
            // Initialize navigation controls.
            initNavigationControls();
            
            // Initialize attributes table.
            initAttributesTable();
            
            // Initialize control panel.
            initControlPanel();
            
            // Create the initial plots.
            runGraphs();
        }
        catch (Exception ex) {
            showMessage("Run Trim Tool Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Run Trim Tool Error: " + ex.getMessage());
        } 
    }
    
    private void initDataTypeControls() {
        // Initialize data type buttons' selected state.
        this.btnAcceleration.setSelected(false);
        this.btnVelocity.setSelected(false);
        this.btnDisplacement.setSelected(false);
        this.btnAll.setSelected(true);
        
        // Add action listener to each data type button.
        BtnDataTypeActionListener btnDataTypeActionListener = 
            new BtnDataTypeActionListener();
        btnAcceleration.addActionListener(btnDataTypeActionListener);
        btnVelocity.addActionListener(btnDataTypeActionListener);
        btnDisplacement.addActionListener(btnDataTypeActionListener);
        btnAll.addActionListener(btnDataTypeActionListener);
    }
    
    private void initNavigationControls() {
        this.btnNavFirst.setEnabled(false);
        this.btnNavPrevious.setEnabled(false);
        this.btnNavNext.setEnabled(false);
        this.btnNavLast.setEnabled(false);
        
        this.lblNavDisplay.setText("");
        
        BtnNavigationActionListener btnNavigationActionListener = 
            new BtnNavigationActionListener();
        btnNavFirst.addActionListener(btnNavigationActionListener);
        btnNavPrevious.addActionListener(btnNavigationActionListener);
        btnNavNext.addActionListener(btnNavigationActionListener);
        btnNavLast.addActionListener(btnNavigationActionListener);
    }
    
    private void initAttributesTable() {
        this.tblAttributes.addMouseListener(new AttributesTableMouseListener());
    }
    
    private void initControlPanel() {
        this.rbtnStartTime.setText("Start Time");
        this.rbtnStopTime.setText("Stop Time");
        
        // Reset initial radio button selection.
        this.rbtnStartTime.setSelected(true);
        
        // Add editor panel control listeners.
        this.ftxtStartTime.addMouseListener(textFieldMouseListener);
        this.ftxtStopTime.addMouseListener(textFieldMouseListener);
            
        setTextFieldPropertyListeners(true);
        setRadioButtonListeners(true);
    }
    
    public SmChartView getCurChartView() {
        if (smChartViews != null) {
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            int curTraceIndx = traceSet.getCurTraceIndx();
            
            if (curTraceIndx >= 0)
                return smChartViews.get(curTraceIndx);
            else
                return null;
        }
        
        return null;
    }
    
    private SmFile createV1SmFile(File file) {
        try {
            Path path = file.toPath();
            String filePath = file.getPath();
            String fileType = SmCore.extractCosmosFileType(filePath);

            if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString())) {
                return new SmFile(file);
            }
            else if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                // Form V1 directory path name.
                Path v1Dir = Paths.get(path.getParent().getParent().toString(),
                    SmGlobal.CosmosFileType.V1.toString());

                if (!Files.isDirectory(v1Dir))
                    return null;

                // Build V1 filename string based on V2 file.
                String regExCosmosFileType = String.format(
                    "(?i)(^.+)([\\.](%s|%s|%s))([\\.](V[\\d][cC]?)$)",
                    SmGlobal.CosmosV2DataType.ACC.toString(),
                    SmGlobal.CosmosV2DataType.VEL.toString(),
                    SmGlobal.CosmosV2DataType.DIS.toString());
                Pattern p = Pattern.compile(regExCosmosFileType);
                Matcher m = p.matcher(file.getName());

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

                if (Files.exists(v1FilePath) && Files.isRegularFile(v1FilePath)) {
                    // Create and return V1 SmFile object.
                    return new SmFile(v1FilePath.toFile());
                }
                else
                    return null;
            }
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        
        return null;
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
            
            // Set log time.
            SmTimeFormatter timer = new SmTimeFormatter();
            String logTime = timer.getGMTdateTime();
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
    
    private ArrayList<ArrayList<V1Component>> createV1ComponentsArr(ArrayList<String> v1FilePaths) {
        
        ArrayList<ArrayList<V1Component>> v1ComponentsArr = new ArrayList();
        
        for (String v1FilePath : v1FilePaths) {
            File v1File = new File(v1FilePath);
            
            if (!v1File.exists())
                continue;
            
            SmFile v1SmFile = new SmFile(v1File);
            ArrayList<V1Component> v1Components = createV1Components(v1SmFile);
            v1ComponentsArr.add(v1Components);
        }
        
        return v1ComponentsArr;
    }
   
    /**
     * Processes the specified V1 file. If the start and stop trim counts are zero,
     * the V1 file is kept unchanged. Otherwise, the file is replaced using the 
     * specified start datetime and acceleration data. 
     * @param v1FilePath pathname of V1 file
     * @param startDateTime starting DateTime
     * @param accArr acceleration data array
     * @param logList array for storing log entries
     * @param trashDir location of trash directory
     * @return true if V1 file processing was successful or false otherwise.
     */
    private boolean createV1File(String v1FilePath, DateTime startDateTime, double[] accArr,
        ArrayList<String> logList, String trashDir) {
        try {
            // Convert DateTime to ZonedDateTime.
            /*
            ZonedDateTime zdtStartDateTime = ZonedDateTime.of(
                startDateTime.getYear(),
                startDateTime.getMonthOfYear(),
                startDateTime.getDayOfMonth(),
                startDateTime.getHourOfDay(),
                startDateTime.getMinuteOfHour(),
                startDateTime.getSecondOfMinute(),
                startDateTime.getMillisOfSecond() * 1_000_000,
                ZoneId.of(startDateTime.getZone().getID(), ZoneId.SHORT_IDS));
            */
            
            ZonedDateTime zdtStartDateTime = startDateTime.toGregorianCalendar().toZonedDateTime();
            
            java.time.format.DateTimeFormatter zdtDateTimeFormatter = 
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSSS");
            
            // Get V1 File object.
            File v1File = new File(v1FilePath);
            
            /*
            SmTimeFormatter timer = new SmTimeFormatter();
            String logTime = timer.getGMTdateTime();
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(v1File,logTime,new File(logDir));
            queue.readInFile(v1File);
            queue.parseVFile(UNCORACC);

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();
            COSMOScontentFormat rec = smList.get(0);
            V1Component v1Component = (V1Component)rec;
            double[] points = v1Component.getDataArray();
            
            SmCore.addMsgToStatusViewer("points: " + points.length);
            */
            
            // Get original record attributes.
            SmFile smFile = new SmFile(v1File);
            SmRec smRec = smFile.getSmRecs().get(0);
            
            DateTime origStartDateTime = smRec.getStartDateTime();
            DateTime origStopDateTime = smRec.getEndDateTime();
            int origNumPts = smRec.getSmPoints().size();
            double origDurationMs = smRec.getDurationMs();
            long origDeltaT = (long)smRec.getDeltaT();
            
            // Get new record attributes.
            double newDurationMs = (accArr.length-1)*smRec.getDeltaT();
            Duration newDuration = new Duration((long)newDurationMs);
            DateTime newStopDateTime = startDateTime.plus(newDuration);
            
            Duration durStart = new Duration(origStartDateTime,startDateTime);
            Duration durStop = new Duration(newStopDateTime,origStopDateTime);
            
            //int startTrimCnt = Math.round(durStart.getMillis() / origDeltaT);
            //int stopTrimCnt = Math.round(durStop.getMillis() / origDeltaT);
            int startTrimCnt = (int)(durStart.getMillis() / origDeltaT);
            int stopTrimCnt = (int)(durStop.getMillis() / origDeltaT);
            int totalTrimCnt = startTrimCnt + stopTrimCnt;
            int remainPtCnt = origNumPts - totalTrimCnt;
            
            boolean adjustTrimCntNeeded = remainPtCnt != accArr.length;
            
            // Adjust counts, if necessary, so that remaining point count
            // will equal acceleration array length.
            if (adjustTrimCntNeeded) {
                stopTrimCnt = origNumPts - accArr.length - startTrimCnt;
                totalTrimCnt = startTrimCnt + stopTrimCnt;
                remainPtCnt = origNumPts - totalTrimCnt;
            }
            
            // Log processing details.
            addToLogList(logList,"Original Record Attributes:");
            addToLogList(logList,"  Orig Start DateTime: " + dateTimeFormatter.print(origStartDateTime));
            addToLogList(logList,"  Orig Stop DateTime: " + dateTimeFormatter.print(origStopDateTime));
            addToLogList(logList,"  Orig Point Count: " + origNumPts);
            addToLogList(logList,"  Orig Duration (ms): " + origDurationMs);
            addToLogList(logList,"New Record Attributes:");
            addToLogList(logList,"  New Start DateTime: " + dateTimeFormatter.print(startDateTime) +
                " (ZDT: " + zdtStartDateTime.format(zdtDateTimeFormatter) + ")");
            addToLogList(logList,"  New Stop DateTime: " + dateTimeFormatter.print(newStopDateTime));
            addToLogList(logList,"  New Point Count: " + accArr.length);
            addToLogList(logList,"  New Duration (ms): " + newDurationMs);
            addToLogList(logList,"Processing Details");
            addToLogList(logList,"  DeltaT (ms): " + smRec.getDeltaT());
            addToLogList(logList,"  Trimmed Start Time Difference (ms): " + durStart.getMillis());
            addToLogList(logList,"  Trimmed Stop Time Difference (ms): " + durStop.getMillis());
            addToLogList(logList,"  Trim Count Adjustment Needed: " + adjustTrimCntNeeded);
            addToLogList(logList,"  Start DateTime Trim Count: " + startTrimCnt);
            addToLogList(logList,"  Stop DateTime Trim Count: " + stopTrimCnt);
            addToLogList(logList,"  Total Trim Count: " + totalTrimCnt);
            addToLogList(logList,"  Remaining Point Count: " + remainPtCnt);
            
            if ((stopTrimCnt == 0 && startTrimCnt == 0)) {
                addToLogList(logList,"File " + v1FilePath + " unchanged.");
            }
            else {
                // Form product output directory.
                String eventsRootDir = SmCore.extractEventsRootDir(v1FilePath);
                String event = SmCore.extractEventName(v1FilePath);
                String station = SmCore.extractStationName(v1FilePath);

                File outputDir = SmCore.inTroubleFolder(v1FilePath) ?
                    Paths.get(eventsRootDir,event,station,SmGlobal.TROUBLE).toFile() :
                    Paths.get(eventsRootDir,event,station).toFile();

                TextFileReader reader = new TextFileReader(v1File);
                String[] contents = reader.readInTextFile();

                // Create V1Component object using original V1 file, then
                // update its accleration data array.
                V1Component v1Component = new V1Component(UNCORACC);
                v1Component.setFileName(v1File.getName());
                v1Component.loadComponent(0, contents);
                v1Component.updateArray(accArr, zdtStartDateTime, startTrimCnt, stopTrimCnt);

                // Write out products.
                SmProductGUI smProductGUI = new SmProductGUI();
                smProductGUI.updateDirectoriesGUI(outputDir);
                smProductGUI.addProduct(v1Component, "V1");
                smProductGUI.writeOutProducts(trashDir);

                /*
                String[] logItems = smProductGUI.writeOutProducts(trashDir);
                for (String logItem : logItems) {
                    addToLogList(logList,logItem);
                }
                addToLogList(logList,"");
                */
                
                addToLogList(logList,"File " + v1FilePath + " replaced.");
            }
        }
        catch (IOException | FormatException | SmException ex ) {
        //catch(Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            
            return false;
        }
        
        return true;
    }
    
    private void setTextFieldPropertyListeners(boolean add) {
        if (add) {
            this.ftxtStartTime.addPropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtStopTime.addPropertyChangeListener("value",textFieldPropertyListener);
        }
        else {
            this.ftxtStartTime.removePropertyChangeListener("value",textFieldPropertyListener);
            this.ftxtStopTime.removePropertyChangeListener("value",textFieldPropertyListener);
        }
    }
    
    private void setRadioButtonListeners(boolean add) {
        if (add) {
            this.rbtnStartTime.addItemListener(rbtnTypeListener);
            this.rbtnStopTime.addItemListener(rbtnTypeListener);
        }
        else {
            this.rbtnStartTime.removeItemListener(rbtnTypeListener);
            this.rbtnStopTime.removeItemListener(rbtnTypeListener);
        }
    }
    
    private void selectChartView(int selectedIndx) {
        for (int i=0; i<smChartViews.size(); i++) {
            JPanel smChartView = smChartViews.get(i);
            if (i==selectedIndx) {
                smChartView.setBorder(BorderFactory.createLineBorder(new Color(0,0,128), 3));
            }
            else {
                smChartView.setBorder(null);
            }
        }
    }
    
    private SmChartView getChartView(int chartViewIndx) {
        if (smChartViews != null) {
            if (chartViewIndx >= 0)
                return smChartViews.get(chartViewIndx);
            else
                return null;
        }
        
        return null;
    }
    
    private void selectAttributesTableRow(int selectedIndx) {
        if (selectedIndx == -1)
            return;
        
        this.tblAttributes.setRowSelectionInterval(selectedIndx, selectedIndx);
    }
    
    private void updateNavigationControls() {
        try {
            // Reset navigation controls to defaults.
            btnNavFirst.setEnabled(false);
            btnNavPrevious.setEnabled(false);
            btnNavNext.setEnabled(false);
            btnNavLast.setEnabled(false);
            lblNavDisplay.setText("");
            
            if (traceSets.isEmpty())
                return;
            
            if (traceSets.size() > 1) {
                if (traceSetsIndx == 0) {
                    btnNavFirst.setEnabled(false);
                    btnNavPrevious.setEnabled(false);
                    btnNavNext.setEnabled(true);
                    btnNavLast.setEnabled(true);
                }
                else if (traceSetsIndx > 0 && traceSetsIndx < traceSets.size()-1) {
                    btnNavFirst.setEnabled(true);
                    btnNavPrevious.setEnabled(true);
                    btnNavNext.setEnabled(true);
                    btnNavLast.setEnabled(true);
               }
               else if (traceSetsIndx == traceSets.size()-1) {
                    btnNavFirst.setEnabled(true);
                    btnNavPrevious.setEnabled(true);
                    btnNavNext.setEnabled(false);
                    btnNavLast.setEnabled(false);
               }
            }
            
            lblNavDisplay.setText("(" + 
                (traceSetsIndx+1) + " of " + traceSets.size() + 
                ") " + traceSets.get(traceSetsIndx).getVxChartsBinGroup().getGroupName());
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private void updateChartViewer() {
        try {
            // Clear chart viewer.
            pnlViewer.removeAll();
            
            // Clear smChartViews array list.
            smChartViews.clear();
            
            if (traceSets.isEmpty())
                return;
            
            // Get current VxChartsBinGroup.
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
            
            // Get V1 and V2 chart bins.
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();

            // Add V1 charts to list, if any.
            if (v1ChartsBin != null)
            {
                if (btnAcceleration.isSelected() || btnAll.isSelected())
                {
                    for (JPanel pnl : v1ChartsBin.getChartsSeismicAcc()) {
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                }
            }

            // Add V2 charts to list, if any.
            if (v2ChartsBin != null)
            {
                if (btnAcceleration.isSelected())
                {
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicAcc()) {
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    } 
                }
                else if (btnVelocity.isSelected())
                {
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicVel()) {
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                }
                else if (btnDisplacement.isSelected())
                {
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicDis()){
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                }
                else if (btnAll.isSelected())
                {
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicAcc()){
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicVel()){
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                    for (JPanel pnl : v2ChartsBin.getChartsSeismicDis()) {
                        if (pnl instanceof SmChartView) {
                            smChartViews.add((SmChartView)pnl);
                        }   
                    }
                }
            }
            
            // Set chart view properties.
            setChartViewProperties(smChartViews);
            
            // Add header to chart viewer.
            pnlViewer.add(vXChartsBinGroup.createHeader());
            
            // Assign name and set edit mode for each chart, then add to chart viewer.
            for (int i=0; i < smChartViews.size(); i++) {
                SmChartView scv = smChartViews.get(i);
                scv.setName("smChartView_" + (i));
                scv.setEditMode(true);
                
                pnlViewer.add(scv);
            }
            
            // Call panel viewer's updateUI to refresh control.
            pnlViewer.updateUI();
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private void updateAttributesTable() {
        try {
            // Clear attributes table.
            DefaultTableModel model = (DefaultTableModel)tblAttributes.getModel();
            model.setRowCount(0);
            
            if (traceSets.isEmpty())
                return;
            
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
            
            // Get V1 and V2 chart bins.
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
            
            // Add V1 charts, if any.
            if (v1ChartsBin != null)
            {
                if (btnAcceleration.isSelected() || btnAll.isSelected())
                {
                    for (JPanel smChartView : v1ChartsBin.getChartsSeismicAcc()) {
                        addToAttributesTable(smChartView);
                    }
                }
            }
            
            // Add V2 charts, if any.
            if (v2ChartsBin != null)
            {
                if (btnAcceleration.isSelected())
                {
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicAcc()) {
                        addToAttributesTable(smChartView);
                    }
                }
                else if (btnVelocity.isSelected())
                {
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicVel()) {
                        addToAttributesTable(smChartView);
                    }
                }
                else if (btnDisplacement.isSelected())
                {
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicDis()) {
                        addToAttributesTable(smChartView);
                    }
                }
                else if (btnAll.isSelected())
                {
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicAcc()) {
                        addToAttributesTable(smChartView);
                    }
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicVel()) {
                        addToAttributesTable(smChartView);
                    }
                    for (JPanel smChartView : v2ChartsBin.getChartsSeismicDis()) {
                        addToAttributesTable(smChartView);
                    }
                }
            }
        }
        catch (Exception ex) {
            throw ex;
        }
    }
    
    private void updateControlPanel() {
        try {
            
            if (traceSets.isEmpty())
                return;
            
            setTextFieldPropertyListeners(false);
            
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            
            VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();

            // Get V1 and V2 chart bins.
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();

            // Set earliest and latest start and stop times.
            ArrayList<String> filePaths = new ArrayList<>();
            if (v1ChartsBin != null)
                filePaths.addAll(v1ChartsBin.getFilePaths());
            if (v2ChartsBin != null)
                filePaths.addAll(v2ChartsBin.getFilePaths());
            
            earliestStartTime = SmUtils.getEarliestStartTime(filePaths);
            latestStartTime = SmUtils.getLatestStartTime(filePaths);
            earliestStopTime = SmUtils.getEarliestStopTime(filePaths);
            latestStopTime = SmUtils.getLatestStopTime(filePaths);
            
            if (earliestStartTime.isEqual(latestStartTime)) {
                lblEarliestStartTime.setForeground(colorEqual);
                lblLatestStartTime.setForeground(colorEqual);
            }
            else {
                lblEarliestStartTime.setForeground(colorUnequal);
                lblLatestStartTime.setForeground(colorUnequal);
            }
            
            if (earliestStopTime.isEqual(latestStopTime)) {
                lblEarliestStopTime.setForeground(colorEqual);
                lblLatestStopTime.setForeground(colorEqual);
            }
            else {
                lblEarliestStopTime.setForeground(colorUnequal);
                lblLatestStopTime.setForeground(colorUnequal);
            }
            
            //Font font = new Font(lblEarliestStopTime.getFont().getFontName(),0,
                //lblEarliestStopTime.getFont().getSize());
            
            lblEarliestStartTime.setText("Earliest Start Time: " + dateTimeFormatter.print(earliestStartTime));
            lblLatestStartTime.setText("Latest Start Time: " + dateTimeFormatter.print(latestStartTime));
            lblEarliestStopTime.setText("Earliest Stop Time: " + dateTimeFormatter.print(earliestStopTime));
            lblLatestStopTime.setText("Latest Stop Time: " + dateTimeFormatter.print(latestStopTime));
                
            int curTraceIndx = traceSet.getCurTraceIndx();
            
            if (curTraceIndx == -1) {
                ftxtStartTime.setValue(null);
                ftxtStopTime.setValue(null);
            }
            else {
                // Get data cursor for current chart view.
                SmChartView scv = getChartView(curTraceIndx);
                SmDataCursor sdc = scv.getSmDataCursor();
                
                // Retrieve start and stop values based on corresponding markers, if any.
                Marker startTimeMarker = 
                    sdc.getMarker(new Color(SmPreferences.SmTrimTool.getStartTimeMarkerColor()));
                Marker stopTimeMarker = 
                    sdc.getMarker(new Color(SmPreferences.SmTrimTool.getStopTimeMarkerColor()));
                
                /*
                if (startTimeMarker != null) 
                    ftxtStartTime.setValue(startTimeMarker.getLocation().getX());
                else {
                    if (scv instanceof SingleChartView) {
                        SmSeries smSeries = ((SingleChartView)scv).getSmSeries();
                        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                        ftxtStartTime.setValue(smPoints.get(0).getX());
                    }
                    else
                        ftxtStartTime.setValue(null);
                }
                    
                if (stopTimeMarker != null) 
                    ftxtStopTime.setValue(stopTimeMarker.getLocation().getX());
                else {
                    if (scv instanceof SingleChartView) {
                        SmSeries smSeries = ((SingleChartView)scv).getSmSeries();
                        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                        ftxtStopTime.setValue(smPoints.get(smPoints.size()-1).getX());
                    }
                    else
                        ftxtStopTime.setValue(null);
                }
                */
                
                if (startTimeMarker != null) 
                    ftxtStartTime.setValue(startTimeMarker.getLocation().getX());
                else if (traceSet.getCurTrimmedStartTime() >= 0) {
                    ftxtStartTime.setValue(traceSet.getCurTrimmedStartTime());
                }
                else {
                    if (scv instanceof SingleChartView) {
                        SmSeries smSeries = ((SingleChartView)scv).getSmSeries();
                        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                        ftxtStartTime.setValue(smPoints.get(0).getX());
                    }
                    else
                        ftxtStartTime.setValue(null);
                }
                    
                if (stopTimeMarker != null) 
                    ftxtStopTime.setValue(stopTimeMarker.getLocation().getX());
                else if (traceSet.getCurTrimmedStopTime() >= 0) {
                    ftxtStopTime.setValue(traceSet.getCurTrimmedStopTime());
                }
                else {
                    if (scv instanceof SingleChartView) {
                        SmSeries smSeries = ((SingleChartView)scv).getSmSeries();
                        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                        ftxtStopTime.setValue(smPoints.get(smPoints.size()-1).getX());
                    }
                    else
                        ftxtStopTime.setValue(null);
                }
            }
        }
        catch (Exception ex) {
            throw ex;
        }
        finally {
            setTextFieldPropertyListeners(true);
        }
    }
    
    private void addToAttributesTable(JPanel panel) {
        
        DefaultTableModel model = (DefaultTableModel)tblAttributes.getModel();
        
        /*
        String eventDate = smRec.getEventDateTime();

        SimpleDateFormat formatOrig = new SimpleDateFormat("E MMM dd, yyyy HH:mm");
        SimpleDateFormat formatNew = new SimpleDateFormat("MM/dd/yyyy");

        Date date = formatOrig.parse(eventDate);

        sbEventDesc.append(formatNew.format(date));
        
        final double startTimeSec = rec.getRealHeaderValue(29);
        final int startSec = (int)Math.floor(startTimeSec);
        final int startMs = (int) Math.round((startTimeSec-Math.floor(startTimeSec))*1000);
        */
        
        /*
        String srcDate = "Sun Aug 24, 2014 10:20:15";
        
        DateTimeFormatter fmtOrig = DateTimeFormat.forPattern("E MMM dd, yyyy HH:mm:ss");
        DateTimeFormatter fmtNew = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
        
        //DateTime dt = new DateTime();
        DateTime dt = new DateTime(fmtOrig.parseDateTime(srcDate));
        
        String strOrig = fmtOrig.print(dt);
        String strNew = fmtNew.print(dt);
        */
        
        if (panel instanceof SingleChartView) {
            SingleChartView scv = (SingleChartView)panel;
            SmSeries smSeries = scv.getSmSeries();
            String filePath = smSeries.getTag().toString();

            SmFile smFile = new SmFile(new File(filePath));
            SmRec smRec = smFile.getSmRecs().get(0);

            String fileDataType = smFile.getFileDataType();
            String dataType = "";
            
            if (fileDataType.equals(RAWACC))
                dataType = SmGlobal.CosmosFileType.V0.toString();
            else if (fileDataType.equals(UNCORACC))
                dataType = SmGlobal.CosmosFileType.V1.toString();
            else if (fileDataType.equals(CORACC))
                dataType = SmGlobal.CosmosFileType.V2.toString() +
                    "." + SmGlobal.CosmosV2DataType.ACC.toString();
            else if (fileDataType.equals(VELOCITY))
                dataType = SmGlobal.CosmosFileType.V2.toString() +
                    "." + SmGlobal.CosmosV2DataType.VEL.toString();
            else if (fileDataType.equals(DISPLACE))
                dataType = SmGlobal.CosmosFileType.V2.toString() +
                    "." + SmGlobal.CosmosV2DataType.DIS.toString();
            
            String channel = smRec.getChannel();
            int numPts = smRec.getSmPoints().size();
            double deltaT_Sec = smRec.getDeltaT()*MSEC_TO_SEC;
            double durationSec = smRec.getDurationMs()*MSEC_TO_SEC;
            DateTime startDateTime = smRec.getStartDateTime();
            DateTime stopDateTime = smRec.getEndDateTime();
            
            model.addRow(new Object[]{dataType,channel,numPts,deltaT_Sec,durationSec,
                dateTimeFormatter.print(startDateTime),dateTimeFormatter.print(stopDateTime)});
        }
    }
    
    private void setChartViewerVertScroll(int selectedIndx) {
  
        JComponent comp = smChartViews.get(selectedIndx);
        
        int headerHeight = pnlViewer.getComponents()[0].getHeight();
        int totalHeight = headerHeight;

        for (JPanel smChartView : smChartViews) {
            totalHeight += smChartView.getHeight();
        }

        // The term, (panel.getHeight()*.15), is a fudge factor.
        int posSelectedPanel = headerHeight;
        for (int i=0; i<selectedIndx;i++) {
            JPanel smChartView = smChartViews.get(i);
            posSelectedPanel += smChartView.getHeight() - (smChartView.getHeight()*.15);
        }

        scrlpaneViewer.getVerticalScrollBar().setMaximum(totalHeight);
        scrlpaneViewer.getVerticalScrollBar().setValue(posSelectedPanel);

        comp.requestFocus();
    }
    
    private void setChartViewProperties(ArrayList<SmChartView> chartViews) {
        if (chartViews == null || chartViews.isEmpty())
            return;
        
        JFormattedTextField field;
        SmPreferences.MarkerStyle markerStyle;
        Color color;
        double width;
        
        if (this.rbtnStartTime.isSelected()) {
            field = this.ftxtStartTime;
            markerStyle = SmPreferences.SmTrimTool.getStartTimeMarkerStyle();
            color = new Color(SmPreferences.SmTrimTool.getStartTimeMarkerColor());
            width = SmPreferences.SmTrimTool.getStartTimeMarkerWidth();
        }
        else {
            field = this.ftxtStopTime;
            markerStyle = SmPreferences.SmTrimTool.getStopTimeMarkerStyle();
            color = new Color(SmPreferences.SmTrimTool.getStopTimeMarkerColor());
            width = SmPreferences.SmTrimTool.getStopTimeMarkerWidth();
        }
                    
        for (SmChartView smChartView : chartViews) {    
            smChartView.setBoundedTextField(field);
            smChartView.setSmDataCursorStyle(markerStyle);
            smChartView.setSmDataCursorColor(color);
            smChartView.setSmDataCursorWidth(width);
        }
    }
    
    private boolean validateInputs() {
        boolean result = true;
        StringBuilder msg = new StringBuilder();
        
        if (this.ftxtStartTime.getValue() == null)
            msg.append("  -Missing Start Time value\n");
        else if (!isDouble(this.ftxtStartTime.getValue().toString()))
            msg.append("  -Invalid Start Time value\n");
        
        if (this.ftxtStopTime.getValue() == null)
            msg.append("  -Missing Stop Time value\n");
        else if (!isDouble(this.ftxtStopTime.getValue().toString()))
            msg.append("  -Invalid Stop Time value\n");
        
        if (msg.length() > 0) { 
            result = false;
            
            JOptionPane.showMessageDialog(this,"Error(s):\n" + msg.toString(),"Error",
                JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error(s):\n" + msg.toString());
            
            return result;
        }
        
        TraceSet traceSet = traceSets.get(traceSetsIndx);
        VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
        
        V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
        V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();

        double startTime = Double.parseDouble(ftxtStartTime.getValue().toString());
        double stopTime = Double.parseDouble(ftxtStopTime.getValue().toString());

        if (v1ChartsBin != null) {
            ArrayList<SmSeries> v1SmSeriesSeismicList = v1ChartsBin.getSmSeriesSeismicList();

            double minDeltaT_Sec = v1ChartsBin.getMinDeltaT()*MSEC_TO_SEC;
            
            boolean invalid = false;
            
            for (SmSeries smSeries : v1SmSeriesSeismicList) {
                
                double firstPoint = smSeries.getSmPoints().get(0).getX();
                double lastPoint = smSeries.getSmPoints().get(smSeries.getSmPoints().size()-1).getX();

                if (startTime < firstPoint && Math.abs(firstPoint-startTime) > minDeltaT_Sec) {
                    msg.append("  -Start Time cannot be set earlier than currently set start time\n");
                    SmCore.addMsgToStatusViewer("Start time: " + startTime + " : " + firstPoint);
                    invalid = true;
                }

                if (stopTime > lastPoint && Math.abs(stopTime-lastPoint) > minDeltaT_Sec) {
                    msg.append("  -Stop Time cannot be set later than currently set stop time\n");
                    SmCore.addMsgToStatusViewer("Stop time: " + stopTime + " : " + lastPoint);
                    invalid = true;
                }
                
                if (invalid)
                    break;
            }
        }

        if (v2ChartsBin != null) {
            ArrayList<SmSeries> v2SmSeriesSeismicList = v2ChartsBin.getSmSeriesSeismicList();
            
            double minDeltaT_Sec = v2ChartsBin.getMinDeltaT()*MSEC_TO_SEC;
            
            boolean invalid = false;
            
            for (SmSeries smSeries : v2SmSeriesSeismicList) {
                
                double firstPoint = smSeries.getSmPoints().get(0).getX();
                double lastPoint = smSeries.getSmPoints().get(smSeries.getSmPoints().size()-1).getX();

                if (startTime < firstPoint && Math.abs(firstPoint-startTime) > minDeltaT_Sec) {
                    SmCore.addMsgToStatusViewer("Start time: " + startTime + " : " + firstPoint);
                    msg.append("  -Start Time cannot be set earlier than currently set start time\n");
                    invalid = true;
                }

                if (stopTime > lastPoint && Math.abs(stopTime-lastPoint) > minDeltaT_Sec) {
                    SmCore.addMsgToStatusViewer("Stop time: " + stopTime + " : " + lastPoint);
                    msg.append("  -Stop Time cannot be set later than currently set stop time\n");
                    invalid = true;
                }
                
                if (invalid)
                    break;
            }
        }
        
        if (msg.length() > 0) {
            result = false;
             
            JOptionPane.showMessageDialog(this,"Error(s):\n" + msg.toString(),"Error",
                JOptionPane.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error(s):\n" + msg.toString());
        }
            
        return result;
    }
    
    private void runGraphs() {
        try {
            final SmTemplate smTemplate = SmCore.getSelectedSmTemplate();
            
            final String chartAPI = SmGlobal.SM_CHARTS_API_QCCHART2D;
            
            final boolean adjustPoints = true;
            
            if (chartAPI == null)
            {
                showMessage("Error","Chart API is unknown.",
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                return;
            }

            if (eventNodes.isEmpty())
            {
                showMessage("Error","Event list is empty.",
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                return;
            }
            
            ProgressHandle handle = ProgressHandle.createHandle("Running Graphs ...");
            RunGraphsByGroupTask task = new RunGraphsByGroupTask(chartAPI, 
                eventNodes, smTemplate, adjustPoints, this, handle);
            task.execute();
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
        }
    }
    
    private void autoTrim() {
        try {
            
            if (traceSets.isEmpty())
                return;
            
            setTextFieldPropertyListeners(false);
            
            Duration diffStartTime = new Duration(earliestStartTime,latestStartTime);
            double diffStartTimeMs = diffStartTime.getMillis();
            
            Duration diffStopTime = new Duration(earliestStartTime,earliestStopTime);
            double diffStopTimeMs = diffStopTime.getMillis();
            
            ftxtStartTime.setValue(diffStartTimeMs*MSEC_TO_SEC);
            ftxtStopTime.setValue(diffStopTimeMs*MSEC_TO_SEC);
            
            /*
            SmCore.addMsgToStatusViewer("Earliest StartTime: " + dateTimeFormatter.print(earliestStartTime));
            SmCore.addMsgToStatusViewer("Latest StartTime: " + dateTimeFormatter.print(latestStartTime));
            SmCore.addMsgToStatusViewer("Earliest StopTime: " + dateTimeFormatter.print(earliestStopTime));
            SmCore.addMsgToStatusViewer("diff start: " + Double.parseDouble(ftxtStartTime.getValue().toString()));
            SmCore.addMsgToStatusViewer("diff stop: " + Double.parseDouble(ftxtStopTime.getValue().toString()));
            SmCore.addMsgToStatusViewer("");
            */
            
            trim();
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        finally {
            setTextFieldPropertyListeners(true);
        }
    }
    
    private void trim() {
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            if (traceSets.isEmpty() || !validateInputs())
                return;
            
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            
            VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
            
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
            
            double startTimeSec = Double.parseDouble(ftxtStartTime.getValue().toString());
            double stopTimeSec = Double.parseDouble(ftxtStopTime.getValue().toString());
            double startTimeMs = startTimeSec / MSEC_TO_SEC;
            double stopTimeMs = stopTimeSec / MSEC_TO_SEC;
            double timeRangeMs = stopTimeMs - startTimeMs;
            
            if (v1ChartsBin != null) {
                ArrayList<SmSeries> v1SmSeriesSeismicList = v1ChartsBin.getSmSeriesSeismicList();
                
                ArrayList<SmSeries> v1SmSeriesSeismicListAdj = new ArrayList<>();
                //ArrayList<SmSeries> v1SmSeriesSpectralListAdj = new ArrayList<>();
                //double minDeltaT = v1ChartsBin.getMinDeltaT();
                
                for (SmSeries smSeries : v1SmSeriesSeismicList) {
                    String filePath = smSeries.getTag().toString();
                    SmFile smFile = new SmFile(new File(filePath));
                    ArrayList<SmRec> smRecs = smFile.getSmRecs();
                    double deltaT = smRecs.get(0).getDeltaT();
                    //long expectedNumPoints = Math.round(timeRangeMs / deltaT) + 1;
                    long expectedNumPoints = (long)((timeRangeMs / deltaT) + 1);
                    long actualNumPoints = 0;
                    
                    ArrayList<SmPoint> smPoints = new ArrayList<>();

                    //double firstPoint = smSeries.getSmPoints().get(0).getX();
                    //double lastPoint = smSeries.getSmPoints().get(smSeries.getSmPoints().size()-1).getX();

                    /*
                    // Add points to pad time before the first point.
                    if (startTime < firstPoint) {
                        double curStartTime = startTime;

                        while (curStartTime < firstPoint) {
                            smPoints.add(new SmPoint(curStartTime,0));
                            curStartTime += minDeltaT*MSEC_TO_SEC;
                        }
                    }
                    */
                    
                    for (SmPoint smPoint : smSeries.getSmPoints()) {
                        double x = smPoint.getX();
                        double y = smPoint.getY();

                        if (actualNumPoints >= expectedNumPoints)
                            break;
                        
                        if (x >= startTimeSec && x <= stopTimeSec) {
                            smPoints.add(new SmPoint(x,y));
                            actualNumPoints++;
                        }
                    }
                    
                    // Add points to array, if needed, to ensure array has the
                    // expected number of points.
                    SmPoint lastPoint = smPoints.get(smPoints.size()-1);
                    double xVal = lastPoint.getX();
                    
                    while (actualNumPoints < expectedNumPoints)
                    {
                        xVal += deltaT * MSEC_TO_SEC;
                        smPoints.add(new SmPoint(xVal,lastPoint.getY()));
                        actualNumPoints++;
                    }
                    
                    /*
                    // Add points to pad time after the last point.
                    if (stopTime > lastPoint) {
                        double curStopTime = stopTime;

                        while (curStopTime > lastPoint) {
                            smPoints.add(new SmPoint(curStopTime,0));
                            curStopTime -= minDeltaT*MSEC_TO_SEC;
                        }
                    }
                    */
                    
                    /*
                    // Create spectral points data.
                    ArrayList<SmPoint> smPointsFFT = SmRec.createSmPointsFFT(
                        smPoints, minDeltaT, MSEC_TO_SEC);

                    ArrayList<SmPoint> smPointsFFT_Adj = new ArrayList<>();

                    for (int i=1; i<smPointsFFT.size()/2; i++) //skip 1st point, frequency=0Hz
                    {
                        SmPoint smPointFFT = smPointsFFT.get(i);
                        smPointsFFT_Adj.add(new SmPoint(smPointFFT.getX(),
                            smPointFFT.getY()));
                    }
                    */

                    v1SmSeriesSeismicListAdj.add(new SmSeries(smPoints,smSeries.getDataParmCode(),
                        smSeries.getTitle(),smSeries.getDecription(),smSeries.getColor(),
                        smSeries.getTag()));

                    /*
                    if (!smPointsFFT_Adj.isEmpty()) {
                        v1SmSeriesSpectralListAdj.add(new SmSeries(smPointsFFT_Adj,smSeries.getDataParmCode(),
                        smSeries.getTitle(),smSeries.getDecription(),smSeries.getColor(),
                        smSeries.getTag()));
                    }
                    */
                }
                
                // Update charts bin with new lists.
                v1ChartsBin.setSmSeriesSeismicList(v1SmSeriesSeismicListAdj);
                //v1ChartsBin.setSmSeriesSpectralList(v1SmSeriesSpectralListAdj);
                
                // Retrieve xy bounds for acceleration data type.
                XYBounds v1BndsSeismicAcc = v1ChartsBin.getXYBoundsSeismicAcc();
                
                // Update charts.
                v1ChartsBin.updateCharts(v1BndsSeismicAcc);
            }
           
            if (v2ChartsBin != null) {
                ArrayList<SmSeries> v2SmSeriesSeismicList = v2ChartsBin.getSmSeriesSeismicList();
                
                ArrayList<SmSeries> v2SmSeriesSeismicListAdj = new ArrayList<>();
                //ArrayList<SmSeries> v2SmSeriesSpectralListAdj = new ArrayList<>();
                
                //double minDeltaT = v2ChartsBin.getMinDeltaT();
                
                for (SmSeries smSeries : v2SmSeriesSeismicList) {
                    String filePath = smSeries.getTag().toString();
                    SmFile smFile = new SmFile(new File(filePath));
                    ArrayList<SmRec> smRecs = smFile.getSmRecs();
                    double deltaT = smRecs.get(0).getDeltaT();
                    //long expectedNumPoints = Math.round(timeRangeMs / deltaT) + 1;
                    long expectedNumPoints = (long)((timeRangeMs / deltaT) + 1);
                    long actualNumPoints = 0;
                    
                    ArrayList<SmPoint> smPoints = new ArrayList<>();

                    /*
                    double firstPoint = smSeries.getSmPoints().get(0).getX();
                    double lastPoint = smSeries.getSmPoints().get(smSeries.getSmPoints().size()-1).getX();

                    // Add points to pad time before the first point.
                    if (startTime < firstPoint) {
                        double curStartTime = startTime;

                        while (curStartTime < firstPoint) {
                            smPoints.add(new SmPoint(curStartTime,0));
                            curStartTime += minDeltaT*MSEC_TO_SEC;
                        }
                    }
                    */

                    for (SmPoint smPoint : smSeries.getSmPoints()) {
                        double x = smPoint.getX();
                        double y = smPoint.getY();

                        if (actualNumPoints >= expectedNumPoints)
                            break;
                        
                        if (x >= startTimeSec && x <= stopTimeSec) {
                            smPoints.add(new SmPoint(x,y));
                        }
                    }
                    
                    // Add points to array, if needed, to ensure array has the
                    // expected number of points.
                    SmPoint lastPoint = smPoints.get(smPoints.size()-1);
                    double xVal = lastPoint.getX();
                    
                    while (actualNumPoints < expectedNumPoints)
                    {
                        xVal += deltaT * MSEC_TO_SEC;
                        smPoints.add(new SmPoint(xVal,lastPoint.getY()));
                        actualNumPoints++;
                    }

                    /*
                    // Add points to pad time after the last point.
                    if (stopTime > lastPoint) {
                        double curStopTime = stopTime;

                        while (curStopTime > lastPoint) {
                            smPoints.add(new SmPoint(curStopTime,0));
                            curStopTime -= minDeltaT*MSEC_TO_SEC;
                        }
                    }
                    */
                    
                    /*
                    // Create spectral points data.
                    ArrayList<SmPoint> smPointsFFT = SmRec.createSmPointsFFT(
                        smPoints, minDeltaT, MSEC_TO_SEC);

                    ArrayList<SmPoint> smPointsFFT_Adj = new ArrayList<>();

                    for (int i=1; i<smPointsFFT.size()/2; i++) //skip 1st point, frequency=0Hz
                    {
                        SmPoint smPointFFT = smPointsFFT.get(i);
                        smPointsFFT_Adj.add(new SmPoint(smPointFFT.getX(),
                            smPointFFT.getY()));
                    }
                    */

                    v2SmSeriesSeismicListAdj.add(new SmSeries(smPoints,smSeries.getDataParmCode(),
                        smSeries.getTitle(),smSeries.getDecription(),smSeries.getColor(),
                        smSeries.getTag()));

                    /*
                    if (!smPointsFFT_Adj.isEmpty() && smSeries.getDataParmCode() == 1) {
                        v2SmSeriesSpectralListAdj.add(new SmSeries(smPointsFFT_Adj,smSeries.getDataParmCode(),
                        smSeries.getTitle(),smSeries.getDecription(),smSeries.getColor(),
                        smSeries.getTag()));
                    }
                    */
                }
                
                // Update charts bin with new lists.
                v2ChartsBin.setSmSeriesSeismicList(v2SmSeriesSeismicListAdj);
                //v2ChartsBin.setSmSeriesSpectralList(v2SmSeriesSpectralListAdj);
                
                // Retrieve xy bounds for each data type.
                XYBounds v2BndsSeismicAcc = v2ChartsBin.getXYBoundsSeismicAcc();
                XYBounds v2BndsSeismicVel = v2ChartsBin.getXYBoundsSeismicVel();
                XYBounds v2BndsSeismicDis = v2ChartsBin.getXYBoundsSeismicDis();
                
                // Update charts.
                v2ChartsBin.updateCharts(v2BndsSeismicAcc,v2BndsSeismicVel,v2BndsSeismicDis);
            }
           
            // Add SmChartMouseListener to each chart.
            addSmChartMouseListeners(traceSetsIndx);
            
            // Update chart viewer and attributes table.
            updateChartViewer();
            updateAttributesTable();
            
            // Restore current chart view index.
            selectChartView(traceSet.getCurTraceIndx());
            selectAttributesTableRow(traceSet.getCurTraceIndx());
            
            // Update current start and stop DateTime variables for current TraceSet.
            DateTime origStartDateTime = traceSet.getOrigStartDateTime();
            //DateTime newStartDateTime = origStartDateTime.plus(Duration.millis(Math.round(startTimeMs)));
            //DateTime newStopDateTime = origStartDateTime.plus(Duration.millis(Math.round(stopTimeMs)));
            DateTime newStartDateTime = origStartDateTime.plus(Duration.millis((long)(startTimeMs)));
            DateTime newStopDateTime = origStartDateTime.plus(Duration.millis((long)(stopTimeMs)));
            
            traceSet.setCurStartDateTime(newStartDateTime);
            traceSet.setCurStopDateTime(newStopDateTime);
            
            // Update current trimmed start and stop time variables for current TraceSet.
            traceSet.setCurTrimmedStartTime(startTimeSec);
            traceSet.setCurTrimmedStopTime(stopTimeSec);
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        finally {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void reset() {
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Reset current TraceSet.
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            traceSet.reset();
            
            // Add SmChartMouseListener to traces in current TraceSet.
            addSmChartMouseListeners(traceSetsIndx);

            // Update chart viewer, attributes table, and control panel.
            updateChartViewer();
            updateAttributesTable();
            updateControlPanel();
            
            // Select current trace.
            selectChartView(traceSet.getCurTraceIndx());
            selectAttributesTableRow(traceSet.getCurTraceIndx());
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        finally {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void resetAll() {
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Reset all TraceSets.
            for (TraceSet traceSet : traceSets) {
                traceSet.reset();
            }
            
            // Add SmChartMouseListener for traces in every TraceSet.
            addSmChartMouseListeners();

            // Update chart viewer, attributes table, and control panel.
            updateChartViewer();
            updateAttributesTable();
            updateControlPanel();
            
            // Select current trace.
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            selectChartView(traceSet.getCurTraceIndx());
            selectAttributesTableRow(traceSet.getCurTraceIndx());
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
        finally {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void commit() {
        int response = JOptionPane.showOptionDialog(this, 
            "A commit will replace V1 files. Do you wish to proceed?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null,null,null);
        
        if (response == JOptionPane.NO_OPTION) 
            return;
        
        String logsDir = SmPreferences.General.getLogsDir();
        String trashDir = SmPreferences.General.getTrashDir();
        
        if (logsDir.isEmpty()) {
            response = JOptionPane.showOptionDialog(this, 
                "Logs directory not set. Log output will not be written out.\n" +
                "Do you wish to proceed?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, 
                null,null,null);

            if (response == JOptionPane.NO_OPTION) 
                return;
        }
    
        if (trashDir.isEmpty()) {
            response = JOptionPane.showOptionDialog(this, 
                "Trash directory not set. Removed files will not be recoverable.\n" +
                "Do you wish to proceed?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, 
                null,null,null);

            if (response == JOptionPane.NO_OPTION) 
                return;
        }
        
        try {
            SmCore.clearStatusViewer();
            
            //DecimalFormat formatter = new DecimalFormat("###,###.####");
            
            //double startTime = ((Number)this.ftxtStartTime.getValue()).doubleValue();
            //double stopTime = ((Number)this.ftxtStopTime.getValue()).doubleValue();
            
            SmTimeFormatter timer = new SmTimeFormatter();
            String timerStart = timer.getGMTdateTime();
            
            ArrayList<String> logList = new ArrayList<>();
            
            addToLogList(logList,"Trim tool commit processing started at " + timerStart);
            addToLogList(logList,"");
            //addToLogList(logList,"Inputs:");
            //addToLogList(logList,"Start Time (sec): " + decimalFormatter.format(startTime));
            //addToLogList(logList,"Stop Time (sec): " + decimalFormatter.format(stopTime));
            //addToLogList(logList,"");
            
            // Write out replacement V1 files.
            for (TraceSet traceSet : traceSets) {
                DateTime startDateTime = traceSet.getCurStartDateTime();
                VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
                
                addToLogList(logList,"Processing group " + vXChartsBinGroup.getGroupName() + " ...");
                addToLogList(logList,"");
                
                ArrayList<String> processedV1FilePaths = new ArrayList();
                
                V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
                V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
                
                if (v1ChartsBin != null) {
                    ArrayList<SmSeries> v1SmSeriesSeismicList = v1ChartsBin.getSmSeriesSeismicList();
                    for (SmSeries smSeries : v1SmSeriesSeismicList) {
                        String v1FilePath = smSeries.getTag().toString();
                        String fileType = SmCore.extractCosmosFileType(v1FilePath);
                        
                        if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString())) {
                            
                            // Check whether file has already been processed.
                            boolean processed = false;
                                
                            for (String processedV1FilePath : processedV1FilePaths) {
                                if (processedV1FilePath.equals(v1FilePath)) {
                                    processed = true;
                                    break;
                                }
                            }

                            if (!processed) {
                                ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                                double accArr[] = new double[smPoints.size()];
                                
                                for (int i=0; i < smPoints.size(); i++) {
                                    accArr[i] = smPoints.get(i).getY();
                                }
                        
                                addToLogList(logList,"Processing file " + v1FilePath + " ...");
                                
                                if (createV1File(v1FilePath, startDateTime, accArr, logList, trashDir)) {
                                    processedV1FilePaths.add(v1FilePath);
                                }
                                else {
                                    addToLogList(logList,"Processing of file " + v1FilePath + " failed.");
                                }
                                
                                addToLogList(logList,"");
                            }
                        }
                    }
                }
                
                if (v2ChartsBin != null) {
                    ArrayList<SmSeries> v2SmSeriesSeismicList = v2ChartsBin.getSmSeriesSeismicList();
                    for (SmSeries smSeries : v2SmSeriesSeismicList) {
                        String filePath = smSeries.getTag().toString();
                        String fileType = SmCore.extractCosmosFileType(filePath);
                    
                        if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                            File v2File = new File(filePath);
                            String v2Dir = v2File.getParent();
                            
                            String[] parts = v2Dir.split(Pattern.quote(File.separator));
                            if (parts.length < 4 || !parts[parts.length-1].matches("V[1,2]")) {
                                String msg = "File " + SmCore.extractFileName(filePath) +
                                    " is in folder that does not comply with expected folder structure.";
                                addToLogList(logList,"Invalid Folder Structure: " + msg);
                                return;
                            }

                            // Form V1 directory path name.
                            Path pathV2FilePath = v2File.toPath();
                            Path v1Dir = Paths.get(pathV2FilePath.getParent().getParent().toString(),
                                SmGlobal.CosmosFileType.V1.toString());

                            if (!Files.isDirectory(v1Dir))
                            {
                                addToLogList(logList,"No matching V1 directory found for V2 file " + 
                                    pathV2FilePath.toString());
                                return;
                            }

                            // Build V1 filename string based on V2 file.
                            String input = SmCore.extractFileName(filePath);
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
                            Path pathV1FilePath = Paths.get(v1Dir.toString(),v1FileName);
                            if (Files.exists(pathV1FilePath) && Files.isRegularFile(pathV1FilePath)) {
                                boolean processed = false;
                                
                                String v1FilePath = pathV1FilePath.toString();
                                
                                for (String processedV1FilePath : processedV1FilePaths) {
                                    if (processedV1FilePath.equals(v1FilePath)) {
                                        processed = true;
                                        break;
                                    }
                                }
                                
                                if (!processed) {
                                    
                                    ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                                    double accArr[] = new double[smPoints.size()];

                                    for (int i=0; i < smPoints.size(); i++) {
                                        accArr[i] = smPoints.get(i).getY();
                                    }
                                    
                                    addToLogList(logList,"Processing file " + v1FilePath + " ...");
                                
                                    if (createV1File(v1FilePath, startDateTime, accArr, logList, trashDir)) {
                                        processedV1FilePaths.add(v1FilePath);
                                    }
                                    else {
                                        addToLogList(logList,"Processing of file " + v1FilePath + " failed.");
                                    }
                                    
                                    addToLogList(logList,"");
                                }
                            }
                        }
                    }
                }
                
                addToLogList(logList,"Processing for group " + vXChartsBinGroup.getGroupName() + " completed.");
                addToLogList(logList,"");
            }
            
            // Update plots in the main interface.
            SmCore.updateSmGraphingTool();
            
            // Update plots in trim tool.
            runGraphs();

            addToLogList(logList,"Trim tool commit processing ended at " + timer.getGMTdateTime());
            logList.add("");
            
            // Write to log file.
            if (!logsDir.isEmpty()) {
                StringBuilder fileName = new StringBuilder();
                String[] segments = SmGlobal.TRIM_LOG_FILE.split("\\.");
                String fileTime = timerStart.replace("-","_").replace(" ", "_").replace(":","_");
                fileName.append(segments[0]).append("_").append(fileTime).
                    append(".").append(segments[1]);
                File logFile = Paths.get(logsDir,eventNodes.get(0).toString(),
                    fileName.toString()).toFile();
                
                SmUtils.writeToFile(logList,logFile);
            }
            
            JOptionPane.showMessageDialog(this, "Processing completed.",
                "Done", JOptionPane.PLAIN_MESSAGE);
        }
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
    }
    
    private void addToLogList(ArrayList<String> logList, String msg) {
        logList.add(msg);
        SmCore.addMsgToStatusViewer(msg);
    }
    
    private void addSmChartMouseListeners() {
        for (TraceSet traceSet : traceSets) {
            VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
            
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
            
            if (v1ChartsBin != null)
            {
                for (JPanel pnl : v1ChartsBin.getChartsSeismicAcc()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.addMouseListener(new SmChartViewMouseListener());
                    }   
                }
            }
            
            if (v2ChartsBin != null)
            {
                for (JPanel pnl : v2ChartsBin.getChartsSeismicAcc()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.addMouseListener(new SmChartViewMouseListener());
                    }   
                }
                for (JPanel pnl : v2ChartsBin.getChartsSeismicVel()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.addMouseListener(new SmChartViewMouseListener());
                    }   
                }
                for (JPanel pnl : v2ChartsBin.getChartsSeismicDis()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.addMouseListener(new SmChartViewMouseListener());
                    }   
                }
            }
        }
    }
    
    private void addSmChartMouseListeners(int traceSetsIndx) {
        TraceSet traceSet = traceSets.get(traceSetsIndx);
        VxChartsBinGroup vXChartsBinGroup = traceSet.getVxChartsBinGroup();
        
        V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
        V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();

        if (v1ChartsBin != null)
        {
            for (JPanel pnl : v1ChartsBin.getChartsSeismicAcc()) {
                if (pnl instanceof SmChartView) {
                    SmChartView scv = (SmChartView)pnl;
                    scv.addMouseListener(new SmChartViewMouseListener());
                }   
            }
        }

        if (v2ChartsBin != null)
        {
            for (JPanel pnl : v2ChartsBin.getChartsSeismicAcc()) {
                if (pnl instanceof SmChartView) {
                    SmChartView scv = (SmChartView)pnl;
                    scv.addMouseListener(new SmChartViewMouseListener());
                }   
            }
            for (JPanel pnl : v2ChartsBin.getChartsSeismicVel()) {
                if (pnl instanceof SmChartView) {
                    SmChartView scv = (SmChartView)pnl;
                    scv.addMouseListener(new SmChartViewMouseListener());
                }   
            }
            for (JPanel pnl : v2ChartsBin.getChartsSeismicDis()) {
                if (pnl instanceof SmChartView) {
                    SmChartView scv = (SmChartView)pnl;
                    scv.addMouseListener(new SmChartViewMouseListener());
                }   
            }
        }
    }
    
    private class BtnDataTypeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                if (evt.getActionCommand().trim().startsWith("Acc"))
                {
                    btnAcceleration.setSelected(true);
                    btnVelocity.setSelected(false);
                    btnDisplacement.setSelected(false);
                    btnAll.setSelected(false);
                }
                else if (evt.getActionCommand().trim().startsWith("Vel"))
                {
                    btnAcceleration.setSelected(false);
                    btnVelocity.setSelected(true);
                    btnDisplacement.setSelected(false);
                    btnAll.setSelected(false);
                }
                else if (evt.getActionCommand().trim().startsWith("Dis"))
                {
                    btnAcceleration.setSelected(false);
                    btnVelocity.setSelected(false);
                    btnDisplacement.setSelected(true);
                    btnAll.setSelected(false);
                }
                else if (evt.getActionCommand().trim().startsWith("All"))
                {
                    btnAcceleration.setSelected(false);
                    btnVelocity.setSelected(false);
                    btnDisplacement.setSelected(false);
                    btnAll.setSelected(true);
                }

                // Remove borders and markers from chart views for current TraceSet.
                traceSets.get(traceSetsIndx).removeBordersAndMarkers();
               
                // Update form components.
                updateChartViewer();
                updateAttributesTable();
                updateControlPanel();
            }
            catch (Exception ex) {
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
        }
    }
    
    private class BtnNavigationActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                if (traceSets.isEmpty())
                    return;
                
                // Update traceSetsIndx.
                if (evt.getActionCommand().trim().startsWith("First")) //First
                {
                    traceSetsIndx = 0;
                }
                else if (evt.getActionCommand().trim().startsWith("Previous")) //Previous
                {
                    if (traceSetsIndx > 0)
                        --traceSetsIndx;
                }
                else if (evt.getActionCommand().trim().startsWith("Next")) //Next
                {
                    if (traceSetsIndx < traceSets.size()-1)
                        ++traceSetsIndx;
                }
                else if (evt.getActionCommand().trim().startsWith("Last")) //Last
                {
                    traceSetsIndx = traceSets.size()-1;
                }
                
                // Update form components.
                updateNavigationControls();
                updateChartViewer();
                updateAttributesTable();
                updateControlPanel();
                
                // Set selected trace for current TraceSet object.
                TraceSet traceSet = traceSets.get(traceSetsIndx);
                selectChartView(traceSet.getCurTraceIndx());
                selectAttributesTableRow(traceSet.getCurTraceIndx());
            }
            catch (Exception ex) {
                SmCore.addMsgToStatusViewer(ex.getMessage());
            }
        } 
    }
    
    private class SmChartViewMouseListener extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {

            if (e.getSource() instanceof SmChartView) {
                SmChartView scv = (SmChartView)e.getSource();
                String scvName = scv.getName();
                SmDataCursor sdc = scv.getSmDataCursor();
                int selChartViewIndx = Integer.parseInt(scvName.substring(scvName.lastIndexOf("_")+1));
                
                TraceSet traceSet = traceSets.get(traceSetsIndx);
                
                if (selChartViewIndx == traceSet.getCurTraceIndx()) {
                    try {
                        // Turn off text field listeners.
                        setTextFieldPropertyListeners(false);
                    
                        // Remove previously drawn numeric label.
                        sdc.removeNumericLabel();

                        // Draw marker.
                        sdc.drawMarker(sdc.getLocation().getX(), 0);

                        // Update bounded text field value.
                        scv.setBoundedTextFieldValue(sdc.getLocation().getX());
                    }
                    finally {
                        // Turn on text field listeners.
                        setTextFieldPropertyListeners(true);
                    }
                }
                else {
                    // Set current trace index for the current TraceSet object.
                    traceSet.setCurTraceIndx(selChartViewIndx);
                    
                    selectChartView(traceSet.getCurTraceIndx());
                    selectAttributesTableRow(traceSet.getCurTraceIndx());

                    updateControlPanel();
                }
            }
        }
    }  
    
    private class AttributesTableMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TraceSet traceSet = traceSets.get(traceSetsIndx);
            
            // Set current trace index in TraceSet object.
            traceSet.setCurTraceIndx(tblAttributes.getSelectedRow());
            
            // Select panel in chart viewer corresponding to selected row index.
            selectChartView(traceSet.getCurTraceIndx());
            
            // Set chart viewer vertical scroll.
            setChartViewerVertScroll(traceSet.getCurTraceIndx());
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
                    if (fieldName.equals(ftxtStartTime.getName()))
                        rbtnStartTime.setSelected(true);
                    else if (fieldName.equals(ftxtStopTime.getName()))
                        rbtnStopTime.setSelected(true);
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
                String fieldName = field.getName();
                
                SmChartView smChartView = getCurChartView();
                
                if (fieldName == null || smChartView == null) 
                    return;
                
                Object value = field.getValue();
                
                if (value != null) {
                    double x = Double.parseDouble(value.toString());
                    double y = 0;

                    smChartView.getSmDataCursor().drawMarker(x, y);
                }
            }
        }
    }
    
    private class RBtnTypeListener implements ItemListener {  
        @Override
        public void itemStateChanged(ItemEvent e) {
            
            JRadioButton rbtn = (JRadioButton)e.getItem();
            if (rbtn.isSelected())
            {
                if (rbtn.getName().equals(rbtnStartTime.getName())) {
                    btnStartTime.setSelected(true);
                    btnStopTime.setSelected(false);
                }             
                else if (rbtn.getName().equals(rbtnStopTime.getName())) {
                    btnStartTime.setSelected(false);
                    btnStopTime.setSelected(true);
                }
                    
                // Set properties for each SmChartView object.
                setChartViewProperties(smChartViews);
            }
        }
    }
    
    /**
     * Class that extends the SwingWorker class and implements the functions to
     * run graphs as a background task.
     */
    private class RunGraphsByGroupTask extends SwingWorker<Integer, Integer> {
        private final String chartAPI;
        private final ArrayList<SmNode> eventNodes;
        private final SmTemplate smTemplate;
        private final boolean adjustPoints;
        private final Object owner;
        private final ProgressHandle handle;
        
        /**
         * Class constructor.
         * @param chartAPI name of chart API to use.
         * @param eventNodes list of event nodes to process
         * @param smTemplate SmTemplate object representing the template to apply.
         * @param handle the handle used for communicating with the calling process.
         */
        public RunGraphsByGroupTask(final String chartAPI, 
            final ArrayList<SmNode> eventNodes, final SmTemplate smTemplate,
            final boolean adjustPoints, final Object owner, final ProgressHandle handle) {
            
            this.chartAPI = chartAPI;
            this.eventNodes = eventNodes;
            this.smTemplate = smTemplate;
            this.adjustPoints = adjustPoints;
            this.owner = owner;
            this.handle = handle;
        }
        
        /**
         * Creates a group of two bins for each combination of event and station node 
         * selected. One bin is used to store charts of V1 data, while the other stores
         * charts of V2 data.
         * @return null if process completed successfully.
         */
        @Override
        public Integer doInBackground()
        {
            try
            {  
                // Set cursor.
                if (owner instanceof JFrame) {
                    JFrame frame = (JFrame)owner;
                    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
                
                // Set number of work units to number of stations.
                int workUnits = 0;
                for (SmNode nodeEvent : eventNodes) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        stations.nextElement();
                        workUnits++;
                    }
                }
                
                // Set number of work units for handle.
                handle.start(workUnits);
                
                int curWorkUnits = 0;
                
                // Clear traceSets array.
                traceSets.clear();
                
                for (SmNode nodeEvent : eventNodes) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();
                        handle.progress(++curWorkUnits);
                        
                        ArrayList<SmNode> checkedLeafNodes = new ArrayList<>();
                        
                        SmCore.addLeafSmNode(nodeStation, checkedLeafNodes, SmCore.CheckedState.CHECKED);
                        
                        if (checkedLeafNodes.isEmpty())
                            continue;
                        
                        ArrayList<String> v1FilePaths = new ArrayList<>();
                        ArrayList<String> v2FilePaths = new ArrayList<>();
                        
                        for (SmNode checkedLeafNode : checkedLeafNodes) {
                            String filePath = checkedLeafNode.getFilePath();
                            String fileType = SmCore.extractCosmosFileType(filePath);
                            
                            if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString())) {
                                v1FilePaths.add(filePath);
                            }
                            if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                v2FilePaths.add(filePath);
                            }
                        }
                        
                        ArrayList<String> filePaths = new ArrayList<>();
                        filePaths.addAll(v1FilePaths);
                        filePaths.addAll(v2FilePaths);
                        
                        DateTime earliestStartTime = SmUtils.getEarliestStartTime(filePaths);
                        DateTime latestStopTime = SmUtils.getLatestStopTime(filePaths);
                        double minDeltaT = SmUtils.getMinimumDeltaT(filePaths);
                        
                        V1ChartsBin v1ChartsBin = !v1FilePaths.isEmpty() ? 
                            new V1ChartsBin(chartAPI,v1FilePaths,earliestStartTime,
                                latestStopTime,minDeltaT,smTemplate,adjustPoints,owner) : null;
                        
                        V2ChartsBin v2ChartsBin = !v2FilePaths.isEmpty() ? 
                            new V2ChartsBin(chartAPI,v2FilePaths,earliestStartTime,
                                latestStopTime,minDeltaT,smTemplate,adjustPoints,owner) : null;
                        
                        String groupName = nodeEvent.toString() + "_" + nodeStation.toString();
                        
                        StringBuilder sbStationDesc = new StringBuilder();
                        StringBuilder sbEventDesc = new StringBuilder();
                        
                        File filePath = null;
                        
                        if (!v1FilePaths.isEmpty())
                            filePath = new File(v1FilePaths.get(0));
                        else if (!v2FilePaths.isEmpty())
                            filePath = new File(v2FilePaths.get(0));
                         
                        if (filePath != null) {
                            SmFile smFile = new SmFile(filePath);
                            SmRec smRec = smFile.getSmRecs().get(0);
                            
                            sbStationDesc.append(smRec.getNetworkCode()).append(".").append(smRec.getStationCode());
                            if (!smRec.getStationName().isEmpty())
                                sbStationDesc.append(" - ").append(smRec.getStationName());

                            try {
                                String eventDate = smRec.getEventDateTime();
                                
                                SimpleDateFormat formatOrig = new SimpleDateFormat("E MMM dd, yyyy HH:mm");
                                SimpleDateFormat formatNew = new SimpleDateFormat("MM/dd/yyyy");
                                
                                Date date = formatOrig.parse(eventDate);
                                
                                sbEventDesc.append(formatNew.format(date));
                            }
                            catch (ParseException ex) {
                            }
                            
                            if (!smRec.getEventName().isEmpty())
                                sbEventDesc.append(" ").append(smRec.getEventName());
                        }
                        
                        traceSets.add(new TraceSet(new VxChartsBinGroup(
                            groupName,sbStationDesc.toString(),sbEventDesc.toString(),
                            chartAPI,v1ChartsBin,v2ChartsBin),earliestStartTime,latestStopTime));
                    }
                }
            } 
            catch (Exception ex) 
            {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            } 
            finally {
                if (owner instanceof JFrame) {
                    JFrame frame = (JFrame)owner;
                    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
            
            return null;
        }
        
        /**
         * Performs tasks after background task has completed, including updating
         * various components of the interface.
         */
        @Override
        public void done() {
            try {
                // Add SmChartMouseListener to each chart.
                addSmChartMouseListeners();
                
                // Update form components.
                updateNavigationControls();
                updateChartViewer();
                updateAttributesTable();
                updateControlPanel();
            }
            catch (Exception ex) {
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
            finally {
                // Finish handle.
                handle.finish(); 
            }
        }
    }
    
    private class TraceSet {
        
        private VxChartsBinGroup vXChartsBinGroup;
        private final DateTime origStartDateTime;
        private final DateTime origStopDateTime;
        private DateTime curStartDateTime;
        private DateTime curStopDateTime;
        private double curTrimmedStartTime = -1;
        private double curTrimmedStopTime = -1;
        private int curTraceIndx = -1;
    
        private TraceSet(VxChartsBinGroup vXChartsBinGroup, DateTime startDateTime,
            DateTime stopDateTime) {
            this.vXChartsBinGroup = vXChartsBinGroup;
            this.origStartDateTime = startDateTime;
            this.origStopDateTime = stopDateTime;
            this.curStartDateTime = startDateTime;
            this.curStopDateTime = stopDateTime;
        }
        
        private VxChartsBinGroup getVxChartsBinGroup() {return this.vXChartsBinGroup;}
        private DateTime getOrigStartDateTime() {return this.origStartDateTime;}
        private DateTime getOrigStopDateTime() {return this.origStopDateTime;}
        private DateTime getCurStartDateTime() {return this.curStartDateTime;}
        private DateTime getCurStopDateTime() {return this.curStopDateTime;}
        private double getCurTrimmedStartTime() {return this.curTrimmedStartTime;}
        private double getCurTrimmedStopTime() {return this.curTrimmedStopTime;}
        private int getCurTraceIndx() {return this.curTraceIndx;}
        
        private void setVxChartsBinGroup(VxChartsBinGroup vXChartsBinGroup) {this.vXChartsBinGroup = vXChartsBinGroup;}
        private void setCurStartDateTime(DateTime startDateTime) {this.curStartDateTime = startDateTime;}
        private void setCurStopDateTime(DateTime stopDateTime) {this.curStopDateTime = stopDateTime;}
        private void setCurTrimmedStartTime(double trimmedStartTime) {this.curTrimmedStartTime = trimmedStartTime;}
        private void setCurTrimmedStopTime(double trimmedStopTime) {this.curTrimmedStopTime = trimmedStopTime;}
        private void setCurTraceIndx(int curTraceIndx) {this.curTraceIndx = curTraceIndx;}
        
        private void removeBordersAndMarkers() {
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
            
            if (v1ChartsBin != null)
            {
                for (JPanel pnl : v1ChartsBin.getChartsSeismicAcc()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.getSmDataCursor().removeMarkers();
                        scv.setBorder(null);
                    }
                }
            }
            
            if (v2ChartsBin != null)
            {
                for (JPanel pnl : v2ChartsBin.getChartsSeismicAcc()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.getSmDataCursor().removeMarkers();
                        scv.setBorder(null);
                    }
                }
                
                for (JPanel pnl : v2ChartsBin.getChartsSeismicVel()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.getSmDataCursor().removeMarkers();
                        scv.setBorder(null);
                    }
                }
                
                for (JPanel pnl : v2ChartsBin.getChartsSeismicDis()) {
                    if (pnl instanceof SmChartView) {
                        SmChartView scv = (SmChartView)pnl;
                        scv.getSmDataCursor().removeMarkers();
                        scv.setBorder(null);
                    }
                }
            }
            
            curTraceIndx = -1;
        } 
        
        private void reset() {
            // Reset current start and stop DateTime variables to what they were originally.
            curStartDateTime = origStartDateTime;
            curStopDateTime = origStopDateTime;
            
            // Reset current trimmed start and stop time variables.
            curTrimmedStartTime = -1;
            curTrimmedStopTime = -1;
            
            // Reset the V1 and V2 charts bin objects.
            V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
            V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();
            
            if (v1ChartsBin != null)
                v1ChartsBin.reset();
            
            if (v2ChartsBin != null)
                v2ChartsBin.reset();
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

        btngrpNavigation = new javax.swing.ButtonGroup();
        rbtngrpStartStopTimes = new javax.swing.ButtonGroup();
        btngrpStartStopTimes = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        btnAcceleration = new javax.swing.JButton();
        btnVelocity = new javax.swing.JButton();
        btnDisplacement = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnStartTime = new javax.swing.JButton();
        btnStopTime = new javax.swing.JButton();
        btnAutoSync = new javax.swing.JButton();
        btnTrim = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnResetAll = new javax.swing.JButton();
        btnCommit = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnNavFirst = new javax.swing.JButton();
        btnNavPrevious = new javax.swing.JButton();
        btnNavNext = new javax.swing.JButton();
        btnNavLast = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 32767));
        lblNavDisplay = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        scrlpaneViewer = new javax.swing.JScrollPane();
        pnlViewer = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        scrlpaneAttributes = new javax.swing.JScrollPane();
        tblAttributes = new javax.swing.JTable();
        pnlControlPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblEarliestStartTime = new javax.swing.JLabel();
        lblLatestStopTime = new javax.swing.JLabel();
        lblLatestStartTime = new javax.swing.JLabel();
        lblEarliestStopTime = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        rbtnStartTime = new javax.swing.JRadioButton();
        rbtnStopTime = new javax.swing.JRadioButton();
        ftxtStartTime = new javax.swing.JFormattedTextField();
        ftxtStopTime = new javax.swing.JFormattedTextField();
        jPanel3 = new javax.swing.JPanel();
        btnReset2 = new javax.swing.JButton();
        btnTrim2 = new javax.swing.JButton();
        btnCommit2 = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnAutoSync2 = new javax.swing.JButton();
        btnResetAll2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(1100, 700));
        setSize(new java.awt.Dimension(1100, 700));

        jToolBar1.setRollover(true);

        btnAcceleration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_acc_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAcceleration, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAcceleration.text")); // NOI18N
        btnAcceleration.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAcceleration.toolTipText")); // NOI18N
        btnAcceleration.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAcceleration.actionCommand")); // NOI18N
        btnAcceleration.setFocusable(false);
        btnAcceleration.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAcceleration.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnAcceleration);

        btnVelocity.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_vel_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnVelocity, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnVelocity.text")); // NOI18N
        btnVelocity.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnVelocity.toolTipText")); // NOI18N
        btnVelocity.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnVelocity.actionCommand")); // NOI18N
        btnVelocity.setFocusable(false);
        btnVelocity.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVelocity.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnVelocity);

        btnDisplacement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_dis_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDisplacement, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnDisplacement.text")); // NOI18N
        btnDisplacement.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnDisplacement.toolTipText")); // NOI18N
        btnDisplacement.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnDisplacement.actionCommand")); // NOI18N
        btnDisplacement.setFocusable(false);
        btnDisplacement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDisplacement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnDisplacement);

        btnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_all_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAll, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAll.text")); // NOI18N
        btnAll.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAll.toolTipText")); // NOI18N
        btnAll.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAll.actionCommand")); // NOI18N
        btnAll.setFocusable(false);
        btnAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAll.setSelected(true);
        btnAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnAll);
        jToolBar1.add(jSeparator1);

        btnStartTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_start_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnStartTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnStartTime.text")); // NOI18N
        btnStartTime.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnStartTime.toolTipText")); // NOI18N
        btngrpStartStopTimes.add(btnStartTime);
        btnStartTime.setFocusable(false);
        btnStartTime.setHideActionText(true);
        btnStartTime.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStartTime.setSelected(true);
        btnStartTime.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStartTime.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnStartTimeMouseClicked(evt);
            }
        });
        jToolBar1.add(btnStartTime);

        btnStopTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_stop_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnStopTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnStopTime.text")); // NOI18N
        btnStopTime.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnStopTime.toolTipText")); // NOI18N
        btngrpStartStopTimes.add(btnStopTime);
        btnStopTime.setFocusable(false);
        btnStopTime.setHideActionText(true);
        btnStopTime.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStopTime.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStopTime.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnStopTimeMouseClicked(evt);
            }
        });
        jToolBar1.add(btnStopTime);

        btnAutoSync.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_auto_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAutoSync, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAutoSync.text")); // NOI18N
        btnAutoSync.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAutoSync.toolTipText")); // NOI18N
        btnAutoSync.setFocusable(false);
        btnAutoSync.setHideActionText(true);
        btnAutoSync.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAutoSync.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAutoSync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoSyncActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAutoSync);

        btnTrim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_manual_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnTrim, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnTrim.text")); // NOI18N
        btnTrim.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnTrim.toolTipText")); // NOI18N
        btnTrim.setFocusable(false);
        btnTrim.setHideActionText(true);
        btnTrim.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTrim.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTrim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTrimActionPerformed(evt);
            }
        });
        jToolBar1.add(btnTrim);

        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_reset_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnReset, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnReset.text")); // NOI18N
        btnReset.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnReset.toolTipText")); // NOI18N
        btnReset.setFocusable(false);
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });
        jToolBar1.add(btnReset);

        btnResetAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_resetAll_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnResetAll, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnResetAll.text")); // NOI18N
        btnResetAll.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnResetAll.toolTipText")); // NOI18N
        btnResetAll.setFocusable(false);
        btnResetAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnResetAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnResetAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetAllActionPerformed(evt);
            }
        });
        jToolBar1.add(btnResetAll);

        btnCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/trim_commit_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCommit, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnCommit.text")); // NOI18N
        btnCommit.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnCommit.toolTipText")); // NOI18N
        btnCommit.setFocusable(false);
        btnCommit.setHideActionText(true);
        btnCommit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCommit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCommit);
        jToolBar1.add(jSeparator2);

        btnNavFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/nav_first_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNavFirst, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavFirst.text")); // NOI18N
        btnNavFirst.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavFirst.toolTipText")); // NOI18N
        btnNavFirst.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavFirst.actionCommand")); // NOI18N
        btngrpNavigation.add(btnNavFirst);
        btnNavFirst.setFocusable(false);
        btnNavFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNavFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnNavFirst);

        btnNavPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/nav_previous_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNavPrevious, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavPrevious.text")); // NOI18N
        btnNavPrevious.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavPrevious.toolTipText")); // NOI18N
        btnNavPrevious.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavPrevious.actionCommand")); // NOI18N
        btngrpNavigation.add(btnNavPrevious);
        btnNavPrevious.setFocusable(false);
        btnNavPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNavPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnNavPrevious);

        btnNavNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/nav_next_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNavNext, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavNext.text")); // NOI18N
        btnNavNext.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavNext.toolTipText")); // NOI18N
        btnNavNext.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavNext.actionCommand")); // NOI18N
        btngrpNavigation.add(btnNavNext);
        btnNavNext.setFocusable(false);
        btnNavNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNavNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnNavNext);

        btnNavLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/nav_last_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNavLast, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavLast.text")); // NOI18N
        btnNavLast.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavLast.toolTipText")); // NOI18N
        btnNavLast.setActionCommand(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnNavLast.actionCommand")); // NOI18N
        btngrpNavigation.add(btnNavLast);
        btnNavLast.setFocusable(false);
        btnNavLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNavLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnNavLast);
        jToolBar1.add(filler1);

        org.openide.awt.Mnemonics.setLocalizedText(lblNavDisplay, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.lblNavDisplay.text")); // NOI18N
        lblNavDisplay.setPreferredSize(new java.awt.Dimension(2, 25));
        jToolBar1.add(lblNavDisplay);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerLocation(350);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);

        pnlViewer.setBackground(new java.awt.Color(255, 255, 255));
        pnlViewer.setLayout(new javax.swing.BoxLayout(pnlViewer, javax.swing.BoxLayout.PAGE_AXIS));
        scrlpaneViewer.setViewportView(pnlViewer);

        jSplitPane1.setLeftComponent(scrlpaneViewer);

        jSplitPane2.setDividerLocation(170);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);

        tblAttributes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Data Type", "Channel", "Number of Points", "Delta Time (sec)", "Duration (sec)", "Start DateTime", "Stop DateTime"
            }
        ));
        tblAttributes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrlpaneAttributes.setViewportView(tblAttributes);

        jSplitPane2.setLeftComponent(scrlpaneAttributes);

        pnlControlPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(lblEarliestStartTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.lblEarliestStartTime.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLatestStopTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.lblLatestStopTime.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLatestStartTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.lblLatestStartTime.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblEarliestStopTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.lblEarliestStopTime.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLatestStopTime)
                    .addComponent(lblEarliestStopTime)
                    .addComponent(lblLatestStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEarliestStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblEarliestStartTime, lblEarliestStopTime, lblLatestStartTime, lblLatestStopTime});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblEarliestStartTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLatestStartTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEarliestStopTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLatestStopTime)
                .addGap(21, 21, 21))
        );

        pnlControlPanel.add(jPanel1, java.awt.BorderLayout.WEST);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        rbtngrpStartStopTimes.add(rbtnStartTime);
        rbtnStartTime.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnStartTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.rbtnStartTime.text")); // NOI18N
        rbtnStartTime.setName("rbtnStartTime"); // NOI18N

        rbtngrpStartStopTimes.add(rbtnStopTime);
        org.openide.awt.Mnemonics.setLocalizedText(rbtnStopTime, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.rbtnStopTime.text")); // NOI18N
        rbtnStopTime.setName("rbtnStopTime"); // NOI18N

        ftxtStartTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtStartTime.setText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.StartTime.text")); // NOI18N
        ftxtStartTime.setName("StartTime"); // NOI18N

        ftxtStopTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.######"))));
        ftxtStopTime.setText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.StopTime.text")); // NOI18N
        ftxtStopTime.setName("StopTime"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnReset2, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnReset2.text")); // NOI18N
        btnReset2.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnReset2.toolTipText")); // NOI18N
        btnReset2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnTrim2, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnTrim2.text")); // NOI18N
        btnTrim2.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnTrim2.toolTipText")); // NOI18N
        btnTrim2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTrimActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCommit2, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnCommit2.text")); // NOI18N
        btnCommit2.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnCommit2.toolTipText")); // NOI18N
        btnCommit2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnExit, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnExit.text")); // NOI18N
        btnExit.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnExit.toolTipText")); // NOI18N
        btnExit.setHideActionText(true);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnAutoSync2, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAutoSync2.text")); // NOI18N
        btnAutoSync2.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnAutoSync2.toolTipText")); // NOI18N
        btnAutoSync2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoSyncActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnResetAll2, org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnResetAll2.text")); // NOI18N
        btnResetAll2.setToolTipText(org.openide.util.NbBundle.getMessage(SmTrimTool.class, "SmTrimTool.btnResetAll2.toolTipText")); // NOI18N
        btnResetAll2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnResetAll2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCommit2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExit))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAutoSync2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTrim2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReset2)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAutoSync2, btnCommit2, btnExit, btnReset2, btnResetAll2, btnTrim2});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTrim2)
                    .addComponent(btnAutoSync2)
                    .addComponent(btnReset2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCommit2)
                    .addComponent(btnExit)
                    .addComponent(btnResetAll2))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(rbtnStartTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftxtStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(rbtnStopTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftxtStopTime)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {rbtnStartTime, rbtnStopTime});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbtnStartTime)
                            .addComponent(ftxtStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(rbtnStopTime)
                            .addComponent(ftxtStopTime, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {ftxtStartTime, ftxtStopTime});

        pnlControlPanel.add(jPanel2, java.awt.BorderLayout.CENTER);

        jSplitPane2.setRightComponent(pnlControlPanel);

        jSplitPane1.setRightComponent(jSplitPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnAutoSyncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutoSyncActionPerformed
        autoTrim();
    }//GEN-LAST:event_btnAutoSyncActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        reset();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnTrimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTrimActionPerformed
        trim();
    }//GEN-LAST:event_btnTrimActionPerformed

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        commit();
    }//GEN-LAST:event_btnCommitActionPerformed

    private void btnStartTimeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnStartTimeMouseClicked
        rbtnStartTime.doClick();
    }//GEN-LAST:event_btnStartTimeMouseClicked

    private void btnStopTimeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnStopTimeMouseClicked
        rbtnStopTime.doClick();
    }//GEN-LAST:event_btnStopTimeMouseClicked

    private void btnResetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllActionPerformed
        resetAll();
    }//GEN-LAST:event_btnResetAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcceleration;
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnAutoSync;
    private javax.swing.JButton btnAutoSync2;
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnCommit2;
    private javax.swing.JButton btnDisplacement;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnNavFirst;
    private javax.swing.JButton btnNavLast;
    private javax.swing.JButton btnNavNext;
    private javax.swing.JButton btnNavPrevious;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnReset2;
    private javax.swing.JButton btnResetAll;
    private javax.swing.JButton btnResetAll2;
    private javax.swing.JButton btnStartTime;
    private javax.swing.JButton btnStopTime;
    private javax.swing.JButton btnTrim;
    private javax.swing.JButton btnTrim2;
    private javax.swing.JButton btnVelocity;
    private javax.swing.ButtonGroup btngrpNavigation;
    private javax.swing.ButtonGroup btngrpStartStopTimes;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JFormattedTextField ftxtStartTime;
    private javax.swing.JFormattedTextField ftxtStopTime;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblEarliestStartTime;
    private javax.swing.JLabel lblEarliestStopTime;
    private javax.swing.JLabel lblLatestStartTime;
    private javax.swing.JLabel lblLatestStopTime;
    private javax.swing.JLabel lblNavDisplay;
    private javax.swing.JPanel pnlControlPanel;
    private javax.swing.JPanel pnlViewer;
    private javax.swing.JRadioButton rbtnStartTime;
    private javax.swing.JRadioButton rbtnStopTime;
    private javax.swing.ButtonGroup rbtngrpStartStopTimes;
    private javax.swing.JScrollPane scrlpaneAttributes;
    private javax.swing.JScrollPane scrlpaneViewer;
    private javax.swing.JTable tblAttributes;
    // End of variables declaration//GEN-END:variables
}
