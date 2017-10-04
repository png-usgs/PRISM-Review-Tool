/*******************************************************************************
 * Name: Java class VxChartsBinGroup.java
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

package gov.usgs.smapp;

import gov.usgs.smcommon.smclasses.SmPreferences;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class defines a group of two bins, one bin for storing charts of V1 data,
 * the other for storing charts of V2 data.
 * @author png
 */
public class VxChartsBinGroup {
    private final String groupName;
    private final String stationDesc;
    private final String eventDesc;
    private final String chartAPI;
    private final V1ChartsBin v1ChartsBin;
    private final V2ChartsBin v2ChartsBin;

    /**
     * Constructor.
     * @param groupName group name.
     * @param stationDesc station description.
     * @param eventDesc event description.
     * @param chartAPI name of chart API to use.
     * @param v1ChartsBin V1ChartsBin object representing the bin for storing charts of V1 data.
     * @param v2ChartsBin V2ChartsBin object representing the bin for storing charts of V2 data.
     */
    public VxChartsBinGroup(String groupName, String stationDesc, String eventDesc,
        String chartAPI, V1ChartsBin v1ChartsBin, V2ChartsBin v2ChartsBin)
    {
        this.groupName = groupName;
        this.stationDesc = stationDesc;
        this.eventDesc = eventDesc;
        this.chartAPI = chartAPI;
        this.v1ChartsBin = v1ChartsBin;
        this.v2ChartsBin = v2ChartsBin;
    }

    /**
     * Gets the group name.
     * @return group name.
     */
    public String getGroupName()
    {
        return this.groupName;
    }
    
    /**
     * Gets the description of station.
     * @return station description.
     */
    public String getStationDesc()
    {
        return this.stationDesc;
    }
    
    /**
     * Gets the description of event.
     * @return event description.
     */
    public String getEventDesc()
    {
        return this.eventDesc;
    }

    /**
     * Gets the name of the chart API.
     * @return name of chart API.
     */
    public String getChartAPI()
    {
        return this.chartAPI;
    }
    
    /**
     * Gets the V1ChartsBin object representing the bin for storing charts of V1 data.
     * @return V1ChartsBin object.
     */
    public V1ChartsBin getV1ChartsBin()
    {
        return this.v1ChartsBin;
    }

    /**
     * Gets the V2ChartsBin object representing the bin for storing charts of V2 data.
     * @return V2ChartsBin object.
     */
    public V2ChartsBin getV2ChartsBin()
    {
        return this.v2ChartsBin;
    }
    
    /**
     * Creates a header JPanel.
     * @return header JPanel object.
     */
    public JPanel createHeader() {
        // Create font.
        String fontName = SmPreferences.ChartOptions.getFontName();
        boolean fontIsBold = SmPreferences.ChartOptions.getFontIsBold();
        boolean fontIsItal = SmPreferences.ChartOptions.getFontIsItalic();
        int fontSize = SmPreferences.ChartOptions.getFontSize();

        Font font = new Font(fontName, (fontIsBold ? Font.BOLD : 0) +
            (fontIsItal ? Font.ITALIC : 0), fontSize);

        // Create station and event description label objects.
        JLabel lblStationDesc = new JLabel(this.getStationDesc());
        lblStationDesc.setFont(font);

        JLabel lblEventDesc = new JLabel(this.eventDesc);
        lblEventDesc.setFont(font);

        // Creat header panel.
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setFont(font);
        pnlHeader.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        pnlHeader.add(lblStationDesc, BorderLayout.WEST);
        pnlHeader.add(lblEventDesc, BorderLayout.EAST);

        return pnlHeader;
    }
}
