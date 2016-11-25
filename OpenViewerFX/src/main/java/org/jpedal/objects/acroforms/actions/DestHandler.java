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
 * DestHandler.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.types.Array;
import org.jpedal.io.types.StreamReaderUtils;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;

/**
 *
 */
public class DestHandler {

    
    public static PdfArrayIterator getDestFromObject(PdfObject formObj, PdfObjectReader currentPdfFile) {
        
        PdfArrayIterator Dest=formObj.getMixedArray(PdfDictionary.Dest);
        
        if(Dest==null || Dest.getTokenCount()==0){
            
            //aData can either be in top level of Form (as in Annots) and in A or AA
            //or second level (as in A/ /D - this allows for both
            //which this routine handles
            PdfObject Aobj=formObj.getDictionary(PdfDictionary.A);
            if(Aobj==null) {
                Aobj = formObj.getDictionary(PdfDictionary.AA);
            }
            if(Aobj!=null) {
                formObj = Aobj;
            }
            
            //allow for D as indirect
            final PdfObject Dobj=formObj.getDictionary(PdfDictionary.D);
            if(Dobj!=null) {
                formObj = Dobj;
            }
            
            Dest=formObj.getMixedArray(PdfDictionary.D);
        }
        
        //its nameString name (name) linking to obj so read that
        if (Dest != null && Dest.getTokenCount()>0 && !Dest.isNextValueRef()){            
            Dest=decodeDest(Dest.getNextValueAsString(false), currentPdfFile, Dest);           
        }
        
        return Dest;
        
    }
   
    /**
     * Gets the page number from an A entry and Dest or D entry
     * @param dest Dest or D entry
     * @return page number
     */
    public static int getPageNumberFromLink(PdfArrayIterator dest, final PdfObjectReader currentPdfFile){

        dest = resolveIfIndirect(dest, currentPdfFile);

        int page=-1;
        
        if(dest.hasMoreTokens()){
            final String pageRef = dest.getNextValueAsString(false);
            
            //convert to target page if ref or ignore
            page =currentPdfFile.convertObjectToPageNumber(pageRef);

            if(page==-1 && dest.getTokenCount()>2 && !pageRef.contains(" R") && !pageRef.isEmpty()){

                //get pageRef as number of ref
                if(dest.hasMoreTokens()){
                    int possiblePage=dest.getNextValueAsInteger(false)+1;
                
                    if(possiblePage>0){ //can also be a number (cant check range as not yet open)
                        page=possiblePage;
                    }
                }
            }
            
            //allow for number
            if (page == -1 && dest.isNextValueNumber()) {
                
                int possiblePage = dest.getNextValueAsInteger(false) + 1;

                if (possiblePage > 0) { //can also be a number (cant check range as not yet open)
                    page = possiblePage;
                }
            }
        }
    
        return page;
    }

    private static PdfArrayIterator convertRef(final String ref, final PdfObjectReader currentPdfFile) {
        
        PdfArrayIterator dest=null;
        final PdfObject aData=new OutlineObject(ref);
        //can be indirect object stored between []
        if(ref.charAt(0)=='['){           
            dest= converDestStringToMixedArray(ref, currentPdfFile, aData);            
        }else{
            dest =decodeDest(ref, currentPdfFile, dest);
        }
        return dest;
    }

    private static PdfArrayIterator converDestStringToMixedArray(final String ref, final PdfObjectReader currentPdfFile, final PdfObject aData) {
        
        final byte[] raw= StringUtils.toBytes(ref);
       
        final Array objDecoder=new Array(currentPdfFile.getObjectReader(), 0,  PdfDictionary.VALUE_IS_MIXED_ARRAY, raw);
        objDecoder.readArray(aData, PdfDictionary.Dest);
        return aData.getMixedArray(PdfDictionary.Dest);
    }
    
