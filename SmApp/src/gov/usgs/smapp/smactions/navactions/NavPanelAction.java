/*******************************************************************************
 * Name: Java class NavPanelAction.java
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

package gov.usgs.smapp.smactions.navactions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Navigation",
        id = "gov.usgs.smapp.smactions.navactions.NavPanelAction"
)
@ActionRegistration(
        displayName = "#CTL_NavPanelAction",
        lazy = false
)
@ActionReference(path = "Toolbars/Navigation", position = 500)
@Messages("CTL_NavPanelAction=NavPanelAction")
@SuppressWarnings("all")
public final class NavPanelAction extends AbstractAction
    implements Presenter.Toolbar {

    private final NavPanel panel = new NavPanel();

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
    
    public NavPanel getNavPanel() {
        return panel;
    }
}
