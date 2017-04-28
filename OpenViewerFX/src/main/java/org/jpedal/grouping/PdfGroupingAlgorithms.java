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
 * PdfGroupingAlgorithms.java
 * ---------------
 */
package org.jpedal.grouping;

import java.awt.Rectangle;
import java.util.*;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfData;
import org.jpedal.utils.repositories.*;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 * Applies heuristics to unstructured PDF text to create content
 */
public class PdfGroupingAlgorithms {

    //marker char used in content (we bury location for each char so we can split)
    private static final String MARKER = PdfData.marker;
    public static final char MARKER2 = MARKER.charAt(0);

    public static boolean useUnrotatedCoords;

    //Value placed between result areas to show they are part of the same result
    private static final int linkedSearchAreas = -101;

    private final PdfSearchUtils searcher;
    private final PdfTextExtractionUtils extracter;

    /**
     * Create a new instance, passing in raw data
     *
     * @param pdf_data        PdfData from the pdf to search
     * @param isXMLExtraction Boolean flag to specify if output should be xml
     */
    public PdfGroupingAlgorithms(final PdfData pdf_data, final boolean isXMLExtraction) {
        searcher = new PdfSearchUtils(pdf_data);
        extracter = new PdfTextExtractionUtils(pdf_data, isXMLExtraction);
    }

    /**
     * sets if we include HTML in teasers
     * (do we want this is <b>word</b> or this is word as teaser)
     *
     * @param value true to use HTML in teasers, otherwise false
     */
    public void setIncludeHTML(final boolean value) {

        searcher.setIncludeHTML(value);

    }

    /**
     * method to show data without encoding
     */
    public static String removeHiddenMarkers(final String contents) {

        //trap null
        if (contents == null) {
            return null;
        }

        //run though the string extracting our markers

        //make sure has markers and ignore if not
        if (!contents.contains(MARKER)) {
            return contents;
        }

        //strip the markers
        final StringTokenizer tokens = new StringTokenizer(contents, MARKER, true);
        String temp_token;
        StringBuilder processed_data = new StringBuilder();

        //with a token to make sure cleanup works
        while (tokens.hasMoreTokens()) {

            //encoding in data
            temp_token = tokens.nextToken();

            //see if first marker
            if (temp_token.equals(MARKER)) {
                tokens.nextToken(); //point character starts
                tokens.nextToken(); //second marker
                tokens.nextToken(); //width
                tokens.nextToken(); //third marker

                //put back chars
                processed_data = processed_data.append(tokens.nextToken());
                //value
            } else {
                processed_data = processed_data.append(temp_token);
            }
        }
        return processed_data.toString();
    }

    /**
     * Calls various low level merging routines on merge -
     * <p>
     * isCSV sets if output is XHTML or CSV format -
     * <p>
     * XHTML also has options to include font tags (keepFontInfo),
     * preserve widths (keepWidthInfo), try to preserve alignment
     * (keepAlignmentInfo), and set a table border width (borderWidth)
     * - AddCustomTags should always be set to false
     *
     * @param x1                is the x coord of the top left corner
     * @param y1                is the y coord of the top left corner
     * @param x2                is the x coord of the bottom right corner
     * @param y2                is the y coord of the bottom right corner
     * @param pageNumber        is the page you wish to extract from
     * @param isCSV             is a boolean. If false the output is xhtml if true the text is out as CSV
     * @param keepFontInfo      if true and isCSV is false keeps font information in extrated text.
     * @param keepWidthInfo     if true and isCSV is false keeps width information in extrated text.
     * @param keepAlignmentInfo if true and isCSV is false keeps alignment information in extrated text.
     * @param borderWidth       is the width of the border for xhtml
     * @return Map containing text found in estimated table cells
     * @throws PdfException If the co-ordinates are not valid
     */
    @SuppressWarnings("UnusedParameters")
    public final Map<String, String> extractTextAsTable(
            final int x1,
            final int y1,
            final int x2,
            final int y2,
            final int pageNumber,
            final boolean isCSV,
            final boolean keepFontInfo,
            final boolean keepWidthInfo,
            final boolean keepAlignmentInfo,
            final int borderWidth)
            throws PdfException {

        return extracter.extractTextAsTable(x1, y1, x2, y2, pageNumber, isCSV, keepFontInfo, keepWidthInfo, keepAlignmentInfo, borderWidth);

    }

