/*******************************************************************************
 * Name: Java class SmPreferences.java
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

import com.quinncurtis.chart2djava.ChartConstants;
import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * This class defines constant variables representing the preference values used in
 * the PRISM Review Tool. A preference variable is defined in a specific preference
 * category sub-class corresponding to the application component in which the preference
 * is used. These preference category sub-classes include General, PRISM parameters, 
 * Seismic Trace Editor, FAS Editor, Filter Editor, Chart Options, Event Dialog, and 
 * Trim Tool.
 * @author png
 */
public class SmPreferences {
    
    /**
     * This enumeration defines the line marker styles that can be applied to a
     * SmMarker object
     */
    public static enum MarkerStyle {SOLID,DASH,DOT};
    
    /**
     * This class defines the general preferences used throughout the PRISM
     * Review Tool
     */
    public static class General {
    
        private static final Preferences prefGeneral = 
            Preferences.userNodeForPackage(SmPreferences.class).node(General.class.getName());
        private static final String EVENTS_ROOT_DIR = "EventsRootDir";
        private static final String TEMPLATES_ROOT_DIR = "TemplatesRootDir";
        private static final String STATION_TEMPLATES_FOLDER_NAME = "StationTemplatesFolderName";
        private static final String TEMPLATE = "Template";
        private static final String LOGS_DIR = "LogsDir";
        private static final String TRASH_DIR = "TrashDir";
        private static final String LAST_VX_DIR = "LastVxDir";
        
        /**
         * Gets the events root directory pathname
         * @return events root directory pathname
         */
        public static String getEventsRootDir()
        {
            return prefGeneral.get(EVENTS_ROOT_DIR, "");
        }
        
        /**
         * Gets the templates root directory pathname
         * @return templates root directory pathname
         */
        public static String getTemplatesRootDir()
        {
            return prefGeneral.get(TEMPLATES_ROOT_DIR, "");
        }
        
        /**
         * Gets the templates directory pathname. Each template is for a
         * station.
         * @return templates directory pathname
         */
        public static String getStationTemplatesFolderName()
        {
            return prefGeneral.get(STATION_TEMPLATES_FOLDER_NAME, "");
        }
        
        /**
         * Gets the currently selected template filename
         * @return currently selected template filename
         */
        public static String getTemplate()
        {
            return prefGeneral.get(TEMPLATE, "");
        }
        
        /**
         * Gets the logs directory pathname
         * @return logs directory pathname
         */
        public static String getLogsDir()
        {
            return prefGeneral.get(LOGS_DIR, "");
        }
        
        /**
         * Gets the trash directory pathname
         * @return trash directory pathname
         */
        public static String getTrashDir()
        {
            return prefGeneral.get(TRASH_DIR, "");
        }
        
        /** 
         * Gets the last visited COSMOS file type (i.e. V0, V1, V2, V3) directory 
         * selected
         * @return last visited COSMOS file type directory selected
         */
        public static String getLastVxDir()
        {
            return prefGeneral.get(LAST_VX_DIR, "");
        }
        
        /**
         * Sets the events root directory pathname
         * @param eventsRootDir events root directory pathname
         */
        public static void setEventsRootDir(String eventsRootDir)
        {
            prefGeneral.put(EVENTS_ROOT_DIR, eventsRootDir);
        }
        
        /**
         * Sets the templates root directory pathname
         * @param templatesRootDir templates root directory pathname
         */
        public static void setTemplatesRootDir(String templatesRootDir)
        {
            prefGeneral.put(TEMPLATES_ROOT_DIR, templatesRootDir);
        }
        
        /**
         * Sets the templates directory pathname
         * @param stationTemplatesFolderName templates directory pathname
         */
        public static void setStationTemplatesFolderName(String stationTemplatesFolderName)
        {
            prefGeneral.put(STATION_TEMPLATES_FOLDER_NAME, stationTemplatesFolderName);
        }
        
        /**
         * Sets the currently selected template filename
         * @param template currently selected template filename
         */
        public static void setTemplate(String template)
        {
            prefGeneral.put(TEMPLATE, template);
        }
        
        /**
         * Sets the logs directory pathname
         * @param logsDir logs directory pathname
         */
        public static void setLogsDir(String logsDir)
        {
            prefGeneral.put(LOGS_DIR, logsDir);
        }
        
        /**
         * Sets the trash directory pathname
         * @param trashDir trash directory pathname
         */
        public static void setTrashDir(String trashDir)
        {
            prefGeneral.put(TRASH_DIR, trashDir);
        }
        
        /**
         * Sets the last visited COSMOS file type directory pathname
         * @param lastVxDir last visited COSMOS file type directory pathname
         */
        public static void setLastVxDir(String lastVxDir)
        {
            prefGeneral.put(LAST_VX_DIR, lastVxDir);
        }
    }
    
    /**
     * This class defines preferences for parameters used in PRISM processing
     */
    public static class PrismParams {
        private static final Preferences PREF_PRISM_PARAMS = 
            Preferences.userNodeForPackage(SmPreferences.class).node(PrismParams.class.getName());
        
        private static final String BUTTERWORTH_FILTER_ORDER = "ButterworthFilterOrder";
        private static final String BUTTERWORTH_FILTER_TAPER_LENGTH = "ButterworthFilterTaperLength";
        private static final String STRONG_MOTION_THRESHOLD_PCNT = "StrongMotionThresholdPcnt";
        private static final String DIFFERENTIAL_ORDER = "DifferentialOrder";
        
        /**
         * Gets the Butterworth filter order value
         * @return Butterworth filter order value
         */
        public static int getButterworthFilterOrder()
        {
            return PREF_PRISM_PARAMS.getInt(BUTTERWORTH_FILTER_ORDER, 4);
        }
        
        /**
         * Gets the Butterworth filter taper length
         * @return Butterworth filter taper length
         */
        public static double getButterworthFilterTaperLength()
        {
            return PREF_PRISM_PARAMS.getDouble(BUTTERWORTH_FILTER_TAPER_LENGTH, 2.0);
        }
        
        /**
         * Gets the strong motion threshold percent
         * @return strong motion threshold percent
         */
        public static double getStrongMotionThresholdPcnt()
        {
            return PREF_PRISM_PARAMS.getDouble(STRONG_MOTION_THRESHOLD_PCNT, 5.0);
        }
        
        /**
         * Gets the differential order value
         * @return differential order value
         */
        public static int getDifferentialOrder()
        {
            return PREF_PRISM_PARAMS.getInt(DIFFERENTIAL_ORDER, 5);
        }
        
