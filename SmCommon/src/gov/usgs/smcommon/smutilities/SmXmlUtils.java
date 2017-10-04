/*******************************************************************************
 * Name: Java class SmXmlUtils.java
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
import gov.usgs.smcommon.smclasses.SmStation;
import gov.usgs.smcommon.smclasses.SmTemplate;
import gov.usgs.smcommon.smutilities.SmXmlUtils.SmConfiguration.FilterDefaults;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author png
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SmXmlUtils {
    
    public static Boolean isXML_FileParsible(String filePath) {
        try
        {
            File file = Paths.get(filePath).toFile();
        
            if (!file.exists())
                return false;
        
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
        }
        catch (ParserConfigurationException | IOException | SAXException ex)
        {
            return false;
        }
        
        return true;
    }
    
    public static SmTemplate readXML_TemplateFile(String filePath)
    {
        File file = Paths.get(filePath).toFile();
        
        if (!file.exists())
            return null;
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // Get Station element.
            Element elementStation = (Element)doc.getElementsByTagName("Station").item(0);
           
            if (elementStation == null)
                return null;

            String stationCode = elementStation.getElementsByTagName("Station_Code").item(0).getTextContent();
            String description = elementStation.getElementsByTagName("Description").item(0).getTextContent();
            String networkCode = elementStation.getElementsByTagName("Network_Code").item(0).getTextContent();
            //JOptionPane.showMessageDialog(null, "Station Code: " + stationCode);
            
            // Get Epochs element.
            Element elementEpochs = (Element)elementStation.getElementsByTagName("Epochs").item(0);
            
            if (elementEpochs == null)
                return null;
            
            NodeList nodelistEpochs = elementEpochs.getElementsByTagName("Epoch");
        
            if (nodelistEpochs == null || nodelistEpochs.getLength() == 0)
                return null;
            
            // Create list of SmEpoch objects.
            ArrayList<SmEpoch> smEpochs = new ArrayList();
            
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
            
            for (int i=0;i<nodelistEpochs.getLength();i++)
            {
                Element elementEpoch = (Element)nodelistEpochs.item(i);
            
                DateTime startUTC = new DateTime(fmt.parseDateTime(elementEpoch.getAttribute("start_utc")));
                DateTime endUTC = new DateTime(fmt.parseDateTime(elementEpoch.getAttribute("end_utc")));

                // Get Channels element.
                Element elementChannels = (Element)elementEpoch.getElementsByTagName("Channels").item(0);

                if (elementChannels == null)
                    return null;

                NodeList nodelistChannels = elementChannels.getElementsByTagName("Channel");

                if (nodelistChannels == null || nodelistChannels.getLength() == 0)
                    return null;
                
                // Create list of SmChannel objects.
                ArrayList<SmChannel> smChannels = new ArrayList();
                for (int j=0;j<nodelistChannels.getLength();j++)
                {
                    Element elementChannel = (Element)nodelistChannels.item(j);

                    String seed = elementChannel.getElementsByTagName("Seed").item(0).getTextContent();
                    String lCode = elementChannel.getElementsByTagName("L_Code").item(0).getTextContent();
                    String location = elementChannel.getElementsByTagName("Location").item(0).getTextContent();
                    String azimuth = elementChannel.getElementsByTagName("Azimuth").item(0).getTextContent();
                    String inclination = elementChannel.getElementsByTagName("Inclination").item(0).getTextContent();

                    Element elementColor = (Element)elementChannel.getElementsByTagName("Color").item(0);
                    int red = Integer.parseInt(elementColor.getAttribute("r"));
                    int green = Integer.parseInt(elementColor.getAttribute("g"));
                    int blue = Integer.parseInt(elementColor.getAttribute("b"));

                    Color color = new Color(red,green,blue);

                    // Add SmChannel object to SmChannel objects list.
                    smChannels.add(new SmChannel(seed,lCode,location,azimuth,inclination,color));
                }
                
                // Add SmEpoch object to SmEpoch objects list.
                smEpochs.add(new SmEpoch(startUTC,endUTC,smChannels));
            }
            
            // Create SmTemplate object.
            return new SmTemplate(file, new SmStation(stationCode,
                description,networkCode,smEpochs));
        }
        catch (ParserConfigurationException | IOException | SAXException ex)
        {
            return null;
        }
    }
    
    public static void writeXML_SmConfigFile(String filePath, SmConfiguration smConfig)
    {
        try
        {
            Path parentPath = Paths.get(filePath).getParent();
            File parentDir = parentPath.toFile();
            
            if (!parentDir.exists())
            {
                try
                {
                    parentDir.mkdirs();
                }
                catch (SecurityException ex)
                {
                    System.out.println(ex.getMessage());
                }
            }
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Root element
            Document doc = docBuilder.newDocument();
            Element elementRoot = doc.createElement("SM_Graphing_Tool");
            doc.appendChild(elementRoot);

            // Settings element
            Element elementSettings = doc.createElement("Settings");
            elementRoot.appendChild(elementSettings);

            // Events Root Directory element
            Element elementEventsRootDir = doc.createElement("Events_Root_Dir");
            elementEventsRootDir.appendChild(doc.createTextNode(smConfig.getEventsRootDir()));
            elementSettings.appendChild(elementEventsRootDir);
            
            // Templates Directory element
            Element elementTemplatesDir = doc.createElement("Templates_Dir");
            elementTemplatesDir.appendChild(doc.createTextNode(smConfig.getTemplatesDir()));
            elementSettings.appendChild(elementTemplatesDir);
            
            // Last Vx Directory element
            Element elementLastVxDir = doc.createElement("Last_Vx_Dir");
            elementLastVxDir.appendChild(doc.createTextNode(smConfig.getLastVxDir()));
            elementSettings.appendChild(elementLastVxDir);
            
            // SmChartEditor specific settings.
            Element elementSmChartEditor = doc.createElement("SmChartEditor");
            elementRoot.appendChild(elementSmChartEditor);
            
            // Filter defaults.
            Element elementFilterDefaults = doc.createElement("FilterDefaults");
            elementSmChartEditor.appendChild(elementFilterDefaults);
            
            // Seismic (Time Series).
            Element elementSeismic = doc.createElement("Seismic");
            elementFilterDefaults.appendChild(elementSeismic);
            
            // Start time.
            Element elementStartTime = doc.createElement("StartTime");
            elementStartTime.appendChild(doc.createTextNode(
                String.valueOf(smConfig.getFilterDefaults().getSeismicStartTime())));
            elementSeismic.appendChild(elementStartTime);
            
            // End time.
            Element elementEndTime = doc.createElement("EndTime");
            elementEndTime.appendChild(doc.createTextNode(
                String.valueOf(smConfig.getFilterDefaults().getSeismicEndTime())));
            elementSeismic.appendChild(elementEndTime);
            
            // Spectral (Frequency).
            Element elementSpectral = doc.createElement("Spectral");
            elementFilterDefaults.appendChild(elementSpectral);
            
            // Low frequency.
            Element elementLowFreq = doc.createElement("LowFreq");
            elementLowFreq.appendChild(doc.createTextNode(
                String.valueOf(smConfig.getFilterDefaults().getSpectralLowFreq())));
            elementSpectral.appendChild(elementLowFreq);
            
            // High frequency.
            Element elementHighFreq = doc.createElement("HighFreq");
            elementHighFreq.appendChild(doc.createTextNode(
                String.valueOf(smConfig.getFilterDefaults().getSpectralHighFreq())));
            elementSpectral.appendChild(elementHighFreq);
            
            // Write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

    //        System.out.println("File saved!");
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } 
        catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
        
    }
    
    public static SmConfiguration readXML_SmConfigFile(String filePath)
    {
        File file = Paths.get(filePath).toFile();
        
        if (!file.exists())
            return null;
        
        SmConfiguration smConfig = new SmConfiguration();
        FilterDefaults filterDefaults = smConfig.getFilterDefaults();
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // Get Settings node.
            NodeList argsList = doc.getElementsByTagName("Settings");
            
            if (argsList.getLength() != 1)
                return null;
            
            Node argsNode = argsList.item(0);
            
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            
            Element argsElement = (Element)argsNode;
            
            smConfig.setEventsRootDir(argsElement.getElementsByTagName("Events_Root_Dir").item(0).getTextContent());
            smConfig.setTemplatesDir(argsElement.getElementsByTagName("Templates_Dir").item(0).getTextContent());
            smConfig.setLastVxDir(argsElement.getElementsByTagName("Last_Vx_Dir").item(0).getTextContent());
            
            // Get SmChartEditor node.
            argsList = doc.getElementsByTagName("SmChartEditor");
            if (argsList.getLength() != 1)
                return null;
            argsNode = argsList.item(0);
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            argsElement = (Element)argsNode;
            
            // Get FilterDefaults node within SmChartEditor node.
            argsList = argsElement.getElementsByTagName("FilterDefaults");
            if (argsList.getLength() != 1)
                return null;
            argsNode = argsList.item(0);
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            argsElement = (Element)argsNode;
            
            // Parse FilterDefaults node to get text nodes.
            String seismicStartTimeStr = 
                argsElement.getElementsByTagName("StartTime").item(0).getTextContent();
            String seismicEndTimeStr =
                argsElement.getElementsByTagName("EndTime").item(0).getTextContent();
            String spectralLowFreqStr = 
                argsElement.getElementsByTagName("LowFreq").item(0).getTextContent();
            String spectralHighFreqStr =
                argsElement.getElementsByTagName("HighFreq").item(0).getTextContent();
            
            try
            {
                filterDefaults.setSeismicStartTime(Double.parseDouble(seismicStartTimeStr));
                filterDefaults.setSeismicEndTime(Double.parseDouble(seismicEndTimeStr));
                filterDefaults.setSpectralLowFreq(Double.parseDouble(spectralLowFreqStr));
                filterDefaults.setSpectralHighFreq(Double.parseDouble(spectralHighFreqStr));
            }
            catch (NumberFormatException ex)
            {
                System.out.println(ex.getMessage());
            }
            
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return smConfig;
    }
    
    public static String getEventsRootDir(String filePath)
    {
        File file = Paths.get(filePath).toFile();
        
        if (!file.exists())
            return null;
        
        String eventsRootDir = "";
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList argsList = doc.getElementsByTagName("Settings");
            
            if (argsList.getLength() != 1)
                return null;
            
            Node argsNode = argsList.item(0);
            
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            
            Element argsElement = (Element)argsNode;
            
            eventsRootDir = argsElement.getElementsByTagName("Events_Root_Dir").item(0).getTextContent();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return eventsRootDir;
    }
    
    public static String getTemplatesDir(String filePath)
    {
        File file = Paths.get(filePath).toFile();
        
        if (!file.exists())
            return null;
        
        String templatesDir = "";
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList argsList = doc.getElementsByTagName("Settings");
            
            if (argsList.getLength() != 1)
                return null;
            
            Node argsNode = argsList.item(0);
            
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            
            Element argsElement = (Element)argsNode;
            
            templatesDir = argsElement.getElementsByTagName("Templates_Dir").item(0).getTextContent();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return templatesDir;
    }
    
    public static String getLastVxDir(String filePath)
    {
        File file = Paths.get(filePath).toFile();
        
        if (!file.exists())
            return null;
        
        String lastVxDir = "";
        
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList argsList = doc.getElementsByTagName("Settings");
            
            if (argsList.getLength() != 1)
                return null;
            
            Node argsNode = argsList.item(0);
            
            if (argsNode.getNodeType() != Node.ELEMENT_NODE)
                return null;
            
            Element argsElement = (Element)argsNode;
            
            lastVxDir = argsElement.getElementsByTagName("Last_Vx_Dir").item(0).getTextContent();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return lastVxDir;
    }
    
    public static class SmConfiguration {
        
        private String eventsRootDir = "";
        private String templatesDir = "";
        private String lastVxDir = "";
        private FilterDefaults filterDefaults = new FilterDefaults();
        
        
        public String getEventsRootDir()
        {
            return this.eventsRootDir;
        }
        
        public void setEventsRootDir(String eventsRootDir)
        {
            this.eventsRootDir = eventsRootDir;
        }
        
        public String getTemplatesDir()
        {
            return this.templatesDir;
        }
        
        public void setTemplatesDir(String templatesDir)
        {
            this.templatesDir = templatesDir;
        }
        
        public String getLastVxDir()
        {
            return this.lastVxDir;
        }
        
        public void setLastVxDir(String lastVxDir)
        {
            this.lastVxDir = lastVxDir;
        }
        
        public FilterDefaults getFilterDefaults()
        {
            return this.filterDefaults;
        }
        
        public void setFilterDefaults(FilterDefaults filterDefaults)
        {
            this.filterDefaults = filterDefaults;
        }
        
        public static class FilterDefaults {
            
            private double seismicStartTime = 0;
            private double seismicEndTime = 0;
            private double spectralLowFreq = 0;
            private double spectralHighFreq = 0;
            
            public double getSeismicStartTime()
            {
                return this.seismicStartTime;
            }
            
            public void setSeismicStartTime(double seismicStartTime)
            {
                this.seismicStartTime = seismicStartTime;
            }
            
            public double getSeismicEndTime()
            {
                return this.seismicEndTime;
            }
            
            public void setSeismicEndTime(double seismicEndTime)
            {
                this.seismicEndTime = seismicEndTime;
            }
            
             public double getSpectralLowFreq()
            {
                return this.spectralLowFreq;
            }
            
            public void setSpectralLowFreq(double spectralLowFreq)
            {
                this.spectralLowFreq = spectralLowFreq;
            }
            
            public double getSpectralHighFreq()
            {
                return this.spectralHighFreq;
            }
            
            public void setSpectralHighFreq(double spectralHighFreq)
            {
                this.spectralHighFreq = spectralHighFreq;
            }
        }
    }
}