    /**
     * Algorithm to place data from within coordinates to a vector of word, word coords (x1,y1,x2,y2)
     *
     * @param x1             is the x coord of the top left corner
     * @param y1             is the y coord of the top left corner
     * @param x2             is the x coord of the bottom right corner
     * @param y2             is the y coord of the bottom right corner
     * @param page_number    is the page you wish to extract from
     * @param breakFragments will divide up text based on white space characters
     * @param punctuation    is a string containing all values that should be used to divide up words
     * @return Vector containing words found and words coordinates (word, x1,y1,x2,y2...)
     * @throws PdfException If the co-ordinates are not valid
     */
    @SuppressWarnings("UnusedParameters")
    public final List<String> extractTextAsWordlist(
            final int x1,
            final int y1,
            final int x2,
            final int y2,
            final int page_number,
            final boolean breakFragments,
            final String punctuation)
            throws PdfException {

        return extracter.extractTextAsWordlist(x1, y1, x2, y2, page_number, breakFragments, punctuation);
    }

    /**
     * Algorithm to place data from specified coordinates on a page into a String.
     *
     * @param x1                 is the x coord of the top left corner
     * @param y1                 is the y coord of the top left corner
     * @param x2                 is the x coord of the bottom right corner
     * @param y2                 is the y coord of the bottom right corner
     * @param page_number        is the page you wish to extract from
     * @param estimateParagraphs will attempt to find paragraphs and add new lines in output if true
     * @param breakFragments     will divide up text based on white space characters if true
     * @return Vector containing words found and words coordinates (word, x1,y1,x2,y2...)
     * @throws PdfException If the co-ordinates are not valid
     */
    @SuppressWarnings("UnusedParameters")
    public final String extractTextInRectangle(
            final int x1,
            final int y1,
            final int x2,
            final int y2,
            final int page_number,
            final boolean estimateParagraphs,
            final boolean breakFragments)
            throws PdfException {

        return extracter.extractTextInRectangle(x1, y1, x2, y2, page_number, estimateParagraphs, breakFragments);

    }

    //<link><a name="findMultipleTermsInRectangleWithMatchingTeasers" />

    /**
     * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>, with matching teaser.
     * The teaser is a section of text that start before the result and ends after, should a teaser not be discovered
     * it will instead be set to the search results text.
     *
     * @param x1         the left x cord
     * @param y1         the upper y cord
     * @param x2         the right x cord
     * @param y2         the lower y cord
     * @param rotation   the rotation of the page to be searched
     * @param terms      the terms to search for
     * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
     * @param listener   an implementation of SearchListener is required, this is to enable searching to be cancelled
     * @return a SortedMap containing a collection of Rectangle describing the location of found text, mapped to a String which is the matching teaser
     * @throws PdfException If the co-ordinates are not valid
     */
    public SortedMap findMultipleTermsInRectangleWithMatchingTeasers(final int x1, final int y1, final int x2, final int y2, final int rotation,
                                                                     final String[] terms, final int searchType, final SearchListener listener) throws PdfException {
        searcher.clearStoredTeasers();

        boolean origIncludeTease = searcher.isGeneratingTeasers();
        searcher.generateTeasers(true);

        final List highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, terms, searchType, listener);

        final SortedMap<Object, String> highlightsWithTeasers = new TreeMap<Object, String>(new PdfTextExtractionUtils.ResultsComparatorRectangle(rotation));

        final String[] teasers = searcher.getTeasers();
        for (int i = 0; i < highlights.size(); i++) {
            //highlights.get(i) is a rectangle or a rectangle[]
            highlightsWithTeasers.put(highlights.get(i), teasers[i]);
        }

        searcher.generateTeasers(origIncludeTease);