    private static PdfArrayIterator decodeDest(String nameString, final PdfObjectReader currentPdfFile, PdfArrayIterator DestObj) {
        
        if(nameString.startsWith("/")){
            nameString=nameString.substring(1);
        }
        
        byte[] rawRef=nameString.getBytes();
         
        if(!StreamReaderUtils.isRef(rawRef, 0)){
            nameString=currentPdfFile.convertNameToRef(nameString);
        }
        
        if(nameString!=null){
            
            rawRef=nameString.getBytes();
            
            if(StreamReaderUtils.isRef(rawRef, 0)){ //indirect so needs resolving if []
                
                final int[] values=StreamReaderUtils.readRefFromStream(rawRef,0);
                
                final int ref2=values[0];
                final int generation=values[1];
                
                OutlineObject obj=new OutlineObject(new String(rawRef));
                
                PdfFileReader objectReader=currentPdfFile.getObjectReader();
                
                //read the Dictionary data
                rawRef = objectReader.readObjectAsByteArray(obj, objectReader.isCompressed(ref2, generation), ref2, generation);
                
                int startArray=0;
                while(rawRef[startArray]!='[' && rawRef[startArray]!='<'){
                    startArray++;
                }
                
                if(rawRef[startArray]=='['){
                    int length=rawRef.length;
                    int newLength=length-startArray;
                    
                    byte[] strippedData=new byte[length];
                    
                    System.arraycopy(rawRef, startArray, strippedData, 0, newLength);
                    nameString=new String(strippedData);
                }else{
                    OutlineObject Aobj=new OutlineObject(nameString);
                    currentPdfFile.readObject(Aobj);
                    DestObj=Aobj.getMixedArray(PdfDictionary.D);
                }
            }
        }
        //allow for direct value
        if(nameString!=null && nameString.startsWith("[")){            
            DestObj= converDestStringToMixedArray(nameString, currentPdfFile, new OutlineObject(nameString));
        }
        
        return DestObj;
    }

    private static PdfArrayIterator resolveIfIndirect(PdfArrayIterator dest, final PdfObjectReader currentPdfFile) {
        if (dest.getTokenCount() == 1) { //indirect value to remap (Name or ref)
            final String ref = currentPdfFile.convertNameToRef(dest.getNextValueAsString(false));
            if (ref != null) {
                dest = convertRef(ref, currentPdfFile);
            } else {
                dest = decodeDest(dest.getNextValueAsString(false), currentPdfFile, dest);
            }
        }
        return dest;
    }

    private static Float getFloatOrNull(final PdfArrayIterator dest) {
        if (dest.getNextValueAsString(false).equals("null")) {
            dest.getNextValueAsString(true); // Roll on
            return null;
        } else {
            return dest.getNextValueAsFloat();
        }
    }

    public static Object[] getZoomFromDest(PdfArrayIterator dest, final PdfObjectReader currentPdfFile) {
        dest.resetToStart();
        dest = resolveIfIndirect(dest, currentPdfFile);

        while (dest.hasMoreTokens()) {
            final Object[] action;
            final int key = dest.getNextValueAsKey();
            switch (key) {

                case PdfDictionary.XYZ:
                    action = new Object[5];
                    action[0] = key;
                    action[1] = "XYZ";
                    action[2] = getFloatOrNull(dest);
                    action[3] = getFloatOrNull(dest);
                    action[4] = getFloatOrNull(dest);
                    return action;

                case PdfDictionary.Fit:
                    action = new Object[2];
                    action[0] = key;
                    action[1] = "Fit";
                    return action;

                case PdfDictionary.FitH:
                    action = new Object[3];
                    action[0] = key;
                    action[1] = "FitH";
                    action[2] = getFloatOrNull(dest);
                    return action;

                case PdfDictionary.FitV:
                    action = new Object[3];
                    action[0] = key;
                    action[1] = "FitV";
                    action[2] = getFloatOrNull(dest);
                    return action;

                case PdfDictionary.FitR:
                    action = new Object[6];
                    action[0] = key;
                    action[1] = "FitR";
                    // Specification does not mention null values for FitR (behavior unspecified)
                    action[2] = getFloatOrNull(dest);
                    action[3] = getFloatOrNull(dest);
                    action[4] = getFloatOrNull(dest);
                    action[5] = getFloatOrNull(dest);
                    return action;

                case PdfDictionary.FitB:
                    action = new Object[2];
                    action[0] = key;
                    action[1] = "FitB";
                    return action;

                case PdfDictionary.FitBH:
                    action = new Object[3];
                    action[0] = key;
                    action[1] = "FitBH";
                    action[2] = getFloatOrNull(dest);
                    return action;

                case PdfDictionary.FitBV:
                    action = new Object[3];
                    action[0] = key;
                    action[1] = "FitBV";
                    action[2] = getFloatOrNull(dest);
                    return action;
            }
        }
        return null;
    }

    public static String convertZoomArrayToString(final Object[] zoomArray) {
        final StringBuilder zoom = new StringBuilder();
        zoom.append(zoomArray[1]);

        for (int i = 2; i < zoomArray.length; i++) {
            zoom.append(' ').append(zoomArray[i]);
        }

        return zoom.toString();
    }
}


