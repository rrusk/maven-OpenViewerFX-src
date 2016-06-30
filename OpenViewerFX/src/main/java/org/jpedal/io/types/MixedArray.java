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
 * MixedArray.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.ArrayList;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 * parse PDF array data from PDF for mixed and object values
 */
public class MixedArray extends BaseArray implements ArrayDecoder{

    
    private ArrayList<byte[]> mixedArray;
     
    //now create array and read values
    private byte[][] mixedValues;
   
    public MixedArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type,byte[] raw) {
        super(pdfFileReader, i, endPoint, type, raw);  
    }

    public MixedArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final Object[] objectValuesArray, final int keyReached,byte[] raw) {
        super(pdfFileReader,i, endPoint, type, objectValuesArray, keyReached, raw);
    }
    
    @Override
    public int readArray(final boolean ignoreRecursion, final PdfObject pdfObject, final int PDFkeyInt) {

        
        if(findStart()){ //will also exit if empty array [] 
            return i + 1;
        }
        
        if(debugFastCode) {
            System.out.println(padding + "Reading array type=" + PdfDictionary.showArrayType(type) + " into " + pdfObject + ' ' + (char) raw[i] + ' ' + (char) raw[i + 1] + ' ' + (char) raw[i + 2] + ' ' + (char) raw[i + 3] + ' ' + (char) raw[i + 4]);
        }
        
        //may need to add method to PdfObject is others as well as Mask (last test to  allow for /Contents null
        //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler
        boolean isIndirect=raw[i]!=91 && raw[i]!='(' && raw[0]!=0 && 
                !ArrayUtils.isNull(raw,i) && ArrayUtils.handleIndirect(endPoint, raw, i); 
        final boolean singleKey=(raw[i]==47 || raw[i]=='(' || raw[i]=='<');
        
        keyStart=i;
        j2=i;
        arrayData=raw;
        endPtr=-1;
        
        //single value ie /Filter /FlateDecode or (text)
        if(!singleKey && isIndirect && readIndirect(false, false, pdfObject)){ 
            return i;
        }
        
        //setup the correct array to size
        mixedArray=new ArrayList<byte[]>();
        
        scanElements(singleKey,pdfObject);
        
        pdfObject.setMixedArray(PDFkeyInt, mixedValues);
        
        //put cursor in correct place (already there if ref)
        if(!isIndirect) {
            i = j2;
        }
        
        if(debugFastCode) {
            showValues();
        }

        //roll back so loop works if no spaces
        if(i<rawLength &&(raw[i]==47 || raw[i]==62 || (raw[i]>='0' && raw[i]<='9'))) {
            i--;
        }

        return i;
    }

    private void scanElements(final boolean singleKey, final PdfObject pdfObject) {

        int currentElement = 0;
        
        findArrayStart();
        
        final int arrayEnd=arrayData.length;
        
        while (j2 < arrayEnd && arrayData[j2] != 93) {
        
            if(endPtr>-1 && j2>=endPtr) {
                break;
            }

            //move cursor to start of text
            while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47) {
                j2++;
            }
            
            if(arrayData[j2]=='%'){ //ignore % comments in middle of value
                while(j2<arrayData.length){
                    j2++;
                    if(arrayData[j2]==10){
                        break;
                    }
                }
                //move cursor to start of text
                while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47) {
                    j2++;
                }
            }
            
            keyStart=j2;

            final boolean isKey=arrayData[j2-1]=='/', isRef=ArrayUtils.isRef(arrayData, j2);
            
            byte[] newValues = null;
            
            if(arrayData[j2]=='e' && arrayData[j2+1]=='n' && arrayData[j2+2]=='d' && arrayData[j2+3]=='o' && arrayData[j2+4]=='b' && arrayData[j2+5]=='j'){
                break;
            }else if(arrayData[j2]=='>' && arrayData[j2-1]=='>'){
                break;
            }else if(isKey){                
                newValues=writeKey();
            }else if(!isRef && ArrayUtils.isNumber(arrayData, j2)){               
                newValues=writeNumber();
            }else if(isRef || (arrayData[j2]=='<' && arrayData[j2+1]=='<')){
                newValues = writeObject();
            }else if(ArrayUtils.isNull(arrayData,j2)){               
                newValues=writeNull();
            }else if(arrayData[j2]=='('){                                 
                newValues=writeString(pdfObject);
            }else if(arrayData[j2]=='['){                                
                newValues=writeArray();
            }else if(arrayData[j2+1]=='<' && arrayData[j2+2]=='<'){                                
                newValues = writeDirectDictionary();
            }else if(arrayData[j2]=='<'){
                newValues=writeHexString(pdfObject);
            }else{ 
                newValues = writeGeneral();
            }
           
            if (debugFastCode) {
                System.out.println(padding + "<Element -----" + currentElement + "( j2=" + j2 + " ) value=" + new String(newValues) + '<');
            }
            
            mixedArray.add(newValues);           
            
            currentElement++;
            
            if(singleKey){
                break;
            }
            
            j2=ArrayUtils.skipSpaces(arrayData,j2);
        }          
        
        fillArray(currentElement);
       
    }

    private byte[] writeGeneral() {
        
        //general value
        while(arrayData[j2]!=10 && arrayData[j2]!=13 && arrayData[j2]!=32 && arrayData[j2]!=93 && arrayData[j2]!=47){
            
            if(arrayData[j2]==62 && arrayData[j2+1]==62) { //end of direct object >>
                break;
            }
            
            if(arrayData[j2]==60 && arrayData[j2+1]==60) { //allow for number then object (ie 12<</)
                break;
            }
            
            j2++;
            
            if(j2==arrayData.length) {
                break;
            }
        }
        
        final byte[] newValues = ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
        if (arrayData[j2]=='>') {
            j2++;
        }
        
        return newValues;
    }

    private byte[] writeDirectDictionary() {
        
        //allow for straight into a <<>>
        j2++;
        if(debugFastCode){
            System.out.println(padding + "----double <<");
        }
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }

    private byte[] writeArray(){ // [59 0 R /XYZ null 711 null ]
        
        if(debugFastCode){
            System.out.println(padding + "----array");
        }
        
        keyStart=j2;
        while(arrayData[j2]!=']') {           
            j2++;    
        }
        
        //exclude end bracket
        j2++;
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }

    private byte[] writeString(final PdfObject pdfObject) {
        
        if(debugFastCode){
            System.out.println(padding + "----string");
        }
        
        keyStart=j2+1;
        while(true){
            if(arrayData[j2]==')' && !ObjectUtils.isEscaped(arrayData, j2)) {
                break;
            }
            
            j2++;
        }
        
        byte[] newValues = ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
            
        j2++;
        
        try {
            if (!pdfObject.isInCompressedStream() && decryption!=null) {
                newValues = decryption.decrypt(newValues, pdfObject.getObjectRefAsString(), false, null, false, false);
            }
        }catch (final PdfSecurityException e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        return newValues;
    }

    private byte[] writeObject() {
        
        if(debugFastCode){
            System.out.println(padding + "----ref or direct obj");
        }
        
        while(arrayData[j2]!='R' && arrayData[j2]!=']'){
            
            //allow for embedded object
            if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
                int levels=1;
                
                if(debugFastCode) {
                    System.out.println(padding + "Reading Direct value");
                }
                
                while(levels>0){
                    j2++;
                    
                    if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
                        j2++;
                        levels++;
                    }else if(arrayData[j2]=='>' && arrayData[j2+1]=='>'){
                        j2++;
                        levels--;
                    }
                }
                break;
            }
            
            j2++;
        }
        
        j2++;
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
    }

    private byte[] writeNumber() {
        
        if(debugFastCode){
            System.out.println(padding + "----number");
        }
        
        j2=ArrayUtils.skipSpaces(arrayData,j2);
        keyStart=j2;
        
        while(arrayData[j2]>='0' && arrayData[j2]<='9'){          
            j2++;
        }
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
    }

    private byte[] writeKey() {
        
        if(debugFastCode){
            System.out.println(padding + "----key");
        }
        
        keyStart=j2;
        j2=ArrayUtils.skipToEndOfKey(arrayData, j2+1);
        
        //include / so we can differentiate /9 and 9
        if(keyStart>0 && arrayData[keyStart-1]==47) {
            keyStart--;
        }
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }

    private void findArrayStart() {
        
        if(j2<0){
            j2=0;
        }
        
        //skip [ and any spaces allow for [[ in recursion
        boolean startFound=false;
        while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || (arrayData[j2]==91 && !startFound)){
            
            if(arrayData[j2]==91) {
                startFound = true;
            }
            
            j2++;
        }
        
        if (debugFastCode) {
            System.out.println(padding + "----scanElements j2="+j2+" chars="+ + arrayData[j2-1]+" "+ arrayData[j2]+" "+ arrayData[j2+1]);
        }
    }

    private byte[] writeNull() {
        if(debugFastCode){
            System.out.println(padding + "----null");
        }
        keyStart=j2;
        j2=j2+4;
        
        return  ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }
    

    private byte[] writeHexString(final PdfObject pdfObject) {
        
        if(debugFastCode){
            System.out.println(padding + "----hex string");
        }
        
        boolean hexString =true;
        
        keyStart=j2+1;
        
        while(true){
            if(arrayData[j2]=='>') {
                break;
            }
            
            if(arrayData[j2]=='/') {
                hexString = false;
            }
            
            j2++;
            
        }
        
        byte[] newValues = ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
        if(hexString){
            newValues =ArrayUtils.handleHexString(newValues, decryption, pdfObject.getObjectRefAsString());
        }
        
        j2++;
        
        return newValues;
    }

    
    private void fillArray(final int elementCount) {
        
        mixedValues = new byte[elementCount][];
        for(int a=0;a<elementCount;a++){
            mixedValues[a]=mixedArray.get(a);
        }
               
    }

    /**
     * used for debugging
     */
    private void showValues() {

        final StringBuilder values = new StringBuilder("[");

        for (final byte[] mixedValue : mixedValues) {
            if (mixedValue == null) {
                values.append("null ");
            } else {
                values.append(new String(mixedValue)).append(' ');
            }
        }

        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }
}