        return highlightsWithTeasers;
    }

    /**
     * Method to search a specified area on a specified page for a search term.
     * The returned map contains a set of coordinate for found values and a teaser.
     * The teaser is a section of text that start before the result and ends after,
     * should a teaser not be discovered it will instead be set to the search results text.
     *
     * @param x1         the left x cord
     * @param y1         the upper y cord
     * @param x2         the right x cord
     * @param y2         the lower y cord
     * @param rotation   the rotation of the page to be searched
     * @param terms      the terms to search for
     * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
     * @param listener   an implementation of SearchListener is required, this is to enable searching to be cancelled
     * @return a SortedMap containing an int[] of coordinates as the key and a String teaser as the value
     * @throws PdfException If the co-ordinates are not valid
     */
    public SortedMap findTextWithinInAreaWithTeasers(final int x1, final int y1, final int x2, final int y2, final int rotation,
                                                     final String[] terms, final int searchType, final SearchListener listener) throws PdfException {

        searcher.clearStoredTeasers();

        final boolean origIncludeTease = searcher.isGeneratingTeasers();
        searcher.generateTeasers(true);

        final List highlights = findTextWithinArea(x1, y1, x2, y2, terms, searchType, listener);

        final SortedMap<Object, String> highlightsWithTeasers = new TreeMap<Object, String>(new PdfTextExtractionUtils.ResultsComparator(rotation));

        final String[] teasers = searcher.getTeasers();
        for (int i = 0; i < highlights.size(); i++) {
            //highlights.get(i) is a rectangle or a rectangle[]
            highlightsWithTeasers.put(highlights.get(i), teasers[i]);
        }

        searcher.generateTeasers(origIncludeTease);

        return highlightsWithTeasers;
    }

    //<link><a name="findMultipleTermsInRectangle" />

    /**
     * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>.
     *
     * @param x1           the left x cord
     * @param y1           the upper y cord
     * @param x2           the right x cord
     * @param y2           the lower y cord
     * @param rotation     the rotation of the page to be searched
     * @param terms        the terms to search for
     * @param orderResults if true the list that is returned is ordered to return the resulting rectangles in a
     *                     logical order descending down the page, if false, rectangles for multiple terms are grouped together.
     * @param searchType   searchType the search type made up from one or more constants obtained from the SearchType class
     * @param listener     an implementation of SearchListener is required, this is to enable searching to be cancelled
     * @return a list of Rectangle describing the location of found text
     * @throws PdfException If the co-ordinates are not valid
     */
    public List findMultipleTermsInRectangle(final int x1, final int y1, final int x2, final int y2, final int rotation,
                                             final String[] terms, final boolean orderResults, final int searchType, final SearchListener listener) throws PdfException {

        searcher.clearStoredTeasers();

        final List<Object> highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, terms, searchType, listener);

        if (orderResults) {
            Collections.sort(highlights, new PdfTextExtractionUtils.ResultsComparator(rotation));
        }

        return highlights;
    }

    private List<Object> findMultipleTermsInRectangle(final int x1, final int y1, final int x2, final int y2, final String[] terms, final int searchType,
                                                      final SearchListener listener) throws PdfException {

        final List<Object> list = new ArrayList<Object>();

        for (final String term : terms) {
            if (listener != null && listener.isCanceled()) {
                break;
            }

            final float[] co_ords;

            co_ords = findText(x1, y1, x2, y2, new String[]{term}, searchType);

            if (co_ords != null) {
                final int count = co_ords.length;
                for (int ii = 0; ii < count; ii += 5) {

                    int wx1 = (int) co_ords[ii];
                    int wy1 = (int) co_ords[ii + 1];
                    int wx2 = (int) co_ords[ii + 2];
                    int wy2 = (int) co_ords[ii + 3];

                    Rectangle rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);

                    int seperator = (int) co_ords[ii + 4];

                    if (seperator == linkedSearchAreas) {
                        final Vector_Rectangle vr = new Vector_Rectangle();
                        vr.addElement(rectangle);
                        while (seperator == linkedSearchAreas) {
                            ii += 5;
                            wx1 = (int) co_ords[ii];
                            wy1 = (int) co_ords[ii + 1];
                            wx2 = (int) co_ords[ii + 2];
                            wy2 = (int) co_ords[ii + 3];
                            seperator = (int) co_ords[ii + 4];
                            rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);
                            vr.addElement(rectangle);
                        }
                        vr.trim();
                        list.add(vr.get());
                    } else {
                        list.add(rectangle);
                    }
                }
            }
        }
        return list;
    }

    private List findTextWithinArea(final int x1, final int y1, final int x2, final int y2, final String[] terms, final int searchType,
                                    final SearchListener listener) throws PdfException {

        final List<Object> list = new ArrayList<Object>();

        for (final String term : terms) {
            if (listener != null && listener.isCanceled()) {
                break;
            }

            final float[] co_ords;

            co_ords = findText(x1, y1, x2, y2, new String[]{term}, searchType);

            if (co_ords != null) {
                final int count = co_ords.length;
                for (int ii = 0; ii < count; ii += 5) {

                    int wx1 = (int) co_ords[ii];
                    int wy1 = (int) co_ords[ii + 1];
                    int wx2 = (int) co_ords[ii + 2];
                    int wy2 = (int) co_ords[ii + 3];

                    int[] rectangle = {wx1, wy2, wx2 - wx1, wy1 - wy2};

                    int seperator = (int) co_ords[ii + 4];

                    if (seperator == linkedSearchAreas) {
                        final Vector_Rectangle_Int vr = new Vector_Rectangle_Int();
                        vr.addElement(rectangle);
                        while (seperator == linkedSearchAreas) {
                            ii += 5;
                            wx1 = (int) co_ords[ii];
                            wy1 = (int) co_ords[ii + 1];
                            wx2 = (int) co_ords[ii + 2];
                            wy2 = (int) co_ords[ii + 3];
                            seperator = (int) co_ords[ii + 4];
                            rectangle = new int[]{wx1, wy2, wx2 - wx1, wy1 - wy2};
                            vr.addElement(rectangle);
                        }
                        vr.trim();
                        list.add(vr.get());
                    } else {
                        list.add(rectangle);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Search a particular area with in pdf page currently loaded and return the areas
     * of the results found as an array of float values.
     *
     * @param x1         is the x coord of the top left corner
     * @param y1         is the y coord of the top left corner
     * @param x2         is the x coord of the bottom right corner
     * @param y2         is the y coord of the bottom right corner
     * @param terms      : String[] of search terms, each String is treated as a single term
     * @param searchType : int containing bit flags for the search (See class SearchType)
     * @return the coords of the found text in a float[] where the coords are pdf page coords.
     * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
     * [0]=result x1 coord<br>
     * [1]=result y1 coord<br>
     * [2]=result x2 coord<br>
     * [3]=result y2 coord<br>
     * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
     * @throws PdfException when the search encounters incorrect values in the page content that it can not recover from
     */
    @SuppressWarnings("UnusedParameters")
    public final float[] findText(
            final int x1,
            final int y1,
            final int x2,
            final int y2,
            final String[] terms,
            final int searchType)
            throws PdfException {

        return searcher.findText(x1, y1, x2, y2, terms, searchType);

    }

    //<link><a name="findTextInRectangle" />

    /**
     * Search with in pdf page currently loaded and return the areas
     * of the results found as an array of float values.
     * <p>
     * Method to find text in the specified area allowing for the text to be split across multiple lines.<br>
     *
     * @param terms      = the text to search for
     * @param searchType = info on how to search the pdf
     * @return the coords of the found text in a float[] where the coords are pdf page coords.
     * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
     * [0]=result x1 coord<br>
     * [1]=result y1 coord<br>
     * [2]=result x2 coord<br>
     * [3]=result y2 coord<br>
     * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
     * @throws PdfException when the search encounters incorrect values in the page content that it can not recover from
     */
    public final float[] findText(
            final String[] terms,
            final int searchType)
            throws PdfException {

        return searcher.findText(terms, searchType);

    }

    /**
     * return text teasers from findtext if generateTeasers() called before find
     */
    public String[] getTeasers() {

        return searcher.getTeasers();

    }

    /**
     * tell find text to generate teasers as well
     */
    public void generateTeasers() {

        searcher.generateTeasers(true);

    }

}
