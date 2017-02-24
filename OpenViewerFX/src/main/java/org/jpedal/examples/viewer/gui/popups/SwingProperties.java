/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2017 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * SwingProperties.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.popups;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.gui.CheckNode;
import org.jpedal.examples.viewer.gui.CheckRenderer;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.utils.ItextFunctions;
import org.jpedal.examples.viewer.utils.Printer;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.Speech;
import org.jpedal.objects.javascript.DefaultParser;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.*;
import org.jpedal.utils.SwingWorker;
import org.mozilla.javascript.ScriptRuntime;
import org.w3c.dom.NodeList;

public class SwingProperties extends JPanel {
    
    private final int TRUE_HASH = "true".hashCode();
    
    //Text to Speech external handler
    final Speech speech;
            
    final Map<String, String> reverseMessage =new HashMap<String, String>();
    
    //Array of menu tabs.
    final String[] menuTabs = {"ShowMenubar","ShowButtons","ShowDisplayoptions", "ShowNavigationbar", "ShowSidetabbar"};
    
    String propertiesLocation = "";
    
    PropertiesFile properties;
    
    //Window Components
    JDialog propertiesDialog;
    
    final JButton confirm = new JButton("OK");
    
    final JButton cancel = new JButton("Cancel");
    
    JTabbedPane tabs = new JTabbedPane();
    
    //Settings Fields Components
    
    //DPI viewer value
    JTextField resolution;
    
    //Search window display style
    JComboBox<String> searchStyle;
    
    //Update search results during search
    JCheckBox liveSearchResuts;
    
    //Show border around page
    JCheckBox border;
    
    //Use Hi Res Printing
    JCheckBox constantTabs;
    
    //Use enhanced viewer
    JCheckBox enhancedViewer;
    
    //Use enhanced viewer
    JCheckBox enhanceFractionalLines;
    
    //Use enhanced viewer
    JCheckBox enhancedFacing;
    
    //Use enhanced viewer
    JCheckBox thumbnailScroll;
    
    //Use right click functionality
    JCheckBox rightClick;
    
    //Allow scrollwheel zooming
    JCheckBox scrollwheelZoom;
    
    //perform automatic update check
    final JCheckBox update = new JCheckBox(Messages.getMessage("PdfPreferences.CheckForUpdate"));
    
    //max no of multiviewers
    JTextField maxMultiViewers;
    
    //inset value
    JTextField pageInsets;
    JLabel pageInsetsText;
    
    //window title
    JTextField windowTitle;
    JLabel windowTitleText;
    
    //Icon radio buttons
    JRadioButton newIconSet;
    JRadioButton classicIconSet;
    ButtonGroup skinGroup;
    static final String newIconLocation = "/org/jpedal/examples/viewer/res/new/";
    static final String classicIconLocation = "/org/jpedal/examples/viewer/res/";
    
    //icons Location
    JTextField iconLocation;
    JLabel iconLocationText;
    
    //Printer blacklist
    JTextField printerBlacklist;
    JLabel printerBlacklistText;
    
    //Default printer
    JComboBox<String> defaultPrinter;
    JLabel defaultPrinterText;
    
    //Default pagesize
    JComboBox<String> defaultPagesize;
    JLabel defaultPagesizeText;
    
    //Default resolution
    JTextField defaultDPI;
    JLabel defaultDPIText;
    
    JTextField sideTabLength;
    JLabel sideTabLengthText;
    
    //Use parented hinting functions
    JCheckBox useHinting;
    
    //Set autoScroll when mouse at the edge of page
    JCheckBox autoScroll;
    
    //Set whether to prompt user on close
    JCheckBox confirmClose;
    
    //Set if we should open the file at the last viewed page
    JCheckBox openLastDoc;
    
    //Set default page layout
    JComboBox<String> pageLayout = new JComboBox<String>(new String[]{"Single Page","Continuous","Continuous Facing", "Facing", "PageFlow"});
    
    //Speech Options
    JComboBox<String> voiceSelect;
    
    final JPanel highlightBoxColor = new JPanel();
    final JPanel viewBGColor = new JPanel();
    final JPanel pdfDecoderBackground = new JPanel();
    final JPanel foreGroundColor = new JPanel();
    final JCheckBox invertHighlight = new JCheckBox("Highlight Inverts Page");
    final JCheckBox replaceDocTextCol = new JCheckBox("Replace Document Text Colors");
    final JCheckBox replaceDisplayBGCol = new JCheckBox("Replace Display Background Color");
    
    final JCheckBox changeTextAndLineArt = new JCheckBox("Change Color of Text and Line art");
    final JCheckBox showMouseSelectionBox = new JCheckBox("Show Mouse Selection Box");
    final JTextField highlightComposite = new JTextField(String.valueOf(DecoderOptions.highlightComposite));
        
    private boolean preferencesSetup;
    
    private JButton clearHistory;
    
    private JLabel historyClearedLabel;
    
    /**
     * Gets the boolean value of a property and loads it into a JCheckBox
     *
     * @param comp JCheckbox to show the data.
     * @param elementName Property name to be loaded.
     */
    private void loadBooleanValue(final JCheckBox comp, final String elementName) {
        final String value = properties.getValue(elementName).toLowerCase();
        comp.setSelected(!value.isEmpty() && value.hashCode() == TRUE_HASH);
    }

    /**
     * Gets the boolean value of a property and loads it into a JCheckBox
     *
     * @param comp CheckNode to show the data.
     * @param elementName Property name to be loaded.
     */
    private void loadBooleanValue(final CheckNode comp, final String elementName) {
        final String value = properties.getValue(elementName).toLowerCase();
        comp.setSelected(!value.isEmpty() && value.hashCode() == TRUE_HASH);
    }

    /**
     * Gets the String value of a property and loads it into a JTextField
     *
     * @param comp JTextfield to show the data.
     * @param elementName Property name to be loaded.
     */
    private void loadStringValue(final JTextField comp, final String elementName) {
        final String propValue = properties.getValue(elementName);
        if (propValue != null && !propValue.isEmpty()) {
            comp.setText(propValue);
        }
    }

    /**
     * Gets the String value of a property and loads it into a JTextField
     *
     * @param comp JTextfield to show the data.
     * @param elementName Property name to be loaded.
     * @param defaultText Default text to be used if property value not found or
     * empty.
     */
    private void loadStringValue(final JTextField comp, final String elementName, final String defaultText) {
        final String propValue = properties.getValue(elementName);
        if (propValue != null && !propValue.isEmpty()) {
            comp.setText(propValue);
        } else {
            comp.setText(defaultText);
        }
    }

    /**
     * Dialog window to show properties options for the user interface
     * @param currentGUI GUIFactory object the properties dialog will be associated with
     */
    public SwingProperties(final GUIFactory currentGUI){
        speech = (Speech)currentGUI.getPdfDecoder().getExternalHandler(Options.SpeechEngine);
        showPreferenceWindow(currentGUI);
    }
    
    //Only allow numerical input to the field
    final KeyListener numericalKeyListener = new KeyListener(){
        
        boolean consume;
        
        @Override
        public void keyPressed(final KeyEvent e) {
            consume = (e.getKeyChar() < '0' || e.getKeyChar() > '9') && (e.getKeyCode() != 8 || e.getKeyCode() != 127);
        }
        
        @Override
        public void keyReleased(final KeyEvent e) {}
        
        @Override
        public void keyTyped(final KeyEvent e) {
            if(consume) {
                e.consume();
            }
        }
        
    };
    
    /**
     * showPreferenceWindow()
     *
     * Ensure current values are loaded then display window.
     */
    private void showPreferenceWindow(final GUIFactory currentGUI){
        
        if(currentGUI.getFrame() instanceof JFrame) {
            propertiesDialog = new JDialog(((Frame) currentGUI.getFrame()));
        } else {
            propertiesDialog = new JDialog();
        }
        
        propertiesDialog.setModal(true);
        
        propertiesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        if(!preferencesSetup){
            preferencesSetup=true;
            
            createPreferenceWindow(currentGUI);
        }
        
        if(properties.getValue("readOnly").equalsIgnoreCase("true")){
            JOptionPane.showMessageDialog(
                    this,
                    "You do not have permission alter jPedal properties.\n"+
                    "Access to the properties window has therefore been disabled.",
                    "Can not write to properties file", JOptionPane.INFORMATION_MESSAGE);
        }
        
        
        if(properties.isReadOnly()){
            JOptionPane.showMessageDialog(
                    this,
                    "Current properties file is read only.\n" +
                    "Any alteration can only be saved as another properties file.",
                    "Properties file is read only", JOptionPane.INFORMATION_MESSAGE);
            confirm.setEnabled(false);
        }else{
            confirm.setEnabled(true);
        }
        
        propertiesDialog.setLocationRelativeTo((Component)currentGUI.getFrame());
        propertiesDialog.setVisible(true);
    }
    
