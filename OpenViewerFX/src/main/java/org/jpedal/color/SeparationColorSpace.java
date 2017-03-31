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
 * SeparationColorSpace.java
 * ---------------
 */

package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.jpedal.JDeliHelper;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.utils.LogWriter;

/**
 * handle Separation ColorSpace and some DeviceN functions
 */
public class SeparationColorSpace extends GenericColorSpace {

    protected GenericColorSpace altCS;

    protected ColorMapping colorMapper;

    float[] domain;

    public SeparationColorSpace() {
    }

    public SeparationColorSpace(final ColorMapping colorMapper, final float[] domain, final GenericColorSpace altCS) {

        this.colorMapper = colorMapper;
        this.domain = domain;
        this.altCS = altCS;

        componentCount = 1;

        setType(ColorSpaces.Separation);

    }

    /**
     * private method to do the calculation
     */
    private void setColor(final float value) {

        //adjust size if needed
        int elements = 1;

        if (domain != null) {
            elements = domain.length / 2;
        }

        final float[] values = new float[elements];
        for (int j = 0; j < elements; j++) {
            values[j] = value;
        }

        final float[] operand = colorMapper.getOperandFloat(values);

        altCS.setColor(operand, operand.length);

    }

    /**
     * set color (translate and set in alt colorspace
     */
    @Override
    public void setColor(final float[] operand, final int opCount) {

        setColor(operand[0]);

    }

    /**
     * set color (translate and set in alt colorspace
     */
    @Override
    public void setColor(final String[] operand, final int opCount) {

        final float[] f = new float[1];
        f[0] = Float.parseFloat(operand[0]);

        setColor(f, 1);

    }

    @Override
    public void invalidateCaching(final int color) {

        super.invalidateCaching(color);

        altCS.invalidateCaching(color);

        altCS.setColor(new PdfColor(color));
    }

    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEGToRGBImage(final byte[] data, final int ww, final int hh, final int pX, final int pY) {

        BufferedImage image;
        ByteArrayInputStream in = null;

        ImageReader iir = null;
        ImageInputStream iin = null;

        try {

            //read the image data
            in = new ByteArrayInputStream(data);

            //suggestion from Carol
            final Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("JPEG");

            while (iterator.hasNext()) {
                final ImageReader o = iterator.next();
                iir = o;
                if (iir.canReadRaster()) {
                    break;
                }
            }

            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            Raster ras = iir.readRaster(0, null);

            ras = cleanupRaster(ras, pX, pY, 1); //note uses 1 not count

            final int w = ras.getWidth();
            final int h = ras.getHeight();

            final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
            final byte[] rawData = rgb.getData();

            //special case
            if (this.altCS.getID() == ColorSpaces.DeviceGray) {

                for (int aa = 0; aa < rawData.length; aa++) {
                    rawData[aa] = (byte) (rawData[aa] ^ 255);
                }
                final int[] bands = {0};
                image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
                final Raster raster = Raster.createInterleavedRaster(new DataBufferByte(rawData, rawData.length), w, h, w, 1, bands, null);

                image.setData(raster);

            } else {
                //convert the image in general case
                image = createImage(w, h, rawData);
            }
        } catch (final Exception ee) {
            image = null;

            LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
        }

        try {
            in.close();
            iir.dispose();
            iin.close();
        } catch (final Exception ee) {

            LogWriter.writeLog("Problem closing  " + ee);
        }

        return image;

    }

    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEG2000ToRGBImage(final byte[] data, final int w, final int h, final int pX, final int pY, final int d) throws PdfException {

        BufferedImage image = null;

        try {
            final byte[] rawData = JDeliHelper.getUnconvertedBytesFromJPEG2000(data);

            if (rawData != null) {

                IndexedColorMap = null; //make index null as we already processed

                //convert the image
                if (getID() == ColorSpaces.DeviceN) {
                    image = createImageN(w, h, rawData);
                } else {
                    image = createImage(w, h, rawData);
                }
            }
        } catch (final Exception ee) {
            image = null;

            LogWriter.writeLog("Exception in JPEG2000ToRGBImage: " + ee);
        }


        return image;

    }

    private BufferedImage createImageN(final int w, final int h, final byte[] rawData) {

        final BufferedImage image;

        final byte[] rgb = new byte[w * h * 3];

        final int bytesCount = rawData.length;

        //convert data to RGB format
        final int byteCount;
        if (IndexedColorMap != null) {
            byteCount = rawData.length;
        } else {
            byteCount = rawData.length / componentCount;
        }

        final float[] values = new float[componentCount];

        int j = 0, j2 = 0, index;

        for (int i = 0; i < byteCount; i++) {

            if (j >= bytesCount) {
                break;
            }

            if (IndexedColorMap != null) {
                index = (rawData[i] & 255) * componentCount;

                for (int comp = 0; comp < componentCount; comp++) {
                    values[comp] = ((IndexedColorMap[index + comp] & 255) / 255f);
                }
            } else {
                for (int comp = 0; comp < componentCount; comp++) {
                    values[comp] = ((rawData[j] & 255) / 255f);
                    j++;
                }
            }

            setColor(values, componentCount);

            //set values
            final int foreground = altCS.currentColor.getRGB();

            rgb[j2] = (byte) ((foreground >> 16) & 0xFF);
            rgb[j2 + 1] = (byte) ((foreground >> 8) & 0xFF);
            rgb[j2 + 2] = (byte) ((foreground) & 0xFF);

            j2 += 3;

        }

        //create the RGB image
        final int[] bands = {0, 1, 2};
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final DataBuffer dataBuf = new DataBufferByte(rgb, rgb.length);
        final Raster raster = Raster.createInterleavedRaster(dataBuf, w, h, w * 3, 3, bands, null);
        image.setData(raster);

        return image;
    }

