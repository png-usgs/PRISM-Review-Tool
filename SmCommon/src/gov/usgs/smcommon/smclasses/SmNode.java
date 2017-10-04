/*******************************************************************************
 * Name: Java class SmNode.java
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

import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class defines a node representing an item added to the Node Explorer
 * hierarchical tree view structure. A node can represent either directory or
 * file.
 * @author png
 */
public class SmNode extends DefaultMutableTreeNode {
    private String filePath;
    private boolean checked = false;
    
    /**
     * Constructor
     * @param userObject a JComponent object representing the owner of the node
     */
    public SmNode(Object userObject) {
        super(userObject);
    }
    
    /**
     * Constructor
     * @param userObject a JComponent object representing the owner of the node
     * @param allowsChildren true if the node allows children, false otherwise
     */
    public SmNode(Object userObject, boolean allowsChildren) {
        super(userObject,allowsChildren);
    }
    
    /**
     * Constructor
     * @param userObject a JComponent object representing the owner of the node
     * @param allowsChildren true if the node allows children, false otherwise
     * @param checked true if the node is checked, false otherwise
     */
    public SmNode(Object userObject, boolean allowsChildren, boolean checked) {
        super(userObject,allowsChildren);
        
        this.checked = checked;
    }
    
    /**
     * 
     * @param userObject a JComponent object representing the owner of the node
     * @param allowsChildren true if the node allows children, false otherwise
     * @param filePath file pathname of the file represented by the node
     * @param checked true if the node is checked, false otherwise
     */
    public SmNode(Object userObject, boolean allowsChildren, String filePath, boolean checked) {
        super(userObject,allowsChildren);
        
        this.filePath = filePath;
        this.checked = checked;
    }
    
    // Copy constructor
    public SmNode(SmNode smNode) {
        super(smNode.getUserObject(),smNode.allowsChildren);
        
        this.filePath = smNode.getFilePath();
        this.checked = smNode.getChecked();
    }
    
    /**
     * Gets the file pathname of the file represented by the node
     * @return file pathname of the file represented by the node
     */
    public String getFilePath() {return this.filePath;}
    
    /**
     * Gets the boolean value indicating whether or not the node is checked
     * @return boolean value indicating whether or not the node is checked
     */
    public boolean getChecked() {return this.checked;}

    /**
     * Sets the file pathname of the file represented by the node
     * @param filePath file pathname of the file represented by the node
     */
    public void setFilePath(String filePath) {this.filePath = filePath;}
    
    /**
     * Sets the boolean value indicating whether or not the node is checked
    */
    public void setChecked(boolean checked) {this.checked = checked;}
    
    /**
     * Creates a formatted string that denotes the owner of the node
     * @return formatted string that denotes the owner of the node
     */
    @Override
    public String toString() {
        return this.getUserObject().toString();
    }
    
    /*Comparator for sorting the list by Student Name*/
    public static Comparator<SmNode> SmNodeComparator = new Comparator<SmNode>() {

	public int compare(SmNode node1, SmNode node2) {
           String nodeName1 = node1.getUserObject().toString().toUpperCase();
           String nodeName2 = node2.getUserObject().toString().toUpperCase();
        
	   //ascending order
	   return nodeName1.compareTo(nodeName2);

	   //descending order
	   //return nodeName2.compareTo(nodeName1);
    }};
}
