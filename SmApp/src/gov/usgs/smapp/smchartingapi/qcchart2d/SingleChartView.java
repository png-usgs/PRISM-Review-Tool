/*******************************************************************************
 * Name: Java class SingleChartView.java
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

import static SmConstants.VFileConstants.*;
import com.quinncurtis.chart2djava.Axis;
import com.quinncurtis.chart2djava.AxisTitle;
import com.quinncurtis.chart2djava.Background;
import com.quinncurtis.chart2djava.CartesianCoordinates;
import com.quinncurtis.chart2djava.ChartAttribute;
import com.quinncurtis.chart2djava.ChartConstants;
import static com.quinncurtis.chart2djava.ChartConstants.PRT_MAX;
import com.quinncurtis.chart2djava.ChartDimension;
import com.quinncurtis.chart2djava.ChartPrint;
import com.quinncurtis.chart2djava.ChartText;
import com.quinncurtis.chart2djava.ChartZoom;
import com.quinncurtis.chart2djava.GraphObj;
import com.quinncurtis.chart2djava.Grid;
import com.quinncurtis.chart2djava.LegendItem;
import com.quinncurtis.chart2djava.LinearAxis;
import com.quinncurtis.chart2djava.LogAxis;
import com.quinncurtis.chart2djava.NumericAxisLabels;
import com.quinncurtis.chart2djava.PhysicalCoordinates;
import com.quinncurtis.chart2djava.SimpleDataset;
import com.quinncurtis.chart2djava.SimpleLinePlot;
import com.quinncurtis.chart2djava.StandardLegend;
import com.quinncurtis.chart2djava.StringLabel;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smapp.smforms.SmSeismicTraceEditor;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPoint;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmTemplate;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;
import org.openide.windows.WindowManager;

/**
 *
 * @author png
 */
public class SingleChartView extends SmChartView 
{
    private final SmChartView smChartView = this;
    private final SmSeries smSeries;
    private final SimpleDataset dataset;
    private final ChartAttribute lineAttribute;
    private final LegendItem legendItem;
    
    public static enum LinePlotType {PRIMARY,BASELINE};
    
