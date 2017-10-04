/*******************************************************************************
 * Name: Java class SmFileChildFactory.java
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

package gov.usgs.smcommon.smnetbeans;

import gov.usgs.smcommon.smclasses.SmFile;
import java.beans.IntrospectionException;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author png
 */
public class SmFileChildFactory extends ChildFactory.Detachable<SmFile> 
    implements LookupListener {

    Result<SmFile> res;
    
    @Override
    protected void addNotify() {
        res = Lookups.forPath("SmFiles").lookupResult(SmFile.class);
        res.addLookupListener(this);
    }
    
    @Override
    protected void removeNotify() {
        res.removeLookupListener(this);
    }
    
    @Override
    protected boolean createKeys(List<SmFile> list) {
        list.addAll(res.allInstances());
        return true;
    }
    
    @Override
    protected Node createNodeForKey(SmFile key) {
        SmBeanNode smBeanNode = null;
        try {
            smBeanNode = new SmBeanNode(key);
        }
        catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return smBeanNode;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refresh(true);
    }
    
}
