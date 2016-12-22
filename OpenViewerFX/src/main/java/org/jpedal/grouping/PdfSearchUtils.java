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
 * PdfSearchUtils.java
 * ---------------
 */
package org.jpedal.grouping;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jpedal.exception.PdfException;
import static org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers;
import org.jpedal.objects.PdfData;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_String;

public class PdfSearchUtils {
    
    private static final String MARKER = PdfData.marker;
	private static final char MARKER2= MARKER.charAt(0);
    
    //teasers for findtext
    private boolean usingMultipleTerms;
            
	private String[] teasers;

	//private final List<String> multipleTermTeasers = new ArrayList<String>();

    //Hold data from pdf so we can create local version
	private final PdfData pdf_data;

    private Line[] fragments;
    private Line[] lines;
    
	//Value placed between result areas to show they are part of the same result
	private static final int linkedSearchAreas=-101;
	
    protected PdfSearchUtils(final PdfData pdf_data) {
		this.pdf_data = pdf_data;
    }
    
    
	/**
	 * Search a particular area with in pdf page currently loaded and return the areas
     * of the results found as an array of float values.
     * 
	 * @param x1 is the x coord of the top left corner
	 * @param y1 is the y coord of the top left corner
	 * @param x2 is the x coord of the bottom right corner
	 * @param y2 is the y coord of the bottom right corner
	 * @param terms : String[] of search terms, each String is treated as a single term
	 * @param searchType : int containing bit flags for the search (See class SearchType)
	 * @return the coords of the found text in a float[] where the coords are pdf page coords.
	 * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
	 * [0]=result x1 coord<br>
	 * [1]=result y1 coord<br>
	 * [2]=result x2 coord<br>
	 * [3]=result y2 coord<br>
	 * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
	 * @throws PdfException
	 */
    @SuppressWarnings("UnusedParameters")
    protected final float[] findText(
    		int x1,
    		int y1,
    		int x2,
    		int y2,
			final String[] terms,
			final int searchType)
	throws PdfException {

		//Failed to supply search terms to do nothing
		if (terms == null) {
            return new float[]{};
        }
		
		//Search result and teaser holders
		final Vector_Float resultCoords = new Vector_Float(0);
		final Vector_String resultTeasers = new Vector_String(0);
		
		//make sure co-ords valid and throw exception if not
		final int[] v = validateCoordinates(x1, y1, x2, y2);
		x1 = v[0];
		y1 = v[1];
		x2 = v[2];
		y2 = v[3];
		
		//Extract the text data into local arrays for searching
		copyToArraysPartial(x1, y2, x2, y1);
		
		//Remove any hidden text on page as should not be found
		cleanupShadowsAndDrownedObjects(false);

		//Get unused text objects and sort them for correct searching
        Line[] localLines = fragments.clone();
        
		final int[] unsorted = getWritingModeCounts(localLines);
		final int[] writingModes = getWritingModeOrder(unsorted);

		for(int u=0; u!=writingModes.length; u++){

			final int mode = writingModes[u];

			//if not lines for writing mode, ignore
			if(unsorted[mode]!=0){
                searchWritingMode(mode, searchType, terms, true, false, resultCoords, resultTeasers);
            }
            
		}
		//Return coord data for search results
		return resultCoords.get();
		 
	}

    /**
     * return text teasers from findtext if generateTeasers() called before find
	 */
	protected String[] getTeasers() {
		
		return teasers;
	}
    