    private void saveGUIPreferences(final GUIFactory gui){
        final Component[] components = tabs.getComponents();
        for(int i=0; i!=components.length; i++){
            if(components[i] instanceof JPanel){
                final Component[] panelComponets = ((Container)components[i]).getComponents();
                for(int j=0; j!=panelComponets.length; j++){
                    if (panelComponets[j] instanceof JScrollPane) {
                        final Component[] scrollComponents = ((Container)panelComponets[j]).getComponents();
                        for(int k=0; k!=scrollComponents.length; k++){
                            if(scrollComponents[k] instanceof JViewport){
                                final Component[] viewportComponents = ((Container)scrollComponents[k]).getComponents();
                                for(int l=0; l!=viewportComponents.length; l++){
                                    if(viewportComponents[l] instanceof JTree){
                                        final JTree tree = ((JTree)viewportComponents[l]);
                                        final CheckNode root = (CheckNode)tree.getModel().getRoot();
                                        if(root.getChildCount()>0){
                                            saveMenuPreferencesChildren(root, gui);
                                        }
                                    }
                                }
                            }
                            
                        }
                    }
                    if(panelComponets[j] instanceof JButton){
                        final JButton tempButton = ((JButton)panelComponets[j]);
                        final String value = (reverseMessage.get(tempButton.getText().substring((Messages.getMessage("PdfCustomGui.HideGuiSection")+ ' ').length())));
                        if(tempButton.getText().startsWith(Messages.getMessage("PdfCustomGui.HideGuiSection")+ ' ')){
                            properties.setValue(value, "true");
                            gui.alterProperty(value, true);
                        }else{
                            properties.setValue(value, "false");
                            gui.alterProperty(value, false);
                        }
                    }
                }
            }
        }
    }
    
    private void saveMenuPreferencesChildren(final CheckNode root, final GUIFactory gui){
        for(int i=0; i!=root.getChildCount(); i++){
            final CheckNode node = (CheckNode)root.getChildAt(i);
            final String value = (reverseMessage.get(node.getText()));
            if(node.isSelected()){
                properties.setValue(value, "true");
                gui.alterProperty(value, true);
            }else{
                properties.setValue(value, "false");
                gui.alterProperty(value, false);
            }
            
            if(node.getChildCount()>0){
                saveMenuPreferencesChildren(node, gui);
            }
        }
    }
    
