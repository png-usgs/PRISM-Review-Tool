/*******************************************************************************
 * Name: Java class SmStation.java
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
import org.joda.time.DateTime;

/**
 *
 * @author png-pr
 */
public class SmStation {
    private String stationCode = "";
    private String description = "";
    private String networkCode = "";
    
    private ArrayList<SmEpoch> smEpochs;
    
    public SmStation() {}

    public SmStation(String stationCode, String description, String networkCode,
        ArrayList<SmEpoch> smEpochs)
    {
        this.stationCode = stationCode;
        this.description = description;
        this.networkCode = networkCode;
        this.smEpochs = smEpochs;
    }
    
    // Copy constructor
    public SmStation(SmStation smStation)
    {
        this.stationCode = smStation.getStationCode();
        this.description = smStation.getDescription();
        this.networkCode = smStation.getNetworkCode();
        this.smEpochs = new ArrayList<>();
        for (SmEpoch smEpoch : smStation.getSmEpochs()) {
            this.smEpochs.add(new SmEpoch(smEpoch));
        }
    }

    public String getStationCode() {return this.stationCode;}
    public String getDescription() {return this.description;}
    public String getNetworkCode() {return this.networkCode;}
    public ArrayList<SmEpoch> getSmEpochs() {return this.smEpochs;}

    public SmEpoch getSmEpoch(DateTime startUTC, DateTime endUTC) {
        for (SmEpoch smEpoch : this.smEpochs) {
            if (startUTC.compareTo(smEpoch.getStartUTC()) >= 0 &&
                endUTC.compareTo(smEpoch.getEndUTC()) <= 0) {
                return smEpoch;
            }
        }
        
        return null;
    }
    
    public void setStationCode(String stationCode) {this.stationCode = stationCode;}
    public void setDescription(String description) {this.description = description;}
    public void setNetworkCode(String networkCode) {this.networkCode = networkCode;}
    public void setSmEpochs(ArrayList<SmEpoch> smEpochs) {this.smEpochs = smEpochs;}

    public void addSmEpoch(DateTime startUTC, DateTime endUTC, ArrayList<SmChannel> smChannels)
    {
        this.smEpochs.add(new SmEpoch(startUTC, endUTC, smChannels));
    }
    
}
