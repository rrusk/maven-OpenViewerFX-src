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
 * FloatArray.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.NumberUtils;

/**
 *
 * @author markee
 */
public class FloatArray extends Array {
    
    public FloatArray(final PdfFileReader pdfFileReader, int i,final byte[] raw) {
        super(pdfFileReader, i,  PdfDictionary.VALUE_IS_FLOAT_ARRAY, raw);
    }

    @Override
    void fillArray(final int elementCount, PdfObject pdfObject) {
        
        float[] finalByteValues = new float[elementCount];
        byte[] data;
        
        for(int a=0;a<elementCount;a++){
            data=valuesRead.get(a);
            finalByteValues[a]=NumberUtils.parseFloat(0, data.length, data); 
        }
        
        pdfObject.setFloatArray(PDFkeyInt, finalByteValues);
        
               
    }
    
}
