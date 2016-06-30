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
 * BaseArray.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.ObjectDecoder;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 * @author markee
 */
public class BaseArray extends ObjectDecoder {

    int i, j2;
    int endPoint;
    int type;

    int keyReached = -1;

    Object[] objectValuesArray;

    final byte[] raw;
    byte[]  arrayData;

    int PDFkeyInt, endI = -1;

    int rawLength;
   
    int keyStart = i;

    boolean isSingleDirectValue; //flag to show points to Single value (ie /FlateDecode)
    boolean isSingleNull = true;

    int endPtr = -1;

    public BaseArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final byte[] raw) {
        super(pdfFileReader);

        this.i = i;
        this.endPoint = endPoint;
        this.type = type;
        this.raw = raw;

        if(raw!=null){
            rawLength=raw.length;
        }
    }

    public BaseArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final Object[] objectValuesArray, final int keyReached, final byte[] raw) {
        super(pdfFileReader);

        this.i = i;
        this.endPoint = endPoint;
        this.type = type;
        this.objectValuesArray = objectValuesArray;
        this.keyReached = keyReached;
        this.raw = raw;
        
        if(raw!=null){
            rawLength=raw.length;
        }
    }

    boolean findStart() {

        if (debugFastCode) {
            System.out.println(padding + "Reading array type=" + PdfDictionary.showArrayType(type) + ' ' + (char) raw[i] + ' ' + (char) raw[i + 1] + ' ' + (char) raw[i + 2] + ' ' + (char) raw[i + 3] + ' ' + (char) raw[i + 4]);
        }

        //roll on
        if (raw[i] != 91 && raw[i] != '<') {
            i++;
        }
        //ignore empty
        if (raw[i] == '[' && raw[i + 1] == ']') {
            return true;
        }
        //move cursor to start of text
        while (raw[i] == 10 || raw[i] == 13 || raw[i] == 32) {
            i++;
        }
        //allow for comment
        if (raw[i] == 37) {
            i = ArrayUtils.skipComment(raw, i);
        }

        return false;
    }

    boolean readIndirect(final boolean ignoreRecursion, final boolean alwaysRead, final PdfObject pdfObject) throws RuntimeException {

        //allow for indirect to 1 item
        final int startI = i;

        if (debugFastCode) {
            System.out.print(padding + "Indirect object ref=");
        }

        //move cursor to end of ref
        i = ArrayUtils.skipToEndOfRef(i, raw);
        //actual value or first part of ref
        final int ref = NumberUtils.parseInt(keyStart, i, raw);
        //move cursor to start of generation
        while (raw[i] == 10 || raw[i] == 13 || raw[i] == 32 || raw[i] == 47 || raw[i] == 60) {
            i++;
        }
        
        // get generation number
        keyStart = i;
        
        //move cursor to end of reference
        i = ArrayUtils.skipToEndOfRef(i, raw);
        
        final int generation = NumberUtils.parseInt(keyStart, i, raw);
        
        if (debugFastCode) {
            System.out.print(padding + " ref=" + ref + " generation=" + generation + '\n');
        }
        
        // check R at end of reference and abort if wrong
        //move cursor to start of R
        while (raw[i] == 10 || raw[i] == 13 || raw[i] == 32 || raw[i] == 47 || raw[i] == 60) {
            i++;
        }
        
        if (raw[i] != 82){ //we are expecting R to end ref
            throw new RuntimeException(padding + "4. Unexpected value " + (char) raw[i] + " in file - please send to IDRsolutions for analysis");
        }
        
        if (ignoreRecursion && !alwaysRead) {
            if (debugFastCode) {
                System.out.println(padding + "Ignore sublevels");
            }
            return true;
        }
        //read the Dictionary data
        arrayData = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

        //allow for data in Linear object not yet loaded
        if (arrayData == null) {
            pdfObject.setFullyResolved(false);

            LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (14)");

            i = rawLength;

            return true;
        }

        //lose obj at start and roll onto [
        j2 = 0;
        while (arrayData[j2] != 91) {

            //allow for % comment
            if (arrayData[j2] == '%') {
                j2 = ArrayUtils.skipComment(arrayData, j2);

                //roll back as [ may be next char
                j2--;
            }

            //allow for null
            if (ArrayUtils.isNull(arrayData, j2)) {
                break;
            }

            //allow for empty
            if (arrayData[j2] == 'e' && arrayData[j2 + 1] == 'n' && arrayData[j2 + 2] == 'd' && arrayData[j2 + 3] == 'o') {
                break;
            }

            if (arrayData[j2] == 47) { //allow for value of type  32 0 obj /FlateDecode endob
                j2--;
                isSingleDirectValue = true;
                break;
            }
            if ((arrayData[j2] == '<' && arrayData[j2 + 1] == '<') || (j2 + 4 < arrayData.length && arrayData[j2 + 3] == '<' && arrayData[j2 + 4] == '<')) { //also check ahead to pick up [<<
                endI = i;

                j2 = startI;
                arrayData = raw;

                if (debugFastCode) {
                    System.out.println(padding + "Single value, not indirect");
                }

                break;
            }

            j2++;
        }

        return false;
    }
    
    /**
     * //see case 23155 (encrypted annot needs obj ref appended so we can decrypt string later)       
     */
    static  byte[] appendObjectRef(final PdfObject pdfObject, byte[] newValues) {
        
        String s=pdfObject.getObjectRefAsString();
        final int len=newValues.length;
        final int strLen=s.length()-1;
        final int newLength=strLen+4+newValues.length;
        byte[] adjustedArray=new byte[newLength];
        System.arraycopy(s.getBytes(), 0,  adjustedArray,0, strLen);
        System.arraycopy("obj ".getBytes(), 0,  adjustedArray,strLen,4);
        System.arraycopy(newValues, 0,  adjustedArray,strLen+4, len);
        newValues=adjustedArray;
        
        return newValues;
        
    }
}
