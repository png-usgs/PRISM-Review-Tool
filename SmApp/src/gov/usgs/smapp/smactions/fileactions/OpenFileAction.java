/*******************************************************************************
 * Name: Java class OpenFileAction.java
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

package gov.usgs.smapp.smactions.fileactions;

import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smtopcomponents.SmNodeExplorerTC;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmNode;
import gov.usgs.smcommon.smclasses.SmPreferences;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.*;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultTreeModel;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "gov.usgs.smapp.smactions.fileactions.OpenFileAction"
)
@ActionRegistration(
        iconBase = "resources/icons/open_file_16.png",
        displayName = "#CTL_OpenFileAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1200),
    @ActionReference(path = "Toolbars/File", position = 200)
})
@Messages("CTL_OpenFileAction=Open File ...")
public final class OpenFileAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            SmCore.configureFileChooser(fileChooser);

            WindowManager wm = WindowManager.getDefault();

            if (fileChooser.showOpenDialog(wm.getMainWindow()) != JFileChooser.APPROVE_OPTION)
                return;
            
            // Retrieve selected Vx directory path.
            String lastVxDir = fileChooser.getCurrentDirectory().getAbsolutePath();

            // Check validity of Vx path.
            String[] parts = lastVxDir.split(Pattern.quote(File.separator));
            
            if (parts.length < 4) {

                String msg = "The folder structure must end with a pattern similar to " +
                    "'.." + File.separator + "events_root_dir" + File.separator + "event" +
                    File.separator + "station" + File.separator + "Vx' or \n" +
                    "'.." + File.separator + "events_root_dir" + File.separator + "event" +
                    File.separator + "station" + File.separator + "Trouble" + File.separator +
                    "Vx', where Vx is either a V1 or V1 folder containing the selected files.";

                showMessage("Invalid Folder Structure",msg,
                    NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
                SmCore.addMsgToStatusViewer("Invalid Folder Structure: " + msg);
                return;
            }
            
            // Reset chart viewers.
            SmCore.resetChartViewers();

            // Reset VxChartsBinGroups.
            SmCore.resetVxChartsBinGroups();

            // Reset navigation controls.
            SmCore.resetNavigationPanel();

            // Reset properties table.
            SmCore.resetPropertiesTable();

            // Update last visited directory in preferences.
            SmPreferences.General.setLastVxDir(lastVxDir);  

            // Create paths to events root, event, station, trouble, and Vx directories.
            Path pathVx = Paths.get(lastVxDir);
            boolean trouble = pathVx.getParent().getFileName().toString().equalsIgnoreCase(SmGlobal.TROUBLE);
            //Path pathTrouble = trouble ? pathVx.getParent() : null;
            Path pathStation = trouble ? pathVx.getParent().getParent() : pathVx.getParent();
            Path pathEvent = pathStation.getParent();
            Path pathEventsRootDir = pathEvent.getParent();

            // Create strings.
            String eventsRootDir = pathEventsRootDir.toString();
            String eventName = pathEvent.getFileName().toString();
            String stationName = pathStation.getFileName().toString();
            String vXName = pathVx.getFileName().toString();

            // Get reference to node explorer.
            SmNodeExplorerTC tcSmNodeExplorer =
                findTopComponent(SmNodeExplorerTC.class);
            if (tcSmNodeExplorer == null)
                return;

            // Get CheckboxTree and model objects.
            CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
            DefaultTreeModel treeModel = (DefaultTreeModel)treeContents.getModel();

            // Turn tree listeners off.
            tcSmNodeExplorer.setTreeListeners(false);

            // Set the checkboxtree's checking mode to simple.
            treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.SIMPLE);

            // Set root node.
            SmNode nodeRoot = new SmNode(eventsRootDir);

            // Add event node to root node.
            SmCore.addToSmNode(nodeRoot,new SmNode(eventName));
            SmNode nodeEvent = SmCore.getFromSmNode(nodeRoot, eventName);

            // Add station node to event node.
            SmCore.addToSmNode(nodeEvent, new SmNode(stationName));
            SmNode nodeStation = SmCore.getFromSmNode(nodeEvent, stationName);

            if (trouble) {
                // Add node for trouble folder.
                SmCore.addToSmNode(nodeStation, new SmNode(SmGlobal.TROUBLE));
                SmNode nodeTrouble = SmCore.getFromSmNode(nodeStation, SmGlobal.TROUBLE);

                // Add node for Vx folder.
                SmCore.addToSmNode(nodeTrouble, new SmNode(vXName));
                SmNode nodeVx = SmCore.getFromSmNode(nodeTrouble, vXName);

                // Add node for each file.
                for (File file : fileChooser.getSelectedFiles()) {
                    SmCore.addToSmNode(nodeVx, new SmNode(file.getName(),
                        false,file.getPath(),false));
                }
            }
            else {
                // Add node for station Vx folder.
                SmCore.addToSmNode(nodeStation, new SmNode(vXName));
                SmNode nodeVx = SmCore.getFromSmNode(nodeStation, vXName);

                // Add node for each file.
                for (File file : fileChooser.getSelectedFiles()) {
                    SmCore.addToSmNode(nodeVx, new SmNode(file.getName(),
                        false,file.getPath(),false));
                }
            }

            // Set tree root.
            treeModel.setRoot(nodeRoot);
            treeContents.setRootVisible(false);
            treeContents.expandAll();

            // Reset the checkboxtree's checking mode to propogate.
            treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);

            // Turn tree listeners back on.
            tcSmNodeExplorer.setTreeListeners(true);
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
}
