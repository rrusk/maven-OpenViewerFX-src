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
 * OneBitDownSampler.java
 * ---------------
 */

package org.jpedal.parser.image.downsample;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
class OneBitDownSampler {
    
    static GenericColorSpace resetSeparationColorSpace(final byte[] index,final ImageData imageData,final byte[] data) {
        
        //needs to have these settings if 1 bit not indexed
        if(index==null && imageData.getDepth()==1){
            imageData.setCompCount(1);
            
            invertBytes(data);
        }
        
        return new DeviceRGBColorSpace();
    }
    
       
    public static GenericColorSpace downSample(int sampling, final ImageData imageData, GenericColorSpace decodeColorData) {

        final byte[] data=imageData.getObjectData();
        
        int newW=imageData.getWidth()/sampling;
        int newH=imageData.getHeight()/sampling;
        
        int size=newW*newH;
        
        final byte[] newData=new byte[size];
        
        final int[] flag={1,2,4,8,16,32,64,128};
        
        final int origLineLength= (imageData.getWidth()+7)>>3;

        //scan all pixels and down-sample
        for(int y=0;y<newH;y++){
            for(int x=0;x<newW;x++){

                //allow for edges in number of pixels left
                int wCount=sampling,hCount=sampling;
                final int wGapLeft=imageData.getWidth()-x;
                final int hGapLeft=imageData.getHeight()-y;
                if(wCount>wGapLeft) {
                    wCount = wGapLeft;
                }
                if(hCount>hGapLeft) {
                    hCount = hGapLeft;
                }
                
                //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                final int bytes = getPixelSetCount(sampling, false, data, flag, origLineLength, y, x, wCount, hCount);

                final int count=(wCount*hCount);

                //set value as white or average of pixels
                final int offset=x+(newW*y);
                
                if(count>0){
                    newData[offset] = (byte) ((255 * bytes) / count);
                    
                }else{                   
                    newData[offset] = (byte) 255;
                    
                }
            }
        }
       
        imageData.setWidth(newW);
        imageData.setHeight(newH);
        decodeColorData.setIndex(null, 0);
        
        //remap Separation as already converted here
        if(decodeColorData.getID()==ColorSpaces.Separation || decodeColorData.getID()==ColorSpaces.DeviceN){
            decodeColorData=new DeviceRGBColorSpace();
            
            imageData.setCompCount(1);                
            invertBytes(newData);           
        }
        
        imageData.setObjectData(newData);
        
        imageData.setDepth(8);
        
        return decodeColorData;
    }

    private static void invertBytes(final byte[] newData) {
        final int count=newData.length;
        for(int aa=0;aa<count;aa++) {
            newData[aa] = (byte) (newData[aa] ^ 255);
        }
    }
   