	/**
	 * put raw data into Arrays for quick merging breakup_fragments shows if we
	 * break on vertical lines and spaces
	 */
    private void copyToArraysPartial(final int minX, final int minY, final int maxX, final int maxY) {
        
		final int count = pdf_data.getRawTextElementCount();

        Line[] localFragments = new Line[count];
        
        float x1,x2,y1,y2;

		int currentPoint = 0;
		
		//set values
		for (int i = 0; i < count; i++) {
            
			//extract values
			x1 = pdf_data.f_x1[i];
			x2 = pdf_data.f_x2[i];
			y1 = pdf_data.f_y1[i];
			y2 = pdf_data.f_y2[i];
			final int mode=pdf_data.f_writingMode[i];

			boolean accepted = false;
			float height;
            
            switch (mode) {
                case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
                case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                    height = y1-y2;
                    if ((((minX < x1 && x1 < maxX) || (minX < x2 && x2 < maxX)) || //Area contains the x1 or x2 coords
                            ((x1 < minX && minX < x2) || (x1 < maxX && maxX < x2)) //Area is within the x1 and x2 coords
                            )
                            && (minY < y2 + (height / 4) && y2 + (height * 0.75) < maxY) //Area also contains atleast 3/4 of the text y coords
                            ) {
                        accepted = true;
                    }
                    break;
                case PdfData.VERTICAL_BOTTOM_TO_TOP:
                case PdfData.VERTICAL_TOP_TO_BOTTOM:
                    height = x2-x1;
                    if ((((minY < y1 && y1 < maxY) || (minY < y2 && y2 < maxY)) || //Area contains the x1 or x2 coords
                            ((y2 < minY && minY < y1) || (y2 < maxY && maxY < y1)) //Area is within the x1 and x2 coords
                            )
                            && (minX < x1 + (height / 4) && x1 + (height * 0.75) < maxX) //Area also contains atleast 3/4 of the text y coords
                            ) {
                        accepted = true;
                    }
                    break;
            }
            
			//if at least partly in the area, process
			if(accepted){
                
                localFragments[currentPoint] = new Line(pdf_data, i);
                
				StringBuilder startTags = new StringBuilder(localFragments[currentPoint].getRawData().substring(0, localFragments[currentPoint].getRawData().indexOf(MARKER)));
				final String contentText = localFragments[currentPoint].getRawData().substring(localFragments[currentPoint].getRawData().indexOf(MARKER), localFragments[currentPoint].getRawData().indexOf('<', localFragments[currentPoint].getRawData().lastIndexOf(MARKER)));
				String endTags = localFragments[currentPoint].getRawData().substring(localFragments[currentPoint].getRawData().lastIndexOf(MARKER));
				//Skips last section of text
				endTags = endTags.substring(endTags.indexOf('<'));
				
				final StringTokenizer tokenizer = new StringTokenizer(contentText, MARKER);
				boolean setX1 = true;
				float width = 0;
				
				while(tokenizer.hasMoreTokens()){
					
					String token = tokenizer.nextToken();
					final float xCoord = (Float.parseFloat(token));
					
					token = tokenizer.nextToken();
					width = Float.parseFloat(token);
					
					token = tokenizer.nextToken();
					final String character = token;
					
					if(setX1){
						if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
							localFragments[currentPoint].setX1(xCoord);
						}else{
							localFragments[currentPoint].setY2(xCoord);
						}
						setX1 = false;
					}
					
					if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
						localFragments[currentPoint].setX2(xCoord);
                    }else{
                        localFragments[currentPoint].setY1(xCoord);
                    }
					
                    boolean storeValues = false;
					if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
                        if(minX<xCoord && (xCoord+width)<maxX){
                            storeValues = true;
                        }
                    }else{
                        if(minY<xCoord && (xCoord+width)<maxY){
                            storeValues = true;
                        }
                    }
					if(storeValues){
						startTags.append(MARKER);
						startTags.append(xCoord); //Add X Coord
						
						startTags.append(MARKER);
						startTags.append(width); //Add Width
						
						startTags.append(MARKER);
						startTags.append(character); //Add Letter
						
						
					}
					
				}
				
                localFragments[currentPoint].setRawData(startTags.append(endTags).toString());
              
