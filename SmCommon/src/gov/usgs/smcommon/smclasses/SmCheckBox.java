/*******************************************************************************
 * Name: Java class SmCheckBox.java
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * This class extends JCheckBox to customize the look and feel of the check box.
 * 
 * @author png
 */
public class SmCheckBox extends JCheckBox {
    
    /**
     * Class constructor
     * @param text the text used to label the check box
     * @param color the color used to draw the select and unselected icons of
     * the check box
     */
    public SmCheckBox(String text, Color color)
    {
        Icon checked = new RectIcon(color, true);
        Icon unchecked = new RectIcon(color, false);
        
        this.setSelected(true);
        this.setText(text);
        this.setIcon(unchecked);
        this.setSelectedIcon(checked);
        this.setIconTextGap(checked.getIconWidth()*2);      
    }
    
    /**
     * This class extends AbstractAction to override one or more
     * of its functions.
     */
    private class CheckboxAction extends AbstractAction {

        /**
         * Class constructor
         * @param text the text used for labeling the check box
         */
        public CheckboxAction(String text) {
            super(text);
        }

        /**
         * Handles the event associated with selecting or un-selecting the
         * check box.
         * @param event the ActionEvent object
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            JCheckBox checkbox = (JCheckBox) event.getSource();
            if (checkbox.isSelected()) {
//                System.out.println("Checkbox is enabled");
            } 
            else {
//                System.out.println("Checbox is disabled");
            }
        }
    }
    
    /**
     * This class implements the Icon interface to define an icon of a rectangle
     * that will be used to represent a check box.
     */
    private class RectIcon implements Icon {
        private Color color;
        private boolean selected;
        private int width;
        private int height;
        private Polygon poly;

        private static final int DEFAULT_WIDTH = 10;
        private static final int DEFAULT_HEIGHT = 10;

        /**
         * Class constructor
         * @param color the color used for drawing the rectangle
         */
        public RectIcon(Color color) {
            this(color, true, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        /**
         * Class constructor
         * @param color the color used for drawing the rectangle
         * @param selected true or false value indicating the selection state
         * of the check box
         */
        public RectIcon(Color color, boolean selected) {
            this(color, selected, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        /**
         * Class constructor
         * @param color the color used for drawing the rectangle
         * @param selected true or false value indicating the selection state
         * of the check box
         * @param width the width of the rectangle
         * @param height the height of the rectangle
         */
        public RectIcon(Color color, boolean selected, int width, int height) {
            this.color = color;
            this.selected = selected;
            this.width = width;
            this.height = height;
            initPolygon();
        }

        /**
         * Initializes the Polygon object used for drawing the rectangle
         */
        private void initPolygon() {
            poly = new Polygon();

            poly.addPoint(0, 0);
            poly.addPoint(0, height);
            poly.addPoint(width*2, height);
            poly.addPoint(width*2, 0);
        }

        /**
         * Gets the height of the check box
         * @return check box height
         */
        @Override
        public int getIconHeight() {
            return height;
        }

        /**
         * Gets the width of the check box
         * @return check box width
         */
        @Override
        public int getIconWidth() {
            return width;
        }

        /**
         * Draws the icon.
         * @param c Component object representing the icon itself
         * @param g Graphics object used for drawing the icon
         * @param x x coordinate of a point defined for drawing the icon
         * @param y y coordinate of a point defined for drawing the icon
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.translate(x, y);

            if (selected) {
                g.fillPolygon(poly);
            } 
            else {
                g.drawPolygon(poly);
            }

            g.translate(-x, -y);
        }
    }
}