    public static GenericColorSpace downSampleMask(int sampling, final ImageData imageData,
            final byte[] maskCol, GenericColorSpace decodeColorData) {

        final byte[] data=imageData.getObjectData();
        
        int newW=imageData.getWidth()/sampling;
        int newH=imageData.getHeight()/sampling;
        
        int size=newW*newH*4;
        
        maskCol[3]=(byte)255;
        
        final byte[] newData=new byte[size];
        
        final int[] flag={1,2,4,8,16,32,64,128};
        
        final int origLineLength= (imageData.getWidth()+7)>>3;

        //scan all pixels and down-sample
        for(int y=0;y<newH;y++){
            for(int x=0;x<newW;x++){

                //allow for edges in number of pixels left
                int wCount=sampling,hCount=sampling;
                final int wGapLeft=imageData.getWidth()-x;
                final int hGapLeft=imageData.getHeight()-y;
                if(wCount>wGapLeft) {
                    wCount = wGapLeft;
                }
                if(hCount>hGapLeft) {
                    hCount = hGapLeft;
                }
                
                //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                final int bytes = getPixelSetCount(sampling, true, data, flag, origLineLength, y, x, wCount, hCount);

                final int count=(wCount*hCount);

                //set value as white or average of pixels
                final int offset=x+(newW*y);
                
                if(count>0){                    
                    for(int ii=0;ii<4;ii++){
                        newData[(offset * 4) + ii] = (byte) ((((maskCol[ii] & 255) * bytes) / count));
                    }                                           
                }else{
                    
                    for(int ii=0;ii<3;ii++) {
                        newData[(offset * 4) + ii] = (byte) 0;
                    }                   
                }
            }
        }
        
        imageData.setWidth(newW);
        imageData.setHeight(newH);
        
        //remap Separation as already converted here
        if(decodeColorData.getID()==ColorSpaces.Separation || decodeColorData.getID()==ColorSpaces.DeviceN){
            decodeColorData=new DeviceRGBColorSpace();
            
            imageData.setCompCount(1);
            invertBytes(newData);           
        }
        
        imageData.setObjectData(newData);
        
        imageData.setDepth(8);
        
        return decodeColorData;
    }

    
    public static GenericColorSpace downSampleIndexed(int sampling, final ImageData imageData,
            final byte[] index, GenericColorSpace decodeColorData) {

        byte[] data=imageData.getObjectData();
        
        int newW=imageData.getWidth()/sampling;
        int newH=imageData.getHeight()/sampling;
        
        int size=newW*newH*3;
        
        final byte[] newData=new byte[size];
        
        final int[] flag={1,2,4,8,16,32,64,128};
        
        final int origLineLength= (imageData.getWidth()+7)>>3;

        //scan all pixels and down-sample
        for(int y=0;y<newH;y++){
            for(int x=0;x<newW;x++){

                //allow for edges in number of pixels left
                int wCount=sampling,hCount=sampling;
                final int wGapLeft=imageData.getWidth()-x;
                final int hGapLeft=imageData.getHeight()-y;
                if(wCount>wGapLeft) {
                    wCount = wGapLeft;
                }
                if(hCount>hGapLeft) {
                    hCount = hGapLeft;
                }
                
                //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                final int bytes = getPixelSetCount(sampling, false, data, flag, origLineLength, y, x, wCount, hCount);

                final int count=(wCount*hCount);

                //set value as white or average of pixels
                final int offset=x+(newW*y);
                
                if(count>0){                   
                    int av;

                    for(int ii=0;ii<3;ii++){

                        //can be in either order so look at index
                        if(index[0]==-1 && index[1]==-1 && index[2]==-1){
                            av=(index[ii] & 255) +(index[ii+3] & 255);
                            newData[(offset*3)+ii]=(byte)(255-((av *bytes)/count));
                        }else{//  if(decodeColorData.getID()==ColorSpaces.DeviceCMYK){  //avoid color 'smoothing' - see CustomersJune2011/lead base paint.pdf
                            final float ratio=bytes/count;
                            if(ratio>0.5) {
                                newData[(offset * 3) + ii] = index[ii + 3];
                            } else {
                                newData[(offset * 3) + ii] = index[ii];
                            }
                        }
                    }                   
                }else{                   
                    for(int ii=0;ii<3;ii++) {
                        newData[((offset) * 3) + ii] = 0;
                    }                   
                }
            }
        }
        imageData.setCompCount(3);
        
        imageData.setWidth(newW);
        imageData.setHeight(newH);
        decodeColorData.setIndex(null, 0);
        
        //remap Separation as already converted here
        if(decodeColorData.getID()==ColorSpaces.Separation || decodeColorData.getID()==ColorSpaces.DeviceN){
            decodeColorData=new DeviceRGBColorSpace();
        }
        
        imageData.setObjectData(newData);
        
        imageData.setDepth(8);
        
        return decodeColorData;
    }

    private static int getPixelSetCount(final int sampling, boolean imageMask, final byte[] data, final int[] flag, final int origLineLength,
                                        final int y, final int x, final int wCount, final int hCount) {

        byte currentByte;
        int bit;
        int bytes=0;
        int ptr;

        for(int yy=0;yy<hCount;yy++){
            for(int xx=0;xx<wCount;xx++){

                ptr=((yy+(y*sampling))*origLineLength)+(((x*sampling)+xx)>>3);

                if(ptr<data.length){
                    currentByte=data[ptr];
                }else{
                    currentByte=0;
                }

                if(imageMask) {
                    currentByte = (byte) (currentByte ^ 255);
                }

                bit=currentByte & flag[7-(((x*sampling)+xx)& 7)];

                if(bit!=0) {
                    bytes++;
                }
            }
        }
        return bytes;
    }
}
