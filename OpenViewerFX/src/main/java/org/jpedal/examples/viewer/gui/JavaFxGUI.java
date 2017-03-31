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
 * JavaFxGUI.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;

import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.display.GUIModes;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.display.PageOffsets;
import org.jpedal.display.javafx.PageFlowDisplayFX;
import org.jpedal.display.javafx.SingleDisplayFX;
import org.jpedal.examples.viewer.*;
import org.jpedal.examples.viewer.commands.OpenFile;
import org.jpedal.examples.viewer.commands.javafx.JavaFXOpenFile;
import org.jpedal.examples.viewer.gui.generic.*;
import org.jpedal.examples.viewer.gui.javafx.*;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionDirection;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionType;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXInputDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXOptionDialog;
import org.jpedal.examples.viewer.paper.PaperSizes;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.external.Options;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.StatusBarFX;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.w3c.dom.Node;

/**
 * <br>Description: GUI functions in Viewer implemented in JavaFX
 */
@SuppressWarnings("MagicConstant")
public class JavaFxGUI extends GUI {

    private RefreshLayout viewListener;

    protected final JavaFXButtons fxButtons;

    //GUICursor object that holds everything todo with Cursor for SwingGUI
    private final SwingCursor guiCursor = new SwingCursor();

    //all mouse actions
    private GUIMouseHandler mouseHandler;

    private Timeline memoryMonitor;

    //flag for marks new thumbnail preview
    private boolean debugThumbnail;

    private boolean sideTabBarOpenByDefault;
    private String startSelectedTab = "Pages";

    //use new GUI layout
//    public static String windowTitle;

    private boolean hasListener;
    private boolean isSetup;
    private int lastTabSelected = -1;
    private boolean tabsExpanded;

    private PaperSizes paperSizes;

    /*
     * Multibox for new GUI Layout Component to contain memory, cursor and
     * loading bars IMPORTANT : the width of the multibox needs to be the same
     * size as the width of pagesToolBar to ensure that it is centered on the
     * page.
     */
    private HBox multiboxfx;
    private FXProgressBarWithText memoryBarFX;

    //visual display of current cursor co-ords on page
    private TextField coordsFX;

    //Track whether both pages are properly displayed
    private static final boolean pageTurnScalingAppropriate = true;

    //holds back/forward buttons at bottom of page
    private HBox navButtons;

    //displayed on left to hold thumbnails, bookmarks
    private TabPane navOptionsPanel;

    //tell user on first form change it can be saved
    private static final boolean firstTimeFormMessage = true;

    private Label pageCounter1;

    public TextField pageCounter2;

    private Label pageCounter3;

    private final JavaFXSignaturesPanel signaturesPanel;

    private final JavaFXLayersPanel layersPanel;

    //stop user forcing open tab before any pages loaded
    private boolean tabsNotInitialised = true;

    //private boolean thumbnailsInitialised;
    private boolean hasPageChanged;
    private ToolBar navToolBar;
    private ToolBar pagesToolBar;

    //The main JavaFX Stage
    private final Stage stage;

    //The main JavaFX Scene
    private Scene scene;

    //BorderPane lays out children in top, left, right, bottom, and center positions. 
    private final BorderPane root;
    private static final int dropshadowDepth = 40;

    private Group group;

    // Scrollpane to contain the PDF page
    private ScrollPane pageContainer;
    private SplitPane center;
    private final VBox topPane;

    private StatusBarFX downloadBar;

    private boolean searchInMenu;

    private TextField searchText;
    private GUISearchList results;

    private MenuBar options;

    private boolean cursorOverPage;

    private TransitionType transitionType = TransitionType.None;

    // Scrolls between pages when the pdf is not zoomed in
    private ScrollBar pageScroll;

    private ChangeListener<Bounds> fxChangeListener;

    public JavaFxGUI(final Stage stage, final PdfDecoderInt decode_pdf, final Values commonValues, final GUIThumbnailPanel thumbnails, final PropertiesFile properties) {
        super(decode_pdf, commonValues, thumbnails, properties);

        isJavaFX = true;

        if (debugFX) {
            System.out.println("JavaFxGUI");
        }

        this.stage = stage;

        root = new BorderPane();
        topPane = new VBox();
        pageCounter2 = new TextField();
        navButtons = new HBox();
        fxButtons = new JavaFXButtons();
        pageCounter3 = new Label();
        navToolBar = new ToolBar();
        pagesToolBar = new ToolBar();
        tree = new JavaFXOutline();
        layersPanel = new JavaFXLayersPanel();
        signaturesPanel = new JavaFXSignaturesPanel();

        setLookAndFeel();
        setupDisplay();
    }

    /**
     * Returns the scroll bar used in single page mode that shows page thumbnails as you scroll
     *
     * @return ScrollBar used for the thumbnail scroll in single page mode
     */
    @Override
    public Object getThumbnailScrollBar() {

        if (debugFX) {
            System.out.println("getThumbnailScrollBar - Implemented");
        }

        return pageScroll;
    }

    /**
     * Control thumbnail scroll bars visibility
     *
     * @param isVisible boolean value to control visibility
     */
    @Override
    public void setThumbnailScrollBarVisibility(final boolean isVisible) {
        if (debugFX) {
            System.out.println("setThumbnailScrollBarVisibility - Implemented");
        }
        pageScroll.setVisible(isVisible);
        //#making invisible still shows the gap so iam padding negative to hide it
        if (isVisible) {
            pageScroll.setStyle("-fx-padding:0px");
        } else {
            pageScroll.setStyle("-fx-padding:-10px");
        }
    }

    /**
     * Set the position of the scroll bar based on desired page number
     *
     * @param pageNum int value for the page
     */
    @Override
    public void setThumbnailScrollBarValue(final int pageNum) {
        if (debugFX) {
            System.out.println("setThumbnailScrollBarValue - Implemented");
        }
        pageScroll.setValue(pageNum);
    }

    /**
     * set the look and feel for the GUI components to be the default for the
     * system it is running on
     */
    private void setLookAndFeel() {
        if (debugFX) {
            System.out.println("setLookAndFeel - Not yet implemented in JavaFX");
        }
        /*
         * JavaFX has no look and feel option, 2.2 has a theme called Caspian
         * (the default theme) only. FX8 has Moderna and Caspian, which can be
         * set in the class which overrides Application with
         * setUserAgentStylesheet(url)
         *
         * We can also define our own rules on top of this with:
         * root.getStylesheets().clear();
         * root.getStylesheets().add("stylesheetlocation"); Also see:
         * http://blog.idrsolutions.com/2014/04/use-external-css-files-javafx/
         */
        root.getStylesheets().clear();
        root.getStylesheets().add(getClass().getResource("/org/jpedal/examples/viewer/res/css/JavaFXForms.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/org/jpedal/examples/viewer/res/css/JavaFXPages.css").toExternalForm());
    }

    private void setupDisplay() {

        setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

        //pass in SwingGUI so we can call via callback
        decode_pdf.addExternalHandler(this, Options.GUIContainer);

        menuItems = new JavaFXMenuItems(properties);

    }

    /**
     * Returns the split pane used in the display area of the viewer.
     *
     * @return SplitPane used to hold the page display and the side tab bar
     */
    @Override
    public SplitPane getDisplayPane() {
        return center;
    }

    /**
     * Used in Swing MultiViewer only
     */
    @Override
    public Object getMultiViewerFrames() {

        if (debugFX) {
            System.out.println("getMultiViewerFrames");
        }

        return null;
    }

    /**
     * Gets the file path for the properties file used in the viewer.
     *
     * @return String containing the properties file name
     */
    @Override
    public String getPropertiesFileLocation() {

        if (debugFX) {
            System.out.println("getPropertiesFileLocation");
        }

        return properties.getConfigFile();
    }

    /**
     * Gets the page number of the given bookmark
     *
     * @param bookmark String value of the bookmark
     * @return String value of the page number
     */
    @Override
    public String getBookmark(final String bookmark) {

        if (debugFX) {
            System.out.println("getBookmark");
        }

        return tree.getPage(bookmark);
    }

    /**
     * Set the side tab bar state and set any values required for that state.
     *
     * @param showVisible true to expand the side tab bar, false to collapse
     */
    @Override
    public void reinitialiseTabs(final boolean showVisible) {

        if (debugFX) {
            System.out.println("reinitialiseTabs");
        }

        //not needed
        if (commonValues.getModeOfOperation() == Values.RUNNING_PLUGIN) {
            return;
        }

        if (properties.getValue("ShowSidetabbar").equalsIgnoreCase("true")) {

            if (!showVisible && !properties.getValue("consistentTabBar").equalsIgnoreCase("true")) {
                if (sideTabBarOpenByDefault) {
                    setSplitDividerLocation(expandedSize);
                    tabsExpanded = true;
                } else {
                    setSplitDividerLocation(collapsedSize);
                    tabsExpanded = false;
                }
            }
            lastTabSelected = -1;

            if (!commonValues.isPDF()) {
                navOptionsPanel.setVisible(false);
            } else {
                navOptionsPanel.setVisible(true);

                /*
                 * add/remove optional tabs
                 */
                if (!decode_pdf.hasOutline()) {

                    int outlineTab = -1;

                    //see if there is an outlines tab
                    for (int jj = 0; jj < navOptionsPanel.getTabs().size(); jj++) {
                        if (navOptionsPanel.getTabs().get(jj).getText().equals(bookmarksTitle)) {
                            outlineTab = jj;
                        }
                    }

                    if (outlineTab != -1) {
                        navOptionsPanel.getTabs().remove(outlineTab);
                    }

                } else if (properties.getValue("Bookmarkstab").equalsIgnoreCase("true")) {
                    int outlineTab = -1;

                    //see if there is an outlines tab
                    for (int jj = 0; jj < navOptionsPanel.getTabs().size(); jj++) {
                        if (navOptionsPanel.getTabs().get(jj).getText().equals(bookmarksTitle)) {
                            outlineTab = jj;
                        }
                    }

                    if (outlineTab == -1) {
                        navOptionsPanel.getTabs().add(navOptionsPanel.getTabs().size() - 1, (Tab) tree);
                    }
                }

                /*
                 * handle signatures pane
                 */
                final AcroRenderer currentFormRenderer = decode_pdf.getFormRenderer();

                Iterator<FormObject> signatureObjects = null;

                if (currentFormRenderer != null) {
                    signatureObjects = currentFormRenderer.getSignatureObjects();
                }

                if (signatureObjects != null) {
                    signaturesPanel.reinitialise(decode_pdf, signatureObjects);
                    checkTabShown(signaturesTitle);
                } else {
                    removeTab(signaturesTitle);
                }


                /*
                 * add a control Panel to enable/disable layers
                 */
                //layers object
                layersObject = (PdfLayerList) decode_pdf.getJPedalObject(PdfDictionary.Layer);

                if (layersObject != null && layersObject.getLayersCount() > 0) { //some files have empty Layers objects

                    checkTabShown(layersTitle);
                    layersPanel.reinitialise(layersObject, decode_pdf, null, commonValues.getCurrentPage());

                } else {
                    removeTab(layersTitle);
                }

                setBookmarks(false);
            }

            if (tabsNotInitialised) {
                for (int i = 0; i != navOptionsPanel.getTabs().size(); i++) {
                    if (navOptionsPanel.getTabs().get(i).getText().equals(startSelectedTab)) {
                        navOptionsPanel.getSelectionModel().select(i);
                        break;
                    }

                }
            }
        }
    }

    private void checkTabShown(final String title) {

        if (debugFX) {
            System.out.println("checkTabShown");
        }

        int outlineTab = -1;

        //see if there is an outlines tab
        for (int jj = 0; jj < navOptionsPanel.getTabs().size(); jj++) {
            if (navOptionsPanel.getTabs().get(jj).getText().equals(title)) {
                outlineTab = jj;
            }
        }

        if (outlineTab == -1) {

            if (title.equals(signaturesTitle) && properties.getValue("Signaturestab").equalsIgnoreCase("true")) {  //stop spurious display of Sig tab
                navOptionsPanel.getTabs().add(navOptionsPanel.getTabs().size() - 1, signaturesPanel);
            } else if (title.equals(layersTitle) && properties.getValue("Layerstab").equalsIgnoreCase("true")) {
                navOptionsPanel.getTabs().add(navOptionsPanel.getTabs().size() - 1, layersPanel);
            }
        }
    }

    private void removeTab(final String title) {

        if (debugFX) {
            System.out.println("removeTab");
        }

        int outlineTab = -1;

        //see if there is an outlines tab
        for (int jj = 0; jj < navOptionsPanel.getTabs().size(); jj++) {
            if (navOptionsPanel.getTabs().get(jj).getText().equals(title)) {
                outlineTab = jj;
            }
        }


        if (outlineTab != -1) {
            navOptionsPanel.getTabs().remove(outlineTab);
        }

    }

    /**
     * Stop thumbnail generation in the pages thumbnail tab
     */
    @Override
    public void stopThumbnails() {

        if (debugFX) {
            System.out.println("stopThumbnails");
        }

        if (thumbnails.isShownOnscreen()) {
            thumbnails.terminateDrawing();

            thumbnails.removeAllListeners();

        }
    }

    /**
     * Flags the pages thumbnail tab as requiring reinitialisation
     */
    @Override
    public void reinitThumbnails() {

        if (debugFX) {
            System.out.println("reinitThumbnails");
        }

        isSetup = false;

    }