    public SingleChartView(SmSeries smSeries, SimpleDataset dataset, XYBounds xyBounds,
        int xScaleType, int yScaleType, ChartAttribute lineAttribute, 
        LegendItem legendItem, String chartTitle,String xAxisTitle, 
        String yAxisTitle, String plotType, boolean editMode, Object owner)
    {
        // Set member variables.
        this.smSeries = smSeries;
        this.dataset = dataset;
        this.lineAttribute = lineAttribute;
        this.legendItem = legendItem;
        
        setXScaleType(xScaleType);
        setYScaleType(yScaleType);
        setChartTitle(chartTitle);
        setXAxisTitle(xAxisTitle);
        setYAxisTitle(yAxisTitle);
        setPlotType(plotType);
        setEditMode(editMode);
        setOwner(owner);
        
        Color backgroundColor = new Color(SmPreferences.ChartOptions.getColorBackground());
        Color foregroundColor = new Color(SmPreferences.ChartOptions.getColorForeground());
        
        String fontName = SmPreferences.ChartOptions.getFontName();
        boolean fontIsBold = SmPreferences.ChartOptions.getFontIsBold();
        boolean fontIsItal = SmPreferences.ChartOptions.getFontIsItalic();
        int fontSize = SmPreferences.ChartOptions.getFontSize();

        Font font = new Font(fontName, (fontIsBold ? Font.BOLD : 0) +
            (fontIsItal ? Font.ITALIC : 0), fontSize);

        pTransform = new CartesianCoordinates(xScaleType,yScaleType);
        pTransform.setGraphBorderDiagonal(
            SmPreferences.ChartOptions.getPlotAreaPositionLeft(),
            SmPreferences.ChartOptions.getPlotAreaPositionTop(),
            SmPreferences.ChartOptions.getPlotAreaPositionRight(),
            SmPreferences.ChartOptions.getPlotAreaPositionBottom());
        
        if (xyBounds == null)
        {
            if (dataset != null)
                pTransform.autoScale(dataset, ChartConstants.AUTOAXES_FAR,
                    ChartConstants.AUTOAXES_FAR);
        }
        else
        {
            pTransform.setScaleX(xyBounds.getXMin(), xyBounds.getXMax());
            pTransform.setScaleY(xyBounds.getYMin(), xyBounds.getYMax());
        }

        Background background = new Background(pTransform,
            ChartConstants.GRAPH_BACKGROUND,backgroundColor);
        chartView.addChartObject(background);

        Axis xAxis = (xScaleType == ChartConstants.LINEAR_SCALE) ?
            new LinearAxis(pTransform, ChartConstants.X_AXIS) :
            new LogAxis(pTransform, ChartConstants.X_AXIS);
        xAxis.setColor(foregroundColor);
        chartView.addChartObject(xAxis);

        Axis yAxis = (yScaleType == ChartConstants.LINEAR_SCALE) ?
            new LinearAxis(pTransform, ChartConstants.Y_AXIS) :
            new LogAxis(pTransform, ChartConstants.Y_AXIS);
        yAxis.setColor(foregroundColor);
        chartView.addChartObject(yAxis);

        if (SmPreferences.ChartOptions.getDisplayXAxisLabels())
        {
            NumericAxisLabels xAxisLab = new NumericAxisLabels(xAxis);
            xAxisLab.setTextFont(font);
            xAxisLab.setColor(foregroundColor);
            chartView.addChartObject(xAxisLab);
        }

        if (SmPreferences.ChartOptions.getDisplayYAxisLabels())
        {
            NumericAxisLabels yAxisLab = new NumericAxisLabels(yAxis);
            yAxisLab.setTextFont(font);
            yAxisLab.setColor(foregroundColor);
            chartView.addChartObject(yAxisLab);
        }

        if (SmPreferences.ChartOptions.getDisplayXAxisTitle())
        {
            AxisTitle xaxistitle = new AxisTitle(xAxis, font, xAxisTitle);
            xaxistitle.setColor(foregroundColor);
            chartView.addChartObject(xaxistitle);
        }

        if (SmPreferences.ChartOptions.getDisplayYAxisTitle())
        {
            AxisTitle yaxistitle = new AxisTitle(yAxis, font, yAxisTitle);
            yaxistitle.setColor(foregroundColor);
            chartView.addChartObject(yaxistitle);
        }

        if (SmPreferences.ChartOptions.getDisplayXGrid())
        {
            Grid xgrid = new Grid(xAxis, yAxis, ChartConstants.X_AXIS,
                ChartConstants.GRID_MAJOR);
            xgrid.setColor(foregroundColor);
            chartView.addChartObject(xgrid);
        }

        if (SmPreferences.ChartOptions.getDisplayYGrid())
        {
            Grid ygrid = new Grid(xAxis, yAxis, ChartConstants.Y_AXIS,
                ChartConstants.GRID_MAJOR);
            ygrid.setColor(foregroundColor);
            chartView.addChartObject(ygrid);
        }
        
        if (SmPreferences.ChartOptions.getDisplayPlotAnnotation())
        {
            if (!chartTitle.isEmpty())
            {
                double xPos = 0.0, yPos = -SmPreferences.ChartOptions.getPlotAreaPositionTop();
                StringLabel plotNote = new StringLabel(pTransform, font, chartTitle,
                    xPos, yPos, ChartConstants.NORM_PLOT_POS);
                plotNote.setColor(foregroundColor);
                chartView.addChartObject(plotNote);
            }
        }

        // Create and add plot to the ChartView object.
        if (dataset != null)
        {
//            SimpleLinePlot plot = new SimpleLinePlot(pTransform, dataset, lineAttribute);
            MySimpleLinePlot plot = new MySimpleLinePlot(pTransform, dataset, 
                lineAttribute, LinePlotType.PRIMARY);
            chartView.addChartObject(plot);
        }
        
        // Get item attributes.
        String itemText = legendItem.getLegendItemText().getTextString();
        int itemSymbolNumber = legendItem.getLegendItemSymbol().getSymbolNumber();
        Color itemLineColor = legendItem.getLegendItemSymbol().getLineColor();
        Color itemFillColor = legendItem.getLegendItemSymbol().getColor();
        ChartAttribute itemAttr = new ChartAttribute(itemLineColor,1,
            ChartConstants.LS_SOLID, itemFillColor);
        
        // Create legend.
        if (SmPreferences.ChartOptions.getDisplayLegend())
        {
            ChartAttribute legendAttr = new ChartAttribute(
                new Color(SmPreferences.ChartOptions.getColorForeground()), 1,
                ChartConstants.LS_SOLID);
            legendAttr.setFillFlag(false);
            legendAttr.setLineFlag(false);
            
            double xPos = 0.1425, yPos = SmPreferences.ChartOptions.getPlotAreaPositionTop()/2;
            
            StandardLegend legend = new StandardLegend(xPos, yPos,
                legendAttr, StandardLegend.HORIZ_DIR);
            
            legend.setLegendItemUniformTextColor(foregroundColor);
            legend.addLegendItem(itemText,itemSymbolNumber,itemAttr,font);
            chartView.addChartObject(legend);
        }
        
        // Add chart text to line plot.
        if (SmPreferences.ChartOptions.getDisplayPlotLabels())
        {
            if (dataset != null)
            {
                double x = dataset.getXDataValue(dataset.getXData().length-1);
                double y = dataset.getYDataValue(dataset.getYData().length-1);
                ChartText chartText = new ChartText(pTransform,font,itemText,
                    x,y,ChartConstants.PHYS_POS);
                chartText.setColor(foregroundColor);
                chartView.addChartObject(chartText);
            }
        }

        // Add zoom and data cursor functionality to ChartView object.
        chartZoom = new ChartZoom(chartView, pTransform, true);
        chartZoom.setLineColor(Color.RED);
        chartZoom.setButtonMask(MouseEvent.BUTTON1_MASK);
        chartZoom.setZoomXRoundMode(ChartConstants.AUTOAXES_FAR);
        chartZoom.setZoomYRoundMode(ChartConstants.AUTOAXES_FAR);
        chartZoom.setZoomRangeLimitsRatio(new ChartDimension(0.0001, 0.0001));
        chartZoom.setZoomStackEnable(true);
        
        smDataCursor = new SmDataCursor(this, pTransform,
            ChartConstants.MARKER_VLINE,8.0);

        createPopupMenu();
        
        chartView.setPreferredSize(new Dimension(
            SmPreferences.ChartOptions.getChartSizeSingleWidth(),
            SmPreferences.ChartOptions.getChartSizeSingleHeight()));
    }
    
