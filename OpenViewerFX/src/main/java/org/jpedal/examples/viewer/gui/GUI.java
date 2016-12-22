/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
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
 * GUI.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.RecentDocumentsFactory;
import org.jpedal.examples.viewer.SharedViewer;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.generic.GUICombo;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import org.jpedal.examples.viewer.gui.generic.GUIOutline;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.exception.PdfException;
import org.jpedal.external.AnnotationHandler;
import org.jpedal.external.CustomMessageHandler;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.io.StatusBar;
import org.jpedal.linear.LinearThread;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecodeStatus;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.BaseDisplay;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.StringUtils;

/**any shared GUI code - generic and AWT*/
public abstract class GUI implements GUIFactory {

    public static boolean deletePropertiesOnExit=GUIFactory.deletePropertiesOnExit;
    public static boolean alwaysShowMouse=GUIFactory.alwaysShowMouse;
    
    protected static int expandedSize=190;
    protected static int collapsedSize=30;
    
    boolean isJavaFX;
    
    static boolean includeExtraMenus;
    
    static{
        includeExtraMenus=org.jpedal.FileAccessHelper.mode==2;
    }

    /**
     * Generic ENUMS for setting similar JavaFX and Swing values.
     */
    public enum ScrollPolicy {
        VERTICAL_AS_NEEDED, HORIZONTAL_AS_NEEDED, VERTICAL_NEVER, HORIZONTAL_NEVER
    }
    public enum PageCounter {
        PAGECOUNTER1, PAGECOUNTER2, PAGECOUNTER3, ALL
    }
        
    public static String windowTitle;

    protected RecentDocumentsFactory recent;

    /**listener on buttons, menus, combboxes to execute options (one instance on all objects)*/
    protected CommandListener currentCommandListener;

    protected Commands currentCommands;

    //allow user to control messages in Viewer
    protected CustomMessageHandler customMessageHandler;

    /**control if messages appear*/
    public static boolean showMessages=true;

    //setup in init so we can pass in some objects
    protected GUIMenuItems menuItems;

    //layers tab
    protected PdfLayerList layersObject;

    protected boolean finishedDecoding;

    public static final int CURSOR = 1;
    /** grabbing cursor */
    public static final int GRAB_CURSOR = 1;
    public static final int GRABBING_CURSOR = 2;
    public static final int DEFAULT_CURSOR = 3;
    public static final int PAN_CURSOR = 4;
    public static final int PAN_CURSORL = 5;
    public static final int PAN_CURSORTL = 6;
    public static final int PAN_CURSORT = 7;
    public static final int PAN_CURSORTR = 8;
    public static final int PAN_CURSORR = 9;
    public static final int PAN_CURSORBR = 10;
    public static final int PAN_CURSORB = 11;
    public static final int PAN_CURSORBL = 12;

    protected Font textFont=new Font("Serif",Font.PLAIN,12);

    protected Font headFont=new Font("SansSerif",Font.BOLD,14);

    protected boolean previewOnSingleScroll =true;

    /** Constants for glowing border */
    protected static final int glowThickness = 11;
    protected final Color glowOuterColor = new Color(0.0f, 0.0f, 0.0f ,0.0f);
    protected final Color glowInnerColor = new Color(0.8f, 0.75f, 0.45f, 0.8f);

    private boolean commandInThread; //If we are running command in thread do not mark command as executed at end of method, it is handled by thread.

    private boolean executingCommand;

    //private Color[] annotColors={Color.RED,Color.BLUE,Color.BLUE};

    //@annot - table of objects we wish to track
    protected Map<org.jpedal.objects.raw.FormObject, String> objs;

    //flag if generated so we setup once for each file
    protected boolean bookmarksGenerated;
    protected GUISearchWindow searchFrame;
    protected String pageTitle,bookmarksTitle, signaturesTitle,layersTitle, annotationTitle;
    
    /**handle for internal use*/
    protected PdfDecoderInt decode_pdf;

    /** minimum screen width to ensure menu buttons are visible */
    protected static final int minimumScreenWidth=700;

    private long start=System.currentTimeMillis();
    
    /**XML structure of bookmarks*/
    protected GUIOutline tree;

    /**stops autoscrolling at screen edge*/
    protected boolean allowScrolling=true;

    /**confirms exit when closing the window*/
    protected boolean confirmClose;

