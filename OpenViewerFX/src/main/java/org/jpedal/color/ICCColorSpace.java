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
 * ICCColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;


/**
 * handle ICCColorSpace
 */
public class ICCColorSpace
        extends GenericColorSpace {

    //cache values to speed up translation
    private final int[] a1, b1, c1;

    private float[] prevFloat;

    public ICCColorSpace(final PdfObject colorSpace) {

        //set cache to -1 as flag
        a1 = new int[256];
        b1 = new int[256];
        c1 = new int[256];

        for (int i = 0; i < 256; i++) {
            a1[i] = -1;
            b1[i] = -1;
            c1[i] = -1;

        }

        setType(ColorSpaces.ICC);
        cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        final byte[] icc_data = colorSpace.getDecodedStream();

        if (icc_data == null) {

            LogWriter.writeLog("Error in ICC data");

        } else {
            try {
                cs = new ICC_ColorSpace(ICC_Profile.getInstance(icc_data));
                type = cs.getType();
            } catch (final Exception e) {
                LogWriter.writeLog("[PDF] Problem " + e.getMessage() + " with ICC data ");

                failed = true;
            }
        }

        componentCount = cs.getNumComponents();
    }


    /**
     * set color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {

        //if(isCached)
        //	System.out.println("BsetColor "+size);

        final float[] colValues = new float[items];

        for (int ii = 0; ii < items; ii++) {
            colValues[ii] = Float.parseFloat(number_values[ii]);
        }

        setColor(colValues, items);
    }

//    private static boolean hasNegative(float[] arr){
//        for (int i = 0; i < arr.length; i++) {
//            if(arr[i]<0){
//                return true;
//            }
//        }
//        return false;
//    }

    private static boolean isSame(final float[] arr0, final float[] arr1) {
        if (arr0 == null || arr1 == null) {
            return false;
        }
        for (int i = 0; i < arr0.length; i++) {
            if (arr0[i] != arr1[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * set color
     */
    @Override
    public final void setColor(final float[] operand, final int size) {

        if (isSame(prevFloat, operand)) {
            //do nothing
        } else {
            final float[] result = cs.toRGB(operand);
            currentColor = new PdfColor(result[0], result[1], result[2]);
            prevFloat = operand.clone();
        }
    }

    /**
     * convert Index to RGB
     */
    @Override
    public byte[] convertIndexToRGB(final byte[] data) {

        isConverted = true;

        if (componentCount == 4) {
            return convert4Index(data);
        } else {
            return data;
        }

    }

    /**
     * <p>
     * Convert DCT encoded image bytestream to sRGB
     * </p>
     * <p>
     * It uses the internal Java classes and the Adobe icm to convert CMYK and
     * YCbCr-Alpha - the data is still DCT encoded.
     * </p>
     * <p>
     * The Sun class JPEGDecodeParam.java is worth examining because it contains
     * lots of interesting comments
     * </p>
     * <p>
     * I tried just using the new IOImage.read() but on type 3 images, all my
     * clipping code stopped working so I am still using 1.3
     * </p>
     */
    @Override
    public BufferedImage JPEGToRGBImage(
            final byte[] data, final int w, final int h, final int pX, final int pY) {

        if (data.length > 9 && data[6] == 'J' && data[7] == 'F' && data[8] == 'I' && data[9] == 'F') {
            return nonRGBJPEGToRGBImage(data, w, h, pX, pY);
        } else {
            return algorithmicICCToRGB(data, w, h, pX, pY);
        }

    }


    /**
     * convert byte[] datastream JPEG to an image in RGB
     *
     * @throws org.jpedal.exception.PdfException
     */
    @Override
    public BufferedImage JPEG2000ToRGBImage(final byte[] data, final int w, final int h,
                                            final int pX, final int pY, final int d) throws PdfException {

        final byte[] index = this.getIndexedMap();

        if (cs.getNumComponents() == 3 || index != null) {
            return super.JPEG2000ToRGBImage(data, w, h, pX, pY, d);
        } else {
            return JPEG2000ToImage(data, pX, pY);
        }
    }


    private BufferedImage algorithmicICCToRGB(
            byte[] data, int w, int h, final int pX, final int pY) {

        BufferedImage image = null;

        ImageReader iir = null;
        @SuppressWarnings("UnusedAssignment") ImageInputStream iin = null;

        final ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {

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
            iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));

            Raster ras = iir.readRaster(0, null);

            //some images need this
            if (iir.getRawImageType(0) == null || alternative == -1) {
                return nonRGBJPEGToRGBImage(data, w, h, pX, pY);
            }

            ras = cleanupRaster(ras, pX, pY, componentCount);
            w = ras.getWidth();
            h = ras.getHeight();

            final byte[] new_data = new byte[w * h * 3];

            //reuse variable
            data = ((DataBufferByte) ras.getDataBuffer()).getData();

            final int pixelCount = w * h * 3;
            float lastR = 0, lastG = 0, lastB = 0;
            int pixelReached = 0;
            float lastIn1 = -1, lastIn2 = -1, lastIn3 = -1;

            for (int i = 0; i < pixelCount; i += 3) {

                final float in1 = ((data[i] & 255)) / 255f;
                final float in2 = ((data[1 + i] & 255)) / 255f;
                final float in3 = ((data[2 + i] & 255)) / 255f;

                final float[] outputValues;
                if ((lastIn1 == in1) && (lastIn2 == in2) && (lastIn3 == in3)) {
                    //use existing values
                } else { //work out new

                    final float[] inputValues = {in1, in2, in3};

                    outputValues = cs.toRGB(inputValues);
                    //outputValues=inputValues;

                    //reset values
                    lastR = (outputValues[0] * 255);
                    lastG = (outputValues[1] * 255);
                    lastB = (outputValues[2] * 255);

                    lastIn1 = in1;
                    lastIn2 = in2;
                    lastIn3 = in3;
                }

                new_data[pixelReached++] = (byte) lastR;
                new_data[pixelReached++] = (byte) lastG;
                new_data[pixelReached++] = (byte) lastB;

            }

            final int[] bands = {0, 1, 2};

            final DataBuffer db = new DataBufferByte(new_data, new_data.length);
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            final Raster raster = Raster.createInterleavedRaster(db, w, h, w * 3, 3, bands, null);
            image.setData(raster);


        } catch (final Exception e) {

            LogWriter.writeLog("Problem with color conversion " + e);

        } finally {

            try {
                in.close();
                iir.dispose();
                iin.close();
            } catch (final Exception ee) {

                LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        return image;
    }
}
