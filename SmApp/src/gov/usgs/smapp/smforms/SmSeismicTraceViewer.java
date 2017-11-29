/*******************************************************************************
 * Name: Java class SmSeismicTraceViewer.java
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
import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.UNCORACC;
import SmConstants.VFileConstants.V2DataType;
import SmControl.SmQueue;
import SmProcessing.V2ProcessGUI;
import SmUtilities.SmTimeFormatter;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smchartingapi.SmCharts_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.QCChart2D_API;
import gov.usgs.smapp.smchartingapi.qcchart2d.SingleChartView;
import gov.usgs.smcommon.smclasses.SmChannel;
import gov.usgs.smcommon.smclasses.SmEpoch;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPoint;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmStation;
import gov.usgs.smcommon.smclasses.SmTemplate;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import gov.usgs.smcommon.smutilities.SmUtils;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.openide.NotifyDescriptor;

/**
 *
 * @author png
 */
public class SmSeismicTraceViewer extends javax.swing.JFrame {

    private final String chartAPI;
    private final ArrayList<V2ProcessGUI> v2ProcessGUIs;
    private final SmTemplate smTemplate;
    
    private final DateTime earliestDateTime;
    private final DateTime latestDateTime;
    
    
    /**
     * Creates new form SmSeismicChartViewer
     * @param chartAPI
     * @param v2ProcessGUIs
     * @param smTemplate
     */
    public SmSeismicTraceViewer(String chartAPI, ArrayList<V2ProcessGUI> v2ProcessGUIs,
        SmTemplate smTemplate) {
        
        initComponents();
        
        // Set form icon.
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
            getResource("/resources/icons/prism_review_tool_16.png")));
        
        setupDataTypeButtonControls();
        
        this.chartAPI = chartAPI;
        this.v2ProcessGUIs = v2ProcessGUIs;
        this.smTemplate = smTemplate;
        
        // Set earliest and latest start times and minimum delta time 
        // based on the event and station in which the source files belong to.
        //File file = v2ProcessGUIs.get(0).getV1File();
        //String filePath = file.getPath();
        //String event = SmCore.extractEventName(filePath);
        //String station = SmCore.extractStationName(filePath);
        
        //this.earliestDateTime = SmCore.getEarliestStartTime(event, station);
        //this.latestDateTime = SmCore.getLatestStopTime(event, station);
        
        ArrayList<String> filePaths = new ArrayList<>();
        for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
            filePaths.add(v2ProcessGUI.getV1File().getPath());
        }
        
        // Set earliest and latest start times and minimum delta time.
        this.earliestDateTime = SmUtils.getEarliestStartTime(filePaths);
        this.latestDateTime = SmUtils.getLatestStopTime(filePaths);
        
        updateChartViewer();
    }
    
    private void updateChartViewer() {
        VFileConstants.V2DataType[] v2DataTypes;
        
        if (this.btnAcceleration.isSelected()) {
            v2DataTypes = new V2DataType[1];
            v2DataTypes[0] = V2DataType.ACC;
        }
        else if (this.btnVelocity.isSelected()) {
            v2DataTypes = new V2DataType[1];
            v2DataTypes[0] = V2DataType.VEL;
        }
        else if (this.btnDisplacement.isSelected()) {
            v2DataTypes = new V2DataType[1];
            v2DataTypes[0] = V2DataType.DIS;
        }
        else {
            v2DataTypes = VFileConstants.V2DataType.values();
        }
        
        // Clear pnlChartViewer.
        pnlChartViewer.removeAll();
            
        // Create the charts.
        for (VFileConstants.V2DataType v2DataType : v2DataTypes) {
            for (V2ProcessGUI v2ProcessGUI : this.v2ProcessGUIs) {
                createSeismicChart(v2ProcessGUI,v2DataType);
            }
        }
    }
    
    private void createSeismicChart(V2ProcessGUI v2ProcessGUI, VFileConstants.V2DataType v2DataType) {
        try {
            File v1File = v2ProcessGUI.getV1File();
            
            SmTimeFormatter timer = new SmTimeFormatter();
            String logTime = timer.getGMTdateTime();
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(v1File,logTime,new File(logDir));
            queue.readInFile(v1File);
            queue.parseVFile(UNCORACC);

            ArrayList<COSMOScontentFormat> smList = queue.getSmList();

            if (smList.isEmpty())
                return;
            
            COSMOScontentFormat rec = smList.get(0);
                
            final String[] textHeaders = rec.getTextHeader();
            String eventName = textHeaders[1].substring(0, 40).
                replaceAll("(?i)record", "").replaceAll("(?i)of","").trim();
            String networkCode = textHeaders[4].substring(25, 27).trim();
            String stationCode = textHeaders[4].substring(28, 34).trim();
            String channel = rec.getChannel();
            //double deltaT = rec.getRealHeaderValue(DELTA_T);
            String seed = "";
            String lCode = "";

            Pattern p = Pattern.compile("^(.{3})(\\.)(.{2})$");
            Matcher m = p.matcher(channel);
            if (m.find()) {
                seed = m.group(1);
                lCode = m.group(3);
            }
            
            ArrayList<SmPoint> smPoints = new ArrayList<>();
            double[] points = v2ProcessGUI.getV2Array(v2DataType);
            
            double xVal = 0;
            for (int i=0; i<points.length; i++){
                smPoints.add(new SmPoint(xVal,points[i]));
                //xVal += deltaT*MSEC_TO_SEC;
                xVal += v2ProcessGUI.getDTime();
            }
                    
            SmCharts_API smCharts_API = (chartAPI.equals(SmGlobal.SM_CHARTS_API_QCCHART2D)) ?
                new QCChart2D_API() : null;

            if (smCharts_API == null)
                return;

            // Initialize SmStation and SmChannel objects.
            SmStation smStation = (smTemplate != null) ? 
                smTemplate.getSmStation() : null;
            SmEpoch smEpoch = (smStation != null) ?
                smStation.getSmEpoch(earliestDateTime, latestDateTime) : null;
            SmChannel smChannel = (smEpoch != null) ?
                smEpoch.getSmChannel(channel) : null;

            // Set series title and description.
            String title = channel;
            String description = eventName + "_" + networkCode + "." + 
                stationCode + "." + seed + "." + lCode;

            // Create color for series.
            Color color;
            if (smTemplate == null)
                color = new Color(SmPreferences.ChartOptions.getColorPlot());
            else {
                color = (smChannel != null) ? smChannel.getColor() :
                    new Color(SmPreferences.ChartOptions.getColorPlot());
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
                xyBounds,chartTitle,xAxisTitle,yAxisTitle,true,null);

            if (chartView != null) {
                pnlChartViewer.add(chartView);
                pnlChartViewer.updateUI();
             }
        }
        catch (Exception ex ) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void setupDataTypeButtonControls()
    {
        BtnDataTypeActionListener btnDataTypeActionListener = 
            new BtnDataTypeActionListener();
        btnAcceleration.addActionListener(btnDataTypeActionListener);
        btnVelocity.addActionListener(btnDataTypeActionListener);
        btnDisplacement.addActionListener(btnDataTypeActionListener);
        btnAll.addActionListener(btnDataTypeActionListener);
    }
    
    private class BtnDataTypeActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent evt) {
        
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
            
            // Update chart viewer.
            updateChartViewer();
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
        tbarChartViewer = new javax.swing.JToolBar();
        btnAcceleration = new javax.swing.JButton();
        btnVelocity = new javax.swing.JButton();
        btnDisplacement = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        scrollpaneChartViewer = new javax.swing.JScrollPane();
        pnlChartViewer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.title_1")); // NOI18N

        tbarChartViewer.setRollover(true);

        btnAcceleration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_acc_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAcceleration, org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAcceleration.text")); // NOI18N
        btnAcceleration.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAcceleration.toolTipText")); // NOI18N
        btnAcceleration.setActionCommand(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAcceleration.actionCommand")); // NOI18N
        btnAcceleration.setFocusable(false);
        btnAcceleration.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAcceleration.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbarChartViewer.add(btnAcceleration);

        btnVelocity.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_vel_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnVelocity, org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnVelocity.text")); // NOI18N
        btnVelocity.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnVelocity.toolTipText")); // NOI18N
        btnVelocity.setActionCommand(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnVelocity.actionCommand")); // NOI18N
        btnVelocity.setFocusable(false);
        btnVelocity.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVelocity.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbarChartViewer.add(btnVelocity);

        btnDisplacement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_dis_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDisplacement, org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnDisplacement.text")); // NOI18N
        btnDisplacement.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnDisplacement.toolTipText")); // NOI18N
        btnDisplacement.setActionCommand(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnDisplacement.actionCommand")); // NOI18N
        btnDisplacement.setFocusable(false);
        btnDisplacement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDisplacement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbarChartViewer.add(btnDisplacement);

        btnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/datatype_all_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAll, org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAll.text")); // NOI18N
        btnAll.setToolTipText(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAll.toolTipText")); // NOI18N
        btnAll.setActionCommand(org.openide.util.NbBundle.getMessage(SmSeismicTraceViewer.class, "SmSeismicTraceViewer.btnAll.actionCommand")); // NOI18N
        btnAll.setFocusable(false);
        btnAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbarChartViewer.add(btnAll);

        scrollpaneChartViewer.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        pnlChartViewer.setBackground(new java.awt.Color(255, 255, 255));
        pnlChartViewer.setLayout(new javax.swing.BoxLayout(pnlChartViewer, javax.swing.BoxLayout.PAGE_AXIS));
        scrollpaneChartViewer.setViewportView(pnlChartViewer);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tbarChartViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addComponent(scrollpaneChartViewer)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tbarChartViewer, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollpaneChartViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcceleration;
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnDisplacement;
    private javax.swing.JButton btnVelocity;
    private javax.swing.ButtonGroup btngrpNavigation;
    private javax.swing.JPanel pnlChartViewer;
    private javax.swing.JScrollPane scrollpaneChartViewer;
    private javax.swing.JToolBar tbarChartViewer;
    // End of variables declaration//GEN-END:variables
}
