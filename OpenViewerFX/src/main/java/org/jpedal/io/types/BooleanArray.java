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
 * BooleanArray.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * @author markee
 */
public class BooleanArray extends Array {

    public BooleanArray(final PdfFileReader pdfFileReader, final int i, final byte[] raw) {
        super(pdfFileReader, i, PdfDictionary.VALUE_IS_BOOLEAN_ARRAY, raw);
    }

    @Override
    void fillArray(final int elementCount, final PdfObject pdfObject) {

        final boolean[] finalByteValues = new boolean[elementCount];
        byte[] data;

        for (int a = 0; a < elementCount; a++) {
            data = valuesRead.get(a);
            finalByteValues[a] = (data.length > 3 && data[0] == 't' && data[1] == 'r' && data[2] == 'u' && data[3] == 'e');
        }

        pdfObject.setBooleanArray(PDFkeyInt, finalByteValues);


    }

}
