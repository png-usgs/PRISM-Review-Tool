/*******************************************************************************
 * Name: Java class NetBeansUtils.java
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author png
 */
@SuppressWarnings({"cast","rawtypes","unchecked"})
public class NetBeansUtils {
    
    public static TopComponent findTopComponent(String tcName) {
        Set<TopComponent> openTopComponents =
            WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : openTopComponents) {
            if (tc.getName().equals(tcName)) {
                return tc;
            }
        }
        
        return null;
    }
    
    public static<T> T findTopComponent(Class<T> type) {
        Set<TopComponent> openTopComponents =
            WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : openTopComponents) {
            Class tcClass = tc.getClass();
            if (type.isAssignableFrom(tcClass))
                return type.cast(tc);   
        }
        
        return null;
    }
    
    public static FileObject getFileObject(String folderName, String className) {
        FileObject[] fileObjects = FileUtil.getConfigFile(folderName).getChildren();
        List<FileObject> orderedFileObjects = 
            FileUtil.getOrder(Arrays.asList(fileObjects), true);
        for (FileObject fileObject : orderedFileObjects) {
            if (fileObject.getAttribute("displayName").equals(className)) {
                return fileObject;
            }
        }
        return null;
    }
    
    public static<T> T getInstanceClass(String folderName, String className,
        Class<T> type) {
        FileObject[] fileObjects = FileUtil.getConfigFile(folderName).getChildren();
        List<FileObject> orderedFileObjects = 
            FileUtil.getOrder(Arrays.asList(fileObjects), true);
        for (FileObject fileObject : orderedFileObjects) {
            if (fileObject.getAttribute("displayName").equals(className)) {
                return (T)FileUtil.getConfigObject(fileObject.getPath(), type);
            }
        }
        return null;
    }
    
    
    /** 
     * Retrieves an action instance 
     * @param category e.g., "Maps" 
     * @param id e.g., "com-emxsys-worldwind-ribbon-actions-ToggleLayerAction" 
     * @return the Action instance or null 
     */ 
    public static Action getAction(String category, String id) 
    { 
        final String folder = "Actions/" + category + "/"; 
        Lookup pathLookup = Lookups.forPath(folder); 
        
        Template<Action> actionTemplate = 
            new Template<Action>(Action.class, folder + id, null); 
        
        Item<Action> item = pathLookup.lookupItem(actionTemplate); 
        if (item != null) 
        { 
            return item.getInstance(); 
        } 
        return null; 
    } 
    
    public static Action getAction2(String category, String displayName)
    {
        FileObject foActions[] =
            FileUtil.getConfigFile("Actions/"+category).getChildren();
        for (FileObject foAction : foActions) {
            if (foAction.getAttribute("displayName").equals(displayName))
                return FileUtil.getConfigObject(foAction.getPath(), Action.class);
        }
        return null;
    }
    
    public static Object showMessage(String title, String msg,
        int optionType, int msgType) {
        NotifyDescriptor d = new NotifyDescriptor(
            msg, // message
            title, // title
            optionType, // option type (e.g., NotifyDescriptor.OK_CANCEL_OPTION)
            msgType, // message type (e.g., NotifyDescriptor.INFORMATION_MESSAGE)
            null, // own buttons as Object[]
            null); // initial value
        
        return DialogDisplayer.getDefault().notify(d);
    }
    
    public static <T> T typeCheck(Object obj, Class<T> type, String name)
      throws ClassCastException {
        Class objClass = obj.getClass();
        if (! type.isAssignableFrom(objClass)) {
            throw new ClassCastException("invalid type " +
              objClass.getSimpleName() + " for " + name + ", should be " +
              type.getSimpleName());
        }
        return type.cast(obj);
    }
    
}
