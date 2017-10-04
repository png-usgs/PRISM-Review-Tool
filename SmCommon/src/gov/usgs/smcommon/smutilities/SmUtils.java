/*******************************************************************************
 * Name: Java class SmUtils.java
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

package gov.usgs.smcommon.smutilities;

import gov.usgs.smcommon.smclasses.SmChannel;
import gov.usgs.smcommon.smclasses.SmEpoch;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmSeries;
import gov.usgs.smcommon.smclasses.SmStation;
import gov.usgs.smcommon.smclasses.SmTemplate;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;

/**
 *
 * @author png
 */
public class SmUtils {
    
    /**
     *
     * @param inSmSeriesList
     * @param smTemplate
     * @param earliestDateTime
     * @param latestDateTime
     * @return a template based list, if possible. Otherwise, returns the
     * original list sorted.
     */
    public static ArrayList<SmSeries> createTemplateBasedList(
        ArrayList<SmSeries> inSmSeriesList, SmTemplate smTemplate,
        DateTime earliestDateTime, DateTime latestDateTime)
    {
        if (smTemplate == null)
            return inSmSeriesList;

        // Create list of all dataParmCodes in smSeriesList.
        ArrayList<Integer> dataParmCodes = new ArrayList<>();
        for (SmSeries smSeries : inSmSeriesList)
        {
            int dataParmCode = smSeries.getDataParmCode();

            if (!dataParmCodes.contains(dataParmCode))
                dataParmCodes.add(dataParmCode);
        }
        
        // Retrieve the epoch that satisfies the earliest and latest 
        // DateTime range, then retrieve the epoch's list of channels.
        SmStation smStation = smTemplate.getSmStation();
        SmEpoch smEpoch = smStation.getSmEpoch(earliestDateTime, latestDateTime);
        ArrayList<SmChannel> smChannels = smEpoch.getSmChannels();
        
        ArrayList<SmSeries> outSmSeriesList = new ArrayList<>();
        
        for (int dataParmCode : dataParmCodes) {
            for (SmChannel smChannel : smChannels) {
                String channel = smChannel.getChannel(); 
                
                boolean found = false;
                for (SmSeries smSeries : inSmSeriesList) {
                    if (smSeries.getDataParmCode() == dataParmCode &&
                        smSeries.getTitle().toLowerCase().startsWith(channel.toLowerCase())){
                        found = true;
                        outSmSeriesList.add(smSeries);
                        break;
                    }
                }
                
                if (!found) {
                    outSmSeriesList.add(new SmSeries(null,dataParmCode,
                        channel,channel,smChannel.getColor(),null));
                }
            }
        }

        return outSmSeriesList;
    }
    
    public static DateTime getEarliestStartTime(ArrayList<String> filePaths)
    {
        DateTime earliestStartTime = null;
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            
            if (!file.exists())
                continue;
                
            SmFile smFile = new SmFile(file);
            
            for (SmRec smRec : smFile.getSmRecs()) {
                DateTime startDateTime = smRec.getStartDateTime();
            
                if (earliestStartTime == null)
                    earliestStartTime = startDateTime;
                else {
                    if (startDateTime.isBefore(earliestStartTime))
                        earliestStartTime = startDateTime;
                }
            }
        }
        
