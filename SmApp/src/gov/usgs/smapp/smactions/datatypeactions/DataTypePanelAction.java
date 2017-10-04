/*******************************************************************************
 * Name: Java class DataTypePanelAction.java
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

package gov.usgs.smapp.smactions.datatypeactions;

import gov.usgs.smapp.smtopcomponents.SmSeismicTraceViewerTC;
import gov.usgs.smapp.smtopcomponents.SmFAS_ViewerTC;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Datatype",
        id = "gov.usgs.smapp.smactions.datatypeactions.DataTypePanelAction"
)
@ActionRegistration(
        displayName = "#CTL_DataTypePanelAction",
        lazy = false
)
@ActionReference(path = "Toolbars/Datatype", position = 400)
@Messages("CTL_DataTypePanelAction=DataTypePanelAction")
@SuppressWarnings("all")
public final class DataTypePanelAction extends AbstractAction
    implements Presenter.Toolbar, ContextAwareAction, LookupListener {

    private final Lookup.Result<Object> result;
    private final DataTypePanel panel = new DataTypePanel();
    
    public DataTypePanelAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public DataTypePanelAction(Lookup lkp) {
        super(Bundle.CTL_DataTypePanelAction());
        result = lkp.lookupResult(Object.class);
        result.addLookupListener(
            WeakListeners.create(LookupListener.class, this, result));
    }
    
    @Override
    public Component getToolbarPresenter() {
        return panel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
    
    public DataTypePanel getDataTypePanel() {
        return panel;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new DataTypePanelAction(lkp);
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        if (result.allInstances().size() > 0) {
            Object obj = result.allInstances().iterator().next();
            
            if (obj instanceof SmFAS_ViewerTC) {
                super.setEnabled(false);
                panel.setPanelEnabledState(false);
            }
            else if (obj instanceof SmSeismicTraceViewerTC) {
                SmSeismicTraceViewerTC tc = 
                    (SmSeismicTraceViewerTC)obj;
                
                if (tc.getSeismicPanel().getComponents().length > 0) {
                    super.setEnabled(true);
                    panel.setPanelEnabledState(true);
                }
                else {
                    super.setEnabled(false);
                    panel.setPanelEnabledState(false);
                }
            }
        }
    }
}
