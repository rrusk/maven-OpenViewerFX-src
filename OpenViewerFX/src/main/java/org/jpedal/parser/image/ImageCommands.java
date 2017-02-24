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
 * ImageCommands.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jpedal.color.ColorSpaces;
import org.jpedal.constants.PDFflags;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.security.DecryptionFactory;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.FunctionObject;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;

public class ImageCommands {
    
    public static final int ID=0;
    
    public static final int XOBJECT=2;
    
    public static boolean trackImages;
    
    public static boolean rejectSuperimposedImages=true;
    
    static{
        final String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
        if(operlapValue!=null) {
            ImageCommands.rejectSuperimposedImages = (operlapValue.toLowerCase().contains("true"));
        }
        
        //hidden value to turn on function
        final String imgSetting=System.getProperty("org.jpedal.trackImages");
        if(imgSetting!=null) {
            trackImages = (imgSetting.toLowerCase().contains("true"));
        }    
    }
    
    /**
     */
    static byte[] getMaskColor(final GraphicsState gs) {
        
        final byte[] maskCol=new byte[4];
        
        final int foreground =gs.nonstrokeColorSpace.getColor().getRGB();
        maskCol[0]=(byte) ((foreground>>16) & 0xFF);
        maskCol[1]=(byte) ((foreground>>8) & 0xFF);
        maskCol[2]=(byte) ((foreground) & 0xFF);
        
        return maskCol;
    }
    
    /**
     * Test whether the data representing a line is uniform along it height
     */
    static boolean isRepeatingLine(final byte[] lineData, final int height)
    {
        if(lineData.length % height != 0) {
            return false;
        }
        
        final int step = lineData.length / height;
        
        for(int x = 0; x < (lineData.length / height) - 1; x++) {
            int targetIndex = step;
            while(targetIndex < lineData.length - 1) {
                if(lineData[x] != lineData[targetIndex]) {
                    return false;
                }
                targetIndex += step;
            }
        }
        return true;
    }
    
    static BufferedImage addBackgroundToMask(BufferedImage image, final boolean isMask) {
        
        if(isMask){
            
            final int cw = image.getWidth();
            final int ch = image.getHeight();
            
            final BufferedImage background=new BufferedImage(cw,ch,BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2 = background.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, cw, ch);
            g2.drawImage(image,0,0,null);
            image=background;
            
        }
        return image;
    }
    
