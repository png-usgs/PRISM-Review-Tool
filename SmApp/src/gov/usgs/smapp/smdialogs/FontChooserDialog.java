/*******************************************************************************
 * Name: Java class FontChooserDialog.java
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

package gov.usgs.smapp.smdialogs;

// FontChooser.java
// A font chooser that allows users to pick a font by name, size, style, and
// color.  The color selection is provided by a JColorChooser pane.  This
// dialog builds an AttributeSet suitable for use with JTextPane.
//
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class FontChooserDialog extends JDialog implements ActionListener {

    private SimpleAttributeSet fontAttributes;
//    private JColorChooser colorChooser;
    private JComboBox fontName;
    private JCheckBox fontBold, fontItalic;
    private JComboBox fontSize;
    private JLabel previewLabel;

    private Font newFont;
//    private Color newColor;

    public FontChooserDialog(Frame parent, SimpleAttributeSet fontAttributes) {
        super(parent, "Font Chooser", true);
        initFontChooser(parent,fontAttributes);
    }

    public FontChooserDialog(Dialog parent, SimpleAttributeSet fontAttributes) {
        super(parent, "Font Chooser", true);
        initFontChooser(parent,fontAttributes);
    }
    
    public Font getNewFont() { return newFont; }
//    public Color getNewColor() { return newColor; }
    public SimpleAttributeSet getFontAttributes() { return fontAttributes; }

    private void initFontChooser(Window parent, SimpleAttributeSet fontAttributes)
    {
//        setSize(450, 450);
        setSize(450,200);
        setLocationRelativeTo(parent);

        // Make sure that any way the user cancels the window does the right thing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAndCancel();
            }
        });

        this.fontAttributes = (SimpleAttributeSet)fontAttributes.copyAttributes();

        // Start the long process of setting up our interface
        Container c = getContentPane();

        JPanel fontPanel = new JPanel();

        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = gEnv.getAllFonts();
        //    fontName = new JComboBox(new String[] {"TimesRoman", 
        //        "Helvetica", "Courier", "SansSerif"});
        fontName = new JComboBox();
        for (Font font : fonts)
            fontName.addItem(font.getName());
        fontName.setSelectedItem(StyleConstants.getFontFamily(fontAttributes));
        fontName.addActionListener(this);

    //    fontSize = new JTextField("12", 4);
    //    fontSize.setHorizontalAlignment(SwingConstants.RIGHT);
        fontSize = new JComboBox(new Integer[] {3,5,8,10,12,14,18,24,36,48});
        fontSize.setSelectedItem(StyleConstants.getFontSize(fontAttributes));
        fontSize.addActionListener(this);
        
        fontBold = new JCheckBox("Bold");
        fontBold.setSelected(StyleConstants.isBold(fontAttributes));
        fontBold.addActionListener(this);
        
        fontItalic = new JCheckBox("Italic");
        fontItalic.setSelected(StyleConstants.isItalic(fontAttributes));
        fontItalic.addActionListener(this);

        fontPanel.add(fontName);
        fontPanel.add(new JLabel(" Size: "));
        fontPanel.add(fontSize);
        fontPanel.add(fontBold);
        fontPanel.add(fontItalic);

        c.add(fontPanel, BorderLayout.NORTH);

//        // Set up the color chooser panel and attach a change listener so that color
//        // updates get reflected in our preview label.
//        colorChooser = new JColorChooser(new Color((int)fontAttributes.getAttribute("FontColor")));
//        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                updatePreviewColor();
//            }
//        });
//        c.add(colorChooser, BorderLayout.CENTER);

        // Create preview panel.
        JPanel previewLabelPanel = new JPanel();
        previewLabel = new JLabel("Here's a sample of this font.");
        previewLabel.setFont(new Font(fontName.getSelectedItem().toString(), 
            (fontBold.isSelected() ? Font.BOLD : 0) +
            (fontItalic.isSelected() ? Font.ITALIC : 0), 
            Integer.parseInt(fontSize.getSelectedItem().toString())));
//        previewLabel.setForeground(colorChooser.getColor());
        previewLabelPanel.add(previewLabel);

        // Add in the Ok and Cancel buttons for our dialog box
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                closeAndSave();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                closeAndCancel();
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(okButton);
        controlPanel.add(cancelButton);
        
        JPanel previewPanel = new JPanel(new BorderLayout());
//        previewPanel.add(previewLabel, BorderLayout.CENTER);
        previewPanel.add(previewLabelPanel, BorderLayout.CENTER);
        previewPanel.add(controlPanel, BorderLayout.SOUTH);

        // Give the preview label room to grow.
        previewPanel.setMinimumSize(new Dimension(100, 100));
        previewPanel.setPreferredSize(new Dimension(100, 100));

        c.add(previewPanel, BorderLayout.SOUTH);
    }
  
    // Ok, something in the font changed, so figure that out and make a
    // new font for the preview label
    @Override
    public void actionPerformed(ActionEvent ae) {
        // Check the name of the font
        if (!StyleConstants.getFontFamily(fontAttributes)
            .equals(fontName.getSelectedItem())) 
        {
            StyleConstants.setFontFamily(fontAttributes,(String)fontName.getSelectedItem());
        }

        // Check the font size (no error checking yet)
        if (StyleConstants.getFontSize(fontAttributes) != 
            Integer.parseInt(fontSize.getSelectedItem().toString())) 
        {
            StyleConstants.setFontSize(fontAttributes,
                Integer.parseInt(fontSize.getSelectedItem().toString()));
        }

        // Check to see if the font should be bold
        if (StyleConstants.isBold(fontAttributes) != fontBold.isSelected()) 
        {
            StyleConstants.setBold(fontAttributes,fontBold.isSelected());
        }

        // Check to see if the font should be italic
        if (StyleConstants.isItalic(fontAttributes) != fontItalic.isSelected()) 
        {
            StyleConstants.setItalic(fontAttributes,fontItalic.isSelected());
        }

        // and update our preview label
        updatePreviewFont();
    }

    // Get the appropriate font from our fontAttributes object and update
    // the preview label
    protected void updatePreviewFont() {
        String name = StyleConstants.getFontFamily(fontAttributes);
        boolean bold = StyleConstants.isBold(fontAttributes);
        boolean ital = StyleConstants.isItalic(fontAttributes);
        int size = StyleConstants.getFontSize(fontAttributes);

        //Bold and italic donâ€™t work properly in beta 4.
        Font f = new Font(name, (bold ? Font.BOLD : 0) +
            (ital ? Font.ITALIC : 0), size);
        previewLabel.setFont(f);
    }

    // Get the appropriate color from our chooser and update previewLabel
    protected void updatePreviewColor() {
//        fontAttributes.addAttribute("FontColor", colorChooser.getColor().getRGB());
//        previewLabel.setForeground(new Color((int)fontAttributes.getAttribute("FontColor")));
        // Manually force the label to repaint
        previewLabel.repaint();
    }

    public void closeAndSave() {
        // Save font & color information
        newFont = previewLabel.getFont();
//        newColor = previewLabel.getForeground();

        // Close the window
        setVisible(false);
    }

    public void closeAndCancel() {
        // Erase any font information and then close the window
        newFont = null;
//        newColor = null;
        fontAttributes = null;
        System.out.println("fontAttributes set to null");
        setVisible(false);
    }
}