        /**
         * Sets the Butterworth filter order value
         * @param order Butterworth filter order value
         */
        public static void setButterworthFilterOrder(int order)
        {
            PREF_PRISM_PARAMS.putInt(BUTTERWORTH_FILTER_ORDER, order);
        }
        
        /**
         * Sets the Butterworth filter taper length
         * @param length Butterworth filter taper length
         */
        public static void setButterworthFilterTaperLength(double length)
        {
            PREF_PRISM_PARAMS.putDouble(BUTTERWORTH_FILTER_TAPER_LENGTH, length);
        }
        
        /**
         * Sets the strong motion threshold percent
         * @param pcnt strong motion threshold percent
         */
        public static void setStrongMotionThresholdPcnt(double pcnt)
        {
            PREF_PRISM_PARAMS.putDouble(STRONG_MOTION_THRESHOLD_PCNT, pcnt);
        }
        
        /**
         * Sets the differential order value
         * @param differentialOrder differential order value
         */
        public static void setDifferentialOrder(int differentialOrder)
        {
            PREF_PRISM_PARAMS.putInt(DIFFERENTIAL_ORDER, differentialOrder);
        }
    }
    
    /**
     * This class defines the preferences used in the Seismic Trace Editor
     */
    public static class SmSeismicTraceEditor {
        private static final Preferences PREF_SM_SEISMIC_CHART_EDITOR = 
            Preferences.userNodeForPackage(SmPreferences.class).node(SmSeismicTraceEditor.class.getName());
        private static final String FUNCTION_RANGE_START = "FunctionRangeStart";
        private static final String FUNCTION_RANGE_STOP = "FunctionRangeStop";
        private static final String APPLICATION_RANGE_START = "ApplicationRangeStart";
        private static final String APPLICATION_RANGE_STOP = "ApplicationRangeStop";
        private static final String EVENT_ONSET = "EventOnset";
        private static final String FILTER_RANGE_LOW = "FilterRangeLow";
        private static final String FILTER_RANGE_HIGH = "FilterRangeHigh";
        private static final String DIFFERENTIAL_ORDER = "DifferentialOrder";
        
        private static final String FUNCTION_RANGE_START_MARKER_COLOR = "FunctionRangeStartMarkerColor";
        private static final String FUNCTION_RANGE_STOP_MARKER_COLOR = "FunctionRangeStopMarkerColor";
        private static final String APPLICATION_RANGE_START_MARKER_COLOR = "ApplicationRangeStartMarkerColor";
        private static final String APPLICATION_RANGE_STOP_MARKER_COLOR = "ApplicationRangeStopMarkerColor";
        private static final String EVENT_ONSET_MARKER_COLOR = "EventOnsetMarkerColor";
        
        private static final String FUNCTION_RANGE_START_MARKER_STYLE = "FunctionRangeStartMarkerStyle";
        private static final String FUNCTION_RANGE_STOP_MARKER_STYLE = "FunctionRangeStopMarkerStyle";
        private static final String APPLICATION_RANGE_START_MARKER_STYLE = "ApplicationRangeStartMarkerStyle";
        private static final String APPLICATION_RANGE_STOP_MARKER_STYLE = "ApplicationRangeStopMarkerStyle";
        private static final String EVENT_ONSET_MARKER_STYLE = "EventOnsetMarkerStyle";
        
        private static final String FUNCTION_RANGE_START_MARKER_WIDTH = "FunctionRangeStartMarkerWidth";
        private static final String FUNCTION_RANGE_STOP_MARKER_WIDTH = "FunctionRangeStopMarkerWidth";
        private static final String APPLICATION_RANGE_START_MARKER_WIDTH = "ApplicationRangeStartMarkerWidth";
        private static final String APPLICATION_RANGE_STOP_MARKER_WIDTH = "ApplicationRangeStopMarkerWidth";
        private static final String EVENT_ONSET_MARKER_WIDTH = "EventOnsetMarkerWidth";
        
        private static final String BASELINE_FUNCTION_COLOR = "BaselineFunctionColor";
        private static final String BASELINE_FUNCTION_STYLE = "BaselineFunctionStyle";
        private static final String BASELINE_FUNCTION_WIDTH = "BaselineFunctionWidth";
        