    /**scaling values as floats to save conversion*/
    protected float[] scalingFloatValues={1.0f,1.0f,1.0f,.25f,.5f,.75f,1.0f,1.25f,1.5f,2.0f,2.5f,5.0f,7.5f,10.0f};

    /**page scaling to use 1=100%*/
    protected float scaling = 1;

    /** padding so that the pdf is not right at the edge */
    protected static int inset=25;

    /**store page rotation*/
    protected int rotation;

    /**scaling factors on the page*/
    protected GUICombo rotationBox;

    /**scaling factors on the page*/
    protected GUICombo scalingBox;

    /**default scaling on the combobox scalingValues*/
    protected static int defaultSelection;

    protected final Values commonValues;

    protected final GUIThumbnailPanel thumbnails;

    protected final PropertiesFile properties;
    
    public static final boolean debugFX=false;
    
    public GUI(final PdfDecoderInt decode_pdf, final Values commonValues, final GUIThumbnailPanel thumbnails, final PropertiesFile properties) {
        this.decode_pdf = decode_pdf;
        this.commonValues = commonValues;
        this.thumbnails = thumbnails;
        this.properties = properties;
    }

    /**
     * Method to allow setting of common properties values
     * @param dpi int value to set the dpi property
     * @param search int value to set the search style property
     * @param border int value to set the border property
     * @param scroll true to enable autoscrolling, false to disable
     * @param pageMode int value to set the starting display view mode
     * @param updateDefaultValue true to enable automaticupdate property, false to disable
     * @param maxNoOfMultiViewers int value to set the maximum amount of files openable by the Multiviewer
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setPreferences(final int dpi, final int search, final int border, final boolean scroll, int pageMode, final boolean updateDefaultValue, final int maxNoOfMultiViewers){

        //Set border config value and repaint
        decode_pdf.setBorderPresent(border==1);
        properties.setValue("borderType", String.valueOf(border));

        //Set autoScroll default and add to properties file
        allowScrolling = scroll;
        properties.setValue("autoScroll", String.valueOf(scroll));

        //Dpi is taken into effect when zoom is called
        decode_pdf.getDPIFactory().setDpi(dpi);
        properties.setValue("resolution", String.valueOf(dpi));

        //Ensure valid value if not recognised
        if(pageMode<Display.SINGLE_PAGE || pageMode>Display.PAGEFLOW) {
            pageMode = Display.SINGLE_PAGE;
        }

        //Default Page Layout
        decode_pdf.setPageMode(pageMode);
        properties.setValue("startView", String.valueOf(pageMode));

        decode_pdf.repaint();

        //Set the search window
        final String propValue = properties.getValue("searchWindowType");
        if((!propValue.isEmpty() && !propValue.equals(String.valueOf(search))) && (showMessages) ){
                ShowGUIMessage.showGUIMessage(Messages.getMessage("PageLayoutViewMenu.ResetSearch"), null);
            }
        properties.setValue("searchWindowType", String.valueOf(search));

        properties.setValue("automaticupdate", String.valueOf(updateDefaultValue));

        commonValues.setMaxMiltiViewers(maxNoOfMultiViewers);
        properties.setValue("maxmultiviewers", String.valueOf(maxNoOfMultiViewers));

    }
    
    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#allowScrolling()
     */
    @Override
    public boolean allowScrolling() {
        return allowScrolling;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#confirmClose()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean confirmClose() {
        return confirmClose;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setAutoScrolling(boolean allowScrolling)
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setAutoScrolling(final boolean allowScrolling) {
        this.allowScrolling=allowScrolling;

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#toogleAutoScrolling()
     */
    @Override
    public void  toogleAutoScrolling(){
        allowScrolling=!allowScrolling;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getRotation()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public int getRotation() {
        return rotation;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getScaling()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public float getScaling() {
        return scaling;
    }

    /**
     * Get the values used by the GUI
     * @return Values object to hold various details for the viewer
     */
    @Override
    public Values getValues(){
        return commonValues;
    }
    
    /* (non-Javadoc)
	 * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#addCombo(java.lang.String, java.lang.String, int)
	 */
    protected void addCombo(final String tooltip, final int ID){

        if(debugFX){
            System.out.println("addCombo");
        }

        GUICombo combo=null;
        switch (ID){

            case Commands.SCALING:
                combo=scalingBox;
                break;
            case Commands.ROTATION:
                combo=rotationBox;
                break;
        }

        combo.setID(ID);


        if(!tooltip.isEmpty()) {
            combo.setToolTipText(tooltip);
        }


        addGUIComboBoxes(combo);

        addComboListenerAndLabel(combo);
    }

    /**
     * get Map containing Form Objects setup for Unique Annotations
     *
     * @return Map
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public Map getHotspots() {

        return Collections.unmodifiableMap(objs);
    }

    /**
     * Set the scaling for the user interface and update the scaling combo box
     * @param s float value representing the scaling where 1 is 100%
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setScaling(final float s){
        scaling = s;
        scalingBox.setSelectedIndex((int) scaling);
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getPDFDisplayInset()
     */
    public static int getPDFDisplayInset() {
        return inset;
    }

    //<link><a name="handleAnnotations" />
    /** example code which sets up an individual icon for each annotation to display - only use
     * if you require each annotation to have its own icon<p>
     * To use this you ideally need to parse the annotations first -there is a method allowing you to
     * extract just the annotations from the data.
     */
    public void createUniqueAnnotationIcons() {

        //and place to store so we can test later
        //flush list if needed
        if(objs==null) {
            objs = new HashMap<org.jpedal.objects.raw.FormObject, String>();
        } else {
            objs.clear();
        }

        //create Annots - you can replace with your own implementation using setExternalHandler()
        ((AnnotationHandler)decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler)).handleAnnotations(decode_pdf, objs, commonValues.getCurrentPage());

    }

    /**
     * Set the dpi value used to adjust scaling
     * @param dpi int value to represent the dpi
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDpi(final int dpi) {
        decode_pdf.getDPIFactory().setDpi(dpi);
    }

    /**
     * Dispose of the elements that require disposal
     */
    @Override
    public void dispose(){
        tree=null;
        scalingFloatValues=null;
        rotationBox=null;
        scalingBox=null;
    }
    
    /**
     * main method to initialise Swing specific code and create GUI display
     * @param currentCommands Commands object to be used by the user interface
     */
    @Override
    public void init(final Commands currentCommands) {

        //setup custom message and switch off error messages if used
        customMessageHandler = (CustomMessageHandler) (decode_pdf.getExternalHandler(Options.CustomMessageOutput));
        if (customMessageHandler != null) {
            DecoderOptions.showErrorMessages = false;
            GUI.showMessages = false;
        }

        /*
         * Set up from properties
         */
        String propValue = properties.getValue("pageInsets");
        if (!propValue.isEmpty()) {
            inset = Integer.parseInt(propValue);
        }

        propValue = properties.getValue("changeTextAndLineart");
        if (!propValue.isEmpty()
                && propValue.equalsIgnoreCase("true")) {
            currentCommands.executeCommand(Commands.CHANGELINEART, new Object[]{Boolean.parseBoolean(propValue)});
        }

        propValue = properties.getValue("windowTitle");
        if (!propValue.isEmpty()) {
            windowTitle = propValue;
        } else {
            windowTitle=getTitle();
        }

        propValue = properties.getValue("vbgColor");
        if (!propValue.isEmpty()) {
            currentCommands.executeCommand(Commands.SETPAGECOLOR, new Object[]{Integer.parseInt(propValue)});
        }

        propValue = properties.getValue("replaceDocumentTextColors");
        if (!propValue.isEmpty()
                && propValue.equalsIgnoreCase("true")) {

            propValue = properties.getValue("vfgColor");
            if (!propValue.isEmpty()) {
                currentCommands.executeCommand(Commands.SETTEXTCOLOR, new Object[]{Integer.parseInt(propValue)});
            }

        }

        propValue = properties.getValue("TextColorThreshold");
        if (!propValue.isEmpty()) {
            currentCommands.executeCommand(Commands.SETREPLACEMENTCOLORTHRESHOLD, new Object[]{Integer.parseInt(propValue)});
        }

        propValue = properties.getValue("enhanceFractionalLines");
        if (!propValue.isEmpty()) {
            currentCommands.executeCommand(Commands.SETENHANCEFRACTIONALLINES, new Object[]{Boolean.parseBoolean(propValue)});
        }

        //Set autoScroll default and add to properties file
        propValue = properties.getValue("autoScroll");
        if (!propValue.isEmpty()) {
            allowScrolling = Boolean.getBoolean(propValue);
        }

        //set confirmClose
        propValue = properties.getValue("confirmClose");
        if (!propValue.isEmpty()) {
            confirmClose = propValue.equals("true");
        }

        //Dpi is taken into effect when zoom is called
        propValue = properties.getValue("resolution");
        if (!propValue.isEmpty()) {
            decode_pdf.getDPIFactory().setDpi(Integer.parseInt(propValue));
        }

        //Ensure valid value if not recognised
        propValue = properties.getValue("startView");

        if (!propValue.isEmpty()) {
            int pageMode = Integer.parseInt(propValue);
            //  pageMode=2;
            //  System.out.println(SwingUtilities.isEventDispatchThread());
            if (pageMode < Display.SINGLE_PAGE || pageMode > Display.PAGEFLOW) {
                pageMode = Display.SINGLE_PAGE;
            }
            //Default Page Layout
            decode_pdf.setPageMode(pageMode);
        }

        propValue = properties.getValue("maxmuliviewers");
        if (!propValue.isEmpty()) {
            commonValues.setMaxMiltiViewers(Integer.parseInt(propValue));
        }

        final String val = properties.getValue("highlightBoxColor"); //empty string to old users
        if (!val.isEmpty()) {
            DecoderOptions.highlightColor = new Color(Integer.parseInt(val));
        }

        propValue = properties.getValue("highlightTextColor");
        if (!propValue.isEmpty()) {
            DecoderOptions.backgroundColor = new Color(Integer.parseInt(propValue));
        }

        propValue = properties.getValue("showMouseSelectionBox");
        if (!propValue.isEmpty()) {
            DecoderOptions.showMouseBox = Boolean.valueOf(propValue);
        }

        propValue = properties.getValue("enhancedViewerMode");
        if (!propValue.isEmpty()) {
            decode_pdf.useNewGraphicsMode(Boolean.valueOf(propValue));
        }

        propValue = properties.getValue("highlightComposite");
        if (!propValue.isEmpty()) {
            float value = Float.parseFloat(propValue);
            if (value > 1) {
                value = 1;
            }
            if (value < 0) {
                value = 0;
            }

            DecoderOptions.highlightComposite = value;
        }

        //Set border config value and repaint
        propValue = properties.getValue("borderType");
        if (!propValue.isEmpty()) {
            decode_pdf.setBorderPresent(Integer.parseInt(propValue)==1);
        }

        //Allow cursor to change
        propValue = properties.getValue("allowCursorToChange");
        if (!propValue.isEmpty()) {
            GUIDisplay.allowChangeCursor = propValue.equalsIgnoreCase("true");
        }

        propValue = properties.getValue("invertHighlights");
        if (!propValue.isEmpty()) {
            BaseDisplay.invertHighlight = Boolean.valueOf(propValue);
        }

        propValue = properties.getValue("enhancedFacingMode");
        if (!propValue.isEmpty()) {
            GUIDisplay.default_turnoverOn = Boolean.valueOf(propValue);
        }

        this.currentCommands = currentCommands;

        setViewerTitle(windowTitle);
        setViewerIcon();

        /*
         * arrange insets
         */
        decode_pdf.setInset(inset, inset);

    }

    protected String getTitle(){
        return Messages.getMessage("PdfViewer.titlebar") + ' ' + PdfDecoderInt.version;
    }
    
    /**
     * Get if command is in a thread
     * @return true if command is in thread, false otherwise.
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean isCommandInThread(){
        return commandInThread;
    }

    /**
     * Used to flag if command is in a thread
     * @param b true to flag command is in thread, false otherwise.
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setCommandInThread(final boolean b){
        commandInThread = b;
    }

    /**
     * Get if their is currently a command executing
     * @return true if command is executing, false otherwise
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean isExecutingCommand(){
        return executingCommand;
    }

    /**
     * Used to flag if command is executing
     * @param b true to flag command is executing, false otherwise.
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setExecutingCommand(final boolean b){
        executingCommand = b;
    }

    protected static void getFlattenedTreeNodes(final TreeNode theNode, final List<TreeNode> items) {
        // add the item
        items.add(theNode);

        // recursion
        for (final Enumeration theChildren = theNode.children(); theChildren.hasMoreElements();) {
            getFlattenedTreeNodes((TreeNode) theChildren.nextElement(), items);
        }
    }

    /**
     * Get the thickness of the glow border
     * @return int value of the glow border thickness
     */
    @SuppressWarnings("MethodMayBeStatic")
    public int getGlowThickness(){
        return glowThickness;
    }

    /**
     * Get the outer colour of the glow border
     * @return Color used for the outer section of the glow border
     */
    public Color getGlowOuterColor(){
        return glowOuterColor;
    }

    /**
     * Get the inner colour of the glow border
     * @return Color used for the inner section of the glow border
     */
    public Color getGlowInnerColor(){
        return glowInnerColor;
    }

    /**
     * Set the search frame to be used as part of the user interface
     * @param searchFrame GUISearchWindow object to be used as the search window
     */
    @Override
    public void setSearchFrame(final GUISearchWindow searchFrame) {
        this.searchFrame = searchFrame;
    }

    protected void setRotation(){
        //PdfPageData currentPageData=decode_pdf.getPdfPageData();
        //rotation=currentPageData.getRotation(commonValues.getCurrentPage());

        //Broke files with when moving from rotated page to non rotated.
        //The pages help previous rotation
        //rotation = (rotation + (getSelectedComboIndex(Commands.ROTATION)*90));

        if(rotation > 360) {
            rotation -= 360;
        }

        if(getSelectedComboIndex(Commands.ROTATION)!=(rotation/90)){
            setSelectedComboIndex(Commands.ROTATION, (rotation/90));
        }else if(!Values.isProcessing() && !SharedViewer.isFX()){
//            decode_pdf.repaint();
        }
    }
    
    /**
     * Get selected index for the specified combo-box
     * @param ID int value specifying a combo-box
     * @return int value of the selected index in the given combo-box or -1 if ID is not valid
     */
    public int getSelectedComboIndex(final int ID) {

        switch (ID){
            case Commands.SCALING:
                return scalingBox.getSelectedIndex();
            case Commands.ROTATION:
                return rotationBox.getSelectedIndex();
            default:
                return -1;
        }
    }
    
    /**
     * Set selected index for the specified combo-box
     * @param ID int value specifying a combo-box
     * @param index int value of the index to select
     */
    public void setSelectedComboIndex(final int ID, final int index) {
        switch (ID){
            case Commands.SCALING:
                scalingBox.setSelectedIndex(index);
                break;
            case Commands.ROTATION:
                rotationBox.setSelectedIndex(index);
                break;

        }

    }
    
    /**
     * Get selected item for the specified combo-box
     * @param ID int value specifying a combo-box
     * @return Object representing the selected item or null if ID is not recognised
     */
    public Object getSelectedComboItem(final int ID) {

        switch (ID){
            case Commands.SCALING:
                return scalingBox.getSelectedItem();
            case Commands.ROTATION:
                return rotationBox.getSelectedItem();
            default:
                return null;

        }
    }
    
    /**
     * Set selected item for the specified combo-box
     * @param ID int value specifying a combo-box
     * @param index String value of the item to select
     */
    public void setSelectedComboItem(final int ID,String index) {
        switch (ID){
            case Commands.SCALING:
                //When using any of the fit scalings, adding a % will break it
                //Only add if scaling is a number
                if(StringUtils.isNumber(index)) {
                    index += '%';
                }
                scalingBox.setSelectedItem(index);
                break;
            case Commands.ROTATION:
                rotationBox.setSelectedItem(index);
                break;

        }
    }

    /**
     * Not part of API - used internally
     * 
     * Set the PdfDecoderInt object used by the user interface
     * @param decode_pdf PdfDecoderInt used for the PDF opened in the viewer
     */
    public void setPdfDecoder(final PdfDecoderInt decode_pdf){
        this.decode_pdf = decode_pdf;
    }

    private void prepareForDecode(final GUIFactory currentGUI){
        //Remove Image extraction outlines when page is changed
        decode_pdf.getPages().setHighlightedImage(null);

        currentGUI.resetRotationBox();

        /* if running terminate first */
        if(thumbnails.isShownOnscreen()) {
            thumbnails.terminateDrawing();
        }

        if(thumbnails.isShownOnscreen()){
            currentGUI.setupThumbnailPanel();
            
            final LinearThread linearizedBackgroundRenderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

            if(linearizedBackgroundRenderer!=null && !linearizedBackgroundRenderer.isAlive()) {
                thumbnails.drawThumbnails();
            }
        }

        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
            currentGUI.setPageCounterText(PageCounter.PAGECOUNTER2, currentGUI.getPageLabel(commonValues.getCurrentPage()));
            if(pageLabelDiffers(commonValues.getCurrentPage())){
                currentGUI.setPageCounterText(PageCounter.PAGECOUNTER3, "(" + commonValues.getCurrentPage() + ' ' + Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount()+")");
            }else{
                currentGUI.setPageCounterText(PageCounter.PAGECOUNTER3, Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount());
            }
        }

        currentGUI.updateTextBoxSize();

        //allow user to now open tabs
        currentGUI.setTabsNotInitialised(false);

        //ensure text and color extracted. If you do not need color, take out line for faster decode
//		decode_pdf.setExtractionMode(PdfDecoderInt.TEXT);
        decode_pdf.setExtractionMode(PdfDecoderInt.TEXT+PdfDecoderInt.TEXTCOLOR);


        //remove any search highlight
        decode_pdf.getTextLines().clearHighlights();
    }
    
    private void performDecoding(final GUIFactory currentGUI){
        Values.setProcessing(true);

        //SwingWorker worker = new SwingWorker() {

        setCursor(2);


        if(LogWriter.isRunningFromIDE){
            start=System.currentTimeMillis();
        }

        try {
            if(!SharedViewer.isFX()){
                ((StatusBar)currentGUI.getStatusBar()).updateStatus("Decoding Page",0);
            }

            /*
             * decode the page
             */
            try {
                decode_pdf.decodePage(commonValues.getCurrentPage());

                //wait to ensure decoded
                decode_pdf.waitForDecodingToFinish();


                //value set from JVM flag org.jpedal.maxShapeCount=maxNumber
                if(decode_pdf.getPageDecodeStatus(DecodeStatus.TooManyShapes)){

                    final String status = "Too many shapes on page";

                    currentGUI.showMessageDialog(status);
                }


                if(!decode_pdf.getPageDecodeStatus(DecodeStatus.ImagesProcessed)){

                    final String status = (Messages.getMessage("PdfViewer.ImageDisplayError")+
                            Messages.getMessage("PdfViewer.ImageDisplayError1")+
                            Messages.getMessage("PdfViewer.ImageDisplayError2")+
                            Messages.getMessage("PdfViewer.ImageDisplayError3")+
                            Messages.getMessage("PdfViewer.ImageDisplayError4")+
                            Messages.getMessage("PdfViewer.ImageDisplayError5")+
                            Messages.getMessage("PdfViewer.ImageDisplayError6")+
                            Messages.getMessage("PdfViewer.ImageDisplayError7"));

                    currentGUI.showMessageDialog(status);
                }

                /*
                 * Tell user if hinting is probably required
                 */
                if(decode_pdf.getPageDecodeStatus(DecodeStatus.TTHintingRequired)){

                    final String status = Messages.getMessage("PdfCustomGui.ttHintingRequired");

                    currentGUI.showMessageDialog(status);
                }

                if(decode_pdf.getPageDecodeStatus(DecodeStatus.NonEmbeddedCIDFonts)){

                    final String status = ("This page contains non-embedded CID fonts \n" +
                            decode_pdf.getPageDecodeStatusReport(DecodeStatus.NonEmbeddedCIDFonts)+
                            "\nwhich may need mapping to display correctly.\n" +
                            "See http://www.idrsolutions.com/how-do-fonts-work");

                    currentGUI.showMessageDialog(status);
                }

                //create custom annot icons
                if(decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler)!=null){
                    /*
                     * ANNOTATIONS code to create unique icons
                     *
                     * this code allows you to create a unique set on icons for any type of annotations, with
                     * an icons for every annotation, not just types.
                     */
                    final FormFactory formfactory = decode_pdf.getFormRenderer().getFormFactory();

                    //swing needs it to be done with invokeLater
                    if(formfactory.getType()== FormFactory.SWING){
                        final Runnable doPaintComponent2 = new Runnable() {
                            @Override
                            public void run() {

                                createUniqueAnnotationIcons();

                                //validate();
                            }
                        };
                        SwingUtilities.invokeLater(doPaintComponent2);

                    }else{
                        createUniqueAnnotationIcons();
                    }


                }
            if(!SharedViewer.isFX()){
                ((StatusBar)currentGUI.getStatusBar()).updateStatus("Displaying Page",0);
            }

            } catch (final Exception e) {
                System.err.println(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e +
                        ' ' +Messages.getMessage("PdfViewerError.DecodePage"));
                e.printStackTrace();
                Values.setProcessing(false);
            }


            //tell user if we had a memory error on decodePage
            if(DecoderOptions.showErrorMessages){
                String status=decode_pdf.getPageDecodeReport();
                if(status.contains("java.lang.OutOfMemoryError")){
                    status = (Messages.getMessage("PdfViewer.OutOfMemoryDisplayError")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError1")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError2")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError3")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError4")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError5"));

                    currentGUI.showMessageDialog(status);

                }
            }

            Values.setProcessing(false);

            //make sure fully drawn
            //decode_pdf.repaint();

            currentGUI.setViewerTitle(null); //restore title


            if(LogWriter.isRunningFromIDE){
                /*
                 * show time and memory usage
                 */
                System.out
                        .println(((Runtime.getRuntime().totalMemory() - Runtime
                                .getRuntime().freeMemory()) / 1000)
                                + "K");
                System.out.println((((float) Math.abs(((System
                        .currentTimeMillis() - start) / 100))) / 10)
                        + "s");

            }
            
            if (decode_pdf.getPageCount()>0 && thumbnails.isShownOnscreen() && decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                thumbnails.generateOtherVisibleThumbnails(commonValues.getCurrentPage());
            }

        } catch (final Exception e) {
            e.printStackTrace();
            Values.setProcessing(false);//remove processing flag so that the viewer can be exited.
            currentGUI.setViewerTitle(null); //restore title
        }
    }
    
