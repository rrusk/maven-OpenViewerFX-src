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
 * ColorspaceFactory.java
 * ---------------
 */
package org.jpedal.color;



import java.awt.color.ColorSpace;
import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.types.Array;
import org.jpedal.io.types.KeyArray;
import org.jpedal.io.types.StreamReaderUtils;
import org.jpedal.objects.raw.*;

/**
 * @author markee
 *
 * returns the correct colorspace, decoding the values
 */
public final class ColorspaceFactory {
    
    /**
     * used by commands which implicitly set colorspace
     * 
     */
    public static GenericColorSpace getColorSpaceInstance(final PdfObjectReader currentPdfFile, final PdfArrayIterator colorSpace) {

        //System.out.println("col="+colorSpace.getNextValueAsString(false));
        int ID = colorSpace.getNextValueAsKey();

        //allow for CMYK in ID
        if (ID == PdfDictionary.CMYK) {
            ID = ColorSpaces.DeviceCMYK;
        }
        
        //setup colorspaces which map onto others
        if (ID == ColorSpaces.Indexed || ID == PdfDictionary.I) {

            GenericColorSpace currentColorData = getColorSpace(colorSpace, currentPdfFile);

            //hival
            final int size = colorSpace.getNextValueAsInteger(true);

            //lookup
            final byte[] lookup = getTransformTable(currentPdfFile, colorSpace.getNextValueAsByte(true));

            //ICC code will wrongly create RGB in case of indexed ICC with DeviceGray alt - here we fit this
            //(sample file is 11jun/early mockup.pdf)
            if (currentColorData.getRawColorSpacePDFType() == ColorSpaces.ICC && lookup.length < 3) {
                currentColorData = new DeviceGrayColorSpace();
            }

            currentColorData.setIndex(lookup, size);

            return currentColorData;
        } else {
            return getColorspace(ID, colorSpace, currentPdfFile);

        }
    }

    private static GenericColorSpace getColorSpace(final PdfArrayIterator colorSpace, final PdfObjectReader currentPdfFile) {
    
        final byte[] colValue=colorSpace.getNextValueAsByte(false);
        
        if (StreamReaderUtils.isRef(colValue, 0) || StreamReaderUtils.isArray(colValue, 0)) {
            final PdfArrayIterator it=convertColValueToMixedArray(currentPdfFile, colorSpace.getNextValueAsByte(true));
            
            return getColorspace(it.getNextValueAsKey(), it, currentPdfFile);          
        } else {
            return getColorspace(colorSpace.getNextValueAsKey(), colorSpace, currentPdfFile);
        }     
    }

    public static FunctionObject getFunctionObjectFromRefOrDirect(final PdfObjectReader currentPdfFile, final byte[] data) {
        
        final FunctionObject colObj  = new FunctionObject(new String(data));

        if(data[0]=='<') {
            colObj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            colObj.setStatus(PdfObject.UNDECODED_REF);
        }
        colObj.setUnresolvedData(data,PdfDictionary.Function);
        
        final ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());
        objectDecoder.checkResolved(colObj);
        