				if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
                    localFragments[currentPoint].setX2(localFragments[currentPoint].getX2()+width);
				}else{
					localFragments[currentPoint].setY1(localFragments[currentPoint].getY1()+width);
				}
				
				currentPoint++;
			}
		}
		
        fragments = new Line[currentPoint];
		
		for(int i=0; i!=currentPoint; i++){
            fragments[i] = localFragments[i];
		}
	}
	
    
	/** make sure co-ords valid and throw exception if not */
	private static int[] validateCoordinates(int x1, int y1, int x2, int y2) {
		if ((x1 > x2) | (y1 < y2)) {
			if (x1 > x2){
				final int temp = x1;
				x1 = x2;
				x2 = temp;
				LogWriter.writeLog("x1 > x2, coordinates were swapped to validate");
			}
			
			if (y1 < y2){
				final int temp = y1;
				y1 = y2;
				y2 = temp;
				LogWriter.writeLog("y1 < y2, coordinates were swapped to validate");
			}
		}
		return new int[]{x1,y1,x2,y2};
	}

    //<link><a name="findTextInRectangle" />
	/**
	 * Search with in pdf page currently loaded and return the areas
     * of the results found as an array of float values.
     * 
     * Method to find text in the specified area allowing for the text to be split across multiple lines.<br>
	 * @param terms = the text to search for
	 * @param searchType = info on how to search the pdf
	 * @return the coords of the found text in a float[] where the coords are pdf page coords.
	 * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
	 * [0]=result x1 coord<br>
	 * [1]=result y1 coord<br>
	 * [2]=result x2 coord<br>
	 * [3]=result y2 coord<br>
	 * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
	 * @throws PdfException
	 */
    protected final float[] findText(
			final String[] terms,
			final int searchType)
	throws PdfException {

		//Failed to supply search terms to do nothing
		if (terms == null) {
            return new float[]{};
        }
        
		//Search result and teaser holders
		final Vector_Float resultCoords = new Vector_Float(0);
		final Vector_String resultTeasers = new Vector_String(0);

		//Extract the text data into local arrays for searching
		copyToArrays();

		//Remove any hidden text on page as should not be found
		cleanupShadowsAndDrownedObjects(false);

		//Get unused text objects and sort them for correct searching
//		final int[] items = getsortedUnusedFragments(true, false);
        Line[] localLines = fragments.clone();

		final int[] unsorted = getWritingModeCounts(localLines);
		final int[] writingModes = getWritingModeOrder(unsorted);

		for(int u=0; u!=writingModes.length; u++){

			final int mode = writingModes[u];

			if(unsorted[mode]!=0){
                searchWritingMode(mode, searchType, terms, true, false, resultCoords, resultTeasers);
            }
		}
		//Return coord data for search results
		return resultCoords.get();
		 
	}
    
    
    private void searchWritingMode(int mode, int searchType, String[] terms, boolean includeTease, boolean includeHTMLtags, Vector_Float resultCoords, Vector_String resultTeasers) throws PdfException {

        //Flags to control the different search options
        boolean firstOccuranceOnly = false;
        boolean wholeWordsOnly = false;
        boolean foundFirst = false;
        boolean useRegEx = false;

        //Merge text localFragments into lines as displayed on page
        createLinesForSearch(mode, false, false, true);
        
        //Bitwise flags for regular expressions engine, options always required 
        int options = loadSearcherOptions(searchType);

        //Only find first occurance of each search term
        if ((searchType & SearchType.FIND_FIRST_OCCURANCE_ONLY) == SearchType.FIND_FIRST_OCCURANCE_ONLY) {
            firstOccuranceOnly = true;
        }

        //Only find whole words, not partial words
        if ((searchType & SearchType.WHOLE_WORDS_ONLY) == SearchType.WHOLE_WORDS_ONLY) {
            wholeWordsOnly = true;
        }

        //Allow the use of regular expressions symbols
        if ((searchType & SearchType.USE_REGULAR_EXPRESSIONS) == SearchType.USE_REGULAR_EXPRESSIONS) {
            useRegEx = true;
        }
        
        //Check if coords need swapping
        boolean valuesSwapped = (mode == PdfData.VERTICAL_BOTTOM_TO_TOP || mode == PdfData.VERTICAL_TOP_TO_BOTTOM);
        
//        for(int i=0; i!=lines.length; i++){
//            System.out.println(i+" >> "+Strip.stripXML(removeHiddenMarkers(removeDuplicateSpaces(lines[i].getRawData())), true).toString());
//        }
        //Portions of text to perform the search on and find teasers
        String searchText = buildSearchText(false, mode);
        String coordsText = buildSearchText(true, mode);
//        System.out.println("searchText : "+searchText);
        //Hold starting point data at page rotation
        int[] resultStart;

        //Work through the search terms one at a time
        for (int j = 0; j != terms.length; j++) {

            String searchValue = alterStringTooDisplayOrder(terms[j]);
            
            //Set the default separator between words in a search term
            String sep = " ";

            //Multiline needs space or newline to be recognised as word separators
            if ((searchType & SearchType.MUTLI_LINE_RESULTS) == SearchType.MUTLI_LINE_RESULTS) {
                sep = "[ \\\\n]+";
            }

            //if not using reg ex add reg ex literal flags around the text and word separators
            if (!useRegEx) {
                searchValue = "\\Q" + searchValue + "\\E";
                sep = "\\\\E" + sep + "\\\\Q";
            }
            
            //If word seperator has changed, replace all spaces with modified seperator
            if (!sep.equals(" ")) {
                searchValue = searchValue.replaceAll(" ", sep);
            }

            //Surround search term with word boundry tags to match whole words
            if (wholeWordsOnly) {
                searchValue = "\\b" + searchValue + "\\b";
            }
//            System.out.println("searchValue : "+searchValue);
            //Create pattern to match search term
            final Pattern searchTerm = Pattern.compile(searchValue, options);

            //Create pattern to match search term with two words before and after
            final Pattern teaserTerm = Pattern.compile("(?:\\S+\\s)?\\S*(?:\\S+\\s)?\\S*" + searchValue + "\\S*(?:\\s\\S+)?\\S*(?:\\s\\S+)?", options);
            
            //So long as text data is not null
            if (searchText != null) {

                //Create two matchers for finding search term and teaser
                final Matcher termFinder = searchTerm.matcher(searchText);
                final Matcher teaserFinder = teaserTerm.matcher(searchText);
                boolean needToFindTeaser = true;

                //Keep looping till no result is returned
                while (termFinder.find()) {
                    resultStart = null;
                    //Make note of the text found and index in the text
                    String foundTerm = termFinder.group();
                    int termStarts = termFinder.start();
                    final int termEnds = termFinder.end() - 1;

                    //If storing teasers
                    if (includeTease) {

                        if (includeHTMLtags) {
                            foundTerm = "<b>" + foundTerm + "</b>";
                        }
                        
                        if (needToFindTeaser) {
                            findTeaser(foundTerm, teaserFinder, termStarts, termEnds, includeHTMLtags, resultTeasers);
                        }
                    }

                    getResultCoords(coordsText, mode, resultStart, termStarts, termEnds, valuesSwapped, resultCoords);

                    //If only finding first occurance,
                    //Stop searching this text data for search term.
                    if (firstOccuranceOnly) {
                        foundFirst = true;
                        break;
                    }
                }

				//If only finding first occurance and first is found,
                //Stop searching all text data for this search term.
                if (firstOccuranceOnly && foundFirst) {
                    break;
                }
            }
        }

        //Remove any trailing empty values
        resultCoords.trim();
        
        //If including tease values
        if (includeTease) {
            storeTeasers(resultTeasers);
        }
        
    }
    
    
    private void getResultCoords(String coordText, int mode, int[] resultStart, int termStarts, int termEnds, boolean valuesSwapped, Vector_Float resultCoords){
        
        //Get coords of found text for highlights
        float currentX;
        float width;

        //Track point in text data line (without coord data)
        int pointInLine = -1;

        //Track line on page
        int lineCounter = 0;

        //Skip null values and value not in the correct writing mode to ensure correct result coords
        while (lines[lineCounter].getRawData() == null || mode != lines[lineCounter].getWritingMode()) {
            lineCounter++;
        }

        //Flags used to catch if result is split accross lines
        boolean startFound = false;
        boolean endFound = false;

		//Cycle through coord text looking for coords of this result
        //Ignore first value as it is known to be the first marker
        for (int pointer = 1; pointer < coordText.length(); pointer++) {

            // find second marker and get x coord
            int startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }
                pointer++;
            }

            //Convert text to float value for x coord
            currentX = Float.parseFloat(coordText.substring(startPointer, pointer));
            pointer++;

            // find third marker and get width
            startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }

                pointer++;
            }

            //Convert text to float value for character width
            width = Float.parseFloat(coordText.substring(startPointer, pointer));
            pointer++;

            // find fourth marker and get text (character)
            startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }

                pointer++;
            }

            //Store text to check for newline character later
            final String text = coordText.substring(startPointer, pointer);
            pointInLine += text.length();

			//Start of term not found yet.
            //Point in line is equal to or greater than start of the term.
            //Store coords and mark start as found.
            if (!startFound && pointInLine >= termStarts) {
                int currentY = (int) lines[lineCounter].getY1();
                if(valuesSwapped){
                    currentY = (int) lines[lineCounter].getX2();
                }
                resultStart = new int[]{(int) currentX, currentY};
                startFound = true;
            }
            
            //End of term not found yet.
            //Point in line is equal to or greater than end of the term.
            //Store coords and mark end as found.
            if (!endFound && pointInLine >= termEnds) {
                int currentY = (int) lines[lineCounter].getY2();
                if(valuesSwapped){
                    currentY = (int) lines[lineCounter].getX1();
                }
                storeResultsCoords(valuesSwapped, mode, resultCoords, resultStart[0], resultStart[1], (currentX + width), currentY, 0.0f);
                
                endFound = true;
            }

			//Using multi line option.
            //Start of term found.
            //End of term not found.
            //New line character found.
            //Set up multi line result.
            if (startFound && !endFound && text.contains("\n")) {

                storeResultsCoords(valuesSwapped, mode, resultCoords, resultStart[0], resultStart[1], (currentX + width), lines[lineCounter].getY2(), linkedSearchAreas);

                //Set start of term as not found
                startFound = false;

				//Set this point in line as start of next term
                //Guarantees next character is found as 
                //start of the next part of the search term
                termStarts = pointInLine;
            }

			//In multiline mode we progress the line number when we find a \n
            //This is to allow the correct calculation of y coords
            if (text.contains("\n")) {
                lineCounter++;

                //If current content pointed at is null or not the correct writing mode, skip value until data is found
                while (lineCounter < lines.length && (lines[lineCounter].getRawData() == null || mode != lines[lineCounter].getWritingMode())) {
                    lineCounter++;
                }
            }

        }
    }
    
    private void storeTeasers(Vector_String resultTeasers){
        
        //Remove any trailing empty values
        resultTeasers.trim();

        //Store teasers so they can be retrieved by different search methods
        if (usingMultipleTerms) {
			//Store all teasers for so they may be returned as a sorted map
            //Only used for one method controled by the above flag
//            for (int i = 0; i != resultTeasers.size(); i++) {
//                multipleTermTeasers.add(resultTeasers.elementAt(i));
//            }
            //Prevent issue this not getting cleared between writing modes 
            //resulting in duplicate teasers
            resultTeasers.clear();
        } else {
            //Store all teasers to be retrieved by getTeaser() method
            teasers = resultTeasers.get();
        }
    }
    
    
    private static void storeResultsCoords(boolean valuesSwapped, int mode, Vector_Float resultCoords, float x1, float y1, float x2, float y2, float connected){
//        System.out.println(x1+" , "+y1+" , "+x2+" , "+y2);
        //Set ends coords      
        if (valuesSwapped) {
            if (mode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
                resultCoords.addElement(y2);
                resultCoords.addElement(x2);
                resultCoords.addElement(y1);
                resultCoords.addElement(x1);
                resultCoords.addElement(connected); //Mark next result as linked

            } else {
                resultCoords.addElement(y2);
                resultCoords.addElement(x1);
                resultCoords.addElement(y1);
                resultCoords.addElement(x2);
                resultCoords.addElement(connected); //Mark next result as linked

            }
        } else {
            resultCoords.addElement(x1);
            resultCoords.addElement(y1);
            resultCoords.addElement(x2);
            resultCoords.addElement(y2);
            resultCoords.addElement(connected); //Mark next result as linked
        }
    }
    
    
    private void findTeaser(String teaser, Matcher teaserFinder, int termStarts, int termEnds, boolean includeHTMLtags, Vector_String resultTeasers){
        
        if (teaserFinder.find()) {
            //Get a teaser if found and set the search term to bold is allowed
            if (teaserFinder.start() < termStarts && teaserFinder.end() > termEnds) {

                //replace default with found teaser
                teaser = teaserFinder.group();
                
                if (includeHTMLtags) {
                    //Calculate points to add bold tags
                    final int teaseStarts = termStarts - teaserFinder.start();
                    final int teaseEnds = (termEnds - teaserFinder.start()) + 1;

                    //Add bold tags
                    teaser = teaser.substring(0, teaseStarts) + "<b>"
                            + teaser.substring(teaseStarts, teaseEnds) + "</b>"
                            + teaser.substring(teaseEnds, teaser.length());
                }
                
                teaserFinder.region(termEnds+1, teaserFinder.regionEnd());
            }
        }
//        System.out.println("teaser : "+teaser);
        //Store teaser
        resultTeasers.addElement(teaser);
    }
    
    
    private static String alterStringTooDisplayOrder(String testTerm) {

        String currentBlock = "";
        String searchValue = "";
        byte lastDirection = Character.getDirectionality(testTerm.charAt(0));
        for (int i = 0; i != testTerm.length(); i++) {
            byte dir = Character.getDirectionality(testTerm.charAt(i));
            
            //Only track is changing from left to right or right to left
            switch(dir){
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE : 
                    dir = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
                    break;
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT : 
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING : 
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE : 
                    dir = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
                    break;
                default:
                    dir = lastDirection;
                    break;
            }
            
            
            if (dir != lastDirection) { //Save and reset block is direction changed
                searchValue += currentBlock;
                currentBlock = "";
                lastDirection = dir;
            }
            
            //Store value based on writing mode
            if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                currentBlock = testTerm.charAt(i) + currentBlock;
            } else {
                currentBlock += testTerm.charAt(i);
            }
        }
        searchValue += currentBlock;
        
        return searchValue;
    }
    
    
    private String buildSearchText(boolean includeCoords, int mode){
        //Portions of text to perform the search on and find teasers
        String searchText;

		//Merge all text into one with \n line separators
        //This will allow checking for multi line split results
        StringBuilder str = new StringBuilder();
        for (int i = 0; i != lines.length; i++) {
            if (lines[i].getRawData() != null && mode == lines[i].getWritingMode()) {
//                System.out.println("Line "+i+" : "+Strip.stripXML(removeHiddenMarkers(lines[i].getRawData()),true));
                    str.append(lines[i].getRawData()).append('\n');
            }
        }
        
        //Remove double spaces, replacing them with single spaces
        searchText = removeDuplicateSpaces(str.toString());

        //Strip xml and coords data from content and keep text data
        if(!includeCoords){
            searchText = removeHiddenMarkers(searchText);
        }
        
        searchText = Strip.stripXML(searchText, true).toString();
        
        //Store text in the search and teaser arrays
        return searchText;
    }
    
	private static String removeDuplicateSpaces(String textValue) {
		
		if(textValue.contains("  ")){
			
			textValue=textValue.replace("  ", " ");
			
		}
		return textValue;
	}
    
    private static int loadSearcherOptions(int searchType) {
        //Bitwise flags for regular expressions engine, options always required 
        int options = 0;

        //Turn on case sensitive mode
        if ((searchType & SearchType.CASE_SENSITIVE) != SearchType.CASE_SENSITIVE) {
            options = (options | Pattern.CASE_INSENSITIVE);
        }
        
        //Allow search to find split line results
        if ((searchType & SearchType.MUTLI_LINE_RESULTS) == SearchType.MUTLI_LINE_RESULTS) {
            options = (options | Pattern.MULTILINE | Pattern.DOTALL);
        }
        
        return options;
    }
    
    private static int[] getWritingModeOrder(int[] unsorted){
        final int[] sorted = {unsorted[0], unsorted[1], unsorted[2], unsorted[3]};

		//Set all to -1 so we can tell if it's been set yet
		final int[] writingModes = {-1,-1,-1,-1};

		Arrays.sort(sorted);

		for(int i=0; i!= unsorted.length; i++){
			for(int j=0; j < sorted.length; j++){
				if(unsorted[i]==sorted[j]){

					int pos = j - 3;
					if(pos<0) {
                        pos=-pos;
                    }

					if(writingModes[pos]==-1){
						writingModes[pos] = i;
						j=sorted.length;
					}
				}
			}
		}
        return writingModes;
    }
    
    private int[] getWritingModeCounts(Line[] items){
        
		//check orientation and get preferred. Items not correct will be ignored
		int l2r = 0;
		int r2l = 0;
		int t2b = 0;
		int b2t = 0;

		for(int i=0; i!=items.length; i++){
			switch(items[i].getWritingMode()){
			case 0 :l2r++; break;
			case 1 :r2l++; break;
			case 2 :t2b++; break;
			case 3 :b2t++; break;			
			}
		}

		return new int[]{l2r, r2l, t2b, b2t};
    }
    
	/**
	 * remove shadows from text created by double printing of text and drowned
	 * items where text inside other text
	 */
	private void cleanupShadowsAndDrownedObjects(final boolean avoidSpaces) {

		//get list of items
//		final int[] items = getUnusedFragments();
        
		final int count = fragments.length;
		int master, child;
		String separator;
        float diff;
        
        //work through objects and eliminate shadows or roll together overlaps
		for (int p = 0; p < count; p++) {

			//master item
			master = p;

			//ignore used items

				//work out mid point in text
				float midX = (fragments[master].getX1() + fragments[master].getX2()) / 2;
				float midY = (fragments[master].getY1() + fragments[master].getY2()) / 2;
				
				for (int p2 = p + 1;p2 < count;p2++) {

					//item to test against
					child = p2;
                    
                    //Ignore localFragments that have been used or have no width
                    if ((fragments[child].getX1() != fragments[child].getX2()) && (!fragments[child].hasMerged()) && (!fragments[master].hasMerged())) {

                        float fontDiff = fragments[child].getFontSize() - fragments[master].getFontSize();
                        if (fontDiff < 0) {
                            fontDiff = -fontDiff;
                        }

                        diff = (fragments[child].getX2() - fragments[child].getX1()) - (fragments[master].getX2() - fragments[master].getX1());
                        if(diff<0) {
                            diff=-diff;
                        }

                        //stop spurious matches on overlapping text
						if (fontDiff==0 && (midX > fragments[child].getX1())&& (midX < fragments[child].getX2())
							&& (diff< 10)
							&& (midY < fragments[child].getY1())&& (midY > fragments[child].getY2())) {

                            fragments[child].setMerged(true);

                            //pick up drowned text items (item inside another)			
                        } else {

							final boolean a_in_b =
								(fragments[child].getX1() > fragments[master].getX1())&& (fragments[child].getX2() < fragments[master].getX2())
									&& (fragments[child].getY1() < fragments[master].getY1())&& (fragments[child].getY2() > fragments[master].getY2());
							final boolean b_in_a =
								(fragments[master].getX1() > fragments[child].getX1())&& (fragments[master].getX2() < fragments[child].getX2())
									&& (fragments[master].getY1() < fragments[child].getY1())&& (fragments[master].getY2() > fragments[child].getY2());

                            //merge together
                            if (a_in_b || b_in_a) {
                                //get order right - bottom y2 underneath
                                if (fragments[master].getY2() > fragments[child].getY2()) {
									separator =getLineDownSeparator(fragments[master].getRawData(),fragments[child].getRawData());
									if((!avoidSpaces)||(separator.indexOf(' ')==-1)){
										merge(fragments[master], fragments[child],separator);
                                    }
                                } else {
									separator =getLineDownSeparator(fragments[child].getRawData(),fragments[master].getRawData());
									if(!avoidSpaces || separator.indexOf(' ')==-1){
										merge(fragments[master], fragments[child],separator);
                                    }
                                }

                                //recalculate as may have changed
                                midX = (fragments[master].getX1() + fragments[master].getX2()) / 2;
                                midY = (fragments[master].getY1() + fragments[master].getY2()) / 2;

                            }
                        }
                    }
				}
			
		}
	}
	
    /**
	 * workout if we should use space, CR or no separator when joining lines
	 */
    private static String getLineDownSeparator(final String rawLine1, final String rawLine2) {

		String returnValue = " "; //space is default

		final boolean hasUnderline = false;

		//get 2 lines without any XML or spaces so we can look at last char
        StringBuilder line1 = new StringBuilder(rawLine1);
        StringBuilder line2 = new StringBuilder(rawLine2);

        line1 = Strip.trim(line1);
        line2 = Strip.trim(line2);
		
		
		//get lengths and if appropriate perform tests
		final int line1Len = line1.length();
		final int line2Len = line2.length();
		
		if((line1Len>1)&&(line2Len>1)){

			//get chars to test
			final char line1Char2 = line1.charAt(line1Len - 1);
			final char line1Char1 = line1.charAt(line1Len - 2);
			final char line2Char1 = line2.charAt(0);
			final char line2Char2 = line2.charAt(1);

			//deal with hyphenation first - ignore unless :- or space-
            final String hyphen_values = "";
            if (hyphen_values.indexOf(line1Char2) != -1) {
				returnValue = ""; //default of nothing
				if (line1Char1 == ':') {
                    returnValue = "\n";
                }
				if (line1Char2 == ' ') {
                    returnValue = " ";
                }
                //paragraph breaks if full stop and next line has ascii char or Capital Letter
            } else if (
				((line1Char1 == '.') || (line1Char2 == '.'))
					&& (Character.isUpperCase(line2Char1)
						|| (line2Char1 == '&')
						|| Character.isUpperCase(line2Char2)
						|| (line2Char2 == '&'))){

                    returnValue="\n";
			}

		}
		
		//add an underline if appropriate
		if (hasUnderline){
            returnValue += '\n';
		}
		
		return returnValue;
	}
    
	/**
	 * general routine to see if we add a space between 2 text localFragments
	 */
    private String isGapASpace(final int c, final int l, final float actualGap, final boolean addMultiplespaceXMLTag, final int writingMode) {
		String sep = "";
		float gap;

		//use smaller gap
		final float gapA = fragments[c].getSpaceWidth() * fragments[c].getFontSize();
		final float gapB = fragments[l].getSpaceWidth() * fragments[l].getFontSize();

		if (gapA > gapB) {
            gap = gapB;
        } else {
            gap = gapA;
        }

        gap = (actualGap / (gap / 1000));

        //Round values to closest full integer as float -> int conversion rounds down
        if(gap > 0.51f && gap<1) {
            gap = 1;
        }

        final int spaceCount = (int) gap;

		if (spaceCount > 0) {
            sep = " ";
        }

		//add an XML tag to flag multiple spaces
		if (spaceCount > 1 && addMultiplespaceXMLTag && writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT) {
            sep = " <SpaceCount space=\"" + spaceCount + "\" />";
        }

		return sep;
	}
    
	/**
	 * convert localFragments into lines of text
	 */
	@SuppressWarnings("unused")
	private void createLinesForSearch(final int mode, final boolean breakOnSpace, final boolean addMultiplespaceXMLTag, final boolean isSearch) throws PdfException{
		
		String separator;

		final boolean debug=false;

		//create local copies of arrays
        Line[] localLines = fragments.clone();
        
//        final boolean[] isUsed = new boolean[lines.length];
        int finalCount = localLines.length;
        for(int i=0; i!=localLines.length; i++){
            if(localLines[i].hasMerged){
                finalCount--;
            }
        }
        
        //reverse order if text right to left
		if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
            for(int i = 0; i < localLines.length; i++){
                localLines[i] = fragments[localLines.length - i - 1];
            }
        }

		//scan items joining best fit to right of each fragment to build lines.
		for (int master = 0; master < localLines.length; master++) {
			    
			int id = -1;
            
			//float smallest_gap = -1, gap, yMidPt;
			if (!localLines[master].hasMerged() && localLines[master].getWritingMode() == mode) {

                if (debug) {
                    System.out.println("Look for match with " + removeHiddenMarkers(localLines[master].getRawData()));
                }

                for (int child = 0; child < localLines.length && id == -1; child++) {

                    float m_x1;
                    float m_x2;
                    float m_y1;
                    float m_y2;

                    float c_x1;
                    float c_x2;
                    float c_y1;
                    float c_y2;

                    //set pointers so left to right text
                    switch (mode) {
                        case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
                            m_x1=localLines[master].getX1();
                            m_x2=localLines[master].getX2();
                            m_y1=localLines[master].getY1();
                            m_y2=localLines[master].getY2();
                            break;                            
                        case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                            m_x2=localLines[master].getX1();
                            m_x1=localLines[master].getX2();
                            m_y1=localLines[master].getY1();
                            m_y2=localLines[master].getY2();
                            break;
                        case PdfData.VERTICAL_BOTTOM_TO_TOP:
                            m_x1=localLines[master].getY2();
                            m_x2=localLines[master].getY1();
                            m_y1=localLines[master].getX2();
                            m_y2=localLines[master].getX1();
                            break;
                        case PdfData.VERTICAL_TOP_TO_BOTTOM:
                            m_x1=localLines[master].getY2();
                            m_x2=localLines[master].getY1();
                            m_y2=localLines[master].getX1();
                            m_y1=localLines[master].getX2();
                            break;
                        default:
                            throw new PdfException("Illegal value "+mode+" for currentWritingMode");
                    }
                    
                    //set pointers so left to right text
                    switch (mode) {
                        case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
                            c_x1=localLines[child].getX1();
                            c_x2=localLines[child].getX2();
                            c_y1=localLines[child].getY1();
                            c_y2=localLines[child].getY2();
                            break;
                        case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                            c_x2=localLines[child].getX1();
                            c_x1=localLines[child].getX2();
                            c_y1=localLines[child].getY1();
                            c_y2=localLines[child].getY2();
                            break;
                        case PdfData.VERTICAL_BOTTOM_TO_TOP:
                            c_x1=localLines[child].getY2();
                            c_x2=localLines[child].getY1();
                            c_y1=localLines[child].getX2();
                            c_y2=localLines[child].getX1();
                            break;
                        case PdfData.VERTICAL_TOP_TO_BOTTOM:
                            c_x1=localLines[child].getY2();
                            c_x2=localLines[child].getY1();
                            c_y2=localLines[child].getX1();
                            c_y1=localLines[child].getX2();
                            break;
                        default:
                            throw new PdfException("Illegal value "+mode+" for currentWritingMode");
                    }


                    if (!localLines[child].hasMerged() && master != child && localLines[master].getWritingMode() == localLines[child].getWritingMode() && c_x1 != c_x2) {
                        if (debug) {
                            System.out.println("Checking " + removeHiddenMarkers(localLines[child].getRawData()));
                        }
                        //Get central points
                        float mx = m_x1 + ((m_x2 - m_x1) / 2);
                        float my = m_y2 + ((m_y1 - m_y2) / 2);
                        float cx = c_x1 + ((c_x2 - c_x1) / 2);
                        float cy = c_y2 + ((c_y1 - c_y2) / 2);

                        float smallestHeight = (m_y1 - m_y2);
                        float fontDifference = (c_y1 - c_y2) - smallestHeight;
                        if (fontDifference < 0) {
                            smallestHeight = (c_y1 - c_y2);
                        }

//                        System.out.println(m_x1+" , "+m_x2+" <> "+c_x1+" , "+c_x2);
//                        System.out.println(fontDifference+" , "+smallestHeight);
//                        System.out.println(my+" , "+cy);
                        //Don't merge is font of 1 is twice the size
                        if (Math.abs(fontDifference) < smallestHeight * 2) {
                                //Check for the same line by checking the center of 
                            //child is within master area
                            if (Math.abs(my - cy) < (smallestHeight * 0.5)) {
                                if (mx < cx) {//Child on right
                                    float distance = c_x1 - m_x2;
//                                    System.out.println("distance : "+distance);
                                    if (distance <= smallestHeight / 2) {
                                        id = child;
                                    }
                                }
                            }
                        }
                        //Match has been found
                        if (id != -1) {
//                            System.out.println("id!=-1");
                            float possSpace = c_x1 - m_x2;
                            if (mode == PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode == PdfData.VERTICAL_TOP_TO_BOTTOM) {
                                possSpace = -possSpace;
                            }

                            //add space if gap between this and last object
                            separator = isGapASpace(master, id, possSpace, addMultiplespaceXMLTag, mode);

                            //merge if adjoin
                            if (breakOnSpace && separator.startsWith(" ")) {
                                break;
                            }

                            if (debug) {
                                System.out.println("Merge items " + master + " & " + id);
                                System.out.println("c  : " + removeHiddenMarkers(localLines[master].getRawData()));
                                System.out.println("id : " + removeHiddenMarkers(localLines[id].getRawData()));
                                System.out.println("");
                            }

                            if ((isSearch && (child != master
                                    && ((c_x1 > m_x1 && mode != PdfData.VERTICAL_TOP_TO_BOTTOM)
                                    || (c_x1 < m_x1 && mode == PdfData.VERTICAL_TOP_TO_BOTTOM)
                                    && localLines[master].getWritingMode() == mode)))
                                    || (!isSearch && (child != master && ((c_x1 > m_x1 && mode != PdfData.VERTICAL_TOP_TO_BOTTOM)
                                    || c_x1 < m_x1 && mode == PdfData.VERTICAL_TOP_TO_BOTTOM && localLines[master].getWritingMode() == mode)))) { //see if on right
                                merge(localLines[master], localLines[id], separator);
                                finalCount--;
                                
                                
                            }

                            id = -1;
                        }
                    }
                }
            }
		}
        lines = new Line[finalCount];
        int next = 0;
        for(int i=0; i!=localLines.length; i++){
            if(!localLines[i].hasMerged()){
                lines[next] = localLines[i];
                next++;
            }
        }
	}
    
    /**
	 * merge 2 text localFragments together and update co-ordinates
	 */
    private void merge(final Line master, final Line child, final String separator) {
        
			//update co-ords
			if (master.getX1() > child.getX1()) {
                master.setX1(child.getX1());
            }
            if (master.getY1() < child.getY1()) {
                master.setY1(child.getY1());
            }
            if (master.getX2() < child.getX2()) {
                master.setX2(child.getX2());
            }
            if (master.getY2() > child.getY2()) {
                master.setY2(child.getY2());
            }

            //use font size of second text (ie at end of merged text)
            master.setFontSize(child.getFontSize());

            //add together
            StringBuilder content = new StringBuilder();
            content.append(master.getRawData()).append(separator).append(child.getRawData());
            master.setRawData(content.toString());

            //track length of text less all tokens
            master.setTextLength(master.getTextLength()+child.getTextLength());

            //set objects to null to flush and log as used
            child.setRawData(null);
            child.setMerged(true);
	}
	
    private void copyToArrays() {

		final int count = pdf_data.getRawTextElementCount();

        fragments = new Line[count];
		
		//set values
		for (int i = 0; i < count; i++) {
            fragments[i] = new Line(pdf_data, i);
		}
	}
        
    private class Line implements Comparable<Line>{
        private float x1, y1, x2, y2, character_spacing, spaceWidth;
        private String raw, currentColor;
        private int text_length, mode, fontSize;
        private boolean hasMerged;
        
        Line(PdfData pdf_data, int index){
            loadData(pdf_data, index);
        }
        
        private void loadData(PdfData pdf_data, int index){
            //extract values
            character_spacing = pdf_data.f_character_spacing[index];
            x1 = pdf_data.f_x1[index];
            x2 = pdf_data.f_x2[index];
            y1 = pdf_data.f_y1[index];
            y2 = pdf_data.f_y2[index];
            currentColor = pdf_data.colorTag[index];
            text_length = pdf_data.text_length[index];
            mode = pdf_data.f_writingMode[index];
            raw = pdf_data.contents[index];
			fontSize = pdf_data.f_end_font_size[index];
			spaceWidth = pdf_data.space_width[index];
            hasMerged = false;
        }
        
        protected float getX1(){return x1;}
        protected float getY1(){return y1;}
        protected float getX2(){return x2;}
        protected float getY2(){return y2;}
        protected float getCharacterSpacing(){return character_spacing;}
        protected float getSpaceWidth(){return spaceWidth;}
        
        protected String getRawData(){return raw;}
        protected String getColorTag(){return currentColor;}
        
        protected int getWritingMode(){return mode;}
        protected int getTextLength(){return text_length;}
        protected int getFontSize(){return fontSize;}
        
        protected boolean hasMerged(){return hasMerged;}
        
        protected void setX1(float value){x1=value;}
        protected void setY1(float value){y1=value;}
        protected void setX2(float value){x2=value;}
        protected void setY2(float value){y2=value;}
        protected void setFontSize(int value){fontSize=value;}
        protected void setRawData(String value){raw=value;}
        protected void setTextLength(int value){text_length=value;}
        protected void setMerged(boolean value){hasMerged=value;}

        @Override
        public int compareTo(Line o) {
            switch(mode){
                case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                case PdfData.HORIZONTAL_RIGHT_TO_LEFT :
                    return (int)(y1 - o.getY1());
                case PdfData.VERTICAL_TOP_TO_BOTTOM :
                case PdfData.VERTICAL_BOTTOM_TO_TOP :
                    return (int)(x1 - o.getX1());
            }
            return 0;
        }
    }
}
