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
 * ArrayUtils.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.DecryptionFactory;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class ArrayUtils {
    
    
    static boolean handleIndirect(final byte[] raw, int aa){
        
        boolean indirect=true;
        
        //find next value and make sure not /
        final int length=raw.length;
        
        while(raw[aa]!=93 ){
            aa++;
            
            //allow for ref (ie 7 0 R)
            if(aa>=length) {
                break;
            }
            
            if(raw[aa]=='R' && (raw[aa-1]==32 || raw[aa-1]==10 || raw[aa-1]==13)) {
                break;
            } else if(raw[aa]=='>' && raw[aa-1]=='>'){
                indirect =false;
                break;
            }else if(raw[aa]==47){
                indirect =false;
                break;
            }
        }
        return indirect;
    }


    static int skipToEndOfRef(int i, final byte[] raw) {

        byte b=raw[i];
        while(b!=10 && b!=13 && b!=32 && b!=47 && b!=60 && b!=62){
            i++;
            b=raw[i];
        }

        return i;
    }
    
    public static boolean isNull(final byte[] arrayData, final int j2){
        return arrayData[j2]=='n' && arrayData[j2+1]=='u' && arrayData[j2+2]=='l' && arrayData[j2+3]=='l';
    }
    
    public static int skipComment(final byte[] raw, int i) {
        
        final int len=raw.length;
        
        while(i<len && raw[i]!=10 && raw[i]!=13){
            i++;
        }
        
        //move cursor to start of text
        while(i<len &&(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==9)) {
            i++;
        }
        
        return i;
    }
    
    
    
    /**
     * convert <FFFE to actual string value)
     * @param newValues
     * @return
     */
    static byte[] handleHexString(byte[] newValues,DecryptionFactory decryptor, String ref) {
        
        //convert to byte values
        String nextValue;
        final String str=new String(newValues);
        final byte[] IDbytes=new byte[newValues.length/2];
        for(int ii=0;ii<newValues.length;ii += 2){
            
            if(ii+2>newValues.length) {
                continue;
            }
            
            /*String array is a series of byte values.
            * If the byte values has a \n in the middle we should ignore it.
            * (customer-June2011/payam.pdf)
            */
            if(str.charAt(ii)=='\n'){
                ii++;
            }
            
            nextValue=str.substring(ii,ii+2);
            IDbytes[ii/2]=(byte)Integer.parseInt(nextValue,16);
            
        }
        newValues=IDbytes;
        
        if(decryptor!=null){
            byte[] decryptedValue=null;
            try {
                
                decryptedValue = decryptor.decryptString(IDbytes, ref);
                
            } catch (PdfSecurityException ex) {
                LogWriter.writeLog("Exception: " + ex.getMessage());
            }
            newValues= (decryptedValue==null) ? IDbytes:decryptedValue;
        }
        
        return newValues;
    }

    public static int skipSpaces(final byte[] data, int start) {
        
        final int len=data.length;
        
        //now skip any spaces to key or text
        while(start<len && (data[start]==10 || data[start]==13 || data[start]==32)) {
            start++;
        }

        return start;
    }
    
    public static int skipToEndOfKey(final byte[] data, int start) {
        
        int len=data.length;
        
        //now skip any spaces to key or text
        while(start<len && (Character.isLetterOrDigit((char)data[start])||data[start]=='_'|| data[start]=='.' || data[start]=='#' || data[start]=='-')) {
            start++;
        }

        return start;
    }

    static boolean isSpace(final byte[] arrayData, final int endPtr) {
        return (arrayData[endPtr] == 32 || arrayData[endPtr] == 13 || arrayData[endPtr] == 10);
    }
    
    
    static boolean isNumber(byte[] arrayData, int j2) {
        
        boolean isNumber=true;
        
        int count=arrayData.length;
        
        j2=skipSpaces(arrayData,j2);
        
        while(isNumber && j2<count){
            
            if((arrayData[j2]>='0' && arrayData[j2]<='9')) {
                //part of number char
                j2++;
            }else{
                isNumber=false;
            }     
        }
        
        return isNumber;      
    }

    static boolean isRef(byte[] arrayData, int j2) {
        
        boolean isRef=true;
        
        int count=arrayData.length, elementCount=0;
        
        j2=skipSpaces(arrayData,j2);
        
        while(isRef && j2<count && arrayData[j2]!='R'){
            
            if((arrayData[j2]>='0' && arrayData[j2]<='9')) {
                //part of number char
                j2++;
            }else if(isSpace(arrayData,j2)){
                
                elementCount++;
                
                j2=skipSpaces(arrayData,j2);
            }else{
                isRef=false;
            }     
        }
        
        return isRef && elementCount==2;
               
    }

    static boolean isEndObj(final byte[] arrayData, final int j2) {
       return arrayData[j2]=='e' && arrayData[j2+1]=='n' && arrayData[j2+2]=='d' && arrayData[j2+3]=='o' && arrayData[j2+4]=='b' && arrayData[j2+5]=='j';
    }
}


