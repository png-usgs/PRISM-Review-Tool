/*******************************************************************************
 * Name: Java class SmNodeGroup.java
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

package gov.usgs.smcommon.smclasses;

import java.util.ArrayList;

/**
 * This class defines a structure for storing a group of SmNode objects
 * @author png
 */
public class SmNodeGroup {
    
    private String groupName;
    private ArrayList<SmNode> smNodes = new ArrayList<>();
    
    /**
     * Constructor
     * @param groupName group name
     */
    public SmNodeGroup(String groupName)
    {
        this.groupName = groupName;
    }

    // Copy constructor
    public SmNodeGroup(SmNodeGroup smNodeGroup)
    {
        this.groupName = smNodeGroup.getGroupName();
        this.smNodes = new ArrayList<>();
        for (SmNode smNode : smNodeGroup.getSmNodes()) {
            this.smNodes.add(new SmNode(smNode));
        }
    }
    
    /**
     * Gets the group name
     * @return group name
     */
    public String getGroupName()
    {
        return this.groupName;
    }

    /**
     * Gets the array list of SmNode objects
     * @return array list of SmNode objects
     */
    public ArrayList<SmNode> getSmNodes()
    {
        return this.smNodes;
    }

    /**
     * Sets the array list of SmNode objects
     * @param smNodes 
     */
    public void setSmNodes(ArrayList<SmNode> smNodes)
    {
        this.smNodes = smNodes;
    }

    /**
     * Adds a SmNode object to the SmNode array list
     * @param smNode SmNode object to add to the SmNode array list
     */
    public void addSmNode(SmNode smNode)
    {
        this.smNodes.add(smNode);
    }
    
}
