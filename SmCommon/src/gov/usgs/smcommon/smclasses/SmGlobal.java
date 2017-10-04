/*******************************************************************************
 * Name: Java class SmGlobal.java
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

/**
 * This class defines constants that are referenced throughout the project
 * @author png
 */
public class SmGlobal {

    public static final String LOGS = "Logs";   //Log folder name
    public static final String EDIT_LOG_FILE = "edit_log.txt";
    public static final String TRIM_LOG_FILE = "trim_log.txt";
    public static final String APK_TABLE_FILE = "apktable.csv";
    public static final String TROUBLE = "Trouble"; //Trouble folder name
    public static final String FAILED = "-failed";
    public static final String NO_SELECTION = "--No Selection--";
    public static final String PLOT_TYPE_SEISMIC = "Seismic";
    public static final String PLOT_TYPE_SPECTRAL = "Spectral";
    
    public static final String SM_CHARTS_API_QCCHART2D = "QCChart2D";
    
    //public static final double MSEC_TO_SEC = 0.001;
    
    public static enum CosmosFileType {V0,V1,V2,V3};
    public static enum CosmosV2DataType {ACC,VEL,DIS}
    
//    public static enum DataType {
//        ACC ("Acceleration",1,"Accelaration (cm/sec" + "\u00B2" + ")"),
//        VEL ("Velocity",2,"Velocity (cm/sec)"), 
//        DIS ("Displacement",3,"Displacement (cm)");
//        
//        private final String name;
//        private final int dataParmCode;
//        private final String label;
//        
//        
//        DataType(String name, int dataParmCode, String label) {
//            this.name = name;
//            this.dataParmCode = dataParmCode;
//            this.label = label;
//        }
//        
//        public String Name() {return this.name;}
//        public int DataParmCode() {return this.dataParmCode;}
//        public String Label() {return this.label;}
//    }
}
