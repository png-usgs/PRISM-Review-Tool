/*******************************************************************************
 * Name: Java class SmBeanNode.java
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
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmBeanNode extends BeanNode {

    public SmBeanNode(SmFile smFile) throws IntrospectionException {
        super(smFile, Children.LEAF, Lookups.singleton(smFile));
        setDisplayName(smFile.getFileName());
        setShortDescription(smFile.getFileName());
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
}
