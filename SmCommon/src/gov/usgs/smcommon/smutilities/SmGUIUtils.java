/*******************************************************************************
 * Name: Java class SmGUIUtils.java
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

import com.sun.glass.events.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultEditorKit;

/**
 *
 * @author png
 */
public class SmGUIUtils {
    
    public static JPopupMenu createCCPPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setLabel("Edit");
        popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));

        JMenuItem item;
        item = new JMenuItem(new DefaultEditorKit.CutAction());
        item.setText("Cut");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/cut_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_X);
        popupMenu.add(item);
        
        item = new JMenuItem(new DefaultEditorKit.CopyAction());
        item.setText("Copy");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/copy_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_C);
        popupMenu.add(item);
        
        item = new JMenuItem(new DefaultEditorKit.PasteAction());
        item.setText("Paste");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/paste_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_V);
        popupMenu.add(item);
        
        return popupMenu;
    }
    
    public static JPopupMenu createEditPopupMenu(UndoHandler undoHandler) {
        
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setLabel("Edit");
        popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));

        JMenuItem item;
        item = new JMenuItem(new DefaultEditorKit.CutAction());
        item.setText("Cut");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/cut_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_X);
        popupMenu.add(item);
        
        item = new JMenuItem(new DefaultEditorKit.CopyAction());
        item.setText("Copy");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/copy_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_C);
        popupMenu.add(item);
        
        item = new JMenuItem(new DefaultEditorKit.PasteAction());
        item.setText("Paste");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/paste_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_V);
        popupMenu.add(item);
        
        popupMenu.addSeparator();
        
        item = new JMenuItem(undoHandler.getUndoAction());
        item.setText("Undo");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/undo_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_Z);
        popupMenu.add(item);
        
        item = new JMenuItem(undoHandler.getRedoAction());
        item.setText("Redo");
        item.setIcon(new javax.swing.ImageIcon(SmGUIUtils.class.getClassLoader().getResource("resources/icons/redo_16.png")));
        item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_Y);
        popupMenu.add(item);
        
        return popupMenu;
    }
    
}
