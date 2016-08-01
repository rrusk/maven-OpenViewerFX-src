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
 * Array.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.ArrayList;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.ObjectDecoder;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 * parse PDF array data from PDF for mixed and object values
 */
public class Array extends ObjectDecoder implements ArrayDecoder{

    final ArrayList<byte[]> valuesRead=new ArrayList<byte[]>();
    
    int i, j2;
    int type;

    final byte[] raw;
    byte[]  arrayData;

    int PDFkeyInt;

    int rawLength;
   
    public Array(final PdfFileReader pdfFileReader, final int i, final int type, final byte[] raw) {
        super(pdfFileReader);

        this.i = i;
        this.type = type;
        this.raw = raw;

        if(raw!=null){
            rawLength=raw.length;
        }
    }

    private boolean findStart() {

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

    private boolean readIndirect(final PdfObject pdfObject) throws RuntimeException {

        //allow for indirect to 1 item
        final int startI = i;

        if (debugFastCode) {
            System.out.print(padding + "Indirect object ref=");
        }

        //move cursor to end of ref
        i = ArrayUtils.skipToEndOfRef(i, raw);
        //actual value or first part of ref
        final int ref = NumberUtils.parseInt(startI, i, raw);
        //move cursor to start of generation
        while (raw[i] == 10 || raw[i] == 13 || raw[i] == 32 || raw[i] == 47 || raw[i] == 60) {
            i++;
        }
        
        // get generation number
        int keyStart = i;
        
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
                break;
            }
            
            if (arrayData[j2] == '<' && arrayData[j2 + 1] == '<') { //also check ahead to pick up [<<
               
                j2 = startI;
                arrayData = raw;

                if (debugFastCode) {
                    System.out.println(padding + "Single value, not indirect "+pdfObject.getObjectRefAsString());
                }

                break;
            }

            j2++;
        }

