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
 * SimpleArray.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.StringUtils;

/**
 * parse PDF array data from PDF for float, int, double, string and boolean values
 */
public class SimpleArray extends BaseArray implements ArrayDecoder{

    //now create array and read values
    private float[] floatValues;
    private int[] intValues;
    private double[] doubleValues;
    
    private byte[][] stringValues;
    private boolean[] booleanValues;
    
    public SimpleArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type,byte[] raw) {
        super(pdfFileReader, i, endPoint, type, raw);  
    }

    public SimpleArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final Object[] objectValuesArray, final int keyReached,byte[] raw) {
        super(pdfFileReader,i, endPoint, type, objectValuesArray, keyReached, raw);
    }
    

    @Override
    public int readArray(final boolean ignoreRecursion, final PdfObject pdfObject, final int PDFkeyInt) {

        this.PDFkeyInt=PDFkeyInt;
        
        if(findStart()){ //will also exit if empty array [] 
            return i + 1;
        }
       
        if(debugFastCode) {
            System.out.println(padding + "Reading array type=" + PdfDictionary.showArrayType(type) + " into " + pdfObject + ' ' + (char) raw[i] + ' ' + (char) raw[i + 1] + ' ' + (char) raw[i + 2] + ' ' + (char) raw[i + 3] + ' ' + (char) raw[i + 4]);
        }
        
        keyStart=i;

        //work out if direct or read ref ( [values] or ref to [values])
        j2=i;
        arrayData=raw;

        //may need to add method to PdfObject is others as well as Mask (last test to  allow for /Contents null
        boolean isIndirect=raw[i]!=91 && raw[i]!='(' && (PDFkeyInt!=PdfDictionary.Mask  && raw[0]!=0) && !ArrayUtils.isNull(raw,i); //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler

        //check indirect and not [/DeviceN[/Cyan/Magenta/Yellow/Black]/DeviceCMYK 36 0 R]
        if(isIndirect) {
            isIndirect = ArrayUtils.handleIndirect(endPoint, raw, i);
        
            if(debugFastCode ) {
                System.out.println(padding + "Indirect ref");
            }
            
            if(isIndirect && readIndirect(false, true, pdfObject)){ 
                return i;
            }
        }

        isSingleDirectValue=false; //flag to show points to Single value (ie /FlateDecode)
        isSingleNull=true;
        endPtr=-1;
        
        endI=-1;//allow for jumping back to single value (ie /Contents 12 0 R )

        int elementCount=countElements();
        
        //setup the correct array to size
        initObjectArray(elementCount);

        setValues(pdfObject, PDFkeyInt, elementCount);

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

    private int countElements() {
        
        int elementCount = 0;
         
        if(j2<0) //avoid exception
        {
            j2 = 0;
        }
        
        //skip [ and any spaces allow for [[ in recursion
        boolean startFound=false;
        while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || (arrayData[j2]==91 && !startFound)){
            
            if(arrayData[j2]==91) {
                startFound = true;
            }
            
            j2++;
        }
        
        //count number of elements
        endPtr = j2;
        
        boolean charIsSpace,lastCharIsSpace=true ;
        final int arrayEnd=arrayData.length;
        if (debugFastCode) {
            System.out.println(padding + "----counting elements----arrayData[endPtr]=" + arrayData[endPtr] + " type=" + type);
        }
        while (endPtr < arrayEnd && arrayData[endPtr] != 93) {
            //allow for empty object with just endobject
            if(arrayData[j2]=='e' && arrayData[j2+1]=='n' && arrayData[j2+2]=='d' && arrayData[j2+3]=='o') {
                break;
            }
            
            //allow for embedded objects
            while (true) {
                if (arrayData[endPtr] == '<' && arrayData[endPtr + 1] == '<') {
                    int levels=1;
                    elementCount++;
                    if(debugFastCode) {
                        System.out.println(padding + "Direct value elementCount=" + elementCount);
                    }
                    while (levels>0) {
                        endPtr++;
                        if (arrayData[endPtr] == '<' && arrayData[endPtr + 1] == '<') {
                            endPtr++;
                            levels++;
                        } else if (arrayData[endPtr] == '>' && arrayData[endPtr - 1] == '>') {
                            endPtr++;
                            levels--;
                        }
                    }
                } else {
                    break;
                }
            }
            
            //allow for null
            if (ArrayUtils.isNull(arrayData, endPtr)) {
                
                    isSingleNull=true;
                    elementCount=1;
                    break;
            }else if ((isSingleDirectValue && ArrayUtils.isSpace(arrayData,endPtr)) ||
                    (endI!=-1 && endPtr > endI)){
                break;
            }
            
            //handle (string)
            switch (arrayData[endPtr]) {
                case '(':
                    elementCount++;
                    if(debugFastCode) {
                        System.out.println(padding + "string");
                    }
                    while (true) {
                        if (arrayData[endPtr] == ')' && !ObjectUtils.isEscaped(arrayData, endPtr)) {
                            break;
                        }
                        endPtr++;
                        lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
                    }
                    break;
                    
                case '<':
                    elementCount++;
                    if(debugFastCode) {
                        System.out.println(padding + "direct");
                    }
                    while (true) {
                        if (arrayData[endPtr] == '>') {
                            break;
                        }
                        endPtr++;
                        lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
                    }
                    break;
                
                default:
                    charIsSpace = arrayData[endPtr] == 10 || arrayData[endPtr] == 13 || arrayData[endPtr] == 32 || arrayData[endPtr] == 47;
                    
                    if(lastCharIsSpace && !charIsSpace ){
                            elementCount++;
                    }
                    
                    lastCharIsSpace=charIsSpace;
                    break;
            }
            
            //allow for empty array [ ]
            if (endPtr < arrayEnd && arrayData[endPtr] == 93) {
                //get first char
                int ptr=ArrayUtils.skipSpaces(arrayData, endPtr - 1);
                if(arrayData[ptr]=='[') //if empty reset
                {
                    elementCount = 0;
                }
                break;
            }
            endPtr++;
        }
        
        if(debugFastCode) {               
            if(elementCount==0) {
                System.out.println(padding + "zero elements found!!!!!!");
            }else{
                System.out.println(padding + "Number of elements=" + elementCount + " rawCount=");
            }
        }
        
        return elementCount;
       
    }

    static int skipThroughRecursiveLevels(byte[] arrayData, int endPtr) {
        int level=1;

        while(true){

            endPtr++;

            if(endPtr==arrayData.length) {
                break;
            }

            if(arrayData[endPtr]==93) {
                level--;
            } else if(arrayData[endPtr]==91) {
                level++;
            }

            if(level==0) {
                break;
            }
        }
        return endPtr;
    }

    private void setValues(PdfObject pdfObject, int PDFkeyInt, int elementCount) {

        if(isSingleNull && ArrayUtils.isNull(arrayData,j2)){

            j2 += 3;

            switch (type) {
                
                case PdfDictionary.VALUE_IS_STRING_ARRAY:
                stringValues[0] = null;
                    break;
            }

        }else {
            j2 = setValue(raw, pdfObject, PDFkeyInt, elementCount, j2, arrayData, endPtr);
        }

        //set value in PdfObject
        switch (type) {
            case PdfDictionary.VALUE_IS_FLOAT_ARRAY:
                pdfObject.setFloatArray(PDFkeyInt, floatValues);
                break;

            case PdfDictionary.VALUE_IS_INT_ARRAY:
                pdfObject.setIntArray(PDFkeyInt, intValues);
                break;

            case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:
                pdfObject.setBooleanArray(PDFkeyInt, booleanValues);
                break;

            case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:
                pdfObject.setDoubleArray(PDFkeyInt, doubleValues);
                break;

            case PdfDictionary.VALUE_IS_STRING_ARRAY:
                pdfObject.setStringArray(PDFkeyInt, stringValues);
                break;

        }
    }

    private int setValue(final byte[] raw, final PdfObject pdfObject, final int PDFkeyInt, final int elementCount, int j2, final byte[] arrayData, final int endPtr) {

        int currentElement=0;
        
        while(arrayData[j2]!=93){

            boolean hexString = false;

            if(endPtr>-1 && j2>=endPtr) {
                break;
            }

            //move cursor to start of text
            while(arrayData[j2]==47 || ArrayUtils.isSpace(arrayData, j2)) {
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
                while(arrayData[j2]==47 || ArrayUtils.isSpace(arrayData, j2)) {
                    j2++;
                }
            }

            keyStart=j2;

            if(debugFastCode) {
                System.out.print("j2=" + j2 + " value=" + (char) arrayData[j2]);
            }

            // handle (string)
            if(arrayData[j2]=='('){

                keyStart=j2+1;
                while(true){
                    if(arrayData[j2]==')' && !ObjectUtils.isEscaped(arrayData, j2)) {
                        break;
                    }

                    j2++;
                }

                hexString =false;

            }else if(arrayData[j2+1]=='<' && arrayData[j2+2]=='<'){ //allow for straight into a <<>>
                j2++;

            }else if(arrayData[j2]=='<'){

                hexString =true;
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

            }else{ //general value ie 14
                while(arrayData[j2]!=10 && arrayData[j2]!=13 && arrayData[j2]!=32 && arrayData[j2]!=93 && arrayData[j2]!=47){

                    if(arrayData[j2]==62 && arrayData[j2+1]==62) { //end of direct object >>
                        break;
                    }

                    j2++;

                    if(j2==arrayData.length) {
                        break;
                    }
                }
            }
          
            //actual value or first part of ref
            switch (type) {
                case PdfDictionary.VALUE_IS_FLOAT_ARRAY:
                    floatValues[currentElement] = NumberUtils.parseFloat(keyStart, j2, arrayData);
                    break;

                case PdfDictionary.VALUE_IS_INT_ARRAY:
                    intValues[currentElement] = NumberUtils.parseInt(keyStart, j2, arrayData);
                    break;

                case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:
                        booleanValues[currentElement] = raw[keyStart] == 't' && raw[keyStart + 1] == 'r' && raw[keyStart + 2] == 'u' && raw[keyStart + 3] == 'e';
                    break;

                case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:
                    doubleValues[currentElement] = NumberUtils.parseFloat(keyStart, j2, arrayData);
                    break;

                default:
                    j2 = setStringArrayValue(pdfObject, PDFkeyInt, hexString, currentElement, elementCount, j2, arrayData, keyStart);
            }

            currentElement++;

            if(debugFastCode) {
                System.out.println(padding + "roll onto ==================================>" + currentElement + '/' + elementCount);
            }
            if(currentElement==elementCount) {
                break;
            }
        }
        return j2;
    }

    private int setStringArrayValue(final PdfObject pdfObject, final int PDFkeyInt, final boolean hexString,  final int currentElement, final int elementCount, int j2, final byte[] arrayData, int keyStart) {

        final boolean isID=PDFkeyInt == PdfDictionary.ID; //needs special treatment
        
        //include / so we can differentiate /9 and 9
        if(keyStart>0 && arrayData[keyStart-1]==47) {
            keyStart--;
        }

        //lose any spurious [
        if(keyStart>0 && arrayData[keyStart]=='[' && PDFkeyInt!= PdfDictionary.Names && PDFkeyInt!= PdfDictionary.ID) {
            keyStart++;
        }

        //lose any nulls
        if(PDFkeyInt==PdfDictionary.Order || PDFkeyInt==PdfDictionary.Layer){

            while(arrayData[keyStart]=='n' && arrayData[keyStart+1]=='u' && arrayData[keyStart+2]=='l' && arrayData[keyStart+3]=='l' ){
                keyStart += 4;

                //lose any spurious chars at start
                while(keyStart>=0 && (arrayData[keyStart]==' ' || arrayData[keyStart]==10 || arrayData[keyStart]==13 || arrayData[keyStart]==9)) {
                    keyStart++;
                }
            }

        }

        //lose any spurious chars at start
        while(keyStart>=0 && (arrayData[keyStart]==10 || arrayData[keyStart]==13 || arrayData[keyStart]==9)) {
            keyStart++;
        }

        byte[] newValues;

        //@mark 23155 (locked down to this case)
        if(decryption!= null && !isID && !pdfObject.isInCompressedStream() && pdfObject.getObjectType() == PdfDictionary.Page && arrayData[j2]=='<' && arrayData[j2+1]=='<'){
        
            newValues = ObjectUtils.readRawValue(j2, arrayData, keyStart);
        }else{
            newValues = ObjectUtils.readEscapedValue(j2, arrayData, keyStart, isID);
        }

        if(debugFastCode) {
            System.out.println(padding + "<Element -----" + currentElement + '/' + elementCount + "( j2=" + j2 + " ) value=" + new String(newValues) + '<');
        }

        if(j2==arrayData.length){
            //ignore
        }else if(arrayData[j2]=='>'){
            j2++;
            //roll past ) and decrypt if needed
        }else if(arrayData[j2]==')'){
            j2++;

            try {
                if(!isID && !pdfObject.isInCompressedStream() && decryption!=null) {
                    newValues = decryption.decrypt(newValues, pdfObject.getObjectRefAsString(), false, null, false, false);
                }
            } catch (final PdfSecurityException e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }

            //convert Strings in Order now
            if(PDFkeyInt==PdfDictionary.Order) {
                newValues = StringUtils.toBytes(StringUtils.getTextString(newValues, false));
            }
        }

        if(hexString){
            stringValues[currentElement] = ArrayUtils.handleHexString(newValues, decryption, pdfObject.getObjectRefAsString());
        }else{
            stringValues[currentElement]=newValues;
        }   
               
        return j2;
    }

    private void initObjectArray(final int elementCount) {
        
        switch (type) {
            case PdfDictionary.VALUE_IS_FLOAT_ARRAY:
                floatValues = new float[elementCount];
                break;

            case PdfDictionary.VALUE_IS_INT_ARRAY:
                intValues = new int[elementCount];
                break;

            case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:
                booleanValues = new boolean[elementCount];
                break;

            case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:
                doubleValues = new double[elementCount];
                break;

            case PdfDictionary.VALUE_IS_STRING_ARRAY:
                stringValues = new byte[elementCount][];
                break;

        }
    }


    /**
     * used for debugging
     */
    private void showValues() {

        final StringBuilder values = new StringBuilder("[");

        switch (type) {
            case PdfDictionary.VALUE_IS_FLOAT_ARRAY:
                for (final float floatValue : floatValues) {
                    values.append(floatValue).append(' ');
                }
                break;

            case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:
                for (final double doubleValue : doubleValues) {
                    values.append(doubleValue).append(' ');
                }
                break;

            case PdfDictionary.VALUE_IS_INT_ARRAY:
                for (final int intValue : intValues) {
                    values.append(intValue).append(' ');
                }
                break;

            case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:
                for (final boolean booleanValue : booleanValues) {
                    values.append(booleanValue).append(' ');
                }
                break;

            case PdfDictionary.VALUE_IS_STRING_ARRAY:
                for (final byte[] stringValue : stringValues) {
                    if (stringValue == null) {
                        values.append("null ");
                    } else {
                        values.append(new String(stringValue)).append(' ');
                    }
                }
                break;
        }

        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }
}