    public void addLinePlot(SmSeries smSeries, LinePlotType linePlotType) {
        SimpleDataset simpleDataset = null;
        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
        
        if (smPoints != null)
        {
            double x[] = new double[smPoints.size()];
            double y[] = new double[smPoints.size()];

            for (int i=0; i<smPoints.size(); i++)
            {
                SmPoint smPoint = smPoints.get(i);
                x[i] = smPoint.getX();
                y[i] = smPoint.getY();
            }
            
            simpleDataset = new SimpleDataset(plotType, x, y);
        }    
        
        ChartAttribute lineAttr = new ChartAttribute (smSeries.getColor(),1,
            ChartConstants.LS_SOLID);
        
        MySimpleLinePlot plot = new MySimpleLinePlot(pTransform, simpleDataset, 
            lineAttr, linePlotType);
        chartView.addChartObject(plot);
    }
    
    public void addLinePlot(SmSeries smSeries, LinePlotType linePlotType,
        ChartAttribute lineAttribute) {
        SimpleDataset simpleDataset = null;
        ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
        
        if (smPoints != null)
        {
            double x[] = new double[smPoints.size()];
            double y[] = new double[smPoints.size()];

            for (int i=0; i<smPoints.size(); i++)
            {
                SmPoint smPoint = smPoints.get(i);
                x[i] = smPoint.getX();
                y[i] = smPoint.getY();
            }
            
            simpleDataset = new SimpleDataset(plotType, x, y);
        }    
        
        MySimpleLinePlot plot = new MySimpleLinePlot(pTransform, simpleDataset, 
            lineAttribute, linePlotType);
        chartView.addChartObject(plot);
    }
    
