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
 * TextTokens.java
 * ---------------
 */
package org.jpedal.io;

/**
 * encapsualtes function to read values from a text string
 */
public class TextTokens {

    /**
     * holds content
     */
    private final byte[] content;

    /**
     * pointers to position to see if finished
     */
    private final int length;
    private int currentCharPointer;

    /**
     * initialise text with value
     */
    public TextTokens(final byte[] rawText) {

        content = rawText;
        length = rawText.length;
        currentCharPointer = 0;

    }

    /**
     * see if end reached
     */
    public boolean hasMoreTokens() {
        return currentCharPointer < length;
    }

    /**
     * read the next double char
     *
     * @param keepReturns
     * @return
     */
    public char nextUnicodeToken(final boolean keepReturns) {

        int first, second = 0;

        first = nextToken();

        //allow for 2 byte return char first and then check both as single
        //Added for file sample_pdfs_html/12jul/1997.pdf (popup characters incorrect)
        if (this.hasMoreTokens()) {
            second = nextToken();
            final int combined = ((first << 8) + second);

            if (combined == 13 && keepReturns) {
                return (char) combined;
            }
        }

        if (first == 13 && keepReturns && this.hasMoreTokens()) {
            first = nextToken();
        }

        if (this.hasMoreTokens()) {
            if (second == 13 && keepReturns && this.hasMoreTokens()) {
                second = nextToken();
            }
        }

        return (char) ((first << 8) + second);
    }

    /**
     * get the char
     */
    private char getChar(final int pointer) {

        final int number = (content[pointer] & 0xFF);

        return (char) number;
    }

    /**
     * read a single char
     */
    public char nextToken() {

        final char nextChar = getChar(currentCharPointer);
        currentCharPointer++;

        return nextChar;
    }

    /**
     * test start to see if unicode
     */
    public boolean isUnicode() {

        //test if unicode by reading first 2 values
        if ((length >= 2) && (nextToken() == 254) && (nextToken() == 255)) {

            return true;
        } else {
            //its not unicode to put pointer back to start
            this.currentCharPointer = 0;
            return false;
        }
    }

}
