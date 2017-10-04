/*******************************************************************************
 * Name: Java class SmRec.java
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


import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static gov.usgs.smcommon.smutilities.MathUtils.isPowerOf2_V2;
import static gov.usgs.smcommon.smutilities.MathUtils.nextPowerOf2;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author png
 */
public class SmRec {
    private ArrayList<SmPoint> smPoints;
    private ArrayList<SmPoint> smPointsFFT;
    
    private String stationCode;
    private String stationName;
    private String channel;
    private String sensorLocation;
    private String networkCode;
    
    private DateTime startDateTime;
    private DateTime endDateTime;
    
    private double deltaT;          //Time interval in milli-seconds
    private double durationMs;      //Time series duration in milli-seconds
    private double maxVal;
    private String eventName;
    private String eventDateTime;
    
    private int dataParmCode;
    private int dataUnitCode;

    public SmRec(ArrayList<SmPoint> smPoints, String channel, String sensorLocation,
        String stationCode, String stationName, String networkCode, DateTime startDateTime, 
        double deltaT, double unitConversionFactor, double maxVal, String eventName, 
        String eventDateTime, int dataParmCode, int dataUnitCode )
    {
        this.smPoints = smPoints;
        
        this.channel = channel;
        this.sensorLocation = sensorLocation;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.networkCode = networkCode;
       
        this.deltaT = deltaT;
        this.durationMs = (smPoints.size()-1)*this.deltaT;
        this.maxVal = maxVal;
        this.eventName = eventName;
        this.eventDateTime = eventDateTime;
        
        this.dataParmCode = dataParmCode;
        this.dataUnitCode = dataUnitCode;

        this.smPointsFFT = createSmPointsFFT(this.smPoints, this.deltaT, unitConversionFactor);

        this.startDateTime = startDateTime;
        this.endDateTime = calcEndDateTime(this.startDateTime,this.durationMs);
    }
    
    // Copy Constructor
    public SmRec(SmRec smRec)
    {
        this.smPoints = new ArrayList<>();
        for (SmPoint smPoint : smRec.getSmPoints()) {
            this.smPoints.add(new SmPoint(smPoint));
        }
        
        this.smPointsFFT = new ArrayList<>();
        for (SmPoint smPointFFT : smRec.getSmPointsFFT()) {
            this.smPointsFFT.add(new SmPoint(smPointFFT));
        }
        
        this.channel = smRec.getChannel();
        this.sensorLocation = smRec.getSensorLocation();
        this.stationCode = smRec.getStationCode();
        this.stationName = smRec.getStationName();
        this.networkCode = smRec.getNetworkCode();
        
        this.startDateTime = smRec.getStartDateTime();
        this.endDateTime = smRec.getEndDateTime();
        
        this.deltaT = smRec.getDeltaT();
        this.durationMs = smRec.getDurationMs();
        this.maxVal = smRec.getMaxVal();
        this.eventName = smRec.getEventName();
        this.eventDateTime = smRec.getEventDateTime();
        
        this.dataParmCode = smRec.getDataParmCode();
        this.dataUnitCode = smRec.getDataUnitCode();
    }

    public ArrayList<SmPoint> getSmPoints() {return this.smPoints;}

    public void setSmPoints(ArrayList<SmPoint> smPoints) {this.smPoints = smPoints;}
    
    public String getChannel() {return this.channel;}
    
    public void setChannel(String channel) {this.channel = channel;}
    
    public String getSensorLocation() {return this.sensorLocation;}
    
    public void setSensorLocation(String sensorLocation) {this.sensorLocation = sensorLocation;}
    
    public String getStationCode() {return this.stationCode;}
    
    public void setStationCode(String stationCode) {this.stationCode = stationCode;}
    
    public String getStationName() {return this.stationName;}
    
    public void setStationName(String stationName) {this.stationName = stationName;}
    
    public String getNetworkCode() {return this.networkCode;}
    
    public void setNetworkCode(String networkCode) {this.networkCode = networkCode;}
    
    public DateTime getStartDateTime() {return this.startDateTime;}
    
    public void setStartDateTime(DateTime startDateTime) {this.startDateTime = startDateTime;}
    
    public DateTime getEndDateTime() {return this.endDateTime;}
    
    public void setEndDateTime(DateTime startDateTime) {this.startDateTime = startDateTime;}

    public double getDeltaT() {return this.deltaT;}

    public void setDeltaT(double deltaT) {this.deltaT = deltaT;}
    
    public double getDurationMs() {return this.durationMs;}
    
    public void setDurationMs(double durationMs) {this.durationMs = durationMs;}

