/*******************************************************************************
 * Name: Java class SmTemplateSettingsDialog.java
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

package gov.usgs.smapp.smdialogs;

import gov.usgs.smapp.SmCore;
import gov.usgs.smcommon.smclasses.SmChannel;
import gov.usgs.smcommon.smclasses.SmEpoch;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmStation;
import gov.usgs.smcommon.smclasses.SmTemplate;
import gov.usgs.smcommon.smutilities.SmXmlUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 *
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmTemplateSettingsDialog extends javax.swing.JDialog {
 
    private final ListSelListener listSelListener = new ListSelListener();
    
    private static boolean returnVal = false;
    
    /**
     * Creates new form dialog for editing template settings
     * @param parent
     * @param modal
     */
    public SmTemplateSettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        initForm();
        setLocationRelativeTo(parent);
    }
    
    public boolean showDialog()
    {
        this.setVisible(true);
        return returnVal;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblStations = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        lblTemplate = new javax.swing.JLabel();
        cboxTemplate = new javax.swing.JComboBox();
        lblTemplatesRootDir = new javax.swing.JLabel();
        txtTemplatesRootDir = new javax.swing.JTextField();
        btnBrowseTemplatesRootDir = new javax.swing.JButton();
        lblRequiredInput = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listStations = new javax.swing.JList();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Template Settings");

        lblStations.setText("Stations");

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        lblTemplate.setText("Template");

        cboxTemplate.setToolTipText("");

        lblTemplatesRootDir.setText("Templates Root Directory *");

        btnBrowseTemplatesRootDir.setText("Browse");
        btnBrowseTemplatesRootDir.setToolTipText("Browse for templates directory");
        btnBrowseTemplatesRootDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseTemplatesRootDirActionPerformed(evt);
            }
        });

        lblRequiredInput.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblRequiredInput.setText("* Required Input");

        listStations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(listStations);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblTemplatesRootDir)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStations)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 514, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTemplate)
                                    .addComponent(lblRequiredInput, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtTemplatesRootDir, javax.swing.GroupLayout.PREFERRED_SIZE, 514, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnBrowseTemplatesRootDir))
                        .addComponent(cboxTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBrowseTemplatesRootDir, btnCancel, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblTemplatesRootDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(txtTemplatesRootDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseTemplatesRootDir))
                .addGap(18, 18, 18)
                .addComponent(lblStations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTemplate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboxTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblRequiredInput)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnOk))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initForm() {
        listStations.setModel(new DefaultListModel());
        
        // Remove listeners.
        listStations.removeListSelectionListener(listSelListener);
        
        // Use preferences to set form controls.
        txtTemplatesRootDir.setText(SmPreferences.General.getTemplatesRootDir());
        updateStationsList(SmPreferences.General.getStationTemplatesFolderName());
        updateTemplatesList(SmPreferences.General.getTemplate());
        
        // Add back listeners.
        listStations.addListSelectionListener(listSelListener);
    }
    
    public String getTemplatesRootDir() {
        return txtTemplatesRootDir.getText();
    }
    
    public String getStation() {
        return listStations.getSelectedIndex() == -1 ? "" : 
            listStations.getSelectedValue().toString();
    }
    
    public String getTemplate() {
        return (cboxTemplate.getSelectedIndex() == -1) ?
            "" : cboxTemplate.getSelectedItem().toString();
    }
    
    private void updateStationsList(String stationTemplatesFolderName)
    {
        // Get station list model and clear list.
        DefaultListModel<String> model = (DefaultListModel<String>)listStations.getModel();
        model.clear();
        
        if (txtTemplatesRootDir.getText().isEmpty())
            return;
        
        File templatesDir = new File(txtTemplatesRootDir.getText());
        
        if (templatesDir.isDirectory()) {
            File[] stations = templatesDir.listFiles();
            
            if (stations != null)
            {               
                // Form reg ex for station, with pattern of network, period, and station.
                Pattern p = Pattern.compile("^(.+)(\\.)(.+)$");
                
                for (File station : stations)
                {
                    if (station.isDirectory()) {
                        String stationName = station.getName();
                        
                        Matcher m = p.matcher(stationName);
                        if (m.find()) {
                            model.addElement(stationName);
                        }
                    }
                }

                if (stationTemplatesFolderName != null && !stationTemplatesFolderName.isEmpty())
                    listStations.setSelectedValue(stationTemplatesFolderName, true);
                
            }
        }
    }
    
    private void updateTemplatesList(String template)
    {
        // Clear template combo box.
        cboxTemplate.removeAllItems();
        
        if (listStations.getSelectedIndex() == -1)
            return;
        
        String station = listStations.getSelectedValue().toString();
        
        File stationDir = new File(txtTemplatesRootDir.getText() + File.separator + station);
       
        if (stationDir.isDirectory())
        {
            cboxTemplate.addItem(SmGlobal.NO_SELECTION);
             
            File[] files = stationDir.listFiles();
            
            for (File file : files)
            {
                if (file.isFile() && file.getName().endsWith(".xml"))
                {
                    cboxTemplate.addItem(file.getName());
                }
            }
            
            if (template != null && !template.isEmpty()) {
                cboxTemplate.setSelectedItem(template);
            }
        }
    }
    
    private boolean validateInput()
    {
        StringBuilder strBuilder = new StringBuilder();
        
        if (txtTemplatesRootDir.getText().isEmpty())
            strBuilder.append("-Templates directory not specified.\n ");
        
        if (strBuilder.length() > 0)
        {
            JOptionPane.showMessageDialog(this,
                "Validation Error(s) detected:\n"+strBuilder.toString(),
                "Event Interface",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void readXML_File() {
        /*
        String srcDate = "Sun Aug 24, 2014 10:20:15";
        
        DateTimeFormatter fmtOrig = DateTimeFormat.forPattern("E MMM dd, yyyy HH:mm:ss");
        DateTimeFormatter fmtNew = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
        
        //DateTime dt = new DateTime();
        DateTime dt = new DateTime(fmtOrig.parseDateTime(srcDate));
        
        String strOrig = fmtOrig.print(dt);
        String strNew = fmtNew.print(dt);
        
        SmCore.addMsgToStatusViewer("Orig Date Time: " + strOrig);
        SmCore.addMsgToStatusViewer("New Date Time: " + strNew);
        */
        
        try {
            String templateFilePath = 
                txtTemplatesRootDir.getText() + File.separator + 
                listStations.getSelectedValue() + File.separator +
                cboxTemplate.getSelectedItem().toString();
            
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");

            if (SmXmlUtils.isXML_FileParsible(templateFilePath)) {
                SmTemplate smTemplate = (templateFilePath.isEmpty()) ? null :
                    SmXmlUtils.readXML_TemplateFile(templateFilePath);
                
                if (smTemplate != null) {
                    SmStation smStation = smTemplate.getSmStation();

                    SmCore.addMsgToStatusViewer("Network Code: " + smStation.getNetworkCode());
                    SmCore.addMsgToStatusViewer("Station Code: " + smStation.getStationCode());

                    ArrayList<SmEpoch> smEpochs = smStation.getSmEpochs();
                    
                    for (SmEpoch smEpoch : smEpochs) {
                        String strStartUTC = smEpoch.getStartUTC().toString();
                        String strEndUTC = smEpoch.getEndUTC().toString();
                        
                        DateTime dtStartUTC = new DateTime(strStartUTC);
                        DateTime dtEndUTC = new DateTime(strEndUTC);
                        
                        SmCore.addMsgToStatusViewer("Epoch Start UTC: " + fmt.print(dtStartUTC));
                        SmCore.addMsgToStatusViewer("Epoch End UTC: " + fmt.print(dtEndUTC));
                        
                        int compareDateVal = dtStartUTC.compareTo(dtEndUTC);
                        
                        if (compareDateVal > 0)
                            SmCore.addMsgToStatusViewer("Start UTC > End UTC");
                        else if (compareDateVal < 0)
                            SmCore.addMsgToStatusViewer("Start UTC < End UTC");
                        else if (compareDateVal == 0)
                            SmCore.addMsgToStatusViewer("Start UTC = End UTC");
                        
                        ArrayList<SmChannel> smChannels = smEpoch.getSmChannels();
                        
                        for (SmChannel smChannel : smChannels) {
                            SmCore.addMsgToStatusViewer("Channel: " + smChannel.getChannel());
                            SmCore.addMsgToStatusViewer("Seed: " + smChannel.getSeed());
                            SmCore.addMsgToStatusViewer("LCode: " + smChannel.getLCode());
                            SmCore.addMsgToStatusViewer("Location: " + smChannel.getLocation());
                            SmCore.addMsgToStatusViewer("Inclination: " + smChannel.getInclination());
                            SmCore.addMsgToStatusViewer("Color: " + smChannel.getColor().toString());
                        }
                    }
                }
                else {
                    SmCore.addMsgToStatusViewer("SmTemplate object is null");
                }
            }
            else {
                SmCore.addMsgToStatusViewer("Unable to parse file " + templateFilePath + ".");
            }
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private void btnBrowseTemplatesRootDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseTemplatesRootDirActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        String templatesRootDir = SmPreferences.General.getTemplatesRootDir();
        if (!templatesRootDir.isEmpty())
            fileChooser.setCurrentDirectory(new File(templatesRootDir));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtTemplatesRootDir.setText(fileChooser.getSelectedFile().getAbsolutePath());
            updateStationsList(null);
            updateTemplatesList(null);
        }
    }//GEN-LAST:event_btnBrowseTemplatesRootDirActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed

        if (!validateInput())
            return;
        
        try
        {
            returnVal = true;

            this.setVisible(false);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),"Event Interface",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        returnVal = false;
        
        this.setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private class ListSelListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) { //This line prevents double events
                updateTemplatesList(null);
            }  
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowseTemplatesRootDir;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cboxTemplate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblRequiredInput;
    private javax.swing.JLabel lblStations;
    private javax.swing.JLabel lblTemplate;
    private javax.swing.JLabel lblTemplatesRootDir;
    private javax.swing.JList listStations;
    private javax.swing.JTextField txtTemplatesRootDir;
    // End of variables declaration//GEN-END:variables
}
