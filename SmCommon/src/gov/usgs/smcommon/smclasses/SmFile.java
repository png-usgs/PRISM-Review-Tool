/*******************************************************************************
 * Name: Java class SmFile.java
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


import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V0Component;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import static SmConstants.VFileConstants.CORACC;
import static SmConstants.VFileConstants.DATA_PHYSICAL_PARAM_CODE;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.DISPLACE;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.RAWACC;
import static SmConstants.VFileConstants.UNCORACC;
import static SmConstants.VFileConstants.VELOCITY;
import static SmConstants.VFileConstants.V_UNITS_INDEX;
import SmControl.SmQueue;
import SmException.FormatException;
import SmException.SmException;
import SmUtilities.SmTimeFormatter;
import static gov.usgs.smcommon.smutilities.SmUtils.getFileExtension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 * This class defines a structure for storing information about a COSMOS
 * formatted file (i.e. V0, V1, V2, or V3).
 * @author png
 */
public class SmFile {
    private File file;
    private String fileName;
    private String fileDataType; //V0, V1, V2, or V3
    private ArrayList<SmRec> smRecs;

    /**
     * Constructor
     * @param file File object that references a particular COSMOS file
     */
    public SmFile(File file)
    {
        this.file = file;
        setFileName(this.file);
        setFileDataType(this.file);
        setSmRecs(this.file,this.fileDataType);
    }
    
    // Copy constructor
    public SmFile(SmFile smFile)
    {
        this.file = new File(smFile.getFile().getPath());
        this.fileName = smFile.getFileName();
        this.fileDataType = smFile.getFileDataType();
        
        this.smRecs = new ArrayList<>();
        for (SmRec smRec : smFile.getSmRecs()) {
            this.smRecs.add(new SmRec(smRec));
        }
    }

    /**
     * Gets the File object for the referenced COSMOS file
     * @return File object for the referenced COSMOS file
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * Gets the file name of the COSMOS file
     * @return file name of COSMOS file
     */
    public String getFileName()
    {
        return this.fileName;
    }

    /**
     * Gets the COSMOS file data type (i.e. V0, V1, V2, or V3) of referenced
     * COSMOS file
     * @return COSMOS file data type of referenced COSMOS file
     */
    public String getFileDataType()
    {
        return this.fileDataType;
    }

    /**
     * Gets the array list of SmRec objects. An SmRec object stores information
     * about a record component from the referenced COSMOS file.
     * @return array list of SmRec objects
     */
    public ArrayList<SmRec> getSmRecs()
    {
        return this.smRecs;
    }

    /**
     * Sets the File object for the referenced COSMOS file
     * @param file File object for the referenced COSMOS file
     */
    public void setFile(File file) {this.file = file;}
    
    /**
     * Sets the file name of referenced COSMOS file
     * @param file File object of referenced COSMOS file
     */
    private void setFileName(File file) {this.fileName = file.getName();}
    
    /**
     * Sets the COSMOS file data type of referenced COSMOS file
     * @param file COSMOS file data type of referenced COSMOS file
     */
    private void setFileDataType(File file)
    {
        String ext = getFileExtension(file).toUpperCase();
        
        if (ext.startsWith(SmGlobal.CosmosFileType.V0.toString()))
            this.fileDataType = RAWACC;
        else if (ext.startsWith(SmGlobal.CosmosFileType.V1.toString()))
            this.fileDataType = UNCORACC;
        else if (ext.startsWith(SmGlobal.CosmosFileType.V2.toString()))
        {
            String fName = file.getName().toUpperCase();
            
            if (fName.contains(SmGlobal.CosmosV2DataType.ACC.toString()))
                this.fileDataType = CORACC;
            else if (fName.contains(SmGlobal.CosmosV2DataType.VEL.toString()))
                this.fileDataType = VELOCITY;
            else if (fName.contains(SmGlobal.CosmosV2DataType.DIS.toString()))
                this.fileDataType = DISPLACE;
        }
    }

