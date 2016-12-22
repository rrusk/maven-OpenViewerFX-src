
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
 * JPEGDecoder.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.jpedal.JDeliHelper;
import static org.jpedal.color.GenericColorSpace.cleanupRaster;
import org.jpedal.objects.raw.MaskObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.utils.ArrayUtils;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class JPEGDecoder {
   
    public static void write(final BufferedImage image, final String type, final String des) {
        
        try {
            final BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(new File(des)));
            ImageIO.write(image, type, bos);
            bos.flush();
            bos.close();
        } catch (final IOException e) {
            LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }
    
    public static void write(final BufferedImage image, final String type, final OutputStream bos) {
        
        try {
            ImageIO.write(image, type, bos);
            bos.flush();
            bos.close();
        } catch (final IOException e) {
            LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }
    
    public static Raster getRasterFromJPEG(final byte[] data, final String type) {
        
        final ByteArrayInputStream in;
        
        ImageReader iir=null;
        final ImageInputStream iin;
        
        Raster ras=null;
        
        try {
            
            //read the image data
            in = new ByteArrayInputStream(data);
            
            //suggestion from Carol
            final Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(type);
            
            while (iterator.hasNext()){
                final ImageReader o = iterator.next();
                iir = o;
                if (iir.canReadRaster()) {
                    break;
                }
            }
            
            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            ras=iir.readRaster(0, null);
            
            in.close();
            iir.dispose();
            iin.close();
            
        }catch(final Exception ee){
            LogWriter.writeLog("Problem closing  " + ee);
        }
        
        return ras;
    }
    
    static BufferedImage grayJPEGToRGBImage(final byte[] data, final int pX, final int pY) {
        
        BufferedImage image=null;
        
        try {
            
            Raster ras= JPEGDecoder.getRasterFromJPEG(data, "JPEG");
            
            if(ras!=null){
                ras=cleanupRaster(ras,pX,pY,1); //note uses 1 not count
                
                final int w = ras.getWidth();
                final int h = ras.getHeight();

                final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
                final byte[] rawData=rgb.getData();
                
                final int byteLength=rawData.length;
                final byte[] rgbData=new byte[byteLength*3];
                int ptr=0;
                for(int ii=0;ii<byteLength;ii++){
                    
                    //if(arrayInverted){ //flip if needed
                    //    rawData[ii]=(byte) (rawData[ii]^255);
                    //}
                    
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    
                }
                
                final int[] bands = {0,1,2};
                image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                final Raster raster =Raster.createInterleavedRaster(new DataBufferByte(rgbData, rgbData.length),w,h,w*3,3,bands,null);
                
                image.setData(raster);
            }
            
        } catch (final Exception ee) {
            image = null;
            
            LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
        }
        
        return image;
    }
   
    public static byte[] getBytesFromJPEGWithImageIO(final byte[] data, GenericColorSpace decodeColorData,final PdfObject XObject) {
        
        byte[] db=null;
        Raster ras=null;
        
        try {
            BufferedImage img=decodeColorData.JPEGToRGBImage(data, XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), -1, -1);
            
            //System.out.println(decodeColorData+" "+img);
            if(img.getType()==BufferedImage.TYPE_INT_RGB){ //we need byte in rgb
                img=org.jpedal.io.ColorSpaceConvertor.convertColorspace(img, BufferedImage.TYPE_3BYTE_BGR);
                
                if(img!=null){
                    ras= img.getData();
                }
                db=((DataBufferByte)ras.getDataBuffer()).getData();
                
                //switch order
                byte r,g,b;
                for(int i=0;i<db.length;i += 3){
                    b=db[i];
                    g=db[i+1];
                    r=db[i+2];
                    
                    db[i]=r;
                    db[i+1]=g;
                    db[i+2]=b;
                }
            }else{ //simple case
                
                if(img!=null){
                    ras= img.getData();
                }
                db=((DataBufferByte)ras.getDataBuffer()).getData();
            }
            
        } catch (Exception e) {
            LogWriter.writeLog("Exception "+e+" with JPeg Image ");
        }
        
        return db;
        
    }
    
    //our version
    public static byte[] getBytesFromJPEG(final byte[] data, GenericColorSpace decodeColorData,final PdfObject XObject) {
        
        byte[] db=null;
        try {
            
            boolean isInverted = ArrayUtils.isArrayInverted(XObject.getFloatArray(PdfDictionary.Decode));
            boolean isMask=XObject instanceof MaskObject;
            boolean isDeviceN = decodeColorData.getType()==ColorSpaces.DeviceN;
           
            if(!isDeviceN){
                try{
                    db = JDeliHelper.getBytesFromJPEG(isInverted, data, isMask);
                }catch(Exception e){ //case 24799 try to fix the file in jdeli if time permits
                    LogWriter.writeLog("Jpeg Data Corrupted Switching to Old Compression "+e);
                    db=getBytesFromJPEGWithImageIO(data, decodeColorData, XObject);   
                }
            }
            if(db==null){
                db=getBytesFromJPEGWithImageIO(data, decodeColorData, XObject);   
            }
           
        } catch (Exception e) {
            LogWriter.writeLog("Exception with JPeg Image "+e);
        }
        
        return db;
        
    }
    
    public static byte[] getUnconvertedBytesFromJPEG(byte [] data, int  adobeColorTransform) throws Exception{
        return JDeliHelper.getUnconvertedBytesFromJPEG(data, adobeColorTransform);     
    }
}