        /**
         * Gets the function range start time (in seconds)
         * @return function range start time (in seconds)
         */
        public static double getFunctionRangeStart()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FUNCTION_RANGE_START, 0.0);
        }
        
        /**
         * Gets the function range stop time (in seconds)
         * @return function range stop time (in seconds)
         */
        public static double getFunctionRangeStop()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FUNCTION_RANGE_STOP, 0.0);
        }
                
        /**
         * Gets the application range start time (in seconds)
         * @return application range start time (in seconds)
         */       
        public static double getApplicationRangeStart()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(APPLICATION_RANGE_START, 0.0);
        }
        
        /**
         * Gets the application range stop time (in seconds)
         * @return application range stop time (in seconds)
         */
        public static double getApplicationRangeStop()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(APPLICATION_RANGE_STOP, 0.0);
        }
        
        /**
         * Gets the event onset time (in seconds)
         * @return event onset time (in seconds)
         */
        public static double getEventOnset()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(EVENT_ONSET, 0.0);
        }
        
        /**
         * Gets the filter range low frequency value (in hertz)
         * @return filter range low frequency value (in hertz)
         */
        public static double getFilterRangeLow()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FILTER_RANGE_LOW, 0.0);
        }
        
        /**
         * Gets the filter range high frequency value (in hertz)
         * @return filter range high frequency value (in hertz)
         */
        public static double getFilterRangeHigh()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FILTER_RANGE_HIGH, 50.0);
        }
        
        /**
         * Gets the differential order value
         * @return differential order value
         */
        public static int getDifferentialOrder()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(DIFFERENTIAL_ORDER, 5);
        }
        
        /**
         * Gets the color used for drawing the function range start time line marker
         * @return color used for drawing the function range start time line marker
         */
        public static int getFunctionRangeStartMarkerColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(FUNCTION_RANGE_START_MARKER_COLOR, Color.GREEN.getRGB());
        }
        
        /**
         * Gets the color used for drawing the function range stop time line marker
         * @return color used for drawing the function range stop time line marker
         */
        public static int getFunctionRangeStopMarkerColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(FUNCTION_RANGE_STOP_MARKER_COLOR, Color.RED.getRGB());
        }
        
        /**
         * Gets the color used for plotting the baseline function
         * @return color used for plotting the baseline function
         */
        public static int getBaselineFunctionColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(BASELINE_FUNCTION_COLOR, Color.PINK.getRGB());
        }
        
        /**
         * Gets the color used for drawing the application range start time line marker
         * @return color used for drawing the application range start time line marker
         */
        public static int getApplicationRangeStartMarkerColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(APPLICATION_RANGE_START_MARKER_COLOR, Color.ORANGE.getRGB());
        }
        
        /**
         * Gets the color used for drawing the application range stop time line marker
         * @return color used for drawing the application range stop time line marker
         */
        public static int getApplicationRangeStopMarkerColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(APPLICATION_RANGE_STOP_MARKER_COLOR, Color.CYAN.getRGB());
        }
        
        /**
         * Gets the color used for drawing the event onset time line marker
         * @return color used for drawing the event onset time line marker
         */
        public static int getEventOnsetMarkerColor()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getInt(EVENT_ONSET_MARKER_COLOR, Color.MAGENTA.getRGB());
        }
        
        /**
         * Gets the MarkerStyle object used for styling the function range start time line marker
         * @return MarkerStyle object used for styling the function range start time line marker
         */
        public static MarkerStyle getFunctionRangeStartMarkerStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(FUNCTION_RANGE_START_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        /**
         * Gets the MarkerStyle object used for styling the function range stop time line marker
         * @return MarkerStyle object used for styling the function range stop time line marker
         */
        public static MarkerStyle getFunctionRangeStopMarkerStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(FUNCTION_RANGE_STOP_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        /**
         * Gets the integer value representing the style to use for plotting the 
         * baseline function
         * @return integer value representing the style to use for plotting the
         * baseline function
         */
        public static int getBaselineFunctionStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(BASELINE_FUNCTION_STYLE,
                ChartConstants.LS_SOLID);
            
            return style;
        }
        
        /**
         * Gets the MarkerStyle object used for styling the application range start
         * time line marker
         * @return MarkerStyle object used for styling the application range start
         * time line marker
         */
        public static MarkerStyle getApplicationRangeStartMarkerStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(APPLICATION_RANGE_START_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        /**
         * Gets the MarkerStyle object used for styling the application range stop
         * time line marker
         * @return MarkerStyle object used for styling the application range stop
         * time line marker
         */
        public static MarkerStyle getApplicationRangeStopMarkerStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(APPLICATION_RANGE_STOP_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        /**
         * Gets the MarkerStyle object used for styling the event onset time line marker
         * @return MarkerStyle object used for styling the event onset time line marker
         */
        public static MarkerStyle getEventOnsetMarkerStyle()
        {
            int style = PREF_SM_SEISMIC_CHART_EDITOR.getInt(EVENT_ONSET_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        /**
         * Gets the width of the function range start time line marker
         * @return width of the function range start time line marker
         */
        public static double getFunctionRangeStartMarkerWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FUNCTION_RANGE_START_MARKER_WIDTH, 1.0);
        }
        
        /**
         * Gets the width of the function range stop time line marker
         * @return width of the function range stop time line marker
         */
        public static double getFunctionRangeStopMarkerWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(FUNCTION_RANGE_STOP_MARKER_WIDTH, 1.0);
        }
        
        /**
         * Gets the width of the baseline function line plot
         * @return width of the baseline function line plot
         */
        public static double getBaselineFunctionWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(BASELINE_FUNCTION_WIDTH, 1.0);
        }
        
        /**
         * Gets the width of the application range start time line marker
         * @return width of the application range start time line marker
         */
        public static double getApplicationRangeStartMarkerWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(APPLICATION_RANGE_START_MARKER_WIDTH, 1.0);
        }
        
        /**
         * Gets the width of the application range stop time line marker
         * @return width of the application range stop time line marker
         */
        public static double getApplicationRangeStopMarkerWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(APPLICATION_RANGE_STOP_MARKER_WIDTH, 1.0);
        }
        
        /**
         * Gets the width of the event onset time line marker
         * @return width of the event onset time line marker
         */
        public static double getEventOnsetMarkerWidth()
        {
            return PREF_SM_SEISMIC_CHART_EDITOR.getDouble(EVENT_ONSET_MARKER_WIDTH, 1.0);
        }
        
        /**
         * Sets the function range start time value (in seconds)
         * @param start function range start time value (in seconds)
         */
        public static void setFunctionRangeStart(double start)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FUNCTION_RANGE_START, start);
        }
        
        /**
         * Sets the function range stop time value (in seconds)
         * @param stop stop function range start time value (in seconds)
         */
        public static void setFunctionRangeStop(double stop)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FUNCTION_RANGE_STOP, stop);
        }
        
        /**
         * Sets the application range start time value (in seconds)
         * @param start application range start time value (in seconds)
         */
        public static void setApplicationRangeStart(double start)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(APPLICATION_RANGE_START, start);
        }
        
        /**
         * Sets the application range stop time value (in seconds)
         * @param stop application range stop time value (in seconds)
         */
        public static void setApplicationRangeStop(double stop)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(APPLICATION_RANGE_STOP, stop);
        }
        
        /**
         * Sets the event onset time value (in seconds)
         * @param onset event onset time value (in seconds)
         */
        public static void setEventOnset(double onset)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(EVENT_ONSET, onset);
        }
        
        /**
         * Sets the filter range low frequency value (in hertz)
         * @param low filter range low frequency value (in hertz)
         */
        public static void setFilterRangeLow(double low)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FILTER_RANGE_LOW, low);
        }
        
        /**
         * Sets the filter range high frequency value (in hertz)
         * @param high filter range high frequency value (in hertz)
         */
        public static void setFilterRangeHigh(double high)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FILTER_RANGE_HIGH, high);
        }
        
        /**
         * Set the integer value representing the differential order to apply
         * when performing baseline correction
         * @param differentialOrder integer value representing the differential
         * order to apply when performing baseline correction
         */
        public static void setDifferentialOrder(int differentialOrder)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(DIFFERENTIAL_ORDER, differentialOrder);
        }
        
        /**
         * Set the color used for drawing the function range start time line marker
         * @param color used for drawing the function range start time line marker
         */
        public static void setFunctionRangeStartMarkerColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(FUNCTION_RANGE_START_MARKER_COLOR, color);
        }
        
        /**
         * Sets the color used for drawing the function range stop time line marker
         * @param color color used for drawing the function range stop time line marker
         */
        public static void setFunctionRangeStopMarkerColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(FUNCTION_RANGE_STOP_MARKER_COLOR, color);
        }
        
        /**
         * Sets the color used for drawing the baseline function line plot
         * @param color color used for drawing the baseline function line plot
         */
        public static void setBaselineFunctionColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(BASELINE_FUNCTION_COLOR, color);
        }
        
        /**
         * Sets the color used for drawing the application range start time line marker
         * @param color color used for drawing the application range start time line marker
         */
        public static void setApplicationRangeStartMarkerColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(APPLICATION_RANGE_START_MARKER_COLOR, color);
        }
        
        /**
         * Sets the color used for drawing the application range stop time line marker
         * @param color color used for drawing the application range stop time line marker
         */
        public static void setApplicationRangeStopMarkerColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(APPLICATION_RANGE_STOP_MARKER_COLOR, color);
        }
        
        /**
         * Sets the color used for drawing the event onset time line marker
         * @param color color used for drawing the event onset time line marker
         */
        public static void setEventOnsetMarkerColor(int color)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(EVENT_ONSET_MARKER_COLOR, color);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the function range start time
         * line marker
         * @param markerStyle MarkerStyle object used for styling the function range
         * start time line marker
         */
        public static void setFunctionRangeStartMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(FUNCTION_RANGE_START_MARKER_STYLE, style);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the function range stop time
         * line marker
         * @param markerStyle MarkerStyle object used for styling the function range
         * stop time line marker
         */
        public static void setFunctionRangeStopMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(FUNCTION_RANGE_STOP_MARKER_STYLE, style);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the baseline function
         * line plot
         * @param markerStyle MarkerStyle object used for styling the baseline function
         * line plot
         */
        public static void setBaselineFunctionStyle(MarkerStyle markerStyle)
        {
            if (markerStyle.equals(MarkerStyle.SOLID))
                PREF_SM_SEISMIC_CHART_EDITOR.putInt(BASELINE_FUNCTION_STYLE, ChartConstants.LS_SOLID);
            else if (markerStyle.equals(MarkerStyle.DASH))
                PREF_SM_SEISMIC_CHART_EDITOR.putInt(BASELINE_FUNCTION_STYLE, ChartConstants.LS_DASH_4_4);
            else
                PREF_SM_SEISMIC_CHART_EDITOR.putInt(BASELINE_FUNCTION_STYLE, ChartConstants.LS_DOT_1_4);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the application range start
         * time line marker
         * @param markerStyle MarkerStyle object used for styling the application 
         * range start time line marker
         */
        public static void setApplicationRangeStartMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(APPLICATION_RANGE_START_MARKER_STYLE, style);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the application range stop
         * time line marker
         * @param markerStyle MarkerStyle object used for styling the application 
         * range stop time line marker
         */
        public static void setApplicationRangeStopMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(APPLICATION_RANGE_STOP_MARKER_STYLE, style);
        }
        
        /**
         * Sets the MarkerStyle object used for styling the event onset time line marker
         * @param markerStyle MarkerStyle object used for styling the event onset 
         * time line marker
         */
        public static void setEventOnsetMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_SEISMIC_CHART_EDITOR.putInt(EVENT_ONSET_MARKER_STYLE, style);
        }
        
        /**
         * Sets the width of the function range start time line marker
         * @param width width of the function range start time line marker
         */
        public static void setFunctionRangeStartMarkerWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FUNCTION_RANGE_START_MARKER_WIDTH, width);
        }
        
        /**
         * Sets the width of the function range stop time line marker
         * @param width width of the function range stop time line marker
         */
        public static void setFunctionRangeStopMarkerWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(FUNCTION_RANGE_STOP_MARKER_WIDTH, width);
        }
        
        /**
         * Sets the width of the baseline function line plot
         * @param width width of the baseline function line plot
         */
        public static void setBaselineFunctionWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(BASELINE_FUNCTION_WIDTH, width);
        }
        
        /**
         * Sets the width of the application range start time line marker
         * @param width width of the application range start time line marker
         */
        public static void setApplicationRangeStartMarkerWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(APPLICATION_RANGE_START_MARKER_WIDTH, width);
        }
        
        /**
         * Sets the width of the application range stop time line marker
         * @param width width of the application range stop time line marker
         */
        public static void setApplicationRangeStopMarkerWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(APPLICATION_RANGE_STOP_MARKER_WIDTH, width);
        }
        
        /**
         * Sets the width of the event onset time line marker
         * @param width width of the event onset time line marker
         */
        public static void setEventOnsetMarkerWidth(double width)
        {
            PREF_SM_SEISMIC_CHART_EDITOR.putDouble(EVENT_ONSET_MARKER_WIDTH, width);
        }
    }
    
    /**
     * This class defines the preferences used in the Fourier Amplitude Spectrum (FAS)
     * Editor
     */
    public static class SmFAS_Editor {
        private static final Preferences PREF_SM_FAS_EDITOR = 
            Preferences.userNodeForPackage(SmPreferences.class).node(SmFAS_Editor.class.getName());
        private static final String FILTER_RANGE_LOW = "FilterRangeLow";
        private static final String FILTER_RANGE_HIGH = "FilterRangeHigh";
        
        private static final String FILTER_RANGE_LOW_MARKER_COLOR = "FilterRangeLowMarkerColor";
        private static final String FILTER_RANGE_HIGH_MARKER_COLOR = "FilterRangeHighMarkerColor";
        
        private static final String FILTER_RANGE_LOW_MARKER_STYLE = "FilterRangeLowMarkerStyle";
        private static final String FILTER_RANGE_HIGH_MARKER_STYLE = "FilterRangeHighMarkerStyle";
        
        private static final String FILTER_RANGE_LOW_MARKER_WIDTH = "FilterRangeLowMarkerWidth";
        private static final String FILTER_RANGE_HIGH_MARKER_WIDTH = "FilterRangeHighMarkerWidth";
        
        public static double getFilterRangeLow()
        {
            return PREF_SM_FAS_EDITOR.getDouble(FILTER_RANGE_LOW, 0.0);
        }
        
        public static double getFilterRangeHigh()
        {
            return PREF_SM_FAS_EDITOR.getDouble(FILTER_RANGE_HIGH, 0.0);
        }
        
        public static int getFilterRangeLowMarkerColor()
        {
            return PREF_SM_FAS_EDITOR.getInt(FILTER_RANGE_LOW_MARKER_COLOR, Color.GREEN.getRGB());
        }
        
        public static int getFilterRangeHighMarkerColor()
        {
            return PREF_SM_FAS_EDITOR.getInt(FILTER_RANGE_HIGH_MARKER_COLOR, Color.RED.getRGB());
        }
        
        public static MarkerStyle getFilterRangeLowMarkerStyle()
        {
            int style = PREF_SM_FAS_EDITOR.getInt(FILTER_RANGE_LOW_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static MarkerStyle getFilterRangeHighMarkerStyle()
        {
            int style = PREF_SM_FAS_EDITOR.getInt(FILTER_RANGE_HIGH_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static double getFilterRangeLowMarkerWidth()
        {
            return PREF_SM_FAS_EDITOR.getDouble(FILTER_RANGE_LOW_MARKER_WIDTH, 1.0);
        }
        
        public static double getFilterRangeHighMarkerWidth()
        {
            return PREF_SM_FAS_EDITOR.getDouble(FILTER_RANGE_HIGH_MARKER_WIDTH, 1.0);
        }
        
        public static void setFilterRangeLow(double low)
        {
            PREF_SM_FAS_EDITOR.putDouble(FILTER_RANGE_LOW, low);
        }
        
        public static void setFilterRangeHigh(double high)
        {
            PREF_SM_FAS_EDITOR.putDouble(FILTER_RANGE_HIGH, high);
        }
        
        public static void setFilterRangeLowMarkerColor(int color)
        {
            PREF_SM_FAS_EDITOR.putInt(FILTER_RANGE_LOW_MARKER_COLOR, color);
        }
        
        public static void setFilterRangeHighMarkerColor(int color)
        {
            PREF_SM_FAS_EDITOR.putInt(FILTER_RANGE_HIGH_MARKER_COLOR, color);
        }
        
        public static void setFilterRangeLowMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_FAS_EDITOR.putInt(FILTER_RANGE_LOW_MARKER_STYLE, style);
        }
        
        public static void setFilterRangeHighMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_FAS_EDITOR.putInt(FILTER_RANGE_HIGH_MARKER_STYLE, style);
        }
        
        public static void setFilterRangeLowMarkerWidth(double width)
        {
            PREF_SM_FAS_EDITOR.putDouble(FILTER_RANGE_LOW_MARKER_WIDTH, width);
        }
        
        public static void setFilterRangeHighMarkerWidth(double width)
        {
            PREF_SM_FAS_EDITOR.putDouble(FILTER_RANGE_HIGH_MARKER_WIDTH, width);
        }
        
    }
    
    /**
     * This class defines the preferences used in the Filter Editor
     */
    public static class SmFilter_Editor {
        private static final Preferences PREF_SM_FILTER_EDITOR = 
            Preferences.userNodeForPackage(SmPreferences.class).node(SmFilter_Editor.class.getName());
        private static final String FILTER_RANGE_LOW = "FilterRangeLow";
        private static final String FILTER_RANGE_HIGH = "FilterRangeHigh";
        
        private static final String FILTER_RANGE_LOW_MARKER_COLOR = "FilterRangeLowMarkerColor";
        private static final String FILTER_RANGE_HIGH_MARKER_COLOR = "FilterRangeHighMarkerColor";
        
        private static final String FILTER_RANGE_LOW_MARKER_STYLE = "FilterRangeLowMarkerStyle";
        private static final String FILTER_RANGE_HIGH_MARKER_STYLE = "FilterRangeHighMarkerStyle";
        
        private static final String FILTER_RANGE_LOW_MARKER_WIDTH = "FilterRangeLowMarkerWidth";
        private static final String FILTER_RANGE_HIGH_MARKER_WIDTH = "FilterRangeHighMarkerWidth";
        
        public static double getFilterRangeLow()
        {
            return PREF_SM_FILTER_EDITOR.getDouble(FILTER_RANGE_LOW, 0.0);
        }
        
        public static double getFilterRangeHigh()
        {
            return PREF_SM_FILTER_EDITOR.getDouble(FILTER_RANGE_HIGH, 0.0);
        }
        
        public static int getFilterRangeLowMarkerColor()
        {
            return PREF_SM_FILTER_EDITOR.getInt(FILTER_RANGE_LOW_MARKER_COLOR, Color.GREEN.getRGB());
        }
        
        public static int getFilterRangeHighMarkerColor()
        {
            return PREF_SM_FILTER_EDITOR.getInt(FILTER_RANGE_HIGH_MARKER_COLOR, Color.RED.getRGB());
        }
        
        public static MarkerStyle getFilterRangeLowMarkerStyle()
        {
            int style = PREF_SM_FILTER_EDITOR.getInt(FILTER_RANGE_LOW_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static MarkerStyle getFilterRangeHighMarkerStyle()
        {
            int style = PREF_SM_FILTER_EDITOR.getInt(FILTER_RANGE_HIGH_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static double getFilterRangeLowMarkerWidth()
        {
            return PREF_SM_FILTER_EDITOR.getDouble(FILTER_RANGE_LOW_MARKER_WIDTH, 1.0);
        }
        
        public static double getFilterRangeHighMarkerWidth()
        {
            return PREF_SM_FILTER_EDITOR.getDouble(FILTER_RANGE_HIGH_MARKER_WIDTH, 1.0);
        }
        
        public static void setFilterRangeLow(double low)
        {
            PREF_SM_FILTER_EDITOR.putDouble(FILTER_RANGE_LOW, low);
        }
        
        public static void setFilterRangeHigh(double high)
        {
            PREF_SM_FILTER_EDITOR.putDouble(FILTER_RANGE_HIGH, high);
        }
        
        public static void setFilterRangeLowMarkerColor(int color)
        {
            PREF_SM_FILTER_EDITOR.putInt(FILTER_RANGE_LOW_MARKER_COLOR, color);
        }
        
        public static void setFilterRangeHighMarkerColor(int color)
        {
            PREF_SM_FILTER_EDITOR.putInt(FILTER_RANGE_HIGH_MARKER_COLOR, color);
        }
        
        public static void setFilterRangeLowMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_FILTER_EDITOR.putInt(FILTER_RANGE_LOW_MARKER_STYLE, style);
        }
        
        public static void setFilterRangeHighMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_FILTER_EDITOR.putInt(FILTER_RANGE_HIGH_MARKER_STYLE, style);
        }
        
        public static void setFilterRangeLowMarkerWidth(double width)
        {
            PREF_SM_FILTER_EDITOR.putDouble(FILTER_RANGE_LOW_MARKER_WIDTH, width);
        }
        
        public static void setFilterRangeHighMarkerWidth(double width)
        {
            PREF_SM_FILTER_EDITOR.putDouble(FILTER_RANGE_HIGH_MARKER_WIDTH, width);
        }
    }
    
    /**
     * This class defines the preferences for various charting options
     */
    public static class ChartOptions {
        private static final Preferences PREF_CHART_OPTIONS = 
            Preferences.userNodeForPackage(SmPreferences.class).node(ChartOptions.class.getName());
        private static final String CHART_SIZE_SINGLE_WIDTH = "ChartSizeSingleWidth";
        private static final String CHART_SIZE_SINGLE_HEIGHT = "ChartSizeSingleHeight";
        private static final String CHART_SIZE_GROUP_WIDTH = "ChartSizeGroupWidth";
        private static final String CHART_SIZE_GROUP_HEIGHT = "ChartSizeGroupHeight";
        private static final String PLOT_AREA_POSITION_LEFT = "PlotAreaPositionLeft";
        private static final String PLOT_AREA_POSITION_TOP = "PlotAreaPositionTop";
        private static final String PLOT_AREA_POSITION_RIGHT = "PlotAreaPositionRight";
        private static final String PLOT_AREA_POSITION_BOTTOM = "PlotAreaPositionBottom";
        
        private static final String COLOR_FOREGROUND = "ColorForeground";
        private static final String COLOR_BACKGROUND = "ColorBackground";
        private static final String COLOR_PLOT = "ColorPlot";
        
        private static final String FONT_NAME = "FontName";
        private static final String FONT_IS_BOLD = "FontIsBold";
        private static final String FONT_IS_ITALIC = "FontIsItalic";
        private static final String FONT_SIZE = "FontSize";
//        private static final String FONT_COLOR = "FontColor";
        
        private static final String DISPLAY_X_AXIS_TITLE = "DisplayXAxisTitle";
        private static final String DISPLAY_Y_AXIS_TITLE = "DisplayYAxisTitle";
        private static final String DISPLAY_X_AXIS_LABELS = "DisplayXAxisLabels";
        private static final String DISPLAY_Y_AXIS_LABELS = "DisplayYAxisLabels";
        private static final String DISPLAY_X_GRID = "DisplayXGrid";
        private static final String DISPLAY_Y_GRID = "DisplayYGrid";
        private static final String DISPLAY_LEGEND = "DisplayLegend";
        private static final String DISPLAY_PLOT_LABELS = "DisplayPlotLabels";
        private static final String DISPLAY_PLOT_ANNOTATION = "DisplayPlotAnnotation";
        
        public static int getChartSizeSingleWidth()
        {
            return PREF_CHART_OPTIONS.getInt(CHART_SIZE_SINGLE_WIDTH, 600);
        }
        
        public static int getChartSizeSingleHeight()
        {
            return PREF_CHART_OPTIONS.getInt(CHART_SIZE_SINGLE_HEIGHT, 200);
        }
        
        public static int getChartSizeGroupWidth()
        {
            return PREF_CHART_OPTIONS.getInt(CHART_SIZE_GROUP_WIDTH, 600);
        }
        
        public static int getChartSizeGroupHeight()
        {
            return PREF_CHART_OPTIONS.getInt(CHART_SIZE_GROUP_HEIGHT, 400);
        }
        
        public static double getPlotAreaPositionLeft()
        {
            return PREF_CHART_OPTIONS.getDouble(PLOT_AREA_POSITION_LEFT, .15);
        }
        
        public static double getPlotAreaPositionTop()
        {
            return PREF_CHART_OPTIONS.getDouble(PLOT_AREA_POSITION_TOP, .25);
        }
        
        public static double getPlotAreaPositionRight()
        {
            return PREF_CHART_OPTIONS.getDouble(PLOT_AREA_POSITION_RIGHT, .85);
        }
        
        public static double getPlotAreaPositionBottom()
        {
            return PREF_CHART_OPTIONS.getDouble(PLOT_AREA_POSITION_BOTTOM, .75);
        }
        
        public static int getColorForeground()
        {
            return PREF_CHART_OPTIONS.getInt(COLOR_FOREGROUND, Color.BLACK.getRGB());
        }
        
        public static int getColorBackground()
        {
            return PREF_CHART_OPTIONS.getInt(COLOR_BACKGROUND, Color.WHITE.getRGB());
        }
        
        public static int getColorPlot()
        {
            return PREF_CHART_OPTIONS.getInt(COLOR_PLOT, Color.BLUE.getRGB());
        }
        
        public static String getFontName()
        {
            return PREF_CHART_OPTIONS.get(FONT_NAME, "SansSerif.plain");
        }
        
        public static boolean getFontIsBold()
        {
            return PREF_CHART_OPTIONS.getBoolean(FONT_IS_BOLD, false);
        }
        
        public static boolean getFontIsItalic()
        {
            return PREF_CHART_OPTIONS.getBoolean(FONT_IS_ITALIC, false);
        }
        
        public static int getFontSize()
        {
            return PREF_CHART_OPTIONS.getInt(FONT_SIZE, 12);
        }
        
//        public static int getFontColor()
//        {
//            return PREF_CHART_OPTIONS.getInt(FONT_COLOR, Color.BLACK.getRGB());
//        }
        
        public static boolean getDisplayXAxisTitle()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_X_AXIS_TITLE, true);
        }
        
        public static boolean getDisplayYAxisTitle()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_Y_AXIS_TITLE, true);
        }
        
        public static boolean getDisplayXAxisLabels()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_X_AXIS_LABELS, true);
        }
        
        public static boolean getDisplayYAxisLabels()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_Y_AXIS_LABELS, true);
        }
        
        public static boolean getDisplayXGrid()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_X_GRID,true);
        }
        
        public static boolean getDisplayYGrid()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_Y_GRID, true);
        }
        
        public static boolean getDisplayLegend()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_LEGEND, true);
        }
        
        public static boolean getDisplayPlotLabels()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_PLOT_LABELS, true);
        }
        
        public static boolean getDisplayPlotAnnotation()
        {
            return PREF_CHART_OPTIONS.getBoolean(DISPLAY_PLOT_ANNOTATION, false);
        }
        
        public static void setChartSizeSingleWidth(int chartSizeSingleWidth)
        {
            PREF_CHART_OPTIONS.putInt(CHART_SIZE_SINGLE_WIDTH, chartSizeSingleWidth);
        }
        
        public static void setChartSizeSingleHeight(int chartSizeSingleHeight)
        {
            PREF_CHART_OPTIONS.putInt(CHART_SIZE_SINGLE_HEIGHT, chartSizeSingleHeight);
        }
        
        public static void setChartSizeGroupWidth(int chartSizeGroupWidth)
        {
            PREF_CHART_OPTIONS.putInt(CHART_SIZE_GROUP_WIDTH, chartSizeGroupWidth);
        }
        
        public static void setChartSizeGroupHeight(int chartSizeGroupHeight)
        {
            PREF_CHART_OPTIONS.putInt(CHART_SIZE_GROUP_HEIGHT, chartSizeGroupHeight);
        }
        
        public static void setPlotAreaPositionLeft(double plotAreaPositionLeft)
        {
            PREF_CHART_OPTIONS.putDouble(PLOT_AREA_POSITION_LEFT, plotAreaPositionLeft);
        }
        
        public static void setPlotAreaPositionTop(double plotAreaPositionTop)
        {
            PREF_CHART_OPTIONS.putDouble(PLOT_AREA_POSITION_TOP, plotAreaPositionTop);
        }
        
        public static void setPlotAreaPositionRight(double plotAreaPositionRight)
        {
            PREF_CHART_OPTIONS.putDouble(PLOT_AREA_POSITION_RIGHT, plotAreaPositionRight);
        }
        
        public static void setPlotAreaPositionBottom(double plotAreaPositionBottom)
        {
            PREF_CHART_OPTIONS.putDouble(PLOT_AREA_POSITION_BOTTOM, plotAreaPositionBottom);
        }
        
        public static void setColorForeground(int colorForeground)
        {
            PREF_CHART_OPTIONS.putInt(COLOR_FOREGROUND, colorForeground);
        }
        
        public static void setColorBackground(int colorBackground)
        {
            PREF_CHART_OPTIONS.putInt(COLOR_BACKGROUND, colorBackground);
        }
        
        public static void setColorPlot(int colorPlot)
        {
            PREF_CHART_OPTIONS.putInt(COLOR_PLOT, colorPlot);
        }
        
        public static void setFontName(String fontName)
        {
            PREF_CHART_OPTIONS.put(FONT_NAME, fontName);
        }
        
        public static void setFontIsBold(boolean fontIsBold)
        {
            PREF_CHART_OPTIONS.putBoolean(FONT_IS_BOLD, fontIsBold);
        }
        
        public static void setFontIsItalic(boolean fontIsItalic)
        {
            PREF_CHART_OPTIONS.putBoolean(FONT_IS_ITALIC, fontIsItalic);
        }
        
        public static void setFontSize(int fontSize)
        {
            PREF_CHART_OPTIONS.putInt(FONT_SIZE, fontSize);
        }
        
