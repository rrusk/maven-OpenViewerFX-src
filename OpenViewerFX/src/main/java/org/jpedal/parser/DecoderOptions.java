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
 * DecoderOptions.java
 * ---------------
 */
package org.jpedal.parser;

import java.awt.Color;
import java.awt.Paint;
import java.util.Map;

import org.jpedal.constants.JPedalSettings;
import org.jpedal.display.Display;
import org.jpedal.display.PageOffsets;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalHelper;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.parser.text.Tj;
import org.jpedal.render.SwingDisplay;
import org.jpedal.utils.LogWriter;

public class DecoderOptions {

    /**
     * version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static float javaVersion;
    /**
     * The transparency of the highlighting box around the text stored as a
     * float
     */
    public static float highlightComposite = 0.35f;
    //Show onscreen mouse dragged box
    public static boolean showMouseBox;

    /**
     * flag to enable popup of error messages in JPedal
     */
    public static boolean showErrorMessages;

    /**
     * flag to show if on mac so we can code around certain bugs
     */
    public static boolean isRunningOnMac;
    public static boolean isRunningOnWindows;
    public static boolean isRunningOnAIX;
    public static boolean isRunningOnLinux;

    /**
     * flag to tell software to embed x point after each character so we can
     * merge any overlapping text together
     */
    public static boolean embedWidthData;

    //allow user to override code
    @SuppressWarnings("CanBeFinal")
    public static JPedalHelper Helper; //new org.jpedal.examples.ExampleHelper();

    /**
     * amount we scroll screen to make visible
     */
    public int scrollInterval = 10;

