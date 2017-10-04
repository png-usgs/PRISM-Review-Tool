/*******************************************************************************
 * Name: Java class QCChart2D_API.java
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
import com.quinncurtis.chart2djava.ChartAttribute;
import com.quinncurtis.chart2djava.ChartConstants;
import com.quinncurtis.chart2djava.GroupDataset;
import com.quinncurtis.chart2djava.LegendItem;
import com.quinncurtis.chart2djava.SimpleDataset;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smchartingapi.SmCharts_API;
import gov.usgs.smapp.smchartingapi.SmCharts_API.XYBounds;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmPoint;
import gov.usgs.smcommon.smclasses.SmPreferences;
import gov.usgs.smcommon.smclasses.SmSeries;
import static gov.usgs.smcommon.smutilities.NetBeansUtils.showMessage;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.openide.NotifyDescriptor;

/**
 * This class extends the SmCharts_API class and implements the charting
 * functions utilizing the Quinn-Curtis Chart2d charting library.
 * @author png
 */
public class QCChart2D_API extends SmCharts_API {
    
    String fontName = SmPreferences.ChartOptions.getFontName();
    boolean fontIsBold = SmPreferences.ChartOptions.getFontIsBold();
    boolean fontIsItal = SmPreferences.ChartOptions.getFontIsItalic();
    int fontSize = SmPreferences.ChartOptions.getFontSize();

    Font font = new Font(fontName, (fontIsBold ? Font.BOLD : 0) +
        (fontIsItal ? Font.ITALIC : 0), fontSize);
        
    public QCChart2D_API()
    {
        super();
    }
    
    @Override
    public JPanel createChart(final SmSeries smSeries, 
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner)
    {
        SimpleDataset dataset = null;
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
            
            dataset = new SimpleDataset(plotType, x, y);
        }    
        
        ChartAttribute lineAttribute = new ChartAttribute (smSeries.getColor(),1,
            ChartConstants.LS_SOLID);
        
        String text = smSeries.getTitle();
        int symbol = ChartConstants.SQUARE;
        ChartAttribute attr = new ChartAttribute(smSeries.getColor(),1,
            ChartConstants.LS_SOLID, smSeries.getColor());
        //Font font = new Font("SansSerif", Font.BOLD, 12);
        
        LegendItem legendItem = new LegendItem(null,text,symbol,attr,font);
        
        final SingleChartView chartView = new SingleChartView(smSeries,
            dataset,null,ChartConstants.LINEAR_SCALE,
            ChartConstants.LINEAR_SCALE,lineAttribute,legendItem,chartTitle,
            xAxisTitle,yAxisTitle,plotType,editMode,owner);
        