        return earliestStartTime;
    }
    
    public static DateTime getLatestStartTime(ArrayList<String> filePaths)
    {
        DateTime latestStartTime = null;
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            
            if (!file.exists())
                continue;
                
            SmFile smFile = new SmFile(file);
            
            for (SmRec smRec : smFile.getSmRecs()) {
                DateTime startDateTime = smRec.getStartDateTime();
            
                if (latestStartTime == null)
                    latestStartTime = startDateTime;
                else {
                    if (startDateTime.isAfter(latestStartTime))
                        latestStartTime = startDateTime;
                }
            }
        }
        
        return latestStartTime;
    }
    
    public static DateTime getEarliestStopTime(ArrayList<String> filePaths)
    {
        DateTime earliestStopTime = null;
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            
            if (!file.exists())
                continue;
                
            SmFile smFile = new SmFile(file);
            
            for (SmRec smRec : smFile.getSmRecs()) {
                DateTime stopDateTime = smRec.getEndDateTime();
            
                if (earliestStopTime == null)
                    earliestStopTime = stopDateTime;
                else {
                    if (stopDateTime.isBefore(earliestStopTime))
                        earliestStopTime = stopDateTime;
                }
            }
        }
        
        return earliestStopTime;
    }
    
    public static DateTime getLatestStopTime(ArrayList<String> filePaths)
    {
        DateTime latestStopTime = null;
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            
            if (!file.exists())
                continue;
                
            SmFile smFile = new SmFile(file);
            
            for (SmRec smRec : smFile.getSmRecs()) {
                DateTime stopDateTime = smRec.getEndDateTime();
            
                if (latestStopTime == null)
                    latestStopTime = stopDateTime;
                else {
                    if (stopDateTime.isAfter(latestStopTime))
                        latestStopTime = stopDateTime;
                }
            }
        }
        
        return latestStopTime;
    }
    
    /**
     *
     * @param filePaths - COSMOS file paths.
     * @return
     */
    public static double getMinimumDeltaT(ArrayList<String> filePaths)
    {
        double minDeltaT = 0;
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            
            if (!file.exists())
                continue;
                
            SmFile smFile = new SmFile(file);
            
            boolean first = true;
            
            for (SmRec smRec : smFile.getSmRecs()) {
                double deltaT = smRec.getDeltaT();
                
                if (first) {
                    minDeltaT = deltaT;
                    first = false;
                }
                else {
                    if (deltaT < minDeltaT)
                        minDeltaT = deltaT;
                }
            }
        }
        
        return minDeltaT;
    }
    
    /**
 * This method returns the formatted date and time, with "GMT" appended
 * @return Text representation of date and time
 */
    public static String getGMT_DateTime() {
        String result;
        
        TimeZone zone = TimeZone.getTimeZone("GMT");
        Calendar cal = new GregorianCalendar(zone);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        result = String.format("%1$4d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d %7$3s",
            year,month,day,hour,min,sec,"GMT");
        
        return result;
    }
    
    public static boolean writeToFile(ArrayList<String> contents, File file) throws IOException {
        try {
            File parentDir = file.getParentFile();
            
            // Create parent directory if necessary.
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Create file if necessary.
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }
            
            Path pathFile = file.toPath();
            BufferedWriter writer = Files.newBufferedWriter(pathFile, 
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            
            for (String s : contents){
                writer.write(s, 0, s.length());
                writer.newLine();
            }
            
            //Close the output stream
            writer.close();
            
            return true;
        }
        catch (IOException ex) {
            throw ex;
        }
    }
    
    public static boolean deleteFile(File srcFilePath) throws IOException {
        try {
            if (srcFilePath.exists()) {
                srcFilePath.delete();
                
                return true;
            }
            else
                return false;
        }
        catch (Exception ex) {throw ex;}
    }
    
    public static boolean moveFile(File srcFile, File destFile) throws IOException {
        try {
            if (srcFile.exists()) {
                // Create any directories along the output path that don't exist.
                Path srcPath = srcFile.toPath();
                Path destPath = destFile.toPath();
                
                // Create destination directory if necessary.
                File destDir = destFile.getParentFile();
                if (!destDir.exists())
                    destDir.mkdirs();

                // Move the infile to outfile.
                Files.move(srcPath, destPath, REPLACE_EXISTING);
                return true;
            }
            else
                return false;
        }
        catch (Exception ex) {throw ex;}
    }
    
    public static boolean moveFileToTrash(File srcFile, File trashFile) throws IOException {
        try {
            if (srcFile.getPath().isEmpty() || trashFile.getPath().isEmpty())
                return false;
            
            if (srcFile.exists()) {
                Path srcPath = srcFile.toPath();
                Path trashPath = trashFile.toPath();
                
                // Create trash directory if necessary.
                File trashDir = trashFile.getParentFile();
                if (!trashDir.exists())
                    trashDir.mkdirs();

                // Check if trashPath already exists. If so, attach an increment
                // number to the filename. Iterate until first filename with 
                // incremented number is not found.
                if (trashFile.exists()) {
                    String origFileName = trashFile.getName();
                    String rx = "(?i)(^.*)(.V[0123][cC]?$)";
                    Pattern pattern = Pattern.compile(rx);
                    Matcher m = pattern.matcher(origFileName);
                    if (!m.find())
                        return false;

                    int num = 2;
                    while (trashFile.exists()) {
                        String newFileName = String.format("%s(%d)%s", m.group(1),num,m.group(2));
                        trashFile = Paths.get(trashFile.getParent(),newFileName).toFile();
                        trashPath = trashFile.toPath();
                        num++;
                    }
                }
                
                // Move file to trash.
                Files.move(srcPath, trashPath, REPLACE_EXISTING);
                return true;
            }
            else
                return false;
        }
        catch (IOException ex) {throw ex;}
    }
    
    public static boolean copyFileToTrash(File srcFile, File trashFile) throws IOException {
        try {
            if (srcFile.getPath().isEmpty() || trashFile.getPath().isEmpty())
                return false;
            
            if (srcFile.exists()) {
                Path srcPath = srcFile.toPath();
                Path trashPath = trashFile.toPath();
                
                // Create trash directory if necessary.
                File trashDir = trashFile.getParentFile();
                if (!trashDir.exists())
                    trashDir.mkdirs();

                // Check if trashPath already exists. If so, attach an increment
                // number to the filename. Iterate until first filename with 
                // incremented number is not found.
                if (trashFile.exists()) {
                    String origFileName = trashFile.getName();
                    String rx = "(?i)(^.*)(.V[0123][cC]?$)";
                    Pattern pattern = Pattern.compile(rx);
                    Matcher m = pattern.matcher(origFileName);
                    if (!m.find())
                        return false;

                    int num = 2;
                    while (trashFile.exists()) {
                        String newFileName = String.format("%s(%d)%s", m.group(1),num,m.group(2));
                        trashFile = Paths.get(trashFile.getParent(),newFileName).toFile();
                        trashPath = trashFile.toPath();
                        num++;
                    }
                }
                
                // Move file to trash.
                Files.copy(srcPath, trashPath, REPLACE_EXISTING);
                return true;
            }
            else
                return false;
        }
        catch (IOException ex) {throw ex;}
    }
    
    public static String getFileExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        
        if (i > 0) {
            return name.substring(i+1);
        }
        else
            return "";
    }
}
