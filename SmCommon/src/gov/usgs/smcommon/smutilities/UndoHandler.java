/*******************************************************************************
 * Name: Java class UndoHandler.java
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author png-pr
 */
public class UndoHandler extends UndoManager {
    private final UndoAction undoAction;
    private final RedoAction redoAction;
    
    
    public UndoHandler() {
        this.undoAction = new UndoAction(this);
        this.redoAction = new RedoAction(this);

        this.undoAction.setRedoAction(redoAction);
        this.redoAction.setUndoAction(undoAction);
    }
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        this.addEdit(e.getEdit());
        undoAction.update();
        redoAction.update();
    }
    
    public UndoAction getUndoAction() {return this.undoAction;}
    public RedoAction getRedoAction() {return this.redoAction;}
    
    public void reset() {
        discardAllEdits();
        
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);
        
        //undoAction.putValue(Action.NAME, "Undo");
        //redoAction.putValue(Action.NAME, "Redo");
    }
    
    public class UndoAction extends AbstractAction {
        
        private final UndoManager undoManager;
        private RedoAction redoAction = null;
        
        public UndoAction(UndoManager undoManager) {
            super("Undo");
            setEnabled(false);
            
            this.undoManager = undoManager;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            }
            catch (CannotUndoException ex) {
            }
            
            this.update();
            if (redoAction != null)
                redoAction.update();
        }
        
        protected void update() {
            if (undoManager.canUndo()) {
                setEnabled(true);
                //putValue(Action.NAME, undoManager.getUndoPresentationName());
            }
            else {
                setEnabled(false);
                //putValue(Action.NAME, "Undo");
            }
        }
        
        public void setRedoAction(RedoAction redoAction) {
            this.redoAction = redoAction;
        }
    }
    
    public class RedoAction extends AbstractAction {

        private final UndoManager undoManager;
        private UndoAction undoAction = null;
        
        public RedoAction(UndoManager undoManager) {
            super("Redo");
            setEnabled(false);
            
            this.undoManager = undoManager;
        }
        
        public void setUndoAction(UndoAction undoAction) {
            this.undoAction = undoAction;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.redo();
            }
            catch (CannotRedoException ex) {
            }
            
            this.update();
            if (undoAction != null)
                undoAction.update();
        }
        
        protected void update() {
            if (undoManager.canRedo()) {
                setEnabled(true);
                //putValue(Action.NAME,undoManager.getRedoPresentationName());
            }
            else {
                setEnabled(false);
                //putValue(Action.NAME,"Redo");
            }
        }
    }
}
