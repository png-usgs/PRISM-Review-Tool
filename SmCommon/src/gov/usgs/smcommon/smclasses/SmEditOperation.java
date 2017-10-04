/*******************************************************************************
 * Name: Java class SmEditOperation.java
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class defines a structure for storing information pertaining to an 
 * edit operation step that is performed in the Seismic Trace Editor.
 * @author png
 */
public class SmEditOperation {
    
    private OperationType operationType;
    
    // List of form input parameters set at the time when the edit operation was performed.
    private ArrayList<Parm<?,?>> parms = new ArrayList<>();
    
    private String description;
    
    /**
     * Class constructor
     */
    public SmEditOperation() {}
    
    /**
     * Class constructor
     * @param operationType the type of edit operation performed
     */
    public SmEditOperation(OperationType operationType) {
        this.operationType = operationType;
    }
    
    /**
     * Class constructor
     * @param operationType the type of edit operation performed
     * @param parms form input parameters set at the time the edit operation was performed
     */
    public SmEditOperation(OperationType operationType, ArrayList<Parm<?,?>> parms) {
        this.operationType = operationType;
        this.parms = parms;
    }
    
    /**
     * Class constructor
     * @param operationType the type of edit operation performed
     * @param parms form input parameters set at the time the edit operation was performed
     * @param description details about the edit operation performed
     */
    public SmEditOperation(OperationType operationType, ArrayList<Parm<?,?>> parms, 
        String description) {
        this.operationType = operationType;
        this.parms = parms;
        this.description = description;
    }
    
    /**
     * Gets the edit operation enumerated type
     * @return edit operation enumerated type
     */
    public OperationType getOperationType() {return this.operationType;}
    
    /**
     * Gets the array list of input parameters
     * @return array list of input parameters
     */
    public ArrayList<Parm<?,?>> getParms() {return this.parms;}
    
    /**
     * Gets the description of edit operation
     * @return description
     */
    public String getDescription() {return this.description;}
    
    /**
     * Sets the edit operation enumerated type
     * @param operationType edit operation enumerated type
     */
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    /**
     * Sets the array list of input parameters
     * @param parms array list of input parameters
     */
    public void setParms(ArrayList<Parm<?,?>> parms) {
        this.parms = parms;
    }
    
    /**
     * Sets the description of the edit operation performed
     * @param description the description of the edit operation performed
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Adds an input parameter item to the input parameter array list
     * @param parm input parameter item to add to the input parameter array list
     */
    public void addParm(Parm<?,?> parm) {
        this.parms.add(parm);
    }
    
    /**
     * Constructs a custom string that includes the edit operation enumerated 
     * type and all the input parameters
     * @return a custom string that includes the edit operation enumerated type
     * and all the input parameters
     */
    @Override
    public String toString() {
        
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(this.operationType);
        
        if (this.parms.size() > 0) {
            strBuilder.append(" (");
            
            for (int i=0; i < this.parms.size(); i++) {
                if (i > 0)
                    strBuilder.append(",");
                
                strBuilder.append(parms.get(i).toString());
            }
            
            strBuilder.append(")");
        }
            
        return strBuilder.toString();
    }
    
    /**
     * This class defines the key and value paired structure used for storing
     * an input parameter
     * @param <K> the key for the input parameter
     * @param <V> the value of the input parameter
     */
    public static class Parm<K,V> {
        private K k;
        private V v;
        
        /**
         * Constructor
         * @param k the key that uniquely identifies an input parameter
         * @param v the value of the input parameter
         */
        public Parm(K k, V v) {
            this.k = k;
            this.v = v;
        }
    
        /**
         * Gets the key that uniquely identifies an input parameter
         * @return the key for the input parameter
         */
        public K getKey() {return this.k; }
        
        /**
         * Gets the value of the input parameter
         * @return the value of the parameter
         */
        public V getValue() {return this.v;}
        
        /**
         * Sets the key that uniquely identifies an input parameter
         * @param k the key for the input parameter
         */
        public void setKey(K k) {this.k = k;}
        
        /**
         * Sets the value for the input parameter
         * @param v the value of the input parameter
         */
        public void setValue(V v) {this.v = v;}
        
        /**
         * Creates a formatted string that depicts the key and value pair
         * representing an input parameter
         * @return the formatted string that depicts the key and value pair  
         * representing an input parameter
         */
        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("0.0###");
            df.setRoundingMode(RoundingMode.CEILING);
                
            if (this.v instanceof Double) 
                //return String.format("%s=%.4f", k.toString(),(Double)this.v);
                return String.format("%s=%s", k.toString(),df.format(this.v));
            if (this.v instanceof Integer) 
                return String.format("%s=%d", k.toString(),(Integer)this.v);
            else
                return String.format("%s=\"%s\"", this.k.toString(),this.v.toString());
        }
    }
    
    /**
     * This enumeration defines the various edit operations that can be performed
     * in the Seismic Trace Editor
     */
    public static enum OperationType {
        SelectChartViewer,
        DrawMarker,
        ShowBaselineFunction,
        HideBaselineFunction,
        PreviewBLC,
        RemoveBLC,
        Filter,
        UnFilter,
        Commit,
        Reset
    }
}