//        public static void setFontColor(int fontColor)
//        {
//            PREF_CHART_OPTIONS.putInt(FONT_COLOR, fontColor);
//        }
        
        public static void setDisplayXAxisTitle(boolean displayXAxisTitle)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_X_AXIS_TITLE, displayXAxisTitle);
        }
        
        public static void setDisplayYAxisTitle(boolean displayYAxisTitle)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_Y_AXIS_TITLE, displayYAxisTitle);
        }
        
        public static void setDisplayXAxisLabels(boolean displayXAxisLabels)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_X_AXIS_LABELS, displayXAxisLabels);
        }
        
        public static void setDisplayYAxisLabels(boolean displayYAxisLabels)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_Y_AXIS_LABELS, displayYAxisLabels);
        }
        
        public static void setDisplayXGrid(boolean displayXGrid)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_X_GRID, displayXGrid);
        }
        
        public static void setDisplayYGrid(boolean displayYGrid)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_Y_GRID, displayYGrid);
        }
        
        public static void setDisplayLegend(boolean displayLegend)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_LEGEND, displayLegend);
        }
        
        public static void setDisplayPlotLabels(boolean displayPlotLabels)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_PLOT_LABELS, displayPlotLabels);
        }
        
        public static void setDisplayPlotAnnotation(boolean displayPlotAnnotation)
        {
            PREF_CHART_OPTIONS.putBoolean(DISPLAY_PLOT_ANNOTATION, displayPlotAnnotation);
        }
    }
    
    /**
     * This class defines the preferences used in the Event Dialog
     */
    public static class SmEventDialog {
        private static final Preferences PREF_SM_EVENT_DIALOG = 
            Preferences.userNodeForPackage(SmPreferences.class).node(SmEventDialog.class.getName());
        private static final String COSMOS_DATATYPE_V1_SELECT_STATE = "CosmosDataTypeV1SelectState";
        private static final String COSMOS_DATATYPE_V2_SELECT_STATE = "CosmosDataTypeV2SelectState";
        private static final String FOLDER_TYPE_ALL_SELECT_STATE = "FolderTypeAllSelectState";
        private static final String FOLDER_TYPE_REGULAR_SELECT_STATE = "FolderTypeRegularSelectState";
        private static final String FOLDER_TYPE_TROUBLE_SELECT_STATE = "FolderTypeTroubleSelectState";
        private static final String FOLDER_TYPE_REGULAR_ONLY_SELECT_STATE = "FolderTypeRegularOnlySelectState";
        private static final String FOLDER_TYPE_TROUBLE_ONLY_SELECT_STATE = "FolderTypeTroubleOnlySelectState";
        
        public static boolean getCosmosDataTypeV1SelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(COSMOS_DATATYPE_V1_SELECT_STATE, false);
        }
        
        public static boolean getCosmosDataTypeV2SelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(COSMOS_DATATYPE_V2_SELECT_STATE, true);
        }
        
        public static boolean getFolderTypeAllSelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(FOLDER_TYPE_ALL_SELECT_STATE, true);
        }
        
        public static boolean getFolderTypeRegularSelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(FOLDER_TYPE_REGULAR_SELECT_STATE, false);
        }
        
        public static boolean getFolderTypeTroubleSelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(FOLDER_TYPE_TROUBLE_SELECT_STATE, false);
        }
        
        public static boolean getFolderTypeRegularOnlySelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(FOLDER_TYPE_REGULAR_ONLY_SELECT_STATE, false);
        }
        
        public static boolean getFolderTypeTroubleOnlySelectState() {
            return PREF_SM_EVENT_DIALOG.getBoolean(FOLDER_TYPE_TROUBLE_ONLY_SELECT_STATE, false);
        }
        
        public static void setCosmosDataTypeV1SelectState(boolean cosmosDataTypeV1SelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(COSMOS_DATATYPE_V1_SELECT_STATE, 
                cosmosDataTypeV1SelectState);
        }
        
        public static void setCosmosDataTypeV2SelectState(boolean cosmosDataTypeV2SelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(COSMOS_DATATYPE_V2_SELECT_STATE, 
                cosmosDataTypeV2SelectState);
        }
        
        public static void setFolderTypeAllSelectState(boolean folderTypeAllSelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(FOLDER_TYPE_ALL_SELECT_STATE, 
                folderTypeAllSelectState);
        }
        
        public static void setFolderTypeRegularSelectState(boolean folderTypeRegularSelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(FOLDER_TYPE_REGULAR_SELECT_STATE, 
                folderTypeRegularSelectState);
        }
        
        public static void setFolderTypeTroubleSelectState(boolean folderTypeTroubleSelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(FOLDER_TYPE_TROUBLE_SELECT_STATE, 
                folderTypeTroubleSelectState);
        }
        
        public static void setFolderTypeRegularOnlySelectState(boolean folderTypeRegularOnlySelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(FOLDER_TYPE_REGULAR_ONLY_SELECT_STATE, 
                folderTypeRegularOnlySelectState);
        }
        
        public static void setFolderTypeTroubleOnlySelectState(boolean folderTypeTroubleOnlySelectState) {
            PREF_SM_EVENT_DIALOG.putBoolean(FOLDER_TYPE_TROUBLE_ONLY_SELECT_STATE, 
                folderTypeTroubleOnlySelectState);
        }
    }
    
    /**
     * This class defines the preferences used in the Trim Tool
     */
    public static class SmTrimTool {
        private static final Preferences PREF_SM_TRIM_TOOL = 
            Preferences.userNodeForPackage(SmPreferences.class).node(SmTrimTool.class.getName());
        private static final String START_TIME = "StartTime";
        private static final String STOP_TIME = "StopTime";
        
        private static final String START_TIME_MARKER_COLOR = "StartTimeMarkerColor";
        private static final String STOP_TIME_MARKER_COLOR = "StopTimeMarkerColor";
        private static final String DATETIME_EQUAL_TEXT_COLOR = "DateTimeEqualTextColor";
        private static final String DATETIME_UNEQUAL_TEXT_COLOR = "DateTimeUnequalTextColor";
        
        private static final String START_TIME_MARKER_STYLE = "StartTimeMarkerStyle";
        private static final String STOP_TIME_MARKER_STYLE = "StopTimeMarkerStyle";
        
        private static final String START_TIME_MARKER_WIDTH = "StartTimeMarkerWidth";
        private static final String STOP_TIME_MARKER_WIDTH = "StopTimeMarkerWidth";
        
        public static double getStartTime() {
            return PREF_SM_TRIM_TOOL.getDouble(START_TIME, 0.0);
        }
        
        public static double getStopTime() {
            return PREF_SM_TRIM_TOOL.getDouble(STOP_TIME, 0.0);
        }
        
        public static int getStartTimeMarkerColor() {
            return PREF_SM_TRIM_TOOL.getInt(START_TIME_MARKER_COLOR, Color.GREEN.getRGB());
        }
        
        public static int getStopTimeMarkerColor() {
            return PREF_SM_TRIM_TOOL.getInt(STOP_TIME_MARKER_COLOR, Color.RED.getRGB());
        }
        
        public static int getDateTimeEqualTextColor() {
            return PREF_SM_TRIM_TOOL.getInt(DATETIME_EQUAL_TEXT_COLOR, new Color(51,102,0).getRGB());
        }
        
        public static int getDateTimeUnequalTextColor() {
            return PREF_SM_TRIM_TOOL.getInt(DATETIME_UNEQUAL_TEXT_COLOR, new Color(102,0,0).getRGB());
        }
        
        public static MarkerStyle getStartTimeMarkerStyle()
        {
            int style = PREF_SM_TRIM_TOOL.getInt(START_TIME_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static MarkerStyle getStopTimeMarkerStyle()
        {
            int style = PREF_SM_TRIM_TOOL.getInt(STOP_TIME_MARKER_STYLE,
                MarkerStyle.SOLID.ordinal());
            
            if (style == MarkerStyle.SOLID.ordinal())
                return MarkerStyle.SOLID;
            else if (style == MarkerStyle.DASH.ordinal())
                return MarkerStyle.DASH;
            else
                return MarkerStyle.DOT;
        }
        
        public static double getStartTimeMarkerWidth() {
            return PREF_SM_TRIM_TOOL.getDouble(START_TIME_MARKER_WIDTH, 1.0);
        }
        
        public static double getStopTimeMarkerWidth() {
            return PREF_SM_TRIM_TOOL.getDouble(STOP_TIME_MARKER_WIDTH, 1.0);
        }
        
        public static void setStartTime(double start) {
            PREF_SM_TRIM_TOOL.putDouble(START_TIME, start);
        }
        
        public static void setStopTime(double stop) {
            PREF_SM_TRIM_TOOL.putDouble(STOP_TIME, stop);
        }
        
        public static void setStartTimeMarkerColor(int color) {
            PREF_SM_TRIM_TOOL.putInt(START_TIME_MARKER_COLOR, color);
        }
        
        public static void setStopTimeMarkerColor(int color) {
            PREF_SM_TRIM_TOOL.putInt(STOP_TIME_MARKER_COLOR, color);
        }
        
        public static void setDateTimeEqualTextColor(int color) {
            PREF_SM_TRIM_TOOL.putInt(DATETIME_EQUAL_TEXT_COLOR, color);
        }
        
        public static void setDateTimeUnequalTextColor(int color) {
            PREF_SM_TRIM_TOOL.putInt(DATETIME_UNEQUAL_TEXT_COLOR, color);
        }
        
        public static void setStartTimeMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_TRIM_TOOL.putInt(START_TIME_MARKER_STYLE, style);
        }
        
        public static void setStopTimeMarkerStyle(MarkerStyle markerStyle)
        {
            int style;
            
            if (markerStyle == MarkerStyle.SOLID)
                style = MarkerStyle.SOLID.ordinal();
            else if (markerStyle == MarkerStyle.DASH)
                style = MarkerStyle.DASH.ordinal();
            else
                style = MarkerStyle.DOT.ordinal();
            
            PREF_SM_TRIM_TOOL.putInt(STOP_TIME_MARKER_STYLE, style);
        }
        
        public static void setStartTimeMarkerWidth(double width) {
            PREF_SM_TRIM_TOOL.putDouble(START_TIME_MARKER_WIDTH, width);
        }
        
        public static void setStopTimeMarkerWidth(double width) {
            PREF_SM_TRIM_TOOL.putDouble(STOP_TIME_MARKER_WIDTH, width);
        }
    }
}
