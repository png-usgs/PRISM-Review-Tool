/*******************************************************************************
 * Name: Java class SmPropertiesViewerTC.java
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

package gov.usgs.smapp.smtopcomponents;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import static SmConstants.VFileConstants.AVG_VAL;
import static SmConstants.VFileConstants.CORACC;
import static SmConstants.VFileConstants.DATA_PHYSICAL_PARAM_CODE;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.DISPLACE;
import static SmConstants.VFileConstants.MEAN_ZERO;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.UNCORACC;
import static SmConstants.VFileConstants.VELOCITY;
import static SmConstants.VFileConstants.V_UNITS_INDEX;
import SmControl.SmQueue;
import SmUtilities.SmTimeFormatter;
import gov.usgs.smapp.SmCore;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmProperty;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//gov.usgs.smapp.smtopcomponents//SmPropertiesViewerTC//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SmPropertiesViewerTC",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "gov.usgs.smapp.smtopcomponents.SmPropertiesViewerTC")
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
@ActionReference(path = "Menu/Window", position = 150)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SmPropertiesViewerTCAction",
        preferredID = "SmPropertiesViewerTC"
)
@Messages({
    "CTL_SmPropertiesViewerTCAction=Properties Viewer",
    "CTL_SmPropertiesViewerTC=Properties",
    "HINT_SmPropertiesViewerTC=Properties Viewer"
})
@SuppressWarnings("rawtypes")
public final class SmPropertiesViewerTC extends TopComponent 
    implements LookupListener {

//    private Lookup.Result<SmNode> smNodeResult = null;
    private Lookup.Result<String> resultFilePath = null;
    
    public SmPropertiesViewerTC() {
        initComponents();
        setName(Bundle.CTL_SmPropertiesViewerTC());
        setToolTipText(Bundle.HINT_SmPropertiesViewerTC());
        
        // Setup tableProperties control.
        setupTablePropertiesControl();
    }
    
    public JTable getTableProperties() {
        return this.tblProperties;
    }
    
    private void setupTablePropertiesControl()
    {
        // Set JTable object properties.
        tblProperties.setShowGrid(true);
        tblProperties.setGridColor(Color.LIGHT_GRAY);
        
        //tblProperties.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //double factor = scrollpaneProperties.getViewport().getWidth();
        double factor = scrollpaneProperties.getPreferredSize().getWidth();
        
        tblProperties.getColumnModel().getColumn(0).setPreferredWidth((int)(.35*factor));
        tblProperties.getColumnModel().getColumn(1).setPreferredWidth((int)(.65*factor));
        
        // Add table header mouse motion listener to display header value in a tooltip.
        tblProperties.getTableHeader().addMouseMotionListener(new TableHeaderMouseMotionListener(tblProperties));
        
        // Set column cell renderer to display cell value in a tooltip.
        tblProperties.getColumnModel().getColumn(0).setCellRenderer(new PropertiesTableCellRenderer());
        tblProperties.getColumnModel().getColumn(1).setCellRenderer(new PropertiesTableCellRenderer());
    }
    
    private void loadRecProperties(File file)
    {
        try 
        {
            SmTimeFormatter timer = new SmTimeFormatter();
            String logTime = timer.getGMTdateTime();
            String logDir = SmPreferences.General.getLogsDir();

            SmQueue queue = new SmQueue(file,logTime,new File(logDir));
            queue.readInFile(file);
            
            String fileName = file.getName();
            String fileType = SmCore.extractCosmosFileType(fileName);
            
            int recCnt = 0;
            
            if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString())) {
                recCnt = queue.parseVFile(UNCORACC);
            }
            else if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString()))
            {
                String v2DataType = SmCore.extractCosmosV2DataType(fileName);
                
                if (v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.ACC.toString()))
                    recCnt = queue.parseVFile(CORACC);
                else if (v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.VEL.toString()))
                    recCnt = queue.parseVFile(VELOCITY);
                else if (v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.DIS.toString()))
                    recCnt = queue.parseVFile(DISPLACE);
            }
            else {
                SmCore.addMsgToStatusViewer("Error - could not determine COSMOS file type.");
                return;
            }
            
            ArrayList<COSMOScontentFormat> smlist = queue.getSmList();
            
            ArrayList<SmProperty> properties = new ArrayList<>();
            properties.add(new SmProperty("File Name", fileName));
            properties.add(new SmProperty("Record Count", String.valueOf(recCnt)));

            int recNum = 0;
            for (COSMOScontentFormat rec : smlist) {

                String[] textHeaders = rec.getTextHeader();
                String eventName = textHeaders[1].substring(0, 40).
                    replaceAll("(?i)record", "").replaceAll("(?i)of","").trim();
                String eventDateTime = textHeaders[1].substring(40).trim();
                String networkNum = textHeaders[4].substring(9, 12).trim();
                String stationNum = textHeaders[4].substring(13, 19).trim();
                String networkCode = textHeaders[4].substring(25, 27).trim();
                String stationCode = textHeaders[4].substring(28, 34).trim();
                String networkAbbr = textHeaders[4].substring(35, 40).trim();
                String stationName = textHeaders[4].substring(40).trim();

                String scnl = "";
//                String scnlStation = "";
//                String scnlSeed = "";
//                String scnlNetwork = "";
//                String scnlLCode = "";
                for (String comment : rec.getComments()) {
                    Pattern p = Pattern.compile("^(\\|<SCNL>)(\\w{1,5})(\\.)(\\w{3})(\\.)(\\w{1,2})(\\.)(\\w{2})");
                    Matcher m = p.matcher(comment);
                    
                    while (m.find()) {
                        scnl = m.group(2) + m.group(3) + m.group(4) + m.group(5) +
                            m.group(6) + m.group(7) + m.group(8);
//                        scnlStation = m.group(2);
//                        scnlSeed = m.group(4);
//                        scnlNetwork = m.group(6);
//                        scnlLCode = m.group(8);
                    }
                }
               
                String channel = rec.getChannel();
                String sensorLocation = rec.getSensorLocation();
                
                String procType = rec.getProcType();
                int dataParmCode = rec.getIntHeaderValue(DATA_PHYSICAL_PARAM_CODE);
                int dataUnitCode = rec.getIntHeaderValue(V_UNITS_INDEX);
             
                double meanZero = rec.getRealHeaderValue(MEAN_ZERO);
                double deltaT = rec.getRealHeaderValue(DELTA_T) * MSEC_TO_SEC ;
                double sampleRate = 1.0/deltaT;
                double timeSeriesLen = rec.getRealHeaderValue(62);
//                double timeSeriesLen = (dataLength-1) * deltaT;
//                double maxVal = rec.getRealHeaderValue(MAX_VAL);
//                double maxValTime = rec.getRealHeaderValue(MAX_VAL_TIME);
                double maxVal = rec.getRealHeaderValue(63);
                double maxValTime = rec.getRealHeaderValue(64);
                double avgVal = rec.getRealHeaderValue(AVG_VAL);
                
                int pointCnt = 0;
                
                if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()))
                {
                    V1Component v1Rec = (V1Component) rec;
                    pointCnt = v1Rec.getDataLength();
                }
                else if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString()))
                {
                    V2Component v2Rec = (V2Component) rec;
                    pointCnt = v2Rec.getDataLength();
                }
                
                properties.add(new SmProperty(String.format("Record Number"), String.valueOf(++recNum)));
                properties.add(new SmProperty("    Event Name", String.valueOf(eventName)));
                properties.add(new SmProperty("    Event DateTime", String.valueOf(eventDateTime)));
                properties.add(new SmProperty("    SCNL", scnl));
                
                properties.add(new SmProperty("    Station Number", String.valueOf(stationNum)));
                properties.add(new SmProperty("    Station Code", String.valueOf(stationCode)));
                properties.add(new SmProperty("    Station Name", String.valueOf(stationName)));
                
                properties.add(new SmProperty("    Channel", channel));
                
                properties.add(new SmProperty("    Sensor Location", sensorLocation));
                
                properties.add(new SmProperty("    Network Number", String.valueOf(networkNum)));
                properties.add(new SmProperty("    Network Code", String.valueOf(networkCode)));
                properties.add(new SmProperty("    Network Abbreviation", String.valueOf(networkAbbr)));
                
                properties.add(new SmProperty("    Point Count", String.valueOf(pointCnt)));
                properties.add(new SmProperty("    Process Type", procType));
                properties.add(new SmProperty("    Data Parm Code", String.valueOf(dataParmCode)));
                properties.add(new SmProperty("    Data Unit Code", String.valueOf(dataUnitCode)));
                properties.add(new SmProperty("    Mean Zero", String.valueOf(meanZero)));
                properties.add(new SmProperty("    Delta Time", String.valueOf(deltaT) + " Sec"));
                properties.add(new SmProperty("    Sample Rate", String.valueOf(sampleRate) + " Samples/Sec"));
                properties.add(new SmProperty("    Time Series Length", String.valueOf(timeSeriesLen) + " Sec"));
                properties.add(new SmProperty("    Max Value", String.valueOf(maxVal)));
                properties.add(new SmProperty("    Max Value Time", String.valueOf(maxValTime) + " Sec"));
                properties.add(new SmProperty("    Avg Value", String.valueOf(avgVal)));
            }
            
            DefaultTableModel model = (DefaultTableModel)tblProperties.getModel();
            model.setRowCount(0);

            // Add Property objects to table.
            for (SmProperty property : properties)
                model.addRow(new Object[]{property.getName(), property.getValue()});
        }
        //catch (IOException | FormatException | SmException ex) {
        catch (Exception ex) {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollpaneProperties = new javax.swing.JScrollPane();
        tblProperties = new javax.swing.JTable();

        setToolTipText(org.openide.util.NbBundle.getMessage(SmPropertiesViewerTC.class, "SmPropertiesViewerTC.toolTipText")); // NOI18N

        scrollpaneProperties.setPreferredSize(new java.awt.Dimension(254, 300));

        tblProperties.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollpaneProperties.setViewportView(tblProperties);
        if (tblProperties.getColumnModel().getColumnCount() > 0) {
            tblProperties.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SmPropertiesViewerTC.class, "SmPropertiesViewerTC.tblProperties.columnModel.title0")); // NOI18N
            tblProperties.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SmPropertiesViewerTC.class, "SmPropertiesViewerTC.tblProperties.columnModel.title1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollpaneProperties, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollpaneProperties, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollpaneProperties;
    private javax.swing.JTable tblProperties;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void componentOpened() {
        
        resultFilePath = Utilities.actionsGlobalContext().lookupResult(String.class);
        resultFilePath.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        resultFilePath.removeLookupListener(this);

    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends String> allStrings = resultFilePath.allInstances();
        
        if (!allStrings.isEmpty()) {
            File file = new File(allStrings.iterator().next());
            
            if (file.exists()) {
                loadRecProperties(file);
                
                /*
                // Adjust size of table columns, if necessary.
                for (int col = 0; col < tblProperties.getColumnCount(); col++) {
                    int width = 0;
                    for (int row = 0; row < tblProperties.getRowCount(); row++) {
                        TableCellRenderer renderer = tblProperties.getCellRenderer(row, col);
                        Component comp = tblProperties.prepareRenderer(renderer, row, col);
                        width = Math.max (comp.getPreferredSize().width + 
                            tblProperties.getColumnModel().getColumnMargin(), width);
                    }
                    tblProperties.getColumnModel().getColumn(col).setPreferredWidth(width);
                }
                */
                
                /*
                // Adjust size of table columns.
                for (int col = 0; col < tblProperties.getColumnCount(); col++) {
                    int width = 0;

                    DefaultTableColumnModel colModel = (DefaultTableColumnModel)tblProperties.getColumnModel();
                    TableColumn tblCol = colModel.getColumn(col);

                    // Get width of column header
                    TableCellRenderer renderer = tblCol.getHeaderRenderer();
                    if (renderer == null) {
                        renderer = tblProperties.getTableHeader().getDefaultRenderer();
                    }
                    Component comp = renderer.getTableCellRendererComponent(
                        tblProperties, tblCol.getHeaderValue(), false, false, 0, 0);

                    width = comp.getPreferredSize().width;

                    // Iterate table rows to adjust width.
                    for (int row = 0; row < tblProperties.getRowCount(); row++) {
                        renderer = tblProperties.getCellRenderer(row, col);
                        comp = tblProperties.prepareRenderer(renderer, row, col);
                        width = Math.max (comp.getPreferredSize().width + 
                            tblProperties.getColumnModel().getColumnMargin(), width);
                    }
                    tblProperties.getColumnModel().getColumn(col).setPreferredWidth(width);
                }
                */
            }
        }
    }
    
    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    public void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    private class TableHeaderMouseMotionListener extends MouseMotionAdapter {
        JTable table = null;
        Map tips = new HashMap();
        
        TableColumn curCol;
        
        
        public TableHeaderMouseMotionListener(JTable table) {
            this.table = table;
            setToolTips();
        }
        
        @SuppressWarnings("unchecked")
        private void setToolTips() {
            try {
                if (table == null)
                    return;

                for (int i = 0; i < table.getColumnCount(); i++) {
                    TableColumn col = table.getColumnModel().getColumn(i);
                    String toolTip = col.getHeaderValue().toString();
                    tips.put(col, toolTip);
                }
            }
            catch (Exception ex) {
                throw ex;
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
    
    private class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
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
    
}
