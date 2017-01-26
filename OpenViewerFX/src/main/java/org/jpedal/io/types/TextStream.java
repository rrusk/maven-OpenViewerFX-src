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
 * TextStream.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.security.DecryptionFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class TextStream {
    
    public static int readTextStream(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
            
        byte[] data;
        try{
            if(raw[i]!='<' && raw[i]!='(') {
                i++;
            }

            i=StreamReaderUtils.skipSpaces(raw,i);

            //get next key to see if indirect
            final boolean isRef=raw[i]!='<' && raw[i]!='(';

            int j=i;
            data=raw;
            if(isRef){

                final int[] values = StreamReaderUtils.readRefFromStream(raw, i);
                final int number = values[0];
                final int generation = values[1];

                i=StreamReaderUtils.skipSpaces(raw,values[2]);

                if(raw[i]!=82){ //we are expecting R to end ref
                    return raw.length;
                }

                //read the Dictionary data
                data=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);

                //allow for data in Linear object not yet loaded
                if(data==null){
                    pdfObject.setFullyResolved(false);

                    if(debugFastCode) {
                        System.out.println(padding + "Data not yet loaded");
                    }

                    LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (7)");

                    return raw.length;
                }

                //lose obj at start
                if(data[0]=='('){
                    j=0;
                }else{
                    j=3;
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111) {
                        j++;
                    }

                    j=StreamReaderUtils.skipSpaces(data,j);
                }               
            }

            //move to start
            while(data[j]!='(' && data[j]!='<'){
                j++;
            }

            int start=j;

            j = skipToEnd(data, j);

            byte[] newString;

            if(data[start]=='<'){
                start++;

                final int byteCount=(j-start)>>1;
                newString=new byte[byteCount];

                int byteReached=0,topHex,bottomHex;
                while(true){

                    if(start==j) {
                        break;
                    }

                    start=StreamReaderUtils.skipSpaces(data,start);

                    topHex=toNumber(data[start]);

                    start=StreamReaderUtils.skipSpaces(data,start+1);

                    bottomHex=toNumber(data[start]);

                    start++;

                    //calc total
                    newString[byteReached] = (byte)(bottomHex+(topHex<<4));

                    byteReached++;

                }

            }else{
                //roll past (
                if(data[start]=='(') {
                    start++;
                }

                boolean lbKeepReturns = false;
                switch ( PDFkeyInt ) {
                    case PdfDictionary.Contents:
                        lbKeepReturns = pdfObject.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.FreeText;
                        break;
                    case PdfDictionary.ID:
                        lbKeepReturns = true;
                        break;
                    case PdfDictionary.O:
                    case PdfDictionary.U:
                        // O and U in Encrypt may contain line breaks as valid password chars ...
                        lbKeepReturns = pdfObject.getObjectType() == PdfDictionary.Encrypt;
                        break;
                }

                newString = ObjectUtils.readEscapedValue(j,data, start,lbKeepReturns);
            }

            if(pdfObject.getObjectType()!= PdfDictionary.Encrypt && pdfObject.getObjectType()!= PdfDictionary.MCID){

                try {
                    if(!pdfObject.isInCompressedStream() || PDFkeyInt==PdfDictionary.Name || PDFkeyInt==PdfDictionary.Reason || PDFkeyInt==PdfDictionary.Location || PDFkeyInt==PdfDictionary.M){
                        final DecryptionFactory decryption=objectReader.getDecryptionObject();

                        if(decryption!=null) {
                            newString = decryption.decryptString(newString, pdfObject.getObjectRefAsString());
                        }
                    }
                } catch (final PdfSecurityException e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }

            pdfObject.setTextStreamValue(PDFkeyInt, newString);

            if(debugFastCode) {
                System.out.println(padding + "TextStream=" + new String(newString) + " in pdfObject=" + pdfObject);
            }
            
            if(!isRef) {
                i = j;
            }

        }catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        return i;
    }

    static int skipToEnd(final byte[] data, int j) {

        final byte startChar=data[j];

        //move to end (allow for ((text in brackets))
        int bracketCount=1;
        while(j<data.length){

            j++;

            if(startChar=='(' && (data[j]==')' || data[j]=='(') && !ObjectUtils.isEscaped(data, j)){
                //allow for non-escaped brackets
                if(data[j]=='(') {
                    bracketCount++;
                } else if(data[j]==')') {
                    bracketCount--;
                }

                if(bracketCount==0) {
                    break;
                }
            }

            if(startChar=='<' && (data[j]=='>' || data[j]==0)) {
                break;
            }
        }
        return j;
    }

    private static int toNumber(int rawVal) {

        if(rawVal >='A' && rawVal <='F'){
            rawVal -= 55;
        }else if(rawVal >='a' && rawVal <='f'){
            rawVal -= 87;
        }else if(rawVal >='0' && rawVal <='9'){
            rawVal -= 48;
        }

        return rawVal;
    }

    public static int setTextStreamValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        if(raw[i+1]==40 && raw[i+2]==41){ //allow for empty stream
            i += 3;
            pdfObject.setTextStreamValue(PDFkeyInt, new byte[1]);
            
            if(raw[i]=='/') {
                i--;
            }
        }else {
            i = TextStream.readTextStream(pdfObject, i, raw, PDFkeyInt, objectReader);
        }
        
        return i;
    }
}