        return colObj;
    }
    
    static byte[] getTransformTable(final PdfObjectReader currentPdfFile, final byte[] data) {
       
        if(StreamReaderUtils.isRef(data, 0)){ //indirect so needs decoding
           final ColorSpaceObject colObj2  = new ColorSpaceObject(new String(data));          
           currentPdfFile.readObject(colObj2);
            return colObj2.getDecodedStream();
         }else{ //direct string so just use
            return data;
        }
    }
    
    private static ColorSpaceObject getColObjectFromRefOrDirect(final PdfObjectReader currentPdfFile, final byte[] data) {
        
        final ColorSpaceObject colObj  = new ColorSpaceObject(new String(data));

        if(data[0]=='<') {
            colObj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            colObj.setStatus(PdfObject.UNDECODED_REF);
        }
        colObj.setUnresolvedData(data,PdfDictionary.ColorSpace);
        
        final ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());
        objectDecoder.checkResolved(colObj);
        
        return colObj;
    }
    
    private static GenericColorSpace getColorspace(final int ID, final PdfArrayIterator colorSpace, final PdfObjectReader currentPdfFile) {
        
        //no DeviceRGB as set as default
        final GenericColorSpace currentColorData;
         
        switch(ID){
            case ColorSpaces.Separation:
                currentColorData= getDeviceNColorSpace(currentPdfFile, colorSpace, ID); 
                break;
                
            case ColorSpaces.DeviceN:
               currentColorData= getDeviceNColorSpace(currentPdfFile, colorSpace, ID); 
                break;
                
            case ColorSpaces.DeviceGray:
                currentColorData=new DeviceGrayColorSpace();
                break;
                
            case ColorSpaces.DeviceCMYK:
                currentColorData=new DeviceCMYKColorSpace();
                currentColorData.setRawColorSpace(ColorSpaces.DeviceCMYK);
                break;
                
            case ColorSpaces.CalGray:
                currentColorData = getCalGrayColorspace(getColObjectFromRefOrDirect(currentPdfFile, colorSpace.getNextValueAsByte(true)));               
                break;
                
            case ColorSpaces.CalRGB:
                currentColorData = getCalRGBColorspace(getColObjectFromRefOrDirect(currentPdfFile, colorSpace.getNextValueAsByte(true)));
                break;
                
            case ColorSpaces.Lab:
                currentColorData = getLabColorspace(getColObjectFromRefOrDirect(currentPdfFile, colorSpace.getNextValueAsByte(true)));
                break;
                
            case ColorSpaces.ICC:
                currentColorData = getICCColorspace(getColObjectFromRefOrDirect(currentPdfFile, colorSpace.getNextValueAsByte(true)));
                break;
                
            case ColorSpaces.Pattern:            
                currentColorData = getPatternColorspace(colorSpace, currentPdfFile);              
                break;
                
            default:
                currentColorData=new DeviceRGBColorSpace();              
        }
        
        return currentColorData;
    }

    private static GenericColorSpace getPatternColorspace(final PdfArrayIterator colorSpace, final PdfObjectReader currentPdfFile) {
        
        final GenericColorSpace currentColorData;
        if(colorSpace.hasMoreTokens()){
            final GenericColorSpace patternColorSpace = getColorSpace(colorSpace, currentPdfFile);
            currentColorData=new PatternColorSpace(currentPdfFile,patternColorSpace);
        }else{
            currentColorData=new PatternColorSpace(currentPdfFile,new DeviceRGBColorSpace());
        }
        return currentColorData;
    }
    
    private static byte[][] convertColValueToKeyArray(final PdfObjectReader currentPdfFile, final byte[] alt) {
        
        int ptr=0;
        if(alt[0]=='['){
            ptr=1;
        }
        
        final KeyArray objDecoder=new KeyArray(currentPdfFile.getObjectReader(),ptr, alt);
        final OCObject obj=new OCObject(new String(alt)); //OCObject used OCOBject as contaisn a KeyArray object
        objDecoder.readArray(obj, PdfDictionary.Configs); //any value which is a key array
        
        return obj.getKeyArray(PdfDictionary.Configs);
       
    }
   
    public static PdfArrayIterator convertColValueToMixedArray(final PdfObjectReader currentPdfFile, byte[] raw) {
        
        ColorSpaceObject obj=new ColorSpaceObject(new String(raw));
        
        final PdfFileReader objectReader=currentPdfFile.getObjectReader();
        
        int startArray=0;
        
        if(StreamReaderUtils.isRef(raw, 0)){ //indirect so needs decoding
            
            final int[] values=StreamReaderUtils.readRefFromStream(raw,0);
            
            final int ref=values[0];
            final int generation=values[1];
            //final int i=values[2]; //added for completeness but not needed in this case
            
            //read the Dictionary data
            raw = objectReader.readObjectAsByteArray(obj, objectReader.isCompressed(ref, generation), ref, generation);

            obj=new ColorSpaceObject(new String(raw));
            
            while(raw[startArray]!='['){
                startArray++;
            }
        
        }
        
        final Array objDecoder=new Array(objectReader, startArray, PdfDictionary.VALUE_IS_MIXED_ARRAY,raw);
        
        objDecoder.readArray(obj, PdfDictionary.ColorSpace);
        
        return obj.getMixedArray(PdfDictionary.ColorSpace);
        
    }
   
    private ColorspaceFactory(){}
  
    private static GenericColorSpace getICCColorspace(final PdfObject colorSpace) {
        
        GenericColorSpace currentColorData=new DeviceRGBColorSpace();
        
        final int alt=colorSpace.getParameterConstant(PdfDictionary.Alternate);
        if(alt!= ColorSpaces.DeviceGray){
            currentColorData=new ICCColorSpace(colorSpace);
        }
        
        currentColorData = getAlternateICCColorSpace(currentColorData, alt);
        
        currentColorData.setRawColorSpace(ColorSpaces.ICC);
        
        return currentColorData;
    }

    private static GenericColorSpace getAlternateICCColorSpace(GenericColorSpace currentColorData, final int alt) {
        
        final int type=currentColorData.getType();
        
        //use if alterative can be used as MUCH faster...
        if(alt==ColorSpaces.DeviceCMYK && currentColorData.isInvalid()){
            currentColorData=new DeviceCMYKColorSpace();
            currentColorData.setAlternateColorSpace(alt);
        }else if(type==ColorSpace.TYPE_CMYK){
            currentColorData=new DeviceCMYKColorSpace();
            currentColorData.setAlternateColorSpace(alt);
        }else if(type==ColorSpace.TYPE_RGB){
            currentColorData=new DeviceRGBColorSpace();
            currentColorData.setAlternateColorSpace(alt);
        }else if(type==ColorSpace.TYPE_GRAY){
            currentColorData=new DeviceGrayColorSpace();
            currentColorData.setAlternateColorSpace(alt);
        }
        return currentColorData;
    }
    
    private static GenericColorSpace getLabColorspace(final PdfObject colorSpace) {
        
        float[] R = { -100f,100f, -100.0f, 100.0f };
        float[] W = { 0.0f, 1.0f, 0.0f };
        
        //float[] blackpointArray=colorSpace.getFloatArray(PdfDictionary.BlackPoint);
        final float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
        final float[] rangeArray=colorSpace.getFloatArray(PdfDictionary.Range);
        
        if (whitepointArray != null) {
            W=whitepointArray;
        }
        
        if (rangeArray != null) {
            R = rangeArray;
        }
        
        return new LabColorSpace(W, R);
    }
    
    private static GenericColorSpace getCalRGBColorspace(final PdfObject colorSpace) {
        
        float[] W = { 1.0f, 1.0f, 1.0f };
//        float[] B = { 0.0f, 0.0f, 0.0f };
        float[] G = { 1.0f, 1.0f, 1.0f };
        float[] Ma = { 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f };
        
        final float[] gammaArray=colorSpace.getFloatArray(PdfDictionary.Gamma);
        final float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
//        final float[] blackpointArray=colorSpace.getFloatArray(PdfDictionary.BlackPoint);
        final float[] matrixArray=colorSpace.getFloatArray(PdfDictionary.Matrix);
        
        if (whitepointArray != null) {
            W=whitepointArray;
        }
        
        if (gammaArray != null) {
            G = gammaArray;
        }
        
        if (matrixArray != null) {
            Ma = matrixArray;
        }
        
        return new CalRGBColorSpace(W,Ma,G);        
    }
    
    private static GenericColorSpace getCalGrayColorspace(final PdfObject colorSpace) {
        
        float[] W = { 0.0f};
        float[] G = { 1.0f};
        
        float[] gammaArray=null;
        final float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
        final float[] rawGamma=colorSpace.getFloatArray(PdfDictionary.Gamma);
        if(rawGamma!=null){
            gammaArray=rawGamma;
        }
        
        if (whitepointArray != null) {
            W=whitepointArray;
        }
        
        if (gammaArray != null) {
            G = gammaArray;
        }
        
        return new CalGrayColorSpace(W, G);
    }
     
    private static GenericColorSpace getDeviceNColorSpace(final PdfObjectReader currentPdfFile, final PdfArrayIterator colorSpace, final int ID) {
     
        int componentCount=1;
        
        final byte[] name=colorSpace.getNextValueAsByte(true);
        
        final GenericColorSpace altCS = getColorSpace(colorSpace, currentPdfFile);
       
        final PdfObject functionObj=getFunctionObjectFromRefOrDirect(currentPdfFile, colorSpace.getNextValueAsByte(true));
       
        //name of color if separation or Components if device and component count
        if(ID!=ColorSpaces.Separation){
            final byte[][] components=convertColValueToKeyArray(currentPdfFile,name);
            componentCount=components.length;
        }

        final ColorMapping colorMapper=new ColorMapping(currentPdfFile,functionObj);
        final float[] domain=functionObj.getFloatArray(PdfDictionary.Domain);
        
        if(ID==ColorSpaces.Separation){
            return new SeparationColorSpace(colorMapper,domain, altCS);
        }else{
            return new DeviceNColorSpace(componentCount, colorMapper, domain, altCS);
        }
    }
}
