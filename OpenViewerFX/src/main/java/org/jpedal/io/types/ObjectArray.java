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
 * ObjectArray.java
 * ---------------
 */
package org.jpedal.io.types;

import   java.util.ArrayList;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * parse PDF array data from PDF for mixed and object values
 */
public class ObjectArray extends Array {

    private final ArrayList<Object> objectArray=new ArrayList<Object>();
    
    public ObjectArray(final PdfFileReader pdfFileReader, final int i, final byte[] raw) {
        super(pdfFileReader, i, PdfDictionary.VALUE_IS_OBJECT_ARRAY, raw);  
    }
    
    @Override
    boolean isFirstKeySingle() {
         return arrayData[j2]=='/' ||arrayData[j2]=='(' || arrayData[j2]=='<' || StreamReaderUtils.isRef(arrayData, j2);
    }
    
    @Override
    void scanElements(final boolean singleKey, final PdfObject pdfObject) {

        int currentElement = 0;

        final int arrayEnd=arrayData.length;
        
        findArrayStart();
        
        if (debugFastCode) {
            System.out.println(padding + "----scanning elements----arrayData[endPtr]=" + arrayData[j2] + " type=" + type);
        }
       
        Object newValues;
         
        int keyStart;
        
        while (j2 < arrayEnd && arrayData[j2] != 93) {
        
            keyStart=moveToStartOfNextValue();

            if(StreamReaderUtils.isEndObj(arrayData,j2)){
                break;
            }else if(arrayData[j2]=='>' && arrayData[j2+1]=='>'){
                break;
            }else if(arrayData[j2-1]=='/'){  // /key               
                newValues=writeKey();
            }else if(StreamReaderUtils.isRef(arrayData, j2)){
                newValues = getIndirectRef(pdfObject, keyStart);
            }else if((arrayData[j2]=='<' && arrayData[j2+1]=='<')){
                newValues = writeObject(keyStart);
            }else if(StreamReaderUtils.isNumber(arrayData, j2)){               
                newValues=writeNumber();
            }else if(StreamReaderUtils.isNull(arrayData,j2)){               
                newValues=writeNull();
            }else if(arrayData[j2]=='('){                                 
                newValues=writeString(pdfObject);
            }else if(arrayData[j2]=='['){                                
                newValues =writeValue(pdfObject);
            }else if(arrayData[j2+1]=='<' && arrayData[j2+2]=='<'){                                
                newValues = writeDirectDictionary(keyStart); 
            }else if(arrayData[j2]=='<'){
                newValues=writeHexString(pdfObject);
            }else if(arrayData[j2]==']'){
                break;
            }else{     
                newValues = writeGeneral(keyStart);
            }
            
            objectArray.add(newValues);
            
            currentElement++;
            
            if(singleKey){
                break;
            }
        }
        
        fillArray(currentElement, pdfObject);
        
    }

    private Object getIndirectRef(PdfObject pdfObject, int keyStart) {

        final byte[] ref=writeObject(keyStart);
        PdfObject obj=new PdfObject(new String(ref));

        final byte[] bytes = objectReader.readObjectData(obj);

        final int len=bytes.length;

        //return null if object does not exist
        if(len==0){
            return null;
        }
        
        //find first key char to show if indirect
        int a=0;
        while(a<len && bytes[a]!='[' && bytes[a]!='<'){
            a++;
        }
        
        if(bytes[a]=='[') {
            final ObjectArray objDecoder = new ObjectArray(objectReader, a, bytes);
            objDecoder.readArray(pdfObject, PDFkeyInt);

            return objDecoder.getValues();
        }else{
            return ref;
        }
    }

    private Object writeValue(final PdfObject pdfObject) {
        
        //find end
        int j3=j2+1;
        int level=1;
        while(true){
            
            j3++;
            
            if(j3==arrayData.length) {
                break;
            }
            
            if(arrayData[j3]==93) {
                level--;
            } else if(arrayData[j3]==91) {
                level++;
            }
            
            if(level==0) {
                break;
            }
        }

        if(debugFastCode) {
            padding += "   ";
        }
        
        final ObjectArray objDecoder=new ObjectArray(objectReader, j2, arrayData);
        j2 = objDecoder.readArray(pdfObject, PDFkeyInt);
        
        if(arrayData[j2]!='[') {
            j2++;
        }

        return objDecoder.getValues();
    }

    @Override
    void fillArray(final int elementCount, PdfObject pdfObject) {
     
        Object[] finalByteValues = new Object[elementCount];
        for(int a=0;a<elementCount;a++){
            finalByteValues[a]=objectArray.get(a);
        }
        
        pdfObject.setObjectArray(PDFkeyInt, finalByteValues);
               
    }

    /**
     * used for debugging
     */
    @Override
    void showValues() {

        final StringBuilder values = new StringBuilder("[");

        values.append(ObjectUtils.showMixedValuesAsString(objectArray.toArray(), ""));
        
        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }

    private Object[] getValues() {
        return objectArray.toArray();
    }
}