    /**
     * apply TR
     */
    static void applyTR(final ImageData imageData, final Object[] TR, final PdfObjectReader currentPdfFile) {

        try {
            final PDFFunction[] functions = getFunctions(TR, currentPdfFile);

            if (functions!=null) {

                final int w = imageData.getWidth();
                final int h = imageData.getHeight();
                final byte[] data = imageData.getObjectData();

                final int compCount = imageData.getCompCount();

                //System.out.println(data.length + " " + w + " " + h + " " + (data.length / (w * h)) + " " + compCount);

                int ptr = 0;

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {

                        for (int a = 0; a < compCount; a++) {
                            final float[] raw = {(data[ptr] & 255) / 255f};

                            if (functions[a] != null) {
                                final float[] processed = functions[a].compute(raw);

                                data[ptr] = (byte) (255 * processed[0]);
                            }
                            ptr++;

                        }
                    }
                }

                //imageData.setObjectData(data);
            }
        } catch (final Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    
    private static PDFFunction[] getFunctions(final Object[] TRvalues, final PdfObjectReader currentPdfFile) {
        
        PDFFunction[] functions =null;

        int total=0;
        final byte[][] kidList = (byte[][]) TRvalues[1];
        if(kidList!=null) {
            total = kidList.length;
            functions =new PDFFunction[total];
        }
        
        
        //read functions
        for(int count=0;count<total;count++){
            
            if(kidList[count]==null) {
                continue;
            }
            
            final String ref=new String(kidList[count]);
            PdfObject Function=new FunctionObject(ref);
            
            //handle /Identity as null or read
            final byte[] possIdent=kidList[count];
            if(possIdent!=null && possIdent.length>4 && possIdent[0]==47 &&  possIdent[1]==73 && possIdent[2]==100 &&  possIdent[3]==101)//(/Identity
            {
                Function = null;
            } else {
                currentPdfFile.readObject(Function);
            }
            
            if(Function!=null) {
                functions[count] = FunctionFactory.getFunction(Function, currentPdfFile);
            }
            
        }
        return functions;
    }
    
    /**
     * apply DecodeArray
     */
    public static void applyDecodeArray(final byte[] data, final int d, final float[] decodeArray, final int type) {
        
        final int count = decodeArray.length;
        
        int maxValue=0;
        for (final float aDecodeArray : decodeArray) {
            if (maxValue < aDecodeArray) {
                maxValue = (int) aDecodeArray;
            }
        }
        
        /*
         * see if will not change output
         * and ignore if unnecessary
         */
        boolean isIdentify=true; //assume true and disprove
        final int compCount=decodeArray.length;
        
        for(int comp=0;comp<compCount;comp += 2){
            if((decodeArray[comp]!=0.0f)||((decodeArray[comp+1]!=1.0f)&&(decodeArray[comp+1]!=255.0f))){
                isIdentify=false;
                comp=compCount;
            }
        }
        
        if(isIdentify) {
            return;
        }
        
        if(d==1){ //bw straight switch (ignore gray)
            
            //changed for /baseline_screens/11dec/Jones contract for Dotloop.pdf
            if(decodeArray[0]>decodeArray[1]){
                
                //if(type!=ColorSpaces.DeviceGray){// || (decodeArray[0]>decodeArray[1] && XObject instanceof MaskObject)){
                final int byteCount=data.length;
                for(int ii=0;ii<byteCount;ii++){
                    data[ii]=(byte) ~data[ii];
                    
                }
            }

        }else if((d==8 && maxValue>1)&&(type==ColorSpaces.DeviceRGB || type==ColorSpaces.CalRGB || type==ColorSpaces.DeviceCMYK)){
            
            int j=0;
            
            for(int ii=0;ii<data.length;ii++){
                int currentByte=(data[ii] & 0xff);
                if(currentByte<decodeArray[j]) {
                    currentByte = (int) decodeArray[j];
                } else if(currentByte>decodeArray[j+1]) {
                    currentByte = (int) decodeArray[j + 1];
                }
                
                j += 2;
                if(j==decodeArray.length) {
                    j = 0;
                }
                data[ii]=(byte)currentByte;
            }
        }else if (d == 8 && maxValue == 1 && type == ColorSpaces.DeviceCMYK) {

            final int[] tempDecode = new int[decodeArray.length];
            for (int i = 0; i < decodeArray.length; i++) {
                tempDecode[i] = (int) (decodeArray[i] * 255);
            }
            int j = 0;
            for (int ii = 0; ii < data.length; ii++) {
                int pp = (data[ii] & 0xff);
                pp = (pp * (tempDecode[j + 1] - tempDecode[j]) / 255) + tempDecode[j];
                j += 2;
                if (j == decodeArray.length) {
                    j = 0;
                }
                data[ii] = (byte) pp;
            }
        }else{
            /*
             * apply array
             *
             * Assumes black and white or gray colorspace
             * */
            maxValue = (d<< 1);
            final int divisor = maxValue - 1;
            
            for(int ii=0;ii<data.length;ii++){
                final byte currentByte=data[ii];
                
                int dd=0;
                int newByte=0;
                int min=0,max=1;
                for(int bits=7;bits>-1;bits--){
                    int current=(currentByte >> bits) & 1;
                    
                    current =(int)(decodeArray[min]+ (current* ((decodeArray[max] - decodeArray[min])/ (divisor))));
                    
                    if (current > maxValue) {
                        current = maxValue;
                    }
                    if (current < 0) {
                        current = 0;
                    }
                    
                    current=((current & 1)<<bits);
                    
                    newByte += current;
                    
                    //rotate around array
                    dd += 2;
                    
                    if(dd==count){
                        dd=0;
                        min=0;
                        max=1;
                    }else{
                        min += 2;
                        max += 2;
                    }
                }
                
                data[ii]=(byte)newByte;
                
            }
        }
    }
    
    static boolean isExtractionAllowed(final PdfObjectReader currentPdfFile) {
        
        final PdfFileReader objectReader=currentPdfFile.getObjectReader();
        
        final DecryptionFactory decryption=objectReader.getDecryptionObject();
        
        return decryption==null || decryption.getBooleanValue(PDFflags.IS_EXTRACTION_ALLOWED);
        
    }
}
