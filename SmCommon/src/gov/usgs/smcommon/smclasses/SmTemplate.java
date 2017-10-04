/*******************************************************************************
 * Name: Java class SmTemplate.java
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

import java.io.File;

/**
 *
 * @author png-pr
 */
public class SmTemplate {
    private File templateFile = null;
    private SmStation smStation = null;
    
    public SmTemplate() {}
    
    public SmTemplate(File templateFile) {
        this.templateFile = templateFile;
    }
    
    public SmTemplate(SmStation smStation) {
        this.smStation = smStation;
    }
    
    public SmTemplate(File templateFile, SmStation smStation) {
        this.templateFile = templateFile;
        this.smStation = smStation;
    }
    
    // Copy constructor
    public SmTemplate(SmTemplate smTemplate)
    {
        this.templateFile = new File(smTemplate.getTemplateFile().getPath());
        this.smStation = new SmStation(smTemplate.getSmStation());
    }
    
    public File getTemplateFile() {
        return this.templateFile;
    }
    
    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public SmStation getSmStation() {return this.smStation;}
    
    public void setSmStation(SmStation smStation2) {this.smStation = smStation;}
    
    @Override
    public String toString() {
        return templateFile.getName();
    }
}