    public void removeLinePlot(LinePlotType linePlotType) {
        Vector<GraphObj> chartObjects = chartView.getChartObjectsVector();
        
        Iterator<GraphObj> itr = chartObjects.iterator();
        
        while(itr.hasNext()) {
            GraphObj obj = itr.next();
            if (obj instanceof MySimpleLinePlot)
            {
                MySimpleLinePlot linePlot = (MySimpleLinePlot)obj;
                
                if (linePlot.getLinePlotType().equals(LinePlotType.BASELINE)) {
                    itr.remove();
                    break;
                }
            }
        }
    }

    public SmSeries getSmSeries() {return this.smSeries;}
    
    public SimpleDataset getDataset() {return this.dataset;}
    
    public ChartAttribute getLineAttribute() {return this.lineAttribute;}
    
    public LegendItem getLegendItem() {return this.legendItem;}
    
    public void updateEditModem(boolean editMode) {
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
    
    private void createPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.setLabel("Chart Options");
        popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));

        PopupMenuListener menuListener = new PopupMenuListener();

        JMenuItem item;
        popupMenu.add(item = new JMenuItem("Save As Image"));
        item.addActionListener(menuListener);
        popupMenu.add(item = new JMenuItem("Print"));
        item.addActionListener(menuListener);
        
        // Create radio buttons for zoom and data cursor modes.
        JRadioButton rbtnZoom = new JRadioButton("Zoom");
        rbtnZoom.setName("Zoom");
        JRadioButton rbtnDataCursor = new JRadioButton("Data Cursor");
        rbtnDataCursor.setName("DataCursor");
        
        // Add listener for radio buttons.
        RBtnListener rbtnListener = new RBtnListener();
        rbtnZoom.addItemListener(rbtnListener);
        rbtnDataCursor.addItemListener(rbtnListener);
        
        ButtonGroup bgrpChartMode = new ButtonGroup();
        bgrpChartMode.add(rbtnZoom);
        bgrpChartMode.add(rbtnDataCursor);
        
        popupMenu.addSeparator();
        popupMenu.add(rbtnZoom);
        popupMenu.add(rbtnDataCursor);
        
        popupMenu.addSeparator();
        popupMenu.add(item = new JMenuItem("Zoom Previous"));
        item.addActionListener(menuListener);
        popupMenu.add(item = new JMenuItem("Zoom Reset"));
        item.addActionListener(menuListener);
