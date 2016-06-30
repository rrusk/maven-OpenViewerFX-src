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
 * ArrayFactory.java
 * ---------------
 */
package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.resolveFully;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 * @author markee
 */
public class ArrayFactory {

    public static ArrayDecoder getDecoder(PdfFileReader objectReader, int i, int endPt, int type,byte[] raw) {
        
        switch (type) {

            case PdfDictionary.VALUE_IS_KEY_ARRAY:
                return new KeyArray(objectReader, i, endPt, type, raw);

            case PdfDictionary.VALUE_IS_MIXED_ARRAY:
                return new MixedArray(objectReader, i, endPt, type, raw);

            case PdfDictionary.VALUE_IS_OBJECT_ARRAY:
                return new ObjectArray(objectReader, i, endPt, type, raw);

            default:
                return new SimpleArray(objectReader, i, endPt, type, raw);
        }
    }

    public static ArrayDecoder getDecoder(PdfFileReader objectReader, int i, int length, int type, Object[] values, int currentElement, byte[] raw) {

        switch (type) {

            case PdfDictionary.VALUE_IS_KEY_ARRAY:
                return new KeyArray(objectReader, i, length, type, values, currentElement, raw);

            case PdfDictionary.VALUE_IS_MIXED_ARRAY:
                return new MixedArray(objectReader, i, length, type, values, currentElement, raw);

            case PdfDictionary.VALUE_IS_OBJECT_ARRAY:
                return new ObjectArray(objectReader, i, length, type, values, currentElement, raw);

            default:
                return new SimpleArray(objectReader, i, length, type,values, currentElement,raw);
        }
    }
    
    /**
    public void readArray(final boolean ignoreRecursion, final byte[] raw, final PdfObject pdfObject, final int PDFkeyInt) {  
            
        //read new
        byte[][] newValues = pdfObject.getKeyArray(PDFkeyInt);

        //reread old 
        //readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);

        //read new
        byte[][] oldValues = pdfObject.getKeyArray(PDFkeyInt);

        boolean areIdentical = compare(oldValues, newValues);
        if (!areIdentical) {
            System.exit(1);
        }
                
        
    }
    
    private static boolean compare(byte[][] oldValues, byte[][] newValues) {
        if((oldValues==null && newValues!=null) || (oldValues!=null && newValues==null)){
            System.out.println("One is null old="+oldValues+" new="+newValues);
            return false;
        }
        
        if((oldValues.length!=newValues.length)){
            System.out.println("Different lengths old="+oldValues.length+" new="+newValues.length);
            return false;
        }
        
        int count=oldValues.length;
        
        for(int i=0;i<count;i++){
            if((oldValues[i]==null && newValues[i]!=null) || (oldValues[i]!=null && newValues[i]==null)){
                System.out.println("One is null old="+oldValues[i]+" new="+newValues[i]);
                return false;
            }else if(!String.valueOf(oldValues[i]).equals(String.valueOf(oldValues[i]))){
                for(int j=0;j<i+1;j++){
                    System.out.println(j+" old="+new String(oldValues[j])+" new="+new String(newValues[j]));
                }
                return false;
            }
        }
        
        return true;
    }/**/
    
    public static int processArray(final PdfObject pdfObject, final byte[] raw, final int PDFkeyInt, final int possibleArrayStart, final PdfFileReader objectReader) {
        
        final int i;//find end
        int endPoint = possibleArrayStart;
        while (raw[endPoint] != ']' && endPoint <= raw.length) {
            endPoint++;
        }
        
        //convert data to new Dictionary object and store
        final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, null, pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);
        pdfObject.setDictionary(PDFkeyInt, valueObj);
        valueObj.ignoreRecursion(pdfObject.ignoreRecursion());
        
        if(valueObj.isDataExternal()){
            valueObj.isDataExternal(true);
            if(!resolveFully(valueObj,objectReader)) {
                pdfObject.setFullyResolved(false);
            }
        }
        
        int type = PdfDictionary.VALUE_IS_INT_ARRAY;
        if (PDFkeyInt == PdfDictionary.TR) {
            type = PdfDictionary.VALUE_IS_KEY_ARRAY;
        }
        
        final ArrayDecoder objDecoder=ArrayFactory.getDecoder(objectReader, possibleArrayStart, endPoint, type, raw);
        i=objDecoder.readArray(pdfObject.ignoreRecursion(),valueObj, PDFkeyInt);
        
        //rollon
        return i;
    }
}
