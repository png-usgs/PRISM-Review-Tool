/*******************************************************************************
 * Name: Java class SmFileGroup.java
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
 * This class defines a structure for storing an array of SmFile objects as a
 * group
 * @author png
 */
public class SmFileGroup {
    private String groupName;
    private ArrayList<SmFile> smFiles = new ArrayList<>();

    /**
     * Constructor
     * @param groupName name for the group of SmFile objects
     */
    public SmFileGroup(String groupName)
    {
        this.groupName = groupName;
    }

    // Copy constructor
    public SmFileGroup(SmFileGroup smFileGroup)
    {
        this.groupName = smFileGroup.getGroupName();
        this.smFiles = new ArrayList<>();
        for (SmFile smFile : smFileGroup.getSmFiles()) {
            this.smFiles.add(new SmFile(smFile));
        }
    }
    
    /**
     * Gets the name assigned to the group of SmFile objects
     * @return name assigned to the group of SmFile objects
     */
    public String getGroupName()
    {
        return this.groupName;
    }

    /**
     * Gets the array list of SmFile objects
     * @return array list of SmFile objects
     */
    public ArrayList<SmFile> getSmFiles()
    {
        return this.smFiles;
    }

    /**
     * Sets the array list of SmFile objects
     * @param smFiles array list of SmFile objects
     */
    public void setSmFiles(ArrayList<SmFile> smFiles)
    {
        this.smFiles = smFiles;
    }

    /**
     * Adds a SmFile object to the SmFile array list
     * @param smFile SmFile object to add to the SmFile array list
     */
    public void addSmFile(SmFile smFile)
    {
        this.smFiles.add(smFile);
    }
}
