/*******************************************************************************
 * Name: Java class OpenEventAction.java
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
import gov.usgs.smapp.smdialogs.SmEventDialog;
import gov.usgs.smapp.smtopcomponents.SmNodeExplorerTC;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmNode;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.findTopComponent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "gov.usgs.smapp.smactions.fileactions.OpenEventAction"
)
@ActionRegistration(
        iconBase = "resources/icons/open_event_16.png",
        displayName = "#CTL_OpenEventAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1500),
    @ActionReference(path = "Toolbars/File", position = 400)
})
@Messages("CTL_OpenEventAction=Open Event ...")
public final class OpenEventAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            WindowManager wm = WindowManager.getDefault();
            SmEventDialog dialog = new SmEventDialog(wm.getMainWindow(),true);
            dialog.setTitle("Open Event");

            if (!dialog.showDialog())
                return;

            // Reset chart viewers.
            SmCore.resetChartViewers();

            // Reset VxChartsBinGroups.
            SmCore.resetVxChartsBinGroups();

            // Reset navigation controls.
            SmCore.resetNavigationPanel();

            // Reset properties table.
            SmCore.resetPropertiesTable();

            String eventsRootDir = dialog.getEventsRootDir();
            String eventName = dialog.getEvent();
            List<String> stationNames = dialog.getStations();

            boolean v1Selected = dialog.getCosmosDataTypeV1SelectState();
            boolean v2Selected = dialog.getCosmosDataTypeV2SelectState();

            //SmCore.updateTemplateList();

            SmNodeExplorerTC tcSmNodeExplorer =
                findTopComponent(SmNodeExplorerTC.class);
            if (tcSmNodeExplorer == null)
                return;

            CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
            DefaultTreeModel treeModel = (DefaultTreeModel)treeContents.getModel();

            // Turn tree listeners off.
            tcSmNodeExplorer.setTreeListeners(false);

            // Set the checkboxtree's checking mode to simple.
            treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.SIMPLE);

            SmNode nodeRoot = new SmNode(eventsRootDir);

            SmCore.addToSmNode(nodeRoot,new SmNode(eventName));

            SmNode nodeEvent = SmCore.getFromSmNode(nodeRoot, eventName);

            // Add station nodes to event node.
            for (String stationName : stationNames) {
                SmCore.addToSmNode(nodeEvent, new SmNode(stationName));
                SmNode nodeStation = SmCore.getFromSmNode(nodeEvent, stationName);

                // Add nodes for V1, V2, and Trouble folders, if they exist.
                Path pathTrouble = Paths.get(eventsRootDir,eventName,stationName,SmGlobal.TROUBLE);
                Path pathV1 = Paths.get(eventsRootDir,eventName,stationName,"V1");
                Path pathV2 = Paths.get(eventsRootDir,eventName,stationName,"V2");

                if (Files.exists(pathTrouble)) {
                    SmCore.addToSmNode(nodeStation, new SmNode(SmGlobal.TROUBLE));
                    SmNode nodeTrouble = SmCore.getFromSmNode(nodeStation, SmGlobal.TROUBLE);

                    // Add nodes for V1 and V2 folders, if they exist.
                    Path pathTroubleV1 = Paths.get(eventsRootDir,eventName,stationName,
                        SmGlobal.TROUBLE,"V1");
                    Path pathTroubleV2 = Paths.get(eventsRootDir,eventName,stationName,
                        SmGlobal.TROUBLE,"V2");

                    if (v1Selected) {
                        if (Files.exists(pathTroubleV1)) {
                            SmCore.addToSmNode(nodeTrouble, new SmNode("V1"));
                            SmNode nodeTroubleV1 = SmCore.getFromSmNode(nodeTrouble, "V1");

                            // Add nodes for files in V1 folder.
                            File troubleV1Folder = pathTroubleV1.toFile();
                            File[] troubleV1Files = troubleV1Folder.listFiles();
                            for (File troubleV1File : troubleV1Files) {
                                SmCore.addToSmNode(nodeTroubleV1, new SmNode(
                                    troubleV1File.getName(),false,troubleV1File.getPath(),false));
                            }
                        }
                    }

                    if (v2Selected) {
                        if (Files.exists(pathTroubleV2)) {
                            SmCore.addToSmNode(nodeTrouble, new SmNode("V2"));
                            SmNode nodeTroubleV2 = SmCore.getFromSmNode(nodeTrouble, "V2");

                            // Add nodes for files in V2 folder.
                            File troubleV2Folder = pathTroubleV2.toFile();
                            File[] troubleV2Files = troubleV2Folder.listFiles();
                            for (File troubleV2File : troubleV2Files) {
                                SmCore.addToSmNode(nodeTroubleV2, new SmNode(
                                    troubleV2File.getName(),false,troubleV2File.getPath(),false));
                            }
                        }
                    }
                }

                if (v1Selected) {
                    if (Files.exists(pathV1)) {
                        SmCore.addToSmNode(nodeStation, new SmNode("V1"));
                        SmNode nodeV1 = SmCore.getFromSmNode(nodeStation, "V1");

                        // Add nodes for files in V1 folder.
                        File v1Folder = pathV1.toFile();
                        File[] v1Files = v1Folder.listFiles();
                        for (File v1File : v1Files) {
                            SmCore.addToSmNode(nodeV1, new SmNode(
                                v1File.getName(),false,v1File.getPath(),false));
                        }
                    }
                }

                if (v2Selected) {
                    if (Files.exists(pathV2)) {
                        SmCore.addToSmNode(nodeStation, new SmNode("V2"));
                        SmNode nodeV2 = SmCore.getFromSmNode(nodeStation, "V2");

                        // Add nodes for files in V2 folder.
                        File v2Folder = pathV2.toFile();
                        File[] v2Files = v2Folder.listFiles();
                        for (File v2File : v2Files) {
                            SmCore.addToSmNode(nodeV2, new SmNode(
                                v2File.getName(),false,v2File.getPath(),false));
                        }
                    }
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
