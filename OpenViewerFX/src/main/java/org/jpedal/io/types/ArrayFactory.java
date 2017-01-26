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
 * ArrayFactory.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;

/**
 *
 * @author markee
 */
public class ArrayFactory {

    public static ArrayDecoder getDecoder(final PdfFileReader objectReader, int i, final int type, final byte[] raw) {

        switch (type) {

            case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:
                return new BooleanArray(objectReader, i, raw);

            case PdfDictionary.VALUE_IS_FLOAT_ARRAY:
                return new FloatArray(objectReader, i, raw);

            case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:
                return new DoubleArray(objectReader, i, raw);

            case PdfDictionary.VALUE_IS_INT_ARRAY:
                return new IntArray(objectReader, i, raw);

            case PdfDictionary.VALUE_IS_KEY_ARRAY:
                return new KeyArray(objectReader, i,  raw);

            case PdfDictionary.VALUE_IS_MIXED_ARRAY:
                return new Array(objectReader, i, type, raw);

            case PdfDictionary.VALUE_IS_OBJECT_ARRAY:
                return new ObjectArray(objectReader, i, raw);

            default:
                return new StringArray(objectReader, i, raw);
        }
    }
}
