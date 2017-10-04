/*******************************************************************************
 * Name: Java class SmChannel.java
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

/**
 * This class encapsulates properties and methods pertaining to a station channel,
 * the value of which is store as an attribute in COSMOS formatted files.
 *
 * @author png
 */
public class SmChannel {

    private String seed = "";
    private String lCode = "";
    private String location = "";
    private String azimuth = "";
    private String inclination = "";
    private Color color = null;

    /**
     * Class constructor
     */
    public SmChannel() {
    }

    /**
     * Class constructor with parameters representing attributes associated with 
     * a channel
     *
     * @param seed seed component of channel
     * @param lCode LCode component of channel
     * @param location location of channel
     * @param azimuth horizontal rotational angle of channel
     * @param inclination vertical rotational angle of channel
     * @param color color used to plot data for channel
     */
    public SmChannel(String seed, String lCode, String location,
            String azimuth, String inclination, Color color) {
        this.seed = seed;
        this.lCode = lCode;
        this.location = location;
        this.azimuth = azimuth;
        this.inclination = inclination;
        this.color = color;
    }

    /**
     * Copy constructor
     *
     * @param smChannel SmChannel object to copy
     */
    public SmChannel(SmChannel smChannel) {
        this.seed = smChannel.getSeed();
        this.lCode = smChannel.getLCode();
        this.location = smChannel.getLocation();
        this.azimuth = smChannel.getAzimuth();
        this.inclination = smChannel.getInclination();
        this.color = new Color(smChannel.getColor().getRGB());
    }

    /**
     * Gets the seed component of channel
     *
     * @return the channel seed component
     */
    public String getSeed() {
        return this.seed;
    }

    /**
     * Gets the LCode component of channel
     *
     * @return the channel LCode component
     */
    public String getLCode() {
        return this.lCode;
    }

    /**
     * Gets the location of channel
     *
     * @return the channel location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Gets the azimuth of channel
     *
     * @return the azimuth of channel
     */
    public String getAzimuth() {
        return this.azimuth;
    }

    /**
     * Gets inclination of channel
     *
     * @return the inclination of channel
     */
    public String getInclination() {
        return this.inclination;
    }

    /**
     * Gets the color used to plot data for the channel
     *
     * @return the color used to plot data for the channel
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Gets the channel identified as a concatenation of its seed and
     * LCode separated by a period
     *
     * @return the channel
     */
    public String getChannel() {
        return this.seed + "." + this.lCode;
    }

    /**
     * Sets the Seed component of channel
     *
     * @param seed the Seed component of channel
     */
    public void setSeed(String seed) {
        this.seed = seed;
    }

    /**
     * Sets the LCode component of channel
     *
     * @param lCode the LCode component of channel
     */
    public void setLCode(String lCode) {
        this.lCode = lCode;
    }

    /**
     * Sets the location of channel
     *
     * @param location the location of channel
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the azimuth of channel
     *
     * @param azimuth the azimuth of channel
     */
    public void setAzimuth(String azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Sets the inclination of channel
     *
     * @param inclination the inclination of channel
     */
    public void setInclination(String inclination) {
        this.inclination = inclination;
    }

    /**
     * Sets the color used to plot data for channel
     *
     * @param color the color used to plot data for channel
     */
    public void setColor(Color color) {
        this.color = color;
    }

}
