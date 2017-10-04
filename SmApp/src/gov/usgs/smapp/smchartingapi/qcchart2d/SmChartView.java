/*******************************************************************************
 * Name: Java class SmChartView.java
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
package gov.usgs.smapp.smchartingapi.qcchart2d;

import com.quinncurtis.chart2djava.CartesianCoordinates;
import com.quinncurtis.chart2djava.ChartBufferedImage;
import com.quinncurtis.chart2djava.ChartConstants;
import com.quinncurtis.chart2djava.ChartPrint;
import com.quinncurtis.chart2djava.ChartView;
import com.quinncurtis.chart2djava.ChartZoom;
import com.quinncurtis.chart2djava.ImageFileChooser;
import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smcommon.smclasses.SmPreferences.MarkerStyle;
import java.awt.Color;
import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author png
 */
public class SmChartView extends ChartView {
    public static enum ChartingMode {ZOOM,DATACURSOR};
    
    protected CartesianCoordinates pTransform;
    
    protected ChartView chartView = this;
    protected XYBounds xyBounds;
    protected int xScaleType;
    protected int yScaleType;
    protected String chartTitle;
    protected String xAxisTitle;
    protected String yAxisTitle;
    
    private JFormattedTextField boundedTextField;
    
    protected String plotType;
    protected boolean editMode;
    protected ChartingMode chartingMode;

    protected ChartZoom chartZoom;
    protected SmDataCursor smDataCursor;
    protected JPopupMenu popupMenu;
    
    protected Object owner;
    
    
//    protected final double LEFT_PLOT_AREA = .15;
//    protected final double TOP_PLOT_AREA = .25;
//    protected final double RIGHT_PLOT_AREA = .85;
//    protected final double BOTTOM_PLOT_AREA = .75;
    
    public SmChartView() {
    }
    
    public XYBounds getXYBounds() {return this.xyBounds;}
    public int getXScaleType() {return this.xScaleType;}
    public int getYScaleType() {return this.yScaleType;}
    public String getChartTitle() {return this.chartTitle;}
    public String getXAxisTitle() {return this.xAxisTitle;}
    public String getYAxisTitle() {return this.yAxisTitle;}
    public JTextField getBoundedTextField() {return this.boundedTextField;}
    public String getPlotType() {return this.plotType;}
    public boolean getEditMode() {return this.editMode;}
    public ChartingMode getChartingMode() {return this.chartingMode;}
    public ChartZoom getChartZoom() {return this.chartZoom;}
    public SmDataCursor getSmDataCursor() {return this.smDataCursor;}
    public Object getOwner() {return this.owner;}
    
    public final void setXYBounds(XYBounds xyBounds) {this.xyBounds = xyBounds;}
    public final void setXScaleType(int xScaleType) {this.xScaleType = xScaleType;}
    public final void setYScaleType(int yScaleType) {this.yScaleType = yScaleType;}
    public final void setChartTitle(String chartTitle) {this.chartTitle = chartTitle;}
    public final void setXAxisTitle(String xAxisTitle) {this.xAxisTitle = xAxisTitle;}
    public final void setYAxisTitle(String yAxisTitle) {this.yAxisTitle = yAxisTitle;}

    public final void setBoundedTextField(JFormattedTextField boundedTextField) {
        this.boundedTextField = boundedTextField;
    }
    public final void setPlotType(String plotType) {this.plotType = plotType;}
    public final void setEditMode(boolean editMode) {
        this.editMode = editMode;
        
        if (this.popupMenu != null) {
            for (Component component : this.popupMenu.getComponents()) {
                if (component instanceof JRadioButton) {
                    JRadioButton rbtn = (JRadioButton)component;
                    if (rbtn.getName().equals("DataCursor")) {
                        rbtn.setSelected(editMode);
                    }
                }
            }
        }
    }
    public final void setChartingMode(ChartingMode chartingMode) {this.chartingMode = chartingMode;}
    
    public final void setOwner(Object owner) {this.owner = owner;}
    
    public final String getBoundedTextFieldText() {
        if (this.boundedTextField == null)
            return null;
        
        return this.boundedTextField.getText();
    }
    
    public final double getBoundedTextFieldValue() {
        if (this.boundedTextField == null)
            return -9999;
        
        return (double)this.boundedTextField.getValue();
    }
    
    public final void setBoundedTextFieldText(String text) {
        if (this.boundedTextField == null)
            return;
        
        this.boundedTextField.setText(text);
    }
    
    public final void setBoundedTextFieldValue(double value) {
        if (this.boundedTextField == null)
            return;
        
        this.boundedTextField.setValue(value);
    }
    
    public final void setChartZoom(boolean enable) {
        if (enable)
        {
            this.chartZoom.setZoomEnable(true);
            this.chartZoom.addZoomListener();
        }
        else
        {
            this.chartZoom.setZoomEnable(false);
            this.chartZoom.removeZoomListener();
        }
    }
    
    public final void setSmDataCursor(boolean enable) {
        if (enable)
        {
            this.smDataCursor.setDataCursorEnable(true);
            this.smDataCursor.addDataCursorListener();
        }
        else
        {
            this.smDataCursor.setDataCursorEnable(false);
            this.smDataCursor.removeDataCursorListener();
        }
    }
    
    public final void setSmDataCursorColor(Color color) {
        this.smDataCursor.setColor(color);
    }
    
    public final void setSmDataCursorStyle(MarkerStyle markerStyle) {
        if (markerStyle == MarkerStyle.SOLID)
            this.smDataCursor.setLineStyle(ChartConstants.LS_SOLID);
        else if (markerStyle == MarkerStyle.DASH)
            this.smDataCursor.setLineStyle(ChartConstants.LS_DASH_4_4);
        else
            this.smDataCursor.setLineStyle(ChartConstants.LS_DOT_1_4);
    }
    
    public final void setSmDataCursorWidth(double width) {
        this.smDataCursor.setLineWidth(width);
    }
    
    public final void print(ChartPrint printobj) {
//            printobj.setPrintChartView(this);
        if (printobj.printDialog())
            printobj.startPrint();
    }

    public final void saveJPEG() { 
//        String fileName = "SmChart.jpg";
        ChartBufferedImage savegraphJPEG = new ChartBufferedImage();
        ImageFileChooser imageFileChooser = new ImageFileChooser(this);

        if (imageFileChooser.process(""))
        {
            String fileName = imageFileChooser.getSelectedFilename();
            
            String jpeg = ".jpg";
            String regEx = String.format("(?i)(^.+)([\\.][\\w]+$)");
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(fileName);
            if (m.find()) {
                if (!m.group(2).equalsIgnoreCase(jpeg)) 
                    fileName = m.group(1) + jpeg;
            }
            else 
                fileName = fileName + jpeg;
            
            savegraphJPEG.setChartViewComponent(this);
            savegraphJPEG.render();
            savegraphJPEG.saveImageAsJPEG(fileName);
        }
    }
}