    /**
     * called by nav functions to decode next page (in GUI code as needs to
     * manipulate large part of GUI)
     */
    public void decodeGUIPage(final GUIFactory currentGUI){
        
        //Prepare GUI for decoding
        prepareForDecode(currentGUI);

        //kick-off thread to create pages
        if(decode_pdf.getDisplayView() == Display.FACING){

            currentGUI.scaleAndRotate();
            currentGUI.scrollToPage(commonValues.getCurrentPage());

            decode_pdf.getPages().decodeOtherPages(commonValues.getCurrentPage(),commonValues.getPageCount());

            return ;
        }else if(decode_pdf.getDisplayView() == Display.CONTINUOUS || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){

            //resize (ensure at least certain size)
            //must be here as otherwise will not redraw if new page opened
            //in multipage mode
            currentGUI.scaleAndRotate();

            currentGUI.scrollToPage(commonValues.getCurrentPage());

                if(!SharedViewer.isFX()){
                return ;
            }
        }else if(decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            return;
        }

        //stop user changing scaling while decode in progress
        currentGUI.resetComboBoxes(false);
        currentGUI.getButtons().setPageLayoutButtonsEnabled(false);

        //Decoding happens here
        performDecoding(currentGUI);

        //Update multibox
        if(!SharedViewer.isFX()){
            ((StatusBar)currentGUI.getStatusBar()).setProgress(100);
        }

        currentGUI.setMultibox(new int[]{});

        //reanable user changing scaling
        currentGUI.resetComboBoxes(true);

        if(decode_pdf.getPageCount()>1) {
            currentGUI.getButtons().setPageLayoutButtonsEnabled(true);
        }
        
        /*
         * if page has transition we will have stored values earlier and now need to use and remove
         */
        if(isJavaFX){
            FXAdditionalData additionaValuesforPage=(FXAdditionalData) decode_pdf.getExternalHandler(Options.JavaFX_ADDITIONAL_OBJECTS);
            
            if(additionaValuesforPage!=null){
                
               DynamicVectorRenderer fxDisplay= decode_pdf.getDynamicRenderer();
                
                try {
                    fxDisplay.drawAdditionalObjectsOverPage(additionaValuesforPage.getType(), null,additionaValuesforPage.getObj());
                } catch (PdfException ex) {
                    org.jpedal.utils.LogWriter.writeLog("Exception attempting to draw additional objects " + ex);
                }
                
            }
        }
        
        if(currentGUI.getFrame() != null){
            currentGUI.reinitialiseTabs(currentGUI.getDividerLocation() > currentGUI.getStartSize());
        }

        finishedDecoding=true;
        
        //Ensure page is at the correct scaling and rotation for display
        currentGUI.scaleAndRotate();
        
        setCursor(1);
        
    }

