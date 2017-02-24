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
 * SMaskDecoder.java
 * ---------------
 */

package org.jpedal.parser.image.mask;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.JPEGDecoder;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.JPeg2000ImageDecoder;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
public class SMaskDecoder {
    
    public static byte[] applyJPX_JBIG_Smask(final ImageData imageData, final ImageData smaskData, byte[] maskData, final PdfObject imageObject, final PdfObject maskObject, final GenericColorSpace colorSpace, final GenericColorSpace maskCS){
        byte[] objectData = imageData.getObjectData();
        int iw=imageData.getWidth();
        int ih=imageData.getHeight();
        final int id=imageData.getDepth();
        
        float[] matte = maskObject.getFloatArray(PdfDictionary.Matte);
        
        smaskData.getFilter(maskObject);
        if(smaskData.isDCT()){
            maskData=JPEGDecoder.getBytesFromJPEG(maskData,maskCS,maskObject);
            maskObject.setMixedArray(PdfDictionary.Filter,null);
            maskObject.setDecodedStream(maskData);
        }else if(smaskData.isJPX()){
            maskData=JPeg2000ImageDecoder.getBytesFromJPEG2000(maskData);
            maskObject.setMixedArray(PdfDictionary.Filter,null);
            maskObject.setDecodedStream(maskData);
        }else {
            objectData = ColorSpaceConvertor.normaliseTo8Bit(id, iw, ih, objectData);
        }
                
        final int sw=smaskData.getWidth();
        final int sh=smaskData.getHeight();
        final int sd=smaskData.getDepth();
        
        final float [] decodeArr = maskObject.getFloatArray(PdfDictionary.Decode);
                
        if(decodeArr!=null && decodeArr[0]==1 && decodeArr[1]==0){ // data inverted refer to dec2011/example.pdf
            for (int i = 0; i < maskData.length; i++) {
                maskData[i]^= 0xff;
            }
        }
        
        byte[] index=colorSpace.getIndexedMap();
       
        if(index!=null){
            index=colorSpace.convertIndexToRGB(index);
            int nComp = imageData.getCompCount();
            nComp = colorSpace.getID()==ColorSpaces.DeviceGray ? 3 : nComp;            
            objectData=ColorSpaceConvertor.convertIndexToRGBByte(index, iw, ih, nComp, id, objectData, false, false);
        } else if (imageData.isDCT() || imageData.isJPX() || imageData.isJBIG()) {      
         
        } else if(colorSpace.getID()==ColorSpaces.DeviceGray){
            objectData=colorSpace.dataToRGBByteArray(objectData,iw,ih);
            if(matte!=null){
                matte = new float[]{matte[0],matte[0],matte[0]};
            }
        } else if(colorSpace.getID()==ColorSpaces.CalRGB){
        } else if(colorSpace.getID()==ColorSpaces.DeviceRGB){
        } else {
            objectData=colorSpace.dataToRGBByteArray(objectData,iw,ih);   
        }
        
        maskData = ColorSpaceConvertor.normaliseTo8Bit(sd,sw, sh, maskData);
        
        int imageDim = iw*ih;
        final int maskDim = sw*sh;
        if(imageDim>maskDim){
            maskData  = getScaledBytes(maskData, sw, sh, iw, ih);            
        }else if(maskDim>imageDim){
            objectData = getScaledBytes(objectData, iw, ih, sw, sh);
            imageDim = maskDim;
            iw = sw;
            ih = sh;
        }else{
            //do nothing
        }
        int p = 0;
               
        final ByteBuffer buffer = ByteBuffer.allocate(iw*ih*4);
                        
        if (imageDim == objectData.length) {
            int aa = 0;
            for (int i = 0; i < imageDim; i++) {
                final byte r = objectData[i] ;
                for (int j = 0; j < 3; j++) {
                    buffer.put(r);
                }
                buffer.put(maskData[aa++]); // write out maskData[aa++] directly and lose a ?
            }
        } else {
            if(matte!=null){
                for (int i = 0; i < maskData.length; i++) {
                    final int a = maskData[i] & 0xff;
                    int r = objectData[p++] & 0xff;
                    int g = objectData[p++] & 0xff;
                    int b = objectData[p++] & 0xff;
                    
                    if(a!=0){
                        final double k = 255.0/a;
                        r = (int) ((r-matte[0])*k+matte[0]);
                        g = (int) ((g-matte[1])*k+matte[1]);
                        b = (int) ((b-matte[2])*k+matte[2]);

                        r = r < 0 ? 0 : r > 255 ? 255 : r;
                        g = g < 0 ? 0 : g > 255 ? 255 : g;
                        b = b < 0 ? 0 : b > 255 ? 255 : b;
                    }
                    final byte[] bb = {(byte)r,(byte)g,(byte)b,(byte)a};
                    buffer.put(bb);
                    
                }
            }else{
                final int expected = imageDim*3;
                if(objectData.length<expected){//odd cases where datastream is not enough
                    final byte[] temp = new byte[expected];
                    System.arraycopy(objectData, 0, temp, 0, objectData.length);
                    objectData = temp;
                }
                final int iter = Math.min(maskData.length, iw*ih);
                
                for (int i = 0; i < iter; i++) {
                    buffer.put(new byte[]{objectData[p++],objectData[p++],objectData[p++], maskData[i]});
                }
            }            
            
        }                
        
        imageObject.setIntNumber(PdfDictionary.Width,iw);
        imageObject.setIntNumber(PdfDictionary.Height,ih);
        imageObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
        
        return buffer.array();
    }
    
