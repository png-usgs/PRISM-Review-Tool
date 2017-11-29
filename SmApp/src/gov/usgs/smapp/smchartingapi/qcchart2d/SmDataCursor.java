/*******************************************************************************
 * Name: Java class SmDataCursor.java
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
import com.quinncurtis.chart2djava.ChartConstants;
import static com.quinncurtis.chart2djava.ChartConstants.PHYS_POS;
import com.quinncurtis.chart2djava.ChartView;
import com.quinncurtis.chart2djava.DataCursor;
import com.quinncurtis.chart2djava.GraphObj;
import com.quinncurtis.chart2djava.Marker;
import com.quinncurtis.chart2djava.NumericLabel;
import gov.usgs.smapp.smforms.SmFAS_Editor;
import gov.usgs.smapp.smforms.SmSeismicTraceEditor;
import gov.usgs.smapp.smforms.SmTrimTool;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author png
 */
@SuppressWarnings("rawtypes")
public class SmDataCursor extends DataCursor {
   
    public SmDataCursor(ChartView chartview, CartesianCoordinates transform, 
        int nMarkerType, double rSize)
    {
        super(chartview, transform, nMarkerType, rSize);
    }
    
    public void drawNumericLabel(double x, double y) {
        // Remove previously drawn label.
        removeNumericLabel();
        
        Font font = new Font("SansSerif", Font.PLAIN,10);
        NumericLabel label = new NumericLabel(getChartObjScale(),font,
            x, x, y, PHYS_POS, DECIMALFORMAT, 4);
        // Nudge text to the right and up
        label.setTextNudge(5,-5);
        
        // Add marker to chart.
        ChartView chartView = getChartObjComponent();
        chartView.addChartObject(label);
        chartView.updateDraw();
    }
    
    public void removeNumericLabel() {
        ChartView chartView = getChartObjComponent();
        Vector<GraphObj> chartObjects = chartView.getChartObjectsVector();
        
        Iterator<GraphObj> itr = chartObjects.iterator();
        
        while(itr.hasNext()) {
            GraphObj obj = itr.next();
            if (obj instanceof NumericLabel)
            {
                itr.remove();
                break;
            }
        }
        chartView.updateDraw();
    }
    
    public void removeMarker()
    {
        ChartView chartView = getChartObjComponent();
        Vector<GraphObj> chartObjects = chartView.getChartObjectsVector();
        
        Iterator<GraphObj> itr = chartObjects.iterator();
        
        while(itr.hasNext()) {
            GraphObj obj = itr.next();
            if (obj instanceof Marker)
            {
                Marker marker = (Marker)obj;
                
                if (marker.getColor().getRGB() == this.getColor().getRGB()) {
                    itr.remove();
                    break;
                }
            }
        }
    }
	  
    public void removeMarkers() 
    {
        ChartView chartView = getChartObjComponent();
        Vector<GraphObj> chartObjects = chartView.getChartObjectsVector();
        
        Iterator<GraphObj> itr = chartObjects.iterator();
        
        while(itr.hasNext()) {
            GraphObj obj = itr.next();
            if (obj instanceof Marker)
                itr.remove();
        }
        
        chartView.updateUI();
    }
    
    public void drawMarker(double x, double y) {
        // Clear old marker.
        removeMarker();

        // Create marker object, placing it at the nearest point
        Marker marker = new Marker(getChartObjScale(), ChartConstants.MARKER_VLINE, 
            x, y, 8.0, PHYS_POS);
        
        marker.setLineStyle(this.getLineStyle());
        marker.setLineColor(this.getColor());
        marker.setLineWidth(this.getLineWidth());
        
        // Add marker to chart.
        ChartView chartView = getChartObjComponent();
        chartView.addChartObject(marker);
        chartView.updateDraw();
    }
    
    public Marker getMarker(Color color) {
        ChartView chartView = getChartObjComponent();
        Vector<GraphObj> chartObjects = chartView.getChartObjectsVector();
        
        Iterator<GraphObj> itr = chartObjects.iterator();
        
        while(itr.hasNext()) {
            GraphObj obj = itr.next();
            if (obj instanceof Marker) {
                Marker marker = (Marker)obj;
                if (marker.getColor().equals(color)) {
                    return marker;
                }
            }
        }
        
        return null;
    }
    
