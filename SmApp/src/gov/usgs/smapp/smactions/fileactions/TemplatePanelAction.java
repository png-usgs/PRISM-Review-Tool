/*******************************************************************************
 * Name: Java class TemplatePanelAction.java
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "File",
        id = "gov.usgs.smapp.smactions.fileactions.TemplatePanelAction"
)
@ActionRegistration(
        //iconBase = "gov/usgs/smapp/smactions/fileactions/template_16.png",
        displayName = "#CTL_TemplatePanelAction",
        lazy = false
)
@ActionReference(path = "Toolbars/File", position = 600)
@Messages("CTL_TemplatePanelAction=TemplatePanelAction")
@SuppressWarnings({"rawtypes","unchecked"})
public final class TemplatePanelAction extends AbstractAction
    implements Presenter.Toolbar {

    private final TemplatePanel panel = new TemplatePanel();

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
    
    public TemplatePanel getTemplatePanel() {
        return panel;
    }
    
    public void setTemplate(String template) {
        panel.setCBoxTemplate(template);
    }
    
}