    private static byte[] getScaledBytes(final byte[] data, final int sw, final int sh, final int dw, final int dh){
        if(data.length == (sw*sh)){ //gray scale image
            return rescaleComponent(data, sw, sh, dw, dh);
        }else{//rgb image
            int dim = sw*sh;
            
            //sanity check
            final int maxSize=data.length/3;
            if(dim>maxSize){
                dim=maxSize;
            }
            byte[] rr = new byte[dim];
            byte[] gg = new byte[dim];
            byte[] bb = new byte[dim];
            int p = 0;
            for (int i = 0; i < dim; i++) {
                    rr[i] = data[p++];
                    gg[i] = data[p++];
                    bb[i] = data[p++];
            }
            rr = rescaleComponent(rr, sw, sh, dw, dh);
            gg = rescaleComponent(gg, sw, sh, dw, dh);
            bb = rescaleComponent(bb, sw, sh, dw, dh);
            
            p=0;
            dim = dw*dh;
            final byte[] temp = new byte[dim*3];
            for (int i = 0; i < dim; i++) {
                temp[p++] = rr[i];
                temp[p++] = gg[i];
                temp[p++] = bb[i];
            }
            return temp;
        }
    }
    
    private static byte[] rescaleComponent(byte[] data, final int sw, int sh, final int dw, final int dh){
        if(data.length==1){
            final byte a = data[0];
            data = new byte[dw*dh];
            Arrays.fill(data, a);
            return data;
        }else if(sh==1){
            final byte[] temp = new byte[2*sw];
            System.arraycopy(data, 0, temp, 0, sw);
            System.arraycopy(data, 0, temp, sw, sw);
            sh = 2;
            data = temp;
        }
        
        final float ratioW=sw/(float)dw;
        final float ratioH=sh/(float)dh;
        final byte[] combinedData=new byte[dw*dh];
        final int rawDataSize=data.length;
        int i = 0;
        
        try{
            for(int mY=0;mY<dh;mY++){
                for(int mX=0;mX<dw;mX++){
                    final int rgbPtr=(((int)(mX*ratioW)))+(((int)(mY*ratioH))*sw);
                    if(rgbPtr<rawDataSize){
                        combinedData[i]=data[rgbPtr];
                    }
                    i++;
                }
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
        //below is bilinear scaling algorithm
//        byte[] temp = new byte[dw * dh];
//        int A, B, C, D, index, yIndex, xr, yr, gray;
//        long x, y = 0, xDiff, yDiff, xDiffMinus, yDiffMinus;
//        int xRatio = ((sw - 1) << 16) / dw;
//        int yRatio = ((sh - 1) << 16) / dh;
//        int offset = 0;
//        for (int i = 0; i < dh; i++) {
//            yr = (int) (y >> 16);
//            yDiff = y - (yr << 16);
//            yDiffMinus = 65536 - yDiff;
//            yIndex = yr * sw;
//            x = 0;
//            for (int j = 0; j < dw; j++) {
//                xr = (int) (x >> 16);
//                xDiff = x - (xr << 16);
//                xDiffMinus = 65536 - xDiff;
//                index = yIndex + xr;
//
//                A = data[index] & 0xff;
//                B = data[index + 1] & 0xff;
//                C = data[index + sw] & 0xff;
//                D = data[index + sw + 1] & 0xff;
//
//                gray = (int) ((A * xDiffMinus * yDiffMinus
//                        + B * xDiff * yDiffMinus
//                        + C * yDiff * xDiffMinus
//                        + D * xDiff * yDiff) >> 32);
//
//                temp[offset++] = (byte) gray;
//
//                x += xRatio;
//            }
//            y += yRatio;
//        }
//        return temp;
    }
    
    public static byte[] applySMask(byte[] maskData, final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newSMask, final PdfObject XObject) {
        
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        final int w=imageData.getWidth();
        final int h=imageData.getHeight();
        final int d=imageData.getDepth();
        
        /*
        * Smask data (ASSUME single component at moment)
        */
        final int maskW=newSMask.getInt(PdfDictionary.Width);
        final int maskH=newSMask.getInt(PdfDictionary.Height);
        final int maskD=newSMask.getInt(PdfDictionary.BitsPerComponent);
        
        objectData = MaskDataDecoder.convertSmaskData(decodeColorData, objectData, w, h, imageData, d, maskD, maskData, newSMask);
        
        //needs to be 'normalised to 8  bit'
        if(maskD!=8){
            maskData=ColorSpaceConvertor.normaliseTo8Bit(maskD, maskW, maskH, maskData);
        }
        
        //add mask as a element so we now have argb
        if(w==maskW && h==maskH){
            //System.out.println("Same size");
            objectData=buildUnscaledByteArray(w, h, objectData, maskData);
        }else if(w<maskW){ //mask bigger than image
            //System.out.println("Mask bigger");
            objectData=upScaleImageToMask(w, h, maskW,maskH,objectData, maskData);
            
            XObject.setIntNumber(PdfDictionary.Width,maskW);
            XObject.setIntNumber(PdfDictionary.Height,maskH);
            
        }else{
            //System.out.println("Image bigger");
            objectData=upScaleMaskToImage(w, h, maskW,maskH,objectData, maskData);
        }
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
        
//        BufferedImage img= ColorSpaceConvertor.createARGBImage( XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), objectData);
//        
//        try{
//        ImageIO.write(img, "PNG", new java.io.File("/Users/markee/Desktop/img.png"));
//        }catch(Exception e){}
//        
        
        
        return objectData;
    }

    static void check4BitData(final byte[] objectData) {
        final int size=objectData.length;
        
        boolean is4Bit=true;
        
        for(final byte b:objectData){
            if(b<0 || b>15){
                is4Bit=false;
                break;
            }
        }
        
        if(is4Bit){
            for (int ii=0;ii<size;ii++){
                objectData[ii]=(byte) (objectData[ii]<<4);
            }
        }
    }
    
    
    private static byte[] upScaleMaskToImage(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr=0, aPtr;
        int i=0;
        final float ratioW=maskW/(float)w;
        final float ratioH=maskH/(float)h;
        final byte[] combinedData=new byte[w*h*4];
        
        final int rawDataSize=objectData.length;
        
        try{
            for(int iY=0;iY<h;iY++){
                for(int iX=0;iX<w;iX++){
                    
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    aPtr=(((int)(iX*ratioW)))+(((int)(iY*ratioH))*w);
                    
                    combinedData[i+3]=maskData[aPtr];
                    
                    i += 4;
                    
                }
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        return combinedData;
    }
    
    
    private static byte[] upScaleImageToMask(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr, aPtr=0;
        int i=0;
        final float ratioW=w/(float)maskW;
        final float ratioH=h/(float)maskH;
        final byte[] combinedData=new byte[maskW*maskH*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int mY=0;mY<maskH;mY++){
                for(int mX=0;mX<maskW;mX++){
                    
                    rgbPtr=(((int)(mX*ratioW))*3)+(((int)(mY*ratioH))*w*3);
                    
                    // System.err.println(mX+"/"+maskW+" "+mY+"/"+maskH+" "+ratioW+" mask="+((int)(mX*ratioW))+" "+((int)(mY*ratioH)));
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    if(aPtr<maskSize){
                        combinedData[i+3]=maskData[aPtr];
                        aPtr++;
                    }
                    
                    i += 4;
                    
                }
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
    
     public static byte[] getSMaskData(byte[] maskData, final ImageData smaskData, final PdfObject newSMask, final GenericColorSpace maskColorData) {
        smaskData.getFilter(newSMask);
       
        if(smaskData.isDCT()){
            maskData=JPEGDecoder.getBytesFromJPEG(maskData,maskColorData,newSMask);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }else if(smaskData.isJPX()){
            maskData=JPeg2000ImageDecoder.getBytesFromJPEG2000(maskData);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }
        return maskData;
    }
     
    private static byte[] buildUnscaledByteArray(final int w, final int h, final byte[] objectData, final byte[] maskData) {
        
        final int pixels=w*h*4;
        int rgbPtr=0, aPtr=0;
        final byte[] combinedData=new byte[w*h*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int i=0;i<pixels;i += 4){
                
                //rgb
                for(int comp=0;comp<3;comp++){
                    if(rgbPtr<rawDataSize){
                        combinedData[i+comp]=objectData[rgbPtr];
                    }
                    rgbPtr++;
                }
                
                if(aPtr<maskSize){
                    //System.out.println(maskData[aPtr]);
                    combinedData[i+3]=maskData[aPtr];
                    aPtr++;
                }
                
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
    
//    private static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
//        int imageWidth = image.getWidth();
//        int imageHeight = image.getHeight();
//        double scaleX = (double) width / imageWidth;
//        double scaleY = (double) height / imageHeight;
//        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
//        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
//        return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
//    }
    
}
