/*******************************************************************************
 * Name: Java class SmNodeExplorerTC.java
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

package gov.usgs.smapp.smtopcomponents;

import static SmConstants.VFileConstants.*;
import gov.usgs.smapp.SmCore;
import gov.usgs.smapp.smforms.SmSeismicTraceEditor;
import gov.usgs.smcommon.smclasses.SmFile;
import gov.usgs.smcommon.smclasses.SmGlobal;
import gov.usgs.smcommon.smclasses.SmNode;
import gov.usgs.smcommon.smclasses.SmRec;
import gov.usgs.smcommon.smclasses.SmTemplate;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//gov.usgs.smapp.smtopcomponents//SmNodeExplorerTC//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SmNodeExplorerTC",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "gov.usgs.smapp.smtopcomponents.SmNodeExplorerTC")
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
@ActionReference(path = "Menu/Window", position = 100)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SmNodeExplorerTCAction",
        preferredID = "SmNodeExplorerTC"
)
@Messages({
    "CTL_SmNodeExplorerTCAction=Node Explorer",
    "CTL_SmNodeExplorerTC=Node Explorer",
    "HINT_SmNodeExplorerTC=Node Explorer"
})
public final class SmNodeExplorerTC extends TopComponent {

    private MyCheckboxTree treeContents;
    private final InstanceContent content = new InstanceContent();
    
    private final TreeCheckingListener treeCheckingListener =
        new TreeCheckingListener() {

        @Override
        public void valueChanged(TreeCheckingEvent e) {
            SmNode node = (SmNode)e.getPath().getLastPathComponent();
            
            if (node == null)
                return;
            
            SmCore.setSmNodeCheckedState(node, e.isCheckedPath());
        }
    };
        
    private final TreeSelectionListener treeSelectionListener =
        new TreeSelectionListener() {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            SmNode node = (SmNode)treeContents.getLastSelectedPathComponent();

            if (node == null)
                return;

            if (node.isLeaf()) {
                String filePath = node.getFilePath();
                content.set(Collections.singleton(filePath),null);
            }
        }
    };
    
    public SmNodeExplorerTC() {
        initComponents();
        setName(Bundle.CTL_SmNodeExplorerTC());
        setToolTipText(Bundle.HINT_SmNodeExplorerTC());

        // Create treeContents CheckboxTree control.
        createCheckboxTreeControl();
        
        // Setup treeContents control.
        setupCheckboxTreeControl();
        
        // Associate the lookup of content with lookup of top component.
        associateLookup(new AbstractLookup(content));
    }
    
    public CheckboxTree getCheckboxTree() {
        return this.treeContents;
    }
    
    public void setTreeListeners(boolean add) {
        if (add) {
            this.treeContents.getCheckingModel().addTreeCheckingListener(treeCheckingListener);
            this.treeContents.getSelectionModel().addTreeSelectionListener(treeSelectionListener);
        }
        else {
            this.treeContents.getCheckingModel().removeTreeCheckingListener(treeCheckingListener);
            this.treeContents.getSelectionModel().removeTreeSelectionListener(treeSelectionListener);
        }
    }
    
    private void createCheckboxTreeControl()
    {
        treeContents = new MyCheckboxTree();
        treeContents.setModel(new DefaultTreeModel(null,true));
        treeContents.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);
        treeContents.setCellRenderer(new MyCheckBoxTreeCellRenderer());
        
        JScrollPane scrollpaneContents = new JScrollPane(treeContents);
//        scrollpaneContents.setViewportView(treeContents);
        scrollpaneContents.setBorder(BorderFactory.createEmptyBorder());
       
        setLayout(new BorderLayout());
        add(scrollpaneContents, BorderLayout.CENTER);
    }
    
    private void setupCheckboxTreeControl()
    {
//        // Adjust tree model to set a node as a leaf node only if that node was
//        // set to not allow children.
//        DefaultTreeModel model = (DefaultTreeModel)treeContents.getModel();
//        model.setAsksAllowsChildren(true);
        
        setTreeListeners(true);
    }

    private class MyCheckBoxTreeCellRenderer implements CheckboxTreeCellRenderer 
    {
        JCheckBox checkbox = new JCheckBox();
        JLabel label = new JLabel();
        JPanel panel = new JPanel();
        
        public MyCheckBoxTreeCellRenderer() 
        {
            label.setFocusable(true);
            label.setOpaque(true);
            
            checkbox.setMargin(new Insets(0,0,0,0));
            
            panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            panel.add(checkbox);
            panel.add(label);
            
            label.setBackground(UIManager.getColor("Tree.textBackground"));
            checkbox.setBackground(UIManager.getColor("Tree.textBackground"));
            panel.setBackground(UIManager.getColor("Tree.textBackground"));
        }
        
        @Override
        public boolean isOnHotspot(int x, int y)
        {
            return checkbox.getBounds().contains(x,y);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            label.setText(value.toString());
            
            if (selected)
                label.setBackground(UIManager.getColor("Tree.selectionBackground"));
            else
                label.setBackground(UIManager.getColor("Tree.textBackgound"));
            
            TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
            
            TreePath path = tree.getPathForRow(row);
            
            boolean enabled = checkingModel.isPathEnabled(path);
            boolean checked = checkingModel.isPathChecked(path);
//            boolean grayed = checkingModel.isPathGreyed(path);
            
            checkbox.setEnabled(enabled);
            
//            if (grayed)
//                label.setForeground(Color.lightGray);
//            else
//                label.setForeground(Color.black);
            
            checkbox.setSelected(checked);
            
            return panel;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    class MyCheckboxTree extends CheckboxTree {
        
        private final JPopupMenu popup;
        private final JMenuItem menuItem;
        private final MyCheckboxTree checkboxTree = this;
        private SmNode nodePopup;
        
        public MyCheckboxTree() {
            
            // Create popup menu.
            popup = new JPopupMenu();
            menuItem = new JMenuItem("Edit");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                       
                        // Fetch selected file.
                        File file = new File(nodePopup.getFilePath());
                        SmFile smFile = new SmFile(file);
                        
                        if (smFile.getFileDataType().equals(UNCORACC) ||
                            smFile.getFileDataType().equals(CORACC)) {
                            // Check if data unit code is cm/sec squared.
                            SmRec smRec = smFile.getSmRecs().get(0);
                            if (smRec.getDataUnitCode() != 4) {
                                JOptionPane.showMessageDialog(null,
                                "Editing only allowed on acceleration data in cm/sec" + "\u00B2" + ".",
                                "Invalid Data Unit",
                                JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        else if (smFile.getFileDataType().equals(VELOCITY)) {
                            // Check if data unit code is cm/sec.
                            SmRec smRec = smFile.getSmRecs().get(0);
                            if (smRec.getDataUnitCode() != 5) {
                                JOptionPane.showMessageDialog(null,
                                "Editing only allowed on velocity data in cm/sec.",
                                "Invalid Data Unit",
                                JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        else if (smFile.getFileDataType().equals(DISPLACE)) {
                            // Check if data unit code is cm.
                            SmRec smRec = smFile.getSmRecs().get(0);
                            if (smRec.getDataUnitCode() != 6) {
                                JOptionPane.showMessageDialog(null,
                                "Editing only allowed on displacement data in cm.",
                                "Invalid Data Unit",
                                JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        
                        // Get selected template.
                        SmTemplate smTemplate = SmCore.getSelectedSmTemplate();
                        
                        // Open editor.
                        SmSeismicTraceEditor chartEditor = new SmSeismicTraceEditor(SmGlobal.SM_CHARTS_API_QCCHART2D,
                            smFile,smTemplate);
                        chartEditor.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                        chartEditor.pack();
                        chartEditor.setVisible(true);
                    }
                    catch (Exception ex) {
                        SmCore.addMsgToStatusViewer("Error: " + ex.getMessage());
                    }
                }
            });
            popup.add(menuItem);
            
            // Add listeners.
            addMouseListener(new MousePopupListener());
        }
        
        class MousePopupListener extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {         
                    Point p = e.getPoint();
                    TreePath path = checkboxTree.getPathForLocation(p.x, p.y);
                    if (path != null) {
                        nodePopup = (SmNode)path.getLastPathComponent();
                    }
                    popup.show((JComponent)e.getSource(),e.getX(),e.getY());
                }
            }
        }
    }
    
}