    private void dispatchToParent(MouseEvent event){
        Component source = (Component) event.getSource();
        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, event, source.getParent());
        source.getParent().dispatchEvent(parentEvent);
    }
    
    @Override
    public void mousePressed(MouseEvent event)
    {  
        try 
        {
            // Get parent SmChartView and its owner.
            SmChartView chartView = (SmChartView)getChartObjComponent();
            Object owner = chartView.getOwner();
            
            if (owner instanceof SmSeismicTraceEditor) {
                SmSeismicTraceEditor editor = (SmSeismicTraceEditor)owner;
                
                Object container = chartView.getParent();
                
                if (container instanceof JPanel) {
                    JPanel pnl = (JPanel)container;
                    JPanel curPnl = editor.getCurrentChartViewer();

                    if (curPnl.getName().equals(pnl.getName())) {
                        super.mousePressed(event);
                    }
                }
            }
            else if (owner instanceof SmTrimTool) {
                SmTrimTool trimTool = (SmTrimTool)owner;
                SmChartView curChartView = trimTool.getCurChartView();
                
                if (curChartView.getName().equals(chartView.getName())) {
                    super.mousePressed(event);
                }
            }
            else {
                super.mousePressed(event);
            }
        }
        catch (Throwable e )
        {
        }
    }

    // override mouseDragged method in order to add stuff
    @Override
    public void mouseDragged (MouseEvent event)
    {  
        try 
        {
            // Get parent SmChartView and its owner.
            SmChartView chartView = (SmChartView)getChartObjComponent();
            Object owner = chartView.getOwner();
            
            if (owner instanceof SmSeismicTraceEditor) {
                SmSeismicTraceEditor editor = (SmSeismicTraceEditor)owner;
                
                Object container = chartView.getParent();
                
                if (container instanceof JPanel) {
                    JPanel pnl = (JPanel)container;
                    JPanel curPnl = editor.getCurrentChartViewer();

                    if (curPnl.getName().equals(pnl.getName())) {
                        super.mouseDragged(event);
                        
                        // Draw numeric label.
                        drawNumericLabel(location.getX(),location.getY());

                        // Draw marker.
                        chartView.getSmDataCursor().drawMarker(location.getX(), 0);

                        // Update bounded text field value.
                        //chartView.setBoundedTextFieldText(String.format("%.4f", location.getX()));
                    }
                }
            }
            else if (owner instanceof SmTrimTool) {
                SmTrimTool trimTool = (SmTrimTool)owner;
                SmChartView curChartView = trimTool.getCurChartView();
                
                if (curChartView.getName().equals(chartView.getName())) {
                    super.mouseDragged(event);

                    // Draw numeric label.
                    drawNumericLabel(location.getX(),location.getY());

                    // Draw marker.
                    chartView.getSmDataCursor().drawMarker(location.getX(), 0);

                    // Update bounded text field value.
                    //chartView.setBoundedTextFieldText(String.format("%.4f", location.getX()));
                }
            }
            else {
                super.mouseDragged(event);
                
                // Draw numeric label.
                drawNumericLabel(location.getX(),location.getY());
                
                // Draw marker.
                chartView.getSmDataCursor().drawMarker(location.getX(), 0);
            }
        }
        catch (Throwable e)
        {
        }
    }

    @Override
    public void mouseReleased (MouseEvent event)
    {  
        try
        {  
            //super.mouseReleased(event);
            
            if ((event.getModifiers() & getButtonMask() ) != 0)
            {
                super.mouseReleased(event);
                
                /*
                // Get parent SmChartView and its owner.
                SmChartView chartView = (SmChartView)getChartObjComponent();
                Object owner = chartView.getOwner();
                
                if (owner instanceof SmSeismicTraceEditor) {
                    SmSeismicTraceEditor editor = (SmSeismicTraceEditor)owner;
                    
                    Object container = chartView.getParent();
                
                    if (container instanceof JPanel) {
                        JPanel pnl = (JPanel)container;
                        JPanel curPnl = editor.getCurrentChartViewer();

                        if (curPnl.getName().equals(pnl.getName())) {
                            // Remove previously drawn numeric label.
                            removeNumericLabel();

                            // Draw marker.
                            chartView.getSmDataCursor().drawMarker(location.getX(), 0);

                            // Update bounded text field value.
                            chartView.setBoundedTextFieldValue(location.getX());
                        }
                        else {
                            editor.setCurrentChartViewer(pnl);
                        }
                    }
                }
                */
            }
        }
        catch (Throwable e)
        {
        }
    }
}
