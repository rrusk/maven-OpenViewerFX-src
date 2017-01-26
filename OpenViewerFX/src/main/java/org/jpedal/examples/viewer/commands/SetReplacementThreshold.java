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
 * SetReplacementThreshold.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.util.HashMap;
import java.util.Map;
import org.jpedal.PdfDecoderInt;
import org.jpedal.constants.JPedalSettings;
import org.jpedal.exception.PdfException;

/**
 * Set the threshold to determine which text has its colour changed when using SetTextColor.
 */
public class SetReplacementThreshold {

    /**
     * Accepts a single value to use as a threshold to use for color changing.
     * All color values (R,G and B), must be under this value in order to change text color.
     *
     * @param args object array containing arguments, args[0] should be an integer between 0 and 255.
     * @param decode_pdf PdfDecoderInt object representing the PdfObject currently open.
     */
    public static void execute(final Object[] args, final PdfDecoderInt decode_pdf) {
        try {
            final Map<Integer, Object> map = new HashMap<Integer, Object>();
            map.put(JPedalSettings.REPLACEMENT_COLOR_THRESHOLD, args[0]);
            decode_pdf.modifyNonstaticJPedalParameters(map);
        } catch (final PdfException e2) {

            e2.printStackTrace();
        }

    }
}
