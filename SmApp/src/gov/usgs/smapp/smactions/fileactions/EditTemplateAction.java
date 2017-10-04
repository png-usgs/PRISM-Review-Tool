/*******************************************************************************
 * Name: Java class EditTemplateAction.java
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
import gov.usgs.smapp.smforms.SmTemplateEditor;
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
        id = "gov.usgs.smapp.smactions.fileactions.EditTemplateAction"
)
@ActionRegistration(
        iconBase = "resources/icons/edit_template_16.png",
        displayName = "#CTL_EditTemplateAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 2000),
    @ActionReference(path = "Toolbars/File", position = 800)
})
@Messages("CTL_EditTemplateAction=Edit Template")
public final class EditTemplateAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            
            SmTemplateEditor form = new SmTemplateEditor(SmPreferences.General.getTemplatesRootDir(),
                SmPreferences.General.getStationTemplatesFolderName(),SmPreferences.General.getTemplate());
           
            form.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            form.pack();
            form.setVisible(true);
        
        }
        catch (Exception ex) {
            SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
        }
    }
}
