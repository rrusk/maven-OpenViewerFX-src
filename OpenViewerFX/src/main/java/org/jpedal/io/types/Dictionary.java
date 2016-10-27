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
 * Dictionary.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.ArrayList;
import org.jpedal.io.ObjectDecoder;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import static org.jpedal.io.ObjectDecoder.resolveFully;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class Dictionary {

    public static int readDictionary(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final boolean ignoreRecursion, final PdfFileReader objectReader) {
        
        //roll on
        if(raw[i]!='<') {
            i++;
        }
        
        i=StreamReaderUtils.skipSpaces(raw, i);
        
        //some objects can have a common value (ie /ToUnicode /Identity-H
        if(raw[i]==47){
            
            i = readKey(pdfObject, i+1, raw, PDFkeyInt, objectReader);

        }else if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){ //allow for empty object
                
                if(debugFastCode) {
                    System.out.println(padding + "Empty object" + new String(raw) + "<<");
                }
        
        }else if(!ignoreRecursion){
            i = Dictionary.readDictionaryFromRefOrDirect(pdfObject,pdfObject.getObjectRefAsString(), i, raw, PDFkeyInt,objectReader);
        }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop
                
            i = readRef(pdfObject, i, raw, PDFkeyInt, objectReader);
        }
        return i;
    }

    static int readRef(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

            if(debugFastCode) {
                System.out.println(padding + "1.About to read ref orDirect i=" + i + " char=" + (char) raw[i]);
            }

            i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);
            
            i = Dictionary.readDictionaryFromRefOrDirect(pdfObject,pdfObject.getObjectRefAsString(), i, raw, PDFkeyInt,objectReader);
 

        return i;
    }

    static int readKey(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        final int keyStart=i;

        i= StreamReaderUtils.skipToEndOfRef(raw, i);

        final PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt,pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);

        //store value
        valueObj.setConstant(PDFkeyInt,keyStart,i-keyStart,raw);

        valueObj.setGeneralStringValue(new String(getByteKeyFromStream(i-keyStart, raw, keyStart)));

        if(debugFastCode) {
            System.out.println(padding + "Set Dictionary as String=" + valueObj.getGeneralStringValue() + "  in " + pdfObject + " to " + valueObj);
        }

        //store value
        pdfObject.setDictionary(PDFkeyInt,valueObj);

        if(pdfObject.isDataExternal()){
            valueObj.isDataExternal(true);
            if(!resolveFully(valueObj,objectReader)) {
                pdfObject.setFullyResolved(false);
            }
        }

        return i;
    }
    
    public static int getPairedValues(final PdfObject pdfObject, final int i, final byte[] raw, final int pdfKeyType, final int length, final int keyLength, final int keyStart) {
        
        boolean isPair=false;
        
        int jj=i;
        
        while(jj<length){
            
            jj=StreamReaderUtils.skipSpaces(raw, jj);
            
            //number (possibly reference)
            if(jj<length && raw[jj]>='0' && raw[jj]<='9'){
                
                //rest of ref
                while(jj<length && raw[jj]>='0' && raw[jj]<='9') {
                    jj++;
                }
                
                jj=StreamReaderUtils.skipSpaces(raw, jj);
        
                //generation and spaces
                while(jj<length && ((raw[jj]>='0' && raw[jj]<='9')||(raw[jj]==32 || raw[jj]==10 || raw[jj]==13))) {
                    jj++;
                }
                
                //not a ref
                if(jj>=length || raw[jj]!='R') {
                    break;
                }
                
                //roll past R
                jj++;
            }
            
            jj=StreamReaderUtils.skipSpaces(raw, jj);
                        
            
            //must be next key or end
            if(raw[jj]=='>' && raw[jj+1]=='>'){
                isPair=true;
                break;
            }else if(raw[jj]!='/') {
                break;
            }
            
            jj++;
            
            //ignore any spaces
            while(jj<length && (raw[jj]!=32 && raw[jj]!=13 && raw[jj]!=10)) {
                jj++;
            }
            
        }
        
        if(isPair){
            pdfObject.setCurrentKey(PdfDictionary.getKey(keyStart,keyLength,raw));
            return PdfDictionary.VALUE_IS_UNREAD_DICTIONARY;
        }else {
            return pdfKeyType;
        }
    }

    public static int setDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final int length, final boolean ignoreRecursion, final PdfFileReader objectReader, final int PDFkeyInt) {
        
        if(debugFastCode) {
            System.out.println(padding + ">>>Reading Dictionary Pairs i=" + i + ' ' + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4] + (char) raw[i + 5] + (char) raw[i + 6]);
        }
        
        i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 47);
        
        //set data which will be switched below if ref
        byte[] data=raw;
        int j=i;
        
        //get next key to see if indirect
        final boolean isRef=data[j]!='<';
        
        if(isRef){
            
            //number
            final int[] values = StreamReaderUtils.readRefFromStream(raw, i);
            final int number = values[0];
            final int generation = values[1];
            i = values[2];
            
            if(!ignoreRecursion){
                
                //read the Dictionary data
                data=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);
                
                //allow for data in Linear object not yet loaded
                if(data==null){
                    pdfObject.setFullyResolved(false);
                    
                    if(debugFastCode) {
                        System.out.println(padding + "Data not yet loaded");
                    }
                    
                    i=length;
                    return i;
                }
                
                if(data[0]=='<' && data[1]=='<'){
                    j=0;
                }else{
                    //lose obj at start
                    j=3;
                    
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
                        
                        if(data[j]=='/'){  //trap for odd case
                            j=0;
                            break;
                        }
                        
                        j++;
                        
                        if(j==data.length){ //some missing obj so catch these
                            j=0;
                            break;
                        }
                    }
                    
                    j=StreamReaderUtils.skipSpaces(data, j);
                    
                    if(data[j]=='%'){
                        j=StreamReaderUtils.skipComment(data, j);
                    }
                }
                
            }
        }
        
        //allow for empty object (ie /Pattern <<>> )
        final int endJ=StreamReaderUtils.skipSpacesOrOtherCharacter(data, j,'<');

        if(data[endJ]=='>'){ //empty object
            j=endJ+1;
        }else{
            
            final PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt, pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
            valueObj.setID(PDFkeyInt);

            //read pairs (stream in data starting at j)
            j=readKeyPairs(data, j,valueObj);

            //store value
            pdfObject.setDictionary(PDFkeyInt,valueObj);

            if(debugFastCode) {
                System.out.println(padding + "Set Dictionary pairs type in " + pdfObject + " to " + valueObj);
            }
            
        }
        
        //update pointer if direct so at end (if ref already in right place)
        if(!isRef){
            i=j;
            
            if(debugFastCode) {
                System.out.println(i + ">>>>" + data[i - 2] + ' ' + data[i - 1] + " >" + data[i] + "< " + data[i + 1] + ' ' + data[i + 2]);
            }
        }
        return i;
    }

    /**
     * sets pairs and returns point reached in stream
     */
    private static int readKeyPairs(final byte[] data,  int start, final PdfObject pdfObject) {

        final ArrayList<byte[]> keys=new ArrayList<byte[]>(100);
        final ArrayList<byte[]> values=new ArrayList<byte[]>(100);

        while(true){

            //move cursor to start of text
            start = StreamReaderUtils.skipSpacesOrOtherCharacter(data, start, 60);

            if(data[start]==37){ //allow for comment
                start = StreamReaderUtils.skipComment(data, start);
            }

            if(data[start]==62 || StreamReaderUtils.isEndObj(data,start)) { //exit at end
                break;
            }

            //read key (starts with /)           
            final int tokenStart=start+1;
            start=StreamReaderUtils.skipToEndOfKey(data, tokenStart);
            keys.add(getByteKeyFromStream(start-tokenStart, data, tokenStart));

            //read value
            start=StreamReaderUtils.skipSpaces(data,start);

            int refStart=start;

            if(StreamReaderUtils.isNull(data,start)){
                start += 4;
                values.add(null);
            }else {

                if (data[start]==60 || data[start]=='[' || data[start]=='/') {
                    
                    refStart = start;

                    if (data[start] == '<') {
                        start = ObjectUtils.skipToEndOfObject(start, data);
                    } else if (data[start] == '[') {
                        start=StreamReaderUtils.skipToEndOfArray(data, start);                        
                    } else if (data[start] == '/') {
                        start=StreamReaderUtils.skipToEndOfKey(data, start+1);
                    }
                } else { //its 50 0 R
                        while (data[start] != 'R') {
                            start++;
                        }

                    start++; //roll past R
                }
                
                values.add(getByteKeyFromStream(start - refStart, data, refStart));
            }
        }

        final int size=keys.size();
        byte[][] returnKeys=new byte[size][];
        byte[][] returnValues=new byte[size][];

        for(int a=0;a<size;a++){
            returnKeys[a]=keys.get(a);
            returnValues[a]=values.get(a);
        }

        pdfObject.setDictionaryPairs(returnKeys, returnValues);

        return start;

    }

    private static byte[] getByteKeyFromStream(final int tokenLength, final byte[] data, final int tokenStart) {
        final byte[] tokenKey=new byte[tokenLength];
        System.arraycopy(data, tokenStart, tokenKey, 0, tokenLength);
        return tokenKey;
    }

    /**
     * @param pdfObject
     * @param objectRef
     * @param i
     * @param raw
     * @param PDFkeyInt - -1 will store in pdfObject directly, not as separate object
     * @return
     */
    public static int readDictionaryFromRefOrDirect(final PdfObject pdfObject, final String objectRef, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        readDictionaryFromRefOrDirect:
        while (true) {
            
            i=StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);
            
            if(raw[i]=='%'){
                i=StreamReaderUtils.skipComment(raw, i);
                i=StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);
            }
            
            if (raw[i] == 60) { //[<<data inside brackets>>]
                
                i =  DirectDictionaryToObject.convert(pdfObject, objectRef, i, raw, PDFkeyInt,objectReader);
                
            } else if (raw[i] == 47) { //direct value such as /DeviceGray
                
                i = ObjectUtils.setDirectValue(pdfObject, i, raw, PDFkeyInt);
                
            } else { // ref or [ref]
                
                int j = i, ref, generation;
                byte[] data = raw;
                
                while (true) {
                    
                    //allow for [ref] at top level (may be followed by gap
                    j=StreamReaderUtils.skipSpacesOrOtherCharacter(data, j, 91);
            
                    //trap empty arrays ie [ ]
                    //ie 13jun/Factuur 2106010.PDF
                    if (data[j] == ']') {
                        return j;
                    }

                    // trap nulls  as well
                    boolean hasNull = false;
                    int keyStart;
                    int[] values;

                    while (true) {

                        //trap null arrays ie [null null]
                        if (hasNull && data[j] == ']') {
                            return j;
                        }
                        values = StreamReaderUtils.readRefFromStream(data, j);
                        ref = values[0];

                        keyStart = j;
                        j=StreamReaderUtils.skipToEndOfRef(data, j);
                        j=StreamReaderUtils.skipSpaces(data, j);

                        //handle nulls
                        if (ref != 69560 || data[keyStart] != 'n') {
                            break; //not null
                        } else {
                            hasNull = true;
                            if (data[j] == '<') { // /DecodeParms [ null << /K -1 /Columns 1778 >>  ] ignore null and jump down to enclosed Dictionary
                                i = j;
                                continue readDictionaryFromRefOrDirect;

                            }
                        }
                    }

                       generation = values[1];
                       j = values[2];
                     
                    data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);
                    
                    //allow for data in Linear object not yet loaded
                    if (data == null) {
                        pdfObject.setFullyResolved(false);
                        
                        return raw.length;
                    }
                    
                    //disregard corrputed data from start of file
                    if (data != null && data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
                        data = null;
                    }else if(StreamReaderUtils.isNull(data,0)){
                        data=null;
                    }
                    
                    if (data == null) {
                        break;
                    }
                    
                    /*
                     * get not indirect and exit if not
                     */
                    int j2 = 0;
                    
                    //allow for [91 0 r]
                    if (data[j2] != '[' && data[0] != '<' && data[1] != '<') {
                        
                        while (j2 < 3 || (j2 > 2 && data[j2 - 1] != 106 && data[j2 - 2] != 98 && data[j2 - 3] != 111)) {
                            
                            //allow for /None as value
                            if (data[j2] == '/') {
                                break;
                            }
                            j2++;
                        }

                        j2=StreamReaderUtils.skipSpaces(data,j2);
                    }
                    
                    //if indirect, round we go again
                    if (data[j2] != 91) {
                        j = 0;
                        break;
                    }else if(data[j2]=='[' && data[j2+1]=='<'){
                        j2++;
                        j=j2;
                        break;
                    }
                    
                    j = j2;
                }
                
                //allow for no data found (ie /PDFdata/baseline_screens/debug/hp_broken_file.pdf)
                if (data != null) {               
                    i=readObj(j, data, raw, ref, generation, i, pdfObject, PDFkeyInt, objectReader);
                }
            }
            
            return i;
        }
    }   

    private static int readObj(int j, byte[] data, final byte[] raw, int ref, int generation, int i, final PdfObject pdfObject, final int PDFkeyInt, final PdfFileReader objectReader) {

        /*
        * get id from stream
        */
        j=StreamReaderUtils.skipSpaces(data, j);

        //check not <</Last
        final boolean isMissingValue = j < raw.length && raw[j] == '<' &&
                raw[StreamReaderUtils.skipSpacesOrOtherCharacter(raw, j,'<')]!='/';

        if (isMissingValue) { //missing value at start for some reason
            
            int keyStart = j;

            j=StreamReaderUtils.skipToEndOfRef(data, j);
            
            ref = NumberUtils.parseInt(keyStart, j, data);
            
            j=StreamReaderUtils.skipSpaces(data, j);
            
            keyStart = j;
            j=StreamReaderUtils.skipToEndOfRef(data, j);

            generation = NumberUtils.parseInt(keyStart, j, data);
            
            //lose obj at start
            while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {
                
                if (data[j] == '<') {
                    break;
                }
                
                j++;
            }
        }
        j=StreamReaderUtils.skipSpaces(data, j);
        //move to start of Dict values
        while (data[j] != 60 && data[j + 1] != 60 && data[j] != 47) {
            j++;
        }
        i = ObjectDecoder.handleValue(pdfObject, i, PDFkeyInt, j, ref, generation, data,objectReader);
        return i;
    }
}


