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
 * PdfKeyPairsIterator.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.NumberUtils;

/**
 * allow fast access to data from PDF object
 */
public class PdfKeyPairsIterator {

    private final byte[][] keys, values;

    int maxCount, current;

    public PdfKeyPairsIterator(final byte[][] keys, final byte[][] values) {

        this.keys = keys;
        this.values = values;

        if (keys != null) {
            maxCount = keys.length;
        }

        current = 0;
    }

    /**
     * number of PAIRS (or keys)
     *
     * @return
     */
    public int getTokenCount() {
        return maxCount;
    }

    /**
     * roll onto next key and value
     */
    public void nextPair() {

        if (current < maxCount) {
            current++;
        } else {
            throw new RuntimeException("No keys left in PdfKeyPairsIterator");
        }
    }

    /**
     * next key
     *
     * @return
     */
    public String getNextKeyAsString() {

        return new String(keys[current]);

    }

    /**
     * used by CharProcs to return number or number of key (ie /12 or /A)
     *
     * @return
     */
    public int getNextKeyAsNumber() {

        final int length = keys[current].length;
        final boolean isNumber = isNextKeyANumber();

        if (!isNumber) {
            if (keys[current].length != 1) {
                if (1 == 1) {
                    throw new RuntimeException("Unexpected value in getNextKeyAsNumber >" + new String(keys[current]) + '<');
                }
            } else {
                return (keys[current][0] & 255);
            }
        } else {
            return NumberUtils.parseInt(0, length, keys[current]);
        }

        return -1;
    }

    public boolean isNextKeyANumber() {

        final int length = keys[current].length;

        boolean isNumber = true;

        for (int ii = 0; ii < length; ii++) {
            final int nextChar = keys[current][ii];

            if (nextChar >= '0' && nextChar <= '9') {

            } else {
                isNumber = false;
                ii = length;
            }
        }
        return isNumber;
    }

    public boolean hasMorePairs() {
        return current < maxCount;
    }

    public byte[] getNextValueAsBytes() {
        if (values[current] == null) {
            return null;
        } else {
            return values[current];
        }
    }

    public String getNextValueAsString() {
        if (values[current] == null) {
            return null;
        } else {
            return new String(values[current]);
        }
    }
}