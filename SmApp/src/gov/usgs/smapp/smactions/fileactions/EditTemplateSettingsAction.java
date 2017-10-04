/*******************************************************************************
 * Name: Java class EditTemplateSettingsAction.java
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
import gov.usgs.smapp.smdialogs.SmTemplateSettingsDialog;
import gov.usgs.smcommon.smclasses.SmPreferences;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "gov.usgs.smapp.smactions.fileactions.EditTemplateSettingsAction"
)
@ActionRegistration(
        iconBase = "resources/icons/edit_template_settings_16.png",
        displayName = "#CTL_EditTemplateSettingsAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1900),
    @ActionReference(path = "Toolbars/File", position = 700)
})
@Messages("CTL_EditTemplateSettingsAction=Edit Template Settings...")
public final class EditTemplateSettingsAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            WindowManager wm = WindowManager.getDefault();
            
            SmTemplateSettingsDialog dialog = new SmTemplateSettingsDialog(wm.getMainWindow(),true);
            
            if (dialog.showDialog()) {
                // Update templates root directory, staion folder name, and
                // template preferences.
                SmPreferences.General.setTemplatesRootDir(dialog.getTemplatesRootDir());
                SmPreferences.General.setStationTemplatesFolderName(dialog.getStation());
                SmPreferences.General.setTemplate(dialog.getTemplate());
                
                // Update main template list.
                SmCore.updateTemplateList();
            }
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
}
