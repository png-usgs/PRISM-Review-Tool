/*******************************************************************************
 * Name: Java class MathUtils.java
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

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author png
 */
public class MathUtils {
    
    public static double round(double Rval, int Rpl) {
        double p = Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return tmp / p;
    }
    
    public static boolean isPowerOf2_V1(int number)
    {
        if ( (number & -number) == number)
            return true;
        else
            return false;
    }
    
    public static boolean isPowerOf2_V2(int number){
        int square = 1;
        while(number >= square){
            if(number == square){
                return true;
            }
            square = square*2;
        }
        return false;
    }
    
    public static boolean isPowerOf2_V3(int number){
         if(number <=0){
            throw new IllegalArgumentException("number: " + number);
        }
        return ((number & (number -1)) == 0);
    }
    
    public static int nextPowerOf2(int n)
    {
        int powOf2 = 1;
        while( powOf2 < n ) powOf2 <<= 1;
        
        return powOf2;
    }
    
    public static Complex[] applyFFT(double[] points)
    {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        
        return fft.transform(points, TransformType.FORWARD);
    }
    
    public static boolean isInt(String text) {
        boolean result = true;
        
        try {
            Integer.parseInt(text);
        }
        catch (NullPointerException | NumberFormatException ex) {
            result = false;
        }
        
        return result;
    }
    
    public static boolean isDouble(String text) {
        boolean result = true;
        
        try {
            Double.parseDouble(text);
        }
        catch (NullPointerException | NumberFormatException ex) {
            result = false;
        }
        
        return result;
    }
}
