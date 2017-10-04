/*******************************************************************************
 * Name: Java class SmEpoch.java
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

import java.awt.Color;
import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 * This class defines a structure for storing information about an epoch used to 
 * delineate a group of station channels for the epoch specified time period. Each 
 * epoch includes start and end UTC date times as well as an array list of SmChannel 
 * objects.
 * @author png-pr
 */
public class SmEpoch {
    
    private DateTime startUTC;
    private DateTime endUTC;
    
    private ArrayList<SmChannel> smChannels;
    
    /**
     * Constructor
     * @param startUTC start UTC date time
     * @param endUTC end UTC date time
     * @param smChannels array list of station channels
     */
    public SmEpoch(DateTime startUTC, DateTime endUTC, ArrayList<SmChannel> smChannels)
    {
        this.startUTC = startUTC;
        this.endUTC = endUTC;
        this.smChannels = smChannels;
    }
    
    // Copy constructor
    public SmEpoch(SmEpoch smEpoch)
    {
        this.startUTC = smEpoch.getStartUTC();
        this.endUTC = smEpoch.getEndUTC();
        this.smChannels = new ArrayList<>();
        for (SmChannel smChannel : smEpoch.getSmChannels()) {
            this.smChannels.add(new SmChannel(smChannel));
        }
    }
    
    /**
     * Gets the start UTC DateTime object
     * @return start UTC DateTime object
     */
    public DateTime getStartUTC() {return this.startUTC;}
    
    /**
     * Gets the end UTC DateTime object
     * @return end UTC DateTime object
     */
    public DateTime getEndUTC() {return this.endUTC;}
    
    /**
     * Gets the array list of SmChannel objects
     * @return array list of SmChannel objects
     */
    public ArrayList<SmChannel> getSmChannels() {return this.smChannels;}
    
    /**
     * Sets the start UTC DateTime object
     * @param startUTC start UTC DateTime object
     */
    public void SetStartUTC(DateTime startUTC) {this.startUTC = startUTC;}
    
    /**
     * Sets the end UTC DateTime object
     * @param endUTC end UTC DateTime object
     */
    public void SetEndUTC(DateTime endUTC) {this.endUTC = endUTC;}
    
    /**
     * Sets the array list of station channels
     * @param smChannels array list of station channels
     */
    public void SetSmChannels(ArrayList<SmChannel> smChannels) {this.smChannels = smChannels;}
    
    /**
     * Adds an SmChannel object to the array list of station channels
     * @param seed channel seed value
     * @param lCode channel lCode value
     * @param location the location of channel
     * @param azimuth the azimuth of channel
     * @param inclination the inclination of channel
     * @param color the color to use when plotting data for channel
     */
    public void addSmChannel(String seed, String lCode, String location,
        String azimuth, String inclination, Color color)
    {
        this.smChannels.add(new SmChannel(seed,lCode,location,azimuth,inclination,color));
    }
    
    /**
     * Gets the SmChannel associated with specified channel
     * @param channel the channel for the SmChannel object to retrieve
     * @return SmChannel object
     */
    public SmChannel getSmChannel(String channel) {
        for (SmChannel smChannel : smChannels) {
            if (smChannel.getChannel().equals(channel))
                return smChannel;
        }
        return null;
    }
    
    //public String getStartUTC_Str() {return this.startUTC.toString(dtf);}
    
    /*
    try {
        String eventDate = smRec.getEventDateTime();

        SimpleDateFormat formatOrig = new SimpleDateFormat("E MMM dd, yyyy HH:mm");
        SimpleDateFormat formatNew = new SimpleDateFormat("MM/dd/yyyy");

        Date date = formatOrig.parse(eventDate);

        sbEventDesc.append(formatNew.format(date));
    }
    catch (ParseException ex) {
    }
    */
}
