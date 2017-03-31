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
 * JavaFXSearchList.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.util.Collections;
import java.util.Map;

import javafx.collections.ObservableList;
import org.jpedal.examples.viewer.gui.generic.GUISearchList;


/**
 * used by search function ro provide page number as tooltip
 */
public class JavaFXSearchList extends javafx.scene.control.ListView<String> implements GUISearchList {

    private final Map<Integer, Integer> textPages;
    private final Map<Integer, Object> textAreas;

    int status = NO_RESULTS_FOUND;

    int index;

    private String searchTerm = "";

    /**
     * Constructor that will set up the search list and store highlight areas
     * internally so the search highlights can be manipulated externally.
     *
     * @param listModel :: List of teasers
     * @param textPages :: Map of key to page of result
     * @param textAreas :: Map of key to highlight area
     */
    public JavaFXSearchList(final ObservableList<String> listModel, final Map<Integer, Integer> textPages, final Map<Integer, Object> textAreas) {
        super(listModel);

        this.textPages = textPages;
        this.textAreas = textAreas;
    }

    /**
     * Get the Map holding page numbers for each search index in the search list
     *
     * @return Map with the search result index as integer keys and page numbers as integer values
     */
    @Override
    public Map<Integer, Integer> getTextPages() {
        return Collections.unmodifiableMap(textPages);
    }

    /**
     * Get the Map holding result areas for each search index in the search list
     *
     * @return Map with the search result index as integer keys and result page areas as int[] and int[][] values stored as Objects
     */
    @Override
    public Map textAreas() {
        return Collections.unmodifiableMap(textAreas);
    }

    /**
     * Find out the current amount of results found
     *
     * @return the amount of search results found
     */
    @Override
    public int getResultCount() {
        return textAreas.size();
    }

    /**
     * Not part of API - used internally
     * <p>
     * Store the value used to generate these results.
     *
     * @param term String value of the search term used
     */
    @Override
    public void setSearchTerm(final String term) {
        this.searchTerm = term;
    }

    /**
     * Get the search term used to generate these results
     *
     * @return String value of the search term used
     */
    @Override
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Get the currently selected index in this list
     *
     * @return int value representing the currently selected index
     */
    @Override
    public int getSelectedIndex() {
        return index;
    }

    /**
     * Set the selected index in this list
     *
     * @param index int value representing the currently selected index
     */
    @Override
    public void setSelectedIndex(final int index) {
        this.index = index;
        getSelectionModel().clearAndSelect(index);
    }

    /**
     * Get the current status of the results in this list.
     * The status is defined as one of the following.
     * GUISearchList.SEARCH_INCOMPLETE
     * GUISearchList.SEARCH_COMPLETE_SUCCESSFULLY
     * GUISearchList.NO_RESULTS_FOUND
     * GUISearchList.SEARCH_PRODUCED_ERROR
     *
     * @return int value representing the results status
     */
    public int getStatus() {
        return status;
    }

    /**
     * The the status of the search results in this list.
     *
     * @param status int value representing the search status
     */
    public void setStatus(final int status) {
        this.status = status;
    }

}
