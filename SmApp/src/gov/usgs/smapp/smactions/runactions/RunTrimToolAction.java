/*******************************************************************************
 * Name: Java class RunTrimToolAction.java
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

package gov.usgs.smapp.smactions.runactions;

import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smforms.SmTrimTool;
import gov.usgs.smapp.smtopcomponents.SmNodeExplorerTC;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.findTopComponent;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.DefaultMutableTreeNode;
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
        id = "gov.usgs.smapp.smactions.runactions.RunTrimToolAction"
)
@ActionRegistration(
        iconBase = "resources/icons/run_trim_tool_16.png",
        displayName = "#CTL_RunTrimToolAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Run", position = 100),
    @ActionReference(path = "Toolbars/Run", position = 100)
})
@Messages("CTL_RunTrimToolAction=Run Trim Tool")
public final class RunTrimToolAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            
            if (!validate())
                return;
            
            SmTrimTool form = new SmTrimTool();
           
            form.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            form.pack();
            form.setVisible(true);
        
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
    
    private boolean validate() {
        SmNodeExplorerTC tcSmNodeExplorer =
            findTopComponent(SmNodeExplorerTC.class);
        if (tcSmNodeExplorer == null) {
            showMessage("Error","Node Explorer not found.",
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Node Explorer not found.");
            return false;
        }
            
        if (SmCore.NodeExplorerIsEmpty()) {
            showMessage("Error","Node Explorer is empty.",
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Node Explorer is empty.");
            return false;
        }
        
        if (!SmCore.NodeExplorerHasCheckedLeafSmNode()) {
            showMessage("Error","No file nodes checked.",
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "No file nodes checked.");
            return false;
        }
        
        CheckboxTree treeContents = tcSmNodeExplorer.getCheckboxTree();
        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
        DefaultMutableTreeNode nodeRoot = (DefaultMutableTreeNode)model.getRoot();
            
        int depth = nodeRoot.getDepth();
        
        if (depth < 4) {
            showMessage("Error","Invalid node structure depth.",
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Error: " + "Invalid node structure depth.");
            return false;
        }
        
        return true;
    }
}
