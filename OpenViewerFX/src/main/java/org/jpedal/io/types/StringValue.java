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
 * StringValue.java
 * ---------------
 */
package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class StringValue {
    
    
    public static int setStringConstantValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {

        i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i+1, 47);
        
        final int keyStart=i;

        //move cursor to end of text
        while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
            i++;
        }

        //store value
        pdfObject.setConstant(PDFkeyInt,keyStart,i-keyStart,raw);
        
        if(debugFastCode) {
            System.out.println(padding + "Set constant in " + pdfObject + " to " + pdfObject.setConstant(PDFkeyInt, keyStart, i-keyStart, raw));
        }
        
        return i-1;   // move back so loop works
    }
    

    
    public static int setStringKeyValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {

        i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i+1, 47);
        
        final int keyStart=i;

        boolean isNull=false;
        
        //move cursor to end of text (allow for null)
        while(raw[i]!='R' && !isNull){
            
            //allow for null for Parent
            if(PDFkeyInt== PdfDictionary.Parent && StreamReaderUtils.isNull(raw,i)) {
                isNull = true;
            }
            
            i++;
        }

        if(!isNull){
            setValue(pdfObject, 1+i-keyStart, raw, PDFkeyInt, keyStart);
        }

        return i-1; // move back so loop works
    }

    static void setValue(final PdfObject pdfObject, final int keyLength, final byte[] raw, final int PDFkeyInt, final int keyStart) {

        //set value
        final byte[] stringBytes=new byte[keyLength];
        System.arraycopy(raw,keyStart,stringBytes,0,keyLength);

        //store value
        pdfObject.setStringKey(PDFkeyInt,stringBytes);

        if(debugFastCode) {
            System.out.println(padding + "Set constant in " + pdfObject + " to " + new String(stringBytes));
        }
    }

}