    /**
     * Sets the array list of SmRec objects for referenced COSMOS file
     * @param file File object for referenced COSMOS file
     * @param fileDataType COSMOS file data type for referenced COSMOS file
     */
    private void setSmRecs(File file, String fileDataType)
    {
        try 
        {
            SmTimeFormatter timer = new SmTimeFormatter();
            String logTime = timer.getGMTdateTime();
            String logDir = SmPreferences.General.getLogsDir();
            
            SmQueue queue = new SmQueue(file,logTime,new File(logDir));
            queue.readInFile(file);
            queue.parseVFile(fileDataType);

            ArrayList<COSMOScontentFormat> smlist = queue.getSmList();

            smRecs = new ArrayList<>();

            for (COSMOScontentFormat rec : smlist) {
                final String[] textHeaders = rec.getTextHeader();
                final String eventName = textHeaders[1].substring(0, 40).
                    replaceAll("(?i)record", "").replaceAll("(?i)of","").trim();
                final String eventDateTime = textHeaders[1].substring(40).trim();
                String networkCode = textHeaders[4].substring(25, 27).trim();
                String stationCode = textHeaders[4].substring(28, 34).trim();
                String stationName = textHeaders[4].substring(40).trim();
                final int dataParmCode = rec.getIntHeaderValue(DATA_PHYSICAL_PARAM_CODE);
                final int dataUnitCode = rec.getIntHeaderValue(V_UNITS_INDEX);
                final String channel = rec.getChannel();
                final String sensorLocation = rec.getSensorLocation();
            
                final int startDateYr = rec.getIntHeaderValue(39);
//                final int startDateJulianDay = rec.getIntHeaderValue(40);
                final int startDateMth = rec.getIntHeaderValue(41);
                final int startDateDay = rec.getIntHeaderValue(42);
                final int startTimeHr = rec.getIntHeaderValue(43);
                final int startTimeMin = rec.getIntHeaderValue(44);
                final double startTimeSec = rec.getRealHeaderValue(29);
                final int startSec = (int)Math.floor(startTimeSec);
                final int startMs = (int) Math.round((startTimeSec-Math.floor(startTimeSec))*1000);
               
                final DateTime startDateTime = new DateTime(startDateYr,
                    startDateMth,startDateDay,startTimeHr,startTimeMin,startSec,startMs);
                
//                final double deltaT = rec.getRealHeaderValue(DELTA_T) * MSEC_TO_SEC;
                final double deltaT = rec.getRealHeaderValue(DELTA_T);
//                final double maxVal = rec.getRealHeaderValue(MAX_VAL);
                final double maxVal = rec.getRealHeaderValue(63);

                ArrayList<SmPoint> smPoints = new ArrayList<>();
                
                if (fileDataType.equals(RAWACC)) {
                    V0Component v0Rec = (V0Component) rec;
                    int[] points = v0Rec.getDataArray();
                    
                    double xVal = 0;
                    for (int i=0; i<points.length; i++){
                        smPoints.add(new SmPoint(xVal,points[i]));
                        xVal += deltaT*MSEC_TO_SEC;
                    }
                }
                else if (fileDataType.equals(UNCORACC))
                {
                    V1Component v1Rec = (V1Component) rec;
                    double[] points = v1Rec.getDataArray();
                    
                    double xVal = 0;
                    for (int i=0; i<points.length; i++){
                        smPoints.add(new SmPoint(xVal,points[i]));
                        xVal += deltaT*MSEC_TO_SEC;
                    }
                }
                else if (fileDataType.equals(CORACC) ||
                    fileDataType.equals(VELOCITY) || 
                    fileDataType.equals(DISPLACE))
                {
                    V2Component v2Rec = (V2Component) rec;
                    double[] points = v2Rec.getDataArray();
                    
                    double xVal = 0;
                    for (int i=0; i<points.length; i++){
                        smPoints.add(new SmPoint(xVal,points[i]));
                        xVal += deltaT*MSEC_TO_SEC;
                    }
                }
                
                smRecs.add(new SmRec(smPoints, channel, sensorLocation, stationCode, 
                    stationName, networkCode, startDateTime, deltaT, MSEC_TO_SEC, maxVal, 
                    eventName, eventDateTime, dataParmCode, dataUnitCode));
            }
        } 
        catch (IOException | FormatException | SmException ex ) {
            System.out.format("Unable to read or parse file: %s - %s%n",
                    file.getName(), ex.getMessage());
        }
    }
    
    /**
     * Creates a formatted string consisting of the file name of the referenced
     * COSMOS file
     * @return 
     */
    @Override
    public String toString() {
        return fileName;
    }
}