//        popupMenu.add(item = new JMenuItem("Remove Markers"));
//        item.addActionListener(menuListener);
        
        if (editMode) {
            rbtnDataCursor.setSelected(true);
        }
        else {
            rbtnZoom.setSelected(true);
            
            popupMenu.addSeparator();
            popupMenu.add(item = new JMenuItem("Edit"));
            item.addActionListener(menuListener);
        }
        
        this.addMouseListener(new SmChartViewMouseListener());
    }
    
    private void enableZoomFunctions(boolean enable) {
        for (Component component : popupMenu.getComponents())
        {
            if (component instanceof JMenuItem)
            {
                JMenuItem item = (JMenuItem)component;
                String actionCommand = item.getActionCommand();
                
                if ("Zoom Previous".equals(actionCommand) || 
                    "Zoom Reset".equals(actionCommand))
                    item.setEnabled(enable);
            }
        }
    }
    
    public class MySimpleLinePlot extends SimpleLinePlot {
        
        private LinePlotType linePlotType;
        
        public MySimpleLinePlot(PhysicalCoordinates pTransform, SimpleDataset dataset, 
            ChartAttribute lineAttribute, LinePlotType linePlotType) {
            super(pTransform,dataset,lineAttribute);
            this.linePlotType = linePlotType;
        }
        
        public LinePlotType getLinePlotType() {return this.linePlotType;}
        public void setLinePlotType(LinePlotType linePlotType) {this.linePlotType = linePlotType;}
    }
    
    private class RBtnListener implements ItemListener 
    {  
        @Override
        public void itemStateChanged(ItemEvent e) {
            
            JRadioButton rbtn = (JRadioButton)e.getItem();
            if (rbtn.isSelected())
            {
                if (rbtn.getName().equals("Zoom"))
                {
                    setChartZoom(true);
                    setSmDataCursor(false);
                    setChartingMode(ChartingMode.ZOOM);
                    
                    enableZoomFunctions(true);
                }
                else if (rbtn.getName().equals("DataCursor"))
                {
                    setChartZoom(false);
                    setSmDataCursor(true);
                    setChartingMode(ChartingMode.DATACURSOR);
                    
                    enableZoomFunctions(false);
                }
            }
        }
    }

    private class PopupMenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent event) {
            if ("Save As Image".equals(event.getActionCommand()))
            {
                saveJPEG();
            }
            else if ("Print".equals(event.getActionCommand()))
            {
                ChartPrint chartPrint = new ChartPrint(chartView,PRT_MAX);
                print(chartPrint);
            }
            else if ("Zoom Previous".equals(event.getActionCommand()))
            {
                chartZoom.popZoomStack(); 
            }
            else if ("Zoom Reset".equals(event.getActionCommand()))
            {
                while (chartZoom.popZoomStack() != 0)
                    chartZoom.popZoomStack();
            }
//            else if ("Remove Markers".equals(event.getActionCommand()))
//            {
//                smDataCursor.removeMarkers();
//            }
            else if ("Edit".equals(event.getActionCommand()))
            {
                try {
                    // Get selected template.
                    SmTemplate smTemplate = SmCore.getSelectedSmTemplate();
                    
                    // Open editor.
                    String filePath = (String)smSeries.getTag();
                    File file = new File(filePath);
                    SmFile smFile = new SmFile(file);
                    
                    if (smFile.getFileDataType().equals(UNCORACC) ||
                        smFile.getFileDataType().equals(CORACC)) {
                        // Check if data unit code is cm/sec squared.
                        SmRec smRec = smFile.getSmRecs().get(0);
                        if (smRec.getDataUnitCode() != 4) {
                            JOptionPane.showMessageDialog(null,
                            "Editing only allowed on acceleration data in cm/sec" + "\u00B2" + ".",
                            "Invalid Data Unit",
                            JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    else if (smFile.getFileDataType().equals(VELOCITY)) {
                        // Check if data unit code is cm/sec.
                        SmRec smRec = smFile.getSmRecs().get(0);
                        if (smRec.getDataUnitCode() != 5) {
                            JOptionPane.showMessageDialog(null,
                            "Editing only allowed on velocity data in cm/sec.",
                            "Invalid Data Unit",
                            JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    else if (smFile.getFileDataType().equals(DISPLACE)) {
                        // Check if data unit code is cm.
                        SmRec smRec = smFile.getSmRecs().get(0);
                        if (smRec.getDataUnitCode() != 6) {
                            JOptionPane.showMessageDialog(null,
                            "Editing only allowed on displacement data in cm.",
                            "Invalid Data Unit",
                            JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    
                    SmSeismicTraceEditor chartEditor = 
                        new SmSeismicTraceEditor(SmGlobal.SM_CHARTS_API_QCCHART2D,smFile,smTemplate);
                    chartEditor.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    chartEditor.pack();
                    chartEditor.setVisible(true);
                }
                catch (Exception ex) {
                    SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
                }
            }
        }
    }

    private class SmChartViewMouseListener extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
//            Container container = chartView.getParent();
//            if (container instanceof JPanel) {
//                JPanel pnl = (JPanel)container;
//                MouseListener[] listeners = pnl.getMouseListeners();
//                for (MouseListener listener : listeners) {
//                    listener.mousePressed(e);
//                }
//            }
            checkPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            /*
            Container container = chartView.getParent();
            
            if (container instanceof JPanel) {
                JPanel pnl = (JPanel)container;
                Object owner = smChartView.getOwner();
            
                if (owner instanceof SmSeismicTraceEditor) {
                    SmSeismicTraceEditor editor = (SmSeismicTraceEditor)owner;
                    JPanel pnlCur = editor.getCurrentChartViewer();

                    if (pnlCur.getName().equals(pnl.getName())) {
                        //MouseListener[] listeners = pnl.getMouseListeners();
                        //for (MouseListener listener : listeners) {
                            //listener.mouseClicked(e);
                        //}  
                    }
                    else {
                        //editor.setCurrentChartViewer(pnl);
                    }
                }
            }
            */
            
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e)
        {
            if (e.isPopupTrigger()) {
                popupMenu.show(chartView, e.getX(), e.getY());
            }
        }
    }  
}
