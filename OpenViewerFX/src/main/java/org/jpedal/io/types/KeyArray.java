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
 * KeyArray.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

/**
 * parse PDF array data from PDF
 */
public class KeyArray extends BaseArray implements ArrayDecoder{

    //now create array and read values
    private byte[][] keyValues;
    
    public KeyArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type,byte[] raw) {
        super(pdfFileReader, i, endPoint, type,raw);  
    }

    public KeyArray(final PdfFileReader pdfFileReader, final int i, final int endPoint, final int type, final Object[] objectValuesArray, final int keyReached,byte[] raw) {
        super(pdfFileReader,i, endPoint, type, objectValuesArray, keyReached,raw);
    }
    
    @Override
    public int readArray(final boolean ignoreRecursion,final PdfObject pdfObject, final int PDFkeyInt) {

        this.PDFkeyInt=PDFkeyInt;
        
        if(findStart()){ //will also exit if empty array [] 
            return i + 1;
        }
        
        final boolean alwaysRead =(PDFkeyInt==PdfDictionary.Kids || PDFkeyInt==PdfDictionary.Annots);

        keyStart=i;

        //work out if direct or read ref ( [values] or ref to [values])
        j2=i;
        arrayData=raw;

        boolean isIndirect=raw[i]!=91 && raw[i]!='(' &&  PDFkeyInt!=PdfDictionary.TR && raw[0]!=0; //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler

        // allow for /Contents null
        if(ArrayUtils.isNull(raw, i)){
            isIndirect=false;
        }

        //check indirect and not [/DeviceN[/Cyan/Magenta/Yellow/Black]/DeviceCMYK 36 0 R]
        if(isIndirect) {
            isIndirect = ArrayUtils.handleIndirect(endPoint, raw, i);
        
            if(debugFastCode) {
                System.out.println(padding + "Indirect ref");
            }
        }

        boolean singleKey=false;
        isSingleDirectValue=false; //flag to show points to Single value (ie /FlateDecode)
        isSingleNull=true;
        endPtr=-1;
        
        int elementCount=1;
        

        if((raw[i]==47 || raw[i]=='(' || raw[i]=='<' || (raw[i]=='<' && raw[i+1]=='f' && raw[i+2]=='e') && raw[i+3]=='f' && raw[i+4]=='f') && PDFkeyInt!=PdfDictionary.TR){ //single value ie /Filter /FlateDecode or (text)

            singleKey=true;

            if(debugFastCode) {
                System.out.println(padding + "Direct single value with /");
            }
        }else{

            endI=-1;//allow for jumping back to single value (ie /Contents 12 0 R )

            if(isIndirect && readIndirect(ignoreRecursion, alwaysRead, pdfObject)){ 
                return i;
            }

            elementCount=countElements(PDFkeyInt);

            
        }

        if(ignoreRecursion && !alwaysRead) {
            return endPtr;
        }

        //setup the correct array to size
        keyValues = new byte[elementCount][];

        if(isSingleNull && ArrayUtils.isNull(arrayData,j2)){

            j2 += 3;

            keyValues[0] = null;
            
        }else {
            j2 = setValue(pdfObject, PDFkeyInt, elementCount, j2, arrayData, endPtr);
            
            //update pointer if needed
            if(singleKey) {
                i = j2;
            }
        }

        //put cursor in correct place (already there if ref)
        if(!isIndirect) {
            i = j2;
        }

        //set value in PdfObject
        setKeyArrayValue(pdfObject, PDFkeyInt, elementCount);

        if(debugFastCode) {
            showValues();
        }

        //roll back so loop works if no spaces
        if(i<rawLength &&(raw[i]==47 || raw[i]==62 || (raw[i]>='0' && raw[i]<='9'))) {
            i--;
        }

        return i;
    }

    private int countElements(final int PDFkeyInt) {
        
        int elementCount=0;
        
        if(j2<0){ //avoid exception
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
        endPtr=j2;
        final int arrayEnd=arrayData.length;
        
        if(debugFastCode) {
            System.out.println(padding + "----counting elements----arrayData[endPtr]=" + arrayData[endPtr] + " type=" + type);
        }
        
        while (endPtr<arrayEnd && arrayData[endPtr]!=93) {
            //allow for empty object with just endobject
            if(arrayData[j2]=='e' && arrayData[j2+1]=='n' && arrayData[j2+2]=='d' && arrayData[j2+3]=='o') {
                break;
            }
            //allow for embedded objects
            while(true){
                
                if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
                    int levels=1;
                    
                    elementCount++;
                    
                    if(debugFastCode) {
                        System.out.println(padding + "Direct value elementCount=" + elementCount);
                    }
                    
                    while(levels>0){
                        endPtr++;
                        
                        if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
                            endPtr++;
                            levels++;
                        }else if(arrayData[endPtr]=='>' && arrayData[endPtr-1]=='>'){
                            endPtr++;
                            levels--;
                        }
                    }
                    
                    endPtr--;
                    
                }else {
                    break;
                }
            }
            //allow for null (not Mixed!)
            if (ArrayUtils.isNull(arrayData,endPtr)) {
                //get next legit value and make sure not only value if layer or Order
                //to handle bum null values in Layers on some files
                byte nextChar=93;
                if (PDFkeyInt == PdfDictionary.Layer || PDFkeyInt == PdfDictionary.Order) {
                    for(int aa=endPtr+3;aa<arrayData.length;aa++){
                        if(arrayData[aa]==10 || arrayData[aa]==13 || arrayData[aa]==32 || arrayData[aa]==9){
                        }else{
                            nextChar=arrayData[aa];
                            aa=arrayData.length;
                        }
                    }
                }
                
                if(nextChar==93){
                    isSingleNull=true;
                    elementCount=1;
                    break;
                }else{  //ignore null value
                    isSingleNull=false;
                    //elementCount++;
                    endPtr += 4;
                    
                    if(debugFastCode) {
                        System.out.println("null");
                    }
                    
                    continue;
                }
            }
            if(isSingleDirectValue && (arrayData[endPtr]==32 || arrayData[endPtr]==13 || arrayData[endPtr]==10)) {
                break;
            }
            if(endI!=-1 && endPtr>endI) {
                break;
            }
            if (arrayData[endPtr]=='R' || ((PDFkeyInt == PdfDictionary.TR || PDFkeyInt == PdfDictionary.Category) && arrayData[endPtr]=='/')) {
                elementCount++;
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

    private int setValue(final PdfObject pdfObject, final int PDFkeyInt, final int elementCount, int j2, final byte[] arrayData, final int endPtr) {

        int keyStart;///read values

        int currentElement=0;
        
        while(arrayData[j2]!=93){

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

            if(debugFastCode) {
                System.out.print("j2=" + j2 + " value=" + (char) arrayData[j2]);
            }

            final boolean isKey=arrayData[j2-1]=='/';
            boolean isRecursiveValue=false; //flag to show if processed in top part so ignore second part

            //move cursor to end of text
            
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

            //actual value or first part of ref
            if(!isRecursiveValue) {
                j2 = setObjectArrayValue(pdfObject, PDFkeyInt,currentElement, elementCount, j2, arrayData, keyStart);
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

    private int setObjectArrayValue(final PdfObject pdfObject, final int PDFkeyInt,  final int currentElement, final int elementCount, int j2, final byte[] arrayData, int keyStart) {
        
        //include / so we can differentiate /9 and 9
        if(keyStart>0 && arrayData[keyStart-1]==47) {
            keyStart--;
        }

        //lose any spurious [
        if(keyStart>0 && arrayData[keyStart]=='[' && PDFkeyInt!= PdfDictionary.Names) {
            keyStart++;
        }

        //lose any nulls
        if(PDFkeyInt==PdfDictionary.Order || PDFkeyInt==PdfDictionary.Layer){


            while(ArrayUtils.isNull(arrayData,keyStart) ){
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

        keyValues[currentElement]= ObjectUtils.convertReturnsToSpaces(newValues);
        
        return j2;
    }

    private void setKeyArrayValue(final PdfObject pdfObject, final int PDFkeyInt, final int elementCount) {

        if(elementCount==1 && PDFkeyInt==PdfDictionary.Annots){//allow for indirect on Annots

            final byte[] objData=keyValues[0];

            //allow for null
            if(objData!=null){

                final int size=objData.length;
                if(objData[size-1]=='R'){

                    final PdfObject obj=new PdfObject(new String(objData));
                    final byte[] newData=objectReader.readObjectData(obj);

                    if(newData!=null){

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
                            }else if(newData[jj-1]=='<' && newData[jj]=='<'){
                                hasArray=false;
                                break;
                            }
                        }

                        if(hasArray){
                            final ArrayDecoder objDecoder=new KeyArray(objectReader, jj, newLen, PdfDictionary.VALUE_IS_KEY_ARRAY,newData);
                            objDecoder.readArray(false, pdfObject, PDFkeyInt);
                        }else {
                            pdfObject.setKeyArray(PDFkeyInt, keyValues);
                        }
                    }
                }
            }
        }else {
            pdfObject.setKeyArray(PDFkeyInt, keyValues);
        }
    }

    /**
     * used for debugging
     */
    private void showValues() {

        final StringBuilder values = new StringBuilder("[");

        for (final byte[] keyValue : keyValues) {
            if (keyValue == null) {
                values.append("null ");
            } else {
                values.append(new String(keyValue)).append(' ');
            }
        }
        
        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }  
}