        return false;
    }
 
    
    @Override
    public int readArray(final PdfObject pdfObject, final int PDFkeyInt) {

        
        if(findStart()){ //will also exit if empty array [] 
            return i + 1;
        }
        
        this.PDFkeyInt=PDFkeyInt;
        j2=i;
        arrayData=raw;
       
        if(debugFastCode) {
            System.out.println(padding + "Reading array type=" + PdfDictionary.showArrayType(type) + " into " + pdfObject + ' ' + (char) raw[i] + ' ' + (char) raw[i + 1] + ' ' + (char) raw[i + 2] + ' ' + (char) raw[i + 3] + ' ' + (char) raw[i + 4]);
        }
        
        //may need to add method to PdfObject is others as well as Mask (last test to  allow for /Contents null
        //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler
        boolean isIndirect=raw[i]!=91 && raw[i]!='(' && raw[0]!=0 && !ArrayUtils.isNull(raw,i) && ArrayUtils.handleIndirect(raw, i); 
        boolean singleKey=isFirstKeySingle();
         
        //single value ie /Filter /FlateDecode or (text)
        if((type==PdfDictionary.VALUE_IS_OBJECT_ARRAY || !singleKey) && isIndirect){ 
            readIndirect(pdfObject);
            singleKey=isFirstKeySingle();
        }
        
        scanElements(singleKey, pdfObject);
        
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

    void scanElements(boolean singleKey,final PdfObject pdfObject) {

        singleKey=isSingleKey();
        
        findArrayStart();
        
        final int arrayEnd=arrayData.length;
        int keyStart=moveToStartOfNextValue(),currentElement = 0;
        byte[] newValues;
        
        while (j2 < arrayEnd && arrayData[j2] != 93) {
        
            if(ArrayUtils.isEndObj(arrayData,j2)){
                break;
            }else if(arrayData[j2]=='>' && arrayData[j2+1]=='>'){
                break;
            }else if(arrayData[j2-1]=='/'){  //isKey               
                newValues=writeKey();
            }else if(ArrayUtils.isRef(arrayData, j2) || (arrayData[j2]=='<' && arrayData[j2+1]=='<')){
                newValues = writeObject(keyStart);
            }else if(ArrayUtils.isNumber(arrayData, j2)){               
                newValues=writeNumber();
            }else if(ArrayUtils.isNull(arrayData,j2)){               
                newValues=writeNull();
            }else if(arrayData[j2]=='('){                                 
                newValues=writeString(pdfObject);
            }else if(arrayData[j2]=='['){                                
                newValues=writeArray();
            }else if(arrayData[j2+1]=='<' && arrayData[j2+2]=='<'){                                
                newValues = writeDirectDictionary(keyStart);
            }else if(arrayData[j2]=='<'){
                newValues=writeHexString(pdfObject);
            }else{ 
                newValues = writeGeneral(keyStart);
            }
           
            if (debugFastCode) {
                System.out.println(padding + "<Element -----" + currentElement + "( j2=" + j2 + " ) value=" + new String(newValues) + '<');
            }
            
            valuesRead.add(newValues);           
            
            currentElement++;
            
            if(singleKey){
                break;
            }
            
            keyStart=moveToStartOfNextValue();

        }          
        
        fillArray(currentElement, pdfObject);
       
    }

    int moveToStartOfNextValue() {
        
        final int size=arrayData.length;
        
        //move cursor to start of text
        while(j2<size &&(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47 || arrayData[j2]==9)) {
            j2++;
        }
        
        while(j2<size && arrayData[j2]=='%'){ //ignore % comments in middle of value
            while(j2<size && arrayData[j2]!=10){
                j2++;
            }
            
            j2=ArrayUtils.skipSpaces(arrayData, j2);
            
        } 
        
        //move cursor to start of text
        while(j2<size &&(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47 || arrayData[j2]==9)) {
            j2++;
        }
        
        return j2;
    }

    byte[] writeGeneral(final int keyStart) {
        
        //general value
        while(arrayData[j2]!=10 && arrayData[j2]!=13 && arrayData[j2]!=32 && arrayData[j2]!=93 && arrayData[j2]!=47  && arrayData[j2]!='['){
            
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

    byte[] writeDirectDictionary(final int keyStart) {
        
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
        
        int keyStart=j2;
        while(arrayData[j2]!=']') {           
            j2++;    
        }
        
        //exclude end bracket
        j2++;
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }

    byte[] writeString(final PdfObject pdfObject) {
        
        if(debugFastCode){
            System.out.println(padding + "----string");
        }
        
        int keyStart=j2+1;
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

    byte[] writeObject(final int keyStart) {
        
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

    byte[] writeNumber() {
        
        if(debugFastCode){
            System.out.println(padding + "----number");
        }
        
        j2=ArrayUtils.skipSpaces(arrayData,j2);
        int keyStart=j2;
        
        while(arrayData[j2]>='0' && arrayData[j2]<='9'){          
            j2++;
        }
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
    }

    byte[] writeKey() {
        
        if(debugFastCode){
            System.out.println(padding + "----key");
        }
        
        int keyStart=j2;
        j2=ArrayUtils.skipToEndOfKey(arrayData, j2+1);
        
        //include / so we can differentiate /9 and 9
        if(keyStart>0 && arrayData[keyStart-1]==47) {
            keyStart--;
        }
        
        return ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }

    void findArrayStart() {
        
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

    byte[] writeNull() {
        if(debugFastCode){
            System.out.println(padding + "----null");
        }
        int keyStart=j2;
        j2 += 4;
        
        return  ObjectUtils.readEscapedValue(j2, arrayData, keyStart, false);
        
    }
    

    byte[] writeHexString(final PdfObject pdfObject) {
        
        if(debugFastCode){
            System.out.println(padding + "----hex string");
        }
        
        boolean hexString =true;
        
        int keyStart=j2+1;
        
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

    
    void fillArray(final int elementCount, PdfObject pdfObject) {
        
        byte[][] finalByteValues = new byte[elementCount][];
        for(int a=0;a<elementCount;a++){
            finalByteValues[a]=valuesRead.get(a);
        }
        
        pdfObject.setMixedArray(PDFkeyInt, finalByteValues);
        
               
    }

    /**
     * used for debugging
     */
    void showValues() {

        final StringBuilder values = new StringBuilder("[");

        for (final byte[] value : valuesRead) {
            if (value == null) {
                values.append("null ");
            } else {
                values.append(new String(value)).append(' ');
            }
        }

        values.append(" ]");

        System.out.println(padding + "values=" + values);
    }

    boolean isSingleKey() {
         return (raw[i]==47 || raw[i]=='(' || raw[i]=='<');
    }
    
    boolean isFirstKeySingle() {
         return (raw[i]==47 || raw[i]=='(' || raw[i]=='<');
    }
}
