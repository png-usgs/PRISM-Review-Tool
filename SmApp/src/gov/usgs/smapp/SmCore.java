/*******************************************************************************
 * Name: Java class SmCore.java
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

import gov.usgs.smapp.smactions.datatypeactions.DataTypePanel;
import gov.usgs.smapp.smactions.datatypeactions.DataTypePanelAction;
import gov.usgs.smapp.smactions.fileactions.TemplatePanel;
import gov.usgs.smapp.smactions.fileactions.TemplatePanelAction;
import gov.usgs.smapp.smactions.navactions.NavPanel;
import gov.usgs.smapp.smactions.navactions.NavPanelAction;
import gov.usgs.smapp.smtopcomponents.SmNodeExplorerTC;
import gov.usgs.smapp.smtopcomponents.SmSeismicTraceViewerTC;
import gov.usgs.smapp.smtopcomponents.SmFAS_ViewerTC;
import gov.usgs.smapp.smtopcomponents.SmPropertiesViewerTC;
import gov.usgs.smapp.smtopcomponents.SmStatusViewerTC;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmNode;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmTemplate;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.getAction;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import gov.usgs.smcommon.smutilities.SmUtils;
import gov.usgs.smcommon.smutilities.SmXmlUtils;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.joda.time.DateTime;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.NotifyDescriptor;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.findTopComponent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * This class defines the core static functions, constants, and other constructs
 * that are used throughout the project suite.
 * 
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmCore {
    
    private static final ArrayList<VxChartsBinGroup> vXChartsBinGroups = 
        new ArrayList<>();
    private static int vXChartsBinGroupsIndx = 0;
    
    //private static final String FILE_SEP = "\\\\";
    private static final String FILE_SEP = Pattern.quote(File.separator);
    private static final String REG_EX_REGULAR = 
        String.format("(?i)(^.+[%s](.+))[%s](.+)[%s](.+)[%s](V[\\d])[%s](.+)", 
            FILE_SEP,FILE_SEP,FILE_SEP,FILE_SEP,FILE_SEP);
    private static final String REG_EX_TROUBLE = 
        String.format("(?i)(^.+[%s](.+))[%s](.+)[%s](.+)[%s](%s)[%s](V[\\d])[%s](.+)", 
            FILE_SEP,FILE_SEP,FILE_SEP,FILE_SEP,SmGlobal.TROUBLE,FILE_SEP,FILE_SEP);

    // Regular Pattern:
    //   Events Root Dir: Group 1
    //   Event: Group 3
    //   Station: Group 4
    //   Vx: Group 5
    //   Filename: Group 6
    //
    // Trouble Pattern:
    //   Events Root Dir: Group 1
    //   Event: Group 3
    //   Station: Group 4
    //   Trouble: Group 5
    //   Vx: Group 6
    //   Filename: Group 7
            
        
    public static enum CheckedState {CHECKED,UNCHECKED,ALL}
    
    /**
     * Gets the static list, VxChartsBinGroups, representing a list of VxChartsBinGroup objects.
     * @return the static constant, VsChartsBinGroups.
     */
    public static ArrayList<VxChartsBinGroup> getVxChartsBinGroups() {return vXChartsBinGroups;}
    
    /**
     * Gets the current index position of the list, VxChartsBinGroups.
     * @return the current index position in the VxChartsBinGroups.
     */
    public static int getVxChartsBinGroupsIndx() {return vXChartsBinGroupsIndx;}
    
    /**
     * Sets the current index position of the list, VxChartsBinGroups.
     * @param indx the index position of VxChartsBinGroups.
     */
    public static void setVxChartsBinGroupsIndx(int indx) {vXChartsBinGroupsIndx=indx;}
    
    /**
     * Configures a JFileChooser object representing a file dialog.
     * @param fileChooser the JFileChooser object to configure.
     */
    public static void configureFileChooser(JFileChooser fileChooser)
    {
        fileChooser.setDialogTitle("Select File(s)");
        fileChooser.setMultiSelectionEnabled(true);
             
        String lastVxDir = SmPreferences.General.getLastVxDir();
        
        // Open directory from last visited Vx directory.
        if (!lastVxDir.isEmpty())
        {
            if (Files.isDirectory(Paths.get(lastVxDir)))
                fileChooser.setCurrentDirectory(new File(lastVxDir));
        }
        else
        {
            String initDir = System.getProperty("user.home");
            fileChooser.setCurrentDirectory(new File(initDir));
        }
       
        fileChooser.addChoosableFileFilter(new V1_FileFilter());
        fileChooser.addChoosableFileFilter(new V2_Acceleration_FileFilter());
        fileChooser.addChoosableFileFilter(new V2_Displacement_FileFilter());
        fileChooser.addChoosableFileFilter(new V2_Velocity_FileFilter());
    }
    
    /**
     * Class that extends the FileFilter class to filter V1 files. 
     */
    private static class V1_FileFilter extends FileFilter {
 
        /**
         * Accepts (i.e., filters) files that are either directories or
         * V1 files.
         * @param f a File object.
         * @return true, if File object refers to either a directory or a V1 file, 
         * or false otherwise.
         */
        @Override
        public boolean accept(File f) {
            String fileName = f.getName();
            String regExCosmosFileType = String.format("(?i)(^.+)([\\.](V[\\d][cC]?)$)");
            Pattern p = Pattern.compile(regExCosmosFileType);
            Matcher m = p.matcher(fileName);
            
            return f.isDirectory() || m.find();
        }

        /**
         * Gets a description of the filter.
         * @return a string representing the description of the filter.
         */
        @Override
        public String getDescription() {
            return "V1 Files";
        }
    }
    
    /**
     * Class that extends the FileFilter class to filter V2 acceleration files. 
     */
    private static class V2_Acceleration_FileFilter extends FileFilter {
 
        /**
         * Accepts (i.e., filters) files that are either directories or
         * V2 acceleration files.
         * @param f a File object.
         * @return true, if File object refers to either a directory 
         * or a V2 acceleration file, or false otherwise.
         */
        @Override
        public boolean accept(File f) {
            String fileName = f.getName();
            String regExCosmosFileType = String.format("(?i)(^.+)([\\.]%s)([\\.](V[\\d][cC]?)$)",
                SmGlobal.CosmosV2DataType.ACC.toString());
            
            Pattern p = Pattern.compile(regExCosmosFileType);
            Matcher m = p.matcher(fileName);
            
            return f.isDirectory() || m.find();
        }

        /**
         * Gets a description of the filter.
         * @return a string representing the description of the filter.
         */
        @Override
        public String getDescription() {
            return "V2 Acceleration Files";
        }
    }
    
    /**
     * Class that extends the FileFilter class to filter V2 velocity files. 
     */
    private static class V2_Velocity_FileFilter extends FileFilter {
 
        /**
         * Accepts (i.e., filters) files that are either directories or
         * V2 velocity files.
         * @param f a File object.
         * @return true, if File object refers to either a directory or 
         * a V2 velocity file, or false otherwise.
         */
        @Override
        public boolean accept(File f) {
            String fileName = f.getName();
            String regExCosmosFileType = String.format("(?i)(^.+)([\\.]%s)([\\.](V[\\d][cC]?)$)",
                SmGlobal.CosmosV2DataType.VEL.toString());
            Pattern p = Pattern.compile(regExCosmosFileType);
            Matcher m = p.matcher(fileName);
            
            return f.isDirectory() || m.find();
        }

        /**
         * Gets a description of the filter.
         * @return a string representing the description of the filter.
         */
        @Override
        public String getDescription() {
            return "V2 Velocity Files";
        }
    }
    
    /**
     * Class that extends the FileFilter class to filter V2 displacement files. 
     */
    private static class V2_Displacement_FileFilter extends FileFilter {
 
        /**
         * Accepts (i.e., filters) files that are either directories or
         * V2 displacement files.
         * @param f a File object.
         * @return true, if File object refers to either a directory or 
         * a V2 displacement file, or false otherwise.
         */
        @Override
        public boolean accept(File f) {
            String fileName = f.getName();
            String regExCosmosFileType = String.format("(?i)(^.+)([\\.]%s)([\\.](V[\\d][cC]?)$)",
                SmGlobal.CosmosV2DataType.DIS.toString());
            Pattern p = Pattern.compile(regExCosmosFileType);
            Matcher m = p.matcher(fileName);
            
            return f.isDirectory() || m.find();
        }

        /**
         * Gets a description of the filter.
         * @return a string representing the description of the filter.
         */
        @Override
        public String getDescription() {
            return "V2 Displacement Files";
        }
    }
    
    /**
     * Resets the Seismic Trace and FAS viewers, removing the contents 
     * contained in each viewer.
     * @throws Exception if an error is detected.
     */
    public static void resetChartViewers() throws Exception
    {
        try {
            SmSeismicTraceViewerTC tcSeismic = 
                findTopComponent(SmSeismicTraceViewerTC.class);

            SmFAS_ViewerTC tcSpectral = 
                findTopComponent(SmFAS_ViewerTC.class);

            if (tcSeismic != null) {
                JPanel pnlSeismic = tcSeismic.getSeismicPanel();
                pnlSeismic.removeAll();
    //            pnlSeismic.repaint();
            }

            if (tcSpectral != null) {
                JPanel pnlSpectral = tcSpectral.getSpectralPanel();
                pnlSpectral.removeAll();
    //            pnlSpectral.repaint();
            }
        }
        catch (Exception ex) {
            throw new Exception("Reset Chart Viewers Error: " + ex.getMessage());
        }
    }
    
    /**
     * Resets the vXChartsBinGroups object, clearing the list and resetting the
     * index to zero.
     * @throws Exception if an error is detected.
     */
    public static void resetVxChartsBinGroups() throws Exception
    {
        try {
            vXChartsBinGroups.clear();
            vXChartsBinGroupsIndx = 0;
        }
        catch (Exception ex) {
            throw new Exception("Reset VxChartsBinGroups Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Resets the navigation panel, enabling or disabling buttons according to whether
     * or not items exist in the vXChartsBinGroups list.
     * @throws Exception if error is detected.
     */
    public static void resetNavigationPanel() throws Exception
    {
        try {
            NavPanelAction navPanelAction = (NavPanelAction)getAction("Navigation",
                "gov-usgs-smapp-smactions-navactions-NavPanelAction");

            if (navPanelAction == null)
                return;

            NavPanel panel = navPanelAction.getNavPanel();

            if (panel == null)
                return;

            JButton btnNavFirst = panel.getNavFirstBtn();
            JButton btnNavPrevious = panel.getNavPreviousBtn();
            JButton btnNavNext = panel.getNavNextBtn();
            JButton btnNavLast = panel.getNavLastBtn();

            btnNavFirst.setEnabled(false);
            btnNavPrevious.setEnabled(false);

            if (vXChartsBinGroups.isEmpty()) {
                btnNavNext.setEnabled(false);
                btnNavLast.setEnabled(false);

                panel.setNavigationText("");
            }
            else {
                if (vXChartsBinGroups.size() == 1) {
                    btnNavNext.setEnabled(false);
                    btnNavLast.setEnabled(false);
                }
                else {
                    btnNavNext.setEnabled(true);
                    btnNavLast.setEnabled(true);
                }

                panel.setNavigationText("(" + 
                    (vXChartsBinGroupsIndx+1) + " of " + vXChartsBinGroups.size() + 
                    ") " + vXChartsBinGroups.get(vXChartsBinGroupsIndx).getGroupName());
            }
        }
        catch (Exception ex) {
            throw new Exception("Reset Navigation Panel Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Resets the properties table viewer, setting the row count to zero to clear
     * the contents of the table.
     * @throws Exception if an error is detected.
     */
    public static void resetPropertiesTable() throws Exception
    {
        try {
            SmPropertiesViewerTC tc = 
                findTopComponent(SmPropertiesViewerTC.class);

            if (tc != null) {
                DefaultTableModel model = (DefaultTableModel)tc.getTableProperties().getModel();
                model.setRowCount(0);
            }
        }
        catch (Exception ex) {
            throw new Exception("Reset Properties Viewer Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Adds a JPanel to the Seismic Trace Viewer.
     * @param panel a JPanel to add to the viewer.
     * @throws Exception if an error is detected.
     */
    public static void addToSeismicTraceViewer(JPanel panel) throws Exception {
        try {
            SmSeismicTraceViewerTC tcSeismic = 
                findTopComponent(SmSeismicTraceViewerTC.class);

            if (tcSeismic != null) {
                JPanel pnlSeismic = tcSeismic.getSeismicPanel();
                pnlSeismic.add(panel);
                pnlSeismic.updateUI();
            }
        }
        catch (Exception ex) {
            throw new Exception("Add to Seismic Chart Viewer Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Adds a JPanel to the FAS Viewer.
     * @param panel a JPanel to add to the viewer.
     * @throws Exception if an error is detected.
     */
    public static void addToFAS_Viewer(JPanel panel) throws Exception {
        try {
            SmFAS_ViewerTC tcSpectral = 
                findTopComponent(SmFAS_ViewerTC.class);

            if (tcSpectral != null) {
                JPanel pnlSpectral = tcSpectral.getSpectralPanel();
                pnlSpectral.add(panel);
                pnlSpectral.updateUI();
            }
        }
        catch (Exception ex) {
            throw new Exception("Add to FAS Viewer error:\n" + ex.getMessage());
        }
    }
        
    /**
     * Creates and runs a background task to create graphs for selected leaf nodes 
     * displayed in the Node Explorer.
     * @param owner
     * @throws Exception if an error is detected.
     */
    public static void runGraphs(Object owner) throws Exception {
        try {
            final SmTemplate smTemplate = SmCore.getSelectedSmTemplate();
            
            final ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

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
            
            //ProgressHandle handle = ProgressHandleFactory.createHandle("Running Graphs ...");
            ProgressHandle handle = ProgressHandle.createHandle("Running Graphs ...");
            RunGraphsByGroupTask task = new RunGraphsByGroupTask(chartAPI, 
                eventNodes, smTemplate, adjustPoints, owner, handle);
            task.execute();
        }
        catch (Exception ex) {
            throw new Exception("Run Graphs Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Updates the Node Explorer by recreating the root node tree structure and
     * setting the tree's default check mode states.
     * @throws Exception if an error is detected.
     */
    public static void updateNodeExplorer() throws Exception {
        try {
            SmNodeExplorerTC tcSmNodeExplorer =
                findTopComponent(SmNodeExplorerTC.class);
        
            if (tcSmNodeExplorer == null)
                return;

            // Recreate root node structure.
            SmNode nodeRoot = SmCore.recreateTreeNodeStructure();

            // Set tree model to new root node.
            CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
            DefaultTreeModel treeModel = (DefaultTreeModel)treeContents.getModel();
            treeModel.setRoot(nodeRoot);
            treeContents.setRootVisible(false);
            treeContents.expandAll();

            // Turn off the tree listeners.
            tcSmNodeExplorer.setTreeListeners(false);

            // Set the checkboxtree's checking mode to simple.
            treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.SIMPLE);

            // Set checkbox tree checked states starting with root node.
            SmCore.setCheckboxTreeCheckedState(nodeRoot);

            // Reset the checkboxtree's checking mode to propogate.
            treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);

            // Turn tree listeners back on.
            tcSmNodeExplorer.setTreeListeners(true);
        }
        catch (Exception ex) {
            throw new Exception("Update Node Explorer Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Updates the SM Graphing Tool by, first, resetting certain components of the tool, 
     * then updating the Node Explorer refreshing it with the current tree node
     * structure, and, lastly, re-running the graphs for selected leaf nodes.
     * @throws Exception if an error is detected.
     */
    public static void updateSmGraphingTool() throws Exception {

        try {
            // Reset VxChartsBinGroups.
            SmCore.resetVxChartsBinGroups();

            // Reset chart viewers.
            SmCore.resetChartViewers();

            // Reset navigation controls.
            SmCore.resetNavigationPanel();

            // Reset properties table.
            SmCore.resetPropertiesTable();

            // Update node explorer.
            SmCore.updateNodeExplorer();

            // Rerun charts.
            SmCore.runGraphs(null);
        }
        catch (Exception ex) {
            throw new Exception("Update SmGraphing Tool Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Updates the panels displayed in the Seismic and FAS viewers.
     * @throws Exception if an error is detected.
     */
    public static void updateChartViewPanels() throws Exception
    {
        try {
            // Reset chart viewers.
            SmCore.resetChartViewers();
        
            if (vXChartsBinGroups.isEmpty())
                return;

            // Create chart panel group lists.
            ArrayList<JPanel> pnlSeismicGroupList = new ArrayList<>();
            ArrayList<JPanel> pnlSpectralGroupList = new ArrayList<>();
        
            // Get DatatypePanelAction instance.
            DataTypePanelAction action = (DataTypePanelAction)getAction("Datatype",
                "gov-usgs-smapp-smactions-datatypeactions-DataTypePanelAction");

            if (action == null) 
                return;

            DataTypePanel pnlDataType = action.getDataTypePanel();
        
            // Get panel controls.
            JButton btnAcceleration = pnlDataType.getAccelerationBtn();
            JButton btnVelocity = pnlDataType.getVelocityBtn();
            JButton btnDisplacement = pnlDataType.getDisplacementBtn();
            JButton btnAll = pnlDataType.getAllBtn();

            // Set default datatype button, if none were set previously.
            if (!btnAcceleration.isSelected() && !btnVelocity.isSelected() &&
                !btnDisplacement.isSelected() && !btnAll.isSelected())
                btnAll.setSelected(true);
        
            // Create list of panel groups.
            for (VxChartsBinGroup vXChartsBinGroup : vXChartsBinGroups)
            {
                JPanel pnlSeismicGroup = new JPanel();
                pnlSeismicGroup.setName("pnlSeismicGroup"+vXChartsBinGroup.getGroupName());
                pnlSeismicGroup.setLayout(new BoxLayout(pnlSeismicGroup, BoxLayout.PAGE_AXIS));

                JPanel pnlSpectralGroup = new JPanel();
                pnlSpectralGroup.setName("pnlSpectralGroup"+vXChartsBinGroup.getGroupName());
                pnlSpectralGroup.setLayout(new BoxLayout(pnlSpectralGroup, BoxLayout.PAGE_AXIS));

                // Add header to each panel group.
                pnlSeismicGroup.add(vXChartsBinGroup.createHeader());
                pnlSpectralGroup.add(vXChartsBinGroup.createHeader());
                
                // Get V1 and V2 chart bins.
                V1ChartsBin v1ChartsBin = vXChartsBinGroup.getV1ChartsBin();
                V2ChartsBin v2ChartsBin = vXChartsBinGroup.getV2ChartsBin();

                 // Add V1 charts, if any.
                if (v1ChartsBin != null)
                {
                    if (btnAcceleration.isSelected() || btnAll.isSelected())
                    {
                        for (JPanel panel : v1ChartsBin.getChartsSeismicAcc())
                            pnlSeismicGroup.add(panel);
                    }

                    for (JPanel panel : v1ChartsBin.getChartsSpectral())
                        pnlSpectralGroup.add(panel);
                }

                // Add V2 charts, if any.
                if (v2ChartsBin != null)
                {
                    if (btnAcceleration.isSelected())
                    {
                        for (JPanel panel : v2ChartsBin.getChartsSeismicAcc())
                            pnlSeismicGroup.add(panel);
                    }
                    else if (btnVelocity.isSelected())
                    {
                        for (JPanel panel : v2ChartsBin.getChartsSeismicVel())
                            pnlSeismicGroup.add(panel);
                    }
                    else if (btnDisplacement.isSelected())
                    {
                        for (JPanel panel : v2ChartsBin.getChartsSeismicDis())
                            pnlSeismicGroup.add(panel);
                    }
                    else if (btnAll.isSelected())
                    {
                        for (JPanel panel : v2ChartsBin.getChartsSeismicAcc())
                            pnlSeismicGroup.add(panel);
                        for (JPanel panel : v2ChartsBin.getChartsSeismicVel())
                            pnlSeismicGroup.add(panel);
                        for (JPanel panel : v2ChartsBin.getChartsSeismicDis())
                            pnlSeismicGroup.add(panel);
                    }

                    for (JPanel panel : v2ChartsBin.getChartsSpectral())
                        pnlSpectralGroup.add(panel);
                }

                pnlSeismicGroupList.add(pnlSeismicGroup);
                pnlSpectralGroupList.add(pnlSpectralGroup);
            }
       
            // Add panels by specified group index to chart viewers.
            addToSeismicTraceViewer(pnlSeismicGroupList.get(vXChartsBinGroupsIndx));
            addToFAS_Viewer(pnlSpectralGroupList.get(vXChartsBinGroupsIndx));

            // Set enable state of data type panel.
            SmSeismicTraceViewerTC tc =
                findTopComponent(SmSeismicTraceViewerTC.class);

            if (tc != null) 
                pnlDataType.setPanelEnabledState(tc.isShowing());
        }
        catch (Exception ex) {
            throw new Exception("Update Chart View Panels Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Updates the list of templates in the corresponding template combo-box control.
     * @throws Exception if an error is detected.
     */
    public static void updateTemplateList() throws Exception
    {   
        try {
            TemplatePanelAction templatePanelAction = 
                (TemplatePanelAction)getAction("File",
                "gov-usgs-smapp-smactions-fileactions-TemplatePanelAction");
            TemplatePanel panel = templatePanelAction.getTemplatePanel();
            panel.setCBoxTemplate(null);
        }
        catch (Exception ex) {
            throw new Exception("Update Template List Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Creates an SmTemplate object based on selected template in the template panel.
     * @return SmTemplate object representing selected template 
     * @throws Exception
     */
    public static SmTemplate getSelectedSmTemplate() throws Exception {
        try {
            TemplatePanelAction templatePanelAction = 
                (TemplatePanelAction)getAction("File",
                "gov-usgs-smapp-smactions-fileactions-TemplatePanelAction");
            
            TemplatePanel panel = templatePanelAction.getTemplatePanel();
            
            JComboBox cboxTemplate = panel.getCBoxTemplate();
            
            if (cboxTemplate.getSelectedIndex() <= 0)
                return null;
        
            String templateFilePath = 
                SmPreferences.General.getTemplatesRootDir() + File.separator + 
                SmPreferences.General.getStationTemplatesFolderName() + File.separator +
                cboxTemplate.getSelectedItem().toString();
            
            if (!SmXmlUtils.isXML_FileParsible(templateFilePath)) {
                throw new Exception("Unable to parse file " + templateFilePath + ".");
            }

            SmTemplate smTemplate = (templateFilePath.isEmpty()) ? null :
                SmXmlUtils.readXML_TemplateFile(templateFilePath);
            
            return smTemplate;
        }
        catch (Exception ex) {
            throw new Exception("Get Selected SmTemplate Error:\n" + ex.getMessage());
        }
    }
    
    /**
     * Clears the message contents displayed in the Status Viewer.
     */
    public static void clearStatusViewer() {
        SmStatusViewerTC tcStatusViewer = 
            findTopComponent(SmStatusViewerTC.class);

        if (tcStatusViewer != null)
            tcStatusViewer.clear();
    }
    
    /**
     * Adds a message to the Status Viewer.
     * @param msg a string representing a message to add to the Status Viewer.
     */
    public static void addMsgToStatusViewer(String msg) {
        SmStatusViewerTC tcStatusViewer = 
                findTopComponent(SmStatusViewerTC.class);

        if (tcStatusViewer != null)
            tcStatusViewer.appendText(msg+"\n");
    }
    
    /**
     * Determines if the specified file has a string pattern that would indicate
     * that it is located in the trouble folder.
     * @param filePath string representing the file path to check.
     * @return true if file is determined to be in a trouble folder, false otherwise.
     */
    public static boolean inTroubleFolder(String filePath) {
        return filePath.matches(REG_EX_TROUBLE);
    }
    
    /**
     * Extracts the events root directory portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the events root directory.
     */
    public static String extractEventsRootDir(String filePath) {   
        String eventsRootDir = null;
        
        if (inTroubleFolder(filePath)) {
            Pattern p = Pattern.compile(REG_EX_TROUBLE);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                eventsRootDir = m.group(1);
            }
        }
        else {
            Pattern p = Pattern.compile(REG_EX_REGULAR);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                eventsRootDir = m.group(1);
            }
        }
        
        return eventsRootDir;
    }
    
    /**
     * Extracts the event name portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the event name.
     */
    public static String extractEventName(String filePath) {
        String eventName = null;
        
        if (inTroubleFolder(filePath)) {
            Pattern p = Pattern.compile(REG_EX_TROUBLE);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                eventName = m.group(3);
            }
        }
        else {
            Pattern p = Pattern.compile(REG_EX_REGULAR);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                eventName = m.group(3);
            }
        }
        
        return eventName;
    }
    
    /**
     * Extracts the station name portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the station name.
     */
    public static String extractStationName(String filePath) {
        String stationName = null;
        
        if (inTroubleFolder(filePath)) {
            Pattern p = Pattern.compile(REG_EX_TROUBLE);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                stationName = m.group(4);
            }
        }
        else {
            Pattern p = Pattern.compile(REG_EX_REGULAR);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                stationName = m.group(4);
            }
        }
        
        return stationName;
    }
    
    /**
     * Extracts the network code portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the network code.
     */
    public static String extractNetworkCode(String filePath) {
        String stationName = extractStationName(filePath);
        
        String regEx = String.format("(?i)(^.+)[\\.](.+$)");
        
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(stationName);
        if (m.find()) {
            return m.group(1);
        }
        
        return null;
    }
    
    /**
     * Extracts the station code portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the station code.
     */
    public static String extractStationCode(String filePath) {
        String stationName = extractStationName(filePath);

        String regEx = String.format("(?i)(^.+)[\\.](.+$)");
        
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(stationName);
        if (m.find()) {
            return m.group(2);
        }
        
        return null;
    }
    
    /**
     * Extracts the COSMOS folder type (i.e., V0, V1, V2, or V3) portion of the string 
     * from the specified file path.
     * @param filePath string representing the file path.
     * @return string representing the COSMOS folder type.
     */
    public static String extractVxName(String filePath) {
        String vxName = null;
        
        if (inTroubleFolder(filePath)) {
            Pattern p = Pattern.compile(REG_EX_TROUBLE);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                vxName = m.group(6);
            }
        }
        else {
            Pattern p = Pattern.compile(REG_EX_REGULAR);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                vxName = m.group(5);
            }
        }
        
        return vxName;
    }
    
    /**
     * Extracts the file name portion of the string from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the file name.
     */
    public static String extractFileName(String filePath) {
        String fileName = null;
        
        if (inTroubleFolder(filePath)) {
            Pattern p = Pattern.compile(REG_EX_TROUBLE);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                fileName = m.group(7);
            }
        }
        else {
            Pattern p = Pattern.compile(REG_EX_REGULAR);
            Matcher m = p.matcher(filePath);
            if (m.find()) {
                fileName = m.group(6);
            }
        }
        
        return fileName;
    }
    
    /**
     * Extracts the COSMOS file type (i.e., V0, V1, V2, or V3) from the specified
     * file path. Any of the file types can end with an optional 'c' or 'C'.
     * @param filePath string representing the file path.
     * @return string representing the COSMOS file type.
     */
    public static String extractCosmosFileType(String filePath) {
        String regExCosmosFileType = String.format("(?i)(^.+)([\\.](V[\\d][cC]?)$)");
        
        Pattern p = Pattern.compile(regExCosmosFileType);
        Matcher m = p.matcher(filePath);

        if (m.find()) {
            String fileType = m.group(3);
            
            if (fileType.toUpperCase().startsWith(SmGlobal.CosmosFileType.V0.toString()))
                return SmGlobal.CosmosFileType.V0.toString();
            else if (fileType.toUpperCase().startsWith(SmGlobal.CosmosFileType.V1.toString()))
                return SmGlobal.CosmosFileType.V1.toString();
            else if (fileType.toUpperCase().startsWith(SmGlobal.CosmosFileType.V2.toString()))
                return SmGlobal.CosmosFileType.V2.toString();
            else if (fileType.toUpperCase().startsWith(SmGlobal.CosmosFileType.V3.toString()))
                return SmGlobal.CosmosFileType.V3.toString();
            else
                return null;
        }
        
        return null;
    }
    
    /**
     * Extracts the COSMOS V2 file data type (i.e., ACC, VEL, or DIS) from the specified
     * file path.
     * @param filePath string representing the file path.
     * @return string representing the COSMOS V2 file data type.
     */
    public static String extractCosmosV2DataType(String filePath) {
        String regExCosmosV2DataType = String.format("(?i)(^.+)([\\.d]([\\w]{3}))([\\.](V[\\d][cC]?)$)");
        
        Pattern p = Pattern.compile(regExCosmosV2DataType);
        Matcher m = p.matcher(filePath);

        if (m.find()) {
            String v2DataType = m.group(3);
      
            if (v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.ACC.toString()) ||
                v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.VEL.toString()) ||
                v2DataType.equalsIgnoreCase(SmGlobal.CosmosV2DataType.DIS.toString()))
                return v2DataType;
            else
                return null;
        }
        
        return null;
    }
    
    /**
     * Prints the structure of specified node to the Status Viewer.
     * @param node the SmNode object to print.
     */
    public static void printNodeStructure(SmNode node) {
       
        StringBuilder str = new StringBuilder();
        
        for (int i=0; i < node.getLevel(); i++)
            str.append("  ");
        
        str.append(node.toString()).append(" checked: ").append(node.getChecked())
            .append(" leaf: ").append(node.isLeaf());
        
        SmCore.addMsgToStatusViewer(str.toString());
        
        if (!node.isLeaf()) {
            Enumeration nodeElements = node.children();
            while (nodeElements.hasMoreElements()) {
                SmNode nodeElement = (SmNode)nodeElements.nextElement();              
                printNodeStructure(nodeElement);
            }
        }
    }
    
    /**
     * Traverse tree node structure, looking for nodes that have been moved. Each
     * moved node is added to the remove node list.
     * @param node a SmNode representing the starting node (e.g., root node) to begin searching
     * @param removeNodeList list containing all removed nodes from the specified node.
     */
    public static void traverseNodeStructure(SmNode node, ArrayList<SmNode> removeNodeList) {
        
        if (node.isLeaf()) {      

            File inFile = new File(node.getFilePath());
            
            // Check if the infile exists. If not, the assumption here is
            // that the file has been moved from the trouble folder into a 
            // regular folder.
            if (!inFile.exists()) {
                // Check if file is in a regular folder.
                //String FILE_SEP = "\\\\";
                String regEx = String.format("(?i)%s%s", SmGlobal.TROUBLE,FILE_SEP);
                String outFilePath = inFile.getPath().replaceFirst(regEx, "");
                
                File outFile = new File(outFilePath);
                
                if (outFile.exists()) {
                    
                    // Get Vx node containing inNode.
                    SmNode inNodeVx = (SmNode)node.getParent();
                    
                    // Get Trouble node containing inNodeVx.
                    SmNode inNodeTrouble = (inNodeVx == null) ? null : 
                        (SmNode)inNodeVx.getParent();
                        
                    // Get station node containing inNodeTrouble.
                    SmNode inNodeStation = (inNodeTrouble == null) ? null :
                        (SmNode)inNodeTrouble.getParent();
                    
                    if (inNodeStation == null)
                        return;
                       
                    // Check if station node contains the Vx node. If not, create it.
                    SmNode nodeTargetVx = null;
                    
                    Enumeration inNodeStationElements = inNodeStation.children();
                    while (inNodeStationElements.hasMoreElements()) {
                        SmNode inNodeStationElement = (SmNode)inNodeStationElements.nextElement();
                        if (inNodeStationElement.toString().equals(inNodeVx.toString())) {
                            nodeTargetVx = inNodeStationElement;
                            break;
                        }
                    }
                    
                    if (nodeTargetVx == null) {
                        nodeTargetVx = new SmNode(inNodeVx.toString(),
                            inNodeVx.getAllowsChildren(),inNodeVx.getChecked());
                        inNodeStation.add(nodeTargetVx);
                    }
                    
                    // Add file to target Vx folder.
                    SmFile outSmFile = new SmFile(outFile);
//                    SmNode outNode = new SmNode(outSmFile.getFileName(),
//                        false,outSmFile,node.getChecked());
                    SmNode outNode = new SmNode(outSmFile.getFileName(),
                        false,outFilePath,node.getChecked());
                    nodeTargetVx.add(outNode);
                    
                    // Add node to removeNodeList.
                    removeNodeList.add(node);
                }
            }
        }
        else {
            Enumeration nodeElements = node.children();
            while (nodeElements.hasMoreElements()) {
                SmNode nodeElement = (SmNode)nodeElements.nextElement();
                traverseNodeStructure(nodeElement,removeNodeList);
            }
        }
    }
    
    /**
     * Recreates the tree node structure shown in the Node Explorer.
     * @return a SmNode object representing the root node.
     */
    public static SmNode recreateTreeNodeStructure() {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
    
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        SmNode nodeRoot = (SmNode)model.getRoot();
        
        ArrayList<SmNode> removeNodeList = new ArrayList<>();
        
        traverseNodeStructure(nodeRoot,removeNodeList);
        
        for (SmNode node : removeNodeList) {
            model.removeNodeFromParent(node);
        }
        
        return nodeRoot;
    }
    
    /**
     * Sets each of the children of the specified parent node to the specified
     * checked state. 
     * @param node the parent node.
     * @param checked the checked state to set the children nodes to.
     */
    public static void setSmNodeCheckedState(SmNode node, boolean checked) {
        node.setChecked(checked);
        
        if (!node.isLeaf()) {
            Enumeration nodes = node.children();
            while (nodes.hasMoreElements()) {
                SmNode subNode = (SmNode)nodes.nextElement();
                setSmNodeCheckedState(subNode,checked);
            }
        }
    }
    
    /**
     * Determines if the Node Explorer is empty.
     * @return true if Node Explorer is empty, false otherwise.
     */
    public static boolean NodeExplorerIsEmpty() {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
    
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        SmNode nodeRoot = (SmNode)model.getRoot();
        
        return (nodeRoot == null);
    }
    
    /**
     * Determines if the Node Explorer has at least one leaf node that is checked.
     * @return true if Node Explorer has at least one leaf node that is checked, else false.
     */
    public static boolean NodeExplorerHasCheckedLeafSmNode() {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
    
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        SmNode nodeRoot = (SmNode)model.getRoot();
        
        return hasCheckedLeafSmNode(nodeRoot);
    }
    
    /**
     * Gets the SmNode object representing the child whose name matches the specified 
     * name of the child node to look retrieve.
     * @param parentNode SmNode object representing the parent node to search from.
     * @param childNodeName name of the child node to retrieve.
     * @return 
     */
    public static SmNode getFromSmNode(SmNode parentNode, String childNodeName) {
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            SmNode childNode = (SmNode)children.nextElement();
            if (childNode.toString().equalsIgnoreCase(childNodeName))
                return childNode;
        }
        
        return null;
    }
    
    /**
     * Adds specified child node to the specified parent node.
     * @param parentNode SmNode representing the parent node.
     * @param childNode SmNode representing the child node.
     * @return 
     */
    public static SmNode addToSmNode(SmNode parentNode, SmNode childNode) {
        Enumeration children = parentNode.children();
        
        // Create to contain list of enumerated child nodes.
        ArrayList<SmNode> list = new ArrayList<>();
        
        while (children.hasMoreElements()) {
            SmNode child = (SmNode)children.nextElement();
            if (child.toString().equalsIgnoreCase(childNode.toString())) 
                return parentNode;
            else
                list.add(child);
        }
        
        // Add child node to list.
        list.add(childNode);
        
        // Sort list.
        Collections.sort(list, SmNode.SmNodeComparator);
        
        // Remove previous child nodes from parent node.
        parentNode.removeAllChildren();
        
        // Add new child nodes to parent node.
        for (SmNode node : list)
            parentNode.add(node);
        
        return parentNode;
    }
    
    /** Determines if input node is a checked leaf node or if
     *  any of its children is a checked leaf node.
     * @param node
     * @return 
     */
    public static boolean IsOrContainsCheckedLeafSmNode(SmNode node) {
        if (node.isLeaf()) {
            return node.getChecked();
        }
        else {
            Enumeration nodes = node.children();
            while (nodes.hasMoreElements()) {
                SmNode subNode = (SmNode)nodes.nextElement();
                if (IsOrContainsCheckedLeafSmNode(subNode))
                    return true;
            }
            return false;
        }
    }
    
    /**
     * Determines if the specified node is a checked leaf node or if
     * any of its children is a checked leaf node.
     * @param node SmNode representing the parent node.
     * @return 
     */
    public static boolean hasCheckedLeafSmNode(SmNode node) {
        if (node.isLeaf()) {
            Object[] elements = node.getPath();
            TreePath treepath = new TreePath(elements);

            SmNodeExplorerTC tcSmNodeExplorer =
                findTopComponent(SmNodeExplorerTC.class);
            CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
            
            return treeContents.isPathChecked(treepath);
        }
        else {
            Enumeration nodes = node.children();
            while (nodes.hasMoreElements()) {
                SmNode subNode = (SmNode)nodes.nextElement();
                if (hasCheckedLeafSmNode(subNode))
                    return true;
            }
            return false;
        }
    }
    
    /**
     * Builds a list of leaf nodes set to the specified checked state.
     * @param checkedState state to which to set the leaf nodes.
     * @return 
     */
    public static ArrayList<SmNode> getLeafSmNodes(CheckedState checkedState) {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
    
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        SmNode nodeRoot = (SmNode)model.getRoot();
        
        ArrayList<SmNode> leafNodes = new ArrayList<>();
        
        addLeafSmNode(nodeRoot,leafNodes,checkedState);
        
        return leafNodes;
    }
    
    /**
     * Builds a list of events having at least one leaf node that is set to the 
     * specified checked state.
     * @param checkedState the checked state to which to set the leaf nodes.
     * @return 
     */
    public static ArrayList<SmNode> getEvents(CheckedState checkedState) {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
    
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        SmNode nodeRoot = (SmNode)model.getRoot();
        
        ArrayList<SmNode> eventNodes = new ArrayList<>();
        
        Enumeration events = nodeRoot.children();
        
        while (events.hasMoreElements()) {
            SmNode nodeEvent = (SmNode)events.nextElement();
            
            if (IsOrContainsCheckedLeafSmNode(nodeEvent)) {
                eventNodes.add(nodeEvent);
            }
        }
        
        return eventNodes;
    }
    
    /**
     * Traverses the node and adds it to the container list, if it's a leaf node and meets
     * the specified checked state. Otherwise, recursively calls this function for each of the 
     * node's children.
     * @param node - the starting node to traverse.
     * @param containerList - the container node in which to store node
     * @param checkedState - the checked state the node has to have to be added to group 
     */
    public static void addLeafSmNode(SmNode node, ArrayList<SmNode> containerList,
        CheckedState checkedState) {
        
        if (node.isLeaf()) {
            if (checkedState.equals(CheckedState.CHECKED)) {
                if (node.getChecked()) {
                    containerList.add(node);
                }
            }
            else if (checkedState.equals(CheckedState.UNCHECKED)) {
                if (!node.getChecked()) {
                    containerList.add(node);
                }
            }
            else { //CheckedState.ALL
                containerList.add(node);
            }
        }
        else {
            Enumeration nodes = node.children();
            while (nodes.hasMoreElements()) {
                SmNode subNode = (SmNode)nodes.nextElement();
                addLeafSmNode(subNode,containerList,checkedState);
            }
        }
    }
    
    /**
     * Traverses the node and adds it to the container node, if it's a leaf node and meets
     * the specified checked state. Otherwise, recursively calls this function for each of the 
     * node's children.
     * @param node - the starting node to traverse.
     * @param containerNode - the container node in which to store node
     * @param checkedState - the checked state the node has to have to be added to group 
     */
    public static void addLeafSmNode(SmNode node, SmNode containerNode, CheckedState checkedState) {
        if (node.isLeaf()) {
            if (checkedState.equals(CheckedState.CHECKED)) {
                if (node.getChecked()) {
                    containerNode.add(node);
                }
            }
            else if (checkedState.equals(CheckedState.UNCHECKED)) {
                if (!node.getChecked()) {
                    containerNode.add(node);
                }
            }
            else { //CheckedState.ALL
                containerNode.add(node);
            }
        }
        else {
            Enumeration nodes = node.children();
            while (nodes.hasMoreElements()) {
                SmNode subNode = (SmNode)nodes.nextElement();
                addLeafSmNode(subNode,containerNode,checkedState);
            }
        }
    }
    
    /**
     * Sets the checked state of the children node of the specified parent node.
     * @param node SmNode representing the parent node.
     */
    public static void setCheckboxTreeCheckedState(SmNode node) {
        Object[] elements = node.getPath();
        TreePath treepath = new TreePath(elements);

        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        
        DefaultTreeCheckingModel checkingModel = (DefaultTreeCheckingModel)treeContents.getCheckingModel();

        if (node.getChecked()) {
            checkingModel.addCheckingPath(treepath);
        }
        else
            checkingModel.removeCheckingPath(treepath);
        
        if (!node.isLeaf()) {
            Enumeration nodeElements = node.children();
            while (nodeElements.hasMoreElements()) {
                SmNode nodeElement = (SmNode)nodeElements.nextElement();
                setCheckboxTreeCheckedState(nodeElement);
            }
        }
    }
    
    public static DateTime getEarliestStartTime(String event, String station) {
        try {  
            // Get selected nodes.
            ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

            for (SmNode nodeEvent : eventNodes) {
                if (nodeEvent.toString().equals(event)) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();

                        if (nodeStation.toString().equals(station)) {
                            ArrayList<String> filePaths = new ArrayList<>();

                            Enumeration children = nodeStation.children();

                            while(children.hasMoreElements()) {
                                SmNode nodeChild = (SmNode)children.nextElement();

                                if (nodeChild.isLeaf()) {
                                    String filePath = nodeChild.getFilePath();
                                    String fileType = SmCore.extractCosmosFileType(filePath);

                                    if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()) ||
                                        fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                        filePaths.add(filePath);
                                    }
                                }
                            }

                            return SmUtils.getEarliestStartTime(filePaths);
                        }  
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
        
        return null;
    }
    
    public static DateTime getLatestStartTime(String event, String station) {
        try {  
            // Get selected nodes.
            ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

            for (SmNode nodeEvent : eventNodes) {
                if (nodeEvent.toString().equals(event)) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();

                        if (nodeStation.toString().equals(station)) {
                            ArrayList<String> filePaths = new ArrayList<>();

                            Enumeration children = nodeStation.children();

                            while(children.hasMoreElements()) {
                                SmNode nodeChild = (SmNode)children.nextElement();

                                if (nodeChild.isLeaf()) {
                                    String filePath = nodeChild.getFilePath();
                                    String fileType = SmCore.extractCosmosFileType(filePath);

                                    if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()) ||
                                        fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                        filePaths.add(filePath);
                                    }
                                }
                            }

                            return SmUtils.getLatestStartTime(filePaths);
                        }  
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
        
        return null;
    }
    
    public static DateTime getEarliestStopTime(String event, String station) {
        try {  
            // Get selected nodes.
            ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

            for (SmNode nodeEvent : eventNodes) {
                if (nodeEvent.toString().equals(event)) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();

                        if (nodeStation.toString().equals(station)) {
                            ArrayList<String> filePaths = new ArrayList<>();

                            Enumeration children = nodeStation.children();

                            while(children.hasMoreElements()) {
                                SmNode nodeChild = (SmNode)children.nextElement();

                                if (nodeChild.isLeaf()) {
                                    String filePath = nodeChild.getFilePath();
                                    String fileType = SmCore.extractCosmosFileType(filePath);

                                    if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()) ||
                                        fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                        filePaths.add(filePath);
                                    }
                                }
                            }

                            return SmUtils.getEarliestStopTime(filePaths);
                        }  
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
        
        return null;
    }
    
    public static DateTime getLatestStopTime(String event, String station) {
        try {  
            // Get selected nodes.
            ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

            for (SmNode nodeEvent : eventNodes) {
                if (nodeEvent.toString().equals(event)) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();

                        if (nodeStation.toString().equals(station)) {
                            ArrayList<String> filePaths = new ArrayList<>();

                            Enumeration children = nodeStation.children();

                            while(children.hasMoreElements()) {
                                SmNode nodeChild = (SmNode)children.nextElement();

                                if (nodeChild.isLeaf()) {
                                    String filePath = nodeChild.getFilePath();
                                    String fileType = SmCore.extractCosmosFileType(filePath);

                                    if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()) ||
                                        fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                        filePaths.add(filePath);
                                    }
                                }
                            }

                            return SmUtils.getLatestStopTime(filePaths);
                        }  
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
        
        return null;
    }
    
    public static double getMinimumDeltaT(String event, String station) {
        try {  
            // Get selected nodes.
            ArrayList<SmNode> eventNodes = 
                SmCore.getEvents(SmCore.CheckedState.CHECKED);

            for (SmNode nodeEvent : eventNodes) {
                if (nodeEvent.toString().equals(event)) {
                    Enumeration stations = nodeEvent.children();
                    while (stations.hasMoreElements()) {
                        SmNode nodeStation = (SmNode)stations.nextElement();

                        if (nodeStation.toString().equals(station)) {
                            ArrayList<String> filePaths = new ArrayList<>();

                            Enumeration children = nodeStation.children();

                            while(children.hasMoreElements()) {
                                SmNode nodeChild = (SmNode)children.nextElement();

                                if (nodeChild.isLeaf()) {
                                    String filePath = nodeChild.getFilePath();
                                    String fileType = SmCore.extractCosmosFileType(filePath);

                                    if (fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V1.toString()) ||
                                        fileType.equalsIgnoreCase(SmGlobal.CosmosFileType.V2.toString())) {
                                        filePaths.add(filePath);
                                    }
                                }
                            }

                            return SmUtils.getMinimumDeltaT(filePaths);
                        }  
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            showMessage("Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        } 
        
        return 0;
    }
    
    /**
     * Class that extends the SwingWorker class and implements the functions to
     * run graphs as a background task.
     */
    private static class RunGraphsByGroupTask extends SwingWorker<Integer, Integer>
    {
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
                
                SmCore.addMsgToStatusViewer("Charting in progress ...");
                
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
                        
                        V1ChartsBin v1ChartsBin = !v1FilePaths.isEmpty() ? 
                            new V1ChartsBin(chartAPI,v1FilePaths,smTemplate,adjustPoints,owner) : null;
                        
                        V2ChartsBin v2ChartsBin = !v2FilePaths.isEmpty() ? 
                            new V2ChartsBin(chartAPI,v2FilePaths,smTemplate,adjustPoints,owner) : null;
                        
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
            
                        SmCore.getVxChartsBinGroups().add(new VxChartsBinGroup(
                            groupName,sbStationDesc.toString(),sbEventDesc.toString(),
                            chartAPI,v1ChartsBin,v2ChartsBin));
                    }
                }
                
                SmCore.addMsgToStatusViewer(String.format(
                    "Charting completed (%d group(s) processed).",SmCore.getVxChartsBinGroups().size()));
            } 
            catch (Exception ex) 
            {
                showMessage("Error",ex.getMessage(),
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
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
                SmCore.resetNavigationPanel();
                SmCore.updateChartViewPanels();

                // Finish handle.
                handle.finish();
            }
            catch (Exception ex) {
                SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
            }
        }
    }
    
}
