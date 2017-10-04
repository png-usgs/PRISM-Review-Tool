/*******************************************************************************
 * Name: Java class EditPreferencesAction.java
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

package gov.usgs.smapp.smactions.editactions;

import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smdialogs.SmPreferencesDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Edit",
        id = "gov.usgs.smapp.smactions.editactions.EditPreferencesAction"
)
@ActionRegistration(
        iconBase = "resources/icons/preferences_16.png",
        displayName = "#CTL_EditPreferencesAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 1100),
    @ActionReference(path = "Toolbars/Edit", position = 100)
})
@Messages("CTL_EditPreferencesAction=Edit Preferences...")
public final class EditPreferencesAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            WindowManager wm = WindowManager.getDefault();
            SmPreferencesDialog dialog = new SmPreferencesDialog(wm.getMainWindow(),true);
            if (dialog.showDialog()) {
                SmCore.updateTemplateList();
            }
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
}
