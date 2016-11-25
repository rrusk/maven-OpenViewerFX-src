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
 * General.java
 * ---------------
 */

package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;


/**
 *
 * @author markee
 */
public class General {
    
    public static int readGeneral(final PdfObject pdfObject, int i, final byte[] raw, final int length, final int PDFkeyInt, final boolean map, final boolean ignoreRecursion, final PdfFileReader objectReader,Object PDFkey){
       
        if(debugFastCode) {
            System.out.println(padding + "general case " + i);
        }

        //see if number or ref
        int jj=i;
        int j=i+1;
        byte[] data=raw;
        int typeFound=0;
        boolean isNumber=true, isRef=false, isString=false;

        String objRef=pdfObject.getObjectRefAsString();

        while(true){

            if(data[j]=='R' && !isString){

                isRef=true;
                final int end=j;
                j=i;
                i=end;

                final int ref;
                final int generation;

                //allow for [ref] at top level (may be followed by gap
                while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10) {
                    j++;
                }
                final int[] values = StreamReaderUtils.readRefFromStream(data, j);
                // get object ref

                final int refStart=j;
                ref = values[0];
                generation = values[1];
                j = values[2];

                if (data[j] != 82)  //we are expecting R to end ref
                {
                    throw new RuntimeException("ref=" + ref + " gen=" + ref + " 1. Unexpected value " + data[j] + " in file - please send to IDRsolutions for analysis char=" + (char) data[j]);
                }

                objRef =new String(data,refStart,1+j-refStart);

                //read the Dictionary data
                //boolean setting=debugFastCode;
                data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

                //allow for data in Linear object not yet loaded
                if(data==null){
                    pdfObject.setFullyResolved(false);

                    if(debugFastCode) {
                        System.out.println(padding + "Data not yet loaded");
                    }

                    i=length;
                    break;
                }

                //find first valid char to see if String
                int firstChar=0;

                //some data actually starts << and not 10 0 obj so allow for this
                if(data!=null && data.length>2 && data[0]=='<' && data[1]=='<'){
                    //check for already at <<

                }else{
                    firstChar = findFirstChar(data, firstChar);
                }

                //stop string with R failing in loop
                isString=data[firstChar]=='(';

                jj=skipStartObj(data);

                j=jj;

                if(debugFastCode) {
                    System.out.println(j + " >>" + new String(data) + "<<next=" + (char) data[j]);
                }

            }else if(data[j]=='[' || data[j]=='('){
                break;
            }else if(data[j]=='<'){
                typeFound=0;
                break;

            }else if(data[j]=='>' || data[j]=='/'){
                typeFound=1;
                break;
            }else if(data[j]==32 || data[j]==10 || data[j]==13 || data[j]==9){
            }else if((data[j]>='0' && data[j] <='9')|| data[j]=='.'){ //assume and disprove
            }else{
                isNumber=false;
            }
            if(data[j]!='['){
                j++;
            }
            if(j==data.length) {
                break;
            }
        }

        jj=StreamReaderUtils.skipSpaces(data, jj);

        if(typeFound==4){//direct ref done above
        }else if(data[jj]=='/' && getKeyCount(jj, data)==0){
            jj = Name.setNameStringValue(pdfObject, jj, data, map, PDFkey, PDFkeyInt, objectReader);
        }else if(data[jj]=='('){
            jj = TextStream.readTextStream(pdfObject, jj, data, PDFkeyInt, ignoreRecursion,objectReader);
        }else if(data[jj]=='['){

            final ArrayDecoder objDecoder=ArrayFactory.getDecoder(objectReader, jj,PdfDictionary.VALUE_IS_STRING_ARRAY, data);
            jj=objDecoder.readArray(pdfObject, PDFkeyInt);
               
        }else if(typeFound==0){           
            try{
            jj = Dictionary.readDictionaryFromRefOrDirect(pdfObject, objRef,jj , data, PDFkeyInt,objectReader);

            }catch(final Exception e){
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }else if(isNumber){
            jj= NumberValue.setNumberValue(pdfObject, jj, data, PDFkeyInt,objectReader);
        }else if(typeFound==1){
            jj = Name.setNameStringValue(pdfObject, jj, data, map,PDFkey, PDFkeyInt, objectReader);            
        }else if(debugFastCode) {
            System.out.println(padding + "Not read");
        }

        if(!isRef) {
            i = jj;
        }

        return i;
    }

    private static int findFirstChar(final byte[] newData, int firstChar) {
        
        final int newLength=newData.length-3;
        for(int aa=3;aa<newLength;aa++){   //skip past 13 0 obj bit at start if present
            if(newData[aa-2]=='o' && newData[aa-1]=='b' && newData[aa]=='j'){
                firstChar=aa+1;
                
                firstChar = StreamReaderUtils.skipSpaces(newData, firstChar);
                
                aa=newLength; //exit loop
            }else if(newData[aa]>47 && newData[aa]<58){//number
            }else if(newData[aa]=='o' || newData[aa]=='b' || newData[aa]=='j' || newData[aa]=='R' || newData[aa]==32 || newData[aa]==10 || newData[aa]==13){ //allowed char
            }else{ //not expected so reset and quit
                aa= newLength;
                firstChar=0;
            }
        }
        return firstChar;
    }

    private static int skipStartObj(final byte[] data) {
        
        int jj=3;
        
        if(data.length<=3){
            jj=0;
        }else{
            while(true){
                if(data[jj-2]=='o' && data[jj-1]=='b' && data[jj]=='j') {
                    break;
                }
                
                jj++;
                
                if(jj==data.length){
                    jj=0;
                    break;
                }
            }
        }
        if(data[jj]!='[' && data[jj]!='(' && data[jj]!='<') //do not roll on if text string
        {
            jj++;
        }
        
        jj=StreamReaderUtils.skipSpaces(data, jj);
        
        return jj;
    }

    private static int getKeyCount(int jj, byte[] data) {
        //check if name by counting /
        int count=0;
        for(int aa=jj+1;aa<data.length;aa++){
            if(data[aa]=='/') {
                count++;
            }
        }
        return count;
    }
}