    /**
     * createPreferanceWindow(final GUI gui)
     * Set up all settings fields then call the required methods to build the window
     *
     * @param gui - Used to allow any changed settings to be saved into an external properties file.
     *
     */
    private void createPreferenceWindow(final GUIFactory gui){
        
        //Get Properties file containing current preferences
        properties = gui.getProperties();
        //Get Properties file location
        propertiesLocation = gui.getPropertiesFileLocation();
        
        //Set window title
        propertiesDialog.setTitle(Messages.getMessage("PdfPreferences.windowTitle"));
        
        update.setToolTipText(Messages.getMessage("PdfPreferences.update.toolTip"));
        invertHighlight.setText(Messages.getMessage("PdfPreferences.InvertHighlight"));
        showMouseSelectionBox.setText(Messages.getMessage("PdfPreferences.ShowSelectionBow"));
        invertHighlight.setToolTipText(Messages.getMessage("PdfPreferences.invertHighlight.toolTip"));
        showMouseSelectionBox.setToolTipText(Messages.getMessage("PdfPreferences.showMouseSelection.toolTip"));
        highlightBoxColor.setToolTipText(Messages.getMessage("PdfPreferences.highlightBox.toolTip"));
        
        //Set up the properties window gui components
        resolution = new JTextField();
        loadStringValue(resolution, "resolution", "72");
        resolution.setToolTipText(Messages.getMessage("PdfPreferences.resolutionInput.toolTip"));
        
        maxMultiViewers = new JTextField();
        loadStringValue(maxMultiViewers, "maxmultiviewers", "20");
        maxMultiViewers.setToolTipText(Messages.getMessage("PdfPreferences.maxMultiViewer.toolTip"));
        
        
        if(gui.isSingle()) {
            searchStyle = new JComboBox<String>(
                    new String[]{Messages.getMessage("PageLayoutViewMenu.WindowSearch"),
                            Messages.getMessage("PageLayoutViewMenu.TabbedSearch"),
                            Messages.getMessage("PageLayoutViewMenu.MenuSearch")
                    });
        } else {
            searchStyle = new JComboBox<String>(
                    new String[]{Messages.getMessage("PageLayoutViewMenu.WindowSearch"),
                            Messages.getMessage("PageLayoutViewMenu.TabbedSearch")
                    });
        }
        searchStyle.setToolTipText(Messages.getMessage("PdfPreferences.searchStyle.toolTip"));
        
        liveSearchResuts = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.LiveSearchResults"));
        liveSearchResuts.setToolTipText(Messages.getMessage("PdfPreferences.LiveSearchResults.toolTip"));
        
        pageLayout = new JComboBox<String>(
                new String[]{Messages.getMessage("PageLayoutViewMenu.SinglePage"),
                    Messages.getMessage("PageLayoutViewMenu.Continuous"),
                    Messages.getMessage("PageLayoutViewMenu.ContinousFacing"),
                    Messages.getMessage("PageLayoutViewMenu.Facing"),
                    Messages.getMessage("PageLayoutViewMenu.PageFlow")});
        pageLayout.setToolTipText(Messages.getMessage("PdfPreferences.pageLayout.toolTip"));
        
        pageInsetsText = new JLabel(Messages.getMessage("PdfViewerViewMenu.pageInsets"));
        pageInsets = new JTextField();
        pageInsets.setToolTipText(Messages.getMessage("PdfPreferences.pageInsets.toolTip"));
        
        windowTitleText = new JLabel(Messages.getMessage("PdfCustomGui.windowTitle"));
        windowTitle = new JTextField();
        windowTitle.setToolTipText(Messages.getMessage("PdfPreferences.windowTitle.toolTip"));
        
        newIconSet = new JRadioButton(Messages.getMessage("PdfViewerViewMenu.newIconButton"));
        classicIconSet = new JRadioButton(Messages.getMessage("PdfViewerViewMenu.classicIconButton"));
        skinGroup = new ButtonGroup();
        skinGroup.add(newIconSet);
        skinGroup.add(classicIconSet);
        
        iconLocationText = new JLabel(Messages.getMessage("PdfViewerViewMenu.iconLocation"));
        iconLocation = new JTextField();
        iconLocation.setToolTipText(Messages.getMessage("PdfPreferences.iconLocation.toolTip"));
        
        newIconSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                iconLocation.setText(newIconLocation);
            }
        });
        
        classicIconSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                iconLocation.setText(classicIconLocation);
            }
        });
        
        iconLocation.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {}

            @Override
            public void focusLost(final FocusEvent e) {
                if(iconLocation.getText().equals(newIconLocation)){
                    newIconSet.setSelected(true);
                }
                
                if(iconLocation.getText().equals(classicIconLocation)){
                    classicIconSet.setSelected(true);
                }
            }
        });
        
        printerBlacklistText = new JLabel(Messages.getMessage("PdfViewerPrint.blacklist"));
        printerBlacklist = new JTextField();
        printerBlacklist.setToolTipText(Messages.getMessage("PdfPreferences.printerBlackList.toolTip"));
        
        defaultPrinterText = new JLabel(Messages.getMessage("PdfViewerPrint.defaultPrinter"));
        
        defaultPrinter = new JComboBox<String>(Printer.getAvailablePrinters(properties.getValue("printerBlacklist")));
        
        final PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrintService!=null) {
            defaultPrinter.addItem(Messages.getMessage("PdfPreferences.systemDefault.text") + " (" + defaultPrintService.getName() + ')');
        } else {
            defaultPrinter.addItem(Messages.getMessage("PdfPreferences.systemDefault.text"));
        }
        defaultPrinter.setToolTipText(Messages.getMessage("PdfPreferences.defaultPrinter.toolTip"));
        
        defaultPagesizeText = new JLabel(Messages.getMessage("PdfViewerPrint.defaultPagesize"));
        defaultPagesize = new JComboBox<String>();
        defaultPagesize.setModel(new javax.swing.DefaultComboBoxModel<String>(gui.getPaperSizes().getPaperSizes()));
        defaultPagesize.setSelectedIndex(gui.getPaperSizes().getDefaultPageIndex());
        defaultPagesize.setToolTipText(Messages.getMessage("PdfPreferences.defaultPageSize.toolTip"));
        
        defaultDPIText = new JLabel(Messages.getMessage("PdfViewerPrint.defaultDPI"));
        defaultDPI = new JTextField();
        defaultDPI.setToolTipText(Messages.getMessage("PdfPreferences.defaultDPI.toolTip"));
        
        sideTabLengthText = new JLabel(Messages.getMessage("PdfCustomGui.SideTabLength"));
        sideTabLength = new JTextField();
        sideTabLength.setToolTipText(Messages.getMessage("PdfPreferences.sideTabLength.toolTip"));
        
        useHinting = new JCheckBox(Messages.getMessage("PdfCustomGui.useHinting"));
        
        useHinting.setToolTipText(Messages.getMessage("PdfPreferences.useHinting.toolTip"));
        
        autoScroll = new JCheckBox(Messages.getMessage("PdfViewerViewMenuAutoscrollSet.text"));
        autoScroll.setToolTipText("Set if autoscroll should be enabled / disabled");
        
        confirmClose = new JCheckBox(Messages.getMessage("PfdViewerViewMenuConfirmClose.text"));
        confirmClose.setToolTipText("Set if we should confirm closing the viewer");
        
        openLastDoc = new JCheckBox(Messages.getMessage("PdfViewerViewMenuOpenLastDoc.text"));
        openLastDoc.setToolTipText("Set if last document should be opened upon start up");
        
        border = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.Borders_Show"));
        border.setToolTipText("Set if we should display a border for the page");
        
        constantTabs = new JCheckBox(Messages.getMessage("PdfCustomGui.consistentTabs"));
        constantTabs.setToolTipText("Set to keep sidetabs consistant between files");
        
        enhancedViewer = new JCheckBox(Messages.getMessage("PdfCustomGui.enhancedViewer"));
        enhancedViewer.setToolTipText("Set to use enahnced viewer mode");
        
        enhanceFractionalLines = new JCheckBox(Messages.getMessage("PdfCustomGui.enhanceFractionalLines"));
        enhanceFractionalLines.setToolTipText("Set to widen thin lines to ensure visiblity at any scaling");
        
        enhancedFacing = new JCheckBox(Messages.getMessage("PdfCustomGui.enhancedFacing"));
        enhancedFacing.setToolTipText("Set to turn facing mode to page turn mode");
        
        thumbnailScroll = new JCheckBox(Messages.getMessage("PdfCustomGui.thumbnailScroll"));
        thumbnailScroll.setToolTipText("Set to show thumbnail whilst scrolling");
        
        rightClick = new JCheckBox(Messages.getMessage("PdfCustomGui.allowRightClick"));
        rightClick.setToolTipText("Set to enable / disable the right click functionality");
        
        scrollwheelZoom = new JCheckBox(Messages.getMessage("PdfCustomGui.allowScrollwheelZoom"));
        scrollwheelZoom.setToolTipText("Set to enable zooming when scrolling with ctrl pressed");
        
        historyClearedLabel = new JLabel(Messages.getMessage("PageLayoutViewMenu.HistoryCleared"));
        historyClearedLabel.setForeground(Color.red);
        historyClearedLabel.setVisible(false);
        clearHistory = new JButton(Messages.getMessage("PageLayoutViewMenu.ClearHistory"));
        clearHistory.setToolTipText("Clears the history of previous files");
        clearHistory.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(final ActionEvent e) {
                gui.getRecentDocument().clearRecentDocuments(gui.getProperties());
                
                final SwingWorker searcher = new SwingWorker() {
                    @Override
                    public Object construct() {
                        for (int i = 0; i < 6; i++) {
                            historyClearedLabel.setVisible(!historyClearedLabel.isVisible());
                            try {
                                Thread.sleep(300);
                            } catch (final InterruptedException e) {
                                LogWriter.writeLog("Exception "+e);
                            }
                        }
                        return null;
                    }
                };
                
                searcher.start();
            }
        });
        final JButton save = new JButton(Messages.getMessage("PdfPreferences.SaveAs"));
        save.setToolTipText("Save preferences in a new file");
        final JButton reset = new JButton(Messages.getMessage("PdfPreferences.ResetToDefault"));
        reset.setToolTipText("Reset  and save preferences to program defaults");
        
        //Create JFrame
        propertiesDialog.getContentPane().setLayout(new BorderLayout());
        propertiesDialog.getContentPane().add(this,BorderLayout.CENTER);
        propertiesDialog.pack();
        if (DecoderOptions.isRunningOnMac) {
            propertiesDialog.setSize(600, 475);
        } else {
            propertiesDialog.setSize(550, 450);
        }
        
        confirm.setText(Messages.getMessage("PdfPreferences.OK"));
        cancel.setText(Messages.getMessage("PdfPreferences.Cancel"));
        
        /*
         * Listeners that are reqired for each setting field
         */
        //Set properties and close the window
        confirm.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(final ActionEvent arg0) {
            	setPreferences(gui);
            	try {
            		properties.writeDoc();
            	} catch (final Exception e) {
            		LogWriter.writeLog("Attempting to write properties " + e);
                }
                if(GUI.showMessages) {
                    JOptionPane.showMessageDialog(null, Messages.getMessage("PdfPreferences.savedTo") + propertiesLocation + '\n' + Messages.getMessage("PdfPreferences.restart"), "Restart Jpedal", JOptionPane.INFORMATION_MESSAGE);
                }
                propertiesDialog.setVisible(false);
            }
        });
        confirm.setToolTipText("Save the preferences in the current loaded preferences file");
        //Close the window, don't save the properties
        cancel.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                propertiesDialog.setVisible(false);
            }
        });
        cancel.setToolTipText("Leave preferences window without saving changes");
        //Save the properties into a new file
        save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(final ActionEvent e) {
                //The properties file used when jpedal opened
                final String lastProperties = gui.getPropertiesFileLocation();
                
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return ".xml";
					}
					
					@Override
					public boolean accept(final File f) {
						return f.isDirectory() ||
								(f.getAbsolutePath().endsWith(".xml"));
					}
				});
                final int i = fileChooser.showSaveDialog(propertiesDialog);
                
                if(i == JFileChooser.CANCEL_OPTION){
                    //Do nothing
                }else if(i== JFileChooser.ERROR_OPTION){
                    //Do nothing
                }else if(i == JFileChooser.APPROVE_OPTION){
                	final String ext = fileChooser.getFileFilter().getDescription();
                    File f = fileChooser.getSelectedFile();
                    String name = fileChooser.getName(f);
                    
                    if(!ext.equals("All Files") && !name.endsWith(ext)){
                    	name+=ext;
                    	f = new File(f.getParent()+System.getProperty("file.separator")+name);
                    }
                    
                    if(f.exists()) {
                        f.delete();
                    }
                    
                    //Setup properties in the new location
                    gui.setPropertiesFileLocation(f.getAbsolutePath());
                    setPreferences(gui);
                    try {
                		properties.writeDoc();
                	} catch (final Exception e1) {
                		LogWriter.writeLog("Attempting to write properties " + e1);
                    }
                   
                }
                //Reset to the properties file used when jpedal opened
                gui.setPropertiesFileLocation(lastProperties);
            }
        });
        //Reset the properties to JPedal defaults
        reset.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int result = JOptionPane.showConfirmDialog(propertiesDialog, Messages.getMessage("PdfPreferences.reset") , "Reset to Default", JOptionPane.YES_NO_OPTION);
                //The properties file used when jpedal opened
                if(result == JOptionPane.YES_OPTION){
                    final String lastProperties = gui.getPropertiesFileLocation();
                    
                    final File f = new File(lastProperties);
                    if(f.exists()){
                        f.delete();
                    }
                    
                    gui.getProperties().loadProperties(lastProperties);
                    try {
                    	properties.writeDoc();
                    } catch (final Exception e2) {
                    	LogWriter.writeLog("Attempting to write properties " + e2);
                    }
                    if(GUI.showMessages) {
                        JOptionPane.showMessageDialog(propertiesDialog, Messages.getMessage("PdfPreferences.restart"));
                    }
                    propertiesDialog.setVisible(false);
                }
            }
        });
        
        
        highlightComposite.addKeyListener(new KeyListener(){
            
            boolean consume;
            
            @Override
            public void keyPressed(final KeyEvent e) {
                consume = (((JTextComponent) e.getSource()).getText().contains(".") && e.getKeyChar() == '.') &&
                        ((e.getKeyChar() < '0' || e.getKeyChar() > '9') && (e.getKeyCode() != 8 || e.getKeyCode() != 127));
            }
            
            @Override
            public void keyReleased(final KeyEvent e) {}
            
            @Override
            public void keyTyped(final KeyEvent e) {
                if(consume) {
                    e.consume();
                }
            }
            
        });
        highlightComposite.setToolTipText("Set the transparency of the highlight");
        
        resolution.addKeyListener(numericalKeyListener);
        maxMultiViewers.addKeyListener(numericalKeyListener);
        
        //Set the current properties from the properties file
        setLayout(new BorderLayout());
        
        final JPanel toolbar = new JPanel();
        
        final BoxLayout layout = new BoxLayout(toolbar, BoxLayout.Y_AXIS);
        toolbar.setLayout(layout);
        
        add(new ButtonBarPanel(toolbar, gui), BorderLayout.CENTER);
        
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));
        
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        final Dimension dimension = new Dimension(5,40);
        final Box.Filler filler = new Box.Filler(dimension, dimension, dimension);
        
        confirm.setPreferredSize(cancel.getPreferredSize());
        
        if(properties.isReadOnly()) {
            confirm.setEnabled(false);
        } else{
            confirm.setEnabled(true);
        }
        
        buttonPanel.add(Box.createHorizontalStrut(4));
        buttonPanel.add(reset);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(confirm);
        buttonPanel.add(save);
        getRootPane().setDefaultButton(confirm);
        
        buttonPanel.add(filler);
        buttonPanel.add(cancel);
        buttonPanel.add(filler);
        
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setPreferences(final GUIFactory gui){
        int borderStyle = 0;
        int pageMode = (pageLayout.getSelectedIndex()+1);
        if(pageMode<Display.SINGLE_PAGE || pageMode>Display.PAGEFLOW) {
            pageMode = Display.SINGLE_PAGE;
        }
        if(border.isSelected()){
            borderStyle = 1;
        }
        
        //set preferences from all but menu options
        properties.setValue("borderType", String.valueOf(borderStyle));
        properties.setValue("useHinting", String.valueOf(useHinting.isSelected()));
        properties.setValue("startView", String.valueOf(pageMode));
        properties.setValue("pageInsets", String.valueOf(pageInsets.getText()));
        properties.setValue("windowTitle", String.valueOf(windowTitle.getText()));
        String loc = iconLocation.getText();
        if(!loc.endsWith("/") && !loc.endsWith("\\")) {
            loc += '/';
        }
        properties.setValue("iconLocation", String.valueOf(loc));
        properties.setValue("sideTabBarCollapseLength", String.valueOf(sideTabLength.getText()));
        properties.setValue("autoScroll", String.valueOf(autoScroll.isSelected()));
        properties.setValue("confirmClose", String.valueOf(confirmClose.isSelected()));
        properties.setValue("openLastDocument", String.valueOf(openLastDoc.isSelected()));
        properties.setValue("resolution", String.valueOf(resolution.getText()));
        properties.setValue("searchWindowType", String.valueOf(searchStyle.getSelectedIndex()));
        properties.setValue("updateResultsDuringSearch", String.valueOf(liveSearchResuts.isSelected()));
        properties.setValue("automaticupdate", String.valueOf(update.isSelected()));
        properties.setValue("maxmultiviewers", String.valueOf(maxMultiViewers.getText()));
        properties.setValue("consistentTabBar", String.valueOf(constantTabs.isSelected()));
        properties.setValue("highlightComposite", String.valueOf(highlightComposite.getText()));
        properties.setValue("highlightBoxColor", String.valueOf(highlightBoxColor.getBackground().getRGB()));
        properties.setValue("vbgColor", String.valueOf(viewBGColor.getBackground().getRGB()));
        properties.setValue("pdfDisplayBackground", String.valueOf(pdfDecoderBackground.getBackground().getRGB()));
        properties.setValue("vfgColor", String.valueOf(foreGroundColor.getBackground().getRGB()));
        properties.setValue("replaceDocumentTextColors", String.valueOf(replaceDocTextCol.isSelected()));
        properties.setValue("replacePdfDisplayBackground", String.valueOf(replaceDisplayBGCol.isSelected()));
        properties.setValue("changeTextAndLineart", String.valueOf(changeTextAndLineArt.isSelected()));
        properties.setValue("invertHighlights", String.valueOf(invertHighlight.isSelected()));
        properties.setValue("showMouseSelectionBox", String.valueOf(showMouseSelectionBox.isSelected()));
        properties.setValue("allowRightClick", String.valueOf(rightClick.isSelected()));
        properties.setValue("allowScrollwheelZoom", String.valueOf(scrollwheelZoom.isSelected()));
        properties.setValue("enhancedViewerMode", String.valueOf(enhancedViewer.isSelected()));
        properties.setValue("enhanceFractionalLines", String.valueOf(enhanceFractionalLines.isSelected()));
        properties.setValue("enhancedFacingMode", String.valueOf(enhancedFacing.isSelected()));
        properties.setValue("previewOnSingleScroll", String.valueOf(thumbnailScroll.isSelected()));
        properties.setValue("printerBlacklist", String.valueOf(printerBlacklist.getText()));
        if (((String)defaultPrinter.getSelectedItem()).startsWith("System Default")) {
            properties.setValue("defaultPrinter", "");
        } else {
            properties.setValue("defaultPrinter", String.valueOf(defaultPrinter.getSelectedItem()));
        }
        properties.setValue("defaultDPI", String.valueOf(defaultDPI.getText()));
        properties.setValue("defaultPagesize", String.valueOf(defaultPagesize.getSelectedItem()));
        
        if(speech!=null) {
            properties.setValue("voice", String.valueOf(voiceSelect.getSelectedItem()));
        }
        
        //Save all options found in a tree
        saveGUIPreferences(gui);
    }
    
    class ButtonBarPanel extends JPanel {
        
        private Component currentComponent;
        
        ButtonBarPanel(final JPanel toolbar, final GUIFactory gui) {
            setLayout(new BorderLayout());
            
            //Add scroll pane as too many options
            final JScrollPane jsp = new JScrollPane();
            jsp.getViewport().add(toolbar);
            jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            
            
            add(jsp, BorderLayout.WEST);
            
            final ButtonGroup group = new ButtonGroup();
            
            addButton(Messages.getMessage("PdfPreferences.GeneralTitle"), gui.getGUICursor().getURLForImage("display.png"), createGeneralSettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.PageDisplayTitle"), gui.getGUICursor().getURLForImage("pagedisplay.png"), createPageDisplaySettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.InterfaceTitle"), gui.getGUICursor().getURLForImage("interface.png"), createInterfaceSettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.ColorTitle"), gui.getGUICursor().getURLForImage("color.png"), createColorSettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.MenuTitle"), gui.getGUICursor().getURLForImage("menu.png"), createMenuSettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.PrintingTitle"), gui.getGUICursor().getURLForImage("printing.png"), createPrintingSettings(), toolbar, group);
            
            addButton(Messages.getMessage("PdfPreferences.ExtensionsTitle"), gui.getGUICursor().getURLForImage("extensions.png"), createExtensionsPane(), toolbar, group);
        }
        
        private JPanel makePanel(final String title) {
            final JPanel panel = new JPanel(new BorderLayout());
            final JLabel topLeft = new JLabel(title);
            topLeft.setFont(topLeft.getFont().deriveFont(Font.BOLD));
            topLeft.setOpaque(true);
            topLeft.setBackground(panel.getBackground().brighter());
            
            final JPanel topbar = new JPanel(new BorderLayout());
            topbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            topbar.setFont(topbar.getFont().deriveFont(Font.BOLD));
            topbar.setOpaque(true);
            topbar.setBackground(panel.getBackground().brighter());
            
            topbar.add(topLeft, BorderLayout.WEST);
            //			topbar.add(topRight, BorderLayout.EAST);
            
            panel.add(topbar, BorderLayout.NORTH);
            panel.setPreferredSize(new Dimension(400, 300));
            panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            return panel;
        }
        
        
        /*
         * Creates a pane holding all General settings
         */
        private JPanel createGeneralSettings(){
            
            //Set values from Properties file
            loadStringValue(resolution, "resolution");
            
            loadBooleanValue(useHinting, "useHinting");
            loadBooleanValue(autoScroll, "autoScroll");
            loadBooleanValue(confirmClose, "confirmClose");
            loadBooleanValue(update, "automaticupdate");
            loadBooleanValue(openLastDoc, "openLastDocument");
            
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.GeneralTitle"));
            
            final JPanel pane = new JPanel();
            final JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            pane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,0,0,5));
            final JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralSection"));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            pane.add(label, c);

            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(10,0,0,5));
            final JLabel label2 = new JLabel(Messages.getMessage("PdfViewerViewMenu.Resolution"));
            label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(label2, c);

            setGridBagConstraints(c, 1, 1, 1, 1, 1, 0, new Insets(10,0,0,0));
            pane.add(resolution, c);

            setGridBagConstraints(c, 0, 2, 2, 1, 1, 0, new Insets(10,0,0,0));
            useHinting.setMargin(new Insets(0,0,0,0));
            useHinting.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(useHinting, c);

            setGridBagConstraints(c, 0, 3, 2, 1, 1, 0, new Insets(10,0,0,0));
            autoScroll.setMargin(new Insets(0,0,0,0));
            autoScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(autoScroll, c);

            setGridBagConstraints(c, 0, 4, 2, 1, 1, 0, new Insets(10,0,0,0));
            confirmClose.setMargin(new Insets(0,0,0,0));
            confirmClose.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(confirmClose, c);

            setGridBagConstraints(c, 0, 5, 2, 1, 0, 0, new Insets(15,0,0,0));
            final JLabel label3 = new JLabel(Messages.getMessage("PdfPreferences.StartUp"));
            label3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label3.setFont(label3.getFont().deriveFont(Font.BOLD));
            pane.add(label3, c);

            setGridBagConstraints(c, 0, 6, 2, 1, 1, 0, new Insets(10,0,0,0));
            update.setMargin(new Insets(0,0,0,0));
            update.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(update, c);

            setGridBagConstraints(c, 0, 7, 2, 1, 1, 0, new Insets(10,0,0,0));
            openLastDoc.setMargin(new Insets(0,0,0,0));
            openLastDoc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(openLastDoc, c);

            setGridBagConstraints(c, 0, 8, 2, 1, 1, 0, new Insets(10,0,0,0));
            final JPanel clearHistoryPanel = new JPanel();
            clearHistoryPanel.setLayout(new BoxLayout(clearHistoryPanel, BoxLayout.X_AXIS));
            clearHistoryPanel.add(clearHistory);
            clearHistoryPanel.add(Box.createHorizontalGlue());
            
            clearHistoryPanel.add(historyClearedLabel);
            clearHistoryPanel.add(Box.createHorizontalGlue());
            pane.add(clearHistoryPanel, c);

            setGridBagConstraints(c, 0, 9, 2, 1, 1, 1, new Insets(10,0,0,0));
            pane.add(Box.createVerticalGlue(), c);
            
            panel.add(scroll, BorderLayout.CENTER);
            
            return panel;
        }
        
        
        /*
         * Creates a pane holding all Page Display settings (e.g Insets, borders, display modes, etc)
         */
        private JPanel createPageDisplaySettings(){
            
            //Set values from Properties file
            loadBooleanValue(enhancedViewer, "enhancedViewerMode");
            loadBooleanValue(enhanceFractionalLines, "enhanceFractionalLines");            
            loadBooleanValue(enhancedFacing, "enhancedFacingMode");
            loadBooleanValue(thumbnailScroll, "previewOnSingleScroll");
            
            loadStringValue(pageInsets, "pageInsets", "25");
            
            final String borderType = properties.getValue("borderType").toLowerCase();
            border.setSelected(!borderType.isEmpty() && Integer.parseInt(borderType) == 1);
            
            final String propValue = properties.getValue("startView");
            if(!propValue.isEmpty()){
                int mode = Integer.parseInt(propValue);
                if(mode<Display.SINGLE_PAGE || mode>Display.PAGEFLOW) {
                    mode = Display.SINGLE_PAGE;
                }
                
                pageLayout.setSelectedIndex(mode-1);
            }
            
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.PageDisplayTitle"));
            
            final JPanel pane = new JPanel();
            final JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            pane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,0,0,5));
            final JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralSection"));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            pane.add(label, c);

            setGridBagConstraints(c, 0, 1, 2, 1, 0, 0, new Insets(5,0,0,0));
            enhancedViewer.setMargin(new Insets(0,0,0,0));
            enhancedViewer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(enhancedViewer, c);

            setGridBagConstraints(c, 0, 2, 2, 1, 0, 0, new Insets(5,0,0,0));
            enhanceFractionalLines.setMargin(new Insets(0,0,0,0));
            enhanceFractionalLines.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(enhanceFractionalLines, c);

            setGridBagConstraints(c, 0, 3, 2, 1, 0, 0, new Insets(5,0,0,0));
            border.setMargin(new Insets(0,0,0,0));
            border.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(border, c);

            setGridBagConstraints(c, 0, 4, 2, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(pageInsetsText, c);

            setGridBagConstraints(c, 1, 4, 2, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(pageInsets, c);

            setGridBagConstraints(c, 0, 5, 2, 1, 0, 0, new Insets(15,0,0,5));
            final JLabel label2 = new JLabel(Messages.getMessage("PdfPreferences.DisplayModes"));
            label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label2.setFont(label2.getFont().deriveFont(Font.BOLD));
            pane.add(label2, c);

            setGridBagConstraints(c, 0, 6, 2, 1, 0, 0, new Insets(5,0,0,5));
            final JLabel label1 = new JLabel(Messages.getMessage("PageLayoutViewMenu.PageLayout"));
            label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(label1, c);

            setGridBagConstraints(c, 1, 6, 2, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(pageLayout, c);

            setGridBagConstraints(c, 0, 7, 2, 1, 1, 0, new Insets(5,0,0,0));
            enhancedFacing.setMargin(new Insets(0,0,0,0));
            enhancedFacing.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(enhancedFacing, c);

            setGridBagConstraints(c, 0, 8, 2, 1, 1, 0, new Insets(5,0,0,0));
            thumbnailScroll.setMargin(new Insets(0,0,0,0));
            thumbnailScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(thumbnailScroll, c);

            setGridBagConstraints(c, 0, 9, 2, 1, 1, 1, new Insets(5,0,0,0));
            pane.add(Box.createVerticalGlue(), c);
            panel.add(scroll, BorderLayout.CENTER);
            
            return panel;
        }
        
        private void loadCreateInterfaceSettings() {
            
            loadBooleanValue(rightClick, "allowRightClick");
            loadBooleanValue(scrollwheelZoom, "allowScrollwheelZoom");
            loadBooleanValue(liveSearchResuts, "updateResultsDuringSearch");
            loadBooleanValue(constantTabs, "consistentTabBar");
            loadBooleanValue(showMouseSelectionBox, "showMouseSelectionBox");
            
            loadStringValue(windowTitle, "windowTitle");
            loadStringValue(maxMultiViewers, "maxmultiviewers");
            
            loadStringValue(iconLocation, "iconLocation", "/org/jpedal/examples/viewer/res/");
            if(iconLocation.getText().equals(newIconLocation)){
                newIconSet.setSelected(true);
            }else{
                if(iconLocation.getText().equals(classicIconLocation)){
                    classicIconSet.setSelected(true);
                }
            }
            loadStringValue(sideTabLength, "sideTabBarCollapseLength", "30");
                    
            final String propValue = properties.getValue("searchWindowType");
            int index = 0;//Default value
            if (!propValue.isEmpty()) {
                index = Integer.parseInt(propValue); //Set from properties
                if (index >= searchStyle.getItemCount()) { //If invalid, set to default
                    index = 0;
                }
            }
            searchStyle.setSelectedIndex(index);
            
        }
    
//        private void setLayoutConstraints(final GridBagConstraints constraints, final Insets insets, final int gridX, final int gridY, final int gridW, final int gridH, final int weightX, final int weightY){
//        }
        
        private JScrollPane createAppearanceTab(){
            final JPanel contentPane = new JPanel();
            final JScrollPane scrollPane = new JScrollPane(contentPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            contentPane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,5,5,5));
            final JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            contentPane.add(label, c);

            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(3,5,0,5));
            contentPane.add(windowTitleText, c);
            setGridBagConstraints(c, 1, 1, 1, 1, 0, 0, new Insets(3,5,0,5));
            contentPane.add(windowTitle, c);

            setGridBagConstraints(c, 0, 2, 1, 1, 0, 0, new Insets(5,5,5,5));
            contentPane.add(newIconSet, c);
            setGridBagConstraints(c, 1, 2, 1, 1, 0, 0, new Insets(5,5,5,5));
            contentPane.add(classicIconSet, c);

            setGridBagConstraints(c, 0, 3, 1, 1, 0, 0, new Insets(5,5,5,5));
            contentPane.add(iconLocationText, c);
            setGridBagConstraints(c, 1, 3, 1, 1, 0, 0, new Insets(5,5,5,5));
            contentPane.add(iconLocation, c);

            setGridBagConstraints(c, 0, 4, 1, 1, 0, 0, new Insets(5,5,5,5));
            final JLabel label5 = new JLabel(Messages.getMessage("PageLayoutViewMenu.SearchLayout"));
            label5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(label5, c);
            setGridBagConstraints(c, 1, 4, 1, 1, 1, 0, new Insets(5,5,5,5));
            contentPane.add(searchStyle, c);

            setGridBagConstraints(c, 0, 5, 1, 1, 1, 0, new Insets(5,0,0,5));
            contentPane.add(liveSearchResuts, c);

            setGridBagConstraints(c, 0, 6, 1, 1, 0, 0, new Insets(10,5,5,5));
            final JLabel label4 = new JLabel(Messages.getMessage("PdfPreferences.MaxMultiViewers"));
            label4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(label4, c);
            setGridBagConstraints(c, 1, 6, 1, 1, 1, 0, new Insets(5,5,5,5));
            contentPane.add(maxMultiViewers, c);

            setGridBagConstraints(c, 0, 7, 1, 1, 0, 0, new Insets(15,5,5,5));
            final JLabel label1 = new JLabel(Messages.getMessage("PdfPreferences.SideTab"));
            label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label1.setFont(label1.getFont().deriveFont(Font.BOLD));
            contentPane.add(label1, c);

            setGridBagConstraints(c, 0, 8, 1, 1, 0, 0, new Insets(5,5,5,5));
            sideTabLengthText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(sideTabLengthText, c);
            setGridBagConstraints(c, 1, 8, 1, 1, 1, 0, new Insets(5,5,5,5));
            contentPane.add(sideTabLength, c);

            setGridBagConstraints(c, 0, 9, 2, 1, 1, 0, new Insets(5,5,5,5));
            constantTabs.setMargin(new Insets(0,0,0,0));
            constantTabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(constantTabs, c);

            setGridBagConstraints(c, 0, 10, 2, 1, 1, 1, new Insets(5,5,5,5));
            contentPane.add(Box.createVerticalGlue(), c);
            
            return scrollPane;
        }

        private JScrollPane createMouseTab(){
            final JPanel contentPane = new JPanel();
            final JScrollPane scrollPane = new JScrollPane(contentPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            contentPane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,5,5,5));
            final JLabel label3 = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
            label3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label3.setFont(label3.getFont().deriveFont(Font.BOLD));
            contentPane.add(label3, c);

            setGridBagConstraints(c, 0, 1, 2, 1, 0, 0, new Insets(5,5,5,5));
            rightClick.setMargin(new Insets(0,0,0,0));
            rightClick.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(rightClick, c);

            setGridBagConstraints(c, 0, 2, 2, 1, 0, 0, new Insets(5,5,5,5));
            scrollwheelZoom.setMargin(new Insets(0,0,0,0));
            scrollwheelZoom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(scrollwheelZoom, c);

            setGridBagConstraints(c, 0, 3, 1, 1, 0, 0, new Insets(0,0,0,5));
            contentPane.add(showMouseSelectionBox, c);

            setGridBagConstraints(c, 0, 4, 1, 1, 1, 0, new Insets(0,0,0,5));
            contentPane.add(Box.createVerticalGlue(), c);

            return scrollPane;
        }
        
        private JScrollPane createSpeechTab(){
            
            final JPanel contentPane = new JPanel();
            final JScrollPane scrollPane = new JScrollPane(contentPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            contentPane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,0,0,5));
            final JLabel label6 = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
            label6.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label6.setFont(label6.getFont().deriveFont(Font.BOLD));
            contentPane.add(label6, c);
            

            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(5,0,0,5));
            voiceSelect = new JComboBox<String>(speech.listVoices());
            final JLabel label7 = new JLabel(Messages.getMessage("PdfPreferences.Voice"));
            label7.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            contentPane.add(label7, c);

            setGridBagConstraints(c, 1, 1, 1, 1, 1, 0, new Insets(5,0,0,0));
            voiceSelect.setSelectedItem(properties.getValue("voice"));
            contentPane.add(voiceSelect, c);

            setGridBagConstraints(c, 0, 2, 1, 1, 1, 1, new Insets(5,0,0,0));
            contentPane.add(Box.createVerticalGlue(), c);
            
            return scrollPane;
        }
        
        /*
         * Creates a contentPane holding all Interface settings (e.g Search Style, icons, etc)
         */
        private JPanel createInterfaceSettings(){
            
            //Set values from Properties file
            loadCreateInterfaceSettings();
            
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.InterfaceTitle"));
            
            final JTabbedPane tabs = new JTabbedPane();
            
            tabs.add(Messages.getMessage("PdfPreferences.AppearanceTab"), createAppearanceTab());
            
            tabs.add(Messages.getMessage("PdfPreferences.Mouse"), createMouseTab());
            
            if(speech!=null) { //Checks that freetts is available in order to make combo box.
                tabs.add(Messages.getMessage("PdfPreferences.Speech"), createSpeechTab());
            }
            
            panel.add(tabs, BorderLayout.CENTER);
            
            return panel;
        }
        
        /*
         * Creates a pane holding all Printing settings
         */
        private JPanel createPrintingSettings(){
                        
            loadStringValue(printerBlacklist, "printerBlacklist");
            
            String propValue = properties.getValue("defaultPrinter");
            if (propValue!=null && !propValue.isEmpty()) {
                defaultPrinter.setSelectedItem(propValue);
            } else {
                final PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
                if (defaultPrintService!= null) {
                    defaultPrinter.setSelectedItem("System Default (" + defaultPrintService.getName() + ')');
                } else {
                    defaultPrinter.setSelectedItem("System Default");
                }
            }
            
            propValue = properties.getValue("defaultDPI");
            if (propValue!=null && !propValue.isEmpty()) {
                try {
                    propValue = propValue.replaceAll("[^0-9]", "");
                    defaultDPI.setText(Integer.parseInt(propValue)+"dpi");
                } catch (final Exception e) {
                    LogWriter.writeLog("Attempting to get Properties values " + e);
                }
            }
            
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.PrintingTitle"));
            
            final JPanel pane = new JPanel();
            final JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            pane.setLayout(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,0,0,5));
            final JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralSection"));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            pane.add(label, c);

            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultPrinterText, c);

            setGridBagConstraints(c, 1, 1, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultPrinter, c);

            setGridBagConstraints(c, 0, 2, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultPagesizeText, c);

            setGridBagConstraints(c, 1, 2, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultPagesize, c);

            setGridBagConstraints(c, 0, 3, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultDPIText, c);

            setGridBagConstraints(c, 1, 3, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(defaultDPI, c);

            setGridBagConstraints(c, 0, 4, 1, 1, 0, 0, new Insets(5,0,0,0));
            pane.add(printerBlacklistText, c);

            setGridBagConstraints(c, 1, 4, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(printerBlacklist, c);

            setGridBagConstraints(c, 0, 5, 1, 1, 0, 1, new Insets(5,0,0,0));
            pane.add(Box.createVerticalGlue(), c);
            
            panel.add(scroll);
            
            return panel;
        }
        
        private JPanel createColorSettings() {
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.MenuTitle"));

            final JPanel pane = new JPanel();
            final JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            pane.setLayout(new GridBagLayout());

            String propValue = properties.getValue("highlightBoxColor");
            final int hBoxColor;
            if(!propValue.isEmpty()){
                hBoxColor = Integer.parseInt(propValue);
            }else{
                hBoxColor = DecoderOptions.highlightColor.getRGB();
            }
            final Color currentBox = new Color(hBoxColor);
            highlightBoxColor.setBackground(currentBox);
            
            final JButton hBoxButton = new JButton(Messages.getMessage("PdfPreferences.ChangeHighlightColor"));
            hBoxButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final Color c = JColorChooser.showDialog(null, "Highlight Color", currentBox);
                    highlightBoxColor.setBackground(c);
                    
                }
            });
            
            loadStringValue(highlightComposite, "highlightComposite");
            
            loadBooleanValue(invertHighlight, "invertHighlights");
            
            final JLabel hCompLabel = new JLabel(Messages.getMessage("PdfPreferences.ChangeHighlightTransparency"));
            
            //Dependent of invert value, set highlight options to enabled / disabled
            if(invertHighlight.isSelected()){
                highlightBoxColor.setEnabled(false);
                highlightComposite.setEnabled(false);
                hBoxButton.setEnabled(false);
                hCompLabel.setEnabled(false);
            }else{
                highlightBoxColor.setEnabled(true);
                highlightComposite.setEnabled(true);
                hBoxButton.setEnabled(true);
                hCompLabel.setEnabled(true);
            }
            
            invertHighlight.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if(((AbstractButton)e.getSource()).isSelected()){
                        highlightBoxColor.setEnabled(false);
                        highlightComposite.setEnabled(false);
                        hBoxButton.setEnabled(false);
                        hCompLabel.setEnabled(false);
                    }else{
                        highlightBoxColor.setEnabled(true);
                        highlightComposite.setEnabled(true);
                        hBoxButton.setEnabled(true);
                        hCompLabel.setEnabled(true);
                    }
                }
            });
            
            propValue = properties.getValue("vbgColor");
            int vbgColor = 0;
            if(!propValue.isEmpty()){
                vbgColor = Integer.parseInt(propValue);
            }else if(DecoderOptions.backgroundColor!=null){
                    vbgColor = DecoderOptions.backgroundColor.getRGB();
            }
            final Color ViewerBackgroundColor = new Color(vbgColor);
            viewBGColor.setBackground(ViewerBackgroundColor);
            
            
            final JButton viewerBackgroundButton = new JButton(Messages.getMessage("PdfPreferences.ChangeBackgroundColor"));
            viewerBackgroundButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final Color c = JColorChooser.showDialog(null, "BackGround Color", ViewerBackgroundColor);
                    viewBGColor.setBackground(c);
                }
            });
            
            propValue = properties.getValue("vfgColor");
            int vfgColor = 0;
            if(!propValue.isEmpty()){
                vfgColor = Integer.parseInt(propValue);
            }
            
            final Color FGColor = new Color(vfgColor);
            foreGroundColor.setBackground(FGColor);
            
            
            final JButton FGButton = new JButton(Messages.getMessage("PdfPreferences.ChangeForegroundColor"));
            FGButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final Color c = JColorChooser.showDialog(null, "Foreground Color", FGColor);
                    foreGroundColor.setBackground(c);
                    
                }
            });
            
            loadBooleanValue(changeTextAndLineArt, "changeTextAndLineart");
            loadBooleanValue(replaceDocTextCol, "replaceDocumentTextColors");
            
            //Dependent of invert value, set highlight options to enabled / disabled
            if(replaceDocTextCol.isSelected()){
                FGButton.setEnabled(true);
                foreGroundColor.setEnabled(true);
                changeTextAndLineArt.setEnabled(true);
            }else{
                FGButton.setEnabled(false);
                foreGroundColor.setEnabled(false);
                changeTextAndLineArt.setEnabled(false);
            }
            
            replaceDocTextCol.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if(((AbstractButton)e.getSource()).isSelected()){
                        FGButton.setEnabled(true);
                        foreGroundColor.setEnabled(true);
                        changeTextAndLineArt.setEnabled(true);
                    }else{
                        FGButton.setEnabled(false);
                        foreGroundColor.setEnabled(false);
                        changeTextAndLineArt.setEnabled(false);
                    }
                }
            });
            
            propValue = properties.getValue("pdfDisplayBackground");
            int pdbColor = 0;
            if(!propValue.isEmpty()){
                pdbColor = Integer.parseInt(propValue);
            }
            final Color PDBColor = new Color(pdbColor);
            pdfDecoderBackground.setBackground(PDBColor);
            
            final JButton PDBButton = new JButton(Messages.getMessage("PdfPreferences.ChangeDisplayBackgroundColor"));
            PDBButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final Color c = JColorChooser.showDialog(null, "Display Background Color", PDBColor);
                    pdfDecoderBackground.setBackground(c);
                    
                }
            });
            
            loadBooleanValue(replaceDisplayBGCol, "replacePdfDisplayBackground");
            
            //Dependent of invert value, set highlight options to enabled / disabled
            if(replaceDisplayBGCol.isSelected()){
                PDBButton.setEnabled(true);
                pdfDecoderBackground.setEnabled(true);
            }else{
                PDBButton.setEnabled(false);
                pdfDecoderBackground.setEnabled(false);
            }
            
            replaceDisplayBGCol.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if(((AbstractButton)e.getSource()).isSelected()){
                        PDBButton.setEnabled(true);
                        pdfDecoderBackground.setEnabled(true);
                    }else{
                        PDBButton.setEnabled(false);
                        pdfDecoderBackground.setEnabled(false);
                    }
                }
            });


            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(5,0,0,5));

            final JLabel label2 = new JLabel("Highlights");
            label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label2.setFont(label2.getFont().deriveFont(Font.BOLD));
            pane.add(label2, c);

            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(5,0,0,5));
            highlightBoxColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pane.add(highlightBoxColor, c);

            setGridBagConstraints(c, 1, 1, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(hBoxButton, c);

            setGridBagConstraints(c, 0, 2, 1, 1, 1, 0, new Insets(5,0,0,5));
            pane.add(highlightComposite, c);

            setGridBagConstraints(c, 1, 2, 1, 1, 1, 0, new Insets(5,25,0,0));
            pane.add(hCompLabel, c);

            setGridBagConstraints(c, 0, 3, 1, 1, 1, 0, new Insets(0,0,0,5));
            pane.add(invertHighlight, c);

            setGridBagConstraints(c, 0, 4, 1, 1, 0, 0, new Insets(15,0,0,5));
            final JLabel label3 = new JLabel("Display Colors");
            label3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            label3.setFont(label3.getFont().deriveFont(Font.BOLD));
            pane.add(label3, c);

            //New colors here
            setGridBagConstraints(c, 0, 5, 1, 1, 0, 0, new Insets(5,0,0,5));
            viewBGColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pane.add(viewBGColor, c);

            setGridBagConstraints(c, 1, 5, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(viewerBackgroundButton, c);

            setGridBagConstraints(c, 0, 6, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(replaceDocTextCol, c);

            setGridBagConstraints(c, 0, 7, 1, 1, 1, 0, new Insets(5,0,0,5));
            foreGroundColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pane.add(foreGroundColor, c);

            setGridBagConstraints(c, 1, 7, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(FGButton, c);

            setGridBagConstraints(c, 0, 8, 1, 1, 1, 0, new Insets(5,0,0,5));
            pane.add(changeTextAndLineArt, c);

            setGridBagConstraints(c, 0, 9, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(replaceDisplayBGCol, c);

            setGridBagConstraints(c, 0, 10, 1, 1, 1, 0, new Insets(5,0,0,5));
            pdfDecoderBackground.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pane.add(pdfDecoderBackground, c);

            setGridBagConstraints(c, 1, 10, 1, 1, 1, 0, new Insets(5,0,0,0));
            pane.add(PDBButton, c);

            setGridBagConstraints(c, 0, 11, 2, 1, 1, 0, new Insets(5,0,0,0));
            final JPanel clearHistoryPanel = new JPanel();
            clearHistoryPanel.setLayout(new BoxLayout(clearHistoryPanel, BoxLayout.X_AXIS));
            clearHistoryPanel.add(Box.createHorizontalGlue());
            clearHistoryPanel.add(Box.createHorizontalGlue());
            pane.add(clearHistoryPanel, c);

            setGridBagConstraints(c, 0, 12, 2, 1, 1, 1, new Insets(5,0,0,0));
            pane.add(Box.createVerticalGlue(), c);
            
            panel.add(scroll, BorderLayout.CENTER); 
            
            return panel;
        }

        // Accepts a GridBagConstraints object and the values to apply to it. Used to reduce repeated code setting up UI
        private void setGridBagConstraints(final GridBagConstraints c, final int gridX, final int gridY, final int gridWidth, final int gridHeight, final double weightX, final double weightY, final Insets insets) {
            c.gridx = gridX;
            c.gridy = gridY;
            c.gridwidth = gridWidth;
            c.gridheight = gridHeight;
            c.weightx = weightX;
            c.weighty = weightY;
            c.insets = insets;
        }
        
        private JPanel createMenuSettings(){
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.MenuTitle"));
            
            final JPanel pane = new JPanel(new BorderLayout());
            
            tabs = new JTabbedPane();
            for(int t=0; t!=menuTabs.length; t++){
                //MenuBar Tab
                reverseMessage.put(Messages.getMessage("PdfCustomGui."+menuTabs[t]), menuTabs[t]);
                final CheckNode top = new CheckNode(Messages.getMessage("PdfCustomGui."+menuTabs[t]));
                top.setEnabled(true);
                top.setSelected(true);
                
                final ArrayList<CheckNode> last = new ArrayList<CheckNode>();
                last.add(top);
                
                final NodeList nodes = properties.getChildren(Messages.getMessage("PdfCustomGui."+menuTabs[t])+"Menu");
                addMenuToTree(t, nodes, top, last);
                
                final JTree tree = new JTree(top);
                final JScrollPane scroll = new JScrollPane(tree);
                tree.setCellRenderer(new CheckRenderer());
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                
                tree.addTreeSelectionListener(new TreeSelectionListener() {
                    
                    private void setChildrenValue(final CheckNode checkNode, final boolean status){
                        for(int i=0; i!=checkNode.getChildCount(); i++){
                            ((CheckNode)checkNode.getChildAt(i)).setSelected(status);
                            if(checkNode.getChildAt(i).getChildCount()>0){
                                setChildrenValue(((CheckNode)checkNode.getChildAt(i)), status);
                            }
                        }
                    }
                    
                    private void setParentValue(final CheckNode checkNode, final boolean status){
                        checkNode.setSelected(status);
                        if(checkNode.getParent() !=null){
                            setParentValue(((CheckNode)checkNode.getParent()), status);
                        }
                    }
                    
                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                tree.getLastSelectedPathComponent();
                        
                        //toggle layer status when clicked
                        final Runnable updateAComponent = new Runnable() {
                            
                            @Override
                            public void run(){
                                //update settings on display and in PdfDecoder
                                final CheckNode checkNode=(CheckNode)node;
                                if(checkNode!=null){
                                    boolean reversedStatus=!checkNode.isSelected();
                                    if(reverseMessage.get(checkNode.getText()).equals("Preferences") && !reversedStatus){
                                        final int result = JOptionPane.showConfirmDialog(propertiesDialog, "Disabling this option will mean you can not acces this menu using this properties file. Do you want to continue?", "Preferences Access", JOptionPane.YES_NO_OPTION);
                                        if(result==JOptionPane.NO_OPTION){
                                            reversedStatus=!reversedStatus;
                                        }
                                    }
                                    
                                    if(checkNode.getChildCount()>0) {
                                        setChildrenValue(checkNode, reversedStatus);
                                    }
                                    
                                    
                                    if(checkNode.getParent() !=null && reversedStatus) {
                                        setParentValue(((CheckNode) checkNode.getParent()), reversedStatus);
                                    }
                                    
                                    
                                    checkNode.setSelected(reversedStatus);
                                    
                                    tree.invalidate();
                                    tree.clearSelection();
                                    tree.repaint();
                                    
                                }
                            }
                        };
                        SwingUtilities.invokeLater(updateAComponent);
                    }
                });
                final JPanel display = new JPanel(new BorderLayout());
                
                
                final JButton hideGuiSection = new JButton();
                
                final String propValue = properties.getValue(menuTabs[t]);
                if(propValue.equalsIgnoreCase("true")) {
                    hideGuiSection.setText(Messages.getMessage("PdfCustomGui.HideGuiSection") + ' ' + Messages.getMessage("PdfCustomGui." + menuTabs[t]));
                } else{
                    hideGuiSection.setText(Messages.getMessage("PdfCustomGui.ShowGuiSection")+ ' ' +  Messages.getMessage("PdfCustomGui."+menuTabs[t]));
                }
                
                final int currentTab = t;
                hideGuiSection.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if(hideGuiSection.getText().startsWith("Click here to show ")){
                            hideGuiSection.setText(Messages.getMessage("PdfCustomGui.HideGuiSection")+ ' ' +  Messages.getMessage("PdfCustomGui."+menuTabs[currentTab]));
                            
                        }else{
                            hideGuiSection.setText(Messages.getMessage("PdfCustomGui.ShowGuiSection")+ ' ' +  Messages.getMessage("PdfCustomGui."+menuTabs[currentTab]));
                        }
                    }
                });
                display.add(scroll, BorderLayout.CENTER);
                display.add(hideGuiSection, BorderLayout.SOUTH);
                tabs.add(display, Messages.getMessage("PdfCustomGui."+menuTabs[t]));
            }
            
            
            pane.add(tabs, BorderLayout.CENTER);
            panel.add(pane, BorderLayout.CENTER);
            
            
            return panel;
        }
        
        private void  addMenuToTree(final int tab, final NodeList nodes, final CheckNode top, final java.util.List<CheckNode> previous){
            
            for(int i=0; i!=nodes.getLength(); i++){
                
                if(i<nodes.getLength()){
                    final String name = nodes.item(i).getNodeName();
                       
                    if(!name.startsWith("#")){
                        //Node to add
                        final CheckNode newLeaf = new CheckNode(Messages.getMessage("PdfCustomGui."+name));
                        newLeaf.setEnabled(true);
                        //Set to reversedMessage for saving of preferences
                        reverseMessage.put(Messages.getMessage("PdfCustomGui."+name), name);
                        loadBooleanValue(newLeaf, name);

                        //If has child nodes
                        if(nodes.item(i).hasChildNodes()){
                            //Store this top value
                            previous.add(top);
                            //Set this node to ned top
                            top.add(newLeaf);
                            //Add new menu to tree
                            addMenuToTree(tab, nodes.item(i).getChildNodes(), newLeaf, previous);
                        }else{
                            //Add to current top
                            top.add(newLeaf);
                        }
                    }               
                }
            }
        }

        /**
         * Creates a contentPane showing which extensions are enabled
         * @return contentPane
         */
        private JPanel createExtensionsPane() {
            final JPanel panel = makePanel(Messages.getMessage("PdfPreferences.ExtensionsTitle"));
            
            final JPanel pane = new JPanel();
            pane.setLayout(new GridBagLayout());
            
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            final JLabel title = new JLabel(Messages.getMessage("PdfPreferences.ExtensionName"));
            final Font titleFont = title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D());
            title.setFont(titleFont);
            setGridBagConstraints(c, 0, 0, 1, 1, 0, 0, new Insets(12,2,5,2));
            pane.add(title, c);

            final JLabel title2 = new JLabel(Messages.getMessage("PdfPreferences.ExtensionDescription"));
            title2.setFont(titleFont);
            setGridBagConstraints(c, 1, 0, 1, 1, 1, 0, new Insets(12,2,5,2));
            pane.add(title2, c);
            
            final JLabel title3 = new JLabel(Messages.getMessage("PdfPreferences.ExtensionVersion"));
            title3.setFont(titleFont);
            setGridBagConstraints(c, 2, 0, 1, 1, 0, 0, new Insets(12,2,5,2));
            pane.add(title3, c);
            
            class Link extends MouseAdapter {
                private final String url;
                Link(final String url) {
                    this.url = url;
                }
                @Override
                public void mouseEntered(final MouseEvent e) {
                    if(GUIDisplay.allowChangeCursor) {
                        pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
                @Override
                public void mouseExited(final MouseEvent e) {
                    if(GUIDisplay.allowChangeCursor) {
                        pane.setCursor(Cursor.getDefaultCursor());
                    }
                }
                @Override
                public void mouseClicked(final MouseEvent e) {
                    try {
                        BrowserLauncher.openURL(url);
                    } catch(final Exception ex){
                        JOptionPane.showMessageDialog(pane, ex.getMessage()+' '+Messages.getMessage("PdfViewer.ErrorWebsite"));
                    }
                }
            }

            String details = "java: "+System.getProperty("java.vendor")+ ' ' +System.getProperty("java.version")+ '\n';
            details += "os: "+System.getProperty("os.name")+ ' ' +System.getProperty("os.version")+ ' ' +System.getProperty("os.arch")+ '\n';
            details += "jpedal: "+PdfDecoderInt.version+ '\n';
            
            //CID
            setGridBagConstraints(c, 0, 1, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("CID"), c);
            setGridBagConstraints(c, 1, 1, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("<html>"+Messages.getMessage("PdfExtensions.CID.text")), c);
            setGridBagConstraints(c, 2, 1, 1, 1, 0, 0, new Insets(2,2,2,2));
            final JLabel cid;
            try{
                if (SwingProperties.class.getResourceAsStream("/org/jpedal/res/cid/00_ReadMe.pdf")!=null) {
                    cid = new JLabel("<html>"+"1.0");
                    details += "cid: 1.0\n";
                } else {
                    cid = new JLabel("<html><u>"+Messages.getMessage("PdfExtensions.getText")+"</u></html>");
                    cid.setForeground(Color.BLUE);
                    cid.addMouseListener(new Link(Messages.getMessage("PdfExtensions.CID.link")));
                }
                pane.add(cid, c);
            }catch(final Exception ee){
                ee.printStackTrace();
            }

            // Because of the following if statement, we store gridY in a variable as it is not always the same
            int gridy = 1;

            //iText
            //noinspection PointlessBooleanExpression
            if (!ItextFunctions.IS_DUMMY) {
                gridy++;
                setGridBagConstraints(c, 0, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
                pane.add(new JLabel("iText"), c);

                setGridBagConstraints(c, 1, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
                pane.add(new JLabel("<html>"+Messages.getMessage("PdfExtensions.iText.text")), c);

                setGridBagConstraints(c, 2, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
                @SuppressWarnings("UnusedAssignment") final JLabel iText;
                if (getClass().getResource("/com/itextpdf") != null) {
                    String vers = ItextFunctions.getVersion();
                    details += "itext: "+vers+ '\n';
                    vers = vers.replaceAll("1T3XT","").replaceAll("[^0-9|.]","");
                    iText = new JLabel("<html>"+vers);
                } else {
                    iText = new JLabel("<html><u>"+Messages.getMessage("PdfExtensions.getText")+"</u></html>");
                    iText.setForeground(Color.BLUE);
                    iText.addMouseListener(new Link(Messages.getMessage("PdfExtensions.iText.link")));
                }
                pane.add(iText, c);
            }
            
            //Java FX
            String version;
            gridy++;
            setGridBagConstraints(c, 0, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("JavaFX"), c);

            setGridBagConstraints(c, 1, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("<html>"+Messages.getMessage("PdfExtensions.JavaFX.text")), c);

            setGridBagConstraints(c, 2, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            final JLabel javaFX;
            if (JavaFXHelper.isJavaFXAvailable()) {
                version= JavaFXHelper.getVersion();
                javaFX = new JLabel("<html>"+version.replaceAll("build","b").replaceAll("[(|)]",""));
                details+="javafx: "+version+ '\n';
            } else {
                javaFX = new JLabel("<html><u>"+Messages.getMessage("PdfExtensions.getText")+"</u></html>");
                javaFX.setForeground(Color.BLUE);
                javaFX.addMouseListener(new Link(Messages.getMessage("PdfExtensions.JavaFX.link")));
            }
            pane.add(javaFX, c);
            
            //JCE
            gridy++;
            setGridBagConstraints(c, 0, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("JCE"), c);

            setGridBagConstraints(c, 1, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("<html>"+Messages.getMessage("PdfExtensions.JCE.text")), c);

            setGridBagConstraints(c, 2, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            JLabel jce;
            version = "Unknown version";
            try {
                final Class jcec = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                final String className = jcec.getName().replace('.', '/');
                final String[] paths = jcec.getResource('/' + className + ".class").getPath().split("!");
                final URL file = new URL(paths[0]);
                final JarFile jar = new JarFile(file.getFile());
                if (!jar.getManifest().getMainAttributes().getValue("Implementation-Version").isEmpty()) {
                    version = jar.getManifest().getMainAttributes().getValue("Implementation-Version");
                }
                
                //  NON JAR VERSION - requires an import the build didn't like, but may return better results.
                //                BouncyCastleProvider provider = new BouncyCastleProvider();
                //                version = provider.getInfo().replaceAll("[^0-9|.]", "");
                //                details+="jce: "+provider.getInfo()+"\n";
                jce = new JLabel("<html>"+version);
                details+="jce: "+version+ '\n';
            }catch(final Exception e) {
                jce = new JLabel("<html><u>"+Messages.getMessage("PdfExtensions.getText")+' '+e+"</u></html>");
                jce.setForeground(Color.BLUE);
                jce.addMouseListener(new Link(Messages.getMessage("PdfExtensions.JCE.link")));
            }
            pane.add(jce, c);
            
            //Rhino
            gridy++;
            setGridBagConstraints(c, 0, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("Rhino"), c);

            setGridBagConstraints(c, 1, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            pane.add(new JLabel("<html>"+Messages.getMessage("PdfExtensions.Rhino.text")), c);

            setGridBagConstraints(c, 2, gridy, 1, 1, 0, 0, new Insets(2,2,2,2));
            final JLabel rhino;
            final java.io.InputStream in = DefaultParser.class.getClassLoader().getResourceAsStream("org/mozilla/javascript/Context.class");
            if (in != null) {
                version = ScriptRuntime.getMessage0("implementation.version");
                details+="rhino: "+version+ '\n';
                
                String release = "";
                if (!version.replaceAll("release 1", "").equals(version)) {
                    release = " R1";
                }
                if (!version.replaceAll("release 2", "").equals(version)) {
                    release = " R2";
                }
                
                version = version.substring(0,12).replaceAll("[^0-9|.]","");
                rhino = new JLabel("<html>"+version+release);
            } else {
                rhino = new JLabel("<html><u>"+Messages.getMessage("PdfExtensions.getText")+"</u></html>");
                rhino.setForeground(Color.BLUE);
                rhino.addMouseListener(new Link(Messages.getMessage("PdfExtensions.Rhino.link")));
            }
            pane.add(rhino, c);
            
            //Add gap between table and button
            gridy++;
            setGridBagConstraints(c, 2, gridy, 1, 1, 0, 1, new Insets(2,2,2,2));
            pane.add(Box.createVerticalGlue(),c);
            
            //Add button
            gridy++;
            setGridBagConstraints(c, 0, gridy, 3, 1, 0, 0, new Insets(2,2,2,2));
            c.anchor=GridBagConstraints.LAST_LINE_END;
            c.fill=GridBagConstraints.EAST;
            final JButton copy = new JButton(Messages.getMessage("PdfPreferences.CopyToClipboard"));
            
            final String finalDetails = details;
            copy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(finalDetails),null);
                    JOptionPane.showMessageDialog(pane,Messages.getMessage("PdfExtensions.clipboard"));
                }
            });
            pane.add(copy, c);
            
            panel.add(pane, BorderLayout.CENTER);
            
            return panel;
        }
        
        private void show(final Component component) {
            if (currentComponent != null) {
                remove(currentComponent);
            }
            
            add("Center", currentComponent = component);
            revalidate();
            repaint();
        }
        
        private void addButton(final String title, final URL iconUrl, final Component component, final JPanel bar, final ButtonGroup group) {
            final Action action = new AbstractAction(title, new ImageIcon(iconUrl)) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    show(component);
                }
            };
            
            final JToggleButton button = new JToggleButton(action);
            button.setVerticalTextPosition(JToggleButton.BOTTOM);
            button.setHorizontalTextPosition(JToggleButton.CENTER);
            
            button.setContentAreaFilled(false);
            if(DecoderOptions.isRunningOnMac) {
                button.setHorizontalAlignment(AbstractButton.LEFT);
            }
            
            //Center buttons
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            bar.add(button);
            
            group.add(button);
            
            if (group.getSelection() == null) {
                button.setSelected(true);
                show(component);
            }
        }      
    }
}