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
import org.jpedal.objects.raw.NamesObject;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class Dictionary {

    public static int readDictionary(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final boolean ignoreRecursion, final PdfFileReader objectReader, final boolean isInlineImage) {
        
        int keyLength;
        final int keyStart;

        final String objectRef=pdfObject.getObjectRefAsString();
        
        //roll on
        if(raw[i]!='<') {
            i++;
        }
        
        i=ArrayUtils.skipSpaces(raw, i);
        
        //some objects can have a common value (ie /ToUnicode /Identity-H
        if(raw[i]==47){
            
            if(debugFastCode) {
                System.out.println(padding + "Indirect");
            }
            
            //move cursor to start of text
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }
            
            keyStart=i;
            keyLength=0;
            
            //move cursor to end of text
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
                i++;
                keyLength++;
            }
            
            i--;// move back so loop works
            
            if(!ignoreRecursion){
                
                final PdfObject valueObj=ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
                valueObj.setID(PDFkeyInt);
                
                //store value
                final int constant=valueObj.setConstant(PDFkeyInt,keyStart,keyLength,raw);
                
                if(constant==PdfDictionary.Unknown || isInlineImage){
                    
                    byte[] newStr=getByteKeyFromStream(keyLength, raw, keyStart);
                    
                    final String s=new String(newStr);
                    valueObj.setGeneralStringValue(s);
                    
                    if(debugFastCode) {
                        System.out.println(padding + "Set Dictionary as String=" + s + "  in " + pdfObject + " to " + valueObj);
                    }
                    
                }else if(debugFastCode) {
                    System.out.println(padding + "Set Dictionary as constant=" + constant + "  in " + pdfObject + " to " + valueObj);
                }
                
                
                //store value
                pdfObject.setDictionary(PDFkeyInt,valueObj);
                
                if(pdfObject.isDataExternal()){
                    valueObj.isDataExternal(true);
                    if(!resolveFully(valueObj,objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }
            }
            
        }else //allow for empty object
            if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
                //        return i;
                
                if(debugFastCode) {
                    System.out.println(padding + "Empty object" + new String(raw) + "<<");
                }
                
            }else if(raw[i]=='(' && PDFkeyInt== PdfDictionary.JS){ //ie <</S/JavaScript/JS( for JS
                i++;
                final int start=i;
                //find end
                while(i<raw.length){
                    i++;
                    if(raw[i]==')' && !ObjectUtils.isEscaped(raw, i)) {
                        break;
                    }
                }
                final byte[] data=ObjectUtils.readEscapedValue(i,raw,start, false);
                
                final NamesObject JS=new NamesObject(objectRef);
                JS.setDecodedStream(data);
                pdfObject.setDictionary(PdfDictionary.JS, JS);
                
            }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop
                
                if(debugFastCode) {
                    System.out.println(padding + "1.About to read ref orDirect i=" + i + " char=" + (char) raw[i] + " ignoreRecursion=" + ignoreRecursion);
                }
                
                
                if(ignoreRecursion){
                    
                    //roll onto first valid char
                    while(raw[i]==91 || raw[i]==32 || raw[i]==13 || raw[i]==10){
                        
                        //if(raw[i]==91) //track incase /Mask [19 19]
                        //	possibleArrayStart=i;
                        
                        i++;
                    }
                    
                    //roll on and ignore
                    if(raw[i]=='<' && raw[i+1]=='<'){
                        
                        i += 2;
                        int reflevel=1;
                        
                        while(reflevel>0){
                            if(raw[i]=='<' && raw[i+1]=='<'){
                                i += 2;
                                reflevel++;
                            }else if(raw[i]=='>' && raw[i+1]=='>'){
                                i += 2;
                                reflevel--;
                            }else {
                                i++;
                            }
                        }
                        i--;
                        
                    }else{ //must be a ref
                       i = Dictionary.readDictionaryFromRefOrDirect(pdfObject,objectRef, i, raw, PDFkeyInt,objectReader);
                    }
                    
                    if(i<raw.length && raw[i]=='/') //move back so loop works
                    {
                        i--;
                    }
                    
                }else{
                    i = Dictionary.readDictionaryFromRefOrDirect(pdfObject,objectRef, i, raw, PDFkeyInt,objectReader);
                }
            }
        return i;
    }
    
    public static int getPairedValues(final PdfObject pdfObject, final int i, final byte[] raw, final int pdfKeyType, final int length, final int keyLength, final int keyStart) {
        
        boolean isPair=false;
        
        int jj=i;
        
        while(jj<length){
            
            jj=ArrayUtils.skipSpaces(raw, jj);
            
            //number (possibly reference)
            if(jj<length && raw[jj]>='0' && raw[jj]<='9'){
                
                //rest of ref
                while(jj<length && raw[jj]>='0' && raw[jj]<='9') {
                    jj++;
                }
                
                jj=ArrayUtils.skipSpaces(raw, jj);
        
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
            
            jj=ArrayUtils.skipSpaces(raw, jj);
                        
            
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
    
    public static boolean isStringPair(final int i, final byte[] raw, boolean stringPair) {
        
        final int len=raw.length;
        for(int aa=i;aa<len;aa++){
            if(raw[aa]=='('){
                aa=len;
                stringPair =true;
            }else if(raw[aa]=='/' || raw[aa]=='>' || raw[aa]=='<' || raw[aa]=='[' || raw[aa]=='R'){
                aa=len;
            }else if(raw[aa]=='M' && raw[aa+1]=='C' && raw[aa+2]=='I' && raw[aa+3]=='D'){
                aa=len;
            }
        }
        return stringPair;
    }
    
    public static int findDictionaryEnd(int jj, final byte[] raw, final int length) {
        
        int keyLength=0;
        while (true) { //get key up to space or [ or / or ( or < or carriage return
            
            if (raw[jj] == 32 || raw[jj] == 13 || raw[jj] == 9 || raw[jj] == 10 || raw[jj] == 91 ||
                    raw[jj]==47 || raw[jj]==40 || raw[jj]==60 || raw[jj]==62) {
                break;
            }
            
            jj++;
            keyLength++;
            
            if(jj==length) {
                break;
            }
        }
        return keyLength;
    }
    
     
    public static int setDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final int length, final boolean ignoreRecursion, final PdfFileReader objectReader, final int PDFkeyInt) {
        
        if(debugFastCode) {
            System.out.println(padding + ">>>Reading Dictionary Pairs i=" + i + ' ' + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4] + (char) raw[i + 5] + (char) raw[i + 6]);
        }
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
            i++;
        }
        
        //set data which will be switched below if ref
        byte[] data=raw;
        int j=i;
        
        //get next key to see if indirect
        final boolean isRef=data[j]!='<';
        
        if(isRef){
            
            //number
            int keyStart2=i;
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
                i++;
            }
            
            final int number= NumberUtils.parseInt(keyStart2, i, raw);
            
            i=ArrayUtils.skipSpaces(raw, i);
        
            keyStart2=i;
            //move cursor to end of reference
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
                i++;
            }
            final int generation= NumberUtils.parseInt(keyStart2, i, raw);
            
            i=ArrayUtils.skipSpaces(raw, i);
            
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
                    
                    //skip any spaces after
                    while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
                    {
                        j++;
                    }
                }
                
            }
        }
        
        //allow for empty object (ie /Pattern <<>> )
        int endJ=j;
        while(data[endJ]=='<' || data[endJ]==' ' || data[endJ]==13 ||  data[endJ]==10) {
            endJ++;
        }
        
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
            start = getStart(data, start);

            if(data[start]==37){ //allow for comment
                start = ArrayUtils.skipComment(data, start);
            }

            if(data[start]==62) { //exit at end
                break;
            }

            //read key (starts with /)           
            final int tokenStart=start+1;
            start=ArrayUtils.skipToEndOfKey(data, tokenStart);
            keys.add(getByteKeyFromStream(start-tokenStart, data, tokenStart));

            //read value
            start=ArrayUtils.skipSpaces(data,start);

            int refStart=start;

            if(ArrayUtils.isNull(data,start)){
                start += 4;
            }else {

                if (data[start]==60 || data[start]=='[' || data[start]=='/') {
                    
                    refStart = start;

                    if (data[start] == '<') {
                        start = ObjectUtils.skipToEndOfObject(start, data);
                    } else if (data[start] == '[') {
                        start=ArrayUtils.skipToEndOfArray(data, start);                        
                    } else if (data[start] == '/') {
                        start=ArrayUtils.skipToEndOfKey(data, start+1);
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

    private static int getStart(byte[] data, int start) {

        byte b=data[start];
        while(b ==9 || b ==10 || b ==13 || b ==32 || b ==60) {
            start++;
            b=data[start];
        }
        return start;
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
            
            int keyStart;
            int possibleArrayStart = -1;
            
            //@speed - find end so we can ignore once no longer reading into map as well
            //and skip to end of object
            //allow for [ref] or [<< >>] at top level (may be followed by gap)
            //good example is /PDFdata/baseline_screens/docusign/test3 _ Residential Purchase and Sale Agreement - 6-03.pdf
            while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10) {
                
                if (raw[i] == 91) //track incase /Mask [19 19]
                {
                    possibleArrayStart = i;
                }
                
                i++;
            }
            
            //some items like MAsk can be [19 19] or stream
            //and colorspace is law unto itself
            if (possibleArrayStart != -1 && (PDFkeyInt == PdfDictionary.Mask || PDFkeyInt == PdfDictionary.TR || PDFkeyInt == PdfDictionary.OpenAction)) {
                return ArrayFactory.processArray(pdfObject, raw, PDFkeyInt, possibleArrayStart, objectReader);
            }
            
            if (raw[i] == '%') { // if %comment roll onto next line
                while (raw[i] != 13 && raw[i] != 10) {
                    i++;
                }
                
                //and lose space after
                while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10) {
                    i++;
                }
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
                    while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10) {
                        j++;
                    }

                    //trap empty arrays ie [ ]
                    //ie 13jun/Factuur 2106010.PDF
                    if (data[j] == ']') {
                        return j;
                    }
                    
                    // trap nulls  as well
                    boolean hasNull = false;
                    
                    while (true) {
                        
                        //trap null arrays ie [null null]
                        if (hasNull && data[j] == ']') {
                            return j;
                        }
                        
                        /*
                         * get object ref
                         */
                        keyStart = j;
                        //move cursor to end of reference
                        while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                            
                            //trap null arrays ie [null null] or [null]                            
                            
                            if (data[j] == 'l' && data[j - 1] == 'l' && data[j - 2] == 'u' && data[j - 3] == 'n') {
                                hasNull = true;
                            }
                            
                            if (hasNull && data[j] == ']') {
                                return j;
                            }
                            
                            j++;
                        }
                        
                        ref = NumberUtils.parseInt(keyStart, j, data);
                        
                        j=ArrayUtils.skipSpaces(data, j);
                        
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

                    keyStart = j;
                    //move cursor to end of reference
                    while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                        j++;
                    }
                    
                    generation = NumberUtils.parseInt(keyStart, j, data);

                    j=ArrayUtils.skipSpaces(data, j);
                     
                    data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);
                    
                    //allow for data in Linear object not yet loaded
                    if (data == null) {
                        pdfObject.setFullyResolved(false);
                        
                        return raw.length;
                    }
                    
                    //disregard corrputed data from start of file
                    if (data != null && data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
                        data = null;
                    }else if(ArrayUtils.isNull(data,0)){
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
                        
                        //skip any spaces
                        while (data[j2] != 91 && (data[j2] == 10 || data[j2] == 13 || data[j2] == 32))// || data[j]==47 || data[j]==60)
                        {
                            j2++;
                        }
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
                    
                    /*
                     * get id from stream
                     */
                    j=ArrayUtils.skipSpaces(data, j);
                     
                    boolean isMissingValue = j < raw.length && raw[j] == '<';
                    
                    if (isMissingValue) { //check not <</Last
                        //find first valid char
                        int xx = j;
                        while (xx < data.length && (raw[xx] == '<' || raw[xx] == 10 || raw[xx] == 13 || raw[xx] == 32)) {
                            xx++;
                        }
                        
                        if (raw[xx] == '/') {
                            isMissingValue = false;
                        }
                    }
                    
                    if (isMissingValue) { //missing value at start for some reason
                        
                        keyStart = j;
                        //move cursor to end of reference
                        while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                            j++;
                        }
                        
                        ref = NumberUtils.parseInt(keyStart, j, data);
                        
                        j=ArrayUtils.skipSpaces(data, j);
                        
                        keyStart = j;
                        //move cursor to end of reference
                        while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                            j++;
                        }
                        
                        generation = NumberUtils.parseInt(keyStart, j, data);
                        
                        //lose obj at start
                        while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {
                            
                            if (data[j] == '<') {
                                break;
                            }
                            
                            j++;
                        }
                    }
                    
                    j=ArrayUtils.skipSpaces(data, j);
                    
                    //move to start of Dict values
                    while (data[j] != 60 && data[j + 1] != 60 && data[j] != 47) {
                        j++;
                    }
                    
                    i = ObjectDecoder.handleValue(pdfObject, i, PDFkeyInt, j, ref, generation, data,objectReader);
                }
            }
            
            return i;
        }
    }   
}