    void setCursor(int type) {
        //only used in Swing implementation
    }

    /*
     * Set title to display on top of Swing of FX viewer (include days left on trial version)
     *(non-Javadoc)
	 * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#addCombo(java.lang.String, java.lang.String, int)
	 */
    @Override
    public void setViewerTitle(String title) {

        if(title!=null && FileAccess.bb>0){

             title="("+FileAccess.bb+" days left) "+title;
           
        }else{
            
            //set null title value to empty string
            if(commonValues.getSelectedFile()==null){
                title=(windowTitle+' ');
            }else{
                title=(windowTitle+' ' + commonValues.getSelectedFile());
            }

            final PdfObject linearObj=(PdfObject)decode_pdf.getJPedalObject(PdfDictionary.Linearized);
            if(linearObj!=null){
                final LinearThread linearizedBackgroundReaderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

                if(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive()) {
                    title += " (still loading)";
                } else {
                    title += " (Linearized)";
                }
            }

            if(commonValues.isFormsChanged() && !isJavaFX) {
                title = "* " + title;
            }
        }

        setTitle(title);
    }

    /**
     * Sets the title for the Viewer.
     */
    protected abstract void setTitle(final String title);

    /**
     * Sets the icon for the Viewer.
     */
    protected void setViewerIcon() {
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds listener for the ComboBoxes and Title.
     */
    protected void addComboListenerAndLabel(final GUICombo combo) {
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds the ComboBoxes to theViewer.
     * Resize box & rotation box.
     */
    protected void addGUIComboBoxes(final GUICombo combo){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets the background for the main center display
     * panel which holds the PDF content pane.
     */
    protected void setupCenterPanelBackground(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Performs initial setup of the ComboBoxes.
     * Resize box & rotation box.
     */
    protected void setupComboBoxes(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds key listeners for keyboard navigation of the Viewer.
     */
    protected void setupKeyboardControl(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the central display pane to display the pdf content.
     */
    protected void setupPDFDisplayPane(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the panes to the left and right of the central display.
     * Bookmarks & Thumbnails Pages etc.
     */
    protected void setupBorderPanes(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    protected void createOtherToolBars(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the position & visual style of the items on
     * the bottom toolbar (page navigation buttons etc).
     */
    protected void setupBottomToolBarItems(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates a glowing border around the PDFDisplayPane.
     */
    protected void setupPDFBorder(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates the top two menu bars, the file loading & viewer properties one
     * and the PDF toolbar, the one which controls printing, searching etc.
     */
    protected void createTopMenuBar(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates the Main Display Window for all of the JavaFX Content.
     *
     * @param width is of type int
     * @param height is of type int
     */
    protected void createMainViewerWindow(final int width, final int height){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    protected void setupSidebarTitles(){
        pageTitle = Messages.getMessage("PdfViewerJPanel.thumbnails");
        bookmarksTitle = Messages.getMessage("PdfViewerJPanel.bookmarks");
        layersTitle = Messages.getMessage("PdfViewerJPanel.layers");
        signaturesTitle = Messages.getMessage("PdfViewerJPanel.signatures");
        annotationTitle = Messages.getMessage("PdfViewerJPanel.annotations");
    }

    /**
     * Get the label of the given page. PDFs can specify a label for a page
     * (such as using Roman numerals), the method will return the page label if 
     * one exists, otherwise it will return the default of the page number.
     * 
     * @param pageNumber int value to present the page number
     * @return String value representing the page label for the page.
     */
    @Override
    public String getPageLabel(int pageNumber) {
        if(commonValues.isPDF()){ //Only check labels if pdf
            String value = decode_pdf.getIO().convertPageNumberToLabel(pageNumber);
            if (value != null) {
                return value;
            }
        }
        return String.valueOf(pageNumber);
    }
    
    /**
     * Get if the page label for a given page differs from the default page numbers
     * 
     * @param pageNumber int value representing the page number
     * @return true if the page label differs, false otherwise
     */
    public boolean pageLabelDiffers(int pageNumber){
        String value = decode_pdf.getIO().convertPageNumberToLabel(pageNumber);
        if (value != null) {
            return !value.equals(String.valueOf(pageNumber));
        }
        return false;
    }
}