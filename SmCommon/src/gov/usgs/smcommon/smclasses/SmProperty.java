/*******************************************************************************
 * Name: Java class SmProperty.java
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

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author png
 */
public class SmProperty {
    private SimpleStringProperty mName;
    private SimpleStringProperty mValue;

    public SmProperty(String name, String value)
    {
        mName = new SimpleStringProperty(name);
        mValue = new SimpleStringProperty(value);
    }

    public String getName() {
        return mName.get();
    }
    public void setName(String name) {
        mName.set(name);
    }

    public String getValue() {
        return mValue.get();
    }
    public void setValue(String value) {
        mValue.set(value);
    }
}