    /**
     * reset the navigation bar so that it appears closed
     */
    @Override
    public void resetNavBar() {

        if (debugFX) {
            System.out.println("resetNavBar");
        }

        if (!properties.getValue("consistentTabBar").equalsIgnoreCase("true")) {
            setSplitDividerLocation(collapsedSize);
            tabsExpanded = false;
            tabsNotInitialised = true;
        }

        //disable page view buttons until we know we have multiple pages
        fxButtons.setPageLayoutButtonsEnabled(false);

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setNoPagesDecoded()
     *
     * Called when new file opened so we set flags here
     */
    @Override
    public void setNoPagesDecoded() {

        if (debugFX) {
            System.out.println("setNoPagesDecoded");
        }

        bookmarksGenerated = false;

        resetNavBar();

    }

    /**
     * setDisplayMode not supported in OpenViewerFX
     */
    @Override
    public void setDisplayMode(final Integer mode) {

        if (debugFX) {
            System.out.println("setDisplayMode not implemented for JavaFX in JavaFxGUI");
        }

    }

    /**
     * JavaFX is only in single mode
     *
     * @return Always returns true
     */
    @Override
    public boolean isSingle() {

        if (debugFX) {
            System.out.println("isSingle");
        }

        return true;
    }

    /**
     * Gets the object used for the thumbnail panel.
     *
     * @return an Object that extends GUIThumbnailPanel
     */
    @Override
    public Object getThumbnailPanel() {

        if (debugFX) {
            System.out.println("getThumbnailPanel");
        }

        return thumbnails;
    }

    /**
     * Gets the object used for the outline panel
     *
     * @return an Object that extends GUIOutline
     */
    @Override
    public Object getOutlinePanel() {

        if (debugFX) {
            System.out.println("getOutlinePanel");
        }

        return tree;
    }

    /**
     * Can't get the scrollbar from a scroll pane, other methods should be
     * implemented to circumvent this when needed
     */
    @Override
    public Object getVerticalScrollBar() {

        if (debugFX) {
            System.out.println("getVerticalScrollBar");
        }
        // Altered function due to above comments, return thumbscroll only when shown
        if (pageScroll.isVisible()) {
            return pageScroll;
        } else {
            return null;
        }
    }

    /*
     *Allows user to embed viewer into own application
     */
    @Override
    public void setRootContainer(final Object rawValue) {

        if (rawValue == null) {
            throw new RuntimeException("Null containers not allowed.");
        }

        final Parent parentPane = (Parent) rawValue;

        //We add the Viewer to the parent pane depending on its type.
        if (parentPane instanceof Pane) {
            ((Pane) parentPane).getChildren().add(root);
        } else if (parentPane instanceof ScrollPane) {
            ((ScrollPane) parentPane).setContent(root);
        }

        //Load width and height from properties file
        int width = Integer.parseInt(properties.getValue("startViewerWidth"));
        int height = Integer.parseInt(properties.getValue("startViewerHeight"));

        //Used to prevent infinite scroll issue as a preferred size has been set
        final Rectangle2D d = Screen.getPrimary().getVisualBounds();
        if (width < 0) {
            width = (int) (d.getWidth() / 2);
            if (width < 700) {
                width = 700;
            }
            properties.setValue("startViewerWidth", String.valueOf(width));
        }

        if (height < 0) {
            height = (int) (d.getHeight() / 2);
            properties.setValue("startViewerHeight", String.valueOf(height));
        }

        //allow user to alter size
        final String customWindowSize = System.getProperty("org.jpedal.startWindowSize");
        if (customWindowSize != null) {

            final StringTokenizer values = new StringTokenizer(customWindowSize, "x");

            if (values.countTokens() != 2) {
                throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize=" + customWindowSize + "\nValue should be in format org.jpedal.startWindowSize=200x300");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#resetRotationBox()
     */
    @Override
    public void resetRotationBox() {

        if (debugFX) {
            System.out.println("resetRotationBox");
        }

        final PdfPageData currentPageData = decode_pdf.getPdfPageData();

        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
            rotation = currentPageData.getRotation(commonValues.getCurrentPage());
        }

        if (getSelectedComboIndex(Commands.ROTATION) != (rotation / 90)) {
            setSelectedComboIndex(Commands.ROTATION, (rotation / 90));
        } else if (!Values.isProcessing()) {

//            ((PdfDecoder)decode_pdf).repaint();
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getProperties()
     */
    @Override
    public PropertiesFile getProperties() {

        return properties;
    }

    /**
     * Set Search Bar to be in the Left hand Tabbed pane
     */
    @Override
    public void searchInTab(final GUISearchWindow searchFrame) {

        if (debugFX) {
            System.out.println("searchInTab");
        }

        this.searchFrame = searchFrame;

        this.searchFrame.init(decode_pdf, commonValues);

    }

    /**
     * Adds Drop-Down Search Menu Options to the In-Menu Search Field.
     *
     * @return
     */
    private MenuBar createMenuBarSearchOptions() {

        //Setup Search In Menu Options.
        final CheckMenuItem cb1 = new CheckMenuItem(Messages.getMessage("PdfViewerSearch.WholeWords"));
        final CheckMenuItem cb2 = new CheckMenuItem(Messages.getMessage("PdfViewerSearch.CaseSense"));
        final CheckMenuItem cb3 = new CheckMenuItem(Messages.getMessage("PdfViewerSearch.MultiLine"));

        //Add Search 0ptions listeners.
        cb1.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue ov,
                                final Boolean old_val, final Boolean new_val) {
                searchFrame.setWholeWords(cb1.isSelected());
            }
        });
        cb2.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue ov,
                                final Boolean old_val, final Boolean new_val) {
                searchFrame.setCaseSensitive(cb2.isSelected());
            }
        });
        cb3.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue ov,
                                final Boolean old_val, final Boolean new_val) {
                searchFrame.setMultiLine(cb3.isSelected());
            }
        });

        //Setup Menu Apply Image, and Add Items.
        final Menu menu = new Menu();
        final Image image = new Image(guiCursor.getURLForImage("menuSearchOptions.png").toString());
        menu.setGraphic(new ImageView(image));
        menu.getItems().addAll(cb1, cb2, cb3);

        //Setup Menu Bar and Add Menu.
        options = new MenuBar();
        options.setBackground(Background.EMPTY);
        options.setFocusTraversable(false);
        options.setTooltip(new Tooltip(Messages.getMessage("PdfViewerSearch.Options")));
        options.getMenus().add(menu);

        return options;
    }

    /*
     * Set Search Bar to be in the Top Button Bar
     */
    private void searchInMenu(final GUISearchWindow searchFrame) {

        this.searchFrame = searchFrame;
        searchInMenu = true;
        searchFrame.find(decode_pdf, commonValues);
        fxButtons.getTopButtons().getItems().add(searchText);
        fxButtons.getTopButtons().getItems().add(createMenuBarSearchOptions());
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerSearch.Previous"), "search_previous.gif", Commands.PREVIOUSRESULT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerSearch.Next"), "search_next.gif", Commands.NEXTRESULT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.getButton(Commands.NEXTRESULT).setEnabled(false);
        fxButtons.getButton(Commands.PREVIOUSRESULT).setEnabled(false);

        fxButtons.getButton(Commands.NEXTRESULT).setVisible(true);
        fxButtons.getButton(Commands.PREVIOUSRESULT).setVisible(true);
    }

    /**
     * Sets the properties file to be used
     *
     * @param file String file name for the properties file to be used
     */
    @Override
    public void setPropertiesFileLocation(final String file) {

        properties.loadProperties(file);
    }

    /**
     * Gets the Commands object used by the user interface
     *
     * @return Commands object for executing commands
     */
    @Override
    public Commands getCommand() {

        return currentCommands;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#init(java.lang.String[], org.jpedal.examples.viewer.Commands, org.jpedal.examples.viewer.utils.Printer)
     */
    @Override
    public void init(final Commands currentCommands) {

        if (debugFX) {
            System.out.println("init");
        }

        updateTransitionType();

        mouseHandler = new JavaFXMouseListener((PdfDecoderFX) decode_pdf, this, commonValues, currentCommands);

        mouseHandler.setupMouse();

        //single listener to execute all commands
        currentCommandListener = new CommandListenerFX(currentCommands);

        super.init(currentCommands);

        //Set up from properties
        try {
            //Set whether to use hinting
            String propValue = properties.getValue("useHinting");
            final String propValue2 = System.getProperty("org.jpedal.useTTFontHinting");

            //check JVM flag first
            if (propValue2 != null) {
                //check if properties file conflicts
                if (!propValue.isEmpty() && !propValue2.equalsIgnoreCase(propValue.toLowerCase())) {
                    showMessageDialog(Messages.getMessage("PdfCustomGui.hintingFlagFileConflict"));
                }

                TTGlyph.useHinting = propValue2.equalsIgnoreCase("true");

                //check properties file
            } else {
                TTGlyph.useHinting = !propValue.isEmpty() && propValue.equalsIgnoreCase("true");
            }

            //Set icon location
            propValue = properties.getValue("iconLocation");
            if (!propValue.isEmpty() && GUI.debugFX) {
                System.out.println("guiCursor is not set to JavaFXCursor in method init() in class JavaFxGUI.java");
            }
            //guiCursor.setIconLocation(propValue);

        } catch (final Exception e) {
            e.printStackTrace();
        }

        //setup combo boxes
        setupComboBoxes();

        if (LogWriter.isRunningFromIDE || org.jpedal.DevFlags.GUITESTINGINPROGRESS) {
            nameElements();
        }

        //Initialise the Swing Buttons *
        fxButtons.init(true);

        //create a menu bar and add to display
        createTopMenuBar();

        //create other tool bars and add to display
        createOtherToolBars();

        //set colours on display boxes and add listener to page number
        setupBottomToolBarItems();

        //sets up all the toolbar items
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.openFile"), "open.gif", Commands.OPENFILE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        if (searchFrame != null && searchFrame.getViewStyle() == GUISearchWindow.SEARCH_EXTERNAL_WINDOW) {
            searchFrame.setViewStyle(GUISearchWindow.SEARCH_EXTERNAL_WINDOW);
            fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.search"), "find.gif", Commands.FIND, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        }

        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.properties"), "properties.gif", Commands.DOCINFO, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        if (commonValues.getModeOfOperation() == Values.RUNNING_PLUGIN) {
            fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.about"), "about.gif", Commands.ABOUT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        }

        //snapshot screen function
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.snapshot"), "snapshot.gif", Commands.SNAPSHOT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        final Separator sep = new Separator();
        sep.setPrefSize(5, 32);

        //combo boxes on toolbar
        addCombo(Messages.getMessage("PdfViewerToolbarTooltip.zoomin"), Commands.SCALING);

        addCombo(Messages.getMessage("PdfViewerToolbarTooltip.rotation"), Commands.ROTATION);

        //rotation buttons
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.rotateLeft"), "rotateLeft.gif", Commands.ROTATELEFT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.rotateRight"), "rotateRight.gif", Commands.ROTATERIGHT, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.mouseMode"), "mouse_select.png", Commands.MOUSEMODE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.getTopButtons().getItems().add(sep);

        if (includeExtraMenus && commonValues.getModeOfOperation() != Values.RUNNING_PLUGIN) {
            fxButtons.addButton(GUIFactory.BUTTONBAR, Messages.getMessage("PdfViewerToolbarTooltip.help"), "help.png", Commands.HELP, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        }

        //addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.buy"),iconLocation+"buy.png",Commands.BUY);
        if (includeExtraMenus && commonValues.getModeOfOperation() != Values.RUNNING_PLUGIN) {
            fxButtons.addButton(GUIFactory.BUTTONBAR, "RSS", "rss.gif", Commands.RSS, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);
        }

        //add cursor location
        initMultiBox();

        //navigation toolbar for moving between pages
        createNavbar();

        if (searchFrame != null && searchFrame.getViewStyle() == GUISearchWindow.SEARCH_MENU_BAR) {
            searchInMenu(searchFrame);
        }

        //status object on toolbar showing 0 -100 % completion
        initStatus();

        /*
         * Ensure all gui sections are displayed correctly
         * Issues found when removing some sections
         */
        if (GUI.debugFX) {
            System.out.println("Swing specific invalidate(), validate(), repaint() not implemented for JavaFX in JavaFxGUI");
        }

        /*
         * set display to occupy half screen size and display, add listener and
         * make sure appears in centre
         */
        if (commonValues.getModeOfOperation() != Values.RUNNING_APPLET) {

            //Load width and height from properties file
            int width = Integer.parseInt(properties.getValue("startViewerWidth"));
            int height = Integer.parseInt(properties.getValue("startViewerHeight"));

            //Used to prevent infinite scroll issue as a preferred size has been set
            //Used to prevent infinite scroll issue as a preferred size has been set
            final Rectangle2D d = Screen.getPrimary().getVisualBounds();

            if (width < 0) {
                width = ((int) d.getWidth()) / 2;
                if (width < minimumScreenWidth) {
                    width = minimumScreenWidth;
                }
                properties.setValue("startViewerWidth", String.valueOf(width));
            }

            if (height < 0) {
                height = ((int) d.getHeight()) / 2;
                properties.setValue("startViewerHeight", String.valueOf(height));
            }

            //allow user to alter size
            final String customWindowSize = System.getProperty("org.jpedal.startWindowSize");
            if (customWindowSize != null) {

                final StringTokenizer values = new StringTokenizer(customWindowSize, "x");

                System.out.println(values.countTokens());
                if (values.countTokens() != 2) {
                    throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize=" + customWindowSize + "\nValue should be in format org.jpedal.startWindowSize=200x300");
                }

                try {
                    width = Integer.parseInt(values.nextToken().trim());
                    height = Integer.parseInt(values.nextToken().trim());

                } catch (final Exception ee) {
                    throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize=" + customWindowSize + "\nValue should be in format org.jpedal.startWindowSize=200x300 " + ee);
                }
            }

            setupPDFDisplayPane();

            // add the pdf display left and right panes
            setupBorderPanes();

            createMainViewerWindow(width, height);

        }

        //Add Background color to the JavaFX panel to help break up view
        setupCenterPanelBackground();

        setupPDFBorder();

        //Set side tab bar state at start up
        if (sideTabBarOpenByDefault) {
            setSplitDividerLocation(expandedSize);
            tabsExpanded = true;
        } else {
            setSplitDividerLocation(collapsedSize);
            tabsExpanded = false;
        }

        //Load properties
        try {
            GUIModifier.load(properties, this);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        //DELETE This code block once all items are implemented.
        //This code is here so the loading of properties does not override
        //the disabling of unimplemented items in the FXViewer.
        final int ALL = -10;
        ((JavaFXMenuItems) menuItems).disableUnimplementedItems(ALL, false);
        fxButtons.disableUnimplementedItems(ALL);
    }

    private void nameElements() {

        ((JavaFXMenuItems) menuItems).getCurrentMenuFX().setId("menuBar");

        pageCounter2.setId("pageCounter");

        if (org.jpedal.DevFlags.GUITESTINGINPROGRESS) {
            rotationBox.setName("rotationBox");
            scalingBox.setName("scalingBox");
        }
    }

    /**
     * method being called from within init to create other tool bars to add to
     * display
     */
    @Override
    protected void createOtherToolBars() {
        if (GUI.debugFX) {
            System.out.println("createOtherToolBars almost finished for JavaFX");
        }

        //This is where we setup the top most menu
        menuItems.createMainMenu(true, currentCommandListener, true, commonValues, currentCommands, fxButtons);
        topPane.getChildren().add(((JavaFXMenuItems) menuItems).getCurrentMenuFX());

        //This is where we add the Buttons for the top ToolBar
        topPane.getChildren().add(fxButtons.getTopButtons());
    }

    private void handleTabbedPanes() {

        if (tabsNotInitialised) {
            return;
        }

        if (debugFX) {
            System.out.println("handleTabbedPanes");
        }

        final int tabSelected;

        tabSelected = navOptionsPanel.getSelectionModel().getSelectedIndex();

        if (tabSelected == -1) {
            return;
        }

        if (!tabsExpanded) {

            //workout selected tab
            final String tabName = navOptionsPanel.getTabs().get(tabSelected).getText();

            if (tabName != null && tabName.equals(pageTitle)) {
                thumbnails.setIsDisplayedOnscreen(true);
            } else {
                thumbnails.setIsDisplayedOnscreen(false);
            }

            // Don't expand on dummy tab
            if (!navOptionsPanel.getSelectionModel().getSelectedItem().isDisable()) {
                setSplitDividerLocation(expandedSize);
                tabsExpanded = true;
            }
        } else if (lastTabSelected == tabSelected && !hasPageChanged) {
            setSplitDividerLocation(collapsedSize);
            tabsExpanded = false;
            thumbnails.setIsDisplayedOnscreen(false);
        }
        lastTabSelected = tabSelected;
        hasPageChanged = false;
    }

    private void initMultiBox() {
        multiboxfx = new HBox();
        initCoordBox();
        initMemoryBar();
        initDownloadBar();
    }

    /**
     * Not part of API - used internally
     * <p>
     * Set the state and value for the multi box at the bottom left of the user interface.
     *
     * @param flags an int[] used to control multi box display
     */
    @Override
    public void setMultibox(final int[] flags) {

        //debug code for formclicktests
        if (GUI.alwaysShowMouse) {
            multiboxfx.getChildren().clear();
            multiboxfx.getChildren().add(coordsFX);
            return;
        }

        //deal with flags
        if (flags.length > 1 && flags[0] == CURSOR) {
            //if no change, return
            if (cursorOverPage != (flags[1] == 1)) {
                cursorOverPage = flags[1] == 1;
            } else {
                return;
            }
        }

        //LOAD_PROGRESS:
//        if (statusBar.isEnabled() && statusBar.isVisible() && !statusBar.isDone()) {
//            multibox.removeAll();
//            statusBar.getStatusObject().setSize(multibox.getSize());
//            multibox.add(statusBar.getStatusObject(), BorderLayout.CENTER);
//
//            multibox.repaint();
//            return;
//        }
        //CURSOR:
        if (cursorOverPage && decode_pdf.isOpen()) {
            multiboxfx.getChildren().clear();
            multiboxfx.getChildren().add(coordsFX);
            return;
        }

        //DOWNLOAD_PROGRESS:
        if (!downloadBar.isDisable() && downloadBar.isVisible() && !downloadBar.isDone() && (decode_pdf.isLoadingLinearizedPDF() || !decode_pdf.isOpen())) {
            multiboxfx.getChildren().clear();
            multiboxfx.getChildren().add(downloadBar.getStatusObject());
            return;
        }

        //MEMORY:
        if (memoryBarFX.isVisible()) {
            multiboxfx.getChildren().clear();
            multiboxfx.getChildren().add(memoryBarFX);
        }

    }

    private void initDownloadBar() {
        downloadBar = new StatusBarFX(new Color((185.0d / 255.0d), (209.0d / 255.0d), 0, 1));
        downloadBar.getStatusObject().prefHeightProperty().bind(multiboxfx.heightProperty());
        downloadBar.getStatusObject().prefWidthProperty().bind(multiboxfx.widthProperty());

        downloadBar.setVisible(false);
    }

    private void initMemoryBar() {
        memoryBarFX = new FXProgressBarWithText();
        memoryBarFX.prefHeightProperty().bind(multiboxfx.heightProperty());
        memoryBarFX.prefWidthProperty().bind(multiboxfx.widthProperty());

        if (memoryMonitor == null) { //ensure only 1 instance

            memoryMonitor = new Timeline(new KeyFrame(Duration.seconds(0.5), new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    final int free = (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
                    final int total = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));

                    final double used = (total - free) / total;

                    //this broke the image saving when it was run every time
                    if (finishedDecoding) {
                        finishedDecoding = false;
                    }
                    if (Platform.isFxApplicationThread()) {
                        memoryBarFX.setProgress(used);
                        memoryBarFX.setText((total - free) + "M of " + total + 'M');

                    } else {
                        // Needs to be done on the FX Thread
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                memoryBarFX.setProgress(used);
                                memoryBarFX.setText((total - free) + "M of " + total + 'M');
                            }
                        });
                    }
                }
            }));
            memoryMonitor.setCycleCount(Timeline.INDEFINITE);
            memoryMonitor.play();
        }
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setViewerIcon() {
        //Check if file location provided
        final URL path = guiCursor.getURLForImage("icon.png");
        if (stage != null && path != null) {
            try {
                //Converting Swing BufferedImage to FX WritableImage
                final BufferedImage bi = ImageIO.read(path);
                WritableImage fontIcon = new WritableImage(bi.getWidth(), bi.getHeight());
                fontIcon = SwingFXUtils.toFXImage(bi, fontIcon);

                stage.getIcons().add(fontIcon);

            } catch (final Exception e) {
                LogWriter.writeLog("Exception attempting to set Icon " + e);
            }
        }

    }

    private boolean setDisplayView2(final int displayView, final int orientation) {

        final DecoderOptions options = decode_pdf.getDecoderOptions();
        Display pages = decode_pdf.getPages();
        final int lastDisplayView = decode_pdf.getDisplayView();
        final FileAccess fileAccess = (FileAccess) decode_pdf.getExternalHandler(Options.FileAccess);
        final int pageNumber = decode_pdf.getPageNumber();
        final ExternalHandlers externalHandlers = decode_pdf.getExternalHandler();

        final PdfDecoderFX comp = (PdfDecoderFX) decode_pdf;

        final Pane highlightsPane = comp.highlightsPane;

        options.setPageAlignment(orientation);

        if (pages != null) {
            pages.stopGeneratingPage();
        }

        if (Platform.isFxApplicationThread()) {

            if (highlightsPane != null && highlightsPane.getParent() != null) {
                ((Group) highlightsPane.getParent()).getChildren().remove(highlightsPane);
            }

        } else {
            final Runnable doPaintComponent = new Runnable() {

                @Override
                public void run() {
                    if (highlightsPane != null && highlightsPane.getParent() != null) {
                        ((Group) highlightsPane.getParent()).getChildren().remove(highlightsPane);
                    }
                }
            };
            Platform.runLater(doPaintComponent);
        }

        final boolean hasChanged = displayView != lastDisplayView;

        options.setDisplayView(displayView);

        if (lastDisplayView != displayView && lastDisplayView == Display.PAGEFLOW) {
            pages.dispose();
        }

        final Object customFXHandle = externalHandlers.getExternalHandler(Options.MultiPageUpdate);

        switch (displayView) {
            case Display.SINGLE_PAGE:
                if (pages == null || hasChanged) {
                    final DynamicVectorRenderer currentDisplay = decode_pdf.getDynamicRenderer();

                    pages = new SingleDisplayFX(pageNumber, currentDisplay, comp, options);
                }
                break;

            case Display.PAGEFLOW:

                if (pages instanceof PageFlowDisplayFX) {
                    return hasChanged;
                }

                if (lastDisplayView != Display.SINGLE_PAGE) {
                    setDisplayView(Display.SINGLE_PAGE, 0);
                    setDisplayView(Display.PAGEFLOW, 0);
                    return hasChanged;
                }

                pages = new PageFlowDisplayFX((GUIFactory) customFXHandle, comp);

                break;

        }

        /*
         * setup once per page getting all page sizes and working out settings
         * for views
         */
        if (fileAccess.getOffset() == null) {
            fileAccess.setOffset(new PageOffsets(decode_pdf.getPageCount(), decode_pdf.getPdfPageData()));
        }

        pages.setup(options.useHardwareAcceleration(), fileAccess.getOffset());

        final DynamicVectorRenderer currentDisplay = decode_pdf.getDynamicRenderer();

        if (decode_pdf.isOpen()) {
            pages.init(scaling, decode_pdf.getDisplayRotation(), pageNumber, currentDisplay, true);
        }

        // force redraw
        pages.forceRedraw();

        pages.refreshDisplay();

        //needs to be realigned (in longer term refactored out)
        comp.pages = pages;

        return hasChanged;
    }

    /**
     * set view mode used in panel and redraw in new mode
     * SINGLE_PAGE,CONTINUOUS,FACING,CONTINUOUS_FACING delay is the time in
     * milli-seconds which scrolling can stop before background page drawing
     * starts Multipage views not in OS releases
     */
    @Override
    public void setDisplayView(final int displayView, final int orientation) {

        // remove listener if setup
        if (viewListener != null) {

            removeComponentListener(viewListener);

            viewListener.dispose();
            viewListener = null;
        }

        final boolean hasChanged = setDisplayView2(displayView, orientation);

        //move to correct page
        final int pageNumber = decode_pdf.getPageNumber();
        if (pageNumber > 0) {
            if (hasChanged && displayView == Display.SINGLE_PAGE && decode_pdf.isOpen()) {
                try {
                    decode_pdf.getPages().getYCordForPage(pageNumber, scaling);
                    decode_pdf.setPageParameters(scaling, pageNumber, decode_pdf.getDisplayRotation());
                    //@swing
//                    invalidate();
//                    updateUI();
                    decode_pdf.decodePage(pageNumber);
                } catch (final Exception e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            } else if (displayView == Display.CONTINUOUS || displayView == Display.CONTINUOUS_FACING) {
                try {
                    decode_pdf.getPages().getYCordForPage(pageNumber, scaling);
                    decode_pdf.setPageParameters(scaling, pageNumber, decode_pdf.getDisplayRotation());
                    decode_pdf.decodePage(pageNumber);
                    scrollToPage(pageNumber);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        //Only all search in certain modes
        if (displayView != Display.SINGLE_PAGE
                && displayView != Display.CONTINUOUS
                && displayView != Display.CONTINUOUS_FACING) {
            enableSearchItems(false);
        } else {
            enableSearchItems(true);
        }

        // add listener if one not already there
        if (viewListener == null) {
            viewListener = new RefreshLayout(decode_pdf);
            addComponentListener(viewListener);
        }
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setTitle(final String title) {
        if (stage != null) {
            stage.setTitle(title);
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#resetComboBoxes(boolean)
     */
    @Override
    public void resetComboBoxes(final boolean value) {

        if (debugFX) {
            System.out.println("resetComboBoxes");
        }

        scalingBox.setEnabled(value);
        rotationBox.setEnabled(value);
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getCombo(int)
     */
    @Override
    public GUICombo getCombo(final int ID) {

        if (debugFX) {
            System.out.println("getCombo");
        }

        switch (ID) {

            case Commands.SCALING:
                return scalingBox;
            case Commands.ROTATION:
                return rotationBox;

        }

        return null;

    }

    /**
     * all scaling and rotation should go through this.
     */
    @Override
    public void scaleAndRotate() {

        if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage(), rotation);
            return;
        }
        //ignore if called too early
        if (!decode_pdf.isOpen() && OpenFile.isPDf) {
            return;
        }

        final double width = pageContainer.getViewportBounds().getWidth() * 0.95;
        final double height = pageContainer.getViewportBounds().getHeight() * 0.95;
        float tempScaling = 1.5f;

//        if(sudaDebug){
//            System.out.println("scaleAndRotate");
//            System.out.println("\tScaling "+scaling);
//            System.out.println("\tbar: "+pageContainer.getViewportBounds());
//            System.out.println("\tGL: "+group.getBoundsInLocal());
//            System.out.println("\tGP: "+group.getBoundsInParent());
//        }
        //update value and GUI
        if (decode_pdf != null) {

            for (int i = 0; i < group.getChildren().size(); i++) {
                if (!group.getChildren().get(i).equals(decode_pdf)) {
                    group.getChildren().remove(group.getChildren().get(i));
                }
            }

            int index = getSelectedComboIndex(Commands.SCALING);

            if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {

                //Ensure we only display in window mode
                setSelectedComboIndex(Commands.SCALING, 0);
                index = 0;

                //Disable scaling option
                scalingBox.setEnabled(false);
            } else if (decode_pdf.getDisplayView() != Display.PAGEFLOW) {

                //No long pageFlow. enable scaling option
                scalingBox.setEnabled(true);
            }

            int page = commonValues.getCurrentPage();

            //Multipage tiff should be treated as a single page
            if (commonValues.isMultiTiff()) {
                page = 1;
            }

            if (index != -1) {
                //always check in facing mode with turnover on
                int tw = 0, th = 0; //temp width and temp hegiht;
                final double sw;
                final double sh;

                final PdfPageData pData = decode_pdf.getPdfPageData();
                final int curPW = pData.getCropBoxWidth(page); //current page width
                final int curPH = pData.getCropBoxHeight(page); //current page height

                switch (rotation) {
                    case 0:
                        tw = curPW;
                        th = curPH;
                        break;
                    case 90:
                        tw = curPH;
                        th = curPW;
                        break;
                    case 180:
                        tw = curPW;
                        th = curPH;
                        break;
                    case 270:
                        tw = curPH;
                        th = curPW;
                        break;
                }

                switch (decode_pdf.getDisplayView()) {
                    case Display.SINGLE_PAGE:

                        switch (index) {
                            case 0://fit page
                                tempScaling = (float) getScalingRatio(tw, th, width, height);
                                break;
                            case 1://fit height;
                                tempScaling = (float) (height / th);
                                break;
                            case 2://fit width;
                                tempScaling = (float) (width / tw);
                                break;
                            default://other percentages;
                                tempScaling = decode_pdf.getDPIFactory().adjustScaling(scalingFloatValues[index]);
                                break;
                        }

                        sw = (int) (tempScaling * tw);
                        sh = (int) (tempScaling * th);

                        if (sw < width) {
                            group.setTranslateX((width - sw) / 2.0);
                        } else {
                            group.setTranslateX(0);
                        }

                        //##we need to hide the scrollbar and show the scrollpane if content does not fit
                        if (sh < height && sw < width) {
//                            setScrollBarPolicy(GUI.ScrollPolicy.VERTICAL_NEVER);
//                            setScrollBarPolicy(GUI.ScrollPolicy.HORIZONTAL_NEVER);
//                            setThumbnailScrollBarVisibility(true);
//                            group.setTranslateY((height - sh) / 2.0);
                        } else {
//                            setScrollBarPolicy(GUI.ScrollPolicy.VERTICAL_AS_NEEDED);
//                            setScrollBarPolicy(GUI.ScrollPolicy.HORIZONTAL_AS_NEEDED);
//                            setThumbnailScrollBarVisibility(false);
//                            group.setTranslateY(0);
                        }
                        group.setTranslateY(0);
                        break;
                    case Display.CONTINUOUS:
                        switch (index) {
                            case 0://fit page
                                tempScaling = (float) getScalingRatio(tw, th, width, height);
                                break;
                            case 1://fit height;
                                tempScaling = (float) (height / th);
                                break;
                            case 2://fit width;
                                tempScaling = (float) (width / tw);
                                break;
                            default://other percentages;
                                tempScaling = decode_pdf.getDPIFactory().adjustScaling(scalingFloatValues[index]);
                                break;
                        }

                        sw = (int) (tempScaling * tw);

                        if (sw < width) {
                            group.setTranslateX((width - sw) / 2.0);
                        } else {
                            group.setTranslateX(0);
                        }
                        group.setTranslateY(0);

                        break;
                    case Display.CONTINUOUS_FACING:
                        tw *= 2;
                        switch (index) {
                            case 0://fit page
                                tempScaling = (float) getScalingRatio(tw, th, width, height);
                                break;
                            case 1://fit height;
                                tempScaling = (float) (height / th);
                                break;
                            case 2://fit width;
                                tempScaling = (float) (width / tw);
                                break;
                            default://other percentages;
                                tempScaling = decode_pdf.getDPIFactory().adjustScaling(scalingFloatValues[index]);
                                break;
                        }

                        sw = (int) (tempScaling * tw);

                        if (sw < width) {
                            group.setTranslateX((width - sw) / 2.0);
                        } else {
                            group.setTranslateX(0);
                        }
                        group.setTranslateY(0);

                        break;
                    case Display.FACING:

                        break;
                    case Display.PAGEFLOW:

                        break;
                    default:
                        System.err.println("unsupported page display ");
                        break;

                }
            }
            scaling = tempScaling;
            // Avoid infinty
            if (Float.isInfinite(scaling)) {
                scaling = 1f;
            }

            decode_pdf.setPageParameters(scaling, page, rotation);

            //Ensure the page is displayed in the correct rotation
//            setRotation();
        }

//        double h = pageContainer.getContent().getBoundsInLocal().getHeight();
//        System.out.println("scrolling " + pageContainer.getVvalue() + " height of container: " + h);
//        adjustGroupToCenter();
        if (LogWriter.isRunningFromIDE) {
            System.out.println("Memory usage after zoom=" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000);
        }

    }

    /**
     * Get the scaling ratio required to fit one dimension to another
     *
     * @param actualW double value representing actual width
     * @param actualH double value representing actual height
     * @param maxW    double value representing max width
     * @param maxH    double value representing max height
     * @return double value representing the scaling ration to convert actual values to max values
     */
    public static double getScalingRatio(final double actualW, final double actualH, final double maxW, final double maxH) {
        return Math.min((maxW / actualW), (maxH / actualH));
    }

    /**
     * Set scaling to nearest of the viewer default values.
     *
     * @param newScaling The scaling value to snap to viewer default values
     */
    @Override
    public void snapScalingToDefaults(float newScaling) {

        if (debugFX) {
            System.out.println("snapScalingToDefaults");
        }

        newScaling = decode_pdf.getDPIFactory().adjustScaling(newScaling / 100);

        final float width;
        final float height;

        //if(isSingle){
        width = (float) pageContainer.getWidth() * 0.95f;
        height = (float) pageContainer.getHeight() * 0.95f;
        //}else{
        //    width=desktopPane.getWidth();
        //    height=desktopPane.getHeight();
        //}

        final PdfPageData pageData = decode_pdf.getPdfPageData();
        int cw, ch, raw_rotation = 0;

        if (decode_pdf.getDisplayView() == Display.FACING) {
            raw_rotation = pageData.getRotation(commonValues.getCurrentPage());
        }

        final boolean isRotated = (rotation + raw_rotation) % 180 == 90;

        final PageOffsets offsets = (PageOffsets) decode_pdf.getExternalHandler(Options.CurrentOffset);
        switch (decode_pdf.getDisplayView()) {
            case Display.CONTINUOUS_FACING:
                if (isRotated) {
                    cw = offsets.getMaxH() * 2;
                    ch = offsets.getMaxW();
                } else {
                    cw = offsets.getMaxW() * 2;
                    ch = offsets.getMaxH();
                }
                break;
            case Display.CONTINUOUS:
                if (isRotated) {
                    cw = offsets.getMaxH();
                    ch = offsets.getMaxW();
                } else {
                    cw = offsets.getMaxW();
                    ch = offsets.getMaxH();
                }
                break;
            case Display.FACING:
                int leftPage;
                if (decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER)) {
                    leftPage = (commonValues.getCurrentPage() / 2) * 2;
                    if (commonValues.getPageCount() == 2) {
                        leftPage = 1;
                    }
                } else {
                    leftPage = commonValues.getCurrentPage();
                    if ((leftPage & 1) == 0) {
                        leftPage--;
                    }
                }

                if (isRotated) {
                    cw = pageData.getCropBoxHeight(leftPage);

                    //if first or last page double the width, otherwise add other page width
                    if (leftPage + 1 > commonValues.getPageCount() || leftPage == 1) {
                        cw *= 2;
                    } else {
                        cw += pageData.getCropBoxHeight(leftPage + 1);
                    }

                    ch = pageData.getCropBoxWidth(leftPage);
                    if (leftPage + 1 <= commonValues.getPageCount() && ch < pageData.getCropBoxWidth(leftPage + 1)) {
                        ch = pageData.getCropBoxWidth(leftPage + 1);
                    }
                } else {
                    cw = pageData.getCropBoxWidth(leftPage);

                    //if first or last page double the width, otherwise add other page width
                    if (leftPage + 1 > commonValues.getPageCount()) {
                        cw *= 2;
                    } else {
                        cw += pageData.getCropBoxWidth(leftPage + 1);
                    }

                    ch = pageData.getCropBoxHeight(leftPage);
                    if (leftPage + 1 <= commonValues.getPageCount() && ch < pageData.getCropBoxHeight(leftPage + 1)) {
                        ch = pageData.getCropBoxHeight(leftPage + 1);
                    }
                }
                break;
            default:
                if (isRotated) {
                    cw = pageData.getCropBoxHeight(commonValues.getCurrentPage());
                    ch = pageData.getCropBoxWidth(commonValues.getCurrentPage());
                } else {
                    cw = pageData.getCropBoxWidth(commonValues.getCurrentPage());
                    ch = pageData.getCropBoxHeight(commonValues.getCurrentPage());
                }
        }

        float x_factor;
        float y_factor;
        final float window_factor;
        x_factor = width / cw;
        y_factor = height / ch;

        if (x_factor < y_factor) {
            window_factor = x_factor;
            x_factor = -1;
        } else {
            window_factor = y_factor;
            y_factor = -1;
        }

        if (getSelectedComboIndex(Commands.SCALING) != 0
                && ((newScaling < window_factor * 1.1 && newScaling > window_factor * 0.91)
                || ((window_factor > scaling && window_factor < newScaling) || (window_factor < scaling && window_factor > newScaling)))) {
            setSelectedComboIndex(Commands.SCALING, 0);
            scaling = window_factor;
        } else if (y_factor != -1
                && getSelectedComboIndex(Commands.SCALING) != 1
                && ((newScaling < y_factor * 1.1 && newScaling > y_factor * 0.91)
                || ((y_factor > scaling && y_factor < newScaling) || (y_factor < scaling && y_factor > newScaling)))) {
            setSelectedComboIndex(Commands.SCALING, 1);
            scaling = y_factor;
        } else if (x_factor != -1
                && getSelectedComboIndex(Commands.SCALING) != 2
                && ((newScaling < x_factor * 1.1 && newScaling > x_factor * 0.91)
                || ((x_factor > scaling && x_factor < newScaling) || (x_factor < scaling && x_factor > newScaling)))) {
            setSelectedComboIndex(Commands.SCALING, 2);
            scaling = x_factor;
        } else {
            setSelectedComboItem(Commands.SCALING, String.valueOf((int) decode_pdf.getDPIFactory().removeScaling(newScaling * 100)));
            scaling = newScaling;
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#rotate()
     */
    @Override
    public void rotate() {

        rotation = Integer.parseInt((String) getSelectedComboItem(Commands.ROTATION));

        scaleAndRotate();

    }

    /**
     * Scrolls the view to the specified page.
     *
     * @param page Page to scroll to.
     */
    @Override
    public void scrollToPage(final int page) {

        if (debugFX) {
            System.out.println("scrollToPage");
        }

        commonValues.setCurrentPage(page);

        if (commonValues.getCurrentPage() > 0) {

            //int xCord = 0;
            final int pageCount = decode_pdf.getPageCount();
            double yCord; // = decode_pdf.getPages().getYCordForPage(page, scaling);

            //$$$$$$$$$$$$$ call it and ignore please dont delete as it has affect in continuous mode               
            decode_pdf.getPages().getYCordForPage(page, scaling);

            // if (decode_pdf.getDisplayView() != Display.SINGLE_PAGE) {
            //   xCord = 0;
            //}
            //System.out.println("Before="+decode_pdf.getVisibleRect()+" "+decode_pdf.getPreferredSize());

            final PdfPageData pageData = decode_pdf.getPdfPageData();

            //final int ch = (int) (pageData.getCropBoxHeight(page) * scaling);
            //final int cw = (int) (pageData.getCropBoxWidth(page) * scaling);

            double totalHeight = 0;

            //int pw = pageData.getScaledCropBoxWidth(page);
            //int ph = pageData.getScaledCropBoxHeight(page);

            //double localH = pageContainer.getContent().getBoundsInLocal().getHeight();
            //double localW = pageContainer.getContent().getBoundsInLocal().getWidth();
            //double viewH = pageContainer.getViewportBounds().getHeight();
            //double viewW = pageContainer.getViewportBounds().getWidth();

            final double vv;
            switch (decode_pdf.getDisplayView()) {
                case Display.SINGLE_PAGE:

                    break;
                case Display.CONTINUOUS:
                    yCord = 0;
                    for (int i = 1; i < pageCount; i++) {
                        totalHeight += pageData.getScaledCropBoxHeight(i);
                    }
                    for (int i = 1; i < page; i++) {
                        yCord += pageData.getScaledCropBoxHeight(i);
                    }
                    vv = yCord / totalHeight;
                    pageContainer.setVvalue(vv);
                    break;
                case Display.CONTINUOUS_FACING:
                    yCord = 0;
                    for (int i = 1; i < pageCount; i += 2) {
                        totalHeight += pageData.getScaledCropBoxHeight(i);
                    }
                    for (int i = 1; i < page; i += 2) {
                        yCord += pageData.getScaledCropBoxHeight(i);
                    }
                    vv = yCord / totalHeight;
                    pageContainer.setVvalue(vv);
                    break;
            }
        }

        if (decode_pdf.getPageCount() > 1) {
            fxButtons.setPageLayoutButtonsEnabled(true);
        }

    }

    /**
     * Set the thumbnail panel ready for display.
     */
    @Override
    public void setupThumbnailPanel() {

        if (debugFX) {
            System.out.println("setupThumbnailPanel");
        }

        decode_pdf.addExternalHandler(thumbnails, Options.ThumbnailHandler);

        if (isSetup) {
            return;
        }

        isSetup = true;

        if (thumbnails.isShownOnscreen()) {
            //setup and add to display
            thumbnails.setupThumbnails(decode_pdf.getPageCount(), null, decode_pdf.getPdfPageData());

            //add listener so clicking on button changes to page - has to be in Viewer so it can update it
            final Button[] buttons = (Button[]) thumbnails.getButtons();
            for (int i = 0; i < commonValues.getPageCount(); i++) {
                buttons[i].setOnAction(new JavaFXPageChanger(this, commonValues, i));
            }

            //add global listener
            thumbnails.addComponentListener();

        }
    }

    /**
     * Set the bookmarks for the outline panel
     *
     * @param alwaysGenerate true we will generate the bookmarks, false we only generate bookmarks if not already generated
     */
    @Override
    public void setBookmarks(final boolean alwaysGenerate) {

        if (debugFX) {
            System.out.println("setBookmarks");

            //ignore if not opened
            System.out.println("currentSize in setBookmarks() in JavaFXGUI requires JavaFX implementation");
        }
//		int currentSize=center.getDividerPosition();
//
//		if((currentSize==startSize)&& !alwaysGenerate)
//			return;

        //ignore if already done and flag
        if (bookmarksGenerated) {
            return;
        }
        bookmarksGenerated = true;

        final org.w3c.dom.Document doc = decode_pdf.getOutlineAsXML();

        Node rootNode = null;
        if (doc != null) {
            rootNode = doc.getFirstChild();
        }

        if (rootNode != null) {

            tree.reset(rootNode);

            // Allows an already selected node to be clicked again
            final EventHandler<MouseEvent> reselectionListener = new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    final String ref = ((JavaFXOutline.OutlineNode) ((javafx.scene.Node) event.getSource()).getUserData()).getObjectRef();
                    gotoPageByRef(ref);
                }
            };

            ((TreeView<Label>) tree.getTree()).getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Label>>() {
                @Override
                public void changed(final ObservableValue<? extends TreeItem<Label>> ov, final TreeItem<Label> t, final TreeItem<Label> t1) {
                    if (t1 == null) {
                        return;
                    }
                    // Remove the listener from the old node
                    if (t != null) {
                        t.getValue().setOnMouseClicked(null);
                    }

                    t1.getValue().setOnMouseClicked(reselectionListener);
                    final String ref = ((JavaFXOutline.OutlineNode) t1).getObjectRef();
                    gotoPageByRef(ref);
                }
            });

        } else {
            tree.reset(null);
        }
    }

    /**
     * Used by the outline tab code
     */
    private void gotoPageByRef(final String ref) {
        final PdfObject Aobj = decode_pdf.getOutlineData().getAobj(ref);
        if (Aobj != null) {
            decode_pdf.getFormRenderer().getActionHandler().gotoDest(Aobj, ActionHandler.MOUSECLICKED, PdfDictionary.Dest);
        }
    }

    private void initStatus() {

        if (debugFX) {
            System.out.println("initStatus not implemented for javafx yet");
        }

        //decode_pdf.setStatusBarObject(statusBar);
        //and initialise the display
        setMultibox(new int[]{});

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setCoordText(java.lang.String)
     */
    @Override
    public void setCoordText(final String string) {
        coordsFX.setText(string);
    }

    private void initCoordBox() {

        if (debugFX) {
            System.out.println("initCoordBox");
        }

        coordsFX = new TextField();

        coordsFX.prefWidthProperty().bind(multiboxfx.prefWidthProperty());
        coordsFX.prefHeightProperty().bind(multiboxfx.heightProperty());
        coordsFX.setAlignment(Pos.CENTER);
        coordsFX.setEditable(false);
        coordsFX.setVisible(false);

        coordsFX.setText("  X: " + " Y: " + ' ' + ' ');
//      coords.setBackground(Color.white);
//      coords.setOpaque(true);

//        coords.setBorder(BorderFactory.createEtchedBorder());
//        coords.setPreferredSize(multibox.getPreferredSize());
        //Needed to ensure the coords appear correctly in facing mode
        //If coords have not been displayed before entering facing mode
//        coords.setSize(multibox.getPreferredSize());
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setPageNumber()
     */
    @Override
    public void setPageNumber() {

        if (Platform.isFxApplicationThread()) {
            setPageNumberWorker();
        } else {
            //Ensure dialog is handled on FX thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setPageNumberWorker();

                }
            });
        }

        if (isSetup) {
            //Page changed so save this page as last viewed
            setThumbnails();
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setPageNumber()
     */
    private void setPageNumberWorker() {

        if (pageCounter2 == null) {
            return;
        }

        if (!decode_pdf.isOpen() && !commonValues.isMultiTiff()) {
            pageCounter2.setText("0");
            pageCounter3.setText(Messages.getMessage("PdfViewerOfLabel.text") + " 0");

        } else {
            if (previewOnSingleScroll && pageScroll != null) {
                pageScroll.setMax(decode_pdf.getPageCount() - 1);
                pageScroll.setValue(commonValues.getCurrentPage() - 1);
                if (debugThumbnail) {
                    System.out.println("setpage=" + commonValues.getCurrentPage());
                }
            }

            final int currentPage = commonValues.getCurrentPage();
            if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
                if (decode_pdf.getPageCount() == 2) {
                    pageCounter2.setText(getPageLabel(1) + "/" + getPageLabel(2));
                } else if (decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
                    final int base = currentPage & -2;
                    if (base != decode_pdf.getPageCount() && base != 0) {
                        pageCounter2.setText(getPageLabel(base) + "/" + getPageLabel(base + 1));
                    } else {
                        pageCounter2.setText(getPageLabel(currentPage));
                    }
                } else {
                    final int base = currentPage - (1 - (currentPage & 1));
                    if (base != decode_pdf.getPageCount()) {
                        pageCounter2.setText(getPageLabel(base) + "/" + getPageLabel(base + 1));
                    } else {
                        pageCounter2.setText(getPageLabel(currentPage));
                    }
                }

            } else {
                pageCounter2.setText(getPageLabel(currentPage));
            }

            if (pageLabelDiffers(currentPage)) {
                pageCounter3.setText("(" + currentPage + ' ' + Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount() + ")"); //$NON-NLS-1$
            } else {
                pageCounter3.setText(Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount()); //$NON-NLS-1$
            }

            fxButtons.hideRedundentNavButtons(this);
        }

    }

    /**
     * note - to plugin put all on single line so addButton values over-ridden
     */
    private void createNavbar() {

        if (debugFX) {
            System.out.println("createNavbar");
        }

        navButtons.getChildren().add(multiboxfx);

//        multibox.setLayout(new BorderLayout());
        //if(commonValues.getModeOfOperation()!=Values.RUNNING_PLUGIN)
        //navButtons.add(multibox, BorderLayout.WEST);
        //Spacer to set the alignment of the navButtons items.
        final Region multiboxSpacer = new Region();
        HBox.setHgrow(multiboxSpacer, Priority.ALWAYS);
        navButtons.getChildren().add(multiboxSpacer);

        //navigation toolbar for moving between pages
        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.RewindToStart"), "start.gif", Commands.FIRSTPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.Rewind10"), "fback.gif", Commands.FBACKPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.Rewind1"), "back.gif", Commands.BACKPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        //put page count in middle of forward and back
        pageCounter1 = new Label(Messages.getMessage("PdfViewerPageLabel.text"));
        navToolBar.getItems().add(pageCounter1);
        navToolBar.getItems().add(pageCounter2);
        navToolBar.getItems().add(pageCounter3);

        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.Forward1"), "forward.gif", Commands.FORWARDPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.Forward10"), "fforward.gif", Commands.FFORWARDPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(NAVBAR, Messages.getMessage("PdfViewerNavBar.ForwardLast"), "end.gif", Commands.LASTPAGE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        //add buttons but not in Content Extractor
        fxButtons.addButton(PAGES, Messages.getMessage("PageLayoutButton.SinglePage"), "single.gif", Commands.SINGLE, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        fxButtons.addButton(PAGES, Messages.getMessage("PageLayoutButton.PageFlow"), "pageflow.gif", Commands.PAGEFLOW, menuItems, this, currentCommandListener, pagesToolBar, navToolBar);

        //on top in plugin
        if (commonValues.getModeOfOperation() == Values.RUNNING_PLUGIN) {
            fxButtons.getTopButtons().getItems().add(pagesToolBar);
        } else {
            navButtons.getChildren().add(navToolBar);
        }

        //Spacer to set the alignment of the navButtons items.
        final Region navToolBarSpacer = new Region();
        HBox.setHgrow(navToolBarSpacer, Priority.ALWAYS);
        navButtons.getChildren().add(navToolBarSpacer);

        //on top in plugin
        if (commonValues.getModeOfOperation() == Values.RUNNING_PLUGIN) {
            fxButtons.getTopButtons().getItems().add(pagesToolBar);
        } else {
            navButtons.getChildren().add(pagesToolBar);
        }

        multiboxfx.prefWidthProperty().bind(pagesToolBar.widthProperty());

    }

    /**
     * Set the interfaces page counter to the page number
     * and alter any other interface options to match.
     *
     * @param page page number as an int
     */
    @Override
    public void setPage(int page) {

        if (debugFX) {
            System.out.println("setPage");
        }

        if (((decode_pdf.getDisplayView() == Display.FACING && decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER))
                || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING)
                && (page & 1) == 1 && page != 1) {
            page--;
        } else if (decode_pdf.getDisplayView() == Display.FACING && !decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER)
                && (page & 1) == 0) {
            page--;
        }

        commonValues.setCurrentPage(page);
        setPageNumber();
    }

    /**
     * Get the type of the user interface
     *
     * @return GUIModes.JAVAFX
     */
    @Override
    public Enum getType() {

        if (debugFX) {
            System.out.println("getType");
        }

        return GUIModes.JAVAFX;
    }

    /**
     * Reset the page counter to empty them.
     */
    @Override
    public void resetPageNav() {

        if (debugFX) {
            System.out.println("resetPageNav");
        }
        pageCounter2.setText("");
        pageCounter3.setText("");
    }

    /**
     * Method to set the viewers rotation from external sources.
     *
     * @param rot The rotation to use in the Viewer
     */
    @Override
    public void setRotationFromExternal(final int rot) {

        rotation = rot;
        rotationBox.setSelectedIndex(rotation / 90);
    }

    /**
     * Method to set the viewers scaling from external sources.
     *
     * @param scale The scaling to use in the Viewer as a String
     */
    @Override
    public void setScalingFromExternal(final String scale) {

        if (debugFX) {
            System.out.println("setScalingFromExternal");
        }

        if (scale.startsWith("Fit ")) { //allow for Fit Page, Fit Width, Fit Height
            scalingBox.setSelectedItem(scale);
        } else {
            scaling = Float.parseFloat(scale);
            scalingBox.setSelectedItem(scale + '%');
        }

        //if (!Values.isProcessing()) {
        //((PdfDecoder) decode_pdf).repaint();
        //}
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getFrame()
     */
    @Override
    public Stage getFrame() {
        return stage;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object)
     */
    @Override
    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object)
     */
    public int showMessageDialog(final Object message1, final Object[] options, final int selectedChoice) {

        if (debugFX) {
            System.out.println("showMessageDialog");
        }

        int n = 0;
        //allow user to replace messages with our action
        boolean showMessage = true;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            showMessage = customMessageHandler.showMessage(message1);
        }

        if (showMessage) {
            final FXOptionDialog optionsDialog = new FXOptionDialog(stage, message1.toString(), "Message", FXOptionDialog.YES_NO_CANCEL_OPTION, options, options[selectedChoice]);
            n = optionsDialog.showOptionDialog();
        }

        return n;

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object, java.lang.String, int)
     */
    @Override
    public void showMessageDialog(final Object message, final String title, final int type) {

        if (debugFX) {
            System.out.println("showMessageDialog - Implemented");
        }

        //allow user to replace messages with our action
        boolean showMessage = true;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            showMessage = customMessageHandler.showMessage(message);
        }

        if (showMessage) {
            final FXMessageDialog dialog = new FXMessageDialog(stage, message.toString());
            dialog.setTitle(title);
            dialog.show();
        }
    }


    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showInputDialog(java.lang.Object, java.lang.String, int)
     */
    @Override
    public String showInputDialog(final Object message, final String title, final int type) {

        if (debugFX) {
            System.out.println("showINputDialog");
        }

        //allow user to replace messages with our action
        String returnMessage = null;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            returnMessage = customMessageHandler.requestInput(new Object[]{message, title, title});
        }

        if (returnMessage == null) {
            final FXInputDialog input = new FXInputDialog(stage, message.toString());
            input.setTitle(title);
            return input.showInputDialog();
        } else {
            return returnMessage;
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showInputDialog(java.lang.String)
     */
    @Override
    public String showInputDialog(final String message) {

        if (debugFX) {
            System.out.println("showInputDialog");
        }

        //allow user to replace messages with our action
        String returnMessage = null;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            returnMessage = customMessageHandler.requestInput(new String[]{message});
        }

        if (returnMessage == null) {
            final FXInputDialog input = new FXInputDialog(stage, message);
            return input.showInputDialog();
        } else {
            return returnMessage;
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showConfirmDialog(java.lang.String, java.lang.String, int)
     */
    @Override
    public int showConfirmDialog(final String message, final String message2, final int option) {

        if (debugFX) {
            System.out.println("showCOnfirmDialog");
        }
        //allow user to replace messages with our action
        int returnMessage = -1;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            returnMessage = customMessageHandler.requestConfirm(new Object[]{message, message2, String.valueOf(option)});
        }

        if (returnMessage == -1) {
            final FXOptionDialog optionsDialog = new FXOptionDialog(stage, message, message2, option, null, null);
            return optionsDialog.showOptionDialog();
        } else {
            return returnMessage;
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showOverwriteDialog(String file,boolean yesToAllPresent)
     */
    @Override
    public int showOverwriteDialog(final String file, final boolean yesToAllPresent) {

        if (debugFX) {
            System.out.println("showOverriteDialog");
        }
        final int n;

        //allow user to replace messages with our action and remove popup
        int returnMessage = -1;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            returnMessage = customMessageHandler.requestConfirm(new Object[]{file, String.valueOf(yesToAllPresent)});
        }

        if (returnMessage != -1) {
            return returnMessage;
        }

        final String message = file + '\n' + Messages.getMessage("PdfViewerMessage.FileAlreadyExists")
                + '\n' + Messages.getMessage("PdfViewerMessage.ConfirmResave");

        if (yesToAllPresent) {

            final Object[] buttonRowObjects = {
                    Messages.getMessage("PdfViewerConfirmButton.Yes"),
                    Messages.getMessage("PdfViewerConfirmButton.YesToAll"),
                    Messages.getMessage("PdfViewerConfirmButton.No"),
                    Messages.getMessage("PdfViewerConfirmButton.Cancel")
            };

            final FXOptionDialog dialog = new FXOptionDialog(stage,
                    message,
                    Messages.getMessage("PdfViewerMessage.Overwrite"),
                    FXOptionDialog.DEFAULT_OPTION,
                    buttonRowObjects,
                    buttonRowObjects[0]);

            n = dialog.showOptionDialog();

        } else {
            final FXOptionDialog dialog = new FXOptionDialog(stage,
                    message,
                    Messages.getMessage("PdfViewerMessage.Overwrite"),
                    FXOptionDialog.DEFAULT_OPTION,
                    null,
                    null);

            n = dialog.showOptionDialog();
        }

        return n;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object)
     */
    @Override
    public void showMessageDialog(final String message) {
        //FXCode to be uncommented when FXViewer implemented
        if (debugFX) {
            System.out.println("showMessageDialog - Implemented");
        }

        //Ensure dialog is handled on FX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final FXMessageDialog dialog = new FXMessageDialog(stage, message);
                dialog.show();
            }
        });
    }

    /**
     * Show a message dialog with the specified message and title
     *
     * @param message String message to be used in the dialog
     * @param title   String title to be used in the dialog
     */
    public void showMessageDialog(final String message, final String title) {

        if (debugFX) {
            System.out.println("showMessageDialog - Implemented " + title);
        }
        final FXMessageDialog dialog = new FXMessageDialog(stage, message);
        dialog.showAndWait();
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showMessageDialog(javax.swing.JTextArea)
     */
    @Override
    public void showMessageDialog(final Object info) {

        if (debugFX) {
            System.out.println("showMessageDialog");
        }

        //allow user to replace messages with our action
        boolean showMessage = true;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            showMessage = customMessageHandler.showMessage(info);
        }

        if (showMessage) {
            final FXMessageDialog dialog = new FXMessageDialog(stage, (Parent) info);
            dialog.show();
        }

    }

    /**
     * Method used to show popups that are displayed the first time the Viewer is opened
     */
    @Override
    public void showFirstTimePopup() {

        if (debugFX) {
            System.out.println("showFirstTImePopup");
        }

        //allow user to disable
        final boolean showMessage = (customMessageHandler != null && customMessageHandler.showMessage("first time popup"))
                || customMessageHandler == null;

        if (!showMessage || commonValues.getModeOfOperation() == Values.RUNNING_APPLET) {
            return;
        }

        try {
            final VBox supportLink = new VBox();
            supportLink.setAlignment(Pos.CENTER);

            final Hyperlink supportImg = new Hyperlink("", new ImageView(getClass().getResource("/org/jpedal/examples/viewer/res/supportScreenshot.png").toExternalForm()));
            supportImg.setBorder(Border.EMPTY);

            final Hyperlink supportText = new Hyperlink(Messages.getMessage("PdfViewer.SupportLink.Text1") + ' ' + Messages.getMessage("PdfViewer.SupportLink.Text2"));
            supportText.setBorder(Border.EMPTY);
            supportText.setTextAlignment(TextAlignment.CENTER);
            supportText.setWrapText(true);

            final EventHandler<ActionEvent> supportEvent = new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent t) {
                    try {
                        BrowserLauncher.openURL(Messages.getMessage("PdfViewer.SupportLink.Link"));
                    } catch (final Exception ex) {
                        LogWriter.writeLog("Exception " + ex.getMessage());
                        showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
                    }
                }
            };

            supportImg.setOnAction(supportEvent);
            supportText.setOnAction(supportEvent);

            supportLink.getChildren().addAll(supportImg, supportText);

            final FXMessageDialog supportDialog = new FXMessageDialog(stage, supportLink);
            supportDialog.setResizeable(true);
            supportDialog.setWidth(280);
            supportDialog.setHeight(220);
            supportDialog.setTitle(Messages.getMessage("PdfViewerTitle.RunningFirstTime"));
            supportDialog.show();

        } catch (final Exception e) {
            //JOptionPane.showMessageDialog(null, "caught an exception "+e);
            System.err.println(Messages.getMessage("PdfViewerFirstRunDialog.Error") + ' ' + e);
        } catch (final Error e) {
            //JOptionPane.showMessageDialog(null, "caught an error "+e);
            System.err.println(Messages.getMessage("PdfViewerFirstRunDialog.Error") + ' ' + e);
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#showConfirmDialog(java.lang.Object, java.lang.String, int, int)
     */
    @Override
    public int showConfirmDialog(final Object message, final String title, final int optionType, final int messageType) {

        if (debugFX) {
            System.out.println("showCOnfirmDialog");
        }

        //allow user to replace messages with our action
        int returnMessage = -1;

        //check user has not setup message and if we still show message
        if (customMessageHandler != null) {
            returnMessage = customMessageHandler.requestConfirm(new Object[]{message, title, String.valueOf(optionType), String.valueOf(messageType)});
        }

        if (returnMessage == -1) {
            final FXOptionDialog dialog = new FXOptionDialog(stage, message, title, optionType, null, null);
            return dialog.showOptionDialog();
        } else {
            return returnMessage;
        }
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#updateStatusMessage(java.lang.String)
     */
    @Override
    public void setDownloadProgress(final String message, final int percentage) {

        downloadBar.setProgress(message, percentage);

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                setMultibox(new int[]{});
            }
        });

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#updateStatusMessage(java.lang.String)
     */
    @Override
    public void updateStatusMessage(final String message) {

        if (debugFX) {
            System.out.println("updateStatusMessage not implemented for javafx");
        }

        //statusBar.updateStatus(message,0);
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#resetStatusMessage(java.lang.String)
     */
    @Override
    public void resetStatusMessage(final String message) {

        if (debugFX) {
            System.out.println("resetStatusMessage not implemented for javafx");
        }

        //statusBar.resetStatus(message);
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setStatusProgress(int)
     */
    @Override
    public void setStatusProgress(final int size) {

        if (debugFX) {
            System.out.println("setStatusProgress not implemented for javafx yet");
        }

        //statusBar.setProgress(size);
        setMultibox(new int[]{});
    }

    /**
     * set location of split pane between main PDF and outline/thumbnail panel
     */
    private void setSplitDividerLocation(final int size) {
        if (debugFX) {
            System.out.println("setSplitDividerLocation");
        }
        double w = center.getWidth();
        if (w == 0) { // If not set, set to 1 to prevent break
            w = 1;
        }
        center.setDividerPosition(0, size / w);

    }

    /**
     * getSplitDividerLocation not supported in OpenViewerFX
     *
     * @return Always returns 0
     */
    @Override
    public int getSplitDividerLocation() {
        if (debugFX) {
            System.out.println("getSplitDividerLocation not implemented et in JavaFXGUI");
        }
        return 0;
    }

    /**
     * Printing is not currently supported in OpenViewerFX
     *
     * @param printersList
     * @param defaultPrinter
     * @return
     */
    @Override
    public Object printDialog(final String[] printersList, final String defaultPrinter) {

        System.err.println("Print is  not supported in FX Viewer");

        return null;
    }

    /**
     * Gets the paper sizes for use by the print dialog
     *
     * @return a PaperSizes object holding all paper sizes
     */
    @Override
    public PaperSizes getPaperSizes() {

        if (debugFX) {
            System.out.println("getPaperSizes");
        }

        if (paperSizes == null) {
            paperSizes = new PaperSizes(properties.getValue("defaultPagesize"));
        }
        return paperSizes;
    }

    private void setThumbnails() {

        if (debugFX) {
            System.out.println("setThumbnails");
        }

        // FX impl of thumbnail worker
        final Task<Void> worker = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (thumbnails.isShownOnscreen()) {
                    setupThumbnailPanel();
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            thumbnails.generateOtherVisibleThumbnails(commonValues.getCurrentPage());
                        }

                    });

                }

                return null;
            }
        };

        new Thread(worker).start();

    }

    /**
     * Set the search text field to be used for the menu bar search input
     *
     * @param searchText A JTextField to be used for search input
     */
    @Override
    public void setSearchText(final Object searchText) {
        this.searchText = (TextField) searchText;
    }

    /**
     * Set the search list to be used to hold search results in menu bar search mode
     *
     * @param results an implementation of GUISearchList
     */
    @Override
    public void setResults(final GUISearchList results) {

        if (debugFX) {
            System.out.println("setResults");
        }

        this.results = results;

        if (searchInMenu && this.results.getResultCount() == 0) {
            showMessageDialog(Messages.getMessage("PdfViewerFileMenuFind.noResultText") + " \"" + results.getSearchTerm() + '"', Messages.getMessage("PdfViewerFileMenuFind.noResultTitle"), FXMessageDialog.INFORMATION_MESSAGE);
        }
    }

    /**
     * Get the TabPane, as an Object, used for the side tab bar
     *
     * @return TabPane used for the side tab bar
     */
    @Override
    public Object getSideTabBar() {
//        if(debugFX){
//            System.out.println("getSideTabBar");
//        }
//        
        return navOptionsPanel;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#enableSearchItems(boolean)
     */
    @Override
    public void enableSearchItems(final boolean enabled) {

        if (searchInMenu) { //Menu Bar search
            searchText.setDisable(!enabled);
            options.setDisable(!enabled);
            fxButtons.getButton(Commands.NEXTRESULT).setEnabled(false);
            fxButtons.getButton(Commands.PREVIOUSRESULT).setEnabled(false);
        } else if (fxButtons.getButton(Commands.FIND) != null) { //External Window search
            fxButtons.getButton(Commands.FIND).setEnabled(enabled);
        }
    }

    /**
     * Removes the search window from view with option to clear search results
     *
     * @param justHide true to just remove the search window, false to also clear current results
     */
    @Override
    public void removeSearchWindow(final boolean justHide) {

        if (debugFX) {
            System.out.println("removeSearchWindow");
        }

        searchFrame.removeSearchWindow(justHide);
    }

    @Override
    public void alterProperty(final String value, final boolean set) {
        if (GUI.debugFX) {
            GUIModifier.alterProperty(value, set, this);
        }
    }

    /**
     * Method to dispose of objects used by the interface before closing.
     */
    @Override
    public void dispose() {

        if (pageContainer != null) {
            if (fxChangeListener != null) {
                pageContainer.viewportBoundsProperty().removeListener(fxChangeListener);
                fxChangeListener = null;
            }
            pageContainer = null;
        }

        if (debugFX) {
            System.out.println("dispose");
        }

        fxButtons.dispose();
        super.dispose();

        mouseHandler = null;

        pageTitle = null;
        bookmarksTitle = null;

        signaturesTitle = null;
        layersTitle = null;

        currentCommandListener = null;

        if (navButtons != null) {
            navButtons.getChildren().removeAll(navButtons.getChildren());
        }
        navButtons = null;
        menuItems.dispose();

//		if(desktopPane!=null)
//			desktopPane.removeAll();
//		desktopPane=null;
        if (navOptionsPanel != null) {
            navOptionsPanel.getTabs().removeAll();
        }
        navOptionsPanel = null;

        headFont = null;

        textFont = null;

        pageCounter2 = null;

        pageCounter3 = null;

        if (navToolBar != null) {
            navToolBar.getItems().removeAll(navToolBar.getItems());
        }
        navToolBar = null;

        if (pagesToolBar != null) //pagesToolBar.removeAll();
        {
            pagesToolBar = null;
        }

        layersObject = null;

        //release memory at end
        if (memoryMonitor != null) {
            memoryMonitor.stop();
        }
    }

    /**
     * Not part of API - used internally
     */
    @Override
    public boolean getPageTurnScalingAppropriate() {

        if (debugFX) {
            System.out.println("getPageTurnScalingAppropriate");
        }

        return pageTurnScalingAppropriate;
    }

    /**
     * Not part of API - used internally
     */
    @Override
    public SwingCursor getGUICursor() {
        return guiCursor;
    }

    /**
     * Not part of API - used internally
     */
    @Override
    public void rescanPdfLayers() {
//        layersPanel.rescanPdfLayers();
    }

    /**
     * Checks if title matches the title of any of the side tab bar tabs
     *
     * @param title String title to check for.
     * @return String title of tab found.
     */
    @Override
    public String getTitles(final String title) {

        if (debugFX) {
            System.out.println("getTitles");
        }

        if (title.equals(pageTitle)) {
            return pageTitle;
        } else if (title.equals(bookmarksTitle)) {
            return bookmarksTitle;
        } else if (title.equals(signaturesTitle)) {
            return signaturesTitle;
        } else if (title.equals(layersTitle)) {
            return layersTitle;
        }
        return null;
    }

    /**
     * getStatusBar not supported in OpenViewerFX
     *
     * @return Always returns null
     */
    @Override
    public Object getStatusBar() {

        if (debugFX) {
            System.out.println("getStatusBar not implemented yet for JavaFX");
        }

        return null;
    }

    /**
     * Get the GUIButtons object that holds all buttons used by the user interface
     *
     * @return GUIButtons implementation used by the user interface
     */
    @Override
    public GUIButtons getButtons() {
        return fxButtons;
    }

    /**
     * Get the GUIMenuItems object that holds all the menu items used by the user interface.
     *
     * @return GUIMenuItems implementation used by the user interface
     */
    @Override
    public GUIMenuItems getMenuItems() {
        return menuItems;
    }

    /**
     * Set tabs not initialised flag to prevent use of tabs.
     *
     * @param b true to mark the tabs as not initialised, false to mark as initialised
     */
    @Override
    public void setTabsNotInitialised(final boolean b) {

        if (debugFX) {
            System.out.println("setTabsNotINitialised");
        }

        tabsNotInitialised = b;
    }

    /**
     * Key method that calls decodePage from GUI.java
     */
    @Override
    public void decodePage() {

        if (debugFX) {
            System.out.println("decodePage");
        }

        final Transition exitTransition = null;

        //only in singlePage mode
        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
            //  exitTransition = FXViewerTransitions.exitTransition((PdfDecoderFX) decode_pdf, transitionType, TransitionDirection.NONE);
        }

        if (exitTransition != null) {

            //if exists any objects will be stored in this and drawn at correct time
            decode_pdf.addExternalHandler(new FXAdditionalData(), Options.JavaFX_ADDITIONAL_OBJECTS);

            final GUIFactory gui = this;
            resetPDFBorder(Color.TRANSPARENT);

            final Transition entryTransition = FXViewerTransitions.entryTransition((PdfDecoderFX) decode_pdf, transitionType, TransitionDirection.NONE);
            entryTransition.setOnFinished(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(final ActionEvent t) {
                    resetPDFBorder(Color.DARKGOLDENROD);
                }
            });

            exitTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent t) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            decodeGUIPage(gui);
                        }
                    });

                    entryTransition.play();
                }
            });

            exitTransition.play();
        } else {
            //Ensure thumbnail scroll bar is updated when page changed
            if (getThumbnailScrollBar() != null) {
                setThumbnailScrollBarValue(commonValues.getCurrentPage() - 1);
            }
            // Ensure the page is visible
            ((javafx.scene.Node) decode_pdf).setOpacity(1);
            resetPDFBorder(Color.DARKGOLDENROD);
            decodeGUIPage(this);
        }

    }

    /**
     * Set the transition type to be used when changing between pages
     */
    public void updateTransitionType() {
        transitionType = TransitionType.valueOf(properties.getValue("transitionType").replace(" ", "_"));
    }

    /**
     * Get the page container for the PDF display area
     *
     * @return ScrollPane containing the PdfDecoderFX
     */
    @Override
    public ScrollPane getPageContainer() {
        return pageContainer;
    }

    /**
     * Get the PdfDecoderFX object used to display the PDF in the user interface
     *
     * @return PdfDecoderFX object used for display
     */
    @Override
    public PdfDecoderFX getPdfDecoder() {
        return ((PdfDecoderFX) decode_pdf);
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void addComboListenerAndLabel(final GUICombo combo) {
        ((ComboBoxBase<String>) combo).setOnAction((EventHandler<ActionEvent>) currentCommandListener.getCommandListener());
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void addGUIComboBoxes(final GUICombo combo) {
        fxButtons.getTopButtons().getItems().add((javafx.scene.Node) combo);
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setupCenterPanelBackground() {
        if (GUI.debugFX) {
            System.out.println("setupCenterPanelBackground in JavaFxGUI.java : DONE");
        }

        String propValue = properties.getValue("replacePdfDisplayBackground");
        if (!propValue.isEmpty()
                && propValue.equalsIgnoreCase("true")) {
            //decode_pdf.useNewGraphicsMode = false;
            propValue = properties.getValue("pdfDisplayBackground");
            if (!propValue.isEmpty()) {
                currentCommands.executeCommand(Commands.SETDISPLAYBACKGROUND, new Object[]{Integer.parseInt(propValue)});
            }
            final int col = Integer.parseInt(propValue);
            final int r = ((col >> 16) & 255);
            final int g = ((col >> 8) & 255);
            final int b = ((col) & 255);
            pageContainer.setStyle("-fx-background:rgb(" + r + ',' + g + ',' + b + ");");

        } else {

            if (decode_pdf.getDecoderOptions().getDisplayBackgroundColor() != null) {
                final int col = decode_pdf.getDecoderOptions().getDisplayBackgroundColor().getRGB();
                final int r = ((col >> 16) & 255);
                final int g = ((col >> 8) & 255);
                final int b = ((col) & 255);
                pageContainer.setStyle("-fx-background:rgb(" + r + ',' + g + ',' + b + ");");
            } else if (decode_pdf.useNewGraphicsMode()) {
                pageContainer.setStyle("-fx-background:#555565;");
            } else {
                pageContainer.setStyle("-fx-background:#190190190;");
            }
        }
    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setupComboBoxes() {

        final String[] rotationValues = {"0", "90", "180", "270"};

        final String[] scalingValues = {Messages.getMessage("PdfViewerScaleWindow.text"), Messages.getMessage("PdfViewerScaleHeight.text"),
                Messages.getMessage("PdfViewerScaleWidth.text"),
                "25%", "50%", "75%", "100%", "125%", "150%", "200%", "250%", "500%", "750%", "1000%"};

        //set new default if appropriate
        String choosenScaling = System.getProperty("org.jpedal.defaultViewerScaling");

        //Only use value from properties is VM arguement not set
        if (choosenScaling == null) {
            choosenScaling = properties.getValue("startScaling");
        }

        if (choosenScaling != null) {
            final int total = scalingValues.length;
            for (int aa = 0; aa < total; aa++) {
                if (scalingValues[aa].equals(choosenScaling)) {
                    defaultSelection = aa;
                    aa = total;
                }
            }
        }

        scalingBox = new JavaFXCombo(scalingValues);
        ((javafx.scene.Node) scalingBox).setStyle("-fx-background-color: #FFFFFF;");
        scalingBox.setEditable(true);
        scalingBox.setSelectedIndex(defaultSelection); //set default before we add a listener

        //if you enable, remember to change rotation and quality Comboboxes
        rotationBox = new JavaFXCombo(rotationValues);
        ((javafx.scene.Node) rotationBox).setStyle("-fx-background-color: #FFFFFF;");
        rotationBox.setSelectedIndex(0); //set default before we add a listener
    }

    /**
     * Uses Keyboard Arrow Keys (UP/DOWN LEFT/RIGT) to Control Page Navigation.
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setupKeyboardControl() {
        if (1 == 1) {
            return;
        }

        ((javafx.scene.Node) decode_pdf).setOnKeyPressed(new EventHandler<KeyEvent>() {
            int count;
            int pageChange;
            java.util.Timer t2;

            @Override
            public void handle(final KeyEvent key) {

                final KeyCode keyPressed = key.getCode();  //Store the current key press

                if (keyPressed == KeyCode.LEFT || keyPressed == KeyCode.RIGHT) {
                    if (pageContainer.getWidth() > ((Region) decode_pdf).getWidth()) {
                        if (keyPressed == KeyCode.LEFT) {
                            pageChange--;  //Go back a page
                        } else {
                            pageChange++;  //Go forward a page
                        }
                    }
                } else if ((keyPressed == KeyCode.UP || keyPressed == KeyCode.DOWN) && (count == 0)) {
                    if (keyPressed == KeyCode.UP
                            && pageContainer.getVvalue() == pageContainer.getVmin()
                            && commonValues.getCurrentPage() > 1) {
                        //change page
                        pageChange--;

                    } else if (keyPressed == KeyCode.DOWN
                            && (pageContainer.getVvalue() == pageContainer.getVmax() - pageContainer.getHeight() || pageContainer.getHeight() == 0)
                            && commonValues.getCurrentPage() < decode_pdf.getPageCount()) {
                        //change page
                        pageChange++;

                    }
                }

                count++;

                if (pageChange != 0) {
                    if (t2 != null) {
                        t2.cancel();
                    }

                    final TimerTask t = new TimerTask() {

                        @Override
                        public void run() {

                            int p = (commonValues.getCurrentPage() + pageChange);

                            if (p < 1) {
                                p = 1;
                            } else if (p > decode_pdf.getPageCount()) {
                                p = decode_pdf.getPageCount();
                            }

                            if (p != commonValues.getCurrentPage()) {
                                final String page = String.valueOf(p);

                                currentCommands.executeCommand(Commands.GOTO, new Object[]{page});

                                if (pageContainer.getVvalue() == pageContainer.getVmin()
                                        && commonValues.getCurrentPage() > 1) {

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
//                                            pageContainer.setVvalue(pageContainer.getVmax());
                                        }
                                    });

                                } else if ((pageContainer.getVvalue() == pageContainer.getVmax() - pageContainer.getHeight() || pageContainer.getHeight() == 0)
                                        && commonValues.getCurrentPage() < decode_pdf.getPageCount()) {

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
//                                            pageContainer.setVvalue(pageContainer.getVmin());
                                        }
                                    });
                                }

                            }

                            pageChange = 0;
                            if (t2 != null) {
                                t2.cancel();
                            }
                        }

                    };

                    //Stop timer from remaining active
                    t2 = new java.util.Timer();
                    t2.schedule(t, 500);

                }

            }

        });
    }

    @Override
    protected void setupPDFDisplayPane() {

        if (GUI.debugFX) {
            System.out.println("setupPDFDisplayPane not implemented for JavaFX in JavaFxGUI class");
        }

        previewOnSingleScroll = properties.getValue("previewOnSingleScroll").equalsIgnoreCase("true");


        if (previewOnSingleScroll && 1 == 2) { //currently disable as some bugs in pageflow to normal modes
            pageScroll = new ScrollBar();
            pageScroll.setOrientation(Orientation.VERTICAL);
            pageScroll.setValue(0);
            pageScroll.setVisibleAmount(1);
            pageScroll.setMin(0);
            pageScroll.setMax(1);
            pageScroll.setUnitIncrement(1);
            pageScroll.valueProperty().addListener(new JavaFXScrollListener(this, pageScroll));
        }

        //Sets up the ScrollPane
        pageContainer = new ScrollPane();
        pageContainer.setCacheHint(CacheHint.QUALITY);
        pageContainer.setPannable(true);
        pageContainer.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        pageContainer.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        decode_pdf.setInset(0, 0); //Not sure if this may cause bugs further down the line.

        /*
         * Sets up the Group object which holds our PdfDecoderInt Object
         * Code to be removed and replaced with decode_pdf once bugs fixed.
         */
        group = new Group();
        group.getChildren().add((javafx.scene.Node) decode_pdf);
        pageContainer.setContent(group);

        //Apply the group object as the Scroll Panes contents
        fxChangeListener = new ChangeListener<Bounds>() {
            @Override
            public void changed(final ObservableValue<? extends Bounds> ov, final Bounds ob, final Bounds nb) {

                if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                    scaleAndRotate();
                }
            }
        };

        pageContainer.viewportBoundsProperty().addListener(fxChangeListener);

        //Keyboard control of next/previous page gets implemented here...
        setupKeyboardControl();

    }

    /**
     * Overrides method from GUI.java, see GUI.java for DOCS.
     */
    @Override
    protected void setupBorderPanes() {

        if (GUI.debugFX) {
            System.out.println("setupBorderPanes not implemented for JavaFX in JavaFxGUI class");
        }
        //Swing dependant code, needs FX implementation.
        setupSidebarTitles();

        center = new SplitPane();
        setupTabPane();

        center.getItems().addAll(navOptionsPanel, pageContainer);
        if (pageScroll != null) {
            center.getItems().add(pageScroll);
        }

        setSplitDividerLocation(collapsedSize);
        tabsExpanded = false;

        //update scaling when divider moved
        center.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> ov, final Number oldN, final Number newN) {
                if (tabsExpanded) {
                    expandedSize = (int) navOptionsPanel.getWidth();
                }
            }
        });

        String propValue = properties.getValue("startSideTabOpen");
        if (!propValue.isEmpty()) {
            sideTabBarOpenByDefault = propValue.equalsIgnoreCase("true");
        }

        propValue = properties.getValue("startSelectedSideTab");
        if (!propValue.isEmpty()) {
            startSelectedTab = propValue;
        }

        if (!hasListener) {
            navOptionsPanel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent t) {
                    // Don't handle unless the click is on the tab bar
                    if (t.getX() < 30) {
                        handleTabbedPanes();
                    }
                }
            });
            hasListener = true;
        }
    }

    /**
     * Sets up the position & visual style of the items on the bottom toolbar
     * (page navigation buttons etc).
     */
    @Override
    protected void setupBottomToolBarItems() {

        if (GUI.debugFX) {
            System.out.println("setupBottomToolBarItems is not implemented in class JavaFxGUI for JavaFX");
        }

        pageCounter2.setEditable(true);
        pageCounter2.setTooltip(new Tooltip(Messages.getMessage("PdfViewerTooltip.goto")));

        pageCounter2.setPrefColumnCount(2);

        pageCounter2.setMaxSize(pageCounter2.getPrefWidth(), pageCounter2.getPrefHeight());

        //Listener which changes the page to user input
        pageCounter2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                // Temp patch to get page nav working
                commonValues.setSelectedFile(decode_pdf.getFileName());
                commonValues.setPageCount(decode_pdf.getPageCount());
                commonValues.setCurrentPage(decode_pdf.getPageNumber());

                String value = pageCounter2.getText().trim();

                //Check if mapped and if so set the actual page number
                //This is updated later to the correct page label
                if (decode_pdf.getIO().getPageLabels() != null && decode_pdf.getIO().getPageLabels().containsValue(value)) {
                    final Set<Map.Entry<Integer, String>> entries = decode_pdf.getIO().getPageLabels().entrySet();
                    for (final Map.Entry<Integer, String> e : entries) {
                        if (e.getValue().equals(value)) {
                            value = e.getKey().toString();
                            break;
                        }
                    }
                }

                currentCommands.executeCommand(Commands.GOTO, new Object[]{value});
            }
        });

        pageCounter2.setAlignment(Pos.CENTER);

        setPageNumber();

        navButtons.setPrefSize(5, 24);

    }

    /**
     * Creates a glowing border around the PDFDisplayPane.
     */
    @Override
    protected void setupPDFBorder() {

        if (GUI.debugFX) {
            System.out.println("setupPDFBorder : DONE ");
        }

        resetPDFBorder(Color.DARKGOLDENROD);
    }

    private void resetPDFBorder(final Color color) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                final DropShadow pdfBorder = new DropShadow();
                pdfBorder.setOffsetY(0f);
                pdfBorder.setOffsetX(0f);

                pdfBorder.setWidth(dropshadowDepth);
                pdfBorder.setHeight(dropshadowDepth);

                //if decoder.borderpresent set effect else transparent
                if (decode_pdf.isBorderPresent()) {
                    pdfBorder.setColor(color);
                } else {
                    pdfBorder.setColor(Color.TRANSPARENT);
                }

                ((javafx.scene.Node) decode_pdf).setEffect(pdfBorder);
            }
        });
    }

    /**
     * Creates the top two menu bars, the file loading & viewer properties one
     * and the PDF toolbar, the one which controls searching etc.
     */
    @Override
    protected void createTopMenuBar() {

        if (GUI.debugFX) {
            System.out.println("createTopMenuBar is not yet implemented for JavaFX in JavaFxGUI.java");
        }

        topPane.setStyle("-fx-background-color: #F5F6F7;");

    }

    /**
     * Creates the Main Display Window for all of the JavaFX Content.
     *
     * @param width  is of type int
     * @param height is of type int
     */
    @Override
    protected void createMainViewerWindow(final int width, final int height) {

        if (GUI.debugFX) {
            System.out.println("createMainViewerWindow : DONE ");
        }

        root.setTop(topPane);
        root.setCenter(center);
        root.setBottom(navButtons);

        if (stage != null) {
            scene = new Scene(root, width, height);
            stage.setScene(scene);

            stage.show();
        }
    }

    /**
     * Sets the scroll bar policy for the display areas scroll pane
     *
     * @param pol ScrollPolicy to be applied to the scroll pane
     */
    @Override
    public void setScrollBarPolicy(final ScrollPolicy pol) {
        switch (pol) {
            case VERTICAL_NEVER:
                pageContainer.setVbarPolicy(ScrollBarPolicy.NEVER);
                break;
            case VERTICAL_AS_NEEDED:
                pageContainer.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
                break;
            case HORIZONTAL_NEVER:
                pageContainer.setHbarPolicy(ScrollBarPolicy.NEVER);
                break;
            case HORIZONTAL_AS_NEEDED:
                pageContainer.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
                break;

        }
    }

    private void setupTabPane() {
        navOptionsPanel = new TabPane();
        navOptionsPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        navOptionsPanel.setSide(Side.LEFT);

        // Dummy tab so no tab is shown when the viewer is started
        final Tab dummy = new Tab("");
        dummy.setDisable(true);
        dummy.setStyle("-fx-opacity:0;");

        final Tab search = new Tab("Search");

        ((Tab) thumbnails).setText(pageTitle);
        ((Tab) tree).setText(bookmarksTitle);
        layersPanel.setText(layersTitle);
        signaturesPanel.setText(signaturesTitle);

        navOptionsPanel.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(final ObservableValue<? extends Tab> ov, final Tab t, final Tab t1) {
                if (navOptionsPanel.getSelectionModel().getSelectedItem() == search) {
                    search.setContent(((JavaFXSearchWindow) searchFrame).getContentVB());
                }

                if (!tabsExpanded && !dummy.isSelected()) {
                    setSplitDividerLocation(expandedSize);
                    tabsExpanded = true;
                } else if (tabsExpanded && t1.equals(t)) {
                    setSplitDividerLocation(collapsedSize);
                    tabsExpanded = false;
                }
            }
        });

        // Stops the thumbnail panel width being changed when the split pane is
        SplitPane.setResizableWithParent(navOptionsPanel, Boolean.FALSE);
        if (searchFrame.getViewStyle() == GUISearchWindow.SEARCH_TABBED_PANE) {
            navOptionsPanel.getTabs().addAll((Tab) thumbnails, search, dummy);
        } else {
            // Make sure dummy goes to the end
            navOptionsPanel.getTabs().addAll((Tab) thumbnails, dummy);
        }
        navOptionsPanel.getSelectionModel().select(dummy);

        tabsNotInitialised = false;
    }

    /**
     * Returns the RecentDocumentFactory used to hand the recent document list
     *
     * @return RecentDocumentFactory used by the user interface
     */
    @Override
    public RecentDocumentsFactory getRecentDocument() {
        if (recent != null) {
            return recent;
        } else {
            return recent = new JavaFXRecentDocuments(PropertiesFile.getNoRecentDocumentsToDisplay());
        }
    }

    /**
     * Create the RecentDocuments for the user interface
     */
    @Override
    public void setRecentDocument() {
        recent = new JavaFXRecentDocuments(PropertiesFile.getNoRecentDocumentsToDisplay());
    }

    /**
     * Open a file in the Viewer
     *
     * @param fileToOpen String filename for the file to be opened
     */
    @Override
    public void openFile(final String fileToOpen) {
        if (fileToOpen != null) {
            JavaFXOpenFile.open(fileToOpen, commonValues, searchFrame, this, decode_pdf, properties, thumbnails);
        }

    }

    /**
     * Open a file in the Viewer
     *
     * @param fileName String filename for the file to be opened
     */
    @Override
    public void open(final String fileName) {
        JavaFXOpenFile.open(fileName, commonValues, searchFrame, this, decode_pdf, properties, thumbnails);
    }

    /**
     * Enable and make visible different parts of the page counter of all of it
     *
     * @param value      PageCounter value for the different page counter elements
     * @param enabled    true to enable the counter section, false to disable.
     * @param visibility true to make visible the counter section, false to disable.
     */
    @Override
    public void enablePageCounter(final PageCounter value, final boolean enabled, final boolean visibility) {
        switch (value) {

            case PAGECOUNTER1:
                pageCounter1.setVisible(visibility);
                pageCounter1.setDisable(!enabled);
                break;

            case PAGECOUNTER2:
                pageCounter2.setVisible(visibility);
                pageCounter2.setDisable(!enabled);
                break;

            case PAGECOUNTER3:
                pageCounter3.setVisible(visibility);
                pageCounter3.setDisable(!enabled);
                break;

            case ALL:
                pageCounter1.setVisible(visibility);
                pageCounter1.setDisable(!enabled);
                pageCounter2.setVisible(visibility);
                pageCounter2.setDisable(!enabled);
                pageCounter3.setVisible(visibility);
                pageCounter3.setDisable(!enabled);
                break;

            default:
                System.out.println("No Value detected, please choose from Enum PageCounter in GUI.java");
                break;
        }
    }

    /**
     * Remove the ChangeListener from the PdfDecoder and flush cached pages
     */
    @Override
    public void removePageListener() {

        // remove listener if not removed by close
        if (viewListener != null) {

            //flush any cached pages
            decode_pdf.getPages().flushPageCaches();

            removeComponentListener(viewListener);

            viewListener.dispose();
            viewListener = null;

        }
    }

    /**
     * Set the text content for a section of the page counter.
     *
     * @param value PageCounter value for the different page counter elements
     * @param text  String value to be place in the page counter
     */
    @Override
    public void setPageCounterText(final PageCounter value, final String text) {
        switch (value) {

            case PAGECOUNTER1:
                pageCounter1.setText(text);
                break;

            case PAGECOUNTER2:
                pageCounter2.setText(text);
                break;

            case PAGECOUNTER3:
                pageCounter3.setText(text);
                break;

            default:
                System.out.println("No Value detected, please choose from Enum PageCounter in GUI.java");
                break;
        }
    }

    /**
     * Get the page counter segments
     *
     * @param value PageCounter value for the different page counter elements
     * @return an Object containing the section of the page counter
     */
    @Override
    public Object getPageCounter(final PageCounter value) {
        switch (value) {

            case PAGECOUNTER1:
                return pageCounter1;

            case PAGECOUNTER2:
                return pageCounter2;

            case PAGECOUNTER3:
                return pageCounter3;

            default:
                System.out.println("No Value detected, please choose from Enum PageCounter in GUI.java");
                return 0;
        }
    }

    /**
     * Update the page counter input size to accommodate the contents
     */
    @Override
    public void updateTextBoxSize() {
        //Set textbox size
        int col = (String.valueOf(commonValues.getPageCount())).length();
        if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
            col *= 2;
        }
        if (col < 2) {
            col = 2;
        }
        if (col > 10) {
            col = 10;
        }
        pageCounter2.setPrefColumnCount(col);
        pageCounter2.setMaxSize(pageCounter2.getPrefWidth(), pageCounter2.getPrefHeight());
    }

    /**
     * Enable and or set visibility for the cursor display for multi box
     *
     * @param enabled true to enable, false to disable
     * @param visible true to make visible, false to make invisible
     */
    @Override
    public void enableCursor(final boolean enabled, final boolean visible) {
        coordsFX.setDisable(!enabled);
        coordsFX.setVisible(visible);
    }

    @Override
    public void enableMemoryBar(final boolean enabled, final boolean visible) {
        memoryBarFX.setDisable(!enabled);
        memoryBarFX.setVisible(visible);
    }

    @Override
    public void enableNavigationBar(final boolean enabled, final boolean visible) {
        navButtons.setDisable(!enabled);
        navButtons.setVisible(visible);
    }

    /**
     * Enable and or set visibility for the download display for multi box
     *
     * @param enabled true to enable, false to disable
     * @param visible true to make visible, false to make invisible
     */
    @Override
    public void enableDownloadBar(final boolean enabled, final boolean visible) {
        downloadBar.setDisable(!enabled);
        downloadBar.setVisible(visible);
    }

    /**
     * Get the current count of tabs on the side tab bar
     *
     * @return int value representing the number of tabs currently visible
     */
    @Override
    public int getSidebarTabCount() {
        return navOptionsPanel.getTabs().size();
    }

    /**
     * Get the title of the tab at the given position
     *
     * @param pos The position of the tab on the tabbed pane
     * @return String title of the given tab or an empty String is tab not present.
     */
    @Override
    public String getSidebarTabTitleAt(final int pos) {
        return navOptionsPanel.getTabs().get(pos).getText();
    }

    /**
     * Remove the tab at the specified position from the side tab bar
     *
     * @param pos Tab index to removed.
     */
    @Override
    public void removeSidebarTabAt(final int pos) {
        navOptionsPanel.getTabs().remove(pos);
    }

    /**
     * Get the divider location for the viewer
     *
     * @return double representation of the divider location
     */
    @Override
    public double getDividerLocation() {
        double w = center.getWidth();
        if (w == 0) { // If not set, set to 1 to prevent break
            w = 1;
        }
        //Convert from double value to int size based on viewer width (To match Swing)
        return w * center.getDividers().get(0).getPosition();
    }

    /**
     * Get the start / collapsed position of the split pane divider
     *
     * @return divider position as a double
     */
    @Override
    public double getStartSize() {
        return collapsedSize;
    }

    /**
     * Set the start / collapsed position of the split pane divider
     *
     * @param size position as an int
     */
    @Override
    public void setStartSize(final int size) {
        collapsedSize = size; //We multiply because JavaFX uses double not int.
    }

    /**
     * calculates the scaling required to make the given area visible
     */
    @Override
    public float scaleToVisible(final float left, final float right, final float top, final float bottom) {

        float scaling;

        final float width;
        final float height;

        //if (isSingle) {
        width = (float) pageContainer.getWidth() * 0.95f;
        height = (float) pageContainer.getHeight() * 0.95f;

        //} else {
        //    width = desktopPane.getSelectedFrame().getWidth();
        //    height = desktopPane.getSelectedFrame().getHeight();
        //}
        final float widthScaling = (right - left) / width;
        final float heightScaling = (top - bottom) / height;

        if (widthScaling > heightScaling) {
            scaling = widthScaling;
        } else {
            scaling = heightScaling;
        }

        scaling = decode_pdf.getDPIFactory().adjustScaling(scaling);

        return scaling;
    }

    /**
     * Get the thickness of the glow border
     *
     * @return thickness of glow as an int
     */
    @Override
    public int getDropShadowDepth() {
        return dropshadowDepth;
    }

    /**
     * Get the root pane that holds the GUI interface
     *
     * @return BorderPane object for the user interface
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Set is the page display can be panned
     *
     * @param pan boolean value to control if pannable
     */
    @Override
    public void setPannable(final boolean pan) {
        pageContainer.setPannable(pan);
    }

    /**
     * Set up the split pane divider for use
     *
     * @param size       unsupported in OpenViewerFX
     * @param visibility true to set left side of divider visible and enabled, false to set disabled and invisible
     */
    @Override
    public void setupSplitPaneDivider(final int size, final boolean visibility) {
        center.setDisable(!visibility);
        center.setVisible(visibility);
    }

    /**
     * Enable and or set visibility for the status bar for multi box
     *
     * @param enabled true to enable, false to disable
     * @param visible true to make visible, false to make invisible
     */
    @Override
    public void enableStatusBar(final boolean enabled, final boolean visible) {
        // System.out.println("enableStatusBar not yet implemented for JavaFX");
    }

    private void removeComponentListener(final RefreshLayout viewListener) {

        final ScrollPane customFXHandle = pageContainer;

        //picks up mode change
        customFXHandle.viewportBoundsProperty().removeListener(viewListener);

        customFXHandle.vvalueProperty().removeListener(viewListener);
        customFXHandle.hvalueProperty().removeListener(viewListener);
    }

    private void addComponentListener(final RefreshLayout viewListener) {

        final ScrollPane customFXHandle = pageContainer;

        if (customFXHandle != null) {
            //picks up mode change
            customFXHandle.viewportBoundsProperty().addListener(viewListener);

            customFXHandle.vvalueProperty().addListener(viewListener);
            customFXHandle.hvalueProperty().addListener(viewListener);
        }
    }

    /**
     * Annotation panel is not available in FX Viewer
     */
    @Override
    public GUIAnnotationPanel getAnnotationPanel() {
        LogWriter.writeLog("JavaFxGUI.getAnnotationPanel is not available in FX Viewer.");
        return null;
    }

    /**
     * Glass pane is not available in FX Viewer
     */
    @Override
    public Object getGlassPane() {
        LogWriter.writeLog("JavaFxGUI.getGlassPane is not available in FX Viewer.");
        return null;
    }

    //class to repaint multiple views
    class RefreshLayout implements ChangeListener<Object> {

        final PageMoveTracker tracker = new PageMoveTracker();

        final PdfDecoderInt decode_pdf;

        RefreshLayout(final PdfDecoderInt pdf) {
            this.decode_pdf = pdf;
        }

        //fix submitted by Niklas Matthies
        public void dispose() {
            tracker.dispose();
        }

        @Override
        public void changed(final ObservableValue ov, final Object t, final Object t1) {

            // System.out.println("XXXXXXX"+ov);//+" "+t+" "+t1);
            //  final ScrollPane customFXHandle= ((JavaFxGUI)getExternalHandler(Options.MultiPageUpdate)).getPageContainer();
            //  customFXHandle.setVvalue(500);
            //  pages.getDisplayedRectangle();
            tracker.startTimer(decode_pdf.getPages(), decode_pdf.getPageNumber(), (FileAccess) decode_pdf.getExternalHandler(Options.FileAccess));

        }
    }

    /**
     * Not used in FX.
     *
     * @throws RuntimeException When called throws a runtime exception
     */
    @Override
    public PdfDecoderInt openNewMultiplePage(final String selectedFile, final Values commonValues) {
        throw new RuntimeException("Not implemented in JavaFX");

    }

    /**
     * triggerPageTurnAnimation is unsupported in OpenViewerFX
     */
    @Override
    public void triggerPageTurnAnimation(final PdfDecoderInt decode_pdf, final Values commonValues, final int updatedTotal, final boolean rightTurn) {

    }

    @Override
    protected String getTitle() {

        if (OpenViewerFX.isOpenFX) {
            return "LGPL PDF JavaFX Viewer " + PdfDecoderInt.version;
        } else {
            return super.getTitle();
        }
    }


}