    /**
     * convert separation stream to RGB and return as an image
     */
    @Override
    public BufferedImage dataToRGB(final byte[] data, final int w, final int h) {

        BufferedImage image;

        try {

            //convert data
            image = createImage(w, h, data);

        } catch (final Exception ee) {
            image = null;

            LogWriter.writeLog("Couldn't convert Separation colorspace data: " + ee);
        }

        return image;

    }

    @Override
    public byte[] dataToRGBByteArray(final byte[] rgb, final int w, final int h) {
        final int pixelCount = 3 * w * h;
        final byte[] imageData = new byte[pixelCount];
        float[] operand;
        final int inpLen = domain.length / 2;

        if (inpLen == 1) {
            float last = -1, cur;
            int p = 0, pp = 0, tt;
            for (int i = 0, ii = w * h; i < ii; i++) {
                cur = (rgb[p++] & 0xff) / 255f;
                if (last == cur) {
                    tt = altCS.getColor().getRGB();
                } else {
                    operand = colorMapper.getOperandFloat(new float[]{cur});
                    altCS.setColor(operand, operand.length);
                    tt = altCS.getColor().getRGB();
                }
                imageData[pp++] = (byte) ((tt >> 16) & 0xff);
                imageData[pp++] = (byte) ((tt >> 8) & 0xff);
                imageData[pp++] = (byte) (tt & 0xff);
                last = cur;
            }
        } else {
            final float[] inputs = new float[inpLen];
            int p = 0, pp = 0, tt;
            for (int i = 0, ii = w * h; i < ii; i++) {
                for (int j = 0; j < inpLen; j++) {
                    inputs[j] = (rgb[p++] & 0xff) / 255f;
                }
                operand = colorMapper.getOperandFloat(inputs);
                altCS.setColor(operand, operand.length);
                tt = altCS.getColor().getRGB();
                imageData[pp++] = (byte) ((tt >> 16) & 0xff);
                imageData[pp++] = (byte) ((tt >> 8) & 0xff);
                imageData[pp++] = (byte) (tt & 0xff);
            }
        }
        return imageData;
    }

//    /**
//     * keeping this old case incase it is needed
//     * convert separation stream to RGB and return as an image
//     */
//    @Override
//    public byte[]  dataToRGBByteArray2(final byte[] rgb, final int w, final int h) {
//
//        final int pixelCount=3*w*h;
//        final byte[] imageData=new byte[pixelCount];
//        
//        //convert data to RGB format
//        int pixelReached=0;
//        
//        //cache table for speed
//        final float[][] lookuptable=new float[3][256];
//        for(int i=0;i<256;i++) {
//            lookuptable[0][i]=-1;
//        }
//        
//        for (final byte aRgb : rgb) {
//            
//            final int value = (aRgb & 255);
//            
//            if (lookuptable[0][value] == -1) {               
//                setColor(value / 255f);
//                
//                lookuptable[0][value] = ((Color) this.getColor()).getRed();
//                lookuptable[1][value] = ((Color) this.getColor()).getGreen();
//                lookuptable[2][value] = ((Color) this.getColor()).getBlue();
//                
//            }
//            
//            for (int comp = 0; comp < 3; comp++) {
//                imageData[pixelReached] = (byte) lookuptable[comp][value];
//                pixelReached++;
//            }
//        }
//        
//        return imageData;
//    }

    /**
     * turn raw data into an image
     */
    BufferedImage createImage(final int w, final int h, final byte[] rgb) {

        final BufferedImage image;

        final byte[] imageData = dataToRGBByteArray(rgb, w, h);

        //create the RGB image
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Raster raster = ColorSpaceConvertor.createInterleavedRaster(imageData, w, h);
        image.setData(raster);

        return image;
    }

    /**
     * create rgb index for color conversion
     */
    @Override
    public byte[] convertIndexToRGB(final byte[] data) {

        final byte[] newdata = new byte[3 * 256]; //converting to RGB so size known

        final int inpLen = domain.length / 2;

        final int palLen = data.length / inpLen;
        final float[] inputs = new float[inpLen];
        float[] operand;
        int p = 0, pp = 0, tt;

        for (int i = 0, ii = Math.min(256, palLen); i < ii; i++) {
            for (int j = 0; j < inpLen; j++) {
                inputs[j] = (data[p++] & 0xff) / 255f;
            }
            operand = colorMapper.getOperandFloat(inputs);
            altCS.setColor(operand, operand.length);
            tt = altCS.getColor().getRGB();
            newdata[pp++] = (byte) ((tt >> 16) & 0xff);
            newdata[pp++] = (byte) ((tt >> 8) & 0xff);
            newdata[pp++] = (byte) (tt & 0xff);
        }
        return newdata;
    }

    /**
     * get color
     */
    @Override
    public PdfPaint getColor() {

        return altCS.getColor();

    }
}
