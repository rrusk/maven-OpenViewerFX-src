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
 * ObjectArray.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.OCObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

/**
 * parse PDF array data from PDF for mixed and object values
 */
public class ObjectArray extends BaseArray implements ArrayDecoder{

    private ArrayList<Object> objectArray;
    
    private Object[] objectValues;

    public ObjectArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type,byte[] raw) {
        super(pdfFileReader, i, endPoint, type, raw);  
    }

    public ObjectArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final Object[] objectValuesArray, final int keyReached,byte[] raw) {
        super(pdfFileReader,i, endPoint, type, objectValuesArray, keyReached, raw);
    }
    
    @Override
    public int readArray(final boolean ignoreRecursion, final PdfObject pdfObject, final int PDFkeyInt) {

        this.PDFkeyInt=PDFkeyInt;
        
        if(findStart()){ //will also exit if empty array [] 
            return i + 1;
        }
        
        final Map<Integer, String> isRef=new HashMap<Integer, String>();

        if(debugFastCode) {
            System.out.println(padding + "Reading array type=" + PdfDictionary.showArrayType(type) + " into " + pdfObject + ' ' + (char) raw[i] + ' ' + (char) raw[i + 1] + ' ' + (char) raw[i + 2] + ' ' + (char) raw[i + 3] + ' ' + (char) raw[i + 4]);
        }
        
        //may need to add method to PdfObject is others as well as Mask (last test to  allow for /Contents null
        boolean isIndirect=raw[i]!=91 && raw[i]!='(' && raw[0]!=0 && !ArrayUtils.isNull(raw,i); //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler

        //check indirect and not [/DeviceN[/Cyan/Magenta/Yellow/Black]/DeviceCMYK 36 0 R]
        if(isIndirect) {
            isIndirect = ArrayUtils.handleIndirect(endPoint, raw, i);
        
            if(debugFastCode ) {
                System.out.println(padding + "Indirect ref");
            }
        }

        keyStart=i;
        j2=i;
        arrayData=raw;
        isSingleDirectValue=false; //flag to show points to Single value (ie /FlateDecode)
        isSingleNull=true;
        endPtr=-1;
        
        int elementCount = 1;
        final boolean singleKey=(raw[i]==47 || raw[i]=='(' || raw[i]=='<');
        
        if(!singleKey){ //single value ie /Filter /FlateDecode or (text)

            endI=-1;//allow for jumping back to single value (ie /Contents 12 0 R )

            if(isIndirect && readIndirect(false, false, pdfObject)){ 
                return i;
            }
            
            elementCount=countElements(isRef);
        }
        
        //setup the correct array to size
        objectArray=new ArrayList<Object>();

        objectValues = new Object[elementCount];
        
        if(isSingleNull && ArrayUtils.isNull(arrayData,j2)){
            j2 += 3;
            objectValues[0] = null;
            objectArray.add(null);
        }else {
            scanElements(singleKey,pdfObject, isRef, elementCount);
        }
    
        setObjectArrayValue(pdfObject, objectValuesArray, keyReached);
        
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

    private int countElements( final Map<Integer, String> isRef) {
        
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
        
        boolean charIsSpace,lastCharIsSpace=true,isRecursive ;
        final int arrayEnd=arrayData.length;
        if (debugFastCode) {
            System.out.println(padding + "----counting elements----arrayData[endPtr]=" + arrayData[endPtr] + " type=" + type);
        }
        
        while (endPtr < arrayEnd && arrayData[endPtr] != 93) {
            //allow for empty object with just endobject
            if(arrayData[j2]=='e' && arrayData[j2+1]=='n' && arrayData[j2+2]=='d' && arrayData[j2+3]=='o') {
                break;
            }
            isRecursive=false;
            
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
            
            //allow for null (not Mixed!)
            if (ArrayUtils.isNull(arrayData, endPtr)) {
                //get next legit value and make sure not only value if layer or Order
                //to handle bum null values in Layers on some files
                byte nextChar=93;
                if (PDFkeyInt == PdfDictionary.Layer || PDFkeyInt == PdfDictionary.Order) {
                    for (int aa = endPtr + 3; aa<arrayData.length; aa++) {
                        if(arrayData[aa]==10 || arrayData[aa]==13 || arrayData[aa]==32 || arrayData[aa]==9){
                        }else{
                            nextChar=arrayData[aa];
                            aa=arrayData.length;
                        }
                    }
                }
                if (nextChar==93) {
                    isSingleNull=true;
                    elementCount=1;
                    break;
                } else {
                    //ignore null value
                    isSingleNull=false;
                    //elementCount++;
                    endPtr += 4;
                    lastCharIsSpace=true;
                    if(debugFastCode) {
                        System.out.println("ignore null");
                    }
                    continue;
                }
            }
            
            if (isSingleDirectValue && (arrayData[endPtr] == 32 || arrayData[endPtr] == 13 || arrayData[endPtr] == 10)) {
                break;
            }
            
            if (endI!=-1 && endPtr > endI) {
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
                case 91:
                    //handle recursion
                    
                    elementCount++;
                    if(debugFastCode) {
                        System.out.println(padding + "recursion");
                    }
                    endPtr = skipThroughRecursiveLevels(arrayData, endPtr);
                    isRecursive=true;
                    lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
                    break;
                default:
                    charIsSpace = arrayData[endPtr] == 10 || arrayData[endPtr] == 13 || arrayData[endPtr] == 32 || arrayData[endPtr] == 47;
                    elementCount = handleSpace(isRef, elementCount, arrayData, endPtr, charIsSpace, lastCharIsSpace);
                    lastCharIsSpace=charIsSpace;
                    break;
            }
            
            //allow for empty array [ ]
            if (!isRecursive && endPtr < arrayEnd && arrayData[endPtr] == 93) {
                //get first char
                int ptr = endPtr - 1;
                while(arrayData[ptr]==13 || arrayData[ptr]==10 || arrayData[ptr]==32) {
                    ptr--;
                }
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

    private int handleSpace(Map<Integer, String> isRef, int elementCount, byte[] arrayData, int endPtr, boolean charIsSpace, boolean lastCharIsSpace) {
        
        if(lastCharIsSpace && !charIsSpace ){
            if(arrayData[endPtr]=='R' && arrayData[endPtr-1]!='/'){ //adjust so returns correct count  /R and  on 12 0 R
                elementCount--;
                
                isRef.put(elementCount - 1,"x");

                if(debugFastCode) {
                    System.out.println(padding + "aref " + (char) arrayData[endPtr]+" elementCount="+elementCount);
                }
            }else {
                elementCount++;
            }
        }
        return elementCount;
    }

    private void scanElements(final boolean singleKey, final PdfObject pdfObject, final Map<Integer, String> ref, final int elementCount) {

        int currentElement = 0;
       
        final int arrayEnd=arrayData.length;
        
        if (debugFastCode) {
            System.out.println(padding + "----scanning elements----arrayData[endPtr]=" + arrayData[j2] + " type=" + type);
        }
       
        while (j2 < arrayEnd && arrayData[j2] != 93) {
        
            boolean hexString = false;

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

            final boolean isKey=arrayData[j2-1]=='/';
            boolean isRecursiveValue=false; //flag to show if processed in top part so ignore second part

            //move cursor to end of text
            if((ref.containsKey(currentElement)||
                            (PDFkeyInt==PdfDictionary.Order && arrayData[j2]>='0' && arrayData[j2]<='9')||
                            (arrayData[j2]=='<' && arrayData[j2+1]=='<'))){

                if(debugFastCode) {
                    System.out.println("ref currentElement=" + currentElement);
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

                    if(isKey && PDFkeyInt==PdfDictionary.TR && arrayData[j2+1]==' ') {
                        break;
                    }

                    j2++;
                }
                j2++;

            }else{

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

                }else if(arrayData[j2]==91){

                    writeValue(currentElement, pdfObject, PDFkeyInt);

                    if(debugFastCode){
                        final int len=padding.length();

                        if(len>3) {
                            padding = padding.substring(0, len - 3);
                        }
                    }

                    if(arrayData[j2]!='[') {
                        j2++;
                    }

                    isRecursiveValue=true;

                    while(j2<arrayData.length && arrayData[j2]==']') {
                        j2++;
                    }

                }else if(ArrayUtils.isNull(arrayData,j2)){
                    j2 += 4;
                    objectValues[currentElement]=null;
                    objectArray.add(null);
                    currentElement++;
                    continue;
                }else if(arrayData[j2]==']'){
                    break;
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
            }

            //actual value or first part of ref
            if(!isRecursiveValue) {
                saveArrayValue(pdfObject, hexString, currentElement);
            }

            currentElement++;

            if(debugFastCode) {
                System.out.println(padding + "roll onto ==================================>" + currentElement);
            }
            
            if(singleKey){
                break;
            }
        }   
    }

    private void writeValue(int currentElement, final PdfObject pdfObject, final int PDFkeyInt1) {
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
        j3++;
        if(debugFastCode) {
            padding += "   ";
        }
        final ArrayDecoder objDecoder=ArrayFactory.getDecoder(objectReader, j2, j3, type, objectValues, currentElement, arrayData);
        j2 = objDecoder.readArray(false, pdfObject, PDFkeyInt1);
    }

    private void saveArrayValue(final PdfObject pdfObject, final boolean hexString,  final int currentElement) {

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
        if(decryption!= null && !pdfObject.isInCompressedStream() && pdfObject.getObjectType() == PdfDictionary.Page && arrayData[j2]=='<' && arrayData[j2+1]=='<'){
        
            newValues = ObjectUtils.readRawValue(j2, arrayData, keyStart);

            if (newValues[0] == '<' && newValues[1] == '<') {
                //see case 23155 (encrypted annot needs obj ref appended so we can decrypt string later)
                newValues = appendObjectRef(pdfObject, newValues);
            }
        }else{
            newValues = ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        }

        if(debugFastCode) {
            System.out.println(padding + "<Element -----" + currentElement + "( j2=" + j2 + " ) value=" + new String(newValues) + '<');
        }

        if(j2==arrayData.length){
            //ignore
        }else if(arrayData[j2]=='>'){
            j2++;
            //roll past ) and decrypt if needed
        }else if(arrayData[j2]==')'){
            j2++;

            try {
                if(!pdfObject.isInCompressedStream() && decryption!=null) {
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
            objectArray.add(ArrayUtils.handleHexString(newValues, decryption, pdfObject.getObjectRefAsString()));
        }else{
            objectArray.add(newValues);
        } 

        if(hexString){
            objectValues[currentElement] = ArrayUtils.handleHexString(newValues, decryption, pdfObject.getObjectRefAsString());
        }else{
            objectValues[currentElement]=newValues;
        }
               
    }
    
    private void setObjectArrayValue(final PdfObject pdfObject, final Object[] objectValuesArray, final int keyReached) {
        
        //allow for indirect order
        if(PDFkeyInt== PdfDictionary.Order && objectValues!=null && objectValues.length==1 && objectValues[0] instanceof byte[]){

            final byte[] objData=(byte[]) objectValues[0];
            final int size=objData.length;
            if(objData[size-1]=='R'){

                final PdfObject obj=new OCObject(new String(objData));
                final byte[] newData=objectReader.readObjectData(obj);

                int jj=0;
                final int newLen=newData.length;
                boolean hasArray=false;
                while(jj<newLen){
                    jj++;

                    if(jj==newData.length) {
                        break;
                    }

                    if(newData[jj]=='['){
                        hasArray=true;
                        break;
                    }
                }

                if(hasArray){
                    final ArrayDecoder objDecoder=ArrayFactory.getDecoder(objectReader, jj, newLen, PdfDictionary.VALUE_IS_OBJECT_ARRAY, newData);
                    objDecoder.readArray(false, pdfObject, PDFkeyInt);
                }
                objectValues=null;

            }
        }

        if(objectValuesArray!=null){
            objectValuesArray[keyReached]=objectValues;

            
            if(debugFastCode) {
                System.out.println(padding + "set Object objectValuesArray[" + keyReached + "]=" + Arrays.toString(objectValues));
            }

        }else if(objectValues!=null){
            pdfObject.setObjectArray(PDFkeyInt,objectValues);

            if(debugFastCode) {
                System.out.println(padding + PDFkeyInt + " set Object value=" + Arrays.toString(objectValues));
            }
        }
    }

    /**
     * used for debugging
     */
    private void showValues() {

        final StringBuilder values = new StringBuilder("[");

        values.append(ObjectUtils.showMixedValuesAsString(objectValues, ""));
        
        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }
}