    public double getMaxVal() {return this.maxVal;}

    public void setMaxVal(double maxVal) {this.maxVal = maxVal;}

    public String getEventName() {return this.eventName;}

    public void setEventName(String eventName) {this.eventName = eventName;}
    
    public String getEventDateTime() {return this.eventDateTime;}

    public void setEventDateTime(String eventDateTime) {this.eventDateTime = eventDateTime;}

    public int getDataParmCode() {return this.dataParmCode;}

    public void setDataParmCode(int dataParmCode) {this.dataParmCode = dataParmCode;}

    public ArrayList<SmPoint> getSmPointsFFT() {return this.smPointsFFT;}

    public int getDataUnitCode() {return this.dataUnitCode;}

    public void setDataUnitCode(int dataUnitCode) {this.dataUnitCode = dataUnitCode;}
     
    /**
     * Returns an adjusted, padded array of SmPoint objects, where for each 
     * SmPoint, the x value represents the time (in seconds) relative to the
     * earliest start date time and the y value the recorded COSMOS data value.
     * SmPoint objects are added to the array as necessary to pad the time between
     * the earliest start date time and the record start date time and between the
     * the record stop date time and the latest stop date time.
     * date time difference.
     * @param earliestStartDateTime
     * @param latestStopDateTime
     * @param minDeltaT
     * @return ArrayList of SmPoints objects.
     */
    public ArrayList<SmPoint> createAdjustedSmPointsPaddedOld(DateTime earliestStartDateTime,
        DateTime latestStopDateTime, double minDeltaT)
    {
        if (startDateTime.isAfter(endDateTime))
            return null;
        
        if (earliestStartDateTime.isAfter(latestStopDateTime))
            return null;
        
        double firstY = this.getSmPoints().get(0).getY();
        double lastY = this.getSmPoints().get(this.getSmPoints().size()-1).getY();
        
        ArrayList<SmPoint> adjustedSmPoints = new ArrayList<>();
        
        int factor = (int) Math.round(this.getDeltaT()/minDeltaT);
        
        Duration durTimeRange = new Duration(earliestStartDateTime,latestStopDateTime);
        double timeRangeMs = durTimeRange.getMillis();
        long expectedNumPts = Math.round(timeRangeMs/minDeltaT)+1;
        
        long actualNumPts = 0;
        double xVal = 0;
        DateTime curDateTime;
            
        // Add points for period between earliest and record start DateTimes.
        if (startDateTime.isAfter(earliestStartDateTime))
        {
            curDateTime = earliestStartDateTime;
            
            // Create points before record start DateTime.
            while (curDateTime.isBefore(startDateTime))
            {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,firstY));
                xVal += minDeltaT;
                curDateTime = curDateTime.plusMillis((int) Math.round(minDeltaT));
                actualNumPts++;
            }
        }
        
        // Create points for period between record start and end DateTimes.
        curDateTime = startDateTime;
        for (SmPoint smPoint : this.getSmPoints())
        {
            for (int i=0; i<factor; i++)
            {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,smPoint.getY()));
                xVal += minDeltaT;
                curDateTime = curDateTime.plusMillis((int) Math.round(minDeltaT));
                actualNumPts++;
            }
        }
        
        // Create points for period between record end and latest DateTimes.
        if (curDateTime.isBefore(latestStopDateTime))
        {
            while (!curDateTime.isAfter(latestStopDateTime))
            {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,lastY));
                xVal += minDeltaT;
                curDateTime = curDateTime.plusMillis((int) Math.round(minDeltaT));
                actualNumPts++;
            }
        }
        
        // Add additional points to equal adjusted number of points.
        while (actualNumPts < expectedNumPts)
        {
            adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,lastY));
            xVal += minDeltaT;
            actualNumPts++;
        }
            
        return adjustedSmPoints;
    }
    
    /**
     * Returns an adjusted, padded array of SmPoint objects, where for each 
     * SmPoint, the x value represents the time (in seconds) relative to the
     * earliest start date time and the y value the recorded COSMOS data value.
     * SmPoint objects are added to the array as necessary to pad the time between
     * the earliest start date time and the record start date time and between the
     * the record stop date time and the latest stop date time.
     * date time difference.
     * @param earliestStartDateTime
     * @param latestStopDateTime
     * @param minDeltaT
     * @return ArrayList of SmPoints objects.
     */
    public ArrayList<SmPoint> createAdjustedSmPointsPadded(DateTime earliestStartDateTime,
        DateTime latestStopDateTime)
    {
        if (startDateTime.isAfter(endDateTime))
            return null;
        
        if (earliestStartDateTime.isAfter(latestStopDateTime))
            return null;
        
        double firstY = this.getSmPoints().get(0).getY();
        double lastY = this.getSmPoints().get(this.getSmPoints().size()-1).getY();
        
        ArrayList<SmPoint> adjustedSmPoints = new ArrayList<>();
        
        Duration durTimeRange = new Duration(earliestStartDateTime,latestStopDateTime);
        double timeRangeMs = durTimeRange.getMillis();
        long expectedNumPts = Math.round(timeRangeMs/deltaT)+1;
        
        long actualNumPts = 0;
        double xVal = 0;
        DateTime curDateTime;
            
        // Add points for period between earliest and record start DateTimes.
        if (startDateTime.isAfter(earliestStartDateTime))
        {
            curDateTime = earliestStartDateTime;
            
            // Create points before record start DateTime.
            while (curDateTime.isBefore(startDateTime))
            {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,firstY));
                xVal += deltaT;
                curDateTime = curDateTime.plusMillis((int) Math.round(deltaT));
                actualNumPts++;
            }
        }
        
        // Create points for period between record start and end DateTimes.
        curDateTime = startDateTime;
        for (SmPoint smPoint : this.getSmPoints())
        {
            adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,smPoint.getY()));
            xVal += deltaT;
            curDateTime = curDateTime.plusMillis((int) Math.round(deltaT));
            actualNumPts++;
        }
        
        // Create points for period between record end and latest DateTimes.
        if (curDateTime.isBefore(latestStopDateTime))
        {
            while (!curDateTime.isAfter(latestStopDateTime))
            {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,lastY));
                xVal += deltaT;
                curDateTime = curDateTime.plusMillis((int) Math.round(deltaT));
                actualNumPts++;
            }
        }
        
        // Add additional points to equal adjusted number of points.
        while (actualNumPts < expectedNumPts)
        {
            adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,lastY));
            xVal += deltaT;
            actualNumPts++;
        }
            
        return adjustedSmPoints;
    }
    
    /**
     * Returns an adjusted, unpadded array of SmPoint objects, where for each 
     * SmPoint, the x value represents the time (in seconds) relative to the
     * earliest start date time and the y value the recorded COSMOS data value.
     * @param earliestStartDateTime
     * @param latestStopDateTime
     * @return ArrayList of SmPoints objects.
     */
    public ArrayList<SmPoint> createAdjustedSmPointsUnpadded(DateTime earliestStartDateTime,
        DateTime latestStopDateTime)
    {
        if (earliestStartDateTime.isAfter(latestStopDateTime))
            return null;
        
        ArrayList<SmPoint> adjustedSmPoints = new ArrayList<>();
        
        if (startDateTime.isAfter(earliestStartDateTime))
        {
            Duration durStartDateTimeDiff = new Duration(earliestStartDateTime,startDateTime);
        
            // Initialize x to the start duration difference relative to earliest start 
            // time, which, in this case, is at time zero.
            double xVal = durStartDateTimeDiff.getMillis();
            
            for (SmPoint smPoint : smPoints) {
                adjustedSmPoints.add(new SmPoint(xVal*MSEC_TO_SEC,smPoint.getY()));
                xVal += deltaT;
            }
        }
        else if (startDateTime.isBefore(earliestStartDateTime)) {
            Duration durStartDateTimeDiff = new Duration(startDateTime,earliestStartDateTime);
        
            // Initialize x to time zero.
            double xVal = 0;
            
            for (SmPoint smPoint : smPoints) {
                // Include the point only if current x is at or after the
                // start date time duration difference.
                if (xVal >= durStartDateTimeDiff.getMillis()) {
                    adjustedSmPoints.add(new SmPoint(smPoint.getX(),smPoint.getY()));
                }
                
                xVal += deltaT;
            }
        }
        else if (startDateTime.isEqual(earliestStartDateTime)) {
            for (SmPoint smPoint : smPoints) {
                adjustedSmPoints.add(new SmPoint(smPoint.getX(),smPoint.getY()));
            }
        }
        
        // Remove points from the adjusted points array that occur after the 
        // latest stop date time.
        if (endDateTime.isBefore(latestStopDateTime)) {
            // do nothing
        }
        else if (endDateTime.isAfter(latestStopDateTime)) {
            Duration durStopDateTimeDiff = new Duration(earliestStartDateTime,latestStopDateTime);
            
            ArrayList<SmPoint> adjustedSmPoints2 = new ArrayList<>();
            
            // Initialize x to the time of first point in adjusted points array,
            // converting the time to milli-seconds.
            double xVal = adjustedSmPoints.get(0).getX() / MSEC_TO_SEC;
            
            for (SmPoint smPoint : adjustedSmPoints) {
                // Add point to array only if x is less than the stop
                // date time duration difference.
                if (xVal < durStopDateTimeDiff.getMillis()) {
                    adjustedSmPoints2.add(new SmPoint(smPoint.getX(),smPoint.getY()));
                    xVal += deltaT;
                }
            }
            
            adjustedSmPoints = adjustedSmPoints2;
        }
        else if (endDateTime.isEqual(latestStopDateTime)) {
            // do nothing
        }
        
        return adjustedSmPoints;
    }
    
    public static ArrayList<SmPoint> createSmPointsFFT(ArrayList<SmPoint> smPoints, double deltaT,
        double unitConversionFactor)
    {
        try {
            int pointsFFTLen = (isPowerOf2_V2(smPoints.size())) ?
                smPoints.size() : nextPowerOf2(smPoints.size());
            double[] pointsFFT = new double[pointsFFTLen];

    //        double lastY = smPoints.get(smPoints.size()-1).getY();

            for (int i=0; i<pointsFFTLen; i++)
            {
    //            pointsFFT[i] = (i < smPoints.size()) ? smPoints.get(i).getY() : lastY;
                pointsFFT[i] = (i < smPoints.size()) ? smPoints.get(i).getY() : 0;
            }

            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] pointsComplexFFT = fft.transform(pointsFFT, TransformType.FORWARD);

            double totalTime = (deltaT * unitConversionFactor)*(pointsFFT.length-1);
            double deltaF = 1 / totalTime;

            // Create SmPoints object list, skipping over the first point because
            // frequency = 0 for the first point.
            ArrayList<SmPoint> smPointsFFT = new ArrayList<>();

            double frequency = 0;
            double magnitude = 0;

            for (int i=0; i<pointsFFT.length; i++)
            {
                magnitude = Math.sqrt((pointsComplexFFT[i].getReal()*pointsComplexFFT[i].getReal()) +
                    (pointsComplexFFT[i].getImaginary()*pointsComplexFFT[i].getImaginary()));

                smPointsFFT.add(new SmPoint(frequency,magnitude));

                frequency += deltaF;
            }
            
            return smPointsFFT;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    private double calcMinFrequency(ArrayList<SmPoint> smPointsFFT)
    {
        SmPoint smPoint = smPointsFFT.get(1); // 2nd point frequency = DeltaF
        return smPoint.getX();
    }

    private double calcMaxFrequency(ArrayList<SmPoint> smPointsFFT)
    {
        int nyquistIndx = smPointsFFT.size()/2;
        SmPoint smPoint = smPointsFFT.get(nyquistIndx);
        return smPoint.getX();
    }

    private double calcMinFrequencyBoundary(double minFrequency)
    {
        return Math.floor(Math.log10(minFrequency));
    }

    private double calcMaxFrequencyBoundary(double maxFrequency)
    {
        return Math.ceil(Math.log10(maxFrequency));
    }

    private double calcMinMagnitude(ArrayList<SmPoint> smPointsFFT)
    {
        // Initialize return variable.
        double minMagnitude = smPointsFFT.get(1).getY();
        
        int nyquistIndx = smPointsFFT.size()/2;
        
        for (int i=1; i<nyquistIndx; i++)
        {
            SmPoint smPointFFT = smPointsFFT.get(i);
            double yVal = smPointFFT.getY();

            if (minMagnitude > yVal)
                minMagnitude = yVal;
        }
        
        return minMagnitude;
    }

    private double calcMaxMagnitude(ArrayList<SmPoint> smPointsFFT)
    {
        // Initialize return variable.
        double maxMagnitude = smPointsFFT.get(1).getY();
        
        int nyquistIndx = smPointsFFT.size()/2;
        
        for (int i=1; i<nyquistIndx; i++)
        {
            SmPoint smPointFFT = smPointsFFT.get(i);
            double yVal = smPointFFT.getY();

            if (maxMagnitude < yVal)
                maxMagnitude = yVal;
        }
        
        return maxMagnitude;
    }

    private double calcMinMagnitudeBoundary(double minMagnitude)
    {
        return Math.floor(Math.log10(minMagnitude));
    }

    private double calcMaxMagnitudeBoundary(double maxMagnitude)
    {
        return Math.ceil(Math.log10(maxMagnitude));
    }
    
    private DateTime calcEndDateTime(DateTime startDateTime, double durationMs)
    { 
        Duration duration = new Duration((long)durationMs);
        return startDateTime.plus(duration);
    }
        
}