        return chartView;
    }
    
    @Override
    public JPanel createChart(final SmSeries smSeries, 
        final String plotType, final XYBounds xyBounds, final String chartTitle, 
        final String xAxisTitle, final String yAxisTitle, final boolean editMode, final Object owner)
    {
        SimpleDataset dataset = null;
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
            
            dataset = new SimpleDataset(plotType, x, y);
        }    
        
        ChartAttribute lineAttribute = new ChartAttribute (smSeries.getColor(),1,
            ChartConstants.LS_SOLID);
        
        String text = smSeries.getTitle();
        int symbol = ChartConstants.SQUARE;
        ChartAttribute attr = new ChartAttribute(smSeries.getColor(),1,
            ChartConstants.LS_SOLID, smSeries.getColor());
        //Font font = new Font("SansSerif", Font.BOLD, 12);
        
        LegendItem legendItem = new LegendItem(null,text,symbol,attr,font);
        
        final SingleChartView chartView = new SingleChartView(smSeries,
            dataset,xyBounds,ChartConstants.LINEAR_SCALE,
            ChartConstants.LINEAR_SCALE,lineAttribute,legendItem,chartTitle,
            xAxisTitle,yAxisTitle,plotType,editMode,owner);
        
        return chartView;
    }
    
    @Override
    public JPanel createChartLog10_XYScale(final SmSeries smSeries, 
        final String plotType, final String chartTitle, final String xAxisTitle, final String yAxisTitle,
        final boolean editMode, final Object owner)
    {
        SimpleDataset dataset = null;
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
            
            dataset = new SimpleDataset(plotType, x, y);
        }    
        
        ChartAttribute lineAttribute = new ChartAttribute (smSeries.getColor(),1,
            ChartConstants.LS_SOLID);
        
        String text = smSeries.getTitle();
        int symbol = ChartConstants.SQUARE;
        ChartAttribute attr = new ChartAttribute(smSeries.getColor(),1,
            ChartConstants.LS_SOLID, smSeries.getColor());
        //Font font = new Font("SansSerif", Font.BOLD, 12);

        LegendItem legendItem = new LegendItem(null,text,symbol,attr,font);
        
        final SingleChartView chartView = new SingleChartView(smSeries,
            dataset,null,ChartConstants.LOG_SCALE,
            ChartConstants.LOG_SCALE,lineAttribute,legendItem,chartTitle,
            xAxisTitle,yAxisTitle,plotType,editMode,owner);
        
        return chartView;
    }
    
    @Override
    public JPanel createChartLog10_XYScale(final SmSeries smSeries, 
        final String plotType, final XYBounds xyBounds, final String chartTitle, 
        final String xAxisTitle, final String yAxisTitle, final boolean editMode, final Object owner)
    {
        SimpleDataset dataset = null;
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
            
            dataset = new SimpleDataset(plotType, x, y);
        }    
        
        ChartAttribute lineAttribute = new ChartAttribute (smSeries.getColor(),1,
            ChartConstants.LS_SOLID);
        
        String text = smSeries.getTitle();
        int symbol = ChartConstants.SQUARE;
        ChartAttribute attr = new ChartAttribute(smSeries.getColor(),1,
            ChartConstants.LS_SOLID, smSeries.getColor());
        //Font font = new Font("SansSerif", Font.BOLD, 12);

        LegendItem legendItem = new LegendItem(null,text,symbol,attr,font);
        
        final SingleChartView chartView = new SingleChartView(smSeries,
            dataset, xyBounds,
            ChartConstants.LOG_SCALE,ChartConstants.LOG_SCALE,
            lineAttribute,legendItem,chartTitle,xAxisTitle,yAxisTitle,plotType,
            editMode,owner);
        
        return chartView;
    }
    
    @Override
    public JPanel createGroupChart(final ArrayList<SmSeries> smSeriesList, 
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner)
    {
        if (smSeriesList == null)
            return null;
        
        try {
            int colSize = getMaxColSize(smSeriesList);
            int rowSize = smSeriesList.size();

            double[] x = new double[colSize];
            double[][] y = new double[rowSize][colSize];

            for (int row=0; row < rowSize; row++)
            {
                ArrayList<SmPoint> smPoints = smSeriesList.get(row).getSmPoints();
                
                if (smPoints == null)
                    continue;

                double lastY = smPoints.get(smPoints.size()-1).getY();

                double deltaX = 0;
                for (int col=0; col < colSize; col++)
                {
                    if (row == 0)
                    {
                        if (col == 1)
                            deltaX = smPoints.get(col).getX()-smPoints.get(0).getX();

                        x[col] = col < smPoints.size() ? smPoints.get(col).getX() : 
                            smPoints.get(smPoints.size()-1).getX() + (col-smPoints.size()+1)*deltaX;
                    }

                    y[row][col] = col < smPoints.size() ? smPoints.get(col).getY() : lastY;
                }
            }

            GroupDataset dataset = new GroupDataset(plotType,x,y);

            ChartAttribute[] lineAttributes = new ChartAttribute[smSeriesList.size()];
            LegendItem[] legendItems = new LegendItem[smSeriesList.size()];
            //Font font = new Font("SansSerif", Font.BOLD, 12);

            for (int i=0; i < smSeriesList.size(); i++)
            {
                lineAttributes[i] = new ChartAttribute (smSeriesList.get(i).getColor(),1,
                    ChartConstants.LS_SOLID);

                String text = smSeriesList.get(i).getTitle();
                int symbol = ChartConstants.SQUARE;
                ChartAttribute attr = new ChartAttribute(smSeriesList.get(i).getColor(),1,
                    ChartConstants.LS_SOLID, smSeriesList.get(i).getColor());

                legendItems[i] = new LegendItem(null,text,symbol,attr,font);
            }

            final GroupChartView chartView = new GroupChartView(smSeriesList,
                dataset,ChartConstants.LINEAR_SCALE,ChartConstants.LINEAR_SCALE,
                lineAttributes,legendItems,chartTitle,xAxisTitle,yAxisTitle,plotType,
                editMode,owner);

            return chartView;
        }
        catch (Exception ex) {
            showMessage("Charting Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Charting Error: " + ex.getMessage());
        }
        
        return null;
    }
    
    @Override
    public JPanel createGroupChartLog10_XYScale(final ArrayList<SmSeries> smSeriesList, 
        final String plotType, final String chartTitle, final String xAxisTitle, 
        final String yAxisTitle, final boolean editMode, final Object owner)
    {
        if (smSeriesList == null)
            return null;
        
        try {
            int colSize = getMaxColSize(smSeriesList);
            int rowSize = smSeriesList.size();

            double[] x = new double[colSize];
            double[][] y = new double[rowSize][colSize];

            for (int row=0; row < rowSize; row++)
            {
                ArrayList<SmPoint> smPoints = smSeriesList.get(row).getSmPoints();
                
                if (smPoints == null)
                    continue;
                
                double lastY = smPoints.get(smPoints.size()-1).getY();

                double deltaX = 0;
                for (int col=0; col < colSize; col++)
                {
                    if (row == 0)
                    {
                        if (col == 1)
                            deltaX = smPoints.get(col).getX()-smPoints.get(0).getX(); 

                        x[col] = col < smPoints.size() ? smPoints.get(col).getX() : 
                                smPoints.get(smPoints.size()-1).getX() + (col-smPoints.size()+1)*deltaX;
                    }

                    y[row][col] = col < smPoints.size() ? smPoints.get(col).getY() : lastY;
                }
            }

            GroupDataset dataset = new GroupDataset(plotType,x,y);

            ChartAttribute[] lineAttributes = new ChartAttribute[smSeriesList.size()];
            LegendItem[] legendItems = new LegendItem[smSeriesList.size()];
            //Font font = new Font("SansSerif", Font.BOLD, 12);

            for (int i=0; i < smSeriesList.size(); i++)
            {
                lineAttributes[i] = new ChartAttribute (smSeriesList.get(i).getColor(),1,
                    ChartConstants.LS_SOLID);

                String text = smSeriesList.get(i).getTitle();
                int symbol = ChartConstants.SQUARE;
                ChartAttribute attr = new ChartAttribute(smSeriesList.get(i).getColor(),1,
                    ChartConstants.LS_SOLID, smSeriesList.get(i).getColor());

                legendItems[i] = new LegendItem(null,text,symbol,attr,font);
            }

            final GroupChartView chartView = new GroupChartView(smSeriesList,
                dataset,ChartConstants.LOG_SCALE,ChartConstants.LOG_SCALE,
                lineAttributes,legendItems,chartTitle,xAxisTitle,yAxisTitle,plotType,
                editMode,owner);
            
            return chartView;
        }
        catch (Exception ex) {
            showMessage("Charting Error",ex.getMessage(),
                NotifyDescriptor.DEFAULT_OPTION,NotifyDescriptor.ERROR_MESSAGE);
            SmCore.addMsgToStatusViewer("Charting Error: " + ex.getMessage());
        }
        
        return null;
    }
    
    @Override
    public XYBounds calcXYBounds(final ArrayList<SmSeries> smSeriesList,
        final String plotType)
    {
        if (smSeriesList == null)
            return null;
        
        SimpleDataset[] simpleDatasetArr = new SimpleDataset[smSeriesList.size()];
        
        for (int arrIndx=0; arrIndx<smSeriesList.size(); arrIndx++)
        {
            SmSeries smSeries = smSeriesList.get(arrIndx);
            
            ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
            
            if (smPoints == null)
                continue;
            
            double x[] = new double[smPoints.size()];
            double y[] = new double[smPoints.size()];

            for (int i=0; i<smPoints.size(); i++)
            {
                SmPoint smPoint = smPoints.get(i);
                x[i] = smPoint.getX();
                y[i] = smPoint.getY();
            }

            simpleDatasetArr[arrIndx] = new SimpleDataset("Series"+
                String.valueOf(arrIndx+1), x, y);
        }
        
        int xScaleType = plotType.equals(SmGlobal.PLOT_TYPE_SEISMIC) ? 
            ChartConstants.LINEAR_SCALE : ChartConstants.LOG_SCALE;
        int yScaleType = plotType.equals(SmGlobal.PLOT_TYPE_SEISMIC) ? 
            ChartConstants.LINEAR_SCALE : ChartConstants.LOG_SCALE;
        
        CartesianCoordinates xyScale = new CartesianCoordinates(xScaleType,yScaleType);
        xyScale.autoScale(simpleDatasetArr, ChartConstants.AUTOAXES_FAR,
            ChartConstants.AUTOAXES_FAR);
        
//        System.out.println("Min X: " + xyScale.getScaleStartX());
//        System.out.println("Max Y: " + xyScale.getScaleStopX());
//        System.out.println("Min Y: " + xyScale.getScaleStartY());
//        System.out.println("Max Y: " + xyScale.getScaleStopY());
        
        XYBounds xyBounds = new XYBounds(xyScale.getScaleStartX(),
            xyScale.getScaleStartY(),xyScale.getScaleStopX(),xyScale.getScaleStopY());
        
        return xyBounds;
    }
    
    @Override
    public XYBounds calcXYBounds(final ArrayList<SmSeries> smSeriesList,
        final String plotType, final int dataParmCode)
    {
        if (smSeriesList == null)
            return null;
        
        ArrayList<SimpleDataset> simpleDatasetList = new ArrayList<>();
        
        int seriesIndx = 0;
        
        for (SmSeries smSeries : smSeriesList)
        {
            if (smSeries.getDataParmCode() == dataParmCode)
            {
                ArrayList<SmPoint> smPoints = smSeries.getSmPoints();
                
                if (smPoints == null)
                    continue;
                
                double x[] = new double[smPoints.size()];
                double y[] = new double[smPoints.size()];

                for (int i=0; i<smPoints.size(); i++)
                {
                    SmPoint smPoint = smPoints.get(i);
                    x[i] = smPoint.getX();
                    y[i] = smPoint.getY();
                }
                
                simpleDatasetList.add(new SimpleDataset("Series"+
                    String.valueOf(++seriesIndx), x, y));
            }
        }
        
        if (simpleDatasetList.isEmpty())
            return null;
        
        SimpleDataset[] simpleDatasetArr = simpleDatasetList.toArray(new SimpleDataset[0]);
 
        int xScaleType = plotType.equals(SmGlobal.PLOT_TYPE_SEISMIC) ? 
            ChartConstants.LINEAR_SCALE : ChartConstants.LOG_SCALE;
        int yScaleType = plotType.equals(SmGlobal.PLOT_TYPE_SEISMIC) ? 
            ChartConstants.LINEAR_SCALE : ChartConstants.LOG_SCALE;
        
        CartesianCoordinates xyScale = new CartesianCoordinates(xScaleType,yScaleType);
        xyScale.autoScale(simpleDatasetArr, ChartConstants.AUTOAXES_FAR,
            ChartConstants.AUTOAXES_FAR);
        
//        System.out.println("Min X: " + xyScale.getScaleStartX());
//        System.out.println("Max Y: " + xyScale.getScaleStopX());
//        System.out.println("Min Y: " + xyScale.getScaleStartY());
//        System.out.println("Max Y: " + xyScale.getScaleStopY());
        
        XYBounds xyBounds = new XYBounds(xyScale.getScaleStartX(),
            xyScale.getScaleStartY(),xyScale.getScaleStopX(),xyScale.getScaleStopY());
        
        return xyBounds;
    }
    
    private int getMaxColSize(final ArrayList<SmSeries> smSeries)
    {
        int colSize = 0;
        
        for (int i = 0; i < smSeries.size(); i++)
        {
            ArrayList<SmPoint> smPoints = smSeries.get(i).getSmPoints();
            
            if (smPoints == null)
                continue;
            
            if (colSize < smPoints.size())
                colSize = smPoints.size();
        }
        
        return colSize;
    }
}