    /*
     * work out machine type so we can call OS X code to get around Java bugs.
     */
    static {

        try {
            final String name = System.getProperty("os.name");
            if (name.equals("Mac OS X")) {
                DecoderOptions.isRunningOnMac = true;
            } else if (name.startsWith("Windows")) {
                DecoderOptions.isRunningOnWindows = true;
            } else if (name.startsWith("AIX")) {
                DecoderOptions.isRunningOnAIX = true;
            } else {
                if (name.equals("Linux")) {
                    DecoderOptions.isRunningOnLinux = true;
                }
            }
        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
    }

    private int alignment = Display.DISPLAY_LEFT_ALIGNED;

    /**
     * allow for inset of display
     */
    public int insetW;
    public int insetH;

    private boolean useAcceleration = true;

    private PageOffsets currentOffset;

    /**
     * copy of flag to tell program whether to create
     * (and possibly update) screen display
     */
    private boolean renderPage;

    /**
     * Set default page Layout
     */
    private int pageMode = Display.SINGLE_PAGE;

    /**
     * The colour of the highlighting box around the text
     */
    public static Color highlightColor = new Color(10, 100, 170);

    /**
     * The colour of the text once highlighted
     */
    public static Color backgroundColor;

    Color nonDrawnPageColor = Color.WHITE;

    /**
     * display mode (continuous, facing, single)
     */
    private int displayView = Display.SINGLE_PAGE;

    /**
     * page colour for PDF background
     */
    public Color altPageColor = Color.WHITE;
    public Color altTextColor;
    public Color altDisplayBackground;
    public int altColorThreshold = 255;
    public boolean enhanceFractionalLines = true;
    boolean changeTextAndLine;

    //non-static version
    private Integer instance_bestQualityMaxScaling;

    private int[] instance_formsNoPrint;

    private static int[] formsNoPrint;

    //page size for extraction
    private static String[] extactionPageSize;
    //non-static version
    private String[] instance_extactionPageSize;

    //page size override
    private static Boolean overridePageSize;
    //non-static version
    private Boolean instance_overridePageSize;

    //non-static version
    private Boolean instance_allowPagesSmallerThanPageSize = Boolean.FALSE;

    /**
     * Flag to control if text extraction should extract as XML
     *
     * @return True if extraction should output XML, false otherwise
     */
    public boolean isXMLExtraction() {
        return isXMLExtraction;
    }

    /**
     * Set if future text extractions should output as XML or text
     *
     * @param XMLExtraction True if extraction should output XML, false otherwise
     */
    public void setXMLExtraction(final boolean XMLExtraction) {
        isXMLExtraction = XMLExtraction;
    }

    /**
     * flag to show if data extracted as text or XML
     */
    private boolean isXMLExtraction = true;

    /**
     * Set the page mode being used
     *
     * @param mode int value representing the current display view - Please
     *             see org.jpedal.display for display view flags
     */
    public void setPageMode(final int mode) {
        pageMode = mode;
    }

    /**
     * Get the current page mode being used
     *
     * @return int value representing the current display view - Please
     * see org.jpedal.display for display view flags
     */
    public int getPageMode() {
        return pageMode;
    }

    /**
     * Vertical inset applied to page to move away from the displays edge
     *
     * @return int value representing the vertical inset
     */
    public int getInsetH() {
        return insetH;
    }

    /**
     * Horizontal inset applied to page to move away from the displays edge
     *
     * @return int value representing the horizontal inset
     */
    public int getInsetW() {
        return insetW;
    }

    /**
     * The scroll interval to be used when scrolling in the viewer
     *
     * @return int value presenting the interval to move per scroll unit
     */
    public int getScrollInterval() {
        return scrollInterval;
    }

    /**
     * Set the scroll interval to be used when scrolling in the viewer
     *
     * @param scrollInterval int value presenting the interval to move per scroll unit
     */
    public void setScrollInterval(final int scrollInterval) {
        this.scrollInterval = scrollInterval;
    }

    /**
     * Set an inset so page does not touch the display area edge
     *
     * @param width  int value representing the horizontal inset
     * @param height int value representing the vertical inset
     */
    public final void setInset(final int width, final int height) {
        this.insetW = width;
        this.insetH = height;
    }

    /**
     * Set extraction mode to XML extraction - pure text extraction is much faster
     */
    @SuppressWarnings("UnusedDeclaration")
    public void useXMLExtraction() {
        isXMLExtraction = true;
    }

    /**
     * returns object containing grouped text of last decoded page
     * - if no page decoded, a Runtime exception is thrown to warn user
     * Please see org.jpedal.examples.text for example code.
     *
     * @param lastPageDecoded int value of the last page decoded
     * @param textData        PdfData object holding the text data from the file
     * @return PdfGroupingAlgorithms object build from the provided PdfData object
     * @throws org.jpedal.exception.PdfException if no page has been decoded
     */
    public PdfGroupingAlgorithms getGroupingObject(final int lastPageDecoded, final PdfData textData) throws PdfException {

        if (lastPageDecoded == -1) {

            throw new RuntimeException("No pages decoded - call decodePage(pageNumber) first");

        } else {

            //PUT BACK when we remove params
            //PdfData textData = getPdfData();
            if (textData == null) {
                return null;
            } else {
                return new PdfGroupingAlgorithms(textData, isXMLExtraction);
            }
        }
    }

    /**
     * returns object containing grouped text from background grouping - Please
     * see org.jpedal.examples.text for example code
     *
     * @param pdfBackgroundData
     */
    public PdfGroupingAlgorithms getBackgroundGroupingObject(final PdfData pdfBackgroundData) {

        if (pdfBackgroundData == null) {
            return null;
        } else {
            return new PdfGroupingAlgorithms(pdfBackgroundData, isXMLExtraction);
        }
    }

    /**
     * Set options to be used in page rendering, printing and image conversion
     *
     * @param values Map Object containing various parameters to modify
     * @throws PdfException if key or value data type is not recognised
     */
    public void set(final Map values) throws PdfException {
        //read values

        for (final Object nextKey : values.keySet()) {
            //check it is valid
            if (nextKey instanceof Integer) {

                final Integer key = (Integer) nextKey;
                final Object rawValue = values.get(key);

                if (key.equals(JPedalSettings.UNDRAWN_PAGE_COLOR)) {
                    if (rawValue instanceof Integer) {

                        nonDrawnPageColor = new Color((Integer) rawValue);

                    } else {
                        throw new PdfException("JPedalSettings.UNDRAWN_PAGE_COLOR expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.PAGE_COLOR)) {
                    if (rawValue instanceof Integer) {

                        altPageColor = new Color((Integer) rawValue);

                    } else {
                        throw new PdfException("JPedalSettings.PAGE_COLOR expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.TEXT_COLOR)) {
                    if (rawValue instanceof Integer) {

                        altTextColor = new Color((Integer) rawValue);

                    } else {
                        throw new PdfException("JPedalSettings.TEXT_COLOR expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.REPLACEMENT_COLOR_THRESHOLD)) {
                    if (rawValue instanceof Integer) {

                        altColorThreshold = ((Integer) rawValue);

                    } else {
                        throw new PdfException("JPedalSettings.TEXT_COLOR expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.DISPLAY_BACKGROUND)) {
                    if (rawValue instanceof Integer) {

                        altDisplayBackground = new Color((Integer) rawValue);

                    } else {
                        throw new PdfException("JPedalSettings.TEXT_COLOR expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.CHANGE_LINEART)) {
                    if (rawValue instanceof Boolean) {
                        changeTextAndLine = (Boolean) rawValue;

                    } else {
                        throw new PdfException("JPedalSettings.CHANGE_LINEART expects a Boolean value");
                    }

                } else if (key.equals(JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING)) {

                    if (rawValue instanceof Integer) {

                        instance_bestQualityMaxScaling = (Integer) rawValue;

                    } else {
                        throw new PdfException("JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING expects a Integer value");
                    }

                } else if (key.equals(JPedalSettings.EXTRACT_AT_PAGE_SIZE)) {

                    if (rawValue instanceof String[]) {

                        instance_extactionPageSize = (String[]) rawValue;

                    } else {
                        throw new PdfException("JPedalSettings.EXTRACT_AT_PAGE_SIZE expects a String[] value");
                    }

                } else if (key.equals(JPedalSettings.IGNORE_FORMS_ON_PRINT)) {

                    if (rawValue instanceof int[]) {

                        instance_formsNoPrint = (int[]) rawValue;

                    } else {
                        throw new PdfException("JPedalSettings.IGNORE_FORMS_ON_PRINT expects a int[] value");
                    }

                } else if (key.equals(JPedalSettings.PAGE_SIZE_OVERRIDES_IMAGE)) {

                    if (rawValue instanceof Boolean) {

                        instance_overridePageSize = (Boolean) rawValue;


                    } else {
                        throw new PdfException("JPedalSettings.EXTRACT_AT_PAGE_SIZE expects a Boolean value");
                    }

                } else if (key.equals(JPedalSettings.ALLOW_PAGES_SMALLER_THAN_PAGE_SIZE)) {

                    if (rawValue instanceof Boolean) {

                        instance_allowPagesSmallerThanPageSize = (Boolean) rawValue;


                    } else {
                        throw new PdfException("JPedalSettings.ALLOW_PAGES_SMALLER_THAN_PAGE_SIZE expects a Boolean value");
                    }
                } else if (key.equals(JPedalSettings.ENHANCE_FRACTIONAL_LINES)) {

                    if (rawValue instanceof Boolean) {

                        enhanceFractionalLines = (Boolean) rawValue;

                    } else {
                        throw new PdfException("JPedalSettings.ENHANCE_FRACTIONAL_LINES expects a Boolean value");
                    }
                    //expansion room here

                } else //all static values
                {
                    setParameter(values, nextKey);
                }

            } else {
                throw new PdfException("Unknown or unsupported key (not Integer) " + nextKey);
            }

        }
    }

    private static void setParameter(final Map values, final Object nextKey) throws PdfException {
        //check it is valid
        if (nextKey instanceof Integer) {

            final Integer key = (Integer) nextKey;
            final Object rawValue = values.get(key);

            if (key.equals(JPedalSettings.INVERT_HIGHLIGHT)) {
                //set mode if valid

                if (rawValue instanceof Boolean) {
                    SwingDisplay.invertHighlight = (Boolean) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.INVERT_HIGHLIGHT expects an Boolean value");
                }

            } else if (key.equals(JPedalSettings.TEXT_INVERTED_COLOUR)) {
                //set colour if valid

                if (rawValue instanceof Color) {
                    backgroundColor = (Color) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.TEXT_INVERTED_COLOUR expects a Color value");
                }

            } else if (key.equals(JPedalSettings.TEXT_HIGHLIGHT_COLOUR)) {
                //set colour if valid

                if (rawValue instanceof Color) {
                    highlightColor = (Color) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.TEXT_HIGHLIGHT_COLOUR expects a Color value");
                }

            } else if (key.equals(JPedalSettings.TEXT_PRINT_NON_EMBEDDED_FONTS)) {

                if (rawValue instanceof Boolean) {

                    PdfStreamDecoder.useTextPrintingForNonEmbeddedFonts = (Boolean) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.TEXT_PRINT_NON_EMBEDDED_FONTS expects a Boolean value");
                }

            } else if (key.equals(JPedalSettings.DISPLAY_INVISIBLE_TEXT)) {

                if (rawValue instanceof Boolean) {

                    Tj.showInvisibleText = (Boolean) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.DISPLAY_INVISIBLE_TEXT expects a Boolean value");
                }

            } else if (key.equals(JPedalSettings.CACHE_LARGE_FONTS)) {

                if (rawValue instanceof Integer) {

                    FontData.maxSizeAllowedInMemory = (Integer) rawValue;
                } else {
                    throw new PdfException("JPedalSettings.CACHE_LARGE_FONTS expects an Integer value");
                }

            } else if (key.equals(JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING)) {

                if (rawValue instanceof Integer) {

                    PDFtoImageConvertor.bestQualityMaxScaling = (Integer) rawValue;

                } else {
                    throw new PdfException("JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING expects a Integer value");
                }
                //expansion room here
            } else if (key.equals(JPedalSettings.EXTRACT_AT_PAGE_SIZE)) {

                if (rawValue instanceof String[]) {

                    extactionPageSize = (String[]) rawValue;

                } else {
                    throw new PdfException("JPedalSettings.EXTRACT_AT_PAGE_SIZE expects a String[] value");
                }
                //expansion room here

            } else if (key.equals(JPedalSettings.PAGE_SIZE_OVERRIDES_IMAGE)) {

                if (rawValue instanceof Boolean) {

                    overridePageSize = (Boolean) rawValue;


                } else {
                    throw new PdfException("JPedalSettings.EXTRACT_AT_PAGE_SIZE expects a Boolean value");
                }
                //expansion room here

            } else if (key.equals(JPedalSettings.IGNORE_FORMS_ON_PRINT)) {

                if (rawValue instanceof int[]) {

                    formsNoPrint = (int[]) rawValue;

                } else {
                    throw new PdfException("JPedalSettings.IGNORE_FORMS_ON_PRINT expects a int[] value");
                }

            } else if (key.equals(JPedalSettings.ALLOW_PAGES_SMALLER_THAN_PAGE_SIZE)) {

                if (rawValue instanceof Boolean) {

                    PDFtoImageConvertor.allowPagesSmallerThanPageSize = (Boolean) rawValue;

                } else {
                    throw new PdfException("JPedalSettings.ALLOW_PAGES_SMALLER_THAN_PAGE_SIZE expects a Boolean value");
                }


            } else {
                //  throw new PdfException("Unknown or unsupported key " + key);
            }

        } else {
            throw new PdfException("Unknown or unsupported key (not Integer) " + nextKey);
        }
    }

    /**
     * Allow the modification of various parameters to customise different aspects of rendering and / or extraction
     *
     * @param values Map Object containing various parameters to modify
     * @throws PdfException if key or value data type is not recognised
     */
    public static void modifyJPedalParameters(final Map values) throws PdfException {

        //read values

        for (final Object nextKey : values.keySet()) {
            setParameter(values, nextKey);
        }
    }

    /**
     * Get the alternative page colour to be used when the option is set.
     *
     * @return Color object representing the alternative page colour
     */
    public Color getPageColor() {
        return altPageColor;
    }

    /**
     * Get the alternative text colour to be used when the option is set.
     *
     * @return Color object representing the alternative text colour
     */
    public Color getTextColor() {
        return altTextColor;
    }

    /**
     * Set the threshold to be used when replacing text and line colours with alternative.
     * This value is designed to allow you to replace only black colours but a
     * problem arises with slightly off black colours. In some cases this can
     * result in black text changing but other 1 value of black colours not changing.
     * <p>
     * The threshold sets a value all RGB components must be under in order to be replaced.
     * 0 would only covert pure black, 255 would convert all colours.
     *
     * @return int value representing the threshold to use when replacing colours
     */
    public int getReplacementColorThreshold() {
        return altColorThreshold;
    }

    /**
     * Flag used to control if we enhance thin lines (width of less than 1)
     * to ensure its width never drops below 1.
     *
     * @return True if we enhance thin lines, false otherwise
     */
    public boolean isEnhanceFractionalLines() {
        return enhanceFractionalLines;
    }

    /**
     * Gets the alternative colour for the display areas background when the
     * option is set.
     *
     * @return Color object representing the colour to be used.
     */
    public Color getDisplayBackgroundColor() {
        return altDisplayBackground;
    }

    /**
     * Flag that shows if shapes should have colours changed when preferences
     * set to change document text colours
     *
     * @return true if shape colours are changes, false otherwise
     */
    public boolean getChangeTextAndLine() {
        return changeTextAndLine;
    }

    /**
     * Gets the Paint to be used when drawing a page that has not been drawn on yet
     *
     * @return Paint object to be used when displaying pages before rendering
     */
    public Paint getNonDrawnPageColor() {
        return nonDrawnPageColor;
    }

    /**
     * Flag to control if image conversion can create pages smaller than the original
     *
     * @return True if scaling can be less than 100%, false otherwise
     */
    public Boolean getInstance_allowPagesSmallerThanPageSize() {
        return instance_allowPagesSmallerThanPageSize;
    }

    /**
     * Flag to control the maximum value the scaling for image conversion can be
     *
     * @return Integer object representing the largest scaling factor to be
     * used in image conversion
     */
    public Integer getInstance_bestQualityMaxScaling() {
        return instance_bestQualityMaxScaling;
    }

    /**
     * Get a list of Form types and subtypes that should not be printed
     *
     * @return int array containing type values of forms to ignore
     */
    public static int[] getFormsNoPrint() {
        return formsNoPrint;
    }

    /**
     * Get a list of Form types and subtypes that should not be printed
     *
     * @return int array containing type values of forms to ignore
     */
    public int[] getInstance_FormsNoPrint() {
        return instance_formsNoPrint;
    }

    /**
     * Flag to check if we should override page size during image conversion
     *
     * @return True is page size can be scaled, flase otherwise
     */
    public Boolean getPageSizeToUse() {
        Boolean overridePageSizeToUse = Boolean.FALSE;
        if (instance_overridePageSize != null) {
            overridePageSizeToUse = instance_overridePageSize;
        } else if (overridePageSize != null) {
            overridePageSizeToUse = overridePageSize;
        }

        return overridePageSizeToUse;

    }

    /**
     * Get the scaling values to be used when converting a page to an image
     *
     * @param pageIndex int value of a page
     * @param pageData  PdfPageData object holding page size data for the document
     * @return float value of the multiplier to be used when converting to an image
     */
    public float getImageDimensions(final int pageIndex, final PdfPageData pageData) {

        float multiplyer = -2;

        final String overridePageSizeJVM = System.getProperty("org.jpedal.pageSizeOverridesImage");
        if (overridePageSizeJVM != null) {
            if (instance_overridePageSize != null) {
                instance_overridePageSize = Boolean.parseBoolean(overridePageSizeJVM);
            } else {
                overridePageSize = Boolean.parseBoolean(overridePageSizeJVM);
            }
        }

        final String maxScalingJVM = System.getProperty("org.jpedal.pageMaxScaling");
        if (maxScalingJVM != null) {
            try {
                if (instance_bestQualityMaxScaling != null) {
                    instance_bestQualityMaxScaling = Integer.parseInt(maxScalingJVM);
                } else {
                    PDFtoImageConvertor.bestQualityMaxScaling = Integer.parseInt(maxScalingJVM);
                }

            } catch (final Exception e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }

        String[] dims = null;
        final String dimsJVM = System.getProperty("org.jpedal.pageSize");
        if (dimsJVM != null) {
            dims = dimsJVM.split("x");
        }

        if (dims == null) {

            if (instance_extactionPageSize != null) {
                dims = instance_extactionPageSize;
            } else {
                dims = extactionPageSize;
            }
        }

        // prefered size of the extracted page
        float prefWidth = 0, prefHeight = 0;

        // parse values as ints, if any issues let know that prarameters are invalid
        if (dims != null) {
            if (dims.length == 2) {

                if (pageData.getRotation(pageIndex) == 90 || pageData.getRotation(pageIndex) == 270) {
                    prefWidth = Float.parseFloat(dims[1]);
                    prefHeight = Float.parseFloat(dims[0]);
                } else {
                    prefWidth = Float.parseFloat(dims[0]);
                    prefHeight = Float.parseFloat(dims[1]);
                }
            } else {
                throw new RuntimeException("Invalid parameters in JVM option -DpageSize ");
            }
        }

        float dScaleW = 0;
        final float dScaleH;

        if (dims != null) {

            //Work out scalings for -DpageSize
            final float crw = pageData.getCropBoxWidth2D(pageIndex);
            final float crh = pageData.getCropBoxHeight2D(pageIndex);

            dScaleW = prefWidth / crw;
            dScaleH = prefHeight / crh;

            if (dScaleH < dScaleW) {
                dScaleW = dScaleH;
            }
        }

        final Boolean overridePageSizeToUse = getPageSizeToUse();

        if (dims != null && overridePageSizeToUse) {

            multiplyer = dScaleW;

        }

        return multiplyer;
    }

    /**
     * Flag to show if pages are being rendered
     *
     * @return flag to show if pages are being rendered (true) or only extraction taking place (false).
     */
    public boolean getRenderPage() {
        return renderPage;
    }

    /**
     * Controls if pages are being rendered or not
     *
     * @param newRender flag to show if pages are being rendered (true) or only extraction taking place (false).
     */
    public void setRenderPage(final boolean newRender) {
        renderPage = newRender;
    }

    /**
     * Flag to specify if the viewer should use hardware acceleration when rendering pages
     *
     * @return True if acceleration should be used, false otherwise
     */
    public boolean useHardwareAcceleration() {
        return useAcceleration;
    }

    /**
     * Specify if the viewer should use hardware acceleration when rendering pages
     *
     * @param newValue True if acceleration should be used, false otherwise
     */
    public void useHardwareAcceleration(final boolean newValue) {
        useAcceleration = newValue;
    }

    /**
     * Get the alignment of the pages such as left aligned or centred - Please
     * see org.jpedal.display for display view flags
     *
     * @return int value representing the page alignment
     */
    public int getPageAlignment() {
        return alignment;
    }

    /**
     * Set the alignment of the pages such as left aligned or centred - Please
     * see org.jpedal.display for display view flags
     *
     * @param orientation int value to use for page alignment
     */
    public void setPageAlignment(final int orientation) {
        alignment = orientation;
    }

    /**
     * Set the display view being used in the display
     *
     * @param displayView int value for the display view - Please
     *                    see org.jpedal.display for display view flags
     */
    public void setDisplayView(final int displayView) {
        this.displayView = displayView;
    }

    /**
     * Get the current display view being used
     *
     * @return int value representing the current display view - Please
     * see org.jpedal.display for display view flags
     */
    public int getDisplayView() {
        return displayView;
    }

    /**
     * Get the page offsets and sizes for the whole document for
     * various display modes
     *
     * @return PageOffsets object containing offset information
     */
    public PageOffsets getCurrentOffsets() {
        return currentOffset;
    }

    /**
     * Set the offsets for the current document
     *
     * @param newOffset PageOffsets object containing offset information to be
     *                  used for the document
     */
    public void setCurrentOffsets(final PageOffsets newOffset) {
        currentOffset = newOffset;
    }
}
