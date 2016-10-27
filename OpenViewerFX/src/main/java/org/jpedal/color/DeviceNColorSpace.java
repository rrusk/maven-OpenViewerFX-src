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
 * DeviceNColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.Map;

/**
 * handle Device ColorSpace
 */
public class DeviceNColorSpace extends SeparationColorSpace {
    
    private static final long serialVersionUID = -1372268945371555187L;
    
    private final Map<Integer, Integer> cache=new HashMap<Integer, Integer>();
            
    private float [] oldValues;
     
    public DeviceNColorSpace(final int componentCount, final ColorMapping colorMapper,final float[] domain, final GenericColorSpace altCS){
        
        setType(ColorSpaces.DeviceN);
        
        this.componentCount=componentCount;
        this.colorMapper=colorMapper;
        this.domain=domain;
        this.altCS=altCS;
        
    }
    
    /** set color (translate and set in alt colorspace) */
    @Override
    public void setColor(final String[] operand, final int opCount) {
        
        final float[] values = new float[opCount];
        for(int j=0;j<opCount;j++) {
            values[j] = Float.parseFloat(operand[j]);
        }
        
        setColor(values,opCount);
    }
    
    /** set color (translate and set in alt colorspace */
    @Override
    public void setColor(final float[] raw, final int opCount) {        
        if (oldValues != null && isSame(raw, oldValues)) {

        } else {
            final float[] operand = colorMapper.getOperandFloat(raw);
            altCS.setColor(operand, operand.length);
            oldValues = raw.clone();
        }        
    }
    
    /**
     * convert separation stream to RGB and return as an image
     */
    @Override
    public BufferedImage  dataToRGB(final byte[] data, final int w, final int h) {
        
        return createImage(w, h, data);

    }
    
    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEGToRGBImage(final byte[] data, final int ww, final int hh, final int pX, final int pY) {
        
        BufferedImage image=null;

        Raster ras= JPEGDecoder.getRasterFromJPEG(data, "JPEG");

        if(ras!=null){
            ras=cleanupRaster(ras,pX,pY, componentCount);
            final int w=ras.getWidth();
            final int h=ras.getHeight();

            final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();

            //convert the image
            image=createImage(w, h, rgb.getData());
        }

        return image;
    }
    
    /**
     * turn raw data into an image
     */
    @Override
    BufferedImage createImage(final int w, final int h, final byte[] rawData) {
        
        final BufferedImage image;
        
        final byte[] rgb=new byte[w*h*3];
        
        final int bytesCount=rawData.length;
        
        //convert data to RGB format
        final int byteCount= rawData.length/componentCount;
        
        final float[] values=new float[componentCount];
                
        int j=0,j2=0;
        
        for(int i=0;i<byteCount;i++){
            
            if(j>=bytesCount) {
                break;
            }
            
            for(int comp=0;comp<componentCount;comp++){
                values[comp]=((rawData[j] & 255)/255f);
                j++;
            }

            setColor(values,componentCount);
            
            //set values
            final int foreground =altCS.currentColor.getRGB();
            
            rgb[j2]=(byte) ((foreground>>16) & 0xFF);
            rgb[j2+1]=(byte) ((foreground>>8) & 0xFF);
            rgb[j2+2]=(byte) ((foreground) & 0xFF);
            
            j2 += 3;
            
        }
        
        //create the RGB image
        final int[] bands = {0,1,2};
        image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        final DataBuffer dataBuf=new DataBufferByte(rgb, rgb.length);
        final Raster raster =Raster.createInterleavedRaster(dataBuf,w,h,w*3,3,bands,null);
        image.setData(raster);
        
        return image;
    }
    
    private static boolean isSame(float[]a , float []b){
        for (int i = 0; i < a.length; i++) {
            if(a[i]!= b[i]){
                return false;
            }
        }
        return true;
    }
    
    public byte[] getRGBBytes(final byte[] rawData,final int w, final int h) {             
        final byte[] rgb=new byte[w*h*3];        
        final int bytesCount=rawData.length;
        final int byteCount= rawData.length/componentCount;
        final float[] values=new float[componentCount];
        int j=0,j2=0;
        for(int i=0;i<byteCount;i++){
            if(j>=bytesCount) {
                break;
            }
            for(int comp=0;comp<componentCount;comp++){
                values[comp]=((rawData[j] & 255)/255f);
                j++;
            }
            setColor(values,componentCount);
            final int foreground =altCS.currentColor.getRGB();
            rgb[j2]=(byte) ((foreground>>16) & 0xFF);
            rgb[j2+1]=(byte) ((foreground>>8) & 0xFF);
            rgb[j2+2]=(byte) ((foreground) & 0xFF);
            j2 += 3;            
        }
        return rgb;
    }
}