// old method for temporary usage
//    private void adjustGroupToCenter(){
//        
//        if(sudaDebug){
//            System.out.println("adjustGroupToCenter called");
//            return;
//        }
//        
//        final int displayView=decode_pdf.getDisplayView();
//        
//        final double scale;
//        double pw=0;
//        final int pageNum = decode_pdf.getPageNumber();
//        PdfPageData pageData=decode_pdf.getPdfPageData();
//        final Transform at = ((PdfDecoderFX)decode_pdf).getTransforms().get(0);
//        
//        /*
//         * the state of the page's rotation is used to determine which scale factor
//         * we use as well as which side we use to translate.
//         */
//        if(rotation == 90){
//            scale = at.getMxy();
//        }else if(rotation == 180){
//            scale = at.getMyy();
//        }else if(rotation == 270){
//            scale = Math.abs(at.getMyx());
//        }else{ // rotation == 0 & handle default
//            scale = at.getMxx();
//        }
//        
//        if(displayView==Display.SINGLE_PAGE || displayView==Display.CONTINUOUS){
//            if(rotation == 90 || rotation==270){
//                pw = pageData.getCropBoxHeight(pageNum);
//            }else{ // rotation == 0 & handle default
//                pw = pageData.getCropBoxWidth(pageNum);
//            }
//        }else if(displayView==Display.CONTINUOUS_FACING){
//            
//            int otherPage=pageNum;
//           
//            //if first page or last odd, choose one above/below for figure
//            if(otherPage==1){
//                otherPage++;
//            }else if((otherPage &1)==1 && otherPage==pageData.getPageCount()){
//                otherPage--;
//            }
//            if((pageNum & 1)==1){
//                otherPage--;
//            }else{
//                otherPage++;
//            }
//            
//            if(rotation == 90 || rotation==270){
//                
//                pw = pageData.getCropBoxHeight(pageNum)+pageData.getCropBoxHeight(otherPage);
//            }else{ // rotation == 0 & handle default
//                pw = pageData.getCropBoxWidth(pageNum)+pageData.getCropBoxWidth(otherPage);
//            }
//            
//        }else{
//            
//            if(LogWriter.isOutput()){
//                LogWriter.writeLog("Not yet coded for display view option "+displayView);
//            }
//            
//        }
//        
//        // Account for drop shadow
//        pw += dropshadowDepth*2;
//        
//        final double pageWidth = pw * scale;
//        
//        // Get the adjustment needed to center the page
//        // (scrollpane width / 2) - (page width / 2)
//        double adjustment = (((pageContainer.getViewportBounds().getWidth()) / 2) - (pageWidth /2));
//        // Keep the group within the viewport of the scrollpane
//        if(adjustment < 0) {
//            adjustment = 0;
//        }
//        
//        group.setTranslateX(adjustment);  //Code to be removed once bugs fixed.
//       
//    }
