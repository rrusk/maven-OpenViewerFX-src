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
 * GlyphData.java
 * ---------------
 */

package org.jpedal.parser.text;

import org.jpedal.fonts.CmapSpec;
import org.jpedal.fonts.PdfFont;

/**
 * @author markee
 */
public class GlyphData {

    private boolean firstTime = true;

    /**
     * length of current text fragment
     */
    private int textLength;

    private int numOfPrefixes;

    private float fontScale;

    private char lastChar, openChar;

    private int fontSize;

    private int charSize = 2;

    private String displayValue, unicodeValue;

    private boolean isHorizontal;

    private boolean inText;

    private char rawChar;

    private float actualWidth;

    private int rawInt, valueForHTML = -1;

    private int possibleVal;

    private char lastTextChar = 'x';
	
	private CmapSpec encodingCMAP = null;

    public int getRawInt() {
        return rawInt;
    }

    public void setRawInt(final int rawInt) {
        this.rawInt = rawInt;
    }

    public void setRaw(final int rawInt) {
        this.rawInt = rawInt;
        this.rawChar = (char) rawInt;
    }

    private boolean isXMLExtraction = true;

    private float leading, width, spacingAdded;

    public float getLeading() {
        return leading;
    }

    public void setLeading(final float leading) {
        this.leading = leading;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(final float width) {
        this.width = width;
    }

    public float getSpacingAdded() {
        return spacingAdded;
    }

    public void setSpacingAdded(final float spacingAdded) {
        this.spacingAdded = spacingAdded;
    }

    /**
     * @return the displayValue
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * @param displayValue the displayValue to set
     */
    public void setDisplayValue(final String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * @return the unicodeValue
     */
    public String getUnicodeValue() {
        return unicodeValue;
    }

    /**
     * @param unicodeValue the unicodeValue to set
     */
    public void setUnicodeValue(final String unicodeValue) {
        this.unicodeValue = unicodeValue;
    }

    void resetValues() {
        inText = false;

        isHorizontal = false;


    }

    void reset() {

        firstTime = true;

        textLength = 0;

        rawChar = ' ';

        displayValue = "";
        unicodeValue = "";

        spacingAdded = 0;

        leading = 0;
        width = 0;

        rawInt = 0;

        actualWidth = 0;

        valueForHTML = -1; //used by HTML to track chars which have ISSUES

        lastChar = ' ';
        openChar = ' ';

        numOfPrefixes = 0;

        lastTextChar = 'x';

    }

    void set(final String val) {
        displayValue = val;
        unicodeValue = val;
    }

    boolean isText() {
        return inText;
    }

    void setText(final boolean b) {
        inText = b;
    }

    boolean isHorizontal() {
        return isHorizontal;
    }

    void setHorizontal(final boolean b) {
        isHorizontal = b;
    }

    void setXMLExtraction(final boolean xmlExtraction) {
        this.isXMLExtraction = xmlExtraction;
    }

    boolean isXMLExtraction() {
        return isXMLExtraction;
    }

    void updateGlyphSettings(final float value, final char rc) {

        width += value;
        leading += value; //keep count on leading
        spacingAdded += leading;

        rawChar = rc;
    }

    void subtractLeading(final float value) {
        leading -= value;
    }

    void addToWidth(final float currentWidth) {
        textLength++; //counter on chars in data

        width += currentWidth;
    }

    char getRawChar() {
        return rawChar;
    }

    void setRawChar(final char c) {
        rawChar = c;
    }

    /**
     * @return the fontScale
     */
    public float getFontScale() {
        return fontScale;
    }

    /**
     * @param fontScale the fontScale to set
     */
    public void setFontScale(final float fontScale) {
        this.fontScale = fontScale;
    }

    void setActualWidth(final float actualWidth) {
        this.actualWidth = actualWidth;
    }

    float getActualWidth() {
        return actualWidth;
    }

    /**
     * @return the valueForHTML
     */
    public int getValueForHTML() {
        return valueForHTML;
    }

    /**
     * @param valueForHTML the valueForHTML to set
     */
    public void setValueForHTML(final int valueForHTML) {
        this.valueForHTML = valueForHTML;
    }

    void setDefaultCharSize(final PdfFont currentFontData) {
        setCharSize(2);
        if (currentFontData.isCIDFont() && !currentFontData.isSingleByte()) {
            setCharSize(4);
        }

    }

    /**
     * @return the charSize
     */
    public int getCharSize() {
        return charSize;
    }

    /**
     * @param charSize the charSize to set
     */
    public void setCharSize(final int charSize) {
        this.charSize = charSize;
    }

    int getFontSize() {
        return fontSize;
    }

    void setFontSize(final int fontSize) {
        this.fontSize = fontSize;
    }

    void setLastChar(final char newChar) {
        lastChar = newChar;
    }

    char getLastChar() {
        return lastChar;
    }

    char getOpenChar() {
        return openChar;
    }

    void setOpenChar(final char rawChar) {
        openChar = rawChar;
    }

    void updatePrefixCount(final char testChar) {

        if (testChar == 40) {
            numOfPrefixes++;
        } else if (testChar == 41) { //')'=41
            if (numOfPrefixes <= 0) {
                inText = false; //unset text flag
            } else {
                numOfPrefixes--;
            }
        }
    }

    void setLastChar() {
        if (lastChar == 92 && rawChar == 92) { //checks if \ has been escaped in '\\'=92
            lastChar = ((char) 120);
        } else {
            lastChar = rawChar;
        }
    }

    int getTextLength() {
        return textLength;
    }

    boolean isfirstTime() {
        return firstTime;
    }

    void setFirstTime(final boolean b) {
        firstTime = b;
    }

    void setPossibleValue(final int val) {
        possibleVal = val;
    }

    int getPossibleValue() {
        return possibleVal;
    }

    public void setLastTextChar(final char lastTextChar) {
        this.lastTextChar = lastTextChar;
    }

    public char getLastTextChar() {
        return lastTextChar;
    }

	public CmapSpec getEncodingCMAP() {
		return encodingCMAP;
	}

	public void setEncodingCMAP(CmapSpec encodingCMAP) {
		this.encodingCMAP = encodingCMAP;
	}
	
	
}
